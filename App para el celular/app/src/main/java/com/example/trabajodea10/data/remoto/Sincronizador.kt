package com.example.trabajodea10.data.remoto

import android.content.Context
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.Network
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.trabajodea10.data.FotoProductoStore
import com.example.trabajodea10.data.Producto
import com.example.trabajodea10.data.TramaStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.IOException

/**
 * Cola de cambios para trabajar con mala señal (RNF-REN-02, RNF-DAT-03).
 * - Cada cuenta tiene su propia cola (archivo aparte): nunca se suben cambios
 *   de una cuenta con la sesión de otra, y cerrar sesión no los borra.
 * - Un cambio se anota aunque la sesión haya vencido; se sube al volver a entrar.
 * - Se reintenta al abrir la app, al entrar, al tocar "Subir ahora" y cuando
 *   vuelve la conexión, con espera creciente si el servidor sigue fallando.
 * - Las ventas se registran con registrar_venta() (idempotente): un reintento
 *   nunca duplica. Si el servidor la rechaza por una regla (por ejemplo, ya no
 *   quedaban unidades) la venta queda marcada como conflicto, no se descarta.
 */
object Sincronizador {

    private const val TAG = "Sincronizador"
    private const val PRODUCTOS = "productos"
    private const val ELIMINADOS = "eliminados"
    private const val VENTAS = "ventas"
    private const val TIENDA = "tienda"
    private val ESPERAS_MS = longArrayOf(5_000, 15_000, 60_000, 300_000)

    private val ambito = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val candado = Mutex()
    private var contexto: Context? = null
    private var prefs: SharedPreferences? = null
    private var reintento: Job? = null
    private var fallosSeguidos = 0

    /** Cuenta dueña de la cola actual (uid de Supabase) o null sin cuenta. */
    var cuentaActiva: String? = null
        private set

    /** Cantidad de cambios que aún no llegan al servidor (se muestra en pantalla). */
    var pendientes by mutableStateOf(0)
        private set

    /** true mientras se está subiendo algo. */
    var subiendo by mutableStateOf(false)
        private set

    /** Último problema al subir (para mostrarlo sin perder el trabajo). */
    var ultimoProblema by mutableStateOf<String?>(null)
        private set

    fun inicializar(context: Context) {
        if (contexto != null) return
        contexto = context.applicationContext
        migrarColaAntigua(ClienteSupabase.sesion?.userId)
        activarCuenta(ClienteSupabase.sesion?.userId)
        escucharConexion()
    }

    /** La cola sin cuenta de versiones anteriores pasa a la cuenta con sesión guardada. */
    private fun migrarColaAntigua(uid: String?) {
        val ctx = contexto ?: return
        val antigua = ctx.getSharedPreferences("supabase_pendientes", Context.MODE_PRIVATE)
        if (uid == null || antigua.all.isEmpty()) return
        val nueva = ctx.getSharedPreferences("supabase_pendientes_$uid", Context.MODE_PRIVATE)
        val editor = nueva.edit()
        antigua.all.forEach { (clave, valor) ->
            @Suppress("UNCHECKED_CAST")
            (valor as? Set<String>)?.let { editor.putStringSet(clave, (nueva.getStringSet(clave, emptySet()) ?: emptySet()) + it) }
        }
        editor.apply()
        antigua.edit().clear().apply()
    }

    /** Cambia a la cola de otra cuenta (o a ninguna). Lo pendiente de la anterior se conserva. */
    @Synchronized
    fun activarCuenta(uid: String?) {
        val ctx = contexto ?: return
        cuentaActiva = uid
        prefs = uid?.let { ctx.getSharedPreferences("supabase_pendientes_$it", Context.MODE_PRIVATE) }
        fallosSeguidos = 0
        ultimoProblema = null
        actualizarConteo()
    }

    private fun escucharConexion() {
        val cm = contexto?.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return
        runCatching {
            cm.registerDefaultNetworkCallback(object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    fallosSeguidos = 0
                    programar(1_500)
                }
            })
        }.onFailure { Log.w(TAG, "No se pudo escuchar la conexión: ${it.message}") }
    }

    // ------------------------------------------------------------ registro

    @Synchronized
    private fun leer(clave: String): Set<String> = prefs?.getStringSet(clave, emptySet())?.toSet() ?: emptySet()

    @Synchronized
    private fun agregar(clave: String, id: String) {
        prefs?.edit()?.putStringSet(clave, leer(clave) + id)?.apply()
        actualizarConteo()
    }

    @Synchronized
    private fun quitar(clave: String, id: String) {
        prefs?.edit()?.putStringSet(clave, leer(clave) - id)?.apply()
        actualizarConteo()
    }

    private fun actualizarConteo() {
        pendientes = leer(PRODUCTOS).size + leer(ELIMINADOS).size + leer(VENTAS).size + leer(TIENDA).size
    }

    /** Hay una cuenta a la que anotar cambios (aunque su sesión haya vencido). */
    private val hayCuenta: Boolean get() = cuentaActiva != null && ConfigSupabase.configurado

    /** Solo se sube con la sesión de la misma cuenta dueña de la cola. */
    private val puedeSubir: Boolean
        get() = hayCuenta && ClienteSupabase.sesion?.userId == cuentaActiva

    fun productoCambiado(id: String) {
        if (!hayCuenta) return
        quitar(ELIMINADOS, id)
        agregar(PRODUCTOS, id)
        subirEnSegundoPlano()
    }

    fun productoEliminado(id: String) {
        if (!hayCuenta) return
        quitar(PRODUCTOS, id)
        agregar(ELIMINADOS, id)
        subirEnSegundoPlano()
    }

    fun ventaRegistrada(id: String) {
        if (!hayCuenta) return
        agregar(VENTAS, id)
        subirEnSegundoPlano()
    }

    fun tiendaPendiente() {
        if (!hayCuenta) return
        agregar(TIENDA, TIENDA)
    }

    /** ¿Las ventas se confirman con el servidor? (si no, quedan solo en el celular). */
    val ventasConServidor: Boolean get() = hayCuenta

    /** El administrador autoriza el correo de una emprendedora nueva (RN-01). */
    fun autorizarEmprendedora(email: String, nombre: String) {
        if (!puedeSubir) return
        ambito.launch {
            runCatching { CuentaRepositorio.autorizarEmprendedora(email, nombre) }
                .onFailure { Log.w(TAG, "No se pudo autorizar $email: ${it.message}") }
        }
    }

    fun idsProductosPendientes(): Set<String> = leer(PRODUCTOS)
    fun idsEliminadosPendientes(): Set<String> = leer(ELIMINADOS)
    fun idsVentasPendientes(): Set<String> = leer(VENTAS)

    fun subirEnSegundoPlano() = programar(0)

    private fun programar(esperaMs: Long) {
        reintento?.cancel()
        reintento = ambito.launch {
            if (esperaMs > 0) delay(esperaMs)
            val todoSubido = subirPendientes()
            if (!todoSubido && pendientes > 0 && puedeSubir) {
                val espera = ESPERAS_MS[fallosSeguidos.coerceAtMost(ESPERAS_MS.size - 1)]
                fallosSeguidos++
                programar(espera)
            } else if (todoSubido) {
                fallosSeguidos = 0
            }
        }
    }

    // ------------------------------------------------------------ subida

    /** Sube todo lo pendiente. Devuelve true si no quedó nada por subir. */
    suspend fun subirPendientes(): Boolean = candado.withLock {
        if (!puedeSubir) return@withLock pendientes == 0
        val cuenta = cuentaActiva
        subiendo = true
        var hubo = false
        try {
            val miTienda = withContext(Dispatchers.Main) { TramaStore.tienda.id }

            for (id in leer(ELIMINADOS)) {
                runCatching { ProductoRepositorio.eliminar(id) }
                    .onSuccess { quitar(ELIMINADOS, id) }
                    .onFailure { hubo = true; anotar("No se pudo borrar un producto", it) }
            }

            // Ventas antes que productos: así el servidor descuenta primero y la
            // edición posterior se aplica como diferencia sobre lo que quede.
            for (id in leer(VENTAS)) {
                if (cuentaActiva != cuenta) break
                val venta = withContext(Dispatchers.Main) { TramaStore.ventas.firstOrNull { it.id == id } }
                if (venta == null) { quitar(VENTAS, id); continue }
                try {
                    val r = VentaRepositorio.registrar(venta)
                    withContext(Dispatchers.Main) { TramaStore.confirmarVenta(id, r.productos) }
                    quitar(VENTAS, id)
                } catch (e: ErrorSupabase) {
                    if (e.esRechazoDeRegla) {
                        // Se conserva y se marca: la emprendedora decide cómo resolverlo.
                        withContext(Dispatchers.Main) { TramaStore.marcarConflictoVenta(id, e.message ?: e.crudo) }
                        quitar(VENTAS, id)
                    } else { hubo = true; anotar("Una venta quedó por subir", e) }
                } catch (e: IOException) {
                    hubo = true; anotar("Una venta quedó por subir", e)
                }
            }

            for (id in leer(PRODUCTOS)) {
                if (cuentaActiva != cuenta) break
                val producto = withContext(Dispatchers.Main) { TramaStore.productoPorId(id) }
                if (producto == null || producto.tiendaId != miTienda) {
                    quitar(PRODUCTOS, id) // ya no existe o no es de esta cuenta
                } else if (producto.nombre.isNotBlank()) {
                    try {
                        val enviado = subirFotos(producto)
                        val guardado = ProductoRepositorio.guardar(enviado)
                        withContext(Dispatchers.Main) { TramaStore.aplicarProductoServidor(guardado, enviado) }
                        quitar(PRODUCTOS, id)
                    } catch (e: ErrorSupabase) {
                        if (e.esRechazoDeRegla) {
                            val actual = runCatching { ProductoRepositorio.deTienda(miTienda).firstOrNull { it.id == id } }.getOrNull()
                            withContext(Dispatchers.Main) { TramaStore.marcarConflictoProducto(id, e.message ?: e.crudo, actual) }
                            quitar(PRODUCTOS, id)
                        } else { hubo = true; anotar("Un producto quedó por subir", e) }
                    } catch (e: IOException) {
                        hubo = true; anotar("Un producto quedó por subir", e)
                    }
                }
            }

            if (leer(TIENDA).isNotEmpty()) {
                val tienda = withContext(Dispatchers.Main) { TramaStore.tienda }
                if (runCatching { TiendaRepositorio.actualizar(tienda) }.getOrDefault(false)) quitar(TIENDA, TIENDA) else hubo = true
            }
            if (!hubo) ultimoProblema = null
            pendientes == 0
        } finally {
            subiendo = false
        }
    }

    private fun anotar(que: String, e: Throwable) {
        Log.w(TAG, "$que: ${e.message}")
        ultimoProblema = "$que. Se reintentará solo: ${e.message ?: "sin conexión"}"
    }

    /** Sube las fotos del celular (producto y opciones) y devuelve el producto con sus URLs. */
    private suspend fun subirFotos(p: Producto): Producto {
        var producto = p
        val foto = p.fotoUrl
        if (foto != null && !foto.startsWith("http")) {
            val url = FotoProductoStore.subir(foto, p.id)
            withContext(Dispatchers.Main) { TramaStore.aplicarFotoRemota(p.id, foto, url) }
            producto = producto.copy(fotoUrl = url)
        }
        val variantes = producto.variantes.map { v ->
            val local = v.fotoUrl
            if (local != null && !local.startsWith("http")) {
                val url = FotoProductoStore.subir(local, "${p.id}_${v.id}")
                withContext(Dispatchers.Main) { TramaStore.aplicarFotoOpcionRemota(p.id, v.id, local, url) }
                v.copy(fotoUrl = url)
            } else v
        }
        return producto.copy(variantes = variantes)
    }
}
