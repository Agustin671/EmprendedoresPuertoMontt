package com.example.trabajodea10.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString

/**
 * Guarda y recupera todo el estado de la app en SharedPreferences usando JSON.
 * Así los datos no se pierden al cerrar la aplicación.
 */
object PersistenciaStore {

    private const val NOMBRE_ARCHIVO = "trama_estado"
    private const val CLAVE_ESTADO = "estado_app"

    private val json = Json {
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

    fun cargar(): EstadoApp? {
        val texto = preferencias?.getString(CLAVE_ESTADO, null) ?: return null
        return runCatching { json.decodeFromString<EstadoApp>(texto) }.getOrNull()
    }

    fun guardar(estado: EstadoApp) {
        val prefs = preferencias ?: return
        runCatching {
            val texto = json.encodeToString(estado)
            prefs.edit().putString(CLAVE_ESTADO, texto).apply()
        }
    }

    fun borrar() {
        preferencias?.edit()?.remove(CLAVE_ESTADO)?.apply()
    }
}
