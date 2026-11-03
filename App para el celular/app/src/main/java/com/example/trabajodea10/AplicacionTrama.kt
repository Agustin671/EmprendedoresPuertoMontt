package com.example.trabajodea10

import android.app.Application
import com.example.trabajodea10.data.TramaStore
import com.example.trabajodea10.data.remoto.ClienteSupabase
import com.example.trabajodea10.data.remoto.Sincronizador

/**
 * Punto de arranque de la aplicación: prepara la sesión guardada de Supabase,
 * la cola de cambios por subir y los datos guardados en el celular.
 * (Las credenciales NO están en el código: vienen de supabase.properties.)
 */
class AplicacionTrama : Application() {
    override fun onCreate() {
        super.onCreate()
        ClienteSupabase.inicializar(this)
        Sincronizador.inicializar(this)
        TramaStore.inicializar(this)
        // Si quedaron cambios sin subir (por ejemplo, sin señal en la feria), se intentan ahora.
        Sincronizador.subirEnSegundoPlano()
    }
}
