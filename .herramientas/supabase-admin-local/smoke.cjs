const fs=require('fs');const path=require('path');const crypto=require('crypto');
const {credentials:c,query,root}=require('./api.cjs');
const tag='revision-'+crypto.randomBytes(5).toString('hex');const accounts=[];const images=[];const results=[];
const qi=s=>"'"+s.replaceAll("'","''")+"'";
function check(name,ok){results.push({name,ok:!!ok});console.log((ok?'OK ':'FALLA ')+name);if(!ok)throw new Error(name);}
async function api(route,{method='GET',body,token,raw,headers={}}={}){
 const r=await fetch(c.url+route,{method,headers:{apikey:c.anon,Authorization:'Bearer '+(token||c.anon),...(raw?{}:{'Content-Type':'application/json'}),...headers},body:raw|| (body===undefined?undefined:JSON.stringify(body)),signal:AbortSignal.timeout(20000)});
 const t=await r.text();let data;try{data=JSON.parse(t)}catch{data=t}return{status:r.status,ok:r.ok,data};
}
async function rpc(name,body,token){return api('/rest/v1/rpc/'+name,{method:'POST',body,token});}
async function signUp(role){const email=tag+'-'+accounts.length+'@example.com';const password=crypto.randomBytes(20).toString('base64url')+'aA1!';const r=await api('/auth/v1/signup',{method:'POST',body:{email,password,data:{nombre:'Prueba temporal '+tag,tipo_cuenta:role}}});if(!r.ok||!r.data.user?.id){fs.writeFileSync(path.join(root,'smoke-error.local.json'),JSON.stringify(r));throw new Error('Registro temporal falló HTTP '+r.status);}const a={id:r.data.user.id,email,password,token:r.data.access_token};accounts.push(a);fs.writeFileSync(path.join(root,'smoke-accounts.local.json'),JSON.stringify(accounts));check('Registro '+role+' con confirmación automática',!!a.token);return a;}
async function getProduct(id,token){return api('/rest/v1/productos?id=eq.'+encodeURIComponent(id)+'&select=*',{token});}
const image=Buffer.from('iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAwMCAO+aR1cAAAAASUVORK5CYII=','base64');
let originalError;
(async()=>{try{
 const a=await signUp('EMPRENDEDORA'),b=await signUp('EMPRENDEDORA'),visitor=await signUp('ADMIN');
 const login=await api('/auth/v1/token?grant_type=password',{method:'POST',body:{email:a.email,password:a.password}});check('Inicio de sesión con correo y contraseña',login.ok&&!!login.data.access_token);
 const profile=(await api('/rest/v1/perfiles?id=eq.'+a.id+'&select=rol,tienda_id',{token:a.token})).data[0];check('Perfil emprendedora y tienda creados',profile?.rol==='EMPRENDEDORA'&&!!profile.tienda_id);
 const vp=(await api('/rest/v1/perfiles?id=eq.'+visitor.id+'&select=rol',{token:visitor.token})).data[0];check('Metadatos no permiten obtener ADMIN',vp?.rol==='VISITANTE');
 const urls={};for(const bucket of ['productos','perfiles']){const key=a.id+'/'+tag+'.png';const up=await api('/storage/v1/object/'+bucket+'/'+key,{method:'POST',raw:image,headers:{'Content-Type':'image/png'},token:a.token});check('Subir imagen a '+bucket,up.ok);images.push({bucket,key,token:a.token});urls[bucket]=c.url+'/storage/v1/object/public/'+bucket+'/'+key;const pub=await fetch(urls[bucket],{signal:AbortSignal.timeout(15000)});check('Leer imagen pública de '+bucket,pub.ok&&Buffer.from(await pub.arrayBuffer()).equals(image));}
 check('Guardar foto del perfil',(await rpc('actualizar_mi_perfil',{p_nombre:'Prueba temporal '+tag,p_foto_url:urls.perfiles},a.token)).ok);
 check('Guardar logo de la tienda',(await api('/rest/v1/tiendas?id=eq.'+profile.tienda_id,{method:'PATCH',body:{logo_url:urls.perfiles},token:a.token,headers:{Prefer:'return=representation'}})).ok);
 const p={id:tag+'-opciones',nombre:'Prueba temporal opciones',descripcion:'Se elimina al terminar la verificación',categoria:'TEXTIL',precio:5000,publicado:true,disponible:true,modalidad:'REGULAR',manejaStock:true,alAgotarse:'MOSTRAR_AGOTADO',fotoUrl:urls.productos,variantes:[{id:'azul',nombre:'Azul',stock:3,precio:5000,fotoUrl:urls.productos},{id:'rojo',nombre:'Rojo',stock:2,precio:7000,fotoUrl:urls.productos}]};
 const save=await rpc('guardar_producto',{p},a.token);check('Crear producto con opciones reales',save.ok&&save.data.variantes.length===2);
 const publicP=(await getProduct(p.id)).data[0];check('Catálogo anónimo con precio y foto por opción',publicP?.variantes[1]?.precio===7000&&publicP.variantes[1].foto_url===urls.productos);
 const foreign=await rpc('guardar_producto',{p:{...p,nombre:'No permitido'}},b.token);check('Otra cuenta no puede editar el producto',!foreign.ok);
 const sale={id:tag+'-venta',items:[{productoId:p.id,varianteId:'azul',cantidad:1}],medioPago:'EFECTIVO',lugarTipo:'OTRO',lugarNombre:'Prueba temporal',fechaLocal:'2026-11-11 12:00'};
 const first=await rpc('registrar_venta',{p_venta:sale},a.token);check('Venta registrada y total correcto',first.ok&&first.data.total===5000);
 const replay=await rpc('registrar_venta',{p_venta:sale},a.token);check('Reintento no duplica venta',replay.ok&&replay.data.estado==='YA_REGISTRADA');
 const after=(await getProduct(p.id,a.token)).data[0];check('Descuento solo en opción vendida',after.variantes[0].stock===2&&after.variantes[1].stock===2);
 const tooMuch=await rpc('registrar_venta',{p_venta:{...sale,id:tag+'-exceso',items:[{productoId:p.id,varianteId:'azul',cantidad:99}]}},a.token);check('Sobreventa rechazada',!tooMuch.ok&&String(tooMuch.data.message).includes('STOCK_INSUFICIENTE'));
 const ownSales=await api('/rest/v1/ventas?id=eq.'+sale.id+'&select=*',{token:a.token});check('Historial con una venta, precio y lugar',ownSales.ok&&ownSales.data.length===1&&ownSales.data[0].lugar_nombre==='Prueba temporal');
 const foreignSales=await api('/rest/v1/ventas?id=eq.'+sale.id+'&select=id',{token:b.token});check('Historial privado entre cuentas',foreignSales.ok&&foreignSales.data.length===0);
 const direct=await api('/rest/v1/ventas',{method:'POST',token:a.token,body:{id:tag+'-directa',tienda_id:profile.tienda_id,items:[],total:1}});check('Venta directa fuera de RPC rechazada',!direct.ok);
 for(const [suffix,mode,onZero,visible] of [['agotado','REGULAR','MOSTRAR_AGOTADO',true],['ocultar','REGULAR','OCULTAR',false],['pedido','REGULAR','PASAR_A_PEDIDO',true],['unica','PIEZA_UNICA','OCULTAR',false]]){const id=tag+'-'+suffix;check('Preparar '+suffix,(await rpc('guardar_producto',{p:{...p,id,modalidad:mode,alAgotarse:onZero,variantes:[{id:'u',nombre:'Unidad',stock:1,precio:5000}]}},a.token)).ok);check('Vender última unidad '+suffix,(await rpc('registrar_venta',{p_venta:{...sale,id:tag+'-v-'+suffix,items:[{productoId:id,varianteId:'u',cantidad:1}]}},a.token)).ok);const read=await getProduct(id);check('Visibilidad pública '+suffix,read.ok&&(read.data.length===1)===visible);if(suffix==='unica')check('Pieza única permanece oculta',(await getProduct(id)).data.length===0);}
 check('Cambiar disponibilidad',(await api('/rest/v1/productos?id=eq.'+p.id,{method:'PATCH',token:a.token,body:{disponible:false}})).ok);check('No disponible oculto al visitante',(await getProduct(p.id)).data.length===0);check('Dueña conserva producto no disponible',(await getProduct(p.id,a.token)).data.length===1);
 const edited=await rpc('guardar_producto',{p:{...p,nombre:'Prueba temporal editada',precio:6000,variantes:after.variantes.map(v=>({id:v.id,nombre:v.nombre,stock:v.stock,stockBase:v.stock,precio:v.precio,fotoUrl:v.foto_url}))}},a.token);check('Editar conservando cantidades',edited.ok);check('Recarga anónima muestra edición',(await getProduct(p.id)).data[0]?.nombre==='Prueba temporal editada');
 const forbidden=await api('/storage/v1/object/productos/'+a.id+'/'+tag+'-ajeno.png',{method:'POST',raw:image,token:b.token,headers:{'Content-Type':'image/png'}});check('Storage rechaza carpeta ajena',!forbidden.ok);
}catch(e){originalError=e;console.error('PRUEBA FALLÓ: '+e.message);}finally{
 for(const i of images){const r=await api('/storage/v1/object/'+i.bucket,{method:'DELETE',token:i.token,body:{prefixes:[i.key]}});if(!r.ok)throw new Error('No se pudo limpiar una imagen temporal');}
 if(accounts.length){const ids=accounts.map(a=>qi(a.id)).join(',');await query(`begin; delete from public.ventas where tienda_id in(select id from public.tiendas where owner_id in(${ids})); delete from public.productos where tienda_id in(select id from public.tiendas where owner_id in(${ids})); delete from public.perfiles where id in(${ids}); delete from public.tiendas where owner_id in(${ids}); delete from auth.users where id in(${ids}); commit; select 'TEMPORALES_ELIMINADOS' as resultado;`);}
 fs.writeFileSync(path.join(root,'smoke-results.json'),JSON.stringify({ok:!originalError,results,temporaryAccountsRemoved:accounts.length,temporaryImagesRemoved:images.length},null,2));console.log('Limpieza de cuentas, productos, ventas y fotos temporales terminada.');
 if(originalError)process.exitCode=1;
}})().catch(e=>{console.error('ERROR: '+e.message);process.exitCode=1});
