package com.example.trabajodea10.data

import kotlin.math.max

/**
 * Reglas del catálogo: las mismas que aplican el servidor
 * (supabase/05_catalogo_ventas.sql, estado_publico_producto) y la web
 * (js/catalogo.js). Contrato: docs/entrega5/contrato-catalogo.md.
 * Sin dependencias de Android: se prueban en el computador.
 */
object ReglasCatalogo {

    /** Unidades que todavía se pueden vender entre todas las opciones. */
    fun unidades(p: Producto): Int = p.variantes.sumOf { max(0, it.stock) }

    fun estadoPublico(p: Producto): EstadoPublico {
        if (!p.publicado || !p.disponible) return EstadoPublico.OCULTO
        if (p.vendidaEn != null) return EstadoPublico.OCULTO
        if (p.modalidad == Modalidad.A_PEDIDO) return EstadoPublico.A_PEDIDO
        val u = unidades(p)
        if (p.modalidad == Modalidad.PIEZA_UNICA) return if (u > 0) EstadoPublico.DISPONIBLE else EstadoPublico.OCULTO
        if (!p.manejaStock) return EstadoPublico.DISPONIBLE
        if (u > 0) return EstadoPublico.DISPONIBLE
        return when (p.alAgotarse) {
            AlAgotarse.OCULTAR -> EstadoPublico.OCULTO
            AlAgotarse.PASAR_A_PEDIDO -> EstadoPublico.A_PEDIDO
            AlAgotarse.MOSTRAR_AGOTADO -> EstadoPublico.AGOTADO
        }
    }

    /** Por qué el público no lo ve (para la vista previa de la emprendedora). */
    fun motivoOculto(p: Producto): String? = when {
        !p.publicado -> "Solo tú lo ves"
        !p.disponible -> "Marcado como no disponible"
        p.vendidaEn != null -> "Pieza única vendida"
        estadoPublico(p) == EstadoPublico.OCULTO -> "Se acabó y está configurado para ocultarse"
        else -> null
    }

    /** ¿Se puede vender esta opción como entrega inmediata? */
    fun opcionVendible(p: Producto, v: Variante?): Boolean {
        if (v == null || p.vendidaEn != null) return false
        if (p.modalidad == Modalidad.A_PEDIDO) return false
        if (p.modalidad == Modalidad.PIEZA_UNICA) return unidades(p) > 0
        if (!p.manejaStock) return true
        return v.stock > 0
    }

    /** Se puede anotar como encargo (no descuenta unidades). */
    fun aceptaEncargo(p: Producto): Boolean =
        p.vendidaEn == null && p.modalidad != Modalidad.PIEZA_UNICA &&
            (p.modalidad == Modalidad.A_PEDIDO || (p.manejaStock && p.alAgotarse == AlAgotarse.PASAR_A_PEDIDO))

    /** Valida una línea de venta antes de registrarla (el servidor vuelve a validar). Devuelve el problema o null. */
    fun validarLinea(p: Producto, v: Variante?, cantidad: Int, encargo: Boolean): String? {
        if (v == null) return "Elige una opción."
        if (cantidad < 1 || cantidad > 9999) return "La cantidad debe ser mayor que cero."
        if (p.vendidaEn != null) return "Esta pieza única ya se vendió."
        if (encargo || p.modalidad == Modalidad.A_PEDIDO) {
            return if (p.modalidad == Modalidad.PIEZA_UNICA) "Una pieza única no se puede encargar." else null
        }
        if (p.modalidad == Modalidad.PIEZA_UNICA) {
            return if (cantidad == 1 && unidades(p) > 0) null else "Una pieza única se vende una sola vez."
        }
        if (!p.manejaStock) return null
        if (v.stock < cantidad) return "Solo quedan ${max(0, v.stock)} de «${v.nombre}»."
        return null
    }

    fun precioDe(p: Producto, v: Variante?): Double = v?.precio ?: p.precio

    fun fotoDe(p: Producto, v: Variante?): String? = v?.fotoUrl?.takeIf { it.isNotBlank() } ?: p.fotoUrl ?: p.fotos.firstOrNull()

    /** Errores de forma antes de guardar un producto. Devuelve el primer problema o null. */
    fun validarProducto(p: Producto, tipo: TipoProducto?): String? {
        if (p.nombre.isBlank()) return "Escribe el nombre del producto."
        if (p.precio <= 0.0) return "Escribe el precio, por ejemplo 2500."
        if (p.variantes.isEmpty()) return "Agrega al menos una opción."
        p.variantes.forEachIndexed { i, v ->
            if (v.nombre.isBlank()) return "Escribe el nombre de la opción ${i + 1}."
            if (v.stock < 0) return "La cantidad de «${v.nombre}» no puede ser negativa."
            if ((v.precio ?: 0.0) < 0.0) return "El precio de «${v.nombre}» no puede ser negativo."
        }
        if (p.variantes.map { it.id }.toSet().size != p.variantes.size) return "Hay dos opciones con el mismo código."
        if (p.modalidad == Modalidad.PIEZA_UNICA && unidades(p) > 1) return "Una pieza única tiene una sola unidad en total."
        tipo?.campos?.firstOrNull { it.obligatorio && p.atributos[it.clave].isNullOrBlank() }?.let {
            return "Completa «${it.etiqueta}»."
        }
        return null
    }

    /** Ajusta reglas que dependen de la modalidad (igual que el servidor). */
    fun normalizar(p: Producto): Producto =
        if (p.modalidad == Modalidad.PIEZA_UNICA) p.copy(manejaStock = true, alAgotarse = AlAgotarse.OCULTAR) else p
}

/** Filtros del historial de ventas (RF-VEN-04). Fechas como "AAAA-MM-DD". */
object FiltrosHistorial {
    const val SIN_FERIA = "__sin_feria__"

    fun filtrar(
        ventas: List<Venta>,
        desde: String? = null,
        hasta: String? = null,
        feriaId: String? = null,
        productoId: String? = null
    ): List<Venta> = ventas.filter { v ->
        val dia = v.fecha.take(10)
        (desde.isNullOrBlank() || dia >= desde) &&
            (hasta.isNullOrBlank() || dia <= hasta) &&
            (feriaId == null || (if (feriaId == SIN_FERIA) v.feriaId == null else v.feriaId == feriaId)) &&
            (productoId == null || v.items.any { it.productoId == productoId })
    }
}
