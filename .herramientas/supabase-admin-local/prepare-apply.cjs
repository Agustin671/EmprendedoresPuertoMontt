const fs=require('fs');const path=require('path');const crypto=require('crypto');
const root=__dirname;const b=JSON.parse(fs.readFileSync(path.join(root,'backup-before.json'),'utf8'))[0].snapshot;
const rehearsal=JSON.parse(fs.readFileSync(path.join(root,'rehearsal-result.json'),'utf8'));if(!rehearsal.ok||!rehearsal.backupRoundtrip)throw new Error('Falta ensayo');
const files=['03_migracion_fotos.sql','04_registro_vendedores.sql','05_catalogo_ventas.sql'];
const parts=["begin; set local lock_timeout='5s'; set local statement_timeout='60s'; set local timezone='UTC';",
 'lock table auth.users, '+Object.keys(b.data).map(t=>'public."'+t+'"').join(',')+' in share row exclusive mode;'];
for(const [t,rows] of Object.entries(b.data))parts.push(`do $guard$ begin if
 (select coalesce(jsonb_agg(x order by x::text),'[]'::jsonb) from (select to_jsonb(t) x from public."${t}" t) s)
 is distinct from
 (select coalesce(jsonb_agg(x order by x::text),'[]'::jsonb) from jsonb_array_elements($snapshot$${JSON.stringify(rows)}$snapshot$::jsonb) x)
 then raise exception 'CAMBIO_DESDE_RESPALDO: ${t}'; end if; end $guard$;`);
const hashes={};for(const file of files){const s=fs.readFileSync(path.join(process.cwd(),'supabase',file),'utf8');hashes[file]=crypto.createHash('sha256').update(s).digest('hex');parts.push(s);}
parts.push("select 'MIGRACIONES_03_04_05_APLICADAS' as resultado, (select count(*) from public.productos) as productos, (select count(*) from public.ventas) as ventas, (select count(*) from public.tiendas) as tiendas; commit;");
fs.writeFileSync(path.join(root,'apply-reviewed.sql'),parts.join('\n'));fs.writeFileSync(path.join(root,'migration-hashes.json'),JSON.stringify(hashes,null,2));
console.log('Transacción preparada con comprobación de cambios desde el respaldo y tiempos máximos de bloqueo.');
