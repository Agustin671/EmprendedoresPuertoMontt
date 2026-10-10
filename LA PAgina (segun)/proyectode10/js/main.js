/* ============================================================
   TRAMA — Lógica de la vitrina pública (main.js)
   ============================================================
   QUÉ HACE ESTE ARCHIVO (en palabras simples):

   1) CAMBIA EL TAMAÑO DE LA LETRA y ACTIVA EL ALTO CONTRASTE
      (botón flotante), igual que antes. Las elecciones se guardan.

   2) BUSCA FERIAS por nombre o lugar (buscador del encabezado).

   3) ARMA LA CARTELERA y la FICHA de cada feria, con:
      - mapa de la ubicación (RF-MAP-02),
      - botón "Cómo llegar" que abre Google Maps,
      - lista de participantes confirmadas con link a su tienda,
      - muestra del catálogo de cada participante (RF-FER-10).

   4) ARMA EL DIRECTORIO DE EMPRENDEDORAS Y ASOCIACIONES con
      búsqueda y filtros por tipo, sector y asociación (RF-CMP-01).

   5) ABRE TIENDAS, PRODUCTOS y ASOCIACIONES en ventanas:
      - variantes que cambian foto y precio (RF-CMP-03),
      - precios o "consultar precio" (RF-CFG-01),
      - contacto por WhatsApp o formulario (RF-CFG-02),
      - calificaciones si la tienda las tiene activas (RF-CMP-09).

   6) PIDE INICIAR SESIÓN para seguir o calificar y, al entrar,
      COMPLETA la acción que la persona intentaba (RF-ACC-08).

   ============================================================
   CÓMO AGREGAR DATOS NUEVOS (ferias se editan en index.html):
   - EMPRENDEDORAS, ASOCIACIONES y PRODUCTOS se agregan en las
     listas de abajo. Sigue el formato del ejemplo y usa IDs
     nuevos (e6, a3, p11...).
   ============================================================ */

(function () {
  "use strict";

  /* ==========================================================
     A) LISTAS DE DATOS DE EJEMPLO (prototipo)
     ========================================================== */

  /* ---------- EMPRENDEDORAS ----------
     Campos:
       id            : identificador único (e1, e2...)
       nombre        : nombre de la tienda
       tipo          : tipo de emprendimiento (para el filtro RF-CMP-01)
       sector        : sector de Puerto Montt (para el filtro RF-CMP-01)
       asociacionId  : asociación a la que pertenece (o "" si no tiene)
       descripcion   : texto que se muestra al comprador
       logo          : imagen de la tienda
       whatsapp      : número de WhatsApp (el botón se genera con él)
       contacto      : "whatsapp" | "formulario" | "ambos" (RF-CFG-02)
       mostrarPrecios: true o false (RF-CFG-01, si es false se muestra
                       "consultar precio")
       calificacionesActivas: true o false (RF-CFG-05, RF-CMP-09)
  */
  var EMPRENDEDORAS = [
    {
      id: "e1",
      nombre: "Cerámica de la Casa",
      tipo: "Artesanía y manualidades",
      sector: "Alerce",
      asociacionId: "a1",
      descripcion: "Jarrones, tazas y platos pintados a mano por la artesana Marta.",
      logo: "https://picsum.photos/seed/cer-casa/640/360",
      whatsapp: "56910000001",
      contacto: "ambos",
      mostrarPrecios: true,
      calificacionesActivas: true
    },
    {
      id: "e2",
      nombre: "Tejidos del Valle",
      tipo: "Artesanía y manualidades",
      sector: "Angelmó",
      asociacionId: "a1",
      descripcion: "Bufandas, gorros y mantas de lana tejidos a mano con lana de oveja.",
      logo: "https://picsum.photos/seed/tej-valle/640/360",
      whatsapp: "56910000002",
      contacto: "whatsapp",
      mostrarPrecios: true,
      calificacionesActivas: true
    },
    {
      id: "e3",
      nombre: "Dulces y Miel del Sur",
      tipo: "Alimentos y repostería",
      sector: "Mirasol",
      asociacionId: "a2",
      descripcion: "Miel pura, mermeladas y dulces caseros sin conservantes.",
      logo: "https://picsum.photos/seed/dul-sur/640/360",
      whatsapp: "56910000003",
      contacto: "formulario",
      mostrarPrecios: false,
      calificacionesActivas: false
    },
    {
      id: "e4",
      nombre: "Aromas de Puerto Montt",
      tipo: "Cosmética natural",
      sector: "Pelluco",
      asociacionId: "",
      descripcion: "Jabones y cremas naturales hechos con hierbas de la zona.",
      logo: "https://picsum.photos/seed/arom-pm/640/360",
      whatsapp: "56910000004",
      contacto: "whatsapp",
      mostrarPrecios: true,
      calificacionesActivas: true
    },
    {
      id: "e5",
      nombre: "Verde Huerta",
      tipo: "Plantas y huerta",
      sector: "Alerce",
      asociacionId: "a1",
      descripcion: "Plantas de temporada, hierbas y maceteros para la huerta de casa.",
      logo: "https://picsum.photos/seed/verde-huerta/640/360",
      whatsapp: "56910000005",
      contacto: "ambos",
      mostrarPrecios: true,
      calificacionesActivas: false
    }
  ];

  /* ---------- ASOCIACIONES (RF-CMP-01, RF-CMP-02) ----------
     `integrantes` es la lista de IDs de EMPRENDEDORAS.
  */
  var ASOCIACIONES = [
    {
      id: "a1",
      nombre: "Manos de Alerce",
      sector: "Alerce",
      descripcion: "Agrupación de artesanas y viveristas de Alerce que participan juntas en ferias.",
      logo: "https://picsum.photos/seed/manos-alerce/640/360",
      contacto: "56910000006",
      integrantes: ["e1", "e2", "e5"]
    },
    {
      id: "a2",
      nombre: "Tierra que Alimenta",
      sector: "Puerto Montt",
      descripcion: "Productoras de alimentos caseros: mermeladas, miel, panes y repostería.",
      logo: "https://picsum.photos/seed/tierra-alimenta/640/360",
      contacto: "56910000007",
      integrantes: ["e3"]
    }
  ];

  /* ---------- PRODUCTOS ----------
     Campos:
       id           : identificador único (p1, p2...)
       emprendedoraId : a qué tienda pertenece
       nombre, descripcion, categoria, tipoProducto
       precio       : número (los pesos se formatean al mostrarlo)
       modalidad    : "regular" | "a-pedido" | "pieza-unica" (RF-PRO-06)
       estado       : "disponible" | "agotado" (RF-PRO-08, RF-PRO-09).
                      Un producto "no-disponible" NO se agrega aquí,
                      porque no debe aparecer en el catálogo público
                      (RF-PRO-12, RN-09).
       imagen       : foto principal
       variantes    : lista de opciones con nombre, precio y foto
                      (RF-PRO-05, RF-CMP-03). Si está vacía, el
                      producto no tiene variantes.
  */
  var PRODUCTOS = [
    {
      id: "p1", emprendedoraId: "e1", nombre: "Jarro de greda pintado",
      descripcion: "Jarro hecho a mano y pintado con motivos del sur. Pieza única.",
      categoria: "Cerámica", tipoProducto: "Cerámica",
      precio: 18000, modalidad: "pieza-unica", estado: "disponible",
      imagen: "https://picsum.photos/seed/jarro/640/640", variantes: []
    },
    {
      id: "p2", emprendedoraId: "e1", nombre: "Tazas esmaltadas",
      descripcion: "Juego de tazas esmaltadas. Puedes elegir el color.",
      categoria: "Cerámica", tipoProducto: "Cerámica",
      precio: 12000, modalidad: "regular", estado: "disponible",
      imagen: "https://picsum.photos/seed/tazas-azul/640/640",
      variantes: [
        { nombre: "Azul", precio: 12000, imagen: "https://picsum.photos/seed/tazas-azul/640/640" },
        { nombre: "Verde", precio: 12000, imagen: "https://picsum.photos/seed/tazas-verde/640/640" },
        { nombre: "Blanco", precio: 13000, imagen: "https://picsum.photos/seed/tazas-blanco/640/640" }
      ]
    },
    {
      id: "p3", emprendedoraId: "e2", nombre: "Bufanda de lana",
      descripcion: "Tejida a mano en lana de oveja. Se hace por encargo en tu color y tamaño.",
      categoria: "Textil", tipoProducto: "Textil",
      precio: 15000, modalidad: "a-pedido", estado: "disponible",
      imagen: "https://picsum.photos/seed/bufanda/640/640",
      variantes: [
        { nombre: "Gris", precio: 15000, imagen: "https://picsum.photos/seed/bufanda-gris/640/640" },
        { nombre: "Terracota", precio: 15000, imagen: "https://picsum.photos/seed/bufanda-terracota/640/640" }
      ]
    },
    {
      id: "p4", emprendedoraId: "e2", nombre: "Gorro tejido",
      descripcion: "Gorro clásico de lana. Agotado hasta la próxima temporada.",
      categoria: "Textil", tipoProducto: "Textil",
      precio: 8000, modalidad: "regular", estado: "agotado",
      imagen: "https://picsum.photos/seed/gorro/640/640", variantes: []
    },
    {
      id: "p5", emprendedoraId: "e3", nombre: "Mermelada artesanal",
      descripcion: "Preparada con fruta de la zona y azúcar de caña. Elige el sabor.",
      categoria: "Alimentos", tipoProducto: "Mermeladas",
      precio: 4500, modalidad: "regular", estado: "disponible",
      imagen: "https://picsum.photos/seed/mermelada/640/640",
      variantes: [
        { nombre: "Frutos rojos", precio: 4500, imagen: "https://picsum.photos/seed/mermelada-rojos/640/640" },
        { nombre: "Ciruela", precio: 4500, imagen: "https://picsum.photos/seed/mermelada-ciruela/640/640" },
        { nombre: "Mora", precio: 4800, imagen: "https://picsum.photos/seed/mermelada-mora/640/640" }
      ]
    },
    {
      id: "p6", emprendedoraId: "e3", nombre: "Miel pura de abeja",
      descripcion: "Miel sin mezclas, envasada en frasco de vidrio de 500 g.",
      categoria: "Alimentos", tipoProducto: "Miel",
      precio: 8000, modalidad: "regular", estado: "disponible",
      imagen: "https://picsum.photos/seed/miel/640/640", variantes: []
    },
    {
      id: "p7", emprendedoraId: "e4", nombre: "Jabón de hierbas",
      descripcion: "Jabón natural con hierbas aromáticas. Ideal para piel sensible.",
      categoria: "Cosmética", tipoProducto: "Cosmética natural",
      precio: 3000, modalidad: "regular", estado: "disponible",
      imagen: "https://picsum.photos/seed/jabon/640/640", variantes: []
    },
    {
      id: "p8", emprendedoraId: "e4", nombre: "Bálsamo de caléndula",
      descripcion: "Cremita reparadora hecha con caléndula y aceites naturales.",
      categoria: "Cosmética", tipoProducto: "Cosmética natural",
      precio: 9000, modalidad: "a-pedido", estado: "disponible",
      imagen: "https://picsum.photos/seed/balasamo/640/640", variantes: []
    },
    {
      id: "p9", emprendedoraId: "e5", nombre: "Planta de temporada",
      descripcion: "Plantas de la huerta listas para plantar en casa.",
      categoria: "Plantas", tipoProducto: "Plantas y huerta",
      precio: 4000, modalidad: "regular", estado: "disponible",
      imagen: "https://picsum.photos/seed/planta/640/640", variantes: []
    },
    {
      id: "p10", emprendedoraId: "e5", nombre: "Macetero de hierbas",
      descripcion: "Macetero surtido con hierbas para cocina (menta, orégano, tomillo).",
      categoria: "Plantas", tipoProducto: "Plantas y huerta",
      precio: 6000, modalidad: "pieza-unica", estado: "disponible",
      imagen: "https://picsum.photos/seed/hierbas/640/640", variantes: []
    }
  ];

  /* ==========================================================
     B) FUNCIONES DE AYUDA PARA BUSCAR EN LAS LISTAS
     ========================================================== */
  function obtenerEmprendedora(id) {
    for (var i = 0; i < EMPRENDEDORAS.length; i++) {
      if (EMPRENDEDORAS[i].id === id) { return EMPRENDEDORAS[i]; }
    }
    return null;
  }

  function obtenerAsociacion(id) {
    for (var i = 0; i < ASOCIACIONES.length; i++) {
      if (ASOCIACIONES[i].id === id) { return ASOCIACIONES[i]; }
    }
    return null;
  }

  function obtenerProducto(id) {
    for (var i = 0; i < PRODUCTOS.length; i++) {
      if (PRODUCTOS[i].id === id) { return PRODUCTOS[i]; }
    }
    return null;
  }

  function productosDe(emprendedoraId) {
    var lista = [];
    PRODUCTOS.forEach(function (p) {
      if (p.emprendedoraId === emprendedoraId) { lista.push(p); }
    });
    return lista;
  }

  /* Formatea un número como pesos chilenos fácil de leer */
  function formatearPrecio(valor) {
    return "$ " + valor.toLocaleString("es-CL");
  }

  /* ==========================================================
     C) REFERENCIAS A ELEMENTOS DE LA PÁGINA
     ========================================================== */
  /* Abrir/cerrar menú de tamaño de letra */
  var botonTamanoMenu = document.getElementById("boton-tamano-menu");
  var menuTamanoOpciones = document.getElementById("menu-tamano-opciones");

  if (botonTamanoMenu && menuTamanoOpciones) {
    botonTamanoMenu.addEventListener("click", function (evento) {
      evento.stopPropagation();
      var abierto = botonTamanoMenu.getAttribute("aria-expanded") === "true";
      if (abierto) {
        cerrarMenuTamano();
      } else {
        abrirMenuTamano();
      }
    });

    function abrirMenuTamano() {
      menuTamanoOpciones.hidden = false;
      botonTamanoMenu.setAttribute("aria-expanded", "true");
      var primera = menuTamanoOpciones.querySelector(".opcion-tamano");
      if (primera) { primera.focus(); }
    }

    function cerrarMenuTamano() {
      menuTamanoOpciones.hidden = true;
      botonTamanoMenu.setAttribute("aria-expanded", "false");
      botonTamanoMenu.focus();
    }

    document.addEventListener("click", function (evento) {
      if (!evento.target.closest(".tamano-menu")) {
        cerrarMenuTamano();
      }
    });

    document.addEventListener("keydown", function (evento) {
      if (evento.key === "Escape") {
        cerrarMenuTamano();
      }
    });

    menuTamanoOpciones.addEventListener("keydown", function (evento) {
      var opciones = Array.prototype.slice.call(menuTamanoOpciones.querySelectorAll(".opcion-tamano"));
      var indice = opciones.indexOf(document.activeElement);
      if (evento.key === "ArrowDown" && indice >= 0) {
        evento.preventDefault();
        opciones[(indice + 1) % opciones.length].focus();
      } else if (evento.key === "ArrowUp" && indice >= 0) {
        evento.preventDefault();
        opciones[(indice - 1 + opciones.length) % opciones.length].focus();
      } else if (evento.key === "Tab") {
        cerrarMenuTamano();
      }
    });
  }

  /* Estado del usuario y de las acciones (RF-ACC-08) */
  var usuarioActual = cargarUsuario();   // null o { nombre: "..." }
  var accionPendiente = null;            // función que se ejecuta al entrar
  var NOMBRE_USUARIO = "trama-usuario";
  var NOMBRE_SEGUIDOS = "trama-seguidos";
  var vistaActiva = "emprendedoras";

  /* Lista de IDs que la persona sigue (RF-CMP-06), se guarda */
  var seguidos = cargarSeguidos();

  function cargarUsuario() {
    try {
      var dato = JSON.parse(localStorage.getItem(NOMBRE_USUARIO));
      return dato || null;
    } catch (e) { return null; }
  }

  function cargarSeguidos() {
    try {
      var dato = JSON.parse(localStorage.getItem(NOMBRE_SEGUIDOS));
      return Array.isArray(dato) ? dato : [];
    } catch (e) { return []; }
  }

  function guardarSeguidos() {
    localStorage.setItem(NOMBRE_SEGUIDOS, JSON.stringify(seguidos));
  }

  function elementoSeguido(id) {
    return seguidos.indexOf(id) !== -1;
  }

  /* ==========================================================
     D) TAMAÑO DE LETRA (igual que la versión anterior)
     ========================================================== */
  botonesTamano.forEach(function (boton) {
    boton.addEventListener("click", function () {
      botonesTamano.forEach(function (b) {
        b.classList.remove("boton-tamano-activo");
        b.setAttribute("aria-checked", "false");
      });
      boton.classList.add("boton-tamano-activo");
      boton.setAttribute("aria-checked", "true");
      var tamanoElegido = boton.getAttribute("data-tamano");
      aplicarTamano(tamanoElegido);
      localStorage.setItem("trama-tamano-letra", tamanoElegido);
    });
  });

  function aplicarTamano(tamano) {
    document.body.classList.remove("tamano-grande", "tamano-muy-grande");
    if (tamano === "grande") {
      document.body.classList.add("tamano-grande");
    } else if (tamano === "muy-grande") {
      document.body.classList.add("tamano-muy-grande");
    }
  }

  /* ---------- ALTO CONTRASTE (botón flotante permanente) ---------- */
  botonContraste.addEventListener("click", function () {
    var activado = document.body.classList.toggle("alto-contraste");
    botonContraste.setAttribute("aria-pressed", activado ? "true" : "false");
    botonContraste.textContent = activado ? "Modo alto contraste: activado" : "Modo alto contraste";
    localStorage.setItem("trama-alto-contraste", activado ? "si" : "no");
  });

  /* ==========================================================
     E) MENSAJE GRANDE DE CONFIRMACIÓN
     (RNF-USA-01: confirmaciones claras para cada acción)
     ========================================================== */
  botonCerrarMensaje.addEventListener("click", ocultarMensaje);

  function mostrarMensaje(texto) {
    textoConfirmacion.textContent = texto;
    mensajeConfirmacion.hidden = false;
    mensajeConfirmacion.focus();
    var temporizador = window.setTimeout(ocultarMensaje, 12000);
    mensajeConfirmacion.dataset.temporizador = temporizador;
  }

  function ocultarMensaje() {
    if (mensajeConfirmacion.dataset.temporizador) {
      window.clearTimeout(Number(mensajeConfirmacion.dataset.temporizador));
      delete mensajeConfirmacion.dataset.temporizador;
    }
    mensajeConfirmacion.hidden = true;
  }

  /* ==========================================================
     F) BUSCADOR DE FERIAS (encabezado)
     ========================================================== */
  formularioBuscador.addEventListener("submit", function (evento) {
    evento.preventDefault();
    filtrarFerias();
  });

  if (campoBusqueda) {
    campoBusqueda.addEventListener("input", filtrarFerias);
  }

  function filtrarFerias() {
    if (!grillaFerias) { return; }
    var textoBuscado = (campoBusqueda ? campoBusqueda.value : "").toLowerCase().trim();
    grillaFerias.querySelectorAll(".tarjeta").forEach(function (tarjeta) {
      var contenido = tarjeta.textContent.toLowerCase();
      var coincide = textoBuscado === "" || contenido.indexOf(textoBuscado) !== -1;
      tarjeta.hidden = !coincide;
    });
    var visibles = grillaFerias.querySelectorAll(".tarjeta:not([hidden])").length;
    avisoFerias.hidden = visibles > 0;
    avisoFerias.textContent = "No encontramos ninguna feria con ese nombre o lugar. Intenta con otra palabra.";
  }

  /* ==========================================================
     G) CARTELERA Y FICHA DE FERIAS (RF-CMP-04, RF-FER-10,
        RF-MAP-02)
     ========================================================== */

  /* Rellena la línea "Participan" de cada tarjeta usando los datos
     de las emprendedoras (atributo data-feria-participantes). */
  function rellenarParticipantesDeTarjetas() {
    grillaFerias.querySelectorAll(".tarjeta").forEach(function (tarjeta) {
      var ids = (tarjeta.getAttribute("data-feria-participantes") || "").split(",");
      var nombres = ids.map(function (id) {
        var em = obtenerEmprendedora(id.trim());
        return em ? em.nombre : "";
      }).filter(Boolean);
      var texto = nombres.length ? nombres.join(", ") : "Por confirmar";
      var objetivo = tarjeta.querySelector(".participantes-texto");
      if (objetivo) { objetivo.textContent = texto; }
    });
  }

  /* Abre la ficha pública de una feria desde su tarjeta. */
  function abrirFichaFeria(tarjeta) {
    var lat = tarjeta.getAttribute("data-feria-lat");
    var lng = tarjeta.getAttribute("data-feria-lng");
    var participantes = (tarjeta.getAttribute("data-feria-participantes") || "").split(",");

    var html = "";
    html += '<p class="etiqueta etiqueta-feria">Ficha de la feria</p>';
    html += '<h2>' + tarjeta.getAttribute("data-feria-nombre") + '</h2>';
    html += '<img class="imagen-modal" src="' + tarjeta.getAttribute("data-feria-imagen") +
            '" alt="' + tarjeta.getAttribute("data-feria-imagen-alt") + '">';

    html += '<div class="campo-ficha"><strong>Fecha y hora</strong>';
    html += escapar(tarjeta.getAttribute("data-feria-fecha")) + " de " + escapar(tarjeta.getAttribute("data-feria-horario")) + '</div>';

    html += '<div class="campo-ficha"><strong>Lugar</strong>' + escapar(tarjeta.getAttribute("data-feria-lugar")) + '</div>';
    html += '<div class="campo-ficha"><strong>Dirección</strong>' + escapar(tarjeta.getAttribute("data-feria-direccion")) + '</div>';
    html += '<div class="campo-ficha"><strong>Organiza</strong>' + escapar(tarjeta.getAttribute("data-feria-organizador")) + '</div>';
    html += '<div class="campo-ficha"><strong>Qué habrá</strong>' + escapar(tarjeta.getAttribute("data-feria-descripcion")) + '</div>';

    /* Mapa con la ubicación (RF-MAP-02) */
    html += '<div class="campo-ficha"><strong>Ubicación en el mapa</strong></div>';
    html += '<div class="mapa">' + mapaEmbebido(lat, lng) + '</div>';

    /* Botón "Cómo llegar": abre la aplicación de mapas del celular */
    html += '<a class="boton boton-principal boton-ancho" target="_blank" rel="noopener" href="' +
            'https://www.google.com/maps/dir/?api=1&destination=' + lat + ',' + lng + '">Cómo llegar (abre el mapa del celular)</a>';

    /* Participantes confirmadas, con acceso a su tienda (RF-FER-10) */
    html += '<div class="campo-ficha"><strong>Quiénes participan</strong></div>';
    html += '<ul class="lista-integrantes">';
    participantes.forEach(function (id) {
      var em = obtenerEmprendedora(id.trim());
      if (em) {
        html += '<li><button type="button" class="boton boton-integrante" data-ir-tienda="' + em.id + '">' +
                '<span><strong>' + escapar(em.nombre) + '</strong><br>(' + escapar(em.tipo) + ')</span>' +
                '<span class="boton-relleno">Ver tienda</span></button></li>';
      }
    });
    html += '</ul>';

    /* Muestra del catálogo público de cada participante (RF-FER-10) */
    html += '<div class="campo-ficha"><strong>Una muestra de lo que llevarán</strong></div>';
    html += '<div class="muestra-catalogo">';
    participantes.forEach(function (id) {
      var em = obtenerEmprendedora(id.trim());
      if (em) {
        productosDe(em.id).slice(0, 2).forEach(function (p) {
          html += '<div class="producto-mini" tabindex="0" role="button" data-ir-producto="' + p.id + '" ' +
                  'aria-label="Ver detalle de ' + escapar(p.nombre) + '">' +
                  '<img src="' + p.imagen + '" alt="Foto de ' + escapar(p.nombre) + '">' +
                  '<strong>' + escapar(p.nombre) + '</strong>' +
                  '<span>' + etiquetasDeProducto(p, em, true) + '</span></div>';
        });
      }
    });
    html += '</div>';

    document.getElementById("modal-feria-cuerpo").innerHTML = html;
    abrirModal(document.getElementById("modal-feria"));
  }

  /* Crea el iframe del mapa (OpenStreetMap, sin necesidad de clave) */
  function mapaEmbebido(lat, lng) {
    var delta = 0.006;
    var oeste = parseFloat(lng) - delta;
    var este = parseFloat(lng) + delta;
    var sur = parseFloat(lat) - delta;
    var norte = parseFloat(lat) + delta;
    var url = "https://www.openstreetmap.org/export/embed.html?bbox=" +
              oeste + "%2C" + sur + "%2C" + este + "%2C" + norte +
              "&layer=mapnik&marker=" + lat + "%2C" + lng;
    return '<iframe src="' + url + '" title="Mapa de la feria" loading="lazy"></iframe>';
  }

  /* ==========================================================
     H) DIRECTORIO (RF-CMP-01): filtros, pestañas y tarjetas
     ========================================================== */

  /* Llena las listas desplegables de filtros con valores únicos */
  function prepararFiltros() {
    var tipos = {}, sectores = {}, asos = {};
    EMPRENDEDORAS.forEach(function (em) {
      tipos[em.tipo] = true;
      sectores[em.sector] = true;
      if (em.asociacionId) { asos[em.asociacionId] = true; }
    });
    llenarSelect(filtroTipo, tipos);
    llenarSelect(filtroSector, sectores);
    var opcionesAsociaciones = {};
    ASOCIACIONES.forEach(function (a) { opcionesAsociaciones[a.id] = a.nombre; });
    llenarSelect(filtroAsociacion, opcionesAsociaciones, true);
  }

  function llenarSelect(select, mapaValores, usarNombres) {
    select.innerHTML = usarNombres ? '<option value="">Todas</option>' :
                                     '<option value="">Todos</option>';
    Object.keys(mapaValores).forEach(function (clave) {
      var opcion = document.createElement("option");
      opcion.value = clave;
      opcion.textContent = usarNombres ? mapaValores[clave] : clave;
      select.appendChild(opcion);
    });
  }

  /* Pestañas Emprendedoras / Asociaciones */
  botonesTab.forEach(function (tab) {
    tab.addEventListener("click", function () {
      botonesTab.forEach(function (t) {
        t.classList.remove("tab-activo");
        t.setAttribute("aria-selected", "false");
      });
      tab.classList.add("tab-activo");
      tab.setAttribute("aria-selected", "true");
      vistaActiva = tab.getAttribute("data-vista");
      mostrarDirectorio();
    });
  });

  /* Botón "Borrar filtros" (vuelve a mostrar todo) */
  botonLimpiarFiltros.addEventListener("click", function () {
    campoDirBusqueda.value = "";
    filtroTipo.value = "";
    filtroSector.value = "";
    filtroAsociacion.value = "";
    mostrarDirectorio();
  });

  [campoDirBusqueda, filtroTipo, filtroSector, filtroAsociacion].forEach(function (campo) {
    campo.addEventListener("input", mostrarDirectorio);
    campo.addEventListener("change", mostrarDirectorio);
  });

  function mostrarDirectorio() {
    var busqueda = (campoDirBusqueda.value || "").toLowerCase().trim();
    var tipo = filtroTipo.value;
    var sector = filtroSector.value;
    var asociacion = filtroAsociacion.value;

    var html = "";

    if (vistaActiva === "asociaciones") {
      html += ASOCIACIONES.filter(function (a) {
        if (sector && a.sector !== sector) { return false; }
        if (busqueda && a.nombre.toLowerCase().indexOf(busqueda) === -1) { return false; }
        return true;
      }).map(tarjetaAsociacion).join("");
    } else {
      html += EMPRENDEDORAS.filter(function (em) {
        if (tipo && em.tipo !== tipo) { return false; }
        if (sector && em.sector !== sector) { return false; }
        if (asociacion && em.asociacionId !== asociacion) { return false; }
        if (busqueda) {
          var texto = (em.nombre + " " + em.tipo + " " + em.sector).toLowerCase();
          if (texto.indexOf(busqueda) === -1) { return false; }
        }
        return true;
      }).map(tarjetaEmprendedora).join("");
    }

    if (html === "") {
      grillaDirectorio.innerHTML = "";
      avisoDirectorio.hidden = false;
      avisoDirectorio.textContent = "No encontramos resultados con esos filtros. Prueba con otros o presiona 'Borrar filtros'.";
    } else {
      grillaDirectorio.innerHTML = html;
      avisoDirectorio.hidden = true;
    }
  }

  /* Tarjeta de una emprendedora en el directorio */
  function tarjetaEmprendedora(em) {
    var aso = em.asociacionId ? obtenerAsociacion(em.asociacionId) : null;
    var siguiendo = elementoSeguido(em.id);
    return '<article class="tarjeta">' +
      '<img src="' + em.logo + '" alt="Foto de la tienda ' + escapar(em.nombre) + '">' +
      '<div class="tarjeta-contenido">' +
        '<h3>' + escapar(em.nombre) + '</h3>' +
        '<p><span class="etiqueta etiqueta-categoria">' + escapar(em.tipo) + '</span></p>' +
        '<p>Sector: <strong>' + escapar(em.sector) + '</strong></p>' +
        (aso ? '<p>Asociación: <strong>' + escapar(aso.nombre) + '</strong></p>' : '') +
        '<p>' + escapar(em.descripcion) + '</p>' +
        '<div class="tarjeta-botones">' +
          '<button type="button" class="boton boton-principal" data-ir-tienda="' + em.id + '">Ver tienda</button>' +
          '<button type="button" class="boton ' + (siguiendo ? 'boton-siguiendo' : 'boton-seguir') +
            '" data-seguir="' + em.id + '" data-tipo-seguir="tienda" aria-pressed="' + siguiendo + '">' +
            (siguiendo ? "Siguiendo (Dejar de seguir)" : "Seguir") + '</button>' +
        '</div>' +
      '</div></article>';
  }

  /* Tarjeta de una asociación en el directorio */
  function tarjetaAsociacion(a) {
    var siguiendo = elementoSeguido(a.id);
    return '<article class="tarjeta">' +
      '<img src="' + a.logo + '" alt="Logo de la asociación ' + escapar(a.nombre) + '">' +
      '<div class="tarjeta-contenido">' +
        '<h3>' + escapar(a.nombre) + '</h3>' +
        '<p>Sector: <strong>' + escapar(a.sector) + '</strong></p>' +
        '<p>Integrantes: <strong>' + a.integrantes.length + '</strong> tiendas</p>' +
        '<p>' + escapar(a.descripcion) + '</p>' +
        '<div class="tarjeta-botones">' +
          '<button type="button" class="boton boton-principal" data-ir-asociacion="' + a.id + '">Ver asociación</button>' +
          '<button type="button" class="boton ' + (siguiendo ? 'boton-siguiendo' : 'boton-seguir') +
            '" data-seguir="' + a.id + '" data-tipo-seguir="asociacion" aria-pressed="' + siguiendo + '">' +
            (siguiendo ? "Siguiendo (Dejar de seguir)" : "Seguir") + '</button>' +
        '</div>' +
      '</div></article>';
  }

  /* ==========================================================
     I) TIENDA (RF-CMP-02) Y DETALLE DE PRODUCTO (RF-CMP-03)
     ========================================================== */

  function abrirTienda(id) {
    var em = obtenerEmprendedora(id);
    if (!em) { return; }
    var aso = em.asociacionId ? obtenerAsociacion(em.asociacionId) : null;
    var siguiendo = elementoSeguido(em.id);
    var contacto = botonesContacto(em);

    var html = "";
    html += '<img class="imagen-modal" src="' + em.logo + '" alt="Foto de la tienda ' + escapar(em.nombre) + '">';
    html += '<h2>' + escapar(em.nombre) + '</h2>';
    html += '<p><span class="etiqueta etiqueta-categoria">' + escapar(em.tipo) + '</span>' +
            '<span class="etiqueta etiqueta-categoria">Sector: ' + escapar(em.sector) + '</span></p>';
    html += '<p class="detalle-descripcion">' + escapar(em.descripcion) + '</p>';
    if (aso) {
      html += '<p>Pertenece a la asociación <strong>' + escapar(aso.nombre) + '</strong>.</p>';
    }

    html += '<div class="tarjeta-botones">' +
      '<button type="button" class="boton ' + (siguiendo ? 'boton-siguiendo' : 'boton-seguir') +
        '" data-seguir="' + em.id + '" data-tipo-seguir="tienda" aria-pressed="' + siguiendo + '">' +
        (siguiendo ? "Siguiendo (Dejar de seguir)" : "Seguir esta tienda") + '</button>' +
      contacto +
    '</div>';

    /* Catálogo de la tienda */
    var productos = productosDe(em.id);
    html += '<div class="campo-ficha"><strong>Productos (' + productos.length + ')</strong><p class="ayuda-tarea">Presiona un producto para ver su detalle.</p></div>';
    html += '<div class="muestra-catalogo">';
    productos.forEach(function (p) {
      html += '<div class="producto-mini" tabindex="0" role="button" data-ir-producto="' + p.id + '" ' +
              'aria-label="Ver detalle de ' + escapar(p.nombre) + '">' +
              '<img src="' + p.imagen + '" alt="Foto de ' + escapar(p.nombre) + '">' +
              '<strong>' + escapar(p.nombre) + '</strong>' +
              '<span>' + etiquetasDeProducto(p, em, false) + '</span></div>';
    });
    html += '</div>';

    /* Calificaciones de la tienda (RF-CMP-09, solo si están activas) */
    if (em.calificacionesActivas) {
      html += '<div class="campo-ficha"><strong>Calificar esta tienda</strong></div>' +
              '<div class="estrellas" data-calificar-tienda="' + em.id + '">' +
              estrellasHTML(em.nombre) + '</div>';
    }

    document.getElementById("modal-tienda-cuerpo").innerHTML = html;
    abrirModal(document.getElementById("modal-tienda"));
  }

  /* Etiquetas que ve el comprador en el catálogo público (RF-PRO-12) */
  function etiquetasDeProducto(p, em, textoCorto) {
    var resultado = "";
    if (p.modalidad === "a-pedido") { resultado += '<span class="etiqueta etiqueta-a-pedido">A pedido</span>'; }
    if (p.modalidad === "pieza-unica") { resultado += '<span class="etiqueta etiqueta-pieza-unica">Pieza única</span>'; }
    if (p.estado === "agotado") { resultado += '<span class="etiqueta etiqueta-agotado">Agotado</span>'; }
    resultado += '<span class="etiqueta etiqueta-categoria">' + escapar(p.categoria) + '</span>';
    if (!em.mostrarPrecios) {
      resultado += '<span class="etiqueta etiqueta-consultar-precio">Consultar precio</span>';
    }
    return resultado;
  }

  /* Botones de contacto según la configuración de la tienda (RF-CFG-02) */
  function botonesContacto(em) {
    var html = "";
    if (em.contacto === "whatsapp" || em.contacto === "ambos") {
      html += '<a class="boton boton-whatsapp" target="_blank" rel="noopener" href="' +
              'https://wa.me/' + em.whatsapp + '?text=' +
              encodeURIComponent("Hola, vi tu tienda en Trama y quiero saber más.") +
              '">Contactar por WhatsApp</a>';
    }
    if (em.contacto === "formulario" || em.contacto === "ambos") {
      html += '<button type="button" class="boton boton-principal" data-contactar-formulario="' + em.id + '">' +
              'Enviar mensaje por formulario</button>';
    }
    return html;
  }

  function abrirProducto(id) {
    var p = obtenerProducto(id);
    if (!p) { return; }
    var em = obtenerEmprendedora(p.emprendedoraId);
    if (!em) { return; }

    var variante = null;
    if (p.variantes.length) { variante = p.variantes[0]; }

    var html = '<div class="detalle-producto">';
    html += '<div><img class="imagen-producto" id="imagen-producto" src="' + (variante ? variante.imagen : p.imagen) +
            '" alt="Foto de ' + escapar(p.nombre) + '"></div>';
    html += '<div>';
    html += '<h2>' + escapar(p.nombre) + '</h2>';
    html += '<p>' + etiquetasDeProducto(p, em, false) + '</p>';
    html += '<p class="detalle-precio" id="precio-producto">' +
            (em.mostrarPrecios ? formatearPrecio(variante ? variante.precio : p.precio) : "Consultar precio") + '</p>';
    html += '<p class="detalle-descripcion">' + escapar(p.descripcion) + '</p>';

    /* Variantes: al seleccionar una se cambia foto y precio (RF-CMP-03) */
    if (p.variantes.length) {
      html += '<p class="campo-ficha"><strong>Elige una opción:</strong></p>';
      html += '<div class="variantes" id="contenedor-variantes">';
      p.variantes.forEach(function (v, indice) {
        html += '<button type="button" class="boton boton-variante' + (indice === 0 ? ' boton-variante-activo' : '') +
                '" data-precio="' + v.precio + '" data-imagen="' + v.imagen + '">' +
                escapar(v.nombre) + '</button>';
      });
      html += '</div>';
    }

    html += '<div class="campo-ficha"><strong>Tienda</strong>' +
            '<button type="button" class="boton boton-secundario boton-ancho" data-ir-tienda="' + em.id + '">' +
            'Ir a la tienda de ' + escapar(em.nombre) + '</button></div>';

    /* Contacto según la configuración (RF-CFG-02, RF-CMP-05) */
    html += '<div class="tarjeta-botones">' + botonesContacto(em, 'Producto') + '</div>';

    /* Calificaciones solo si la tienda las tiene activas (RF-CMP-09) */
    if (em.calificacionesActivas) {
      html += '<div class="campo-ficha"><strong>Calificar este producto</strong></div>' +
              '<div class="estrellas" data-calificar-producto="' + p.id + '">' +
              estrellasHTML(p.nombre) + '</div>';
    }
    html += '</div></div>';

    document.getElementById("modal-producto-cuerpo").innerHTML = html;
    abrirModal(document.getElementById("modal-producto"));

    /* Comportamiento de las variantes (foto + precio al tocar) */
    var contenedor = document.getElementById("contenedor-variantes");
    if (contenedor) {
      contenedor.querySelectorAll(".boton-variante").forEach(function (boton) {
        boton.addEventListener("click", function () {
          contenedor.querySelectorAll(".boton-variante").forEach(function (b) {
            b.classList.remove("boton-variante-activo");
          });
          boton.classList.add("boton-variante-activo");
          var imagen = document.getElementById("imagen-producto");
          var precio = document.getElementById("precio-producto");
          if (imagen) { imagen.src = boton.getAttribute("data-imagen"); }
          if (precio && em.mostrarPrecios) {
            precio.textContent = formatearPrecio(Number(boton.getAttribute("data-precio")));
          }
        });
      });
    }
  }

  /* Estrellas de calificación (RF-CMP-09) */
  function estrellasHTML(nombre) {
    var html = "";
    for (var n = 1; n <= 5; n++) {
      html += '<button type="button" class="boton-estrella" data-estrellas="' + n +
              '" aria-label="Calificar con ' + n + ' de 5 estrellas">' + n + '</button>';
    }
    return html + '<p class="auxilio" id="base-estrellas">' +
           '<span class="solo-lector">La escala es de 1 a 5, siendo 5 la mejor nota.</span></p>';
  }

  /* ==========================================================
     J) ASOCIACIÓN (RF-CMP-02, RF-ASO-03)
     ========================================================== */
  function abrirAsociacion(id) {
    var a = obtenerAsociacion(id);
    if (!a) { return; }
    var siguiendo = elementoSeguido(a.id);

    var html = "";
    html += '<img class="imagen-modal" src="' + a.logo + '" alt="Logo de la asociación ' + escapar(a.nombre) + '">';
    html += '<h2>' + escapar(a.nombre) + '</h2>';
    html += '<p><span class="etiqueta etiqueta-categoria">Sector: ' + escapar(a.sector) + '</span></p>';
    html += '<p class="detalle-descripcion">' + escapar(a.descripcion) + '</p>';

    html += '<div class="tarjeta-botones">' +
      '<button type="button" class="boton ' + (siguiendo ? 'boton-siguiendo' : 'boton-seguir') +
        '" data-seguir="' + a.id + '" data-tipo-seguir="asociacion" aria-pressed="' + siguiendo + '">' +
        (siguiendo ? "Siguiendo (Dejar de seguir)" : "Seguir asociación") + '</button>' +
      '<a class="boton boton-whatsapp" target="_blank" rel="noopener" href="https://wa.me/' + a.contacto + '">Contactar por WhatsApp</a>' +
    '</div>';

    html += '<div class="campo-ficha"><strong>Sus integrantes (' + a.integrantes.length + ' tiendas)</strong></div>';
    html += '<ul class="lista-integrantes">';
    a.integrantes.forEach(function (empId) {
      var em = obtenerEmprendedora(empId);
      if (em) {
        html += '<li><button type="button" class="boton boton-integrante" data-ir-tienda="' + em.id + '">' +
                '<strong>' + escapar(em.nombre) + '</strong><span>' + escapar(em.tipo) + '</span></button></li>';
      }
    });
    html += '</ul>';

    document.getElementById("modal-asociacion-cuerpo").innerHTML = html;
    abrirModal(document.getElementById("modal-asociacion"));
  }

  /* ==========================================================
     K) FORMULARIO DE CONTACTO (RF-CFG-02, RF-CMP-05)
     ========================================================== */
  function abrirFormularioContacto(emId) {
    var em = obtenerEmprendedora(emId);
    if (!em) { return; }
    var html = "";
    html += '<h2>Enviar mensaje a ' + escapar(em.nombre) + '</h2>';
    html += '<p class="ayuda-tarea">Este mensaje llega a la emprendedora para que te responda cuando pueda.</p>';
    html += '<form id="formulario-contacto" class="formulario-login">';
    html += '<label for="contacto-nombre">Tu nombre</label>';
    html += '<input type="text" id="contacto-nombre" placeholder="Escribe tu nombre" required>';
    html += '<label for="contacto-mensaje">Tu mensaje</label>';
    html += '<textarea id="contacto-mensaje" rows="4" placeholder="Escribe aquí tu pregunta o pedido" required></textarea>';
    html += '<button type="submit" class="boton boton-principal boton-ancho">Enviar mensaje</button>';
    html += '</form>';
    html += '<p class="nota-login">Prototipo: el mensaje no se envía realmente, solo simula la confirmación.</p>';

    document.getElementById("modal-contacto-cuerpo").innerHTML = html;
    abrirModal(document.getElementById("modal-contacto"));

    document.getElementById("formulario-contacto").addEventListener("submit", function (evento) {
      evento.preventDefault();
      cerrarModal(document.getElementById("modal-contacto"));
      mostrarMensaje("¡Mensaje enviado a " + em.nombre + "! Te responderá a la brevedad. Tu mensaje queda guardado aunque esté fuera de su horario (RF-CFG-04).");
    });
  }

  /* ==========================================================
     L) SEGUIR / DEJAR DE SEGUIR (RF-CMP-06) Y CALIFICAR
        (RF-CMP-09), CON ACCESO POR CUENTA (RF-ACC-08)
     ========================================================== */

  /* Solicita iniciar sesión y, al terminar, ejecuta lo pendiente */
  function pideIniciarSesion(motivo, accion) {
    loginMotivo.textContent = motivo;
    loginMotivo.hidden = false;
    accionPendiente = accion;
    abrirModal(modalLogin);
  }

  function entrarConCuenta(nombre) {
    usuarioActual = { nombre: nombre };
    localStorage.setItem(NOMBRE_USUARIO, JSON.stringify(usuarioActual));
    actualizarEstadoUsuario();

    /* Pide permiso de notificaciones del navegador (RF-CMP-08).
       Es una simulación: la notificación ocurre solo si el
       navegador lo permite. */
    if ("Notification" in window && Notification.permission === "default") {
      try { Notification.requestPermission(); } catch (e) { /* el navegador no lo permite */ }
    }

    cerrarModal(modalLogin);
    mostrarMensaje("¡Bienvenida/o, " + nombre + "! Ahora puedes seguir tiendas y calificar.");

    /* Completa la acción que estaba pendiente (RF-ACC-08) */
    if (typeof accionPendiente === "function") {
      var accion = accionPendiente;
      accionPendiente = null;
      accion();
    }
  }

  function actualizarEstadoUsuario() {
    if (usuarioActual) {
      botonEntrar.hidden = true;
      usuarioLogueado.hidden = false;
      usuarioSaludo.textContent = "Hola, " + usuarioActual.nombre;
    } else {
      botonEntrar.hidden = false;
      usuarioLogueado.hidden = true;
    }
  }

  botonEntrar.addEventListener("click", function () {
    loginMotivo.hidden = true;
    accionPendiente = null;
    abrirModal(modalLogin);
  });

  botonLoginGoogle.addEventListener("click", function () {
    entrarConCuenta("Compradora de Puerto Montt");
  });

  formularioLogin.addEventListener("submit", function (evento) {
    evento.preventDefault();
    var correo = (document.getElementById("login-correo").value || "").trim();
    entrarConCuenta(correo ? correo.split("@")[0] : "Compradora");
  });

  botonSalir.addEventListener("click", function () {
    usuarioActual = null;
    localStorage.removeItem(NOMBRE_USUARIO);
    actualizarEstadoUsuario();
    mostrarMensaje("Cerraste sesión. Puedes seguir navegando como visitante (RF-ACC-07).");
  });

  /* Acción de seguir o dejar de seguir (se usa en varios lugares) */
  function alternarSeguimiento(boton) {
    var id = boton.getAttribute("data-seguir");
    var tipo = boton.getAttribute("data-tipo-seguir");
    var nombre = tipo === "asociacion"
      ? (obtenerAsociacion(id) ? obtenerAsociacion(id).nombre : "")
      : (obtenerEmprendedora(id) ? obtenerEmprendedora(id).nombre : "");

    if (!usuarioActual) {
      pideIniciarSesion("Para seguir " + (tipo === "asociacion" ? "una asociación" : "una tienda") + " primero debes iniciar sesión.", function () {
        alternarSeguimiento(boton);
      });
      return;
    }

    if (elementoSeguido(id)) {
      seguidos = seguidos.filter(function (x) { return x !== id; });
      mostrarMensaje("Dejaste de seguir " + nombre + ". Ya no recibirás sus avisos de ferias.");
    } else {
      seguidos.push(id);
      mostrarMensaje("¡Ahora sigues " + nombre + "! Te avisaremos cuando confirme su participación en una feria (RF-CMP-07/08).");
    }
    guardarSeguidos();

    /* Actualiza el botón que se acaba de presionar */
    var ahoraSiguiendo = elementoSeguido(id);
    boton.classList.toggle("boton-siguiendo", ahoraSiguiendo);
    boton.classList.toggle("boton-seguir", !ahoraSiguiendo);
    boton.setAttribute("aria-pressed", ahoraSiguiendo ? "true" : "false");
    if (boton.getAttribute("data-tipo-seguir") === "asociacion") {
      boton.textContent = ahoraSiguiendo ? "Siguiendo (Dejar de seguir)" : "Seguir asociación";
    } else {
      boton.textContent = ahoraSiguiendo ? "Siguiendo (Dejar de seguir)" : "Seguir";
    }
  }

  /* ==========================================================
     M) ABRIR Y CERRAR VENTANAS (MODALES)
     Mueve el foco al botón "Cerrar" y devuelve el foco al cerrar,
     para que el teclado funcione sin perderse (RNF-USA-01).
     ========================================================== */
  var ultimoFoco = null;

  function abrirModal(modal) {
    if (!modal) { return; }
    ultimoFoco = document.activeElement;
    modal.hidden = false;
    document.body.style.overflow = "hidden";   // evita que la página se desplace
    var botonCerrar = modal.querySelector("[data-cerrar-modal]");
    if (botonCerrar) { botonCerrar.focus(); }
  }

  function cerrarModal(modal) {
    if (!modal) { return; }
    modal.hidden = true;
    document.body.style.overflow = "";
    if (ultimoFoco && ultimoFoco.focus) { ultimoFoco.focus(); }
  }

  /* Botones "Cerrar esta ventana" de todos los modales */
  document.querySelectorAll("[data-cerrar-modal]").forEach(function (boton) {
    boton.addEventListener("click", function () {
      cerrarModal(boton.closest(".modal"));
    });
  });

  /* Tecla Escape para cerrar la ventana abierta */
  document.addEventListener("keydown", function (evento) {
    if (evento.key === "Escape") {
      var abierta = document.querySelector(".modal:not([hidden])");
      if (abierta) { cerrarModal(abierta); }
    }
  });

  /* Clic fuera del panel cierra la ventana */
  document.querySelectorAll(".modal").forEach(function (modal) {
    modal.addEventListener("click", function (evento) {
      if (evento.target === modal) { cerrarModal(modal); }
    });
  });

  /* ==========================================================
     N) EVENTOS "VIVOS" (delegación)
     El documento escucha los clics y decide qué hacer según
     data-* del botón. Así funcionan los botones creados en las
     ventanas sin volver a enlazarlos.
     ========================================================== */
  document.addEventListener("click", function (evento) {
    var boton = evento.target.closest("[data-seguir],[data-ir-tienda],[data-ir-producto],[data-ir-asociacion],[data-contactar-formulario],[data-calificar-tienda],[data-calificar-producto],[data-estrellas],.boton-ver-feria");
    if (!boton) { return; }

    if (boton.hasAttribute("data-seguir")) {
      alternarSeguimiento(boton);
    } else if (boton.hasAttribute("data-ir-tienda")) {
      cerrarModal(boton.closest(".modal"));
      abrirTienda(boton.getAttribute("data-ir-tienda"));
    } else if (boton.hasAttribute("data-ir-producto")) {
      cerrarModal(boton.closest(".modal"));
      abrirProducto(boton.getAttribute("data-ir-producto"));
    } else if (boton.hasAttribute("data-ir-asociacion")) {
      cerrarModal(boton.closest(".modal"));
      abrirAsociacion(boton.getAttribute("data-ir-asociacion"));
    } else if (boton.hasAttribute("data-contactar-formulario")) {
      abrirFormularioContacto(boton.getAttribute("data-contactar-formulario"));
    } else if (boton.hasAttribute("data-calificar-tienda")) {
      calificar(boton.getAttribute("data-calificar-tienda"), "tienda");
    } else if (boton.hasAttribute("data-calificar-producto")) {
      calificar(boton.getAttribute("data-calificar-producto"), "producto");
    } else if (boton.hasAttribute("data-estrellas")) {
      calificarDesdeEstrella(boton);
    } else if (boton.classList.contains("boton-ver-feria")) {
      abrirFichaFeria(boton.closest(".tarjeta"));
    }
  });

  /* Las minis de producto responden al toque o a la tecla Enter. */
  document.addEventListener("keydown", function (evento) {
    if (evento.key !== "Enter" && evento.key !== " ") { return; }
    var boton = evento.target.closest("[role='button'][data-ir-producto]");
    if (boton) {
      evento.preventDefault();
      cerrarModal(boton.closest(".modal"));
      abrirProducto(boton.getAttribute("data-ir-producto"));
    }
  });

  /* Calificar una tienda o un producto pidiendo cuenta primero (RF-ACC-08) */
  function calificar(id, tipo) {
    if (!usuarioActual) {
      pideIniciarSesion("Para calificar primero debes iniciar sesión.", function () {
        calificar(id, tipo);
      });
      return;
    }
    var nombre = tipo === "tienda"
      ? (obtenerEmprendedora(id) ? obtenerEmprendedora(id).nombre : "")
      : (obtenerProducto(id) ? obtenerProducto(id).nombre : "");
    mostrarMensaje("Gracias. Presiona una estrella (de 1 a 5) para calificar " + nombre + ".");
  }

  /* Cuando se toca una estrella, se guarda la calificación (RF-CMP-09) */
  function calificarDesdeEstrella(estrella) {
    if (!usuarioActual) {
      pideIniciarSesion("Para calificar primero debes iniciar sesión.", function () {
        calificarDesdeEstrella(estrella);
      });
      return;
    }
    var contenedor = estrella.closest(".estrellas");
    if (!contenedor) { return; }
    var cantidad = estrella.getAttribute("data-estrellas");
    var tipo = contenedor.hasAttribute("data-calificar-tienda") ? "tienda" : "producto";
    var id = contenedor.getAttribute("data-calificar-tienda") || contenedor.getAttribute("data-calificar-producto");
    var nombre = tipo === "tienda"
      ? (obtenerEmprendedora(id) ? obtenerEmprendedora(id).nombre : "")
      : (obtenerProducto(id) ? obtenerProducto(id).nombre : "");

    /* Marca visualmente las estrellas seleccionadas */
    contenedor.querySelectorAll(".boton-estrella").forEach(function (b) {
      b.classList.toggle("activa", Number(b.getAttribute("data-estrellas")) <= Number(cantidad));
    });

    mostrarMensaje("¡Gracias! Calificaste " + nombre + " con " + cantidad + " de 5 estrellas.");
  }

  /* ==========================================================
     O) UTILIDADES
     ========================================================== */
  /* Escapa caracteres especiales para que el texto se muestre
     literalmente (evita problemas con comillas y signos). */
  function escapar(texto) {
    return String(texto || "")
      .replace(/&/g, "&amp;")
      .replace(/</g, "&lt;")
      .replace(/>/g, "&gt;")
      .replace(/"/g, "&quot;");
  }

  /* ==========================================================
     P) ARRANQUE DE LA PÁGINA
     ========================================================== */
  function restaurarPreferencias() {
    var tamano = localStorage.getItem("trama-tamano-letra");
    if (tamano) {
      aplicarTamano(tamano);
      botonesTamano.forEach(function (b) {
        var elegido = b.getAttribute("data-tamano") === tamano;
        b.classList.toggle("boton-tamano-activo", elegido);
        b.setAttribute("aria-checked", elegido ? "true" : "false");
      });
    }
    if (localStorage.getItem("trama-alto-contraste") === "si") {
      document.body.classList.add("alto-contraste");
      botonContraste.setAttribute("aria-pressed", "true");
      botonContraste.textContent = "Modo alto contraste: activado";
    }
  }

  /* Se ejecuta cuando la página ya está lista */
  function iniciar() {
    restaurarPreferencias();
    actualizarEstadoUsuario();
    rellenarParticipantesDeTarjetas();
    prepararFiltros();
    mostrarDirectorio();

    /* Mensaje breve de bienvenida con el tamaño grande activo */
    console.log("Trama: vitrina pública lista.");
  }

  /* Espera a que el HTML esté listo para ejecutar iniciar() */
  if (document.readyState === "loading") {
    document.addEventListener("DOMContentLoaded", iniciar);
  } else {
    iniciar();
  }
})();