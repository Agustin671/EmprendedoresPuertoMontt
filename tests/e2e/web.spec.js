"use strict";
// Recorridos visibles de la vitrina y de la gestión web con un Supabase sustituto.
// Prueban la interfaz (lo que ve y hace la persona); la seguridad real del
// servidor (RLS, ventas atómicas) se prueba en tests/sql contra PostgreSQL.
const { test, expect } = require("@playwright/test");
const fs = require("fs");
const path = require("path");
const { RAIZ } = require("./servidor");

const ORIGEN = "http://alerce.test";
const FALSO = fs.readFileSync(path.join(__dirname, "supabase_falso.js"), "utf8");
const TIPOS = { ".html": "text/html", ".js": "text/javascript", ".css": "text/css", ".otf": "font/otf", ".png": "image/png" };

const TIENDA = { id: "t_prueba", owner_id: "u-marta", nombre: "Tejidos de Prueba", rubro: "Artesanía", sector: "Alerce Sur", bio: "Tienda de prueba", whatsapp: "+56911112222", color_portada: 4284232027, color_logo: 4281236827, necesidades: {}, historial_activo: true, valores_producto: { categoria: "GOURMET", modalidad: "REGULAR", manejaStock: true, alAgotarse: "OCULTAR", publicado: true } };
function producto(id, extra) {
  return Object.assign({ id, tienda_id: "t_prueba", nombre: "Producto " + id, descripcion: "Descripción de " + id, categoria: "TEXTIL", precio: 1000,
    publicado: true, disponible: true, modalidad: "REGULAR", maneja_stock: true, al_agotarse: "MOSTRAR_AGOTADO", vendida_en: null,
    foto_url: "https://falso.supabase.co/p/" + id + ".jpg", variantes: [{ id: id + "_a", nombre: "Única", stock: 3 }] }, extra || {});
}

/** Prepara la página: archivos locales servidos como alerce.test, Supabase sustituto y configuración. */
async function preparar(page, estadoFalso, { config = true } = {}) {
  await page.route(ORIGEN + "/**", (route) => {
    const ruta = decodeURIComponent(new URL(route.request().url()).pathname);
    if (ruta.endsWith("/js/config.js")) {
      return route.fulfill({ contentType: "text/javascript", body: config ? 'window.__CONFIG__ = {"supabase":{"url":"https://falso.supabase.co","anonKey":"clave-de-prueba"}};' : 'window.__CONFIG__ = {"supabase":{"url":"","anonKey":""}};' });
    }
    const archivo = path.join(RAIZ, ruta === "/" ? "index.html" : ruta);
    if (!archivo.startsWith(RAIZ) || !fs.existsSync(archivo)) return route.fulfill({ status: 404, body: "" });
    return route.fulfill({ contentType: TIPOS[path.extname(archivo)] || "application/octet-stream", body: fs.readFileSync(archivo) });
  });
  await page.route("https://cdn.jsdelivr.net/npm/@supabase/**", (route) => route.fulfill({ contentType: "text/javascript", body: FALSO }));
  await page.route("https://falso.supabase.co/auth/v1/settings", (route) => route.fulfill({ contentType: "application/json", body: JSON.stringify({ external: { google: false } }) }));
  await page.route(/^https:\/\/(fonts\.|picsum|images\.unsplash|falso\.supabase\.co\/(p|storage))/, (route) => route.fulfill({ status: 200, contentType: "image/svg+xml", body: "<svg xmlns='http://www.w3.org/2000/svg' width='10' height='10'/>" }));
  await page.addInitScript((estado) => { window.__FALSO__ = estado; }, estadoFalso);
}

function datosPublicos(productos) {
  return { tablas: { tiendas: [TIENDA], productos, perfiles: [], tipos_producto: [{ id: "tp", categoria: "TEXTIL", nombre: "Chaleco", campos: [], activo: true, orden: 0 }], ferias: [], ventas: [] } };
}

test.describe("Vitrina pública (sin sesión)", () => {
  test("muestra nombre, foto, descripción, precio, categoría y estado; no muestra ocultos", async ({ page }) => {
    await preparar(page, datosPublicos([
      producto("visible", { tipo: "tp" }),
      producto("oculto", { publicado: false }),
      producto("no_disp", { disponible: false }),
      producto("agotado", { variantes: [{ id: "x", nombre: "U", stock: 0 }] }),
      producto("ocultar", { al_agotarse: "OCULTAR", variantes: [{ id: "x", nombre: "U", stock: 0 }] }),
      producto("pedido", { al_agotarse: "PASAR_A_PEDIDO", variantes: [{ id: "x", nombre: "U", stock: 0 }] }),
      producto("pieza_vendida", { modalidad: "PIEZA_UNICA", vendida_en: "2026-11-10T10:00:00Z", variantes: [{ id: "x", nombre: "U", stock: 0 }] })
    ]));
    await page.goto(ORIGEN + "/?tienda=t_prueba");
    const lista = page.locator("#lista-productos-tienda-modal");
    await expect(lista.locator("[data-producto-vitrina]")).toHaveCount(3);
    const visible = lista.locator('[data-producto-vitrina="visible"]');
    await expect(visible.locator(".prod-nombre")).toHaveText("Producto visible");
    await expect(visible.locator(".prod-descripcion")).toHaveText("Descripción de visible");
    await expect(visible.locator(".prod-precio")).toHaveText("$ 1.000");
    await expect(visible.locator(".prod-categoria")).toHaveText("Textil · Chaleco");
    await expect(visible.locator("img.prod-foto")).toHaveAttribute("src", "https://falso.supabase.co/p/visible.jpg");
    await expect(lista.locator('[data-producto-vitrina="agotado"] .badge-necesidad')).toHaveText("Agotado");
    await expect(lista.locator('[data-producto-vitrina="agotado"] .btn-anadir-carrito')).toBeDisabled();
    await expect(lista.locator('[data-producto-vitrina="pedido"] .badge-necesidad')).toHaveText("A pedido");
    for (const id of ["oculto", "no_disp", "ocultar", "pieza_vendida"]) await expect(lista.locator(`[data-producto-vitrina="${id}"]`)).toHaveCount(0);
  });

  test("al elegir una opción cambian su precio y su foto", async ({ page }) => {
    await preparar(page, datosPublicos([producto("op", { precio: 1000, variantes: [
      { id: "a", nombre: "Chica", stock: 2, precio: null, foto_url: null },
      { id: "b", nombre: "Grande", stock: 1, precio: 9000, foto_url: "https://falso.supabase.co/p/grande.jpg" }] })]));
    await page.goto(ORIGEN + "/?tienda=t_prueba");
    const card = page.locator('[data-producto-vitrina="op"]');
    await expect(card.locator(".prod-precio")).toHaveText("$ 1.000");
    await card.getByRole("button", { name: "Grande" }).click();
    const nueva = page.locator('[data-producto-vitrina="op"]');
    await expect(nueva.locator(".prod-precio")).toHaveText("$ 9.000");
    await expect(nueva.locator("img.prod-foto")).toHaveAttribute("src", "https://falso.supabase.co/p/grande.jpg");
    await expect(nueva.getByRole("button", { name: "Grande" })).toHaveAttribute("aria-pressed", "true");
  });

  test("el texto del servidor no se interpreta como HTML", async ({ page }) => {
    await preparar(page, datosPublicos([producto("xss", { nombre: '<img src=x onerror="window.__inyectado=1">Malo', foto_url: 'javascript:alert(1)' })]));
    await page.goto(ORIGEN + "/?tienda=t_prueba");
    await expect(page.locator('[data-producto-vitrina="xss"] .prod-nombre')).toHaveText('<img src=x onerror="window.__inyectado=1">Malo');
    expect(await page.evaluate(() => window.__inyectado)).toBeUndefined();
    await expect(page.locator('[data-producto-vitrina="xss"] img')).toHaveCount(0);
  });

  test("un error de red se informa y se puede reintentar; no equivale a catálogo vacío", async ({ page }) => {
    const estado = datosPublicos([producto("p1")]);
    estado.fallar = { "productos:select": "Failed to fetch" };
    await preparar(page, estado);
    await page.goto(ORIGEN + "/");
    const aviso = page.locator("#estado-datos-vitrina");
    await expect(aviso).toContainText("No se pudo cargar el catálogo");
    await expect(page.locator("#texto-estado-sincronizacion")).toHaveText("Error de conexión con el servidor");
    await page.evaluate(() => { window.__FALSO__.fallar = {}; });
    await aviso.getByRole("button", { name: "Reintentar" }).click();
    await expect(aviso).toBeHidden();
    await page.locator("#grilla-tiendas-marketplace .tarjeta-tienda").getByRole("button", { name: "Ver vitrina" }).click();
    await expect(page.locator('[data-producto-vitrina="p1"]')).toBeVisible();
  });

  test("una respuesta válida vacía elimina lo que se mostraba", async ({ page }) => {
    await preparar(page, datosPublicos([producto("p1")]));
    await page.goto(ORIGEN + "/?tienda=t_prueba");
    await expect(page.locator('[data-producto-vitrina="p1"]')).toBeVisible();
    await page.evaluate(() => { window.__FALSO__.tablas.productos = []; return window.TramaWeb.cargarCatalogoPublico(); });
    await expect(page.locator('[data-producto-vitrina="p1"]')).toHaveCount(0);
    await expect(page.locator("#lista-productos-tienda-modal")).toContainText("aún no tiene productos");
  });

  test("producción sin configuración: aviso honesto y sin tiendas de ejemplo", async ({ page }) => {
    await preparar(page, datosPublicos([]), { config: false });
    await page.goto(ORIGEN + "/");
    await expect(page.locator("#estado-datos-vitrina")).toContainText("no está conectada a la base de datos");
    await expect(page.locator("#grilla-tiendas-marketplace .tarjeta-tienda")).toHaveCount(0);
    await expect(page.getByText("Minimarket Doña Rosa")).toHaveCount(0);
  });

  test("Google no habilitado: se informa y no se simula una sesión", async ({ page }) => {
    await preparar(page, datosPublicos([]));
    await page.goto(ORIGEN + "/");
    await page.evaluate(() => window.TramaWeb.abrirModalAuthNormal());
    await page.getByRole("button", { name: "Continuar con Google" }).click();
    await expect(page.locator("#auth-aviso-error")).toContainText("aún no está habilitado");
    await expect(page.locator("#tag-nombre-usuario")).toHaveText("Inicio de Sesión / Registro");
  });
});

test.describe("Emprendedora con sesión", () => {
  function estadoConSesion(productos) {
    const e = datosPublicos(productos);
    e.tablas.perfiles = [{ id: "u-marta", nombre: "Marta", rol: "EMPRENDEDORA", tienda_id: "t_prueba", foto_url: null }];
    e.sesion = { user: { id: "u-marta", email: "marta@prueba.cl", user_metadata: {} }, access_token: "falso" };
    return e;
  }

  test("crea un producto con opciones y solo anuncia éxito tras la respuesta del servidor", async ({ page }) => {
    const e = estadoConSesion([]);
    e.respuestasRpc = { guardar_producto: [{ error: { message: "CONFLICTO_STOCK" } }, { data: producto("nuevo", { nombre: "Chal", precio: 5000 }) }] };
    await preparar(page, e);
    await page.goto(ORIGEN + "/");
    await expect(page.locator("#btn-abrir-publicar-nav")).toBeVisible();
    await page.locator("#btn-abrir-publicar-nav").click();
    // Valores iniciales de la tienda
    await expect(page.locator("#pub-categoria")).toHaveValue("GOURMET");
    await expect(page.locator("#pub-al-agotarse")).toHaveValue("OCULTAR");
    await expect(page.locator("#pub-publicado")).toBeChecked();
    await page.locator("#pub-nombre").fill("Chal");
    await page.locator("#pub-precio").fill("5000");
    await page.locator("#pub-op-0-nombre").fill("Azul");
    await page.locator("#pub-op-0-stock").fill("2");
    await page.locator("#pub-agregar-opcion").click();
    await page.locator("#pub-op-1-nombre").fill("Rojo");
    await page.locator("#pub-op-1-precio").fill("6000");
    await page.locator("#pub-op-1-stock").fill("1");
    await page.locator("#pub-guardar").click();
    await expect(page.locator("#pub-aviso")).toContainText("No se guardó");
    await expect(page.locator("#modal-publicar-producto")).toHaveClass(/abierto/);
    await page.locator("#pub-guardar").click();
    await expect(page.locator("#modal-publicar-producto")).not.toHaveClass(/abierto/);
    await expect(page.locator("#toast-texto")).toContainText("Guardado en el servidor");
    const llamada = await page.evaluate(() => window.__FALSO__.llamadas.filter((x) => x.rpc === "guardar_producto").pop().args.p);
    expect(llamada.nombre).toBe("Chal");
    expect(llamada.variantes.map((v) => [v.nombre, v.precio, v.stock])).toEqual([["Azul", null, 2], ["Rojo", 6000, 1]]);
    expect(llamada.tiendaId).toBeUndefined();
  });

  test("vender: el error de unidades se muestra y la venta no se da por hecha; reintento por red usa el mismo id", async ({ page }) => {
    const e = estadoConSesion([producto("pan", { variantes: [{ id: "a", nombre: "Bolsa", stock: 2 }] })]);
    e.respuestasRpc = { registrar_venta: [
      { error: { message: "STOCK_INSUFICIENTE", details: '{"disponible":1}' } },
      { red: true },
      { data: { estado: "REGISTRADA", total: 1000, productos: [{ id: "pan", variantes: [{ id: "a", nombre: "Bolsa", stock: 1 }], vendida_en: null }] } }
    ] };
    await preparar(page, e);
    await page.goto(ORIGEN + "/");
    await page.locator("#btn-abrir-pos-nav").click();
    await page.getByRole("button", { name: /Agregar Producto pan/ }).click();
    await page.locator("#pos-btn-confirmar").click();
    await expect(page.locator("#pos-aviso")).toContainText("quedan 1");
    await page.locator("#pos-btn-confirmar").click();
    await expect(page.locator("#pos-aviso")).toContainText("sin conexión");
    page.once("dialog", (d) => d.dismiss());
    await page.locator("#pos-btn-confirmar").click();
    await expect(page.locator("#pos-aviso")).toContainText("Venta registrada en el servidor");
    const ids = await page.evaluate(() => window.__FALSO__.llamadas.filter((x) => x.rpc === "registrar_venta").map((x) => x.args.p_venta.id));
    expect(ids[1]).toBe(ids[2]);
    expect(ids[0]).not.toBe(ids[1]);
    await expect(page.getByRole("button", { name: /Agregar Producto pan/ })).toContainText("Quedan 1");
  });

  test("al cerrar sesión no quedan datos privados de la cuenta", async ({ page }) => {
    const e = estadoConSesion([producto("publico"), producto("privado", { publicado: false })]);
    await preparar(page, e);
    await page.goto(ORIGEN + "/");
    await page.locator("#btn-abrir-catalogo-nav").click();
    await expect(page.locator("#lista-items-catalogo-gestion")).toContainText("Producto privado");
    await page.keyboard.press("Escape");
    page.once("dialog", (d) => d.accept());
    await page.evaluate(() => { window.__FALSO__.tablas.productos = window.__FALSO__.tablas.productos.filter((p) => p.publicado); });
    await page.locator("#btn-logout").click();
    await expect(page.locator("#btn-abrir-catalogo-nav")).toBeHidden();
    await expect(page.locator("#lista-items-catalogo-gestion")).toBeEmpty();
    const enMemoria = await page.evaluate(() => document.body.innerText.includes("Producto privado"));
    expect(enMemoria).toBe(false);
  });
});

test.describe("Adaptación a pantallas", () => {
  for (const ancho of [320, 375, 390, 768, 1280]) {
    test(`sin desborde horizontal a ${ancho}px (inicio, vitrina y formulario)`, async ({ page }) => {
      const e = datosPublicos([producto("p1", { nombre: "Chaleco de lana chilota tejido a palillo con nombre muy largo", variantes: [{ id: "a", nombre: "Gris natural talla M", stock: 2 }, { id: "b", nombre: "Blanco crudo talla L", stock: 1, precio: 42000 }] })]);
      e.tablas.perfiles = [{ id: "u-marta", nombre: "Marta Emprendedora de Alerce", rol: "EMPRENDEDORA", tienda_id: "t_prueba" }];
      e.sesion = { user: { id: "u-marta", email: "marta@prueba.cl", user_metadata: {} } };
      await page.setViewportSize({ width: ancho, height: 800 });
      await preparar(page, e);
      await page.goto(ORIGEN + "/");
      await expect(page.locator("#grilla-tiendas-marketplace .tarjeta-tienda")).toHaveCount(1);
      const medir = () => page.evaluate(() => ({ doc: document.documentElement.scrollWidth, vista: window.innerWidth }));
      let m = await medir();
      expect(m.doc, "inicio").toBeLessThanOrEqual(m.vista);
      await page.evaluate(() => window.TramaWeb.abrirVitrinaTienda("t_prueba"));
      await expect(page.locator('[data-producto-vitrina="p1"]')).toBeVisible();
      m = await medir();
      expect(m.doc, "vitrina").toBeLessThanOrEqual(m.vista);
      const panel = await page.locator("#modal-tienda-vitrina .modal-panel-editorial").evaluate((el) => ({ sw: el.scrollWidth, cw: el.clientWidth }));
      expect(panel.sw, "contenido del modal").toBeLessThanOrEqual(panel.cw + 1);
      await page.keyboard.press("Escape");
      await page.locator("#btn-abrir-publicar-nav").click();
      await page.waitForTimeout(350);
      const form = await page.locator("#modal-publicar-producto .modal-panel-editorial").evaluate((el) => ({ sw: el.scrollWidth, cw: el.clientWidth }));
      expect(form.sw, "formulario").toBeLessThanOrEqual(form.cw + 1);
      await page.screenshot({ path: path.join(__dirname, "..", "..", "test-results", `capturas`, `formulario-${ancho}.png`), fullPage: false });
    });
  }

  test("el foco entra al modal, no se escapa con Tab y vuelve al cerrar", async ({ page }) => {
    await preparar(page, datosPublicos([producto("p1")]));
    await page.goto(ORIGEN + "/");
    const boton = page.locator("#grilla-tiendas-marketplace .tarjeta-tienda").getByRole("button", { name: "Ver vitrina" });
    await boton.focus();
    await page.keyboard.press("Enter");
    await expect(page.locator("#modal-tienda-vitrina")).toHaveClass(/abierto/);
    await expect.poll(() => page.evaluate(() => document.getElementById("modal-tienda-vitrina").contains(document.activeElement))).toBe(true);
    for (let i = 0; i < 25; i++) await page.keyboard.press("Tab");
    expect(await page.evaluate(() => document.getElementById("modal-tienda-vitrina").contains(document.activeElement))).toBe(true);
    await page.keyboard.press("Escape");
    await expect(boton).toBeFocused();
  });
});
