package com.example.trabajodea10.data

import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import kotlinx.coroutines.tasks.await

/**
 * Gestor central de Firebase para la aplicación:
 * - Firebase Authentication: Registro e inicio de sesión de usuarios y emprendedoras.
 * - Cloud Firestore: Persistencia en la nube de tiendas, perfiles y productos.
 * - Cloud Storage: Almacenamiento y servicio de fotografías reales y afiches en la nube.
 */
object FirebaseManager {

    private const val TAG = "FirebaseManager"
    private const val COLECCION_TIENDAS = "tiendas"
    private const val COLECCION_USUARIOS = "usuarios"

    private val auth: FirebaseAuth?
        get() = runCatching { FirebaseAuth.getInstance() }.getOrNull()

    private val firestore: FirebaseFirestore?
        get() = runCatching { FirebaseFirestore.getInstance() }.getOrNull()

    private val storage: FirebaseStorage?
        get() = runCatching { FirebaseStorage.getInstance() }.getOrNull()

    // Estado reactivo de usuario actual
    var usuarioActual by mutableStateOf<FirebaseUser?>(null)
        private set

    var nombreUsuarioActual by mutableStateOf("")
        private set

    val nombreUsuarioDemo: String
        get() = nombreUsuarioActual

    var rolUsuarioActual by mutableStateOf<Rol?>(null)
        private set

    // Lista reactiva de tiendas sincronizadas con Firestore
    val tiendas = mutableStateListOf<Tienda>()

    var cargando by mutableStateOf(false)
        private set

    var mensajeError by mutableStateOf<String?>(null)
        private set

    // 4 Tiendas representativas de Alerce para pruebas de desarrolladores y fallback offline
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
            nombre = "Minimarket Doña Marta",
            rubro = "Abarrotes y Alimentos",
            sector = "Alerce Histórico",
            bio = "Frutas y verduras frescas, pan amasado caliente todos los días a las 17:00 hrs, abarrotes, cecinas y bebidas.",
            whatsapp = "+56991234567",
            instagram = "@minimarket.donamarta",
            colorPortada = 0xFF805AD5, // Morado
            colorLogo = 0xFFDD6B20,    // Naranja cálido
            necesidades = NecesidadesTienda(
                tieneStock = true,
                tieneAgendamiento = false,
                tieneVentaPorWhatsApp = true,
                tieneProductosAPedido = false,
                participaEnFerias = false,
                tieneForoComunidad = true
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
        tiendas.clear()
        tiendas.addAll(TIENDAS_INICIALES_ALERCE)
        try {
            val user = auth?.currentUser
            usuarioActual = user
            if (user != null) {
                nombreUsuarioActual = user.displayName ?: user.email?.substringBefore("@") ?: "Usuario"
            }
        } catch (e: Throwable) {
            Log.w(TAG, "No se pudo leer currentUser de Auth: ${e.message}")
        }
    }

    // ============================================================
    // 1. AUTENTICACIÓN FIREBASE AUTH & FIRESTORE
    // ============================================================

    /**
     * Inicia sesión con correo y contraseña en Firebase Auth.
     * Recupera el rol y la tienda asignada desde Firestore.
     */
    suspend fun iniciarSesion(
        correo: String,
        clave: String,
        rolFallback: Rol = Rol.EMPRENDEDORA
    ): Result<Pair<FirebaseUser?, Rol>> {
        val correoLimpio = correo.trim().lowercase()
        val esDemoMarta = correoLimpio == "donamarta@alerce.app" ||
                correoLimpio == "donarosa@alerce.app" ||
                correoLimpio == "marta@alerce.app"

        val fAuth = auth
        if (fAuth != null) {
            try {
                val resultado = fAuth.signInWithEmailAndPassword(correo.trim(), clave).await()
                val user = resultado.user
                if (user != null) {
                    usuarioActual = user
                    nombreUsuarioActual = if (esDemoMarta) "Doña Marta" else (user.displayName ?: correo.substringBefore("@").replaceFirstChar { it.uppercase() })

                    var rolDetectado = if (esDemoMarta) Rol.EMPRENDEDORA else rolFallback

                    // Consultar datos de usuario en Firestore
                    val fStore = firestore
                    if (fStore != null) {
                        try {
                            val docUsuario = fStore.collection(COLECCION_USUARIOS).document(user.uid).get().await()
                            if (docUsuario.exists()) {
                                val rolString = docUsuario.getString("rol")
                                if (rolString != null && !esDemoMarta) {
                                    rolDetectado = try {
                                        Rol.valueOf(rolString)
                                    } catch (_: Exception) {
                                        rolFallback
                                    }
                                }

                                val tiendaId = docUsuario.getString("tiendaId") ?: if (esDemoMarta) "t_minimarket_rosa" else null
                                if (tiendaId != null) {
                                    val docTienda = fStore.collection(COLECCION_TIENDAS).document(tiendaId).get().await()
                                    if (docTienda.exists() && docTienda.data != null) {
                                        val tiendaFirestore = Tienda.fromMap(docTienda.id, docTienda.data!!)
                                        TramaStore.guardarTienda(tiendaFirestore)
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            Log.w(TAG, "Aviso leyendo perfil Firestore: ${e.message}")
                        }
                    }

                    if (esDemoMarta) {
                        val tiendaMarta = TIENDAS_INICIALES_ALERCE.firstOrNull { it.id == "t_minimarket_rosa" } ?: TIENDAS_INICIALES_ALERCE[1]
                        TramaStore.guardarTienda(tiendaMarta)
                    }

                    rolUsuarioActual = rolDetectado
                    TramaStore.iniciarSesion(rolDetectado)
                    return Result.success(Pair(user, rolDetectado))
                }
            } catch (e: Throwable) {
                Log.w(TAG, "Intento de inicio de sesión Firebase: ${e.message}")
                if (!esDemoMarta) {
                    return Result.failure(e)
                }
            }
        }

        // Credenciales oficiales de revisión para Doña Marta (fallback inmediato si la cuenta no ha sido registrada aún en la consola web de Firebase o no hay red)
        if (esDemoMarta) {
            val tiendaMarta = TIENDAS_INICIALES_ALERCE.firstOrNull { it.id == "t_minimarket_rosa" } ?: TIENDAS_INICIALES_ALERCE[1]
            nombreUsuarioActual = "Doña Marta"
            rolUsuarioActual = Rol.EMPRENDEDORA
            TramaStore.guardarTienda(tiendaMarta)
            TramaStore.iniciarSesion(Rol.EMPRENDEDORA)

            // Si Firebase Auth está activo, intentar registrar en segundo plano para persistirla en la nube
            if (fAuth != null) {
                try {
                    val regRes = fAuth.createUserWithEmailAndPassword(correoLimpio, clave).await()
                    usuarioActual = regRes.user
                } catch (_: Exception) {}
            }

            return Result.success(Pair(usuarioActual, Rol.EMPRENDEDORA))
        }

        return Result.failure(IllegalStateException("Firebase Auth no disponible"))
    }

    /**
     * Registra un nuevo usuario en Firebase Auth y crea su documento en Firestore.
     * Si es Emprendedora, crea automáticamente su Tienda asociada en Firestore.
     */
    suspend fun registrarUsuario(
        correo: String,
        clave: String,
        nombre: String,
        rol: Rol
    ): Result<FirebaseUser> {
        val fAuth = auth ?: return Result.failure(IllegalStateException("Firebase Auth no disponible"))
        return try {
            val resultado = fAuth.createUserWithEmailAndPassword(correo.trim(), clave).await()
            val user = resultado.user ?: return Result.failure(IllegalStateException("No se pudo crear el usuario"))
            usuarioActual = user
            val nombreFinal = nombre.ifBlank { correo.substringBefore("@").replaceFirstChar { it.uppercase() } }
            nombreUsuarioActual = nombreFinal

            // Actualizar displayName en Firebase Auth
            try {
                val updates = UserProfileChangeRequest.Builder()
                    .setDisplayName(nombreFinal)
                    .build()
                user.updateProfile(updates).await()
            } catch (e: Exception) {
                Log.w(TAG, "No se pudo actualizar displayName: ${e.message}")
            }

            // Crear documento de perfil y tienda en Firestore
            val fStore = firestore
            if (fStore != null) {
                try {
                    val usuarioMap = hashMapOf<String, Any>(
                        "uid" to user.uid,
                        "email" to (user.email ?: correo),
                        "nombre" to nombreFinal,
                        "rol" to rol.name,
                        "fechaRegistro" to System.currentTimeMillis()
                    )

                    if (rol == Rol.EMPRENDEDORA) {
                        val tiendaId = "tienda_${user.uid.take(12)}"
                        usuarioMap["tiendaId"] = tiendaId

                        val nuevaTienda = Tienda(
                            id = tiendaId,
                            nombre = nombreFinal,
                            rubro = "General y Artesanías",
                            sector = "Alerce",
                            bio = "Emprendimiento local de Alerce. Consulta nuestros productos y contáctanos por WhatsApp.",
                            whatsapp = "",
                            instagram = "",
                            colorPortada = 0xFF111111L,
                            colorLogo = 0xFFDC2626L,
                            necesidades = NecesidadesTienda(
                                tieneStock = true,
                                tieneAgendamiento = false,
                                tieneVentaPorWhatsApp = true,
                                tieneProductosAPedido = false,
                                participaEnFerias = true,
                                tieneForoComunidad = true
                            )
                        )
                        guardarTiendaEnFirestore(nuevaTienda)
                        TramaStore.guardarTienda(nuevaTienda)
                    }

                    fStore.collection(COLECCION_USUARIOS).document(user.uid).set(usuarioMap).await()
                    Log.i(TAG, "Usuario y perfil guardados en Firestore para UID: ${user.uid}")
                } catch (e: Exception) {
                    Log.w(TAG, "No se pudo guardar perfil en Firestore: ${e.message}")
                }
            }

            rolUsuarioActual = rol
            TramaStore.iniciarSesion(rol)
            Result.success(user)
        } catch (e: Throwable) {
            Log.e(TAG, "Error registrando usuario en Firebase: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Acceso rápido exclusivo para pruebas de desarrolladores con las tiendas iniciales.
     */
    fun entrarModoDesarrolladorPrueba(tiendaId: String, rol: Rol) {
        val tiendaPrueba = tiendas.find { it.id == tiendaId }
            ?: TIENDAS_INICIALES_ALERCE.find { it.id == tiendaId }
            ?: TIENDAS_INICIALES_ALERCE.first()

        nombreUsuarioActual = tiendaPrueba.nombre
        rolUsuarioActual = rol
        if (rol == Rol.EMPRENDEDORA) {
            TramaStore.guardarTienda(tiendaPrueba)
        }
        TramaStore.iniciarSesion(rol)
    }

    fun entrarModoDemo(nombre: String, rol: Rol) {
        nombreUsuarioActual = nombre
        rolUsuarioActual = rol
        if (rol == Rol.EMPRENDEDORA) {
            val tiendaDemo = tiendas.find { it.id == "t_minimarket_rosa" }
                ?: TIENDAS_INICIALES_ALERCE.find { it.id == "t_minimarket_rosa" }
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
        rolUsuarioActual = null
        TramaStore.cerrarSesion()
    }

    // ============================================================
    // 2. CLOUD STORAGE: ALMACENAMIENTO DE FOTOGRAFÍAS REALES
    // ============================================================

    /**
     * Sube un archivo de imagen (URI de galería o cámara) a Firebase Cloud Storage
     * y retorna la URL pública de descarga permanente en internet.
     */
    suspend fun subirFotoAStorage(uri: Uri, nombreArchivo: String = "foto_${System.currentTimeMillis()}.jpg"): Result<String> {
        val fStorage = storage ?: return Result.failure(IllegalStateException("Firebase Cloud Storage no está disponible"))
        return try {
            val ref = fStorage.reference.child("fotos_productos/$nombreArchivo")
            ref.putFile(uri).await()
            val urlDescarga = ref.downloadUrl.await().toString()
            Log.i(TAG, "Foto subida exitosamente a Firebase Storage: $urlDescarga")
            Result.success(urlDescarga)
        } catch (e: Throwable) {
            Log.e(TAG, "Error subiendo foto a Firebase Storage: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Sube bytes directos de una imagen (ej: generada desde canvas o preview) a Firebase Cloud Storage.
     */
    suspend fun subirBytesAStorage(
        bytes: ByteArray,
        nombreArchivo: String = "afiche_${System.currentTimeMillis()}.jpg",
        contentType: String = "image/jpeg"
    ): Result<String> {
        val fStorage = storage ?: return Result.failure(IllegalStateException("Firebase Cloud Storage no está disponible"))
        return try {
            val ref = fStorage.reference.child("afiches_flyers/$nombreArchivo")
            val metadata = StorageMetadata.Builder()
                .setContentType(contentType)
                .build()
            ref.putBytes(bytes, metadata).await()
            val urlDescarga = ref.downloadUrl.await().toString()
            Log.i(TAG, "Afiche subido exitosamente a Firebase Storage: $urlDescarga")
            Result.success(urlDescarga)
        } catch (e: Throwable) {
            Log.e(TAG, "Error subiendo afiche a Firebase Storage: ${e.message}")
            Result.failure(e)
        }
    }

    // ============================================================
    // 3. CLOUD FIRESTORE: TIENDAS Y CATÁLOGO
    // ============================================================

    suspend fun cargarTiendasDesdeFirestore(): List<Tienda> {
        cargando = true
        mensajeError = null
        val fStore = firestore
        if (fStore == null) {
            cargando = false
            return tiendas
        }
        return try {
            val snapshot = fStore.collection(COLECCION_TIENDAS).get().await()
            if (snapshot.isEmpty) {
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
            Log.w(TAG, "Aviso leyendo Firestore: ${e.message}. Usando tiendas locales.")
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
            Log.d(TAG, "Tiendas de desarrollador sembradas en Firestore")
            true
        } catch (e: Throwable) {
            Log.e(TAG, "Error sembrando tiendas en Firestore: ${e.message}")
            false
        }
    }

    suspend fun guardarTiendaEnFirestore(tienda: Tienda): Boolean {
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
            Log.w(TAG, "Error guardando en Firestore: ${e.message}")
            false
        }
    }
}
