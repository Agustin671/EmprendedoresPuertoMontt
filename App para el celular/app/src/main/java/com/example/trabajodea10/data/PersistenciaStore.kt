package com.example.trabajodea10.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString

/**
 * Guarda y recupera el estado de la app en SharedPreferences usando JSON.
 * Cada cuenta tiene su propio estado (catálogo, ventas, borrador): al entrar
 * con otra cuenta no se ven ni se suben los datos de la anterior.
 */
object PersistenciaStore {

    private const val NOMBRE_ARCHIVO = "trama_estado"
    private const val CLAVE_ANTIGUA = "estado_app"
    private const val INVITADO = "invitado"

    val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        prettyPrint = false
    }

    private var preferencias: SharedPreferences? = null

    fun inicializar(context: Context) {
        if (preferencias == null) {
            preferencias = context.applicationContext
                .getSharedPreferences(NOMBRE_ARCHIVO, Context.MODE_PRIVATE)
        }
    }

    fun clave(cuenta: String?): String = "estado_" + (cuenta ?: INVITADO)

    /**
     * Estado guardado de una cuenta. Las versiones anteriores guardaban un único
     * estado: se asigna a la cuenta con sesión guardada la primera vez.
     */
    fun cargar(cuenta: String?): EstadoApp? {
        val prefs = preferencias ?: return null
        val texto = prefs.getString(clave(cuenta), null)
            ?: prefs.getString(CLAVE_ANTIGUA, null)?.also {
                prefs.edit().putString(clave(cuenta), it).remove(CLAVE_ANTIGUA).apply()
            }
            ?: return null
        return decodificar(texto)
    }

    fun decodificar(texto: String): EstadoApp? = runCatching { json.decodeFromString<EstadoApp>(texto) }.getOrNull()

    fun codificar(estado: EstadoApp): String = json.encodeToString(estado)

    fun guardar(cuenta: String?, estado: EstadoApp) {
        val prefs = preferencias ?: return
        runCatching { prefs.edit().putString(clave(cuenta), codificar(estado)).apply() }
    }

    fun borrar(cuenta: String?) {
        preferencias?.edit()?.remove(clave(cuenta))?.apply()
    }
}
