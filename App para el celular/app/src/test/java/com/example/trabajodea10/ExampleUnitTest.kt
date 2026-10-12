package com.example.trabajodea10

import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun testEntrarModoDemoDonaRosa() {
        com.example.trabajodea10.data.FirebaseManager.entrarModoDemo("Doña Rosa", com.example.trabajodea10.data.Rol.EMPRENDEDORA)
        assertEquals("Doña Rosa", com.example.trabajodea10.data.FirebaseManager.nombreUsuarioDemo)
        assertEquals(com.example.trabajodea10.data.Rol.EMPRENDEDORA, com.example.trabajodea10.data.TramaStore.rol)
        assertEquals("t_minimarket_rosa", com.example.trabajodea10.data.TramaStore.tienda.id)

        val nav = com.example.trabajodea10.ui.navegacion.ControladorNavegacion()
        nav.iniciarSesion(com.example.trabajodea10.data.Rol.EMPRENDEDORA)
        assertEquals(com.example.trabajodea10.ui.navegacion.Ruta.EmpTienda, nav.actual)

        val items = com.example.trabajodea10.ui.navegacion.itemsSegunRol(com.example.trabajodea10.data.Rol.EMPRENDEDORA)
        assertEquals(5, items.size)
        items.forEach { println("Item: ${it.etiqueta} - ${it.icono.name}") }

        val c1 = androidx.compose.ui.graphics.Color(0xFF805AD5)
        val c2 = androidx.compose.ui.graphics.Color(0xFF805AD5.toLong())
        val c3 = androidx.compose.ui.graphics.Color(com.example.trabajodea10.data.TramaStore.tienda.colorPortada)
        val c4 = androidx.compose.ui.graphics.Color(com.example.trabajodea10.data.TramaStore.tienda.colorLogo)
        println("Colors: $c1, $c2, $c3, $c4")
    }
}