# Tipos de producto y campos específicos (RF-PRO-03)

Los requisitos dicen que los campos específicos se definen «según los productos que se identifiquen en la visita». En el repositorio **no hay una lista acordada con la contraparte**. Por eso:

- Se implementó el **mecanismo**: tabla `tipos_producto` (05), lectura en la web y en la app, campos de tipo texto, número u opciones, y obligatorios validados al guardar.
- La migración **no inserta tipos**. Los únicos ejemplos existen dentro de las pruebas y se llaman «provisional».
- **A05 no se puede dar por validado** hasta cargar los tipos reales confirmados con la contraparte.

## Cómo los carga el administrador (cuando estén acordados)

```sql
-- Ejemplo de FORMATO (reemplazar por los tipos acordados con la contraparte)
insert into public.tipos_producto (id, categoria, nombre, campos, orden) values
  ('<id>', 'TEXTIL', '<Nombre visible>',
   '[{"clave":"talla","etiqueta":"Talla","tipo":"opciones","opciones":["S","M","L"],"obligatorio":true},
     {"clave":"material","etiqueta":"Material","tipo":"texto"}]'::jsonb, 1);
```

- `categoria` debe ser una de: TEXTIL, CUERO, JOYERIA, COSMETICA, GOURMET, DECORACION.
- `tipo` del campo: `texto`, `numero` u `opciones` (con lista `opciones`).
- Para retirar un tipo sin romper productos existentes: `update tipos_producto set activo = false where id = '…'`.
- La app los descarga al iniciar sesión y los guarda para usarlos sin señal. La web los lee al cargar.
