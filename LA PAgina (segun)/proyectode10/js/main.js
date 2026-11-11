/**
 * TRAMA ALERCE — Lógica Web Interactiva (Farfetch & Apple Style)
 * Sincronizado con la App Android: Carrito, Códigos QR, Muro en Vivo,
 * Catálogo, Agendamiento y Accesibilidad.
 */

(function () {
  "use strict";

  // ============================================================
  // CONEXIÓN OFICIAL SUPABASE CLOUD (INSTANCIA ALERCE I7)
  // Backend oficial para Programación Android 2026-2 & Web
  // ============================================================
  var CFG = (typeof window !== "undefined" && window.__CONFIG__) ? window.__CONFIG__ : {};
  var CFG_SUPA = CFG.supabase || (CFG.SUPABASE_URL ? { url: CFG.SUPABASE_URL, anonKey: CFG.SUPABASE_ANON_KEY } : {});
  var SUPABASE_URL = CFG_SUPA.url || "";
  var SUPABASE_ANON_KEY = CFG_SUPA.anonKey || "";
  var BUCKET_FOTOS = "productos";
  var BUCKET_PERFILES = "perfiles";

  function subirArchivoSupabase(bucket, ruta, archivo) {
    if (!supabaseClient || !supabaseClient.storage) {
      return Promise.reject(new Error("Supabase Storage no está disponible"));
    }
    return comprimirImagen(archivo).then(function (jpeg) {
      return supabaseClient.storage.from(bucket).upload(ruta, jpeg, {
        upsert: true,
        cacheControl: "3600",
        contentType: "image/jpeg"
      });
    }).then(function(res) {
      if (res.error) throw res.error;
      var pub = supabaseClient.storage.from(bucket).getPublicUrl(ruta);
      return pub && pub.data ? pub.data.publicUrl : "";
    });
  }

  function comprimirImagen(archivo) {
    return new Promise(function (resolve, reject) {
      if (!archivo || !archivo.type || archivo.type.indexOf("image/") !== 0) {
        reject(new Error("Elige un archivo de imagen válido."));
        return;
      }
      var imagen = new Image();
      var url = URL.createObjectURL(archivo);
      imagen.onload = function () {
        URL.revokeObjectURL(url);
        var escala = Math.min(1, 1280 / Math.max(imagen.naturalWidth, imagen.naturalHeight));
        var canvas = document.createElement("canvas");
        canvas.width = Math.max(1, Math.round(imagen.naturalWidth * escala));
        canvas.height = Math.max(1, Math.round(imagen.naturalHeight * escala));
        canvas.getContext("2d").drawImage(imagen, 0, 0, canvas.width, canvas.height);
        canvas.toBlob(function (blob) {
          if (blob) resolve(blob);
          else reject(new Error("No se pudo preparar la imagen."));
        }, "image/jpeg", 0.8);
      };
      imagen.onerror = function () {
        URL.revokeObjectURL(url);
        reject(new Error("No se pudo abrir la imagen elegida."));
      };
      imagen.src = url;
    });
  }
  var supabaseClient = null;

  try {
    if (SUPABASE_URL && SUPABASE_ANON_KEY && typeof window !== "undefined" && window.supabase && window.supabase.createClient) {
      supabaseClient = window.supabase.createClient(SUPABASE_URL, SUPABASE_ANON_KEY);
      console.log("✅ Supabase Client conectado exitosamente a instancia oficial Alerce i7:", SUPABASE_URL);
    }
  } catch (errSupa) {
    console.warn("Aviso inicializando Supabase Client:", errSupa);
  }

  // Conversión exacta de color ARGB (Android) a Hex (Web) y viceversa (AGENTS.md)
  var aHex = function (n, fallback) {
    if (typeof n === "string" && n.charAt(0) === "#") return n;
    if (typeof n === "number") return "#" + (Number(n) & 0xFFFFFF).toString(16).padStart(6, "0").toUpperCase();
    return fallback || "#1A202C";
  };
  var aArgb = function (hex, fallback) {
    if (typeof hex === "number") return hex;
    var str = String(hex || fallback || "#1A202C").replace("#", "");
    var parsed = parseInt(str, 16);
    return isNaN(parsed) ? 0xFF1A202C : 0xFF000000 + parsed;
  };
  var normalizarColorHex = aHex;

  // ============================================================
  // CONEXIÓN OFICIAL FIREBASE WEB (AUTH, FIRESTORE, STORAGE)
  // ============================================================
  var firebaseConfig = CFG.firebase || CFG.FIREBASE || null;

  var firebaseApp = null;
  var firebaseAuth = null;
  var firebaseFirestore = null;
  var firebaseStorage = null;

  try {
    if (firebaseConfig && typeof firebase !== "undefined") {
      if (!firebase.apps || !firebase.apps.length) {
        firebaseApp = firebase.initializeApp(firebaseConfig);
      } else {
        firebaseApp = firebase.app();
      }
      firebaseAuth = firebase.auth();
      firebaseFirestore = firebase.firestore();
      firebaseStorage = firebase.storage();
      console.log("Firebase Web SDK conectado a proyecto alercei7");
    }
  } catch (errFb) {
    console.warn("Aviso inicializando Firebase Web:", errFb);
  }

  var estadoAuthWeb = {
    modoRegistro: false,
    rolSeleccionado: "EMPRENDEDORA"
  };


  // ============================================================
  // DATOS LOCALES SINCRONIZADOS CON ANDROID TRAMASTORE & FIRESTORE
  // ============================================================

  var TIENDAS_DEMO = [
    {
      id: "t_barberia_alerce",
      nombre: "Barbería & Estilo Alerce",
      rubro: "Servicios y Peluquería",
      sector: "Alerce Norte",
      bio: "Cortes de cabello clásicos y modernos, perfilado de barba y tratamientos capilares. Atención personalizada y agendamiento de turnos.",
      whatsapp: "+56987654321",
      colorPortada: "#1A202C",
      colorLogo: "#2B6CB0",
      necesidades: {
        tieneStock: true,
        tieneAgendamiento: true,
        tieneVentaPorWhatsApp: true,
        tieneProductosAPedido: false,
        participaEnFerias: false,
        tieneForoComunidad: false
      }
    },
    {
      id: "t_minimarket_rosa",
      nombre: "Minimarket Doña Rosa",
      rubro: "Abarrotes y Alimentos",
      sector: "Alerce Histórico",
      bio: "Frutas y verduras frescas, pan amasado caliente todos los días a las 17:00 hrs, abarrotes, cecinas artesanales y bebidas para los vecinos de Alerce.",
      whatsapp: "+56991234567",
      colorPortada: "#C53030",
      colorLogo: "#DD6B20",
      necesidades: {
        tieneStock: true,
        tieneAgendamiento: false,
        tieneVentaPorWhatsApp: true,
        tieneProductosAPedido: true,
        participaEnFerias: true,
        tieneForoComunidad: true
      }
    },
    {
      id: "t_tejidos_austral",
      nombre: "Tejidos & Lanares Alerce Austral",
      rubro: "Artesanía y Textil",
      sector: "Alerce Sur",
      bio: "Piezas únicas tejidas a palillo y telar con lana 100% natural de oveja chilota, teñida con tintes de cortezas nativas.",
      whatsapp: "+56976543210",
      colorPortada: "#5C315B",
      colorLogo: "#2E7D5B",
      necesidades: {
        tieneStock: true,
        tieneAgendamiento: false,
        tieneVentaPorWhatsApp: true,
        tieneProductosAPedido: true,
        participaEnFerias: true,
        tieneForoComunidad: true
      }
    },
    {
      id: "t_reposteria_surena",
      nombre: "Repostería Casera Tradición Sureña",
      rubro: "Pastelería y Gastronomía",
      sector: "Senda Central",
      bio: "Kuchenes tradicionales de murta y frambuesa, tortas de hojarasca a pedido, alfajores caseros y galletas horneadas con amor.",
      whatsapp: "+56965432109",
      colorPortada: "#D69E2E",
      colorLogo: "#C53030",
      necesidades: {
        tieneStock: true,
        tieneAgendamiento: false,
        tieneVentaPorWhatsApp: true,
        tieneProductosAPedido: true,
        participaEnFerias: true,
        tieneForoComunidad: false
      }
    }
  ];

  var PRODUCTOS_DEMO = [
    // Doña Rosa
    {
      id: "p_mini_1",
      tiendaId: "t_minimarket_rosa",
      nombre: "Pan Amasado Caliente (Salida 17:00 hrs)",
      precio: 2500,
      stockTotal: 25,
      descripcion: "Pan amasado artesanal recién salido del horno de barro.",
      variantes: [{ nombre: "Bolsa 5 unidades", precio: 2500 }]
    },
    {
      id: "p_mini_2",
      tiendaId: "t_minimarket_rosa",
      nombre: "Canasta de Verduras Frescas de Alerce",
      precio: 7000,
      stockTotal: 10,
      descripcion: "Lechugas, papas chilotas, zanahorias y cilantro cosechados en el día.",
      variantes: [{ nombre: "Surtido Semanal", precio: 7000 }]
    },
    {
      id: "p_mini_3",
      tiendaId: "t_minimarket_rosa",
      nombre: "Huevos de Campo Libres (Docena)",
      precio: 4200,
      stockTotal: 15,
      descripcion: "Huevos frescos de gallinas felices de criadero local.",
      variantes: [{ nombre: "Docena", precio: 4200 }]
    },
    {
      id: "p_mini_4",
      tiendaId: "t_minimarket_rosa",
      nombre: "Miel de Ulmo Pura de Bosque Nativo 1kg",
      precio: 6500,
      stockTotal: 8,
      descripcion: "Miel 100% natural recolectada en la cordillera de la costa.",
      variantes: [{ nombre: "Frasco 1kg", precio: 6500 }]
    },

    // Tejidos Austral
    {
      id: "p_tej_1",
      tiendaId: "t_tejidos_austral",
      nombre: "Chaleco de Lana Chilota a Palillo",
      precio: 38000,
      stockTotal: 0, // A pedido
      descripcion: "Chaleco grueso tejido a mano con lana de oveja virgen sin teñir.",
      variantes: [
        { nombre: "Gris Natural · Talla M", precio: 38000 },
        { nombre: "Blanco Crudo · Talla L", precio: 38000 }
      ]
    },
    {
      id: "p_tej_2",
      tiendaId: "t_tejidos_austral",
      nombre: "Gorro y Cuello de Lana Pura de Oveja",
      precio: 14000,
      stockTotal: 6,
      descripcion: "Conjunto abrigado ideal para el invierno del sur.",
      variantes: [{ nombre: "Talla Única", precio: 14000 }]
    },
    {
      id: "p_tej_3",
      tiendaId: "t_tejidos_austral",
      nombre: "Piecera Rústica en Telar Tradicional",
      precio: 45000,
      stockTotal: 2,
      descripcion: "Piecera para cama de 2 plazas tejida en telar de cuatro cuadros.",
      variantes: [{ nombre: "2 Plazas", precio: 45000 }]
    },

    // Barbería Alerce
    {
      id: "p_bar_1",
      tiendaId: "t_barberia_alerce",
      nombre: "Corte de Cabello Clásico / Degrade Moderno",
      precio: 8000,
      stockTotal: 99,
      descripcion: "Sesión completa de 40 minutos con lavado y peinado con cera mate.",
      variantes: [{ nombre: "Sesión 40 min", precio: 8000 }]
    },
    {
      id: "p_bar_2",
      tiendaId: "t_barberia_alerce",
      nombre: "Perfilado y Ritual de Barba con Toalla Caliente",
      precio: 6500,
      stockTotal: 99,
      descripcion: "Ritual relajante con aceites esenciales y navaja tradicional.",
      variantes: [{ nombre: "Sesión 30 min", precio: 6500 }]
    },
    {
      id: "p_bar_3",
      tiendaId: "t_barberia_alerce",
      nombre: "Cera y Pomada Modeladora Capilar",
      precio: 7500,
      stockTotal: 12,
      descripcion: "Pote de 100g con acabado mate y fijación fuerte duradera.",
      variantes: [{ nombre: "Pote 100g", precio: 7500 }]
    },

    // Repostería Sureña
    {
      id: "p_rep_1",
      tiendaId: "t_reposteria_surena",
      nombre: "Kuchen Tradicional de Murta y Frambuesa",
      precio: 12000,
      stockTotal: 0, // A pedido
      descripcion: "Receta típica alemana sureña con masa crocante y frutas silvestres.",
      variantes: [{ nombre: "Molde Familiar (8 porciones)", precio: 12000 }]
    },
    {
      id: "p_rep_2",
      tiendaId: "t_reposteria_surena",
      nombre: "Torta de Hojarasca con Manjar Casero",
      precio: 18500,
      stockTotal: 0,
      descripcion: "Finas láminas de hojarasca con abundante manjar de leche condensada.",
      variantes: [{ nombre: "15 Personas", precio: 18500 }]
    },
    {
      id: "p_rep_3",
      tiendaId: "t_reposteria_surena",
      nombre: "Pack 6 Alfajores Artesanales con Nuez",
      precio: 5500,
      stockTotal: 14,
      descripcion: "Bañados en chocolate bitter y rellenos con nuez picada fina.",
      variantes: [{ nombre: "Caja 6 un.", precio: 5500 }]
    }
  ];

  // ============================================================
  // MODO DE DATOS: real (Supabase), demostración o sin configuración.
  // En producción nunca se muestran tiendas de ejemplo como si fueran reales.
  // ============================================================
  var Cat = window.AlerceCatalogo;
  var servicio = (supabaseClient && window.AlerceServicio) ? window.AlerceServicio.crear(supabaseClient) : null;
  var ES_LOCAL = /^(localhost|127\.0\.0\.1|\[::1\])$/.test(window.location.hostname);
  var MODO = servicio ? "real" : ((ES_LOCAL || /[?&]demo=1(&|$)/.test(window.location.search)) ? "demo" : "sin_config");
  var CATEGORIA_DEMO = { t_minimarket_rosa: "GOURMET", t_reposteria_surena: "GOURMET", t_tejidos_austral: "TEXTIL", t_barberia_alerce: "COSMETICA" };

  // Las versiones anteriores guardaban el catálogo en el navegador y mostraban
  // productos ocultos o borrados. Esos datos se eliminan al cargar.
  ["trama_productos_completo", "trama_productos_custom", "trama_tiendas_completo", "trama_tiendas_custom"].forEach(function (k) {
    try { localStorage.removeItem(k); } catch (e) { /* almacenamiento no disponible */ }
  });

  function productoDemo(p) {
    return Cat.normalizarProducto({
      id: p.id, tienda_id: p.tiendaId, nombre: p.nombre, descripcion: p.descripcion, precio: p.precio,
      publicado: true, categoria: CATEGORIA_DEMO[p.tiendaId] || "",
      modalidad: p.stockTotal === 0 ? "A_PEDIDO" : "REGULAR", maneja_stock: p.stockTotal !== 99,
      variantes: (p.variantes || []).map(function (v, i) {
        return { id: p.id + "_v" + (i + 1), nombre: v.nombre, precio: v.precio, stock: i === 0 ? p.stockTotal : 0 };
      })
    });
  }

  function tiendaDemo(t) {
    return Cat.normalizarTienda({
      id: t.id, nombre: t.nombre, rubro: t.rubro, sector: t.sector, bio: t.bio, whatsapp: t.whatsapp,
      color_portada: t.colorPortada, color_logo: t.colorLogo, necesidades: t.necesidades
    });
  }

  var TIENDAS = MODO === "demo" ? TIENDAS_DEMO.map(tiendaDemo) : [];
  var MIS_PRODUCTOS = [];          // productos de la tienda con sesión (incluye los que solo ella ve)
  var TIPOS = {};                  // tipos_producto por id
  var FERIAS_SERVIDOR = [];
  var estadoCatalogo = { cargando: MODO === "real", cargado: MODO === "demo", error: null, actualizado: null };
  var promesaCatalogo = Promise.resolve();
  var seleccionOpcion = {};        // productoId -> varianteId elegida en la vitrina

  function cargarProductosDeStorage() {
    return MODO === "demo" ? PRODUCTOS_DEMO.map(productoDemo) : [];
  }

  /** Crea un elemento sin interpretar HTML: los datos van como texto o atributos validados. */
  function h(tag, props, hijos) {
    var el = document.createElement(tag);
    if (props) {
      Object.keys(props).forEach(function (k) {
        var v = props[k];
        if (v === null || v === undefined || v === false) return;
        if (k === "class") el.className = v;
        else if (k === "text") el.textContent = v;
        else if (k === "style") el.style.cssText = v;
        else if (k.indexOf("on") === 0 && typeof v === "function") el.addEventListener(k.slice(2), v);
        else el.setAttribute(k, v === true ? "" : String(v));
      });
    }
    (hijos || []).forEach(function (c) {
      if (c === null || c === undefined || c === false) return;
      el.appendChild(typeof c === "string" ? document.createTextNode(c) : c);
    });
    return el;
  }

  function imagenSegura(url, alt, clase, estilo) {
    var limpia = Cat.urlImagenSegura(url);
    if (!limpia) return null;
    var img = h("img", { src: limpia, alt: alt || "", class: clase, style: estilo, loading: "lazy", decoding: "async" });
    img.addEventListener("error", function () { img.style.display = "none"; });
    return img;
  }

  function enlaceWhatsapp(numero, texto) {
    var digitos = String(numero || "").replace(/[^0-9]/g, "");
    if (!digitos) return null;
    return "https://wa.me/" + digitos + "?text=" + encodeURIComponent(texto);
  }

  // El catálogo ya no se guarda en el navegador: la fuente de verdad es el servidor.
  function guardarProductosEnStorage() {}
  /** Conecta los controles nuevos (sin atributos onclick en el HTML). */
  function configurarDomEntrega5() {
    function al(id, evento, fn) { var el = document.getElementById(id); if (el) el.addEventListener(evento, fn); }
    al("pub-categoria", "change", function () { seleccionarCategoriaFB(); });
    al("pub-tipo", "change", function () { pintarCamposEspecificos({}); });
    al("pub-maneja-stock", "change", actualizarReglasPublicar);
    document.querySelectorAll('input[name="pub-modalidad"]').forEach(function (r) { r.addEventListener("change", actualizarReglasPublicar); });
    al("pub-agregar-opcion", "click", agregarOpcionPublicar);
    al("pub-guardar", "click", confirmarPublicacionFB);
    al("pub-foto-input", "change", onFotoProductoSubida);
    al("pub-quitar-foto", "click", quitarFotoProducto);
    al("pub-abrir-afiche", "click", function () { abrirEstudioFlyerDesdePublicar(); });
    al("gestion-nuevo", "click", function () { abrirModalPublicarProducto(); });
    al("gestion-disponibles", "click", function () { cambiarDisponibleSeleccion(true); });
    al("gestion-no-disponibles", "click", function () { cambiarDisponibleSeleccion(false); });
    al("gestion-filtro-categoria", "change", renderizarGestionCatalogo);
    al("pos-lugar", "change", pintarLugaresPOS);
    al("pos-btn-confirmar", "click", confirmarVentaPOS);
    al("btn-guardar-perfil-tienda", "click", guardarPerfilTienda);

    var accesosDemo = document.getElementById("auth-accesos-demo");
    if (accesosDemo) accesosDemo.style.display = MODO === "demo" ? "" : "none";
    pintarEstadoDatos();
  }


  var PRODUCTOS = cargarProductosDeStorage();

  var PUBLICACIONES_FORO = [
    {
      id: "pub_rosa_1",
      tiendaId: "t_minimarket_rosa",
      autorNombre: "Minimarket Doña Rosa",
      esEmprendedor: true,
      texto: "🥖 ¡Aviso a los vecinos de Alerce! Acaba de salir la primera tanda de pan amasado bien calentito y recibimos huevos frescos de campo. ¡Los esperamos!",
      fecha: "Hoy, 16:30",
      respuestas: [
        {
          id: "resp_1",
          autorNombre: "Carlos M.",
          esEmprendedor: false,
          texto: "¿Doña Rosa, le queda queso de campo artesanal para acompañar el pancito?",
          fecha: "Hoy, 16:45"
        },
        {
          id: "resp_2",
          autorNombre: "Minimarket Doña Rosa",
          esEmprendedor: true,
          texto: "¡Hola! Sí vecino, nos llegaron 3 piezas recién traídas de Los Muermos. Le guardo una si gusta.",
          fecha: "Hoy, 16:50"
        }
      ]
    },
    {
      id: "pub_rosa_2",
      tiendaId: "t_minimarket_rosa",
      autorNombre: "Marta Gómez",
      esEmprendedor: false,
      texto: "¿Vecina Doña Rosa, hasta qué hora atiende hoy domingo? Necesito pasar por abarrotes y bebidas para la once familiar.",
      fecha: "Ayer, 18:20",
      respuestas: [
        {
          id: "resp_3",
          autorNombre: "Minimarket Doña Rosa",
          esEmprendedor: true,
          texto: "¡Hola Marta! Atendemos de corrido hasta las 21:30 hrs todos los días.",
          fecha: "Ayer, 18:35"
        }
      ]
    },
    {
      id: "pub_tej_1",
      tiendaId: "t_tejidos_austral",
      autorNombre: "Tejidos & Lanares Alerce Austral",
      esEmprendedor: true,
      texto: "🧶 ¡Estimadas clientas! Acabamos de hilar una nueva partida de lana pura teñida con cáscara de cebolla y corteza de nalca. Aceptamos pedidos para el invierno.",
      fecha: "Hace 2 días",
      respuestas: []
    }
  ];

  // El muro aún no se guarda en el servidor: fuera del modo demostración no se muestran mensajes de ejemplo.
  if (MODO !== "demo") PUBLICACIONES_FORO = [];

  var ASOCIACIONES = [
    {
      id: "aso_1",
      nombre: "Manos del Sur Alerce",
      sector: "Alerce Histórico",
      descripcion: "Agrupación de artesanas y productoras locales que organizan ferias comunales y compras asociativas de insumos.",
      whatsapp: "+56961230000",
      integrantes: 18
    },
    {
      id: "aso_2",
      nombre: "Red Gastronómica Tradición y Sabor",
      sector: "Alerce Norte",
      descripcion: "Red de emprendedores de cocina tradicional, panadería artesanal y repostería casera de la comuna.",
      whatsapp: "+56961230000",
      integrantes: 12
    }
  ];

  // ============================================================
  // ESTADO GLOBAL REACTIVO (CARRITO, TIENDA ACTUAL, USUARIO)
  // ============================================================

  var estado = {
    categoriaActiva: "Todos",
    textoBusqueda: "",
    tiendaSeleccionada: null,
    tabModalActiva: 0, // 0 = Catalogo, 1 = Muro y Foro
    medioPagoSeleccionado: "Efectivo",
    fechaAgendaSeleccionada: "",
    horaAgendaSeleccionada: "10:00",
    carrito: cargarCarritoDesdeStorage(),
    favoritos: cargarFavoritosDesdeStorage(),
    usuario: {
      nombre: "",
      rol: "Visitante"
    }
  };

  // Helper de formato de moneda chilena
  function formatearMoneda(valor) {
    return "$ " + Math.round(valor).toLocaleString("es-CL");
  }

  var toastTimer = null;
  function mostrarToast(mensaje, botonTexto, botonAccion) {
    var toast = document.getElementById("toast-alerta");
    var texto = document.getElementById("toast-texto");
    if (!toast || !texto) return;

    texto.textContent = mensaje;

    // Remover botón previo si existe
    var btnPrevio = toast.querySelector(".btn-toast-ver-carrito");
    if (btnPrevio) btnPrevio.remove();

    if (botonTexto && typeof botonAccion === "function") {
      var btn = document.createElement("button");
      btn.type = "button";
      btn.className = "btn-toast-ver-carrito";
      btn.textContent = botonTexto;
      btn.onclick = function (e) {
        e.stopPropagation();
        toast.style.display = "none";
        botonAccion();
      };
      toast.appendChild(btn);
    }

    toast.style.display = "flex";
    if (toastTimer) clearTimeout(toastTimer);
    toastTimer = setTimeout(function () {
      toast.style.display = "none";
    }, 4500);
  }

  // ============================================================
  // GESTIÓN DEL CARRITO DE COMPRAS (COMPONENTE DEL COMPAÑERO)
  // ============================================================

  function cargarCarritoDesdeStorage() {
    try {
      var guardado = localStorage.getItem("trama_carrito_compras");
      return guardado ? JSON.parse(guardado) : [];
    } catch (e) {
      return [];
    }
  }

  function guardarCarritoEnStorage() {
    try {
      localStorage.setItem("trama_carrito_compras", JSON.stringify(estado.carrito));
    } catch (e) {}
    actualizarBadgesCarrito();
  }

  function cargarFavoritosDesdeStorage() {
    try {
      var guardado = localStorage.getItem("trama_tiendas_favoritas");
      return guardado ? JSON.parse(guardado) : {};
    } catch (e) {
      return {};
    }
  }

  function guardarFavoritosEnStorage() {
    try {
      localStorage.setItem("trama_tiendas_favoritas", JSON.stringify(estado.favoritos));
    } catch (e) {}
  }

  function obtenerIconoProducto(nombre) {
    var n = (nombre || "").toLowerCase();
    if (n.includes("pan") || n.includes("marraqueta") || n.includes("hallulla")) return "🥖";
    if (n.includes("huevo")) return "🥚";
    if (n.includes("miel")) return "🍯";
    if (n.includes("tejido") || n.includes("lana") || n.includes("chaleco") || n.includes("gorro") || n.includes("cuello") || n.includes("piecera")) return "🧶";
    if (n.includes("corte") || n.includes("barba") || n.includes("pelo") || n.includes("cera")) return "💈";
    if (n.includes("torta") || n.includes("kuchen") || n.includes("alfajor") || n.includes("dulce") || n.includes("pasteleria")) return "🍰";
    if (n.includes("cafe") || n.includes("te")) return "☕";
    if (n.includes("pizza") || n.includes("empanada")) return "🍕";
    return "🛍️";
  }

  function agregarAlCarrito(productoId, varianteId) {
    var prod = PRODUCTOS.find(function (p) { return p.id === productoId; });
    if (!prod) return;
    var estadoP = Cat.estadoPublico(prod);
    if (estadoP === "OCULTO") return;
    var tienda = TIENDAS.find(function (t) { return t.id === prod.tiendaId; });
    var variante = prod.variantes.find(function (v) { return v.id === varianteId; }) || prod.variantes[0] || null;
    var encargo = estadoP === "A_PEDIDO";
    if (!encargo && (estadoP === "AGOTADO" || !Cat.opcionVendible(prod, variante))) {
      mostrarToast("Esa opción está agotada.");
      return;
    }
    var nombreOpcion = variante ? variante.nombre : "Única";
    var itemExistente = estado.carrito.find(function (item) {
      return item.productoId === prod.id && item.varianteId === (variante ? variante.id : null);
    });

    if (itemExistente) {
      itemExistente.cantidad += 1;
    } else {
      estado.carrito.push({
        id: "cart_" + Date.now() + "_" + Math.random().toString(36).substr(2, 4),
        productoId: prod.id,
        varianteId: variante ? variante.id : null,
        tiendaId: prod.tiendaId,
        tiendaNombre: tienda ? tienda.nombre : "Tienda",
        tiendaWhatsapp: tienda ? tienda.whatsapp : "",
        nombre: prod.nombre + (encargo ? " (encargo)" : ""),
        varianteNombre: nombreOpcion,
        precio: Cat.precioDeOpcion(prod, variante),
        cantidad: 1,
        fotoUrl: Cat.fotoDeOpcion(prod, variante)
      });
    }

    guardarCarritoEnStorage();
    renderizarCarrito();
    mostrarToast("«" + prod.nombre + "» agregado al carrito", "Ver carrito", function () {
      renderizarCarrito();
      abrirModal("modal-carrito");
    });
  }

  function actualizarCantidadCarrito(itemId, delta) {
    var idx = estado.carrito.findIndex(function (i) { return i.id === itemId; });
    if (idx !== -1) {
      estado.carrito[idx].cantidad += delta;
      if (estado.carrito[idx].cantidad <= 0) {
        estado.carrito.splice(idx, 1);
      }
      guardarCarritoEnStorage();
      renderizarCarrito();
    }
  }

  function eliminarItemCarrito(itemId) {
    estado.carrito = estado.carrito.filter(function (i) { return i.id !== itemId; });
    guardarCarritoEnStorage();
    renderizarCarrito();
    mostrarToast("Producto quitado del carrito");
  }

  function vaciarCarrito() {
    if (estado.carrito.length === 0) return;
    if (confirm("¿Deseas vaciar todos los productos del carrito?")) {
      estado.carrito = [];
      guardarCarritoEnStorage();
      renderizarCarrito();
      mostrarToast("Carrito vaciado");
    }
  }

  function actualizarBadgesCarrito() {
    var totalArticulos = estado.carrito.reduce(function (acc, i) { return acc + i.cantidad; }, 0);
    var sumaTotal = estado.carrito.reduce(function (acc, i) { return acc + (i.precio * i.cantidad); }, 0);

    var bHeader = document.getElementById("contador-carrito-badge");
    var bFlotante = document.getElementById("contador-carrito-flotante");
    var tFlotante = document.getElementById("total-carrito-flotante");
    var btnFlotante = document.getElementById("btn-flotante-carrito");

    if (bHeader) bHeader.textContent = totalArticulos;
    if (bFlotante) bFlotante.textContent = totalArticulos;
    if (tFlotante) tFlotante.textContent = formatearMoneda(sumaTotal);

    if (btnFlotante) {
      btnFlotante.style.display = totalArticulos > 0 ? "flex" : "none";
    }
  }

  function renderizarCarrito() {
    var listaContainer = document.getElementById("lista-items-carrito");
    var avisoVacio = document.getElementById("carrito-vacio-aviso");
    var seccionResumen = document.getElementById("carrito-resumen-seccion");
    var subtotalTexto = document.getElementById("carrito-subtotal-texto");
    var totalTexto = document.getElementById("carrito-total-texto");

    if (!listaContainer) return;

    if (estado.carrito.length === 0) {
      listaContainer.innerHTML = "";
      if (avisoVacio) avisoVacio.style.display = "block";
      if (seccionResumen) seccionResumen.style.display = "none";
      return;
    }

    if (avisoVacio) avisoVacio.style.display = "none";
    if (seccionResumen) seccionResumen.style.display = "block";

    // Agrupar items por tienda para orden y claridad
    var tiendasMap = {};
    var totalGlobal = 0;

    estado.carrito.forEach(function (item) {
      if (!tiendasMap[item.tiendaId]) {
        tiendasMap[item.tiendaId] = {
          tiendaId: item.tiendaId,
          nombre: item.tiendaNombre,
          whatsapp: item.tiendaWhatsapp,
          items: [],
          subtotal: 0
        };
      }
      tiendasMap[item.tiendaId].items.push(item);
      tiendasMap[item.tiendaId].subtotal += item.precio * item.cantidad;
      totalGlobal += item.precio * item.cantidad;
    });

    var keysTiendas = Object.keys(tiendasMap);
    var html = "";

    keysTiendas.forEach(function (tId) {
      var grupo = tiendasMap[tId];
      var esMultipleTienda = keysTiendas.length > 1;

      if (esMultipleTienda) {
        html += '<div class="item-carrito-grupo-tienda">';
        html += '  <div class="item-carrito-grupo-tienda-cabecera">';
        html += '    <strong style="font-size: 13px; color: var(--negro-apple);">🏪 ' + escaparHtml(grupo.nombre) + '</strong>';
        html += '    <span style="font-size: 11.5px; font-weight: 700; color: var(--gris-texto);">Subtotal: ' + formatearMoneda(grupo.subtotal) + '</span>';
        html += '  </div>';
      }

      grupo.items.forEach(function (item) {
        var subtotalItem = item.precio * item.cantidad;
        var thumbHtml = item.fotoUrl
          ? '<img src="' + escaparHtml(item.fotoUrl) + '" alt="' + escaparHtml(item.nombre) + '" class="item-carrito-img">'
          : '<div class="item-carrito-img-placeholder">' + obtenerIconoProducto(item.nombre) + '</div>';

        html += '<div class="item-carrito-fila">';
        html += '  ' + thumbHtml;
        html += '  <div class="item-carrito-info">';
        html += '    <h4 class="item-carrito-titulo">' + escaparHtml(item.nombre) + '</h4>';
        html += '    <p class="item-carrito-tienda">' + escaparHtml(item.tiendaNombre) + (item.varianteNombre && item.varianteNombre !== "Estándar" ? ' · ' + escaparHtml(item.varianteNombre) : '') + '</p>';
        html += '    <p class="item-carrito-precio">' + formatearMoneda(item.precio) + ' c/u · Subtotal: <strong>' + formatearMoneda(subtotalItem) + '</strong></p>';
        html += '  </div>';
        html += '  <div class="controles-cantidad">';
        html += '    <button type="button" class="btn-qty" onclick="TramaWeb.actualizarCantidadCarrito(\'' + item.id + '\', -1)" aria-label="Disminuir">−</button>';
        html += '    <span class="qty-numero">' + item.cantidad + '</span>';
        html += '    <button type="button" class="btn-qty" onclick="TramaWeb.actualizarCantidadCarrito(\'' + item.id + '\', 1)" aria-label="Aumentar">+</button>';
        html += '    <button type="button" class="btn-eliminar-item" onclick="TramaWeb.eliminarItemCarrito(\'' + item.id + '\')" title="Quitar producto" aria-label="Quitar">🗑️</button>';
        html += '  </div>';
        html += '</div>';
      });

      if (esMultipleTienda) {
        html += '  <div style="text-align: right; margin-top: 8px;">';
        html += '    <button type="button" class="btn-accion-pill" style="height: 30px; font-size: 11px; padding: 0 12px;" onclick="TramaWeb.finalizarPedidoWhatsApp(\'' + tId + '\')">';
        html += '      Pedir a ' + escaparHtml(grupo.nombre) + ' por WhatsApp ➔';
        html += '    </button>';
        html += '  </div>';
        html += '</div>';
      }
    });

    listaContainer.innerHTML = html;
    if (subtotalTexto) subtotalTexto.textContent = formatearMoneda(totalGlobal);
    if (totalTexto) totalTexto.textContent = formatearMoneda(totalGlobal);

    // Si está seleccionado Pago QR, actualizar el código QR
    if (estado.medioPagoSeleccionado === "QR") {
      generarQRPago(totalGlobal);
    }
    // Si Efectivo estaba activo, recalcular vuelto
    if (estado.medioPagoSeleccionado === "Efectivo" && estado.pagoConEfectivo) {
      calcularVueltoEfectivo(estado.pagoConEfectivo);
    }
  }

  function seleccionarMedioPago(medio) {
    estado.medioPagoSeleccionado = medio;
    var pills = document.querySelectorAll("#selector-medio-pago .pill-pago");
    pills.forEach(function (p) {
      if (p.getAttribute("data-pago") === medio) {
        p.classList.add("activo");
      } else {
        p.classList.remove("activo");
      }
    });

    var cajaQR = document.getElementById("caja-qr-pago");
    var cajaEfectivo = document.getElementById("caja-efectivo-pago");

    if (medio === "QR") {
      if (cajaQR) cajaQR.style.display = "block";
      var total = estado.carrito.reduce(function (acc, i) { return acc + (i.precio * i.cantidad); }, 0);
      generarQRPago(total);
    } else {
      if (cajaQR) cajaQR.style.display = "none";
    }

    if (medio === "Efectivo") {
      if (cajaEfectivo) cajaEfectivo.style.display = "block";
    } else {
      if (cajaEfectivo) cajaEfectivo.style.display = "none";
    }
  }

  function calcularVueltoEfectivo(montoIngresado) {
    var total = estado.carrito.reduce(function (acc, i) { return acc + (i.precio * i.cantidad); }, 0);
    var cajaResultado = document.getElementById("caja-resultado-vuelto");
    var textoVuelto = document.getElementById("texto-monto-vuelto");
    var monto = parseInt(montoIngresado, 10);
    if (!isNaN(monto) && monto >= total) {
      var vuelto = monto - total;
      if (cajaResultado) cajaResultado.style.display = "flex";
      if (textoVuelto) textoVuelto.textContent = formatearMoneda(vuelto);
      estado.pagoConEfectivo = monto;
    } else {
      if (cajaResultado) cajaResultado.style.display = "none";
      estado.pagoConEfectivo = 0;
    }
  }

  function generarQRPago(monto) {
    var qrContainer = document.getElementById("qr-pago-container");
    if (!qrContainer) return;
    qrContainer.innerHTML = "";

    var textoQR = "TRAMA-PAGO|ORDEN-" + Date.now() + "|MONTO-" + monto + "|ALERCE-PUERTOMONTT";
    if (window.QRCode) {
      new window.QRCode(qrContainer, {
        text: textoQR,
        width: 170,
        height: 170,
        colorDark: "#111111",
        colorLight: "#ffffff"
      });
    }
  }

  function finalizarPedidoWhatsApp(tiendaIdFiltro) {
    if (estado.carrito.length === 0) {
      mostrarToast("Tu carrito está vacío.");
      return;
    }

    var itemsParaEnviar = estado.carrito;
    if (tiendaIdFiltro) {
      itemsParaEnviar = estado.carrito.filter(function (i) { return i.tiendaId === tiendaIdFiltro; });
    }

    if (itemsParaEnviar.length === 0) return;

    var primerItem = itemsParaEnviar[0];
    var telefono = primerItem.tiendaWhatsapp ? primerItem.tiendaWhatsapp.replace(/[^0-9]/g, "") : "56961230000";
    var total = itemsParaEnviar.reduce(function (acc, i) { return acc + (i.precio * i.cantidad); }, 0);
    var nombreCliente = (estado.usuario && estado.usuario.nombre && estado.usuario.nombre.trim() !== "" && estado.usuario.nombre !== "Visitante")
      ? estado.usuario.nombre.trim()
      : "Vecino(a) de Alerce";

    var mensaje = "👋 ¡Hola *" + primerItem.tiendaNombre + "*! Quiero confirmar mi pedido desde la vitrina de *Alerce Emprende*:\n\n";
    mensaje += "📦 *Detalle de la compra:*\n";
    itemsParaEnviar.forEach(function (i) {
      var variante = (i.varianteNombre && i.varianteNombre !== "Estándar") ? " (" + i.varianteNombre + ")" : "";
      mensaje += "• " + i.cantidad + "x " + i.nombre + variante + " — " + formatearMoneda(i.precio * i.cantidad) + "\n";
    });
    mensaje += "\n💰 *Total a pagar:* " + formatearMoneda(total);
    mensaje += "\n💳 *Medio de Pago:* " + (estado.medioPagoSeleccionado || "Efectivo");
    if (estado.medioPagoSeleccionado === "Efectivo" && estado.pagoConEfectivo && estado.pagoConEfectivo >= total) {
      var vuelto = estado.pagoConEfectivo - total;
      mensaje += " (Paga con: " + formatearMoneda(estado.pagoConEfectivo) + " · Vuelto: " + formatearMoneda(vuelto) + ")";
    }
    mensaje += "\n👤 *Cliente:* " + nombreCliente;
    mensaje += "\n📍 *Sector:* Alerce, Puerto Montt\n";
    mensaje += "\n_Pedido coordinado mediante Trama Alerce Emprende._";

    var url = "https://wa.me/" + telefono + "?text=" + encodeURIComponent(mensaje);
    window.open(url, "_blank");
    mostrarToast("¡Abriendo WhatsApp para confirmar tu pedido con " + primerItem.tiendaNombre + "!");
  }

  // ============================================================
  // RENDERIZADO DEL MARKETPLACE Y VITRINA DE TIENDAS
  // ============================================================

  function renderizarTiendasMarketplace() {
    var contenedor = document.getElementById("grilla-tiendas-marketplace");
    var avisoVacio = document.getElementById("aviso-sin-tiendas");
    var subtitulo = document.getElementById("subtitulo-conteo-tiendas");
    if (!contenedor) return;
    pintarEstadoDatos();

    var filtro = estado.categoriaActiva;
    var texto = estado.textoBusqueda.toLowerCase().trim();

    var tiendasFiltradas = TIENDAS.filter(function (t) {
      var coincideCategoria = filtro === "Todos" || t.rubro === filtro;
      var coincideTexto = texto === "" ||
        t.nombre.toLowerCase().indexOf(texto) !== -1 ||
        t.rubro.toLowerCase().indexOf(texto) !== -1 ||
        t.sector.toLowerCase().indexOf(texto) !== -1;
      return coincideCategoria && coincideTexto;
    });

    if (subtitulo) {
      subtitulo.textContent = estadoCatalogo.cargando && !estadoCatalogo.cargado
        ? "Cargando emprendimientos…"
        : "Mostrando " + tiendasFiltradas.length + " emprendimientos de Alerce (" + filtro + ")";
    }

    contenedor.textContent = "";
    if (tiendasFiltradas.length === 0) {
      if (avisoVacio) {
        avisoVacio.textContent = (MODO === "sin_config" || (estadoCatalogo.error && !estadoCatalogo.cargado))
          ? "No pudimos mostrar las tiendas. Revisa el aviso de arriba."
          : (estadoCatalogo.cargando ? "Cargando tiendas…" : "No encontramos tiendas que coincidan con la búsqueda.");
        avisoVacio.style.display = "block";
      }
      return;
    }
    if (avisoVacio) avisoVacio.style.display = "none";

    tiendasFiltradas.forEach(function (tienda) {
      var esFav = !!estado.favoritos[tienda.id];
      var nec = tienda.necesidades || {};
      var badges = [];
      if (nec.tieneStock) badges.push(h("span", { class: "badge-necesidad badge-stock", text: "📦 Stock" }));
      if (nec.tieneProductosAPedido) badges.push(h("span", { class: "badge-necesidad badge-pedido", text: "✂️ A pedido" }));
      if (nec.participaEnFerias) badges.push(h("span", { class: "badge-necesidad badge-ferias", text: "🎪 Ferias" }));
      if (nec.tieneForoComunidad) badges.push(h("span", { class: "badge-necesidad badge-foro", text: "💬 Muro y foro" }));
      if (nec.tieneAgendamiento) badges.push(h("span", { class: "badge-necesidad badge-agenda", text: "📅 Agenda horas" }));

      var logo = imagenSegura(tienda.logoUrl, "", "avatar-circulo", "object-fit: cover;") ||
        h("div", { class: "avatar-circulo", style: "background-color: " + Cat.colorHex(tienda.colorLogo, "#2B6CB0") + ";", "aria-hidden": "true", text: tienda.nombre.charAt(0).toUpperCase() });

      var accionPrincipal = nec.tieneAgendamiento
        ? h("button", { type: "button", class: "btn-accion-pill btn-agenda", text: "Agendar cita", onclick: function () { abrirAgendamientoDirecto(tienda.id); } })
        : h("button", { type: "button", class: "btn-accion-pill btn-negro", text: nec.tieneForoComunidad ? "Vitrina y foro" : "Ver vitrina", onclick: function () { abrirVitrinaTienda(tienda.id); } });
      var urlWsp = enlaceWhatsapp(tienda.whatsapp, "Hola " + tienda.nombre + "! Vi tu tienda en Alerce Emprende y me gustaría hacer una consulta.");

      contenedor.appendChild(h("article", { class: "tarjeta-tienda" }, [
        h("div", { class: "tarjeta-franja-color", style: "background-color: " + Cat.colorHex(tienda.colorPortada, "#1A202C") + ";" }, [
          h("span", { class: "franja-rubro", text: tienda.rubro }),
          h("div", { class: "franja-lado-derecho" }, [
            h("span", { class: "franja-sector-chip", text: tienda.sector }),
            h("button", {
              type: "button", class: "btn-favorito-corazon" + (esFav ? " activo" : ""), "aria-pressed": esFav ? "true" : "false",
              "aria-label": (esFav ? "Quitar de favoritos: " : "Agregar a favoritos: ") + tienda.nombre, text: esFav ? "❤️" : "🤍",
              onclick: function () { toggleFavorito(tienda.id); }
            })
          ])
        ]),
        h("div", { class: "tarjeta-cuerpo" }, [
          h("div", { class: "tarjeta-perfil-fila" }, [
            logo,
            h("div", { class: "tarjeta-titulos" }, [
              h("h3", { class: "tarjeta-nombre", text: tienda.nombre }),
              h("p", { class: "tarjeta-sub", text: [tienda.rubro, tienda.sector].filter(Boolean).join(" · ") })
            ])
          ]),
          h("p", { class: "tarjeta-descripcion", text: tienda.bio }),
          h("div", { class: "tarjeta-badges-wrap" }, badges),
          h("hr", { class: "divisor-sutil" }),
          h("div", { class: "tarjeta-acciones-fila" }, [
            accionPrincipal,
            urlWsp ? h("a", { href: urlWsp, target: "_blank", rel: "noopener", class: "btn-accion-pill", text: "WhatsApp" }) : null,
            h("button", { type: "button", class: "btn-qr-icon", "aria-label": "Código QR de " + tienda.nombre, title: "Código QR de la tienda", text: "▦", onclick: function () { abrirQRTienda(tienda.id); } })
          ])
        ])
      ]));
    });
  }

  function toggleFavorito(tiendaId) {
    if (estado.favoritos[tiendaId]) {
      delete estado.favoritos[tiendaId];
      mostrarToast("Tienda removida de favoritos");
    } else {
      estado.favoritos[tiendaId] = true;
      mostrarToast("¡Tienda agregada a favoritos!");
    }
    guardarFavoritosEnStorage();
    renderizarTiendasMarketplace();
  }

  // ============================================================
  // MODAL VITRINA Y FORO DE TIENDA
  // ============================================================

  function abrirVitrinaTienda(tiendaId) {
    var tienda = TIENDAS.find(function (t) { return t.id === tiendaId; });
    if (!tienda) return;
    estado.tiendaSeleccionada = tienda;
    estado.tabModalActiva = 0;

    var avatar = document.getElementById("modal-tienda-avatar");
    var titulo = document.getElementById("modal-tienda-titulo");
    var rubroSector = document.getElementById("modal-tienda-rubro-sector");
    var bio = document.getElementById("modal-tienda-bio");
    var bannerAgenda = document.getElementById("modal-tienda-banner-agenda");
    var btnWhatsapp = document.getElementById("btn-modal-tienda-whatsapp");

    if (avatar) {
      avatar.textContent = "";
      var logo = imagenSegura(tienda.logoUrl, "", null, "width: 100%; height: 100%; object-fit: cover; border-radius: 50%;");
      avatar.style.backgroundColor = logo ? "transparent" : Cat.colorHex(tienda.colorLogo, "#111111");
      if (logo) avatar.appendChild(logo);
      else avatar.textContent = tienda.nombre.charAt(0).toUpperCase();
    }
    if (titulo) titulo.textContent = tienda.nombre;
    if (rubroSector) rubroSector.textContent = [tienda.rubro, tienda.sector].filter(Boolean).join(" · ");
    if (bio) bio.textContent = tienda.bio;
    if (bannerAgenda) bannerAgenda.style.display = tienda.necesidades.tieneAgendamiento ? "block" : "none";
    if (btnWhatsapp) {
      var url = enlaceWhatsapp(tienda.whatsapp, "Hola " + tienda.nombre + "! Vi tu vitrina en Alerce Emprende.");
      if (url) { btnWhatsapp.href = url; btnWhatsapp.style.display = ""; } else { btnWhatsapp.removeAttribute("href"); btnWhatsapp.style.display = "none"; }
    }

    cambiarTabModal(0);
    actualizarContadorTabsModal();
    abrirModal("modal-tienda-vitrina");
  }

  function cambiarTabModal(indice) {
    estado.tabModalActiva = indice;
    var tabCat = document.getElementById("tab-modal-catalogo");
    var tabForo = document.getElementById("tab-modal-foro");
    var contCat = document.getElementById("contenido-tab-catalogo");
    var contForo = document.getElementById("contenido-tab-foro");

    if (indice === 0) {
      if (tabCat) tabCat.classList.add("activo");
      if (tabForo) tabForo.classList.remove("activo", "activo-foro");
      if (contCat) contCat.style.display = "block";
      if (contForo) contForo.style.display = "none";
      renderizarProductosModal();
    } else {
      if (tabCat) tabCat.classList.remove("activo");
      if (tabForo) tabForo.classList.add("activo-foro");
      if (contCat) contCat.style.display = "none";
      if (contForo) contForo.style.display = "block";
      renderizarMuroModal();
    }
  }

  function productosPublicosDeTienda(tiendaId) {
    return PRODUCTOS.filter(function (p) {
      return p.tiendaId === tiendaId && Cat.estadoPublico(p) !== "OCULTO";
    }).sort(function (a, b) { return (b.destacado ? 1 : 0) - (a.destacado ? 1 : 0); });
  }

  function actualizarContadorTabsModal() {
    if (!estado.tiendaSeleccionada) return;
    var prods = productosPublicosDeTienda(estado.tiendaSeleccionada.id);
    var pubs = PUBLICACIONES_FORO.filter(function (f) { return f.tiendaId === estado.tiendaSeleccionada.id; });
    var cCat = document.getElementById("conteo-tab-catalogo");
    var cForo = document.getElementById("conteo-tab-foro");
    if (cCat) cCat.textContent = prods.length;
    if (cForo) cForo.textContent = pubs.length;
  }

  function avisoErrorConReintento(mensaje) {
    return h("div", { class: "aviso-catalogo aviso-error", role: "alert" }, [
      h("span", { text: mensaje + " " }),
      h("button", { type: "button", class: "btn-accion-pill", text: "Reintentar", onclick: function () { cargarCatalogoPublico(); } })
    ]);
  }

  function renderizarProductosModal() {
    if (!estado.tiendaSeleccionada) return;
    var contenedor = document.getElementById("lista-productos-tienda-modal");
    if (!contenedor) return;
    contenedor.textContent = "";

    if (estadoCatalogo.cargando && !estadoCatalogo.cargado) {
      contenedor.appendChild(h("p", { class: "aviso-catalogo", role: "status", text: "Cargando catálogo…" }));
      return;
    }
    if (estadoCatalogo.error) {
      contenedor.appendChild(avisoErrorConReintento(estadoCatalogo.cargado
        ? "No se pudo actualizar el catálogo; se muestra lo último que llegó del servidor."
        : "No se pudo cargar el catálogo. " + Cat.mensajeError(estadoCatalogo.error)));
      if (!estadoCatalogo.cargado) return;
    }

    var prods = productosPublicosDeTienda(estado.tiendaSeleccionada.id);
    if (prods.length === 0) {
      contenedor.appendChild(h("p", { class: "aviso-catalogo", text: "Esta tienda aún no tiene productos a la vista." }));
      return;
    }
    prods.forEach(function (p) { contenedor.appendChild(tarjetaProductoVitrina(p)); });
  }

  function opcionElegida(p) {
    var id = seleccionOpcion[p.id];
    return p.variantes.find(function (v) { return v.id === id; }) ||
      p.variantes.find(function (v) { return Cat.opcionVendible(p, v); }) || p.variantes[0] || null;
  }

  function tarjetaProductoVitrina(p) {
    var estadoP = Cat.estadoPublico(p);
    var opcion = opcionElegida(p);
    var precio = Cat.precioDeOpcion(p, opcion);
    var foto = Cat.fotoDeOpcion(p, opcion);
    var vendible = estadoP === "A_PEDIDO" || (estadoP === "DISPONIBLE" && Cat.opcionVendible(p, opcion));
    var claseEstado = { DISPONIBLE: "badge-stock", AGOTADO: "badge-agotado", A_PEDIDO: "badge-pedido" }[estadoP] || "";
    var detalleUnidades = "";
    if (p.modalidad === "PIEZA_UNICA") detalleUnidades = "Pieza única";
    else if (estadoP === "DISPONIBLE" && p.manejaStock && opcion) detalleUnidades = opcion.stock > 0 ? "Quedan " + opcion.stock : "Opción agotada";

    var imagen = imagenSegura(foto, p.nombre + (opcion ? " — " + opcion.nombre : ""), "prod-foto") ||
      h("div", { class: "prod-foto prod-foto-vacia", "aria-hidden": "true", text: obtenerIconoProducto(p.nombre) });

    var chips = null;
    if (p.variantes.length > 1) {
      chips = h("div", { class: "prod-variantes-chips", role: "group", "aria-label": "Opciones de " + p.nombre },
        p.variantes.map(function (v) {
          var agotada = estadoP !== "A_PEDIDO" && !Cat.opcionVendible(p, v);
          return h("button", {
            type: "button", class: "chip-variante" + (opcion && v.id === opcion.id ? " activa" : "") + (agotada ? " agotada" : ""),
            "aria-pressed": opcion && v.id === opcion.id ? "true" : "false",
            text: v.nombre + (agotada ? " (agotada)" : ""),
            onclick: function () {
              seleccionOpcion[p.id] = v.id;
              var actual = document.querySelector('[data-producto-vitrina="' + CSS.escape(p.id) + '"]');
              if (actual) {
                var nueva = tarjetaProductoVitrina(p);
                actual.replaceWith(nueva);
                var boton = nueva.querySelector('.chip-variante[aria-pressed="true"]');
                if (boton) boton.focus();
              }
            }
          });
        }));
    }

    var urlWsp = estado.tiendaSeleccionada ? enlaceWhatsapp(estado.tiendaSeleccionada.whatsapp,
      "Hola " + estado.tiendaSeleccionada.nombre + "! Me interesa «" + p.nombre + (opcion && p.variantes.length > 1 ? " — " + opcion.nombre : "") +
      "» (" + formatearMoneda(precio) + ") que vi en tu vitrina de Alerce.") : null;

    return h("article", { class: "producto-fila-card", "data-producto-vitrina": p.id }, [
      h("div", { class: "producto-card-top" }, [
        h("div", { class: "producto-card-info" }, [
          imagen,
          h("div", null, [
            h("h4", { class: "prod-nombre", text: p.nombre }),
            Cat.etiquetaCategoria(p) ? h("p", { class: "prod-categoria", text: Cat.etiquetaCategoria(p) }) : null,
            p.descripcion ? h("p", { class: "prod-descripcion", text: p.descripcion }) : null
          ])
        ]),
        h("div", { class: "producto-card-precio" }, [
          h("span", { class: "prod-precio", text: formatearMoneda(precio) }),
          h("div", null, [h("span", { class: "badge-necesidad " + claseEstado, text: Cat.etiquetaEstado(estadoP) })]),
          detalleUnidades ? h("small", { class: "prod-unidades", text: detalleUnidades }) : null
        ])
      ]),
      chips,
      h("div", { class: "producto-botones-fila" }, [
        h("button", {
          type: "button", class: "btn-anadir-carrito", disabled: !vendible,
          text: !vendible ? "Agotado" : (estadoP === "A_PEDIDO" ? "🛒 Encargar" : "🛒 Agregar al carrito"),
          onclick: function () { agregarAlCarrito(p.id, opcion ? opcion.id : null); }
        }),
        urlWsp ? h("a", { href: urlWsp, target: "_blank", rel: "noopener", class: "btn-whatsapp-prod", text: "Pedir por WhatsApp" }) : null
      ])
    ]);
  }

  // ============================================================
  // MURO Y FORO COMUNITARIO EN VIVO (Live Forum)
  // ============================================================

  function renderizarMuroModal() {
    if (!estado.tiendaSeleccionada) return;
    var contenedor = document.getElementById("lista-mensajes-muro-tienda");
    var labelPregunta = document.getElementById("label-pregunta-muro");
    if (!contenedor) return;

    if (labelPregunta) {
      labelPregunta.textContent = "Pregúntale a " + estado.tiendaSeleccionada.nombre + " en el Muro:";
    }

    var publicaciones = PUBLICACIONES_FORO.filter(function (f) {
      return f.tiendaId === estado.tiendaSeleccionada.id;
    });

    if (publicaciones.length === 0) {
      contenedor.innerHTML = '<p style="font-size: 13px; color: var(--gris-texto); padding: 14px 0;">Aún no hay publicaciones en el muro. ¡Sé el primero en hacer una pregunta!</p>';
      return;
    }

    var html = "";
    publicaciones.forEach(function (pub) {
      var esOficial = pub.esEmprendedor;

      html += '<div class="mensaje-muro-card ' + (esOficial ? 'oficial' : '') + '">';
      html += '  <div class="muro-card-header">';
      html += '    <div class="autor-info">';
      html += '      <div class="autor-avatar-mini" style="background: ' + (esOficial ? '#d97706' : '#4b5563') + ';">' + pub.autorNombre.charAt(0).toUpperCase() + '</div>';
      html += '      <strong style="font-size: 13px;">' + escaparHtml(pub.autorNombre) + '</strong>';
      if (esOficial) {
        html += '      <span class="badge-oficial-dorado">Oficial</span>';
      }
      html += '    </div>';
      html += '    <span class="muro-fecha">' + escaparHtml(pub.fecha) + '</span>';
      html += '  </div>';

      html += '  <p class="muro-texto">' + escaparHtml(pub.texto) + '</p>';

      // Respuestas oficiales anidadas
      if (pub.respuestas && pub.respuestas.length > 0) {
        html += '  <div class="respuestas-anidadas">';
        pub.respuestas.forEach(function (resp) {
          html += '    <div class="respuesta-card">';
          html += '      <div class="respuesta-header">';
          html += '        <span>' + escaparHtml(resp.autorNombre) + '</span>';
          if (resp.esEmprendedor) {
            html += '        <span style="color: var(--rojo-alerce);">· Respuesta de la Tienda</span>';
          }
          html += '        <span class="muro-fecha" style="margin-left: auto;">' + escaparHtml(resp.fecha) + '</span>';
          html += '      </div>';
          html += '      <p style="font-size: 12px; color: var(--negro-apple);">' + escaparHtml(resp.texto) + '</p>';
          html += '    </div>';
        });
        html += '  </div>';
      }

      // Botón para responder como tienda
      html += '  <div style="margin-top: 8px;">';
      html += '    <button type="button" style="font-size: 11px; font-weight: 700; color: var(--rojo-alerce); cursor: pointer;" onclick="TramaWeb.responderPublicacionMuro(\'' + pub.id + '\')">';
      html += '      Responder como ' + escaparHtml(estado.tiendaSeleccionada.nombre) + ' ➔';
      html += '    </button>';
      html += '  </div>';

      html += '</div>';
    });

    contenedor.innerHTML = html;
  }

  function publicarPreguntaEnMuro() {
    var input = document.getElementById("input-pregunta-muro");
    if (!input || !estado.tiendaSeleccionada) return;
    var texto = input.value.trim();
    if (!texto) {
      alert("Por favor escribe tu consulta antes de publicar.");
      return;
    }

    var nueva = {
      id: "pub_" + Date.now(),
      tiendaId: estado.tiendaSeleccionada.id,
      autorNombre: (estado.usuario && estado.usuario.nombre) ? (estado.usuario.nombre + " (Vecino)") : "Vecino de Alerce",
      esEmprendedor: false,
      texto: texto,
      fecha: "Recién",
      respuestas: []
    };

    PUBLICACIONES_FORO.unshift(nueva);
    input.value = "";
    actualizarContadorTabsModal();
    renderizarMuroModal();
    mostrarToast("¡Consulta publicada en el muro de " + estado.tiendaSeleccionada.nombre + "!");
  }

  function responderPublicacionMuro(pubId) {
    var pub = PUBLICACIONES_FORO.find(function (f) { return f.id === pubId; });
    if (!pub || !estado.tiendaSeleccionada) return;

    var respuesta = prompt("Escribe tu respuesta oficial como " + estado.tiendaSeleccionada.nombre + " a " + pub.autorNombre + ":");
    if (!respuesta || !respuesta.trim()) return;

    pub.respuestas.push({
      id: "resp_" + Date.now(),
      autorNombre: estado.tiendaSeleccionada.nombre,
      esEmprendedor: true,
      texto: respuesta.trim(),
      fecha: "Recién"
    });

    renderizarMuroModal();
    mostrarToast("¡Respuesta oficial publicada!");
  }

  // ============================================================
  // CÓDIGO QR DE LA TIENDA (Matching TiendaQRScreen.kt)
  // ============================================================

  function abrirQRTienda(tiendaId) {
    var tienda = TIENDAS.find(function (t) { return t.id === tiendaId; });
    if (!tienda) return;
    estado.tiendaSeleccionada = tienda;
    abrirQRTiendaActual();
  }

  function abrirQRTiendaActual() {
    if (!estado.tiendaSeleccionada) return;
    var t = estado.tiendaSeleccionada;
    // Enlace real a la vitrina que abre directo la ficha de esta tienda
    var url = "https://agustin671.github.io/EmprendedoresPuertoMontt/?tienda=" + encodeURIComponent(t.id);

    var nombreEl = document.getElementById("qr-tienda-nombre");
    var urlEl = document.getElementById("qr-tienda-url");
    var canvasEl = document.getElementById("canvas-qr-tienda");

    if (nombreEl) nombreEl.textContent = t.nombre;
    if (urlEl) urlEl.textContent = url;

    if (canvasEl && window.QRCode) {
      canvasEl.innerHTML = "";
      new window.QRCode(canvasEl, {
        text: url,
        width: 220,
        height: 220,
        colorDark: "#111111",
        colorLight: "#ffffff"
      });
    }

    abrirModal("modal-qr-tienda");
  }

  function copiarEnlaceQR() {
    if (!estado.tiendaSeleccionada) return;
    var url = "https://agustin671.github.io/EmprendedoresPuertoMontt/?tienda=" + encodeURIComponent(estado.tiendaSeleccionada.id);
    if (navigator.clipboard) {
      navigator.clipboard.writeText(url).then(function () {
        mostrarToast("¡Enlace copiado al portapapeles!");
      });
    } else {
      prompt("Copia este enlace de la tienda:", url);
    }
  }

  // ============================================================
  // AGENDAMIENTO DE CITAS (Próximas 2 semanas)
  // ============================================================

  function abrirAgendamientoDirecto(tiendaId) {
    var tienda = TIENDAS.find(function (t) { return t.id === tiendaId; });
    if (!tienda) return;
    estado.tiendaSeleccionada = tienda;
    abrirAgendamientoActual();
  }

  function abrirAgendamientoActual() {
    if (!estado.tiendaSeleccionada) return;
    var t = estado.tiendaSeleccionada;

    var titulo = document.getElementById("modal-agenda-titulo");
    var subtitulo = document.getElementById("agenda-subtitulo");
    if (titulo) titulo.textContent = "Agendar Cita en " + t.nombre;
    if (subtitulo) subtitulo.textContent = "Selecciona fecha y hora para tu atención en " + t.sector;

    generarDiasAgenda();
    abrirModal("modal-agendamiento");
  }

  function generarDiasAgenda() {
    var contenedor = document.getElementById("barra-fechas-agenda");
    if (!contenedor) return;
    contenedor.innerHTML = "";

    var diasSemana = ["Dom", "Lun", "Mar", "Mié", "Jue", "Vie", "Sáb"];
    var meses = ["ene", "feb", "mar", "abr", "may", "jun", "jul", "ago", "sep", "oct", "nov", "dic"];
    var hoy = new Date();

    for (var i = 0; i < 14; i++) {
      var d = new Date(hoy.getTime() + (i * 24 * 60 * 60 * 1000));
      var textoDia = diasSemana[d.getDay()] + " " + d.getDate() + " " + meses[d.getMonth()];
      var iso = d.toISOString().split("T")[0];

      if (i === 0) estado.fechaAgendaSeleccionada = iso;

      var btn = document.createElement("button");
      btn.type = "button";
      btn.className = "pill-categoria " + (i === 0 ? "activa" : "");
      btn.textContent = textoDia;
      btn.setAttribute("data-iso", iso);
      btn.onclick = (function (fechaIso, elemento) {
        return function () {
          estado.fechaAgendaSeleccionada = fechaIso;
          document.querySelectorAll("#barra-fechas-agenda .pill-categoria").forEach(function (b) {
            b.classList.remove("activa");
          });
          elemento.classList.add("activa");
        };
      })(iso, btn);

      contenedor.appendChild(btn);
    }
  }

  function seleccionarHora(hora) {
    estado.horaAgendaSeleccionada = hora;
    document.querySelectorAll("#contenedor-horarios-agenda .pill-pago").forEach(function (btn) {
      if (btn.getAttribute("data-hora") === hora) {
        btn.classList.add("activo");
      } else {
        btn.classList.remove("activo");
      }
    });
  }

  function confirmarReservaCita() {
    var nombreInp = document.getElementById("agenda-nombre-cliente");
    var telInp = document.getElementById("agenda-telefono-cliente");
    var nombre = nombreInp ? nombreInp.value.trim() : "Cliente";
    var tel = telInp ? telInp.value.trim() : "";

    if (!nombre) {
      alert("Por favor ingresa tu nombre para la reserva.");
      return;
    }

    cerrarModal("modal-agendamiento");
    mostrarToast("¡Cita agendada para " + estado.fechaAgendaSeleccionada + " a las " + estado.horaAgendaSeleccionada + " hrs!");
  }

  // ============================================================
  // FICHA DE FERIAS & ASOCIACIONES
  // ============================================================

  function abrirFichaFeria(feriaClave) {
    var modalCuerpo = document.getElementById("modal-feria-cuerpo");
    if (!modalCuerpo) return;

    var info = {
      valle: {
        nombre: "Feria de Emprendedoras del Valle",
        fecha: "Miércoles 7 de octubre de 2026 · 09:00 a 18:00 hrs",
        lugar: "Plaza de Armas, Puerto Montt",
        descripcion: "Gran muestra de productos hechos a mano por emprendedoras locales. Tejidos, panadería artesanal y productos de campo.",
        lat: "-41.4717",
        lng: "-72.9396",
        participantes: ["t_minimarket_rosa", "t_tejidos_austral", "t_reposteria_surena"]
      },
      rio: {
        nombre: "Artesanías del Río – Primavera",
        fecha: "Domingo 8 y lunes 9 de noviembre de 2026 · 09:00 a 18:00 hrs",
        lugar: "Costanera, Puerto Varas",
        descripcion: "Feria de artesanía y diseño frente al lago Llanquihue con demostración de telar en vivo.",
        lat: "-41.3185",
        lng: "-72.9856",
        participantes: ["t_tejidos_austral", "t_reposteria_surena"]
      },
      diseno: {
        nombre: "Mercado de Diseño Independiente",
        fecha: "Sábado 5 y domingo 6 de diciembre de 2026 · 09:00 a 18:00 hrs",
        lugar: "Centro Cultural, Puerto Montt",
        descripcion: "Exposición de diseño, cosmética natural, barbería y gastronomía sureña.",
        lat: "-41.4730",
        lng: "-72.9410",
        participantes: ["t_barberia_alerce", "t_tejidos_austral"]
      }
    }[feriaClave] || {};

    var html = "";
    html += '<h3 style="font-size: 20px; font-weight: 900; margin-bottom: 6px;">' + escaparHtml(info.nombre) + '</h3>';
    html += '<p style="font-size: 13.5px; color: var(--rojo-alerce); font-weight: 700;">' + escaparHtml(info.fecha) + '</p>';
    html += '<p style="font-size: 13px; color: var(--gris-texto); margin-top: 4px;">📍 ' + escaparHtml(info.lugar) + '</p>';
    html += '<p style="font-size: 13px; margin: 12px 0; line-height: 1.5;">' + escaparHtml(info.descripcion) + '</p>';

    // Participantes de la feria
    html += '<hr class="divisor-sutil">';
    html += '<strong style="font-size: 13px; color: var(--negro-apple);">Emprendimientos confirmados que participan:</strong>';
    html += '<div style="display: flex; gap: 8px; flex-wrap: wrap; margin-top: 8px;">';
    info.participantes.forEach(function (tId) {
      var t = TIENDAS.find(function (item) { return item.id === tId; });
      if (t) {
        html += '<button type="button" class="btn-accion-pill" style="height: 36px; padding: 0 14px; font-size: 12px;" onclick="TramaWeb.cerrarModal(\'modal-feria\'); TramaWeb.abrirVitrinaTienda(\'' + t.id + '\');">';
        html += '  🏪 ' + escaparHtml(t.nombre);
        html += '</button>';
      }
    });
    html += '</div>';

    // Botón cómo llegar
    html += '<div style="margin-top: 20px;">';
    html += '  <a href="https://maps.google.com/?q=' + info.lat + ',' + info.lng + '" target="_blank" rel="noopener" class="btn-accion-pill btn-negro" style="height: 44px; text-align: center;">';
    html += '    Ver mapa y cómo llegar';
    html += '  </a>';
    html += '</div>';

    modalCuerpo.innerHTML = html;
    abrirModal("modal-feria");
  }

  function renderizarAsociaciones() {
    var contenedor = document.getElementById("grilla-asociaciones-container");
    if (!contenedor) return;

    var html = "";
    ASOCIACIONES.forEach(function (aso) {
      html += '<article class="tarjeta-tienda" style="padding: 20px;">';
      html += '  <span class="badge-necesidad badge-pedido" style="align-self: flex-start; margin-bottom: 8px;">Asociación Comunal</span>';
      html += '  <h3 style="font-size: 17px; font-weight: 800; color: var(--negro-apple);">' + escaparHtml(aso.nombre) + '</h3>';
      html += '  <p style="font-size: 12px; color: var(--gris-texto); margin-bottom: 10px;">Sector ' + escaparHtml(aso.sector) + ' · ' + aso.integrantes + ' socias activas</p>';
      html += '  <p style="font-size: 12.5px; color: var(--gris-texto); line-height: 1.45; margin-bottom: 16px;">' + escaparHtml(aso.descripcion) + '</p>';
      html += '  <a href="https://wa.me/' + aso.whatsapp.replace(/[^0-9]/g, "") + '?text=' + encodeURIComponent("Hola! Quisiera información sobre la asociación " + aso.nombre) + '" target="_blank" rel="noopener" class="btn-accion-pill btn-negro" style="height: 38px;">';
      html += '    Contactar Asociación';
      html += '  </a>';
      html += '</article>';
    });

    contenedor.innerHTML = html;
  }

  // ============================================================
  // GESTIÓN DE VENTANAS MODALES (ABRIR / CERRAR)
  // ============================================================

  var focoAntesDeModal = [];

  function elementosEnfocables(contenedor) {
    return Array.prototype.filter.call(
      contenedor.querySelectorAll('a[href], button:not([disabled]), input:not([disabled]):not([type="hidden"]), select:not([disabled]), textarea:not([disabled]), [tabindex]:not([tabindex="-1"])'),
      function (el) { return el.offsetParent !== null || el === document.activeElement; });
  }

  function abrirModal(modalId) {
    var m = document.getElementById(modalId);
    if (!m) return;
    if (!m.classList.contains("abierto")) focoAntesDeModal.push(document.activeElement);
    m.classList.add("abierto");
    m.style.display = "flex";
    document.body.style.overflow = "hidden";
    function enfocar() {
      if (!m.classList.contains("abierto") || m.contains(document.activeElement)) return;
      var panel = m.querySelector(".modal-panel-editorial") || m;
      var objetivo = panel.querySelector("[autofocus]") || elementosEnfocables(panel)[0];
      if (objetivo) objetivo.focus();
    }
    // Se intenta al abrir y otra vez al terminar la animación (antes puede no aceptar el foco).
    setTimeout(enfocar, 30);
    setTimeout(enfocar, 320);
  }

  function cerrarModal(modalId) {
    var m = document.getElementById(modalId);
    if (!m) return;
    var estabaAbierto = m.classList.contains("abierto");
    m.classList.remove("abierto");
    m.style.display = "none";
    if (!document.querySelector(".modal-overlay.abierto, .modal-fondo.abierto")) document.body.style.overflow = "";
    if (estabaAbierto) {
      var previo = focoAntesDeModal.pop();
      if (previo && typeof previo.focus === "function" && document.contains(previo)) previo.focus();
    }
  }

  function modalSuperior() {
    var abiertos = document.querySelectorAll(".modal-overlay.abierto, .modal-fondo.abierto");
    return abiertos.length ? abiertos[abiertos.length - 1] : null;
  }

  // Tab no sale del modal abierto (foco atrapado mientras está visible).
  document.addEventListener("keydown", function (e) {
    if (e.key !== "Tab") return;
    var m = modalSuperior();
    if (!m) return;
    var lista = elementosEnfocables(m);
    if (!lista.length) return;
    var primero = lista[0], ultimo = lista[lista.length - 1];
    if (e.shiftKey && (document.activeElement === primero || !m.contains(document.activeElement))) { e.preventDefault(); ultimo.focus(); }
    else if (!e.shiftKey && (document.activeElement === ultimo || !m.contains(document.activeElement))) { e.preventDefault(); primero.focus(); }
  });

  // Cerrar al hacer clic en el backdrop
  document.addEventListener("click", function (e) {
    if (e.target && e.target.id && (e.target.classList.contains("modal-overlay") || e.target.classList.contains("modal-fondo"))) {
      cerrarModal(e.target.id);
    }
  });

  // Cerrar con Escape
  document.addEventListener("keydown", function (e) {
    if (e.key === "Escape") {
      var superior = modalSuperior();
      if (superior && superior.id) cerrarModal(superior.id);
    }
  });

  // ============================================================
  // ACCESIBILIDAD Y CAMBIO DE TAMAÑO / CONTRASTE
  // ============================================================

  function configurarAccesibilidad() {
    var btnNormal = document.getElementById("btn-tamano-normal");
    var btnGrande = document.getElementById("btn-tamano-grande");
    var btnMuyGrande = document.getElementById("btn-tamano-muy-grande");
    var btnContraste = document.getElementById("btn-alto-contraste");

    if (btnNormal) {
      btnNormal.addEventListener("click", function () {
        document.body.classList.remove("tamano-grande", "tamano-muy-grande");
        mostrarToast("Tamaño de texto: Normal");
      });
    }

    if (btnGrande) {
      btnGrande.addEventListener("click", function () {
        document.body.classList.add("tamano-grande");
        document.body.classList.remove("tamano-muy-grande");
        mostrarToast("Tamaño de texto: Grande");
      });
    }

    if (btnMuyGrande) {
      btnMuyGrande.addEventListener("click", function () {
        document.body.classList.add("tamano-muy-grande");
        document.body.classList.remove("tamano-grande");
        mostrarToast("Tamaño de texto: Muy Grande");
      });
    }

    if (btnContraste) {
      btnContraste.addEventListener("click", function () {
        document.body.classList.toggle("alto-contraste");
        var activo = document.body.classList.contains("alto-contraste");
        btnContraste.setAttribute("aria-pressed", activo ? "true" : "false");
        btnContraste.style.color = activo ? "#10b981" : "#ffffff";
        mostrarToast(activo ? "Modo Alto Contraste Activado" : "Modo Normal Activado");
      });
    }
  }

  // Helper para escapar HTML y prevenir inyecciones
  function escaparHtml(texto) {
    if (!texto) return "";
    return String(texto)
      .replace(/&/g, "&amp;")
      .replace(/</g, "&lt;")
      .replace(/>/g, "&gt;")
      .replace(/"/g, "&quot;")
      .replace(/'/g, "&#039;");
  }

  // ============================================================
  // MÓDULO 8: ESTUDIO DE FLYERS (BURGER KING / MCDONALDS) Y PUBLICADOR FACEBOOK MARKETPLACE
  // ============================================================

  var FOTOS_SUGERIDAS = [
    { nombre: "Pan Amasado", url: "https://images.unsplash.com/photo-1509440159596-0249088772ff?w=600&q=80", emoji: "🍞" },
    { nombre: "Kuchen Sureño", url: "https://images.unsplash.com/photo-1519869325930-281384150729?w=600&q=80", emoji: "🍰" },
    { nombre: "Tejidos Lana", url: "https://images.unsplash.com/photo-1606760227091-3dd870d97f1d?w=600&q=80", emoji: "🧶" },
    { nombre: "Barbería", url: "https://images.unsplash.com/photo-1503951914875-452162b0f3f1?w=600&q=80", emoji: "💈" },
    { nombre: "Empanadas", url: "https://images.unsplash.com/photo-1628191010210-a59de33e5941?w=600&q=80", emoji: "🥟" }
  ];

  var estadoPublicar = {
    fotoDataUrl: "",
    categoriaSeleccionada: "Abarrotes y Alimentos",
    stockSeleccionado: "inmediato"
  };

  var estadoFlyer = {
    plantilla: "estilo1", // 'estilo1' | 'estilo2' | 'estilo3' | 'estilo4' | 'estilo5'
    fuente: "Britti Sans",
    color: "original",
    fotoImg: null,
    fotoUrlActual: ""
  };

  // Abrir Modal Publicar estilo FB Marketplace
  function nuevoEstadoPublicar() {
    return { productoId: null, previo: null, foto: null, opciones: [], guardando: false };
  }
  var estadoPublicar = nuevoEstadoPublicar();

  function valoresIniciales() {
    var tienda = obtenerTiendaUsuarioActivo();
    var v = (tienda && tienda.valoresProducto) || {};
    return {
      categoria: Cat.CATEGORIAS[v.categoria] ? v.categoria : "TEXTIL",
      modalidad: Cat.MODALIDADES.indexOf(v.modalidad) >= 0 ? v.modalidad : "REGULAR",
      manejaStock: v.manejaStock !== false,
      alAgotarse: Cat.AL_AGOTARSE.indexOf(v.alAgotarse) >= 0 ? v.alAgotarse : "MOSTRAR_AGOTADO",
      publicado: v.publicado === true,
      disponible: v.disponible !== false
    };
  }

  function campo(id) { return document.getElementById(id); }

  function previsualizar(contenedor, url, alt) {
    contenedor.textContent = "";
    var img = imagenSegura(url, alt, "pub-foto-img") || (url && /^blob:/.test(url) ? h("img", { src: url, alt: alt, class: "pub-foto-img" }) : null);
    if (img) contenedor.appendChild(img);
    else contenedor.appendChild(h("span", { class: "pub-foto-vacia", text: "Sin foto" }));
  }

  /** Abre el formulario guiado: sin id crea un producto con los valores de la tienda. */
  function abrirModalPublicarProducto(productoId) {
    if (!esUsuarioEmprendedora()) {
      abrirModalAuthEmprendedora();
      mostrarToast("Inicia sesión como emprendedora para añadir productos.");
      return;
    }
    if (MODO !== "real") {
      mostrarToast("La gestión de productos necesita la conexión con el servidor.");
      return;
    }
    var previo = typeof productoId === "string" ? MIS_PRODUCTOS.find(function (p) { return p.id === productoId; }) : null;
    var base = previo || Object.assign({ id: null, nombre: "", descripcion: "", precio: "", tipo: "", atributos: {}, destacado: false, fotoUrl: "",
      variantes: [{ id: Cat.nuevoId("v_"), nombre: "Única", stock: 1, precio: null, fotoUrl: "" }] }, valoresIniciales());
    estadoPublicar = nuevoEstadoPublicar();
    estadoPublicar.productoId = previo ? previo.id : Cat.nuevoId("p_");
    estadoPublicar.previo = previo;
    estadoPublicar.foto = { url: base.fotoUrl || "", blob: null };
    estadoPublicar.opciones = base.variantes.map(function (v) {
      return { id: v.id, nombre: v.nombre, precio: v.precio === null ? "" : v.precio, stock: v.stock, foto: { url: v.fotoUrl || "", blob: null } };
    });

    campo("modal-publicar-titulo").textContent = previo ? "Editar producto" : "Añadir producto";
    campo("pub-nombre").value = base.nombre;
    campo("pub-precio").value = base.precio === "" ? "" : String(base.precio);
    campo("pub-descripcion").value = base.descripcion;
    campo("pub-categoria").value = Cat.CATEGORIAS[base.categoria] ? base.categoria : "TEXTIL";
    var radio = document.querySelector('input[name="pub-modalidad"][value="' + base.modalidad + '"]');
    if (radio) radio.checked = true;
    campo("pub-maneja-stock").checked = base.manejaStock !== false;
    campo("pub-al-agotarse").value = base.alAgotarse;
    campo("pub-disponible").checked = base.disponible !== false;
    campo("pub-publicado").checked = base.publicado === true;
    campo("pub-destacado").checked = base.destacado === true;
    campo("pub-foto-input").value = "";
    previsualizar(campo("pub-foto-preview"), estadoPublicar.foto.url, "Foto del producto");
    pintarTiposPublicar(base.tipo, base.atributos);
    pintarOpcionesPublicar();
    actualizarReglasPublicar();
    mostrarAviso("pub-aviso", "");
    abrirModal("modal-publicar-producto");
  }

  function pintarTiposPublicar(tipoSeleccionado, atributos) {
    var select = campo("pub-tipo");
    var categoria = campo("pub-categoria").value;
    var tipos = Object.keys(TIPOS).map(function (k) { return TIPOS[k]; }).filter(function (t) { return t.categoria === categoria && t.activo !== false; });
    select.textContent = "";
    if (!tipos.length) {
      select.appendChild(h("option", { value: "", text: "Sin tipos configurados para esta categoría" }));
      select.disabled = true;
    } else {
      select.disabled = false;
      select.appendChild(h("option", { value: "", text: "Sin tipo específico" }));
      tipos.forEach(function (t) { select.appendChild(h("option", { value: t.id, text: t.nombre })); });
      select.value = tipos.some(function (t) { return t.id === tipoSeleccionado; }) ? tipoSeleccionado : "";
    }
    pintarCamposEspecificos(atributos || {});
  }

  /** Campos específicos definidos por el tipo (tabla tipos_producto). */
  function pintarCamposEspecificos(atributos) {
    var cont = campo("pub-campos");
    cont.textContent = "";
    var tipo = TIPOS[campo("pub-tipo").value];
    if (!tipo || !Array.isArray(tipo.campos) || !tipo.campos.length) { cont.style.display = "none"; return; }
    cont.style.display = "";
    tipo.campos.forEach(function (c, i) {
      var id = "pub-campo-" + i;
      var control;
      if (c.tipo === "opciones" && Array.isArray(c.opciones)) {
        control = h("select", { id: id, class: "input-fb-market", "data-clave": c.clave }, [h("option", { value: "", text: "Elegir…" })].concat(
          c.opciones.map(function (o) { return h("option", { value: o, text: o }); })));
      } else {
        control = h("input", { id: id, class: "input-fb-market", "data-clave": c.clave, type: c.tipo === "numero" ? "number" : "text", required: c.obligatorio === true });
      }
      control.value = atributos[c.clave] !== undefined ? String(atributos[c.clave]) : "";
      cont.appendChild(h("div", null, [h("label", { for: id, class: "label-fb-market", text: (c.etiqueta || c.clave) + (c.obligatorio ? " *" : "") }), control]));
    });
  }

  function pintarOpcionesPublicar() {
    var cont = campo("pub-opciones");
    cont.textContent = "";
    var manejaStock = campo("pub-maneja-stock").checked;
    estadoPublicar.opciones.forEach(function (o, i) {
      var pre = "pub-op-" + i;
      var vista = h("div", { class: "pub-op-foto" });
      previsualizar(vista, o.foto.blob ? o.foto.objectUrl : o.foto.url, "Foto de " + (o.nombre || "la opción"));
      var entradaFoto = h("input", { id: pre + "-foto", type: "file", accept: "image/*", class: "sr-only" });
      entradaFoto.addEventListener("change", function () {
        var archivo = entradaFoto.files && entradaFoto.files[0];
        if (!archivo) return;
        if (o.foto.objectUrl) URL.revokeObjectURL(o.foto.objectUrl);
        o.foto = { url: o.foto.url, blob: archivo, objectUrl: URL.createObjectURL(archivo) };
        pintarOpcionesPublicar();
      });
      cont.appendChild(h("fieldset", { class: "pub-opcion" }, [
        h("legend", { text: "Opción " + (i + 1) }),
        h("div", { class: "pub-opcion-grid" }, [
          h("div", null, [h("label", { for: pre + "-nombre", class: "label-fb-market", text: "Nombre (talla, color, sabor…)" }),
            h("input", { id: pre + "-nombre", class: "input-fb-market", value: o.nombre, maxlength: "60", oninput: function (e) { o.nombre = e.target.value; } })]),
          h("div", null, [h("label", { for: pre + "-precio", class: "label-fb-market", text: "Precio propio (opcional)" }),
            h("input", { id: pre + "-precio", class: "input-fb-market", type: "number", min: "0", step: "1", inputmode: "numeric", value: o.precio === "" ? "" : String(o.precio), placeholder: "Igual al del producto", oninput: function (e) { o.precio = e.target.value; } })]),
          h("div", null, [h("label", { for: pre + "-stock", class: "label-fb-market", text: manejaStock ? "¿Cuántos tienes?" : "Sin conteo de unidades" }),
            h("input", { id: pre + "-stock", class: "input-fb-market", type: "number", min: "0", step: "1", inputmode: "numeric", value: String(o.stock), disabled: !manejaStock, oninput: function (e) { o.stock = e.target.value; } })])
        ]),
        h("div", { class: "pub-opcion-pie" }, [
          vista,
          h("label", { for: pre + "-foto", class: "btn-accion-pill", text: "Foto de esta opción" }),
          entradaFoto,
          estadoPublicar.opciones.length > 1 ? h("button", { type: "button", class: "btn-accion-pill btn-quitar", text: "Quitar opción", onclick: function () {
            estadoPublicar.opciones.splice(i, 1);
            pintarOpcionesPublicar();
          } }) : null
        ])
      ]));
    });
  }

  function agregarOpcionPublicar() {
    estadoPublicar.opciones.push({ id: Cat.nuevoId("v_"), nombre: "", precio: "", stock: 0, foto: { url: "", blob: null } });
    pintarOpcionesPublicar();
    var ultimo = campo("pub-op-" + (estadoPublicar.opciones.length - 1) + "-nombre");
    if (ultimo) ultimo.focus();
  }

  function modalidadElegida() {
    var r = document.querySelector('input[name="pub-modalidad"]:checked');
    return r ? r.value : "REGULAR";
  }

  /** La pieza única siempre cuenta unidades y se oculta al venderse; a pedido no se agota. */
  function actualizarReglasPublicar() {
    var modalidad = modalidadElegida();
    var stock = campo("pub-maneja-stock");
    var agotar = campo("pub-al-agotarse");
    if (modalidad === "PIEZA_UNICA") { stock.checked = true; stock.disabled = true; agotar.value = "OCULTAR"; agotar.disabled = true; }
    else { stock.disabled = false; agotar.disabled = !stock.checked || modalidad === "A_PEDIDO"; }
    var ayuda = campo("pub-ayuda-modalidad");
    if (ayuda) {
      ayuda.textContent = {
        REGULAR: "Se vende de inmediato mientras queden unidades.",
        A_PEDIDO: "Lo haces por encargo: no se agota y la venta queda anotada como encargo.",
        PIEZA_UNICA: "Hay una sola unidad en total, aunque tenga varias opciones. Al venderse desaparece para siempre."
      }[modalidad];
    }
    pintarOpcionesPublicar();
  }

  function leerAtributos() {
    var salida = {};
    document.querySelectorAll("#pub-campos [data-clave]").forEach(function (el) {
      if (el.value !== "") salida[el.getAttribute("data-clave")] = el.type === "number" ? Number(el.value) : el.value;
    });
    return salida;
  }

  function blobDesdeDataUrl(dataUrl) {
    return fetch(dataUrl).then(function (r) { return r.blob(); });
  }

  /** Sube una imagen nueva (si la hay) y devuelve { url, ruta } o la URL anterior. */
  function subirFotoSiCambio(foto, nombreArchivo, subidas) {
    if (!foto || (!foto.blob && !foto.dataUrl)) return Promise.resolve(foto ? foto.url : "");
    var origen = foto.blob ? Promise.resolve(foto.blob) : blobDesdeDataUrl(foto.dataUrl);
    var ruta = estado.usuario.id + "/" + nombreArchivo + "_" + Date.now() + ".jpg";
    return origen.then(comprimirImagen).then(function (jpeg) {
      return servicio.subirImagen(BUCKET_FOTOS, ruta, jpeg);
    }).then(function (r) {
      if (!r.ok) throw r.error;
      subidas.push(ruta);
      return r.datos;
    });
  }

  function confirmarPublicacionFB() {
    if (!esUsuarioEmprendedora() || !servicio) { mostrarAviso("pub-aviso", "Inicia sesión como emprendedora para guardar."); return; }
    if (estadoPublicar.guardando) return;
    var nombre = campo("pub-nombre").value.trim();
    var precio = campo("pub-precio").value.trim();
    var modalidad = modalidadElegida();
    var manejaStock = modalidad === "PIEZA_UNICA" || campo("pub-maneja-stock").checked;
    if (!nombre) { mostrarAviso("pub-aviso", "Escribe el nombre del producto."); campo("pub-nombre").focus(); return; }
    if (!/^\d+$/.test(precio)) { mostrarAviso("pub-aviso", "Escribe el precio en pesos, sin puntos (ej: 2500)."); campo("pub-precio").focus(); return; }
    if (!estadoPublicar.opciones.length) { mostrarAviso("pub-aviso", "Agrega al menos una opción."); return; }
    for (var i = 0; i < estadoPublicar.opciones.length; i++) {
      var o = estadoPublicar.opciones[i];
      if (!String(o.nombre || "").trim()) { mostrarAviso("pub-aviso", "Escribe el nombre de la opción " + (i + 1) + "."); campo("pub-op-" + i + "-nombre").focus(); return; }
      if (manejaStock && !/^\d+$/.test(String(o.stock))) { mostrarAviso("pub-aviso", "La cantidad de la opción " + (i + 1) + " debe ser un número entero desde 0."); campo("pub-op-" + i + "-stock").focus(); return; }
      if (String(o.precio) !== "" && !/^\d+$/.test(String(o.precio))) { mostrarAviso("pub-aviso", "El precio de la opción " + (i + 1) + " debe ser un número entero."); return; }
    }
    if (modalidad === "PIEZA_UNICA") {
      var total = estadoPublicar.opciones.reduce(function (s, x) { return s + (Number(x.stock) || 0); }, 0);
      if (total > 1) { mostrarAviso("pub-aviso", "Una pieza única tiene una sola unidad en total (ahora suma " + total + ")."); return; }
    }
    var obligatorios = Array.prototype.filter.call(document.querySelectorAll("#pub-campos [required]"), function (el) { return !el.value; });
    if (obligatorios.length) { mostrarAviso("pub-aviso", "Completa los campos obligatorios del tipo de producto."); obligatorios[0].focus(); return; }

    var boton = campo("pub-guardar");
    var subidas = [];
    estadoPublicar.guardando = true;
    boton.disabled = true;
    boton.textContent = "Guardando en el servidor…";
    mostrarAviso("pub-aviso", "");
    var id = estadoPublicar.productoId;

    subirFotoSiCambio(estadoPublicar.foto, id, subidas).then(function (urlFoto) {
      return Promise.all(estadoPublicar.opciones.map(function (op) {
        return subirFotoSiCambio(op.foto, id + "_" + op.id, subidas).then(function (url) { return url; });
      })).then(function (urlsOpciones) {
        var cuerpo = Cat.productoParaGuardar({
          id: id, nombre: nombre, descripcion: campo("pub-descripcion").value, categoria: campo("pub-categoria").value,
          tipo: campo("pub-tipo").value || null, atributos: leerAtributos(), precio: Number(precio),
          publicado: campo("pub-publicado").checked, disponible: campo("pub-disponible").checked, destacado: campo("pub-destacado").checked,
          modalidad: modalidad, manejaStock: manejaStock, alAgotarse: campo("pub-al-agotarse").value, fotoUrl: urlFoto,
          variantes: estadoPublicar.opciones.map(function (op, j) {
            return { id: op.id, nombre: op.nombre, precio: String(op.precio) === "" ? null : Number(op.precio), stock: manejaStock ? Number(op.stock) : 0, fotoUrl: urlsOpciones[j] };
          })
        }, estadoPublicar.previo);
        return servicio.guardarProducto(cuerpo);
      });
    }).then(function (r) {
      if (!r.ok) throw r.error;
      var guardado = Cat.normalizarProducto(r.datos);
      var idx = MIS_PRODUCTOS.findIndex(function (p) { return p.id === guardado.id; });
      if (idx >= 0) MIS_PRODUCTOS[idx] = guardado; else MIS_PRODUCTOS.unshift(guardado);
      cerrarModal("modal-publicar-producto");
      mostrarToast("Guardado en el servidor: «" + guardado.nombre + "»" + (Cat.estadoPublico(guardado) === "OCULTO" ? " (por ahora no se muestra al público)" : ""));
      estadoPublicar = nuevoEstadoPublicar();
      renderizarGestionCatalogo();
      cargarCatalogoPublico();
    }).catch(function (e) {
      // Las imágenes subidas en este intento no quedaron en ningún producto: se borran.
      subidas.forEach(function (ruta) { servicio.borrarImagen(BUCKET_FOTOS, ruta); });
      mostrarAviso("pub-aviso", "No se guardó: " + Cat.mensajeError(e));
    }).then(function () {
      estadoPublicar.guardando = false;
      boton.disabled = false;
      boton.textContent = "Guardar producto";
    });
  }

  // Renderizar galería de fotos sugeridas en modal FB
  // Las fotos de muestra solo se usan en el diseñador de afiches, no como foto real del producto.
  function renderizarGaleriaSugeridaFB() {}

  // Seleccionar foto sugerida en FB
  function seleccionarFotoSugeridaFB(url) {
    seleccionarFotoSugeridaFlyer(url);
  }

  // Al subir foto local (teléfono / PC)
  function onFotoProductoSubida(event) {
    var archivo = event.target.files && event.target.files[0];
    if (!archivo) return;
    if (!/^image\//.test(archivo.type)) { mostrarAviso("pub-aviso", "Elige un archivo de imagen."); return; }
    if (estadoPublicar.foto && estadoPublicar.foto.objectUrl) URL.revokeObjectURL(estadoPublicar.foto.objectUrl);
    var objectUrl = URL.createObjectURL(archivo);
    estadoPublicar.foto = { url: (estadoPublicar.foto && estadoPublicar.foto.url) || "", blob: archivo, objectUrl: objectUrl };
    previsualizar(campo("pub-foto-preview"), objectUrl, "Foto del producto");
    cargarFotoEnFlyer(objectUrl);
  }

  function quitarFotoProducto() {
    if (estadoPublicar.foto && estadoPublicar.foto.objectUrl) URL.revokeObjectURL(estadoPublicar.foto.objectUrl);
    estadoPublicar.foto = { url: "", blob: null };
    var inp = campo("pub-foto-input");
    if (inp) inp.value = "";
    previsualizar(campo("pub-foto-preview"), "", "Foto del producto");
  }

  function seleccionarCategoriaFB() {
    pintarTiposPublicar(campo("pub-tipo").value, leerAtributos());
  }

  function seleccionarStockFB() {
    actualizarReglasPublicar();
  }

  // Confirmar publicación estilo Facebook Marketplace
  // Pasar del modal de publicar al estudio de flyers
  function abrirEstudioFlyerDesdePublicar() {
    var inpTitulo = document.getElementById("fb-inp-titulo");
    var inpPrecio = document.getElementById("fb-inp-precio");

    if (inpTitulo && inpTitulo.value.trim()) {
      var flyerTit = document.getElementById("flyer-inp-titulo");
      if (flyerTit) flyerTit.value = inpTitulo.value.trim();
    }
    if (inpPrecio && inpPrecio.value.trim()) {
      var flyerPre = document.getElementById("flyer-inp-precio");
      if (flyerPre) flyerPre.value = inpPrecio.value.trim();
    }

    cerrarModal("modal-publicar-producto");
    abrirModalEstudioFlyer();
  }

  // Abrir Modal Estudio de Flyers
  function abrirModalEstudioFlyer() {
    if (!esUsuarioEmprendedora()) {
      abrirModalAuthEmprendedora();
      mostrarToast("🔒 Debes iniciar sesión como Emprendedor/a para diseñar afiches comerciales.");
      return;
    }
    renderizarGaleriaFotosFlyer();
    abrirModal("modal-estudio-flyer");
    setTimeout(function () {
      actualizarLienzoFlyer();
    }, 100);
  }

  function renderizarGaleriaFotosFlyer() {
    var cont = document.getElementById("flyer-galeria-fotos");
    if (!cont) return;
    cont.innerHTML = FOTOS_SUGERIDAS.map(function (f) {
      return '<button type="button" class="fb-chip-cat" style="font-size: 11px; padding: 4px 8px;" onclick="TramaWeb.seleccionarFotoSugeridaFlyer(\'' + f.url + '\')">' +
        f.emoji + ' ' + f.nombre +
      '</button>';
    }).join("");
  }

  function seleccionarFotoSugeridaFlyer(url) {
    cargarFotoEnFlyer(url, function () {
      actualizarLienzoFlyer();
    });
    mostrarToast("Foto de muestra cargada en el flyer");
  }

  function onFotoFlyerSubida(event) {
    var file = event.target.files && event.target.files[0];
    if (!file) return;
    var reader = new FileReader();
    reader.onload = function (e) {
      var dataUrl = e.target.result;
      cargarFotoEnFlyer(dataUrl, function () {
        actualizarLienzoFlyer();
      });
      mostrarToast("Foto personalizada cargada");
    };
    reader.readAsDataURL(file);
  }

  function cargarFotoEnFlyer(src, callback) {
    estadoFlyer.fotoUrlActual = src;
    var img = new Image();
    img.crossOrigin = "anonymous";
    img.onload = function () {
      estadoFlyer.fotoImg = img;
      if (callback) callback();
      actualizarLienzoFlyer();
    };
    img.onerror = function () {
      estadoFlyer.fotoImg = null;
      if (callback) callback();
      actualizarLienzoFlyer();
    };
    img.src = src;
  }

  function cambiarPlantillaFlyer(plantillaId) {
    estadoFlyer.plantilla = plantillaId;
    var btns = document.querySelectorAll(".btn-plantilla-selector");
    btns.forEach(function (b) {
      if (b.getAttribute("data-plantilla") === plantillaId) {
        b.classList.add("activa");
      } else {
        b.classList.remove("activa");
      }
    });

    if (!estadoFlyer.fuente || estadoFlyer.fuente === "Impact" || estadoFlyer.fuente === "Phonk") {
      cambiarFuenteFlyer("Britti Sans");
    }

    actualizarLienzoFlyer();
  }

  function cambiarFuenteFlyer(fuenteNombre) {
    estadoFlyer.fuente = fuenteNombre;
    var btns = document.querySelectorAll("#flyer-selector-fuente .fb-chip-cat");
    btns.forEach(function (b) {
      if (b.getAttribute("data-fuente") === fuenteNombre) {
        b.classList.add("activo");
      } else {
        b.classList.remove("activo");
      }
    });
    if (document.fonts && document.fonts.load) {
      document.fonts.load("900 48px '" + fuenteNombre + "'").then(function () {
        actualizarLienzoFlyer();
      }).catch(function () {
        actualizarLienzoFlyer();
      });
    } else {
      actualizarLienzoFlyer();
    }
  }

  function cambiarColorFlyer(colorHex) {
    estadoFlyer.color = colorHex;
    var btns = document.querySelectorAll("#flyer-selector-color .btn-color-chip");
    btns.forEach(function (b) {
      if (b.getAttribute("data-color") === colorHex) {
        b.classList.add("activo");
      } else {
        b.classList.remove("activo");
      }
    });
    var colorPicker = document.getElementById("flyer-color-custom");
    if (colorPicker && colorHex !== "original") {
      colorPicker.value = colorHex;
    }
    actualizarLienzoFlyer();
  }

  function hexToRgb(hex) {
    if (!hex || hex === "original") return null;
    var c = hex.replace("#", "");
    if (c.length === 3) c = c[0] + c[0] + c[1] + c[1] + c[2] + c[2];
    var num = parseInt(c, 16);
    if (isNaN(num)) return null;
    return {
      r: (num >> 16) & 255,
      g: (num >> 8) & 255,
      b: num & 255
    };
  }

  function oscurecerColor(hex, factor) {
    var rgb = hexToRgb(hex);
    if (!rgb) return hex;
    var r = Math.max(0, Math.floor(rgb.r * (1 - factor)));
    var g = Math.max(0, Math.floor(rgb.g * (1 - factor)));
    var b = Math.max(0, Math.floor(rgb.b * (1 - factor)));
    return "rgb(" + r + "," + g + "," + b + ")";
  }

  function aclararColor(hex, factor) {
    var rgb = hexToRgb(hex);
    if (!rgb) return hex;
    var r = Math.min(255, Math.floor(rgb.r + (255 - rgb.r) * factor));
    var g = Math.min(255, Math.floor(rgb.g + (255 - rgb.g) * factor));
    var b = Math.min(255, Math.floor(rgb.b + (255 - rgb.b) * factor));
    return "rgb(" + r + "," + g + "," + b + ")";
  }

  // ============================================================
  // MOTOR DE DIBUJO CANVAS 700x700 (ESTILOS 1 A 5 - LETRAS DETRÁS, PRODUCTO AL FRENTE)
  // ============================================================

  function actualizarLienzoFlyer() {
    var canvas = document.getElementById("canvas-flyer");
    if (!canvas) return;
    var ctx = canvas.getContext("2d");
    if (!ctx) return;

    var w = 700;
    var h = 700;
    canvas.width = w;
    canvas.height = h;

    var inpTitulo = document.getElementById("flyer-inp-titulo");
    var inpPrecio = document.getElementById("flyer-inp-precio");
    var inpSello = document.getElementById("flyer-inp-sello");
    var inpMensaje = document.getElementById("flyer-inp-mensaje");
    var inpSector = document.getElementById("flyer-inp-sector");

    var datos = {
      titulo: (inpTitulo ? inpTitulo.value.trim() : "") || "Producto",
      precio: (inpPrecio ? inpPrecio.value.trim() : "") || "2500",
      sello: (inpSello ? inpSello.value.trim() : "") || "EDICIÓN LIMITADA",
      mensaje: (inpMensaje ? inpMensaje.value.trim() : "") || "+56 9 9123 4567",
      sector: (inpSector ? inpSector.value.trim() : "") || "Alerce · Puerto Montt",
      fuente: estadoFlyer.fuente || "Britti Sans",
      color: estadoFlyer.color || "original",
      img: estadoFlyer.fotoImg
    };

    ctx.save();

    switch (estadoFlyer.plantilla) {
      case "estilo1":
      case "burgerking":
        dibujarPlantillaEstilo1(ctx, datos, w, h);
        break;
      case "estilo2":
      case "mcdonalds":
        dibujarPlantillaEstilo2(ctx, datos, w, h);
        break;
      case "estilo3":
      case "whatsapp":
        dibujarPlantillaEstilo3(ctx, datos, w, h);
        break;
      case "estilo4":
      case "editorial":
        dibujarPlantillaEstilo4(ctx, datos, w, h);
        break;
      case "estilo5":
      case "sureno":
        dibujarPlantillaEstilo5(ctx, datos, w, h);
        break;
      default:
        dibujarPlantillaEstilo1(ctx, datos, w, h);
    }

    ctx.restore();
  }

  // ESTILO 1: SPLIT URBANO (Fondo dividido, letras verticales detrás, producto al frente)
  function dibujarPlantillaEstilo1(ctx, d, w, h) {
    var c1 = "#2F3136";
    var c2 = "#0C0D0E";
    if (d.color && d.color !== "original") {
      c1 = d.color;
      c2 = oscurecerColor(d.color, 0.45);
    }
    // Fondo dividido
    ctx.fillStyle = c1;
    ctx.fillRect(0, 0, w * 0.44, h);
    ctx.fillStyle = c2;
    ctx.fillRect(w * 0.44, 0, w * 0.56, h);

    // CAPA DETRÁS: Letras gigantes verticales en lado derecho
    ctx.save();
    ctx.font = "900 64px '" + d.fuente + "', sans-serif";
    ctx.fillStyle = "rgba(255, 255, 255, 0.18)";
    ctx.textAlign = "center";
    var letras = ["E", "D", "I", "T", "I", "O", "N"];
    for (var i = 0; i < letras.length; i++) {
      ctx.fillText(letras[i], w * 0.86, 120 + i * 78);
    }
    ctx.restore();

    // CAPA AL FRENTE: Producto en centro inclinado sobre la división
    var px = 390;
    var py = 350;
    ctx.save();
    ctx.translate(px, py);
    ctx.rotate(-0.24);

    // Sombra del producto
    ctx.shadowColor = "rgba(0, 0, 0, 0.65)";
    ctx.shadowBlur = 35;
    ctx.shadowOffsetX = -12;
    ctx.shadowOffsetY = 18;

    var pw = 300;
    var ph = 300;

    if (d.img) {
      ctx.beginPath();
      if (ctx.roundRect) ctx.roundRect(-pw / 2, -ph / 2, pw, ph, 24);
      else ctx.rect(-pw / 2, -ph / 2, pw, ph);
      ctx.clip();
      ctx.drawImage(d.img, -pw / 2, -ph / 2, pw, ph);
    } else {
      ctx.fillStyle = "#FFFFFF";
      ctx.beginPath();
      if (ctx.roundRect) ctx.roundRect(-pw / 2, -ph / 2, pw, ph, 28);
      else ctx.rect(-pw / 2, -ph / 2, pw, ph);
      ctx.fill();
      ctx.font = "130px sans-serif";
      ctx.textAlign = "center";
      ctx.fillText("👟", 0, 45);
    }
    ctx.restore();

    // CAPA SUPERIOR: Textos frontales
    ctx.save();
    // Título Marca / Producto en itálica impact/phonk
    ctx.font = "italic 900 46px '" + d.fuente + "', sans-serif";
    ctx.fillStyle = "#FFFFFF";
    ctx.textAlign = "left";
    ctx.fillText(d.titulo.toUpperCase(), 36, 110, w * 0.42);

    // Subtítulo distintivo
    ctx.font = "italic 600 19px '" + d.fuente + "', sans-serif";
    ctx.fillStyle = "#B0B3B8";
    ctx.fillText(d.sello, 36, 150, w * 0.42);

    // Caja de precio estilo poster urbano
    ctx.fillStyle = "#EAEAEA";
    if (ctx.roundRect) ctx.roundRect(36, h - 140, 190, 68, 6);
    else ctx.fillRect(36, h - 140, 190, 68);
    ctx.fill();

    ctx.fillStyle = "#000000";
    ctx.font = "900 38px '" + d.fuente + "', sans-serif";
    ctx.textAlign = "center";
    ctx.fillText("$" + Number(d.precio.replace(/\D/g, "") || 0).toLocaleString("es-CL"), 36 + 95, h - 92);

    ctx.font = "24px sans-serif";
    ctx.fillText("🔥", 255, h - 94);

    ctx.restore();
  }

  // ESTILO 2: IMPACTO TIPOGRÁFICO (Fondo ámbar, letras huecas gigantes detrás, producto al frente)
  function dibujarPlantillaEstilo2(ctx, d, w, h) {
    // Fondo degradado
    var grad = ctx.createRadialGradient(w / 2, h / 2, 60, w / 2, h / 2, 450);
    if (d.color && d.color !== "original") {
      grad.addColorStop(0, aclararColor(d.color, 0.2));
      grad.addColorStop(1, oscurecerColor(d.color, 0.35));
    } else {
      grad.addColorStop(0, "#F5A623");
      grad.addColorStop(1, "#CE7900");
    }
    ctx.fillStyle = grad;
    ctx.fillRect(0, 0, w, h);

    // CAPA DETRÁS: Enormes letras outline blancas detrás
    ctx.save();
    ctx.strokeStyle = "rgba(255, 255, 255, 0.45)";
    ctx.lineWidth = 6;
    ctx.font = "900 170px '" + d.fuente + "', sans-serif";
    ctx.textAlign = "center";
    ctx.letterSpacing = "6px";
    ctx.strokeText("BUR", w / 2, 230);
    ctx.strokeText("GER", w / 2, 570);
    ctx.restore();

    // CAPA AL FRENTE: Producto en el centro sobre las letras gigantes
    var cx = w / 2;
    var cy = h / 2;
    var rad = 185;

    ctx.save();
    ctx.shadowColor = "rgba(0, 0, 0, 0.5)";
    ctx.shadowBlur = 38;
    ctx.shadowOffsetY = 16;

    if (d.img) {
      ctx.beginPath();
      ctx.arc(cx, cy, rad, 0, Math.PI * 2);
      ctx.clip();
      ctx.drawImage(d.img, cx - rad, cy - rad, rad * 2, rad * 2);
    } else {
      ctx.fillStyle = "#FFF7E6";
      ctx.beginPath();
      ctx.arc(cx, cy, rad - 10, 0, Math.PI * 2);
      ctx.fill();
      ctx.font = "140px sans-serif";
      ctx.textAlign = "center";
      ctx.fillText("🍔", cx, cy + 50);
    }
    ctx.restore();

    // CAPA SUPERIOR: Badge superior derecho
    ctx.save();
    var badgeBg = (d.color && d.color !== "original") ? oscurecerColor(d.color, 0.55) : "#3D1C06";
    ctx.fillStyle = badgeBg;
    if (ctx.roundRect) ctx.roundRect(w - 250, 36, 214, 46, 23);
    else ctx.fillRect(w - 250, 36, 214, 46);
    ctx.fill();

    ctx.fillStyle = (d.color && d.color !== "original") ? "#FFFFFF" : "#FFAE00";
    ctx.font = "900 14px '" + d.fuente + "', sans-serif";
    ctx.textAlign = "center";
    ctx.fillText(d.sello.toUpperCase(), w - 143, 65);

    // Badge de precio inferior izquierdo estilo banner chocolate
    ctx.fillStyle = badgeBg;
    if (ctx.roundRect) ctx.roundRect(36, h - 165, 250, 105, 20);
    else ctx.fillRect(36, h - 165, 250, 105);
    ctx.fill();

    ctx.fillStyle = (d.color && d.color !== "original") ? "#E0E0E0" : "#FFC555";
    ctx.font = "800 14px '" + d.fuente + "', sans-serif";
    ctx.textAlign = "left";
    ctx.fillText(d.titulo.toUpperCase(), 56, h - 130, 210);

    ctx.fillStyle = "#FFFFFF";
    ctx.font = "900 48px '" + d.fuente + "', sans-serif";
    ctx.fillText("$" + Number(d.precio.replace(/\D/g, "") || 0).toLocaleString("es-CL"), 56, h - 82);
    ctx.restore();
  }

  // ESTILO 3: SPLASH POP (Azul eléctrico, explosión amarilla detrás, producto al frente)
  function dibujarPlantillaEstilo3(ctx, d, w, h) {
    // Fondo
    var bg = (d.color && d.color !== "original") ? d.color : "#0055D4";
    ctx.fillStyle = bg;
    ctx.fillRect(0, 0, w, h);

    // CAPA DETRÁS: Explosión / Sol amarillo
    ctx.save();
    ctx.fillStyle = (d.color && d.color === "#F59E0B") ? "#FFFFFF" : "#FFB800";
    ctx.beginPath();
    var cx = w / 2;
    var cy = h / 2 + 15;
    for (var i = 0; i < 16; i++) {
      var r = (i % 2 === 0) ? 230 : 180;
      var ang = (i * Math.PI) / 8;
      var sx = cx + Math.cos(ang) * r;
      var sy = cy + Math.sin(ang) * r;
      if (i === 0) ctx.moveTo(sx, sy);
      else ctx.lineTo(sx, sy);
    }
    ctx.closePath();
    ctx.fill();
    ctx.restore();

    // CAPA AL FRENTE: Producto en el centro
    ctx.save();
    ctx.shadowColor = "rgba(0, 0, 0, 0.45)";
    ctx.shadowBlur = 30;
    ctx.shadowOffsetY = 12;

    var rad = 165;
    if (d.img) {
      ctx.beginPath();
      ctx.arc(cx, cy, rad, 0, Math.PI * 2);
      ctx.clip();
      ctx.drawImage(d.img, cx - rad, cy - rad, rad * 2, rad * 2);
    } else {
      ctx.fillStyle = "#FFFFFF";
      ctx.beginPath();
      ctx.arc(cx, cy, rad - 10, 0, Math.PI * 2);
      ctx.fill();
      ctx.font = "130px sans-serif";
      ctx.textAlign = "center";
      ctx.fillText("🥐", cx, cy + 45);
    }
    ctx.restore();

    // Título superior estilo retro
    ctx.save();
    ctx.font = "900 22px '" + d.fuente + "', sans-serif";
    ctx.fillStyle = "#FFFFFF";
    ctx.textAlign = "center";
    ctx.fillText("SUPER PROMO", w / 2, 55);

    ctx.font = "900 48px '" + d.fuente + "', sans-serif";
    ctx.fillStyle = (d.color && d.color === "#F59E0B") ? "#FFFFFF" : "#FFB800";
    ctx.fillText(d.titulo.toUpperCase(), w / 2, 110, w - 80);

    // Badge lateral dentado
    ctx.save();
    ctx.translate(w - 95, cy - 20);
    ctx.rotate(0.18);
    ctx.fillStyle = "#FFFFFF";
    ctx.beginPath();
    ctx.arc(0, 0, 68, 0, Math.PI * 2);
    ctx.fill();
    ctx.lineWidth = 4;
    ctx.strokeStyle = bg;
    ctx.stroke();

    ctx.fillStyle = bg;
    ctx.font = "900 13px '" + d.fuente + "', sans-serif";
    ctx.textAlign = "center";
    ctx.fillText(d.sello.toUpperCase(), 0, -8);
    ctx.font = "900 32px '" + d.fuente + "', sans-serif";
    ctx.fillText("$" + Number(d.precio.replace(/\D/g, "") || 0).toLocaleString("es-CL"), 0, 28);
    ctx.restore();

    // Botón inferior
    ctx.fillStyle = (d.color && d.color === "#F59E0B") ? "#FFFFFF" : "#FFB800";
    if (ctx.roundRect) ctx.roundRect(w / 2 - 170, h - 85, 340, 56, 28);
    else ctx.fillRect(w / 2 - 170, h - 85, 340, 56);
    ctx.fill();

    ctx.font = "900 21px '" + d.fuente + "', sans-serif";
    ctx.fillStyle = (d.color && d.color === "#F59E0B") ? "#000000" : "#00388C";
    ctx.textAlign = "center";
    ctx.fillText("➔ PEDIR AHORA: " + d.mensaje, w / 2, h - 50, 310);
    ctx.restore();
  }

  // ESTILO 4: MINIMALISTA EDITORIAL (Apple / Farfetch)
  function dibujarPlantillaEstilo4(ctx, d, w, h) {
    var bg = "#F8F7F4";
    var strokeCol = "#1A1A1A";
    if (d.color && d.color !== "original") {
      bg = aclararColor(d.color, 0.94);
      strokeCol = d.color;
    }
    ctx.fillStyle = bg;
    ctx.fillRect(0, 0, w, h);

    ctx.strokeStyle = strokeCol;
    ctx.lineWidth = 1.5;
    ctx.strokeRect(28, 28, w - 56, h - 56);

    ctx.font = "600 13px '" + d.fuente + "', sans-serif";
    ctx.fillStyle = strokeCol;
    ctx.textAlign = "center";
    ctx.letterSpacing = "3px";
    ctx.fillText("EDICIÓN ARTESANAL LOCAL", w / 2, 60);

    var ex = 56;
    var ey = 82;
    var ew = w - 112;
    var eh = 390;

    if (d.img) {
      ctx.drawImage(d.img, ex, ey, ew, eh);
    } else {
      ctx.fillStyle = "#ECE9E2";
      ctx.fillRect(ex, ey, ew, eh);
      ctx.font = "70px sans-serif";
      ctx.textAlign = "center";
      ctx.fillText("☕", ex + ew / 2, ey + eh / 2 + 20);
    }

    ctx.fillStyle = (d.color && d.color !== "original") ? d.color : "rgba(26, 26, 26, 0.9)";
    ctx.fillRect(ex + 18, ey + 18, 140, 30);
    ctx.font = "700 11px '" + d.fuente + "', sans-serif";
    ctx.fillStyle = "#FFFFFF";
    ctx.textAlign = "center";
    ctx.fillText(d.sello.toUpperCase(), ex + 88, ey + 37);

    ctx.font = "700 32px '" + d.fuente + "', serif";
    ctx.fillStyle = strokeCol;
    ctx.textAlign = "left";
    ctx.fillText(d.titulo, ex, 525);

    ctx.font = "700 34px '" + d.fuente + "', sans-serif";
    ctx.fillStyle = strokeCol;
    ctx.fillText("$" + Number(d.precio.replace(/\D/g, "") || 0).toLocaleString("es-CL"), ex, 575);

    ctx.font = "400 14px sans-serif";
    ctx.fillStyle = "#777777";
    ctx.fillText("CLP", ex + 180, 575);

    ctx.strokeStyle = "#E0DED9";
    ctx.lineWidth = 1;
    ctx.beginPath();
    ctx.moveTo(ex, 605);
    ctx.lineTo(ex + ew, 605);
    ctx.stroke();

    ctx.font = "600 13px sans-serif";
    ctx.fillStyle = "#222222";
    ctx.textAlign = "left";
    ctx.fillText("COMPRA DIRECTA: " + d.mensaje, ex, 638);
  }

  // ESTILO 5: TRADICIÓN (Bosque nativo y dorado)
  function dibujarPlantillaEstilo5(ctx, d, w, h) {
    var bg = (d.color && d.color !== "original") ? oscurecerColor(d.color, 0.35) : "#163024";
    ctx.fillStyle = bg;
    ctx.fillRect(0, 0, w, h);

    ctx.strokeStyle = "#D4AF37";
    ctx.lineWidth = 4;
    ctx.strokeRect(20, 20, w - 40, h - 40);

    ctx.fillStyle = "#D4AF37";
    ctx.font = "800 18px '" + d.fuente + "', sans-serif";
    ctx.textAlign = "center";
    ctx.fillText("TRADICIÓN Y SABOR DE ALERCE", w / 2, 62);

    var sx = 65;
    var sy = 90;
    var sw = w - 130;
    var sh = 350;

    ctx.strokeStyle = "#FFFFFF";
    ctx.lineWidth = 4;
    ctx.strokeRect(sx, sy, sw, sh);

    if (d.img) {
      ctx.drawImage(d.img, sx, sy, sw, sh);
    } else {
      ctx.fillStyle = "#264837";
      ctx.fillRect(sx, sy, sw, sh);
      ctx.font = "75px sans-serif";
      ctx.textAlign = "center";
      ctx.fillText("🌲", sx + sw / 2, sy + sh / 2 + 25);
    }

    ctx.font = "800 36px '" + d.fuente + "', serif";
    ctx.fillStyle = "#FFF9E6";
    ctx.textAlign = "center";
    ctx.fillText(d.titulo, w / 2, 490);

    var rx = w / 2;
    var ry = 555;
    ctx.fillStyle = "#D4AF37";
    ctx.beginPath();
    if (ctx.roundRect) ctx.roundRect(rx - 150, ry - 32, 300, 56, 28);
    else ctx.fillRect(rx - 150, ry - 32, 300, 56);
    ctx.fill();

    ctx.font = "900 32px '" + d.fuente + "', sans-serif";
    ctx.fillStyle = bg;
    ctx.textAlign = "center";
    ctx.fillText("$" + Number(d.precio.replace(/\D/g, "") || 0).toLocaleString("es-CL"), rx, ry + 9);

    ctx.font = "700 16px sans-serif";
    ctx.fillStyle = "#FFFFFF";
    ctx.fillText("📲 " + d.mensaje, w / 2, 628);
  }

  // DESCARGAR IMAGEN DEL CANVAS
  function descargarFlyerImagen(formato) {
    var canvas = document.getElementById("canvas-flyer");
    if (!canvas) return;

    var inpTitulo = document.getElementById("flyer-inp-titulo");
    var nombreBase = inpTitulo && inpTitulo.value.trim() ? inpTitulo.value.trim().toLowerCase().replace(/[^a-z0-9]/g, "-") : "flyer-comercial";

    try {
      var mime = formato === "png" ? "image/png" : "image/jpeg";
      var dataUrl = canvas.toDataURL(mime, 0.92);
      var link = document.createElement("a");
      link.download = nombreBase + "-alerce." + (formato === "png" ? "png" : "jpg");
      link.href = dataUrl;
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      mostrarToast("📥 Afiche descargado con éxito");
    } catch (err) {
      console.error(err);
      mostrarToast("⚠️ No se pudo descargar directamente debido a políticas de seguridad de la imagen.");
    }
  }

  // APLICAR FLYER COMO FOTO DEL PRODUCTO
  // El afiche se convierte en imagen y se sube al guardar (nunca se guarda como Base64).
  function aplicarFlyerComoFotoProducto() {
    var canvas = document.getElementById("canvas-flyer");
    if (!canvas) return;
    try {
      var dataUrl = canvas.toDataURL("image/jpeg", 0.9);
      estadoPublicar.foto = { url: (estadoPublicar.foto && estadoPublicar.foto.url) || "", blob: null, dataUrl: dataUrl };
      var vista = campo("pub-foto-preview");
      if (vista) { vista.textContent = ""; vista.appendChild(h("img", { src: dataUrl, alt: "Afiche del producto", class: "pub-foto-img" })); }
      var flyerTit = document.getElementById("flyer-inp-titulo");
      var flyerPre = document.getElementById("flyer-inp-precio");
      if (flyerTit && campo("pub-nombre") && !campo("pub-nombre").value.trim()) campo("pub-nombre").value = flyerTit.value.trim();
      if (flyerPre && campo("pub-precio") && !campo("pub-precio").value.trim()) campo("pub-precio").value = flyerPre.value.trim().replace(/\D/g, "");
      cerrarModal("modal-estudio-flyer");
      if (!document.getElementById("modal-publicar-producto").classList.contains("abierto")) abrirModalPublicarProducto(estadoPublicar.productoId && estadoPublicar.previo ? estadoPublicar.productoId : undefined);
      mostrarToast("Afiche listo: se subirá cuando guardes el producto.");
    } catch (err) {
      console.error(err);
      mostrarToast("No se pudo usar el afiche (la imagen de fondo no lo permite).");
    }
  }

  // ============================================================
  // INICIALIZACIÓN DE EVENTOS Y ARRANQUE
  // ============================================================

  function inicializar() {
    // Buscador
    var inputBusqueda = document.getElementById("campo-busqueda-principal");
    if (inputBusqueda) {
      inputBusqueda.addEventListener("input", function (e) {
        estado.textoBusqueda = e.target.value;
        renderizarTiendasMarketplace();
      });
    }

    // Filtros de categoría pills
    var pillsCategorias = document.querySelectorAll("#barra-categorias-pills .pill-categoria");
    pillsCategorias.forEach(function (pill) {
      pill.addEventListener("click", function () {
        pillsCategorias.forEach(function (p) { p.classList.remove("activa"); });
        pill.classList.add("activa");
        estado.categoriaActiva = pill.getAttribute("data-categoria") || "Todos";
        renderizarTiendasMarketplace();
      });
    });

    // Abrir carrito
    var btnAbrirCarrito = document.getElementById("btn-abrir-carrito");
    var btnFlotanteCarrito = document.getElementById("btn-flotante-carrito");
    if (btnAbrirCarrito) {
      btnAbrirCarrito.addEventListener("click", function () {
        renderizarCarrito();
        abrirModal("modal-carrito");
      });
    }
    if (btnFlotanteCarrito) {
      btnFlotanteCarrito.addEventListener("click", function () {
        renderizarCarrito();
        abrirModal("modal-carrito");
      });
    }

    // Botón logout
    var btnLogout = document.getElementById("btn-logout");
    if (btnLogout) {
      btnLogout.addEventListener("click", function () {
        if (confirm("¿Deseas cerrar la sesión activa?")) {
          cerrarSesionWeb(); return;

          estado.usuario.nombre = "Visitante";
          estado.usuario.rol = "Visitante";
          var tag = document.getElementById("tag-nombre-usuario");
          var saludo = document.getElementById("saludo-usuario-cabecera");
    var btnHeaderAdmin = document.getElementById("btn-header-admin-panel");
    if (btnHeaderAdmin) {
      btnHeaderAdmin.style.display = (rol === "Administrador" || rol === "ADMIN") ? "inline-flex" : "none";
    }
          if (tag) tag.textContent = "Visitante";
          if (saludo) saludo.textContent = "Bienvenido a la Vitrina de Alerce";
          mostrarToast("Sesión cerrada");
        }
      });
    }

    // Navegación por anclas
    document.querySelectorAll(".menu-navegacion a").forEach(function (link) {
      link.addEventListener("click", function () {
        document.querySelectorAll(".menu-navegacion a").forEach(function (l) { l.classList.remove("activo"); });
        link.classList.add("activo");
      });
    });

    // Accesibilidad
    configurarAccesibilidad();

    // Render inicial
    actualizarBadgesCarrito();
    renderizarTiendasMarketplace();
    renderizarAsociaciones();
    configurarDomEntrega5();
    cargarCatalogoPublico();
    configurarObservadorAuthSupabase();
    cargarFeriasDesdeSupabase();
    actualizarUIControlesSegunRol();
  }

  // ============================================================
  // MÓDULO 9: PUNTO DE VENTA (POS) Y REGISTRO DE VENTAS EN VIVO
  // ============================================================

  var estadoPOS = {
    tiendaId: null,
    ventaPendiente: null,
    enviando: false,
    medioPago: "Efectivo",
    ticket: [],
    montoPagaCon: 0
  };

  function abrirModalPOS() {
    if (!esUsuarioEmprendedora()) {
      abrirModalAuthEmprendedora();
      mostrarToast("Inicia sesión como emprendedora para usar la caja.");
      return;
    }
    if (MODO !== "real") { mostrarToast("La caja necesita la conexión con el servidor."); return; }
    renderizarPOS();
    abrirModal("modal-pos");
    recargarMisProductos();
    if (servicio && !FERIAS_SERVIDOR.length) {
      servicio.listarFerias().then(function (r) {
        if (r.ok) { FERIAS_SERVIDOR = r.datos || []; pintarLugaresPOS(); }
      });
    }
  }

  function pintarLugaresPOS() {
    var sel = document.getElementById("pos-lugar");
    if (!sel) return;
    var actual = sel.value;
    sel.textContent = "";
    sel.appendChild(h("option", { value: "", text: "Sin indicar" }));
    sel.appendChild(h("option", { value: "OTRO", text: "Otro lugar (escribir)" }));
    FERIAS_SERVIDOR.forEach(function (f) { sel.appendChild(h("option", { value: "FERIA:" + f.id, text: "Feria: " + f.nombre })); });
    sel.value = Array.prototype.some.call(sel.options, function (o) { return o.value === actual; }) ? actual : "";
    var otro = document.getElementById("pos-lugar-otro");
    if (otro) otro.parentNode.style.display = sel.value === "OTRO" ? "" : "none";
  }

  // La tienda de la caja es siempre la de la sesión (ya no se elige de una lista).
  function cambiarTiendaPOS() {
    renderizarPOS();
  }

  function renderizarPOS() {
    var tienda = obtenerTiendaUsuarioActivo();
    var sub = document.getElementById("pos-tienda-subtitulo");
    if (sub) sub.textContent = tienda ? "Caja de " + tienda.nombre + " · las ventas descuentan unidades en el servidor" : "";
    var chkHist = document.getElementById("pos-historial");
    if (chkHist && tienda) chkHist.checked = tienda.historialActivo !== false;
    pintarLugaresPOS();

    var grid = document.getElementById("pos-grid-productos");
    var conteoDisp = document.getElementById("pos-conteo-disponibles");
    if (!grid) return;
    grid.textContent = "";
    var lineas = [];
    MIS_PRODUCTOS.forEach(function (p) {
      if (p.vendidaEn) return;
      p.variantes.forEach(function (v) { lineas.push({ p: p, v: v }); });
    });
    if (conteoDisp) conteoDisp.textContent = lineas.length + " opciones";
    if (!lineas.length) {
      grid.appendChild(h("p", { class: "aviso-catalogo", style: "grid-column: 1 / -1;", text: "No tienes productos para vender. Agrégalos desde «Añadir producto»." }));
    }
    lineas.forEach(function (x) {
      var p = x.p, v = x.v;
      var aPedido = p.modalidad === "A_PEDIDO" || Cat.estadoPublico(Object.assign({}, p, { publicado: true, disponible: true })) === "A_PEDIDO";
      var vendible = Cat.opcionVendible(p, v);
      var habilitado = vendible || aPedido;
      var detalle = p.modalidad === "PIEZA_UNICA" ? "Pieza única" : (aPedido && !vendible ? "Por encargo" : (p.manejaStock ? "Quedan " + v.stock : "Sin conteo"));
      grid.appendChild(h("button", {
        type: "button", class: "pos-tarjeta", disabled: !habilitado,
        "aria-label": "Agregar " + p.nombre + (p.variantes.length > 1 ? " " + v.nombre : "") + ", " + formatearMoneda(Cat.precioDeOpcion(p, v)) + ", " + detalle,
        onclick: function () { agregarProductoTicketPOS(p.id, v.id); }
      }, [
        imagenSegura(Cat.fotoDeOpcion(p, v), "", "pos-foto") || h("span", { class: "pos-foto pos-foto-vacia", "aria-hidden": "true", text: "📦" }),
        h("strong", { text: p.nombre + (p.variantes.length > 1 ? " · " + v.nombre : "") }),
        h("span", { class: "pos-precio", text: formatearMoneda(Cat.precioDeOpcion(p, v)) }),
        h("small", { text: habilitado ? detalle : "Agotada" })
      ]));
    });
    renderizarTicketPOS();
  }

  function agregarProductoTicketPOS(prodId, varianteId) {
    var p = MIS_PRODUCTOS.find(function (x) { return x.id === prodId; });
    if (!p) return;
    var v = p.variantes.find(function (x) { return x.id === varianteId; }) || p.variantes[0];
    if (!v) return;
    var encargo = !Cat.opcionVendible(p, v);
    var existe = estadoPOS.ticket.find(function (l) { return l.productoId === p.id && l.varianteId === v.id && l.encargo === encargo; });
    var cantidad = existe ? existe.cantidad + 1 : 1;
    var problema = Cat.validarLineaVenta(p, v, cantidad, encargo);
    if (problema) { mostrarAvisoPOS(problema, true); return; }
    if (existe) existe.cantidad = cantidad;
    else estadoPOS.ticket.push({ productoId: p.id, varianteId: v.id, nombre: p.nombre, detalle: v.nombre, precio: Cat.precioDeOpcion(p, v), cantidad: 1, encargo: encargo, fotoUrl: Cat.fotoDeOpcion(p, v) });
    estadoPOS.ventaPendiente = null;
    mostrarAvisoPOS("");
    renderizarTicketPOS();
  }

  function mostrarAvisoPOS(mensaje, esError) {
    var el = document.getElementById("pos-aviso");
    if (!el) { if (mensaje) mostrarToast(mensaje); return; }
    el.textContent = mensaje || "";
    el.className = "estado-datos" + (esError ? " error" : (mensaje ? " ok" : ""));
    el.style.display = mensaje ? "" : "none";
    el.setAttribute("role", esError ? "alert" : "status");
  }

  function modificarCantidadTicketPOS(index, delta) {
    var l = estadoPOS.ticket[index];
    if (!l) return;
    var nueva = l.cantidad + delta;
    if (nueva <= 0) estadoPOS.ticket.splice(index, 1);
    else {
      var p = MIS_PRODUCTOS.find(function (x) { return x.id === l.productoId; });
      var v = p && p.variantes.find(function (x) { return x.id === l.varianteId; });
      var problema = p ? Cat.validarLineaVenta(p, v, nueva, l.encargo) : "El producto ya no existe.";
      if (problema) { mostrarAvisoPOS(problema, true); return; }
      l.cantidad = nueva;
    }
    estadoPOS.ventaPendiente = null;
    mostrarAvisoPOS("");
    renderizarTicketPOS();
  }

  function vaciarTicketPOS() {
    estadoPOS.ticket = [];
    estadoPOS.ventaPendiente = null;
    renderizarTicketPOS();
  }

  function renderizarTicketPOS() {
    var lista = document.getElementById("pos-lista-items");
    var totalCobrarEl = document.getElementById("pos-total-cobrar");
    var total = 0;
    if (!lista) return;
    lista.textContent = "";
    if (!estadoPOS.ticket.length) {
      lista.appendChild(h("p", { class: "aviso-catalogo", text: "El ticket está vacío. Toca un producto para agregarlo." }));
    }
    estadoPOS.ticket.forEach(function (item, idx) {
      var subtotal = item.precio * item.cantidad;
      total += subtotal;
      lista.appendChild(h("div", { class: "pos-linea" }, [
        h("div", { class: "pos-linea-texto" }, [
          h("strong", { text: item.nombre }),
          h("small", { text: item.detalle + " · " + formatearMoneda(item.precio) + " c/u" + (item.encargo ? " · Encargo" : "") })
        ]),
        h("div", { class: "pos-linea-cantidad" }, [
          h("button", { type: "button", class: "btn-qty", "aria-label": "Quitar una unidad de " + item.nombre, text: "−", onclick: function () { modificarCantidadTicketPOS(idx, -1); } }),
          h("span", { "aria-live": "polite", text: String(item.cantidad) }),
          h("button", { type: "button", class: "btn-qty", "aria-label": "Sumar una unidad de " + item.nombre, text: "+", onclick: function () { modificarCantidadTicketPOS(idx, 1); } })
        ]),
        h("strong", { class: "pos-linea-total", text: formatearMoneda(subtotal) })
      ]));
    });
    if (totalCobrarEl) totalCobrarEl.textContent = formatearMoneda(total);
    calcularVueltoPOS();
  }

  function seleccionarMedioPagoPOS(medio) {
    estadoPOS.medioPago = medio;
    var btnEfe = document.getElementById("btn-pos-pago-efectivo");
    var btnTra = document.getElementById("btn-pos-pago-transferencia");
    var btnTar = document.getElementById("btn-pos-pago-tarjeta");
    var cajaVuelto = document.getElementById("pos-caja-calculo-vuelto");

    [btnEfe, btnTra, btnTar].forEach(function (b) {
      if (b) {
        b.className = "btn-accion-pill";
        b.style.background = "#fff";
        b.style.color = "var(--negro-apple)";
        b.style.border = "1px solid var(--gris-borde)";
      }
    });

    if (medio === "Efectivo" && btnEfe) {
      btnEfe.className = "btn-accion-pill btn-negro";
      btnEfe.style.background = "var(--negro-apple)";
      btnEfe.style.color = "#fff";
      if (cajaVuelto) cajaVuelto.style.display = "block";
    } else if (medio === "Transferencia" && btnTra) {
      btnTra.className = "btn-accion-pill btn-negro";
      btnTra.style.background = "var(--negro-apple)";
      btnTra.style.color = "#fff";
      if (cajaVuelto) cajaVuelto.style.display = "none";
    } else if (medio === "Tarjeta" && btnTar) {
      btnTar.className = "btn-accion-pill btn-negro";
      btnTar.style.background = "var(--negro-apple)";
      btnTar.style.color = "#fff";
      if (cajaVuelto) cajaVuelto.style.display = "none";
    }

    calcularVueltoPOS();
  }

  function calcularVueltoPOS() {
    var inp = document.getElementById("pos-inp-paga-con");
    var valorVueltoEl = document.getElementById("pos-valor-vuelto");
    if (!valorVueltoEl) return;

    var total = 0;
    estadoPOS.ticket.forEach(function (i) { total += (i.precio * i.cantidad); });

    var pagaCon = inp ? (parseInt(inp.value, 10) || 0) : 0;
    estadoPOS.montoPagaCon = pagaCon;

    if (estadoPOS.medioPago !== "Efectivo") {
      valorVueltoEl.textContent = "$ 0 (Monto exacto)";
      valorVueltoEl.style.color = "var(--gris-texto)";
      return;
    }

    if (pagaCon >= total && total > 0) {
      var vuelto = pagaCon - total;
      valorVueltoEl.textContent = formatearMoneda(vuelto);
      valorVueltoEl.style.color = "#16a34a";
    } else if (pagaCon > 0 && pagaCon < total) {
      var falta = total - pagaCon;
      valorVueltoEl.textContent = "Faltan " + formatearMoneda(falta);
      valorVueltoEl.style.color = "#dc2626";
    } else {
      valorVueltoEl.textContent = "$ 0";
      valorVueltoEl.style.color = "var(--gris-texto)";
    }
  }

  function confirmarVentaPOS() {
    if (!estadoPOS.ticket.length) { mostrarAvisoPOS("El ticket está vacío.", true); return; }
    if (!servicio || !esUsuarioEmprendedora()) { mostrarAvisoPOS("Vuelve a iniciar sesión para vender.", true); return; }
    if (estadoPOS.enviando) return;
    var tienda = obtenerTiendaUsuarioActivo();
    var lugar = (document.getElementById("pos-lugar") || {}).value || "";
    var otro = ((document.getElementById("pos-lugar-otro") || {}).value || "").trim();
    if (lugar === "OTRO" && !otro) { mostrarAvisoPOS("Escribe dónde fue la venta.", true); return; }
    var conHistorial = (document.getElementById("pos-historial") || { checked: true }).checked;

    // El mismo cuerpo (mismo id) se reutiliza si hay que reintentar: el servidor no duplica.
    if (!estadoPOS.ventaPendiente) {
      estadoPOS.ventaPendiente = Cat.ventaParaRegistrar(estadoPOS.ticket, {
        medioPago: estadoPOS.medioPago,
        feriaId: lugar.indexOf("FERIA:") === 0 ? lugar.slice(6) : null,
        lugarNombre: lugar === "OTRO" ? otro : null,
        conHistorial: conHistorial && (!tienda || tienda.historialActivo !== false)
      });
    }
    var cuerpo = estadoPOS.ventaPendiente;
    var boton = document.getElementById("pos-btn-confirmar");
    estadoPOS.enviando = true;
    if (boton) { boton.disabled = true; boton.textContent = "Registrando en el servidor…"; }
    mostrarAvisoPOS("Registrando la venta…");

    servicio.registrarVenta(cuerpo).then(function (r) {
      if (!r.ok) {
        var red = /fetch|network|conexión/i.test(Cat.mensajeError(r.error));
        if (!red) estadoPOS.ventaPendiente = null;
        mostrarAvisoPOS((red ? "No se confirmó la venta (sin conexión). Puedes reintentar sin duplicarla. " : "No se registró la venta: ") + Cat.mensajeError(r.error), true);
        if (!red) recargarMisProductos();
        return;
      }
      (r.datos.productos || []).forEach(function (cambio) {
        var p = MIS_PRODUCTOS.find(function (x) { return x.id === cambio.id; });
        if (p) {
          var fila = Object.assign({}, cambio, { id: p.id, tienda_id: p.tiendaId, nombre: p.nombre, descripcion: p.descripcion, categoria: p.categoria, tipo: p.tipo, precio: p.precio,
            publicado: p.publicado, disponible: p.disponible, destacado: p.destacado, foto_url: p.fotoUrl, fotos: p.fotos, modalidad: p.modalidad, maneja_stock: p.manejaStock, al_agotarse: p.alAgotarse });
          Object.assign(p, Cat.normalizarProducto(fila));
        }
      });
      var resumen = "Venta registrada en el servidor" + (r.datos.estado === "YA_REGISTRADA" ? " (ya estaba anotada; no se duplicó)" : "") +
        (r.datos.total !== null && r.datos.total !== undefined ? " · " + formatearMoneda(r.datos.total) : " · sin detalle (historial desactivado)");
      var lineasTexto = estadoPOS.ticket.map(function (it) { return "• " + it.nombre + " (" + it.detalle + ") x" + it.cantidad + " = " + formatearMoneda(it.precio * it.cantidad); }).join("\n");
      estadoPOS.ticket = [];
      estadoPOS.ventaPendiente = null;
      renderizarPOS();
      mostrarAvisoPOS(resumen + ".");
      mostrarToast(resumen);
      cargarCatalogoPublico();
      actualizarDashboardEmprendedora();
      if (confirm(resumen + ".\n¿Quieres compartir el comprobante por WhatsApp?")) {
        var texto = "Comprobante de venta - " + (tienda ? tienda.nombre : "Alerce") + "\n" + lineasTexto + "\nMedio de pago: " + estadoPOS.medioPago;
        window.open("https://wa.me/?text=" + encodeURIComponent(texto), "_blank", "noopener");
      }
    }).then(function () {
      estadoPOS.enviando = false;
      if (boton) { boton.disabled = false; boton.textContent = "Registrar venta"; }
    });
  }

  // ============================================================
  // SINCRONIZACIÓN EN VIVO CON SUPABASE CLOUD (INSTANCIA ALERCE I7)
  // ============================================================

  /** Aviso visible del estado de los datos: demostración, sin configuración, cargando o error. */
  function pintarEstadoDatos() {
    var caja = document.getElementById("estado-datos-vitrina");
    var estadoCabecera = document.getElementById("texto-estado-sincronizacion");
    var punto = document.getElementById("punto-estado-sincronizacion");
    var texto = "", clase = "", cabecera = "", conReintento = false;
    if (MODO === "sin_config") {
      clase = "error";
      texto = "La vitrina no está conectada a la base de datos (falta la configuración del sitio). No se muestran tiendas de ejemplo como si fueran reales.";
      cabecera = "Sin conexión configurada";
    } else if (MODO === "demo") {
      clase = "demo";
      texto = "Modo demostración: las tiendas y productos son de ejemplo y no son datos reales.";
      cabecera = "Modo demostración · datos de ejemplo";
    } else if (estadoCatalogo.error) {
      clase = "error";
      conReintento = true;
      texto = (estadoCatalogo.cargado ? "No se pudo actualizar el catálogo; se muestra lo último recibido. " : "No se pudo cargar el catálogo. ") + Cat.mensajeError(estadoCatalogo.error);
      cabecera = "Error de conexión con el servidor";
    } else if (estadoCatalogo.cargando) {
      clase = "cargando";
      texto = "Cargando tiendas y productos…";
      cabecera = "Conectando…";
    } else {
      cabecera = "Conectado · datos del servidor" + (estadoCatalogo.actualizado ? " (" + estadoCatalogo.actualizado.toLocaleTimeString("es-CL", { hour: "2-digit", minute: "2-digit" }) + ")" : "");
    }
    if (estadoCabecera) estadoCabecera.textContent = cabecera;
    if (punto) punto.className = "punto-verde" + (clase ? " punto-" + clase : "");
    if (!caja) return;
    caja.textContent = "";
    caja.className = "estado-datos" + (clase ? " " + clase : "");
    caja.style.display = texto ? "" : "none";
    caja.setAttribute("role", clase === "error" ? "alert" : "status");
    if (texto) caja.appendChild(h("span", { text: texto }));
    if (conReintento) caja.appendChild(h("button", { type: "button", class: "btn-accion-pill", text: "Reintentar", onclick: function () { cargarCatalogoPublico(); } }));
  }

  /**
   * Trae tiendas, productos visibles y tipos. Una respuesta válida (aunque
   * vacía) reemplaza lo anterior; un error lo conserva y se informa.
   */
  function cargarCatalogoPublico() {
    if (MODO !== "real") {
      pintarEstadoDatos();
      return Promise.resolve();
    }
    estadoCatalogo.cargando = true;
    pintarEstadoDatos();
    promesaCatalogo = Promise.all([servicio.listarTiendas(), servicio.listarProductosVisibles(), servicio.listarTipos()]).then(function (r) {
      var t = Cat.aplicarRespuesta({ elementos: TIENDAS, cargado: estadoCatalogo.cargado }, r[0], Cat.normalizarTienda);
      var p = Cat.aplicarRespuesta({ elementos: PRODUCTOS, cargado: estadoCatalogo.cargado }, r[1], Cat.normalizarProducto);
      if (r[2].ok) {
        TIPOS = {};
        (r[2].datos || []).forEach(function (x) { TIPOS[x.id] = x; });
      }
      TIENDAS = t.elementos;
      PRODUCTOS = p.elementos;
      PRODUCTOS.forEach(function (x) { x.tipoNombre = TIPOS[x.tipo] ? TIPOS[x.tipo].nombre : ""; });
      estadoCatalogo = { cargando: false, cargado: t.cargado && p.cargado, error: t.error || p.error, actualizado: (t.error || p.error) ? estadoCatalogo.actualizado : new Date() };
      if (estado.tiendaSeleccionada) {
        var vigente = TIENDAS.find(function (x) { return x.id === estado.tiendaSeleccionada.id; });
        if (vigente) estado.tiendaSeleccionada = vigente;
      }
      refrescarVistasCatalogo();
    });
    return promesaCatalogo;
  }

  function refrescarVistasCatalogo() {
    renderizarTiendasMarketplace();
    var modal = document.getElementById("modal-tienda-vitrina");
    if (modal && modal.classList.contains("abierto") && estado.tabModalActiva === 0) renderizarProductosModal();
    actualizarContadorTabsModal();
    if (typeof actualizarDashboardEmprendedora === "function") actualizarDashboardEmprendedora();
    pintarEstadoDatos();
  }

  /** Productos de mi tienda (también los que solo yo veo). Solo con sesión de emprendedora. */
  function recargarMisProductos() {
    var tienda = obtenerTiendaUsuarioActivo();
    if (!servicio || !tienda) { MIS_PRODUCTOS = []; return Promise.resolve({ ok: false }); }
    return servicio.listarMisProductos(tienda.id).then(function (r) {
      if (r.ok) {
        MIS_PRODUCTOS = (r.datos || []).map(Cat.normalizarProducto);
        MIS_PRODUCTOS.forEach(function (x) { x.tipoNombre = TIPOS[x.tipo] ? TIPOS[x.tipo].nombre : ""; });
      } else {
        mostrarEstadoGestion("No se pudo cargar tu catálogo. " + Cat.mensajeError(r.error), true);
      }
      renderizarGestionCatalogo();
      actualizarDashboardEmprendedora();
      var pos = document.getElementById("modal-pos");
      if (pos && pos.classList.contains("abierto")) renderizarPOS();
      return r;
    });
  }

  function cargarProductosDesdeSupabase() {
    return cargarCatalogoPublico();
  }

  function cargarFeriasDesdeSupabase() {
    if (!supabaseClient) return;
    supabaseClient.from("ferias").select("*").then(function (res) {
      if (res.error || !res.data || !res.data.length) return;
      res.data.forEach(function (f) {
        var idx = FERIAS_COMUNALES.findIndex(function (item) { return item.id === f.id; });
        var feriaObj = {
          id: f.id,
          nombre: f.nombre,
          sector: f.sector || "Alerce",
          ubicacion: f.direccion || f.sector || "Alerce",
          fecha: f.fecha_inicio ? new Date(f.fecha_inicio).toLocaleDateString("es-CL") : "Próximamente",
          dias: f.descripcion || "Sábados y Domingos",
          horario: "10:00 - 18:00 hrs",
          emprendedoresConfirmados: 12,
          inscripcionAbierta: f.estado !== "CERRADA"
        };
        if (idx >= 0) {
          FERIAS_COMUNALES[idx] = Object.assign({}, FERIAS_COMUNALES[idx], feriaObj);
        } else {
          FERIAS_COMUNALES.push(feriaObj);
        }
      });
    }).catch(function (e) {
      console.warn("Aviso ferias Supabase:", e);
    });
  }

  function leerErrorDeEnlace() {
    var hash = window.location.hash ? window.location.hash.slice(1) : "";
    var datos = new URLSearchParams(hash);
    var query = new URLSearchParams(window.location.search);
    var codigo = datos.get("error_code") || query.get("error_code");
    var descripcion = datos.get("error_description") || query.get("error_description");
    if (!codigo && !descripcion && !datos.get("error") && !query.get("error")) return null;
    if (codigo === "otp_expired") return "El enlace venció o ya se usó. Pide uno nuevo.";
    return "El enlace no es válido" + (descripcion ? ": " + descripcion.replace(/\+/g, " ") : "") + ". Pide uno nuevo.";
  }

  function configurarObservadorAuthSupabase() {
    if (!supabaseClient || !supabaseClient.auth) return;
    var vieneDeRecuperacion = /[?&]recuperar=1(&|$)/.test(window.location.search);
    var errorEnlace = leerErrorDeEnlace();
    var recuperacionActiva = false;

    if (errorEnlace) {
      mostrarAviso("recuperar-aviso-error", errorEnlace);
      abrirModal("modal-recuperar-contrasena");
      try { window.history.replaceState({}, document.title, window.location.pathname + (vieneDeRecuperacion ? "" : window.location.search)); } catch (e) {}
    }

    supabaseClient.auth.onAuthStateChange(function (event, session) {
      if (event === "PASSWORD_RECOVERY") {
        recuperacionActiva = true;
        mostrarAviso("cambiar-clave-aviso-error", "");
        abrirModal("modal-cambiar-clave");
        return;
      }
      if (session && session.user) {
        if (event === "INITIAL_SESSION" || event === "SIGNED_IN" || event === "USER_UPDATED") setTimeout(function () { procesarUsuarioSupabase(session.user); }, 0);
      } else if (event === "SIGNED_OUT") {
        setTimeout(cerrarSesionWebLocal, 0);
      }
    });

    supabaseClient.auth.getSession().then(function (resp) {
      var session = resp && resp.data ? resp.data.session : null;
      if (vieneDeRecuperacion && !errorEnlace) {
        setTimeout(function () {
          if (recuperacionActiva) return;
          if (session) { abrirModal("modal-cambiar-clave"); return; }
          mostrarAviso("recuperar-aviso-error", "No encontramos una solicitud de recuperación activa en este navegador. Pide un enlace nuevo.");
          abrirModal("modal-recuperar-contrasena");
        }, 800);
      }
    }).catch(function (e) { console.warn("Aviso sesión Supabase:", e); });
  }

  function procesarUsuarioSupabase(user) {
    if (!user || !servicio) return;
    if (estado.usuario && estado.usuario.id && estado.usuario.id !== user.id) {
      // Cambio de cuenta sin cierre previo: no deben quedar datos de la anterior.
      limpiarDatosPrivados();
    }
    var meta = user.user_metadata || {};
    var email = user.email || "";
    var nombreFallback = meta.nombre || meta.full_name || email.split("@")[0] || "Usuario";

    // RN-01: el rol y la tienda solo se aceptan desde el perfil creado por el servidor.
    servicio.leerPerfil(user.id).then(function (res) {
      if (!res.ok) throw res.error;
      var p = res.datos || {};
      var nombre = p.nombre || nombreFallback;
      var rol = p.rol === "ADMIN" || p.rol === "EMPRENDEDORA" ? p.rol : "VISITANTE";
      var tiendaId = rol === "EMPRENDEDORA" && p.tienda_id ? p.tienda_id : null;
      var fotoUrl = Cat.urlImagenSegura(p.foto_url || "") || null;

      estado.usuario = { id: user.id, email: email, nombre: nombre, rol: rol, tiendaId: tiendaId, fotoUrl: fotoUrl };
      estado.tiendaEmprendedoraId = tiendaId;
      estadoPOS.tiendaId = tiendaId;

      var rolEtiqueta = (rol === "ADMIN") ? "Administrador" : (rol === "EMPRENDEDORA" ? "Emprendedor/a" : "Comprador");
      aplicarUsuarioLocal(nombre, rolEtiqueta, fotoUrl);
      return promesaCatalogo.then(function () {
        if (tiendaId && !TIENDAS.some(function (t) { return t.id === tiendaId; })) return cargarCatalogoPublico();
      }).then(function () {
        actualizarUIControlesSegunRol();
        if (tiendaId) return recargarMisProductos();
        if (rol === "ADMIN") entrarModoAdmin();
      });
    }).catch(function (err) {
      console.warn("No se pudo leer el perfil:", err);
      estado.usuario = { id: user.id, email: email, nombre: nombreFallback, rol: "VISITANTE", tiendaId: null, fotoUrl: "" };
      estado.tiendaEmprendedoraId = null;
      estadoPOS.tiendaId = null;
      aplicarUsuarioLocal(nombreFallback, "Comprador", null);
      actualizarUIControlesSegunRol();
      mostrarToast("No pudimos leer tu perfil: " + Cat.mensajeError(err));
    });
  }

  // Sincronización en vivo con Cloud Firestore (compatibilidad de respaldo)
  // Firestore ya no se usa: los datos vienen solo de Supabase.
  function cargarProductosDesdeFirestore() {}

  // ============================================================
  // MÓDULO DE AUTENTICACIÓN FIREBASE WEB
  // ============================================================

  function abrirModalAuth() {
    var errorDiv = document.getElementById("auth-aviso-error");
    var exitoDiv = document.getElementById("auth-aviso-exito");
    if (errorDiv) errorDiv.style.display = "none";
    if (exitoDiv) exitoDiv.style.display = "none";
    abrirModal("modal-auth-web");
  }

  function cambiarRolAuth(rol) {
    estadoAuthWeb.rolSeleccionado = rol;
    var btnEmp = document.getElementById("btn-tab-auth-emprendedora");
    var btnComp = document.getElementById("btn-tab-auth-comprador");
    var btnAdmin = document.getElementById("btn-tab-auth-admin");
    var boxDonaMarta = document.getElementById("box-demo-donamarta");
    var boxAdmin = document.getElementById("box-demo-admin");
    var titulo = document.getElementById("auth-modal-titulo");
    var subtitulo = document.getElementById("auth-modal-subtitulo");
    var btnSubmit = document.getElementById("btn-auth-submit");

    if (btnEmp) {
      btnEmp.className = (rol === "EMPRENDEDORA") ? "btn-accion-pill btn-negro" : "btn-accion-pill";
      btnEmp.style.background = (rol === "EMPRENDEDORA") ? "" : "transparent";
      btnEmp.style.color = (rol === "EMPRENDEDORA") ? "" : "#374151";
    }
    if (btnComp) {
      btnComp.className = (rol === "COMPRADOR") ? "btn-accion-pill btn-negro" : "btn-accion-pill";
      btnComp.style.background = (rol === "COMPRADOR") ? "" : "transparent";
      btnComp.style.color = (rol === "COMPRADOR") ? "" : "#374151";
    }
    if (btnAdmin) {
      btnAdmin.className = (rol === "ADMIN") ? "btn-accion-pill btn-negro" : "btn-accion-pill";
      btnAdmin.style.background = (rol === "ADMIN") ? "#1e3a8a" : "transparent";
      btnAdmin.style.color = (rol === "ADMIN") ? "#ffffff" : "#374151";
    }

    if (boxDonaMarta) boxDonaMarta.style.display = (rol === "EMPRENDEDORA") ? "block" : "none";
    if (boxAdmin) boxAdmin.style.display = (rol === "ADMIN") ? "block" : "none";

    if (titulo) {
      if (rol === "EMPRENDEDORA") titulo.textContent = "Acceso Emprendedor/a";
      else if (rol === "ADMIN") titulo.textContent = "Acceso Administración de Plataforma";
      else titulo.textContent = "Acceso Comprador/a";
    }
    if (subtitulo) {
      if (rol === "EMPRENDEDORA") {
        subtitulo.textContent = "Ingresa a tu taller virtual de Alerce y administra tu vitrina comercial.";
      } else if (rol === "ADMIN") {
        subtitulo.textContent = "Panel de control oficial exclusivo para el Administrador de Plataforma (W01 / W02 / W03).";
      } else {
        subtitulo.textContent = "Inicia sesión para guardar favoritos y chatear con las artesanas.";
      }
    }
    if (btnSubmit) {
      btnSubmit.textContent = (rol === "ADMIN") ? "Iniciar Sesión como Administrador (W01)" : "Iniciar Sesión en Supabase";
    }
  }

  window.alternarPestanaAuth = seleccionarModoAuth;
  window.seleccionarModoAuth = seleccionarModoAuth;
  window.registrarEmprendedorModal = registrarEmprendedorModal;

  function seleccionarModoAuth(modo) {
    var btnTabLogin = document.getElementById("btn-tab-auth-modo-login");
    var btnTabReg = document.getElementById("btn-tab-auth-modo-registro");
    var secLogin = document.getElementById("auth-seccion-login");
    var secReg = document.getElementById("auth-seccion-registro");
    var titulo = document.getElementById("modal-auth-titulo");
    var errLogin = document.getElementById("auth-aviso-error");
    var errReg = document.getElementById("reg-emp-aviso-error");
    if (errLogin) errLogin.style.display = "none";
    if (errReg) errReg.style.display = "none";

    if (modo === "REGISTRO") {
      estadoAuthWeb.modoRegistro = true;
      if (btnTabLogin) {
        btnTabLogin.className = "btn-accion-pill";
        btnTabLogin.style.background = "transparent";
        btnTabLogin.style.border = "none";
        btnTabLogin.style.color = "";
      }
      if (btnTabReg) {
        btnTabReg.className = "btn-accion-pill btn-negro";
        btnTabReg.style.background = "#059669";
        btnTabReg.style.borderColor = "#059669";
        btnTabReg.style.color = "#ffffff";
      }
      if (secLogin) secLogin.style.display = "none";
      if (secReg) secReg.style.display = "flex";
      if (titulo) titulo.textContent = "Registrar Emprendedor/a";
    } else {
      estadoAuthWeb.modoRegistro = false;
      if (btnTabLogin) {
        btnTabLogin.className = "btn-accion-pill btn-negro";
        btnTabLogin.style.background = "";
        btnTabLogin.style.border = "";
        btnTabLogin.style.color = "";
      }
      if (btnTabReg) {
        btnTabReg.className = "btn-accion-pill";
        btnTabReg.style.background = "transparent";
        btnTabReg.style.border = "none";
        btnTabReg.style.color = "";
      }
      if (secLogin) secLogin.style.display = "flex";
      if (secReg) secReg.style.display = "none";
      if (titulo) titulo.textContent = "Acceso Emprendedor/a";
    }
  }

  function toggleModoAuth() {
    seleccionarModoAuth(estadoAuthWeb.modoRegistro ? "LOGIN" : "REGISTRO");
  }

  function registrarEmprendedorModal() {
    var inpNombre = document.getElementById("reg-emp-nombre");
    var selectRubro = document.getElementById("reg-emp-rubro");
    var selectSector = document.getElementById("reg-emp-sector");
    var inpWsp = document.getElementById("reg-emp-whatsapp");
    var inpEmail = document.getElementById("reg-emp-email");
    var inpPass = document.getElementById("reg-emp-password");
    var inpAvatar = document.getElementById("reg-emp-avatar");
    var inpLogo = document.getElementById("reg-emp-logo");
    var errorDiv = document.getElementById("reg-emp-aviso-error");
    var exitoDiv = document.getElementById("reg-emp-aviso-exito");
    var btnSubmit = document.getElementById("btn-submit-reg-emp");

    var nombre = inpNombre ? inpNombre.value.trim() : "";
    var rubro = selectRubro ? selectRubro.value : "Abarrotes y Alimentos";
    var sector = selectSector ? selectSector.value : "Alerce Histórico";
    var whatsapp = inpWsp ? inpWsp.value.trim() : "";
    var email = inpEmail ? inpEmail.value.trim().toLowerCase() : "";
    var pass = inpPass ? inpPass.value : "";
    var archivoAvatar = (inpAvatar && inpAvatar.files && inpAvatar.files[0]) ? inpAvatar.files[0] : null;
    var archivoLogo = (inpLogo && inpLogo.files && inpLogo.files[0]) ? inpLogo.files[0] : null;

    function mostrarErrorReg(msg) {
      if (errorDiv) {
        errorDiv.textContent = msg;
        errorDiv.style.display = "block";
      }
      if (btnSubmit) {
        btnSubmit.disabled = false;
        btnSubmit.textContent = "🚀 Registrar mi Emprendimiento";
      }
    }

    if (!nombre) {
      mostrarErrorReg("Por favor ingresa el nombre de tu emprendimiento o tu nombre.");
      return;
    }
    if (!email || !pass || pass.length < 6) {
      mostrarErrorReg("Por favor ingresa un correo válido y una contraseña de al menos 6 caracteres.");
      return;
    }

    if (errorDiv) errorDiv.style.display = "none";
    if (exitoDiv) exitoDiv.style.display = "none";
    if (btnSubmit) {
      btnSubmit.disabled = true;
      btnSubmit.textContent = "Registrando tu cuenta...";
    }

    if (supabaseClient && supabaseClient.auth) {
      supabaseClient.auth.signUp({
        email: email,
        password: pass,
        options: {
          data: {
            nombre: nombre,
            tipo_cuenta: "EMPRENDEDORA",
            rubro: rubro,
            sector: sector,
            whatsapp: whatsapp
          }
        }
      }).then(function (res) {
        if (res.error) throw res.error;
        var user = res.data ? res.data.user : null;
        if (!user) throw new Error("No se pudo obtener el usuario registrado.");
        if (!res.data || !res.data.session) {
          throw new Error("Cuenta creada. Revisa la configuración de Supabase: la confirmación de correo debe estar desactivada para entrar de inmediato.");
        }

        var cargaAvatar = archivoAvatar
          ? subirArchivoSupabase(BUCKET_PERFILES, user.id + "/avatar_" + Date.now() + ".jpg", archivoAvatar)
          : Promise.resolve(null);

        cargaAvatar.catch(function (eAvatar) {
          console.warn("Aviso subiendo foto de perfil:", eAvatar);
          mostrarToast("La cuenta se creó; no se pudo subir la foto. Puedes intentarlo desde Mi Perfil.");
          return null;
        }).then(function (fotoUrl) {
          return supabaseClient.rpc("actualizar_mi_perfil", {
            p_nombre: nombre,
            p_foto_url: fotoUrl || null
          }).then(function (resPerfil) {
            if (resPerfil.error) throw resPerfil.error;
            return supabaseClient.from("perfiles").select("rol, tienda_id, nombre, foto_url").eq("id", user.id).maybeSingle();
          });
        }).then(function (resPerfil) {
          if (resPerfil.error) throw resPerfil.error;
          var perfil = resPerfil.data || {};
          var rol = perfil.rol === "EMPRENDEDORA" || perfil.rol === "ADMIN" ? perfil.rol : "VISITANTE";
          var tiendaId = rol === "EMPRENDEDORA" ? perfil.tienda_id : null;
          var fotoUrl = perfil.foto_url || "";

          if (!tiendaId) {
            estado.usuario = { id: user.id, email: email, nombre: nombre, rol: "VISITANTE", tiendaId: null, fotoUrl: fotoUrl };
            estado.tiendaEmprendedoraId = null;
            estadoPOS.tiendaId = null;
            aplicarUsuarioLocal(nombre, "Comprador", fotoUrl);
            cerrarModal("modal-registro-emprendedor");
            alert("Tu cuenta ha sido creada como compradora. Para abrir una tienda, el administrador debe autorizar primero tu correo.");
            return;
          }

          var cargaLogo = archivoLogo
            ? subirArchivoSupabase("productos", user.id + "/logo_tienda_" + tiendaId + "_" + Date.now() + ".jpg", archivoLogo)
            : Promise.resolve(null);
          return cargaLogo.then(function (logoUrl) {
            var datosTienda = { nombre: nombre, rubro: rubro, sector: sector, whatsapp: whatsapp };
            if (logoUrl) datosTienda.logo_url = logoUrl;
            return supabaseClient.from("tiendas").update(datosTienda).eq("id", tiendaId).select("id").then(function (resTienda) {
              if (resTienda.error) throw resTienda.error;
              if (!resTienda.data || !resTienda.data.length) throw new Error("El servidor no confirmó la actualización de la tienda autorizada.");
              estado.tiendaEmprendedoraId = tiendaId;
              estadoPOS.tiendaId = tiendaId;
              estado.usuario = { id: user.id, email: email, nombre: nombre, rol: "EMPRENDEDORA", tiendaId: tiendaId, fotoUrl: fotoUrl };
              aplicarUsuarioLocal(nombre, "Emprendedor/a", fotoUrl);
              cargarCatalogoPublico();
              actualizarDashboardEmprendedora();
              cerrarModal("modal-registro-emprendedor");
              mostrarToast("¡Cuenta creada! Tu negocio autorizado ya tiene su ficha y su imagen.");
            });
          }).catch(function (eTienda) {
            console.warn("No se pudo guardar la tienda/logo:", eTienda);
            estado.usuario = { id: user.id, email: email, nombre: nombre, rol: "EMPRENDEDORA", tiendaId: tiendaId, fotoUrl: fotoUrl };
            estado.tiendaEmprendedoraId = tiendaId;
            estadoPOS.tiendaId = tiendaId;
            aplicarUsuarioLocal(nombre, "Emprendedor/a", fotoUrl);
            mostrarErrorReg("La cuenta está creada, pero no se pudo guardar el logo o los datos del negocio. Puedes volver a intentarlo desde Mi Tienda.");
          });
        }).catch(function (ePerfil) {
          console.warn("No se pudo completar el perfil inicial:", ePerfil);
          mostrarErrorReg("La cuenta se creó, pero no se pudo leer el perfil autorizado. Inicia sesión nuevamente o pide ayuda al administrador.");
        }).then(function () {
          if (btnSubmit) { btnSubmit.disabled = false; btnSubmit.textContent = "🚀 Registrar mi Emprendimiento"; }
        });
      }).catch(function (err) {
        console.warn("Aviso Supabase SignUp:", err);
        mostrarErrorReg("Error al registrar cuenta: " + (err.message || err));
      });
      return;
    }

    mostrarErrorReg("El servicio de cuentas no está configurado. Intenta más tarde.");
  }

  function intentarFallbackFirebase(email, pass, nombre, errorDiv, btnSubmit) {
    if (errorDiv) {
      errorDiv.textContent = "El acceso con Firebase ya no está habilitado. Configura Supabase para iniciar sesión.";
      errorDiv.style.display = "block";
    }
    if (btnSubmit) {
      btnSubmit.disabled = false;
      btnSubmit.textContent = estadoAuthWeb.modoRegistro ? "Registrarme en Supabase" : "Iniciar Sesión";
    }
  }

  function ejecutarAuthFirebase() {
    var inpEmail = document.getElementById("auth-inp-email");
    var inpPass = document.getElementById("auth-inp-password");
    var inpNombre = document.getElementById("auth-inp-nombre");
    var errorDiv = document.getElementById("auth-aviso-error");
    var exitoDiv = document.getElementById("auth-aviso-exito");
    var btnSubmit = document.getElementById("btn-submit-auth");

    var email = inpEmail ? inpEmail.value.trim() : "";
    var pass = inpPass ? inpPass.value : "";
    var nombre = inpNombre ? inpNombre.value.trim() : "";

    // Verificación de cuenta desactivada por el administrador (W08 / W09 / RNF-SEG-01)
    if (typeof ESTADO_ADMIN !== "undefined" && ESTADO_ADMIN.cuentasEmprendedoras) {
      var cuentaAdmin = ESTADO_ADMIN.cuentasEmprendedoras.find(function(c) {
        return c.email && c.email.toLowerCase() === email.toLowerCase();
      });
      if (cuentaAdmin && cuentaAdmin.activa === false) {
        mostrarErrorAuth("🚫 Tu cuenta ha sido desactivada por el administrador de la plataforma (Requisito W09). Comunícate con la administración.");
        return;
      }
    }

    if (!email || !pass) {
      if (errorDiv) {
        errorDiv.textContent = "Por favor ingresa tu correo y contraseña.";
        errorDiv.style.display = "block";
      }
      return;
    }

    if (estadoAuthWeb.modoRegistro && !nombre) {
      if (errorDiv) {
        errorDiv.textContent = "Por favor ingresa tu nombre o el de tu emprendimiento.";
        errorDiv.style.display = "block";
      }
      return;
    }

    if (errorDiv) errorDiv.style.display = "none";
    if (exitoDiv) exitoDiv.style.display = "none";
    if (btnSubmit) {
      btnSubmit.disabled = true;
      btnSubmit.textContent = "Iniciando sesión...";
    }

    // 1. Intentar con Supabase Auth Oficial
    if (supabaseClient && supabaseClient.auth) {
      if (estadoAuthWeb.modoRegistro) {
        supabaseClient.auth.signUp({
          email: email,
          password: pass,
          options: {
            data: {
              nombre: nombre
            }
          }
        }).then(function (res) {
          if (btnSubmit) {
            btnSubmit.disabled = false;
            btnSubmit.textContent = "Registrarme en Supabase";
          }
          if (res.error) {
            throw res.error;
          }
          var user = res.data ? res.data.user : null;
          if (!user) throw new Error("No se pudo obtener el usuario registrado.");
          var session = res.data ? res.data.session : null;
          if (!session) {
            if (errorDiv) {
              errorDiv.textContent = "Cuenta creada. Confirma tu correo para iniciar sesión.";
              errorDiv.style.display = "block";
            }
            return;
          }
          procesarUsuarioSupabase(user);
          cerrarModal("modal-auth-web");
          mostrarToast("¡Cuenta creada exitosamente!");
        }).catch(function (err) {
          console.warn("Aviso Supabase SignUp:", err);
          if (errorDiv) { errorDiv.textContent = err.message || "No se pudo crear la cuenta."; errorDiv.style.display = "block"; }
          if (btnSubmit) { btnSubmit.disabled = false; btnSubmit.textContent = "Registrarme en Supabase"; }
        });
        return;
      } else {
        supabaseClient.auth.signInWithPassword({
          email: email,
          password: pass
        }).then(function (res) {
          if (btnSubmit) {
            btnSubmit.disabled = false;
            btnSubmit.textContent = "Iniciar Sesión";
          }
          if (res.error) {
            throw res.error;
          }
          var user = res.data ? res.data.user : null;
          if (!user) throw new Error("El servicio no devolvió un usuario de sesión válido.");
          procesarUsuarioSupabase(user);
          cerrarModal("modal-auth-web");
          mostrarToast("¡Sesión iniciada con éxito!");
        }).catch(function (err) {
          console.warn("Aviso Supabase SignIn:", err);
          if (errorDiv) { errorDiv.textContent = err.message || "No se pudo iniciar sesión."; errorDiv.style.display = "block"; }
          if (btnSubmit) { btnSubmit.disabled = false; btnSubmit.textContent = "Iniciar Sesión"; }
        });
        return;
      }
    }

    if (errorDiv) { errorDiv.textContent = "El servicio de cuentas no está configurado. Intenta más tarde."; errorDiv.style.display = "block"; }
    if (btnSubmit) { btnSubmit.disabled = false; btnSubmit.textContent = "Iniciar Sesión"; }
  }

  // Solo existe en modo demostración: en producción no hay cuentas ni claves de muestra.
  function autocompletarCredenciales(email, pass) {
    if (MODO !== "demo") return;
    var inpEmail = document.getElementById("auth-inp-email");
    var inpPass = document.getElementById("auth-inp-password");
    if (inpEmail) inpEmail.value = email;
    if (inpPass) inpPass.value = pass;
  }

  function entrarModoDev(tiendaId) {
    if (!esUsuarioEmprendedora() || estado.usuario.tiendaId !== tiendaId) {
      mostrarToast("Inicia sesión con la cuenta autorizada para esta tienda.");
      return;
    }
    if (tiendaId === "t_minimarket_rosa") {
      entrarModoDonaRosa();
      return;
    }
    var tienda = null;
    for (var i = 0; i < TIENDAS.length; i++) {
      if (TIENDAS[i].id === tiendaId) {
        tienda = TIENDAS[i];
        break;
      }
    }
    var nombre = tienda ? tienda.nombre : "Minimarket Doña Rosa";
    cerrarModal("modal-auth-web");
    mostrarToast("Sesión activa para " + nombre + ".");
  }

  function entrarModoDonaRosa() {
    if (!esUsuarioEmprendedora()) {
      mostrarToast("Inicia sesión con un correo autorizado por la administración para gestionar una tienda.");
      return;
    }
    // Conserva la tienda asociada por el servidor; este acceso antiguo ya no cambia la identidad.
    actualizarDashboardEmprendedora();
    mostrarToast("Se abrió el panel de tu tienda autorizada.");
  }

  function esUsuarioEmprendedora() {
    if (!estado.usuario || !estado.usuario.nombre || estado.usuario.nombre === "Visitante") {
      return false;
    }
    var rol = (estado.usuario.rol || "").toLowerCase();
    return rol.indexOf("emprendedor") !== -1 && Boolean(estado.usuario.tiendaId) &&
      estado.usuario.tiendaId === estado.tiendaEmprendedoraId;
  }

  function actualizarUIControlesSegunRol() {
    var esEmp = esUsuarioEmprendedora();

    var btnPubNav = document.getElementById("btn-abrir-publicar-nav");
    var btnPosNav = document.getElementById("btn-abrir-pos-nav");
    var btnCatNav = document.getElementById("btn-abrir-catalogo-nav");
    var btnAccesoEmp = document.getElementById("btn-header-acceso-emp");
    var bannerEmp = document.getElementById("banner-emprendedoras-cta");
    var bannerInvitaEmp = document.getElementById("banner-invitacion-emprendedora");
    var btnLogout = document.getElementById("btn-logout");

    if (btnPubNav) btnPubNav.style.display = esEmp ? "inline-flex" : "none";
    if (btnPosNav) btnPosNav.style.display = esEmp ? "inline-flex" : "none";
    if (btnCatNav) btnCatNav.style.display = esEmp ? "inline-flex" : "none";
    if (btnAccesoEmp) btnAccesoEmp.style.display = esEmp ? "none" : "inline-flex";

    if (bannerEmp) bannerEmp.style.display = esEmp ? "flex" : "none";
    if (bannerInvitaEmp) bannerInvitaEmp.style.display = esEmp ? "none" : "flex";

    if (btnLogout) {
      btnLogout.style.display = (estado.usuario && estado.usuario.nombre && estado.usuario.nombre !== "Visitante") ? "inline-flex" : "none";
    }

    if (esEmp) {
      actualizarDashboardEmprendedora();
    }
  }

  function abrirModalAuthEmprendedora() {
    cambiarRolAuth('EMPRENDEDORA');
    seleccionarModoAuth('LOGIN');
    abrirModalAuth();
  }

  function inicialesDeNombre(nombre) {
    var partes = String(nombre || "").trim().split(/\s+/).filter(function (p) { return p.length > 0; });
    if (!partes.length) return "U";
    return partes.slice(0, 2).map(function (p) { return p.charAt(0).toUpperCase(); }).join("");
  }

  function actualizarAvatarHeader(nombre, fotoUrl) {
    var caja = document.getElementById("avatar-usuario-header");
    var img = document.getElementById("avatar-img-usuario");
    var iniciales = document.getElementById("avatar-iniciales-usuario");
    var hayUsuario = nombre && nombre !== "Visitante";
    if (caja) caja.style.display = hayUsuario ? "inline-flex" : "none";
    if (!hayUsuario) {
      if (img) { img.style.display = "none"; img.removeAttribute("src"); }
      if (iniciales) { iniciales.style.display = "inline-block"; iniciales.textContent = "U"; }
      return;
    }
    if (iniciales) iniciales.textContent = inicialesDeNombre(nombre);
    if (img) {
      img.onerror = function () {
        img.style.display = "none";
        if (iniciales) iniciales.style.display = "inline-block";
      };
      if (fotoUrl) {
        img.src = fotoUrl;
        img.style.display = "block";
        if (iniciales) iniciales.style.display = "none";
      } else {
        img.style.display = "none";
        img.removeAttribute("src");
        if (iniciales) iniciales.style.display = "inline-block";
      }
    }
  }

  function aplicarUsuarioLocal(nombre, rol, fotoUrl) {
    estado.usuario = estado.usuario || {};
    estado.usuario.nombre = nombre;
    estado.usuario.rol = rol;
    if (typeof fotoUrl !== "undefined") estado.usuario.fotoUrl = fotoUrl || "";
    actualizarAvatarHeader(nombre, estado.usuario.fotoUrl);
    var tag = document.getElementById("tag-nombre-usuario");
    var saludo = document.getElementById("saludo-usuario-cabecera");
    if (tag) {
      if (nombre && nombre !== "Visitante") {
        tag.textContent = nombre + " (" + rol + ")";
      } else {
        tag.textContent = "Inicio de Sesión / Registro";
      }
    }
    if (saludo) {
      if (nombre && nombre !== "Visitante") {
        saludo.textContent = "Bienvenido(a), " + nombre + " · Vitrina de Alerce";
      } else {
        saludo.textContent = "Vitrina Comercial de Alerce · Puerto Montt";
      }
    }
    actualizarUIControlesSegunRol();
  }

  /** Quita de memoria y del navegador todo lo que pertenecía a la cuenta anterior. */
  function limpiarDatosPrivados() {
    MIS_PRODUCTOS = [];
    estadoPOS.ticket = [];
    estadoPOS.ventaPendiente = null;
    estadoPublicar = nuevoEstadoPublicar();
    try { localStorage.removeItem("alerce_admin_state_v1"); } catch (e) { /* sin almacenamiento */ }
    var lista = document.getElementById("lista-items-catalogo-gestion");
    if (lista) lista.textContent = "";
    ["modal-gestion-catalogo", "modal-pos", "modal-publicar-producto", "modal-mi-perfil", "modal-editar-tienda", "modal-admin"].forEach(function (id) {
      var m = document.getElementById(id);
      if (m && m.classList.contains("abierto")) cerrarModal(id);
    });
  }

  function cerrarSesionWebLocal() {
    limpiarDatosPrivados();
    estado.tiendaEmprendedoraId = null;
    estadoPOS.tiendaId = null;
    estado.usuario = { nombre: "Visitante", rol: "Visitante", fotoUrl: "" };
    aplicarUsuarioLocal("Visitante", "Visitante", "");
    actualizarUIControlesSegunRol();
    // La lista pública se vuelve a pedir sin sesión: lo que solo veía la dueña desaparece.
    cargarCatalogoPublico();
  }

  function cerrarSesionWeb() {
    var salida = (supabaseClient && supabaseClient.auth) ? supabaseClient.auth.signOut() : Promise.resolve({});
    Promise.resolve(salida).then(function (res) {
      if (res && res.error) mostrarToast("La sesión se cerró en este navegador, pero el servidor respondió: " + Cat.mensajeError(res.error));
      else mostrarToast("Sesión cerrada");
    }, function () {
      mostrarToast("La sesión se cerró en este navegador (sin conexión con el servidor).");
    }).then(cerrarSesionWebLocal);
  }

  function configurarObservadorAuthFirebase() {
    if (firebaseAuth && firebaseAuth.onAuthStateChanged) {
      firebaseAuth.onAuthStateChanged(function (user) {
        if (user) {
          var nombre = user.displayName || (user.email ? user.email.split("@")[0] : "Usuario");
          if (firebaseFirestore) {
            firebaseFirestore.collection("usuarios").doc(user.uid).get().then(function (docU) {
              var rol = "Visitante";
              if (docU.exists && docU.data().rol) {
                rol = docU.data().rol === "EMPRENDEDORA" ? "Emprendedor/a" : "Comprador/a";
              }
              aplicarUsuarioLocal(nombre, rol);
            }).catch(function () {
              aplicarUsuarioLocal(nombre, "Usuario");
            });
          } else {
            aplicarUsuarioLocal(nombre, "Usuario");
          }
        } else {
          aplicarUsuarioLocal("Visitante", "Visitante");
        }
      });
    }
  }

  // ============================================================
  // MÓDULO 10: FUNCIONALIDADES DE EMPRENDEDORA (Matching Android)
  // DashboardScreen, CatalogoScreen, MisFeriasScreen, TiendaScreen
  // ============================================================

  
  // ============================================================
  // RESOLUCIÓN DINÁMICA DE TIENDA POR USUARIO (CERO FUGA DE DOÑA ROSA)
  // ============================================================

  function obtenerTiendaUsuarioActivo() {
    var user = estado.usuario || {};
    var tieneRolEmprendedora = user.rol === "EMPRENDEDORA" || String(user.rol || "").toLowerCase().indexOf("emprendedor") !== -1;
    if (!tieneRolEmprendedora || !user.tiendaId || estado.tiendaEmprendedoraId !== user.tiendaId) return null;
    return TIENDAS.find(function (it) { return it.id === user.tiendaId; }) || null;
  }


  var resumenVentasMes = { tiendaId: null, total: null, cargando: false };

  function actualizarDashboardEmprendedora() {
    var tienda = obtenerTiendaUsuarioActivo();
    if (!tienda) return;
    var prodsTienda = MIS_PRODUCTOS;
    var publicos = prodsTienda.filter(function (p) { return Cat.estadoPublico(p) !== "OCULTO"; });
    var unidades = prodsTienda.reduce(function (acc, p) { return acc + (p.manejaStock ? Cat.unidadesVendibles(p) : 0); }, 0);

    function texto(id, valor) { var el = document.getElementById(id); if (el) el.textContent = valor; }
    var nombreSaludo = (estado.usuario && estado.usuario.nombre && estado.usuario.nombre !== "Visitante") ? estado.usuario.nombre : tienda.nombre;
    texto("dashboard-emp-saludo", "Hola, " + nombreSaludo + " 👋");
    texto("dashboard-emp-tienda-badge", "Emprendedor/a · " + tienda.nombre);
    texto("dashboard-emp-subtitulo", "Panel de " + tienda.nombre + (tienda.sector ? " (" + tienda.sector + ")" : "") + ".");
    texto("subtitulo-gestion-catalogo", tienda.nombre + (tienda.sector ? " · " + tienda.sector : ""));
    texto("subtitulo-mis-ferias", "Ferias de " + tienda.nombre + " (las inscripciones de esta sección aún se guardan solo en este navegador)");
    texto("metrica-prods-activos", publicos.length + " a la vista");
    texto("metrica-stock-total", unidades + " un.");
    // Sin datos de seguidores en el servidor: no se inventan cifras.
    texto("metrica-seguidores", "Sin datos");
    texto("metrica-sub-seguidores", "Disponible en una próxima entrega");

    var agotados = prodsTienda.filter(function (p) { return Cat.estadoPublico(p) === "AGOTADO"; });
    var bajos = prodsTienda.filter(function (p) { return p.manejaStock && p.modalidad === "REGULAR" && Cat.unidadesVendibles(p) > 0 && Cat.unidadesVendibles(p) <= 3; });
    texto("texto-alerta-stock", !prodsTienda.length ? "Aún no has agregado productos." :
      (agotados.length ? agotados.length + " producto(s) agotado(s): " + agotados.map(function (p) { return p.nombre; }).slice(0, 3).join(", ") :
      (bajos.length ? bajos[0].nombre + ": quedan " + Cat.unidadesVendibles(bajos[0]) + " unidades." : "Todos tus productos con conteo tienen unidades.")));
    texto("texto-proxima-feria", FERIAS_SERVIDOR.length ? FERIAS_SERVIDOR.length + " ferias registradas en la plataforma." : "Sin ferias registradas en la plataforma.");

    if (servicio && resumenVentasMes.tiendaId !== tienda.id && !resumenVentasMes.cargando) {
      resumenVentasMes = { tiendaId: tienda.id, total: null, cargando: true };
      var inicio = new Date(); inicio.setDate(1); inicio.setHours(0, 0, 0, 0);
      supabaseClient.from("ventas").select("total").eq("tienda_id", tienda.id).eq("en_historial", true).gte("fecha", inicio.toISOString()).then(function (res) {
        resumenVentasMes.cargando = false;
        resumenVentasMes.total = res.error ? null : (res.data || []).reduce(function (s, v) { return s + (Number(v.total) || 0); }, 0);
        texto("metrica-ventas-mes", resumenVentasMes.total === null ? "Sin datos" : formatearMoneda(resumenVentasMes.total));
      });
    }
    texto("metrica-ventas-mes", resumenVentasMes.total === null ? (resumenVentasMes.cargando ? "Cargando…" : "Sin datos") : formatearMoneda(resumenVentasMes.total));
  }

  function abrirModalGestionCatalogo() {
    if (!esUsuarioEmprendedora()) {
      abrirModalAuthEmprendedora();
      mostrarToast("Inicia sesión como emprendedora para gestionar el catálogo.");
      return;
    }
    renderizarGestionCatalogo();
    abrirModal("modal-gestion-catalogo");
    recargarMisProductos();
  }

  function mostrarEstadoGestion(mensaje, esError) {
    var el = document.getElementById("estado-gestion-catalogo");
    if (!el) { if (mensaje) mostrarToast(mensaje); return; }
    el.textContent = mensaje || "";
    el.className = "estado-datos" + (esError ? " error" : (mensaje ? " ok" : ""));
    el.style.display = mensaje ? "" : "none";
    el.setAttribute("role", esError ? "alert" : "status");
  }

  /** Ejecuta una operación remota y solo informa éxito después de la respuesta del servidor. */
  function operacionRemota(promesa, mensajeExito) {
    mostrarEstadoGestion("Guardando en el servidor…");
    return promesa.then(function (r) {
      if (!r.ok) {
        mostrarEstadoGestion("No se guardó: " + Cat.mensajeError(r.error), true);
        return r;
      }
      mostrarEstadoGestion(mensajeExito + " ✓");
      return recargarMisProductos().then(function () { cargarCatalogoPublico(); return r; });
    });
  }

  var seleccionGestion = {};

  function renderizarGestionCatalogo() {
    var cont = document.getElementById("lista-items-catalogo-gestion");
    var conteo = document.getElementById("conteo-prods-catalogo-gestion");
    if (!cont) return;
    cont.textContent = "";
    var tienda = obtenerTiendaUsuarioActivo();
    if (!tienda) { cont.appendChild(h("p", { text: "Tu cuenta todavía no tiene una tienda." })); return; }
    var filtroCat = (document.getElementById("gestion-filtro-categoria") || {}).value || "";
    var lista = MIS_PRODUCTOS.filter(function (p) { return !filtroCat || p.categoria === filtroCat; });
    if (conteo) conteo.textContent = MIS_PRODUCTOS.length + " productos en tu catálogo";
    Object.keys(seleccionGestion).forEach(function (id) { if (!MIS_PRODUCTOS.some(function (p) { return p.id === id; })) delete seleccionGestion[id]; });

    if (!lista.length) {
      cont.appendChild(h("p", { class: "aviso-catalogo", text: MIS_PRODUCTOS.length ? "No hay productos en esa categoría." : "Aún no tienes productos. Usa «Añadir producto»." }));
      return;
    }
    lista.forEach(function (p) {
      var estadoP = Cat.estadoPublico(p);
      var unidades = p.modalidad === "PIEZA_UNICA" ? (p.vendidaEn ? "Pieza única vendida" : "Pieza única")
        : (p.modalidad === "A_PEDIDO" ? "A pedido" : (p.manejaStock ? "Quedan " + Cat.unidadesVendibles(p) : "Sin conteo"));
      var chk = h("input", { type: "checkbox", id: "sel-" + p.id, "aria-label": "Seleccionar " + p.nombre });
      chk.checked = !!seleccionGestion[p.id];
      chk.addEventListener("change", function () { if (chk.checked) seleccionGestion[p.id] = true; else delete seleccionGestion[p.id]; });
      var miniatura = imagenSegura(p.fotoUrl, "", "gestion-miniatura") || h("div", { class: "gestion-miniatura", "aria-hidden": "true", text: obtenerIconoProducto(p.nombre) });
      cont.appendChild(h("div", { class: "item-catalogo-gestion" }, [
        h("div", { class: "item-catalogo-gestion-info" }, [
          chk, miniatura,
          h("div", { style: "flex: 1; min-width: 0;" }, [
            h("strong", { class: "gestion-nombre", text: p.nombre }),
            h("span", { class: "gestion-detalle", text: [Cat.etiquetaCategoria(p), Cat.ETIQUETA_MODALIDAD[p.modalidad], unidades, p.variantes.length > 1 ? p.variantes.length + " opciones" : ""].filter(Boolean).join(" · ") }),
            h("div", { class: "gestion-estado" }, [
              h("strong", { text: formatearMoneda(p.precio) }),
              h("span", { class: "chip-estado-publico estado-" + estadoP.toLowerCase(), text: p.publicado ? (p.disponible ? "Público: " + Cat.etiquetaEstado(estadoP) : "No disponible") : "Solo yo lo veo" })
            ])
          ])
        ]),
        h("div", { class: "gestion-acciones" }, [
          h("button", { type: "button", class: "btn-accion-pill", text: "Editar", onclick: function () { abrirModalPublicarProducto(p.id); } }),
          h("button", { type: "button", class: "btn-accion-pill", "aria-pressed": p.publicado ? "true" : "false", text: p.publicado ? "Lo ven todos" : "Solo yo", onclick: function () { togglePublicadoProducto(p.id); } }),
          h("button", { type: "button", class: "btn-accion-pill", "aria-pressed": p.disponible ? "true" : "false", text: p.disponible ? "Disponible" : "No disponible", onclick: function () {
            operacionRemota(servicio.cambiarDisponible([p.id], !p.disponible), p.disponible ? "Marcado como no disponible" : "Marcado como disponible");
          } }),
          h("button", { type: "button", class: "btn-eliminar-item", "aria-label": "Eliminar " + p.nombre, text: "🗑️", onclick: function () { eliminarProductoCatalogo(p.id); } })
        ])
      ]));
    });
  }

  function cambiarDisponibleSeleccion(disponible) {
    var ids = Object.keys(seleccionGestion);
    var filtroCat = (document.getElementById("gestion-filtro-categoria") || {}).value || "";
    if (!ids.length) {
      var alcance = filtroCat ? "todos los productos de " + Cat.CATEGORIAS[filtroCat] : "todo tu catálogo";
      if (!confirm("No elegiste productos. ¿Aplicar a " + alcance + "?")) return;
      ids = MIS_PRODUCTOS.filter(function (p) { return !filtroCat || p.categoria === filtroCat; }).map(function (p) { return p.id; });
    }
    if (!ids.length) return;
    operacionRemota(servicio.cambiarDisponible(ids, disponible), ids.length + (disponible ? " productos disponibles" : " productos no disponibles")).then(function (r) {
      if (r.ok) seleccionGestion = {};
    });
  }

  /** Suma o resta una unidad (solo productos con una opción); no pisa ventas simultáneas. */
  function modificarStockProducto(prodId, delta) {
    var p = MIS_PRODUCTOS.find(function (x) { return x.id === prodId; });
    if (!p || p.variantes.length !== 1) { abrirModalPublicarProducto(prodId); return; }
    var nueva = Math.max(0, p.variantes[0].stock + delta);
    var form = Object.assign({}, p, { variantes: [Object.assign({}, p.variantes[0], { stock: nueva })] });
    operacionRemota(servicio.guardarProducto(Cat.productoParaGuardar(form, p)), "Cantidad actualizada");
  }

  function togglePublicadoProducto(prodId) {
    var p = MIS_PRODUCTOS.find(function (x) { return x.id === prodId; });
    if (!p) return;
    operacionRemota(servicio.cambiarPublicado(p.id, !p.publicado), p.publicado ? "Ahora solo tú lo ves" : "Ahora lo ven todos");
  }

  function editarPrecioProducto(prodId) {
    abrirModalPublicarProducto(prodId);
  }

  function eliminarProductoCatalogo(prodId) {
    var p = MIS_PRODUCTOS.find(function (x) { return x.id === prodId; });
    if (!p) return;
    if (!confirm("¿Borrar «" + p.nombre + "»? Se quitará de tu tienda y no se puede recuperar. Si solo se acabó, mejor márcalo como no disponible.")) return;
    operacionRemota(servicio.eliminarProducto(p.id), "Producto borrado");
  }

  function abrirModalEditarTienda() {
    var tienda = obtenerTiendaUsuarioActivo();
    if (!esUsuarioEmprendedora() || !tienda) {
      abrirModalAuthEmprendedora();
      mostrarToast("Inicia sesión como emprendedora para editar tu tienda.");
      return;
    }
    function poner(id, valor) { var el = document.getElementById(id); if (el) el.value = valor || ""; }
    function marcar(id, valor) { var el = document.getElementById(id); if (el) el.checked = !!valor; }
    poner("edit-tienda-nombre", tienda.nombre);
    poner("edit-tienda-rubro", tienda.rubro);
    poner("edit-tienda-sector", tienda.sector);
    poner("edit-tienda-whatsapp", tienda.whatsapp);
    poner("edit-tienda-bio", tienda.bio);
    var nec = tienda.necesidades || {};
    marcar("check-cap-stock", nec.tieneStock);
    marcar("check-cap-whatsapp", nec.tieneVentaPorWhatsApp);
    marcar("check-cap-pedido", nec.tieneProductosAPedido);
    marcar("check-cap-ferias", nec.participaEnFerias);
    marcar("check-cap-foro", nec.tieneForoComunidad);
    marcar("check-cap-agenda", nec.tieneAgendamiento);
    marcar("check-historial-ventas", tienda.historialActivo !== false);
    var v = valoresIniciales();
    poner("def-categoria", v.categoria);
    poner("def-modalidad", v.modalidad);
    poner("def-al-agotarse", v.alAgotarse);
    marcar("def-maneja-stock", v.manejaStock);
    marcar("def-publicado", v.publicado);
    mostrarAviso("edit-tienda-aviso", "");
    abrirModal("modal-editar-tienda");
  }

  function guardarPerfilTienda() {
    var tienda = obtenerTiendaUsuarioActivo();
    if (!tienda || !servicio) { mostrarAviso("edit-tienda-aviso", "Vuelve a iniciar sesión para guardar."); return; }
    function valor(id) { var el = document.getElementById(id); return el ? el.value.trim() : ""; }
    function marcado(id, porDefecto) { var el = document.getElementById(id); return el ? el.checked : porDefecto; }
    var cambios = {
      nombre: valor("edit-tienda-nombre") || tienda.nombre,
      rubro: valor("edit-tienda-rubro") || tienda.rubro,
      sector: valor("edit-tienda-sector") || tienda.sector,
      whatsapp: valor("edit-tienda-whatsapp"),
      bio: valor("edit-tienda-bio"),
      necesidades: {
        tieneStock: marcado("check-cap-stock", true),
        tieneVentaPorWhatsApp: marcado("check-cap-whatsapp", true),
        tieneProductosAPedido: marcado("check-cap-pedido", false),
        participaEnFerias: marcado("check-cap-ferias", true),
        tieneForoComunidad: marcado("check-cap-foro", false),
        tieneAgendamiento: marcado("check-cap-agenda", false)
      },
      historial_activo: marcado("check-historial-ventas", true),
      valores_producto: {
        categoria: valor("def-categoria") || "TEXTIL",
        modalidad: valor("def-modalidad") || "REGULAR",
        alAgotarse: valor("def-al-agotarse") || "MOSTRAR_AGOTADO",
        manejaStock: marcado("def-maneja-stock", true),
        publicado: marcado("def-publicado", false),
        disponible: true
      }
    };
    if (tienda.historialActivo !== false && cambios.historial_activo === false &&
        !confirm("Al desactivar el historial, las próximas ventas solo guardarán lo mínimo para descontar unidades (sin precio ni lugar). Las ventas anteriores no se borran. ¿Continuar?")) {
      return;
    }
    var boton = document.getElementById("btn-guardar-perfil-tienda");
    if (boton) { boton.disabled = true; boton.textContent = "Guardando…"; }
    mostrarAviso("edit-tienda-aviso", "");
    servicio.actualizarTienda(tienda.id, cambios).then(function (r) {
      if (!r.ok) { mostrarAviso("edit-tienda-aviso", "No se guardó: " + Cat.mensajeError(r.error)); return; }
      var actualizada = Cat.normalizarTienda(r.datos[0]);
      var idx = TIENDAS.findIndex(function (t) { return t.id === actualizada.id; });
      if (idx >= 0) TIENDAS[idx] = actualizada; else TIENDAS.push(actualizada);
      renderizarTiendasMarketplace();
      actualizarDashboardEmprendedora();
      cerrarModal("modal-editar-tienda");
      mostrarToast("Tienda guardada en el servidor.");
    }).then(function () {
      if (boton) { boton.disabled = false; boton.textContent = "Guardar cambios"; }
    });
  }

  var FERIAS_COMUNALES = [
    {
      id: "valle",
      nombre: "Feria de Emprendedoras del Valle",
      fecha: "Miércoles 7 de octubre de 2026 · 09:00 a 18:00 hrs",
      lugar: "Plaza de Armas, Puerto Montt",
      descripcion: "Gran muestra de productos artesanales, panadería, repostería y abarrotes típicos del sur.",
      participantes: ["t_minimarket_rosa", "t_tejidos_austral", "t_reposteria_surena"]
    },
    {
      id: "costumbrista",
      nombre: "Muestra Costumbrista Alerce Histórico",
      fecha: "Sábado 24 y domingo 25 de octubre · 11:00 a 20:00 hrs",
      lugar: "Parque Fundadores, Alerce Histórico",
      descripcion: "Encuentro vecinal con gastronomía típica, pan amasado caliente, cecinas artesanales y shows folclóricos.",
      participantes: ["t_minimarket_rosa", "t_barberia_alerce"]
    },
    {
      id: "norte",
      nombre: "Feria Campesina y Artesanal Alerce Norte",
      fecha: "Sábado 14 y domingo 15 de noviembre · 10:00 a 19:00 hrs",
      lugar: "Sede Social y Multicancha Alerce Norte",
      descripcion: "Feria de productos del campo, hortalizas frescas, tejidos a telar y servicios comunitarios.",
      participantes: ["t_tejidos_austral"]
    }
  ];

  function cargarFeriasDeStorage() {
    try {
      var guardadas = localStorage.getItem("trama_ferias_inscripciones");
      if (guardadas) {
        var map = JSON.parse(guardadas);
        FERIAS_COMUNALES.forEach(function (f) {
          if (map[f.id]) f.participantes = map[f.id];
        });
      }
    } catch(e) {}
  }
  cargarFeriasDeStorage();

  function abrirModalMisFerias() {
    if (!esUsuarioEmprendedora()) {
      abrirModalAuthEmprendedora();
      mostrarToast("🔒 Debes iniciar sesión como Emprendedor/a.");
      return;
    }
    renderizarMisFerias();
    abrirModal("modal-mis-ferias");
  }

  function renderizarMisFerias() {
    var cont = document.getElementById("lista-ferias-gestion-cuerpo");
    if (!cont) return;

    var tiendaId = estado.tiendaEmprendedoraId || "t_minimarket_rosa";
    var html = "";

    FERIAS_COMUNALES.forEach(function (feria) {
      var estaInscrita = feria.participantes.indexOf(tiendaId) !== -1;

      html += '<div class="tarjeta-feria-gestion ' + (estaInscrita ? 'inscrita' : '') + '">';
      html += '  <div style="flex: 1; min-width: 240px;">';
      html += '    <div style="display: flex; align-items: center; gap: 8px; margin-bottom: 4px;">';
      html += '      <strong style="font-size: 14.5px; color: var(--negro-apple);">' + escaparHtml(feria.nombre) + '</strong>';
      html += estaInscrita
        ? '      <span style="background: #dcfce7; color: #15803d; font-size: 10px; font-weight: 800; padding: 2px 8px; border-radius: 12px;">✅ Inscrita</span>'
        : '      <span style="background: #f4f4f5; color: #71717a; font-size: 10px; font-weight: 700; padding: 2px 8px; border-radius: 12px;">Disponible</span>';
      html += '    </div>';
      html += '    <p style="font-size: 12.5px; color: var(--rojo-alerce); font-weight: 700; margin: 2px 0;">📅 ' + escaparHtml(feria.fecha) + '</p>';
      html += '    <p style="font-size: 12px; color: var(--gris-texto); margin: 2px 0;">📍 ' + escaparHtml(feria.lugar) + '</p>';
      html += '    <p style="font-size: 12px; color: var(--gris-texto); margin-top: 6px; line-height: 1.4;">' + escaparHtml(feria.descripcion) + '</p>';
      html += '  </div>';

      html += '  <div style="text-align: right;">';
      if (estaInscrita) {
        html += '    <button type="button" class="btn-accion-pill" style="height: 34px; font-size: 11.5px; color: #dc2626; border-color: #fca5a5;" onclick="TramaWeb.toggleInscripcionFeria(\'' + feria.id + '\')">';
        html += '      Retirar Inscripción';
        html += '    </button>';
      } else {
        html += '    <button type="button" class="btn-accion-pill btn-negro" style="height: 34px; font-size: 11.5px;" onclick="TramaWeb.toggleInscripcionFeria(\'' + feria.id + '\')">';
        html += '      Inscribir a ' + tienda.nombre + ' ➔';
        html += '    </button>';
      }
      html += '  </div>';
      html += '</div>';
    });

    cont.innerHTML = html;
  }

  function toggleInscripcionFeria(feriaId) {
    var feria = FERIAS_COMUNALES.find(function (f) { return f.id === feriaId; });
    if (!feria) return;
    var tiendaId = estado.tiendaEmprendedoraId || "t_minimarket_rosa";
    var idx = feria.participantes.indexOf(tiendaId);

    if (idx !== -1) {
      feria.participantes.splice(idx, 1);
      mostrarToast("Inscripción retirada de " + feria.nombre);
    } else {
      feria.participantes.push(tiendaId);
      mostrarToast("¡Inscrita con éxito en " + feria.nombre + "!");
    }

    try {
      var map = {};
      FERIAS_COMUNALES.forEach(function (f) { map[f.id] = f.participantes; });
      localStorage.setItem("trama_ferias_inscripciones", JSON.stringify(map));
    } catch(e) {}

    renderizarMisFerias();

    var proximaEl = document.getElementById("texto-proxima-feria");
    if (proximaEl) {
      var inscrita = FERIAS_COMUNALES.find(function (f) { return f.participantes.indexOf(tiendaId) !== -1; });
      if (inscrita) {
        proximaEl.textContent = "Próxima feria: " + inscrita.nombre + " · Estado: Inscrita (Confirmada)";
      } else {
        proximaEl.textContent = "Sin inscripción activa en ferias comunales.";
      }
    }
  }


  // ============================================================
  // MÓDULO 11: ADMINISTRACIÓN DE PLATAFORMA (W01 a W24, WA3-WA6)
  // Requisitos Entrega 4: Canal exclusivo Web para el Administrador
  // ============================================================

  var ESTADO_ADMIN = {
    cuentasEmprendedoras: [
      {
        id: "emp_marta",
        nombre: "Doña Marta Sánchez",
        email: "donamarta@alerce.app",
        telefono: "+56 9 9123 4567",
        tiendaId: "t_minimarket_rosa",
        tiendaNombre: "Minimarket Doña Rosa",
        sector: "Alerce Histórico",
        asociacionId: "as1",
        asociacionNombre: "Manos del Sur",
        clave: "alerce123",
        activa: true,
        creadoEn: "2026-10-01"
      },
      {
        id: "emp_barberia",
        nombre: "Carlos Almonacid",
        email: "barberia@alerce.app",
        telefono: "+56 9 8765 4321",
        tiendaId: "t_barberia_alerce",
        tiendaNombre: "Barbería & Estilo Alerce",
        sector: "Alerce Norte",
        asociacionId: "as1",
        asociacionNombre: "Manos del Sur",
        clave: "alerce123",
        activa: true,
        creadoEn: "2026-10-02"
      },
      {
        id: "emp_tejidos",
        nombre: "Elena Soto",
        email: "tejidos@alerce.app",
        telefono: "+56 9 7654 3210",
        tiendaId: "t_tejidos_austral",
        tiendaNombre: "Tejidos & Lanas Austral",
        sector: "Alerce Sur",
        asociacionId: "as2",
        asociacionNombre: "Red del Lago",
        clave: "alerce123",
        activa: true,
        creadoEn: "2026-10-03"
      },
      {
        id: "emp_reposteria",
        nombre: "Clara Mansilla",
        email: "reposteria@alerce.app",
        telefono: "+56 9 6543 2109",
        tiendaId: "t_reposteria_surena",
        tiendaNombre: "Repostería Casera Tradición Sureña",
        sector: "Senda Central",
        asociacionId: "as3",
        asociacionNombre: "Taller Costero",
        clave: "alerce123",
        activa: true,
        creadoEn: "2026-10-04"
      }
    ],
    asociaciones: [
      {
        id: "as1",
        nombre: "Manos del Sur",
        localidad: "Puerto Montt",
        sector: "Alerce Histórico",
        descripcion: "Agrupación de artesanas y productoras dedicadas a oficios tradicionales y manufactura local.",
        contacto: "contacto@manosdelsur.cl / +56 9 6123 4500",
        logo: "https://images.unsplash.com/photo-1544717305-2782549b5136?w=200&auto=format&fit=crop&q=80",
        color: 4287523732,
        activa: true
      },
      {
        id: "as2",
        nombre: "Red del Lago",
        localidad: "Puerto Varas",
        sector: "Alerce Norte",
        descripcion: "Red colaborativa de emprendedoras gastronómicas, repostería casera y huertos comunitarios.",
        contacto: "reddellago@alerce.cl / +56 9 6123 4510",
        logo: "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=200&auto=format&fit=crop&q=80",
        color: 4282285422,
        activa: true
      },
      {
        id: "as3",
        nombre: "Taller Costero",
        localidad: "Frutillar",
        sector: "Senda Central",
        descripcion: "Talleristas de lana virgen chilota, telar decorativo, orfebrería y cosmética natural de bosque nativo.",
        contacto: "tallercostero@alerce.cl / +56 9 6123 4520",
        logo: "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=200&auto=format&fit=crop&q=80",
        color: 4290804286,
        activa: true
      }
    ],
    tiposEmprendimiento: [
      "Artesanía Tradicional",
      "Abarrotes y Alimentos",
      "Servicios Personales",
      "Pastelería y Gastronomía",
      "Cosmética y Belleza",
      "Confección y Textil",
      "Diseño y Decoración"
    ],
    tiposProducto: [
      "Producto Físico Artesanal",
      "Alimento Empacado / Fresco",
      "Prenda o Accesorio Textil",
      "Servicio con Turno / Cita",
      "Pieza Decorativa Única"
    ],
    categorias: [
      "Abarrotes y Alimentos",
      "Servicios y Peluquería",
      "Artesanía y Textil",
      "Pastelería y Gastronomía",
      "Joyería y Accesorios",
      "Decoración y Hogar"
    ],
    admins: [
      {
        id: "adm1",
        nombre: "Administración General Alerce",
        email: "admin@alerce.app",
        rol: "SUPERADMIN",
        activa: true,
        creadoEn: "2026-09-01"
      },
      {
        id: "adm2",
        nombre: "Coordinación Comunal Alerce",
        email: "coordinacion@alerce.app",
        rol: "ADMIN",
        activa: true,
        creadoEn: "2026-10-15"
      }
    ],
    bitacora: [
      {
        id: "log_1",
        fecha: "2026-11-03 21:00",
        admin: "admin@alerce.app",
        accion: "Inicio de Plataforma",
        detalle: "Sistema administrativo verificado para Entrega 4 (W01 a W24)."
      }
    ],
    tabActual: "emp",
    credencialesGeneradas: null
  };

  (function initEstadoAdmin() {
    try {
      if (typeof localStorage !== "undefined") {
        var raw = localStorage.getItem("alerce_admin_state_v1");
        if (raw) {
          var parsed = JSON.parse(raw);
          if (parsed && parsed.cuentasEmprendedoras) {
            ESTADO_ADMIN = parsed;
          }
        }
      }
    } catch (e) {
      console.warn("Aviso cargando estado admin:", e);
    }
  })();

  function persistirEstadoAdmin() {
    try {
      if (typeof localStorage !== "undefined") {
        localStorage.setItem("alerce_admin_state_v1", JSON.stringify(ESTADO_ADMIN));
      }
    } catch (e) {
      console.warn("Aviso persistiendo estado admin:", e);
    }
  }

  function registrarAccionBitacora(accion, detalle) {
    var adminActual = (estado.usuario && estado.usuario.email) || "admin@alerce.app";
    var fechaStr = new Date().toISOString().replace("T", " ").slice(0, 16);
    ESTADO_ADMIN.bitacora.unshift({
      id: "log_" + Date.now(),
      fecha: fechaStr,
      admin: adminActual,
      accion: accion,
      detalle: detalle
    });
    if (ESTADO_ADMIN.bitacora.length > 50) ESTADO_ADMIN.bitacora.pop();
    persistirEstadoAdmin();
    actualizarKPIsAdmin();
  }

  function actualizarKPIsAdmin() {
    var kpiEmp = document.getElementById("kpi-admin-total-emp");
    var kpiAsoc = document.getElementById("kpi-admin-total-asoc");
    var kpiCat = document.getElementById("kpi-admin-total-cat");
    var kpiLog = document.getElementById("kpi-admin-total-log");

    if (kpiEmp) kpiEmp.textContent = ESTADO_ADMIN.cuentasEmprendedoras.length;
    if (kpiAsoc) kpiAsoc.textContent = ESTADO_ADMIN.asociaciones.filter(function(a){ return a.activa; }).length;
    if (kpiCat) kpiCat.textContent = ESTADO_ADMIN.categorias.length;
    if (kpiLog) kpiLog.textContent = ESTADO_ADMIN.bitacora.length;
  }

  function abrirModalAdmin() {
    if (!esUsuarioAdmin()) {
      cambiarRolAuth('ADMIN');
      abrirModalAuth();
      mostrarToast("🔒 Acceso exclusivo para Administradores de Plataforma (W01).");
      return;
    }
    actualizarKPIsAdmin();
    seleccionarTabAdmin(ESTADO_ADMIN.tabActual || "emp");
    abrirModal("modal-admin-plataforma");
  }

  function esUsuarioAdmin() {
    return estado.usuario && (estado.usuario.rol === "Administrador" || estado.usuario.rol === "ADMIN");
  }

    function entrarModoAdmin() {
    if (!esUsuarioAdmin()) {
      mostrarToast("Inicia sesión con una cuenta administradora autorizada.");
      return;
    }
    aplicarUsuarioLocal("Administración Alerce", "Administrador");
    estado.usuario = estado.usuario || {};
    estado.usuario.rol = "Administrador";
    estado.usuario.email = "admin@alerce.app";
    cerrarModal("modal-auth-web");
    cerrarModal("modal-registro-emprendedor");

    // Desbloquear pestaña y sección en la página (sin modal emergente automático)
    var navAdmin = document.getElementById("item-nav-admin");
    if (navAdmin) navAdmin.style.display = "inline-block";
    var secAdmin = document.getElementById("admin-dashboard");
    if (secAdmin) secAdmin.style.display = "block";

    actualizarKPIsAdmin();
    renderizarAdminEmprendedoras();

    // Activar pestaña visual en el menú y desplazarse suavemente
    document.querySelectorAll(".menu-navegacion a").forEach(function (l) { l.classList.remove("activo"); });
    var linkAdmin = document.getElementById("link-nav-admin");
    if (linkAdmin) linkAdmin.classList.add("activo");

    mostrarToast("👑 Bienvenido, Administrador. Pestaña de administración desbloqueada.");

    if (secAdmin) {
      setTimeout(function() {
        if (secAdmin.scrollIntoView) secAdmin.scrollIntoView({ behavior: "smooth", block: "start" });
      }, 150);
    }
  }

  function seleccionarTabAdmin(tab) {
    ESTADO_ADMIN.tabActual = tab;
    var tabs = ["emp", "asoc", "cat", "adm", "log"];
    tabs.forEach(function(t) {
      var btn = document.getElementById("tab-btn-admin-" + t);
      var sec = document.getElementById("admin-sec-" + t);
      if (btn) {
        if (t === tab) {
          btn.className = "btn-accion-pill btn-negro";
          btn.style.background = "#111";
          btn.style.color = "#fff";
        } else {
          btn.className = "btn-accion-pill";
          btn.style.background = "transparent";
          btn.style.color = "";
        }
      }
      if (sec) {
        sec.style.display = (t === tab) ? "flex" : "none";
      }
    });

    if (tab === "emp") renderizarAdminEmprendedoras();
    else if (tab === "asoc") renderizarAdminAsociaciones();
    else if (tab === "cat") renderizarAdminCatalogos();
    else if (tab === "adm") renderizarAdminAdmins();
    else if (tab === "log") renderizarAdminBitacora();
  }

  function filtrarAdminEmprendedoras() {
    renderizarAdminEmprendedoras();
  }

  function renderizarAdminEmprendedoras() {
    var cont = document.getElementById("admin-lista-emp-contenedor");
    if (!cont) return;

    var busqEl = document.getElementById("admin-busq-emp");
    var busq = busqEl ? busqEl.value.trim().toLowerCase() : "";
    var filtroEstadoEl = document.getElementById("admin-filtro-estado-emp");
    var filtroEstado = filtroEstadoEl ? filtroEstadoEl.value : "TODAS";

    var lista = ESTADO_ADMIN.cuentasEmprendedoras.filter(function(e) {
      var cumpleBusq = !busq || 
        (e.nombre && e.nombre.toLowerCase().indexOf(busq) !== -1) ||
        (e.tiendaNombre && e.tiendaNombre.toLowerCase().indexOf(busq) !== -1) ||
        (e.email && e.email.toLowerCase().indexOf(busq) !== -1);
      
      var cumpleEstado = true;
      if (filtroEstado === "ACTIVAS") cumpleEstado = e.activa === true;
      if (filtroEstado === "DESACTIVADAS") cumpleEstado = e.activa === false;

      return cumpleBusq && cumpleEstado;
    });

    if (lista.length === 0) {
      cont.innerHTML = '<div style="text-align: center; padding: 24px; color: var(--gris-texto); font-size: 13px;">No se encontraron cuentas de emprendedoras con los filtros actuales.</div>';
      return;
    }

    var html = "";
    lista.forEach(function(emp) {
      var estadoBadge = emp.activa 
        ? '<span style="background: #dcfce7; color: #166534; font-size: 10.5px; font-weight: 800; padding: 2px 8px; border-radius: 12px;">🟢 ACTIVA</span>'
        : '<span style="background: #fee2e2; color: #991b1b; font-size: 10.5px; font-weight: 800; padding: 2px 8px; border-radius: 12px;">🔴 DESACTIVADA (W08)</span>';

      html += '<div style="background: #ffffff; border: 1px solid var(--gris-borde); border-radius: 12px; padding: 12px 14px; display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 10px;">';
      html += '  <div style="display: flex; align-items: center; gap: 12px;">';
      html += '    <div style="width: 38px; height: 38px; border-radius: 50%; background: #1e1e24; color: #fff; display: flex; align-items: center; justify-content: center; font-weight: 800; font-size: 14px;">' + (emp.nombre.charAt(0) || "E") + '</div>';
      html += '    <div>';
      html += '      <div style="display: flex; align-items: center; gap: 6px;">';
      html += '        <strong style="font-size: 13.5px; color: var(--negro-apple);">' + emp.nombre + '</strong>';
      html += '        ' + estadoBadge;
      html += '      </div>';
      html += '      <div style="font-size: 12px; color: var(--gris-texto); margin-top: 2px;">';
      html += '        🏪 <strong>' + emp.tiendaNombre + '</strong> · 📍 ' + (emp.sector || "Alerce") + ' · 🏛️ ' + (emp.asociacionNombre || "Sin asociación");
      html += '      </div>';
      html += '      <div style="font-size: 11.5px; color: #6b7280; margin-top: 1px;">';
      html += '        ✉️ ' + emp.email + ' &middot; 📞 ' + (emp.telefono || "Sin teléfono");
      html += '      </div>';
      html += '    </div>';
      html += '  </div>';

      html += '  <div style="display: flex; gap: 6px; align-items: center;">';
      html += '    <button type="button" class="btn-accion-pill" style="height: 32px; font-size: 11px; padding: 0 10px;" onclick="TramaWeb.mostrarCredencialesEmprendedora(\'' + emp.id + '\')" title="Ver credenciales (W05)">📋 Credenciales</button>';
      html += '    <button type="button" class="btn-accion-pill" style="height: 32px; font-size: 11px; padding: 0 10px;" onclick="TramaWeb.editarCuentaEmprendedoraAdmin(\'' + emp.id + '\')" title="Editar datos (W06)">✏️ Editar</button>';
      if (emp.activa) {
        html += '    <button type="button" class="btn-accion-pill" style="height: 32px; font-size: 11px; padding: 0 10px; background: #fee2e2; color: #dc2626; border-color: #fca5a5;" onclick="TramaWeb.toggleEstadoCuentaEmprendedora(\'' + emp.id + '\')" title="Desactivar cuenta (W08)">🚫 Desactivar</button>';
      } else {
        html += '    <button type="button" class="btn-accion-pill" style="height: 32px; font-size: 11px; padding: 0 10px; background: #dcfce7; color: #166534; border-color: #86efac;" onclick="TramaWeb.toggleEstadoCuentaEmprendedora(\'' + emp.id + '\')" title="Reactivar cuenta">✅ Activar</button>';
      }
      html += '  </div>';
      html += '</div>';
    });

    cont.innerHTML = html;
  }

  function abrirFormCrearEmprendedora() {
    var panel = document.getElementById("admin-form-emp-panel");
    var tit = document.getElementById("admin-form-emp-titulo");
    var idInput = document.getElementById("admin-form-emp-id");
    var inpNom = document.getElementById("admin-form-emp-nombre");
    var inpTienda = document.getElementById("admin-form-emp-tienda");
    var inpEmail = document.getElementById("admin-form-emp-email");
    var inpClave = document.getElementById("admin-form-emp-clave");
    var inpTel = document.getElementById("admin-form-emp-tel");
    var selAsoc = document.getElementById("admin-form-emp-asoc");

    if (idInput) idInput.value = "";
    if (tit) tit.textContent = "Crear Cuenta de Emprendedora (W04)";
    if (inpNom) inpNom.value = "";
    if (inpTienda) inpTienda.value = "";
    if (inpEmail) inpEmail.value = "";
    if (inpClave) inpClave.value = "alerce" + Math.floor(100 + Math.random() * 900);
    if (inpTel) inpTel.value = "+56 9 ";

    if (selAsoc) {
      selAsoc.innerHTML = '<option value="">Sin asociación asignada</option>' + 
        ESTADO_ADMIN.asociaciones.filter(function(a){ return a.activa; }).map(function(a) {
          return '<option value="' + a.id + '">' + a.nombre + ' (' + a.sector + ')</option>';
        }).join("");
    }

    if (panel) {
      panel.style.display = "flex";
      panel.scrollIntoView({ behavior: "smooth" });
    }
  }

  function cerrarFormEmprendedora() {
    var panel = document.getElementById("admin-form-emp-panel");
    if (panel) panel.style.display = "none";
  }

  function generarClaveAleatoriaEmp() {
    var inpClave = document.getElementById("admin-form-emp-clave");
    if (inpClave) inpClave.value = "alerce" + Math.floor(100 + Math.random() * 900);
  }

  function guardarCuentaEmprendedoraAdmin() {
    var idInput = document.getElementById("admin-form-emp-id");
    var inpNom = document.getElementById("admin-form-emp-nombre");
    var inpTienda = document.getElementById("admin-form-emp-tienda");
    var inpEmail = document.getElementById("admin-form-emp-email");
    var inpClave = document.getElementById("admin-form-emp-clave");
    var inpTel = document.getElementById("admin-form-emp-tel");
    var selSector = document.getElementById("admin-form-emp-sector");
    var selAsoc = document.getElementById("admin-form-emp-asoc");

    var id = idInput ? idInput.value.trim() : "";
    var nombre = inpNom ? inpNom.value.trim() : "";
    var tiendaNombre = inpTienda ? inpTienda.value.trim() : "";
    var email = inpEmail ? inpEmail.value.trim().toLowerCase() : "";
    var clave = inpClave ? inpClave.value.trim() : "alerce123";
    var tel = inpTel ? inpTel.value.trim() : "";
    var sector = selSector ? selSector.value : "Alerce Histórico";
    var asocId = selAsoc ? selAsoc.value : "";
    var asocObj = ESTADO_ADMIN.asociaciones.find(function(a){ return a.id === asocId; });
    var asocNombre = asocObj ? asocObj.nombre : "Sin asociación";

    if (!nombre || !tiendaNombre || !email) {
      alert("Por favor completa el nombre, tienda y correo de la emprendedora.");
      return;
    }

    if (id) {
      var emp = ESTADO_ADMIN.cuentasEmprendedoras.find(function(e){ return e.id === id; });
      if (emp) {
        emp.nombre = nombre;
        emp.tiendaNombre = tiendaNombre;
        emp.email = email;
        if (clave) emp.clave = clave;
        emp.telefono = tel;
        emp.sector = sector;
        emp.asociacionId = asocId;
        emp.asociacionNombre = asocNombre;

        var tObj = TIENDAS.find(function(t){ return t.id === emp.tiendaId; });
        if (tObj) {
          tObj.nombre = tiendaNombre;
          tObj.sector = sector;
          tObj.whatsapp = tel;
        }

        registrarAccionBitacora("Editó cuenta emprendedora", nombre + " (" + email + ")");
        persistirEstadoAdmin();
        cerrarFormEmprendedora();
        renderizarAdminEmprendedoras();
        mostrarToast("✅ Cuenta de " + nombre + " actualizada y guardada (W07).");
      }
    } else {
      var nuevoId = "emp_" + Date.now();
      var tiendaId = "t_" + tiendaNombre.toLowerCase().replace(/[^a-z0-9]/g, "_").slice(0, 20) + "_" + Math.floor(Math.random() * 100);

      var nuevaCuenta = {
        id: nuevoId,
        nombre: nombre,
        email: email,
        telefono: tel,
        tiendaId: tiendaId,
        tiendaNombre: tiendaNombre,
        sector: sector,
        asociacionId: asocId,
        asociacionNombre: asocNombre,
        clave: clave,
        activa: true,
        creadoEn: new Date().toISOString().slice(0, 10)
      };

      ESTADO_ADMIN.cuentasEmprendedoras.unshift(nuevaCuenta);

      TIENDAS.unshift({
        id: tiendaId,
        nombre: tiendaNombre,
        rubro: "Artesanía y Emprendimiento",
        sector: sector,
        bio: "Emprendimiento oficial registrado por la Administración de Alerce.",
        whatsapp: tel || "+56991234567",
        instagram: "",
        colorPortada: "#1A202C",
        colorLogo: "#2B6CB0",
        necesidades: { tieneStock: true, tieneVentaPorWhatsApp: true, participaEnFerias: true }
      });

      if (supabaseClient) {
        supabaseClient.from("cuentas_autorizadas").insert([{
          email: email,
          rol: "EMPRENDEDORA",
          nombre: nombre,
          tienda_id: tiendaId,
          asociacion_id: asocId || null
        }]).then(function(res) {
          console.log("Cuenta autorizada en Supabase:", res);
        }).catch(function(e){ console.warn("Aviso:", e); });
      }

      registrarAccionBitacora("Creó cuenta emprendedora (W04)", nombre + " <" + email + "> con tienda " + tiendaNombre);
      persistirEstadoAdmin();
      cerrarFormEmprendedora();
      renderizarAdminEmprendedoras();
      actualizarKPIsAdmin();

      mostrarCredencialesDatos(email, clave, nombre, tiendaNombre);
      mostrarToast("🚀 Cuenta creada. Credenciales generadas listas para entregar.");
    }
  }

  function mostrarCredencialesDatos(email, clave, nombre, tienda) {
    ESTADO_ADMIN.credencialesGeneradas = { email: email, clave: clave, nombre: nombre, tienda: tienda };
    var box = document.getElementById("admin-box-credenciales");
    var texto = document.getElementById("admin-texto-credenciales");
    if (texto) {
      texto.innerHTML = "<strong>Emprendedora:</strong> " + nombre + " (" + tienda + ")<br>" +
                        "<strong>Usuario / Correo:</strong> <code>" + email + "</code><br>" +
                        "<strong>Contraseña Inicial:</strong> <code>" + clave + "</code>";
    }
    if (box) {
      box.style.display = "block";
      box.scrollIntoView({ behavior: "smooth" });
    }
  }

  function mostrarCredencialesEmprendedora(id) {
    var emp = ESTADO_ADMIN.cuentasEmprendedoras.find(function(e){ return e.id === id; });
    if (!emp) return;
    mostrarCredencialesDatos(emp.email, emp.clave || "alerce123", emp.nombre, emp.tiendaNombre);
  }

  function copiarCredencialesGeneradas() {
    var c = ESTADO_ADMIN.credencialesGeneradas;
    if (!c) return;
    var texto = "Plataforma de Emprendedoras Alerce\\n" +
                "Hola " + (c.nombre || "") + ", aquí están tus credenciales de acceso:\\n" +
                "Usuario: " + c.email + "\\n" +
                "Contraseña: " + c.clave + "\\n" +
                "Ingresa en: https://agustin671.github.io/EmprendedoresPuertoMontt/";
    
    if (typeof navigator !== "undefined" && navigator.clipboard && navigator.clipboard.writeText) {
      navigator.clipboard.writeText(texto).then(function() {
        mostrarToast("📋 Credenciales copiadas al portapapeles con éxito (WA5).");
      });
    } else {
      prompt("Copia las credenciales manualmente:", texto);
    }
  }

  function editarCuentaEmprendedoraAdmin(id) {
    var emp = ESTADO_ADMIN.cuentasEmprendedoras.find(function(e){ return e.id === id; });
    if (!emp) return;

    abrirFormCrearEmprendedora();
    var tit = document.getElementById("admin-form-emp-titulo");
    var idInput = document.getElementById("admin-form-emp-id");
    var inpNom = document.getElementById("admin-form-emp-nombre");
    var inpTienda = document.getElementById("admin-form-emp-tienda");
    var inpEmail = document.getElementById("admin-form-emp-email");
    var inpClave = document.getElementById("admin-form-emp-clave");
    var inpTel = document.getElementById("admin-form-emp-tel");
    var selSector = document.getElementById("admin-form-emp-sector");
    var selAsoc = document.getElementById("admin-form-emp-asoc");

    if (tit) tit.textContent = "Editar Cuenta de Emprendedora (W06)";
    if (idInput) idInput.value = emp.id;
    if (inpNom) inpNom.value = emp.nombre;
    if (inpTienda) inpTienda.value = emp.tiendaNombre;
    if (inpEmail) inpEmail.value = emp.email;
    if (inpClave) inpClave.value = emp.clave || "alerce123";
    if (inpTel) inpTel.value = emp.telefono || "";
    if (selSector) selSector.value = emp.sector || "Alerce Histórico";
    if (selAsoc) selAsoc.value = emp.asociacionId || "";
  }

  function toggleEstadoCuentaEmprendedora(id) {
    var emp = ESTADO_ADMIN.cuentasEmprendedoras.find(function(e){ return e.id === id; });
    if (!emp) return;

    var accion = emp.activa ? "desactivar" : "activar";
    var seguro = confirm("¿Confirmas que deseas " + accion + " la cuenta de " + emp.nombre + " (" + emp.email + ")?\\n\\n" +
      (emp.activa ? "Al desactivarla, la emprendedora NO podrá iniciar sesión en la plataforma (W09)." : "Al activarla, podrá ingresar normalmente."));
    
    if (!seguro) return;

    emp.activa = !emp.activa;
    registrarAccionBitacora((emp.activa ? "Reactivó" : "Desactivó (W08)") + " cuenta emprendedora", emp.nombre + " <" + emp.email + ">");
    persistirEstadoAdmin();
    renderizarAdminEmprendedoras();
    actualizarKPIsAdmin();
    mostrarToast("Cuenta de " + emp.nombre + " " + (emp.activa ? "reactivada" : "desactivada") + " correctamente.");
  }

  function filtrarAdminAsociaciones() {
    renderizarAdminAsociaciones();
  }

  function renderizarAdminAsociaciones() {
    var cont = document.getElementById("admin-lista-asoc-contenedor");
    if (!cont) return;

    var busqEl = document.getElementById("admin-busq-asoc");
    var busq = busqEl ? busqEl.value.trim().toLowerCase() : "";

    var lista = ESTADO_ADMIN.asociaciones.filter(function(a) {
      return !busq || (a.nombre && a.nombre.toLowerCase().indexOf(busq) !== -1) ||
                      (a.sector && a.sector.toLowerCase().indexOf(busq) !== -1);
    });

    if (lista.length === 0) {
      cont.innerHTML = '<div style="grid-column: 1/-1; text-align: center; padding: 24px; color: var(--gris-texto); font-size: 13px;">No se encontraron asociaciones.</div>';
      return;
    }

    var html = "";
    lista.forEach(function(asoc) {
      var socias = ESTADO_ADMIN.cuentasEmprendedoras.filter(function(e){ return e.asociacionId === asoc.id; });
      var estadoBadge = asoc.activa 
        ? '<span style="background: #dcfce7; color: #166534; font-size: 10px; font-weight: 800; padding: 2px 7px; border-radius: 12px;">🟢 ACTIVA</span>'
        : '<span style="background: #fee2e2; color: #991b1b; font-size: 10px; font-weight: 800; padding: 2px 7px; border-radius: 12px;">🔴 DESACTIVADA (W17)</span>';

      html += '<div style="background: #fff; border: 1px solid var(--gris-borde); border-radius: 14px; padding: 14px; display: flex; flex-direction: column; justify-content: space-between; gap: 10px;">';
      html += '  <div>';
      html += '    <div style="display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 8px;">';
      html += '      <div style="display: flex; align-items: center; gap: 10px;">';
      html += '        <img src="' + (asoc.logo || "https://picsum.photos/seed/" + asoc.id + "/100/100") + '" alt="' + asoc.nombre + '" style="width: 40px; height: 40px; border-radius: 8px; object-fit: cover; border: 1px solid var(--gris-borde);">';
      html += '        <div>';
      html += '          <strong style="font-size: 14px; color: var(--negro-apple); display: block;">' + asoc.nombre + '</strong>';
      html += '          <span style="font-size: 11.5px; color: var(--gris-texto);">' + asoc.sector + '</span>';
      html += '        </div>';
      html += '      </div>';
      html += '      ' + estadoBadge;
      html += '    </div>';
      html += '    <p style="font-size: 12px; color: #4b5563; line-height: 1.4; margin: 6px 0 8px 0;">' + asoc.descripcion + '</p>';
      html += '    <div style="font-size: 11.5px; color: #6b7280; margin-bottom: 6px;">📞 ' + asoc.contacto + '</div>';
      html += '    <div style="font-size: 11.5px; background: #f8fafc; border-radius: 6px; padding: 6px 8px; color: #334155;">';
      html += '      👥 <strong>' + socias.length + ' emprendedoras asignadas:</strong> ' + (socias.map(function(s){ return s.nombre; }).join(", ") || "Ninguna aún.");
      html += '    </div>';
      html += '  </div>';

      html += '  <div style="display: flex; gap: 6px; flex-wrap: wrap; border-top: 1px solid var(--gris-borde); padding-top: 10px; margin-top: 4px;">';
      html += '    <button type="button" class="btn-accion-pill" style="height: 28px; font-size: 11px; padding: 0 8px;" onclick="TramaWeb.editarAsociacionAdmin(\'' + asoc.id + '\')">✏️ Editar (W16)</button>';
      html += '    <button type="button" class="btn-accion-pill" style="height: 28px; font-size: 11px; padding: 0 8px; background: #f0fdf4; color: #166534; border-color: #86efac;" onclick="TramaWeb.asignarEmprendedoraAsocPrompt(\'' + asoc.id + '\')">+ Asignar (W18)</button>';
      if (socias.length > 0) {
        html += '    <button type="button" class="btn-accion-pill" style="height: 28px; font-size: 11px; padding: 0 8px;" onclick="TramaWeb.quitarEmprendedoraAsocPrompt(\'' + asoc.id + '\')">- Quitar (W19)</button>';
      }
      if (asoc.activa) {
        html += '    <button type="button" class="btn-accion-pill" style="height: 28px; font-size: 11px; padding: 0 8px; background: #fee2e2; color: #991b1b; border-color: #fca5a5;" onclick="TramaWeb.toggleEstadoAsociacionAdmin(\'' + asoc.id + '\')">🚫 Desactivar (W17)</button>';
      } else {
        html += '    <button type="button" class="btn-accion-pill" style="height: 28px; font-size: 11px; padding: 0 8px; background: #dcfce7; color: #166534; border-color: #86efac;" onclick="TramaWeb.toggleEstadoAsociacionAdmin(\'' + asoc.id + '\')">✅ Activar</button>';
      }
      html += '  </div>';
      html += '</div>';
    });

    cont.innerHTML = html;
  }

  function abrirFormCrearAsociacion() {
    var panel = document.getElementById("admin-form-asoc-panel");
    var tit = document.getElementById("admin-form-asoc-titulo");
    var idInput = document.getElementById("admin-form-asoc-id");
    var inpNom = document.getElementById("admin-form-asoc-nombre");
    var inpSector = document.getElementById("admin-form-asoc-sector");
    var inpCont = document.getElementById("admin-form-asoc-contacto");
    var inpLogo = document.getElementById("admin-form-asoc-logo");
    var inpDesc = document.getElementById("admin-form-asoc-desc");

    if (idInput) idInput.value = "";
    if (tit) tit.textContent = "Crear Nueva Asociación (W10-W15)";
    if (inpNom) inpNom.value = "";
    if (inpSector) inpSector.value = "Alerce Histórico";
    if (inpCont) inpCont.value = "contacto@asociacion.cl / +56 9 6123 4500";
    if (inpLogo) inpLogo.value = "";
    if (inpDesc) inpDesc.value = "";

    if (panel) {
      panel.style.display = "flex";
      panel.scrollIntoView({ behavior: "smooth" });
    }
  }

  function cerrarFormAsociacion() {
    var panel = document.getElementById("admin-form-asoc-panel");
    if (panel) panel.style.display = "none";
  }

  function guardarAsociacionAdmin() {
    var idInput = document.getElementById("admin-form-asoc-id");
    var inpNom = document.getElementById("admin-form-asoc-nombre");
    var inpSector = document.getElementById("admin-form-asoc-sector");
    var inpCont = document.getElementById("admin-form-asoc-contacto");
    var inpLogo = document.getElementById("admin-form-asoc-logo");
    var inpDesc = document.getElementById("admin-form-asoc-desc");

    var id = idInput ? idInput.value.trim() : "";
    var nombre = inpNom ? inpNom.value.trim() : "";
    var sector = inpSector ? inpSector.value.trim() : "Alerce";
    var contacto = inpCont ? inpCont.value.trim() : "";
    var logo = inpLogo ? inpLogo.value.trim() : "";
    var desc = inpDesc ? inpDesc.value.trim() : "";

    if (!nombre || !desc) {
      alert("Por favor ingresa nombre y descripción de la asociación.");
      return;
    }

    if (id) {
      var asoc = ESTADO_ADMIN.asociaciones.find(function(a){ return a.id === id; });
      if (asoc) {
        asoc.nombre = nombre;
        asoc.sector = sector;
        asoc.contacto = contacto;
        if (logo) asoc.logo = logo;
        asoc.descripcion = desc;

        registrarAccionBitacora("Editó asociación (W16)", nombre + " (" + sector + ")");
        persistirEstadoAdmin();
        cerrarFormAsociacion();
        renderizarAdminAsociaciones();
        mostrarToast("✅ Asociación " + nombre + " actualizada con éxito (W16).");
      }
    } else {
      var nuevoId = "as_" + Date.now();
      var nuevaAsoc = {
        id: nuevoId,
        nombre: nombre,
        sector: sector,
        localidad: "Puerto Montt",
        contacto: contacto || "contacto@alerce.cl",
        logo: logo || ("https://picsum.photos/seed/" + nuevoId + "/200/200"),
        descripcion: desc,
        color: 4287523732,
        activa: true
      };

      ESTADO_ADMIN.asociaciones.push(nuevaAsoc);

      if (supabaseClient) {
        supabaseClient.from("asociaciones").insert([{
          id: nuevoId,
          nombre: nombre,
          localidad: sector,
          color: 4287523732
        }]).then(function(res){ console.log("Asociación en Supabase:", res); });
      }

      registrarAccionBitacora("Creó nueva asociación (W10)", nombre + " con ficha completa (W11-W15)");
      persistirEstadoAdmin();
      cerrarFormAsociacion();
      renderizarAdminAsociaciones();
      actualizarKPIsAdmin();
      mostrarToast("🏛️ Asociación " + nombre + " creada exitosamente.");
    }
  }

  function editarAsociacionAdmin(id) {
    var asoc = ESTADO_ADMIN.asociaciones.find(function(a){ return a.id === id; });
    if (!asoc) return;

    abrirFormCrearAsociacion();
    var tit = document.getElementById("admin-form-asoc-titulo");
    var idInput = document.getElementById("admin-form-asoc-id");
    var inpNom = document.getElementById("admin-form-asoc-nombre");
    var inpSector = document.getElementById("admin-form-asoc-sector");
    var inpCont = document.getElementById("admin-form-asoc-contacto");
    var inpLogo = document.getElementById("admin-form-asoc-logo");
    var inpDesc = document.getElementById("admin-form-asoc-desc");

    if (tit) tit.textContent = "Editar Asociación (W16)";
    if (idInput) idInput.value = asoc.id;
    if (inpNom) inpNom.value = asoc.nombre;
    if (inpSector) inpSector.value = asoc.sector;
    if (inpCont) inpCont.value = asoc.contacto;
    if (inpLogo) inpLogo.value = asoc.logo;
    if (inpDesc) inpDesc.value = asoc.descripcion;
  }

  function toggleEstadoAsociacionAdmin(id) {
    var asoc = ESTADO_ADMIN.asociaciones.find(function(a){ return a.id === id; });
    if (!asoc) return;

    var accion = asoc.activa ? "desactivar" : "activar";
    var seguro = confirm("¿Confirmas que deseas " + accion + " la asociación '" + asoc.nombre + "'?\\n\\n" +
      (asoc.activa ? "Tras desactivarla, no aparecerá como activa en los selectores de la vitrina (WA6)." : "Al reactivarla, quedará disponible para asignar socias."));
    
    if (!seguro) return;

    asoc.activa = !asoc.activa;
    registrarAccionBitacora((asoc.activa ? "Activó" : "Desactivó (W17)") + " asociación", asoc.nombre);
    persistirEstadoAdmin();
    renderizarAdminAsociaciones();
    actualizarKPIsAdmin();
    mostrarToast("Asociación '" + asoc.nombre + "' " + (asoc.activa ? "activada" : "desactivada") + " con éxito.");
  }

  function asignarEmprendedoraAsocPrompt(asocId) {
    var asoc = ESTADO_ADMIN.asociaciones.find(function(a){ return a.id === asocId; });
    if (!asoc) return;

    var candidatas = ESTADO_ADMIN.cuentasEmprendedoras.filter(function(e){ return e.asociacionId !== asocId; });
    if (candidatas.length === 0) {
      alert("Todas las emprendedoras registradas ya están asignadas a esta asociación.");
      return;
    }

    var opciones = candidatas.map(function(e, i) { return (i + 1) + ". " + e.nombre + " (" + e.tiendaNombre + ")"; }).join("\\n");
    var res = prompt("Selecciona el NÚMERO de la emprendedora para ASIGNAR a " + asoc.nombre + " (W18):\\n\\n" + opciones);
    if (!res) return;

    var idx = parseInt(res, 10) - 1;
    if (isNaN(idx) || idx < 0 || idx >= candidatas.length) {
      alert("Opción no válida.");
      return;
    }

    var seleccionada = candidatas[idx];
    seleccionada.asociacionId = asoc.id;
    seleccionada.asociacionNombre = asoc.nombre;

    registrarAccionBitacora("Asignó emprendedora a asociación (W18)", seleccionada.nombre + " -> " + asoc.nombre);
    persistirEstadoAdmin();
    renderizarAdminAsociaciones();
    renderizarAdminEmprendedoras();
    mostrarToast("✅ " + seleccionada.nombre + " asignada a " + asoc.nombre + " (W18).");
  }

  function quitarEmprendedoraAsocPrompt(asocId) {
    var asoc = ESTADO_ADMIN.asociaciones.find(function(a){ return a.id === asocId; });
    if (!asoc) return;

    var socias = ESTADO_ADMIN.cuentasEmprendedoras.filter(function(e){ return e.asociacionId === asocId; });
    if (socias.length === 0) return;

    var opciones = socias.map(function(e, i) { return (i + 1) + ". " + e.nombre; }).join("\\n");
    var res = prompt("Selecciona el NÚMERO de la emprendedora para QUITAR de " + asoc.nombre + " (W19):\\n\\n" + opciones);
    if (!res) return;

    var idx = parseInt(res, 10) - 1;
    if (isNaN(idx) || idx < 0 || idx >= socias.length) {
      alert("Opción no válida.");
      return;
    }

    var seleccionada = socias[idx];
    seleccionada.asociacionId = null;
    seleccionada.asociacionNombre = "Sin asociación";

    registrarAccionBitacora("Quitó emprendedora de asociación (W19)", seleccionada.nombre + " de " + asoc.nombre);
    persistirEstadoAdmin();
    renderizarAdminAsociaciones();
    renderizarAdminEmprendedoras();
    mostrarToast("Emprendedora " + seleccionada.nombre + " removida de " + asoc.nombre + ".");
  }

  function renderizarAdminCatalogos() {
    var ulEmp = document.getElementById("admin-lista-tipo-emp");
    var ulProd = document.getElementById("admin-lista-tipo-prod");
    var ulCat = document.getElementById("admin-lista-categorias");

    if (ulEmp) {
      ulEmp.innerHTML = ESTADO_ADMIN.tiposEmprendimiento.map(function(item, i) {
        return '<li style="display: flex; justify-content: space-between; align-items: center; padding: 6px 8px; background: #f8fafc; border-radius: 6px; font-size: 12.5px;">' +
          '<span>' + item + '</span>' +
          '<div style="display: flex; gap: 4px;">' +
          '  <button type="button" style="background: none; border: none; cursor: pointer; font-size: 11px;" onclick="TramaWeb.editarCatalogoItem(\'tipoEmp\', ' + i + ')" title="Editar (W20)">✏️</button>' +
          '  <button type="button" style="background: none; border: none; cursor: pointer; font-size: 11px;" onclick="TramaWeb.eliminarCatalogoItem(\'tipoEmp\', ' + i + ')" title="Eliminar">🗑️</button>' +
          '</div>' +
        '</li>';
      }).join("");
    }

    if (ulProd) {
      ulProd.innerHTML = ESTADO_ADMIN.tiposProducto.map(function(item, i) {
        return '<li style="display: flex; justify-content: space-between; align-items: center; padding: 6px 8px; background: #f8fafc; border-radius: 6px; font-size: 12.5px;">' +
          '<span>' + item + '</span>' +
          '<div style="display: flex; gap: 4px;">' +
          '  <button type="button" style="background: none; border: none; cursor: pointer; font-size: 11px;" onclick="TramaWeb.editarCatalogoItem(\'tipoProd\', ' + i + ')" title="Editar (W21)">✏️</button>' +
          '  <button type="button" style="background: none; border: none; cursor: pointer; font-size: 11px;" onclick="TramaWeb.eliminarCatalogoItem(\'tipoProd\', ' + i + ')" title="Eliminar">🗑️</button>' +
          '</div>' +
        '</li>';
      }).join("");
    }

    if (ulCat) {
      ulCat.innerHTML = ESTADO_ADMIN.categorias.map(function(item, i) {
        return '<li style="display: flex; justify-content: space-between; align-items: center; padding: 6px 8px; background: #f8fafc; border-radius: 6px; font-size: 12.5px;">' +
          '<span>' + item + '</span>' +
          '<div style="display: flex; gap: 4px;">' +
          '  <button type="button" style="background: none; border: none; cursor: pointer; font-size: 11px;" onclick="TramaWeb.editarCatalogoItem(\'categoria\', ' + i + ')" title="Editar (W22)">✏️</button>' +
          '  <button type="button" style="background: none; border: none; cursor: pointer; font-size: 11px;" onclick="TramaWeb.eliminarCatalogoItem(\'categoria\', ' + i + ')" title="Eliminar">🗑️</button>' +
          '</div>' +
        '</li>';
      }).join("");
    }
  }

  function agregarCatalogoItem(tipo) {
    var titulos = {
      tipoEmp: "Nuevo Tipo de Emprendimiento (W20)",
      tipoProd: "Nuevo Tipo de Producto (W21)",
      categoria: "Nueva Categoría Comercial (W22)"
    };
    var valor = prompt(titulos[tipo] + ":");
    if (!valor || !valor.trim()) return;

    valor = valor.trim();
    if (tipo === "tipoEmp") ESTADO_ADMIN.tiposEmprendimiento.push(valor);
    if (tipo === "tipoProd") ESTADO_ADMIN.tiposProducto.push(valor);
    if (tipo === "categoria") ESTADO_ADMIN.categorias.push(valor);

    registrarAccionBitacora("Añadió elemento de catálogo (" + tipo + ")", valor);
    persistirEstadoAdmin();
    renderizarAdminCatalogos();
    actualizarKPIsAdmin();
    mostrarToast("✅ '" + valor + "' agregado al catálogo.");
  }

  function editarCatalogoItem(tipo, index) {
    var arrayActual = tipo === "tipoEmp" ? ESTADO_ADMIN.tiposEmprendimiento : (tipo === "tipoProd" ? ESTADO_ADMIN.tiposProducto : ESTADO_ADMIN.categorias);
    if (!arrayActual || index < 0 || index >= arrayActual.length) return;

    var actual = arrayActual[index];
    var nuevo = prompt("Modificar valor:", actual);
    if (!nuevo || !nuevo.trim() || nuevo.trim() === actual) return;

    arrayActual[index] = nuevo.trim();
    registrarAccionBitacora("Editó elemento catálogo", actual + " -> " + nuevo.trim());
    persistirEstadoAdmin();
    renderizarAdminCatalogos();
    mostrarToast("Catálogo actualizado.");
  }

  function eliminarCatalogoItem(tipo, index) {
    var arrayActual = tipo === "tipoEmp" ? ESTADO_ADMIN.tiposEmprendimiento : (tipo === "tipoProd" ? ESTADO_ADMIN.tiposProducto : ESTADO_ADMIN.categorias);
    if (!arrayActual || index < 0 || index >= arrayActual.length) return;

    var item = arrayActual[index];
    if (!confirm("¿Eliminar '" + item + "' del catálogo?")) return;

    arrayActual.splice(index, 1);
    registrarAccionBitacora("Eliminó elemento catálogo", item);
    persistirEstadoAdmin();
    renderizarAdminCatalogos();
    actualizarKPIsAdmin();
    mostrarToast("Elemento eliminado.");
  }

  function renderizarAdminAdmins() {
    var cont = document.getElementById("admin-lista-admins-contenedor");
    if (!cont) return;

    var html = "";
    ESTADO_ADMIN.admins.forEach(function(adm) {
      var badge = adm.activa
        ? '<span style="background: #dcfce7; color: #166534; font-size: 10.5px; font-weight: 800; padding: 2px 7px; border-radius: 12px;">🟢 ACTIVA</span>'
        : '<span style="background: #fee2e2; color: #991b1b; font-size: 10.5px; font-weight: 800; padding: 2px 7px; border-radius: 12px;">🔴 SUSPENDIDA</span>';

      html += '<div style="background: #fff; border: 1px solid var(--gris-borde); border-radius: 12px; padding: 12px 14px; display: flex; justify-content: space-between; align-items: center;">';
      html += '  <div>';
      html += '    <div style="display: flex; align-items: center; gap: 8px;">';
      html += '      <strong style="font-size: 13.5px; color: var(--negro-apple);">' + adm.nombre + '</strong>';
      html += '      ' + badge;
      html += '    </div>';
      html += '    <span style="font-size: 12px; color: var(--gris-texto);">' + adm.email + ' &middot; Rol: ' + adm.rol + '</span>';
      html += '  </div>';
      html += '  <div style="display: flex; gap: 6px;">';
      if (adm.email !== "admin@alerce.app") {
        html += '    <button type="button" class="btn-accion-pill" style="height: 30px; font-size: 11px;" onclick="TramaWeb.toggleEstadoAdmin(\'' + adm.id + '\')">' + (adm.activa ? "Desactivar" : "Activar") + '</button>';
      } else {
        html += '    <span style="font-size: 11px; color: var(--gris-texto); font-weight: 700;">Superadmin Principal</span>';
      }
      html += '  </div>';
      html += '</div>';
    });

    cont.innerHTML = html;
  }

  function abrirFormCrearAdmin() {
    var nombre = prompt("Nombre del nuevo administrador:");
    if (!nombre || !nombre.trim()) return;
    var email = prompt("Correo del nuevo administrador (ej: soporte@alerce.app):");
    if (!email || !email.trim()) return;

    var nuevo = {
      id: "adm_" + Date.now(),
      nombre: nombre.trim(),
      email: email.trim().toLowerCase(),
      rol: "ADMIN",
      activa: true,
      creadoEn: new Date().toISOString().slice(0, 10)
    };

    ESTADO_ADMIN.admins.push(nuevo);
    registrarAccionBitacora("Creó cuenta de administrador (W23)", nombre + " <" + email + ">");
    persistirEstadoAdmin();
    renderizarAdminAdmins();
    mostrarToast("👑 Administrador creado con éxito (W23).");
  }

  function toggleEstadoAdmin(id) {
    var adm = ESTADO_ADMIN.admins.find(function(a){ return a.id === id; });
    if (!adm) return;

    if (!confirm("¿Deseas cambiar el estado de la cuenta de " + adm.nombre + "?")) return;
    adm.activa = !adm.activa;
    registrarAccionBitacora((adm.activa ? "Activó" : "Desactivó") + " cuenta admin", adm.email);
    persistirEstadoAdmin();
    renderizarAdminAdmins();
    mostrarToast("Cuenta de administrador actualizada.");
  }

  function renderizarAdminBitacora() {
    var tbody = document.getElementById("admin-tabla-bitacora-body");
    if (!tbody) return;

    if (ESTADO_ADMIN.bitacora.length === 0) {
      tbody.innerHTML = '<tr><td colspan="4" style="text-align: center; padding: 18px; color: var(--gris-texto);">No hay eventos en la bitácora.</td></tr>';
      return;
    }

    var html = "";
    ESTADO_ADMIN.bitacora.forEach(function(item) {
      html += '<tr style="border-bottom: 1px solid var(--gris-borde);">';
      html += '  <td style="padding: 9px 12px; font-family: monospace; font-size: 11.5px; color: #4b5563;">' + item.fecha + '</td>';
      html += '  <td style="padding: 9px 12px; font-weight: 700; color: #1e1e24;">' + item.admin + '</td>';
      html += '  <td style="padding: 9px 12px;"><span style="background: #f3f4f6; padding: 2px 6px; border-radius: 4px; font-weight: 600;">' + item.accion + '</span></td>';
      html += '  <td style="padding: 9px 12px; color: #374151;">' + item.detalle + '</td>';
      html += '</tr>';
    });

    tbody.innerHTML = html;
  }

  function mostrarAviso(id, mensaje) {
    var el = document.getElementById(id);
    if (!el) return;
    el.textContent = mensaje || "";
    el.style.display = mensaje ? "block" : "none";
  }

  function recuperarContrasenaWeb() {
    var inpEmail = document.getElementById("auth-inp-email");
    var emailInicial = inpEmail ? inpEmail.value.trim() : "";
    var campo = document.getElementById("recuperar-email");
    if (campo && emailInicial) campo.value = emailInicial;
    mostrarAviso("recuperar-aviso-error", "");
    mostrarAviso("recuperar-aviso-exito", "");
    cerrarModal("modal-auth-web");
    abrirModal("modal-recuperar-contrasena");
  }

  function enviarEnlaceRecuperacion() {
    var campo = document.getElementById("recuperar-email");
    var email = campo ? campo.value.trim().toLowerCase() : "";
    var btn = document.getElementById("btn-enviar-recuperacion");
    mostrarAviso("recuperar-aviso-error", "");
    mostrarAviso("recuperar-aviso-exito", "");

    if (!email || email.indexOf("@") === -1) {
      mostrarAviso("recuperar-aviso-error", "Escribe un correo electrónico válido.");
      return;
    }
    if (!supabaseClient || !supabaseClient.auth) {
      mostrarAviso("recuperar-aviso-error", "No se pudo conectar con el servicio de correo. Intenta más tarde.");
      return;
    }

    if (btn) { btn.disabled = true; btn.textContent = "Enviando enlace..."; }
    var redireccion = window.location.origin + window.location.pathname + "?recuperar=1";

    supabaseClient.auth.resetPasswordForEmail(email, { redirectTo: redireccion }).then(function (res) {
      if (res && res.error) throw res.error;
      mostrarAviso("recuperar-aviso-exito", "Si el correo está registrado, recibirás un enlace para crear una nueva contraseña. Revisa también tu carpeta de spam.");
      mostrarToast("📧 Solicitud de recuperación enviada.");
    }).catch(function (e) {
      mostrarAviso("recuperar-aviso-error", "No se pudo enviar el enlace: " + (e.message || e));
    }).then(function () {
      if (btn) { btn.disabled = false; btn.textContent = "✉️ Enviar enlace de recuperación"; }
    });
  }

  function guardarNuevaClave() {
    var campo1 = document.getElementById("nueva-clave-input");
    var campo2 = document.getElementById("nueva-clave-confirmar");
    var btn = document.getElementById("btn-guardar-nueva-clave");
    var clave1 = campo1 ? campo1.value : "";
    var clave2 = campo2 ? campo2.value : "";
    mostrarAviso("cambiar-clave-aviso-error", "");
    mostrarAviso("cambiar-clave-aviso-exito", "");

    if (!clave1 || clave1.length < 6) {
      mostrarAviso("cambiar-clave-aviso-error", "La contraseña debe tener al menos 6 caracteres.");
      return;
    }
    if (clave1 !== clave2) {
      mostrarAviso("cambiar-clave-aviso-error", "Las contraseñas no coinciden.");
      return;
    }
    if (!supabaseClient || !supabaseClient.auth) {
      mostrarAviso("cambiar-clave-aviso-error", "No se pudo conectar con el servicio de autenticación.");
      return;
    }

    if (btn) { btn.disabled = true; btn.textContent = "Guardando contraseña..."; }
    supabaseClient.auth.updateUser({ password: clave1 }).then(function (res) {
      if (res && res.error) throw res.error;
      return supabaseClient.auth.signOut();
    }).then(function (res) {
      if (res && res.error) throw res.error;
      mostrarAviso("cambiar-clave-aviso-exito", "Contraseña actualizada. Ya puedes usar tu nueva clave.");
      mostrarToast("🔒 Contraseña actualizada correctamente.");
      if (campo1) campo1.value = "";
      if (campo2) campo2.value = "";
      try {
        var limpia = window.location.origin + window.location.pathname;
        window.history.replaceState({}, document.title, limpia);
      } catch (e) {}
      setTimeout(function () {
        cerrarModal("modal-cambiar-clave");
        abrirModalAuthNormal();
      }, 1400);
    }).catch(function (e) {
      mostrarAviso("cambiar-clave-aviso-error", "No se pudo cambiar la contraseña: " + (e.message || e));
    }).then(function () {
      if (btn) { btn.disabled = false; btn.textContent = "🔒 Guardar nueva contraseña"; }
    });
  }

  function etiquetaRol(rol) {
    var valor = String(rol || "").toUpperCase();
    if (valor.indexOf("ADMIN") !== -1) return "Administrador";
    if (valor.indexOf("EMPREND") !== -1) return "Emprendedor/a";
    return "Comprador";
  }

  function pintarAvatarPreview(idCaja, idImg, idIniciales, nombre, fotoUrl) {
    var caja = document.getElementById(idCaja);
    var img = document.getElementById(idImg);
    var iniciales = document.getElementById(idIniciales);
    if (caja) caja.style.backgroundColor = "#cbd5e1";
    if (iniciales) iniciales.textContent = inicialesDeNombre(nombre);
    if (!img) return;
    img.onerror = function () {
      img.style.display = "none";
      if (iniciales) iniciales.style.display = "inline-block";
    };
    if (fotoUrl) {
      img.src = fotoUrl;
      img.style.display = "block";
      if (iniciales) iniciales.style.display = "none";
    } else {
      img.style.display = "none";
      img.removeAttribute("src");
      if (iniciales) iniciales.style.display = "inline-block";
    }
  }

  function cargarPerfilEnModal() {
    var u = estado.usuario || {};
    var nombre = u.nombre || "Usuario";
    var email = u.email || "";
    var tiendaId = u.tiendaId || estado.tiendaEmprendedoraId || null;
    var tienda = tiendaId ? TIENDAS.find(function (t) { return t.id === tiendaId; }) : null;

    var nombreEl = document.getElementById("perfil-info-nombre");
    var emailEl = document.getElementById("perfil-info-email");
    var rolEl = document.getElementById("perfil-info-rol-badge");
    var inputNombre = document.getElementById("perfil-input-nombre");
    if (nombreEl) nombreEl.textContent = nombre;
    if (emailEl) emailEl.textContent = email || "Sin correo registrado";
    if (rolEl) rolEl.textContent = etiquetaRol(u.rol);
    if (inputNombre) inputNombre.value = nombre;

    pintarAvatarPreview("perfil-avatar-preview", "perfil-img-avatar", "perfil-iniciales-avatar", nombre, u.fotoUrl);

    var seccionTienda = document.getElementById("perfil-seccion-tienda");
    if (seccionTienda) seccionTienda.style.display = tienda ? "flex" : "none";
    if (tienda) {
      var nombreTienda = document.getElementById("perfil-tienda-nombre");
      if (nombreTienda) nombreTienda.textContent = tienda.nombre;
      pintarAvatarPreview("perfil-logo-preview", "perfil-img-logo", "perfil-iniciales-logo", tienda.nombre, tienda.logoUrl);
    }

    var inputAvatar = document.getElementById("perfil-input-avatar");
    var inputLogo = document.getElementById("perfil-input-logo");
    if (inputAvatar) inputAvatar.value = "";
    if (inputLogo) inputLogo.value = "";
    mostrarAviso("perfil-aviso-error", "");
    mostrarAviso("perfil-aviso-exito", "");
  }

  function gestionarClickUsuarioHeader() {
    var u = estado.usuario || {};
    if (u.nombre && u.nombre !== "Visitante") {
      cargarPerfilEnModal();
      abrirModal("modal-mi-perfil");
    } else {
      abrirModalAuthNormal();
    }
  }

  function guardarCambiosMiPerfil() {
    var u = estado.usuario || {};
    var btn = document.getElementById("btn-guardar-perfil");
    var inputNombre = document.getElementById("perfil-input-nombre");
    var inputAvatar = document.getElementById("perfil-input-avatar");
    var inputLogo = document.getElementById("perfil-input-logo");
    var nombre = inputNombre ? inputNombre.value.trim() : "";
    var archivoAvatar = (inputAvatar && inputAvatar.files && inputAvatar.files[0]) ? inputAvatar.files[0] : null;
    var archivoLogo = (inputLogo && inputLogo.files && inputLogo.files[0]) ? inputLogo.files[0] : null;
    var tiendaId = u.tiendaId || estado.tiendaEmprendedoraId || null;

    mostrarAviso("perfil-aviso-error", "");
    mostrarAviso("perfil-aviso-exito", "");

    if (!nombre) {
      mostrarAviso("perfil-aviso-error", "Escribe tu nombre.");
      return;
    }
    if (!u.id) {
      mostrarAviso("perfil-aviso-error", "Vuelve a iniciar sesión para guardar los cambios.");
      return;
    }
    if (!supabaseClient || !supabaseClient.auth) {
      mostrarAviso("perfil-aviso-error", "No se pudo conectar con el servicio de cuentas.");
      return;
    }

    if (btn) { btn.disabled = true; btn.textContent = "Guardando cambios..."; }

    function subirAvatarSiCorresponde() {
      if (!archivoAvatar) return Promise.resolve(u.fotoUrl || "");
      var ext = (archivoAvatar.name.split(".").pop() || "jpg").toLowerCase();
      var ruta = u.id + "/avatar_" + Date.now() + "." + ext;
      return subirArchivoSupabase(BUCKET_PERFILES, ruta, archivoAvatar);
    }

    function actualizarLogoSiCorresponde() {
      if (!archivoLogo || !tiendaId) return Promise.resolve(null);
      var ext = (archivoLogo.name.split(".").pop() || "jpg").toLowerCase();
      var ruta = u.id + "/logo_tienda_" + tiendaId + "_" + Date.now() + "." + ext;
      return subirArchivoSupabase(BUCKET_FOTOS, ruta, archivoLogo).then(function (urlLogo) {
        return supabaseClient.from("tiendas").update({ logo_url: urlLogo }).eq("id", tiendaId).then(function (resTienda) {
          if (resTienda && resTienda.error) throw resTienda.error;
          return urlLogo;
        });
      });
    }

    Promise.all([subirAvatarSiCorresponde(), actualizarLogoSiCorresponde()]).then(function (resultados) {
      var fotoUrl = resultados[0] || "";
      var logoUrl = resultados[1] || null;
      return supabaseClient.rpc("actualizar_mi_perfil", {
        p_nombre: nombre,
        p_foto_url: fotoUrl || null
      }).then(function (resPerfil) {
        if (resPerfil && resPerfil.error) throw resPerfil.error;
        var etiqueta = etiquetaRol(u.rol);
        estado.usuario.nombre = nombre;
        estado.usuario.fotoUrl = fotoUrl;
        aplicarUsuarioLocal(nombre, etiqueta, fotoUrl);
        if (logoUrl && tiendaId) {
          var tienda = TIENDAS.find(function (t) { return t.id === tiendaId; });
          if (tienda) tienda.logoUrl = logoUrl;
          renderizarTiendasMarketplace();
        }
        cargarPerfilEnModal();
        mostrarAviso("perfil-aviso-exito", "Perfil actualizado correctamente.");
        mostrarToast("✅ Perfil actualizado.");
      });
    }).catch(function (e) {
      mostrarAviso("perfil-aviso-error", "No se pudieron guardar los cambios: " + (e.message || e));
    }).then(function () {
      if (btn) { btn.disabled = false; btn.textContent = "💾 Guardar Cambios"; }
    });
  }

  /**
   * Acceso real con Google mediante Supabase Auth. Si el proveedor no está
   * habilitado en el servidor se informa; nunca se simula una sesión.
   */
  function iniciarSesionGoogleWeb() {
    mostrarAviso("auth-aviso-error", "");
    if (!supabaseClient || !SUPABASE_URL) {
      mostrarAviso("auth-aviso-error", "El acceso con Google no está disponible: la web no está conectada al servidor.");
      return;
    }
    fetch(SUPABASE_URL + "/auth/v1/settings", { headers: { apikey: SUPABASE_ANON_KEY } }).then(function (r) {
      return r.ok ? r.json() : null;
    }).catch(function () { return null; }).then(function (ajustes) {
      if (!ajustes) { mostrarAviso("auth-aviso-error", "No se pudo consultar el servidor de cuentas. Intenta más tarde."); return; }
      if (!ajustes.external || !ajustes.external.google) {
        mostrarAviso("auth-aviso-error", "El acceso con Google aún no está habilitado en el servidor. Usa tu correo y contraseña.");
        return;
      }
      return supabaseClient.auth.signInWithOAuth({
        provider: "google",
        options: { redirectTo: window.location.origin + window.location.pathname }
      }).then(function (res) {
        if (res && res.error) mostrarAviso("auth-aviso-error", "No se pudo iniciar el acceso con Google: " + Cat.mensajeError(res.error));
      });
    });
  }

  // Exponer API global para callbacks en línea
  
  // ============================================================
  // MÓDULO 12: MODALES SEPARADOS EMPRENDEDOR / USUARIO NORMAL
  // ============================================================

  function abrirModalRegistroEmprendedor() {
    cerrarModal("modal-auth-web");
    abrirModal("modal-registro-emprendedor");
  }

  function abrirModalAuthNormal() {
    cerrarModal("modal-registro-emprendedor");
    seleccionarModoUsuario("LOGIN");
    abrirModal("modal-auth-web");
  }

  function seleccionarModoUsuario(modo) {
    var secLogin = document.getElementById("usuario-seccion-login");
    var secReg = document.getElementById("usuario-seccion-registro");
    var btnLogin = document.getElementById("btn-tab-usuario-login");
    var btnReg = document.getElementById("btn-tab-usuario-registro");
    var errLogin = document.getElementById("auth-aviso-error");
    var errReg = document.getElementById("reg-usuario-aviso-error");
    if (errLogin) errLogin.style.display = "none";
    if (errReg) errReg.style.display = "none";

    if (modo === "REGISTRO") {
      if (secLogin) secLogin.style.display = "none";
      if (secReg) secReg.style.display = "flex";
      if (btnLogin) {
        btnLogin.className = "btn-accion-pill";
        btnLogin.style.background = "transparent";
        btnLogin.style.border = "none";
        btnLogin.style.color = "";
      }
      if (btnReg) {
        btnReg.className = "btn-accion-pill btn-negro";
        btnReg.style.background = "#2563eb";
        btnReg.style.border = "none";
        btnReg.style.color = "#ffffff";
      }
    } else {
      if (secLogin) secLogin.style.display = "flex";
      if (secReg) secReg.style.display = "none";
      if (btnLogin) {
        btnLogin.className = "btn-accion-pill btn-negro";
        btnLogin.style.background = "";
        btnLogin.style.border = "";
        btnLogin.style.color = "";
      }
      if (btnReg) {
        btnReg.className = "btn-accion-pill";
        btnReg.style.background = "transparent";
        btnReg.style.border = "none";
        btnReg.style.color = "";
      }
    }
  }

  function registrarUsuarioNormalModal() {
    var inpNom = document.getElementById("reg-usuario-nombre");
    var inpMail = document.getElementById("reg-usuario-email");
    var inpPass = document.getElementById("reg-usuario-password");
    var inpAvatar = document.getElementById("reg-usuario-avatar");
    var errDiv = document.getElementById("reg-usuario-aviso-error");
    var okDiv = document.getElementById("reg-usuario-aviso-exito");
    var btn = document.getElementById("btn-submit-reg-usuario");

    var nombre = inpNom ? inpNom.value.trim() : "";
    var email = inpMail ? inpMail.value.trim().toLowerCase() : "";
    var pass = inpPass ? inpPass.value : "";
    var archivoAvatar = (inpAvatar && inpAvatar.files && inpAvatar.files[0]) ? inpAvatar.files[0] : null;

    function mostrarError(msg) {
      if (errDiv) { errDiv.textContent = msg; errDiv.style.display = "block"; }
      if (okDiv) okDiv.style.display = "none";
      if (btn) { btn.disabled = false; btn.textContent = "Crear mi Cuenta de Usuario"; }
    }

    if (!nombre || !email || !pass) {
      mostrarError("Por favor completa todos los campos obligatorios.");
      return;
    }
    if (email.indexOf("@") === -1) {
      mostrarError("Escribe un correo electrónico válido.");
      return;
    }
    if (pass.length < 6) {
      mostrarError("La contraseña debe tener al menos 6 caracteres.");
      return;
    }

    if (errDiv) errDiv.style.display = "none";
    if (okDiv) okDiv.style.display = "none";
    if (btn) { btn.disabled = true; btn.textContent = "Creando tu cuenta..."; }

    if (!supabaseClient || !supabaseClient.auth) {
      mostrarError("El servicio de cuentas no está configurado. Intenta más tarde.");
      return;
    }

    supabaseClient.auth.signUp({
      email: email,
      password: pass,
      options: { data: { nombre: nombre } }
    }).then(function (res) {
      if (res && res.error) throw res.error;
      var user = res.data ? res.data.user : null;
      if (!user) throw new Error("No se pudo obtener el usuario registrado.");
      var session = res.data ? res.data.session : null;

      if (!session) {
        if (okDiv) {
          okDiv.textContent = "Cuenta creada. Revisa tu correo para confirmar y luego inicia sesión.";
          okDiv.style.display = "block";
        }
        if (btn) { btn.disabled = false; btn.textContent = "Crear mi Cuenta de Usuario"; }
        return;
      }

      setTimeout(function () {
        var subida = archivoAvatar
          ? (function () {
              var ruta = user.id + "/avatar_" + Date.now() + ".jpg";
              return subirArchivoSupabase(BUCKET_PERFILES, ruta, archivoAvatar);
            })()
          : Promise.resolve("");

        subida.then(function (fotoUrl) {
          return supabaseClient.rpc("actualizar_mi_perfil", {
            p_nombre: nombre,
            p_foto_url: fotoUrl || null
          }).then(function (resPerfil) {
            if (resPerfil && resPerfil.error) throw resPerfil.error;
            return fotoUrl;
          });
        }).catch(function (e) {
          console.warn("Aviso guardando foto de perfil:", e);
          return "";
        }).then(function (fotoUrl) {
          return supabaseClient.from("perfiles").select("rol, tienda_id").eq("id", user.id).maybeSingle().then(function (resPerfil) {
            if (resPerfil.error) throw resPerfil.error;
            var perfil = resPerfil.data || {};
            var rol = perfil.rol === "ADMIN" || perfil.rol === "EMPRENDEDORA" ? perfil.rol : "VISITANTE";
            var tiendaId = rol === "EMPRENDEDORA" ? perfil.tienda_id : null;
            estado.usuario = { id: user.id, nombre: nombre, email: email, rol: rol, tiendaId: tiendaId, fotoUrl: fotoUrl || "" };
            estado.tiendaEmprendedoraId = tiendaId || null;
            estadoPOS.tiendaId = tiendaId || null;
            aplicarUsuarioLocal(nombre, etiquetaRol(rol), fotoUrl || "");
            if (rol === "ADMIN") entrarModoAdmin();
            if (rol === "EMPRENDEDORA" && tiendaId) {
              cargarCatalogoPublico();
              actualizarDashboardEmprendedora();
            }
          }).then(function () {
            if (okDiv) {
              okDiv.textContent = "¡Cuenta creada con éxito! Bienvenido(a) a la comunidad.";
              okDiv.style.display = "block";
            }
            setTimeout(function () {
              cerrarModal("modal-auth-web");
              mostrarToast("🎉 ¡Bienvenido(a) " + nombre + "! Cuenta creada con éxito.");
            }, 900);
          });        }).then(function () {
          if (btn) { btn.disabled = false; btn.textContent = "Crear mi Cuenta de Usuario"; }
        });
      }, 600);
    }).catch(function (e) {
      console.warn("Aviso Supabase signUp normal:", e);
      mostrarError("No se pudo crear la cuenta: " + (e.message || e));
    });
  }


  window.TramaWeb = {
    agregarOpcionPublicar: agregarOpcionPublicar,
    cambiarDisponibleSeleccion: cambiarDisponibleSeleccion,
    actualizarReglasPublicar: actualizarReglasPublicar,
    abrirVitrinaTienda: abrirVitrinaTienda,
    abrirQRTienda: abrirQRTienda,
    abrirQRTiendaActual: abrirQRTiendaActual,
    abrirAgendamientoDirecto: abrirAgendamientoDirecto,
    abrirAgendamientoActual: abrirAgendamientoActual,
    abrirFichaFeria: abrirFichaFeria,
    cambiarTabModal: cambiarTabModal,
    agregarAlCarrito: agregarAlCarrito,
    actualizarCantidadCarrito: actualizarCantidadCarrito,
    eliminarItemCarrito: eliminarItemCarrito,
    vaciarCarrito: vaciarCarrito,
    seleccionarMedioPago: seleccionarMedioPago,
    calcularVueltoEfectivo: calcularVueltoEfectivo,
    finalizarPedidoWhatsApp: finalizarPedidoWhatsApp,
    copiarEnlaceQR: copiarEnlaceQR,
    seleccionarHora: seleccionarHora,
    confirmarReservaCita: confirmarReservaCita,
    publicarPreguntaEnMuro: publicarPreguntaEnMuro,
    responderPublicacionMuro: responderPublicacionMuro,
    toggleFavorito: toggleFavorito,
    cerrarModal: cerrarModal,
    abrirModal: abrirModal,
    // Módulo 8: Publicador Facebook Marketplace y Estudio de Flyers
    abrirModalPublicarProducto: abrirModalPublicarProducto,
    abrirModalEstudioFlyer: abrirModalEstudioFlyer,
    abrirEstudioFlyerDesdePublicar: abrirEstudioFlyerDesdePublicar,
    seleccionarFotoSugeridaFB: seleccionarFotoSugeridaFB,
    seleccionarFotoSugeridaFlyer: seleccionarFotoSugeridaFlyer,
    onFotoProductoSubida: onFotoProductoSubida,
    quitarFotoProducto: quitarFotoProducto,
    seleccionarCategoriaFB: seleccionarCategoriaFB,
    seleccionarStockFB: seleccionarStockFB,
    confirmarPublicacionFB: confirmarPublicacionFB,
    cambiarPlantillaFlyer: cambiarPlantillaFlyer,
    cambiarFuenteFlyer: cambiarFuenteFlyer,
    cambiarColorFlyer: cambiarColorFlyer,
    onFotoFlyerSubida: onFotoFlyerSubida,
    actualizarLienzoFlyer: actualizarLienzoFlyer,
    descargarFlyerImagen: descargarFlyerImagen,
    aplicarFlyerComoFotoProducto: aplicarFlyerComoFotoProducto,
    abrirModalAuth: abrirModalAuth,
    abrirModalAuthEmprendedora: abrirModalAuthEmprendedora,
    cambiarRolAuth: cambiarRolAuth,
    toggleModoAuth: toggleModoAuth,
    seleccionarModoAuth: seleccionarModoAuth,
    registrarEmprendedorModal: registrarEmprendedorModal,
    autocompletarCredenciales: autocompletarCredenciales,
    ejecutarAuthFirebase: ejecutarAuthFirebase,
    cargarCatalogoPublico: cargarCatalogoPublico,
    cargarProductosDesdeSupabase: cargarProductosDesdeSupabase,
    entrarModoDev: entrarModoDev,
    entrarModoDonaRosa: entrarModoDonaRosa,
    // Módulo 9: Punto de Venta (POS) y Caja Rápida
    abrirModalPOS: abrirModalPOS,
    cambiarTiendaPOS: cambiarTiendaPOS,
    agregarProductoTicketPOS: agregarProductoTicketPOS,
    modificarCantidadTicketPOS: modificarCantidadTicketPOS,
    vaciarTicketPOS: vaciarTicketPOS,
    seleccionarMedioPagoPOS: seleccionarMedioPagoPOS,
    calcularVueltoPOS: calcularVueltoPOS,
    confirmarVentaPOS: confirmarVentaPOS,
    cerrarSesionWeb: cerrarSesionWeb,
    // Módulo 10: Funcionalidades Android Emprendedora
    actualizarDashboardEmprendedora: actualizarDashboardEmprendedora,
    abrirModalGestionCatalogo: abrirModalGestionCatalogo,
    renderizarGestionCatalogo: renderizarGestionCatalogo,
    modificarStockProducto: modificarStockProducto,
    togglePublicadoProducto: togglePublicadoProducto,
    editarPrecioProducto: editarPrecioProducto,
    eliminarProductoCatalogo: eliminarProductoCatalogo,
    abrirModalEditarTienda: abrirModalEditarTienda,
    guardarPerfilTienda: guardarPerfilTienda,
    abrirModalMisFerias: abrirModalMisFerias,
    renderizarMisFerias: renderizarMisFerias,
    toggleInscripcionFeria: toggleInscripcionFeria,
    // Módulo 11: Administración de Plataforma (W01 a W24, WA3-WA6)
    abrirModalAdmin: abrirModalAdmin,
    entrarModoAdmin: entrarModoAdmin,
    seleccionarTabAdmin: seleccionarTabAdmin,
    filtrarAdminEmprendedoras: filtrarAdminEmprendedoras,
    abrirFormCrearEmprendedora: abrirFormCrearEmprendedora,
    cerrarFormEmprendedora: cerrarFormEmprendedora,
    generarClaveAleatoriaEmp: generarClaveAleatoriaEmp,
    guardarCuentaEmprendedoraAdmin: guardarCuentaEmprendedoraAdmin,
    editarCuentaEmprendedoraAdmin: editarCuentaEmprendedoraAdmin,
    mostrarCredencialesEmprendedora: mostrarCredencialesEmprendedora,
    copiarCredencialesGeneradas: copiarCredencialesGeneradas,
    toggleEstadoCuentaEmprendedora: toggleEstadoCuentaEmprendedora,
    filtrarAdminAsociaciones: filtrarAdminAsociaciones,
    abrirFormCrearAsociacion: abrirFormCrearAsociacion,
    cerrarFormAsociacion: cerrarFormAsociacion,
    guardarAsociacionAdmin: guardarAsociacionAdmin,
    editarAsociacionAdmin: editarAsociacionAdmin,
    toggleEstadoAsociacionAdmin: toggleEstadoAsociacionAdmin,
    asignarEmprendedoraAsocPrompt: asignarEmprendedoraAsocPrompt,
    quitarEmprendedoraAsocPrompt: quitarEmprendedoraAsocPrompt,
    agregarCatalogoItem: agregarCatalogoItem,
    editarCatalogoItem: editarCatalogoItem,
    eliminarCatalogoItem: eliminarCatalogoItem,
    abrirFormCrearAdmin: abrirFormCrearAdmin,
    toggleEstadoAdmin: toggleEstadoAdmin,
    recuperarContrasenaWeb: recuperarContrasenaWeb,
    enviarEnlaceRecuperacion: enviarEnlaceRecuperacion,
    guardarNuevaClave: guardarNuevaClave,
    gestionarClickUsuarioHeader: gestionarClickUsuarioHeader,
    guardarCambiosMiPerfil: guardarCambiosMiPerfil,
    iniciarSesionGoogleWeb: iniciarSesionGoogleWeb,
    abrirModalRegistroEmprendedor: abrirModalRegistroEmprendedor,
    abrirModalAuthNormal: abrirModalAuthNormal,
    aplicarUsuarioLocal: aplicarUsuarioLocal,
    esUsuarioEmprendedora: esUsuarioEmprendedora,
    seleccionarModoUsuario: seleccionarModoUsuario,
    registrarUsuarioNormalModal: registrarUsuarioNormalModal
  };

  // Si se llegó por el QR o el enlace de una tienda (?tienda=ID), abrir su ficha.
  // Se reintenta unos segundos porque las tiendas de Firestore llegan después.
  // Si se llegó por el QR o el enlace de una tienda (?tienda=ID), se abre su
  // ficha cuando termina la carga del servidor (no con datos viejos).
  function abrirTiendaDesdeEnlace() {
    var id = null;
    try { id = new URLSearchParams(window.location.search).get("tienda"); } catch (e) { id = null; }
    if (!id) return;
    promesaCatalogo.then(function () {
      if (TIENDAS.some(function (t) { return t.id === id; })) abrirVitrinaTienda(id);
      else if (!estadoCatalogo.error) mostrarToast("No encontramos la tienda del enlace. Puede haber cambiado de dirección.");
    });
  }

  function arrancar() {
    inicializar();
    abrirTiendaDesdeEnlace();
  }

  // Ejecutar al cargar el DOM
  if (document.readyState === "loading") {
    document.addEventListener("DOMContentLoaded", arrancar);
  } else {
    arrancar();
  }

})();
