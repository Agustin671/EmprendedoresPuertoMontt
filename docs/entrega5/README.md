# Entrega 5 · Informe de avance

Rama: `entrega5/catalogo-ventas` (sobre `main` en `6859b8f`). El 11-nov-2026 se aplicaron las migraciones 03, 04 y 05 a la instancia del grupo, con autorización, respaldo y ensayo previo. Esta operación no publicó Pages ni hizo commit/push.

| Documento | Contenido |
|---|---|
| [administrador.md](administrador.md) | Migraciones, respaldo, comprobaciones, orden coordinado, secretos, Pages, datos temporales, correo, Google y recuperación |
| [contrato-catalogo.md](contrato-catalogo.md) | Modelo compartido, reglas y decisiones |
| [tipos-producto.md](tipos-producto.md) | Tipos y campos específicos (pendientes de la contraparte) |
| [datos-temporales-de-prueba.sql](datos-temporales-de-prueba.sql) | Feria y tipo **temporales** para probar el mecanismo; incluye su limpieza. No se ha ejecutado |
| [../desarrollo-y-pruebas.md](../desarrollo-y-pruebas.md) | Comandos y alcance de cada batería |

## Estado de las pruebas (11-nov-2026)

| Batería | Resultado | Qué es |
|---|---|---|
| SQL (`npm run test:sql`) | 394 de 394 en PostgreSQL 15 y 17 | RLS, ventas, concurrencia, actualización, reversión y privilegios excesivos de instalaciones Supabase |
| Web unitarias (`npm run test:web`) | 17 de 17 | Reglas y contrato con Supabase |
| Web recorridos (`npm run test:e2e`) | 16 de 16 en Edge | Interfaz con Supabase sustituto (no prueba RLS) |
| Android unitarias | 38 de 38 | Lógica en el computador: contrato, formulario y huella por cuenta |
| Android en emulador | Recorrido manual sin servidor | Pixel 7, Android 37 beta, modo desarrollador. No prueba sincronización |
| API de la instancia real | 41 de 41 | Registro, login, roles, fotos, catálogo, ventas, reintentos, visibilidad y aislamiento; datos temporales eliminados |
| Web local en Edge + Supabase real | 4 de 4 | Conexión, registro de emprendedora, guardado desde el formulario y lectura en navegador anónimo |
| Vitrina local conectada, sin sesión | Lectura correcta | 5 tiendas reales, fichas con categoría, precio, estado y opciones; sin errores ni escrituras; sin desborde a 390 px |
| APK con configuración real | Compilación correcta | `1.3-entrega5`, versionCode 4; **no se ha probado con una cuenta conectada** |

Estas baterías son apoyo. No reemplazan la demostración que pide el checklist.

### Verificación de la instancia (11-nov-2026)

Se encontró PostgreSQL 15.8 sin 03/04/05, con 15 cuentas, 4 tiendas, 14 productos, una venta y dos fotos. Se respaldaron las ocho tablas públicas (42 filas), definiciones, permisos, metadatos de Storage y archivos de fotos. Se restauró el respaldo en PostgreSQL local y se ensayó la actualización sobre esa copia. El respaldo privado está fuera del repositorio; no es una copia completa del host ni de las sesiones/contraseñas de Auth.

Las migraciones se aplicaron en una transacción, con verificación de que los datos no hubieran cambiado desde el respaldo y límite de espera de bloqueos. La reparación de 04 creó una tienda para una cuenta que ya había solicitado ser emprendedora. Resultado: 15 cuentas, 5 tiendas, 14 productos, una venta y dos fotos originales. Se compararon los campos originales de productos y ventas: permanecen iguales.

La instancia concedía `TRUNCATE`, `REFERENCES` y `TRIGGER` a `anon` y `authenticated`. Se añadió a 05 su revocación sobre las tablas de `public` (RLS no protege `TRUNCATE`) y una prueba que reproduce esos permisos antes de reaplicar 05. Las ventas quedan accesibles a clientes solo mediante lectura protegida por RLS y la RPC autorizada para registrar.

Se verificó que el registro por correo está habilitado y autoconfirmado. La configuración local de Android y web quedó en archivos ignorados por Git. No se usó la clave `service_role`. Las pruebas reales crearon cuentas, productos, ventas y fotos temporales; se eliminaron al terminar. No se enviaron correos de recuperación ni se validó Google.

Lectura anónima posterior (11-nov-2026): 5 tiendas, 14 productos visibles, **0 ferias, 0 tipos de producto**, 0 ventas visibles sin sesión. Los productos visibles son los que ya existían: ninguno fue creado desde la app Android.

## Matriz de los 35 criterios

**Entorno exigido:** APK 1.3 con configuración real y una emprendedora autenticada (A01–A22); web sin sesión mostrando datos creados, modificados y vendidos desde Android (W01–W13).

**Resultado al 11-nov-2026:** ninguno de los 35 se ha ejecutado de punta a punta en ese entorno. No hubo una cuenta temporal con la que entrar a la app conectada, y el grupo decidió no cargar la feria y el tipo temporales. Por eso el estado se separa de la evidencia de apoyo.

Leyenda:
- **Cumple y demostrado**: ejecutado en el entorno exigido, con resultado verificado.
- **Falla**: se ejecutó y no cumple.
- **No probado**: hay código y pruebas de apoyo, pero falta la demostración en el entorno exigido.
- **Bloqueado**: depende de un dato o una decisión externa.

| ID | Estado | Evidencia de apoyo (no es la demostración) |
|---|---|---|
| A01 | No probado | Asistente de 5 etapas recorrido en emulador, sin servidor |
| A02 | No probado | Cámara virtual del emulador; la foto no se subió a Storage desde Android |
| A03 | No probado | Galería no abierta en el emulador |
| A04 | No probado | Nombre, descripción, categoría, precio y fotos en emulador; el tipo depende de A05 |
| A05 | **Bloqueado** | No hay tipos acordados con la contraparte y la tabla `tipos_producto` está vacía. El tipo temporal solo probaría el mecanismo: no cierra A05 |
| A06 | No probado | SQL y unitarias; precio y cantidad por opción en emulador |
| A07 | No probado | SQL, unitarias e interfaz en emulador |
| A08 | No probado | SQL y contrato compartido |
| A09 | No probado | SQL, contrato e interfaz en emulador |
| A10 | No probado | RLS en SQL y unitarias |
| A11 | No probado | Cierre forzado en emulador, sin servidor |
| A12 | No probado | Mismo asistente en modo edición; sin sincronizar |
| A13 | No probado | Unitaria: un borrado pendiente no reaparece al sincronizar |
| A14 | No probado | Unitaria; pantalla vista en emulador; sin comprobar el reflejo en la web |
| A15 | No probado | Unitaria; pantalla en emulador |
| A16 | No probado | Vista previa con las reglas del contrato, en emulador |
| A17 | No probado | Cola por cuenta y reintento con unitarias; **nunca se ejecutó sin red contra el servidor** |
| A18 | No probado | Venta con opción y cantidad en emulador, sin servidor |
| A19 | No probado | SQL (descuento por opción, concurrencia, idempotencia) |
| A20 | No probado | SQL y unitarias; interruptor visto en emulador |
| A21 | **Bloqueado** (feria) / No probado (otro lugar) | La tabla `ferias` está vacía: no hay feria de la plataforma que elegir. «Otro lugar» se registró solo en emulador |
| A22 | No probado | Unitaria; filtro por producto en emulador |
| W01 | No probado | Vitrina local conectada muestra los nombres de los productos existentes |
| W02 | No probado | Recorrido e2e; los productos existentes no tienen foto |
| W03 | No probado | Recorrido e2e |
| W04 | No probado | Vitrina conectada muestra precios existentes |
| W05 | No probado (categoría) / **Bloqueado** (tipo, por A05) | Vitrina conectada muestra la categoría |
| W06 | No probado | Recorrido e2e |
| W07 | No probado | Recorrido e2e |
| W08 | No probado | RLS en SQL + recorrido e2e |
| W09 | No probado | SQL + recorrido e2e |
| W10 | No probado | SQL + recorrido e2e |
| W11 | No probado | SQL + recorrido e2e |
| W12 | No probado | SQL (permanente, también frente al APK 1.2) |
| W13 | No probado | Recorrido e2e de recarga |

Ningún criterio quedó en **Falla**: no se ejecutó ninguno en el entorno exigido. Los defectos encontrados al revisar están en la sección siguiente.

## Cierre del 11-nov-2026

### Se corrigió

- **La app no actualizaba al abrirse con una sesión guardada.** Ferias, tipos y productos del servidor solo se traían al iniciar sesión. Ahora `SesionManager.actualizarAlAbrir()` corre al abrir la app; sin red usa lo guardado.
- **El APK con configuración real mostraba el modo desarrollador y las cuentas de ejemplo** (en compilación debug). Ahora solo aparecen sin servidor configurado, para no mezclar tiendas inventadas con datos reales.
- **La huella no estaba ligada a una cuenta.** Si otra persona entraba con contraseña en el mismo celular, la huella seguía activa. Ahora la activación guarda la cuenta (`ReglaHuella`) y se olvida al entrar con otra.
- **Huella o PIN.** Si el celular no tiene huella registrada, los textos dicen «PIN del celular» en lugar de «huella».

Ninguna corrección se ha verificado aún en emulador o celular conectado al servidor.

### Se añadió

- `ReglaHuella.kt` y 3 pruebas (`ReglaHuellaTest.kt`).
- `datos-temporales-de-prueba.sql`: feria y tipo identificados como «PRUEBA E5», con su limpieza. **No se ha ejecutado.**
- Revocación de `TRUNCATE`, `REFERENCES` y `TRIGGER` en 05, con su prueba.

### Huella (fuera de los 35; revisión de código)

| Caso | Comportamiento esperado según el código |
|---|---|
| Activar | Se ofrece después de entrar con contraseña; queda ligada a esa cuenta |
| Cancelar | Vuelve a la pantalla de ingreso sin mensaje de error |
| Intentos fallidos | Mensaje; tras varios, el bloqueo del sistema |
| Sin biometría | No se ofrece; con solo PIN se rotula «PIN del celular» |
| Otra cuenta | Al entrar con contraseña de otra cuenta se desactiva |
| Cerrar sesión | Con huella activa, la sesión se conserva en el celular para volver a entrar con ella |
| Sin red | Entra con los datos guardados |
| Sesión no renovable | Borra la sesión, desactiva la huella y avisa «Tu sesión venció» |

No se ha probado en emulador ni con un lector físico.

### Sigue pendiente

1. **Recorrido conectado A01–A22** con una cuenta temporal de emprendedora, en emulador o celular (decir cuál).
2. **W01–W13** en una ventana sin sesión, con productos creados desde ese recorrido.
3. **A17 sin red:** entrar, cortar la red, modificar, cerrar y abrir la app, volver a conectar; revisar servidor y web.
4. **A05:** tipos y campos acordados con la contraparte.
5. **A21 (feria):** una feria en la tabla `ferias` (real o la temporal).
6. **Pages:** sigue publicada la versión de `main` `6859b8f`, sin configuración de Supabase y con el APK 1.2. Los secretos no se pudieron revisar.
7. Huella en el dispositivo; correo real de recuperación; Google (deshabilitado).
8. Quitar de la rama los cambios de `App para el celular/.idea/` incluidos en el commit `b05944b`.

### Recorrido para cerrar los 35 (en orden)

1. Instalar el APK 1.3 con configuración real e ingresar con la cuenta temporal.
2. A15: fijar valores iniciales. A01–A04, A06–A09: crear productos con prefijo «PRUEBA E5», uno por comportamiento al agotarse, cada uno con 1 unidad. A11: cerrar a mitad y retomar. A16: vista previa.
3. W01–W07 y W13: abrir la web sin sesión, revisar, editar desde Android (A12), recargar.
4. A10, A14 y W08: marcar no disponible (uno y masivo); la web ya no los muestra y siguen en «Mis productos». A13: borrar uno.
5. A18–A21 y W09–W12: vender 1 unidad de cada producto de prueba, con opción, en «otro lugar» y en la feria; revisar que baje solo esa opción y cómo los muestra la web. A20: apagar y encender el historial y reabrir la app. A22: filtrar.
6. A17: la prueba sin red del punto 3 de la lista anterior.
7. Borrar los productos de prueba y, si se cargaron, la feria y el tipo temporales.

### Guion breve de presentación

1. **Problema:** el catálogo y las ventas de la emprendedora deben verse igual en la app y en la web.
2. **Un solo contrato** (`tests/contrato/casos_estado.json`): SQL, web y Android aplican las mismas reglas de estado público.
3. **Servidor:** RLS oculta lo no disponible, lo agotado a ocultar y la pieza única vendida; las ventas pasan por `registrar_venta()`, que descuenta solo la opción vendida y no duplica reintentos.
4. **Demostración en vivo:** el recorrido anterior, con la web sin sesión al lado.
5. **Sin red:** la cola se guarda por cuenta y se sube al volver la conexión.
6. **Pruebas:** 394 SQL, 17 + 16 web, 38 Android, 41 contra la instancia real.
7. **Pendiente honesto:** tipos acordados, ferias y publicación de Pages.

## Mejora de facilidad de uso del alta de productos (Android, 11-nov-2026)

El asistente de 5 etapas (A01) ahora:
- hace preguntas simples: «¿Tiene distintos tamaños, colores o sabores?», «¿Cómo lo vendes?», «¿Quién lo ve?»;
- muestra cada control solo cuando corresponde;
- deja la etiqueta de cada campo siempre visible y muestra los errores junto a él;
- pide confirmar cualquier acción que borre datos.

Cambiar una respuesta no borra datos ni modifica cantidades. Esa lógica está en `data/FormularioProducto.kt`, con 12 pruebas en `FormularioProductoTest.kt`. No cambia el modelo, el SQL ni las RPC.

Se verificó en el emulador en modo desarrollador, sin servidor. Eso no prueba la sincronización con Supabase.

## Además del checklist (prioridad 2)

- **Gestión web real:** alta y edición con opciones, publicar, disponibilidad masiva, eliminar, cantidades sin pisar ventas.
- **Caja web** con la misma operación segura y reintento idempotente.
- **Fotos:** comprimidas, en la carpeta del usuario; la URL solo se guarda tras una subida confirmada; se borran las subidas sin uso si falla el guardado.
- **Recuperación de contraseña** con manejo de enlaces vencidos; Google real o un aviso honesto.
- **Accesos de demostración solo donde corresponde:**
  - en la web, las cuentas de ejemplo solo aparecen en modo demostración;
  - en Android, el modo desarrollador y las cuentas de ejemplo solo aparecen en compilación debug **sin** servidor configurado.
- **Seguridad:** se corrigió la política de Storage que permitía mover fotos a carpetas ajenas.

## Limitaciones registradas (fuera del alcance)

- **Ferias, agenda, foro y directorio:** siguen con datos locales o de referencia; se rotularon como tales en la web (Entrega 6/7).
- **Panel de administración web:** usa estado local de demostración; no se reconstruyó.
- **APK publicado en Pages (`app-debug.apk`):** sigue siendo la versión 1.2; no se reemplazó.
- **Android:** varias fotos por producto (`fotos`) solo se leen y conservan; la interfaz maneja la foto principal y una por opción.

## Decisiones pendientes de la contraparte

1. Tipos de producto y sus campos específicos (A05, W05).
2. Si las emprendedoras usarán las ferias de la plataforma: hoy la tabla `ferias` debe tener datos para elegir «feria» al vender.
3. Plantilla y remitente del correo de recuperación.
