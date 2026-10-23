package com.example.trabajodea10.data

import android.content.Context
import android.net.Uri
import java.io.File

/**
 * Copia las fotos elegidas desde la galería al almacenamiento interno de la app.
 * Así la imagen sigue disponible después de cerrar la aplicación, aunque el
 * permiso temporal de la galería expire.
 */
object FotoProductoStore {

    private const val CARPETA = "productos"

    fun copiarDesdeUri(contexto: Context, uri: Uri, productoId: String): String? {
        return runCatching {
            val carpeta = File(contexto.filesDir, CARPETA).apply { mkdirs() }
            val destino = File(carpeta, "${productoId}_${System.currentTimeMillis()}.jpg")
            val entrada = contexto.contentResolver.openInputStream(uri) ?: return null
            entrada.use { origen ->
                destino.outputStream().use { salida ->
                    origen.copyTo(salida)
                }
            }
            destino.absolutePath
        }.getOrNull()
    }

    fun eliminar(ruta: String?) {
        if (ruta.isNullOrBlank()) return
        runCatching {
            val archivo = File(ruta)
            if (archivo.exists() && archivo.parentFile?.name == CARPETA) {
                archivo.delete()
            }
        }
    }
}
