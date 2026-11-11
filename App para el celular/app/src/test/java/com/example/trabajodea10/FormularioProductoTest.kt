package com.example.trabajodea10

import com.example.trabajodea10.data.AlAgotarse
import com.example.trabajodea10.data.CampoTipo
import com.example.trabajodea10.data.Categoria
import com.example.trabajodea10.data.FormularioProducto
import com.example.trabajodea10.data.Modalidad
import com.example.trabajodea10.data.Producto
import com.example.trabajodea10.data.ReglasCatalogo
import com.example.trabajodea10.data.TipoProducto
import com.example.trabajodea10.data.Variante
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Comportamiento del asistente de productos (lógica sin interfaz): preguntas
 * simples sin borrar datos, cantidades que nunca cambian solas y errores
 * ubicados en el paso donde se corrigen.
 */
class FormularioProductoTest {

    private fun sencillo(stock: Int = 3) = Producto(
        id = "p", nombre = "Queque", categoria = Categoria.GOURMET, precio = 3000.0, publicado = false,
        destacado = false, colorFoto = 0, variantes = listOf(Variante("p_v1", "Única", stock))
    )

    private fun conOpciones() = sencillo().copy(variantes = listOf(
        Variante("a", "Chico", 2, precio = null, fotoUrl = null),
        Variante("b", "Grande", 1, precio = 5000.0, fotoUrl = "/fotos/b.jpg")
    ))

    @Test
    fun unProductoConUnaSolaOpcionPorDefectoEsSencillo() {
        assertFalse(FormularioProducto.tieneOpciones(sencillo()))
        assertTrue(FormularioProducto.tieneOpciones(conOpciones()))
        // Una sola opción con nombre, precio o foto propios también cuenta como «con opciones».
        assertTrue(FormularioProducto.tieneOpciones(sencillo().copy(variantes = listOf(Variante("x", "Talla M", 1)))))
        assertTrue(FormularioProducto.tieneOpciones(sencillo().copy(variantes = listOf(Variante("x", "Única", 1, precio = 10.0)))))
    }

    @Test
    fun responderQueTieneOpcionesNoBorraNiCreaDatos() {
        val p = FormularioProducto.activarOpciones(sencillo(stock = 4))
        assertEquals(1, p.variantes.size)
        assertEquals("se conserva la cantidad", 4, p.variantes[0].stock)
        assertEquals("se conserva el id", "p_v1", p.variantes[0].id)
        assertEquals("el nombre técnico «Única» queda para que lo escriba", "", p.variantes[0].nombre)
        assertTrue(FormularioProducto.tieneOpciones(p))
    }

    @Test
    fun volverASencilloSinPerderNadaNoPideConfirmacion() {
        val p = FormularioProducto.activarOpciones(sencillo())
        assertTrue(FormularioProducto.perdidasAlSimplificar(p).isEmpty())
        val vuelta = FormularioProducto.simplificar(p)
        assertEquals("Única", vuelta.variantes.single().nombre)
        assertEquals(3, vuelta.variantes.single().stock)
    }

    @Test
    fun volverASencilloConVariasOpcionesDiceExactamenteQueSePierde() {
        val perdidas = FormularioProducto.perdidasAlSimplificar(conOpciones())
        assertEquals(1, perdidas.size)
        assertTrue(perdidas[0].contains("Grande"))
        assertTrue(perdidas[0].contains("1"))
        // Solo se aplica si la persona lo confirma: conserva la primera opción y su cantidad.
        val p = FormularioProducto.simplificar(conOpciones())
        assertEquals(listOf("a"), p.variantes.map { it.id })
        assertEquals(2, p.variantes[0].stock)
    }

    @Test
    fun unaOpcionConPrecioOFotoPropiosAvisaAntesDeQuitarlos() {
        val p = sencillo().copy(variantes = listOf(Variante("x", "Talla M", 1, precio = 9000.0, fotoUrl = "/f.jpg")))
        val perdidas = FormularioProducto.perdidasAlSimplificar(p)
        assertTrue(perdidas.any { it.contains("precio") })
        assertTrue(perdidas.any { it.contains("foto") })
    }

    @Test
    fun cambiarComoSeVendeNuncaCambiaCantidadesNiOtrasRespuestas() {
        val inicial = conOpciones().copy(manejaStock = false, alAgotarse = AlAgotarse.PASAR_A_PEDIDO)
        val unica = FormularioProducto.cambiarModalidad(inicial, Modalidad.PIEZA_UNICA)
        assertEquals(inicial.variantes, unica.variantes)
        val deVuelta = FormularioProducto.cambiarModalidad(FormularioProducto.cambiarModalidad(unica, Modalidad.A_PEDIDO), Modalidad.REGULAR)
        assertEquals(inicial.variantes, deVuelta.variantes)
        assertEquals("se recuerda lo elegido al agotarse", AlAgotarse.PASAR_A_PEDIDO, deVuelta.alAgotarse)
        assertFalse(deVuelta.manejaStock)
    }

    @Test
    fun soloSePreguntaLoPertinenteSegunComoSeVende() {
        val p = sencillo()
        assertTrue(FormularioProducto.pideCantidades(p))
        assertTrue(FormularioProducto.pideAlAgotarse(p))
        assertFalse(FormularioProducto.pideCantidades(p.copy(modalidad = Modalidad.A_PEDIDO)))
        assertFalse(FormularioProducto.pideAlAgotarse(p.copy(modalidad = Modalidad.A_PEDIDO)))
        assertFalse(FormularioProducto.pideCantidades(p.copy(manejaStock = false)))
        // La pieza única siempre cuenta su unidad y siempre se oculta al venderse.
        assertTrue(FormularioProducto.pideCantidades(p.copy(modalidad = Modalidad.PIEZA_UNICA, manejaStock = false)))
        assertFalse(FormularioProducto.pideAlAgotarse(p.copy(modalidad = Modalidad.PIEZA_UNICA)))
    }

    @Test
    fun losErroresSeUbicanEnElPasoYCampoDondeSeCorrigen() {
        val p = sencillo().copy(nombre = " ", precio = 0.0)
        val errores = FormularioProducto.errores(p, null)
        assertEquals(setOf(FormularioProducto.Campo.NOMBRE, FormularioProducto.Campo.PRECIO), errores.map { it.campo }.toSet())
        assertTrue(errores.all { it.paso == 1 })

        val sinNombreDeOpcion = conOpciones().copy(variantes = conOpciones().variantes.map { if (it.id == "b") it.copy(nombre = "") else it })
        val e = FormularioProducto.errores(sinNombreDeOpcion, null).single()
        assertEquals(2, e.paso)
        assertEquals("b", e.opcionId)
    }

    @Test
    fun piezaUnicaConMasDeUnaUnidadSeExplicaSinCorregirlaSola() {
        val simple = sencillo(stock = 3).copy(modalidad = Modalidad.PIEZA_UNICA)
        val e = FormularioProducto.errores(simple, null).single()
        assertEquals(FormularioProducto.Campo.PIEZA_UNICA, e.campo)
        assertEquals("en un producto sencillo la cantidad está en Disponibilidad", 3, e.paso)
        assertEquals("no se tocan las cantidades", 3, simple.variantes[0].stock)
        val varias = conOpciones().copy(modalidad = Modalidad.PIEZA_UNICA)
        assertEquals("con opciones las cantidades están en Opciones", 2, FormularioProducto.errores(varias, null).single().paso)
    }

    @Test
    fun camposObligatoriosDelTipoSeMarcanEnDatos() {
        val tipo = TipoProducto("t", "GOURMET", "Prueba provisional", listOf(CampoTipo("peso", "Peso", obligatorio = true)))
        val e = FormularioProducto.errores(sencillo(), tipo).single()
        assertEquals(1, e.paso)
        assertEquals("peso", e.clave)
        assertTrue(FormularioProducto.errores(sencillo().copy(atributos = mapOf("peso" to "1 kg")), tipo).isEmpty())
    }

    @Test
    fun sinErroresDelFormularioTampocoHayErroresDeLasReglasDelCatalogo() {
        listOf(sencillo(), conOpciones(), sencillo(stock = 1).copy(modalidad = Modalidad.PIEZA_UNICA), sencillo().copy(modalidad = Modalidad.A_PEDIDO))
            .forEach { p ->
                assertTrue(FormularioProducto.errores(p, null).isEmpty())
                assertNull(ReglasCatalogo.validarProducto(ReglasCatalogo.normalizar(p), null))
            }
    }

    @Test
    fun elAvisoDeGuardadoNoPrometeLoQueAunNoOcurre() {
        val visible = sencillo().copy(publicado = true)
        val conServidor = FormularioProducto.mensajeGuardado(conServidor = true, producto = visible)
        assertTrue(conServidor.contains("celular"))
        assertTrue(conServidor.contains("se subirá") || conServidor.contains("subiendo"))
        assertFalse(conServidor.contains("ya se ve", ignoreCase = true))
        val local = FormularioProducto.mensajeGuardado(conServidor = false, producto = visible)
        assertTrue(local.contains("solo en este celular"))
        assertTrue(FormularioProducto.mensajeGuardado(true, sencillo()).contains("Solo tú lo ves"))
    }
}
