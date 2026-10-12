package com.example.trabajodea10.data

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

data class Participacion(
    val emprendedoraId: String,
    val asistencia: EstadoAsistencia = EstadoAsistencia.PENDIENTE,
    val invitacion: EstadoInvitacion = EstadoInvitacion.CONFIRMADA
)

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
    val organizadora: String = "Red Trama",
    val fotoUrl: String? = null,
    val estiloMapa: String = "Satelital"
)

data class Asociacion(
    val id: String,
    val nombre: String,
    val localidad: String,
    val color: Long
)

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

data class Variante(
    val id: String,
    val nombre: String,
    val stock: Int
)

data class Producto(
    val id: String,
    val tiendaId: String = "emp1",
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

data class ItemVenta(
    val productoId: String,
    val varianteId: String,
    val nombre: String,
    val detalle: String,
    val precio: Double,
    val cantidad: Int
)

data class Venta(
    val id: String,
    val fecha: String,
    val items: List<ItemVenta>,
    val medioPago: MedioPago,
    val feriaId: String?,
    val total: Double
)

data class NecesidadesTienda(
    val tieneStock: Boolean = true,
    val tieneAgendamiento: Boolean = false,
    val tieneVentaPorWhatsApp: Boolean = true,
    val tieneProductosAPedido: Boolean = false,
    val participaEnFerias: Boolean = true,
    val tieneForoComunidad: Boolean = false
) {
    fun toMap(): Map<String, Any> = mapOf(
        "tieneStock" to tieneStock,
        "tieneAgendamiento" to tieneAgendamiento,
        "tieneVentaPorWhatsApp" to tieneVentaPorWhatsApp,
        "tieneProductosAPedido" to tieneProductosAPedido,
        "participaEnFerias" to participaEnFerias,
        "tieneForoComunidad" to tieneForoComunidad
    )

    companion object {
        fun fromMap(map: Map<String, Any?>): NecesidadesTienda {
            return NecesidadesTienda(
                tieneStock = (map["tieneStock"] as? Boolean) ?: true,
                tieneAgendamiento = (map["tieneAgendamiento"] as? Boolean) ?: false,
                tieneVentaPorWhatsApp = (map["tieneVentaPorWhatsApp"] as? Boolean) ?: true,
                tieneProductosAPedido = (map["tieneProductosAPedido"] as? Boolean) ?: false,
                participaEnFerias = (map["participaEnFerias"] as? Boolean) ?: true,
                tieneForoComunidad = (map["tieneForoComunidad"] as? Boolean) ?: false
            )
        }
    }
}

data class Tienda(
    val id: String = "emp1",
    val nombre: String = "Mi Tienda",
    val rubro: String = "Artesanía y Comercio",
    val sector: String = "Alerce",
    val bio: String = "",
    val whatsapp: String = "+56 9 1234 5678",
    val instagram: String = "@tienda.alerce",
    val colorPortada: Long = 0xFF5C315B,
    val colorLogo: Long = 0xFF2E7D5B,
    val necesidades: NecesidadesTienda = NecesidadesTienda()
) {
    fun toMap(): Map<String, Any> = mapOf(
        "id" to id,
        "nombre" to nombre,
        "rubro" to rubro,
        "sector" to sector,
        "bio" to bio,
        "whatsapp" to whatsapp,
        "instagram" to instagram,
        "colorPortada" to colorPortada,
        "colorLogo" to colorLogo,
        "necesidades" to necesidades.toMap()
    )

    companion object {
        @Suppress("UNCHECKED_CAST")
        fun fromMap(id: String, map: Map<String, Any?>): Tienda {
            val necMap = map["necesidades"] as? Map<String, Any?> ?: emptyMap()
            return Tienda(
                id = id,
                nombre = (map["nombre"] as? String) ?: "Comercio Local",
                rubro = (map["rubro"] as? String) ?: "Comercio",
                sector = (map["sector"] as? String) ?: "Alerce",
                bio = (map["bio"] as? String) ?: "",
                whatsapp = (map["whatsapp"] as? String) ?: "+56 9 1234 5678",
                instagram = (map["instagram"] as? String) ?: "",
                colorPortada = (map["colorPortada"] as? Number)?.toLong() ?: 0xFF5C315B,
                colorLogo = (map["colorLogo"] as? Number)?.toLong() ?: 0xFF2E7D5B,
                necesidades = NecesidadesTienda.fromMap(necMap)
            )
        }
    }
}

data class ConfigSistema(
    val moneda: String = "CLP – Peso chileno",
    val zonaHoraria: String = "America/Santiago",
    val registroPublico: Boolean = true,
    val catalogoPublico: Boolean = true,
    val chatDirecto: Boolean = true,
    val feriasPublicas: Boolean = true
)

data class PreferenciasEmprendedora(
    val alertasStock: Boolean = true,
    val recordatoriosFeria: Boolean = true,
    val resumenVentas: Boolean = false
)

data class BloqueHorario(
    val id: String,
    val tiendaId: String,
    val fecha: String,             // Formato YYYY-MM-DD
    val hora: String,              // Formato HH:mm
    val duracion: String = "45 min",
    val reservado: Boolean = false,
    val clienteNombre: String? = null,
    val clienteTelefono: String? = null,
    val servicio: String? = null
)
