package com.example.trabajodea10

import com.example.trabajodea10.data.ReglaHuella
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** La huella (o el PIN del celular) solo abre la cuenta que la activó. */
class ReglaHuellaTest {

    @Test
    fun abreSoloLaCuentaQueLaActivo() {
        assertTrue(ReglaHuella.abreEstaCuenta(guardado = "uid-a", uidSesion = "uid-a"))
        assertFalse("otra cuenta entró con contraseña después", ReglaHuella.abreEstaCuenta(guardado = "uid-a", uidSesion = "uid-b"))
        assertFalse("sin sesión guardada no hay a qué entrar", ReglaHuella.abreEstaCuenta(guardado = "uid-a", uidSesion = null))
        assertFalse("no activada", ReglaHuella.abreEstaCuenta(guardado = null, uidSesion = "uid-a"))
    }

    @Test
    fun alEntrarConContrasenaDeOtraCuentaSeOlvidaLaActivacionAnterior() {
        assertTrue(ReglaHuella.debeOlvidar(guardado = "uid-a", uidNuevo = "uid-b"))
        assertFalse(ReglaHuella.debeOlvidar(guardado = "uid-a", uidNuevo = "uid-a"))
        assertFalse(ReglaHuella.debeOlvidar(guardado = null, uidNuevo = "uid-b"))
        // Formato de versiones anteriores («si», sin cuenta): se vuelve a preguntar.
        assertTrue(ReglaHuella.debeOlvidar(guardado = "si", uidNuevo = "uid-a"))
    }

    @Test
    fun elFormatoAnteriorSigueSirviendoHastaElProximoIngresoConContrasena() {
        assertTrue(ReglaHuella.abreEstaCuenta(guardado = "si", uidSesion = "uid-a"))
    }
}
