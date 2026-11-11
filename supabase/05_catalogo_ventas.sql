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
