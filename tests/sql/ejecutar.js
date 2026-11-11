"use strict";
// Ejecuta la batería SQL en PostgreSQL local y desechable (15 y 17 si están):
//   A) instalación limpia: stub + 01 + 02 + 03 + 04 + 05 (dos veces: idempotente)
//   B) actualización: esquema y datos del formato anterior, luego 03 + 04 + 05
// Uso: npm run test:sql   (opción: --version 17)
const { execFileSync } = require("child_process");
const { PostgresLocal, versionesDisponibles, ejecutarArchivo, RAIZ } = require("./postgres_local");
const { crearUsuarios, como, UID, Registro } = require("./ayudas");
const pruebasEntrega5 = require("./pruebas_entrega5");
const concurrencia = require("./concurrencia");

const COMMIT_ESQUEMA_ANTERIOR = "6859b8f";

async function instalacionLimpia(pg, r) {
  await pg.crearBase("limpia");
  const c = pg.cliente("limpia");
  await c.connect();
  try {
    for (const f of ["supabase/stub_local.sql", "supabase/01_esquema.sql", "supabase/02_datos_prueba.sql",
                     "supabase/03_migracion_fotos.sql", "supabase/04_registro_vendedores.sql", "supabase/05_catalogo_ventas.sql"]) {
      await ejecutarArchivo(c, f);
    }
    await ejecutarArchivo(c, "supabase/05_catalogo_ventas.sql");
    r.ok("05 se puede ejecutar dos veces", true);

    const salida = await ejecutarArchivo(c, "supabase/pruebas_rls.sql");
    const filas = [].concat(salida).filter(x => x && x.fields && x.fields.some(f => f.name === "resultado")).flatMap(x => x.rows);
    for (const f of filas) r.ok("pruebas_rls.sql · " + f.resultado.slice(6), f.resultado.startsWith("OK"));
    r.ok("pruebas_rls.sql produjo resultados", filas.length > 30, filas.length);

    await crearUsuarios(c);
    await pruebasEntrega5(c, r);
    await concurrencia(pg, "limpia", c, r);
  } finally {
    await c.end();
  }
}

async function actualizacion(pg, r) {
  await pg.crearBase("antigua");
  const c = pg.cliente("antigua");
  await c.connect();
  try {
    await ejecutarArchivo(c, "supabase/stub_local.sql");
    const esquemaAnterior = execFileSync("git", ["show", `${COMMIT_ESQUEMA_ANTERIOR}:supabase/01_esquema.sql`], { cwd: RAIZ, encoding: "utf8" });
    await c.query(esquemaAnterior);
    await ejecutarArchivo(c, "supabase/02_datos_prueba.sql");
    await crearUsuarios(c);
    // Datos como los dejaban los clientes anteriores.
    await como(c, UID.marta, `insert into ventas (id, tienda_id, fecha_local, items, medio_pago, total)
      values ('v_antigua', 't_minimarket_rosa', '2026-10-01 10:00', '[{"productoId":"p_mini_1","varianteId":"v_min1","nombre":"Pan","detalle":"Bolsa","precio":2500,"cantidad":1}]', 'EFECTIVO', 2500)`);
    await como(c, UID.marta, `insert into productos (id, tienda_id, nombre, categoria, precio, publicado, variantes, foto_url)
      values ('p_custom_1', 't_minimarket_rosa', 'Alta web antigua', 'GENERAL', 3000, true, '[{"nombre":"Unidad","precio":3000}]', 'data:image/png;base64,AAAA')`);
    const antes = (await c.query("select (select count(*) from productos)::int p, (select count(*) from ventas)::int v, (select count(*) from tiendas)::int t")).rows[0];

    for (const f of ["supabase/03_migracion_fotos.sql", "supabase/04_registro_vendedores.sql", "supabase/05_catalogo_ventas.sql"]) {
      await ejecutarArchivo(c, f);
    }
    const despues = (await c.query("select (select count(*) from productos)::int p, (select count(*) from ventas)::int v, (select count(*) from tiendas)::int t")).rows[0];
    r.igual("Se conservan productos, ventas y tiendas", despues, antes);
    const va = (await como(c, UID.marta, "select total, items->0->>'precio' precio, en_historial from ventas where id = 'v_antigua'")).rows[0];
    r.igual("La venta anterior sigue visible para su dueña y completa", va, { total: "2500", precio: "2500", en_historial: true });
    const pa = (await c.query("select variantes->0->>'id' id, modalidad, maneja_stock, disponible, al_agotarse from productos where id = 'p_custom_1'")).rows[0];
    r.igual("Producto antiguo: id de opción asignado y valores por defecto", pa, { id: "p_custom_1_v1", modalidad: "REGULAR", maneja_stock: true, disponible: true, al_agotarse: "MOSTRAR_AGOTADO" });
    const visibles = (await como(c, null, "select count(*)::int n from productos where id like 'p\\_%' and tienda_id like 't\\_%' and publicado")).rows[0].n;
    r.ok("Los productos publicados con unidades siguen visibles sin sesión", visibles >= 14, visibles);
    const migr = (await c.query("select nombre from migraciones_aplicadas")).rows.map(x => x.nombre);
    r.igual("Queda registrada la migración", migr, ["05_catalogo_ventas"]);
    await pruebasEntrega5(c, r);
  } finally {
    await c.end();
  }
}

(async () => {
  const pedida = process.argv.includes("--version") ? Number(process.argv[process.argv.indexOf("--version") + 1]) : null;
  const versiones = versionesDisponibles().filter(v => !pedida || v.version === pedida);
  if (!versiones.length) {
    console.error("No hay binarios de PostgreSQL. Ejecuta `npm install` (PG17) y revisa docs/desarrollo-y-pruebas.md (PG15).");
    process.exit(2);
  }
  let fallas = 0;
  let total = 0;
  for (const v of versiones) {
    const pg = new PostgresLocal({ version: v.version, bin: v.bin, puerto: 54300 + v.version });
    const r = new Registro(`PG${v.version}`);
    await pg.iniciar();
    try {
      const rl = new Registro(`PG${v.version} limpia`);
      await instalacionLimpia(pg, rl).catch(e => rl.ok("instalación limpia sin errores", false, e.message));
      const ra = new Registro(`PG${v.version} actualización`);
      await actualizacion(pg, ra).catch(e => ra.ok("actualización sin errores", false, e.message));
      r.casos.push(...rl.casos, ...ra.casos);
    } finally {
      await pg.detener();
    }
    for (const caso of r.casos) {
      total++;
      if (!caso.ok) fallas++;
      console.log(`${caso.ok ? "OK   " : "FALLA"} ${caso.nombre}${caso.ok ? "" : "  → " + caso.detalle}`);
    }
  }
  console.log(`\n${total - fallas} de ${total} casos correctos (${versiones.map(v => "PG" + v.version).join(", ")})`);
  process.exit(fallas ? 1 : 0);
})();
