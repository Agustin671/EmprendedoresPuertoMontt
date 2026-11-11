-- =====================================================================
-- Datos TEMPORALES para demostrar la Entrega 5 en la instancia real.
-- Los ejecuta el administrador en Supabase Studio → SQL Editor.
-- Todo lleva el prefijo «e5prueba» y se borra con el bloque final.
-- NO son datos reales: la feria no es un evento y el tipo no está
-- validado por la contraparte (no cierra A05).
-- =====================================================================

-- 1) Feria temporal para probar «vender en una feria de la plataforma».
insert into public.ferias (id, nombre, ubicacion, fecha_inicio, fecha_fin, estado)
values ('e5prueba_feria', 'PRUEBA E5 · feria temporal (no es un evento real)', 'Prueba', current_date, current_date, 'BORRADOR')
on conflict (id) do nothing;

-- 2) Tipo de producto temporal para probar el mecanismo de campos específicos.
insert into public.tipos_producto (id, categoria, nombre, campos, orden)
values ('e5prueba_tipo', 'TEXTIL', 'PRUEBA E5 · tipo provisional',
        '[{"clave":"talla","etiqueta":"Talla","tipo":"opciones","opciones":["S","M","L"],"obligatorio":true},
          {"clave":"material","etiqueta":"Material","tipo":"texto"}]'::jsonb, 99)
on conflict (id) do nothing;

-- =====================================================================
-- LIMPIEZA (ejecutar al terminar las pruebas)
-- Las ventas temporales conservan feria_id como texto; no hay clave foránea.
-- delete from public.tipos_producto where id = 'e5prueba_tipo';
-- delete from public.ferias where id = 'e5prueba_feria';
-- =====================================================================
