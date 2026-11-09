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
