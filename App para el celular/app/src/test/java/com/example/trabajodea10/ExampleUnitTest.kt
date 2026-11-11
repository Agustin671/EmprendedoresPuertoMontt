package com.example.trabajodea10

import com.example.trabajodea10.data.Categoria
import com.example.trabajodea10.data.Rol
import com.example.trabajodea10.data.SesionManager
import com.example.trabajodea10.data.TramaStore
import com.example.trabajodea10.data.remoto.ClienteSupabase
import com.example.trabajodea10.data.remoto.ProductoRepositorio
import com.example.trabajodea10.ui.navegacion.ControladorNavegacion
import com.example.trabajodea10.ui.navegacion.Ruta
import com.example.trabajodea10.ui.navegacion.itemsSegunRol
import kotlinx.serialization.json.jsonObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pruebas locales (se ejecutan en el computador, sin celular).
 */
class ExampleUnitTest {

    @Test
    fun entrarConTiendaDePrueba() {
        SesionManager.entrarModoDesarrolladorPrueba("t_minimarket_rosa", Rol.EMPRENDEDORA)
        assertEquals("Minimarket Doña Marta", SesionManager.nombreUsuarioActual)
        assertEquals(Rol.EMPRENDEDORA, TramaStore.rol)
        assertEquals("t_minimarket_rosa", TramaStore.tienda.id)

        val nav = ControladorNavegacion()
        nav.iniciarSesion(Rol.EMPRENDEDORA)
        assertEquals(Ruta.EmpInicio, nav.actual)
        assertEquals(5, itemsSegunRol(Rol.EMPRENDEDORA).size)
    }

    @Test
    fun soloSeVenLosProductosDeMiTienda() {
        SesionManager.entrarModoDesarrolladorPrueba("t_reposteria_surena", Rol.EMPRENDEDORA)
        val mios = TramaStore.productosDeMiTienda()
        assertTrue(mios.isNotEmpty())
        assertTrue(mios.all { it.tiendaId == "t_reposteria_surena" })
    }

    @Test
    fun leerProductoQueLlegaDelServidor() {
        val texto = """
            {"id":"p1","tienda_id":"t1","nombre":"Mermelada de murta","descripcion":"Frasco 250 g",
             "categoria":"GOURMET","precio":3500,"publicado":true,"destacado":false,
             "color_foto":4287523732,"variantes":[{"id":"v1","nombre":"Frasco","stock":12}],
             "foto_url":null}
        """.trimIndent()
        val producto = ProductoRepositorio.desdeJson(ClienteSupabase.json.parseToJsonElement(texto).jsonObject)
        assertEquals("Mermelada de murta", producto.nombre)
        assertEquals(Categoria.GOURMET, producto.categoria)
        assertEquals(3500.0, producto.precio, 0.0)
        assertEquals(12, producto.stockTotal)
        assertEquals(null, producto.fotoUrl)
        assertEquals("Frasco 250 g", producto.descripcion)
    }
}
