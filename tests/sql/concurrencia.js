"use strict";
// Concurrencia real: conexiones independientes a PostgreSQL, sin simulación.
const { UID, guardarProducto, stock } = require("./ayudas");

async function abrirComo(pg, base, uid) {
  const c = pg.cliente(base);
  await c.connect();
  return {
    c,
    async empezar() {
      await c.query("begin");
      await c.query("set local role authenticated");
      await c.query("select set_config('request.jwt.claim.sub', $1, true)", [uid]);
    },
    vender(cuerpo) { return c.query("select public.registrar_venta($1::jsonb) as r", [JSON.stringify(cuerpo)]).then(x => x.rows[0].r); },
    async ventaSola(cuerpo) {
      await this.empezar();
      try { const r = await this.vender(cuerpo); await c.query("commit"); return { ok: r }; }
      catch (e) { await c.query("rollback"); return { error: e.message, code: e.code }; }
    },
    fin() { return c.end(); }
  };
}

const esperar = ms => new Promise(r => setTimeout(r, ms));

module.exports = async function concurrencia(pg, base, admin, r) {
  const M = UID.marta;
  const mk = (id, variantes) => ({ id, nombre: id, precio: 1000, publicado: true, disponible: true, modalidad: "REGULAR", manejaStock: true, alAgotarse: "MOSTRAR_AGOTADO", variantes });
  await guardarProducto(admin, M, mk("cc_ultima", [{ id: "u", nombre: "U", stock: 1 }]));
  await guardarProducto(admin, M, mk("cc_reintento", [{ id: "u", nombre: "U", stock: 5 }]));
  await guardarProducto(admin, M, mk("cc_p1", [{ id: "u", nombre: "U", stock: 1000 }]));
  await guardarProducto(admin, M, mk("cc_p2", [{ id: "u", nombre: "U", stock: 1000 }]));
  await guardarProducto(admin, M, mk("cc_ajuste", [{ id: "u", nombre: "U", stock: 10 }]));

  const a = await abrirComo(pg, base, M);
  const b = await abrirComo(pg, base, M);
  try {
    // 1) Dos ventas simultáneas por la última unidad.
    await a.empezar();
    const ra = await a.vender({ id: "cc_v_a", items: [{ productoId: "cc_ultima", varianteId: "u", cantidad: 1 }] });
    let bTermino = false;
    const pb = b.ventaSola({ id: "cc_v_b", items: [{ productoId: "cc_ultima", varianteId: "u", cantidad: 1 }] }).then(x => { bTermino = true; return x; });
    await esperar(400);
    r.ok("La segunda venta espera el bloqueo de la primera", !bTermino);
    await a.c.query("commit");
    const rb = await pb;
    r.ok("Solo una se lleva la última unidad", ra.estado === "REGISTRADA" && rb.error && rb.error.includes("STOCK_INSUFICIENTE"), { ra, rb });
    const n = (await admin.query("select count(*)::int n from ventas where id in ('cc_v_a','cc_v_b')")).rows[0].n;
    r.igual("Queda una venta y cero unidades", [n, (await stock(admin, "cc_ultima")).u], [1, 0]);

    // 2) El mismo reintento llega dos veces a la vez.
    const cuerpo = { id: "cc_v_r", items: [{ productoId: "cc_reintento", varianteId: "u", cantidad: 2 }] };
    await a.empezar();
    const r1 = await a.vender(cuerpo);
    const p2 = b.ventaSola(cuerpo);
    await esperar(300);
    await a.c.query("commit");
    const r2 = await p2;
    r.ok("Reintentos simultáneos: uno registra y el otro reconoce la venta", r1.estado === "REGISTRADA" && r2.ok && r2.ok.estado === "YA_REGISTRADA", { r1, r2 });
    r.igual("Reintentos simultáneos descuentan una sola vez", (await stock(admin, "cc_reintento")).u, 3);

    // 3) Orden cruzado de productos: sin interbloqueos.
    // Cada conexión procesa su serie de ventas; las dos series corren en paralelo.
    const serie = async (conexion, prefijo, orden) => {
      const salida = [];
      for (let i = 0; i < 25; i++) {
        salida.push(await conexion.ventaSola({ id: prefijo + i, items: orden.map(p => ({ productoId: p, varianteId: "u", cantidad: 1 })) }));
      }
      return salida;
    };
    const res = [].concat(...await Promise.all([serie(a, "cc_x_a", ["cc_p1", "cc_p2"]), serie(b, "cc_x_b", ["cc_p2", "cc_p1"])]));
    const errores = res.filter(x => x.error);
    r.ok("50 ventas cruzadas sin interbloqueos ni errores", errores.length === 0, errores.slice(0, 3));
    r.igual("Cada producto descontó exactamente 50", [(await stock(admin, "cc_p1")).u, (await stock(admin, "cc_p2")).u], [950, 950]);

    // 4) Edición con stockBase mientras se vende.
    await a.empezar();
    await a.vender({ id: "cc_v_aj", items: [{ productoId: "cc_ajuste", varianteId: "u", cantidad: 3 }] });
    const pEdit = (async () => {
      await b.empezar();
      try {
        const x = await b.c.query("select public.guardar_producto($1::jsonb) r", [JSON.stringify(mk("cc_ajuste", [{ id: "u", nombre: "U", stock: 15, stockBase: 10 }]))]);
        await b.c.query("commit"); return x.rows[0].r;
      } catch (e) { await b.c.query("rollback"); return { error: e.message }; }
    })();
    await esperar(300);
    await a.c.query("commit");
    await pEdit;
    r.igual("Reponer 5 mientras se venden 3 deja 12 (no pisa la venta)", (await stock(admin, "cc_ajuste")).u, 12);
  } finally {
    await a.fin();
    await b.fin();
  }
};
