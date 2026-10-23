package com.example.trabajodea10

import android.app.Application
import android.util.Log
import com.example.trabajodea10.data.TramaStore
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions

/**
 * Punto de arranque de la aplicación. Se encarga de inicializar el almacenamiento
 * local y asegurar la conexión a Firebase (Auth, Firestore y Cloud Storage).
 */
class AplicacionTrama : Application() {
    override fun onCreate() {
        super.onCreate()
        TramaStore.inicializar(this)

        // Inicialización robusta de Firebase con credenciales del proyecto alercei7
        try {
            if (FirebaseApp.getApps(this).isEmpty()) {
                val opciones = FirebaseOptions.Builder()
                    .setApplicationId("1:60295127775:android:4e6ec2188b21076f01ff6a")
                    .setApiKey("AIzaSyBmxcinIPVJB-AKgHQQ9iEp3p6-uRS58oI")
                    .setProjectId("alercei7")
                    .setStorageBucket("alercei7.firebasestorage.app")
                    .setGcmSenderId("60295127775")
                    .build()
                FirebaseApp.initializeApp(this, opciones)
                Log.i("AplicacionTrama", "Firebase inicializado con éxito para alercei7")
            } else {
                Log.i("AplicacionTrama", "Firebase ya se encontraba inicializado")
            }
        } catch (e: Exception) {
            Log.e("AplicacionTrama", "Error al inicializar Firebase: ${e.message}")
        }
    }
}
