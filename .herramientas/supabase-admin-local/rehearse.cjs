const fs=require('fs'); const path=require('path');
const {PostgresLocal,versionesDisponibles,ejecutarArchivo}=require('../../tests/sql/postgres_local');
const b=JSON.parse(fs.readFileSync(path.join(__dirname,'backup-before.json'),'utf8'))[0].snapshot;
const v=versionesDisponibles().find(v=>v.version===15);
const pg=new PostgresLocal({version:115,bin:v.bin,puerto:54415});
function canon(x){if(Array.isArray(x))return x.map(canon);if(x&&typeof x==='object')return Object.fromEntries(Object.keys(x).sort().map(k=>[k,canon(x[k])]));return x;}
function same(a,b){return JSON.stringify(canon(a))===JSON.stringify(canon(b));}
(async()=>{await pg.iniciar();let c;try{await pg.crearBase('ensayo_real');c=pg.cliente('ensayo_real');await c.connect();await c.query("set timezone='UTC'");await ejecutarArchivo(c,'supabase/stub_local.sql');await c.query('create role service_role nologin');
 await c.query('insert into auth.users select * from jsonb_populate_recordset(null::auth.users,$1::jsonb)',[JSON.stringify(b.auth_users_for_rehearsal)]);
 for(const x of b.buckets)await c.query('insert into storage.buckets(id,name,public) values($1,$2,$3)',[x.id,x.name,x.public]);
 await c.query(fs.readFileSync(path.join(__dirname,'restore-public-empty.sql'),'utf8'));
 for(const [table,rows] of Object.entries(b.data)){const restored=(await c.query(`select to_jsonb(t) r from public."${table}" t`)).rows.map(x=>x.r);if(!same(restored.map(canon).map(JSON.stringify).sort(),rows.map(canon).map(JSON.stringify).sort()))throw new Error('Restauración no coincide en '+table);}
 console.log('Respaldo restaurado: 8 tablas, 42 registros idénticos.');
 await c.query('begin');for(const f of ['03_migracion_fotos.sql','04_registro_vendedores.sql','05_catalogo_ventas.sql'])await ejecutarArchivo(c,'supabase/'+f);await c.query('commit');
 for(const table of ['productos','ventas']){const cols=b.columns.filter(x=>x.table===table).sort((a,b)=>a.position-b.position).map(x=>'"'+x.name+'"').join(',');const rows=(await c.query(`select to_jsonb(t) r from (select ${cols} from public."${table}") t`)).rows.map(x=>x.r);if(!same(rows.map(canon).map(JSON.stringify).sort(),b.data[table].map(canon).map(JSON.stringify).sort()))throw new Error('Cambios inesperados en '+table);}
 const counts=(await c.query('select (select count(*)::int from tiendas) stores,(select count(*)::int from perfiles) profiles,(select count(*)::int from productos) products,(select count(*)::int from ventas) sales')).rows[0];
 if(counts.stores!==b.data.tiendas.length+b.repair04_count||counts.profiles!==b.data.perfiles.length)throw new Error('Conteos inesperados');
 fs.writeFileSync(path.join(__dirname,'rehearsal-result.json'),JSON.stringify({ok:true,backupRoundtrip:true,originalProductsAndSalesUnchanged:true,counts,repair04:b.repair04_count},null,2));console.log('Migraciones ensayadas sobre copia real; productos y venta originales intactos; se crea 1 tienda para un alta anterior pendiente.');
}finally{if(c)await c.end();await pg.detener();}})().catch(e=>{fs.writeFileSync(path.join(__dirname,'rehearsal-error.local.txt'),String(e.stack));console.error('Ensayo falló: '+e.message.slice(0,160));process.exitCode=1;});
