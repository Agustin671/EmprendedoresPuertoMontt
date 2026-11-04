package com.example.trabajodea10.data.remoto

import android.content.Context
import android.content.SharedPreferences
import com.example.trabajodea10.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.concurrent.TimeUnit

/** Datos de conexión. Vienen de supabase.properties vía BuildConfig (nunca escritos en el código). */
object ConfigSupabase {
    val url: String get() = BuildConfig.SUPABASE_URL.trim().trimEnd('/')
    val anonKey: String get() = BuildConfig.SUPABASE_ANON_KEY.trim()
    val configurado: Boolean get() = url.startsWith("https://") && anonKey.isNotBlank()
    const val BUCKET_FOTOS = "productos"
}

/** Error de la API con un mensaje ya traducido para mostrar a la usuaria. */
class ErrorSupabase(val codigo: Int, mensaje: String) : IOException(mensaje)

data class SesionSupabase(
    val accessToken: String,
    val refreshToken: String,
    val userId: String,
    val email: String
)

/**
 * Cliente mínimo de Supabase por HTTP: Auth (GoTrue), base de datos (PostgREST)
 * y Storage. Solo usa la anon key + el token de la sesión iniciada; los permisos
 * reales los aplican las reglas RLS del servidor (supabase/01_esquema.sql).
 */
object ClienteSupabase {

    private const val PREFS = "supabase_sesion"
    private val tipoJson = "application/json; charset=utf-8".toMediaType()

    val json = Json { ignoreUnknownKeys = true }

    private val http: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()
    }

    private var prefs: SharedPreferences? = null

    @Volatile
    var sesion: SesionSupabase? = null
        private set

    fun inicializar(context: Context) {
        if (prefs != null) return
        val p = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        prefs = p
        val access = p.getString("access", null)
        val refresh = p.getString("refresh", null)
        val uid = p.getString("uid", null)
        if (access != null && refresh != null && uid != null) {
            sesion = SesionSupabase(access, refresh, uid, p.getString("email", "") ?: "")
        }
    }

    /** Pequeño almacén de datos de la sesión (nombre, rol, huella activada). */
    fun guardarDato(clave: String, valor: String?) {
        prefs?.edit()?.apply { if (valor == null) remove(clave) else putString(clave, valor) }?.apply()
    }

    fun leerDato(clave: String): String? = prefs?.getString(clave, null)

    private fun guardarSesion(nueva: SesionSupabase?) {
        sesion = nueva
        prefs?.edit()?.apply {
            if (nueva == null) {
                remove("access"); remove("refresh"); remove("uid"); remove("email")
            } else {
                putString("access", nueva.accessToken)
                putString("refresh", nueva.refreshToken)
                putString("uid", nueva.userId)
                putString("email", nueva.email)
            }
        }?.apply()
    }

    // ---------------------------------------------------------------- HTTP

    private fun ejecutar(
        metodo: String,
        ruta: String,
        cuerpo: RequestBody?,
        cabeceras: Map<String, String>,
        conToken: Boolean
    ): Pair<Int, String> {
        val token = if (conToken) sesion?.accessToken ?: ConfigSupabase.anonKey else ConfigSupabase.anonKey
        val constructor = Request.Builder()
            .url(ConfigSupabase.url + ruta)
            .header("apikey", ConfigSupabase.anonKey)
            .header("Authorization", "Bearer $token")
            .header("Accept", "application/json")
        cabeceras.forEach { (clave, valor) -> constructor.header(clave, valor) }
        constructor.method(metodo, cuerpo)
        http.newCall(constructor.build()).execute().use { respuesta ->
            return respuesta.code to (respuesta.body?.string() ?: "")
        }
    }

    /**
     * Hace una petición y devuelve el texto de la respuesta. Si el token venció,
     * lo renueva una vez y reintenta. Lanza [ErrorSupabase] o IOException (sin red).
     */
    suspend fun pedir(
        metodo: String,
        ruta: String,
        cuerpoJson: String? = null,
        bytes: ByteArray? = null,
        tipoBytes: String = "image/jpeg",
        cabeceras: Map<String, String> = emptyMap(),
        conToken: Boolean = true
    ): String = withContext(Dispatchers.IO) {
        if (!ConfigSupabase.configurado) {
            throw ErrorSupabase(0, "La app no tiene configurada la conexión (supabase.properties).")
        }
        fun cuerpo(): RequestBody? = when {
            bytes != null -> bytes.toRequestBody(tipoBytes.toMediaType())
            cuerpoJson != null -> cuerpoJson.toRequestBody(tipoJson)
            metodo == "POST" || metodo == "PATCH" -> "{}".toRequestBody(tipoJson)
            else -> null
        }
        var resultado = ejecutar(metodo, ruta, cuerpo(), cabeceras, conToken)
        if (resultado.first == 401 && conToken && sesion != null && renovarSesionBloqueante()) {
            resultado = ejecutar(metodo, ruta, cuerpo(), cabeceras, conToken)
        }
        if (resultado.first !in 200..299) {
            throw ErrorSupabase(resultado.first, traducirError(resultado.first, resultado.second))
        }
        resultado.second
    }

    private fun traducirError(codigo: Int, texto: String): String {
        val original = runCatching {
            val o = json.parseToJsonElement(texto).jsonObject
            listOf("error_description", "msg", "message", "error")
                .firstNotNullOfOrNull { o[it]?.jsonPrimitive?.contentOrNull }
        }.getOrNull() ?: texto.take(160)
        val minus = original.lowercase()
        return when {
            "invalid login credentials" in minus -> "Correo o contraseña incorrectos."
            "already registered" in minus || "already been registered" in minus ->
                "Ese correo ya tiene cuenta. Usa «Iniciar sesión»."
            "password should be at least" in minus -> "La contraseña debe tener al menos 6 caracteres."
            "unable to validate email" in minus || "invalid email" in minus -> "Revisa que el correo esté bien escrito."
            "email not confirmed" in minus -> "Falta confirmar el correo."
            "row-level security" in minus || codigo == 403 -> "No tienes permiso para hacer este cambio."
            codigo >= 500 -> "El servidor no respondió bien. Intenta en un momento."
            else -> original
        }
    }

    // ---------------------------------------------------------------- Auth

    private fun leerSesion(texto: String, emailRespaldo: String): SesionSupabase {
        val o = json.parseToJsonElement(texto).jsonObject
        val access = o["access_token"]?.jsonPrimitive?.contentOrNull
            ?: throw ErrorSupabase(0, "La cuenta se creó, pero falta confirmarla por correo.")
        val refresh = o["refresh_token"]?.jsonPrimitive?.contentOrNull ?: ""
        val usuario = o["user"]?.jsonObject ?: JsonObject(emptyMap())
        val id = usuario["id"]?.jsonPrimitive?.contentOrNull
            ?: throw ErrorSupabase(0, "Respuesta de inicio de sesión incompleta.")
        val email = usuario["email"]?.jsonPrimitive?.contentOrNull ?: emailRespaldo
        return SesionSupabase(access, refresh, id, email)
    }

    suspend fun registrar(email: String, clave: String, nombre: String): SesionSupabase {
        val cuerpo = buildJsonObject {
            put("email", email.trim())
            put("password", clave)
            putJsonObject("data") { put("nombre", nombre.trim()) }
        }.toString()
        val texto = pedir("POST", "/auth/v1/signup", cuerpoJson = cuerpo, conToken = false)
        val nueva = leerSesion(texto, email.trim())
        guardarSesion(nueva)
        return nueva
    }

    suspend fun ingresar(email: String, clave: String): SesionSupabase {
        val cuerpo = buildJsonObject {
            put("email", email.trim())
            put("password", clave)
        }.toString()
        val texto = pedir("POST", "/auth/v1/token?grant_type=password", cuerpoJson = cuerpo, conToken = false)
        val nueva = leerSesion(texto, email.trim())
        guardarSesion(nueva)
        return nueva
    }

    /** Renueva el token con el refresh token guardado (se usa al entrar con huella). */
    suspend fun renovarSesion(): Boolean = withContext(Dispatchers.IO) { renovarSesionBloqueante() }

    private fun renovarSesionBloqueante(): Boolean {
        val actual = sesion ?: return false
        if (actual.refreshToken.isBlank()) return false
        return try {
            val cuerpo = buildJsonObject { put("refresh_token", actual.refreshToken) }.toString()
            val (codigo, texto) = ejecutar(
                "POST", "/auth/v1/token?grant_type=refresh_token",
                cuerpo.toRequestBody(tipoJson), emptyMap(), conToken = false
            )
            when {
                codigo in 200..299 -> {
                    guardarSesion(leerSesion(texto, actual.email)); true
                }
                codigo == 400 || codigo == 401 -> {
                    // El refresh token ya no sirve: hay que volver a entrar con contraseña.
                    guardarSesion(null); false
                }
                else -> false
            }
        } catch (e: Exception) {
            false
        }
    }

    /** Cierra la sesión en el servidor y borra los tokens del celular. */
    suspend fun cerrarSesion() {
        if (sesion != null) {
            runCatching { pedir("POST", "/auth/v1/logout") }
        }
        guardarSesion(null)
    }

    // ---------------------------------------------------------------- Storage

    /** Sube una foto (ya comprimida) y devuelve su URL pública. */
    suspend fun subirFoto(ruta: String, bytes: ByteArray): String {
        pedir(
            "POST",
            "/storage/v1/object/${ConfigSupabase.BUCKET_FOTOS}/$ruta",
            bytes = bytes,
            tipoBytes = "image/jpeg",
            cabeceras = mapOf("x-upsert" to "true")
        )
        return "${ConfigSupabase.url}/storage/v1/object/public/${ConfigSupabase.BUCKET_FOTOS}/$ruta"
    }
}
