package com.example.trabajodea10

import android.app.Application
import com.example.trabajodea10.data.TramaStore

/**
 * Punto de arranque de la aplicación. Se encarga de inicializar el almacenamiento
 * local antes de que se dibuje cualquier pantalla, para que el estado guardado
 * esté disponible desde el primer momento.
 */
class AplicacionTrama : Application() {
    override fun onCreate() {
        super.onCreate()
        TramaStore.inicializar(this)
    }
}
