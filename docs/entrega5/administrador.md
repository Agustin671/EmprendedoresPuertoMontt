# Entrega 5 · Guía para el administrador (Supabase y GitHub)

Esta guía es para la persona que administra la instancia de Supabase del grupo (el profesor) y el repositorio en GitHub. **Ningún cambio remoto se hizo automáticamente.** Todos los SQL se revisan y se ejecutan a mano desde Supabase Studio. No se pide ni se usa `service_role`.

## 1. Qué necesita cada pieza

| Pieza | Necesita | Lo hace |
|---|---|---|
| Base de datos | Ejecutar `supabase/05_catalogo_ventas.sql` (y 03/04 si faltan) | Administrador, en SQL Editor |
| Tipos de producto | Insertar los tipos y campos reales del levantamiento | Administrador, con el grupo ([tipos-producto.md](tipos-producto.md)) |
| Web (Pages) | Secretos `SUPABASE_URL` y `SUPABASE_ANON_KEY` | Quien administra el repositorio |
| Correo de recuperación | URLs de redirección permitidas | Administrador, en Auth |
| Google | Proveedor habilitado (opcional) | Administrador, en Auth |
| APK 1.3 | Compilar con `supabase.properties` y distribuir | Grupo, después de aplicar 05 |

## 2. Antes de aplicar: diagnóstico y respaldo

1. **Diagnóstico de solo lectura.** Ejecuta `supabase/diagnostico_solo_lectura.sql` y guarda el resultado. Responde:
   - la versión de PostgreSQL (05 está probada en 15 y 17; no usa vistas ni opciones dependientes de versión);
   - si 03 y 04 están aplicadas, comprobado por su efecto real (columnas `foto_url`/`logo_url`, funciones `actualizar_mi_perfil` y `crear_mi_tienda`, y si el trigger acepta `tipo_cuenta`), no por la existencia de los archivos;
   - políticas, permisos sobre `ventas`, buckets y cantidades de registros, sin datos personales.

   Si la consulta 12 da «does not exist», 05 no está aplicada.
2. **Respaldo.** Antes de 05:
   - Si el plan de Supabase tiene respaldos automáticos o PITR, anota la hora exacta antes de aplicar.
   - Además, desde Table Editor exporta a CSV `productos`, `tiendas`, `ventas`, `perfiles` y `ferias`. También sirve `pg_dump` con la cadena de conexión del administrador.
   - Guarda el respaldo fuera del repositorio: contiene datos personales.
3. **Ventana tranquila.** Avisa al grupo que no vendan desde el APK 1.2 durante la aplicación (unos minutos).

## 3. Aplicar las migraciones (orden)

| Paso | Archivo | Cuándo |
|---|---|---|
| 1 | `03_migracion_fotos.sql` | Solo si el diagnóstico dice que falta (es idempotente) |
| 2 | `04_registro_vendedores.sql` | Solo si el diagnóstico dice que falta (es idempotente) |
| 3 | `05_catalogo_ventas.sql` | Siempre; se puede volver a ejecutar |

**No ejecutes `02_datos_prueba.sql` en la instancia real.**

Si alguien vuelve a ejecutar `01_esquema.sql` sobre una base ya creada, debe ejecutar `05` justo después: `01` vuelve a dar permiso de escritura directa sobre `ventas`.

### Qué cambia 05 (efectos reales)

- **Columnas nuevas con valores por defecto.** Los registros actuales conservan su comportamiento: `modalidad=REGULAR`, `maneja_stock=true`, `al_agotarse=MOSTRAR_AGOTADO`, `disponible=true`, `historial_activo=true`.
- **Opciones sin id.** Las opciones creadas por la web anterior reciben un id estable (`<producto>_v1`). Es el único cambio sobre datos existentes.
- **Lectura pública más estricta.** Sin sesión, o con sesión de otra persona, ya no se leen productos no disponibles, agotados con «ocultar» ni piezas únicas vendidas. La dueña sigue viendo todos los suyos.
- **Ventas solo por `registrar_venta()`.** Se retira `INSERT/UPDATE/DELETE` directo en `ventas`. El historial sigue siendo privado de la dueña; ADMIN no lo ve.
- **Cantidades protegidas.** Un trigger impide que una actualización directa (APK 1.2) pise cantidades, borre precio o foto de las opciones o reviva una pieza única vendida.
- **Storage.** Ya no se puede mover una foto a la carpeta de otra persona.
- **Registro.** Se anota `05_catalogo_ventas` en `migraciones_aplicadas`.

## 4. Después de aplicar: comprobaciones

1. Vuelve a ejecutar el diagnóstico:
   - la consulta 12 muestra `05_catalogo_ventas`;
   - la consulta 8 muestra solo `SELECT` sobre `ventas` para `authenticated`;
   - las cantidades de la consulta 10 no cambiaron.
2. En la web publicada, sin sesión:
   - la vitrina muestra las tiendas reales;
   - el encabezado dice «Conectado · datos del servidor».
3. Con una cuenta de prueba de una tienda de prueba (no una tienda real):
   - crea un producto con dos opciones;
   - véndelo desde la caja web o el APK 1.3;
   - revisa que la cantidad baje solo en la opción vendida.

## 5. Orden coordinado de actualización

1. **Base:** diagnóstico → respaldo → 03/04 si faltan → 05 → comprobaciones.
2. **Web:** configura los secretos (sección 6) y vuelve a ejecutar el workflow, o haz push a `main` cuando el grupo lo decida. La web nueva necesita 05: con la base antigua, guardar y vender responde «función no encontrada».
3. **APK 1.3** (`versionCode 4`): compílalo con `supabase.properties` y distribúyelo. Reemplazar el APK publicado en Pages (`app-debug.apk`) es una decisión aparte: no se cambió en esta entrega.

### Qué pasa con el APK 1.2 después de 05

- **Productos.** Puede editar nombre, precio, descripción y publicación. No puede cambiar cantidades: el servidor conserva las que tenía. No borra precio ni foto de las opciones. No puede volver a mostrar una pieza única vendida.
- **Ventas.** Las que sube se rechazan con un error de permiso (`42501`). No se pierden: quedan en su cola «por subir» en el celular, pero no descuentan unidades en el servidor hasta actualizar.
- **Conclusión.** Hay que actualizar al APK 1.3 antes de vender. Avísalo a las emprendedoras. Esta compatibilidad está probada en `tests/sql` («APK antiguo: …»).

## 6. Secretos y Pages (GitHub)

1. Repositorio → Settings → Secrets and variables → Actions. Crea dos secretos, como secretos del repositorio o del entorno `github-pages`:
   - `SUPABASE_URL` = `https://<proyecto>.supabase.co`
   - `SUPABASE_ANON_KEY` = la clave **anon** o `sb_publishable_…`. **Nunca** `service_role` ni `sb_secret_…`: el workflow las rechaza.
2. Settings → Pages → Source: **GitHub Actions**.
3. Settings → Environments → `github-pages`: no hace falta cambiar sus reglas. El último despliegue desde `main` funcionó; solo faltaban los secretos.
4. Vuelve a ejecutar «Deploy to GitHub Pages». Lo que hace el workflow:
   - corre las pruebas de reglas web;
   - valida los secretos sin imprimirlos;
   - falla antes de publicar si faltan o tienen formato inválido;
   - genera `js/config.js`, que no se versiona.
5. Verificación: en el sitio publicado, el encabezado dice «Conectado · datos del servidor». Si dice «Sin conexión configurada», los secretos no llegaron.

## 7. Correo de recuperación y redirecciones (Auth)

En Authentication → URL Configuration:
- **Site URL:** `https://agustin671.github.io/EmprendedoresPuertoMontt/`
- **Redirect URLs** (agrega ambas):
  - `https://agustin671.github.io/EmprendedoresPuertoMontt/?recuperar=1`. La usan la web y la app (`ConfigSupabase.URL_RECUPERACION`).
  - `https://agustin671.github.io/EmprendedoresPuertoMontt/`. Se usa al volver de Google.

Flujo comprobado en la web:
1. Se solicita el enlace.
2. Se vuelve con una sesión de recuperación (`PASSWORD_RECOVERY`).
3. Se escribe la clave nueva y luego se cierra la sesión.
4. Un enlace vencido (`otp_expired`) muestra «El enlace venció o ya se usó. Pide uno nuevo.».

Pendiente del administrador:
- Revisar la plantilla de correo «Reset Password» (en español).
- Revisar el límite de envíos.
- Si se usa SMTP propio, configurarlo. Con el SMTP por defecto de Supabase el envío está limitado.

## 8. Google (opcional)

La web ya no simula el acceso con Google. Consulta `auth/v1/settings`:
- **Si Google está habilitado**, usa `signInWithOAuth`.
- **Si no**, informa «El acceso con Google aún no está habilitado en el servidor».

Para habilitarlo: Authentication → Providers → Google, con Client ID y Secret de Google Cloud. La URI de redirección autorizada en Google es `https://<proyecto>.supabase.co/auth/v1/callback`.

En Android no hay acceso con Google (sigue pendiente: Entrega 7, RN-07).

## 9. Recuperación ante fallos

| Situación | Qué hacer | Datos |
|---|---|---|
| 05 falló a mitad | Volver a ejecutar 05: es idempotente y cada sentencia se puede repetir | No se pierden |
| La web o el APK 1.3 fallan y hay que volver atrás **sin** datos dañados | Ejecutar `supabase/05_revertir.sql` y volver a la web y el APK anteriores | Se conservan todas las filas y columnas nuevas; las ventas hechas con 05 siguen registradas y sus descuentos son correctos |
| Datos dañados (cantidades incorrectas, productos alterados) | Restaurar el respaldo del paso 2 (PITR o CSV) | Se pierde lo ocurrido después del respaldo |

`05_revertir.sql` **no** es una reversión completa:
- no borra columnas;
- no deshace descuentos ya aplicados;
- devuelve la escritura directa en `ventas` (vuelve el riesgo de duplicados del APK 1.2).

Después de revertir, la web nueva y el APK 1.3 no pueden guardar ni vender. Para volver a aplicar 05 basta ejecutarlo de nuevo (probado en `tests/sql`).
