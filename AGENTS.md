# Guía para asistentes de IA (y personas nuevas en el repo)

Proyecto: **alerceshopin** (Grupo 1 ALERCEIS7) · *Programación en Android 2026-2* · Emprendedores Puerto Montt.
Plataforma para emprendedoras de Alerce: **app Android** (principal) + **vitrina web** (GitHub Pages).
Requisitos oficiales del curso: <https://github.com/rinostrozacl/ProyectoVinculacionConElMedio2026> (`docs/requerimientos-base.md`, `docs/planificacion.md`, `docs/problematicas.md`).

> Lee esta guía completa antes de cambiar código. Las secciones 6 y 7 dicen qué falta y cómo hacerlo.

---

## 1. Reglas que no se rompen

- **Nunca subas credenciales al repo** (RNF-SEG-03). La URL y la *anon key* de Supabase van en `App para el celular/supabase.properties` (está en `.gitignore`). No las copies a ningún archivo versionado, ni a esta guía. La `service_role` es del profesor: no se usa ni se pide.
- **Interfaz en español de Chile** (RNF-IDI-01): nada de voseo ("creá", "tocá"). Palabras simples para la emprendedora: "Mis productos", "Cuántos tengo", "Se acabó", "Solo yo lo veo / Lo ven todos". Evita "stock", "variante", "publicar", "borrador" en pantalla.
- **Botones de 48 dp mínimo** y respetar el tamaño de letra del celular (RNF-USA-02/03).
- **Las pantallas no llaman a la base de datos**: todo pasa por repositorios (RNF-DAT-02).
- **El rol lo decide el servidor**, no la app ni la web (RN-01). Ver sección 4.

## 2. Estructura

```
├── AGENTS.md                      ← esta guía
├── App para el celular/           ← app Android (Kotlin + Jetpack Compose)
│   ├── supabase.properties        ← credenciales LOCALES (no está en git)
│   ├── supabase.properties.ejemplo
│   └── app/src/main/java/com/example/trabajodea10/
│       ├── AplicacionTrama.kt     ← arranque: ClienteSupabase, Sincronizador, TramaStore
│       ├── MainActivity.kt        ← FragmentActivity (necesario para la huella)
│       ├── TramaApp.kt            ← Scaffold + elige pantalla según la ruta
│       ├── data/
│       │   ├── Models.kt          ← modelos (Producto, Tienda, Venta, Feria…)
│       │   ├── TramaStore.kt      ← estado en memoria + guardado local (JSON)
│       │   ├── SesionManager.kt   ← login, registro, huella, vitrina (reemplaza a FirebaseManager)
│       │   ├── FotoProductoStore.kt ← comprime y sube fotos
│       │   ├── FirebaseManager.kt ← VACÍO, se puede borrar
│       │   └── remoto/
│       │       ├── ClienteSupabase.kt ← HTTP (OkHttp) a Auth, PostgREST y Storage
│       │       ├── Repositorios.kt    ← Perfil, Cuenta, Tienda, Producto, Venta
│       │       └── Sincronizador.kt   ← cola de cambios "por subir" (sin señal)
│       └── ui/ (admin, ajustes, auth, componentes, emprendedora, navegacion, publico, theme)
├── LA PAgina (segun)/proyectode10/ ← vitrina web (HTML + JS sin framework)
│   ├── index.html, css/styles.css
│   └── js/main.js                 ← TODO el JS de la web (~3.500 líneas)
├── supabase/                      ← backend
│   ├── 01_esquema.sql             ← tablas, RLS, trigger, bucket (se puede re-ejecutar)
│   ├── 02_datos_prueba.sql        ← datos ficticios (4 tiendas de Alerce)
│   ├── pruebas_rls.sql            ← 26 pruebas de seguridad por perfil
│   ├── stub_local.sql             ← simula auth/storage para probar en Postgres local
│   └── README.md                  ← modelo de datos y puesta en marcha
├── index.html                     ← redirige a la vitrina (conserva ?tienda=ID)
└── .github/workflows/deploy-pages.yml ← publica la web en GitHub Pages
```

## 3. Cómo está hecha la app

- **Navegación propia** (sin Navigation Compose): `ui/navegacion/Navegacion.kt` tiene `sealed class Ruta` y `ControladorNavegacion` (una pila). `TramaApp.kt` muestra la pantalla de la ruta actual.
- **Estado**: `TramaStore` (objeto con listas `mutableStateListOf`) es la fuente para la UI. Todo cambio llama a `persistir()` (JSON en SharedPreferences) y a `Sincronizador` (servidor).
- **Flujo de un cambio** (ej. guardar producto): pantalla → `TramaStore.guardarProducto()` → guarda local → `Sincronizador.productoCambiado(id)` → si hay red, `ProductoRepositorio.guardar()`; si no, queda en la cola y se reintenta al abrir la app o al entrar.
- **Al entrar** (`SesionManager.prepararSesion`): lee `perfiles` → si es emprendedora trae su tienda, productos e historial (sin pisar lo pendiente).
- **Huella**: después de entrar con contraseña se ofrece activarla. La próxima vez, `SesionManager.ingresarConHuella()` renueva el refresh token guardado. La app no guarda datos biométricos.
- **Fotos**: `FotoProductoStore.copiarDesdeUri` comprime (1280 px, JPEG 80 %) y corrige rotación. Al sincronizar se sube a Storage `productos/<uid>/<productoId>_<ts>.jpg` y se guarda la URL pública.
- **"Modo Desarrollador"** en el login (tiendas de prueba sin servidor): solo aparece en builds debug (`BuildConfig.DEBUG`).
- **HTTP**: OkHttp directo contra la API REST de Supabase (no se usa supabase-kt). Actualizar tiendas usa `PATCH` (la emprendedora no tiene permiso de INSERT en `tiendas`, así que un *upsert* fallaría). Productos y ventas usan *upsert* (`POST … ?on_conflict=id` + `Prefer: resolution=merge-duplicates`).

### Compilar

1. Copiar `App para el celular/supabase.properties.ejemplo` → `supabase.properties` y completar `supabase.url` y `supabase.anonKey` (pedir los datos al grupo; **no** están en el repo).
2. Abrir la carpeta `App para el celular` en Android Studio → Build → Generate App Bundles or APKs → Generate APKs.
3. Salida: `App para el celular/app/build/outputs/apk/debug/app-debug.apk`.

Sin `supabase.properties` la app compila y funciona solo con datos de ejemplo.

## 4. Backend Supabase (resumen; detalle en `supabase/README.md`)

| Tabla | Clave | Lectura | Escritura |
|---|---|---|---|
| `cuentas_autorizadas` | `email` | admin | admin |
| `perfiles` | `id` = `auth.users.id` | la propia persona, admin | admin |
| `tiendas` | `id` (text) | todos (sin sesión también) | dueña (`owner_id`) o admin; crear/borrar solo admin |
| `productos` | `id` (text) | publicados: todos · no publicados: solo la dueña | dueña |
| `ventas` | `id` (text) | **solo la dueña** (ni el admin, RN-10) | dueña |
| `asociaciones`, `ferias` | `id` | todos | admin |
| `participaciones` | (`feria_id`, `tienda_id`) | todos | admin; la dueña responde la suya |

- **Registro no libre (RN-01)**: un trigger en `auth.users` crea el perfil. Si el correo está en `cuentas_autorizadas` como `EMPRENDEDORA`, crea (o asigna) su tienda; si no, queda `VISITANTE` (comprador). El admin autoriza correos desde la app (Emprendedoras → alta).
- Funciones de apoyo: `es_admin()`, `es_duena_de(tienda_id)`.
- **Storage**: bucket público `productos`; cada usuaria escribe solo en su carpeta `uid/…`.
- Cuentas de prueba (se registran una vez desde la app o la web con cualquier contraseña de 6+): `admin@alerce.app`, `donamarta@alerce.app`, `barberia@alerce.app`, `tejidos@alerce.app`, `reposteria@alerce.app`.

### Formato de columnas que importa al conectar la web

| Columna | Formato en la base | En la web hoy |
|---|---|---|
| `tiendas.color_portada`, `color_logo`, `productos.color_foto` | número ARGB, ej. `4281153352` (= `0xFF2D3748`) | texto `"#2D3748"` |
| `productos.variantes` | `[{ "id": "v1", "nombre": "Frasco", "stock": 12 }]` | `[{ nombre, precio }]` + `stockTotal` aparte |
| `productos.precio` | un solo precio por producto | precio por variante |
| `productos.categoria` | `TEXTIL`, `CUERO`, `JOYERIA`, `COSMETICA`, `GOURMET`, `DECORACION` | texto libre |
| `productos.publicado` | boolean; si es `false` el público no lo ve (RLS) | no existe |
| `tiendas.necesidades` | JSON `{tieneStock, tieneAgendamiento, tieneVentaPorWhatsApp, tieneProductosAPedido, participaEnFerias, tieneForoComunidad}` | igual |
| `ventas.items` | `[{productoId, varianteId, nombre, detalle, precio, cantidad, fotoUrl}]`; `fecha_local` = `"AAAA-MM-DD HH:mm"` | distinto (ver POS) |

Conversión de color en JS:

```js
const aHex = n => "#" + (Number(n) & 0xFFFFFF).toString(16).padStart(6, "0").toUpperCase();
const aArgb = hex => 0xFF000000 + parseInt(hex.replace("#", ""), 16);
```

## 5. Qué está hecho (al 3-nov-2026)

- App conectada a Supabase: login/registro, roles del servidor, huella, tienda, productos, ventas, fotos, cola sin señal.
- Esquema SQL con RLS + 26 pruebas por perfil que pasan (`supabase/pruebas_rls.sql`).
- Mejoras de uso: "Mis productos" solo muestra los de la propia tienda; botón "Se acabó" con Deshacer; dictado por voz en nombre y descripción; guardado automático de productos nuevos; "¿Cuántos tienes?" (antes se forzaba a 15 unidades); confirmación antes de borrar; letra al 100 % por defecto; vocabulario simple; QR y enlace por tienda (`?tienda=ID`).
- Web: abre la ficha de una tienda si la URL trae `?tienda=ID`; el QR y "Copiar enlace" usan la URL real de GitHub Pages.

## 6. Qué falta

1. **Conectar la web a Supabase** (sección 7). Es lo siguiente.
2. Ferias en el servidor: las tablas `ferias` y `participaciones` existen, pero la app y la web aún usan datos locales (Entrega 7).
3. Ingreso de compradores con Google en la app (RN-07).
4. Seguidores y avisos de feria (Entrega 7).
5. Borrar `data/FirebaseManager.kt` (vacío).

## 7. Tarea: conectar la web con Supabase

**Objetivo:** que la web lea y escriba las mismas tablas que la app, para que ambas muestren lo mismo.

**Pasos sugeridos**

1. **Configuración sin credenciales en el repo.**
   - Crear `LA PAgina (segun)/proyectode10/js/config.ejemplo.js` con `window.SUPABASE_URL = ""; window.SUPABASE_ANON_KEY = "";` y agregar `js/config.js` al `.gitignore`.
   - En GitHub: *Settings → Secrets and variables → Actions* → crear `SUPABASE_URL` y `SUPABASE_ANON_KEY`.
   - En `.github/workflows/deploy-pages.yml`, antes de "Upload artifact":
     ```yaml
           - name: Crear config de Supabase
             run: |
               printf 'window.SUPABASE_URL = "%s";\nwindow.SUPABASE_ANON_KEY = "%s";\n' \
                 "${{ secrets.SUPABASE_URL }}" "${{ secrets.SUPABASE_ANON_KEY }}" \
                 > "./LA PAgina (segun)/proyectode10/js/config.js"
     ```
   - Ojo: la anon key igual queda visible en la página publicada (es inevitable en un cliente web; la seguridad la dan las reglas RLS). Confirmar con el profesor.
2. **Cliente**: en `index.html` cargar `config.js` y supabase-js por CDN con versión fija (ej. `https://cdn.jsdelivr.net/npm/@supabase/supabase-js@2.45.4/dist/umd/supabase.min.js`) en vez de los 4 scripts de Firebase. Crear el cliente con `supabase.createClient(window.SUPABASE_URL, window.SUPABASE_ANON_KEY)`.
3. **Capa de datos**: crear `js/datos.js` con funciones `listarTiendas()`, `listarProductosPublicados()`, `productosDeTienda(id)`, `guardarProducto(p)`, `actualizarTienda(t)`, `registrarVenta(v, tiendaId)`, `subirFoto(file, productoId)`, `ingresar(email, clave)`, `registrar(email, clave, nombre)`, `miPerfil()`, `cerrarSesion()`. Ahí va la conversión de formatos de la sección 4. `main.js` solo llama a estas funciones.
4. **Reemplazar en `main.js`** (buscar `firebaseFirestore.` y `firebaseAuth.`):
   - `cargarProductosDesdeFirestore()` → `listarTiendas()` + `listarProductosPublicados()`. Las listas fijas `TIENDAS` / `PRODUCTOS_BASE` quedan solo como respaldo sin conexión.
   - Publicar producto (`…collection("productos").doc(…).set`) → `guardarProducto()`, con `tienda_id` de la emprendedora con sesión, `publicado: true` y la foto subida a Storage (comprimir antes, RNF-REN-01).
   - Venta del POS (`…collection("ventas").add`) → `registrarVenta()`, que también descuenta stock en `variantes` del producto.
   - Guardar tienda (`collection("tiendas").doc(id).set(…, {merge:true})`) → `actualizarTienda()` (UPDATE por `id`, no upsert).
   - Login y registro (`createUserWithEmailAndPassword`, `signInWithEmailAndPassword`, `onAuthStateChanged`, colección `usuarios`) → `supabase.auth.signUp({ email, password, options: { data: { nombre } } })`, `signInWithPassword`, `onAuthStateChange`; el rol se lee de la tabla `perfiles`, **no** se escribe desde la web.
   - Dejar de guardar productos y tiendas en `localStorage` (`trama_productos_custom`, `trama_productos_completo`, `trama_tiendas_completo`). El carrito y los favoritos sí pueden seguir en localStorage.
5. **Probar**:
   - Sin sesión, la web muestra las 4 tiendas y solo los productos publicados.
   - Entrar como `donamarta@alerce.app` y cambiar un precio en la web → el cambio aparece en la app al recargar la vitrina, y al revés.
   - Intentar editar otra tienda → debe fallar (RLS).

## 8. Probar las reglas de seguridad

En un Postgres local (no en la instancia del curso); los comandos están en `supabase/README.md`. Resultado esperado: `26 de 26 casos correctos`. Si cambias `01_esquema.sql`, vuelve a correrlas y agrega casos para lo nuevo.

## 9. Trampas conocidas

- Las carpetas tienen espacios y paréntesis (`App para el celular`, `LA PAgina (segun)`): usa comillas en la terminal.
- `HttpURLConnection` no soporta `PATCH`; por eso la app usa OkHttp.
- Las pruebas unitarias se compilan junto con el APK: si renombras clases, actualiza `app/src/test/…/ExampleUnitTest.kt`.
- `index.html` de la raíz redirige a la web; la URL pública es `https://agustin671.github.io/EmprendedoresPuertoMontt/`.
