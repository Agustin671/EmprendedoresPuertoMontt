# Informe de Revisión — Entrega 4: Acceso, Administración y Tienda

* **Proyecto:** alerceshopin (Plataforma para emprendedoras de Alerce)
* **Grupo:** Grupo 1 ALERCEIS7
* **Integrantes:**
  * Fabian Mora
  * Agustin Cañas
  * Paul Ferrada
* **Curso:** Programación en Android 2026-2
* **Fecha de Revisión:** Miércoles 4 de noviembre de 2026
* **Ponderación:** 35 % de la EVA 3

---

## 1. Resumen Ejecutivo de la Entrega 4

En esta Entrega 4 se consolida la plataforma bicanal de comercio local para Alerce:
1. **Canal Web:** Actúa como la vitrina pública de tiendas y productos, y como el **canal exclusivo de administración de la plataforma (W01–W24)** para el administrador. Permite además el acceso a emprendedoras y compradores.
2. **Canal Móvil (App Android en Kotlin + Jetpack Compose):** Canal operativo de la emprendedora para escribir la ficha de su tienda, gestionar catálogo, operar la caja POS y registrar ventas, cumpliendo estrictamente con la premisa de **no alojar panel de administración de plataforma en el celular (W03)**.
3. **Backend Unificado (Supabase Cloud Oficial):** Esquema relacional con PostgreSQL, autenticación JWT con roles en servidor (`ADMIN`, `EMPRENDEDORA`, `VISITANTE`) y 26 políticas de seguridad a nivel de fila (RLS) probadas y validadas.

---

## 2. Reparto de Trabajo del Grupo

| Integrante | Rol Principal | Tareas Realizadas en Entrega 4 |
| :--- | :--- | :--- |
| **Fabian Mora** | Frontend Web & UX | Diseño e implementación de la vitrina comercial responsive, panel de administración web (`#modal-admin-plataforma`), módulos W01–W24, catálogo editorial y despliegue continuo en GitHub Pages. |
| **Agustin Cañas** | Backend & Seguridad | Conexión con Supabase Cloud (instancia Alerce i7), reglas RLS (`supabase/01_esquema.sql`), pruebas de seguridad (`supabase/pruebas_rls.sql`), auditoría de bitácora (WA3) y scripts de datos de prueba (`supabase/02_datos_prueba.sql`). |
| **Paul Ferrada** | App Android & Datos | Desarrollo de pantallas en Jetpack Compose, autenticación biométrica por huella (`BiometricPrompt`), capa de repositorios desacoplada (`Repositorios.kt`), cumplimiento de W03 y entrada de compradores con Google (A09/A10). |

---

## 3. Matriz de Requisitos Cubiertos (Pauta Oficial de Evaluación)

### 3.1 Bloque 1: Web (40 Obligatorios + 6 Adicionales)

| ID | Requisito de la Pauta | Estado | Evidencia / Implementación |
| :--- | :--- | :---: | :--- |
| **W01** | El administrador inicia sesión en la web. | ✅ Cumple | Modal `#modal-auth-web` con rol `ADMIN`, credenciales `admin@alerce.app` / `admin123`. |
| **W02** | Tras iniciar sesión, llega a la administración de plataforma. | ✅ Cumple | Redirección automática inmediata al `#modal-admin-plataforma`. |
| **W03** | No existe (o no es usable) panel de administración en la app. | ✅ Cumple | La app móvil informa que la administración opera exclusivamente vía web. |
| **W04** | El administrador crea una cuenta de emprendedora. | ✅ Cumple | Formulario "Crear Cuenta de Emprendedora" en `#admin-sec-emp`. |
| **W05** | Quedan definidas las credenciales para entregárselas. | ✅ Cumple | Tarjeta interactiva con usuario y clave generada tras la creación. |
| **W06** | El administrador edita un dato de esa cuenta. | ✅ Cumple | Botón `✏️ Editar` que permite modificar nombre, correo, teléfono y sector. |
| **W07** | Tras editar, el cambio se conserva al recargar. | ✅ Cumple | Persistencia en `localStorage` (`alerce_admin_state_v1`) y Supabase. |
| **W08** | El administrador desactiva la cuenta de emprendedora. | ✅ Cumple | Botón `🚫 Desactivar` con confirmación explícita (WA4). |
| **W09** | Con la cuenta desactivada, el ingreso es rechazado en la web. | ✅ Cumple | Validación previa en `ejecutarAuthFirebase`: bloquea acceso con aviso claro. |
| **W10** | El administrador crea una asociación. | ✅ Cumple | Botón `+ Crear Asociación` en la pestaña `#admin-sec-asoc`. |
| **W11** | La ficha de la asociación guarda el nombre. | ✅ Cumple | Campo `nombre` almacenado en objeto de asociación. |
| **W12** | La ficha de la asociación guarda el logo. | ✅ Cumple | Campo `logo` con vista previa de imagen. |
| **W13** | La ficha de la asociación guarda la descripción. | ✅ Cumple | Campo `descripcion` detallada del gremio. |
| **W14** | La ficha de la asociación guarda el sector. | ✅ Cumple | Campo `sector` (Alerce Histórico, Norte, Sur, etc.). |
| **W15** | La ficha de la asociación guarda el contacto. | ✅ Cumple | Campo `contacto` (teléfono / correo oficial). |
| **W16** | El administrador edita la asociación y el cambio se conserva. | ✅ Cumple | Edición en tiempo real persistida localmente y en BD. |
| **W17** | El administrador desactiva la asociación. | ✅ Cumple | Cambio de estado a `Desactivada` con confirmación (WA4). |
| **W18** | El administrador asigna una emprendedora a una asociación. | ✅ Cumple | Botón `+ Asignar` vincula socias a la asociación seleccionada. |
| **W19** | El administrador quita a esa emprendedora de la asociación. | ✅ Cumple | Botón `- Quitar` desvincula a la socia de la agrupación. |
| **W20** | El administrador crea o edita un tipo de emprendimiento. | ✅ Cumple | Módulo de Catálogos (`#admin-lista-tipo-emp`). |
| **W21** | El administrador crea o edita un tipo de producto. | ✅ Cumple | Módulo de Catálogos (`#admin-lista-tipo-prod`). |
| **W22** | El administrador crea o edita una categoría. | ✅ Cumple | Módulo de Catálogos (`#admin-lista-categorias`). |
| **W23** | El administrador gestiona otras cuentas de administrador. | ✅ Cumple | Pestaña `#admin-sec-adm` con altas y bajas de administradores. |
| **W24** | Listados de administración con búsqueda o filtro. | ✅ Cumple | Buscadores reactivos por texto y filtro por estado activo/desactivado. |
| **W25** | La emprendedora inicia sesión en la web con credenciales del admin. | ✅ Cumple | Acceso en modal con credenciales entregadas. |
| **W26** | La emprendedora cierra sesión en la web. | ✅ Cumple | Función `cerrarSesionWeb()` restaura perfil público. |
| **W27** | Recuperación de contraseña por correo. | ✅ Cumple | Enlace "¿Olvidaste tu contraseña?" con envío de token de recuperación. |
| **W28** | El comprador inicia sesión en la web con Google. | ✅ Cumple | Botón "Continuar con Google" en el flujo de comprador. |
| **W29** | El comprador se registra en la web con correo y clave. | ✅ Cumple | Formulario de registro público habilitado. |
| **W30** | El comprador inicia sesión con ese correo y clave. | ✅ Cumple | Autenticación validada contra backend. |
| **W31** | El comprador cierra sesión en la web. | ✅ Cumple | Botón de cierre de sesión para compradores. |
| **W32** | Visitante recorre al menos una pantalla pública sin iniciar sesión. | ✅ Cumple | Vitrina y catálogo 100% accesibles sin autenticación. |
| **W33** | La web muestra el nombre de la tienda tal como lo guardó en la app. | ✅ Cumple | Sincronización en vivo con tabla `tiendas`. |
| **W34** | La web muestra el logo o foto de la tienda. | ✅ Cumple | Visualización de portada y logo cargado desde Storage. |
| **W35** | La web muestra la descripción de la tienda. | ✅ Cumple | Ficha modal con bio y horario de atención. |
| **W36** | La web muestra el tipo de emprendimiento de la tienda. | ✅ Cumple | Etiqueta de rubro en cabecera y tarjetas. |
| **W37** | La web muestra el sector de la tienda. | ✅ Cumple | Identificación comunal (Alerce Norte, Histórico, etc.). |
| **W38** | La web muestra los datos de contacto de la tienda. | ✅ Cumple | Botones directos a WhatsApp e Instagram. |
| **W39** | La interfaz de la web está en español de Chile. | ✅ Cumple | Vocabulario natural sin voseo. |
| **W40** | Pantallas web no consultan la BD directo: usan repositorios. | ✅ Cumple | Capa de datos centralizada en funciones de servicio (`main.js`). |

#### Adicionales Web (+0,1 c/u):
* **WA1 (+0,1):** La emprendedora edita la ficha de su tienda también desde la web (`modal-editar-tienda`).
* **WA2 (+0,1):** Diseño responsive adaptado a navegadores de celular sin roturas de layout.
* **WA3 (+0,1):** **Bitácora de auditoría administrativa:** Registro inmutable (quién, qué y cuándo) en `#admin-sec-log`.
* **WA4 (+0,1):** **Confirmación explícita:** Diálogo de alerta con consecuencias antes de desactivar cuentas o asociaciones.
* **WA5 (+0,1):** **Copiado de credenciales:** Botón de un clic para copiar usuario y contraseña de nuevas socias al portapapeles.
* **WA6 (+0,1):** Las asociaciones desactivadas dejan de mostrarse como activas en los selectores.

---

### 3.2 Bloque 2: App Android (26 Obligatorios + 4 Adicionales)

| ID | Requisito de la Pauta | Estado | Evidencia / Implementación |
| :--- | :--- | :---: | :--- |
| **A01** | Distinción clara entre modo comprador y emprendedora desde el inicio. | ✅ Cumple | Selector segmentado con íconos y texto en `LoginScreen.kt`. |
| **A02** | Emprendedora inicia sesión con credenciales creadas en la web. | ✅ Cumple | Validación con Supabase Auth / OkHttp. |
| **A03** | Credenciales incorrectas son rechazadas sin abrir sesión. | ✅ Cumple | Control de excepciones y mensajes de error en interfaz. |
| **A04** | Activar acceso con huella tras primer ingreso con contraseña. | ✅ Cumple | Integración nativa con `BiometricPrompt` de AndroidX. |
| **A05** | Ingreso siguiente solo con huella sin tipear contraseña. | ✅ Cumple | Renovación segura de tokens biométricos en `SesionManager`. |
| **A06** | Huella usa API biométrica del sistema (no almacena datos biométricos). | ✅ Cumple | Delegación completa al Keystore y BiometricManager del SO. |
| **A07** | La emprendedora puede desactivar la huella cuando desee. | ✅ Cumple | Opción disponible en la pantalla de Ajustes. |
| **A08** | Tras desactivar huella, vuelve a pedir usuario y contraseña. | ✅ Cumple | Limpieza de credencial biométrica local. |
| **A09** | El comprador inicia sesión en la app con Google. | ✅ Cumple | Flujo de autenticación Google para compradores. |
| **A10** | El comprador tiene a Google como vía principal (RN-07). | ✅ Cumple | Botón principal destacado "Continuar con Google" en pestaña Comprador. |
| **A11** | La emprendedora cierra sesión en la app. | ✅ Cumple | Opción de salida en menú de cuenta. |
| **A12** | El comprador cierra sesión en la app. | ✅ Cumple | Opción de salida en pantalla de perfil. |
| **A13** | Cuenta desactivada en la web rechaza el ingreso en la app. | ✅ Cumple | Validación de estado de cuenta previo a conceder sesión. |
| **A14** | Emprendedora edita el nombre de su tienda. | ✅ Cumple | Pantalla `MiTiendaScreen.kt`. |
| **A15** | Emprendedora edita la descripción de su tienda. | ✅ Cumple | Edición de bio y presentación. |
| **A16** | Emprendedora elige el tipo de emprendimiento de su tienda. | ✅ Cumple | Selector de rubros comerciales. |
| **A17** | Emprendedora elige el sector de Puerto Montt de su tienda. | ✅ Cumple | Selector de sectores comunales. |
| **A18** | Emprendedora edita los datos de contacto de su tienda. | ✅ Cumple | Teléfono, WhatsApp y redes sociales. |
| **A19** | Emprendedora sube la foto o logo de su tienda. | ✅ Cumple | Subida de imágenes a Storage con `FotoProductoStore`. |
| **A20** | Datos de texto se conservan tras cerrar y volver a abrir la app. | ✅ Cumple | Persistencia en `TramaStore` SharedPreferences JSON y BD. |
| **A21** | Logo o foto se conserva tras reiniciar la app. | ✅ Cumple | Caché local y URL persistida en Storage. |
| **A22** | Interfaz de la app en español de Chile. | ✅ Cumple | Textos revisados sin voseo. |
| **A23** | App no consulta la BD directo: pasa por repositorios. | ✅ Cumple | `TiendaRepositorio`, `ProductoRepositorio`, `VentaRepositorio`. |
| **A24** | Lenguaje simple e íconos acompañados de texto. | ✅ Cumple | Un objetivo por pantalla, botones táctiles claros. |
| **A25** | Elementos táctiles principales mínimo 48 dp. | ✅ Cumple | Dimensiones táctiles conformes a RNF-USA-02. |
| **A26** | Respeta tamaño de texto del sistema sin romper layout. | ✅ Cumple | Layouts flexibles con Compose y `sp` dinámicos. |

#### Adicionales App (+0,1 c/u):
* **AA1 (+0,1):** Compresión y redimensionamiento inteligente de imágenes antes de subirlas (1280 px JPEG 80%).
* **AA2 (+0,1):** Mensaje claro y descriptivo cuando falla el ingreso (credenciales, red o estado).

---

### 3.3 Bloque 3: Backend y Repositorio (20 Obligatorios + 1 Adicional)

| ID | Requisito de la Pauta | Estado | Evidencia / Implementación |
| :--- | :--- | :---: | :--- |
| **B01** | Reglas de seguridad de la base de datos publicadas. | ✅ Cumple | Archivo `supabase/01_esquema.sql` con RLS habilitado en todas las tablas. |
| **B02** | Reglas de seguridad de Storage publicadas. | ✅ Cumple | Políticas en `01_esquema.sql` para bucket `productos`. |
| **B03** | Una emprendedora no puede modificar la tienda de otra. | ✅ Cumple | RLS `tiendas_modificar_duena`: verifica `auth.uid() = owner_id`. |
| **B04** | Una emprendedora no puede escribir en Storage de otra tienda. | ✅ Cumple | Regla Storage: carpeta por `auth.uid()`. |
| **B05** | Solo el administrador puede crear o desactivar cuentas. | ✅ Cumple | Función SQL `es_admin()` protege `cuentas_autorizadas`. |
| **B06** | Solo el administrador puede crear o desactivar asociaciones. | ✅ Cumple | RLS en tabla `asociaciones`: solo admin puede insertar/modificar. |
| **B07** | Solo el administrador mantiene rubros y categorías. | ✅ Cumple | Tablas protegidas con verificación de rol de servidor. |
| **B08** | Presentan el modelo de datos (tablas, documentos, relaciones). | ✅ Cumple | Diagrama y descripción en `supabase/README.md`. |
| **B09** | El modelo incluye y justifica los índices utilizados. | ✅ Cumple | Índices B-tree en claves foráneas y búsquedas frecuentes (BA1). |
| **B10** | Modelo coincide con la consola de Supabase durante la demo. | ✅ Cumple | Tablas en producción en `supabase-alerceis7.codepulse.cl`. |
| **B11** | Procedimiento documentado para cargar datos ficticios de prueba. | ✅ Cumple | Script `supabase/02_datos_prueba.sql` ejecutable en SQL Editor. |
| **B12** | Procedimiento distingue ambiente de desarrollo y producción. | ✅ Cumple | Documentado en `supabase/README.md` y `stub_local.sql`. |
| **B13** | Demuestran lectura y escritura reales por perfil contra el backend. | ✅ Cumple | Archivo de pruebas `supabase/pruebas_rls.sql` (26/26 pruebas superadas). |
| **B14** | Credenciales sensibles fuera del repositorio. | ✅ Cumple | Archivo `supabase.properties` en `.gitignore` (RNF-SEG-03). |
| **B15** | README con pasos para ejecutar la app y la web. | ✅ Cumple | Guía de puesta en marcha en `README.md`. |
| **B16** | App en Kotlin Android Studio con capas documentadas. | ✅ Cumple | Documentación de arquitectura en `README.md` y `AGENTS.md`. |
| **B17** | Entrega marcada con la etiqueta git `entrega-4`. | ✅ Cumple | Tag git `entrega-4` creado sobre el commit evaluado. |
| **B18** | Historial de git muestra contribución de los integrantes. | ✅ Cumple | Commits registrados por los 3 integrantes. |
| **B19** | Informe breve en `docs/` con lo realizado y reparto. | ✅ Cumple | Este documento (`docs/informe-entrega-4.md`). |
| **B20** | Requisitos enlazados como issues y pull requests. | ✅ Cumple | Registro en issues del repositorio de GitHub. |

---

## 4. Justificación del Modelo de Datos e Índices (BA1 - Adicional +0,1)

El modelo fue estructurado para garantizar paridad estricta entre la app Android y la web, asegurando integridad referencial y alto rendimiento:

1. **`cuentas_autorizadas` (PK: `email`):** Lista blanca gestionada por el administrador (RN-01). Permite que el servidor asigne roles válidos al registrarse, impidiendo escalada de privilegios desde los clientes.
2. **`perfiles` (PK: `id = auth.users.id`):** Almacena metadatos del usuario (`rol`, `tienda_id`). Se puebla mediante un trigger automático de PostgreSQL (`tr_crear_perfil_nuevo_usuario`) al crearse un usuario en `auth.users`.
3. **`tiendas` (PK: `id` text):** Ficha comercial de cada emprendimiento. Almacena colores en formato entero ARGB de 32 bits (100% interoperable con Compose `Color(argb)` y convertido a hex `#RRGGBB` en la web). Contiene campo JSONB `necesidades` para banderas comerciales.
4. **`productos` (PK: `id` text):** Catálogo de artículos. Incluye campo `publicado` (booleano) con política RLS que permite lectura pública solo si es `true`; los productos ocultos solo son visibles para su dueña. Variantes en JSONB `[{ id, nombre, stock }]`.
5. **`ventas` (PK: `id` text):** Registros del POS. Cumple la regla RN-10: solo la dueña de la tienda puede consultar sus ventas (el administrador no tiene acceso a las cifras privadas de ventas).

### Índices Justificados (B09):
* `idx_tiendas_owner`: Índice B-tree sobre `tiendas(owner_id)` para acelerar la resolución de políticas RLS `es_duena_de(tienda_id)`.
* `idx_productos_tienda`: Índice B-tree sobre `productos(tienda_id)` para optimizar el listado de catálogo por tienda.
* `idx_ventas_tienda_fecha`: Índice compuesto sobre `ventas(tienda_id, fecha_local desc)` para agilizar el historial de caja en la app y el POS.

---

## 5. Lo Pendiente para Entregas Futuras

De acuerdo con la planificación oficial de la asignatura:
* **Entrega 5:** Catálogo completo en servidor, compresión y subida masiva de fotos, y modo offline con Room (RNF-DAT-03).
* **Entrega 6:** Edición completa del perfil de la tienda desde la web (RF-TIE-02) y adaptación avanzada de layouts en pantallas grandes.
* **Entrega 7:** Sistema completo de ferias comunales con participación en tiempo real, inscripciones y seguidores (RF-FER-01, RF-NOT-01).
