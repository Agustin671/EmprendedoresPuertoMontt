package com.example.trabajodea10.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.text.NumberFormat
import java.util.Locale

private val localeCL = Locale.forLanguageTag("es-CL")

fun formatearMoneda(valor: Double): String {
    val formato = NumberFormat.getNumberInstance(localeCL)
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

fun fechaIsoEnDias(diasDesdeHoy: Int): String {
    val cal = java.util.Calendar.getInstance()
    cal.add(java.util.Calendar.DAY_OF_YEAR, diasDesdeHoy)
    val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
    return sdf.format(cal.time)
}

fun nombreDiaLegible(fechaIso: String): String {
    return try {
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        val date = sdf.parse(fechaIso) ?: return fechaIso
        val salida = java.text.SimpleDateFormat("EEE d MMM", java.util.Locale("es", "CL"))
        salida.format(date).replaceFirstChar { it.uppercase() }
    } catch (_: Throwable) {
        fechaIso
    }
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

    // Bloques horarios de agendamiento para las próximas 2 semanas
    val citasHorarios = mutableStateListOf<BloqueHorario>()

    // Publicaciones del muro y foro de comunidad
    val publicacionesForo = mutableStateListOf<PublicacionForo>()

    init {
        generarHorariosIniciales()
        generarPublicacionesForoIniciales()
    }

    private fun generarHorariosIniciales() {
        citasHorarios.clear()
        val horasBase = listOf("10:00", "11:30", "15:00", "16:30", "18:00")
        val tiendasConAgenda = listOf("t_barberia_alerce", "emp1", "t_minimarket_rosa")
        for (i in 0..13) {
            val fechaIso = fechaIsoEnDias(i)
            horasBase.forEachIndexed { idx, hora ->
                val esReservado = (i == 1 && idx == 1) || (i == 3 && idx == 2)
                tiendasConAgenda.forEach { tId ->
                    citasHorarios.add(
                        BloqueHorario(
                            id = "bloque_${tId}_${fechaIso}_${hora.replace(":", "")}",
                            tiendaId = tId,
                            fecha = fechaIso,
                            hora = hora,
                            duracion = "45 min",
                            reservado = esReservado,
                            clienteNombre = if (esReservado) "Carlos Almonacid" else null,
                            clienteTelefono = if (esReservado) "+56988887777" else null,
                            servicio = if (esReservado) "Atención Personalizada" else "Atención General"
                        )
                    )
                }
            }
        }
    }

    private fun generarPublicacionesForoIniciales() {
        publicacionesForo.clear()
        publicacionesForo.addAll(
            listOf(
                PublicacionForo(
                    id = "pub_rosa_1",
                    tiendaId = "t_minimarket_rosa",
                    autorNombre = "Minimarket Doña Rosa",
                    esEmprendedor = true,
                    texto = "🥖 ¡Aviso a los vecinos de Alerce! Acaba de salir la primera tanda de pan amasado bien calentito y recibimos huevos frescos de campo. ¡Los esperamos!",
                    fecha = "Hoy, 16:30",
                    respuestas = listOf(
                        RespuestaForo(
                            id = "resp_rosa_1_1",
                            autorNombre = "Paul M.",
                            esEmprendedor = false,
                            texto = "¿Doña Rosa, le queda queso de campo artesanal para acompañar el pancito?",
                            fecha = "Hoy, 16:45"
                        ),
                        RespuestaForo(
                            id = "resp_rosa_1_2",
                            autorNombre = "Minimarket Doña Rosa",
                            esEmprendedor = true,
                            texto = "¡Hola Paul! Sí vecino, nos llegaron 3 piezas recién traídas de Los Muermos. Te guardo una si gustas.",
                            fecha = "Hoy, 16:50"
                        )
                    )
                ),
                PublicacionForo(
                    id = "pub_rosa_2",
                    tiendaId = "t_minimarket_rosa",
                    autorNombre = "Marta Gómez",
                    esEmprendedor = false,
                    texto = "¿Vecina Doña Rosa, hasta qué hora atiende hoy domingo? Necesito pasar por abarrotes y bebidas para la once familiar.",
                    fecha = "Ayer, 18:20",
                    respuestas = listOf(
                        RespuestaForo(
                            id = "resp_rosa_2_1",
                            autorNombre = "Minimarket Doña Rosa",
                            esEmprendedor = true,
                            texto = "¡Hola Marta! Atendemos de corrido hasta las 21:30 hrs todos los días.",
                            fecha = "Ayer, 18:35"
                        )
                    )
                ),
                PublicacionForo(
                    id = "pub_barberia_1",
                    tiendaId = "t_barberia_alerce",
                    autorNombre = "Barbería & Estilo Alerce",
                    esEmprendedor = true,
                    texto = "💈 Vecinos, esta semana abrimos turnos especiales para corte degradado y perfilado de barba. Recuerden reservar su hora en la app.",
                    fecha = "Hoy, 11:00",
                    respuestas = emptyList()
                )
            )
        )
    }

    // Escala de texto del modo accesible (+A / -A). 0.90f = tamaño cómodo compacto editorial.
    var escalaTexto by mutableStateOf(0.90f)
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

    fun productosDeTienda(tiendaId: String): List<Producto> {
        val prods = productos.filter { it.tiendaId == tiendaId && it.publicado }
        if (prods.isNotEmpty()) return prods
        return productos.filter { it.publicado }.take(4)
    }

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

    // ---------- Agendamiento de horas (Próximas 2 semanas) ----------

    fun horariosDeTienda(tiendaId: String, fecha: String): List<BloqueHorario> {
        return citasHorarios.filter { it.tiendaId == tiendaId && it.fecha == fecha }
    }

    fun proximos14Dias(): List<String> {
        return (0..13).map { fechaIsoEnDias(it) }
    }

    fun reservarHora(bloqueId: String, clienteNombre: String, clienteTelefono: String, servicio: String): Boolean {
        val index = citasHorarios.indexOfFirst { it.id == bloqueId }
        if (index != -1 && !citasHorarios[index].reservado) {
            val actual = citasHorarios[index]
            citasHorarios[index] = actual.copy(
                reservado = true,
                clienteNombre = clienteNombre.trim().ifBlank { "Cliente Alerce" },
                clienteTelefono = clienteTelefono.trim().ifBlank { "+56 9 0000 0000" },
                servicio = servicio.trim().ifBlank { "Atención General" }
            )
            return true
        }
        return false
    }

    fun agregarHorario(
        tiendaId: String,
        fecha: String,
        hora: String,
        duracion: String = "45 min",
        servicio: String = "Atención General"
    ): BloqueHorario {
        val nuevo = BloqueHorario(
            id = "bloque_${fecha}_${hora.replace(":", "")}_${System.currentTimeMillis() % 1000}",
            tiendaId = tiendaId,
            fecha = fecha,
            hora = hora,
            duracion = duracion,
            reservado = false,
            servicio = servicio
        )
        citasHorarios.add(nuevo)
        return nuevo
    }

    fun cancelarReserva(bloqueId: String) {
        val index = citasHorarios.indexOfFirst { it.id == bloqueId }
        if (index != -1) {
            citasHorarios[index] = citasHorarios[index].copy(
                reservado = false,
                clienteNombre = null,
                clienteTelefono = null
            )
        }
    }

    fun eliminarHorario(bloqueId: String) {
        citasHorarios.removeAll { it.id == bloqueId }
    }

    // ---------- Muro y Foro Comunitario ----------

    var versionForo by mutableStateOf(0)
        private set

    fun publicacionesDeTienda(tiendaId: String): List<PublicacionForo> {
        @Suppress("UNUSED_VARIABLE")
        val v = versionForo
        return publicacionesForo.filter { it.tiendaId == tiendaId }
    }

    fun agregarPublicacionForo(
        tiendaId: String,
        autorNombre: String,
        esEmprendedor: Boolean,
        texto: String
    ): PublicacionForo {
        val nueva = PublicacionForo(
            id = "pub_${tiendaId}_${System.currentTimeMillis()}",
            tiendaId = tiendaId,
            autorNombre = autorNombre,
            esEmprendedor = esEmprendedor,
            texto = texto,
            fecha = "Recién",
            respuestas = emptyList()
        )
        publicacionesForo.add(0, nueva)
        versionForo++
        return nueva
    }

    fun responderPublicacionForo(
        publicacionId: String,
        autorNombre: String,
        esEmprendedor: Boolean,
        texto: String
    ) {
        val index = publicacionesForo.indexOfFirst { it.id == publicacionId }
        if (index != -1) {
            val actual = publicacionesForo[index]
            val nuevaResp = RespuestaForo(
                id = "resp_${System.currentTimeMillis()}",
                autorNombre = autorNombre,
                esEmprendedor = esEmprendedor,
                texto = texto,
                fecha = "Recién"
            )
            publicacionesForo[index] = actual.copy(
                respuestas = actual.respuestas + nuevaResp
            )
            versionForo++
        }
    }

    // ---------- Accesibilidad: tamaño de texto ----------

    fun aumentarTexto() {
        escalaTexto = (escalaTexto + 0.10f).coerceAtMost(1.75f)
    }

    fun reducirTexto() {
        escalaTexto = (escalaTexto - 0.10f).coerceAtLeast(0.80f)
    }

    fun restablecerTexto() {
        escalaTexto = 0.90f
    }
}
