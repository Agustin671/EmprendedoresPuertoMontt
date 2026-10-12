package com.example.trabajodea10.ui.publico

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.trabajodea10.data.FirebaseManager
import com.example.trabajodea10.data.Tienda
import com.example.trabajodea10.ui.theme.Berenjena
import com.example.trabajodea10.ui.theme.BerenjenaContenedor
import com.example.trabajodea10.ui.theme.BerenjenaOscuro
import com.example.trabajodea10.ui.theme.LineaSuave
import com.example.trabajodea10.ui.theme.SuperficieBlanca
import com.example.trabajodea10.ui.theme.SuperficieVariante
import com.example.trabajodea10.ui.theme.TextoSecundario
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

@OptIn(ExperimentalLayoutApi::class)
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
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Cabecera Marketplace estilo refinado
        Surface(
            color = SuperficieBlanca,
            shadowElevation = 2.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Marketplace Alerce",
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp,
                            color = BerenjenaOscuro
                        )
                        Text(
                            text = "Comercios y servicios locales de Puerto Montt",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextoSecundario
                        )
                    }

                    // Botón para refrescar o sembrar en Firebase
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = VerdeContenedor,
                        modifier = Modifier.clickable {
                            coroutineScope.launch {
                                sembrando = true
                                val exito = FirebaseManager.sembrarTiendasIniciales()
                                FirebaseManager.cargarTiendasDesdeFirestore()
                                sembrando = false
                                avisar?.invoke(if (exito) "¡Base de datos Firestore sincronizada con 4 tiendas!" else "Tiendas cargadas en memoria")
                            }
                        }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (sembrando) {
                                CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp, color = VerdeOk)
                            } else {
                                Icon(Icons.Filled.CloudSync, contentDescription = "Sincronizar", tint = VerdeOk, modifier = Modifier.size(16.dp))
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Firestore",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = VerdeOk
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Buscador
                OutlinedTextField(
                    value = busqueda,
                    onValueChange = { busqueda = it },
                    placeholder = { Text("Buscar tiendas, barberías, abarrotes en Alerce...") },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = Berenjena) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Filtros horizontales por rubro
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CATEGORIAS.forEach { cat ->
                        val seleccionado = categoriaSeleccionada == cat
                        FilterChip(
                            selected = seleccionado,
                            onClick = { categoriaSeleccionada = cat },
                            label = { Text(cat, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = BerenjenaContenedor,
                                selectedLabelColor = BerenjenaOscuro
                            )
                        )
                    }
                }
            }
        }

        // Estado de carga o contenido
        if (FirebaseManager.cargando && FirebaseManager.tiendas.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Berenjena)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Banner informativo sobre el origen de los datos
                item {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (FirebaseManager.mensajeError != null) Color(0xFFFFFBEB) else Color(0xFFF0FDF4),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (FirebaseManager.mensajeError != null) Icons.Filled.Store else Icons.Filled.CloudDone,
                                contentDescription = null,
                                tint = if (FirebaseManager.mensajeError != null) Color(0xFFB45309) else VerdeOk,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (FirebaseManager.mensajeError != null)
                                    "Modo Demostración Offline (datos típicos de Alerce)"
                                else
                                    "Conectado a Firebase Cloud Firestore (${tiendasFiltradas.size} tiendas activas)",
                                fontSize = 12.sp,
                                color = if (FirebaseManager.mensajeError != null) Color(0xFFB45309) else VerdeOk,
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
                    Spacer(modifier = Modifier.height(60.dp))
                }
            }
        }
    }

    // Modal de Detalle de Tienda / Servicios
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TarjetaTiendaMarketplace(
    tienda: Tienda,
    onVerDetalle: () -> Unit,
    onContactarWhatsApp: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SuperficieBlanca),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onVerDetalle)
    ) {
        Column {
            // Franja superior con color de marca de la pyme
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
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
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.Black.copy(alpha = 0.25f)
                    ) {
                        Text(
                            text = tienda.sector,
                            color = Color.White,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Column(modifier = Modifier.padding(14.dp)) {
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
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.LocationOn, contentDescription = null, tint = TextoSecundario, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "${tienda.sector}, Puerto Montt",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextoSecundario
                            )
                        }
                    }
                }

                if (tienda.bio.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = tienda.bio,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Badges que reflejan la "Selección de Necesidades" configurada por el emprendedor
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (tienda.necesidades.tieneAgendamiento) {
                        InsigniaNecesidad(
                            texto = "Agendamiento Disponible",
                            icono = Icons.Filled.CalendarMonth,
                            colorFondo = Color(0xFFE0F2FE),
                            colorTexto = Color(0xFF0369A1)
                        )
                    }

                    if (tienda.necesidades.tieneStock) {
                        InsigniaNecesidad(
                            texto = "Stock en Tienda",
                            icono = Icons.Filled.Inventory2,
                            colorFondo = Color(0xFFFEF3C7),
                            colorTexto = Color(0xFFB45309)
                        )
                    }

                    if (tienda.necesidades.tieneProductosAPedido) {
                        InsigniaNecesidad(
                            texto = "Trabajos A Pedido",
                            icono = Icons.Filled.ContentCut,
                            colorFondo = Color(0xFFEDE9FE),
                            colorTexto = Color(0xFF6D28D9)
                        )
                    }

                    if (tienda.necesidades.participaEnFerias) {
                        InsigniaNecesidad(
                            texto = "Ferias Comunales",
                            icono = Icons.Filled.ShoppingBag,
                            colorFondo = VerdeContenedor,
                            colorTexto = VerdeOk
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = LineaSuave)
                Spacer(modifier = Modifier.height(10.dp))

                // Botones de acción
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onVerDetalle,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = if (tienda.necesidades.tieneAgendamiento) "Agendar / Ver" else "Ver Vitrina",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Button(
                        onClick = onContactarWhatsApp,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = VerdeOk),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("WhatsApp", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
private fun InsigniaNecesidad(
    texto: String,
    icono: androidx.compose.ui.graphics.vector.ImageVector,
    colorFondo: Color,
    colorTexto: Color
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = colorFondo
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icono, contentDescription = null, tint = colorTexto, modifier = Modifier.size(13.dp))
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
        title = {
            Column {
                Text(tienda.nombre, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Text(tienda.rubro, style = MaterialTheme.typography.bodySmall, color = TextoSecundario)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(tienda.bio, style = MaterialTheme.typography.bodyMedium)

                HorizontalDivider()

                Text("Servicios y facilidades activas:", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)

                if (tienda.necesidades.tieneAgendamiento) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Check, contentDescription = null, tint = VerdeOk, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Permite agendar horas de atención por WhatsApp.", fontSize = 13.sp)
                    }
                }

                if (tienda.necesidades.tieneStock) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Check, contentDescription = null, tint = VerdeOk, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Dispone de productos para entrega inmediata.", fontSize = 13.sp)
                    }
                }

                if (tienda.necesidades.tieneProductosAPedido) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Check, contentDescription = null, tint = VerdeOk, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Acepta pedidos y encargos personalizados.", fontSize = 13.sp)
                    }
                }

                Text("Ubicación: ${tienda.sector}, Puerto Montt", fontSize = 13.sp, color = TextoSecundario)
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onDismiss()
                    onContactarWhatsApp()
                },
                colors = ButtonDefaults.buttonColors(containerColor = VerdeOk)
            ) {
                Text("Hablar por WhatsApp")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cerrar")
            }
        }
    )
}
