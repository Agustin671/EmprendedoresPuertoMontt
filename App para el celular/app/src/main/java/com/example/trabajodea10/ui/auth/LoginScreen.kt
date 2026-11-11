package com.example.trabajodea10.ui.auth

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.trabajodea10.BuildConfig
import com.example.trabajodea10.data.SesionManager
import com.example.trabajodea10.data.Rol
import com.example.trabajodea10.ui.navegacion.ControladorNavegacion
import com.example.trabajodea10.ui.theme.BlancoPuro
import androidx.compose.ui.platform.LocalContext
import androidx.fragment.app.FragmentActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import java.util.concurrent.Executor
import com.example.trabajodea10.ui.theme.FondoLimpio
import com.example.trabajodea10.ui.theme.GrisBorde
import com.example.trabajodea10.ui.theme.GrisChip
import com.example.trabajodea10.ui.theme.GrisTexto
import com.example.trabajodea10.ui.theme.NegroApple
import com.example.trabajodea10.ui.theme.RojoAlerce
import com.example.trabajodea10.ui.theme.TextoSecundario
import com.example.trabajodea10.ui.theme.VerdeOk
import kotlinx.coroutines.launch

@Composable
fun PantallaLogin(nav: ControladorNavegacion) {
    val coroutineScope = rememberCoroutineScope()
    val activity = LocalContext.current as? FragmentActivity
    val contexto = LocalContext.current
    var pestanaModo by remember { mutableIntStateOf(0) } // 0: Emprendedora, 1: Comprador
    var correo by remember { mutableStateOf("") }
    var clave by remember { mutableStateOf("") }
    var nombreRegistro by remember { mutableStateOf("") }
    var esModoRegistro by remember { mutableStateOf(false) }
    var cargando by remember { mutableStateOf(false) }
    var mensajeError by remember { mutableStateOf<String?>(null) }
    var mensajeExito by remember { mutableStateOf<String?>(null) }
    var puedeUsarBiometria by remember { mutableStateOf(false) }
    var mostrarDevTools by remember { mutableStateOf(false) }
    // Después de entrar con contraseña: ofrecer activar la huella para la próxima vez
    var rolPorConfirmarHuella by remember { mutableStateOf<Rol?>(null) }
    var huellaDisponible by remember { mutableStateOf(SesionManager.huellaDisponible()) }
    // Recuperación de contraseña y fotos opcionales del registro
    var mostrarRecuperacion by remember { mutableStateOf(false) }
    var correoRecuperacion by remember { mutableStateOf("") }
    var enviandoRecuperacion by remember { mutableStateOf(false) }
    var fotoPerfilUri by remember { mutableStateOf<Uri?>(null) }
    var logoNegocioUri by remember { mutableStateOf<Uri?>(null) }

    val selectorFotoPerfil = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        fotoPerfilUri = uri
    }
    val selectorLogo = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        logoNegocioUri = uri
    }

    if (mostrarRecuperacion) {
        AlertDialog(
            onDismissRequest = { if (!enviandoRecuperacion) mostrarRecuperacion = false },
            title = { Text("Recuperar contraseña", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "Escribe el correo con el que te registraste. Te enviaremos un enlace para crear una contraseña nueva.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextoSecundario
                    )
                    OutlinedTextField(
                        value = correoRecuperacion,
                        onValueChange = { correoRecuperacion = it },
                        label = { Text("Correo electrónico") },
                        leadingIcon = { Icon(Icons.Filled.Email, contentDescription = null, tint = GrisTexto) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        "Revisa también la carpeta de spam.",
                        style = MaterialTheme.typography.bodySmall,
                        color = GrisTexto
                    )
                }
            },
            confirmButton = {
                TextButton(
                    enabled = !enviandoRecuperacion,
                    onClick = {
                        enviandoRecuperacion = true
                        coroutineScope.launch {
                            val res = SesionManager.recuperarContrasena(correoRecuperacion)
                            enviandoRecuperacion = false
                            res.onSuccess {
                                mostrarRecuperacion = false
                                mensajeExito = "Si el correo está registrado, recibirás un enlace para crear una contraseña nueva. Revisa tu correo y la carpeta de spam."
                            }.onFailure { mensajeError = it.message }
                        }
                    }
                ) {
                    Text(if (enviandoRecuperacion) "Enviando..." else "Enviar enlace", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarRecuperacion = false }) { Text("Cancelar") }
            }
        )
    }

    rolPorConfirmarHuella?.let { rolObtenido ->
        AlertDialog(
            onDismissRequest = {
                SesionManager.marcarHuellaOfrecida()
                rolPorConfirmarHuella = null
                nav.iniciarSesion(rolObtenido)
            },
            icon = { Icon(Icons.Filled.Fingerprint, contentDescription = null) },
            title = { Text("¿Entrar con tu huella la próxima vez?", fontWeight = FontWeight.Bold) },
            text = { Text("Así no tienes que escribir tu correo y contraseña cada vez. Puedes desactivarlo cuando quieras.") },
            confirmButton = {
                TextButton(onClick = {
                    SesionManager.activarHuella(true)
                    rolPorConfirmarHuella = null
                    nav.iniciarSesion(rolObtenido)
                }) { Text("Sí, usar huella", fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = {
                    SesionManager.marcarHuellaOfrecida()
                    rolPorConfirmarHuella = null
                    nav.iniciarSesion(rolObtenido)
                }) { Text("Ahora no") }
            }
        )
    }

    LaunchedEffect(Unit) {
        val biometricManager = BiometricManager.from(contexto)
        puedeUsarBiometria = biometricManager.canAuthenticate(autenticadoresHuella()) ==
            BiometricManager.BIOMETRIC_SUCCESS
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FondoLimpio)
            .verticalScroll(rememberScrollState())
    ) {
        // Cabecera Editorial Limpia (Estilo Farfetch / Apple)
        Surface(
            color = BlancoPuro,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 44.dp, bottom = 22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "ALERCE",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 4.sp,
                        color = NegroApple
                    )
                    Box(
                        modifier = Modifier
                            .padding(bottom = 6.dp, start = 3.dp)
                            .size(7.dp)
                            .background(RojoAlerce, CircleShape)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "MERCADO LOCAL & ARTESANÍA",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    color = NegroApple.copy(alpha = 0.85f)
                )
                Text(
                    text = "Puerto Montt · Región de Los Lagos",
                    fontSize = 12.sp,
                    color = GrisTexto
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Selector Segmentado de Rol (Emprendedora / Comprador)
        Box(
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .fillMaxWidth()
                .background(GrisChip, RoundedCornerShape(50.dp))
                .padding(4.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(50.dp))
                        .background(if (pestanaModo == 0) NegroApple else Color.Transparent)
                        .clickable { pestanaModo = 0 }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.Store,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = if (pestanaModo == 0) BlancoPuro else GrisTexto
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "Soy Emprendedora",
                            fontWeight = if (pestanaModo == 0) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 13.sp,
                            color = if (pestanaModo == 0) BlancoPuro else GrisTexto
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(50.dp))
                        .background(if (pestanaModo == 1) NegroApple else Color.Transparent)
                        .clickable { pestanaModo = 1 }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.ShoppingBag,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = if (pestanaModo == 1) BlancoPuro else GrisTexto
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "Soy Comprador",
                            fontWeight = if (pestanaModo == 1) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 13.sp,
                            color = if (pestanaModo == 1) BlancoPuro else GrisTexto
                        )
                    }
                }
            }
        }

        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Tarjeta principal de acceso (Supabase Auth)
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = BlancoPuro,
                border = BorderStroke(1.dp, GrisBorde),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = if (esModoRegistro) {
                            if (pestanaModo == 0) "Activar mi cuenta de emprendedora" else "Crear cuenta de comprador"
                        } else {
                            if (pestanaModo == 0) "Iniciar Sesión · Emprendedora" else "Iniciar Sesión · Comprador"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = NegroApple
                    )

                    Text(
                        text = if (pestanaModo == 0)
                            "Accede a tu panel para publicar productos, crear afiches comerciales y gestionar tus pedidos online."
                        else
                            "Conéctate para comprar con carrito directo, seguir tus tiendas favoritas y recibir avisos de ferias.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextoSecundario
                    )

                    // Credenciales de prueba: solo en el APK de depuración, nunca en producción.
                    if (BuildConfig.DEBUG && pestanaModo == 0 && !esModoRegistro) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFF8F9FA),
                            border = BorderStroke(1.dp, GrisBorde),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Tienda Creada para Revisión",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.5.sp,
                                        color = NegroApple
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(50.dp),
                                        color = NegroApple
                                    ) {
                                        Text(
                                            text = "Doña Marta",
                                            color = BlancoPuro,
                                            fontSize = 9.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Correo: donamarta@alerce.app\nClave: alerce123",
                                    fontSize = 11.sp,
                                    color = TextoSecundario,
                                    lineHeight = 16.sp
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                OutlinedButton(
                                    onClick = {
                                        correo = "donamarta@alerce.app"
                                        clave = "alerce123"
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, NegroApple),
                                    modifier = Modifier.fillMaxWidth().height(32.dp)
                                ) {
                                    Text("Usar credenciales de Doña Marta", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NegroApple)
                                }
                            }
                        }
                    }

                    if (esModoRegistro) {
                        OutlinedTextField(
                            value = nombreRegistro,
                            onValueChange = { nombreRegistro = it },
                            label = { Text(if (pestanaModo == 0) "Nombre de tu Emprendimiento" else "Tu Nombre Completo") },
                            leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null, tint = GrisTexto) },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedButton(
                            onClick = { selectorFotoPerfil.launch("image/*") },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().height(48.dp)
                        ) {
                            Text(
                                text = if (fotoPerfilUri == null) "Elegir foto de perfil (opcional)" else "Foto de perfil elegida",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp,
                                color = NegroApple
                            )
                        }

                        if (pestanaModo == 0) {
                            OutlinedButton(
                                onClick = { selectorLogo.launch("image/*") },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth().height(48.dp)
                            ) {
                                Text(
                                    text = if (logoNegocioUri == null) "Elegir logo del negocio (opcional)" else "Logo del negocio elegido",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp,
                                    color = NegroApple
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = correo,
                        onValueChange = { correo = it },
                        label = { Text("Correo electrónico") },
                        leadingIcon = { Icon(Icons.Filled.Email, contentDescription = null, tint = GrisTexto) },
                        placeholder = { Text("ejemplo@gmail.com") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = clave,
                        onValueChange = { clave = it },
                        label = { Text("Contraseña") },
                        leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null, tint = GrisTexto) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (!esModoRegistro) {
                        TextButton(
                            onClick = {
                                correoRecuperacion = correo
                                mostrarRecuperacion = true
                                mensajeError = null
                                mensajeExito = null
                            },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text(
                                "¿Olvidaste tu contraseña?",
                                color = NegroApple,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp
                            )
                        }
                    }

                    mensajeError?.let {
                        Text(
                            text = it,
                            color = RojoAlerce,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    mensajeExito?.let {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = VerdeOk, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = it, color = VerdeOk, style = MaterialTheme.typography.bodySmall)
                        }
                    }

                    Button(
                        onClick = {
                            if (correo.isBlank() || clave.isBlank()) {
                                mensajeError = "Por favor ingresa correo y contraseña."
                                return@Button
                            }
                            if (esModoRegistro && nombreRegistro.isBlank()) {
                                mensajeError = if (pestanaModo == 0)
                                    "Por favor ingresa el nombre de tu emprendimiento."
                                else
                                    "Por favor ingresa tu nombre."
                                return@Button
                            }

                            val esPestanaEmprendedora = pestanaModo == 0
                            cargando = true
                            mensajeError = null
                            mensajeExito = null

                            coroutineScope.launch {
                                // El rol lo decide el servidor (RN-01), no la pestaña elegida.
                                val res = if (esModoRegistro) {
                                    SesionManager.registrarUsuario(
                                        correo = correo,
                                        clave = clave,
                                        nombre = nombreRegistro,
                                        crearTienda = esPestanaEmprendedora
                                    )
                                } else {
                                    SesionManager.iniciarSesion(correo, clave)
                                }
                                cargando = false
                                res.onSuccess { rolObtenido ->
                                    // Fotos elegidas en el registro (opcionales). Si algo falla,
                                    // la cuenta ya quedó creada y no se bloquea el ingreso.
                                    if (esModoRegistro) {
                                        fotoPerfilUri?.let { uri ->
                                            runCatching { SesionManager.subirFotoPerfil(contexto, uri) }
                                                .onFailure { Toast.makeText(contexto, "La cuenta se creó, pero no se pudo subir la foto de perfil.", Toast.LENGTH_LONG).show() }
                                        }
                                        if (esPestanaEmprendedora && rolObtenido == Rol.EMPRENDEDORA) {
                                            logoNegocioUri?.let { uri ->
                                                runCatching { SesionManager.subirLogo(contexto, uri) }
                                                    .onFailure { Toast.makeText(contexto, "La cuenta se creó, pero no se pudo subir el logo.", Toast.LENGTH_LONG).show() }
                                            }
                                        }
                                    }
                                    if (esPestanaEmprendedora && rolObtenido == Rol.VISITANTE) {
                                        // RN-01: solo el administrador autoriza emprendedoras.
                                        Toast.makeText(
                                            contexto,
                                            "Tu cuenta quedó como compradora. Actualiza Supabase con 04_registro_vendedores.sql y vuelve a iniciar sesión para activar tu tienda.",
                                            Toast.LENGTH_LONG
                                        ).show()
                                        }
                                    val ofrecerHuella = puedeUsarBiometria &&
                                        !SesionManager.huellaActivada() &&
                                        !SesionManager.huellaYaOfrecida()
                                    if (ofrecerHuella) {
                                        rolPorConfirmarHuella = rolObtenido
                                    } else {
                                        nav.iniciarSesion(rolObtenido)
                                    }
                                }.onFailure { e ->
                                    mensajeError = e.message ?: "No se pudo entrar. Revisa tu conexión."
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NegroApple),
                        shape = RoundedCornerShape(14.dp),
                        enabled = !cargando,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        if (cargando) {
                            CircularProgressIndicator(color = BlancoPuro, modifier = Modifier.size(22.dp))
                        } else {
                            Text(
                                text = if (esModoRegistro) "Crear cuenta y entrar" else "Iniciar Sesión",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = BlancoPuro
                            )
                        }
                    }

                    if (puedeUsarBiometria && huellaDisponible) {
                        OutlinedButton(
                            onClick = {
                                if (activity == null) {
                                    mensajeError = "La huella no está disponible en este momento. Ingresa con tu correo."
                                } else {
                                    try {
                                        autenticarConHuella(
                                            activity = activity,
                                            onExito = {
                                                // La huella la valida Android; la app no guarda datos biométricos (RNF-SEG-02).
                                                cargando = true
                                                coroutineScope.launch {
                                                    val res = SesionManager.ingresarConHuella()
                                                    cargando = false
                                                    res.onSuccess { nav.iniciarSesion(it) }
                                                        .onFailure { e ->
                                                            mensajeError = e.message
                                                            huellaDisponible = SesionManager.huellaDisponible()
                                                        }
                                                }
                                            },
                                            onError = { err -> mensajeError = "No se pudo leer la huella: " + err },
                                            onCancelar = {}
                                        )
                                    } catch (e: Exception) {
                                        mensajeError = "La huella no está disponible en este celular. Ingresa con tu correo."
                                    }
                                }
                            },
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, NegroApple),
                            modifier = Modifier.fillMaxWidth().height(48.dp)
                        ) {
                            Icon(Icons.Filled.Fingerprint, contentDescription = null, tint = NegroApple, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Ingresar con huella digital", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = NegroApple)
                        }
                        TextButton(
                            onClick = {
                                SesionManager.activarHuella(false)
                                huellaDisponible = false
                                mensajeExito = "Listo, ya no se usará la huella. Entra con tu correo y contraseña."
                            },
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        ) {
                            Text("Dejar de usar la huella", color = GrisTexto, fontSize = 13.sp)
                        }
                    }

                    TextButton(
                        onClick = {
                            esModoRegistro = !esModoRegistro
                            mensajeError = null
                        },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text(
                            text = if (esModoRegistro) "¿Ya tienes cuenta? Inicia sesión aquí" else "¿No tienes cuenta? Regístrate aquí",
                            color = NegroApple,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            // SECCIÓN DESARROLLADORES: solo en el APK de pruebas (debug), nunca en el de producción
            if (BuildConfig.DEBUG) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = BlancoPuro,
                    border = BorderStroke(1.dp, GrisBorde),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { mostrarDevTools = !mostrarDevTools },
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.Settings, contentDescription = null, tint = GrisTexto, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Tiendas de prueba (Modo Desarrollador)",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GrisTexto
                                )
                            }
                            Text(
                                text = if (mostrarDevTools) "▲ Ocultar" else "▼ Abrir",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = GrisTexto
                            )
                        }

                        if (mostrarDevTools) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Acceso directo para pruebas de desarrollo con las tiendas creadas de Alerce:",
                                fontSize = 11.5.sp,
                                color = TextoSecundario
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            // Botones para las 4 tiendas de prueba
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(
                                    onClick = {
                                        SesionManager.entrarModoDesarrolladorPrueba("t_minimarket_rosa", Rol.EMPRENDEDORA)
                                        nav.iniciarSesion(Rol.EMPRENDEDORA)
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, GrisBorde),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                                        Text("🥖", fontSize = 16.sp)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text("Minimarket Doña Marta", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = NegroApple)
                                            Text("Abarrotes y pan amasado con stock", fontSize = 10.5.sp, color = GrisTexto)
                                        }
                                    }
                                }

                                OutlinedButton(
                                    onClick = {
                                        SesionManager.entrarModoDesarrolladorPrueba("t_barberia_alerce", Rol.EMPRENDEDORA)
                                        nav.iniciarSesion(Rol.EMPRENDEDORA)
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, GrisBorde),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                                        Text("💈", fontSize = 16.sp)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text("Barbería & Estilo Alerce", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = NegroApple)
                                            Text("Servicios con agendamiento de horas", fontSize = 10.5.sp, color = GrisTexto)
                                        }
                                    }
                                }

                                OutlinedButton(
                                    onClick = {
                                        SesionManager.entrarModoDesarrolladorPrueba("t_tejidos_austral", Rol.EMPRENDEDORA)
                                        nav.iniciarSesion(Rol.EMPRENDEDORA)
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, GrisBorde),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                                        Text("🧶", fontSize = 16.sp)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text("Tejidos Alerce Austral", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = NegroApple)
                                            Text("Lanares y artesanía a pedido", fontSize = 10.5.sp, color = GrisTexto)
                                        }
                                    }
                                }

                                OutlinedButton(
                                    onClick = {
                                        SesionManager.entrarModoDesarrolladorPrueba("t_reposteria_surena", Rol.EMPRENDEDORA)
                                        nav.iniciarSesion(Rol.EMPRENDEDORA)
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, GrisBorde),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                                        Text("🍰", fontSize = 16.sp)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text("Repostería Tradición Sureña", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = NegroApple)
                                            Text("Kuchenes y pasteles a pedido", fontSize = 10.5.sp, color = GrisTexto)
                                        }
                                    }
                                }

                                HorizontalDivider(color = GrisBorde, modifier = Modifier.padding(vertical = 4.dp))

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                    OutlinedButton(
                                        onClick = {
                                            SesionManager.entrarModoDesarrolladorPrueba("t_minimarket_rosa", Rol.VISITANTE)
                                            nav.iniciarSesion(Rol.VISITANTE)
                                        },
                                        shape = RoundedCornerShape(10.dp),
                                        border = BorderStroke(1.dp, GrisBorde),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Entrar Comprador", fontSize = 11.sp, color = NegroApple, fontWeight = FontWeight.Bold)
                                    }

                                    OutlinedButton(
                                        onClick = { Toast.makeText(contexto, "Acceso exclusivo Web: La administración de plataforma opera solo vía web (Requisito W03).", Toast.LENGTH_LONG).show() },
                                        shape = RoundedCornerShape(10.dp),
                                        border = BorderStroke(1.dp, GrisBorde),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Admin (Solo Web W03)", fontSize = 10.sp, color = NegroApple, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * En Android 11+ se permite huella o el PIN del celular. En Android 9 y 10 esa
 * combinación no está soportada (lanza un error), así que se usa solo huella.
 */
fun autenticadoresHuella(): Int =
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
        BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL
    } else {
        BiometricManager.Authenticators.BIOMETRIC_WEAK
    }

fun autenticarConHuella(
    activity: FragmentActivity,
    onExito: () -> Unit,
    onError: (String) -> Unit,
    onCancelar: () -> Unit
) {
    val executor: Executor = ContextCompat.getMainExecutor(activity)
    val biometricPrompt = BiometricPrompt(activity, executor,
        object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                val cancelado = errorCode == BiometricPrompt.ERROR_USER_CANCELED ||
                    errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON ||
                    errorCode == BiometricPrompt.ERROR_CANCELED
                if (!cancelado) onError(errString.toString())
                onCancelar()
            }

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                onExito()
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                onError("Huella no reconocida. Intenta nuevamente.")
            }
        })

    val autenticadores = autenticadoresHuella()
    val constructor = BiometricPrompt.PromptInfo.Builder()
        .setTitle("Ingresar con huella")
        .setSubtitle("Pon tu dedo en el lector del celular")
        .setAllowedAuthenticators(autenticadores)
    // Sin el PIN del celular como alternativa, Android exige un botón para cancelar.
    if ((autenticadores and BiometricManager.Authenticators.DEVICE_CREDENTIAL) == 0) {
        constructor.setNegativeButtonText("Cancelar")
    }
    val promptInfo = constructor.build()

    biometricPrompt.authenticate(promptInfo)
}
