package com.example.trabajodea10.data

/**
 * Lógica del asistente de productos, sin interfaz (se prueba en el computador).
 * Simplifica lo que se muestra sin cambiar el modelo: un producto «sencillo»
 * sigue teniendo una opción interna. Ninguna respuesta borra datos ni cambia
 * cantidades por sí sola; lo destructivo pasa por [perdidasAlSimplificar] y
 * una confirmación explícita.
 */
object FormularioProducto {

    const val NOMBRE_OPCION_UNICA = "Única"

    enum class Campo { NOMBRE, PRECIO, OPCION_NOMBRE, OPCIONES, PIEZA_UNICA, CAMPO_TIPO }

    /** Problema que impide guardar, con el paso (0 a 4) y el campo donde se corrige. */
    data class Error(
        val paso: Int,
        val campo: Campo,
        val mensaje: String,
        val opcionId: String? = null,
        val clave: String? = null
    )

    /** true si la persona usa opciones (tallas, colores, sabores) y no solo la opción interna. */
    fun tieneOpciones(p: Producto): Boolean {
        if (p.variantes.size != 1) return p.variantes.size > 1
        val v = p.variantes[0]
        return v.nombre != NOMBRE_OPCION_UNICA || v.precio != null || v.fotoUrl != null
    }

    /** «Sí, tiene opciones»: la opción existente pasa a ser la primera (se conserva su cantidad e id). */
    fun activarOpciones(p: Producto): Producto {
        if (tieneOpciones(p) || p.variantes.isEmpty()) return p
        return p.copy(variantes = listOf(p.variantes[0].copy(nombre = "")))
    }

    /** Lo que se perdería al volver a «es uno solo» (vacío: no hace falta confirmar). */
    fun perdidasAlSimplificar(p: Producto): List<String> {
        val perdidas = mutableListOf<String>()
        p.variantes.drop(1).forEach { v ->
            val nombre = v.nombre.ifBlank { "sin nombre" }
            perdidas += "La opción «$nombre» (cantidad ${v.stock}" +
                (v.precio?.let { ", precio ${formatearMoneda(it)}" } ?: "") +
                (if (v.fotoUrl != null) ", con foto" else "") + ")"
        }
        p.variantes.firstOrNull()?.let { v ->
            if (v.precio != null) perdidas += "El precio propio de la primera opción (${formatearMoneda(v.precio)}); se usará el del producto"
            if (v.fotoUrl != null) perdidas += "La foto propia de la primera opción; se usará la foto principal"
        }
        return perdidas
    }

    /** Deja una sola opción interna (solo tras confirmar si [perdidasAlSimplificar] no está vacío). */
    fun simplificar(p: Producto): Producto {
        val primera = p.variantes.firstOrNull() ?: return p
        return p.copy(variantes = listOf(primera.copy(nombre = NOMBRE_OPCION_UNICA, precio = null, fotoUrl = null)))
    }

    /** Cambia cómo se vende sin tocar cantidades ni las demás respuestas (las reglas se aplican al guardar). */
    fun cambiarModalidad(p: Producto, m: Modalidad): Producto = p.copy(modalidad = m)

    /** ¿Corresponde preguntar cantidades? (la pieza única siempre cuenta su unidad). */
    fun pideCantidades(p: Producto): Boolean = when (p.modalidad) {
        Modalidad.PIEZA_UNICA -> true
        Modalidad.A_PEDIDO -> false
        Modalidad.REGULAR -> p.manejaStock
    }

    /** ¿Corresponde preguntar qué pasa al acabarse? */
    fun pideAlAgotarse(p: Producto): Boolean = p.modalidad == Modalidad.REGULAR && p.manejaStock

    /** Todos los problemas que impiden guardar, ubicados en su paso y campo. */
    fun errores(p: Producto, tipo: TipoProducto?): List<Error> {
        val lista = mutableListOf<Error>()
        if (p.nombre.isBlank()) lista += Error(1, Campo.NOMBRE, "Escribe el nombre del producto.")
        if (p.precio <= 0.0) lista += Error(1, Campo.PRECIO, "Escribe el precio en pesos, por ejemplo 2500.")
        tipo?.campos?.filter { it.obligatorio && p.atributos[it.clave].isNullOrBlank() }?.forEach {
            lista += Error(1, Campo.CAMPO_TIPO, "Completa «${it.etiqueta}».", clave = it.clave)
        }
        if (p.variantes.isEmpty()) lista += Error(2, Campo.OPCIONES, "Agrega al menos una opción.")
        p.variantes.forEach { v ->
            if (v.nombre.isBlank()) lista += Error(2, Campo.OPCION_NOMBRE, "Escribe el nombre de esta opción, por ejemplo «Talla M».", opcionId = v.id)
        }
        if (p.modalidad == Modalidad.PIEZA_UNICA) {
            val total = ReglasCatalogo.unidades(p)
            if (total > 1) {
                lista += Error(
                    if (tieneOpciones(p)) 2 else 3, Campo.PIEZA_UNICA,
                    "Una pieza única tiene una sola unidad en total y ahora suma $total. Deja 1 en las cantidades."
                )
            }
        }
        return lista
    }

    fun erroresDelPaso(paso: Int, p: Producto, tipo: TipoProducto?): List<Error> = errores(p, tipo).filter { it.paso == paso }

    /** Aviso después de guardar: dice dónde quedó de verdad, sin adelantar la confirmación del servidor. */
    fun mensajeGuardado(conServidor: Boolean, producto: Producto): String {
        val quien = if (ReglasCatalogo.estadoPublico(producto) == EstadoPublico.OCULTO) "Solo tú lo ves." else "Lo verá el público cuando llegue al servidor."
        return if (conServidor) "Guardado en tu celular; se subirá al servidor apenas haya conexión. $quien"
        else "Guardado solo en este celular (la app no está conectada al servidor). ${if (quien.startsWith("Solo")) quien else ""}".trim()
    }
}
