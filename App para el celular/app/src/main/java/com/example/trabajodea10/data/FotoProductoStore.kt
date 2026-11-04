package com.example.trabajodea10.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import com.example.trabajodea10.data.remoto.ClienteSupabase
import com.example.trabajodea10.data.remoto.ErrorSupabase
import java.io.File
import kotlin.math.max

/**
 * Fotos de productos.
 * - Copia la foto elegida al almacenamiento interno de la app, ya COMPRIMIDA
 *   (lado mayor 1280 px, JPEG 80 %), para gastar pocos datos al subirla (RNF-REN-01).
 * - Corrige la rotación que guardan algunas cámaras.
 * - Sube la foto a Supabase Storage dentro de la carpeta de la usuaria.
 */
object FotoProductoStore {

    private const val CARPETA = "productos"
    private const val LADO_MAX = 1280
    private const val CALIDAD_JPEG = 80

    fun copiarDesdeUri(contexto: Context, uri: Uri, productoId: String): String? {
        return runCatching {
            val resolver = contexto.contentResolver

            // 1. Leer solo el tamaño para no cargar en memoria una foto gigante.
            val limites = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            // (en este modo decodeStream siempre devuelve null; solo llena outWidth/outHeight)
            val flujoLimites = resolver.openInputStream(uri) ?: return null
            flujoLimites.use { BitmapFactory.decodeStream(it, null, limites) }
            if (limites.outWidth <= 0 || limites.outHeight <= 0) return null
            var muestreo = 1
            while (max(limites.outWidth, limites.outHeight) / (muestreo * 2) >= LADO_MAX) muestreo *= 2

            // 2. Cargar la foto reducida.
            val opciones = BitmapFactory.Options().apply { inSampleSize = muestreo }
            var imagen: Bitmap = resolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, opciones)
            } ?: return null

            // 3. Ajustar al tamaño máximo.
            val escala = LADO_MAX.toFloat() / max(imagen.width, imagen.height)
            if (escala < 1f) {
                imagen = Bitmap.createScaledBitmap(
                    imagen,
                    (imagen.width * escala).toInt().coerceAtLeast(1),
                    (imagen.height * escala).toInt().coerceAtLeast(1),
                    true
                )
            }

            // 4. Girar según la orientación guardada por la cámara.
            val grados = resolver.openInputStream(uri)?.use { flujo ->
                when (ExifInterface(flujo).getAttributeInt(
                    ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL
                )) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                    ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                    ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                    else -> 0f
                }
            } ?: 0f
            if (grados != 0f) {
                val matriz = Matrix().apply { postRotate(grados) }
                imagen = Bitmap.createBitmap(imagen, 0, 0, imagen.width, imagen.height, matriz, true)
            }

            // 5. Guardar comprimida.
            val carpeta = File(contexto.filesDir, CARPETA).apply { mkdirs() }
            val destino = File(carpeta, "${productoId}_${System.currentTimeMillis()}.jpg")
            destino.outputStream().use { salida ->
                imagen.compress(Bitmap.CompressFormat.JPEG, CALIDAD_JPEG, salida)
            }
            destino.absolutePath
        }.getOrNull()
    }

    /** Sube una foto guardada en el celular y devuelve su URL pública. */
    suspend fun subir(rutaLocal: String, productoId: String): String {
        val sesion = ClienteSupabase.sesion ?: throw ErrorSupabase(401, "No hay sesión iniciada.")
        val archivo = File(rutaLocal)
        if (!archivo.exists()) throw ErrorSupabase(0, "La foto ya no está en el celular.")
        val ruta = "${sesion.userId}/${productoId}_${System.currentTimeMillis()}.jpg"
        return ClienteSupabase.subirFoto(ruta, archivo.readBytes())
    }

    fun eliminar(ruta: String?) {
        if (ruta.isNullOrBlank() || ruta.startsWith("http")) return
        runCatching {
            val archivo = File(ruta)
            if (archivo.exists() && archivo.parentFile?.name == CARPETA) {
                archivo.delete()
            }
        }
    }
}
