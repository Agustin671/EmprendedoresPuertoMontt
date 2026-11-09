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
