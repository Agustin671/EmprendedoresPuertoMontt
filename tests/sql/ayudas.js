"use strict";
// Ayudas comunes para las pruebas SQL: ejecutar como una persona (rol + uid
// simulados igual que Supabase) y registrar resultados.

const UID = {
  admin: "00000000-0000-0000-0000-00000000000a",
  marta: "00000000-0000-0000-0000-00000000000b",
  barberia: "00000000-0000-0000-0000-00000000000c",
  vecina: "00000000-0000-0000-0000-00000000000d"
};

async function crearUsuarios(c) {
  await c.query(`insert into auth.users (id, email, raw_user_meta_data) values
    ($1, 'admin@alerce.app', '{}'), ($2, 'donamarta@alerce.app', '{}'),
    ($3, 'barberia@alerce.app', '{}'), ($4, 'vecina@gmail.com', '{}')
    on conflict (id) do nothing`, [UID.admin, UID.marta, UID.barberia, UID.vecina]);
}

/** Ejecuta sql como la persona indicada (null = sin sesión) en su propia transacción. */
async function como(c, uid, sql, params = []) {
  await c.query("begin");
  try {
    await c.query(uid ? "set local role authenticated" : "set local role anon");
    await c.query("select set_config('request.jwt.claim.sub', $1, true)", [uid || ""]);
    const r = await c.query(sql, params);
    await c.query("commit");
    return r;
  } catch (e) {
    await c.query("rollback");
    throw e;
  }
}

/** Devuelve el mensaje de error o null si no falló. */
async function error(promesa) {
  try { await promesa; return null; } catch (e) { return e.message || String(e); }
}

function venta(id, items, extra = {}) {
  return JSON.stringify(Object.assign({ id, items }, extra));
}

async function registrarVenta(c, uid, cuerpo) {
  const r = await como(c, uid, "select public.registrar_venta($1::jsonb) as r", [cuerpo]);
  return r.rows[0].r;
}

async function guardarProducto(c, uid, producto) {
  const r = await como(c, uid, "select public.guardar_producto($1::jsonb) as r", [JSON.stringify(producto)]);
  return r.rows[0].r;
}

async function stock(c, productoId) {
  const r = await c.query("select variantes, vendida_en from productos where id = $1", [productoId]);
  if (!r.rows.length) return null;
  const m = {};
  for (const v of r.rows[0].variantes) m[v.id] = v.stock;
  m._vendida = r.rows[0].vendida_en;
  return m;
}

class Registro {
  constructor(prefijo) { this.prefijo = prefijo; this.casos = []; }
  ok(nombre, condicion, detalle) {
    this.casos.push({ nombre: `${this.prefijo} · ${nombre}`, ok: !!condicion, detalle: condicion ? "" : (detalle === undefined ? "" : JSON.stringify(detalle)) });
  }
  igual(nombre, real, esperado) {
    const ok = JSON.stringify(real) === JSON.stringify(esperado);
    this.ok(nombre, ok, { real, esperado });
  }
  falla(nombre, mensaje, contiene) {
    this.ok(nombre, mensaje && mensaje.includes(contiene), { mensaje, esperado: contiene });
  }
}

module.exports = { UID, crearUsuarios, como, error, venta, registrarVenta, guardarProducto, stock, Registro };
