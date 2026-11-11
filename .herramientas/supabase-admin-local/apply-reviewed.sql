begin; set local lock_timeout='5s'; set local statement_timeout='60s'; set local timezone='UTC';
lock table auth.users, public."ferias",public."ventas",public."tiendas",public."perfiles",public."productos",public."asociaciones",public."participaciones",public."cuentas_autorizadas" in share row exclusive mode;
do $guard$ begin if
 (select coalesce(jsonb_agg(x order by x::text),'[]'::jsonb) from (select to_jsonb(t) x from public."ferias" t) s)
 is distinct from
 (select coalesce(jsonb_agg(x order by x::text),'[]'::jsonb) from jsonb_array_elements($snapshot$[]$snapshot$::jsonb) x)
 then raise exception 'CAMBIO_DESDE_RESPALDO: ferias'; end if; end $guard$;
do $guard$ begin if
 (select coalesce(jsonb_agg(x order by x::text),'[]'::jsonb) from (select to_jsonb(t) x from public."ventas" t) s)
 is distinct from
 (select coalesce(jsonb_agg(x order by x::text),'[]'::jsonb) from jsonb_array_elements($snapshot$[{"id":"v_test_1793751871719","fecha":"2026-11-04T00:24:31.797241+00:00","items":[{"id":"p_mini_1","nombre":"Pan Amasado Caliente","precio":2500,"cantidad":2}],"total":5000,"feria_id":null,"tienda_id":"t_minimarket_rosa","medio_pago":"Efectivo","fecha_local":"2026-11-04T00:24:31.719Z"}]$snapshot$::jsonb) x)
 then raise exception 'CAMBIO_DESDE_RESPALDO: ventas'; end if; end $guard$;
do $guard$ begin if
 (select coalesce(jsonb_agg(x order by x::text),'[]'::jsonb) from (select to_jsonb(t) x from public."tiendas" t) s)
 is distinct from
 (select coalesce(jsonb_agg(x order by x::text),'[]'::jsonb) from jsonb_array_elements($snapshot$[{"id":"t_minimarket_rosa","bio":"Frutas y verduras frescas, pan amasado caliente todos los días a las 17:00 hrs, abarrotes, cecinas y bebidas.","rubro":"Abarrotes y Alimentos","nombre":"Minimarket Doña Marta","sector":"Alerce Histórico","owner_id":"b9ceb90c-4afb-4230-b09f-71d49c9ad97c","whatsapp":"+56991234567","instagram":"@minimarket.donamarta","color_logo":4292700960,"necesidades":{"tieneStock":true,"participaEnFerias":false,"tieneAgendamiento":false,"tieneForoComunidad":true,"tieneProductosAPedido":false,"tieneVentaPorWhatsApp":true},"color_portada":4286601941,"actualizado_en":"2026-11-02T13:38:53.543493+00:00","mostrar_precios":true},{"id":"t_barberia_alerce","bio":"Cortes de cabello clásicos y modernos, perfilado de barba y tratamientos capilares. Atención personalizada en local y a domicilio.","rubro":"Servicios y Peluquería","nombre":"Barbería & Estilo Alerce","sector":"Alerce Norte","owner_id":"8fe7410f-3d87-4b27-80f3-9f299067619b","whatsapp":"+56987654321","instagram":"@estilo_alerce","color_logo":4281440149,"necesidades":{"tieneStock":false,"participaEnFerias":false,"tieneAgendamiento":true,"tieneForoComunidad":true,"tieneProductosAPedido":false,"tieneVentaPorWhatsApp":true},"color_portada":4281153352,"actualizado_en":"2026-11-02T13:38:53.543493+00:00","mostrar_precios":true},{"id":"t_tejidos_austral","bio":"Piezas únicas tejidas a palillo y telar con lana 100% natural de oveja chilota, teñida con tintes de cortezas nativas.","rubro":"Artesanía y Textil","nombre":"Tejidos & Lanares Alerce Austral","sector":"Alerce Sur","owner_id":"23a6384f-f36e-47df-8f38-fad1f24627fd","whatsapp":"+56976543210","instagram":"@alerceaustral.tejidos","color_logo":4281236827,"necesidades":{"tieneStock":true,"participaEnFerias":true,"tieneAgendamiento":false,"tieneForoComunidad":true,"tieneProductosAPedido":true,"tieneVentaPorWhatsApp":true},"color_portada":4284232027,"actualizado_en":"2026-11-02T13:38:53.543493+00:00","mostrar_precios":true},{"id":"t_reposteria_surena","bio":"Kuchenes tradicionales de murta y frambuesa, tortas de hojarasca a pedido, alfajores caseros y galletas horneadas con amor.","rubro":"Pastelería y Gastronomía","nombre":"Repostería Casera Tradición Sureña","sector":"Senda Central","owner_id":"35aa6cc6-64bc-4ad8-be57-611201ea1adb","whatsapp":"+56965432109","instagram":"@tradicion_surena","color_logo":4291113008,"necesidades":{"tieneStock":true,"participaEnFerias":true,"tieneAgendamiento":false,"tieneForoComunidad":true,"tieneProductosAPedido":true,"tieneVentaPorWhatsApp":true},"color_portada":4292255278,"actualizado_en":"2026-11-02T13:38:53.543493+00:00","mostrar_precios":true}]$snapshot$::jsonb) x)
 then raise exception 'CAMBIO_DESDE_RESPALDO: tiendas'; end if; end $guard$;
do $guard$ begin if
 (select coalesce(jsonb_agg(x order by x::text),'[]'::jsonb) from (select to_jsonb(t) x from public."perfiles" t) s)
 is distinct from
 (select coalesce(jsonb_agg(x order by x::text),'[]'::jsonb) from jsonb_array_elements($snapshot$[{"id":"30bc9911-a2d7-4115-a87b-8141621b1a79","rol":"VISITANTE","email":"donarosa@alerce.app","nombre":"Minimarket Doña Rosa","creado_en":"2026-11-04T00:10:02.098034+00:00","tienda_id":null},{"id":"b9ceb90c-4afb-4230-b09f-71d49c9ad97c","rol":"EMPRENDEDORA","email":"donamarta@alerce.app","nombre":"Doña Marta","creado_en":"2026-11-04T00:24:10.354535+00:00","tienda_id":"t_minimarket_rosa"},{"id":"8fe7410f-3d87-4b27-80f3-9f299067619b","rol":"EMPRENDEDORA","email":"barberia@alerce.app","nombre":"Barbería Alerce","creado_en":"2026-11-04T00:24:40.98455+00:00","tienda_id":"t_barberia_alerce"},{"id":"23a6384f-f36e-47df-8f38-fad1f24627fd","rol":"EMPRENDEDORA","email":"tejidos@alerce.app","nombre":"Tejidos Austral","creado_en":"2026-11-04T00:24:40.997334+00:00","tienda_id":"t_tejidos_austral"},{"id":"35aa6cc6-64bc-4ad8-be57-611201ea1adb","rol":"EMPRENDEDORA","email":"reposteria@alerce.app","nombre":"Repostería Sureña","creado_en":"2026-11-04T00:24:41.009203+00:00","tienda_id":"t_reposteria_surena"},{"id":"86272071-f5f7-46be-a75e-49d4b3a64c80","rol":"ADMIN","email":"admin@alerce.app","nombre":"Administración Alerce","creado_en":"2026-11-04T00:24:41.011892+00:00","tienda_id":null},{"id":"5f0aeb32-6c95-4ba9-b8ec-149074cac33b","rol":"VISITANTE","email":"agustin@gmail.com","nombre":"Punto 3D","creado_en":"2026-11-04T01:33:37.310711+00:00","tienda_id":null},{"id":"30deddb9-5ed7-4e46-9fd1-fba0afc41f80","rol":"VISITANTE","email":"botanica123@gmail.com","nombre":"Botanica","creado_en":"2026-11-04T01:50:23.775336+00:00","tienda_id":null},{"id":"80b94be6-e404-4e86-8fd2-3cfc41d25406","rol":"VISITANTE","email":"67@gmail.com","nombre":"Seis Ciete","creado_en":"2026-11-04T13:43:43.712012+00:00","tienda_id":null},{"id":"e98a6bcf-2f2c-44e1-ad2b-c820e784c5f0","rol":"VISITANTE","email":"labarcafelipe21@gmail.com","nombre":"Felipe labarca","creado_en":"2026-11-04T14:32:15.748175+00:00","tienda_id":null},{"id":"13118cef-4d60-43d7-b950-6a2f0cd6d867","rol":"VISITANTE","email":"tonito@gmail.com","nombre":"tonito","creado_en":"2026-11-04T14:46:43.529429+00:00","tienda_id":null},{"id":"ee277e89-29ea-4cec-8a57-1e115228a073","rol":"VISITANTE","email":"paulferrada578@gmail.com","nombre":"FerradaTienda","creado_en":"2026-11-04T14:52:12.146046+00:00","tienda_id":null},{"id":"4c085ede-fc86-4576-9a9e-cf526974afb8","rol":"VISITANTE","email":"creepercool578@gmail.com","nombre":"maxito perez","creado_en":"2026-11-04T14:55:46.996264+00:00","tienda_id":null},{"id":"f7953754-6853-45e0-ad7c-656cf9c684f3","rol":"VISITANTE","email":"hugoamedinao27@gmail.com","nombre":"muro d3 berlin","creado_en":"2026-11-09T03:17:36.612606+00:00","tienda_id":null},{"id":"013b2809-3573-4f8c-8f51-9aa1193ed0fc","rol":"VISITANTE","email":"g@gmail.com","nombre":"si","creado_en":"2026-11-09T13:50:45.947871+00:00","tienda_id":null}]$snapshot$::jsonb) x)
 then raise exception 'CAMBIO_DESDE_RESPALDO: perfiles'; end if; end $guard$;
do $guard$ begin if
 (select coalesce(jsonb_agg(x order by x::text),'[]'::jsonb) from (select to_jsonb(t) x from public."productos" t) s)
 is distinct from
 (select coalesce(jsonb_agg(x order by x::text),'[]'::jsonb) from jsonb_array_elements($snapshot$[{"id":"p_rep_1","nombre":"Kuchen Tradicional de Murta y Frambuesa","precio":12000,"foto_url":null,"categoria":"GOURMET","destacado":true,"publicado":true,"tienda_id":"t_reposteria_surena","variantes":[{"id":"v_rep1","stock":4,"nombre":"Molde Familiar (8 porciones)"}],"color_foto":4292255278,"descripcion":"","actualizado_en":"2026-11-02T13:38:53.543493+00:00"},{"id":"p_rep_2","nombre":"Torta de Hojarasca con Manjar Casero","precio":18500,"foto_url":null,"categoria":"GOURMET","destacado":true,"publicado":true,"tienda_id":"t_reposteria_surena","variantes":[{"id":"v_rep2","stock":2,"nombre":"15 Personas"}],"color_foto":4291113008,"descripcion":"","actualizado_en":"2026-11-02T13:38:53.543493+00:00"},{"id":"p_rep_3","nombre":"Pack 6 Alfajores Artesanales con Nuez","precio":5500,"foto_url":null,"categoria":"GOURMET","destacado":false,"publicado":true,"tienda_id":"t_reposteria_surena","variantes":[{"id":"v_rep3","stock":8,"nombre":"Caja 6 un."}],"color_foto":4287257147,"descripcion":"","actualizado_en":"2026-11-02T13:38:53.543493+00:00"},{"id":"p_rep_4","nombre":"Galletas Horneadas de Avena y Miel de Ulmo","precio":3800,"foto_url":null,"categoria":"GOURMET","destacado":false,"publicado":true,"tienda_id":"t_reposteria_surena","variantes":[{"id":"v_rep4","stock":12,"nombre":"Bolsa 250g"}],"color_foto":4290804286,"descripcion":"","actualizado_en":"2026-11-02T13:38:53.543493+00:00"},{"id":"p_mini_1","nombre":"Pan Amasado Caliente (Salida 17:00 hrs)","precio":2500,"foto_url":null,"categoria":"GOURMET","destacado":true,"publicado":true,"tienda_id":"t_minimarket_rosa","variantes":[{"id":"v_min1","stock":20,"nombre":"Bolsa 5 unidades"}],"color_foto":4292700960,"descripcion":"","actualizado_en":"2026-11-02T13:38:53.543493+00:00"},{"id":"p_mini_2","nombre":"Canasta de Verduras Frescas de Alerce","precio":7000,"foto_url":null,"categoria":"GOURMET","destacado":false,"publicado":true,"tienda_id":"t_minimarket_rosa","variantes":[{"id":"v_min2","stock":5,"nombre":"Surtido Semanal"}],"color_foto":4281236827,"descripcion":"","actualizado_en":"2026-11-02T13:38:53.543493+00:00"},{"id":"p_mini_3","nombre":"Huevos de Campo Libres (Docena)","precio":4200,"foto_url":null,"categoria":"GOURMET","destacado":false,"publicado":true,"tienda_id":"t_minimarket_rosa","variantes":[{"id":"v_min3","stock":15,"nombre":"Docena"}],"color_foto":4292255278,"descripcion":"","actualizado_en":"2026-11-02T13:38:53.543493+00:00"},{"id":"p_mini_4","nombre":"Miel de Ulmo Pura de Bosque Nativo 1kg","precio":6500,"foto_url":null,"categoria":"GOURMET","destacado":false,"publicado":true,"tienda_id":"t_minimarket_rosa","variantes":[{"id":"v_min4","stock":6,"nombre":"Frasco 1kg"}],"color_foto":4290804286,"descripcion":"","actualizado_en":"2026-11-02T13:38:53.543493+00:00"},{"id":"p_tej_1","nombre":"Chaleco de Lana Chilota a Palillo","precio":38000,"foto_url":null,"categoria":"TEXTIL","destacado":true,"publicado":true,"tienda_id":"t_tejidos_austral","variantes":[{"id":"v_tej1","stock":2,"nombre":"Gris Natural · Talla M"},{"id":"v_tej2","stock":1,"nombre":"Blanco Crudo · Talla L"}],"color_foto":4284232027,"descripcion":"","actualizado_en":"2026-11-02T13:38:53.543493+00:00"},{"id":"p_tej_2","nombre":"Gorro y Cuello de Lana Pura de Oveja","precio":14000,"foto_url":null,"categoria":"TEXTIL","destacado":false,"publicado":true,"tienda_id":"t_tejidos_austral","variantes":[{"id":"v_tej3","stock":6,"nombre":"Talla Única"}],"color_foto":4281236827,"descripcion":"","actualizado_en":"2026-11-02T13:38:53.543493+00:00"},{"id":"p_tej_3","nombre":"Piecera Rústica en Telar Tradicional","precio":45000,"foto_url":null,"categoria":"DECORACION","destacado":true,"publicado":true,"tienda_id":"t_tejidos_austral","variantes":[{"id":"v_tej4","stock":2,"nombre":"2 Plazas"}],"color_foto":4290804286,"descripcion":"","actualizado_en":"2026-11-02T13:38:53.543493+00:00"},{"id":"p_bar_1","nombre":"Corte de Cabello Clásico / Degrade Moderno","precio":8000,"foto_url":null,"categoria":"COSMETICA","destacado":true,"publicado":true,"tienda_id":"t_barberia_alerce","variantes":[{"id":"v_bar1","stock":10,"nombre":"Sesión 40 min"}],"color_foto":4281153352,"descripcion":"","actualizado_en":"2026-11-02T13:38:53.543493+00:00"},{"id":"p_bar_2","nombre":"Perfilado y Ritual de Barba con Toalla Caliente","precio":6500,"foto_url":null,"categoria":"COSMETICA","destacado":false,"publicado":true,"tienda_id":"t_barberia_alerce","variantes":[{"id":"v_bar2","stock":8,"nombre":"Sesión 30 min"}],"color_foto":4281440149,"descripcion":"","actualizado_en":"2026-11-02T13:38:53.543493+00:00"},{"id":"p_bar_3","nombre":"Cera y Pomada Modeladora Capilar","precio":7500,"foto_url":null,"categoria":"COSMETICA","destacado":false,"publicado":true,"tienda_id":"t_barberia_alerce","variantes":[{"id":"v_bar3","stock":12,"nombre":"Pote 100g"}],"color_foto":4283061608,"descripcion":"","actualizado_en":"2026-11-02T13:38:53.543493+00:00"}]$snapshot$::jsonb) x)
 then raise exception 'CAMBIO_DESDE_RESPALDO: productos'; end if; end $guard$;
do $guard$ begin if
 (select coalesce(jsonb_agg(x order by x::text),'[]'::jsonb) from (select to_jsonb(t) x from public."asociaciones" t) s)
 is distinct from
 (select coalesce(jsonb_agg(x order by x::text),'[]'::jsonb) from jsonb_array_elements($snapshot$[{"id":"as1","color":4287523732,"nombre":"Manos del Sur","localidad":"Puerto Montt"},{"id":"as2","color":4282285422,"nombre":"Red del Lago","localidad":"Puerto Montt"},{"id":"as3","color":4290804286,"nombre":"Taller Costero","localidad":"Puerto Montt"}]$snapshot$::jsonb) x)
 then raise exception 'CAMBIO_DESDE_RESPALDO: asociaciones'; end if; end $guard$;
do $guard$ begin if
 (select coalesce(jsonb_agg(x order by x::text),'[]'::jsonb) from (select to_jsonb(t) x from public."participaciones" t) s)
 is distinct from
 (select coalesce(jsonb_agg(x order by x::text),'[]'::jsonb) from jsonb_array_elements($snapshot$[]$snapshot$::jsonb) x)
 then raise exception 'CAMBIO_DESDE_RESPALDO: participaciones'; end if; end $guard$;
do $guard$ begin if
 (select coalesce(jsonb_agg(x order by x::text),'[]'::jsonb) from (select to_jsonb(t) x from public."cuentas_autorizadas" t) s)
 is distinct from
 (select coalesce(jsonb_agg(x order by x::text),'[]'::jsonb) from jsonb_array_elements($snapshot$[{"rol":"ADMIN","email":"admin@alerce.app","nombre":"Administración Alerce","creado_en":"2026-11-02T13:38:53.543493+00:00","tienda_id":null,"asociacion_id":null},{"rol":"EMPRENDEDORA","email":"donamarta@alerce.app","nombre":"Doña Marta","creado_en":"2026-11-02T13:38:53.543493+00:00","tienda_id":"t_minimarket_rosa","asociacion_id":null},{"rol":"EMPRENDEDORA","email":"barberia@alerce.app","nombre":"Barbería Alerce","creado_en":"2026-11-02T13:38:53.543493+00:00","tienda_id":"t_barberia_alerce","asociacion_id":null},{"rol":"EMPRENDEDORA","email":"tejidos@alerce.app","nombre":"Tejidos Austral","creado_en":"2026-11-02T13:38:53.543493+00:00","tienda_id":"t_tejidos_austral","asociacion_id":null},{"rol":"EMPRENDEDORA","email":"reposteria@alerce.app","nombre":"Repostería Sureña","creado_en":"2026-11-02T13:38:53.543493+00:00","tienda_id":"t_reposteria_surena","asociacion_id":null}]$snapshot$::jsonb) x)
 then raise exception 'CAMBIO_DESDE_RESPALDO: cuentas_autorizadas'; end if; end $guard$;
-- =====================================================================
-- Migración: fotos de perfil y logo del negocio
-- Se puede ejecutar sobre una base ya creada con 01_esquema.sql.
-- Es idempotente: no borra datos ni falla si ya está aplicada.
-- =====================================================================

-- 1. Columnas nuevas
alter table public.perfiles add column if not exists foto_url text;
alter table public.tiendas  add column if not exists logo_url text;

-- 2. Cada persona actualiza solo su nombre y su foto (nunca rol, correo ni tienda)
create or replace function public.actualizar_mi_perfil(
  p_nombre text default null,
  p_foto_url text default null
)
returns void language sql security definer set search_path = public as $$
  update public.perfiles
     set nombre   = coalesce(nullif(btrim(p_nombre), ''), nombre),
         foto_url = coalesce(p_foto_url, foto_url)
   where id = auth.uid();
$$;

revoke all on function public.actualizar_mi_perfil(text, text) from public;
grant execute on function public.actualizar_mi_perfil(text, text) to authenticated;

-- 3. Bucket público para las fotos de perfil (cada persona escribe en uid/...)
insert into storage.buckets (id, name, public)
values ('perfiles', 'perfiles', true)
on conflict (id) do nothing;

drop policy if exists perfiles_foto_ver on storage.objects;
create policy perfiles_foto_ver on storage.objects
  for select using (bucket_id = 'perfiles');
drop policy if exists perfiles_foto_subir on storage.objects;
create policy perfiles_foto_subir on storage.objects
  for insert to authenticated
  with check (bucket_id = 'perfiles' and (storage.foldername(name))[1] = auth.uid()::text);
drop policy if exists perfiles_foto_reemplazar on storage.objects;
create policy perfiles_foto_reemplazar on storage.objects
  for update to authenticated
  using (bucket_id = 'perfiles' and (storage.foldername(name))[1] = auth.uid()::text);
drop policy if exists perfiles_foto_borrar on storage.objects;
create policy perfiles_foto_borrar on storage.objects
  for delete to authenticated
  using (bucket_id = 'perfiles' and (storage.foldername(name))[1] = auth.uid()::text);

-- =====================================================================
-- Registro abierto de emprendedoras/vendedores (RN-01 actualizado)
-- Ejecutar una vez sobre Supabase ya configurado. ADMIN sigue reservado
-- para cuentas autorizadas. Las personas pueden escoger compradora o
-- emprendedora; la tienda y el rol los crea el servidor.
-- =====================================================================

create or replace function public.al_crear_usuario()
returns trigger language plpgsql security definer set search_path = public as $$
declare
  v_rol    text;
  v_nombre text;
  v_tienda text;
begin
  select rol, nombre, tienda_id into v_rol, v_nombre, v_tienda
    from public.cuentas_autorizadas
   where lower(email) = lower(new.email);

  -- La lista autorizada prevalece. El cliente solo puede solicitar
  -- EMPRENDEDORA; nunca puede enviar ADMIN desde metadatos.
  if v_rol is null
     and upper(coalesce(new.raw_user_meta_data ->> 'tipo_cuenta', '')) = 'EMPRENDEDORA' then
    v_rol := 'EMPRENDEDORA';
  end if;
  v_rol := coalesce(v_rol, 'VISITANTE');
  v_nombre := coalesce(
    nullif(btrim(new.raw_user_meta_data ->> 'nombre'), ''),
    v_nombre,
    split_part(new.email, '@', 1)
  );

  if v_rol = 'EMPRENDEDORA' then
    -- Conserva la tienda de las cuentas de prueba ya autorizadas si está libre.
    if v_tienda is not null then
      update public.tiendas
         set owner_id = new.id
       where id = v_tienda and (owner_id is null or owner_id = new.id);
      if not found then v_tienda := null; end if;
    end if;

    -- Toda emprendedora nueva obtiene una tienda privada de su propiedad.
    if v_tienda is null then
      v_tienda := 'tienda_' || replace(new.id::text, '-', '');
      insert into public.tiendas (id, owner_id, nombre, rubro, sector, bio, whatsapp)
      values (
        v_tienda,
        new.id,
        v_nombre,
        coalesce(nullif(btrim(new.raw_user_meta_data ->> 'rubro'), ''), 'Comercio y servicios'),
        coalesce(nullif(btrim(new.raw_user_meta_data ->> 'sector'), ''), 'Alerce'),
        'Emprendimiento local de Alerce.',
        coalesce(nullif(btrim(new.raw_user_meta_data ->> 'whatsapp'), ''), '')
      )
      on conflict (id) do nothing;
    end if;
  else
    v_tienda := null;
  end if;

  insert into public.perfiles (id, email, nombre, rol, tienda_id)
  values (new.id, new.email, v_nombre, v_rol, v_tienda)
  on conflict (id) do nothing;
  return new;
end;
$$;

drop trigger if exists al_crear_usuario on auth.users;
create trigger al_crear_usuario
  after insert on auth.users
  for each row execute function public.al_crear_usuario();

-- Permite que una cuenta compradora existente abra su propia tienda.
create or replace function public.crear_mi_tienda(p_nombre text)
returns void language plpgsql security definer set search_path = public as $$
declare
  v_uid uuid := auth.uid();
  v_tienda text;
  v_rol text;
begin
  if v_uid is null then raise exception 'Inicia sesión para crear tu tienda.'; end if;
  if nullif(btrim(p_nombre), '') is null then raise exception 'Escribe el nombre de tu tienda.'; end if;

  select rol, tienda_id into v_rol, v_tienda from public.perfiles where id = v_uid;
  if not found then raise exception 'No encontramos tu perfil.'; end if;
  if v_rol = 'ADMIN' then raise exception 'La cuenta de administración no puede abrir una tienda.'; end if;
  if v_tienda is not null then return; end if;

  v_tienda := 'tienda_' || replace(v_uid::text, '-', '');
  insert into public.tiendas as tienda (id, owner_id, nombre, rubro, sector, bio)
  values (v_tienda, v_uid, btrim(p_nombre), 'Comercio y servicios', 'Alerce', 'Emprendimiento local de Alerce.')
  on conflict (id) do update set owner_id = excluded.owner_id
  where tienda.owner_id = excluded.owner_id;

  if not exists (select 1 from public.tiendas where id = v_tienda and owner_id = v_uid) then
    raise exception 'No se pudo crear una tienda propia.';
  end if;

  update public.perfiles
     set rol = 'EMPRENDEDORA', tienda_id = v_tienda, nombre = btrim(p_nombre)
   where id = v_uid;
end;
$$;

revoke all on function public.crear_mi_tienda(text) from public;
grant execute on function public.crear_mi_tienda(text) to authenticated;

-- Repara cuentas que intentaron registrarse como vendedoras antes de aplicar
-- esta migración: su tipo de cuenta sí quedó guardado en Auth metadata.
insert into public.tiendas (id, owner_id, nombre, rubro, sector, bio, whatsapp)
select
  'tienda_' || replace(u.id::text, '-', ''),
  u.id,
  coalesce(nullif(btrim(u.raw_user_meta_data ->> 'nombre'), ''), split_part(u.email, '@', 1)),
  coalesce(nullif(btrim(u.raw_user_meta_data ->> 'rubro'), ''), 'Comercio y servicios'),
  coalesce(nullif(btrim(u.raw_user_meta_data ->> 'sector'), ''), 'Alerce'),
  'Emprendimiento local de Alerce.',
  coalesce(nullif(btrim(u.raw_user_meta_data ->> 'whatsapp'), ''), '')
from auth.users u
join public.perfiles p on p.id = u.id
where p.rol = 'VISITANTE'
  and upper(coalesce(u.raw_user_meta_data ->> 'tipo_cuenta', '')) = 'EMPRENDEDORA'
on conflict (id) do nothing;

update public.perfiles p
   set rol = 'EMPRENDEDORA',
       nombre = coalesce(nullif(btrim(u.raw_user_meta_data ->> 'nombre'), ''), p.nombre),
       tienda_id = 'tienda_' || replace(u.id::text, '-', '')
  from auth.users u
 where p.id = u.id
   and p.rol = 'VISITANTE'
   and upper(coalesce(u.raw_user_meta_data ->> 'tipo_cuenta', '')) = 'EMPRENDEDORA'
   and exists (
     select 1 from public.tiendas t
      where t.id = 'tienda_' || replace(u.id::text, '-', '')
        and t.owner_id = u.id
   );

-- Publica las funciones recién creadas para que PostgREST pueda resolverlas.
notify pgrst, 'reload schema';

-- =====================================================================
-- Migración 05 · Catálogo completo y ventas atómicas (Entrega 5)
--
-- Requisitos: 01_esquema.sql, 03_migracion_fotos.sql y 04_registro_vendedores.sql
-- ya aplicados. Probada en PostgreSQL 15 y 17. No usa vistas ni opciones
-- que dependan de la versión (por ejemplo, security_invoker en vistas).
--
-- Se puede volver a ejecutar: usa "if not exists", "create or replace" y
-- recrea políticas y triggers. NO borra ni reescribe registros existentes,
-- salvo lo indicado en la sección 3 (ids de opciones faltantes).
--
-- Qué hace (detalle en docs/entrega5/migraciones.md):
--  1. Columnas nuevas en productos, tiendas y ventas, con valores por defecto
--     que mantienen el comportamiento de los registros actuales.
--  2. Tabla tipos_producto: tipos y campos específicos configurables.
--  3. Reglas públicas del catálogo en el servidor (RLS), con las mismas
--     reglas que aplican la web y la app (estado_publico_producto).
--  4. Trigger que protege cantidades, campos nuevos y piezas únicas vendidas
--     frente a clientes antiguos que envían el producto completo.
--  5. guardar_producto(): alta y edición con ajuste de cantidades sin pisar
--     ventas simultáneas.
--  6. registrar_venta(): venta y descuento en una sola transacción,
--     idempotente y sin sobreventa. La escritura directa en ventas se retira.
--  7. Storage: las fotos no se pueden mover a la carpeta de otra persona.
-- =====================================================================

-- ---------- 1. Columnas nuevas ----------

alter table public.productos add column if not exists tipo          text;
alter table public.productos add column if not exists atributos     jsonb not null default '{}'::jsonb;
alter table public.productos add column if not exists fotos         jsonb not null default '[]'::jsonb;
alter table public.productos add column if not exists modalidad     text  not null default 'REGULAR';
alter table public.productos add column if not exists maneja_stock  boolean not null default true;
alter table public.productos add column if not exists al_agotarse   text  not null default 'MOSTRAR_AGOTADO';
alter table public.productos add column if not exists disponible    boolean not null default true;
alter table public.productos add column if not exists vendida_en    timestamptz;

alter table public.tiendas add column if not exists valores_producto jsonb not null default '{}'::jsonb;
alter table public.tiendas add column if not exists historial_activo boolean not null default true;

alter table public.ventas add column if not exists creado_por   uuid;
alter table public.ventas add column if not exists huella       text;
alter table public.ventas add column if not exists en_historial boolean not null default true;
alter table public.ventas add column if not exists lugar_tipo   text;
alter table public.ventas add column if not exists lugar_nombre text;
-- Con el historial desactivado no se guarda el total (registro técnico mínimo).
alter table public.ventas alter column total drop not null;

do $$
begin
  if not exists (select 1 from pg_constraint where conname = 'productos_modalidad_valida') then
    alter table public.productos add constraint productos_modalidad_valida
      check (modalidad in ('REGULAR', 'A_PEDIDO', 'PIEZA_UNICA'));
  end if;
  if not exists (select 1 from pg_constraint where conname = 'productos_al_agotarse_valido') then
    alter table public.productos add constraint productos_al_agotarse_valido
      check (al_agotarse in ('MOSTRAR_AGOTADO', 'OCULTAR', 'PASAR_A_PEDIDO'));
  end if;
  if not exists (select 1 from pg_constraint where conname = 'productos_variantes_es_lista') then
    alter table public.productos add constraint productos_variantes_es_lista
      check (jsonb_typeof(variantes) = 'array');
  end if;
  if not exists (select 1 from pg_constraint where conname = 'ventas_lugar_valido') then
    alter table public.ventas add constraint ventas_lugar_valido
      check (lugar_tipo is null or lugar_tipo in ('FERIA', 'OTRO'));
  end if;
end $$;

-- ---------- 2. Tipos de producto y campos específicos ----------
-- Los tipos reales salen del levantamiento con la contraparte. Esta tabla
-- queda vacía: el administrador la completa (ver docs/entrega5/tipos-producto.md).
-- campos = [{"clave":"talla","etiqueta":"Talla","tipo":"opciones","opciones":["S","M"],"obligatorio":false}]
create table if not exists public.tipos_producto (
  id         text primary key,
  categoria  text not null,
  nombre     text not null,
  campos     jsonb not null default '[]'::jsonb check (jsonb_typeof(campos) = 'array'),
  activo     boolean not null default true,
  orden      int not null default 0
);
alter table public.tipos_producto enable row level security;
drop policy if exists tipos_ver on public.tipos_producto;
create policy tipos_ver on public.tipos_producto for select using (true);
drop policy if exists tipos_admin on public.tipos_producto;
create policy tipos_admin on public.tipos_producto
  for all to authenticated using (public.es_admin()) with check (public.es_admin());
grant select on public.tipos_producto to anon;
grant select, insert, update, delete on public.tipos_producto to authenticated; -- RLS: solo admin escribe

-- ---------- 3. Reglas del catálogo ----------

-- Unidades que todavía se pueden vender entre todas las opciones.
create or replace function public.unidades_vendibles(p_variantes jsonb)
returns integer language sql immutable set search_path = public as $$
  select coalesce(sum(greatest(
           case when jsonb_typeof(v -> 'stock') = 'number' then floor((v ->> 'stock')::numeric)::int else 0 end,
           0)), 0)::int
    from jsonb_array_elements(case when jsonb_typeof(p_variantes) = 'array' then p_variantes else '[]'::jsonb end) v;
$$;

-- Estado que ve el público. Misma tabla de verdad que js/catalogo.js y
-- data/ReglasCatalogo.kt (docs/entrega5/contrato-catalogo.md):
--   OCULTO     → no se muestra (no publicado, no disponible, pieza única vendida,
--                agotado con "ocultar" o pieza única sin unidad)
--   A_PEDIDO   → modalidad a pedido, o agotado con "pasar a pedido"
--   AGOTADO    → agotado con "mostrar agotado"
--   DISPONIBLE → resto
create or replace function public.estado_publico_producto(
  p_publicado boolean, p_disponible boolean, p_vendida_en timestamptz,
  p_maneja_stock boolean, p_modalidad text, p_al_agotarse text, p_variantes jsonb
) returns text language sql immutable set search_path = public as $$
  select case
    when not coalesce(p_publicado, false) or not coalesce(p_disponible, true) then 'OCULTO'
    when p_vendida_en is not null then 'OCULTO'
    when p_modalidad = 'A_PEDIDO' then 'A_PEDIDO'
    when p_modalidad = 'PIEZA_UNICA' then
      case when public.unidades_vendibles(p_variantes) > 0 then 'DISPONIBLE' else 'OCULTO' end
    when not coalesce(p_maneja_stock, true) then 'DISPONIBLE'
    when public.unidades_vendibles(p_variantes) > 0 then 'DISPONIBLE'
    when p_al_agotarse = 'OCULTAR' then 'OCULTO'
    when p_al_agotarse = 'PASAR_A_PEDIDO' then 'A_PEDIDO'
    else 'AGOTADO'
  end;
$$;

grant execute on function public.unidades_vendibles(jsonb) to anon, authenticated;
grant execute on function public.estado_publico_producto(boolean, boolean, timestamptz, boolean, text, text, jsonb)
  to anon, authenticated;

-- El público (con o sin sesión) solo lee lo que la regla deja ver.
-- La dueña sigue viendo todos los suyos (RN-09).
drop policy if exists productos_ver on public.productos;
create policy productos_ver on public.productos
  for select using (
    public.estado_publico_producto(publicado, disponible, vendida_en, maneja_stock, modalidad, al_agotarse, variantes) <> 'OCULTO'
    or public.es_duena_de(tienda_id)
  );

-- Opciones antiguas sin id (las creó la web anterior): se les asigna uno estable.
update public.productos p
   set variantes = (
     select jsonb_agg(
              case when coalesce(v ->> 'id', '') = '' then v || jsonb_build_object('id', p.id || '_v' || o) else v end
              order by o)
       from jsonb_array_elements(p.variantes) with ordinality as e(v, o))
 where jsonb_typeof(p.variantes) = 'array'
   and exists (select 1 from jsonb_array_elements(p.variantes) v where coalesce(v ->> 'id', '') = '');

-- ---------- 4. Protección frente a clientes antiguos ----------
-- Un APK anterior sube el producto completo con las cantidades que tenía en
-- el celular y sin los campos nuevos. Sin esta protección pisaría ventas
-- hechas desde otro dispositivo y borraría el precio/foto de cada opción.
-- Fuera de guardar_producto() y registrar_venta():
--   * la cantidad de una opción existente no cambia;
--   * los datos de una opción que el cliente no envía se conservan;
--   * una pieza única vendida no vuelve a venderse ni a mostrarse.
create or replace function public.proteger_producto()
returns trigger language plpgsql set search_path = public as $$
declare
  v_operacion boolean := coalesce(current_setting('alerce.operacion_stock', true), '') = 'on';
  v_resultado jsonb := '[]'::jsonb;
  v_nueva jsonb;
  v_vieja jsonb;
  v_orden int := 0;
begin
  if jsonb_typeof(new.variantes) is distinct from 'array' then
    new.variantes := '[]'::jsonb;
  end if;
  if jsonb_typeof(new.fotos) is distinct from 'array' then new.fotos := '[]'::jsonb; end if;
  if jsonb_typeof(new.atributos) is distinct from 'object' then new.atributos := '{}'::jsonb; end if;

  for v_nueva in select value from jsonb_array_elements(new.variantes) loop
    v_orden := v_orden + 1;
    if jsonb_typeof(v_nueva) <> 'object' then continue; end if;
    if coalesce(v_nueva ->> 'id', '') = '' then
      v_nueva := v_nueva || jsonb_build_object('id', new.id || '_v' || v_orden);
    end if;
    if tg_op = 'UPDATE' and not v_operacion then
      select value into v_vieja from jsonb_array_elements(old.variantes)
       where value ->> 'id' = v_nueva ->> 'id' limit 1;
      if v_vieja is not null then
        v_nueva := v_vieja || v_nueva || jsonb_build_object('stock', coalesce(v_vieja -> 'stock', '0'::jsonb));
      end if;
      v_vieja := null;
    end if;
    v_resultado := v_resultado || jsonb_build_array(v_nueva);
  end loop;
  new.variantes := v_resultado;

  if tg_op = 'UPDATE' then
    -- vendida_en es permanente (RN-08): nadie lo borra, ni la operación segura.
    if old.vendida_en is not null then new.vendida_en := old.vendida_en; end if;
    if not v_operacion then
      new.vendida_en := old.vendida_en;
    end if;
  elsif not v_operacion then
    new.vendida_en := null;
  end if;

  if new.modalidad = 'PIEZA_UNICA' then
    new.maneja_stock := true;
    new.al_agotarse := 'OCULTAR';
  end if;
  new.actualizado_en := now();
  return new;
end;
$$;

drop trigger if exists proteger_producto on public.productos;
create trigger proteger_producto
  before insert or update on public.productos
  for each row execute function public.proteger_producto();

-- ---------- 5. Guardar producto (alta y edición) ----------
-- La tienda sale de la sesión, nunca del cliente. Para cada opción ya
-- existente el cliente puede enviar "stockBase" (la cantidad que veía al
-- editar): el servidor aplica la diferencia sobre la cantidad actual, así
-- una venta simultánea no se pierde. Si la diferencia deja la opción bajo
-- cero, responde CONFLICTO_STOCK y no guarda nada.
create or replace function public.mi_tienda_id()
returns text language sql stable security definer set search_path = public as $$
  select t.id
    from public.perfiles pf
    join public.tiendas t on t.id = pf.tienda_id and t.owner_id = pf.id
   where pf.id = auth.uid();
$$;
revoke all on function public.mi_tienda_id() from public;
grant execute on function public.mi_tienda_id() to authenticated;

-- Tienda dueña de un producto (cualquiera sea su visibilidad). Solo la usa
-- guardar_producto() para responder PRODUCTO_AJENO en vez de un error de RLS.
create or replace function public.tienda_del_producto(p_id text)
returns text language sql stable security definer set search_path = public as $$
  select tienda_id from public.productos where id = p_id;
$$;
revoke all on function public.tienda_del_producto(text) from public;
grant execute on function public.tienda_del_producto(text) to authenticated;

create or replace function public.guardar_producto(p jsonb)
returns jsonb language plpgsql security invoker set search_path = public as $$
declare
  v_tienda text := public.mi_tienda_id();
  v_id text := btrim(coalesce(p ->> 'id', ''));
  v_actual public.productos;
  v_existe boolean;
  v_modalidad text := coalesce(nullif(p ->> 'modalidad', ''), 'REGULAR');
  v_al_agotarse text := coalesce(nullif(p ->> 'alAgotarse', ''), 'MOSTRAR_AGOTADO');
  v_maneja boolean := coalesce((p ->> 'manejaStock')::boolean, true);
  v_precio numeric;
  v_variantes jsonb := '[]'::jsonb;
  v_v jsonb;
  v_vieja jsonb;
  v_stock int;
  v_base int;
  v_orden int := 0;
  v_vid text;
  v_ids text[] := '{}';
  v_foto text;
  v_fila public.productos;
begin
  if auth.uid() is null then raise exception 'NO_AUTENTICADO' using errcode = '28000'; end if;
  if v_tienda is null then raise exception 'SIN_TIENDA' using errcode = '42501'; end if;
  if v_id = '' or length(v_id) > 100 then raise exception 'ID_INVALIDO'; end if;
  if nullif(btrim(coalesce(p ->> 'nombre', '')), '') is null then raise exception 'NOMBRE_OBLIGATORIO'; end if;
  if v_modalidad not in ('REGULAR', 'A_PEDIDO', 'PIEZA_UNICA') then raise exception 'MODALIDAD_INVALIDA'; end if;
  if v_al_agotarse not in ('MOSTRAR_AGOTADO', 'OCULTAR', 'PASAR_A_PEDIDO') then raise exception 'AL_AGOTARSE_INVALIDO'; end if;
  if jsonb_typeof(p -> 'precio') <> 'number' or (p ->> 'precio')::numeric < 0 then raise exception 'PRECIO_INVALIDO'; end if;
  v_precio := (p ->> 'precio')::numeric;
  if jsonb_typeof(coalesce(p -> 'variantes', '[]'::jsonb)) <> 'array' or jsonb_array_length(coalesce(p -> 'variantes', '[]'::jsonb)) = 0 then
    raise exception 'OPCIONES_OBLIGATORIAS';
  end if;
  if jsonb_array_length(p -> 'variantes') > 50 then raise exception 'DEMASIADAS_OPCIONES'; end if;

  if coalesce(public.tienda_del_producto(v_id), v_tienda) <> v_tienda then
    raise exception 'PRODUCTO_AJENO' using errcode = '42501';
  end if;
  select * into v_actual from public.productos where id = v_id for update;
  v_existe := found;

  for v_v in select value from jsonb_array_elements(p -> 'variantes') loop
    v_orden := v_orden + 1;
    if jsonb_typeof(v_v) <> 'object' then raise exception 'OPCION_INVALIDA'; end if;
    v_vid := coalesce(nullif(btrim(v_v ->> 'id'), ''), v_id || '_v' || v_orden);
    if v_vid = any(v_ids) then raise exception 'OPCION_DUPLICADA' using detail = v_vid; end if;
    v_ids := v_ids || v_vid;
    if nullif(btrim(coalesce(v_v ->> 'nombre', '')), '') is null then raise exception 'OPCION_SIN_NOMBRE'; end if;
    if v_v ? 'precio' and jsonb_typeof(v_v -> 'precio') not in ('number', 'null') then raise exception 'PRECIO_OPCION_INVALIDO'; end if;
    if jsonb_typeof(v_v -> 'precio') = 'number' and (v_v ->> 'precio')::numeric < 0 then raise exception 'PRECIO_OPCION_INVALIDO'; end if;
    v_foto := nullif(v_v ->> 'fotoUrl', '');
    if v_foto is not null and v_foto !~* '^https://' then raise exception 'FOTO_OPCION_INVALIDA'; end if;
    if jsonb_typeof(v_v -> 'stock') <> 'number' or (v_v ->> 'stock')::numeric <> floor((v_v ->> 'stock')::numeric)
       or (v_v ->> 'stock')::int < 0 or (v_v ->> 'stock')::int > 100000 then
      raise exception 'CANTIDAD_INVALIDA' using detail = v_vid;
    end if;
    v_stock := (v_v ->> 'stock')::int;

    v_vieja := null;
    if v_existe then
      select value into v_vieja from jsonb_array_elements(v_actual.variantes) where value ->> 'id' = v_vid limit 1;
    end if;
    if v_vieja is not null and jsonb_typeof(v_v -> 'stockBase') = 'number' then
      v_base := (v_v ->> 'stockBase')::int;
      v_stock := coalesce((v_vieja ->> 'stock')::int, 0) + (v_stock - v_base);
      if v_stock < 0 then
        raise exception 'CONFLICTO_STOCK'
          using detail = jsonb_build_object('varianteId', v_vid, 'actual', coalesce((v_vieja ->> 'stock')::int, 0))::text;
      end if;
    end if;

    v_variantes := v_variantes || jsonb_build_array(jsonb_build_object(
      'id', v_vid,
      'nombre', btrim(v_v ->> 'nombre'),
      'stock', v_stock,
      'precio', case when jsonb_typeof(v_v -> 'precio') = 'number' then v_v -> 'precio' else null end,
      'foto_url', v_foto));
  end loop;

  if v_modalidad = 'PIEZA_UNICA' then
    if public.unidades_vendibles(v_variantes) > 1 then raise exception 'PIEZA_UNICA_UNA_UNIDAD'; end if;
    if v_existe and v_actual.vendida_en is not null then
      -- Nadie puede reponer una pieza única vendida (RN-08).
      select jsonb_agg(value || jsonb_build_object('stock', 0)) into v_variantes from jsonb_array_elements(v_variantes);
    end if;
  end if;

  perform set_config('alerce.operacion_stock', 'on', true);
  insert into public.productos as pr (
    id, tienda_id, nombre, descripcion, categoria, tipo, atributos, precio, publicado, destacado,
    color_foto, variantes, foto_url, fotos, modalidad, maneja_stock, al_agotarse, disponible)
  values (
    v_id, v_tienda, btrim(p ->> 'nombre'), coalesce(p ->> 'descripcion', ''),
    coalesce(nullif(p ->> 'categoria', ''), 'TEXTIL'), nullif(p ->> 'tipo', ''),
    case when jsonb_typeof(p -> 'atributos') = 'object' then p -> 'atributos' else '{}'::jsonb end,
    v_precio, coalesce((p ->> 'publicado')::boolean, false), coalesce((p ->> 'destacado')::boolean, false),
    case when jsonb_typeof(p -> 'colorFoto') = 'number' then (p ->> 'colorFoto')::bigint else null end,
    v_variantes,
    case when coalesce(p ->> 'fotoUrl', '') ~* '^https://' then p ->> 'fotoUrl' else null end,
    coalesce((select jsonb_agg(f) from jsonb_array_elements(case when jsonb_typeof(p -> 'fotos') = 'array' then p -> 'fotos' else '[]'::jsonb end) f
               where jsonb_typeof(f) = 'string' and f #>> '{}' ~* '^https://'), '[]'::jsonb),
    v_modalidad, v_maneja, v_al_agotarse, coalesce((p ->> 'disponible')::boolean, true))
  on conflict (id) do update set
    nombre = excluded.nombre, descripcion = excluded.descripcion, categoria = excluded.categoria,
    tipo = excluded.tipo, atributos = excluded.atributos, precio = excluded.precio,
    publicado = excluded.publicado, destacado = excluded.destacado, color_foto = excluded.color_foto,
    variantes = excluded.variantes, foto_url = excluded.foto_url, fotos = excluded.fotos,
    modalidad = excluded.modalidad, maneja_stock = excluded.maneja_stock,
    al_agotarse = excluded.al_agotarse, disponible = excluded.disponible
  returning * into v_fila;
  perform set_config('alerce.operacion_stock', 'off', true);
  return to_jsonb(v_fila);
end;
$$;
revoke all on function public.guardar_producto(jsonb) from public;
grant execute on function public.guardar_producto(jsonb) to authenticated;

-- ---------- 6. Registrar venta (atómica e idempotente) ----------
-- Entrada:
-- {"id":"<uuid>", "items":[{"productoId","varianteId","cantidad","encargo"?,"precio"?,
--   "nombre"?,"detalle"?,"fotoUrl"?}], "medioPago"?, "feriaId"?, "lugarTipo"?,
--   "lugarNombre"?, "fechaLocal"?, "conHistorial"?}
-- Garantías:
--  * la tienda sale de la sesión; un producto de otra tienda no existe para la venta;
--  * las filas se bloquean en orden de id (sin interbloqueos entre ventas);
--  * sin unidades suficientes responde STOCK_INSUFICIENTE y no guarda nada;
--  * mismo id y mismo contenido → YA_REGISTRADA (no descuenta de nuevo);
--    mismo id con otro contenido → ID_CONFLICTO (no modifica la venta previa);
--  * una pieza única vendida queda retirada para siempre (vendida_en).
create or replace function public.registrar_venta(p_venta jsonb)
returns jsonb language plpgsql security definer set search_path = public as $$
declare
  v_uid uuid := auth.uid();
  v_tienda text;
  v_historial_tienda boolean;
  v_id text := btrim(coalesce(p_venta ->> 'id', ''));
  v_items jsonb := p_venta -> 'items';
  v_norm jsonb;
  v_huella text;
  v_previa public.ventas;
  v_item jsonb;
  v_prod public.productos;
  v_variantes jsonb;
  v_idx int;
  v_var jsonb;
  v_stock int;
  v_encargo boolean;
  v_precio numeric;
  v_lineas jsonb := '[]'::jsonb;
  v_minimas jsonb := '[]'::jsonb;
  v_total numeric := 0;
  v_historial boolean;
  v_productos text[];
  v_bloqueados int;
  v_cambios jsonb := '[]'::jsonb;
  v_feria text := nullif(p_venta ->> 'feriaId', '');
  v_lugar_tipo text := nullif(p_venta ->> 'lugarTipo', '');
  v_pid text;
  v_unidades_pieza int;
begin
  if v_uid is null then raise exception 'NO_AUTENTICADO' using errcode = '28000'; end if;
  select t.id, t.historial_activo into v_tienda, v_historial_tienda
    from public.perfiles pf join public.tiendas t on t.id = pf.tienda_id and t.owner_id = pf.id
   where pf.id = v_uid;
  if v_tienda is null then raise exception 'SIN_TIENDA' using errcode = '42501'; end if;
  if coalesce(p_venta ->> 'tiendaId', v_tienda) <> v_tienda then raise exception 'TIENDA_AJENA' using errcode = '42501'; end if;
  if v_id = '' or length(v_id) > 100 then raise exception 'ID_INVALIDO'; end if;
  if jsonb_typeof(v_items) is distinct from 'array' or jsonb_array_length(v_items) = 0 or jsonb_array_length(v_items) > 100 then
    raise exception 'ITEMS_INVALIDOS';
  end if;
  if v_lugar_tipo is not null and v_lugar_tipo not in ('FERIA', 'OTRO') then raise exception 'LUGAR_INVALIDO'; end if;

  -- Validación y normalización (una línea por producto + opción + tipo de entrega).
  for v_item in select value from jsonb_array_elements(v_items) loop
    if jsonb_typeof(v_item) <> 'object'
       or nullif(v_item ->> 'productoId', '') is null or nullif(v_item ->> 'varianteId', '') is null then
      raise exception 'ITEM_INVALIDO';
    end if;
    if jsonb_typeof(v_item -> 'cantidad') <> 'number'
       or (v_item ->> 'cantidad')::numeric <> floor((v_item ->> 'cantidad')::numeric)
       or (v_item ->> 'cantidad')::numeric < 1 or (v_item ->> 'cantidad')::numeric > 9999 then
      raise exception 'CANTIDAD_INVALIDA' using detail = v_item ->> 'productoId';
    end if;
    if v_item ? 'precio' and jsonb_typeof(v_item -> 'precio') not in ('number', 'null') then raise exception 'PRECIO_INVALIDO'; end if;
    if jsonb_typeof(v_item -> 'precio') = 'number' and (v_item ->> 'precio')::numeric < 0 then raise exception 'PRECIO_INVALIDO'; end if;
  end loop;

  select jsonb_agg(l order by l ->> 'productoId', l ->> 'varianteId', l ->> 'encargo') into v_norm
    from (
      select jsonb_build_object(
               'productoId', value ->> 'productoId',
               'varianteId', value ->> 'varianteId',
               'encargo', coalesce((value ->> 'encargo')::boolean, false),
               'cantidad', sum((value ->> 'cantidad')::int),
               'precio', max(case when jsonb_typeof(value -> 'precio') = 'number' then (value ->> 'precio')::numeric end),
               'nombre', max(value ->> 'nombre'),
               'detalle', max(value ->> 'detalle'),
               'fotoUrl', max(value ->> 'fotoUrl')) as l
        from jsonb_array_elements(v_items)
       group by value ->> 'productoId', value ->> 'varianteId', coalesce((value ->> 'encargo')::boolean, false)
    ) s;

  v_huella := md5(v_tienda || '|' || (
    select jsonb_agg(jsonb_build_object('p', l ->> 'productoId', 'v', l ->> 'varianteId',
                                        'e', l -> 'encargo', 'c', l -> 'cantidad', 'pr', l -> 'precio')
                     order by l ->> 'productoId', l ->> 'varianteId', l ->> 'encargo')::text
      from jsonb_array_elements(v_norm) l));

  -- Reintento rápido (sin bloquear productos).
  select * into v_previa from public.ventas where id = v_id;
  if found then
    if v_previa.tienda_id = v_tienda and v_previa.huella = v_huella then
      return jsonb_build_object('estado', 'YA_REGISTRADA', 'id', v_id, 'total', v_previa.total);
    end if;
    raise exception 'ID_CONFLICTO' using errcode = '23505';
  end if;

  -- Bloqueo en orden fijo: dos ventas con los mismos productos nunca se cruzan.
  select array_agg(distinct l ->> 'productoId' order by l ->> 'productoId') into v_productos from jsonb_array_elements(v_norm) l;
  select count(*) into v_bloqueados from (
    select 1 from public.productos where tienda_id = v_tienda and id = any(v_productos) order by id for update
  ) b;
  if v_bloqueados <> array_length(v_productos, 1) then raise exception 'PRODUCTO_NO_ENCONTRADO'; end if;

  -- Otro intento con el mismo id pudo terminar mientras esperábamos el bloqueo.
  select * into v_previa from public.ventas where id = v_id;
  if found then
    if v_previa.tienda_id = v_tienda and v_previa.huella = v_huella then
      return jsonb_build_object('estado', 'YA_REGISTRADA', 'id', v_id, 'total', v_previa.total);
    end if;
    raise exception 'ID_CONFLICTO' using errcode = '23505';
  end if;

  foreach v_pid in array v_productos loop
    select * into v_prod from public.productos where id = v_pid;
    v_variantes := v_prod.variantes;
    v_unidades_pieza := 0;
    if v_prod.vendida_en is not null then raise exception 'PIEZA_VENDIDA' using detail = v_pid; end if;

    for v_item in select value from jsonb_array_elements(v_norm) where value ->> 'productoId' = v_pid loop
      select o - 1 into v_idx from jsonb_array_elements(v_variantes) with ordinality e(x, o)
       where x ->> 'id' = v_item ->> 'varianteId' limit 1;
      if v_idx is null then raise exception 'OPCION_NO_ENCONTRADA' using detail = (v_item ->> 'varianteId'); end if;
      v_var := v_variantes -> v_idx;
      v_encargo := (v_item ->> 'encargo')::boolean or v_prod.modalidad = 'A_PEDIDO';
      v_stock := coalesce((v_var ->> 'stock')::int, 0);

      if v_encargo then
        if v_prod.modalidad = 'PIEZA_UNICA' then raise exception 'ENCARGO_NO_PERMITIDO' using detail = v_pid; end if;
      elsif v_prod.modalidad = 'PIEZA_UNICA' then
        v_unidades_pieza := v_unidades_pieza + (v_item ->> 'cantidad')::int;
      elsif v_prod.maneja_stock then
        if v_stock < (v_item ->> 'cantidad')::int then
          raise exception 'STOCK_INSUFICIENTE'
            using detail = jsonb_build_object('productoId', v_pid, 'varianteId', v_item ->> 'varianteId', 'disponible', v_stock)::text;
        end if;
        v_variantes := jsonb_set(v_variantes, array[v_idx::text, 'stock'], to_jsonb(v_stock - (v_item ->> 'cantidad')::int));
      end if;

      v_precio := coalesce((v_item ->> 'precio')::numeric, (v_var ->> 'precio')::numeric, v_prod.precio);
      v_total := v_total + v_precio * (v_item ->> 'cantidad')::int;
      v_lineas := v_lineas || jsonb_build_array(jsonb_build_object(
        'productoId', v_pid, 'varianteId', v_item ->> 'varianteId',
        'nombre', coalesce(v_item ->> 'nombre', v_prod.nombre),
        'detalle', coalesce(v_item ->> 'detalle', v_var ->> 'nombre'),
        'precio', v_precio, 'cantidad', (v_item ->> 'cantidad')::int, 'encargo', v_encargo,
        'fotoUrl', coalesce(v_item ->> 'fotoUrl', v_var ->> 'foto_url', v_prod.foto_url)));
      v_minimas := v_minimas || jsonb_build_array(jsonb_build_object(
        'productoId', v_pid, 'varianteId', v_item ->> 'varianteId',
        'cantidad', (v_item ->> 'cantidad')::int, 'encargo', v_encargo));
    end loop;

    if v_unidades_pieza > 0 then
      -- Una pieza única es una unidad total, aunque tenga opciones.
      if v_unidades_pieza <> 1 or public.unidades_vendibles(v_variantes) < 1 then
        raise exception 'STOCK_INSUFICIENTE'
          using detail = jsonb_build_object('productoId', v_pid, 'disponible', least(public.unidades_vendibles(v_variantes), 1))::text;
      end if;
      select jsonb_agg(value || jsonb_build_object('stock', 0)) into v_variantes from jsonb_array_elements(v_variantes);
    end if;

    if v_variantes is distinct from v_prod.variantes or v_unidades_pieza > 0 then
      perform set_config('alerce.operacion_stock', 'on', true);
      update public.productos
         set variantes = v_variantes,
             vendida_en = case when v_unidades_pieza > 0 then now() else vendida_en end
       where id = v_pid;
      perform set_config('alerce.operacion_stock', 'off', true);
    end if;
    select * into v_prod from public.productos where id = v_pid;
    v_cambios := v_cambios || jsonb_build_array(jsonb_build_object(
      'id', v_pid, 'variantes', v_prod.variantes, 'vendida_en', v_prod.vendida_en));
  end loop;

  if v_feria is not null and not exists (select 1 from public.ferias where id = v_feria) then
    raise exception 'FERIA_NO_ENCONTRADA';
  end if;

  v_historial := coalesce((p_venta ->> 'conHistorial')::boolean, true) and v_historial_tienda;
  if v_historial then
    insert into public.ventas (id, tienda_id, fecha_local, items, medio_pago, feria_id, total,
                               creado_por, huella, en_historial, lugar_tipo, lugar_nombre)
    values (v_id, v_tienda, nullif(p_venta ->> 'fechaLocal', ''), v_lineas, nullif(p_venta ->> 'medioPago', ''),
            v_feria, v_total, v_uid, v_huella, true,
            coalesce(v_lugar_tipo, case when v_feria is not null then 'FERIA' end),
            nullif(btrim(coalesce(p_venta ->> 'lugarNombre', '')), ''));
  else
    -- Registro técnico mínimo: sin precio, total, medio de pago, lugar ni fotos.
    insert into public.ventas (id, tienda_id, items, total, creado_por, huella, en_historial)
    values (v_id, v_tienda, v_minimas, null, v_uid, v_huella, false);
  end if;

  return jsonb_build_object('estado', 'REGISTRADA', 'id', v_id,
                            'total', case when v_historial then v_total end,
                            'enHistorial', v_historial, 'productos', v_cambios);
end;
$$;
revoke all on function public.registrar_venta(jsonb) from public;
grant execute on function public.registrar_venta(jsonb) to authenticated;

-- Ningún camino antiguo escribe ventas directamente: solo registrar_venta().
-- La dueña sigue leyendo su historial; el admin no (RN-10).
revoke insert, update, delete on public.ventas from authenticated, anon;

-- ---------- 7. Storage: no mover fotos a carpetas ajenas ----------
drop policy if exists fotos_reemplazar on storage.objects;
create policy fotos_reemplazar on storage.objects
  for update to authenticated
  using (bucket_id = 'productos' and (storage.foldername(name))[1] = auth.uid()::text)
  with check (bucket_id = 'productos' and (storage.foldername(name))[1] = auth.uid()::text);
drop policy if exists perfiles_foto_reemplazar on storage.objects;
create policy perfiles_foto_reemplazar on storage.objects
  for update to authenticated
  using (bucket_id = 'perfiles' and (storage.foldername(name))[1] = auth.uid()::text)
  with check (bucket_id = 'perfiles' and (storage.foldername(name))[1] = auth.uid()::text);

-- ---------- 8. Registro de la migración ----------
create table if not exists public.migraciones_aplicadas (
  nombre text primary key,
  aplicada_en timestamptz not null default now()
);
alter table public.migraciones_aplicadas enable row level security;
drop policy if exists migraciones_admin on public.migraciones_aplicadas;
create policy migraciones_admin on public.migraciones_aplicadas
  for select to authenticated using (public.es_admin());
grant select on public.migraciones_aplicadas to authenticated;
insert into public.migraciones_aplicadas (nombre) values ('05_catalogo_ventas')
on conflict (nombre) do update set aplicada_en = now();

-- Algunas instalaciones de Supabase conceden ALL a los clientes por defecto.
-- RLS no protege TRUNCATE. Los clientes no necesitan vaciar tablas, crear
-- triggers ni referencias: se conservan solo sus permisos de datos existentes.
revoke truncate, references, trigger on all tables in schema public from anon, authenticated;

notify pgrst, 'reload schema';

select 'MIGRACIONES_03_04_05_APLICADAS' as resultado, (select count(*) from public.productos) as productos, (select count(*) from public.ventas) as ventas, (select count(*) from public.tiendas) as tiendas; commit;