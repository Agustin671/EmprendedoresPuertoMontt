const fs=require('fs'); const path=require('path'); const crypto=require('crypto');
const {root,credentials}=require('./api.cjs');
const b=JSON.parse(fs.readFileSync(path.join(root,'backup-before.json'),'utf8'))[0].snapshot;
const qi=s=>'"'+s.replaceAll('"','""')+'"'; const qs=s=>"'"+s.replaceAll("'","''")+"'";
let sql=['-- Respaldo lógico de public. Restaurar solo en un esquema vacío y revisado.','-- Requiere auth.users y storage existentes; NO recrea Auth ni sustituye un respaldo del host.','begin;','set local search_path=public,pg_catalog;','set local standard_conforming_strings=on;'];
for(const t of b.tables){const cols=b.columns.filter(c=>c.table===t.name).sort((a,b)=>a.position-b.position);sql.push(`create table public.${qi(t.name)} (${cols.map(c=>`${qi(c.name)} ${c.type}${c.default?' default '+c.default:''}${c.notnull?' not null':''}`).join(',\n')});`);}
for(const c of b.constraints.filter(c=>c.type!=='f'))sql.push(`alter table public.${qi(c.table)} add constraint ${qi(c.name)} ${c.definition};`);
for(const [name,rows] of Object.entries(b.data))sql.push(`insert into public.${qi(name)} select * from jsonb_populate_recordset(null::public.${qi(name)},${qs(JSON.stringify(rows))}::jsonb);`);
for(const c of b.constraints.filter(c=>c.type==='f'))sql.push(`alter table public.${qi(c.table)} add constraint ${qi(c.name)} ${c.definition};`);
for(const f of b.functions)sql.push(f.definition+';');
for(const i of b.indexes.filter(i=>!b.constraints.some(c=>c.table===i.tablename&&c.name===i.indexname)))sql.push(i.indexdef+';');
for(const t of b.triggers)sql.push(`drop trigger if exists ${qi(t.name)} on ${qi(t.schema)}.${qi(t.table)};`, t.definition+';');
for(const t of b.tables)if(t.rls)sql.push(`alter table public.${qi(t.name)} enable row level security;`);
for(const p of b.policies){sql.push(`drop policy if exists ${qi(p.policyname)} on ${qi(p.schemaname)}.${qi(p.tablename)};`,`create policy ${qi(p.policyname)} on ${qi(p.schemaname)}.${qi(p.tablename)} as ${p.permissive} for ${p.cmd} to ${p.roles.map(r=>r==='public'?'public':qi(r)).join(',')}${p.qual?' using ('+p.qual+')':''}${p.with_check?' with check ('+p.with_check+')':''};`);}
// Los propietarios y ACL originales quedan en backup-before.json. No se reproducen
// roles de infraestructura en una restauración local aislada.
for(const g of b.grants.filter(g=>['anon','authenticated','service_role'].includes(g.grantee)))sql.push(`grant ${g.privilege_type} on public.${qi(g.table_name)} to ${qi(g.grantee)}${g.is_grantable==='YES'?' with grant option':''};`);
sql.push('commit;');fs.writeFileSync(path.join(root,'restore-public-empty.sql'),sql.join('\n'));
(async()=>{const manifest=[];for(const o of b.objects){const rel=o.bucket_id+'/'+o.name;const response=await fetch(credentials.url+'/storage/v1/object/public/'+rel.split('/').map(encodeURIComponent).join('/'),{headers:{apikey:credentials.anon},signal:AbortSignal.timeout(20000)});if(!response.ok)throw new Error('No se pudo respaldar una foto: HTTP '+response.status);const content=Buffer.from(await response.arrayBuffer());const filename='storage-'+crypto.createHash('sha256').update(rel).digest('hex')+'.bin';fs.writeFileSync(path.join(root,filename),content);manifest.push({bucket:o.bucket_id,name:o.name,file:filename,bytes:content.length,sha256:crypto.createHash('sha256').update(content).digest('hex')});}fs.writeFileSync(path.join(root,'storage-manifest.json'),JSON.stringify(manifest,null,2));console.log(JSON.stringify({tables:b.tables.length,rows:Object.values(b.data).reduce((n,r)=>n+r.length,0),files:manifest.length,repair04:b.repair04_count}));})().catch(e=>{console.error(e.message);process.exitCode=1});
