-- =====================================================================
-- Pruebas de reglas de seguridad por perfil (RNF-SEG-04).
-- Se corren en un Postgres LOCAL y desechable con auth/storage simulados
-- (supabase/stub_local.sql), NUNCA en la instancia del curso.
-- Requiere 01, 02, 03, 04 y 05 aplicados. SQL puro (sin comandos de psql):
-- lo ejecuta tests/sql/ejecutar.js y también funciona con psql.
-- Cada caso imprime OK o FALLA. Todo se deshace al final (rollback).
-- =====================================================================
begin;

-- Usuarios de prueba (el trigger crea perfiles y tiendas)
insert into auth.users (id, email, raw_user_meta_data) values
  ('00000000-0000-0000-0000-00000000000a', 'admin@alerce.app', '{}'),
  ('00000000-0000-0000-0000-00000000000b', 'donamarta@alerce.app', '{}'),
  ('00000000-0000-0000-0000-00000000000c', 'barberia@alerce.app', '{}'),
  ('00000000-0000-0000-0000-00000000000d', 'vecina@gmail.com', '{}'),
  ('00000000-0000-0000-0000-00000000000f', 'vendedora@gmail.com', '{"tipo_cuenta":"EMPRENDEDORA","nombre":"Tienda Nueva"}'),
  ('00000000-0000-0000-0000-000000000010', 'intento-admin@gmail.com', '{"tipo_cuenta":"ADMIN","nombre":"Cuenta de prueba"}');

create temp table resultados (caso text, ok boolean);
grant all on resultados to authenticated, anon;

-- Ejecuta una sentencia como un usuario y registra si se permitió o se rechazó
create or replace function pg_temp.probar(p_caso text, p_uid text, p_sql text, p_debe_pasar boolean)
returns void language plpgsql as $$
declare v_filas int; v_ok boolean;
begin
  begin
    if p_uid is null then
      execute 'set local role anon';
    else
      execute 'set local role authenticated';
    end if;
    perform set_config('request.jwt.claim.sub', coalesce(p_uid, ''), true);
    execute p_sql;
    get diagnostics v_filas = row_count;
    v_ok := (v_filas > 0) = p_debe_pasar;
    execute 'reset role';
  exception when others then
    execute 'reset role';
    v_ok := not p_debe_pasar;
  end;
  insert into resultados values (p_caso, v_ok);
end $$;

-- Perfiles y tiendas creados por el trigger (RN-01)
insert into resultados select 'Admin queda como ADMIN', rol = 'ADMIN' from perfiles where email = 'admin@alerce.app';
insert into resultados select 'Doña Marta queda como EMPRENDEDORA con su tienda', rol = 'EMPRENDEDORA' and tienda_id = 't_minimarket_rosa' from perfiles where email = 'donamarta@alerce.app';
insert into resultados select 'Correo no autorizado queda como VISITANTE', rol = 'VISITANTE' and tienda_id is null from perfiles where email = 'vecina@gmail.com';
insert into resultados select 'La tienda de Marta queda a su nombre', owner_id = '00000000-0000-0000-0000-00000000000b' from tiendas where id = 't_minimarket_rosa';
insert into resultados select 'Registro vendedor crea tienda propia', p.rol = 'EMPRENDEDORA' and t.owner_id = p.id
  from perfiles p join tiendas t on t.id = p.tienda_id where p.email = 'vendedora@gmail.com';
insert into resultados select 'Metadatos no pueden crear una cuenta ADMIN', rol = 'VISITANTE' and tienda_id is null
  from perfiles where email = 'intento-admin@gmail.com';

select pg_temp.probar('Marta edita su tienda', '00000000-0000-0000-0000-00000000000b', $$update tiendas set bio = 'Pan amasado' where id = 't_minimarket_rosa'$$, true);
select pg_temp.probar('Marta NO edita la tienda de otra', '00000000-0000-0000-0000-00000000000b', $$update tiendas set bio = 'x' where id = 't_barberia_alerce'$$, false);
select pg_temp.probar('Marta crea un producto en su tienda', '00000000-0000-0000-0000-00000000000b', $$insert into productos (id, tienda_id, nombre) values ('pm1', 't_minimarket_rosa', 'Queque')$$, true);
select pg_temp.probar('Marta NO crea productos en otra tienda', '00000000-0000-0000-0000-00000000000b', $$insert into productos (id, tienda_id, nombre) values ('pm2', 't_barberia_alerce', 'X')$$, false);
select pg_temp.probar('Marta ve su producto no publicado', '00000000-0000-0000-0000-00000000000b', $$select 1 from productos where id = 'pm1'$$, true);
select pg_temp.probar('Visitante NO ve productos no publicados', '00000000-0000-0000-0000-00000000000d', $$select 1 from productos where id = 'pm1'$$, false);
select pg_temp.probar('Visitante ve productos publicados', '00000000-0000-0000-0000-00000000000d', $$select 1 from productos where publicado$$, true);
select pg_temp.probar('Visitante NO crea productos', '00000000-0000-0000-0000-00000000000d', $$insert into productos (id, tienda_id, nombre) values ('pv', 't_minimarket_rosa', 'X')$$, false);
select pg_temp.probar('Vendedora nueva crea producto en su tienda', '00000000-0000-0000-0000-00000000000f', $$insert into productos (id, tienda_id, nombre) select 'ps1', 'tienda_' || replace(auth.uid()::text, '-', ''), 'Mi producto'$$, true);
select pg_temp.probar('Vendedora nueva NO crea producto en otra tienda', '00000000-0000-0000-0000-00000000000f', $$insert into productos (id, tienda_id, nombre) values ('ps2', 't_minimarket_rosa', 'X')$$, false);
select pg_temp.probar('Sin sesión se ven las tiendas', null, $$select 1 from tiendas$$, true);
select pg_temp.probar('Sin sesión NO se modifica nada', null, $$update tiendas set bio = 'x'$$, false);
select pg_temp.probar('Marta registra una venta con registrar_venta', '00000000-0000-0000-0000-00000000000b',
  $$select public.registrar_venta('{"id":"v1","items":[{"productoId":"p_mini_1","varianteId":"v_min1","cantidad":1}]}'::jsonb)$$, true);
select pg_temp.probar('Marta NO inserta ventas directamente (solo registrar_venta)', '00000000-0000-0000-0000-00000000000b', $$insert into ventas (id, tienda_id, total) values ('v_directa', 't_minimarket_rosa', 2500)$$, false);
select pg_temp.probar('Marta ve su historial de ventas', '00000000-0000-0000-0000-00000000000b', $$select 1 from ventas$$, true);
select pg_temp.probar('El admin NO ve ventas (RN-10)', '00000000-0000-0000-0000-00000000000a', $$select 1 from ventas$$, false);
select pg_temp.probar('Sin sesión NO se ven ventas', null, $$select 1 from ventas$$, false);
select pg_temp.probar('El admin autoriza una emprendedora', '00000000-0000-0000-0000-00000000000a', $$insert into cuentas_autorizadas (email, rol, nombre) values ('nueva@alerce.app', 'EMPRENDEDORA', 'Nueva')$$, true);
select pg_temp.probar('Marta NO autoriza cuentas', '00000000-0000-0000-0000-00000000000b', $$insert into cuentas_autorizadas (email, rol) values ('otra@alerce.app', 'ADMIN')$$, false);
select pg_temp.probar('Visitante NO se cambia el rol', '00000000-0000-0000-0000-00000000000d', $$update perfiles set rol = 'ADMIN' where id = auth.uid()$$, false);
select pg_temp.probar('El admin crea una feria', '00000000-0000-0000-0000-00000000000a', $$insert into ferias (id, nombre) values ('f1', 'Feria de la Plaza')$$, true);
select pg_temp.probar('Marta NO crea ferias (RN-02)', '00000000-0000-0000-0000-00000000000b', $$insert into ferias (id, nombre) values ('f2', 'X')$$, false);
select pg_temp.probar('Marta sube foto en su carpeta', '00000000-0000-0000-0000-00000000000b', $$insert into storage.objects (bucket_id, name) values ('productos', '00000000-0000-0000-0000-00000000000b/pm1.jpg')$$, true);
select pg_temp.probar('Marta NO sube foto en carpeta ajena', '00000000-0000-0000-0000-00000000000b', $$insert into storage.objects (bucket_id, name) values ('productos', '00000000-0000-0000-0000-00000000000c/x.jpg')$$, false);
select pg_temp.probar('Marta NO mueve su foto a una carpeta ajena', '00000000-0000-0000-0000-00000000000b', $$update storage.objects set name = '00000000-0000-0000-0000-00000000000c/robada.jpg' where name = '00000000-0000-0000-0000-00000000000b/pm1.jpg'$$, false);
select pg_temp.probar('Sin sesión NO se suben fotos', null, $$insert into storage.objects (bucket_id, name) values ('productos', 'anon/x.jpg')$$, false);

-- Fotos de perfil y logo del negocio
select pg_temp.probar('Marta cambia su foto con la función', '00000000-0000-0000-0000-00000000000b', $$select public.actualizar_mi_perfil(null, 'https://ejemplo.cl/fotos/marta.jpg')$$, true);
insert into resultados select 'La foto de Marta quedó guardada', foto_url = 'https://ejemplo.cl/fotos/marta.jpg' from perfiles where email = 'donamarta@alerce.app';
select pg_temp.probar('Marta NO cambia su perfil directamente', '00000000-0000-0000-0000-00000000000b', $$update perfiles set nombre = 'X' where id = auth.uid()$$, false);
select pg_temp.probar('Compradora cambia su foto con la función', '00000000-0000-0000-0000-00000000000d', $$select public.actualizar_mi_perfil(null, 'https://ejemplo.cl/fotos/vecina.jpg')$$, true);
insert into resultados select 'La función NO cambió el rol de Marta', rol = 'EMPRENDEDORA' from perfiles where email = 'donamarta@alerce.app';
insert into resultados select 'La función NO cambió la foto de otra persona', foto_url is null from perfiles where email = 'barberia@alerce.app';
select pg_temp.probar('Marta sube su foto de perfil a su carpeta', '00000000-0000-0000-0000-00000000000b', $$insert into storage.objects (bucket_id, name) values ('perfiles', '00000000-0000-0000-0000-00000000000b/perfil.jpg')$$, true);
select pg_temp.probar('Marta NO sube foto de perfil a carpeta ajena', '00000000-0000-0000-0000-00000000000b', $$insert into storage.objects (bucket_id, name) values ('perfiles', '00000000-0000-0000-0000-00000000000c/perfil.jpg')$$, false);
select pg_temp.probar('Marta cambia el logo de su tienda', '00000000-0000-0000-0000-00000000000b', $$update tiendas set logo_url = 'https://ejemplo.cl/fotos/logo.jpg' where id = 't_minimarket_rosa'$$, true);
insert into resultados select 'El logo de Marta quedó guardado', logo_url = 'https://ejemplo.cl/fotos/logo.jpg' from tiendas where id = 't_minimarket_rosa';
select pg_temp.probar('Marta NO cambia el logo de otra tienda', '00000000-0000-0000-0000-00000000000b', $$update tiendas set logo_url = 'https://ejemplo.cl/fotos/x.jpg' where id = 't_barberia_alerce'$$, false);

-- Compradora existente abre su tienda (04)
select pg_temp.probar('Compradora abre su tienda con crear_mi_tienda', '00000000-0000-0000-0000-00000000000d', $$select public.crear_mi_tienda('Almacén Vecina')$$, true);
insert into resultados select 'La compradora quedó como EMPRENDEDORA con tienda propia', p.rol = 'EMPRENDEDORA' and t.owner_id = p.id
  from perfiles p join tiendas t on t.id = p.tienda_id where p.email = 'vecina@gmail.com';

-- Emprendedora nueva autorizada por el admin: al registrarse obtiene tienda propia
insert into auth.users (id, email) values ('00000000-0000-0000-0000-00000000000e', 'nueva@alerce.app');
insert into resultados select 'Emprendedora nueva recibe tienda propia', p.rol = 'EMPRENDEDORA' and t.owner_id = p.id
  from perfiles p join tiendas t on t.id = p.tienda_id where p.email = 'nueva@alerce.app';

select case when ok then 'OK    ' else 'FALLA ' end || caso as resultado from resultados;
select count(*) filter (where ok) || ' de ' || count(*) || ' casos correctos' as resumen from resultados;
rollback;
