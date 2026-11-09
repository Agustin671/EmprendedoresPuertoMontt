-- =====================================================================
-- alerceshopin · Esquema de base de datos en Supabase (Grupo ALERCEIS7)
-- Se ejecuta UNA vez en Studio → SQL Editor. Se puede volver a correr:
-- usa "if not exists" / "on conflict" y recrea políticas y funciones.
--
-- Cubre: RN-01 (registro no libre), RN-10 (ventas privadas),
-- RNF-SEG-01 (cada emprendedora solo modifica su tienda; solo el admin
-- gestiona cuentas, asociaciones y ferias) y RNF-DAT-01 (modelo documentado).
-- =====================================================================

-- ---------- 1. Tablas ----------

-- Permisos especiales asignados por administración. ADMIN sigue siendo
-- exclusivo de esta lista; las cuentas nuevas pueden elegir ser compradoras
-- o emprendedoras al registrarse.
create table if not exists public.cuentas_autorizadas (
  email        text primary key,
  rol          text not null check (rol in ('ADMIN', 'EMPRENDEDORA')),
  nombre       text,
  tienda_id    text,                 -- opcional: tienda ya creada que se le asigna
  asociacion_id text,
  creado_en    timestamptz not null default now()
);

create table if not exists public.perfiles (
  id         uuid primary key references auth.users (id) on delete cascade,
  email      text,
  nombre     text,
  rol        text not null default 'VISITANTE' check (rol in ('ADMIN', 'EMPRENDEDORA', 'VISITANTE')),
  tienda_id  text,
  foto_url   text,
  creado_en  timestamptz not null default now()
);
-- Bases ya creadas: agrega la foto de perfil sin borrar datos.
alter table public.perfiles add column if not exists foto_url text;

create table if not exists public.asociaciones (
  id        text primary key,
  nombre    text not null,
  localidad text,
  color     bigint
);

create table if not exists public.tiendas (
  id              text primary key,
  owner_id        uuid references auth.users (id) on delete set null,
  nombre          text not null,
  rubro           text default 'Comercio',
  sector          text default 'Alerce',
  bio             text default '',
  whatsapp        text default '',
  instagram       text default '',
  color_portada   bigint,
  color_logo      bigint,
  logo_url        text,
  necesidades     jsonb not null default '{}'::jsonb,
  mostrar_precios boolean not null default true,
  actualizado_en  timestamptz not null default now()
);
-- Bases ya creadas: agrega el logo del negocio sin borrar datos.
alter table public.tiendas add column if not exists logo_url text;

create table if not exists public.productos (
  id             text primary key,
  tienda_id      text not null references public.tiendas (id) on delete cascade,
  nombre         text not null,
  descripcion    text not null default '',
  categoria      text not null default 'TEXTIL',
  precio         numeric not null default 0,
  publicado      boolean not null default false,
  destacado      boolean not null default false,
  color_foto     bigint,
  variantes      jsonb not null default '[]'::jsonb,  -- [{id, nombre, stock}]
  foto_url       text,
  actualizado_en timestamptz not null default now()
);
create index if not exists productos_tienda_idx on public.productos (tienda_id);

-- Historial de ventas: privado, ni el administrador lo ve (RN-10).
create table if not exists public.ventas (
  id          text primary key,
  tienda_id   text not null references public.tiendas (id) on delete cascade,
  fecha       timestamptz not null default now(),
  fecha_local text,                  -- fecha y hora tal como la vio la emprendedora
  items       jsonb not null default '[]'::jsonb,
  medio_pago  text,
  feria_id    text,
  total       numeric not null default 0
);
alter table public.ventas add column if not exists fecha_local text;
create index if not exists ventas_tienda_fecha_idx on public.ventas (tienda_id, fecha desc);

create table if not exists public.ferias (
  id             text primary key,
  nombre         text not null,
  ubicacion      text,
  fecha_inicio   date,
  fecha_fin      date,
  horario        text,
  cupos          int default 20,
  precio_entrada numeric default 0,
  estado         text default 'BORRADOR',
  latitud        double precision,
  longitud       double precision
);

create table if not exists public.participaciones (
  feria_id   text not null references public.ferias (id) on delete cascade,
  tienda_id  text not null references public.tiendas (id) on delete cascade,
  invitacion text not null default 'PENDIENTE',
  asistencia text not null default 'PENDIENTE',
  primary key (feria_id, tienda_id)
);

-- ---------- 2. Funciones de apoyo ----------

create or replace function public.es_admin()
returns boolean language sql stable security definer set search_path = public as $$
  select exists (select 1 from public.perfiles where id = auth.uid() and rol = 'ADMIN');
$$;

create or replace function public.es_duena_de(p_tienda text)
returns boolean language sql stable security definer set search_path = public as $$
  select exists (select 1 from public.tiendas where id = p_tienda and owner_id = auth.uid());
$$;

-- Al registrarse alguien (Auth), el servidor crea su perfil. Las cuentas de
-- administración tienen prioridad; cualquier alta que solicite ser
-- EMPRENDEDORA recibe tienda propia. Nunca se acepta ADMIN desde metadatos.
create or replace function public.al_crear_usuario()
returns trigger language plpgsql security definer set search_path = public as $$
declare
  v_rol    text;
  v_nombre text;
  v_tienda text;
begin
  select rol, nombre, tienda_id into v_rol, v_nombre, v_tienda
  from public.cuentas_autorizadas where lower(email) = lower(new.email);

  if v_rol is null and upper(coalesce(new.raw_user_meta_data ->> 'tipo_cuenta', '')) = 'EMPRENDEDORA' then
    v_rol := 'EMPRENDEDORA';
  end if;
  v_rol := coalesce(v_rol, 'VISITANTE');
  v_nombre := coalesce(nullif(new.raw_user_meta_data ->> 'nombre', ''), v_nombre, split_part(new.email, '@', 1));

  if v_rol = 'EMPRENDEDORA' then
    if v_tienda is not null then
      update public.tiendas
         set owner_id = new.id
       where id = v_tienda and (owner_id is null or owner_id = new.id);
      if not found then v_tienda := null; end if;
    end if;
    if v_tienda is null then
      v_tienda := 'tienda_' || replace(new.id::text, '-', '');
      insert into public.tiendas (id, owner_id, nombre, rubro, sector, bio, whatsapp)
      values (
        v_tienda,
        new.id,
        v_nombre,
        coalesce(nullif(new.raw_user_meta_data ->> 'rubro', ''), 'Comercio y servicios'),
        coalesce(nullif(new.raw_user_meta_data ->> 'sector', ''), 'Alerce'),
        'Emprendimiento local de Alerce.',
        coalesce(nullif(new.raw_user_meta_data ->> 'whatsapp', ''), '')
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

-- Cada persona puede cambiar SOLO su nombre y su foto. No puede tocar rol,
-- correo ni tienda (RN-01): la función ignora esos datos.
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

-- Una persona con cuenta compradora puede abrir su propia tienda desde Mi perfil.
-- La función impide crear una segunda tienda o cambiar una cuenta ADMIN.
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

-- ---------- 3. Seguridad por fila (RLS) ----------

alter table public.cuentas_autorizadas enable row level security;
alter table public.perfiles            enable row level security;
alter table public.asociaciones        enable row level security;
alter table public.tiendas             enable row level security;
alter table public.productos           enable row level security;
alter table public.ventas              enable row level security;
alter table public.ferias              enable row level security;
alter table public.participaciones     enable row level security;

-- cuentas_autorizadas: solo el administrador
drop policy if exists cuentas_admin on public.cuentas_autorizadas;
create policy cuentas_admin on public.cuentas_autorizadas
  for all to authenticated using (public.es_admin()) with check (public.es_admin());

-- perfiles: cada uno ve el suyo; el admin ve y edita todos
drop policy if exists perfiles_ver on public.perfiles;
create policy perfiles_ver on public.perfiles
  for select to authenticated using (id = auth.uid() or public.es_admin());
drop policy if exists perfiles_admin on public.perfiles;
create policy perfiles_admin on public.perfiles
  for update to authenticated using (public.es_admin()) with check (public.es_admin());

-- asociaciones y ferias: lectura pública, escritura solo admin (RN-02, RN-04)
drop policy if exists asociaciones_ver on public.asociaciones;
create policy asociaciones_ver on public.asociaciones for select using (true);
drop policy if exists asociaciones_admin on public.asociaciones;
create policy asociaciones_admin on public.asociaciones
  for all to authenticated using (public.es_admin()) with check (public.es_admin());

drop policy if exists ferias_ver on public.ferias;
create policy ferias_ver on public.ferias for select using (true);
drop policy if exists ferias_admin on public.ferias;
create policy ferias_admin on public.ferias
  for all to authenticated using (public.es_admin()) with check (public.es_admin());

-- tiendas: todos las ven; solo su dueña (o el admin) las modifica
drop policy if exists tiendas_ver on public.tiendas;
create policy tiendas_ver on public.tiendas for select using (true);
drop policy if exists tiendas_editar on public.tiendas;
create policy tiendas_editar on public.tiendas
  for update to authenticated
  using (owner_id = auth.uid() or public.es_admin())
  with check (owner_id = auth.uid() or public.es_admin());
drop policy if exists tiendas_admin_crear on public.tiendas;
create policy tiendas_admin_crear on public.tiendas
  for insert to authenticated with check (public.es_admin());
drop policy if exists tiendas_admin_borrar on public.tiendas;
create policy tiendas_admin_borrar on public.tiendas
  for delete to authenticated using (public.es_admin());

-- productos: el público ve los publicados; la dueña ve y maneja todos los suyos
drop policy if exists productos_ver on public.productos;
create policy productos_ver on public.productos
  for select using (publicado or public.es_duena_de(tienda_id));
drop policy if exists productos_crear on public.productos;
create policy productos_crear on public.productos
  for insert to authenticated with check (public.es_duena_de(tienda_id));
drop policy if exists productos_editar on public.productos;
create policy productos_editar on public.productos
  for update to authenticated
  using (public.es_duena_de(tienda_id)) with check (public.es_duena_de(tienda_id));
drop policy if exists productos_borrar on public.productos;
create policy productos_borrar on public.productos
  for delete to authenticated using (public.es_duena_de(tienda_id));

-- ventas: solo la dueña de la tienda (el admin NO tiene acceso, RN-10)
drop policy if exists ventas_duena on public.ventas;
create policy ventas_duena on public.ventas
  for all to authenticated
  using (public.es_duena_de(tienda_id)) with check (public.es_duena_de(tienda_id));

-- participaciones: lectura pública; el admin invita; la emprendedora responde la suya
drop policy if exists participaciones_ver on public.participaciones;
create policy participaciones_ver on public.participaciones for select using (true);
drop policy if exists participaciones_admin on public.participaciones;
create policy participaciones_admin on public.participaciones
  for all to authenticated using (public.es_admin()) with check (public.es_admin());
drop policy if exists participaciones_responder on public.participaciones;
create policy participaciones_responder on public.participaciones
  for update to authenticated
  using (public.es_duena_de(tienda_id)) with check (public.es_duena_de(tienda_id));

grant usage on schema public to anon, authenticated;
grant select on public.asociaciones, public.tiendas, public.productos, public.ferias, public.participaciones to anon;
grant select, insert, update, delete on all tables in schema public to authenticated;
grant execute on function public.es_admin(), public.es_duena_de(text) to anon, authenticated;

-- ---------- 4. Fotos (Storage) ----------
-- Bucket público "productos". Cada usuaria sube solo dentro de su carpeta (uid/...).

insert into storage.buckets (id, name, public)
values ('productos', 'productos', true)
on conflict (id) do nothing;

drop policy if exists fotos_ver on storage.objects;
create policy fotos_ver on storage.objects
  for select using (bucket_id = 'productos');
drop policy if exists fotos_subir on storage.objects;
create policy fotos_subir on storage.objects
  for insert to authenticated
  with check (bucket_id = 'productos' and (storage.foldername(name))[1] = auth.uid()::text);
drop policy if exists fotos_reemplazar on storage.objects;
create policy fotos_reemplazar on storage.objects
  for update to authenticated
  using (bucket_id = 'productos' and (storage.foldername(name))[1] = auth.uid()::text);
drop policy if exists fotos_borrar on storage.objects;
create policy fotos_borrar on storage.objects
  for delete to authenticated
  using (bucket_id = 'productos' and (storage.foldername(name))[1] = auth.uid()::text);

-- Bucket público "perfiles": foto de cada usuaria (compradora o emprendedora).
-- Igual que las fotos de productos, cada persona escribe solo en su carpeta uid/.
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
