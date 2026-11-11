-- =====================================================================
-- Reversión PARCIAL de 05_catalogo_ventas.sql (solo con autorización).
-- Lee docs/entrega5/administrador.md, sección «Recuperación», antes de usarlo.
--
-- Qué hace: devuelve el comportamiento anterior de permisos y lectura:
--   * la política de lectura de productos vuelve a "publicado o dueña";
--   * quita el trigger que protege cantidades;
--   * devuelve INSERT/UPDATE en ventas a usuarios autenticados (APK 1.2);
--   * quita las funciones nuevas.
-- Qué NO hace (a propósito, para no perder datos):
--   * no borra columnas nuevas ni sus valores (modalidad, vendida_en, etc.);
--   * no borra ventas registradas con registrar_venta();
--   * no recupera cantidades: si hubo ventas después de aplicar 05, sus
--     descuentos ya están hechos y son correctos.
-- Después de revertir, la web y el APK 1.3 dejan de funcionar para guardar y
-- vender (usan guardar_producto/registrar_venta): hay que volver a la web y
-- al APK anteriores. Si hay datos dañados, se restaura el respaldo previo.
-- =====================================================================
begin;

drop policy if exists productos_ver on public.productos;
create policy productos_ver on public.productos
  for select using (publicado or public.es_duena_de(tienda_id));

drop trigger if exists proteger_producto on public.productos;
drop function if exists public.proteger_producto();
drop function if exists public.registrar_venta(jsonb);
drop function if exists public.guardar_producto(jsonb);
drop function if exists public.tienda_del_producto(text);
drop function if exists public.mi_tienda_id();

grant insert, update, delete on public.ventas to authenticated;

delete from public.migraciones_aplicadas where nombre = '05_catalogo_ventas';
notify pgrst, 'reload schema';
commit;
