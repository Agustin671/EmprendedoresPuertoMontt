/**
 * TRAMA ALERCE — Lógica Web Interactiva (Farfetch & Apple Style)
 * Sincronizado con la App Android: Carrito, Códigos QR, Muro en Vivo,
 * Catálogo, Agendamiento y Accesibilidad.
 */

(function () {
  "use strict";

  // ============================================================
  // CONEXIÓN OFICIAL FIREBASE WEB (AUTH, FIRESTORE, STORAGE)
  // ============================================================
  var firebaseConfig = {
    apiKey: "AIzaSyBmxcinIPVJB-AKgHQQ9iEp3p6-uRS58oI",
    authDomain: "alercei7.firebaseapp.com",
    projectId: "alercei7",
    storageBucket: "alercei7.firebasestorage.app",
    messagingSenderId: "60295127775",
    appId: "1:60295127775:web:4e6ec2188b21076f01ff6a"
  };

  var firebaseApp = null;
  var firebaseAuth = null;
  var firebaseFirestore = null;
  var firebaseStorage = null;

  try {
    if (typeof firebase !== "undefined") {
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

  var TIENDAS = [
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
      nombre: "Minimarket Doña Marta",
      rubro: "Abarrotes y Alimentos",
      sector: "Alerce Histórico",
      bio: "Frutas y verduras frescas, pan amasado caliente todos los días a las 17:00 hrs, abarrotes, cecinas y bebidas.",
      whatsapp: "+56991234567",
      colorPortada: "#C53030",
      colorLogo: "#DD6B20",
      necesidades: {
        tieneStock: true,
        tieneAgendamiento: false,
        tieneVentaPorWhatsApp: true,
        tieneProductosAPedido: false,
        participaEnFerias: false,
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

  var PRODUCTOS_BASE = [
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

  function cargarProductosDeStorage() {
    try {
      var guardados = localStorage.getItem("trama_productos_custom");
      var custom = guardados ? JSON.parse(guardados) : [];
      return PRODUCTOS_BASE.concat(custom);
    } catch (e) {
      return PRODUCTOS_BASE.slice();
    }
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
          texto: "¿Doña Marta, le queda queso de campo artesanal para acompañar el pancito?",
          fecha: "Hoy, 16:45"
        },
        {
          id: "resp_2",
          autorNombre: "Minimarket Doña Marta",
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

  function mostrarToast(mensaje) {
    var toast = document.getElementById("toast-alerta");
    var texto = document.getElementById("toast-texto");
    if (toast && texto) {
      texto.textContent = mensaje;
      toast.style.display = "flex";
      setTimeout(function () {
        toast.style.display = "none";
      }, 3500);
    }
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

  function agregarAlCarrito(productoId, varianteIndice) {
    var prod = PRODUCTOS.find(function (p) { return p.id === productoId; });
    if (!prod) return;
    var tienda = TIENDAS.find(function (t) { return t.id === prod.tiendaId; });
    var variante = prod.variantes[varianteIndice || 0] || { nombre: "Estándar", precio: prod.precio };

    var itemExistente = estado.carrito.find(function (item) {
      return item.productoId === prod.id && item.varianteNombre === variante.nombre;
    });

    if (itemExistente) {
      itemExistente.cantidad += 1;
    } else {
      estado.carrito.push({
        id: "cart_" + Date.now() + "_" + Math.random().toString(36).substr(2, 4),
        productoId: prod.id,
        tiendaId: prod.tiendaId,
        tiendaNombre: tienda ? tienda.nombre : "Tienda",
        tiendaWhatsapp: tienda ? tienda.whatsapp : "+56961230000",
        nombre: prod.nombre,
        varianteNombre: variante.nombre,
        precio: variante.precio,
        cantidad: 1
      });
    }

    guardarCarritoEnStorage();
    mostrarToast("¡" + prod.nombre + " agregado al carrito!");
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
  }

  function vaciarCarrito() {
    estado.carrito = [];
    guardarCarritoEnStorage();
    renderizarCarrito();
    mostrarToast("Carrito vaciado");
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

    var html = "";
    var total = 0;

    estado.carrito.forEach(function (item) {
      var subtotalItem = item.precio * item.cantidad;
      total += subtotalItem;

      html += '<div class="item-carrito-fila">';
      html += '  <div class="item-carrito-info">';
      html += '    <h4 class="item-carrito-titulo">' + escaparHtml(item.nombre) + '</h4>';
      html += '    <p class="item-carrito-tienda">' + escaparHtml(item.tiendaNombre) + ' · ' + escaparHtml(item.varianteNombre) + '</p>';
      html += '    <p class="item-carrito-precio">' + formatearMoneda(item.precio) + ' c/u · Subtotal: <strong>' + formatearMoneda(subtotalItem) + '</strong></p>';
      html += '  </div>';
      html += '  <div class="controles-cantidad">';
      html += '    <button type="button" class="btn-qty" onclick="TramaWeb.actualizarCantidadCarrito(\'' + item.id + '\', -1)">−</button>';
      html += '    <span class="qty-numero">' + item.cantidad + '</span>';
      html += '    <button type="button" class="btn-qty" onclick="TramaWeb.actualizarCantidadCarrito(\'' + item.id + '\', 1)">+</button>';
      html += '    <button type="button" style="margin-left: 6px; font-size: 14px; cursor: pointer;" onclick="TramaWeb.eliminarItemCarrito(\'' + item.id + '\')" title="Quitar">🗑️</button>';
      html += '  </div>';
      html += '</div>';
    });

    listaContainer.innerHTML = html;
    if (subtotalTexto) subtotalTexto.textContent = formatearMoneda(total);
    if (totalTexto) totalTexto.textContent = formatearMoneda(total);

    // Si está seleccionado Pago QR, actualizar el código QR
    if (estado.medioPagoSeleccionado === "QR") {
      generarQRPago(total);
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
    if (medio === "QR") {
      if (cajaQR) cajaQR.style.display = "block";
      var total = estado.carrito.reduce(function (acc, i) { return acc + (i.precio * i.cantidad); }, 0);
      generarQRPago(total);
    } else {
      if (cajaQR) cajaQR.style.display = "none";
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

  function finalizarPedidoWhatsApp() {
    if (estado.carrito.length === 0) return;

    var primerItem = estado.carrito[0];
    var telefono = primerItem.tiendaWhatsapp.replace(/[^0-9]/g, "");
    var total = estado.carrito.reduce(function (acc, i) { return acc + (i.precio * i.cantidad); }, 0);

    var mensaje = "¡Hola! Quiero confirmar mi pedido desde la vitrina de Alerce Emprende:\n\n";
    estado.carrito.forEach(function (i) {
      mensaje += "• " + i.cantidad + "x " + i.nombre + " (" + i.varianteNombre + ") — " + formatearMoneda(i.precio * i.cantidad) + "\n";
    });
    mensaje += "\n💰 Total: " + formatearMoneda(total);
    mensaje += "\n💳 Medio de Pago: " + estado.medioPagoSeleccionado;
    mensaje += "\n👤 Cliente: " + estado.usuario.nombre;

    var url = "https://wa.me/" + telefono + "?text=" + encodeURIComponent(mensaje);
    window.open(url, "_blank");
    mostrarToast("¡Abriendo WhatsApp para confirmar pedido!");
  }

  // ============================================================
  // RENDERIZADO DEL MARKETPLACE Y VITRINA DE TIENDAS
  // ============================================================

  function renderizarTiendasMarketplace() {
    var contenedor = document.getElementById("grilla-tiendas-marketplace");
    var avisoVacio = document.getElementById("aviso-sin-tiendas");
    var subtitulo = document.getElementById("subtitulo-conteo-tiendas");
    if (!contenedor) return;

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
      subtitulo.textContent = "Mostrando " + tiendasFiltradas.length + " emprendimientos de Alerce (" + filtro + ")";
    }

    if (tiendasFiltradas.length === 0) {
      contenedor.innerHTML = "";
      if (avisoVacio) avisoVacio.style.display = "block";
      return;
    }

    if (avisoVacio) avisoVacio.style.display = "none";

    var html = "";
    tiendasFiltradas.forEach(function (tienda) {
      var esFav = !!estado.favoritos[tienda.id];
      var corazonIcon = esFav ? "❤️" : "🤍";

      html += '<article class="tarjeta-tienda">';
      
      // Franja de color superior con rubro y sector
      html += '  <div class="tarjeta-franja-color" style="background-color: ' + tienda.colorPortada + ';">';
      html += '    <span class="franja-rubro">' + escaparHtml(tienda.rubro) + '</span>';
      html += '    <div class="franja-lado-derecho">';
      html += '      <span class="franja-sector-chip">' + escaparHtml(tienda.sector) + '</span>';
      html += '      <button type="button" class="btn-favorito-corazon ' + (esFav ? 'activo' : '') + '" onclick="TramaWeb.toggleFavorito(\'' + tienda.id + '\')" title="Favorito">' + corazonIcon + '</button>';
      html += '    </div>';
      html += '  </div>';

      // Cuerpo de la tarjeta
      html += '  <div class="tarjeta-cuerpo">';
      html += '    <div class="tarjeta-perfil-fila">';
      html += '      <div class="avatar-circulo" style="background-color: ' + tienda.colorLogo + ';">' + tienda.nombre.charAt(0).toUpperCase() + '</div>';
      html += '      <div class="tarjeta-titulos">';
      html += '        <h3 class="tarjeta-nombre">' + escaparHtml(tienda.nombre) + '</h3>';
      html += '        <p class="tarjeta-sub">' + escaparHtml(tienda.rubro) + ' · ' + escaparHtml(tienda.sector) + '</p>';
      html += '      </div>';
      html += '    </div>';

      html += '    <p class="tarjeta-descripcion">' + escaparHtml(tienda.bio) + '</p>';

      // Badges de capacidades
      html += '    <div class="tarjeta-badges-wrap">';
      if (tienda.necesidades.tieneStock) {
        html += '      <span class="badge-necesidad badge-stock">📦 Stock</span>';
      }
      if (tienda.necesidades.tieneProductosAPedido) {
        html += '      <span class="badge-necesidad badge-pedido">✂️ A Pedido</span>';
      }
      if (tienda.necesidades.participaEnFerias) {
        html += '      <span class="badge-necesidad badge-ferias">🎪 Ferias</span>';
      }
      if (tienda.necesidades.tieneForoComunidad) {
        html += '      <span class="badge-necesidad badge-foro">💬 Muro y Foro</span>';
      }
      if (tienda.necesidades.tieneAgendamiento) {
        html += '      <span class="badge-necesidad badge-agenda">📅 Agenda Horas</span>';
      }
      html += '    </div>';

      html += '    <hr class="divisor-sutil">';

      // Botones de acción inferiores
      html += '    <div class="tarjeta-acciones-fila">';
      if (tienda.necesidades.tieneAgendamiento) {
        html += '      <button type="button" class="btn-accion-pill btn-agenda" onclick="TramaWeb.abrirAgendamientoDirecto(\'' + tienda.id + '\')">Agendar Cita</button>';
      } else {
        html += '      <button type="button" class="btn-accion-pill btn-negro" onclick="TramaWeb.abrirVitrinaTienda(\'' + tienda.id + '\')">' + (tienda.necesidades.tieneForoComunidad ? 'Vitrina y Foro' : 'Ver Vitrina') + '</button>';
      }

      html += '      <a href="https://wa.me/' + tienda.whatsapp.replace(/[^0-9]/g, "") + '?text=' + encodeURIComponent("Hola " + tienda.nombre + "! Vi tu tienda en Alerce Emprende y me gustaría hacer una consulta.") + '" target="_blank" rel="noopener" class="btn-accion-pill">WhatsApp</a>';
      html += '      <button type="button" class="btn-qr-icon" onclick="TramaWeb.abrirQRTienda(\'' + tienda.id + '\')" title="Ver Código QR de la tienda" aria-label="Código QR">' +
        '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><rect x="3" y="3" width="7" height="7"/><rect x="14" y="3" width="7" height="7"/><rect x="14" y="14" width="7" height="7"/><rect x="3" y="14" width="7" height="7"/><path d="M7 7h.01"/><path d="M17 7h.01"/><path d="M7 17h.01"/><path d="M17 17h.01"/></svg>' +
        '</button>';
      html += '    </div>';

      html += '  </div>';
      html += '</article>';
    });

    contenedor.innerHTML = html;
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
    estado.tabModalActiva = 0; // Default: Catálogo

    var modal = document.getElementById("modal-tienda-vitrina");
    var avatar = document.getElementById("modal-tienda-avatar");
    var titulo = document.getElementById("modal-tienda-titulo");
    var rubroSector = document.getElementById("modal-tienda-rubro-sector");
    var bio = document.getElementById("modal-tienda-bio");
    var bannerAgenda = document.getElementById("modal-tienda-banner-agenda");
    var btnWhatsapp = document.getElementById("btn-modal-tienda-whatsapp");

    if (avatar) {
      avatar.textContent = tienda.nombre.charAt(0).toUpperCase();
      avatar.style.backgroundColor = tienda.colorLogo;
    }
    if (titulo) titulo.textContent = tienda.nombre;
    if (rubroSector) rubroSector.textContent = tienda.rubro + " · " + tienda.sector;
    if (bio) bio.textContent = tienda.bio;

    if (bannerAgenda) {
      bannerAgenda.style.display = tienda.necesidades.tieneAgendamiento ? "block" : "none";
    }

    if (btnWhatsapp) {
      btnWhatsapp.href = "https://wa.me/" + tienda.whatsapp.replace(/[^0-9]/g, "") +
        "?text=" + encodeURIComponent("Hola " + tienda.nombre + "! Vi tu vitrina en Alerce Emprende.");
    }

    // Actualizar tabs y contenidos
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

  function actualizarContadorTabsModal() {
    if (!estado.tiendaSeleccionada) return;
    var prods = PRODUCTOS.filter(function (p) { return p.tiendaId === estado.tiendaSeleccionada.id; });
    var pubs = PUBLICACIONES_FORO.filter(function (f) { return f.tiendaId === estado.tiendaSeleccionada.id; });

    var cCat = document.getElementById("conteo-tab-catalogo");
    var cForo = document.getElementById("conteo-tab-foro");
    if (cCat) cCat.textContent = prods.length;
    if (cForo) cForo.textContent = pubs.length;
  }

  function renderizarProductosModal() {
    if (!estado.tiendaSeleccionada) return;
    var contenedor = document.getElementById("lista-productos-tienda-modal");
    if (!contenedor) return;

    var prods = PRODUCTOS.filter(function (p) { return p.tiendaId === estado.tiendaSeleccionada.id; });

    if (prods.length === 0) {
      contenedor.innerHTML = '<p style="font-size: 13px; color: var(--gris-texto); padding: 18px 0;">Esta tienda aún no tiene productos registrados en la vitrina.</p>';
      return;
    }

    var html = "";
    prods.forEach(function (p) {
      var tieneStock = p.stockTotal > 0;
      var badgeStock = tieneStock
        ? '<span class="badge-necesidad badge-stock">Stock Disponible (' + p.stockTotal + ')</span>'
        : '<span class="badge-necesidad badge-pedido">A Pedido</span>';

      html += '<div class="producto-fila-card">';
      html += '  <div class="producto-card-top">';
      html += '    <div style="display: flex; gap: 12px; align-items: center;">';
      if (p.fotoUrl) {
        html += '      <img src="' + p.fotoUrl + '" alt="' + escaparHtml(p.nombre) + '" style="width: 52px; height: 52px; object-fit: cover; border-radius: 6px; border: 1px solid var(--gris-borde); flex-shrink: 0;">';
      }
      html += '      <div>';
      html += '        <h4 class="prod-nombre">' + escaparHtml(p.nombre) + '</h4>';
      html += '        <p style="font-size: 12px; color: var(--gris-texto); margin-top: 2px;">' + escaparHtml(p.descripcion) + '</p>';
      html += '      </div>';
      html += '    </div>';
      html += '    <div style="text-align: right;">';
      html += '      <span class="prod-precio">' + formatearMoneda(p.precio) + '</span>';
      html += '      <div style="margin-top: 4px;">' + badgeStock + '</div>';
      html += '    </div>';
      html += '  </div>';

      // Variantes chips
      if (p.variantes && p.variantes.length > 0) {
        html += '  <div class="prod-variantes-chips">';
        p.variantes.forEach(function (v) {
          html += '    <span class="chip-variante">' + escaparHtml(v.nombre) + '</span>';
        });
        html += '  </div>';
      }

      // Botones de acción
      html += '  <div class="producto-botones-fila">';
      html += '    <button type="button" class="btn-anadir-carrito" onclick="TramaWeb.agregarAlCarrito(\'' + p.id + '\', 0)">🛒 Agregar al Carrito</button>';
      html += '    <a href="https://wa.me/' + estado.tiendaSeleccionada.whatsapp.replace(/[^0-9]/g, "") + '?text=' + encodeURIComponent("Hola " + estado.tiendaSeleccionada.nombre + "! Me interesa comprar '" + p.nombre + "' (" + formatearMoneda(p.precio) + ") que vi en tu vitrina de Alerce.") + '" target="_blank" rel="noopener" class="btn-whatsapp-prod">Pedir por WhatsApp</a>';
      html += '  </div>';

      html += '</div>';
    });

    contenedor.innerHTML = html;
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
    var url = "https://trama.app/tienda/" + t.id;

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
    var url = "https://trama.app/tienda/" + estado.tiendaSeleccionada.id;
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

  function abrirModal(modalId) {
    var m = document.getElementById(modalId);
    if (m) {
      m.classList.add("abierto");
      document.body.style.overflow = "hidden";
    }
  }

  function cerrarModal(modalId) {
    var m = document.getElementById(modalId);
    if (m) {
      m.classList.remove("abierto");
      document.body.style.overflow = "";
    }
  }

  // Cerrar al hacer clic en el backdrop
  document.addEventListener("click", function (e) {
    if (e.target && e.target.classList.contains("modal-overlay")) {
      e.target.classList.remove("abierto");
      document.body.style.overflow = "";
    }
  });

  // Cerrar con Escape
  document.addEventListener("keydown", function (e) {
    if (e.key === "Escape") {
      document.querySelectorAll(".modal-overlay.abierto").forEach(function (m) {
        m.classList.remove("abierto");
      });
      document.body.style.overflow = "";
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
  function abrirModalPublicarProducto() {
    renderizarGaleriaSugeridaFB();
    abrirModal("modal-publicar-producto");
  }

  // Renderizar galería de fotos sugeridas en modal FB
  function renderizarGaleriaSugeridaFB() {
    var cont = document.getElementById("fb-galeria-rapida");
    if (!cont) return;
    cont.innerHTML = FOTOS_SUGERIDAS.map(function (f) {
      return '<button type="button" class="fb-chip-cat" style="font-size: 11px; padding: 4px 8px;" onclick="TramaWeb.seleccionarFotoSugeridaFB(\'' + f.url + '\')">' +
        f.emoji + ' ' + f.nombre +
      '</button>';
    }).join("");
  }

  // Seleccionar foto sugerida en FB
  function seleccionarFotoSugeridaFB(url) {
    estadoPublicar.fotoDataUrl = url;
    var preview = document.getElementById("fb-img-preview");
    var placeholder = document.getElementById("fb-placeholder-foto");
    var opciones = document.getElementById("fb-opciones-foto-activa");
    if (preview) {
      preview.src = url;
      preview.style.display = "block";
    }
    if (placeholder) placeholder.style.display = "none";
    if (opciones) opciones.style.display = "flex";
    cargarFotoEnFlyer(url);
    mostrarToast("Foto sugerida seleccionada");
  }

  // Al subir foto local (teléfono / PC)
  function onFotoProductoSubida(event) {
    var file = event.target.files && event.target.files[0];
    if (!file) return;
    var reader = new FileReader();
    reader.onload = function (e) {
      var dataUrl = e.target.result;
      estadoPublicar.fotoDataUrl = dataUrl;
      var preview = document.getElementById("fb-img-preview");
      var placeholder = document.getElementById("fb-placeholder-foto");
      var opciones = document.getElementById("fb-opciones-foto-activa");
      if (preview) {
        preview.src = dataUrl;
        preview.style.display = "block";
      }
      if (placeholder) placeholder.style.display = "none";
      if (opciones) opciones.style.display = "flex";

      cargarFotoEnFlyer(dataUrl);
      mostrarToast("Foto cargada con éxito");
    };
    reader.readAsDataURL(file);
  }

  function quitarFotoProducto() {
    estadoPublicar.fotoDataUrl = "";
    var preview = document.getElementById("fb-img-preview");
    var placeholder = document.getElementById("fb-placeholder-foto");
    var opciones = document.getElementById("fb-opciones-foto-activa");
    var inp = document.getElementById("inp-subir-foto-producto");
    if (preview) preview.style.display = "none";
    if (placeholder) placeholder.style.display = "flex";
    if (opciones) opciones.style.display = "none";
    if (inp) inp.value = "";
  }

  function seleccionarCategoriaFB(cat) {
    estadoPublicar.categoriaSeleccionada = cat;
    var contenedor = document.getElementById("fb-selector-categoria");
    if (!contenedor) return;
    var btns = contenedor.querySelectorAll(".fb-chip-cat");
    btns.forEach(function (b) {
      if (b.getAttribute("data-cat") === cat) {
        b.classList.add("activo");
      } else {
        b.classList.remove("activo");
      }
    });
  }

  function seleccionarStockFB(stock) {
    estadoPublicar.stockSeleccionado = stock;
    var contenedor = document.getElementById("fb-selector-stock");
    if (!contenedor) return;
    var btns = contenedor.querySelectorAll(".fb-chip-cat");
    btns.forEach(function (b) {
      if (b.getAttribute("data-stock") === stock) {
        b.classList.add("activo");
      } else {
        b.classList.remove("activo");
      }
    });
  }

  // Confirmar publicación estilo Facebook Marketplace
  function confirmarPublicacionFB() {
    var inpTitulo = document.getElementById("fb-inp-titulo");
    var inpPrecio = document.getElementById("fb-inp-precio");
    var selectTienda = document.getElementById("fb-select-tienda");
    var inpDesc = document.getElementById("fb-inp-desc");

    var titulo = inpTitulo ? inpTitulo.value.trim() : "";
    var precio = inpPrecio ? parseInt(inpPrecio.value, 10) : 0;
    var tiendaId = selectTienda ? selectTienda.value : "t_minimarket_rosa";
    var desc = inpDesc ? inpDesc.value.trim() : "";

    if (!titulo) {
      mostrarToast("⚠️ Por favor escribe el nombre de tu producto");
      if (inpTitulo) inpTitulo.focus();
      return;
    }

    if (!precio || isNaN(precio) || precio <= 0) {
      mostrarToast("⚠️ Ingresa un precio válido en pesos chilenos ($)");
      if (inpPrecio) inpPrecio.focus();
      return;
    }

    var tiendaEncontrada = null;
    for (var i = 0; i < TIENDAS.length; i++) {
      if (TIENDAS[i].id === tiendaId) {
        tiendaEncontrada = TIENDAS[i];
        break;
      }
    }
    var nombreTienda = tiendaEncontrada ? tiendaEncontrada.nombre : "Emprendedora de Alerce";

    var nuevoProducto = {
      id: "p_custom_" + Date.now(),
      tiendaId: tiendaId,
      nombre: titulo,
      precio: precio,
      stockTotal: estadoPublicar.stockSeleccionado === "inmediato" ? 15 : 0,
      descripcion: desc || (titulo + " elaborado con amor por " + nombreTienda + "."),
      fotoUrl: estadoPublicar.fotoDataUrl || "",
      variantes: [{ nombre: "Unidad", precio: precio }]
    };

    // Guardar en almacenamiento local
    try {
      var guardados = localStorage.getItem("trama_productos_custom");
      var lista = guardados ? JSON.parse(guardados) : [];
      lista.unshift(nuevoProducto);
      localStorage.setItem("trama_productos_custom", JSON.stringify(lista));
    } catch (e) {
      console.warn("No se pudo guardar en localStorage", e);
    }

    // Sincronizar con Cloud Firestore en tiempo real si está conectado
    if (firebaseFirestore) {
      try {
        firebaseFirestore.collection("tiendas").doc(tiendaId).collection("productos").doc(nuevoProducto.id).set({
          id: nuevoProducto.id,
          nombre: nuevoProducto.nombre,
          precio: nuevoProducto.precio,
          stockTotal: nuevoProducto.stockTotal,
          descripcion: nuevoProducto.descripcion,
          fotoUrl: nuevoProducto.fotoUrl,
          publicado: true,
          fechaCreacion: Date.now()
        }).then(function () {
          console.log("Producto guardado exitosamente en Firestore online");
        }).catch(function (err) {
          console.warn("Aviso Firestore al guardar producto:", err);
        });
      } catch (errFs) {
        console.warn("Excepción guardando producto en Firestore:", errFs);
      }
    }

    // Agregar al listado en memoria
    PRODUCTOS.unshift(nuevoProducto);

    // Limpiar formulario
    if (inpTitulo) inpTitulo.value = "";
    if (inpPrecio) inpPrecio.value = "";
    if (inpDesc) inpDesc.value = "";
    quitarFotoProducto();

    cerrarModal("modal-publicar-producto");
    mostrarToast("🎉 ¡Producto publicado con éxito en la vitrina!");
    renderizarTiendasMarketplace();

    // Si la vitrina de la tienda está abierta, refrescarla
    if (estado.tiendaSeleccionada && estado.tiendaSeleccionada.id === tiendaId) {
      renderizarProductosTienda(tiendaId);
    }
  }

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
  function aplicarFlyerComoFotoProducto() {
    var canvas = document.getElementById("canvas-flyer");
    if (!canvas) return;

    try {
      var dataUrl = canvas.toDataURL("image/jpeg", 0.9);
      estadoPublicar.fotoDataUrl = dataUrl;

      var preview = document.getElementById("fb-img-preview");
      var placeholder = document.getElementById("fb-placeholder-foto");
      var opciones = document.getElementById("fb-opciones-foto-activa");
      if (preview) {
        preview.src = dataUrl;
        preview.style.display = "block";
      }
      if (placeholder) placeholder.style.display = "none";
      if (opciones) opciones.style.display = "flex";

      // Sincronizar título y precio si están vacíos
      var flyerTit = document.getElementById("flyer-inp-titulo");
      var flyerPre = document.getElementById("flyer-inp-precio");
      var fbTit = document.getElementById("fb-inp-titulo");
      var fbPre = document.getElementById("fb-inp-precio");

      if (flyerTit && fbTit && !fbTit.value.trim()) {
        fbTit.value = flyerTit.value.trim();
      }
      if (flyerPre && fbPre && !fbPre.value.trim()) {
        fbPre.value = flyerPre.value.trim().replace(/\D/g, "");
      }

      cerrarModal("modal-estudio-flyer");
      abrirModal("modal-publicar-producto");
      mostrarToast("✅ Afiche aplicado como foto de tu producto");
    } catch (err) {
      console.error(err);
      mostrarToast("⚠️ Error al capturar afiche");
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
    configurarObservadorAuthFirebase();
    cargarProductosDesdeFirestore();
  }

  // ============================================================
  // MÓDULO 9: PUNTO DE VENTA (POS) Y REGISTRO DE VENTAS EN VIVO
  // ============================================================

  var estadoPOS = {
    tiendaId: "t_minimarket_rosa",
    medioPago: "Efectivo",
    ticket: [],
    montoPagaCon: 0
  };

  function abrirModalPOS() {
    var selectTienda = document.getElementById("pos-select-tienda");
    if (selectTienda) {
      selectTienda.innerHTML = "";
      TIENDAS.forEach(function (t) {
        var opt = document.createElement("option");
        opt.value = t.id;
        opt.textContent = t.nombre;
        if (t.id === estadoPOS.tiendaId) opt.selected = true;
        selectTienda.appendChild(opt);
      });
    }
    renderizarPOS();
    abrirModal("modal-pos");
  }

  function cambiarTiendaPOS(nuevaTiendaId) {
    estadoPOS.tiendaId = nuevaTiendaId;
    renderizarPOS();
  }

  function renderizarPOS() {
    var tienda = null;
    for (var i = 0; i < TIENDAS.length; i++) {
      if (TIENDAS[i].id === estadoPOS.tiendaId) {
        tienda = TIENDAS[i];
        break;
      }
    }
    var sub = document.getElementById("pos-tienda-subtitulo");
    if (sub && tienda) {
      sub.textContent = "Catálogo y caja de " + tienda.nombre + " · " + tienda.rubro;
    }

    var prods = PRODUCTOS.filter(function (p) {
      return p.tiendaId === estadoPOS.tiendaId;
    });

    var conteoDisp = document.getElementById("pos-conteo-disponibles");
    if (conteoDisp) {
      conteoDisp.textContent = prods.length + " productos listados";
    }

    var grid = document.getElementById("pos-grid-productos");
    if (grid) {
      grid.innerHTML = "";
      if (prods.length === 0) {
        grid.innerHTML = '<div style="grid-column: 1 / -1; text-align: center; padding: 24px; color: var(--gris-texto); font-size: 12px;">No hay productos registrados en esta tienda.<br>Usa el botón "Añadir Producto" para crear uno.</div>';
      } else {
        prods.forEach(function (prod) {
          var tarjeta = document.createElement("div");
          tarjeta.style.cssText = "background: #fff; border: 1px solid var(--gris-borde); border-radius: 8px; padding: 10px; cursor: pointer; display: flex; flex-direction: column; justify-content: space-between; transition: transform .15s ease, border-color .15s ease;";
          tarjeta.onmouseover = function () { this.style.borderColor = "var(--negro-apple)"; };
          tarjeta.onmouseout = function () { this.style.borderColor = "var(--gris-borde)"; };
          tarjeta.onclick = function () { agregarProductoTicketPOS(prod.id); };

          var fotoHtml = prod.fotoUrl
            ? '<img src="' + prod.fotoUrl + '" alt="' + prod.nombre + '" style="width: 100%; height: 60px; object-fit: cover; border-radius: 6px; margin-bottom: 6px;">'
            : '<div style="width: 100%; height: 50px; background: #e4e4e7; border-radius: 6px; display: flex; align-items: center; justify-content: center; font-weight: 800; color: #71717a; margin-bottom: 6px;">📦</div>';

          tarjeta.innerHTML = fotoHtml +
            '<strong style="font-size: 11.5px; color: var(--negro-apple); line-height: 1.2; margin-bottom: 4px;">' + prod.nombre + '</strong>' +
            '<span style="font-size: 12.5px; font-weight: 800; color: var(--rojo-alerce);">' + formatearMoneda(prod.precio) + '</span>';

          grid.appendChild(tarjeta);
        });
      }
    }

    renderizarTicketPOS();
  }

  function agregarProductoTicketPOS(prodId) {
    var prod = null;
    for (var i = 0; i < PRODUCTOS.length; i++) {
      if (PRODUCTOS[i].id === prodId) {
        prod = PRODUCTOS[i];
        break;
      }
    }
    if (!prod) return;

    var existe = null;
    for (var j = 0; j < estadoPOS.ticket.length; j++) {
      if (estadoPOS.ticket[j].productoId === prodId) {
        existe = estadoPOS.ticket[j];
        break;
      }
    }

    if (existe) {
      existe.cantidad++;
    } else {
      estadoPOS.ticket.push({
        id: "tick_" + Date.now() + "_" + Math.floor(Math.random() * 1000),
        productoId: prod.id,
        nombre: prod.nombre,
        precio: prod.precio,
        cantidad: 1,
        fotoUrl: prod.fotoUrl || ""
      });
    }

    renderizarTicketPOS();
  }

  function modificarCantidadTicketPOS(index, delta) {
    if (estadoPOS.ticket[index]) {
      estadoPOS.ticket[index].cantidad += delta;
      if (estadoPOS.ticket[index].cantidad <= 0) {
        estadoPOS.ticket.splice(index, 1);
      }
    }
    renderizarTicketPOS();
  }

  function vaciarTicketPOS() {
    estadoPOS.ticket = [];
    renderizarTicketPOS();
  }

  function renderizarTicketPOS() {
    var lista = document.getElementById("pos-lista-items");
    var totalCobrarEl = document.getElementById("pos-total-cobrar");
    var total = 0;

    if (!lista) return;
    lista.innerHTML = "";

    if (estadoPOS.ticket.length === 0) {
      lista.innerHTML = '<div style="color: var(--gris-texto); text-align: center; padding: 24px 0;">El ticket está vacío.<br>Haz clic en los productos para agregarlos.</div>';
    } else {
      estadoPOS.ticket.forEach(function (item, idx) {
        var subtotal = item.precio * item.cantidad;
        total += subtotal;

        var fila = document.createElement("div");
        fila.style.cssText = "display: flex; align-items: center; justify-content: space-between; padding: 6px 0; border-bottom: 1px solid #f0f0f0;";
        fila.innerHTML =
          '<div style="flex: 1; padding-right: 6px;">' +
            '<div style="font-weight: 700; color: var(--negro-apple); line-height: 1.2;">' + item.nombre + '</div>' +
            '<div style="font-size: 11px; color: var(--gris-texto);">' + formatearMoneda(item.precio) + ' c/u</div>' +
          '</div>' +
          '<div style="display: flex; align-items: center; gap: 4px;">' +
            '<button type="button" style="width: 22px; height: 22px; border-radius: 4px; border: 1px solid var(--gris-borde); background: #fff; font-weight: 800; cursor: pointer;" onclick="TramaWeb.modificarCantidadTicketPOS(' + idx + ', -1)">-</button>' +
            '<span style="font-weight: 800; min-width: 18px; text-align: center;">' + item.cantidad + '</span>' +
            '<button type="button" style="width: 22px; height: 22px; border-radius: 4px; border: 1px solid var(--gris-borde); background: #fff; font-weight: 800; cursor: pointer;" onclick="TramaWeb.modificarCantidadTicketPOS(' + idx + ', 1)">+</button>' +
          '</div>' +
          '<div style="min-width: 65px; text-align: right; font-weight: 800; color: var(--negro-apple);">' + formatearMoneda(subtotal) + '</div>';

        lista.appendChild(fila);
      });
    }

    if (totalCobrarEl) {
      totalCobrarEl.textContent = formatearMoneda(total);
    }

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
    if (estadoPOS.ticket.length === 0) {
      mostrarToast("⚠️ El ticket está vacío. Agrega productos para cobrar.");
      return;
    }

    var total = 0;
    var cantTotal = 0;
    var resumenTexto = "🛒 Comprobante de Venta - Alerce Emprende\n";
    var tienda = null;
    for (var i = 0; i < TIENDAS.length; i++) {
      if (TIENDAS[i].id === estadoPOS.tiendaId) {
        tienda = TIENDAS[i];
        break;
      }
    }
    if (tienda) resumenTexto += "🏪 Tienda: " + tienda.nombre + "\n\n";

    estadoPOS.ticket.forEach(function (it) {
      var sub = it.precio * it.cantidad;
      total += sub;
      cantTotal += it.cantidad;
      resumenTexto += "• " + it.nombre + " x" + it.cantidad + " = " + formatearMoneda(sub) + "\n";
    });

    resumenTexto += "\n💰 Total: " + formatearMoneda(total);
    resumenTexto += "\n💳 Medio de Pago: " + estadoPOS.medioPago;

    if (estadoPOS.medioPago === "Efectivo" && estadoPOS.montoPagaCon > 0) {
      resumenTexto += "\n💵 Pagado con: " + formatearMoneda(estadoPOS.montoPagaCon);
      var vto = Math.max(0, estadoPOS.montoPagaCon - total);
      resumenTexto += "\n🪙 Vuelto entregado: " + formatearMoneda(vto);
    }

    resumenTexto += "\n\n¡Gracias por apoyar a los emprendedores locales de Alerce!";

    // Guardar en Firestore si está conectado
    if (firebaseFirestore) {
      try {
        firebaseFirestore.collection("tiendas").doc(estadoPOS.tiendaId).collection("ventas").add({
          items: estadoPOS.ticket,
          total: total,
          cantArticulos: cantTotal,
          medioPago: estadoPOS.medioPago,
          fecha: Date.now()
        }).catch(function (e) { console.warn("Aviso Firestore al guardar venta:", e); });
      } catch (err) {
        console.warn("Excepción guardando venta:", err);
      }
    }

    // Ofrecer enviar recibo por WhatsApp
    if (confirm("✅ Venta de " + formatearMoneda(total) + " registrada con éxito.\n¿Deseas enviar o compartir el comprobante por WhatsApp al cliente?")) {
      var urlWsp = "https://wa.me/?text=" + encodeURIComponent(resumenTexto);
      window.open(urlWsp, "_blank");
    } else {
      mostrarToast("✅ Venta de " + formatearMoneda(total) + " registrada en caja.");
    }

    vaciarTicketPOS();
    cerrarModal("modal-pos");
  }

  // Sincronización en vivo con Cloud Firestore
  function cargarProductosDesdeFirestore() {
    if (!firebaseFirestore) return;
    try {
      firebaseFirestore.collection("tiendas").get().then(function (snapshotTiendas) {
        snapshotTiendas.forEach(function (docTienda) {
          docTienda.ref.collection("productos").get().then(function (snapProds) {
            snapProds.forEach(function (docP) {
              var data = docP.data();
              if (data && data.nombre) {
                var yaExiste = false;
                for (var i = 0; i < PRODUCTOS.length; i++) {
                  if (PRODUCTOS[i].id === docP.id) {
                    yaExiste = true;
                    break;
                  }
                }
                if (!yaExiste) {
                  PRODUCTOS.unshift({
                    id: docP.id,
                    tiendaId: docTienda.id,
                    nombre: data.nombre,
                    precio: Number(data.precio) || 0,
                    stockTotal: Number(data.stockTotal) || 10,
                    descripcion: data.descripcion || "",
                    fotoUrl: data.fotoUrl || "",
                    variantes: data.variantes || [{ nombre: "Unidad", precio: Number(data.precio) || 0 }]
                  });
                }
              }
            });
            renderizarTiendasMarketplace();
          }).catch(function(e){ console.warn("Aviso Firestore productos:", e); });
        });
      }).catch(function(e){ console.warn("Aviso Firestore tiendas:", e); });
    } catch(err) {
      console.warn("Error conectando Firestore:", err);
    }
  }

  
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
    if (rol === "EMPRENDEDORA") {
      if (btnEmp) {
        btnEmp.className = "btn-accion-pill btn-negro";
        btnEmp.style.background = "";
      }
      if (btnComp) {
        btnComp.className = "btn-accion-pill";
        btnComp.style.background = "transparent";
      }
    } else {
      if (btnEmp) {
        btnEmp.className = "btn-accion-pill";
        btnEmp.style.background = "transparent";
      }
      if (btnComp) {
        btnComp.className = "btn-accion-pill btn-negro";
        btnComp.style.background = "";
      }
    }
  }

  function toggleModoAuth() {
    estadoAuthWeb.modoRegistro = !estadoAuthWeb.modoRegistro;
    var camposReg = document.getElementById("auth-campos-registro");
    var titulo = document.getElementById("modal-auth-titulo");
    var btnSubmit = document.getElementById("btn-submit-auth");
    var btnToggle = document.getElementById("btn-toggle-modo-auth");
    var errorDiv = document.getElementById("auth-aviso-error");
    if (errorDiv) errorDiv.style.display = "none";

    if (estadoAuthWeb.modoRegistro) {
      if (camposReg) camposReg.style.display = "block";
      if (titulo) titulo.textContent = "Crear Cuenta en Firebase";
      if (btnSubmit) btnSubmit.textContent = "Registrarme en Firebase";
      if (btnToggle) btnToggle.textContent = "¿Ya tienes cuenta? Inicia sesión aquí";
    } else {
      if (camposReg) camposReg.style.display = "none";
      if (titulo) titulo.textContent = "Iniciar Sesión";
      if (btnSubmit) btnSubmit.textContent = "Iniciar Sesión";
      if (btnToggle) btnToggle.textContent = "¿No tienes cuenta? Regístrate en Firebase aquí";
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
      btnSubmit.textContent = "Conectando con Firebase...";
    }

    if (!firebaseAuth) {
      // Modo local si Firebase no está disponible
      aplicarUsuarioLocal(nombre || email.split("@")[0], estadoAuthWeb.rolSeleccionado === "EMPRENDEDORA" ? "Emprendedora" : "Comprador");
      if (btnSubmit) {
        btnSubmit.disabled = false;
        btnSubmit.textContent = "Iniciar Sesión";
      }
      cerrarModal("modal-auth-web");
      mostrarToast("¡Bienvenido! Sesión iniciada.");
      return;
    }

    if (estadoAuthWeb.modoRegistro) {
      firebaseAuth.createUserWithEmailAndPassword(email, pass)
        .then(function (cred) {
          var user = cred.user;
          return user.updateProfile({ displayName: nombre }).then(function () {
            // Guardar documento en Firestore
            if (firebaseFirestore) {
              firebaseFirestore.collection("usuarios").doc(user.uid).set({
                uid: user.uid,
                email: email,
                nombre: nombre,
                rol: estadoAuthWeb.rolSeleccionado,
                fechaRegistro: Date.now()
              }).catch(function(e){ console.warn("Aviso Firestore:", e); });
            }
            aplicarUsuarioLocal(nombre, estadoAuthWeb.rolSeleccionado === "EMPRENDEDORA" ? "Emprendedora" : "Comprador");
            if (btnSubmit) {
              btnSubmit.disabled = false;
              btnSubmit.textContent = "Registrarme en Firebase";
            }
            cerrarModal("modal-auth-web");
            mostrarToast("¡Cuenta creada exitosamente en Firebase!");
          });
        })
        .catch(function (err) {
          if (btnSubmit) {
            btnSubmit.disabled = false;
            btnSubmit.textContent = "Registrarme en Firebase";
          }
          if (errorDiv) {
            errorDiv.textContent = "Error de Firebase: " + (err.message || err);
            errorDiv.style.display = "block";
          }
        });
    } else {
      firebaseAuth.signInWithEmailAndPassword(email, pass)
        .then(function (cred) {
          var user = cred.user;
          var nombreDetectado = user.displayName || email.split("@")[0];
          aplicarUsuarioLocal(nombreDetectado, estadoAuthWeb.rolSeleccionado === "EMPRENDEDORA" ? "Emprendedora" : "Comprador");
          if (btnSubmit) {
            btnSubmit.disabled = false;
            btnSubmit.textContent = "Iniciar Sesión";
          }
          cerrarModal("modal-auth-web");
          mostrarToast("¡Sesión iniciada con éxito en Firebase!");
        })
        .catch(function (err) {
          var emailLower = email.toLowerCase().trim();
          if (emailLower === "donamarta@alerce.app" || emailLower === "donarosa@alerce.app" || emailLower === "marta@alerce.app") {
            aplicarUsuarioLocal("Doña Marta", "Emprendedora (Minimarket)");
            if (btnSubmit) {
              btnSubmit.disabled = false;
              btnSubmit.textContent = "Iniciar Sesión";
            }
            cerrarModal("modal-auth-web");
            mostrarToast("¡Bienvenida Doña Marta! Sesión iniciada.");
            return;
          }

          if (btnSubmit) {
            btnSubmit.disabled = false;
            btnSubmit.textContent = "Iniciar Sesión";
          }
          if (errorDiv) {
            errorDiv.textContent = "Error: " + (err.message || "Credenciales incorrectas");
            errorDiv.style.display = "block";
          }
        });
    }
  }

  function autocompletarCredenciales(email, pass) {
    var inpEmail = document.getElementById("auth-inp-email");
    var inpPass = document.getElementById("auth-inp-password");
    if (inpEmail) inpEmail.value = email;
    if (inpPass) inpPass.value = pass;
    cambiarRolAuth("EMPRENDEDORA");
    if (estadoAuthWeb.modoRegistro) {
      toggleModoAuth();
    }
  }

  function entrarModoDev(tiendaId) {
    var tienda = null;
    for (var i = 0; i < TIENDAS.length; i++) {
      if (TIENDAS[i].id === tiendaId) {
        tienda = TIENDAS[i];
        break;
      }
    }
    var nombre = tienda ? tienda.nombre : "Doña Marta";
    aplicarUsuarioLocal(nombre, "Emprendedora (Prueba)");
    cerrarModal("modal-auth-web");
    mostrarToast("Entrando como " + nombre + " (Modo Prueba)");
  }

  function aplicarUsuarioLocal(nombre, rol) {
    estado.usuario = estado.usuario || {};
    estado.usuario.nombre = nombre;
    estado.usuario.rol = rol;
    var tag = document.getElementById("tag-nombre-usuario");
    var saludo = document.getElementById("saludo-usuario-cabecera");
    if (tag) {
      if (nombre && nombre !== "Visitante") {
        tag.textContent = nombre + " (" + rol + ")";
      } else {
        tag.textContent = "Iniciar Sesión / Mi Cuenta";
      }
    }
    if (saludo) {
      if (nombre && nombre !== "Visitante") {
        saludo.textContent = "Bienvenido(a), " + nombre + " · Vitrina de Alerce";
      } else {
        saludo.textContent = "Vitrina Comercial de Alerce · Puerto Montt";
      }
    }
  }

  function cerrarSesionWeb() {
    if (firebaseAuth) {
      try { firebaseAuth.signOut(); } catch(e){}
    }
    aplicarUsuarioLocal("Visitante", "Visitante");
    mostrarToast("Sesión cerrada");
  }

  function configurarObservadorAuthFirebase() {
    if (firebaseAuth && firebaseAuth.onAuthStateChanged) {
      firebaseAuth.onAuthStateChanged(function (user) {
        if (user) {
          var nombre = user.displayName || (user.email ? user.email.split("@")[0] : "Usuario");
          aplicarUsuarioLocal(nombre, "Usuario Firebase");
        }
      });
    }
  }

  // Exponer API global para callbacks en línea
  window.TramaWeb = {
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
    cambiarRolAuth: cambiarRolAuth,
    toggleModoAuth: toggleModoAuth,
    autocompletarCredenciales: autocompletarCredenciales,
    ejecutarAuthFirebase: ejecutarAuthFirebase,
    entrarModoDev: entrarModoDev,
    // Módulo 9: Punto de Venta (POS) y Caja Rápida
    abrirModalPOS: abrirModalPOS,
    cambiarTiendaPOS: cambiarTiendaPOS,
    agregarProductoTicketPOS: agregarProductoTicketPOS,
    modificarCantidadTicketPOS: modificarCantidadTicketPOS,
    vaciarTicketPOS: vaciarTicketPOS,
    seleccionarMedioPagoPOS: seleccionarMedioPagoPOS,
    calcularVueltoPOS: calcularVueltoPOS,
    confirmarVentaPOS: confirmarVentaPOS,
    cerrarSesionWeb: cerrarSesionWeb
  };

  // Ejecutar al cargar el DOM
  if (document.readyState === "loading") {
    document.addEventListener("DOMContentLoaded", inicializar);
  } else {
    inicializar();
  }

})();