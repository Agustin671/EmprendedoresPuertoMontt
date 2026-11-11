package com.example.trabajodea10

import com.example.trabajodea10.data.AlAgotarse
import com.example.trabajodea10.data.BorradorProducto
import com.example.trabajodea10.data.Categoria
import com.example.trabajodea10.data.EstadoPublico
import com.example.trabajodea10.data.EstadoSync
import com.example.trabajodea10.data.FiltrosHistorial
import com.example.trabajodea10.data.ItemVenta
import com.example.trabajodea10.data.MedioPago
import com.example.trabajodea10.data.Modalidad
import com.example.trabajodea10.data.PersistenciaStore
import com.example.trabajodea10.data.Producto
import com.example.trabajodea10.data.ReglasCatalogo
import com.example.trabajodea10.data.Tienda
import com.example.trabajodea10.data.TramaStore
import com.example.trabajodea10.data.ValoresProducto
import com.example.trabajodea10.data.Variante
import com.example.trabajodea10.data.Venta
import com.example.trabajodea10.data.remoto.ClienteSupabase
import com.example.trabajodea10.data.remoto.ProductoRepositorio
import com.example.trabajodea10.data.remoto.StockServidor
import com.example.trabajodea10.data.remoto.VentaRepositorio
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File

/**
 * Entrega 5: reglas del catálogo, contratos JSON con Supabase, ventas,
 * borradores y sincronización (lógica que corre en el computador).
 * La seguridad real (RLS, ventas atómicas) se prueba en tests/sql contra PostgreSQL.
 */
class Entrega5Test {

    private val miTienda = "t_prueba_e5"

    private fun producto(id: String, vararg stock: Int, modalidad: Modalidad = Modalidad.REGULAR, maneja: Boolean = true) = Producto(
        id = id, tiendaId = miTienda, nombre = "Producto $id", categoria = Categoria.TEXTIL, precio = 1000.0,
        publicado = true, destacado = false, colorFoto = 0xFF000000,
        variantes = stock.mapIndexed { i, s -> Variante("${id}_$i", "Opción $i", s, stockBase = s) },
        modalidad = modalidad, manejaStock = maneja
    )

    @Before
    fun preparar() {
        TramaStore.guardarTienda(Tienda(id = miTienda, nombre = "Tienda de prueba"))
        TramaStore.productos.removeAll { it.tiendaId == miTienda }
        TramaStore.ventas.clear()
        TramaStore.descartarBorrador()
    }

    // ------------------------------------------------------------ contrato compartido

    @Test
    fun estadoPublicoCumpleElContratoCompartidoConWebYServidor() {
        val archivo = listOf("../../tests/contrato/casos_estado.json", "../tests/contrato/casos_estado.json")
            .map { File(it) }.first { it.exists() }
        val casos = ClienteSupabase.json.parseToJsonElement(archivo.readText()).jsonObject["casos"]!!.jsonArray
        assertTrue(casos.size >= 10)
        casos.forEach { c ->
            val o = c.jsonObject
            val p = o["p"]!!.jsonObject
            val prod = Producto(
                id = "c", nombre = "c", categoria = Categoria.TEXTIL, precio = 1.0,
                publicado = p["publicado"]!!.jsonPrimitive.booleanOrNull!!, destacado = false, colorFoto = 0,
                disponible = p["disponible"]!!.jsonPrimitive.booleanOrNull!!,
                modalidad = Modalidad.valueOf(p["modalidad"]!!.jsonPrimitive.contentOrNull!!),
                manejaStock = p["manejaStock"]!!.jsonPrimitive.booleanOrNull!!,
                alAgotarse = AlAgotarse.valueOf(p["alAgotarse"]!!.jsonPrimitive.contentOrNull!!),
                vendidaEn = if (p["vendida"]?.jsonPrimitive?.booleanOrNull == true) "2026-11-10" else null,
                variantes = (p["variantes"] as JsonArray).mapIndexed { i, s -> Variante("v$i", "O$i", s.jsonPrimitive.intOrNull!!) }
            )
            assertEquals(o["nombre"]!!.jsonPrimitive.contentOrNull, EstadoPublico.valueOf(o["estado"]!!.jsonPrimitive.contentOrNull!!), ReglasCatalogo.estadoPublico(prod))
        }
    }

    // ------------------------------------------------------------ JSON con Supabase

    @Test
    fun leeOpcionesConPrecioFotoYCantidadPropiosDesdeElServidor() {
        val texto = """{"id":"p1","tienda_id":"t1","nombre":"Chal","categoria":"TEXTIL","precio":1000,"publicado":true,
            "disponible":false,"modalidad":"PIEZA_UNICA","maneja_stock":true,"al_agotarse":"OCULTAR","vendida_en":null,
            "tipo":"chal","atributos":{"material":"lana"},
            "variantes":[{"id":"a","nombre":"Azul","stock":1,"precio":null,"foto_url":"https://x/a.jpg"},
                         {"id":"b","nombre":"Roja","stock":0,"precio":9000,"foto_url":null}]}"""
        val p = ProductoRepositorio.desdeJson(ClienteSupabase.json.parseToJsonElement(texto).jsonObject)
        assertEquals(Modalidad.PIEZA_UNICA, p.modalidad)
        assertFalse(p.disponible)
        assertEquals("lana", p.atributos["material"])
        assertEquals(listOf(null, 9000.0), p.variantes.map { it.precio })
        assertEquals("https://x/a.jpg", p.variantes[0].fotoUrl)
        assertEquals(listOf(1, 0), p.variantes.map { it.stockBase })
    }

    @Test
    fun productoDeVersionAnteriorSinCamposNuevosConservaElComportamiento() {
        val texto = """{"id":"p_viejo","tienda_id":"t1","nombre":"Pan","categoria":"GENERAL","precio":2500,"publicado":true,
            "variantes":[{"nombre":"Bolsa","precio":2500}]}"""
        val p = ProductoRepositorio.desdeJson(ClienteSupabase.json.parseToJsonElement(texto).jsonObject)
        assertEquals(Modalidad.REGULAR, p.modalidad)
        assertTrue(p.manejaStock && p.disponible)
        assertEquals("p_viejo_v1", p.variantes[0].id)
        assertEquals(0, p.variantes[0].stock)
    }

    @Test
    fun guardarEnviaDiferenciasDeCantidadYNuncaLaTiendaNiRutasDelCelular() {
        val p = producto("g", 5).let { it.copy(fotoUrl = "/data/user/0/app/files/productos/g.jpg", variantes = listOf(it.variantes[0].copy(stock = 7, stockBase = 5), Variante("nueva", "Nueva", 2))) }
        val cuerpo = ProductoRepositorio.cuerpoGuardar(p)
        assertNull(cuerpo["tiendaId"])
        assertEquals("null", cuerpo["fotoUrl"].toString())
        val v = cuerpo["variantes"]!!.jsonArray.map { it.jsonObject }
        assertEquals("5", v[0]["stockBase"].toString())
        assertNull("una opción nueva no lleva base", v[1]["stockBase"])
    }

    @Test
    fun ventaSeEnviaConOpcionCantidadEncargoYLugarSinTienda() {
        val venta = Venta("v1", "2026-11-10 10:00", listOf(ItemVenta("p", "a", "Pan", "Bolsa", 1000.0, 2, encargo = true)),
            MedioPago.EFECTIVO, null, 2000.0, lugarTipo = "OTRO", lugarNombre = "Plaza")
        val cuerpo = VentaRepositorio.cuerpoRegistrar(venta)
        assertNull(cuerpo["tiendaId"])
        assertEquals("\"Plaza\"", cuerpo["lugarNombre"].toString())
        val item = (cuerpo["items"] as JsonArray)[0] as JsonObject
        assertEquals("2", item["cantidad"].toString())
        assertEquals("true", item["encargo"].toString())
        assertEquals("\"a\"", item["varianteId"].toString())
    }

    // ------------------------------------------------------------ ventas

    @Test
    fun laVentaDescuentaSoloLaOpcionVendidaYSuBase() {
        TramaStore.productos.add(producto("pv", 5, 3))
        val r = TramaStore.registrarVenta(listOf(ItemVenta("pv", "pv_1", "Producto", "Opción 1", 1000.0, 2)), MedioPago.EFECTIVO, null, "Plaza")
        assertTrue(r.isSuccess)
        val p = TramaStore.productoPorId("pv")!!
        assertEquals(listOf(5, 1), p.variantes.map { it.stock })
        assertEquals("la base también baja: una edición posterior no vuelve a descontar", listOf(5, 1), p.variantes.map { it.stockBase })
        assertEquals("Plaza", r.getOrThrow().lugarNombre)
        assertEquals("OTRO", r.getOrThrow().lugarTipo)
    }

    @Test
    fun noSeVendeMasDeLoQueQuedaNiSumandoLineas() {
        TramaStore.productos.add(producto("ps", 2))
        val lineas = listOf(ItemVenta("ps", "ps_0", "P", "O", 1000.0, 1), ItemVenta("ps", "ps_0", "P", "O", 1000.0, 2))
        val r = TramaStore.registrarVenta(lineas, MedioPago.EFECTIVO, null)
        assertTrue(r.isFailure)
        assertEquals(2, TramaStore.productoPorId("ps")!!.variantes[0].stock)
        assertTrue(TramaStore.ventas.isEmpty())
    }

    @Test
    fun opcionAgotadaSoloSeAnotaComoEncargoYNoDescuenta() {
        TramaStore.productos.add(producto("pe", 0).copy(alAgotarse = AlAgotarse.PASAR_A_PEDIDO))
        assertTrue(TramaStore.registrarVenta(listOf(ItemVenta("pe", "pe_0", "P", "O", 1000.0, 1)), MedioPago.EFECTIVO, null).isFailure)
        assertTrue(TramaStore.registrarVenta(listOf(ItemVenta("pe", "pe_0", "P", "O", 1000.0, 1, encargo = true)), MedioPago.EFECTIVO, null).isSuccess)
        assertEquals(0, TramaStore.productoPorId("pe")!!.variantes[0].stock)
    }

    @Test
    fun piezaUnicaVendidaSeOcultaYNoSeVendeDosVeces() {
        TramaStore.productos.add(producto("pu", 1, 0, modalidad = Modalidad.PIEZA_UNICA))
        assertTrue(TramaStore.registrarVenta(listOf(ItemVenta("pu", "pu_1", "P", "O", 1000.0, 1)), MedioPago.EFECTIVO, null).isSuccess)
        val p = TramaStore.productoPorId("pu")!!
        assertEquals(EstadoPublico.OCULTO, ReglasCatalogo.estadoPublico(p))
        assertTrue(TramaStore.registrarVenta(listOf(ItemVenta("pu", "pu_0", "P", "O", 1000.0, 1)), MedioPago.EFECTIVO, null).isFailure)
    }

    @Test
    fun conflictoDelServidorConservaLaVentaYDevuelveLasUnidades() {
        TramaStore.productos.add(producto("pc", 3))
        val venta = TramaStore.registrarVenta(listOf(ItemVenta("pc", "pc_0", "P", "O", 1000.0, 2)), MedioPago.EFECTIVO, null).getOrThrow()
        TramaStore.marcarConflictoVenta(venta.id, "No quedan unidades suficientes de esa opción.")
        val v = TramaStore.ventas.first { it.id == venta.id }
        assertEquals(EstadoSync.CONFLICTO, v.estadoSync)
        assertEquals(3, TramaStore.productoPorId("pc")!!.variantes[0].stock)
        // Reintentar vuelve a descontar y la deja pendiente; anular solo aplica a conflictos.
        assertNull(TramaStore.reintentarVenta(venta.id))
        assertEquals(1, TramaStore.productoPorId("pc")!!.variantes[0].stock)
    }

    @Test
    fun confirmacionUsaCantidadesDelServidorSinPerderEdicionesPorSubir() {
        // Base 10 en servidor; en el celular se repusieron 2 (12) y luego se vendió 1.
        TramaStore.productos.add(producto("pr", 10).let { it.copy(variantes = listOf(it.variantes[0].copy(stock = 12, stockBase = 10))) })
        val venta = TramaStore.registrarVenta(listOf(ItemVenta("pr", "pr_0", "P", "O", 1000.0, 1)), MedioPago.EFECTIVO, null).getOrThrow()
        // Mientras tanto otro dispositivo vendió 3: el servidor responde 6 (10 - 3 - 1).
        TramaStore.confirmarVenta(venta.id, listOf(StockServidor("pr", listOf(Variante("pr_0", "O", 6)), null)))
        val v = TramaStore.productoPorId("pr")!!.variantes[0]
        assertEquals(6, v.stockBase)
        assertEquals("se mantiene la reposición de 2 que falta subir", 8, v.stock)
    }

    @Test
    fun conHistorialDesactivadoSoloQuedaElRegistroMinimo() {
        TramaStore.guardarTienda(TramaStore.tienda.copy(historialActivo = false))
        TramaStore.productos.add(producto("ph", 4))
        val venta = TramaStore.registrarVenta(listOf(ItemVenta("ph", "ph_0", "P", "O", 1500.0, 1)), MedioPago.EFECTIVO, null, "Casa").getOrThrow()
        assertFalse(venta.enHistorial)
        TramaStore.confirmarVenta(venta.id, emptyList())
        val guardada = TramaStore.ventas.first { it.id == venta.id }
        assertEquals(0.0, guardada.total, 0.0)
        assertNull(guardada.lugarNombre)
        assertEquals(0.0, guardada.items[0].precio, 0.0)
        assertEquals(3, TramaStore.productoPorId("ph")!!.variantes[0].stock)
    }

    // ------------------------------------------------------------ sincronización y borradores

    @Test
    fun unBorradoPendienteNoReapareceAlDescargarDelServidor() {
        val remotos = listOf(producto("r1", 1), producto("r2", 1))
        TramaStore.reemplazarProductosDeTienda(miTienda, remotos, conservar = emptySet(), eliminados = setOf("r2"))
        assertNotNull(TramaStore.productoPorId("r1"))
        assertNull(TramaStore.productoPorId("r2"))
    }

    @Test
    fun productoNuevoNaceConLosValoresDeLaTiendaYSeRetomaSinTerminar() {
        TramaStore.guardarTienda(TramaStore.tienda.copy(valoresProducto = ValoresProducto(categoria = Categoria.GOURMET, modalidad = Modalidad.A_PEDIDO, alAgotarse = AlAgotarse.OCULTAR, publicado = true)))
        val id = TramaStore.crearProductoBorrador()
        val b = TramaStore.borrador!!
        assertEquals(Categoria.GOURMET, b.producto.categoria)
        assertEquals(Modalidad.A_PEDIDO, b.producto.modalidad)
        assertTrue(b.producto.publicado)
        assertEquals("un segundo «Añadir» retoma el mismo borrador", id, TramaStore.crearProductoBorrador())
        assertNull("el borrador no aparece en el catálogo hasta guardarlo", TramaStore.productoPorId(id))
    }

    @Test
    fun borradorConSoloUnaFotoNoSeDescartaAlSalir() {
        val id = TramaStore.crearProductoBorrador()
        TramaStore.actualizarBorrador(TramaStore.borrador!!.producto.copy(fotoUrl = "/ruta/foto.jpg"), 0)
        TramaStore.descartarSiEstaVacio(id)
        assertNotNull(TramaStore.borrador)
        TramaStore.actualizarBorrador(TramaStore.borrador!!.producto.copy(fotoUrl = null), 0)
        TramaStore.descartarSiEstaVacio(id)
        assertNull("vacío de verdad: se descarta", TramaStore.borrador)
    }

    @Test
    fun alGuardarElBorradorQuedaEnElCatalogo() {
        val id = TramaStore.crearProductoBorrador()
        TramaStore.actualizarBorrador(TramaStore.borrador!!.producto.copy(nombre = "  Queque  ", precio = 3000.0), 4)
        val p = TramaStore.finalizarBorrador()!!
        assertEquals("Queque", p.nombre)
        assertNotNull(TramaStore.productoPorId(id))
        assertNull(TramaStore.borrador)
    }

    @Test
    fun cambioMasivoDeDisponibilidadSoloAfectaMiTienda() {
        TramaStore.productos.add(producto("m1", 1)); TramaStore.productos.add(producto("m2", 1))
        TramaStore.productos.add(producto("ajeno", 1).copy(tiendaId = "otra"))
        assertEquals(2, TramaStore.cambiarDisponibilidad(listOf("m1", "m2", "ajeno"), false))
        assertFalse(TramaStore.productoPorId("m1")!!.disponible)
        assertTrue(TramaStore.productoPorId("ajeno")!!.disponible)
        assertTrue("no disponible sigue en su tienda", TramaStore.productosDeMiTienda().any { it.id == "m1" })
        TramaStore.productos.removeAll { it.id == "ajeno" }
    }

    @Test
    fun estadoGuardadoDeVersionAnteriorSeLeeConValoresPorDefecto() {
        val antiguo = """{"rol":"EMPRENDEDORA","productos":[{"id":"x","nombre":"Pan","categoria":"GOURMET","precio":1.0,
            "publicado":true,"destacado":false,"colorFoto":0,"variantes":[{"id":"v","nombre":"U","stock":3}]}],"ventas":[]}"""
        val estado = PersistenciaStore.decodificar(antiguo)!!
        val p = estado.productos.first()
        assertEquals(Modalidad.REGULAR, p.modalidad)
        assertTrue(p.disponible)
        assertNull(estado.borrador)
        val conBorrador = estado.copy(borrador = BorradorProducto(p, esNuevo = true, paso = 2))
        assertEquals(2, PersistenciaStore.decodificar(PersistenciaStore.codificar(conBorrador))!!.borrador!!.paso)
    }

    // ------------------------------------------------------------ historial y validaciones

    @Test
    fun historialFiltraPorFechaFeriaYProducto() {
        fun v(id: String, fecha: String, feria: String?, prod: String) = Venta(id, fecha, listOf(ItemVenta(prod, "a", prod, "a", 1.0, 1)), MedioPago.EFECTIVO, feria, 1.0)
        val ventas = listOf(v("1", "2026-11-01 10:00", "f1", "pan"), v("2", "2026-11-05 10:00", null, "pan"), v("3", "2026-11-09 10:00", "f1", "miel"))
        assertEquals(listOf("2", "3"), FiltrosHistorial.filtrar(ventas, desde = "2026-11-02").map { it.id })
        assertEquals(listOf("1", "3"), FiltrosHistorial.filtrar(ventas, feriaId = "f1").map { it.id })
        assertEquals(listOf("2"), FiltrosHistorial.filtrar(ventas, feriaId = FiltrosHistorial.SIN_FERIA).map { it.id })
        assertEquals(listOf("3"), FiltrosHistorial.filtrar(ventas, productoId = "miel").map { it.id })
        assertEquals(listOf("1"), FiltrosHistorial.filtrar(ventas, hasta = "2026-11-01", feriaId = "f1", productoId = "pan").map { it.id })
    }

    @Test
    fun validacionDelProductoAntesDeGuardar() {
        val base = producto("val", 1)
        assertNull(ReglasCatalogo.validarProducto(base, null))
        assertEquals("Escribe el nombre del producto.", ReglasCatalogo.validarProducto(base.copy(nombre = " "), null))
        assertTrue(ReglasCatalogo.validarProducto(base.copy(modalidad = Modalidad.PIEZA_UNICA, variantes = listOf(Variante("a", "A", 1), Variante("b", "B", 1))), null)!!.contains("una sola unidad"))
        val tipo = com.example.trabajodea10.data.TipoProducto("t", "TEXTIL", "Prueba provisional", listOf(com.example.trabajodea10.data.CampoTipo("talla", "Talla", obligatorio = true)))
        assertEquals("Completa «Talla».", ReglasCatalogo.validarProducto(base, tipo))
    }
}
