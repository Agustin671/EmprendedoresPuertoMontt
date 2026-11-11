/**
 * Reglas del catálogo compartidas por la vitrina y la gestión web.
 * Son las mismas que aplica el servidor (supabase/05_catalogo_ventas.sql,
 * estado_publico_producto) y la app (data/ReglasCatalogo.kt). El contrato
 * está en docs/entrega5/contrato-catalogo.md.
 *
 * Sin dependencias ni acceso a la red: se prueba con `node --test tests/web`.
 */
(function (raiz, fabrica) {
  "use strict";
  var modulo = fabrica();
  if (typeof module === "object" && module.exports) module.exports = modulo;
  else raiz.AlerceCatalogo = modulo;
})(typeof self !== "undefined" ? self : this, function () {
  "use strict";

  var MODALIDADES = ["REGULAR", "A_PEDIDO", "PIEZA_UNICA"];
  var AL_AGOTARSE = ["MOSTRAR_AGOTADO", "OCULTAR", "PASAR_A_PEDIDO"];
  var CATEGORIAS = {
    TEXTIL: "Textil", CUERO: "Cuero", JOYERIA: "Joyería",
    COSMETICA: "Cosmética", GOURMET: "Gourmet", DECORACION: "Decoración"
  };
  var ETIQUETA_MODALIDAD = { REGULAR: "Regular", A_PEDIDO: "A pedido", PIEZA_UNICA: "Pieza única" };
  var ETIQUETA_AL_AGOTARSE = {
    MOSTRAR_AGOTADO: "Mostrar «Agotado»", OCULTAR: "Ocultar", PASAR_A_PEDIDO: "Pasar a «A pedido»"
  };
  var MEDIOS_PAGO = { Efectivo: "EFECTIVO", Transferencia: "TRANSFERENCIA", Tarjeta: "DEBITO", QR: "QR" };

  function entero(valor) {
    var n = typeof valor === "number" ? valor : (typeof valor === "string" && valor.trim() !== "" ? Number(valor) : NaN);
    if (!isFinite(n)) return 0;
    return Math.max(0, Math.floor(n));
  }

  function numeroONulo(valor) {
    if (valor === null || valor === undefined || valor === "") return null;
    var n = Number(valor);
    return isFinite(n) && n >= 0 ? n : null;
  }

  /** Solo URLs https (o http en localhost para desarrollo). Nada de javascript:, data: ni rutas relativas. */
  function urlImagenSegura(url) {
    if (typeof url !== "string") return "";
    var limpia = url.trim();
    if (/^https:\/\/[^\s"'<>]+$/i.test(limpia)) return limpia;
    if (/^http:\/\/(localhost|127\.0\.0\.1)(:\d+)?\/[^\s"'<>]*$/i.test(limpia)) return limpia;
    return "";
  }

  function colorHex(valor, respaldo) {
    if (typeof valor === "string" && /^#[0-9a-f]{6}$/i.test(valor)) return valor.toUpperCase();
    if (typeof valor === "number" && isFinite(valor)) {
      return "#" + (Number(valor) & 0xFFFFFF).toString(16).padStart(6, "0").toUpperCase();
    }
    return respaldo || "#4A5568";
  }

  function normalizarOpcion(v, productoId, indice) {
    v = v || {};
    return {
      id: (typeof v.id === "string" && v.id.trim()) ? v.id : productoId + "_v" + (indice + 1),
      nombre: typeof v.nombre === "string" && v.nombre.trim() ? v.nombre : "Opción " + (indice + 1),
      stock: entero(v.stock),
      precio: numeroONulo(v.precio),
      fotoUrl: urlImagenSegura(v.foto_url || v.fotoUrl || "")
    };
  }

  /** Fila de Supabase → producto de la web. No inventa cantidades ni categorías. */
  function normalizarProducto(fila) {
    fila = fila || {};
    var variantes = fila.variantes;
    if (typeof variantes === "string") {
      try { variantes = JSON.parse(variantes); } catch (e) { variantes = []; }
    }
    if (!Array.isArray(variantes)) variantes = [];
    var fotos = Array.isArray(fila.fotos) ? fila.fotos.map(urlImagenSegura).filter(Boolean) : [];
    return {
      id: String(fila.id || ""),
      tiendaId: String(fila.tienda_id || fila.tiendaId || ""),
      nombre: String(fila.nombre || ""),
      descripcion: String(fila.descripcion || ""),
      categoria: String(fila.categoria || ""),
      tipo: fila.tipo ? String(fila.tipo) : "",
      atributos: (fila.atributos && typeof fila.atributos === "object" && !Array.isArray(fila.atributos)) ? fila.atributos : {},
      precio: Number(fila.precio) >= 0 ? Number(fila.precio) : 0,
      publicado: fila.publicado === true,
      disponible: fila.disponible !== false,
      destacado: fila.destacado === true,
      colorFoto: colorHex(fila.color_foto, "#4A5568"),
      fotoUrl: urlImagenSegura(fila.foto_url || fila.fotoUrl || ""),
      fotos: fotos,
      modalidad: MODALIDADES.indexOf(fila.modalidad) >= 0 ? fila.modalidad : "REGULAR",
      manejaStock: fila.modalidad === "PIEZA_UNICA" ? true : fila.maneja_stock !== false && fila.manejaStock !== false,
      alAgotarse: fila.modalidad === "PIEZA_UNICA" ? "OCULTAR" : (AL_AGOTARSE.indexOf(fila.al_agotarse || fila.alAgotarse) >= 0 ? (fila.al_agotarse || fila.alAgotarse) : "MOSTRAR_AGOTADO"),
      vendidaEn: fila.vendida_en || fila.vendidaEn || null,
      variantes: variantes.map(function (v, i) { return normalizarOpcion(v, String(fila.id || ""), i); }),
      actualizadoEn: fila.actualizado_en || null
    };
  }

  function normalizarTienda(t) {
    t = t || {};
    var nec = t.necesidades;
    if (typeof nec === "string") { try { nec = JSON.parse(nec); } catch (e) { nec = {}; } }
    nec = nec && typeof nec === "object" ? nec : {};
    var valores = t.valores_producto && typeof t.valores_producto === "object" ? t.valores_producto : {};
    return {
      id: String(t.id || ""),
      ownerId: t.owner_id || null,
      nombre: String(t.nombre || "Emprendimiento"),
      rubro: String(t.rubro || ""),
      sector: String(t.sector || ""),
      bio: String(t.bio || ""),
      whatsapp: String(t.whatsapp || ""),
      instagram: String(t.instagram || ""),
      colorPortada: colorHex(t.color_portada, "#1A202C"),
      colorLogo: colorHex(t.color_logo, "#2B6CB0"),
      logoUrl: urlImagenSegura(t.logo_url || ""),
      historialActivo: t.historial_activo !== false,
      valoresProducto: valores,
      necesidades: {
        tieneStock: nec.tieneStock !== false,
        tieneAgendamiento: nec.tieneAgendamiento === true,
        tieneVentaPorWhatsApp: nec.tieneVentaPorWhatsApp !== false,
        tieneProductosAPedido: nec.tieneProductosAPedido === true,
        participaEnFerias: nec.participaEnFerias === true,
        tieneForoComunidad: nec.tieneForoComunidad === true
      }
    };
  }

  function unidadesVendibles(producto) {
    return (producto.variantes || []).reduce(function (s, v) { return s + entero(v.stock); }, 0);
  }

  /** OCULTO | A_PEDIDO | AGOTADO | DISPONIBLE (misma tabla que el servidor). */
  function estadoPublico(p) {
    if (!p.publicado || p.disponible === false) return "OCULTO";
    if (p.vendidaEn) return "OCULTO";
    if (p.modalidad === "A_PEDIDO") return "A_PEDIDO";
    var unidades = unidadesVendibles(p);
    if (p.modalidad === "PIEZA_UNICA") return unidades > 0 ? "DISPONIBLE" : "OCULTO";
    if (p.manejaStock === false) return "DISPONIBLE";
    if (unidades > 0) return "DISPONIBLE";
    if (p.alAgotarse === "OCULTAR") return "OCULTO";
    if (p.alAgotarse === "PASAR_A_PEDIDO") return "A_PEDIDO";
    return "AGOTADO";
  }

  function etiquetaEstado(estado) {
    return { DISPONIBLE: "Disponible", AGOTADO: "Agotado", A_PEDIDO: "A pedido", OCULTO: "Oculto" }[estado] || "";
  }

  function etiquetaCategoria(p) {
    var cat = CATEGORIAS[p.categoria] || (p.categoria ? p.categoria.charAt(0) + p.categoria.slice(1).toLowerCase() : "");
    return [cat, p.tipoNombre || ""].filter(Boolean).join(" · ");
  }

  function precioDeOpcion(p, opcion) {
    if (opcion && opcion.precio !== null && opcion.precio !== undefined) return opcion.precio;
    return p.precio;
  }

  function fotoDeOpcion(p, opcion) {
    return (opcion && opcion.fotoUrl) || p.fotoUrl || (p.fotos && p.fotos[0]) || "";
  }

  /** ¿Se puede vender esta opción como entrega inmediata? */
  function opcionVendible(p, opcion) {
    if (!opcion || p.vendidaEn) return false;
    if (p.modalidad === "A_PEDIDO") return false;
    if (p.modalidad === "PIEZA_UNICA") return unidadesVendibles(p) > 0;
    if (p.manejaStock === false) return true;
    return entero(opcion.stock) > 0;
  }

  /** Validación previa de una línea de venta (el servidor vuelve a validar). */
  function validarLineaVenta(p, opcion, cantidad, encargo) {
    if (!opcion) return "Elige una opción.";
    if (!(Number.isInteger(cantidad) && cantidad >= 1 && cantidad <= 9999)) return "La cantidad debe ser un número entero mayor que cero.";
    if (p.vendidaEn) return "Esta pieza única ya se vendió.";
    if (encargo || p.modalidad === "A_PEDIDO") {
      if (p.modalidad === "PIEZA_UNICA") return "Una pieza única no se puede encargar.";
      return null;
    }
    if (p.modalidad === "PIEZA_UNICA") return cantidad === 1 && unidadesVendibles(p) > 0 ? null : "Una pieza única se vende una sola vez.";
    if (p.manejaStock === false) return null;
    if (entero(opcion.stock) < cantidad) return "Solo quedan " + entero(opcion.stock) + " de «" + opcion.nombre + "».";
    return null;
  }

  /**
   * Aplica una respuesta del servidor al catálogo mostrado.
   * Respuesta válida (aunque venga vacía) → reemplaza todo.
   * Error → conserva lo anterior y lo informa; nunca equivale a catálogo vacío.
   */
  function aplicarRespuesta(anterior, respuesta, normalizar) {
    anterior = anterior || { elementos: [], cargado: false, error: null };
    if (respuesta && respuesta.ok) {
      return { elementos: (respuesta.datos || []).map(normalizar), cargado: true, error: null };
    }
    return { elementos: anterior.elementos, cargado: anterior.cargado, error: (respuesta && respuesta.error) || "Error desconocido" };
  }

  function nuevoId(prefijo) {
    var c = (typeof crypto !== "undefined" && crypto.randomUUID) ? crypto.randomUUID()
      : "xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx".replace(/[xy]/g, function (ch) {
          var r = Math.random() * 16 | 0; return (ch === "x" ? r : (r & 0x3 | 0x8)).toString(16);
        });
    return (prefijo || "") + c;
  }

  /**
   * Formulario → cuerpo de guardar_producto(). Para opciones existentes envía
   * stockBase (lo que se veía al editar) para no pisar ventas simultáneas.
   */
  function productoParaGuardar(form, previo) {
    var previas = {};
    ((previo && previo.variantes) || []).forEach(function (v) { previas[v.id] = v; });
    var modalidad = MODALIDADES.indexOf(form.modalidad) >= 0 ? form.modalidad : "REGULAR";
    return {
      id: form.id || (previo && previo.id) || nuevoId("p_"),
      nombre: String(form.nombre || "").trim(),
      descripcion: String(form.descripcion || "").trim(),
      categoria: CATEGORIAS[form.categoria] ? form.categoria : "TEXTIL",
      tipo: form.tipo || null,
      atributos: form.atributos || {},
      precio: Math.max(0, Math.round(Number(form.precio) || 0)),
      publicado: form.publicado === true,
      disponible: form.disponible !== false,
      destacado: form.destacado === true,
      modalidad: modalidad,
      manejaStock: modalidad === "PIEZA_UNICA" ? true : form.manejaStock !== false,
      alAgotarse: modalidad === "PIEZA_UNICA" ? "OCULTAR" : (AL_AGOTARSE.indexOf(form.alAgotarse) >= 0 ? form.alAgotarse : "MOSTRAR_AGOTADO"),
      fotoUrl: urlImagenSegura(form.fotoUrl || "") || null,
      fotos: (form.fotos || []).map(urlImagenSegura).filter(Boolean),
      variantes: (form.variantes || []).map(function (v, i) {
        var salida = {
          id: v.id || nuevoId("v_"),
          nombre: String(v.nombre || "").trim() || "Opción " + (i + 1),
          precio: numeroONulo(v.precio),
          fotoUrl: urlImagenSegura(v.fotoUrl || "") || null,
          stock: entero(v.stock)
        };
        if (previas[salida.id]) salida.stockBase = entero(previas[salida.id].stock);
        return salida;
      })
    };
  }

  function fechaLocal(fecha) {
    var d = fecha || new Date();
    function dos(n) { return String(n).padStart(2, "0"); }
    return d.getFullYear() + "-" + dos(d.getMonth() + 1) + "-" + dos(d.getDate()) + " " + dos(d.getHours()) + ":" + dos(d.getMinutes());
  }

  /** Ticket del POS → cuerpo de registrar_venta(). La tienda la decide el servidor. */
  function ventaParaRegistrar(ticket, opciones) {
    opciones = opciones || {};
    return {
      id: opciones.id || nuevoId("v_"),
      items: ticket.map(function (l) {
        return {
          productoId: l.productoId, varianteId: l.varianteId, cantidad: l.cantidad,
          encargo: l.encargo === true, precio: l.precio, nombre: l.nombre, detalle: l.detalle,
          fotoUrl: urlImagenSegura(l.fotoUrl || "") || null
        };
      }),
      medioPago: MEDIOS_PAGO[opciones.medioPago] || "EFECTIVO",
      lugarTipo: opciones.feriaId ? "FERIA" : (opciones.lugarNombre ? "OTRO" : null),
      feriaId: opciones.feriaId || null,
      lugarNombre: opciones.lugarNombre || null,
      fechaLocal: fechaLocal(opciones.fecha),
      conHistorial: opciones.conHistorial !== false
    };
  }

  var MENSAJES = {
    NO_AUTENTICADO: "Tu sesión no está activa. Vuelve a iniciar sesión.",
    SIN_TIENDA: "Tu cuenta todavía no tiene una tienda.",
    TIENDA_AJENA: "Esa tienda no es tuya.",
    PRODUCTO_AJENO: "Ese producto pertenece a otra tienda.",
    PRODUCTO_NO_ENCONTRADO: "Uno de los productos ya no existe en tu tienda.",
    OPCION_NO_ENCONTRADA: "Una de las opciones ya no existe. Recarga tu catálogo.",
    STOCK_INSUFICIENTE: "No quedan unidades suficientes. Revisa las cantidades.",
    PIEZA_VENDIDA: "Esa pieza única ya se vendió.",
    ENCARGO_NO_PERMITIDO: "Una pieza única no se puede encargar.",
    ID_CONFLICTO: "Ya existe otra venta con ese código. Vuelve a intentarlo.",
    CANTIDAD_INVALIDA: "Revisa las cantidades: deben ser números enteros desde 0.",
    PRECIO_INVALIDO: "Revisa el precio.",
    NOMBRE_OBLIGATORIO: "Escribe el nombre del producto.",
    OPCIONES_OBLIGATORIAS: "Agrega al menos una opción.",
    OPCION_SIN_NOMBRE: "Cada opción necesita un nombre.",
    OPCION_DUPLICADA: "Hay dos opciones con el mismo código.",
    PIEZA_UNICA_UNA_UNIDAD: "Una pieza única tiene una sola unidad en total.",
    CONFLICTO_STOCK: "Mientras editabas se vendieron unidades. Revisa la cantidad actual y vuelve a guardar.",
    FOTO_OPCION_INVALIDA: "La foto de una opción no es válida.",
    FERIA_NO_ENCONTRADA: "Esa feria no existe en la plataforma.",
    MODALIDAD_INVALIDA: "Elige la modalidad.",
    AL_AGOTARSE_INVALIDO: "Elige qué pasa al agotarse."
  };

  /** Error de Supabase/red → mensaje en español para la emprendedora. */
  function mensajeError(error) {
    if (!error) return "";
    var texto = typeof error === "string" ? error : (error.message || error.error_description || "");
    var codigo = Object.keys(MENSAJES).filter(function (k) { return texto.indexOf(k) >= 0; })[0];
    if (codigo) {
      var mensaje = MENSAJES[codigo];
      if (codigo === "STOCK_INSUFICIENTE" && error.details) {
        try { var d = JSON.parse(error.details); if (typeof d.disponible === "number") mensaje = "No quedan unidades suficientes: quedan " + d.disponible + "."; } catch (e) { /* sin detalle */ }
      }
      return mensaje;
    }
    if (/failed to fetch|networkerror|load failed|network request failed/i.test(texto)) return "No hay conexión con el servidor. Revisa tu internet y vuelve a intentarlo.";
    if (/jwt expired|invalid jwt|refresh token/i.test(texto)) return "Tu sesión venció. Vuelve a iniciar sesión.";
    if (/permission denied|row-level security|42501/i.test(texto)) return "No tienes permiso para hacer esto con esta cuenta.";
    return "El servidor respondió con un error: " + (texto || "sin detalle") + ".";
  }

  return {
    MODALIDADES: MODALIDADES, AL_AGOTARSE: AL_AGOTARSE, CATEGORIAS: CATEGORIAS,
    ETIQUETA_MODALIDAD: ETIQUETA_MODALIDAD, ETIQUETA_AL_AGOTARSE: ETIQUETA_AL_AGOTARSE,
    urlImagenSegura: urlImagenSegura, colorHex: colorHex,
    normalizarProducto: normalizarProducto, normalizarTienda: normalizarTienda,
    unidadesVendibles: unidadesVendibles, estadoPublico: estadoPublico, etiquetaEstado: etiquetaEstado,
    etiquetaCategoria: etiquetaCategoria, precioDeOpcion: precioDeOpcion, fotoDeOpcion: fotoDeOpcion,
    opcionVendible: opcionVendible, validarLineaVenta: validarLineaVenta,
    aplicarRespuesta: aplicarRespuesta, productoParaGuardar: productoParaGuardar,
    ventaParaRegistrar: ventaParaRegistrar, mensajeError: mensajeError, nuevoId: nuevoId, fechaLocal: fechaLocal
  };
});
