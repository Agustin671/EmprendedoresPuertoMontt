"use strict";
// Reglas del catálogo web (interfaz pública: js/catalogo.js).
const test = require("node:test");
const assert = require("node:assert/strict");
const path = require("path");
const C = require(path.join(__dirname, "..", "..", "LA PAgina (segun)", "proyectode10", "js", "catalogo.js"));
const contrato = require("../contrato/casos_estado.json");

function desdeCaso(p) {
  return C.normalizarProducto({
    id: "p", publicado: p.publicado, disponible: p.disponible, modalidad: p.modalidad,
    maneja_stock: p.manejaStock, al_agotarse: p.alAgotarse, vendida_en: p.vendida ? "2026-11-10T10:00:00Z" : null,
    variantes: p.variantes.map((s, i) => ({ id: "v" + i, nombre: "O" + i, stock: s }))
  });
}

test("estado público: misma tabla de verdad que el servidor y la app", () => {
  for (const caso of contrato.casos) {
    assert.equal(C.estadoPublico(desdeCaso(caso.p)), caso.estado, caso.nombre);
  }
});

test("cero unidades sigue siendo cero (no se inventan 10)", () => {
  const p = C.normalizarProducto({ id: "x", publicado: true, variantes: [{ id: "a", nombre: "U", stock: 0 }] });
  assert.equal(C.unidadesVendibles(p), 0);
  assert.equal(C.estadoPublico(p), "AGOTADO");
});

test("producto sin opciones no recibe opciones inventadas", () => {
  const p = C.normalizarProducto({ id: "x", publicado: true, variantes: [] });
  assert.deepEqual(p.variantes, []);
});

test("categoría desconocida no se reemplaza por otra", () => {
  assert.equal(C.normalizarProducto({ id: "x", categoria: "" }).categoria, "");
  assert.equal(C.etiquetaCategoria(C.normalizarProducto({ id: "x", categoria: "GOURMET" })), "Gourmet");
});

test("cada opción muestra su propio precio y su propia foto", () => {
  const p = C.normalizarProducto({
    id: "x", precio: 1000, foto_url: "https://e.cl/p.jpg",
    variantes: [{ id: "a", nombre: "Chica", stock: 1, precio: null }, { id: "b", nombre: "Grande", stock: 1, precio: 9000, foto_url: "https://e.cl/b.jpg" }]
  });
  assert.equal(C.precioDeOpcion(p, p.variantes[0]), 1000);
  assert.equal(C.fotoDeOpcion(p, p.variantes[0]), "https://e.cl/p.jpg");
  assert.equal(C.precioDeOpcion(p, p.variantes[1]), 9000);
  assert.equal(C.fotoDeOpcion(p, p.variantes[1]), "https://e.cl/b.jpg");
});

test("URLs de imagen inseguras se descartan", () => {
  for (const mala of ["javascript:alert(1)", "data:image/png;base64,AAAA", "https://e.cl/x.jpg\" onerror=\"alert(1)", "/relativa.jpg", "http://otro.cl/x.jpg"]) {
    assert.equal(C.urlImagenSegura(mala), "", mala);
  }
  assert.equal(C.urlImagenSegura("https://abc.supabase.co/storage/v1/object/public/productos/u/x.jpg"), "https://abc.supabase.co/storage/v1/object/public/productos/u/x.jpg");
});

test("respuesta válida vacía reemplaza el catálogo; un error lo conserva", () => {
  const previo = C.aplicarRespuesta(null, { ok: true, datos: [{ id: "viejo", publicado: true }] }, C.normalizarProducto);
  const vacio = C.aplicarRespuesta(previo, { ok: true, datos: [] }, C.normalizarProducto);
  assert.deepEqual(vacio.elementos, []);
  assert.equal(vacio.error, null);
  const conError = C.aplicarRespuesta(previo, { ok: false, error: "Failed to fetch" }, C.normalizarProducto);
  assert.equal(conError.elementos.length, 1);
  assert.equal(conError.error, "Failed to fetch");
});

test("una opción agotada no se vende como entrega inmediata, pero sí como encargo", () => {
  const p = C.normalizarProducto({ id: "x", publicado: true, variantes: [{ id: "a", nombre: "Azul", stock: 0 }, { id: "b", nombre: "Roja", stock: 2 }] });
  assert.equal(C.opcionVendible(p, p.variantes[0]), false);
  assert.match(C.validarLineaVenta(p, p.variantes[0], 1, false), /Solo quedan 0/);
  assert.equal(C.validarLineaVenta(p, p.variantes[0], 1, true), null);
  assert.equal(C.validarLineaVenta(p, p.variantes[1], 2, false), null);
  assert.match(C.validarLineaVenta(p, p.variantes[1], 3, false), /Solo quedan 2/);
  assert.match(C.validarLineaVenta(p, p.variantes[1], 1.5, false), /entero/);
});

test("pieza única: una unidad total aunque tenga opciones, y no se encarga", () => {
  const p = C.normalizarProducto({ id: "x", publicado: true, modalidad: "PIEZA_UNICA", variantes: [{ id: "a", nombre: "Azul", stock: 1 }, { id: "b", nombre: "Roja", stock: 0 }] });
  assert.equal(C.validarLineaVenta(p, p.variantes[1], 1, false), null);
  assert.match(C.validarLineaVenta(p, p.variantes[0], 2, false), /una sola vez/);
  assert.match(C.validarLineaVenta(p, p.variantes[0], 1, true), /no se puede encargar/);
});

test("guardar producto: envía stockBase solo para opciones que ya existían", () => {
  const previo = C.normalizarProducto({ id: "p1", variantes: [{ id: "a", nombre: "A", stock: 5 }] });
  const cuerpo = C.productoParaGuardar({
    id: "p1", nombre: " Queque ", precio: "2500", categoria: "GOURMET", modalidad: "REGULAR", alAgotarse: "OCULTAR",
    publicado: true, variantes: [{ id: "a", nombre: "A", stock: 7 }, { id: "b", nombre: "B", stock: 2, precio: "3000" }]
  }, previo);
  assert.equal(cuerpo.nombre, "Queque");
  assert.equal(cuerpo.precio, 2500);
  assert.deepEqual(cuerpo.variantes[0], { id: "a", nombre: "A", precio: null, fotoUrl: null, stock: 7, stockBase: 5 });
  assert.equal("stockBase" in cuerpo.variantes[1], false);
  assert.equal(cuerpo.variantes[1].precio, 3000);
  assert.equal("tiendaId" in cuerpo, false, "la tienda la decide el servidor");
});

test("guardar producto: una pieza única siempre controla unidades y se oculta al venderse", () => {
  const cuerpo = C.productoParaGuardar({ nombre: "Chal", precio: 1, modalidad: "PIEZA_UNICA", manejaStock: false, alAgotarse: "MOSTRAR_AGOTADO", variantes: [{ nombre: "U", stock: 1 }] });
  assert.equal(cuerpo.manejaStock, true);
  assert.equal(cuerpo.alAgotarse, "OCULTAR");
});

test("venta: cuerpo para registrar_venta sin tienda y con lugar", () => {
  const v = C.ventaParaRegistrar([{ productoId: "p", varianteId: "a", cantidad: 2, precio: 1000, nombre: "Pan", detalle: "Bolsa" }],
    { medioPago: "Tarjeta", lugarNombre: "Plaza", fecha: new Date(2026, 10, 10, 9, 5) });
  assert.equal(v.medioPago, "DEBITO");
  assert.equal(v.lugarTipo, "OTRO");
  assert.equal(v.fechaLocal, "2026-11-10 09:05");
  assert.equal("tiendaId" in v, false);
  assert.ok(v.id.length > 10);
});

test("errores del servidor se traducen a mensajes claros", () => {
  assert.match(C.mensajeError({ message: "STOCK_INSUFICIENTE", details: '{"disponible":1}' }), /quedan 1/);
  assert.match(C.mensajeError(new TypeError("Failed to fetch")), /conexión/);
  assert.match(C.mensajeError({ message: "JWT expired" }), /sesión venció/);
  assert.match(C.mensajeError({ message: "CONFLICTO_STOCK" }), /se vendieron/);
});
