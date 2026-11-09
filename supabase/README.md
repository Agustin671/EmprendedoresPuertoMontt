# Backend Supabase — alerceshopin (Grupo ALERCEIS7)

La app Android y la web usan Supabase para cuentas (Auth), datos (tablas con RLS) y fotos (Storage).

## Puesta en marcha (una vez)

1. Entra a Studio de la instancia del grupo → **SQL Editor**.
2. Pega y ejecuta `01_esquema.sql` (tablas, reglas de seguridad y buckets).
3. Pega y ejecuta `02_datos_prueba.sql` (datos ficticios: 4 tiendas de Alerce, sus productos y cuentas autorizadas).
4. Ejecuta `03_migracion_fotos.sql` para configurar el bucket de perfiles y la función segura de actualización de nombre/foto (es idempotente).
5. Ejecuta `04_registro_vendedores.sql` para que cualquier persona pueda crear una cuenta de emprendedora, recibir su tienda y convertir una cuenta compradora existente en tienda propia.
6. En `App para el celular/`, copia `supabase.properties.ejemplo` como `supabase.properties` y completa URL y **anon key** (nunca `service_role`). El archivo está en `.gitignore`.
7. Configura los secretos `SUPABASE_URL` y `SUPABASE_ANON_KEY` en GitHub Actions para generar la configuración web.
8. En Supabase Auth, desactiva confirmación de correo si el registro debe iniciar sesión inmediatamente. Sin `supabase.properties`, la app funciona con datos de ejemplo.

Los scripts SQL se pueden volver a ejecutar sin romper nada.

## Cuentas de prueba

El rol lo crea el servidor (RN-01). En la app, elige “Emprendedora” para recibir una tienda propia o “Compradora” para entrar a la vitrina. ADMIN sigue reservado a la lista `cuentas_autorizadas`. Para usar una cuenta de prueba, regístrala una vez desde la app con cualquier contraseña de 6+ caracteres:

| Correo | Rol | Tienda |
|---|---|---|
| admin@alerce.app | Administrador | — |
| donamarta@alerce.app | Emprendedora | Minimarket Doña Marta |
| barberia@alerce.app | Emprendedora | Barbería & Estilo Alerce |
| tejidos@alerce.app | Emprendedora | Tejidos & Lanares Alerce Austral |
| reposteria@alerce.app | Emprendedora | Repostería Casera Tradición Sureña |

Cualquier otro correo se crea como **comprador**, salvo que la persona elija “Emprendedora”. Si la cuenta ya existe como compradora, puede abrir “Mi perfil” y crear su tienda sin registrar otra cuenta.

## Modelo de datos (RNF-DAT-01)

| Tabla | Para qué | Quién lee | Quién escribe |
|---|---|---|---|
| `cuentas_autorizadas` | Correos que el admin autoriza como emprendedora o admin | Admin | Admin |
| `perfiles` | Un perfil por cuenta: nombre, rol, tienda y foto (lo crea un trigger al registrarse) | La propia persona y el admin | Admin (cambio de rol); la propia persona cambia nombre y foto con la función `actualizar_mi_perfil` |
| `tiendas` | Ficha pública de cada emprendimiento, con logo | Todos, sin sesión | Su dueña (`owner_id`) o el admin |
| `productos` | Catálogo; `variantes` es una lista `[{id, nombre, stock}]` | Publicados: todos. No publicados: solo la dueña | Su dueña |
| `ventas` | Historial privado de ventas (`items` en JSON) | Solo la dueña — **ni el admin** (RN-10) | Su dueña |
| `asociaciones` | Agrupaciones de emprendedoras | Todos | Admin (RN-04) |
| `ferias`, `participaciones` | Ferias e invitaciones | Todos | Admin; la emprendedora responde su invitación (RN-02, RN-03) |

Relaciones: `perfiles.id = auth.users.id`; `tiendas.owner_id → auth.users`; `productos.tienda_id → tiendas`; `ventas.tienda_id → tiendas`; `participaciones (feria_id, tienda_id)`.

Índices: `productos(tienda_id)` para el catálogo de cada tienda; `ventas(tienda_id, fecha desc)` para el historial ordenado.

Fotos: buckets públicos `productos` (fotos de productos y logo del negocio) y `perfiles` (foto de cada usuaria). Cada persona solo puede subir dentro de su carpeta `uid/…`. La app comprime las fotos antes de subirlas (lado mayor 1280 px, JPEG 80 %, RNF-REN-01).

## Sin señal (RNF-REN-02, RNF-DAT-03)

Todo cambio se guarda primero en el celular y queda en una cola «por subir» (`Sincronizador.kt`). Se sube apenas hay conexión y se reintenta al abrir la app o al volver a entrar. Al entrar, se trae la tienda, los productos y el historial sin pisar lo que falta subir.

## Pruebas de reglas de seguridad (RNF-SEG-04)

`pruebas_rls.sql` prueba los permisos por perfil, incluido el registro abierto de vendedoras, la creación de productos en la tienda propia y los rechazos de acceso cruzado. Se ejecuta en un **Postgres local**, nunca en la instancia del curso:

```bash
psql -h localhost -U postgres -c "create database prueba_alerce"
psql -h localhost -U postgres -d prueba_alerce -f stub_local.sql      # simula auth/storage de Supabase
psql -h localhost -U postgres -d prueba_alerce -f 01_esquema.sql
psql -h localhost -U postgres -d prueba_alerce -f 02_datos_prueba.sql
psql -h localhost -U postgres -d prueba_alerce -f pruebas_rls.sql     # imprime OK / FALLA por caso
```

Resultado esperado: cada caso debe imprimir `OK`. No se ha ejecutado esta batería en esta entrega; no uses el resultado histórico como verificación del esquema actual.
