package com.example.trabajodea10.data.remoto

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.trabajodea10.data.FotoProductoStore
import com.example.trabajodea10.data.Producto
import com.example.trabajodea10.data.TramaStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/**
 * Cola de cambios para trabajar con mala señal (RNF-REN-02, RNF-DAT-03):
 * todo cambio se guarda primero en el celular y queda anotado aquí como
 * "por subir". Se sube apenas hay conexión; si falla, se reintenta después
 * (al abrir la app, al entrar o al tocar "Subir ahora"). Nada se pierde.
 */
object Sincronizador {

    private const val TAG = "Sincronizador"
    private const val PRODUCTOS = "productos"
    private const val ELIMINADOS = "eliminados"
    private const val VENTAS = "ventas"
    private const val TIENDA = "tienda"

    private val ambito = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val candado = Mutex()
    private var prefs: SharedPreferences? = null

    /** Cantidad de cambios que aún no llegan al servidor (se muestra en pantalla). */
    var pendientes by mutableStateOf(0)
        private set

    /** true mientras se está subiendo algo. */
    var subiendo by mutableStateOf(false)
        private set

    fun inicializar(context: Context) {
        if (prefs != null) return
        prefs = context.applicationContext.getSharedPreferences("supabase_pendientes", Context.MODE_PRIVATE)
        actualizarConteo()
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

    private val hayCuenta: Boolean get() = ClienteSupabase.sesion != null && ConfigSupabase.configurado

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

    /** El administrador autoriza el correo de una emprendedora nueva (RN-01). */
    fun autorizarEmprendedora(email: String, nombre: String) {
        if (!hayCuenta) return
        ambito.launch {
            runCatching { CuentaRepositorio.autorizarEmprendedora(email, nombre) }
                .onFailure { Log.w(TAG, "No se pudo autorizar $email: ${it.message}") }
        }
    }

    fun idsProductosPendientes(): Set<String> = leer(PRODUCTOS)
    fun idsVentasPendientes(): Set<String> = leer(VENTAS)

    /** Al cerrar sesión de verdad: lo pendiente era de esa cuenta y no debe subirse con otra. */
    @Synchronized
    fun descartarTodo() {
        prefs?.edit()?.clear()?.apply()
        actualizarConteo()
    }

    fun subirEnSegundoPlano() {
        ambito.launch { subirPendientes() }
    }

    // ------------------------------------------------------------ subida

    /** Sube todo lo pendiente. Devuelve true si no quedó nada por subir. */
    suspend fun subirPendientes(): Boolean = candado.withLock {
        if (!hayCuenta) return@withLock false
        subiendo = true
        try {
            val miTienda = withContext(Dispatchers.Main) { TramaStore.tienda.id }

            for (id in leer(ELIMINADOS)) {
                if (runCatching { ProductoRepositorio.eliminar(id) }.isSuccess) quitar(ELIMINADOS, id)
            }

            for (id in leer(PRODUCTOS)) {
                val producto = withContext(Dispatchers.Main) { TramaStore.productoPorId(id) }
                if (producto == null || producto.tiendaId != miTienda) {
                    quitar(PRODUCTOS, id) // ya no existe o no es de esta cuenta
                } else if (producto.nombre.isNotBlank()) {
                    runCatching { subirProducto(producto) }
                        .onSuccess { quitar(PRODUCTOS, id) }
                        .onFailure { Log.w(TAG, "Producto $id quedó pendiente: ${it.message}") }
                }
            }

            for (id in leer(VENTAS)) {
                val venta = withContext(Dispatchers.Main) { TramaStore.ventas.firstOrNull { it.id == id } }
                if (venta == null) {
                    quitar(VENTAS, id)
                } else {
                    runCatching { VentaRepositorio.registrar(venta, miTienda) }
                        .onSuccess { quitar(VENTAS, id) }
                        .onFailure { Log.w(TAG, "Venta $id quedó pendiente: ${it.message}") }
                }
            }

            if (leer(TIENDA).isNotEmpty()) {
                val tienda = withContext(Dispatchers.Main) { TramaStore.tienda }
                if (runCatching { TiendaRepositorio.actualizar(tienda) }.getOrDefault(false)) quitar(TIENDA, TIENDA)
            }

            pendientes == 0
        } finally {
            subiendo = false
        }
    }

    private suspend fun subirProducto(p: Producto) {
        var producto = p
        val foto = p.fotoUrl
        if (foto != null && !foto.startsWith("http")) {
            val url = FotoProductoStore.subir(foto, p.id)
            withContext(Dispatchers.Main) { TramaStore.aplicarFotoRemota(p.id, foto, url) }
            producto = producto.copy(fotoUrl = url)
        }
        ProductoRepositorio.guardar(producto)
    }
}
