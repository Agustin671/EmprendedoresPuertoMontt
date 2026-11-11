"use strict";
// Pruebas reales contra PostgreSQL (no simuladas): catálogo, visibilidad,
// aislamiento, ventas, idempotencia, piezas únicas, historial, clientes
// antiguos y Storage. Requiere 01+03+04+05 aplicados y los usuarios creados.
const { UID, como, error, venta, registrarVenta, guardarProducto, stock } = require("./ayudas");

function producto(id, extra = {}) {
  return Object.assign({
    id, nombre: "Producto " + id, descripcion: "Descripción " + id, categoria: "TEXTIL", precio: 1000,
    publicado: true, disponible: true, modalidad: "REGULAR", manejaStock: true, alAgotarse: "MOSTRAR_AGOTADO",
    variantes: [{ id: id + "_a", nombre: "Única", stock: 5 }]
  }, extra);
}

module.exports = async function pruebasEntrega5(c, r) {
  const M = UID.marta, B = UID.barberia, A = UID.admin, V = UID.vecina;

  // ---------- Catálogo y visibilidad pública ----------
  const casos = {
    e5_visible: producto("e5_visible"),
    e5_no_publicado: producto("e5_no_publicado", { publicado: false }),
    e5_no_disponible: producto("e5_no_disponible", { disponible: false }),
    e5_agotado_mostrar: producto("e5_agotado_mostrar", { variantes: [{ id: "x1", nombre: "U", stock: 0 }] }),
    e5_agotado_ocultar: producto("e5_agotado_ocultar", { alAgotarse: "OCULTAR", variantes: [{ id: "x2", nombre: "U", stock: 0 }] }),
    e5_agotado_pedido: producto("e5_agotado_pedido", { alAgotarse: "PASAR_A_PEDIDO", variantes: [{ id: "x3", nombre: "U", stock: 0 }] }),
    e5_a_pedido: producto("e5_a_pedido", { modalidad: "A_PEDIDO", manejaStock: false, variantes: [{ id: "x4", nombre: "U", stock: 0 }] }),
    e5_sin_conteo: producto("e5_sin_conteo", { manejaStock: false, variantes: [{ id: "x5", nombre: "U", stock: 0 }] }),
    e5_pieza: producto("e5_pieza", { modalidad: "PIEZA_UNICA", variantes: [{ id: "pz_a", nombre: "Azul", stock: 1 }, { id: "pz_b", nombre: "Roja", stock: 0 }] }),
    e5_opciones: producto("e5_opciones", {
      precio: 1000,
      variantes: [
        { id: "op_a", nombre: "Chica", stock: 5, precio: 1000, fotoUrl: "https://ejemplo.invalid/a.jpg" },
        { id: "op_b", nombre: "Grande", stock: 3, precio: 9000, fotoUrl: "https://ejemplo.invalid/b.jpg" }
      ]
    })
  };
  for (const p of Object.values(casos)) await guardarProducto(c, M, p);

  const anon = await como(c, null, "select id from productos where id like 'e5_%' order by id");
  r.igual("Sin sesión solo se ven los productos que la regla deja ver",
    anon.rows.map(x => x.id),
    ["e5_a_pedido", "e5_agotado_mostrar", "e5_agotado_pedido", "e5_opciones", "e5_pieza", "e5_sin_conteo", "e5_visible"]);
  const comp = await como(c, V, "select id from productos where id in ('e5_no_publicado','e5_no_disponible','e5_agotado_ocultar')");
  r.igual("Una compradora con sesión tampoco ve ocultos (RLS, no JavaScript)", comp.rows.length, 0);
  const duena = await como(c, M, "select count(*)::int n from productos where id like 'e5_%'");
  r.igual("La dueña sigue viendo todos sus productos (RN-09)", duena.rows[0].n, 10);
  const estados = await como(c, null, `select id, public.estado_publico_producto(publicado, disponible, vendida_en, maneja_stock, modalidad, al_agotarse, variantes) e
     from productos where id in ('e5_agotado_mostrar','e5_agotado_pedido','e5_a_pedido','e5_sin_conteo') order by id`);
  r.igual("Estados públicos calculados en el servidor",
    estados.rows.map(x => [x.id, x.e]),
    [["e5_a_pedido", "A_PEDIDO"], ["e5_agotado_mostrar", "AGOTADO"], ["e5_agotado_pedido", "A_PEDIDO"], ["e5_sin_conteo", "DISPONIBLE"]]);
  const op = await c.query("select variantes from productos where id='e5_opciones'");
  r.igual("Cada opción guarda su precio, foto y cantidad", op.rows[0].variantes.map(v => [v.id, v.precio, v.foto_url, v.stock]),
    [["op_a", 1000, "https://ejemplo.invalid/a.jpg", 5], ["op_b", 9000, "https://ejemplo.invalid/b.jpg", 3]]);
  r.falla("Pieza única no acepta más de una unidad", await error(guardarProducto(c, M, producto("e5_pieza2", { modalidad: "PIEZA_UNICA", variantes: [{ id: "q", nombre: "U", stock: 2 }] }))), "PIEZA_UNICA_UNA_UNIDAD");
  r.falla("Precio negativo rechazado", await error(guardarProducto(c, M, producto("e5_neg", { precio: -1 }))), "PRECIO_INVALIDO");
  r.falla("Foto de opción que no es https rechazada", await error(guardarProducto(c, M, producto("e5_js", { variantes: [{ id: "j", nombre: "U", stock: 1, fotoUrl: "javascript:alert(1)" }] }))), "FOTO_OPCION_INVALIDA");

  // ---------- Aislamiento entre tiendas ----------
  r.falla("Otra emprendedora no puede sobrescribir un producto ajeno", await error(guardarProducto(c, B, producto("e5_visible", { nombre: "Robado" }))), "PRODUCTO_AJENO");
  const directo = await como(c, B, "update productos set nombre = 'Robado' where id = 'e5_no_publicado'");
  r.igual("Ni por actualización directa (0 filas)", directo.rowCount, 0);
  const propio = await guardarProducto(c, B, Object.assign(producto("e5_barberia"), { tiendaId: "t_minimarket_rosa" }));
  r.igual("El tienda_id enviado por el cliente se ignora: queda en la tienda de la sesión", propio.tienda_id, "t_barberia_alerce");
  r.falla("Sin sesión no se guarda", await error(guardarProducto(c, null, producto("e5_anon"))), "permission denied");

  // ---------- Ventas ----------
  const v1 = await registrarVenta(c, M, venta("e5v1", [{ productoId: "e5_opciones", varianteId: "op_b", cantidad: 2 }], { medioPago: "EFECTIVO", fechaLocal: "2026-11-10 12:00", lugarTipo: "OTRO", lugarNombre: "Plaza de Alerce" }));
  r.igual("Venta registrada", v1.estado, "REGISTRADA");
  r.igual("Descuenta solo la opción vendida", await stock(c, "e5_opciones").then(s => [s.op_a, s.op_b]), [5, 1]);
  const fila = (await c.query("select items, total, lugar_tipo, lugar_nombre, en_historial, tienda_id from ventas where id='e5v1'")).rows[0];
  r.igual("Guarda precio de la opción, fecha y lugar", [fila.items[0].precio, fila.total, fila.lugar_tipo, fila.lugar_nombre, fila.en_historial], [9000, "18000", "OTRO", "Plaza de Alerce", true]);
  r.falla("Sin unidades suficientes se rechaza (no queda en cero en silencio)",
    await error(registrarVenta(c, M, venta("e5v2", [{ productoId: "e5_opciones", varianteId: "op_b", cantidad: 2 }]))), "STOCK_INSUFICIENTE");
  r.igual("El rechazo no cambia cantidades ni guarda venta", [await stock(c, "e5_opciones").then(s => s.op_b), (await c.query("select count(*)::int n from ventas where id='e5v2'")).rows[0].n], [1, 0]);

  const otraVez = await registrarVenta(c, M, venta("e5v1", [{ productoId: "e5_opciones", varianteId: "op_b", cantidad: 2 }]));
  r.igual("Reintento con el mismo id y contenido no duplica", [otraVez.estado, await stock(c, "e5_opciones").then(s => s.op_b)], ["YA_REGISTRADA", 1]);
  r.falla("Mismo id con otro contenido no modifica la venta previa",
    await error(registrarVenta(c, M, venta("e5v1", [{ productoId: "e5_opciones", varianteId: "op_a", cantidad: 1 }]))), "ID_CONFLICTO");
  r.igual("La venta previa sigue intacta", (await c.query("select items->0->>'varianteId' v from ventas where id='e5v1'")).rows[0].v, "op_b");

  r.falla("Otra tienda no puede vender productos ajenos",
    await error(registrarVenta(c, B, venta("e5v3", [{ productoId: "e5_opciones", varianteId: "op_a", cantidad: 1 }]))), "PRODUCTO_NO_ENCONTRADO");
  r.falla("Un tiendaId ajeno en la venta se rechaza",
    await error(registrarVenta(c, B, venta("e5v4", [{ productoId: "e5_barberia", varianteId: "e5_barberia_a", cantidad: 1 }], { tiendaId: "t_minimarket_rosa" }))), "TIENDA_AJENA");
  for (const [n, cant] of [["cero", 0], ["negativa", -1], ["decimal", 1.5], ["texto", "2"]]) {
    r.falla(`Cantidad ${n} rechazada`, await error(registrarVenta(c, M, venta("e5_c_" + n, [{ productoId: "e5_visible", varianteId: "e5_visible_a", cantidad: cant }]))), "CANTIDAD_INVALIDA");
  }
  r.falla("Opción inexistente rechazada", await error(registrarVenta(c, M, venta("e5v5", [{ productoId: "e5_visible", varianteId: "no_existe", cantidad: 1 }]))), "OPCION_NO_ENCONTRADA");
  r.falla("Sin sesión no se vende", await error(registrarVenta(c, null, venta("e5v6", [{ productoId: "e5_visible", varianteId: "e5_visible_a", cantidad: 1 }]))), "permission denied");

  await registrarVenta(c, M, venta("e5v7", [{ productoId: "e5_sin_conteo", varianteId: "x5", cantidad: 3 }]));
  r.igual("Producto sin conteo se vende sin tocar cantidades", await stock(c, "e5_sin_conteo").then(s => s.x5), 0);
  const enc = await registrarVenta(c, M, venta("e5v8", [{ productoId: "e5_a_pedido", varianteId: "x4", cantidad: 2 }]));
  const encItem = (await c.query("select items->0->>'encargo' e from ventas where id='e5v8'")).rows[0].e;
  r.igual("Un producto a pedido queda registrado como encargo", [enc.estado, encItem], ["REGISTRADA", "true"]);
  r.falla("Una opción agotada no se vende como entrega inmediata",
    await error(registrarVenta(c, M, venta("e5v9", [{ productoId: "e5_agotado_pedido", varianteId: "x3", cantidad: 1 }]))), "STOCK_INSUFICIENTE");
  const enc2 = await registrarVenta(c, M, venta("e5v10", [{ productoId: "e5_agotado_pedido", varianteId: "x3", cantidad: 1, encargo: true }]));
  r.igual("La misma opción sí se puede anotar como encargo", [enc2.estado, await stock(c, "e5_agotado_pedido").then(s => s.x3)], ["REGISTRADA", 0]);

  // ---------- Pieza única ----------
  await registrarVenta(c, M, venta("e5v11", [{ productoId: "e5_pieza", varianteId: "pz_a", cantidad: 1 }]));
  const pz = await stock(c, "e5_pieza");
  r.ok("Pieza única vendida queda marcada y sin unidades", pz._vendida && pz.pz_a === 0 && pz.pz_b === 0, pz);
  r.igual("La pieza vendida desaparece del catálogo público", (await como(c, null, "select 1 from productos where id='e5_pieza'")).rowCount, 0);
  r.falla("No se puede vender dos veces", await error(registrarVenta(c, M, venta("e5v12", [{ productoId: "e5_pieza", varianteId: "pz_b", cantidad: 1 }]))), "PIEZA_VENDIDA");
  await guardarProducto(c, M, producto("e5_pieza", { modalidad: "REGULAR", variantes: [{ id: "pz_a", nombre: "Azul", stock: 1 }] }));
  await como(c, M, "update productos set vendida_en = null, publicado = true where id = 'e5_pieza'");
  r.igual("Ninguna edición vuelve a publicarla (RN-08)", (await como(c, null, "select 1 from productos where id='e5_pieza'")).rowCount, 0);
  r.ok("vendida_en es permanente", (await stock(c, "e5_pieza"))._vendida);

  // ---------- Ajuste de cantidades sin pisar ventas ----------
  await guardarProducto(c, M, producto("e5_ajuste", { variantes: [{ id: "aj", nombre: "U", stock: 5 }] }));
  await registrarVenta(c, M, venta("e5v13", [{ productoId: "e5_ajuste", varianteId: "aj", cantidad: 2 }]));
  await guardarProducto(c, M, producto("e5_ajuste", { variantes: [{ id: "aj", nombre: "U", stock: 7, stockBase: 5 }] }));
  r.igual("Editar con stockBase suma la diferencia a lo que queda (5-2+2)", await stock(c, "e5_ajuste").then(s => s.aj), 5);
  r.falla("Si la diferencia deja la opción bajo cero, se informa conflicto",
    await error(guardarProducto(c, M, producto("e5_ajuste", { variantes: [{ id: "aj", nombre: "U", stock: 0, stockBase: 6 }] }))), "CONFLICTO_STOCK");

  // ---------- Clientes antiguos ----------
  await como(c, M, `insert into productos (id, tienda_id, nombre, precio, publicado, variantes)
     values ('e5_opciones', 't_minimarket_rosa', 'Nombre desde APK viejo', 1000, true, '[{"id":"op_a","nombre":"Chica","stock":99},{"id":"op_b","nombre":"Grande","stock":99}]')
     on conflict (id) do update set nombre = excluded.nombre, variantes = excluded.variantes, precio = excluded.precio, publicado = excluded.publicado`);
  const viejo = (await c.query("select nombre, variantes, modalidad, al_agotarse from productos where id='e5_opciones'")).rows[0];
  r.igual("APK antiguo: el nombre se actualiza", viejo.nombre, "Nombre desde APK viejo");
  r.igual("APK antiguo: no pisa cantidades", viejo.variantes.map(v => v.stock), [5, 1]);
  r.igual("APK antiguo: no borra precio ni foto de opciones", viejo.variantes.map(v => [v.precio, v.foto_url]), [[1000, "https://ejemplo.invalid/a.jpg"], [9000, "https://ejemplo.invalid/b.jpg"]]);
  r.falla("APK antiguo: su subida directa de ventas se rechaza con un error claro",
    await error(como(c, M, "insert into ventas (id, tienda_id, total, items) values ('e5_vieja', 't_minimarket_rosa', 1, '[]')")), "permission denied");
  r.falla("Tampoco puede modificar ventas existentes", await error(como(c, M, "update ventas set total = 1 where id = 'e5v1'")), "permission denied");
  await como(c, M, `insert into productos (id, tienda_id, nombre, precio, publicado, variantes) values ('e5_web_vieja', 't_minimarket_rosa', 'Alta web antigua', 2000, true, '[{"nombre":"Unidad","precio":2000}]')`);
  r.ok("Una opción sin id recibe un id estable", (await c.query("select variantes->0->>'id' i from productos where id='e5_web_vieja'")).rows[0].i === "e5_web_vieja_v1");

  // ---------- Ventas privadas e historial ----------
  r.igual("El admin no ve ventas (RN-10)", (await como(c, A, "select 1 from ventas")).rowCount, 0);
  r.igual("Otra emprendedora no ve ventas ajenas", (await como(c, B, "select 1 from ventas where tienda_id='t_minimarket_rosa'")).rowCount, 0);
  const anonVentas = await como(c, null, "select 1 from ventas").then(x => x.rowCount, e => "rechazado: " + e.message);
  r.ok("Sin sesión no se ven ventas", anonVentas === 0 || String(anonVentas).startsWith("rechazado"), anonVentas);
  const antes = (await c.query("select count(*)::int n from ventas where tienda_id='t_minimarket_rosa' and en_historial")).rows[0].n;
  await como(c, M, "update tiendas set historial_activo = false where id = 't_minimarket_rosa'");
  await registrarVenta(c, M, venta("e5v14", [{ productoId: "e5_visible", varianteId: "e5_visible_a", cantidad: 1, precio: 1500 }], { medioPago: "EFECTIVO", lugarTipo: "OTRO", lugarNombre: "Casa" }));
  const min = (await c.query("select items, total, medio_pago, lugar_nombre, en_historial from ventas where id='e5v14'")).rows[0];
  r.ok("Historial desactivado: solo registro técnico mínimo",
    min.en_historial === false && min.total === null && min.medio_pago === null && min.lugar_nombre === null && !("precio" in min.items[0]), min);
  r.igual("Historial desactivado: igual descuenta la cantidad", await stock(c, "e5_visible").then(s => s.e5_visible_a), 4);
  r.igual("Las ventas anteriores no se borran", (await c.query("select count(*)::int n from ventas where tienda_id='t_minimarket_rosa' and en_historial")).rows[0].n, antes);
  await como(c, M, "update tiendas set historial_activo = true where id = 't_minimarket_rosa'");

  // ---------- Lugar de venta ----------
  r.falla("Feria inexistente rechazada", await error(registrarVenta(c, M, venta("e5v15", [{ productoId: "e5_sin_conteo", varianteId: "x5", cantidad: 1 }], { lugarTipo: "FERIA", feriaId: "no_existe" }))), "FERIA_NO_ENCONTRADA");
  await como(c, A, "insert into ferias (id, nombre) values ('e5_feria', 'Feria de prueba') on conflict do nothing");
  await registrarVenta(c, M, venta("e5v16", [{ productoId: "e5_sin_conteo", varianteId: "x5", cantidad: 1 }], { lugarTipo: "FERIA", feriaId: "e5_feria" }));
  r.igual("Venta en feria de la plataforma", (await c.query("select feria_id, lugar_tipo from ventas where id='e5v16'")).rows[0], { feria_id: "e5_feria", lugar_tipo: "FERIA" });

  // ---------- Tipos de producto ----------
  await como(c, A, `insert into tipos_producto (id, categoria, nombre, campos) values ('prueba_provisional', 'TEXTIL', 'Tipo provisional de prueba', '[{"clave":"talla","etiqueta":"Talla","tipo":"texto"}]')`);
  r.igual("Sin sesión se leen los tipos de producto", (await como(c, null, "select 1 from tipos_producto")).rowCount, 1);
  r.igual("Una emprendedora no crea tipos", (await error(como(c, M, "insert into tipos_producto (id, categoria, nombre) values ('x','TEXTIL','X')"))) !== null, true);

  // ---------- Storage ----------
  await como(c, M, `insert into storage.objects (bucket_id, name) values ('productos', '${M}/e5.jpg')`);
  r.falla("No se puede mover una foto propia a una carpeta ajena",
    await error(como(c, M, `update storage.objects set name = '${B}/robada.jpg' where name = '${M}/e5.jpg'`)), "row-level security");
  r.igual("Otra persona no borra fotos ajenas", (await como(c, B, `delete from storage.objects where name = '${M}/e5.jpg'`)).rowCount, 0);
  r.ok("Sin sesión no se suben fotos", (await error(como(c, null, `insert into storage.objects (bucket_id, name) values ('productos', 'x/y.jpg')`))) !== null);
};
