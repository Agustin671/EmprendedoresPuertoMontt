# Desarrollo y pruebas (reproducible)

Todo corre en el computador, sin tocar la instancia real.

## Requisitos

| Herramienta | Para qué | Cómo |
|---|---|---|
| Node.js 22+ | pruebas web, SQL y servidor local | `node --version` |
| `npm install` | PostgreSQL 17 empaquetado (`embedded-postgres`), cliente `pg`, Playwright | en la raíz del repositorio |
| PostgreSQL 15 (opcional) | probar también en 15 | `npm install --prefix .herramientas/pg15 --no-save @embedded-postgres/windows-x64@15.19.0-beta.19` (en macOS/Linux, el paquete de su plataforma) |
| Microsoft Edge | recorridos visibles (Playwright usa el Edge instalado; no descarga navegadores) | otro canal: `PW_CANAL=chrome` |
| JDK de Android Studio (JBR) + SDK | compilar y probar la app | `JAVA_HOME` apuntando a `Android Studio/jbr`; `local.properties` con `sdk.dir` (no se versiona) |

`node_modules/`, `.herramientas/`, `tests/.pgdata-*` y `test-results/` están en `.gitignore`.

## Comandos

```bash
npm run test:web    # reglas del catálogo y contrato con Supabase (node --test)
```
```bash
npm run test:sql    # PostgreSQL desechable: instalación limpia, actualización, RLS, ventas, concurrencia, reversión
```
```bash
npm run test:e2e    # recorridos visibles de la web en Edge con un Supabase sustituto
```
```bash
npm run servir      # web local en http://localhost:4173 (modo demostración si no existe js/config.js)
```

Android (desde `App para el celular`):

```bash
./gradlew testDebugUnitTest assembleDebug
```

## Qué prueba cada batería

- **SQL (real, no simulada):**
  - `tests/sql/ejecutar.js` levanta PostgreSQL 15 y 17 locales.
  - Aplica `stub_local.sql` (simula `auth`/`storage` de Supabase).
  - Ejecuta `pruebas_rls.sql` (SQL puro) y `pruebas_entrega5.js`: visibilidad anónima, aislamiento entre tiendas, ventas privadas frente a ADMIN, Storage, idempotencia, sobreventa, piezas únicas, historial, clientes antiguos.
  - Ejecuta `concurrencia.js` con dos conexiones independientes: última unidad, reintentos simultáneos, orden cruzado sin interbloqueos, reposición mientras se vende.
  - Escenario de actualización: el esquema del commit `6859b8f` con datos antiguos, luego 03, 04 y 05.
  - Diagnóstico (sin cambios) y `05_revertir.sql` con reaplicación.
- **Web unitarias:** `tests/web/*.test.js`.
- **Web recorridos:** `tests/e2e/web.spec.js`. Usa `tests/e2e/supabase_falso.js` (imita supabase-js; **no** prueba RLS). Cubre: vitrina, opciones, XSS, errores y reintento, catálogo vacío, sin configuración, Google no habilitado, alta con error y éxito, venta con reintento idempotente, cierre de sesión, anchos de 320 a 1280 px y foco en modales.
- **Android:** `app/src/test/.../Entrega5Test.kt`: contrato compartido, JSON, ventas y conflictos, borradores, borrados pendientes, estado antiguo y filtros del historial.

## Prueba en emulador (manual, con el AVD de Android Studio)

1. Instala la APK de depuración (`adb install -r app/build/outputs/apk/debug/app-debug.apk`).
2. En el inicio de sesión, abre «Tiendas de prueba (Modo Desarrollador)». Solo existe en el APK de depuración y funciona sin servidor.

Lo verificado así en esta entrega: alta guiada, foto con la cámara virtual, recuperación del borrador tras forzar el cierre, opción con precio propio, bloqueo de sobreventa, venta con lugar, historial filtrado, configuración y vista previa.

La sincronización con Supabase necesita `supabase.properties` y la migración 05 aplicada.
