package com.example.trabajodea10.data

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.trabajodea10.data.remoto.ClienteSupabase
import com.example.trabajodea10.data.remoto.ConfigSupabase
import com.example.trabajodea10.data.remoto.ErrorSupabase
import com.example.trabajodea10.data.remoto.PerfilRepositorio
import com.example.trabajodea10.data.remoto.ProductoRepositorio
import com.example.trabajodea10.data.remoto.Sincronizador
import com.example.trabajodea10.data.remoto.TiendaRepositorio
import com.example.trabajodea10.data.remoto.VentaRepositorio
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Sesión y datos de la nube (Supabase). Reemplaza al antiguo FirebaseManager.
 * - Ingreso / registro con correo y contraseña. El ROL lo decide el servidor
 *   (tabla cuentas_autorizadas), no la app: registro no libre (RN-01).
 * - Huella: después de entrar con contraseña se puede activar; la próxima vez
 *   se entra solo con la huella usando la sesión guardada (RF-ACC, RNF-SEG-02).
 * - Trae la tienda, los productos y el historial de la emprendedora al entrar.
 */
object SesionManager {

    private const val TAG = "SesionManager"
    private const val DATO_NOMBRE = "nombre"
    private const val DATO_ROL = "rol"
    private const val DATO_HUELLA = "huella_activada"
    private const val DATO_FOTO = "foto_perfil"
    private const val DATO_HUELLA_OFRECIDA = "huella_ofrecida"

    data class UsuarioSesion(val id: String, val email: String)

    private val ambito = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    val usuarioActual: UsuarioSesion?
        get() = ClienteSupabase.sesion?.let { UsuarioSesion(it.userId, it.email) }

    val emailActual: String? get() = ClienteSupabase.sesion?.email

    var nombreUsuarioActual by mutableStateOf(ClienteSupabase.leerDato(DATO_NOMBRE) ?: "")
        private set

    /** Foto de perfil guardada (URL pública de Storage), para mostrarla al instante. */
    var fotoPerfilUrl by mutableStateOf(ClienteSupabase.leerDato(DATO_FOTO) ?: "")
        private set

    val nombreUsuarioDemo: String get() = nombreUsuarioActual

    var rolUsuarioActual by mutableStateOf<Rol?>(null)
        private set

    /** Tiendas de la vitrina (servidor, o las de ejemplo si no hay conexión). */
    val tiendas = mutableStateListOf<Tienda>()

    var cargando by mutableStateOf(false)
        private set

    /** Mensaje si la vitrina no se pudo traer del servidor (se muestran datos de ejemplo). */
    var mensajeError by mutableStateOf<String?>(null)
        private set

    val conectadoAlServidor: Boolean get() = ConfigSupabase.configurado

    // Tiendas de ejemplo para pruebas sin conexión (las mismas que siembra 02_datos_prueba.sql)
    val TIENDAS_INICIALES_ALERCE = listOf(
        Tienda(
            id = "t_barberia_alerce",
            nombre = "Barbería & Estilo Alerce",
            rubro = "Servicios y Peluquería",
            sector = "Alerce Norte",
            bio = "Cortes de cabello clásicos y modernos, perfilado de barba y tratamientos capilares. Atención personalizada en local y a domicilio.",
            whatsapp = "+56987654321",
            instagram = "@estilo_alerce",
            colorPortada = 0xFF2D3748,
            colorLogo = 0xFF319795,
            necesidades = NecesidadesTienda(
                tieneStock = false, tieneAgendamiento = true, tieneVentaPorWhatsApp = true,
                tieneProductosAPedido = false, participaEnFerias = false, tieneForoComunidad = true
            )
        ),
        Tienda(
            id = "t_minimarket_rosa",
            nombre = "Minimarket Doña Marta",
            rubro = "Abarrotes y Alimentos",
            sector = "Alerce Histórico",
            bio = "Frutas y verduras frescas, pan amasado caliente todos los días a las 17:00 hrs, abarrotes, cecinas y bebidas.",
            whatsapp = "+56991234567",
            instagram = "@minimarket.donamarta",
            colorPortada = 0xFF805AD5,
            colorLogo = 0xFFDD6B20,
            necesidades = NecesidadesTienda(
                tieneStock = true, tieneAgendamiento = false, tieneVentaPorWhatsApp = true,
                tieneProductosAPedido = false, participaEnFerias = false, tieneForoComunidad = true
            )
        ),
        Tienda(
            id = "t_tejidos_austral",
            nombre = "Tejidos & Lanares Alerce Austral",
            rubro = "Artesanía y Textil",
            sector = "Alerce Sur",
            bio = "Piezas únicas tejidas a palillo y telar con lana 100% natural de oveja chilota, teñida con tintes de cortezas nativas.",
            whatsapp = "+56976543210",
            instagram = "@alerceaustral.tejidos",
            colorPortada = 0xFF5C315B,
            colorLogo = 0xFF2E7D5B,
            necesidades = NecesidadesTienda(
                tieneStock = true, tieneAgendamiento = false, tieneVentaPorWhatsApp = true,
                tieneProductosAPedido = true, participaEnFerias = true, tieneForoComunidad = true
            )
        ),
        Tienda(
            id = "t_reposteria_surena",
            nombre = "Repostería Casera Tradición Sureña",
            rubro = "Pastelería y Gastronomía",
            sector = "Senda Central",
            bio = "Kuchenes tradicionales de murta y frambuesa, tortas de hojarasca a pedido, alfajores caseros y galletas horneadas con amor.",
            whatsapp = "+56965432109",
            instagram = "@tradicion_surena",
            colorPortada = 0xFFD69E2E,
            colorLogo = 0xFFC53030,
            necesidades = NecesidadesTienda(
                tieneStock = true, tieneAgendamiento = false, tieneVentaPorWhatsApp = true,
                tieneProductosAPedido = true, participaEnFerias = true, tieneForoComunidad = true
            )
        )
    )

    init {
        tiendas.addAll(TIENDAS_INICIALES_ALERCE)
        rolUsuarioActual = ClienteSupabase.leerDato(DATO_ROL)?.let { runCatching { Rol.valueOf(it) }.getOrNull() }
    }

    // ============================================================ Acceso

    suspend fun iniciarSesion(correo: String, clave: String): Result<Rol> = runCatching {
        ClienteSupabase.ingresar(correo, clave)
        prepararSesion()
    }.onFailure { Log.w(TAG, "Ingreso fallido: ${it.message}") }

    /**
     * Crea la cuenta. El rol lo asigna el servidor: si el administrador autorizó
     * el correo, queda como emprendedora con su tienda; si no, como comprador.
     */
    suspend fun registrarUsuario(
        correo: String,
        clave: String,
        nombre: String,
        crearTienda: Boolean = false
    ): Result<Rol> = runCatching {
        ClienteSupabase.registrar(correo, clave, nombre, crearTienda)
        prepararSesion()
    }.onFailure { Log.w(TAG, "Registro fallido: ${it.message}") }

    /** Una cuenta compradora existente puede abrir su propia tienda. */
    suspend fun crearTiendaParaUsuario(nombre: String): Result<Rol> = runCatching {
        if (nombre.isBlank()) throw ErrorSupabase(0, "Escribe el nombre de tu tienda.")
        TiendaRepositorio.crearMiTienda(nombre)
        prepararSesion()
    }.onFailure { Log.w(TAG, "No se pudo crear la tienda: ${it.message}") }

    /** Envía el correo con el enlace para crear una contraseña nueva. */
    suspend fun recuperarContrasena(correo: String): Result<Unit> = runCatching {
        if (correo.isBlank()) throw ErrorSupabase(0, "Escribe tu correo electrónico.")
        ClienteSupabase.recuperarContrasena(correo)
    }.onFailure { Log.w(TAG, "No se pudo enviar el correo de recuperación: ${it.message}") }

    /** Hay una sesión guardada y la persona activó la huella. */
    fun huellaDisponible(): Boolean =
        ClienteSupabase.sesion != null && ClienteSupabase.leerDato(DATO_HUELLA) == "si"

    fun huellaActivada(): Boolean = ClienteSupabase.leerDato(DATO_HUELLA) == "si"

    fun activarHuella(activar: Boolean) {
        ClienteSupabase.guardarDato(DATO_HUELLA, if (activar) "si" else null)
        if (activar) ClienteSupabase.guardarDato(DATO_HUELLA_OFRECIDA, "si")
    }

    /** Para no preguntar por la huella en cada ingreso si la persona dijo que no. */
    fun huellaYaOfrecida(): Boolean = ClienteSupabase.leerDato(DATO_HUELLA_OFRECIDA) == "si"

    fun marcarHuellaOfrecida() {
        ClienteSupabase.guardarDato(DATO_HUELLA_OFRECIDA, "si")
    }

    /** Después de validar la huella con el sistema: renueva la sesión guardada y entra. */
    suspend fun ingresarConHuella(): Result<Rol> = runCatching {
        if (ClienteSupabase.sesion == null) {
            throw ErrorSupabase(401, "Entra una vez con tu correo y contraseña para activar la huella.")
        }
        if (!ClienteSupabase.renovarSesion()) {
            // Sin internet: se entra con lo guardado en el celular.
            val rolGuardado = rolUsuarioActual
            if (ClienteSupabase.sesion != null && rolGuardado != null) {
                TramaStore.iniciarSesion(rolGuardado)
                return@runCatching rolGuardado
            }
            activarHuella(false)
            throw ErrorSupabase(401, "Tu sesión venció. Entra con tu correo y contraseña.")
        }
        prepararSesion()
    }

    /** Lee el perfil del servidor y deja la app lista según el rol. */
    private suspend fun prepararSesion(): Rol {
        val perfil = PerfilRepositorio.miPerfil()
        nombreUsuarioActual = perfil.nombre
        rolUsuarioActual = perfil.rol
        fotoPerfilUrl = perfil.fotoUrl.orEmpty()
        ClienteSupabase.guardarDato(DATO_NOMBRE, perfil.nombre)
        ClienteSupabase.guardarDato(DATO_ROL, perfil.rol.name)
        ClienteSupabase.guardarDato(DATO_FOTO, perfil.fotoUrl)

        if (perfil.rol == Rol.EMPRENDEDORA && perfil.tiendaId != null) {
            traerMiTienda(perfil.tiendaId)
        }
        TramaStore.iniciarSesion(perfil.rol)
        Sincronizador.subirEnSegundoPlano()
        return perfil.rol
    }

    /** Trae la tienda, sus productos y su historial de ventas, sin pisar lo que falta subir. */
    private suspend fun traerMiTienda(tiendaId: String) {
        TiendaRepositorio.obtener(tiendaId)?.let { TramaStore.guardarTienda(it) }
        runCatching { ProductoRepositorio.deTienda(tiendaId) }
            .onSuccess { TramaStore.reemplazarProductosDeTienda(tiendaId, it, Sincronizador.idsProductosPendientes()) }
            .onFailure { Log.w(TAG, "No se pudieron traer los productos: ${it.message}") }
        runCatching { VentaRepositorio.historial(tiendaId) }
            .onSuccess { TramaStore.reemplazarVentas(it, Sincronizador.idsVentasPendientes()) }
            .onFailure { Log.w(TAG, "No se pudo traer el historial: ${it.message}") }
    }

    /** Acceso a las tiendas de ejemplo sin servidor (solo en el APK de pruebas). */
    fun entrarModoDesarrolladorPrueba(tiendaId: String, rol: Rol) {
        val tiendaPrueba = TIENDAS_INICIALES_ALERCE.find { it.id == tiendaId } ?: TIENDAS_INICIALES_ALERCE.first()
        nombreUsuarioActual = tiendaPrueba.nombre
        rolUsuarioActual = rol
        if (rol == Rol.EMPRENDEDORA) TramaStore.guardarTienda(tiendaPrueba)
        TramaStore.iniciarSesion(rol)
    }

    /**
     * Cierra la sesión. Si la huella está activada se conserva la sesión guardada
     * para volver a entrar solo con la huella; si no, se cierra también en el servidor.
     */
    suspend fun cerrarSesionCompleta() {
        if (!huellaActivada()) {
            runCatching { Sincronizador.subirPendientes() }
            Sincronizador.descartarTodo()
            ClienteSupabase.cerrarSesion()
            ClienteSupabase.guardarDato(DATO_ROL, null)
            ClienteSupabase.guardarDato(DATO_NOMBRE, null)
            ClienteSupabase.guardarDato(DATO_FOTO, null)
            ClienteSupabase.guardarDato(DATO_HUELLA_OFRECIDA, null)
            nombreUsuarioActual = ""
            fotoPerfilUrl = ""
            rolUsuarioActual = null
        }
        TramaStore.cerrarSesion()
    }

    /** Versión sin espera para botones: la parte del servidor sigue en segundo plano. */
    fun cerrarSesion() {
        TramaStore.cerrarSesion()
        ambito.launch { cerrarSesionCompleta() }
    }

    // ============================================================ Vitrina y tienda

    /** Trae tiendas y productos publicados del servidor. Sin conexión deja los de ejemplo. */
    suspend fun cargarTiendas(): List<Tienda> {
        cargando = true
        try {
            val remotas = TiendaRepositorio.listar()
            val productos = ProductoRepositorio.publicados()
            tiendas.clear()
            tiendas.addAll(remotas)
            TramaStore.fusionarProductosPublicos(productos, excluirTienda = miTiendaSiEmprendedora())
            mensajeError = null
        } catch (e: Exception) {
            Log.w(TAG, "Vitrina sin conexión: ${e.message}")
            mensajeError = e.message ?: "Sin conexión"
            if (tiendas.isEmpty()) tiendas.addAll(TIENDAS_INICIALES_ALERCE)
        } finally {
            cargando = false
        }
        return tiendas
    }

    private fun miTiendaSiEmprendedora(): String? =
        if (TramaStore.rol == Rol.EMPRENDEDORA) TramaStore.tienda.id else null

    /**
     * Guarda la ficha de la tienda: primero en el celular y luego en el servidor.
     * Devuelve true si quedó en el servidor; false si quedó pendiente de subir.
     */
    suspend fun guardarTienda(tienda: Tienda): Boolean {
        val indice = tiendas.indexOfFirst { it.id == tienda.id }
        if (indice >= 0) tiendas[indice] = tienda else tiendas.add(tienda)
        TramaStore.guardarTienda(tienda)
        if (ClienteSupabase.sesion == null) return false
        val ok = runCatching { TiendaRepositorio.actualizar(tienda) }.getOrDefault(false)
        if (!ok) Sincronizador.tiendaPendiente()
        return ok
    }

    /** Cambia la foto de perfil de quien tiene la sesión. */
    suspend fun subirFotoPerfil(contexto: Context, uri: Uri): Result<Unit> = runCatching {
        val url = FotoProductoStore.subirAvatar(contexto, uri)
            ?: throw ErrorSupabase(0, "No se pudo leer la foto elegida.")
        PerfilRepositorio.actualizarMiPerfil(fotoUrl = url)
        fotoPerfilUrl = url
        ClienteSupabase.guardarDato(DATO_FOTO, url)
    }.onFailure { Log.w(TAG, "No se pudo cambiar la foto de perfil: ${it.message}") }

    /** Cambia el logo del negocio de la emprendedora con sesión. */
    suspend fun subirLogo(contexto: Context, uri: Uri): Result<Unit> = runCatching {
        val tienda = TramaStore.tienda
        if (tienda.id.isBlank()) throw ErrorSupabase(0, "Primero inicia sesión como emprendedora.")
        val url = FotoProductoStore.subirLogo(contexto, uri, tienda.id)
            ?: throw ErrorSupabase(0, "No se pudo leer la imagen elegida.")
        guardarTienda(tienda.copy(logoUrl = url))
        Unit
    }.onFailure { Log.w(TAG, "No se pudo cambiar el logo: ${it.message}") }
}
