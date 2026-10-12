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
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.example.trabajodea10.ui.theme.Berenjena
import com.example.trabajodea10.ui.theme.BerenjenaOscuro
import com.example.trabajodea10.ui.theme.SuperficieVariante
import com.example.trabajodea10.ui.theme.TextoSecundario
import com.example.trabajodea10.ui.theme.VerdeOk
import kotlinx.coroutines.launch

@Composable
fun PantallaLogin(nav: ControladorNavegacion) {
    val coroutineScope = rememberCoroutineScope()
    var pestañaModo by remember { mutableIntStateOf(0) } // 0: Emprendedora, 1: Comprador
    var correo by remember { mutableStateOf("") }
    var clave by remember { mutableStateOf("") }
    var nombreRegistro by remember { mutableStateOf("") }
    var esModoRegistro by remember { mutableStateOf(false) }
    var cargando by remember { mutableStateOf(false) }
    var mensajeError by remember { mutableStateOf<String?>(null) }
    var mensajeExito by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        // Cabecera institucional elegante
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Berenjena)
                .padding(top = 48.dp, bottom = 32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Surface(
                    shape = CircleShape,
                    color = Color.White,
                    shadowElevation = 4.dp
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Store,
                            contentDescription = "Logo Alerce",
                            tint = Berenjena,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "Alerce Emprende",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "Punto de encuentro del comercio local en Puerto Montt",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.9f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
            }
        }

        // Selector claro de Modo: Emprendedora vs Comprador (RF-ACC-05)
        TabRow(
            selectedTabIndex = pestañaModo,
            containerColor = Color.White,
            contentColor = Berenjena,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[pestañaModo]),
                    color = Berenjena
                )
            }
        ) {
            Tab(
                selected = pestañaModo == 0,
                onClick = { pestañaModo = 0 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Store, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Soy Emprendedora", fontWeight = FontWeight.SemiBold)
                    }
                }
            )
            Tab(
                selected = pestañaModo == 1,
                onClick = { pestañaModo = 1 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.ShoppingBag, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Soy Comprador", fontWeight = FontWeight.SemiBold)
                    }
                }
            )
        }

        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Tarjeta con formulario de Firebase Auth
            Tarjeta {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = if (esModoRegistro) "Crear cuenta en Firebase" else "Iniciar Sesión con Firebase Auth",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = BerenjenaOscuro
                    )

                    Text(
                        text = if (pestañaModo == 0)
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
                            leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    OutlinedTextField(
                        value = correo,
                        onValueChange = { correo = it },
                        label = { Text("Correo electrónico") },
                        leadingIcon = { Icon(Icons.Filled.Email, contentDescription = null) },
                        placeholder = { Text("ejemplo@gmail.com") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = clave,
                        onValueChange = { clave = it },
                        label = { Text("Contraseña") },
                        leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier.fillMaxWidth()
                    )

                    mensajeError?.let {
                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.error,
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
                            cargando = true
                            mensajeError = null
                            coroutineScope.launch {
                                if (esModoRegistro) {
                                    val res = FirebaseManager.registrarUsuario(correo, clave, nombreRegistro)
                                    cargando = false
                                    if (res.isSuccess) {
                                        mensajeExito = "¡Cuenta creada en Firebase! Bienvenido."
                                        val rol = if (pestañaModo == 0) Rol.EMPRENDEDORA else Rol.VISITANTE
                                        nav.iniciarSesion(rol)
                                    } else {
                                        mensajeError = res.exceptionOrNull()?.localizedMessage ?: "Error al registrar"
                                    }
                                } else {
                                    val res = FirebaseManager.iniciarSesion(correo, clave)
                                    cargando = false
                                    if (res.isSuccess) {
                                        val rol = if (pestañaModo == 0) Rol.EMPRENDEDORA else Rol.VISITANTE
                                        nav.iniciarSesion(rol)
                                    } else {
                                        mensajeError = res.exceptionOrNull()?.localizedMessage ?: "Credenciales incorrectas"
                                    }
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Berenjena),
                        enabled = !cargando,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        if (cargando) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Text(
                                text = if (esModoRegistro) "Registrarme y Entrar" else "Iniciar Sesión",
                                fontWeight = FontWeight.Bold
                            )
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
                            color = Berenjena,
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
                HorizontalDivider(modifier = Modifier.weight(1f))
                Text(
                    text = "  O INGRESO RÁPIDO PARA DEMOSTRACIÓN  ",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextoSecundario,
                    fontWeight = FontWeight.SemiBold
                )
                HorizontalDivider(modifier = Modifier.weight(1f))
            }

            // Botones de acceso rápido para la reunión del miércoles (Garantía de fluidez total)
            Tarjeta(onClick = {
                FirebaseManager.entrarModoDemo("Doña Rosa (Minimarket Alerce)", Rol.EMPRENDEDORA)
                nav.iniciarSesion(Rol.EMPRENDEDORA)
            }) {
                FilaAccesoRapido(
                    titulo = "Ingresar como Emprendedora (Demo)",
                    subtitulo = "Entrar directo al Panel de Tienda y Selección de Necesidades.",
                    icono = Icons.Filled.Store
                )
            }

            Tarjeta(onClick = {
                FirebaseManager.entrarModoDemo("Vecino Comprador", Rol.VISITANTE)
                nav.iniciarSesion(Rol.VISITANTE)
            }) {
                FilaAccesoRapido(
                    titulo = "Explorar como Comprador (Marketplace)",
                    subtitulo = "Navegar las tiendas de Alerce leídas desde Firestore.",
                    icono = Icons.Filled.ShoppingBag
                )
            }

            OutlinedButton(
                onClick = { nav.iniciarSesion(Rol.ADMIN) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Acceso Administración de Ferias", color = TextoSecundario, fontSize = 13.sp)
            }
        }
    }
}

@Composable
private fun FilaAccesoRapido(
    titulo: String,
    subtitulo: String,
    icono: androidx.compose.ui.graphics.vector.ImageVector
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(shape = RoundedCornerShape(12.dp), color = SuperficieVariante) {
            Box(modifier = Modifier.padding(10.dp)) {
                Icon(icono, contentDescription = null, tint = Berenjena, modifier = Modifier.size(24.dp))
            }
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(titulo, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Text(subtitulo, style = MaterialTheme.typography.bodySmall, color = TextoSecundario)
        }
        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = BerenjenaOscuro)
    }
}

