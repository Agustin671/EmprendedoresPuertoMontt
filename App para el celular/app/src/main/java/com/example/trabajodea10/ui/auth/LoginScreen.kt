package com.example.trabajodea10.ui.auth

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
import com.example.trabajodea10.data.FirebaseManager
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

    LaunchedEffect(Unit) {
        val biometricManager = BiometricManager.from(contexto)
        puedeUsarBiometria = biometricManager.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL
        ) == BiometricManager.BIOMETRIC_SUCCESS
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
            // Tarjeta Principal de Autenticación Firebase
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
                            if (pestanaModo == 0) "Crear Tienda en Firebase" else "Crear Cuenta de Comprador"
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

                            val rolSeleccionado = if (pestanaModo == 0) Rol.EMPRENDEDORA else Rol.VISITANTE
                            cargando = true
                            mensajeError = null
                            mensajeExito = null

                            coroutineScope.launch {
                                if (esModoRegistro) {
                                    val res = FirebaseManager.registrarUsuario(
                                        correo = correo,
                                        clave = clave,
                                        nombre = nombreRegistro,
                                        rol = rolSeleccionado
                                    )
                                    cargando = false
                                    if (res.isSuccess) {
                                        mensajeExito = "¡Cuenta creada en Firebase! Bienvenido."
                                        nav.iniciarSesion(rolSeleccionado)
                                    } else {
                                        mensajeError = res.exceptionOrNull()?.localizedMessage ?: "Error al registrar en Firebase"
                                    }
                                } else {
                                    val res = FirebaseManager.iniciarSesion(correo, clave, rolFallback = rolSeleccionado)
                                    cargando = false
                                    if (res.isSuccess) {
                                        val (_, rolObtenido) = res.getOrThrow()
                                        nav.iniciarSesion(rolObtenido)
                                    } else {
                                        mensajeError = res.exceptionOrNull()?.localizedMessage ?: "Credenciales incorrectas"
                                    }
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

                    if (pestanaModo == 0 && puedeUsarBiometria) {
                        OutlinedButton(
                            onClick = {
                                activity?.let {
                                    autenticarConHuella(
                                        activity = it,
                                        onExito = { nav.iniciarSesion(Rol.EMPRENDEDORA) },
                                        onError = { err -> mensajeError = "Error de biometría: " + err },
                                        onCancelar = {}
                                    )
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

            // SECCIÓN DESARROLLADORES: TIENDAS DE PRUEBA Y ACCESOS RÁPIDOS
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
                                    FirebaseManager.entrarModoDesarrolladorPrueba("t_minimarket_rosa", Rol.EMPRENDEDORA)
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
                                        Text("Minimarket Doña Rosa", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = NegroApple)
                                        Text("Abarrotes y pan amasado con stock", fontSize = 10.5.sp, color = GrisTexto)
                                    }
                                }
                            }

                            OutlinedButton(
                                onClick = {
                                    FirebaseManager.entrarModoDesarrolladorPrueba("t_barberia_alerce", Rol.EMPRENDEDORA)
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
                                    FirebaseManager.entrarModoDesarrolladorPrueba("t_tejidos_austral", Rol.EMPRENDEDORA)
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
                                    FirebaseManager.entrarModoDesarrolladorPrueba("t_reposteria_surena", Rol.EMPRENDEDORA)
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
                                        FirebaseManager.entrarModoDesarrolladorPrueba("t_minimarket_rosa", Rol.VISITANTE)
                                        nav.iniciarSesion(Rol.VISITANTE)
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, GrisBorde),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Entrar Comprador", fontSize = 11.sp, color = NegroApple, fontWeight = FontWeight.Bold)
                                }

                                OutlinedButton(
                                    onClick = { nav.iniciarSesion(Rol.ADMIN) },
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, GrisBorde),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Admin Ferias", fontSize = 11.sp, color = NegroApple, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
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
                onError(errString.toString())
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

    val promptInfo = BiometricPrompt.PromptInfo.Builder()
        .setTitle("Verificación biométrica")
        .setSubtitle("Ingresa con tu huella digital")
        .setDescription("Usa tu huella para acceder de forma rápida y segura")
        .setAllowedAuthenticators(
            BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL
        )
        .build()

    biometricPrompt.authenticate(promptInfo)
}
