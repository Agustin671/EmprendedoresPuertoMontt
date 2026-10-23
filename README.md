# 🛍️ Trama Alerce / PuertoEmprende — Emprendedores Puerto Montt

Plataforma integral diseñada para potenciar a las emprendedoras de Alerce y Puerto Montt. Cuenta con una **App Móvil nativa en Android (Jetpack Compose)** y una **Vitrina Web interactiva desplegada en GitHub Pages**.

---

## 📲 Accesos Rápidos y Descargas

* 📦 **[Descargar App Android (APK v1.0.0)](https://github.com/Agustin671/EmprendedoresPuertoMontt/raw/main/app-debug.apk)** *(Clic para descargar el archivo `.apk` e instalar en tu celular)*
* 🌐 **[Ver Vitrina Web en Vivo (GitHub Pages)](https://agustin671.github.io/EmprendedoresPuertoMontt/)**
* 📦 **[Descarga Alternativa del APK (vía GitHub Pages)](https://agustin671.github.io/EmprendedoresPuertoMontt/app-debug.apk)**

---

## ✨ Características Principales

### 1. 🛒 Publicación Ultra-Simple (Estilo Facebook Marketplace)
* Formulario directo, sin pasos complejos ni textos técnicos redundantes.
* Pensado especialmente para emprendedoras que solo utilizan WhatsApp y Facebook.
* Subida de fotos reales desde la galería del teléfono o explorador web.
* Selección táctil de disponibilidad inmediata o por encargo ("A pedido").

### 2. 🎨 Estudio y Generador de Afiches Comerciales (Flyers por Capas)
* Generador de flyers promocionales para compartir en WhatsApp, Facebook e Instagram.
* **Profundidad visual por capas:** Letras y gráficos gigantes detrás, producto en primer plano con sombra, y pastillas de precio / llamada a la acción en la parte superior.
* **5 Estilos de afiches profesionales:**
  1. ⚡ **Estilo 1 (Split Urbano):** Fondo dividido en 2 tonos, letras gigantes verticales rotadas detrás y producto en ángulo flotante.
  2. 🔥 **Estilo 2 (Tipografía Outline):** Fondo en degradé cálido con tipografía hueca de gran escala y banner de precio.
  3. ✨ **Estilo 3 (Splash Pop):** Explosión / sol orgánico de contraste en la capa posterior, producto al frente y botón *"➔ PEDIR AHORA"*.
  4. 🏷️ **Estilo 4 (Minimal Editorial):** Estética sobria, marco perimetral fino y tipografía refinada estilo Apple / Farfetch.
  5. 🌲 **Estilo 5 (Tradición):** Fondo verde bosque profundo, marco dorado y sello artesanal de identidad local.

### 3. 🔤 Tipografía Suiza y Selector de Colores Dinámico
* **Britti Sans:** Tipografía neo-grotesca moderna, limpia y de máxima legibilidad editorial integrada en formato OpenType (`.otf`) nativo.
* **Paleta dinámica de 8 colores:** Original, Carbón (`#18181B`), Carmín (`#DC2626`), Ámbar (`#F59E0B`), Cobalto (`#1D4ED8`), Bosque (`#047857`), Violeta (`#7C3AED`) y Fucsia (`#DB2777`), más selector hexadecimal libre en la web.
* Cálculo armónico de sombras y contrastes para garantizar legibilidad de precios y textos.

### 4. 🛒 Carrito de Compras & Integración con WhatsApp
* Selección de productos con variantes y cálculo automático de total.
* Generación de pedido formateado listo para enviar al WhatsApp de la emprendedora.

### 5. 📱 Códigos QR Oficiales
* Generación de códigos QR vectoriales para acceder directamente a la tienda o vitrina digital en ferias presenciales.

---

## 📥 ¿Cómo instalar la App en tu Celular Android?

1. Descarga el archivo [`app-debug.apk`](https://github.com/Agustin671/EmprendedoresPuertoMontt/raw/main/app-debug.apk).
2. En tu celular, abre el archivo descargado.
3. Si el sistema te lo solicita, activa la opción **"Permitir la instalación de aplicaciones de fuentes desconocidas"** en tu navegador o explorador de archivos.
4. Presiona **Instalar** y ¡listo! Podrás utilizar la app completa en tu dispositivo.

---

## 📂 Estructura del Proyecto

```text
├── app-debug.apk                  # Última versión compilada del APK para instalación directa
├── App para el celular/           # Código fuente de la App Android nativa
│   ├── app/src/main/java/...      # Pantallas Jetpack Compose, navegación y estado
│   └── app/src/main/res/font/     # Tipografías nativas (Britti Sans)
├── LA PAgina (segun)/             # Código fuente de la Vitrina Web
│   └── proyectode10/
│       ├── index.html             # Interfaz web responsiva con canvas de afiches
│       ├── css/styles.css         # Estilos sobrios minimalistas estilo Apple
│       ├── js/main.js             # Motor Canvas 2D, carrito, QR y marketplace
│       └── fonts/                 # Tipografías web (@font-face)
└── README.md                      # Documentación oficial del proyecto
```

---

## 👥 Roles de Usuario

* **Emprendedoras:** Gestión de productos, creación y exportación de afiches para redes sociales, control de inventario y pedidos.
* **Compradores:** Exploración de vitrina local, filtrado por rubros, armado de carrito y compra directa por WhatsApp.
* **Administrador:** Registro de emprendedoras, gestión de asociaciones y coordinación de ferias comunales.
