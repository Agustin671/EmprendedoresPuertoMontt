package com.example.trabajodea10.ui.publico

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import com.example.trabajodea10.ui.componentes.ContenedorChips
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
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
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
fun PantallaMarketplace(avisar: ((String) -> Unit)? = null) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var busqueda by remember { mutableStateOf("") }
    var categoriaSeleccionada by remember { mutableStateOf("Todos") }
    var tiendaDetalle by remember { mutableStateOf<Tienda?>(null) }
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
                        Text(
                            text = "Bienvenido, Paul · Vitrina de Alerce",
                            style = MaterialTheme.typography.bodySmall,
                            color = GrisTexto
                        )
                    }

                    // Botón para refrescar o sembrar en Firebase (Pill minimalista)
                    Surface(
                        shape = RoundedCornerShape(50.dp),
                        color = if (FirebaseManager.mensajeError != null) RojoAlerceContenedor else GrisChip,
                        border = BorderStroke(1.dp, GrisBorde),
                        modifier = Modifier.clickable {
                            coroutineScope.launch {
                                sembrando = true
                                val exito = FirebaseManager.sembrarTiendasIniciales()
                                FirebaseManager.cargarTiendasDesdeFirestore()
                                sembrando = false
                                avisar?.invoke(if (exito) "¡Firestore sincronizado con éxito!" else "Tiendas cargadas en memoria")
                            }
                        }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (sembrando) {
                                CircularProgressIndicator(modifier = Modifier.size(12.dp), strokeWidth = 2.dp, color = NegroApple)
                            } else {
                                Icon(
                                    imageVector = Icons.Filled.CloudSync,
                                    contentDescription = "Sincronizar",
                                    tint = if (FirebaseManager.mensajeError != null) RojoAlerce else NegroApple,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Firestore",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (FirebaseManager.mensajeError != null) RojoAlerce else NegroApple
                            )
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
            onContactarWhatsApp = {
                abrirWhatsApp(
                    context,
                    tienda.whatsapp,
                    "Hola ${tienda.nombre}! Vi tu tienda en Alerce Emprende y me gustaría hacer una consulta."
                )
            }
        )
    }
}

@Composable
private fun TarjetaTiendaMarketplace(
    tienda: Tienda,
    onVerDetalle: () -> Unit,
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
                        onClick = onVerDetalle,
                        shape = RoundedCornerShape(50.dp),
                        border = BorderStroke(1.dp, GrisBorde),
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                    ) {
                        Text(
                            text = if (tienda.necesidades.tieneAgendamiento) "Agendar Cita" else "Ver Vitrina",
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
    onContactarWhatsApp: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = BlancoPuro,
        shape = RoundedCornerShape(24.dp),
        title = {
            Column {
                Text(tienda.nombre, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = NegroApple)
                Text(tienda.rubro, style = MaterialTheme.typography.bodySmall, color = GrisTexto)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(tienda.bio, style = MaterialTheme.typography.bodyMedium, color = NegroApple)

                HorizontalDivider(color = GrisBorde)

                Text("Servicios y facilidades activas:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = NegroApple)

                if (tienda.necesidades.tieneAgendamiento) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Check, contentDescription = null, tint = VerdeOk, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Permite agendar horas de atención.", fontSize = 13.sp, color = GrisTexto)
                    }
                }

                if (tienda.necesidades.tieneStock) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Check, contentDescription = null, tint = VerdeOk, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Dispone de stock para entrega inmediata.", fontSize = 13.sp, color = GrisTexto)
                    }
                }

                if (tienda.necesidades.tieneProductosAPedido) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Check, contentDescription = null, tint = VerdeOk, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Acepta pedidos y encargos personalizados.", fontSize = 13.sp, color = GrisTexto)
                    }
                }

                Text("Ubicación: ${tienda.sector}, Puerto Montt", fontSize = 12.sp, color = GrisTexto)
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
                Text("Contactar por WhatsApp", color = BlancoPuro, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cerrar", color = GrisTexto)
            }
        }
    )
}
