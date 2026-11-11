package com.example.trabajodea10.data

/**
 * La huella (o el PIN del celular) abre solo la cuenta que la activó.
 * Se guarda el id de esa cuenta; "si" es el formato de versiones anteriores.
 */
object ReglaHuella {
    private const val FORMATO_ANTERIOR = "si"

    fun abreEstaCuenta(guardado: String?, uidSesion: String?): Boolean =
        uidSesion != null && guardado != null && (guardado == uidSesion || guardado == FORMATO_ANTERIOR)

    /** Al entrar con contraseña: si la activación era de otra cuenta (o antigua), se olvida y se vuelve a ofrecer. */
    fun debeOlvidar(guardado: String?, uidNuevo: String): Boolean = guardado != null && guardado != uidNuevo
}
