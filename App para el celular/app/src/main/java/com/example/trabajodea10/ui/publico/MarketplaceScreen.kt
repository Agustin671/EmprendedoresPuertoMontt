package com.example.trabajodea10.ui.publico

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import com.example.trabajodea10.data.BloqueHorario
import com.example.trabajodea10.data.Producto
import com.example.trabajodea10.data.PublicacionForo
import com.example.trabajodea10.data.RespuestaForo
import com.example.trabajodea10.data.TramaStore
import com.example.trabajodea10.data.formatearMoneda
import com.example.trabajodea10.data.nombreDiaLegible
import com.example.trabajodea10.ui.componentes.CampoTexto
import com.example.trabajodea10.ui.componentes.ContenedorChips
import com.example.trabajodea10.ui.componentes.FotoProducto
import com.example.trabajodea10.ui.navegacion.ControladorNavegacion
import com.example.trabajodea10.ui.theme.VerdeContenedor
import com.example.trabajodea10.ui.theme.VerdeOk
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.trabajodea10.data.FirebaseManager
import com.example.trabajodea10.data.Tienda
import com.example.trabajodea10.ui.theme.BlancoPuro
import com.example.trabajodea10.ui.theme.FondoLimpio
import com.example.trabajodea10.ui.theme.GrisBorde
import com.example.trabajodea10.ui.theme.GrisChip
import com.example.trabajodea10.ui.theme.GrisTexto
import com.example.trabajodea10.ui.theme.NegroApple
import com.example.trabajodea10.ui.theme.RojoAlerce
import com.example.trabajodea10.ui.theme.RojoAlerceContenedor
import com.example.trabajodea10.ui.theme.VerdeContenedor
import com.example.trabajodea10.ui.theme.VerdeOk
import kotlinx.coroutines.launch

private val CATEGORIAS = listOf(
    "Todos",
    "Servicios y Peluquería",
    "Abarrotes y Alimentos",
    "Artesanía y Textil",
    "Pastelería y Gastronomía"
)

@Composable
fun PantallaMarketplace(
    nav: ControladorNavegacion? = null,
    avisar: ((String) -> Unit)? = null
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var busqueda by remember { mutableStateOf("") }
    var categoriaSeleccionada by remember { mutableStateOf("Todos") }
    var tiendaDetalle by remember { mutableStateOf<Tienda?>(null) }
    var tiendaAgendamiento by remember { mutableStateOf<Tienda?>(null) }
    var sembrando by remember { mutableStateOf(false) }

    // Cargar tiendas desde Firestore en el primer render
    LaunchedEffect(Unit) {
        FirebaseManager.cargarTiendasDesdeFirestore()
    }

    val tiendasFiltradas = FirebaseManager.tiendas.filter { tienda ->
        val coincideTexto = busqueda.isBlank() ||
                tienda.nombre.contains(busqueda, ignoreCase = true) ||
                tienda.rubro.contains(busqueda, ignoreCase = true) ||
                tienda.sector.contains(busqueda, ignoreCase = true)

        val coincideCategoria = categoriaSeleccionada == "Todos" ||
                tienda.rubro.equals(categoriaSeleccionada, ignoreCase = true)

        coincideTexto && coincideCategoria
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FondoLimpio)
    ) {
        // Cabecera Minimalista Editorial (Inspirada en Farfetch & Apple)
        Surface(
            color = BlancoPuro,
            border = BorderStroke(1.dp, GrisBorde)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = "ALERCE",
                                fontWeight = FontWeight.Black,
                                fontSize = 24.sp,
                                letterSpacing = 3.sp,
                                color = NegroApple
                            )
                            Box(
                                modifier = Modifier
                                    .padding(bottom = 4.dp, start = 2.dp)
                                    .size(6.dp)
                                    .background(RojoAlerce, CircleShape)
                            )
                        }
                        val saludoCabecera = if (FirebaseManager.nombreUsuarioActual.isNotBlank()) {
                            "Bienvenido(a), ${FirebaseManager.nombreUsuarioActual} · Vitrina de Alerce"
                        } else {
                            "Vitrina Comercial de Alerce · Puerto Montt"
                        }
                        Text(
                            text = saludoCabecera,
                            style = MaterialTheme.typography.bodySmall,
                            color = GrisTexto
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Botón circular para refrescar (sin texto "Firestore")
                        Surface(
                            shape = CircleShape,
                            color = if (FirebaseManager.mensajeError != null) RojoAlerceContenedor else GrisChip,
                            border = BorderStroke(1.dp, GrisBorde),
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .clickable {
                                    coroutineScope.launch {
                                        sembrando = true
                                        val exito = FirebaseManager.sembrarTiendasIniciales()
                                        FirebaseManager.cargarTiendasDesdeFirestore()
                                        sembrando = false
                                        avisar?.invoke(if (exito) "¡Vitrina sincronizada!" else "Tiendas actualizadas")
                                    }
                                }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                if (sembrando) {
                                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = NegroApple)
                                } else {
                                    Icon(
                                        imageVector = Icons.Filled.Refresh,
                                        contentDescription = "Actualizar tiendas",
                                        tint = if (FirebaseManager.mensajeError != null) RojoAlerce else NegroApple,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }

                        // Botón circular de Cerrar Sesión (Logout)
                        if (nav != null) {
                            Surface(
                                shape = CircleShape,
                                color = GrisChip,
                                border = BorderStroke(1.dp, GrisBorde),
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .clickable {
                                        FirebaseManager.cerrarSesion()
                                        nav.cerrarSesion()
                                        avisar?.invoke("Sesión cerrada")
                                    }
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Filled.Logout,
                                        contentDescription = "Cerrar sesión",
                                        tint = NegroApple,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Buscador Estilo Apple / Pinterest (Pill con borde sutil)
                OutlinedTextField(
                    value = busqueda,
                    onValueChange = { busqueda = it },
                    placeholder = { Text("Buscar barberías, panaderías, artesanía...", color = GrisTexto, fontSize = 14.sp) },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = NegroApple) },
                    singleLine = true,
                    shape = RoundedCornerShape(50.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = GrisChip,
                        unfocusedContainerColor = GrisChip,
                        focusedBorderColor = NegroApple,
                        unfocusedBorderColor = Color.Transparent
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Filtros horizontales por rubro (Pills tipo Farfetch: negro sólido activo, gris inactivo)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CATEGORIAS.forEach { cat ->
                        val seleccionado = categoriaSeleccionada == cat
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50.dp))
                                .background(if (seleccionado) NegroApple else GrisChip)
                                .clickable { categoriaSeleccionada = cat }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = cat,
                                fontSize = 12.sp,
                                fontWeight = if (seleccionado) FontWeight.Bold else FontWeight.Medium,
                                color = if (seleccionado) BlancoPuro else NegroApple
                            )
                        }
                    }
                }
            }
        }

        // Estado de carga o listado de tiendas
        if (FirebaseManager.cargando && FirebaseManager.tiendas.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = NegroApple)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Banner sutil de estado
                item {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = BlancoPuro,
                        border = BorderStroke(1.dp, GrisBorde),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (FirebaseManager.mensajeError != null) Icons.Filled.Store else Icons.Filled.CloudDone,
                                contentDescription = null,
                                tint = if (FirebaseManager.mensajeError != null) RojoAlerce else VerdeOk,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (FirebaseManager.mensajeError != null)
                                    "Modo Demostración Offline (datos típicos de Alerce)"
                                else
                                    "Conectado a Firebase Cloud Firestore (${tiendasFiltradas.size} tiendas activas)",
                                fontSize = 12.sp,
                                color = NegroApple,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                items(tiendasFiltradas) { tienda ->
                    TarjetaTiendaMarketplace(
                        tienda = tienda,
                        onVerDetalle = { tiendaDetalle = tienda },
                        onAgendarCita = { tiendaAgendamiento = tienda },
                        onContactarWhatsApp = {
                            abrirWhatsApp(
                                context,
                                tienda.whatsapp,
                                "Hola ${tienda.nombre}! Vi tu tienda en Alerce Emprende y me gustaría hacer una consulta."
                            )
                        }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(70.dp))
                }
            }
        }
    }

    // Modal de Detalle de Tienda / Servicios (Estilo Apple)
    tiendaDetalle?.let { tienda ->
        DialogoDetalleTienda(
            tienda = tienda,
            onDismiss = { tiendaDetalle = null },
            onAgendarCita = {
                tiendaDetalle = null
                tiendaAgendamiento = tienda
            },
            onContactarWhatsApp = {
                abrirWhatsApp(
                    context,
                    tienda.whatsapp,
                    "Hola ${tienda.nombre}! Vi tu tienda en Alerce Emprende y me gustaría hacer una consulta."
                )
            }
        )
    }

    // Modal de Agendamiento de Citas (Próximas 2 Semanas)
    tiendaAgendamiento?.let { tienda ->
        DialogoAgendamientoTienda(
            tienda = tienda,
            onDismiss = { tiendaAgendamiento = null },
            onCitaConfirmada = { mensaje ->
                avisar?.invoke(mensaje)
            }
        )
    }
}

@Composable
private fun TarjetaTiendaMarketplace(
    tienda: Tienda,
    onVerDetalle: () -> Unit,
    onAgendarCita: () -> Unit,
    onContactarWhatsApp: () -> Unit
) {
    var esFavorito by remember { mutableStateOf(false) }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = BlancoPuro,
        border = BorderStroke(1.dp, GrisBorde),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onVerDetalle)
    ) {
        Column {
            // Franja superior editorial con rubro y botón de corazón (Wishlist Farfetch/Pinterest)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
                    .background(Color(tienda.colorPortada)),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = tienda.rubro.uppercase(),
                        color = Color.White.copy(alpha = 0.95f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color.Black.copy(alpha = 0.35f)
                        ) {
                            Text(
                                text = tienda.sector,
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        // Corazón Wishlist (Farfetch/Apple Style)
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.25f))
                                .clickable { esFavorito = !esFavorito },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (esFavorito) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                                contentDescription = "Favorito",
                                tint = if (esFavorito) RojoAlerce else Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = Color(tienda.colorLogo),
                        modifier = Modifier.size(46.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = tienda.nombre.firstOrNull()?.uppercase() ?: "T",
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 20.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = tienda.nombre,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = NegroApple
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.LocationOn, contentDescription = null, tint = GrisTexto, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "${tienda.sector}, Puerto Montt",
                                style = MaterialTheme.typography.bodySmall,
                                color = GrisTexto
                            )
                        }
                    }
                }

                if (tienda.bio.isNotBlank()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = tienda.bio,
                        style = MaterialTheme.typography.bodySmall,
                        color = GrisTexto,
                        maxLines = 2
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Badges que reflejan la "Selección de Necesidades" configurada por el emprendedor
                ContenedorChips(
                    espacioHorizontal = 6.dp,
                    espacioVertical = 6.dp
                ) {
                    if (tienda.necesidades.tieneAgendamiento) {
                        InsigniaNecesidadEditorial(
                            texto = "Agendamiento",
                            icono = Icons.Filled.CalendarMonth,
                            colorFondo = Color(0xFFF0F9FF),
                            colorTexto = Color(0xFF0369A1)
                        )
                    }

                    if (tienda.necesidades.tieneStock) {
                        InsigniaNecesidadEditorial(
                            texto = "Stock en Tienda",
                            icono = Icons.Filled.Inventory2,
                            colorFondo = RojoAlerceContenedor,
                            colorTexto = RojoAlerce
                        )
                    }

                    if (tienda.necesidades.tieneProductosAPedido) {
                        InsigniaNecesidadEditorial(
                            texto = "A Pedido",
                            icono = Icons.Filled.ContentCut,
                            colorFondo = Color(0xFFF5F3FF),
                            colorTexto = Color(0xFF6D28D9)
                        )
                    }

                    if (tienda.necesidades.participaEnFerias) {
                        InsigniaNecesidadEditorial(
                            texto = "Ferias Comunales",
                            icono = Icons.Filled.ShoppingBag,
                            colorFondo = VerdeContenedor,
                            colorTexto = VerdeOk
                        )
                    }

                    if (tienda.necesidades.tieneForoComunidad) {
                        InsigniaNecesidadEditorial(
                            texto = "Muro y Foro",
                            icono = Icons.Filled.Email,
                            colorFondo = Color(0xFFFCE7F3),
                            colorTexto = Color(0xFF9D174D)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = GrisBorde)
                Spacer(modifier = Modifier.height(12.dp))

                // Botones de acción estilo Farfetch / Apple (Pills)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            if (tienda.necesidades.tieneAgendamiento) {
                                onAgendarCita()
                            } else {
                                onVerDetalle()
                            }
                        },
                        shape = RoundedCornerShape(50.dp),
                        border = BorderStroke(1.dp, GrisBorde),
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                    ) {
                        Text(
                            text = when {
                                tienda.necesidades.tieneAgendamiento -> "Agendar Cita"
                                tienda.necesidades.tieneForoComunidad -> "Vitrina y Foro"
                                else -> "Ver Vitrina"
                            },
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = NegroApple
                        )
                    }

                    Button(
                        onClick = onContactarWhatsApp,
                        shape = RoundedCornerShape(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NegroApple),
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                    ) {
                        Text("WhatsApp", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BlancoPuro)
                    }
                }
            }
        }
    }
}

@Composable
private fun InsigniaNecesidadEditorial(
    texto: String,
    icono: androidx.compose.ui.graphics.vector.ImageVector,
    colorFondo: Color,
    colorTexto: Color
) {
    Surface(
        shape = RoundedCornerShape(50.dp),
        color = colorFondo
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icono, contentDescription = null, tint = colorTexto, modifier = Modifier.size(12.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = texto, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = colorTexto)
        }
    }
}

@Composable
private fun DialogoDetalleTienda(
    tienda: Tienda,
    onDismiss: () -> Unit,
    onAgendarCita: (() -> Unit)? = null,
    onContactarWhatsApp: () -> Unit
) {
    val context = LocalContext.current
    val productos = remember(tienda.id) { TramaStore.productosDeTienda(tienda.id) }
    var nuevaPreguntaCliente by remember { mutableStateOf("") }
    var pestanaDetalle by remember { mutableStateOf(0) }
    val publicaciones = remember(tienda.id, TramaStore.versionForo) {
        TramaStore.publicacionesDeTienda(tienda.id)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = BlancoPuro,
        shape = RoundedCornerShape(24.dp),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color(tienda.colorLogo),
                    modifier = Modifier.size(38.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = tienda.nombre.firstOrNull()?.uppercase() ?: "T",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 17.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(tienda.nombre, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = NegroApple)
                    Text("${tienda.rubro} · ${tienda.sector}", style = MaterialTheme.typography.bodySmall, color = GrisTexto)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (tienda.bio.isNotBlank()) {
                    Text(tienda.bio, style = MaterialTheme.typography.bodySmall, color = GrisTexto)
                }

                // Opción destacada de agendamiento si la tienda lo ofrece
                if (tienda.necesidades.tieneAgendamiento && onAgendarCita != null) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFF0F9FF),
                        border = BorderStroke(1.dp, Color(0xFFBAE6FD)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(onClick = onAgendarCita)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.CalendarMonth, contentDescription = null, tint = Color(0xFF0369A1), modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text("Agendar Cita de Atención", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF0369A1))
                                    Text("Turnos en las próximas 2 semanas", fontSize = 11.sp, color = GrisTexto)
                                }
                            }
                            Text("Agendar ➔", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color(0xFF0369A1))
                        }
                    }
                }

                HorizontalDivider(color = GrisBorde)

                // SELECTOR DE PESTAÑAS (Si la tienda tiene foro de comunidad)
                if (tienda.necesidades.tieneForoComunidad) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(GrisChip, RoundedCornerShape(50.dp))
                            .padding(3.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(50.dp))
                                .background(if (pestanaDetalle == 0) BlancoPuro else Color.Transparent)
                                .clickable { pestanaDetalle = 0 }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Catálogo (${productos.size})",
                                fontSize = 12.sp,
                                fontWeight = if (pestanaDetalle == 0) FontWeight.Bold else FontWeight.Medium,
                                color = NegroApple
                            )
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(50.dp))
                                .background(if (pestanaDetalle == 1) NegroApple else Color.Transparent)
                                .clickable { pestanaDetalle = 1 }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Muro y Foro (${publicaciones.size})",
                                fontSize = 12.sp,
                                fontWeight = if (pestanaDetalle == 1) FontWeight.Bold else FontWeight.Medium,
                                color = if (pestanaDetalle == 1) BlancoPuro else NegroApple
                            )
                        }
                    }
                }

                if (!tienda.necesidades.tieneForoComunidad || pestanaDetalle == 0) {
                    // CATÁLOGO DE PRODUCTOS DE LA VITRINA
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                    Text(
                        text = "Productos en Vitrina (${productos.size}):",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = NegroApple
                    )
                    Surface(
                        shape = RoundedCornerShape(50.dp),
                        color = GrisChip
                    ) {
                        Text(
                            text = "Catálogo Directo",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = NegroApple,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }

                if (productos.isEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = GrisChip,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Esta tienda aún no tiene productos publicados en vitrina. Puedes consultar por WhatsApp.",
                            style = MaterialTheme.typography.bodySmall,
                            color = GrisTexto,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        productos.forEach { producto ->
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = BlancoPuro,
                                border = BorderStroke(1.dp, GrisBorde),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    FotoProducto(
                                        fotoUrl = producto.fotoUrl,
                                        colorRespaldo = producto.colorFoto,
                                        inicial = producto.nombre,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(120.dp),
                                        forma = RoundedCornerShape(10.dp),
                                        tamanoInicial = 38.sp
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = producto.nombre,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                color = NegroApple
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = formatearMoneda(producto.precio),
                                                fontWeight = FontWeight.Black,
                                                fontSize = 15.sp,
                                                color = RojoAlerce
                                            )
                                        }

                                        Surface(
                                            shape = RoundedCornerShape(50.dp),
                                            color = if (producto.stockTotal > 0) VerdeContenedor else RojoAlerceContenedor
                                        ) {
                                            Text(
                                                text = if (producto.stockTotal > 0) "Stock Disponible" else "A Pedido",
                                                color = if (producto.stockTotal > 0) VerdeOk else RojoAlerce,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                            )
                                        }
                                    }

                                    if (producto.variantes.isNotEmpty() && producto.variantes.firstOrNull()?.nombre != "Único") {
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            producto.variantes.take(2).forEach { v ->
                                                Surface(
                                                    shape = RoundedCornerShape(6.dp),
                                                    color = GrisChip
                                                ) {
                                                    Text(
                                                        text = v.nombre,
                                                        fontSize = 10.sp,
                                                        color = GrisTexto,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    // Botón para pedir directamente este producto
                                    OutlinedButton(
                                        onClick = {
                                            abrirWhatsApp(
                                                context,
                                                tienda.whatsapp,
                                                "Hola ${tienda.nombre}! Me interesa comprar '${producto.nombre}' (${formatearMoneda(producto.precio)}) que vi en tu vitrina de Alerce Emprende."
                                            )
                                        },
                                        shape = RoundedCornerShape(50.dp),
                                        border = BorderStroke(1.dp, NegroApple),
                                        modifier = Modifier.fillMaxWidth().height(36.dp)
                                    ) {
                                        Text(
                                            text = "Pedir este producto por WhatsApp",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = NegroApple
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                }

                if (tienda.necesidades.tieneForoComunidad && pestanaDetalle == 1) {
                    // SECCIÓN MURO Y FORO COMUNITARIO
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Email, contentDescription = null, tint = RojoAlerce, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Muro Comunitario & Consultas",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = NegroApple
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(50.dp),
                            color = Color(0xFFFCE7F3)
                        ) {
                            Text(
                                text = "En Vivo",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF9D174D),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }

                    if (publicaciones.isEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = GrisChip,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Aún no hay publicaciones en el muro. ¡Sé el primero en hacer una pregunta a ${tienda.nombre}!",
                                style = MaterialTheme.typography.bodySmall,
                                color = GrisTexto,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            publicaciones.forEach { pub ->
                                val esOficial = pub.esEmprendedor
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (esOficial) Color(0xFFFFFBEB) else GrisChip,
                                    border = BorderStroke(1.dp, if (esOficial) Color(0xFFFDE68A) else GrisBorde),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Surface(
                                                    shape = CircleShape,
                                                    color = if (esOficial) Color(0xFFD97706) else Color(0xFF4B5563),
                                                    modifier = Modifier.size(22.dp)
                                                ) {
                                                    Box(contentAlignment = Alignment.Center) {
                                                        Text(
                                                            text = pub.autorNombre.firstOrNull()?.uppercase() ?: "U",
                                                            color = Color.White,
                                                            fontSize = 10.sp,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                    }
                                                }
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = pub.autorNombre,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 12.sp,
                                                    color = NegroApple
                                                )
                                                if (esOficial) {
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Surface(
                                                        shape = RoundedCornerShape(50.dp),
                                                        color = Color(0xFFFEF3C7)
                                                    ) {
                                                        Text(
                                                            text = "Oficial",
                                                            fontSize = 9.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = Color(0xFFB45309),
                                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                        )
                                                    }
                                                }
                                            }
                                            Text(
                                                text = pub.fecha,
                                                fontSize = 10.sp,
                                                color = GrisTexto
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = pub.texto,
                                            fontSize = 12.sp,
                                            color = NegroApple,
                                            lineHeight = 16.sp
                                        )

                                        // Respuestas de la tienda
                                        if (pub.respuestas.isNotEmpty()) {
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(start = 10.dp),
                                                verticalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                pub.respuestas.forEach { resp ->
                                                    Surface(
                                                        shape = RoundedCornerShape(8.dp),
                                                        color = if (resp.esEmprendedor) Color(0xFFFEF2F2) else BlancoPuro,
                                                        border = BorderStroke(0.5.dp, if (resp.esEmprendedor) Color(0xFFFECACA) else GrisBorde),
                                                        modifier = Modifier.fillMaxWidth()
                                                    ) {
                                                        Column(modifier = Modifier.padding(6.dp)) {
                                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                                Text(
                                                                    text = resp.autorNombre,
                                                                    fontWeight = FontWeight.Bold,
                                                                    fontSize = 11.sp,
                                                                    color = if (resp.esEmprendedor) RojoAlerce else NegroApple
                                                                )
                                                                if (resp.esEmprendedor) {
                                                                    Spacer(modifier = Modifier.width(4.dp))
                                                                    Text("· Respuesta de la Tienda", fontSize = 9.sp, color = RojoAlerce, fontWeight = FontWeight.SemiBold)
                                                                }
                                                                Spacer(modifier = Modifier.width(6.dp))
                                                                Text(text = resp.fecha, fontSize = 9.sp, color = GrisTexto)
                                                            }
                                                            Spacer(modifier = Modifier.height(2.dp))
                                                            Text(text = resp.texto, fontSize = 11.sp, color = NegroApple)
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

                    // Formulario para que el cliente pregunte
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFF9FAFB),
                        border = BorderStroke(1.dp, GrisBorde),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "Pregúntale a ${tienda.nombre} en el Muro:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = NegroApple
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            CampoTexto(
                                valor = nuevaPreguntaCliente,
                                onValorChange = { nuevaPreguntaCliente = it },
                                etiqueta = "Tu consulta pública",
                                placeholder = "Ej: ¿Vecina, tiene pan amasado o stock hoy?",
                                esMultilinea = false
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    if (nuevaPreguntaCliente.isNotBlank()) {
                                        val nombreFirma = if (FirebaseManager.nombreUsuarioActual.isNotBlank())
                                            "${FirebaseManager.nombreUsuarioActual} (Vecino)"
                                        else
                                            "Vecino de Alerce"
                                        TramaStore.agregarPublicacionForo(
                                            tiendaId = tienda.id,
                                            autorNombre = nombreFirma,
                                            esEmprendedor = false,
                                            texto = nuevaPreguntaCliente.trim()
                                        )
                                        nuevaPreguntaCliente = ""
                                    }
                                },
                                enabled = nuevaPreguntaCliente.isNotBlank(),
                                shape = RoundedCornerShape(50.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = NegroApple),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(38.dp)
                            ) {
                                Icon(Icons.Filled.Send, contentDescription = null, tint = BlancoPuro, modifier = Modifier.size(13.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                val textoBotonPublicar = if (FirebaseManager.nombreUsuarioActual.isNotBlank())
                                    "Publicar Consulta como ${FirebaseManager.nombreUsuarioActual}"
                                else
                                    "Publicar Consulta en el Muro"
                                Text(textoBotonPublicar, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BlancoPuro)
                            }
                        }
                    }

                    HorizontalDivider(color = GrisBorde)
                }

                // Facilidades y ubicación
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (tienda.necesidades.tieneStock) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Check, contentDescription = null, tint = VerdeOk, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Entrega inmediata en Alerce.", fontSize = 12.sp, color = GrisTexto)
                        }
                    }
                    if (tienda.necesidades.tieneProductosAPedido) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Check, contentDescription = null, tint = VerdeOk, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Acepta encargos y pedidos personalizados.", fontSize = 12.sp, color = GrisTexto)
                        }
                    }
                    Text("Ubicación: ${tienda.sector}, Puerto Montt", fontSize = 11.sp, color = GrisTexto)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onDismiss()
                    onContactarWhatsApp()
                },
                shape = RoundedCornerShape(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NegroApple)
            ) {
                Text("Chat General WhatsApp", color = BlancoPuro, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cerrar", color = GrisTexto)
            }
        }
    )
}

@Composable
fun DialogoAgendamientoTienda(
    tienda: Tienda,
    onDismiss: () -> Unit,
    onCitaConfirmada: (String) -> Unit
) {
    val context = LocalContext.current
    val fechas = remember { TramaStore.proximos14Dias() }
    var fechaSeleccionada by remember { mutableStateOf(fechas.firstOrNull() ?: "") }
    var bloqueSeleccionado by remember { mutableStateOf<BloqueHorario?>(null) }

    var nombreCliente by remember {
        mutableStateOf(FirebaseManager.nombreUsuarioActual)
    }
    var telefonoCliente by remember { mutableStateOf("+56 9 ") }
    var servicio by remember { mutableStateOf("Atención / Corte de Cabello") }
    var reservaConfirmada by remember { mutableStateOf(false) }

    val bloquesDelDia = TramaStore.horariosDeTienda(tienda.id, fechaSeleccionada)

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = BlancoPuro,
        shape = RoundedCornerShape(24.dp),
        title = {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (reservaConfirmada) "¡Hora Reservada!" else "Agendar Cita",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = NegroApple
                    )
                    Surface(
                        shape = RoundedCornerShape(50.dp),
                        color = RojoAlerceContenedor
                    ) {
                        Text(
                            text = "2 Semanas",
                            color = RojoAlerce,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
                Text(
                    text = tienda.nombre,
                    style = MaterialTheme.typography.bodySmall,
                    color = GrisTexto
                )
            }
        },
        text = {
            if (reservaConfirmada) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = VerdeContenedor,
                        modifier = Modifier.size(54.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Filled.Check, contentDescription = null, tint = VerdeOk, modifier = Modifier.size(28.dp))
                        }
                    }
                    Text(
                        text = "¡Tu cita ha sido agendada con éxito!",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = NegroApple
                    )
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = GrisChip,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Día: ${nombreDiaLegible(fechaSeleccionada)}", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = NegroApple)
                            Text("Hora: ${bloqueSeleccionado?.hora ?: ""} (${bloqueSeleccionado?.duracion ?: "45 min"})", fontSize = 13.sp, color = GrisTexto)
                            Text("Servicio: $servicio", fontSize = 13.sp, color = GrisTexto)
                            Text("Cliente: $nombreCliente", fontSize = 13.sp, color = GrisTexto)
                        }
                    }
                    Text(
                        text = "Puedes enviar los detalles directamente al WhatsApp de ${tienda.nombre} para confirmar con el emprendedor.",
                        style = MaterialTheme.typography.bodySmall,
                        color = GrisTexto
                    )
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "1. Selecciona la fecha (Próximos 14 días):",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = NegroApple
                    )

                    // Carrusel horizontal de días
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        fechas.forEachIndexed { idx, fechaIso ->
                            val esSeleccionado = fechaIso == fechaSeleccionada
                            val etiqueta = if (idx == 0) "Hoy" else nombreDiaLegible(fechaIso)
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (esSeleccionado) NegroApple else GrisChip,
                                border = BorderStroke(1.dp, if (esSeleccionado) NegroApple else GrisBorde),
                                modifier = Modifier.clickable {
                                    fechaSeleccionada = fechaIso
                                    bloqueSeleccionado = null
                                }
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = etiqueta,
                                        fontSize = 11.sp,
                                        fontWeight = if (esSeleccionado) FontWeight.Bold else FontWeight.Medium,
                                        color = if (esSeleccionado) BlancoPuro else NegroApple
                                    )
                                    Text(
                                        text = fechaIso.takeLast(5).replace("-", "/"),
                                        fontSize = 10.sp,
                                        color = if (esSeleccionado) Color.White.copy(alpha = 0.8f) else GrisTexto
                                    )
                                }
                            }
                        }
                    }

                    HorizontalDivider(color = GrisBorde)

                    Text(
                        text = "2. Horarios disponibles para el ${nombreDiaLegible(fechaSeleccionada)}:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = NegroApple
                    )

                    if (bloquesDelDia.isEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = GrisChip,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "No hay bloques creados para este día.",
                                    fontSize = 12.sp,
                                    color = GrisTexto
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedButton(
                                    onClick = {
                                        listOf("10:00", "11:30", "15:00", "16:30").forEach { hora ->
                                            TramaStore.agregarHorario(tienda.id, fechaSeleccionada, hora)
                                        }
                                    },
                                    shape = RoundedCornerShape(50.dp),
                                    border = BorderStroke(1.dp, GrisBorde)
                                ) {
                                    Text("+ Generar bloques de prueba", fontSize = 11.sp, color = NegroApple)
                                }
                            }
                        }
                    } else {
                        ContenedorChips(
                            espacioHorizontal = 8.dp,
                            espacioVertical = 8.dp
                        ) {
                            bloquesDelDia.forEach { bloque ->
                                val esEsteSeleccionado = bloqueSeleccionado?.id == bloque.id
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = when {
                                        bloque.reservado -> Color(0xFFF3F4F6)
                                        esEsteSeleccionado -> NegroApple
                                        else -> BlancoPuro
                                    },
                                    border = BorderStroke(
                                        1.dp,
                                        when {
                                            bloque.reservado -> Color(0xFFE5E7EB)
                                            esEsteSeleccionado -> NegroApple
                                            else -> GrisBorde
                                        }
                                    ),
                                    modifier = Modifier.clickable(enabled = !bloque.reservado) {
                                        bloqueSeleccionado = bloque
                                    }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = bloque.hora,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = when {
                                                bloque.reservado -> GrisTexto
                                                esEsteSeleccionado -> BlancoPuro
                                                else -> NegroApple
                                            }
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = if (bloque.reservado) "(Ocupado)" else "(${bloque.duracion})",
                                            fontSize = 10.sp,
                                            color = when {
                                                bloque.reservado -> Color(0xFF9CA3AF)
                                                esEsteSeleccionado -> Color.White.copy(alpha = 0.8f)
                                                else -> GrisTexto
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (bloqueSeleccionado != null) {
                        HorizontalDivider(color = GrisBorde)

                        Text(
                            text = "3. Datos para tu reserva:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = NegroApple
                        )

                        CampoTexto(
                            valor = nombreCliente,
                            onValorChange = { nombreCliente = it },
                            etiqueta = "Tu Nombre Completo"
                        )

                        CampoTexto(
                            valor = telefonoCliente,
                            onValorChange = { telefonoCliente = it },
                            etiqueta = "Tu Teléfono / WhatsApp",
                            tipoTeclado = KeyboardType.Phone
                        )

                        CampoTexto(
                            valor = servicio,
                            onValorChange = { servicio = it },
                            etiqueta = "Servicio o Motivo"
                        )
                    }
                }
            }
        },
        confirmButton = {
            if (reservaConfirmada) {
                Button(
                    onClick = {
                        abrirWhatsApp(
                            context,
                            tienda.whatsapp,
                            "Hola ${tienda.nombre}! Acabo de reservar una hora para el ${nombreDiaLegible(fechaSeleccionada)} a las ${bloqueSeleccionado?.hora ?: ""} (${servicio}) a nombre de $nombreCliente."
                        )
                        onDismiss()
                    },
                    shape = RoundedCornerShape(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NegroApple)
                ) {
                    Text("Avisar por WhatsApp", color = BlancoPuro, fontWeight = FontWeight.Bold)
                }
            } else {
                Button(
                    onClick = {
                        val bloque = bloqueSeleccionado
                        if (bloque != null) {
                            val exito = TramaStore.reservarHora(
                                bloqueId = bloque.id,
                                clienteNombre = nombreCliente,
                                clienteTelefono = telefonoCliente,
                                servicio = servicio
                            )
                            if (exito) {
                                reservaConfirmada = true
                                onCitaConfirmada("¡Cita agendada para el ${nombreDiaLegible(fechaSeleccionada)} a las ${bloque.hora}!")
                            }
                        }
                    },
                    enabled = bloqueSeleccionado != null && nombreCliente.isNotBlank(),
                    shape = RoundedCornerShape(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NegroApple)
                ) {
                    Text("Confirmar Reserva", color = BlancoPuro, fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (reservaConfirmada) "Cerrar" else "Cancelar", color = GrisTexto)
            }
        }
    )
}
