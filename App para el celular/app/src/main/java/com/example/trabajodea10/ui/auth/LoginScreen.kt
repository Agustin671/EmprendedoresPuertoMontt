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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.trabajodea10.data.FirebaseManager
import com.example.trabajodea10.data.Rol
import com.example.trabajodea10.data.TramaStore
import com.example.trabajodea10.ui.componentes.Tarjeta
import com.example.trabajodea10.ui.navegacion.ControladorNavegacion
import com.example.trabajodea10.ui.theme.BlancoPuro
import android.widget.Toast
import androidx.compose.foundation.text.ClickableText
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextDecoration
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
import com.example.trabajodea10.ui.theme.RojoAlerceContenedor
import com.example.trabajodea10.ui.theme.SuperficieBlanca
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
        // Cabecera Minimalista Editorial (Inspirada en Farfetch & Apple)
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

        // Selector Segmentado Estilo iOS / Farfetch (Pills)
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
            // Tarjeta con formulario de Firebase Auth
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
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = if (esModoRegistro) "Crear cuenta en Firebase" else "Iniciar Sesión",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = NegroApple
                    )

                    Text(
                        text = if (pestanaModo == 0)
                            "Ingresa con tus credenciales de la asociación para administrar tu tienda."
                        else
                            "Ingresa para seguir tus tiendas favoritas y recibir avisos de ferias.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextoSecundario
                    )

                    if (esModoRegistro) {
                        OutlinedTextField(
                            value = nombreRegistro,
                            onValueChange = { nombreRegistro = it },
                            label = { Text("Nombre o Nombre de la Tienda") },
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
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = if (it.contains("CONFIGURATION_NOT_FOUND"))
                                    "Nota de Firebase: Se activó 'Correo/contraseña' en la consola. Si aún propaga, puedes entrar con tu nombre abajo:"
                                else it,
                                color = RojoAlerce,
                                style = MaterialTheme.typography.bodySmall
                            )
                            OutlinedButton(
                                onClick = {
                                    val nombre = nombreRegistro.ifBlank { correo.substringBefore("@").ifBlank { "Paul Ferrada" } }
                                    val rol = if (pestanaModo == 0) Rol.EMPRENDEDORA else Rol.VISITANTE
                                    FirebaseManager.entrarModoDemo(nombre, rol)
                                    nav.iniciarSesion(rol)
                                },
                                shape = RoundedCornerShape(50.dp),
                                border = BorderStroke(1.dp, NegroApple),
                                modifier = Modifier.fillMaxWidth().height(42.dp)
                            ) {
                                Text(
                                    text = "Continuar como ${nombreRegistro.ifBlank { correo.substringBefore("@").ifBlank { "Paul" } }} (Acceso Rápido)",
                                    color = NegroApple,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
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
                            cargando = true
                            mensajeError = null
                            coroutineScope.launch {
                                if (esModoRegistro) {
                                    val res = FirebaseManager.registrarUsuario(correo, clave, nombreRegistro)
                                    cargando = false
                                    if (res.isSuccess) {
                                        mensajeExito = "¡Cuenta creada en Firebase! Bienvenido."
                                        val rol = if (pestanaModo == 0) Rol.EMPRENDEDORA else Rol.VISITANTE
                                        nav.iniciarSesion(rol)
                                    } else {
                                        mensajeError = res.exceptionOrNull()?.localizedMessage ?: "Error al registrar"
                                    }
                                } else {
                                    val res = FirebaseManager.iniciarSesion(correo, clave)
                                    cargando = false
                                    if (res.isSuccess) {
                                        val rol = if (pestanaModo == 0) Rol.EMPRENDEDORA else Rol.VISITANTE
                                        nav.iniciarSesion(rol)
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
                                text = if (esModoRegistro) "Registrarme y Entrar" else "Iniciar Sesión",
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
                            modifier = Modifier.fillMaxWidth().height(50.dp)
                        ) {
                            Text("Ingresar con huella digital", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = NegroApple)
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

            // Separador con texto
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f), color = GrisBorde)
                Text(
                    text = "  O ACCESO RÁPIDO PARA DEMOSTRACIÓN  ",
                    style = MaterialTheme.typography.labelSmall,
                    color = GrisTexto,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                HorizontalDivider(modifier = Modifier.weight(1f), color = GrisBorde)
            }

            // Botones de acceso rápido estilo Apple Cards
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = BlancoPuro,
                border = BorderStroke(1.dp, GrisBorde),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .clickable {
                        FirebaseManager.entrarModoDemo("Doña Rosa", Rol.EMPRENDEDORA)
                        nav.iniciarSesion(Rol.EMPRENDEDORA)
                    }
            ) {
                FilaAccesoRapidoEditorial(
                    badge = "DEMO EMPRENDEDORA",
                    badgeColor = RojoAlerceContenedor,
                    badgeTextColor = RojoAlerce,
                    titulo = "Ingresar como Doña Rosa",
                    subtitulo = "Minimarket Alerce · Selección de necesidades en vivo.",
                    icono = Icons.Filled.Store
                )
            }

            Surface(
                shape = RoundedCornerShape(18.dp),
                color = BlancoPuro,
                border = BorderStroke(1.dp, GrisBorde),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .clickable {
                        FirebaseManager.entrarModoDemo("Paul", Rol.VISITANTE)
                        nav.iniciarSesion(Rol.VISITANTE)
                    }
            ) {
                FilaAccesoRapidoEditorial(
                    badge = "EXPLORADOR / COMPRADOR",
                    badgeColor = GrisChip,
                    badgeTextColor = NegroApple,
                    titulo = "Explorar como Comprador",
                    subtitulo = "Ver vitrina de tiendas leídas de Firestore y WhatsApp.",
                    icono = Icons.Filled.ShoppingBag
                )
            }

            OutlinedButton(
                onClick = { nav.iniciarSesion(Rol.ADMIN) },
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, GrisBorde),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
            ) {
                Text("Acceso Administración de Ferias", color = GrisTexto, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
private fun FilaAccesoRapidoEditorial(
    badge: String,
    badgeColor: Color,
    badgeTextColor: Color,
    titulo: String,
    subtitulo: String,
    icono: androidx.compose.ui.graphics.vector.ImageVector
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(shape = RoundedCornerShape(12.dp), color = GrisChip) {
            Box(modifier = Modifier.padding(12.dp)) {
                Icon(icono, contentDescription = null, tint = NegroApple, modifier = Modifier.size(24.dp))
            }
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Surface(shape = RoundedCornerShape(4.dp), color = badgeColor) {
                Text(
                    text = badge,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.5.sp,
                    color = badgeTextColor,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(titulo, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = NegroApple)
            Text(subtitulo, style = MaterialTheme.typography.bodySmall, color = GrisTexto)
        }
        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = NegroApple.copy(alpha = 0.5f))
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
        .setSubtitle("Ingresa al modo emprendedora")
        .setDescription("Usa tu huella o credencial del dispositivo para continuar")
        .setAllowedAuthenticators(
            BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL
        )
        .build()

    biometricPrompt.authenticate(promptInfo)
}


