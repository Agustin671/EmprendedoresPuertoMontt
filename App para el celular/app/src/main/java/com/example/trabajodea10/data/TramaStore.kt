package com.example.trabajodea10.data

import android.content.Context
import com.example.trabajodea10.data.remoto.ClienteSupabase
import com.example.trabajodea10.data.remoto.Sincronizador
import com.example.trabajodea10.data.remoto.StockServidor
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.text.NumberFormat
import java.util.Calendar
import java.util.Locale

private val localeCL = Locale.forLanguageTag("es-CL")

fun formatearMoneda(valor: Double): String {
    val formato = NumberFormat.getNumberInstance(localeCL)
    formato.maximumFractionDigits = 0
    return "$ " + formato.format(valor)
}

/** Devuelve el mes en curso como "2026-10", para filtrar ventas por mes sin hardcodear. */
fun mesActualIso(): String {
    val calendario = Calendar.getInstance()
    return "%04d-%02d".format(
        calendario.get(Calendar.YEAR),
        calendario.get(Calendar.MONTH) + 1
    )
}

/** Fecha de hoy como "2026-10-12". */
fun fechaActualIso(): String {
    val calendario = Calendar.getInstance()
    return "%04d-%02d-%02d".format(
        calendario.get(Calendar.YEAR),
        calendario.get(Calendar.MONTH) + 1,
        calendario.get(Calendar.DAY_OF_MONTH)
    )
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
    val mes = mesActualIso()
    return filter { it.fecha.startsWith(mes) }.sumOf { it.total }
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

    // Carrito de venta (persiste entre navegación)
    val carritoVenta = mutableStateListOf<ItemVenta>()

    fun agregarAlCarrito(item: ItemVenta) {
        val existente = carritoVenta.firstOrNull {
            it.productoId == item.productoId && it.varianteId == item.varianteId && it.encargo == item.encargo
        }
        if (existente != null) {
            val indice = carritoVenta.indexOf(existente)
            if (indice >= 0) {
                carritoVenta[indice] = existente.copy(cantidad = existente.cantidad + item.cantidad)
            }
        } else {
            carritoVenta.add(item)
        }
    }

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
                            autorNombre = "Carlos M.",
                            esEmprendedor = false,
                            texto = "¿Doña Marta, le queda queso de campo artesanal para acompañar el pancito?",
                            fecha = "Hoy, 16:45"
                        ),
                        RespuestaForo(
                            id = "resp_rosa_1_2",
                            autorNombre = "Minimarket Doña Marta",
                            esEmprendedor = true,
                            texto = "¡Hola vecino! Sí, nos llegaron 3 piezas recién traídas de Los Muermos. Le guardo una si gusta.",
                            fecha = "Hoy, 16:50"
                        )
                    )
                ),
                PublicacionForo(
                    id = "pub_rosa_2",
                    tiendaId = "t_minimarket_rosa",
                    autorNombre = "Vecina Alerce",
                    esEmprendedor = false,
                    texto = "¿Vecina Doña Marta, hasta qué hora atiende hoy domingo? Necesito pasar por abarrotes y bebidas para la once familiar.",
                    fecha = "Ayer, 18:20",
                    respuestas = listOf(
                        RespuestaForo(
                            id = "resp_rosa_2_1",
                            autorNombre = "Minimarket Doña Marta",
                            esEmprendedor = true,
                            texto = "¡Hola vecina! Atendemos de corrido hasta las 21:30 hrs todos los días.",
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

    // Escala de texto del modo accesible (+A / -A). 1f = el mismo tamaño que eligió la persona en su celular.
    var escalaTexto by mutableStateOf(1f)
        private set

    val emprendedoraActual: Emprendedora
        get() = emprendedoras.firstOrNull { it.id == emprendedoraActualId } ?: emprendedoras.first()

    // ---------- Persistencia ----------

    private var persistenciaLista = false

    /** Cuenta cuyos datos están cargados (uid de Supabase) o null sin cuenta. */
    var cuentaActual: String? = null
        private set

    // Tipos de producto y ferias de la plataforma (copias del servidor para usar sin señal).
    val tiposProducto = mutableStateListOf<TipoProducto>()
    val feriasPlataforma = mutableStateListOf<FeriaPlataforma>()

    /** Producto que se está creando o editando (se guarda a cada cambio, RNF-USA-04). */
    var borrador by mutableStateOf<BorradorProducto?>(null)
        private set

    /** Avisos de sincronización que la emprendedora debe ver (conflictos resueltos por el servidor). */
    val avisosSync = mutableStateListOf<String>()

    /** Inicializa el almacenamiento local y recupera lo guardado. Se llama desde el Application. */
    fun inicializar(context: Context) {
        if (persistenciaLista) return
        PersistenciaStore.inicializar(context)
        persistenciaLista = true
        cuentaActual = ClienteSupabase.sesion?.userId
        cargarDesdeDisco()
    }

    /**
     * Cambia a los datos de otra cuenta (o a los de invitado). Lo de la cuenta
     * anterior queda guardado en el celular y no se mezcla con la nueva.
     */
    fun cambiarCuenta(uid: String?) {
        if (persistenciaLista && uid == cuentaActual) { Sincronizador.activarCuenta(uid); return }
        persistir()
        cuentaActual = uid
        restablecerEnMemoria()
        cargarDesdeDisco()
        Sincronizador.activarCuenta(uid)
    }

    private fun restablecerEnMemoria() {
        rol = null
        emprendedoraActualId = "emp1"
        asociaciones.clear(); asociaciones.addAll(MockData.asociaciones)
        emprendedoras.clear(); emprendedoras.addAll(MockData.emprendedoras)
        ferias.clear(); ferias.addAll(MockData.ferias)
        productos.clear(); productos.addAll(MockData.productos)
        ventas.clear(); ventas.addAll(MockData.ventas)
        tienda = MockData.tienda
        config = ConfigSistema()
        prefs = PreferenciasEmprendedora()
        inscripcionesVisitante.clear()
        tiposProducto.clear()
        feriasPlataforma.clear()
        borrador = null
        avisosSync.clear()
        carritoVenta.clear()
    }

    private fun cargarDesdeDisco() {
        val estado = PersistenciaStore.cargar(cuentaActual) ?: return
        rol = estado.rol
        emprendedoraActualId = estado.emprendedoraActualId
        if (estado.asociaciones.isNotEmpty()) {
            asociaciones.clear()
            asociaciones.addAll(estado.asociaciones)
        }
        if (estado.emprendedoras.isNotEmpty()) {
            emprendedoras.clear()
            emprendedoras.addAll(estado.emprendedoras)
        }
        if (estado.ferias.isNotEmpty()) {
            ferias.clear()
            ferias.addAll(estado.ferias)
        }
        if (estado.productos.isNotEmpty()) {
            productos.clear()
            productos.addAll(estado.productos)
        }
        ventas.clear()
        ventas.addAll(estado.ventas)
        tienda = estado.tienda
        config = estado.config
        prefs = estado.prefs
        inscripcionesVisitante.clear()
        inscripcionesVisitante.addAll(estado.inscripcionesVisitante)
        // Nunca por debajo del tamaño de letra que eligió la persona en su celular.
        escalaTexto = estado.escalaTexto.coerceAtLeast(1f)
        tiposProducto.clear(); tiposProducto.addAll(estado.tiposProducto)
        feriasPlataforma.clear(); feriasPlataforma.addAll(estado.feriasPlataforma)
        borrador = estado.borrador
    }

    /** Fotografía completa del estado (también la usan las pruebas). */
    fun estadoActual(): EstadoApp = EstadoApp(
        rol = rol,
        emprendedoraActualId = emprendedoraActualId,
        asociaciones = asociaciones.toList(),
        emprendedoras = emprendedoras.toList(),
        ferias = ferias.toList(),
        productos = productos.toList(),
        ventas = ventas.toList(),
        tienda = tienda,
        config = config,
        prefs = prefs,
        inscripcionesVisitante = inscripcionesVisitante.toList(),
        escalaTexto = escalaTexto,
        tiposProducto = tiposProducto.toList(),
        feriasPlataforma = feriasPlataforma.toList(),
        borrador = borrador
    )

    private fun persistir() {
        if (!persistenciaLista) return
        PersistenciaStore.guardar(
            cuentaActual,
            EstadoApp(
                rol = rol,
                emprendedoraActualId = emprendedoraActualId,
                asociaciones = asociaciones.toList(),
                emprendedoras = emprendedoras.toList(),
                ferias = ferias.toList(),
                productos = productos.toList(),
                ventas = ventas.toList(),
                tienda = tienda,
                config = config,
                prefs = prefs,
                inscripcionesVisitante = inscripcionesVisitante.toList(),
                escalaTexto = escalaTexto,
                tiposProducto = tiposProducto.toList(),
                feriasPlataforma = feriasPlataforma.toList(),
                borrador = borrador
            )
        )
    }

    // ---------- Sesión ----------

    fun iniciarSesion(nuevoRol: Rol) {
        rol = nuevoRol
        persistir()
    }

    fun cerrarSesion() {
        rol = null
        persistir()
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
        persistir()
        return id
    }

    fun guardarFeria(feria: Feria) {
        val indice = ferias.indexOfFirst { it.id == feria.id }
        if (indice >= 0) ferias[indice] = feria else ferias.add(feria)
        persistir()
    }

    fun descartarSiEstaVacio(id: String) {
        val feria = ferias.firstOrNull { it.id == id }
        if (feria != null && feria.nombre.isBlank()) ferias.removeAll { it.id == id }
        // Un producto nuevo solo se descarta si no se ingresó nada (ni siquiera una foto).
        val b = borrador
        if (b != null && b.producto.id == id && b.esNuevo && borradorVacio(b.producto)) borrador = null
        persistir()
    }

    fun borradorVacio(p: Producto): Boolean =
        p.nombre.isBlank() && p.fotoUrl == null && p.descripcion.isBlank() && p.precio <= 0.0 &&
            p.atributos.isEmpty() && p.variantes.size <= 1 && p.variantes.all { it.fotoUrl == null && it.precio == null }

    fun setAsistencia(feriaId: String, emprendedoraId: String, estado: EstadoAsistencia) {
        val indice = ferias.indexOfFirst { it.id == feriaId }
        if (indice < 0) return
        val feria = ferias[indice]
        val nuevas = feria.participaciones.map {
            if (it.emprendedoraId == emprendedoraId) it.copy(asistencia = estado) else it
        }
        ferias[indice] = feria.copy(participaciones = nuevas)
        persistir()
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
        persistir()
    }

    fun responderInvitacion(feriaId: String, respuesta: EstadoInvitacion) {
        val indice = ferias.indexOfFirst { it.id == feriaId }
        if (indice < 0) return
        val feria = ferias[indice]
        val nuevas = feria.participaciones.map {
            if (it.emprendedoraId == emprendedoraActualId) it.copy(invitacion = respuesta) else it
        }
        ferias[indice] = feria.copy(participaciones = nuevas)
        persistir()
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
        persistir()
        // RN-01: deja el correo autorizado en el servidor; al registrarse queda como emprendedora.
        Sincronizador.autorizarEmprendedora(email.trim(), nombre.trim())
        return true
    }

    fun cambiarEstadoEmprendedora(id: String, estado: EstadoCuenta) {
        val indice = emprendedoras.indexOfFirst { it.id == id }
        if (indice >= 0) emprendedoras[indice] = emprendedoras[indice].copy(estado = estado)
        persistir()
    }

    fun nombreAsociacion(id: String?): String =
        asociaciones.firstOrNull { it.id == id }?.nombre ?: "Sin asociación"

    // ---------- Productos ----------

    fun productoPorId(id: String): Producto? = productos.firstOrNull { it.id == id }

    /** Productos que pertenecen a la tienda con la sesión iniciada (no los de otras tiendas). */
    fun productosDeMiTienda(): List<Producto> = productos.filter { it.tiendaId == tienda.id }

    /** Productos visibles de una tienda. Ya no rellena con productos de otras tiendas. */
    fun productosDeTienda(tiendaId: String): List<Producto> =
        productos.filter { it.tiendaId == tiendaId && ReglasCatalogo.estadoPublico(it) != EstadoPublico.OCULTO }

    /**
     * Empieza un producto nuevo con los valores de la tienda (RF-PRO-11). Si
     * quedó uno sin terminar, se retoma ese (RNF-USA-04).
     */
    fun crearProductoBorrador(): String {
        borrador?.takeIf { it.esNuevo }?.let { return it.producto.id }
        val v = tienda.valoresProducto
        val id = "p_" + java.util.UUID.randomUUID().toString()
        val nuevo = ReglasCatalogo.normalizar(
            Producto(
                id = id,
                tiendaId = tienda.id,
                nombre = "",
                categoria = v.categoria,
                precio = 0.0,
                publicado = v.publicado,
                destacado = false,
                colorFoto = 0xFF8E6B94,
                variantes = listOf(Variante("${id}_v1", "Única", if (v.manejaStock && v.modalidad != Modalidad.A_PEDIDO) 1 else 0)),
                modalidad = v.modalidad,
                manejaStock = v.manejaStock,
                alAgotarse = v.alAgotarse,
                disponible = v.disponible
            )
        )
        borrador = BorradorProducto(nuevo, esNuevo = true, paso = 0)
        persistir()
        return id
    }

    /** Abre un producto existente para editarlo (o retoma su edición sin terminar). */
    fun abrirEdicion(id: String): BorradorProducto? {
        borrador?.takeIf { it.producto.id == id }?.let { return it }
        val p = productoPorId(id) ?: return null
        borrador = BorradorProducto(p, esNuevo = false, paso = 0)
        persistir()
        return borrador
    }

    /** Guarda en el celular lo ingresado hasta ahora (autoguardado). */
    fun actualizarBorrador(producto: Producto, paso: Int) {
        val b = borrador ?: return
        if (b.producto.id != producto.id) return
        borrador = b.copy(producto = producto, paso = paso)
        persistir()
    }

    /** Termina el alta o la edición: queda en el catálogo y se sube al servidor. */
    fun finalizarBorrador(): Producto? {
        val b = borrador ?: return null
        val p = ReglasCatalogo.normalizar(b.producto.copy(nombre = b.producto.nombre.trim(), tiendaId = tienda.id))
        borrador = null
        guardarProducto(p)
        return p
    }

    /** Descarta el borrador y borra sus fotos del celular que nadie más usa. */
    fun descartarBorrador() {
        val b = borrador ?: return
        val usadas = productos.flatMap { listOfNotNull(it.fotoUrl) + it.variantes.mapNotNull { v -> v.fotoUrl } }.toSet()
        (listOfNotNull(b.producto.fotoUrl) + b.producto.variantes.mapNotNull { it.fotoUrl })
            .filter { it !in usadas }.forEach { FotoProductoStore.eliminar(it) }
        borrador = null
        persistir()
    }

    /** Disponible / no disponible para varios productos a la vez (RF-PRO-10). */
    fun cambiarDisponibilidad(ids: Collection<String>, disponible: Boolean): Int {
        var cambiados = 0
        ids.forEach { id ->
            val i = productos.indexOfFirst { it.id == id && it.tiendaId == tienda.id }
            if (i >= 0 && productos[i].disponible != disponible) {
                productos[i] = productos[i].copy(disponible = disponible)
                cambiados++
                Sincronizador.productoCambiado(id)
            }
        }
        persistir()
        return cambiados
    }

    /** Guarda los valores iniciales de los productos nuevos y el interruptor del historial. */
    fun guardarConfiguracionVentas(valores: ValoresProducto, historialActivo: Boolean) {
        tienda = tienda.copy(valoresProducto = valores, historialActivo = historialActivo)
        persistir()
        Sincronizador.tiendaPendiente()
        Sincronizador.subirEnSegundoPlano()
    }

    fun reemplazarTipos(lista: List<TipoProducto>) {
        tiposProducto.clear(); tiposProducto.addAll(lista); persistir()
    }

    fun reemplazarFeriasPlataforma(lista: List<FeriaPlataforma>) {
        feriasPlataforma.clear(); feriasPlataforma.addAll(lista); persistir()
    }

    fun tipoPorId(id: String?): TipoProducto? = tiposProducto.firstOrNull { it.id == id }

    fun guardarProducto(producto: Producto) {
        val indice = productos.indexOfFirst { it.id == producto.id }
        if (indice >= 0) productos[indice] = producto else productos.add(producto)
        persistir()
        Sincronizador.productoCambiado(producto.id)
    }

    fun togglePublicado(id: String) {
        val indice = productos.indexOfFirst { it.id == id }
        if (indice >= 0) {
            val producto = productos[indice]
            productos[indice] = producto.copy(publicado = !producto.publicado)
            persistir()
            Sincronizador.productoCambiado(id)
        }
    }

    fun toggleDestacado(id: String) {
        val indice = productos.indexOfFirst { it.id == id }
        if (indice >= 0) {
            val producto = productos[indice]
            productos[indice] = producto.copy(destacado = !producto.destacado)
            persistir()
            Sincronizador.productoCambiado(id)
        }
    }

    /** "Se acabó": deja en cero las unidades de todas las opciones del producto. */
    fun marcarAgotado(id: String) {
        val indice = productos.indexOfFirst { it.id == id }
        if (indice >= 0) {
            val producto = productos[indice]
            productos[indice] = producto.copy(variantes = producto.variantes.map { it.copy(stock = 0) })
            persistir()
            Sincronizador.productoCambiado(id)
        }
    }

    /** Deshace "Se acabó" devolviendo las unidades que tenía cada opción. */
    fun restaurarVariantes(id: String, variantes: List<Variante>) {
        val indice = productos.indexOfFirst { it.id == id }
        if (indice >= 0) {
            productos[indice] = productos[indice].copy(variantes = variantes)
            persistir()
            Sincronizador.productoCambiado(id)
        }
    }

    fun eliminarProducto(id: String) {
        productos.filter { it.id == id }.forEach { FotoProductoStore.eliminar(it.fotoUrl) }
        productos.removeAll { it.id == id }
        persistir()
        Sincronizador.productoEliminado(id)
    }

    // ---------- Datos que llegan del servidor (Supabase) ----------

    /** Reemplaza los productos de una tienda por los del servidor, sin pisar los que faltan subir. */
    fun reemplazarProductosDeTienda(
        tiendaId: String,
        remotos: List<Producto>,
        conservar: Set<String>,
        eliminados: Set<String> = emptySet()
    ) {
        val locales = productos.filter { it.tiendaId == tiendaId && it.id in conservar }
        val idsLocales = locales.map { it.id }.toSet()
        productos.removeAll { it.tiendaId == tiendaId }
        // Un borrado que aún no llega al servidor no debe "revivir" al descargar.
        productos.addAll(remotos.filterNot { it.id in idsLocales || it.id in eliminados })
        productos.addAll(locales)
        persistir()
    }

    /**
     * El servidor confirmó un producto. Si no cambió en el celular mientras se
     * subía, se usa la versión del servidor; si cambió, solo se actualiza la base
     * de cada opción conservando la diferencia que falta subir.
     */
    fun aplicarProductoServidor(guardado: Producto, enviado: Producto) {
        val i = productos.indexOfFirst { it.id == guardado.id }
        if (i < 0) return
        val actual = productos[i]
        productos[i] = if (actual == enviado) {
            guardado
        } else {
            actual.copy(variantes = rebasar(actual.variantes, guardado.variantes))
        }
        persistir()
    }

    /** Ajusta las opciones locales a las cantidades del servidor sin perder lo que falta subir. */
    private fun rebasar(locales: List<Variante>, servidor: List<Variante>): List<Variante> = locales.map { v ->
        val s = servidor.firstOrNull { it.id == v.id } ?: return@map v
        val pendiente = v.stock - (v.stockBase ?: v.stock)
        v.copy(stock = (s.stock + pendiente).coerceAtLeast(0), stockBase = s.stock)
    }

    /** El servidor rechazó una edición (por ejemplo, se vendieron unidades mientras se editaba). */
    fun marcarConflictoProducto(id: String, mensaje: String, actualServidor: Producto?) {
        val i = productos.indexOfFirst { it.id == id }
        if (i >= 0 && actualServidor != null) productos[i] = actualServidor
        val nombre = actualServidor?.nombre ?: productoPorId(id)?.nombre ?: "un producto"
        avisosSync.add("«$nombre»: $mensaje Se dejó la cantidad que tiene el servidor; revísala y vuelve a guardar.")
        persistir()
    }

    fun descartarAviso(texto: String) { avisosSync.remove(texto) }

    /** Agrega o actualiza los productos publicados de la vitrina (excepto los de mi propia tienda). */
    fun fusionarProductosPublicos(remotos: List<Producto>, excluirTienda: String?) {
        val tiendasRemotas = remotos.map { it.tiendaId }.toSet()
        // Quita lo viejo de esas tiendas (lo despublicado ya no viene del servidor)
        productos.removeAll { it.tiendaId in tiendasRemotas && it.tiendaId != excluirTienda && it.publicado }
        remotos.filter { it.tiendaId != excluirTienda }.forEach { remoto ->
            val i = productos.indexOfFirst { it.id == remoto.id }
            if (i >= 0) productos[i] = remoto else productos.add(remoto)
        }
        persistir()
    }

    fun reemplazarVentas(remotas: List<Venta>, conservar: Set<String>) {
        // Se conservan las que faltan subir y las que quedaron en conflicto.
        val pendientes = ventas.filter { it.id in conservar || it.estadoSync != EstadoSync.CONFIRMADA }
        val idsPendientes = pendientes.map { it.id }.toSet()
        ventas.clear()
        ventas.addAll((pendientes + remotas.filterNot { it.id in idsPendientes }).sortedByDescending { it.fecha })
        persistir()
    }

    /** La foto de una opción ya se subió. */
    fun aplicarFotoOpcionRemota(productoId: String, varianteId: String, rutaLocal: String, url: String) {
        val i = productos.indexOfFirst { it.id == productoId }
        if (i < 0) return
        productos[i] = productos[i].copy(variantes = productos[i].variantes.map {
            if (it.id == varianteId && it.fotoUrl == rutaLocal) it.copy(fotoUrl = url) else it
        })
        persistir()
        if (productos.none { p -> p.fotoUrl == rutaLocal || p.variantes.any { it.fotoUrl == rutaLocal } } && borrador?.producto?.variantes?.none { it.fotoUrl == rutaLocal } != false) {
            FotoProductoStore.eliminar(rutaLocal)
        }
    }

    /** La foto ya se subió: se usa la URL pública y se borra la copia del celular. */
    fun aplicarFotoRemota(id: String, rutaLocal: String, url: String) {
        val indice = productos.indexOfFirst { it.id == id }
        if (indice >= 0 && productos[indice].fotoUrl == rutaLocal) {
            productos[indice] = productos[indice].copy(fotoUrl = url)
            persistir()
            FotoProductoStore.eliminar(rutaLocal)
        }
    }

    // ---------- Ventas ----------

    /** Problema que impide registrar la venta (se muestra antes de cobrar). */
    fun validarVenta(items: List<ItemVenta>): String? {
        if (items.isEmpty()) return "Agrega al menos un producto."
        // Se suman las cantidades por opción para no vender dos veces la misma unidad.
        items.groupBy { Triple(it.productoId, it.varianteId, it.encargo) }.forEach { (clave, lineas) ->
            val p = productoPorId(clave.first) ?: return "«${lineas.first().nombre}» ya no está en tu catálogo."
            val v = p.variantes.firstOrNull { it.id == clave.second }
            ReglasCatalogo.validarLinea(p, v, lineas.sumOf { it.cantidad }, clave.third)?.let { return "«${p.nombre}»: $it" }
        }
        val piezas = items.filter { !it.encargo }.groupBy { it.productoId }
            .filter { (id, l) -> productoPorId(id)?.modalidad == Modalidad.PIEZA_UNICA && l.sumOf { it.cantidad } > 1 }
        if (piezas.isNotEmpty()) return "Una pieza única se vende una sola vez."
        return null
    }

    /**
     * Anota la venta en el celular y la deja en la cola. El descuento real lo
     * hace el servidor (registrar_venta); aquí se descuenta también la base
     * para que una edición posterior no vuelva a descontar lo mismo.
     */
    fun registrarVenta(
        items: List<ItemVenta>,
        medioPago: MedioPago,
        feriaId: String?,
        lugarNombre: String? = null
    ): Result<Venta> {
        validarVenta(items)?.let { return Result.failure(IllegalArgumentException(it)) }
        val historial = tienda.historialActivo
        val venta = Venta(
            id = "v_" + java.util.UUID.randomUUID().toString(),
            fecha = fechaActualIso() + " " + horaActual(),
            items = items,
            medioPago = medioPago,
            feriaId = feriaId,
            total = items.sumOf { it.precio * it.cantidad },
            lugarTipo = when { feriaId != null -> "FERIA"; !lugarNombre.isNullOrBlank() -> "OTRO"; else -> null },
            lugarNombre = lugarNombre?.trim()?.takeIf { it.isNotEmpty() && feriaId == null },
            enHistorial = historial,
            estadoSync = if (Sincronizador.ventasConServidor) EstadoSync.PENDIENTE else EstadoSync.CONFIRMADA
        )
        ventas.add(0, venta)
        aplicarDescuentoLocal(items, signo = -1)
        persistir()
        Sincronizador.ventaRegistrada(venta.id)
        return Result.success(venta)
    }

    /** signo = -1 descuenta, +1 devuelve (si el servidor rechazó la venta). */
    private fun aplicarDescuentoLocal(items: List<ItemVenta>, signo: Int) {
        items.filter { !it.encargo }.groupBy { it.productoId }.forEach { (productoId, lineas) ->
            val i = productos.indexOfFirst { it.id == productoId }
            if (i < 0) return@forEach
            val p = productos[i]
            if (p.modalidad == Modalidad.A_PEDIDO) return@forEach
            productos[i] = if (p.modalidad == Modalidad.PIEZA_UNICA) {
                if (signo < 0) p.copy(variantes = p.variantes.map { it.copy(stock = 0, stockBase = it.stockBase?.let { 0 }) }, vendidaEn = "pendiente")
                else p.copy(variantes = p.variantes.map { v -> if (v.id == lineas.first().varianteId) v.copy(stock = 1, stockBase = v.stockBase?.let { 1 }) else v }, vendidaEn = null)
            } else if (!p.manejaStock) p
            else p.copy(variantes = p.variantes.map { v ->
                val cant = lineas.filter { it.varianteId == v.id }.sumOf { it.cantidad }
                if (cant == 0) v else v.copy(stock = (v.stock + signo * cant).coerceAtLeast(0), stockBase = v.stockBase?.let { (it + signo * cant).coerceAtLeast(0) })
            })
        }
    }

    /** El servidor registró la venta: se usan sus cantidades (sin perder ediciones por subir). */
    fun confirmarVenta(id: String, productosServidor: List<StockServidor>) {
        val i = ventas.indexOfFirst { it.id == id }
        if (i >= 0) {
            val v = ventas[i]
            // Con el historial desactivado se queda solo el registro técnico mínimo.
            ventas[i] = if (v.enHistorial) v.copy(estadoSync = EstadoSync.CONFIRMADA, mensajeConflicto = null)
            else v.copy(estadoSync = EstadoSync.CONFIRMADA, mensajeConflicto = null, total = 0.0, lugarTipo = null, lugarNombre = null,
                items = v.items.map { it.copy(precio = 0.0, fotoUrl = null) })
        }
        productosServidor.forEach { s ->
            val j = productos.indexOfFirst { it.id == s.productoId }
            if (j >= 0) productos[j] = productos[j].copy(variantes = rebasar(productos[j].variantes, s.variantes), vendidaEn = s.vendidaEn)
        }
        persistir()
    }

    /** El servidor rechazó la venta: se conserva, se marca y se devuelven las unidades descontadas en el celular. */
    fun marcarConflictoVenta(id: String, mensaje: String) {
        val i = ventas.indexOfFirst { it.id == id }
        if (i < 0) return
        val v = ventas[i]
        if (v.estadoSync == EstadoSync.CONFLICTO) return
        ventas[i] = v.copy(estadoSync = EstadoSync.CONFLICTO, mensajeConflicto = mensaje)
        aplicarDescuentoLocal(v.items, signo = +1)
        persistir()
    }

    /** Vuelve a intentar una venta en conflicto (por ejemplo, después de reponer unidades). */
    fun reintentarVenta(id: String): String? {
        val i = ventas.indexOfFirst { it.id == id }
        if (i < 0) return "La venta ya no existe."
        val v = ventas[i]
        if (v.estadoSync != EstadoSync.CONFLICTO) return null
        validarVenta(v.items)?.let { return it }
        ventas[i] = v.copy(estadoSync = EstadoSync.PENDIENTE, mensajeConflicto = null)
        aplicarDescuentoLocal(v.items, signo = -1)
        persistir()
        Sincronizador.ventaRegistrada(id)
        return null
    }

    /** La emprendedora decide que la venta en conflicto no ocurrió. */
    fun anularVentaEnConflicto(id: String) {
        val v = ventas.firstOrNull { it.id == id } ?: return
        if (v.estadoSync != EstadoSync.CONFLICTO) return
        ventas.removeAll { it.id == id }
        persistir()
    }

    private fun horaActual(): String {
        val calendario = java.util.Calendar.getInstance()
        val hora = calendario.get(java.util.Calendar.HOUR_OF_DAY)
        val minuto = calendario.get(java.util.Calendar.MINUTE)
        return "%02d:%02d".format(hora, minuto)
    }

    fun feriaDeLaVenta(feriaId: String?): String =
        feriaId?.let { id -> feriasPlataforma.firstOrNull { it.id == id }?.nombre ?: feriaPorId(id)?.nombre } ?: "Venta sin feria"

    // ---------- Tienda y configuración ----------

    fun guardarTienda(nueva: Tienda) {
        tienda = nueva
        persistir()
    }

    fun guardarConfig(nueva: ConfigSistema) {
        config = nueva
        persistir()
    }

    fun guardarPrefs(nuevas: PreferenciasEmprendedora) {
        prefs = nuevas
        persistir()
    }

    // ---------- Métricas ----------

    fun ventasDelMes(): Double = ventas.totalDelMes()

    fun productosPublicados(): Int = productosDeMiTienda().count { it.publicado }

    fun stockTotal(): Int = productosDeMiTienda().sumOf { it.stockTotal }

    fun seguidores(): Int = emprendedoraActual.seguidores

    fun alertasStock(): List<Producto> = productosDeMiTienda().filter { it.agotado || it.stockBajo }

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
        if (!estaInscrito(feriaId)) {
            inscripcionesVisitante.add(feriaId)
            persistir()
        }
    }

    fun desinscribirse(feriaId: String) {
        inscripcionesVisitante.removeAll { it == feriaId }
        persistir()
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
        escalaTexto = (escalaTexto + 0.15f).coerceAtMost(1.75f)
        persistir()
    }

    fun reducirTexto() {
        escalaTexto = (escalaTexto - 0.15f).coerceAtLeast(1f)
        persistir()
    }

    fun restablecerTexto() {
        escalaTexto = 1f
        persistir()
    }

    /** Borra todo lo guardado y vuelve a los datos de ejemplo originales. */
    fun restablecerDatos() {
        PersistenciaStore.borrar(cuentaActual)
        rol = null
        emprendedoraActualId = "emp1"
        asociaciones.clear(); asociaciones.addAll(MockData.asociaciones)
        emprendedoras.clear(); emprendedoras.addAll(MockData.emprendedoras)
        ferias.clear(); ferias.addAll(MockData.ferias)
        productos.clear(); productos.addAll(MockData.productos)
        ventas.clear(); ventas.addAll(MockData.ventas)
        tienda = MockData.tienda
        config = ConfigSistema()
        prefs = PreferenciasEmprendedora()
        inscripcionesVisitante.clear()
        escalaTexto = 1f
        persistir()
    }
}
