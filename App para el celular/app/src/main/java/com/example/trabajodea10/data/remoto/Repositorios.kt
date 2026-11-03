package com.example.trabajodea10.data.remoto

import com.example.trabajodea10.data.Categoria
import com.example.trabajodea10.data.ItemVenta
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
    val tiendaId: String?
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
            tiendaId = o.texto("tienda_id")
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

    fun desdeJson(o: JsonObject): Tienda {
        val nec = o["necesidades"] as? JsonObject ?: JsonObject(emptyMap())
        fun n(clave: String, porDefecto: Boolean) = nec[clave]?.jsonPrimitive?.booleanOrNull ?: porDefecto
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
            necesidades = NecesidadesTienda(
                tieneStock = n("tieneStock", true),
                tieneAgendamiento = n("tieneAgendamiento", false),
                tieneVentaPorWhatsApp = n("tieneVentaPorWhatsApp", true),
                tieneProductosAPedido = n("tieneProductosAPedido", false),
                participaEnFerias = n("participaEnFerias", true),
                tieneForoComunidad = n("tieneForoComunidad", false)
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
        put("necesidades", buildJsonObject {
            put("tieneStock", t.necesidades.tieneStock)
            put("tieneAgendamiento", t.necesidades.tieneAgendamiento)
            put("tieneVentaPorWhatsApp", t.necesidades.tieneVentaPorWhatsApp)
            put("tieneProductosAPedido", t.necesidades.tieneProductosAPedido)
            put("participaEnFerias", t.necesidades.participaEnFerias)
            put("tieneForoComunidad", t.necesidades.tieneForoComunidad)
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

object ProductoRepositorio {

    fun desdeJson(o: JsonObject): Producto {
        val variantes = (o["variantes"] as? JsonArray)?.mapNotNull { elemento: JsonElement ->
            val v = elemento as? JsonObject ?: return@mapNotNull null
            Variante(
                id = v.textoO("id", "v1"),
                nombre = v.textoO("nombre", "Unidad"),
                stock = v["stock"]?.jsonPrimitive?.intOrNull ?: 0
            )
        } ?: emptyList()
        return Producto(
            id = o.textoO("id"),
            tiendaId = o.textoO("tienda_id"),
            nombre = o.textoO("nombre"),
            categoria = runCatching { Categoria.valueOf(o.textoO("categoria", "TEXTIL")) }
                .getOrDefault(Categoria.TEXTIL),
            precio = o.decimal("precio"),
            publicado = o.booleano("publicado"),
            destacado = o.booleano("destacado"),
            colorFoto = o.largo("color_foto") ?: 0xFF8E6B94,
            variantes = variantes.ifEmpty { listOf(Variante("${o.textoO("id")}v1", "Unidad", 0)) },
            fotoUrl = o.texto("foto_url"),
            descripcion = o.textoO("descripcion")
        )
    }

    private fun aJson(p: Producto): String = buildJsonObject {
        put("id", p.id)
        put("tienda_id", p.tiendaId)
        put("nombre", p.nombre)
        put("descripcion", p.descripcion)
        put("categoria", p.categoria.name)
        put("precio", p.precio)
        put("publicado", p.publicado)
        put("destacado", p.destacado)
        put("color_foto", p.colorFoto)
        put("variantes", buildJsonArray {
            p.variantes.forEach { v ->
                addJsonObject {
                    put("id", v.id)
                    put("nombre", v.nombre)
                    put("stock", v.stock)
                }
            }
        })
        // Solo se guardan URLs públicas; una ruta del celular no sirve en el servidor.
        put("foto_url", p.fotoUrl?.takeIf { it.startsWith("http") })
    }.toString()

    /** Productos visibles para el público (vitrina). */
    suspend fun publicados(): List<Producto> =
        arreglo(ClienteSupabase.pedir("GET", "/rest/v1/productos?select=*&publicado=eq.true")).map(::desdeJson)

    /** Todos los productos de una tienda (la dueña también ve los que solo ella ve). */
    suspend fun deTienda(tiendaId: String): List<Producto> =
        arreglo(ClienteSupabase.pedir("GET", "/rest/v1/productos?select=*&tienda_id=eq.${cod(tiendaId)}"))
            .map(::desdeJson)

    suspend fun guardar(p: Producto) {
        ClienteSupabase.pedir(
            "POST", "/rest/v1/productos?on_conflict=id",
            cuerpoJson = aJson(p), cabeceras = mapOf("Prefer" to PREFER_UPSERT)
        )
    }

    suspend fun eliminar(id: String) {
        ClienteSupabase.pedir("DELETE", "/rest/v1/productos?id=eq.${cod(id)}")
    }
}

// ===================================================================== Ventas

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
                fotoUrl = i.texto("fotoUrl")
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
            total = o.decimal("total")
        )
    }

    suspend fun historial(tiendaId: String): List<Venta> =
        arreglo(
            ClienteSupabase.pedir("GET", "/rest/v1/ventas?select=*&tienda_id=eq.${cod(tiendaId)}&order=fecha.desc")
        ).map(::desdeJson)

    suspend fun registrar(venta: Venta, tiendaId: String) {
        val cuerpo = buildJsonObject {
            put("id", venta.id)
            put("tienda_id", tiendaId)
            put("fecha_local", venta.fecha)
            put("medio_pago", venta.medioPago.name)
            put("feria_id", venta.feriaId)
            put("total", venta.total)
            put("items", buildJsonArray {
                venta.items.forEach { i ->
                    addJsonObject {
                        put("productoId", i.productoId)
                        put("varianteId", i.varianteId)
                        put("nombre", i.nombre)
                        put("detalle", i.detalle)
                        put("precio", i.precio)
                        put("cantidad", i.cantidad)
                        put("fotoUrl", i.fotoUrl?.takeIf { it.startsWith("http") })
                    }
                }
            })
        }.toString()
        ClienteSupabase.pedir(
            "POST", "/rest/v1/ventas?on_conflict=id",
            cuerpoJson = cuerpo, cabeceras = mapOf("Prefer" to PREFER_UPSERT)
        )
    }
}
