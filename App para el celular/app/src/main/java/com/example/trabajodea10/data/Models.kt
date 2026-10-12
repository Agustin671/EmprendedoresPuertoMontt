package com.example.trabajodea10.data

import kotlinx.serialization.Serializable

enum class Rol(val etiqueta: String) {
    ADMIN("Administradora"),
    EMPRENDEDORA("Emprendedora"),
    VISITANTE("Visitante")
}

enum class EstadoFeria(val etiqueta: String) {
    EN_CURSO("En curso"),
    PROXIMA("Próxima"),
    BORRADOR("Borrador"),
    FINALIZADA("Finalizada")
}

enum class EstadoAsistencia(val etiqueta: String) {
    PRESENTE("Presente"),
    PENDIENTE("Pendiente"),
    RECHAZADA("Rechazada")
}

enum class EstadoCuenta(val etiqueta: String) {
    ACTIVA("Activa"),
    SUSPENDIDA("Suspendida"),
    INVITADA("Invitada")
}

enum class EstadoInvitacion(val etiqueta: String) {
    CONFIRMADA("Confirmada"),
    PENDIENTE("Pendiente"),
    RECHAZADA("Rechazada")
}

enum class MedioPago(val etiqueta: String) {
    EFECTIVO("Efectivo"),
    TRANSFERENCIA("Transferencia"),
    DEBITO("Débito"),
    QR("QR")
}

enum class Categoria(val etiqueta: String) {
    TEXTIL("Textil"),
    CUERO("Cuero"),
    JOYERIA("Joyería"),
    COSMETICA("Cosmética"),
    GOURMET("Gourmet"),
    DECORACION("Decoración")
}

@Serializable
data class Participacion(
    val emprendedoraId: String,
    val asistencia: EstadoAsistencia = EstadoAsistencia.PENDIENTE,
    val invitacion: EstadoInvitacion = EstadoInvitacion.CONFIRMADA
)

@Serializable
data class Feria(
    val id: String,
    val nombre: String,
    val ubicacion: String,
    val fechaInicio: String,
    val fechaFin: String,
    val horario: String = "9 a 18 hs",
    val cupos: Int,
    val precioEntrada: Double,
    val estado: EstadoFeria,
    val participaciones: List<Participacion> = emptyList(),
    val organizadora: String = "Red Trama"
)

@Serializable
data class Asociacion(
    val id: String,
    val nombre: String,
    val localidad: String,
    val color: Long
)

@Serializable
data class Emprendedora(
    val id: String,
    val nombre: String,
    val email: String,
    val telefono: String,
    val asociacionId: String?,
    val estado: EstadoCuenta,
    val color: Long,
    val seguidores: Int = 0
)

@Serializable
data class Variante(
    val id: String,
    val nombre: String,
    val stock: Int
)

@Serializable
data class Producto(
    val id: String,
    val nombre: String,
    val categoria: Categoria,
    val precio: Double,
    val publicado: Boolean,
    val destacado: Boolean,
    val colorFoto: Long,
    val variantes: List<Variante>
) {
    val stockTotal: Int get() = variantes.sumOf { it.stock }
    val agotado: Boolean get() = stockTotal == 0
    val stockBajo: Boolean get() = stockTotal in 1..3
}

@Serializable
data class ItemVenta(
    val productoId: String,
    val varianteId: String,
    val nombre: String,
    val detalle: String,
    val precio: Double,
    val cantidad: Int
)

@Serializable
data class Venta(
    val id: String,
    val fecha: String,
    val items: List<ItemVenta>,
    val medioPago: MedioPago,
    val feriaId: String?,
    val total: Double
)

@Serializable
data class Tienda(
    val nombre: String,
    val bio: String,
    val whatsapp: String,
    val instagram: String,
    val colorPortada: Long,
    val colorLogo: Long
)

@Serializable
data class ConfigSistema(
    val moneda: String = "CLP – Peso chileno",
    val zonaHoraria: String = "America/Santiago",
    val registroPublico: Boolean = true,
    val catalogoPublico: Boolean = true,
    val chatDirecto: Boolean = false,
    val feriasPublicas: Boolean = true
)

@Serializable
data class PreferenciasEmprendedora(
    val alertasStock: Boolean = true,
    val recordatoriosFeria: Boolean = true,
    val resumenVentas: Boolean = false
)

/**
 * Fotografía completa del estado de la app que se guarda en disco para que
 * todo siga igual cuando se cierra y se vuelve a abrir la aplicación.
 */
@Serializable
data class EstadoApp(
    val rol: Rol? = null,
    val emprendedoraActualId: String = "emp1",
    val asociaciones: List<Asociacion> = emptyList(),
    val emprendedoras: List<Emprendedora> = emptyList(),
    val ferias: List<Feria> = emptyList(),
    val productos: List<Producto> = emptyList(),
    val ventas: List<Venta> = emptyList(),
    val tienda: Tienda = MockData.tienda,
    val config: ConfigSistema = ConfigSistema(),
    val prefs: PreferenciasEmprendedora = PreferenciasEmprendedora(),
    val inscripcionesVisitante: List<String> = emptyList(),
    val escalaTexto: Float = 1f
)
