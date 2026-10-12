package com.example.trabajodea10.ui.emprendedora

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.SettingsSuggest
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.trabajodea10.data.FirebaseManager
import com.example.trabajodea10.data.NecesidadesTienda
import com.example.trabajodea10.data.Tienda
import com.example.trabajodea10.data.TramaStore
import com.example.trabajodea10.ui.componentes.BarraSuperior
import com.example.trabajodea10.ui.componentes.CampoTexto
import com.example.trabajodea10.ui.componentes.Tarjeta
import com.example.trabajodea10.ui.componentes.TituloSeccion
import com.example.trabajodea10.ui.navegacion.ControladorNavegacion
import com.example.trabajodea10.ui.navegacion.Ruta
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

private val COLORES_TEMA = listOf(
    0xFF5C315B, 0xFF2E7D5B, 0xFF319795, 0xFFC07A3E, 0xFF2D3748, 0xFFC53030
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PantallaTienda(
    nav: ControladorNavegacion,
    avisar: (String) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    // Buscar si ya existe tienda cargada o usar la de TramaStore
    val tiendaBase = FirebaseManager.tiendas.firstOrNull { it.id == TramaStore.tienda.id } ?: TramaStore.tienda

    var nombre by remember { mutableStateOf(tiendaBase.nombre) }
    var rubro by remember { mutableStateOf(tiendaBase.rubro) }
    var sector by remember { mutableStateOf(tiendaBase.sector) }
    var bio by remember { mutableStateOf(tiendaBase.bio) }
    var whatsapp by remember { mutableStateOf(tiendaBase.whatsapp) }
    var instagram by remember { mutableStateOf(tiendaBase.instagram) }
    var colorPortada by remember { mutableStateOf(tiendaBase.colorPortada) }
    var colorLogo by remember { mutableStateOf(tiendaBase.colorLogo) }

    // Switches de "Selección de Necesidades"
    var tieneAgendamiento by remember { mutableStateOf(tiendaBase.necesidades.tieneAgendamiento) }
    var tieneStock by remember { mutableStateOf(tiendaBase.necesidades.tieneStock) }
    var tieneProductosAPedido by remember { mutableStateOf(tiendaBase.necesidades.tieneProductosAPedido) }
    var tieneVentaPorWhatsApp by remember { mutableStateOf(tiendaBase.necesidades.tieneVentaPorWhatsApp) }
    var participaEnFerias by remember { mutableStateOf(tiendaBase.necesidades.participaEnFerias) }
    var tieneForoComunidad by remember { mutableStateOf(tiendaBase.necesidades.tieneForoComunidad) }

    var guardando by remember { mutableStateOf(false) }

    val tiendaActualizada = remember(
        nombre, rubro, sector, bio, whatsapp, instagram, colorPortada, colorLogo,
        tieneAgendamiento, tieneStock, tieneProductosAPedido, tieneVentaPorWhatsApp,
        participaEnFerias, tieneForoComunidad
    ) {
        Tienda(
            id = tiendaBase.id,
            nombre = nombre.trim().ifBlank { "Mi Tienda" },
            rubro = rubro.trim().ifBlank { "Comercio" },
            sector = sector.trim().ifBlank { "Alerce" },
            bio = bio.trim(),
            whatsapp = whatsapp.trim().ifBlank { "+56 9 1234 5678" },
            instagram = instagram.trim(),
            colorPortada = colorPortada,
            colorLogo = colorLogo,
            necesidades = NecesidadesTienda(
                tieneStock = tieneStock,
                tieneAgendamiento = tieneAgendamiento,
                tieneVentaPorWhatsApp = tieneVentaPorWhatsApp,
                tieneProductosAPedido = tieneProductosAPedido,
                participaEnFerias = participaEnFerias,
                tieneForoComunidad = tieneForoComunidad
            )
        )
    }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        BarraSuperior(
            titulo = "Panel del Emprendedor",
            subtitulo = "Personaliza tu vitrina y elige tus herramientas",
            onVolver = if (nav.pila.size > 1) ({ nav.volver() }) else null
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Saludo personalizado con efecto wow
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = BerenjenaContenedor,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(shape = CircleShape, color = Berenjena, modifier = Modifier.size(42.dp)) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Filled.Store, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "¡Hola, ${FirebaseManager.nombreUsuarioDemo}!",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = BerenjenaOscuro
                        )
                        Text(
                            text = "Configuración activa para la comuna de Puerto Montt",
                            style = MaterialTheme.typography.bodySmall,
                            color = BerenjenaOscuro.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            // SECCIÓN CLAVE: SELECCIÓN DE NECESIDADES
            TituloSeccion(texto = "1. SELECCIÓN DE NECESIDADES (MÓDULOS)")
            Tarjeta {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.SettingsSuggest, contentDescription = null, tint = Berenjena, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "¿Qué funciones necesita tu pyme?",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = BerenjenaOscuro
                        )
                    }
                    Text(
                        text = "Activa o desactiva con los interruptores según tu tipo de negocio. La aplicación mostrará solo las vistas que elijas.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextoSecundario
                    )

                    HorizontalDivider(color = LineaSuave)

                    // Switch 1: Agendamiento de horas
                    FilaSwitchNecesidad(
                        titulo = "Agendamiento de Horas / Citas",
                        descripcion = "Para barberías, peluquerías o talleres. Permite a los clientes reservar turnos de atención.",
                        icono = Icons.Filled.CalendarMonth,
                        activo = tieneAgendamiento,
                        onCheckedChange = { tieneAgendamiento = it }
                    )

                    // Switch 2: Catálogo con control de stock
                    FilaSwitchNecesidad(
                        titulo = "Catálogo con Control de Stock",
                        descripcion = "Para abarrotes y productos físicos con conteo de unidades disponibles.",
                        icono = Icons.Filled.Inventory2,
                        activo = tieneStock,
                        onCheckedChange = { tieneStock = it }
                    )

                    // Switch 3: Productos a pedido
                    FilaSwitchNecesidad(
                        titulo = "Ventas por Encargo / A Pedido",
                        descripcion = "Para artesanías o repostería elaborada a pedido sin stock inmediato.",
                        icono = Icons.Filled.ContentCut,
                        activo = tieneProductosAPedido,
                        onCheckedChange = { tieneProductosAPedido = it }
                    )

                    // Switch 4: Venta directa por WhatsApp
                    FilaSwitchNecesidad(
                        titulo = "Pedidos Directos por WhatsApp",
                        descripcion = "Botón con mensaje precargado para facilitar la compra a clientes que no usan carrito.",
                        icono = Icons.Filled.Chat,
                        activo = tieneVentaPorWhatsApp,
                        onCheckedChange = { tieneVentaPorWhatsApp = it }
                    )

                    // Switch 5: Ferias Comunales
                    FilaSwitchNecesidad(
                        titulo = "Participar en Ferias Comunales",
                        descripcion = "Recibir invitaciones municipales a ferias y avisar a tus clientes seguidores.",
                        icono = Icons.Filled.ShoppingBag,
                        activo = participaEnFerias,
                        onCheckedChange = { participaEnFerias = it }
                    )

                    // Switch 6: Muro y Foro de Comunidad
                    FilaSwitchNecesidad(
                        titulo = "Muro de Novedades y Foro con Clientes",
                        descripcion = "Publicar avisos de nuevo stock y responder consultas abiertas.",
                        icono = Icons.Filled.Forum,
                        activo = tieneForoComunidad,
                        onCheckedChange = { tieneForoComunidad = it }
                    )
                }
            }

            // SECCIÓN 2: DATOS DEL NEGOCIO
            TituloSeccion(texto = "2. DATOS DE TU NEGOCIO")
            CampoTexto(valor = nombre, onValorChange = { nombre = it }, etiqueta = "Nombre de la Tienda")
            CampoTexto(valor = rubro, onValorChange = { rubro = it }, etiqueta = "Rubro (ej. Barbería, Minimarket, Artesanía)")
            CampoTexto(valor = sector, onValorChange = { sector = it }, etiqueta = "Sector (ej. Alerce Norte, Alerce Histórico)")
            CampoTexto(
                valor = bio,
                onValorChange = { bio = it },
                etiqueta = "Descripción para tus clientes",
                placeholder = "Cuéntanos qué ofreces a tu comunidad...",
                esMultilinea = true
            )
            CampoTexto(
                valor = whatsapp,
                onValorChange = { whatsapp = it },
                etiqueta = "WhatsApp de Contacto (+56 9 ...)",
                tipoTeclado = androidx.compose.ui.text.input.KeyboardType.Phone
            )

            // SECCIÓN 3: PERSONALIZACIÓN VISUAL
            TituloSeccion(texto = "3. COLOR DE MARCA Y PORTADA")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                COLORES_TEMA.forEach { color ->
                    SelectorColor(
                        color = color,
                        seleccionado = color == colorPortada,
                        onClick = { colorPortada = color }
                    )
                }
            }

            // VISTA PREVIA EN VIVO
            TituloSeccion(texto = "4. VISTA PREVIA PÚBLICA (EN TIEMPO REAL)")
            Text(
                text = "Así verán tu negocio los clientes en el Marketplace según los interruptores que activaste:",
                style = MaterialTheme.typography.bodySmall,
                color = TextoSecundario
            )

            // Tarjeta de vista previa en vivo
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SuperficieBlanca),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .background(Color(colorPortada)),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            text = "  ${rubro.uppercase()} • $sector",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(shape = CircleShape, color = Color(colorLogo), modifier = Modifier.size(42.dp)) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = nombre.firstOrNull()?.uppercase() ?: "T",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(nombre, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Text("$sector, Puerto Montt", style = MaterialTheme.typography.bodySmall, color = TextoSecundario)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Badges activos según los switches
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            if (tieneAgendamiento) {
                                InsigniaPreview("Agendamiento de Horas", Color(0xFFE0F2FE), Color(0xFF0369A1))
                            }
                            if (tieneStock) {
                                InsigniaPreview("Stock en Tienda", Color(0xFFFEF3C7), Color(0xFFB45309))
                            }
                            if (tieneProductosAPedido) {
                                InsigniaPreview("A Pedido", Color(0xFFEDE9FE), Color(0xFF6D28D9))
                            }
                            if (participaEnFerias) {
                                InsigniaPreview("Ferias Comunales", VerdeContenedor, VerdeOk)
                            }
                            if (tieneForoComunidad) {
                                InsigniaPreview("Muro de Novedades", Color(0xFFFCE7F3), Color(0xFF9D174D))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // BOTÓN DE GUARDADO EN CLOUD FIRESTORE
            Button(
                onClick = {
                    guardando = true
                    coroutineScope.launch {
                        val exito = FirebaseManager.guardarTiendaEnFirestore(tiendaActualizada)
                        guardando = false
                        avisar(if (exito) "¡Guardado exitosamente en Cloud Firestore!" else "Guardado localmente en tu app")
                    }
                },
                enabled = !guardando,
                colors = ButtonDefaults.buttonColors(containerColor = Berenjena),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                if (guardando) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.CloudUpload, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Guardar Cambios en Firebase", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }
            }

            OutlinedButton(
                onClick = { nav.seleccionarTab(Ruta.VisProductos) },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Filled.Visibility, contentDescription = null, tint = Berenjena)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Ver en el Marketplace como Comprador", color = Berenjena, fontWeight = FontWeight.SemiBold)
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
private fun FilaSwitchNecesidad(
    titulo: String,
    descripcion: String,
    icono: ImageVector,
    activo: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!activo) }
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = if (activo) VerdeContenedor else SuperficieVariante,
            modifier = Modifier.size(38.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icono,
                    contentDescription = null,
                    tint = if (activo) VerdeOk else TextoSecundario,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(titulo, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Text(descripcion, style = MaterialTheme.typography.bodySmall, color = TextoSecundario, fontSize = 12.sp)
        }
        Spacer(modifier = Modifier.width(8.dp))
        Switch(
            checked = activo,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = VerdeOk
            )
        )
    }
}

@Composable
private fun InsigniaPreview(texto: String, fondo: Color, textoColor: Color) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = fondo
    ) {
        Text(
            text = texto,
            color = textoColor,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
        )
    }
}

@Composable
private fun SelectorColor(
    color: Long,
    seleccionado: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .background(
                if (seleccionado) SuperficieVariante else Color.Transparent,
                RoundedCornerShape(50)
            )
            .clickable(onClick = onClick)
            .padding(5.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(color), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (seleccionado) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(Color.White, CircleShape)
                )
            }
        }
    }
}

