package com.example.trabajodea10.data

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

object FirebaseManager {

    private const val TAG = "FirebaseManager"
    private const val COLECCION_TIENDAS = "tiendas"

    private val auth: FirebaseAuth?
        get() = runCatching { FirebaseAuth.getInstance() }.getOrNull()

    private val firestore: FirebaseFirestore?
        get() = runCatching { FirebaseFirestore.getInstance() }.getOrNull()

    // Estado reactivo de usuario actual
    var usuarioActual by mutableStateOf<FirebaseUser?>(null)
        private set

    var nombreUsuarioDemo by mutableStateOf("Emprendedora Alerce")
        private set

    // Lista en memoria reactiva de tiendas leídas de Firestore (con fallback)
    val tiendas = mutableStateListOf<Tienda>()

    var cargando by mutableStateOf(false)
        private set

    var mensajeError by mutableStateOf<String?>(null)
        private set

    // 4 Tiendas representativas de Alerce para prueba y fallback offline
    val TIENDAS_INICIALES_ALERCE = listOf(
        Tienda(
            id = "t_barberia_alerce",
            nombre = "Barbería & Estilo Alerce",
            rubro = "Servicios y Peluquería",
            sector = "Alerce Norte",
            bio = "Cortes de cabello clásicos y modernos, perfilado de barba y tratamientos capilares. Atención personalizada en local y a domicilio.",
            whatsapp = "+56987654321",
            instagram = "@estilo_alerce",
            colorPortada = 0xFF2D3748, // Gris azulado moderno
            colorLogo = 0xFF319795,    // Teal
            necesidades = NecesidadesTienda(
                tieneStock = false,
                tieneAgendamiento = true,
                tieneVentaPorWhatsApp = true,
                tieneProductosAPedido = false,
                participaEnFerias = false,
                tieneForoComunidad = true
            )
        ),
        Tienda(
            id = "t_minimarket_rosa",
            nombre = "Minimarket y Provisiones Doña Rosa",
            rubro = "Abarrotes y Alimentos",
            sector = "Alerce Histórico",
            bio = "Frutas y verduras frescas, pan amasado caliente todos los días a las 17:00 hrs, abarrotes, cecinas y bebidas.",
            whatsapp = "+56991234567",
            instagram = "@minimarket.donarosa",
            colorPortada = 0xFF805AD5, // Morado
            colorLogo = 0xFFDD6B20,    // Naranja cálido
            necesidades = NecesidadesTienda(
                tieneStock = true,
                tieneAgendamiento = false,
                tieneVentaPorWhatsApp = true,
                tieneProductosAPedido = false,
                participaEnFerias = false,
                tieneForoComunidad = false
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
            colorPortada = 0xFF5C315B, // Berenjena
            colorLogo = 0xFF2E7D5B,    // Verde bosque
            necesidades = NecesidadesTienda(
                tieneStock = true,
                tieneAgendamiento = false,
                tieneVentaPorWhatsApp = true,
                tieneProductosAPedido = true,
                participaEnFerias = true,
                tieneForoComunidad = true
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
            colorPortada = 0xFFD69E2E, // Dorado miel
            colorLogo = 0xFFC53030,    // Rojo cereza
            necesidades = NecesidadesTienda(
                tieneStock = true,
                tieneAgendamiento = false,
                tieneVentaPorWhatsApp = true,
                tieneProductosAPedido = true,
                participaEnFerias = true,
                tieneForoComunidad = true
            )
        )
    )

    init {
        // Inicializar tiendas en memoria con el set representativo de Alerce
        tiendas.clear()
        tiendas.addAll(TIENDAS_INICIALES_ALERCE)
        try {
            usuarioActual = auth?.currentUser
        } catch (e: Throwable) {
            Log.w(TAG, "No se pudo leer currentUser de Auth: ${e.message}")
        }
    }

    // ---------- Autenticación con Firebase Auth ----------

    suspend fun iniciarSesion(correo: String, clave: String): Result<FirebaseUser> {
        val fAuth = auth ?: return Result.failure(IllegalStateException("Firebase Auth no disponible en este dispositivo"))
        return try {
            val resultado = fAuth.signInWithEmailAndPassword(correo.trim(), clave).await()
            val user = resultado.user
            usuarioActual = user
            nombreUsuarioDemo = user?.displayName ?: correo.substringBefore("@").replaceFirstChar { it.uppercase() }
            Result.success(user!!)
        } catch (e: Throwable) {
            Log.w(TAG, "Error iniciando sesión en Firebase Auth: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun registrarUsuario(correo: String, clave: String, nombre: String): Result<FirebaseUser> {
        val fAuth = auth ?: return Result.failure(IllegalStateException("Firebase Auth no disponible en este dispositivo"))
        return try {
            val resultado = fAuth.createUserWithEmailAndPassword(correo.trim(), clave).await()
            val user = resultado.user
            usuarioActual = user
            nombreUsuarioDemo = nombre.ifBlank { correo.substringBefore("@") }
            Result.success(user!!)
        } catch (e: Throwable) {
            Log.w(TAG, "Error registrando usuario en Firebase Auth: ${e.message}")
            Result.failure(e)
        }
    }

    fun entrarModoDemo(nombre: String, rol: Rol) {
        nombreUsuarioDemo = nombre
        if (rol == Rol.EMPRENDEDORA) {
            val tiendaDemo = TIENDAS_INICIALES_ALERCE.find { it.id == "t_minimarket_rosa" }
            if (tiendaDemo != null) {
                TramaStore.guardarTienda(tiendaDemo)
            }
        }
        TramaStore.iniciarSesion(rol)
    }

    fun cerrarSesion() {
        try {
            auth?.signOut()
        } catch (e: Throwable) {
            Log.w(TAG, "Error al cerrar sesión en Auth: ${e.message}")
        }
        usuarioActual = null
        TramaStore.cerrarSesion()
    }

    // ---------- Cloud Firestore: Colección 'tiendas' ----------

    suspend fun cargarTiendasDesdeFirestore(): List<Tienda> {
        cargando = true
        mensajeError = null
        val fStore = firestore
        if (fStore == null) {
            mensajeError = "Modo Local Alerce (Firebase desconectado)"
            cargando = false
            return tiendas
        }
        return try {
            val snapshot = fStore.collection(COLECCION_TIENDAS).get().await()
            if (snapshot.isEmpty) {
                Log.d(TAG, "Colección 'tiendas' vacía en Firestore. Sembrando 4 tiendas de Alerce...")
                sembrarTiendasIniciales()
                tiendas.clear()
                tiendas.addAll(TIENDAS_INICIALES_ALERCE)
                cargando = false
                TIENDAS_INICIALES_ALERCE
            } else {
                val resultado = snapshot.documents.map { doc ->
                    Tienda.fromMap(doc.id, doc.data ?: emptyMap())
                }
                tiendas.clear()
                tiendas.addAll(resultado)
                cargando = false
                resultado
            }
        } catch (e: Throwable) {
            Log.w(TAG, "No se pudo conectar a Firestore: ${e.message}. Usando datos locales de Alerce.")
            mensajeError = "Modo sin conexión: mostrando tiendas locales de Alerce."
            cargando = false
            if (tiendas.isEmpty()) {
                tiendas.addAll(TIENDAS_INICIALES_ALERCE)
            }
            tiendas
        }
    }

    suspend fun sembrarTiendasIniciales(): Boolean {
        val fStore = firestore ?: return false
        return try {
            val batch = fStore.batch()
            TIENDAS_INICIALES_ALERCE.forEach { t ->
                val ref = fStore.collection(COLECCION_TIENDAS).document(t.id)
                batch.set(ref, t.toMap())
            }
            batch.commit().await()
            Log.d(TAG, "4 tiendas sembradas exitosamente en Firestore")
            true
        } catch (e: Throwable) {
            Log.e(TAG, "Error sembrando tiendas en Firestore: ${e.message}")
            false
        }
    }

    suspend fun guardarTiendaEnFirestore(tienda: Tienda): Boolean {
        // Actualizar localmente de inmediato para reactividad instantánea
        val indice = tiendas.indexOfFirst { it.id == tienda.id }
        if (indice >= 0) {
            tiendas[indice] = tienda
        } else {
            tiendas.add(tienda)
        }
        TramaStore.guardarTienda(tienda)

        val fStore = firestore ?: return false
        return try {
            fStore.collection(COLECCION_TIENDAS)
                .document(tienda.id)
                .set(tienda.toMap())
                .await()
            Log.d(TAG, "Tienda ${tienda.id} guardada en Firestore")
            true
        } catch (e: Throwable) {
            Log.w(TAG, "Error guardando en Firestore (se mantuvo localmente): ${e.message}")
            false
        }
    }
}
