/**
 * TRAMA ALERCE — Lógica Web Interactiva (Farfetch & Apple Style)
 * Sincronizado con la App Android: Carrito, Códigos QR, Muro en Vivo,
 * Catálogo, Agendamiento y Accesibilidad.
 */

(function () {
  "use strict";

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
      nombre: "Minimarket y Provisiones Doña Rosa",
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

  var PRODUCTOS = [
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
          autorNombre: "Paul M.",
          esEmprendedor: false,
          texto: "¿Doña Rosa, le queda queso de campo artesanal para acompañar el pancito?",
          fecha: "Hoy, 16:45"
        },
        {
          id: "resp_2",
          autorNombre: "Minimarket Doña Rosa",
          esEmprendedor: true,
          texto: "¡Hola Paul! Sí vecino, nos llegaron 3 piezas recién traídas de Los Muermos. Te guardo una si gustas.",
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
      nombre: "Paul Morales",
      rol: "Comprador / Vecino"
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
      html += '    <div>';
      html += '      <h4 class="prod-nombre">' + escaparHtml(p.nombre) + '</h4>';
      html += '      <p style="font-size: 12px; color: var(--gris-texto); margin-top: 2px;">' + escaparHtml(p.descripcion) + '</p>';
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
      autorNombre: "Paul M. (Vecino)",
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
        if (confirm("¿Deseas cerrar la sesión de Paul?")) {
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
    abrirModal: abrirModal
  };

  // Ejecutar al cargar el DOM
  if (document.readyState === "loading") {
    document.addEventListener("DOMContentLoaded", inicializar);
  } else {
    inicializar();
  }

})();