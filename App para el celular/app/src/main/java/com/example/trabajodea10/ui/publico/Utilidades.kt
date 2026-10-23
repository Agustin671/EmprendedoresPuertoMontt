package com.example.trabajodea10.ui.publico

import android.content.Context
import android.content.Intent
import android.net.Uri

/**
 * Vitrina pública que se comparte por WhatsApp y se codifica en el QR.
 */
const val URL_VITRINA = "https://agustin671.github.io/EmprendedoresPuertoMontt/"

/**
 * Acciones externas (mapas, teléfono, WhatsApp) pensadas para botones grandes.
 * Se usan desde las pantallas públicas (Visitante) para que las personas mayores
 * puedan llamar o escribir con un solo toque, sin escribir números.
 */

/** Abre la app de mapas del teléfono con la dirección buscada. */
fun abrirMapa(contexto: Context, direccion: String) {
    val uri = Uri.parse("geo:0,0?q=" + Uri.encode(direccion))
    val intent = Intent(Intent.ACTION_VIEW, uri).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    runCatching { contexto.startActivity(intent) }
}

/** Abre el marcador con el número listo para llamar (la persona solo toca "llamar"). */
fun llamarTelefono(contexto: Context, numero: String) {
    val numeroLimpio = numero.replace(Regex("[^+0-9]"), "")
    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$numeroLimpio"))
    runCatching { contexto.startActivity(intent) }
}

/** Abre WhatsApp con un mensaje ya escrito, para pedir ayuda o consultar un producto. */
fun abrirWhatsApp(contexto: Context, numero: String, mensaje: String) {
    val numeroLimpio = numero.replace(Regex("[^0-9]"), "")
    val uri = Uri.parse("https://wa.me/$numeroLimpio?text=" + Uri.encode(mensaje))
    val intent = Intent(Intent.ACTION_VIEW, uri).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    runCatching { contexto.startActivity(intent) }
}

/** Comparte un texto directamente en WhatsApp; si no está instalado, abre el selector. */
fun compartirPorWhatsApp(contexto: Context, mensaje: String) {
    val enviar = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, mensaje)
        setPackage("com.whatsapp")
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    val abrioWhatsApp = runCatching { contexto.startActivity(enviar) }.isSuccess
    if (!abrioWhatsApp) {
        val alternativo = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, mensaje)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        runCatching {
            contexto.startActivity(Intent.createChooser(alternativo, "Compartir tienda"))
        }
    }
}