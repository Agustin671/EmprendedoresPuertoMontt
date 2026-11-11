"use strict";
// Contrato entre la web y Supabase (js/servicio.js) con un cliente sustituto
// que imita las respuestas de supabase-js. No reemplaza las pruebas SQL reales.
const test = require("node:test");
const assert = require("node:assert/strict");
const path = require("path");
const S = require(path.join(__dirname, "..", "..", "LA PAgina (segun)", "proyectode10", "js", "servicio.js"));

function clienteFalso(respuestas, registro = []) {
  function consulta(tabla) {
    const q = { tabla, filtros: [], operacion: "select" };
    const api = {
      select(c) { q.columnas = c; return api; },
      update(v) { q.operacion = "update"; q.valores = v; return api; },
      delete() { q.operacion = "delete"; return api; },
      eq(k, v) { q.filtros.push([k, "eq", v]); return api; },
      in(k, v) { q.filtros.push([k, "in", v]); return api; },
      order() { return api; },
      maybeSingle() { return api; },
      then(ok, ko) { registro.push(q); const r = respuestas[tabla + ":" + q.operacion]; return (r instanceof Error ? Promise.reject(r) : Promise.resolve(r)).then(ok, ko); }
    };
    return api;
  }
  return {
    from: consulta,
    rpc(nombre, args) { registro.push({ rpc: nombre, args }); const r = respuestas["rpc:" + nombre]; return r instanceof Error ? Promise.reject(r) : Promise.resolve(r); },
    storage: {
      from(bucket) {
        return {
          upload(ruta) { registro.push({ upload: bucket + "/" + ruta }); return Promise.resolve(respuestas["upload"] || { data: { path: ruta }, error: null }); },
          getPublicUrl(ruta) { return { data: { publicUrl: "https://x.supabase.co/storage/v1/object/public/" + bucket + "/" + ruta } }; },
          remove(rutas) { registro.push({ remove: rutas }); return Promise.resolve({ data: [], error: null }); }
        };
      }
    }
  };
}

test("una lista vacía válida es éxito; un fallo de red es error (no lista vacía)", async () => {
  const vacio = await S.crear(clienteFalso({ "productos:select": { data: [], error: null } })).listarProductosVisibles();
  assert.deepEqual(vacio, { ok: true, datos: [] });
  const red = await S.crear(clienteFalso({ "productos:select": new TypeError("Failed to fetch") })).listarProductosVisibles();
  assert.equal(red.ok, false);
  assert.match(red.error.message, /Failed to fetch/);
  const rls = await S.crear(clienteFalso({ "productos:select": { data: null, error: { message: "permission denied" } } })).listarProductosVisibles();
  assert.equal(rls.ok, false);
});

test("guardar y vender usan las funciones seguras del servidor", async () => {
  const reg = [];
  const s = S.crear(clienteFalso({ "rpc:guardar_producto": { data: { id: "p1" }, error: null }, "rpc:registrar_venta": { data: { estado: "REGISTRADA" }, error: null } }, reg));
  assert.equal((await s.guardarProducto({ id: "p1" })).datos.id, "p1");
  assert.equal((await s.registrarVenta({ id: "v1", items: [] })).datos.estado, "REGISTRADA");
  assert.deepEqual(reg.map(x => x.rpc), ["guardar_producto", "registrar_venta"]);
  assert.deepEqual(reg[1].args, { p_venta: { id: "v1", items: [] } });
});

test("si la RLS no actualiza filas, se informa error en vez de éxito", async () => {
  const s = S.crear(clienteFalso({ "productos:update": { data: [], error: null }, "productos:delete": { data: [], error: null } }));
  assert.equal((await s.cambiarPublicado("p1", false)).ok, false);
  assert.equal((await s.eliminarProducto("p1")).ok, false);
  const parcial = await S.crear(clienteFalso({ "productos:update": { data: [{ id: "a" }], error: null } })).cambiarDisponible(["a", "b"], false);
  assert.equal(parcial.ok, false);
  assert.match(parcial.error.message, /1 de 2/);
});

test("la imagen se sube a la ruta indicada y devuelve URL https", async () => {
  const reg = [];
  const r = await S.crear(clienteFalso({}, reg)).subirImagen("productos", "uid-1/p1_123.jpg", {});
  assert.equal(r.ok, true);
  assert.match(r.datos, /^https:\/\/.*productos\/uid-1\/p1_123\.jpg$/);
  assert.deepEqual(reg[0], { upload: "productos/uid-1/p1_123.jpg" });
  const fallo = await S.crear(clienteFalso({ upload: { data: null, error: { message: "new row violates row-level security policy" } } })).subirImagen("productos", "otro/x.jpg", {});
  assert.equal(fallo.ok, false);
});
