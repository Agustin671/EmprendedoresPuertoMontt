package com.example.trabajodea10.ui.ajustes

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.biometric.BiometricManager
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import com.example.trabajodea10.data.SesionManager
import com.example.trabajodea10.data.Rol
import com.example.trabajodea10.data.TramaStore
import com.example.trabajodea10.ui.auth.autenticarConHuella
import com.example.trabajodea10.ui.auth.autenticadoresHuella
import com.example.trabajodea10.ui.componentes.AvatarUsuario
import com.example.trabajodea10.ui.componentes.BarraSuperior
import com.example.trabajodea10.ui.componentes.BotonSecundario
import com.example.trabajodea10.ui.componentes.BotonesTamanoTexto
import com.example.trabajodea10.ui.componentes.ChipEstado
import com.example.trabajodea10.ui.componentes.DatoFila
import com.example.trabajodea10.ui.componentes.FilaSwitch
import com.example.trabajodea10.ui.componentes.LineaSeparadora
import com.example.trabajodea10.ui.componentes.Tarjeta
import com.example.trabajodea10.ui.componentes.TituloSeccion
import com.example.trabajodea10.ui.componentes.colorDeCuenta
import com.example.trabajodea10.ui.navegacion.ControladorNavegacion
import com.example.trabajodea10.ui.navegacion.Ruta
import com.example.trabajodea10.ui.theme.LineaSuave
import com.example.trabajodea10.ui.theme.NegroApple
import com.example.trabajodea10.ui.theme.RojoError
import com.example.trabajodea10.ui.theme.SuperficieVariante
import com.example.trabajodea10.ui.theme.TextoSecundario
import kotlinx.coroutines.launch

@Composable
fun PantallaAjustes(
    nav: ControladorNavegacion,
    avisar: (String) -> Unit
) {
    val rol = TramaStore.rol
    val puedeVolver = nav.pila.size > 1
    val contextoHuella = LocalContext.current
    val actividadHuella = contextoHuella as? FragmentActivity
    var huellaActiva by remember { mutableStateOf(SesionManager.huellaActivada()) }
    val biometriaDisponible = remember {
        BiometricManager.from(contextoHuella).canAuthenticate(autenticadoresHuella()) ==
            BiometricManager.BIOMETRIC_SUCCESS
    }

    Column(modifier = Modifier.fillMaxSize()) {
        BarraSuperior(
            titulo = "Ajustes",
            subtitulo = when (rol) {
                Rol.ADMIN -> "Configuración central"
                Rol.EMPRENDEDORA -> "Perfil, tienda y notificaciones"
                Rol.VISITANTE -> "Perfil y preferencias"
                null -> ""
            },
            onVolver = if (puedeVolver) ({ nav.volver() }) else null
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            when (rol) {
                Rol.ADMIN -> PerfilAdmin(avisar)
                Rol.EMPRENDEDORA -> PerfilEmprendedora(nav, avisar)
                Rol.VISITANTE -> PerfilVisitante(nav, avisar)
                null -> Unit
            }

            // Seguridad: huella disponible para todas las personas con cuenta
            TituloSeccion(texto = "Seguridad y acceso")
            Tarjeta {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    FilaSwitch(
                        titulo = "Ingresar con huella",
                        descripcion = "Usa el lector de huella o biometría del celular para iniciar sesión rápido",
                        checked = huellaActiva,
                        onCheckedChange = { activado ->
                            if (!activado) {
                                SesionManager.activarHuella(false)
                                huellaActiva = false
                                avisar("Huella desactivada.")
                            } else if (SesionManager.emailActual == null) {
                                huellaActiva = false
                                avisar("Inicia sesión con tu correo y contraseña para activar la huella.")
                            } else if (!biometriaDisponible || actividadHuella == null) {
                                huellaActiva = false
                                avisar("Este celular no tiene huella o biometría disponible.")
                            } else {
                                autenticarConHuella(
                                    activity = actividadHuella,
                                    onExito = {
                                        SesionManager.activarHuella(true)
                                        huellaActiva = true
                                        avisar("Huella activada para los próximos inicios de sesión.")
                                    },
                                    onError = { error ->
                                        huellaActiva = SesionManager.huellaActivada()
                                        avisar(error)
                                    },
                                    onCancelar = {
                                        huellaActiva = SesionManager.huellaActivada()
                                    }
                                )
                            }
                        }
                    )
                }
            }

            if (rol == Rol.ADMIN) {
                AjustesSistema()
            } else if (rol == Rol.EMPRENDEDORA) {
                AjustesNotificaciones()
            }

            // Accesibilidad: disponible para todos los perfiles.
            AjustesAccesibilidad()

            BotonSecundario(
                texto = "Cerrar sesión",
                onClick = { nav.cerrarSesion() },
                icono = { Icon(Icons.Filled.Logout, contentDescription = null, tint = RojoError) },
                modifier = Modifier.padding(top = 4.dp)
            )

            Text(
                text = "Alerce Emprende · versión 1.0.0 (Oficial Online)",
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                style = MaterialTheme.typography.bodySmall,
                color = TextoSecundario,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
private fun PerfilAdmin(avisar: (String) -> Unit) {
    val contexto = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var subiendoFoto by remember { mutableStateOf(false) }

    val selectorFoto = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            subiendoFoto = true
            coroutineScope.launch {
                val res = SesionManager.subirFotoPerfil(contexto, uri)
                subiendoFoto = false
                res.onSuccess { avisar("Foto de perfil actualizada.") }
                    .onFailure { avisar(it.message ?: "No se pudo cambiar la foto.") }
            }
        }
    }

    val nombre = if (SesionManager.nombreUsuarioActual.isNotBlank()) SesionManager.nombreUsuarioActual else "Administrador"
    val email = SesionManager.emailActual ?: "admin@alerce.app"

    Tarjeta {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AvatarUsuario(
                    nombre = nombre,
                    color = 0xFF111111,
                    fotoUrl = SesionManager.fotoPerfilUrl,
                    tamano = 56
                )
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(nombre, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = email,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextoSecundario
                    )
                }
                ChipEstado(
                    texto = "Super Admin",
                    color = com.example.trabajodea10.ui.componentes.ColorEstado(
                        MaterialTheme.colorScheme.primaryContainer,
                        MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }

            Spacer(modifier = Modifier.height(14.dp))
            OutlinedButton(
                onClick = { selectorFoto.launch("image/*") },
                enabled = !subiendoFoto,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, NegroApple),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                if (subiendoFoto) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = NegroApple)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Guardando foto...", fontSize = 13.sp, color = NegroApple)
                } else {
                    Icon(Icons.Filled.Person, contentDescription = null, tint = NegroApple, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Cambiar foto de perfil", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = NegroApple)
                }
            }
        }
    }
}

@Composable
private fun PerfilEmprendedora(nav: ControladorNavegacion, avisar: (String) -> Unit) {
    val contexto = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var subiendoFoto by remember { mutableStateOf(false) }
    var subiendoLogo by remember { mutableStateOf(false) }

    val selectorFoto = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            subiendoFoto = true
            coroutineScope.launch {
                val res = SesionManager.subirFotoPerfil(contexto, uri)
                subiendoFoto = false
                res.onSuccess { avisar("Foto de perfil actualizada.") }
                    .onFailure { avisar(it.message ?: "No se pudo cambiar la foto.") }
            }
        }
    }

    val selectorLogo = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            subiendoLogo = true
            coroutineScope.launch {
                val res = SesionManager.subirLogo(contexto, uri)
                subiendoLogo = false
                res.onSuccess { avisar("Logo del negocio actualizado.") }
                    .onFailure { avisar(it.message ?: "No se pudo cambiar el logo.") }
            }
        }
    }

    val emprendedora = TramaStore.emprendedoraActual
    val nombre = if (SesionManager.nombreUsuarioActual.isNotBlank()) SesionManager.nombreUsuarioActual else emprendedora.nombre
    val tienda = TramaStore.tienda

    // Perfil personal
    Tarjeta {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AvatarUsuario(
                    nombre = nombre,
                    color = emprendedora.color,
                    fotoUrl = SesionManager.fotoPerfilUrl,
                    tamano = 56
                )
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(nombre, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = SesionManager.emailActual ?: tienda.nombre,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextoSecundario
                    )
                }
                ChipEstado(
                    texto = emprendedora.estado.etiqueta,
                    color = colorDeCuenta(emprendedora.estado)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))
            OutlinedButton(
                onClick = { selectorFoto.launch("image/*") },
                enabled = !subiendoFoto,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, NegroApple),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                if (subiendoFoto) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = NegroApple)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Guardando foto...", fontSize = 13.sp, color = NegroApple)
                } else {
                    Icon(Icons.Filled.Person, contentDescription = null, tint = NegroApple, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Cambiar foto de perfil", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = NegroApple)
                }
            }
        }
    }

    // Logo y vitrina del negocio
    Tarjeta {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AvatarUsuario(
                    nombre = tienda.nombre,
                    color = tienda.colorLogo,
                    fotoUrl = tienda.logoUrl,
                    tamano = 56
                )
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(tienda.nombre, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = "Logo público del negocio",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextoSecundario
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            OutlinedButton(
                onClick = { selectorLogo.launch("image/*") },
                enabled = !subiendoLogo,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, NegroApple),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                if (subiendoLogo) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = NegroApple)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Guardando logo...", fontSize = 13.sp, color = NegroApple)
                } else {
                    Icon(Icons.Filled.Image, contentDescription = null, tint = NegroApple, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Cambiar logo del negocio", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = NegroApple)
                }
            }
        }
    }

    Tarjeta {
        Column(modifier = Modifier.padding(vertical = 4.dp)) {
            FilaNavegacion(
                titulo = "Mi tienda",
                descripcion = "Logo, portada, bio y contacto",
                icono = { Icon(Icons.Filled.Store, contentDescription = null, tint = TextoSecundario) },
                onClick = { nav.irA(Ruta.EmpTienda) }
            )
            LineaSeparadora()
            FilaNavegacion(
                titulo = "Mis ferias",
                descripcion = "Invitaciones y estados de participación",
                icono = { Icon(Icons.Filled.Public, contentDescription = null, tint = TextoSecundario) },
                onClick = { nav.irA(Ruta.EmpFerias) }
            )
        }
    }
}

@Composable
private fun FilaNavegacion(
    titulo: String,
    descripcion: String,
    icono: @Composable () -> Unit,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(24.dp), contentAlignment = Alignment.Center) { icono() }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(titulo, fontWeight = FontWeight.Medium)
            Text(descripcion, style = MaterialTheme.typography.bodySmall, color = TextoSecundario)
        }
        Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = TextoSecundario)
    }
}

@Composable
private fun PerfilVisitante(nav: ControladorNavegacion, avisar: (String) -> Unit) {
    val contexto = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var subiendoFoto by remember { mutableStateOf(false) }
    var mostrarCrearTienda by remember { mutableStateOf(false) }
    var nombreTienda by remember { mutableStateOf("") }
    var creandoTienda by remember { mutableStateOf(false) }

    val selectorFoto = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            subiendoFoto = true
            coroutineScope.launch {
                val res = SesionManager.subirFotoPerfil(contexto, uri)
                subiendoFoto = false
                res.onSuccess { avisar("Foto de perfil actualizada.") }
                    .onFailure { avisar(it.message ?: "No se pudo cambiar la foto.") }
            }
        }
    }

    val nombre = if (SesionManager.nombreUsuarioActual.isNotBlank()) SesionManager.nombreUsuarioActual else "Comprador"
    val email = SesionManager.emailActual ?: "Sin correo registrado"

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Tarjeta {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AvatarUsuario(
                        nombre = nombre,
                        color = 0xFF6B7FB3,
                        fotoUrl = SesionManager.fotoPerfilUrl,
                        tamano = 56
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(nombre, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleMedium)
                        Text(
                            text = email,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextoSecundario
                        )
                    }
                    ChipEstado(
                        texto = "Comprador",
                        color = com.example.trabajodea10.ui.componentes.ColorEstado(
                            MaterialTheme.colorScheme.primaryContainer,
                            MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))
            OutlinedButton(
                onClick = { selectorFoto.launch("image/*") },
                enabled = !subiendoFoto,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, NegroApple),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                if (subiendoFoto) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = NegroApple)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Guardando foto...", fontSize = 13.sp, color = NegroApple)
                } else {
                    Icon(Icons.Filled.Person, contentDescription = null, tint = NegroApple, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Cambiar foto de perfil", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = NegroApple)
                }
            }
            }
        }

        OutlinedButton(
            onClick = { mostrarCrearTienda = true },
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, NegroApple),
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Icon(Icons.Filled.Store, contentDescription = null, tint = NegroApple)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Crear mi tienda y empezar a vender", fontWeight = FontWeight.SemiBold, color = NegroApple)
        }
    }

    if (mostrarCrearTienda) {
        AlertDialog(
            onDismissRequest = { if (!creandoTienda) mostrarCrearTienda = false },
            title = { Text("Crear mi tienda", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Con tu tienda podrás agregar productos, precios y fotos. Luego podrás completar sus datos desde la pestaña Tienda.")
                    OutlinedTextField(
                        value = nombreTienda,
                        onValueChange = { nombreTienda = it },
                        label = { Text("Nombre de la tienda") },
                        singleLine = true,
                        enabled = !creandoTienda,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    enabled = nombreTienda.isNotBlank() && !creandoTienda,
                    onClick = {
                        creandoTienda = true
                        coroutineScope.launch {
                            val resultado = SesionManager.crearTiendaParaUsuario(nombreTienda)
                            creandoTienda = false
                            resultado.onSuccess { rol ->
                                mostrarCrearTienda = false
                                nav.iniciarSesion(rol)
                                avisar("Tienda creada. Ya puedes agregar tus productos.")
                            }.onFailure { error ->
                                avisar(error.message ?: "No se pudo crear tu tienda.")
                            }
                        }
                    }
                ) { Text(if (creandoTienda) "Creando…" else "Crear tienda") }
            },
            dismissButton = {
                TextButton(enabled = !creandoTienda, onClick = { mostrarCrearTienda = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun AjustesSistema() {
    val config = TramaStore.config

    TituloSeccion(texto = "Sistema")
    Tarjeta {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            DatoFila(etiqueta = "Moneda", valor = config.moneda)
            LineaSeparadora()
            DatoFila(etiqueta = "Zona horaria", valor = config.zonaHoraria)
            LineaSeparadora()
            DatoFila(etiqueta = "Idioma", valor = "Español (Chile)")
        }
    }

    TituloSeccion(texto = "Interacción pública")
    Tarjeta {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            FilaSwitch(
                titulo = "Registro público de cuentas",
                descripcion = "Permite que emprendedoras se registren desde la web",
                checked = config.registroPublico,
                onCheckedChange = {
                    TramaStore.guardarConfig(config.copy(registroPublico = it))
                }
            )
            FilaSwitch(
                titulo = "Catálogo visible para visitantes",
                descripcion = "Muestra los productos publicados sin iniciar sesión",
                checked = config.catalogoPublico,
                onCheckedChange = {
                    TramaStore.guardarConfig(config.copy(catalogoPublico = it))
                }
            )
            FilaSwitch(
                titulo = "Chat directo",
                descripcion = "Habilita mensajes entre emprendedoras",
                checked = config.chatDirecto,
                onCheckedChange = {
                    TramaStore.guardarConfig(config.copy(chatDirecto = it))
                }
            )
            FilaSwitch(
                titulo = "Ferias visibles públicamente",
                descripcion = "Las ferias aparecen en el calendario público",
                checked = config.feriasPublicas,
                onCheckedChange = {
                    TramaStore.guardarConfig(config.copy(feriasPublicas = it))
                }
            )
        }
    }
}

@Composable
private fun AjustesNotificaciones() {
    val prefs = TramaStore.prefs

    TituloSeccion(texto = "Notificaciones")
    Tarjeta {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            FilaSwitch(
                titulo = "Alertas de stock bajo",
                descripcion = "Avisa cuando un producto queda con 3 unidades o menos",
                checked = prefs.alertasStock,
                onCheckedChange = { TramaStore.guardarPrefs(prefs.copy(alertasStock = it)) }
            )
            FilaSwitch(
                titulo = "Recordatorios de ferias",
                descripcion = "Te avisa 24 horas antes de una feria confirmada",
                checked = prefs.recordatoriosFeria,
                onCheckedChange = { TramaStore.guardarPrefs(prefs.copy(recordatoriosFeria = it)) }
            )
            FilaSwitch(
                titulo = "Resumen semanal de ventas",
                descripcion = "Envía un resumen los lunes",
                checked = prefs.resumenVentas,
                onCheckedChange = { TramaStore.guardarPrefs(prefs.copy(resumenVentas = it)) }
            )
        }
    }
}

/** Control de accesibilidad para agrandar el texto, disponible para todos los perfiles. */
@Composable
private fun AjustesAccesibilidad() {
    TituloSeccion(texto = "Accesibilidad")
    Tarjeta {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Tamaño del texto",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Agrandá todo el texto de la app para leerlo más fácil.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextoSecundario,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(modifier = Modifier.height(10.dp))
            BotonesTamanoTexto()
        }
    }
}
