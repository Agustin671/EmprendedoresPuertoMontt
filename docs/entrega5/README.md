# Entrega 5 · Informe de avance

Rama: `entrega5/catalogo-ventas` (sobre `main` en `6859b8f`). Nada se publicó, ni se aplicó a la base remota, ni se hizo push.

| Documento | Contenido |
|---|---|
| [administrador.md](administrador.md) | Migraciones, respaldo, comprobaciones, orden coordinado, secretos, correo, Google y recuperación |
| [contrato-catalogo.md](contrato-catalogo.md) | Modelo compartido, reglas y decisiones |
| [tipos-producto.md](tipos-producto.md) | Tipos y campos específicos (pendientes de la contraparte) |
| [../desarrollo-y-pruebas.md](../desarrollo-y-pruebas.md) | Comandos y alcance de cada batería |

## Estado de las pruebas (11-nov-2026)

| Batería | Resultado | Qué es |
|---|---|---|
| SQL (`npm run test:sql`) | 392 de 392 en PostgreSQL 15 y 17 | Real: RLS, ventas, concurrencia con conexiones independientes, actualización desde el esquema anterior, reversión |
| Web unitarias (`npm run test:web`) | 17 de 17 | Reglas y contrato con Supabase |
| Web recorridos (`npm run test:e2e`) | 16 de 16 en Edge | Interfaz con Supabase sustituto (no prueba RLS) |
| Android unitarias | 23 de 23 | Lógica en el computador |
| Android en emulador | Recorrido manual sin servidor | Pixel 7, Android 37 beta; ver la matriz |

**No verificado (bloqueado externamente):**
- web y app contra la instancia real (falta aplicar 05 y configurar los secretos);
- celular físico;
- correo real de recuperación;
- Google;
- compilación con `supabase.properties`.

## Matriz de los 35 criterios

Leyenda:
- **Probado**: ejecutado, con resultado verificado.
- **Implementado**: código y pruebas de lógica, sin demostrar de punta a punta.
- **Bloqueado**: depende de un acceso o una decisión externa.

No se asigna nota: el checklist se evalúa usándolo.

| ID | Qué se hizo | Estado |
|---|---|---|
| A01 | Alta guiada en 5 pasos: foto, datos, opciones, disponibilidad, revisión | Probado en emulador |
| A02 | Cámara (`TakePicture` + permiso + FileProvider) | Probado en emulador (cámara virtual) |
| A03 | Galería (`GetContent`), para el producto y para cada opción | Implementado (no se abrió la galería en el emulador) |
| A04 | Nombre, descripción, categoría, precio, tipo, foto principal y fotos por opción | Implementado; el tipo depende de A05 |
| A05 | Campos específicos según el tipo (`tipos_producto`) | **Bloqueado**: faltan los tipos reales de la contraparte |
| A06 | Opción con precio, foto y cantidad propios | Probado (SQL, unitarias; precio y cantidad en emulador) |
| A07 | Regular, a pedido, pieza única | Probado (SQL, unitarias, interfaz en emulador) |
| A08 | Llevar o no la cuenta de unidades | Probado (SQL, contrato) |
| A09 | Agotado, ocultar o a pedido | Probado (SQL, contrato, interfaz) |
| A10 | Disponible / no disponible; sigue en su tienda | Probado (SQL RLS, unitarias) |
| A11 | Autoguardado desde el primer paso; se retoma tras cerrar | Probado en emulador (cierre forzado) |
| A12 | Editar con el mismo asistente; se guarda y sincroniza | Implementado |
| A13 | Borrar; un borrado pendiente no reaparece | Implementado + unitaria |
| A14 | Cambio masivo por selección, por categoría o de todo el catálogo | Implementado + unitaria (interfaz vista en emulador) |
| A15 | Valores iniciales configurables por tienda | Probado (unitaria; pantalla en emulador) |
| A16 | Vista previa con las mismas reglas que la web | Probado en emulador |
| A17 | Catálogo sin red; cola por cuenta y reintento al volver la red | Implementado (modo avión con servidor sin probar) |
| A18 | Venta con opción y cantidad | Probado en emulador |
| A19 | Descuenta solo la opción vendida | Probado (SQL, unitarias) |
| A20 | Interruptor del historial | Probado (SQL, unitarias; pantalla en emulador) |
| A21 | Precio, fecha y lugar (feria de la plataforma u otro lugar) | Probado en emulador (otro lugar); las ferias de la plataforma dependen de la tabla `ferias` del servidor |
| A22 | Filtros por fecha, feria y producto | Probado (unitaria; filtro por producto en emulador) |
| W01 | Nombre publicado | Implementado + recorrido; **bloqueado** en Pages (secretos y 05) |
| W02 | Foto (URL https de Storage) | Implementado + recorrido; bloqueado igual |
| W03 | Descripción | Implementado + recorrido; bloqueado igual |
| W04 | Precio | Implementado + recorrido; bloqueado igual |
| W05 | Categoría y tipo visibles | Implementado + recorrido (el tipo depende de A05) |
| W06 | Precio de la opción elegida | Implementado + recorrido |
| W07 | Foto de la opción elegida | Implementado + recorrido |
| W08 | No disponible no aparece (RLS y vitrina) | Probado en SQL (RLS) + recorrido |
| W09 | Mostrar agotado | Probado en SQL + recorrido |
| W10 | Ocultar al agotarse | Probado en SQL + recorrido |
| W11 | Pasar a pedido | Probado en SQL + recorrido |
| W12 | Pieza única vendida no vuelve a aparecer | Probado en SQL (permanente, también frente al APK antiguo) |
| W13 | Edición visible al recargar (sin caché) | Implementado + recorrido de recarga |

## Además del checklist (prioridad 2)

- **Gestión web real:** alta y edición con opciones, publicar, disponibilidad masiva, eliminar, cantidades sin pisar ventas.
- **Caja web** con la misma operación segura y reintento idempotente.
- **Fotos:** comprimidas, en la carpeta del usuario; la URL solo se guarda tras una subida confirmada; se borran las subidas sin uso si falla el guardado.
- **Recuperación de contraseña** con manejo de enlaces vencidos; Google real o un aviso honesto.
- **Accesos de demostración solo donde corresponde:**
  - en la web, las cuentas de ejemplo solo aparecen en modo demostración;
  - en el APK de producción ya no aparecen la contraseña de prueba ni el acceso de Doña Marta.
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
