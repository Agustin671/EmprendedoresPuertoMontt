package com.example.trabajodea10.data.remoto

import com.example.trabajodea10.data.AlAgotarse
import com.example.trabajodea10.data.CampoTipo
import com.example.trabajodea10.data.Categoria
import com.example.trabajodea10.data.EstadoSync
import com.example.trabajodea10.data.FeriaPlataforma
import com.example.trabajodea10.data.ItemVenta
import com.example.trabajodea10.data.Modalidad
import com.example.trabajodea10.data.TipoProducto
import com.example.trabajodea10.data.ValoresProducto
import com.example.trabajodea10.data.MedioPago
import com.example.trabajodea10.data.NecesidadesTienda
import com.example.trabajodea10.data.Producto
import com.example.trabajodea10.data.Rol
import com.example.trabajodea10.data.Tienda
import com.example.trabajodea10.data.Variante
import com.example.trabajodea10.data.Venta
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.addJsonObject
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull
import kotlinx.serialization.json.put
import java.net.URLEncoder

/*
 * Capa de datos (RNF-DAT-02): las pantallas nunca llaman a la base de datos;
 * todo pasa por estos repositorios. Tablas y reglas: supabase/01_esquema.sql
 */

private const val PREFER_UPSERT = "resolution=merge-duplicates,return=minimal"

private fun cod(valor: String): String = URLEncoder.encode(valor, "UTF-8")

private fun JsonObject.texto(clave: String): String? = this[clave]?.jsonPrimitive?.contentOrNull
private fun JsonObject.textoO(clave: String, porDefecto: String = ""): String = texto(clave) ?: porDefecto
private fun JsonObject.booleano(clave: String, porDefecto: Boolean = false): Boolean =
    this[clave]?.jsonPrimitive?.booleanOrNull ?: porDefecto
private fun JsonObject.largo(clave: String): Long? = this[clave]?.jsonPrimitive?.longOrNull
private fun JsonObject.decimal(clave: String): Double = this[clave]?.jsonPrimitive?.doubleOrNull ?: 0.0

private fun arreglo(texto: String): List<JsonObject> =
    ClienteSupabase.json.parseToJsonElement(texto).jsonArray.map { it.jsonObject }

// ===================================================================== Perfil

data class Perfil(
    val id: String,
    val email: String,
    val nombre: String,
    val rol: Rol,
    val tiendaId: String?,
    val fotoUrl: String? = null
)

object PerfilRepositorio {
    suspend fun miPerfil(): Perfil {
        val sesion = ClienteSupabase.sesion ?: throw ErrorSupabase(401, "No hay sesión iniciada.")
        val texto = ClienteSupabase.pedir("GET", "/rest/v1/perfiles?select=*&id=eq.${cod(sesion.userId)}")
        val o = arreglo(texto).firstOrNull()
            ?: throw ErrorSupabase(404, "Tu cuenta existe, pero aún no tiene perfil. Avisa al administrador.")
        return Perfil(
            id = o.textoO("id"),
            email = o.textoO("email", sesion.email),
            nombre = o.textoO("nombre"),
            rol = runCatching { Rol.valueOf(o.textoO("rol", "VISITANTE")) }.getOrDefault(Rol.VISITANTE),
            tiendaId = o.texto("tienda_id"),
            fotoUrl = o.texto("foto_url")
        )
    }

    /**
     * La propia persona cambia su nombre o su foto. La función del servidor
     * solo toca esas dos columnas: el rol, el correo y la tienda no se pueden
     * cambiar desde la app ni desde la web (RN-01).
     */
    suspend fun actualizarMiPerfil(nombre: String? = null, fotoUrl: String? = null) {
        val cuerpo = buildJsonObject {
            nombre?.let { put("p_nombre", it) }
            fotoUrl?.let { put("p_foto_url", it) }
        }.toString()
        ClienteSupabase.pedir(
            "POST", "/rest/v1/rpc/actualizar_mi_perfil",
            cuerpoJson = cuerpo
        )
    }
}

// ===================================================================== Cuentas (admin)

object CuentaRepositorio {
    /** El administrador autoriza un correo como emprendedora (RN-01). */
    suspend fun autorizarEmprendedora(email: String, nombre: String) {
        val cuerpo = buildJsonObject {
            put("email", email.trim().lowercase())
            put("rol", "EMPRENDEDORA")
            put("nombre", nombre.trim())
        }.toString()
        ClienteSupabase.pedir(
            "POST", "/rest/v1/cuentas_autorizadas?on_conflict=email",
            cuerpoJson = cuerpo, cabeceras = mapOf("Prefer" to PREFER_UPSERT)
        )
    }
}


// ===================================================================== Tiendas

object TiendaRepositorio {

    /** Crea una tienda para la persona autenticada; el servidor asigna propiedad y rol. */
    suspend fun crearMiTienda(nombre: String) {
        val cuerpo = buildJsonObject { put("p_nombre", nombre.trim()) }.toString()
        ClienteSupabase.pedir(
            "POST", "/rest/v1/rpc/crear_mi_tienda",
            cuerpoJson = cuerpo
        )
    }

    private inline fun <reified T : Enum<T>> enumO(valor: String?, porDefecto: T): T =
        runCatching { enumValueOf<T>(valor ?: "") }.getOrDefault(porDefecto)

    fun desdeJson(o: JsonObject): Tienda {
        val nec = o["necesidades"] as? JsonObject ?: JsonObject(emptyMap())
        fun n(clave: String, porDefecto: Boolean) = nec[clave]?.jsonPrimitive?.booleanOrNull ?: porDefecto
        val val0 = o["valores_producto"] as? JsonObject ?: JsonObject(emptyMap())
        fun vt(clave: String) = val0[clave]?.jsonPrimitive?.contentOrNull
        fun vb(clave: String, porDefecto: Boolean) = val0[clave]?.jsonPrimitive?.booleanOrNull ?: porDefecto
        return Tienda(
            id = o.textoO("id"),
            nombre = o.textoO("nombre", "Comercio local"),
            rubro = o.textoO("rubro", "Comercio"),
            sector = o.textoO("sector", "Alerce"),
            bio = o.textoO("bio"),
            whatsapp = o.textoO("whatsapp"),
            instagram = o.textoO("instagram"),
            colorPortada = o.largo("color_portada") ?: 0xFF5C315B,
            colorLogo = o.largo("color_logo") ?: 0xFF2E7D5B,
            logoUrl = o.texto("logo_url"),
            necesidades = NecesidadesTienda(
                tieneStock = n("tieneStock", true),
                tieneAgendamiento = n("tieneAgendamiento", false),
                tieneVentaPorWhatsApp = n("tieneVentaPorWhatsApp", true),
                tieneProductosAPedido = n("tieneProductosAPedido", false),
                participaEnFerias = n("participaEnFerias", true),
                tieneForoComunidad = n("tieneForoComunidad", false)
            ),
            historialActivo = o.booleano("historial_activo", true),
            valoresProducto = ValoresProducto(
                categoria = enumO(vt("categoria"), Categoria.TEXTIL),
                modalidad = enumO(vt("modalidad"), Modalidad.REGULAR),
                manejaStock = vb("manejaStock", true),
                alAgotarse = enumO(vt("alAgotarse"), AlAgotarse.MOSTRAR_AGOTADO),
                publicado = vb("publicado", false),
                disponible = vb("disponible", true)
            )
        )
    }

    private fun aJson(t: Tienda): String = buildJsonObject {
        put("nombre", t.nombre)
        put("rubro", t.rubro)
        put("sector", t.sector)
        put("bio", t.bio)
        put("whatsapp", t.whatsapp)
        put("instagram", t.instagram)
        put("color_portada", t.colorPortada)
        put("color_logo", t.colorLogo)
        put("logo_url", t.logoUrl?.takeIf { it.startsWith("http") })
        put("necesidades", buildJsonObject {
            put("tieneStock", t.necesidades.tieneStock)
            put("tieneAgendamiento", t.necesidades.tieneAgendamiento)
            put("tieneVentaPorWhatsApp", t.necesidades.tieneVentaPorWhatsApp)
            put("tieneProductosAPedido", t.necesidades.tieneProductosAPedido)
            put("participaEnFerias", t.necesidades.participaEnFerias)
            put("tieneForoComunidad", t.necesidades.tieneForoComunidad)
        })
        put("historial_activo", t.historialActivo)
        put("valores_producto", buildJsonObject {
            put("categoria", t.valoresProducto.categoria.name)
            put("modalidad", t.valoresProducto.modalidad.name)
            put("manejaStock", t.valoresProducto.manejaStock)
            put("alAgotarse", t.valoresProducto.alAgotarse.name)
            put("publicado", t.valoresProducto.publicado)
            put("disponible", t.valoresProducto.disponible)
        })
    }.toString()

    suspend fun listar(): List<Tienda> =
        arreglo(ClienteSupabase.pedir("GET", "/rest/v1/tiendas?select=*&order=nombre.asc")).map(::desdeJson)

    suspend fun obtener(id: String): Tienda? =
        arreglo(ClienteSupabase.pedir("GET", "/rest/v1/tiendas?select=*&id=eq.${cod(id)}"))
            .firstOrNull()?.let(::desdeJson)

    /**
     * Actualiza la ficha de la tienda. Devuelve false si el servidor no cambió ninguna
     * fila (por ejemplo, porque la tienda no es de quien inició sesión).
     */
    suspend fun actualizar(t: Tienda): Boolean {
        val texto = ClienteSupabase.pedir(
            "PATCH", "/rest/v1/tiendas?id=eq.${cod(t.id)}",
            cuerpoJson = aJson(t), cabeceras = mapOf("Prefer" to "return=representation")
        )
        return arreglo(texto).isNotEmpty()
    }
}

// ===================================================================== Productos

/** Cantidades confirmadas por el servidor para un producto después de una venta. */
data class StockServidor(val productoId: String, val variantes: List<Variante>, val vendidaEn: String?)

object ProductoRepositorio {

    private inline fun <reified T : Enum<T>> enumO(valor: String?, porDefecto: T): T =
        runCatching { enumValueOf<T>(valor ?: "") }.getOrDefault(porDefecto)

    /** Opciones tal como las guarda el servidor; stockBase = lo que tenía el servidor. */
    fun variantesDesdeJson(arr: JsonArray?, productoId: String): List<Variante> =
        arr?.mapIndexedNotNull { i, elemento ->
            val v = elemento as? JsonObject ?: return@mapIndexedNotNull null
            val stock = v["stock"]?.jsonPrimitive?.intOrNull ?: 0
            Variante(
                id = v.texto("id")?.takeIf { it.isNotBlank() } ?: "${productoId}_v${i + 1}",
                nombre = v.textoO("nombre", "Unidad"),
                stock = stock,
                precio = v["precio"]?.jsonPrimitive?.doubleOrNull,
                fotoUrl = v.texto("foto_url")?.takeIf { it.startsWith("http") },
                stockBase = stock
            )
        } ?: emptyList()

    fun desdeJson(o: JsonObject): Producto {
        val id = o.textoO("id")
        val atributos = (o["atributos"] as? JsonObject)?.mapNotNull { (k, v) ->
            v.jsonPrimitive.contentOrNull?.let { k to it }
        }?.toMap() ?: emptyMap()
        val fotos = (o["fotos"] as? JsonArray)?.mapNotNull { it.jsonPrimitive.contentOrNull?.takeIf { u -> u.startsWith("http") } } ?: emptyList()
        return Producto(
            id = id,
            tiendaId = o.textoO("tienda_id"),
            nombre = o.textoO("nombre"),
            categoria = enumO(o.texto("categoria"), Categoria.TEXTIL),
            precio = o.decimal("precio"),
            publicado = o.booleano("publicado"),
            destacado = o.booleano("destacado"),
            colorFoto = o.largo("color_foto") ?: 0xFF8E6B94,
            variantes = variantesDesdeJson(o["variantes"] as? JsonArray, id),
            fotoUrl = o.texto("foto_url"),
            descripcion = o.textoO("descripcion"),
            tipo = o.texto("tipo")?.takeIf { it.isNotBlank() },
            atributos = atributos,
            fotos = fotos,
            modalidad = enumO(o.texto("modalidad"), Modalidad.REGULAR),
            manejaStock = o.booleano("maneja_stock", true),
            alAgotarse = enumO(o.texto("al_agotarse"), AlAgotarse.MOSTRAR_AGOTADO),
            disponible = o.booleano("disponible", true),
            vendidaEn = o.texto("vendida_en")
        )
    }

    /**
     * Cuerpo de guardar_producto(). La tienda la decide el servidor. Para opciones
     * que ya existían envía stockBase: el servidor suma la diferencia a lo que
     * quede, sin pisar ventas hechas en otro dispositivo.
     */
    fun cuerpoGuardar(p: Producto): JsonObject = buildJsonObject {
        put("id", p.id)
        put("nombre", p.nombre.trim())
        put("descripcion", p.descripcion)
        put("categoria", p.categoria.name)
        put("tipo", p.tipo)
        put("atributos", buildJsonObject { p.atributos.forEach { (k, v) -> put(k, v) } })
        put("precio", p.precio)
        put("publicado", p.publicado)
        put("disponible", p.disponible)
        put("destacado", p.destacado)
        put("colorFoto", p.colorFoto)
        put("modalidad", p.modalidad.name)
        put("manejaStock", p.manejaStock)
        put("alAgotarse", p.alAgotarse.name)
        // Solo se guardan URLs públicas; una ruta del celular no sirve en el servidor.
        put("fotoUrl", p.fotoUrl?.takeIf { it.startsWith("https://") })
        put("fotos", buildJsonArray { p.fotos.filter { it.startsWith("https://") }.forEach { add(JsonPrimitive(it)) } })
        put("variantes", buildJsonArray {
            p.variantes.forEach { v ->
                addJsonObject {
                    put("id", v.id)
                    put("nombre", v.nombre.trim())
                    put("precio", v.precio)
                    put("fotoUrl", v.fotoUrl?.takeIf { it.startsWith("https://") })
                    put("stock", v.stock.coerceAtLeast(0))
                    v.stockBase?.let { put("stockBase", it) }
                }
            }
        })
    }

    /** Productos que la RLS deja ver a esta sesión (sin sesión: solo los públicos). */
    suspend fun publicados(): List<Producto> =
        arreglo(ClienteSupabase.pedir("GET", "/rest/v1/productos?select=*")).map(::desdeJson)

    /** Todos los productos de una tienda (la dueña también ve los que solo ella ve). */
    suspend fun deTienda(tiendaId: String): List<Producto> =
        arreglo(ClienteSupabase.pedir("GET", "/rest/v1/productos?select=*&tienda_id=eq.${cod(tiendaId)}"))
            .map(::desdeJson)

    /** Alta o edición segura; devuelve el producto como quedó en el servidor. */
    suspend fun guardar(p: Producto): Producto {
        val cuerpo = buildJsonObject { put("p", cuerpoGuardar(p)) }.toString()
        val texto = ClienteSupabase.pedir("POST", "/rest/v1/rpc/guardar_producto", cuerpoJson = cuerpo)
        return desdeJson(ClienteSupabase.json.parseToJsonElement(texto).jsonObject)
    }

    /** Disponible / no disponible para varios a la vez; devuelve cuántos cambió el servidor. */
    suspend fun cambiarDisponible(ids: List<String>, disponible: Boolean): Int {
        if (ids.isEmpty()) return 0
        val filtro = ids.joinToString(",") { "\"" + it.replace("\"", "") + "\"" }
        val texto = ClienteSupabase.pedir(
            "PATCH", "/rest/v1/productos?id=in.(${cod(filtro)})",
            cuerpoJson = buildJsonObject { put("disponible", disponible) }.toString(),
            cabeceras = mapOf("Prefer" to "return=representation")
        )
        return arreglo(texto).size
    }

    suspend fun eliminar(id: String) {
        ClienteSupabase.pedir("DELETE", "/rest/v1/productos?id=eq.${cod(id)}")
    }
}

// ===================================================================== Ventas

/** Resultado de registrar_venta(): REGISTRADA o YA_REGISTRADA (reintento). */
data class ResultadoVenta(val estado: String, val productos: List<StockServidor>)

object VentaRepositorio {

    private fun desdeJson(o: JsonObject): Venta {
        val items = (o["items"] as? JsonArray)?.mapNotNull { e ->
            val i = e as? JsonObject ?: return@mapNotNull null
            ItemVenta(
                productoId = i.textoO("productoId"),
                varianteId = i.textoO("varianteId"),
                nombre = i.textoO("nombre"),
                detalle = i.textoO("detalle"),
                precio = i.decimal("precio"),
                cantidad = i["cantidad"]?.jsonPrimitive?.intOrNull ?: 1,
                fotoUrl = i.texto("fotoUrl"),
                encargo = i.booleano("encargo")
            )
        } ?: emptyList()
        // Se usa la hora local con que se anotó; si no está, la del servidor.
        val fecha = o.texto("fecha_local") ?: o.textoO("fecha").replace('T', ' ').take(16)
        return Venta(
            id = o.textoO("id"),
            fecha = fecha,
            items = items,
            medioPago = runCatching { MedioPago.valueOf(o.textoO("medio_pago", "EFECTIVO")) }
                .getOrDefault(MedioPago.EFECTIVO),
            feriaId = o.texto("feria_id"),
            total = o.decimal("total"),
            lugarTipo = o.texto("lugar_tipo"),
            lugarNombre = o.texto("lugar_nombre"),
            enHistorial = o.booleano("en_historial", true),
            estadoSync = EstadoSync.CONFIRMADA
        )
    }

    suspend fun historial(tiendaId: String): List<Venta> =
        arreglo(
            ClienteSupabase.pedir("GET", "/rest/v1/ventas?select=*&tienda_id=eq.${cod(tiendaId)}&order=fecha.desc")
        ).map(::desdeJson)

    fun cuerpoRegistrar(venta: Venta): JsonObject = buildJsonObject {
        put("id", venta.id)
        put("fechaLocal", venta.fecha)
        put("medioPago", venta.medioPago.name)
        put("feriaId", venta.feriaId)
        put("lugarTipo", venta.lugarTipo ?: if (venta.feriaId != null) "FERIA" else null)
        put("lugarNombre", venta.lugarNombre)
        put("conHistorial", venta.enHistorial)
        put("items", buildJsonArray {
            venta.items.forEach { i ->
                addJsonObject {
                    put("productoId", i.productoId)
                    put("varianteId", i.varianteId)
                    put("cantidad", i.cantidad)
                    put("encargo", i.encargo)
                    put("precio", i.precio)
                    put("nombre", i.nombre)
                    put("detalle", i.detalle)
                    put("fotoUrl", i.fotoUrl?.takeIf { it.startsWith("https://") })
                }
            }
        })
    }

    /**
     * Registra la venta y descuenta unidades en una sola operación del servidor.
     * Es idempotente: reintentar con el mismo id no duplica la venta.
     */
    suspend fun registrar(venta: Venta): ResultadoVenta {
        val cuerpo = buildJsonObject { put("p_venta", cuerpoRegistrar(venta)) }.toString()
        val texto = ClienteSupabase.pedir("POST", "/rest/v1/rpc/registrar_venta", cuerpoJson = cuerpo)
        val o = ClienteSupabase.json.parseToJsonElement(texto).jsonObject
        val productos = (o["productos"] as? JsonArray)?.mapNotNull { e ->
            val p = e as? JsonObject ?: return@mapNotNull null
            val id = p.textoO("id")
            StockServidor(id, ProductoRepositorio.variantesDesdeJson(p["variantes"] as? JsonArray, id), p.texto("vendida_en"))
        } ?: emptyList()
        return ResultadoVenta(o.textoO("estado", "REGISTRADA"), productos)
    }
}

// ===================================================================== Tipos y ferias (solo lectura)

object TipoRepositorio {
    suspend fun listar(): List<TipoProducto> =
        arreglo(ClienteSupabase.pedir("GET", "/rest/v1/tipos_producto?select=*&activo=eq.true&order=orden.asc")).map { o ->
            TipoProducto(
                id = o.textoO("id"),
                categoria = o.textoO("categoria"),
                nombre = o.textoO("nombre"),
                campos = (o["campos"] as? JsonArray)?.mapNotNull { e ->
                    val c = e as? JsonObject ?: return@mapNotNull null
                    CampoTipo(
                        clave = c.textoO("clave"),
                        etiqueta = c.textoO("etiqueta", c.textoO("clave")),
                        tipo = c.textoO("tipo", "texto"),
                        opciones = (c["opciones"] as? JsonArray)?.mapNotNull { it.jsonPrimitive.contentOrNull } ?: emptyList(),
                        obligatorio = c.booleano("obligatorio")
                    )
                }?.filter { it.clave.isNotBlank() } ?: emptyList()
            )
        }
}

object FeriaRepositorio {
    suspend fun listar(): List<FeriaPlataforma> =
        arreglo(ClienteSupabase.pedir("GET", "/rest/v1/ferias?select=id,nombre&order=fecha_inicio.desc.nullslast"))
            .map { FeriaPlataforma(it.textoO("id"), it.textoO("nombre")) }
}
