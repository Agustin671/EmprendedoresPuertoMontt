# Backend Supabase — alerceshopin (Grupo ALERCEIS7)

La app Android usa la instancia Supabase del grupo para cuentas (Auth), datos (tablas con RLS) y fotos (Storage).

## Puesta en marcha (una vez)

1. Entra a Studio de la instancia del grupo → **SQL Editor**.
2. Pega y ejecuta `01_esquema.sql` (tablas, reglas de seguridad, bucket de fotos).
3. Pega y ejecuta `02_datos_prueba.sql` (datos ficticios: 4 tiendas de Alerce, sus productos y cuentas autorizadas).
4. En la carpeta `App para el celular/`, copia `supabase.properties.ejemplo` como `supabase.properties` y completa la URL y la **anon key** (nunca la `service_role`). Ese archivo está en `.gitignore`: no se sube al repositorio (RNF-SEG-03).
5. Compila la app. Si `supabase.properties` falta, la app funciona solo con datos de ejemplo y lo avisa.

Los dos scripts se pueden volver a ejecutar sin romper nada.

## Cuentas de prueba

El registro **no es libre** (RN-01): el rol lo decide el servidor según la tabla `cuentas_autorizadas`. Para usar una cuenta de prueba, regístrala una vez desde la app («¿No tienes cuenta? Regístrate aquí») con cualquier contraseña de 6+ caracteres:

| Correo | Rol | Tienda |
|---|---|---|
| admin@alerce.app | Administrador | — |
| donamarta@alerce.app | Emprendedora | Minimarket Doña Marta |
| barberia@alerce.app | Emprendedora | Barbería & Estilo Alerce |
| tejidos@alerce.app | Emprendedora | Tejidos & Lanares Alerce Austral |
| reposteria@alerce.app | Emprendedora | Repostería Casera Tradición Sureña |

Cualquier otro correo queda como **comprador**. Cuando el administrador da de alta una emprendedora en la app, su correo queda autorizado; al registrarse, recibe su tienda automáticamente.

## Modelo de datos (RNF-DAT-01)

| Tabla | Para qué | Quién lee | Quién escribe |
|---|---|---|---|
| `cuentas_autorizadas` | Correos que el admin autoriza como emprendedora o admin | Admin | Admin |
| `perfiles` | Un perfil por cuenta: nombre, rol y tienda (lo crea un trigger al registrarse) | La propia persona y el admin | Admin (cambio de rol) |
| `tiendas` | Ficha pública de cada emprendimiento | Todos, sin sesión | Su dueña (`owner_id`) o el admin |
| `productos` | Catálogo; `variantes` es una lista `[{id, nombre, stock}]` | Publicados: todos. No publicados: solo la dueña | Su dueña |
| `ventas` | Historial privado de ventas (`items` en JSON) | Solo la dueña — **ni el admin** (RN-10) | Su dueña |
| `asociaciones` | Agrupaciones de emprendedoras | Todos | Admin (RN-04) |
| `ferias`, `participaciones` | Ferias e invitaciones | Todos | Admin; la emprendedora responde su invitación (RN-02, RN-03) |

Relaciones: `perfiles.id = auth.users.id`; `tiendas.owner_id → auth.users`; `productos.tienda_id → tiendas`; `ventas.tienda_id → tiendas`; `participaciones (feria_id, tienda_id)`.

Índices: `productos(tienda_id)` para el catálogo de cada tienda; `ventas(tienda_id, fecha desc)` para el historial ordenado.

Fotos: bucket público `productos`. Cada usuaria solo puede subir dentro de su carpeta `uid/…`. La app comprime las fotos antes de subirlas (lado mayor 1280 px, JPEG 80 %, RNF-REN-01).

## Sin señal (RNF-REN-02, RNF-DAT-03)

Todo cambio se guarda primero en el celular y queda en una cola «por subir» (`Sincronizador.kt`). Se sube apenas hay conexión y se reintenta al abrir la app o al volver a entrar. Al entrar, se trae la tienda, los productos y el historial sin pisar lo que falta subir.

## Pruebas de reglas de seguridad (RNF-SEG-04)

`pruebas_rls.sql` prueba 26 casos por perfil (admin, emprendedora, comprador y sin sesión), incluidos los rechazos esperados. Se ejecuta en un **Postgres local**, nunca en la instancia del curso:

```bash
psql -h localhost -U postgres -c "create database prueba_alerce"
psql -h localhost -U postgres -d prueba_alerce -f stub_local.sql      # simula auth/storage de Supabase
psql -h localhost -U postgres -d prueba_alerce -f 01_esquema.sql
psql -h localhost -U postgres -d prueba_alerce -f 02_datos_prueba.sql
psql -h localhost -U postgres -d prueba_alerce -f pruebas_rls.sql     # imprime OK / FALLA por caso
```

Resultado actual: **26 de 26 casos correctos**.
