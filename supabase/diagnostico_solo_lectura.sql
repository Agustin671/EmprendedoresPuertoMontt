-- =====================================================================
-- Diagnóstico de SOLO LECTURA de la instancia (para el administrador).
-- Ejecutar en Supabase Studio → SQL Editor. No modifica nada: solo SELECT.
-- Responde: versión de PostgreSQL, qué migraciones están aplicadas (por
-- su efecto real, no por la existencia de archivos), políticas, permisos,
-- buckets y cantidades de registros (sin mostrar datos personales).
-- Copia el resultado de cada bloque y compártelo con el grupo.
-- =====================================================================

-- 1. Versión de PostgreSQL (05 está probada en 15 y 17)
select version() as version_postgres, current_setting('server_version_num') as version_num;

-- 2. Columnas que agrega cada migración
select 'perfiles.foto_url (03)' as elemento, exists (select 1 from information_schema.columns where table_schema='public' and table_name='perfiles' and column_name='foto_url') as presente
union all select 'tiendas.logo_url (03)', exists (select 1 from information_schema.columns where table_schema='public' and table_name='tiendas' and column_name='logo_url')
union all select 'productos.modalidad (05)', exists (select 1 from information_schema.columns where table_schema='public' and table_name='productos' and column_name='modalidad')
union all select 'productos.vendida_en (05)', exists (select 1 from information_schema.columns where table_schema='public' and table_name='productos' and column_name='vendida_en')
union all select 'tiendas.historial_activo (05)', exists (select 1 from information_schema.columns where table_schema='public' and table_name='tiendas' and column_name='historial_activo')
union all select 'ventas.huella (05)', exists (select 1 from information_schema.columns where table_schema='public' and table_name='ventas' and column_name='huella')
union all select 'tabla tipos_producto (05)', to_regclass('public.tipos_producto') is not null
union all select 'tabla migraciones_aplicadas (05)', to_regclass('public.migraciones_aplicadas') is not null;

-- 3. Funciones (03: actualizar_mi_perfil · 04: crear_mi_tienda · 05: el resto)
select p.proname as funcion, pg_get_function_identity_arguments(p.oid) as argumentos,
       case when p.prosecdef then 'security definer' else 'invoker' end as seguridad
  from pg_proc p join pg_namespace n on n.oid = p.pronamespace
 where n.nspname = 'public'
   and p.proname in ('es_admin','es_duena_de','al_crear_usuario','actualizar_mi_perfil','crear_mi_tienda',
                     'estado_publico_producto','unidades_vendibles','proteger_producto','mi_tienda_id',
                     'tienda_del_producto','guardar_producto','registrar_venta')
 order by 1;

-- 4. ¿El trigger de 04 tiene la versión nueva? (busca el texto característico)
select position('tipo_cuenta' in pg_get_functiondef('public.al_crear_usuario'::regproc)) > 0 as al_crear_usuario_acepta_tipo_cuenta;

-- 5. Triggers
select event_object_table as tabla, trigger_name, action_timing, string_agg(event_manipulation, ',') as eventos
  from information_schema.triggers
 where (event_object_schema = 'public') or (event_object_schema = 'auth' and event_object_table = 'users')
 group by 1, 2, 3 order by 1, 2;

-- 6. Políticas RLS de las tablas del proyecto
select tablename, policyname, cmd, roles, qual, with_check
  from pg_policies where schemaname = 'public' order by tablename, policyname;

-- 7. RLS activada por tabla
select relname as tabla, relrowsecurity as rls_activa
  from pg_class c join pg_namespace n on n.oid = c.relnamespace
 where n.nspname = 'public' and c.relkind = 'r' order by 1;

-- 8. Permisos de escritura directa sobre ventas (después de 05 deben ser solo SELECT)
select grantee, privilege_type from information_schema.role_table_grants
 where table_schema = 'public' and table_name = 'ventas' and grantee in ('anon','authenticated') order by 1, 2;

-- 9. Storage: buckets y políticas
select id, public from storage.buckets where id in ('productos','perfiles');
select policyname, cmd, qual, with_check from pg_policies where schemaname = 'storage' and tablename = 'objects' order by policyname;

-- 10. Cantidades (sin datos personales)
select (select count(*) from public.tiendas) as tiendas,
       (select count(*) from public.productos) as productos,
       (select count(*) from public.ventas) as ventas,
       (select count(*) from public.perfiles) as perfiles,
       (select count(*) from public.ferias) as ferias;

-- 11. Formato de opciones existentes (sin id → 05 les asignará uno)
select count(*) filter (where jsonb_typeof(variantes) <> 'array') as variantes_no_lista,
       count(*) filter (where exists (select 1 from jsonb_array_elements(case when jsonb_typeof(variantes)='array' then variantes else '[]' end) v where coalesce(v->>'id','') = '')) as productos_con_opcion_sin_id,
       count(*) filter (where foto_url like 'data:%') as fotos_base64
  from public.productos;

-- 12. Registro de migraciones (existe solo después de 05)
select * from public.migraciones_aplicadas;  -- si da error "does not exist", 05 no está aplicada
