package com.example.trabajodea10.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.text.NumberFormat
import java.util.Locale

private val localeAR = Locale("es", "AR")

fun formatearMoneda(valor: Double): String {
    val formato = NumberFormat.getNumberInstance(localeAR)
    formato.maximumFractionDigits = 0
    return "$ " + formato.format(valor)
}

private val MESES = listOf(
    "ene", "feb", "mar", "abr", "may", "jun",
    "jul", "ago", "sep", "oct", "nov", "dic"
)

fun fechaLegible(iso: String, anio: Boolean = false): String {
    val partes = iso.split(" ")[0].split("-")
    if (partes.size < 3) return iso
    val dia = partes[2].trimStart('0').ifEmpty { "0" }
    val mes = partes[1].toIntOrNull()?.let { MESES.getOrElse(it - 1) { "" } } ?: partes[1]
    return if (anio) "$dia $mes ${partes[0]}" else "$dia $mes"
}

fun rangoDeFechas(inicio: String, fin: String): String {
    if (inicio == fin) return fechaLegible(inicio, anio = true)
    return "${fechaLegible(inicio, anio = true)} – ${fechaLegible(fin, anio = true)}"
}

fun isoALegible(iso: String): String {
    val partes = iso.split("-")
    if (partes.size < 3) return iso
    return "${partes[2]}/${partes[1]}/${partes[0]}"
}

fun legibleAIso(texto: String): String? {
    val partes = texto.trim().split("/")
    if (partes.size != 3) return null
    val dia = partes[0].padStart(2, '0')
    val mes = partes[1].padStart(2, '0')
    val anio = partes[2]
    if (dia.length != 2 || mes.length != 2 || anio.length != 4) return null
    if (dia.toIntOrNull() == null || mes.toIntOrNull() == null || anio.toIntOrNull() == null) return null
    return "$anio-$mes-$dia"
}

fun List<Venta>.totalDelMes(): Double {
    val mesActual = "2026-10"
    return filter { it.fecha.startsWith(mesActual) }.sumOf { it.total }
}

object TramaStore {

    var rol by mutableStateOf<Rol?>(null)
        private set

    var emprendedoraActualId = "emp1"

    val asociaciones = mutableStateListOf<Asociacion>().apply { addAll(MockData.asociaciones) }
    val emprendedoras = mutableStateListOf<Emprendedora>().apply { addAll(MockData.emprendedoras) }
    val ferias = mutableStateListOf<Feria>().apply { addAll(MockData.ferias) }
    val productos = mutableStateListOf<Producto>().apply { addAll(MockData.productos) }
    val ventas = mutableStateListOf<Venta>().apply { addAll(MockData.ventas) }

    var tienda by mutableStateOf(MockData.tienda)
        private set

    var config by mutableStateOf(ConfigSistema())
        private set

    var prefs by mutableStateOf(PreferenciasEmprendedora())
        private set

    // Personas que se anotaron como visitantes a una feria (id de la feria).
    val inscripcionesVisitante = mutableStateListOf<String>()

    // Escala de texto del modo accesible (+A / -A). 1f = tamaño normal.
    var escalaTexto by mutableStateOf(1f)
        private set

    val emprendedoraActual: Emprendedora
        get() = emprendedoras.firstOrNull { it.id == emprendedoraActualId } ?: emprendedoras.first()

    // ---------- Sesión ----------

    fun iniciarSesion(nuevoRol: Rol) {
        rol = nuevoRol
    }

    fun cerrarSesion() {
        rol = null
    }

    // ---------- Ferias ----------

    fun feriaPorId(id: String): Feria? = ferias.firstOrNull { it.id == id }

    fun crearFeriaBorrador(): String {
        val id = "f${System.currentTimeMillis()}"
        ferias.add(
            0,
            Feria(
                id = id,
                nombre = "",
                ubicacion = "",
                fechaInicio = "2026-11-01",
                fechaFin = "2026-11-01",
                cupos = 20,
                precioEntrada = 0.0,
                estado = EstadoFeria.BORRADOR
            )
        )
        return id
    }

    fun guardarFeria(feria: Feria) {
        val indice = ferias.indexOfFirst { it.id == feria.id }
        if (indice >= 0) ferias[indice] = feria else ferias.add(feria)
    }

    fun descartarSiEstaVacio(id: String) {
        val feria = ferias.firstOrNull { it.id == id }
        if (feria != null && feria.nombre.isBlank()) ferias.removeAll { it.id == id }
        val producto = productos.firstOrNull { it.id == id }
        if (producto != null && producto.nombre.isBlank()) productos.removeAll { it.id == id }
    }

    fun setAsistencia(feriaId: String, emprendedoraId: String, estado: EstadoAsistencia) {
        val indice = ferias.indexOfFirst { it.id == feriaId }
        if (indice < 0) return
        val feria = ferias[indice]
        val nuevas = feria.participaciones.map {
            if (it.emprendedoraId == emprendedoraId) it.copy(asistencia = estado) else it
        }
        ferias[indice] = feria.copy(participaciones = nuevas)
    }

    fun enviarInvitaciones(feriaId: String, emprendedoraIds: List<String>) {
        val indice = ferias.indexOfFirst { it.id == feriaId }
        if (indice < 0) return
        val feria = ferias[indice]
        val existentes = feria.participaciones.map { it.emprendedoraId }.toSet()
        val nuevas = feria.participaciones +
            emprendedoraIds.filterNot { it in existentes }.map {
                Participacion(it, invitacion = EstadoInvitacion.PENDIENTE)
            }
        ferias[indice] = feria.copy(participaciones = nuevas)
    }

    fun responderInvitacion(feriaId: String, respuesta: EstadoInvitacion) {
        val indice = ferias.indexOfFirst { it.id == feriaId }
        if (indice < 0) return
        val feria = ferias[indice]
        val nuevas = feria.participaciones.map {
            if (it.emprendedoraId == emprendedoraActualId) it.copy(invitacion = respuesta) else it
        }
        ferias[indice] = feria.copy(participaciones = nuevas)
    }

    fun misFerias(): List<Pair<Feria, Participacion>> =
        ferias.mapNotNull { feria ->
            val participacion = feria.participaciones.firstOrNull { it.emprendedoraId == emprendedoraActualId }
            if (participacion != null) feria to participacion else null
        }

    // ---------- Emprendedoras ----------

    fun emprendedoraPorId(id: String): Emprendedora? = emprendedoras.firstOrNull { it.id == id }

    fun altaEmprendedora(
        nombre: String,
        email: String,
        telefono: String,
        asociacionId: String?,
        estado: EstadoCuenta
    ): Boolean {
        if (nombre.isBlank() || email.isBlank()) return false
        val id = "emp${System.currentTimeMillis()}"
        emprendedoras.add(
            Emprendedora(
                id = id,
                nombre = nombre.trim(),
                email = email.trim(),
                telefono = telefono.trim(),
                asociacionId = asociacionId,
                estado = estado,
                color = 0xFF6B7FB3
            )
        )
        return true
    }

    fun cambiarEstadoEmprendedora(id: String, estado: EstadoCuenta) {
        val indice = emprendedoras.indexOfFirst { it.id == id }
        if (indice >= 0) emprendedoras[indice] = emprendedoras[indice].copy(estado = estado)
    }

    fun nombreAsociacion(id: String?): String =
        asociaciones.firstOrNull { it.id == id }?.nombre ?: "Sin asociación"

    // ---------- Productos ----------

    fun productoPorId(id: String): Producto? = productos.firstOrNull { it.id == id }

    fun crearProductoBorrador(): String {
        val id = "p${System.currentTimeMillis()}"
        productos.add(
            0,
            Producto(
                id = id,
                nombre = "",
                categoria = Categoria.TEXTIL,
                precio = 0.0,
                publicado = false,
                destacado = false,
                colorFoto = 0xFF8E6B94,
                variantes = listOf(Variante("${id}v1", "Único", 0))
            )
        )
        return id
    }

    fun guardarProducto(producto: Producto) {
        val indice = productos.indexOfFirst { it.id == producto.id }
        if (indice >= 0) productos[indice] = producto else productos.add(producto)
    }

    fun togglePublicado(id: String) {
        val indice = productos.indexOfFirst { it.id == id }
        if (indice >= 0) {
            val producto = productos[indice]
            productos[indice] = producto.copy(publicado = !producto.publicado)
        }
    }

    fun toggleDestacado(id: String) {
        val indice = productos.indexOfFirst { it.id == id }
        if (indice >= 0) {
            val producto = productos[indice]
            productos[indice] = producto.copy(destacado = !producto.destacado)
        }
    }

    fun eliminarProducto(id: String) {
        productos.removeAll { it.id == id }
    }

    // ---------- Ventas ----------

    fun registrarVenta(
        items: List<ItemVenta>,
        medioPago: MedioPago,
        feriaId: String?
    ): Venta {
        val total = items.sumOf { it.precio * it.cantidad }
        val venta = Venta(
            id = "v${System.currentTimeMillis()}",
            fecha = "2026-10-07 " + horaActual(),
            items = items,
            medioPago = medioPago,
            feriaId = feriaId,
            total = total
        )
        ventas.add(0, venta)
        descontarStock(items)
        return venta
    }

    private fun descontarStock(items: List<ItemVenta>) {
        items.forEach { item ->
            val indice = productos.indexOfFirst { it.id == item.productoId }
            if (indice >= 0) {
                val producto = productos[indice]
                val nuevasVariantes = producto.variantes.map { variante ->
                    if (variante.id == item.varianteId) {
                        variante.copy(stock = (variante.stock - item.cantidad).coerceAtLeast(0))
                    } else variante
                }
                productos[indice] = producto.copy(variantes = nuevasVariantes)
            }
        }
    }

    private fun horaActual(): String {
        val calendario = java.util.Calendar.getInstance()
        val hora = calendario.get(java.util.Calendar.HOUR_OF_DAY)
        val minuto = calendario.get(java.util.Calendar.MINUTE)
        return "%02d:%02d".format(hora, minuto)
    }

    fun feriaDeLaVenta(feriaId: String?): String =
        feriaId?.let { feriaPorId(it) }?.nombre ?: "Venta sin feria"

    // ---------- Tienda y configuración ----------

    fun guardarTienda(nueva: Tienda) {
        tienda = nueva
    }

    fun guardarConfig(nueva: ConfigSistema) {
        config = nueva
    }

    fun guardarPrefs(nuevas: PreferenciasEmprendedora) {
        prefs = nuevas
    }

    // ---------- Métricas ----------

    fun ventasDelMes(): Double = ventas.totalDelMes()

    fun productosPublicados(): Int = productos.count { it.publicado }

    fun stockTotal(): Int = productos.sumOf { it.stockTotal }

    fun seguidores(): Int = emprendedoraActual.seguidores

    fun alertasStock(): List<Producto> = productos.filter { it.agotado || it.stockBajo }

    fun feriasEnCurso(): List<Feria> = ferias.filter { it.estado == EstadoFeria.EN_CURSO }

    fun proximasFerias(): List<Feria> = ferias.filter { it.estado == EstadoFeria.PROXIMA }

    fun borradores(): List<Feria> = ferias.filter { it.estado == EstadoFeria.BORRADOR }

    fun invitacionesPendientes(): Int =
        ferias.sumOf { feria -> feria.participaciones.count { it.invitacion == EstadoInvitacion.PENDIENTE } }

    fun cuentasActivas(): Int = emprendedoras.count { it.estado == EstadoCuenta.ACTIVA }

    fun proximaFeriaDeLaEmprendedora(): Feria? =
        misFerias()
            .filter { it.second?.invitacion == EstadoInvitacion.CONFIRMADA }
            .map { it.first }
            .minByOrNull { it.fechaInicio }

    fun ultimasVentas(cantidad: Int): List<Venta> = ventas.take(cantidad)

    // ---------- Inscripción de visitantes ----------

    fun estaInscrito(feriaId: String): Boolean = inscripcionesVisitante.contains(feriaId)

    fun inscribirse(feriaId: String) {
        if (!estaInscrito(feriaId)) inscripcionesVisitante.add(feriaId)
    }

    fun desinscribirse(feriaId: String) {
        inscripcionesVisitante.removeAll { it == feriaId }
    }

    // ---------- Accesibilidad: tamaño de texto ----------

    fun aumentarTexto() {
        escalaTexto = (escalaTexto + 0.15f).coerceAtMost(1.75f)
    }

    fun reducirTexto() {
        escalaTexto = (escalaTexto - 0.15f).coerceAtLeast(0.85f)
    }

    fun restablecerTexto() {
        escalaTexto = 1f
    }
}
