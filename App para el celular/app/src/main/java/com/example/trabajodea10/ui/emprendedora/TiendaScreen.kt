package com.example.trabajodea10.ui.emprendedora

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Store
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
import com.example.trabajodea10.data.BloqueHorario
import com.example.trabajodea10.data.FirebaseManager
import com.example.trabajodea10.data.NecesidadesTienda
import com.example.trabajodea10.data.PublicacionForo
import com.example.trabajodea10.data.RespuestaForo
import androidx.compose.foundation.BorderStroke
import com.example.trabajodea10.data.Tienda
import com.example.trabajodea10.data.TramaStore
import com.example.trabajodea10.data.nombreDiaLegible
import com.example.trabajodea10.ui.componentes.BarraSuperior
import com.example.trabajodea10.ui.componentes.CampoTexto
import com.example.trabajodea10.ui.componentes.ContenedorChips
import com.example.trabajodea10.ui.componentes.Tarjeta
import com.example.trabajodea10.ui.componentes.TituloSeccion
import com.example.trabajodea10.ui.navegacion.ControladorNavegacion
import com.example.trabajodea10.ui.navegacion.Ruta
import com.example.trabajodea10.ui.theme.Berenjena
import com.example.trabajodea10.ui.theme.BerenjenaContenedor
import com.example.trabajodea10.ui.theme.BerenjenaOscuro
import com.example.trabajodea10.ui.theme.LineaSuave
import com.example.trabajodea10.ui.theme.NegroApple
import com.example.trabajodea10.ui.theme.RojoAlerce
import com.example.trabajodea10.ui.theme.RojoAlerceContenedor
import com.example.trabajodea10.ui.theme.SuperficieBlanca
import com.example.trabajodea10.ui.theme.SuperficieVariante
import com.example.trabajodea10.ui.theme.TextoSecundario
import com.example.trabajodea10.ui.theme.VerdeContenedor
import com.example.trabajodea10.ui.theme.VerdeOk
import kotlinx.coroutines.launch

private val COLORES_TEMA = listOf(
    0xFF111111L, 0xFFDC2626L, 0xFF18181BL, 0xFF059669L, 0xFF2563EBL, 0xFFD97706L
)

private fun colorSeguro(valor: Long, porDefecto: Color = RojoAlerce): Color {
    return try {
        Color(valor)
    } catch (_: Throwable) {
        porDefecto
    }
}

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
            // Saludo personalizado estilo Apple Studio
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = SuperficieBlanca,
                border = BorderStroke(1.dp, LineaSuave),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(shape = CircleShape, color = SuperficieVariante, modifier = Modifier.size(44.dp)) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Filled.Store, contentDescription = null, tint = NegroApple, modifier = Modifier.size(22.dp))
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "¡Hola, ${FirebaseManager.nombreUsuarioDemo}!",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = NegroApple
                        )
                        Text(
                            text = "Configuración activa · Alerce, Puerto Montt",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextoSecundario
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(50.dp),
                        color = RojoAlerceContenedor
                    ) {
                        Text(
                            text = "En línea",
                            color = RojoAlerce,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
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
                        Icon(Icons.Filled.Settings, contentDescription = null, tint = Berenjena, modifier = Modifier.size(24.dp))
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
                        icono = Icons.Filled.Edit,
                        activo = tieneProductosAPedido,
                        onCheckedChange = { tieneProductosAPedido = it }
                    )

                    // Switch 4: Venta directa por WhatsApp
                    FilaSwitchNecesidad(
                        titulo = "Pedidos Directos por WhatsApp",
                        descripcion = "Botón con mensaje precargado para facilitar la compra a clientes que no usan carrito.",
                        icono = Icons.Filled.Phone,
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
                        icono = Icons.Filled.Email,
                        activo = tieneForoComunidad,
                        onCheckedChange = { tieneForoComunidad = it }
                    )
                }
            }

            // GESTIÓN DE AGENDA (Si tieneAgendamiento está activo)
            if (tieneAgendamiento) {
                TituloSeccion(texto = "2. GESTIÓN DE AGENDA (PRÓXIMAS 2 SEMANAS)")
                SeccionGestionAgenda(
                    tiendaId = tiendaBase.id,
                    avisar = avisar
                )
            }

            // GESTIÓN DE FORO Y MURO (Si tieneForoComunidad está activo)
            if (tieneForoComunidad) {
                Spacer(modifier = Modifier.height(10.dp))
                TituloSeccion(
                    texto = when {
                        tieneAgendamiento -> "3. MURO Y FORO COMUNITARIO"
                        else -> "2. MURO Y FORO COMUNITARIO"
                    }
                )
                SeccionGestionForo(
                    tiendaId = tiendaBase.id,
                    nombreTienda = nombre,
                    avisar = avisar
                )
            }

            // SECCIÓN SIGUIENTE: DATOS DEL NEGOCIO
            TituloSeccion(
                texto = when {
                    tieneAgendamiento && tieneForoComunidad -> "4. DATOS DE TU NEGOCIO"
                    tieneAgendamiento || tieneForoComunidad -> "3. DATOS DE TU NEGOCIO"
                    else -> "2. DATOS DE TU NEGOCIO"
                }
            )
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
                            .background(colorSeguro(colorPortada, NegroApple)),
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
                            Surface(shape = CircleShape, color = colorSeguro(colorLogo, RojoAlerce), modifier = Modifier.size(42.dp)) {
                                Box(contentAlignment = Alignment.Center) {
                                    val letraInicial = remember(nombre) { (nombre.trim().firstOrNull() ?: 'T').uppercaseChar().toString() }
                                    Text(
                                        text = letraInicial,
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
                        ContenedorChips(
                            espacioHorizontal = 6.dp,
                            espacioVertical = 6.dp
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
                colors = ButtonDefaults.buttonColors(containerColor = NegroApple),
                shape = RoundedCornerShape(50.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                if (guardando) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp))
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Check, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Guardar Cambios en Firebase", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
                    }
                }
            }

            OutlinedButton(
                onClick = { nav.seleccionarTab(Ruta.VisProductos) },
                shape = RoundedCornerShape(50.dp),
                border = BorderStroke(1.dp, LineaSuave),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Icon(Icons.Filled.Search, contentDescription = null, tint = NegroApple)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Ver en el Marketplace como Comprador", color = NegroApple, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
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
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = if (activo) RojoAlerceContenedor else SuperficieVariante,
            modifier = Modifier.size(40.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icono,
                    contentDescription = null,
                    tint = if (activo) RojoAlerce else TextoSecundario,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(titulo, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = NegroApple)
            Text(descripcion, style = MaterialTheme.typography.bodySmall, color = TextoSecundario, fontSize = 12.sp)
        }
        Spacer(modifier = Modifier.width(8.dp))
        Switch(
            checked = activo,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = RojoAlerce,
                uncheckedTrackColor = LineaSuave
            )
        )
    }
}

@Composable
private fun InsigniaPreview(texto: String, fondo: Color, textoColor: Color) {
    Surface(
        shape = RoundedCornerShape(50.dp),
        color = fondo
    ) {
        Text(
            text = texto,
            color = textoColor,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
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
                .background(colorSeguro(color, NegroApple), CircleShape),
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

@Composable
private fun SeccionGestionAgenda(
    tiendaId: String,
    avisar: (String) -> Unit
) {
    val fechas = remember { TramaStore.proximos14Dias() }
    var fechaSeleccionada by remember { mutableStateOf(fechas.firstOrNull() ?: "") }
    var nuevaHora by remember { mutableStateOf("10:00") }
    var nuevaDuracion by remember { mutableStateOf("45 min") }
    val servicioDefecto = "Atención General"

    val bloques = TramaStore.horariosDeTienda(tiendaId, fechaSeleccionada)

    Tarjeta {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.CalendarMonth,
                        contentDescription = null,
                        tint = RojoAlerce,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Turnos Disponibles",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = NegroApple
                    )
                }

                Surface(
                    shape = RoundedCornerShape(50.dp),
                    color = RojoAlerceContenedor
                ) {
                    Text(
                        text = "14 Días Activos",
                        color = RojoAlerce,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            Text(
                text = "Selecciona un día para revisar reservas o añadir nuevos cupos de atención para tus clientes.",
                style = MaterialTheme.typography.bodySmall,
                color = TextoSecundario
            )

            // Selector horizontal de fechas (14 días)
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
                        color = if (esSeleccionado) NegroApple else SuperficieVariante,
                        border = BorderStroke(1.dp, if (esSeleccionado) NegroApple else LineaSuave),
                        modifier = Modifier.clickable {
                            fechaSeleccionada = fechaIso
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
                                color = if (esSeleccionado) Color.White else NegroApple
                            )
                            Text(
                                text = fechaIso.takeLast(5).replace("-", "/"),
                                fontSize = 10.sp,
                                color = if (esSeleccionado) Color.White.copy(alpha = 0.8f) else TextoSecundario
                            )
                        }
                    }
                }
            }

            HorizontalDivider(color = LineaSuave)

            Text(
                text = "Horarios programados para ${nombreDiaLegible(fechaSeleccionada)}:",
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                color = NegroApple
            )

            if (bloques.isEmpty()) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = SuperficieVariante,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "No has configurado turnos para este día. Agrega uno abajo para habilitarlo en tu vitrina.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextoSecundario,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    bloques.forEach { bloque ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (bloque.reservado) Color(0xFFFEF2F2) else SuperficieVariante,
                            border = BorderStroke(1.dp, if (bloque.reservado) Color(0xFFFCA5A5) else LineaSuave),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = bloque.hora,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = NegroApple
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "(${bloque.duracion})",
                                            fontSize = 12.sp,
                                            color = TextoSecundario
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Surface(
                                            shape = RoundedCornerShape(50.dp),
                                            color = if (bloque.reservado) RojoAlerce else VerdeOk
                                        ) {
                                            Text(
                                                text = if (bloque.reservado) "RESERVADO" else "DISPONIBLE",
                                                color = Color.White,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Black,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }

                                    if (bloque.reservado) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Cliente: ${bloque.clienteNombre ?: "Paul"} · ${bloque.clienteTelefono ?: ""}",
                                            fontSize = 11.sp,
                                            color = NegroApple,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Text(
                                            text = "Servicio: ${bloque.servicio ?: "Atención"}",
                                            fontSize = 11.sp,
                                            color = TextoSecundario
                                        )
                                    }
                                }

                                if (bloque.reservado) {
                                    OutlinedButton(
                                        onClick = {
                                            TramaStore.cancelarReserva(bloque.id)
                                            avisar("Turno de las ${bloque.hora} liberado")
                                        },
                                        shape = RoundedCornerShape(50.dp),
                                        border = BorderStroke(1.dp, LineaSuave)
                                    ) {
                                        Text("Liberar", fontSize = 11.sp, color = NegroApple)
                                    }
                                } else {
                                    IconButton(
                                        onClick = {
                                            TramaStore.eliminarHorario(bloque.id)
                                            avisar("Horario de las ${bloque.hora} eliminado")
                                        }
                                    ) {
                                        Icon(
                                            Icons.Filled.Delete,
                                            contentDescription = "Eliminar",
                                            tint = TextoSecundario,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            HorizontalDivider(color = LineaSuave)

            // Formulario para agregar horario disponible
            Text(
                text = "Añadir Horario para ${nombreDiaLegible(fechaSeleccionada)}:",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = NegroApple
            )

            // Chips con presets rápidos de horas
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf("09:30", "11:00", "12:30", "15:00", "16:30", "18:00").forEach { preset ->
                    Surface(
                        shape = RoundedCornerShape(50.dp),
                        color = if (nuevaHora == preset) NegroApple else SuperficieVariante,
                        border = BorderStroke(1.dp, LineaSuave),
                        modifier = Modifier.clickable { nuevaHora = preset }
                    ) {
                        Text(
                            text = preset,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (nuevaHora == preset) Color.White else NegroApple,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    CampoTexto(
                        valor = nuevaHora,
                        onValorChange = { nuevaHora = it },
                        etiqueta = "Hora (ej. 14:00)"
                    )
                }
                Box(modifier = Modifier.weight(1f)) {
                    CampoTexto(
                        valor = nuevaDuracion,
                        onValorChange = { nuevaDuracion = it },
                        etiqueta = "Duración"
                    )
                }
            }

            Button(
                onClick = {
                    if (nuevaHora.isNotBlank()) {
                        TramaStore.agregarHorario(
                            tiendaId = tiendaId,
                            fecha = fechaSeleccionada,
                            hora = nuevaHora.trim(),
                            duracion = nuevaDuracion.trim().ifBlank { "45 min" },
                            servicio = servicioDefecto
                        )
                        avisar("¡Horario $nuevaHora agregado para ${nombreDiaLegible(fechaSeleccionada)}!")
                    }
                },
                shape = RoundedCornerShape(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NegroApple),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp)
            ) {
                Icon(Icons.Filled.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("+ Agregar este Turno a la Agenda", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun SeccionGestionForo(
    tiendaId: String,
    nombreTienda: String,
    avisar: (String) -> Unit
) {
    var nuevoAviso by remember { mutableStateOf("") }
    var respuestaTextoPorPubId by remember { mutableStateOf(mapOf<String, String>()) }
    var mostrandoResponderPorPubId by remember { mutableStateOf(mapOf<String, Boolean>()) }

    val publicaciones = remember(tiendaId, TramaStore.versionForo) {
        TramaStore.publicacionesDeTienda(tiendaId)
    }

    Tarjeta {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.Email,
                        contentDescription = null,
                        tint = RojoAlerce,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Muro y Foro Comunitario",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
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
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Text(
                text = "Publica avisos oficiales para tus vecinos (ej. pan caliente, ofertas del día) y responde consultas vecinales.",
                style = MaterialTheme.typography.bodySmall,
                color = TextoSecundario,
                fontSize = 12.sp
            )

            // CAJA PARA PUBLICAR AVISO OFICIAL
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = SuperficieVariante,
                border = BorderStroke(1.dp, LineaSuave),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Nuevo Comunicado Oficial de $nombreTienda",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = NegroApple
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    CampoTexto(
                        valor = nuevoAviso,
                        onValorChange = { nuevoAviso = it },
                        etiqueta = "Mensaje a la comunidad",
                        placeholder = "Ej: ¡Acaba de salir pan amasado calentito y llegaron huevos frescos de campo! 🥖🥚",
                        esMultilinea = true
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = {
                            if (nuevoAviso.isNotBlank()) {
                                TramaStore.agregarPublicacionForo(
                                    tiendaId = tiendaId,
                                    autorNombre = nombreTienda,
                                    esEmprendedor = true,
                                    texto = nuevoAviso.trim()
                                )
                                nuevoAviso = ""
                                avisar("¡Aviso oficial publicado en el Muro!")
                            }
                        },
                        enabled = nuevoAviso.isNotBlank(),
                        shape = RoundedCornerShape(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NegroApple),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                    ) {
                        Icon(Icons.Filled.Send, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Publicar en el Muro de la Comunidad", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }

            HorizontalDivider(color = LineaSuave)

            // LISTA DE PUBLICACIONES DEL MURO
            Text(
                text = "Mensajes y Consultas Vecinales (${publicaciones.size}):",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = NegroApple
            )

            if (publicaciones.isEmpty()) {
                Text(
                    text = "Aún no hay publicaciones en el muro. Redacta tu primer aviso oficial arriba para que lo vean en tu vitrina.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextoSecundario,
                    fontSize = 12.sp
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    publicaciones.forEach { pub ->
                        val esAvisoOficial = pub.esEmprendedor
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (esAvisoOficial) Color(0xFFFFFBEB) else SuperficieBlanca,
                            border = BorderStroke(1.dp, if (esAvisoOficial) Color(0xFFFDE68A) else LineaSuave),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Surface(
                                            shape = CircleShape,
                                            color = if (esAvisoOficial) Color(0xFFD97706) else Color(0xFF4B5563),
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text(
                                                    text = pub.autorNombre.firstOrNull()?.uppercase() ?: "U",
                                                    color = Color.White,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = pub.autorNombre,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 12.sp,
                                                    color = NegroApple
                                                )
                                                if (esAvisoOficial) {
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
                                                color = TextoSecundario
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = pub.texto,
                                    fontSize = 13.sp,
                                    color = NegroApple,
                                    lineHeight = 18.sp
                                )

                                // RESPUESTAS
                                if (pub.respuestas.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(start = 12.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        pub.respuestas.forEach { resp ->
                                            Surface(
                                                shape = RoundedCornerShape(10.dp),
                                                color = if (resp.esEmprendedor) Color(0xFFFEF2F2) else SuperficieVariante,
                                                border = BorderStroke(0.5.dp, if (resp.esEmprendedor) Color(0xFFFECACA) else LineaSuave),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Column(modifier = Modifier.padding(8.dp)) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Text(
                                                            text = resp.autorNombre,
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 11.sp,
                                                            color = if (resp.esEmprendedor) RojoAlerce else NegroApple
                                                        )
                                                        if (resp.esEmprendedor) {
                                                            Spacer(modifier = Modifier.width(4.dp))
                                                            Text(
                                                                text = "· Tu respuesta",
                                                                fontSize = 10.sp,
                                                                fontWeight = FontWeight.SemiBold,
                                                                color = RojoAlerce
                                                            )
                                                        }
                                                        Spacer(modifier = Modifier.width(6.dp))
                                                        Text(
                                                            text = resp.fecha,
                                                            fontSize = 9.sp,
                                                            color = TextoSecundario
                                                        )
                                                    }
                                                    Spacer(modifier = Modifier.height(2.dp))
                                                    Text(
                                                        text = resp.texto,
                                                        fontSize = 12.sp,
                                                        color = NegroApple
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                // BOTÓN / FORMULARIO PARA RESPONDER
                                val mostrando = mostrandoResponderPorPubId[pub.id] ?: false
                                val textoResp = respuestaTextoPorPubId[pub.id] ?: ""

                                Spacer(modifier = Modifier.height(6.dp))
                                if (!mostrando) {
                                    Text(
                                        text = "Responder como $nombreTienda ➔",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = RojoAlerce,
                                        modifier = Modifier
                                            .clickable {
                                                mostrandoResponderPorPubId = mostrandoResponderPorPubId + (pub.id to true)
                                            }
                                            .padding(vertical = 4.dp)
                                    )
                                } else {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 4.dp)
                                    ) {
                                        CampoTexto(
                                            valor = textoResp,
                                            onValorChange = {
                                                respuestaTextoPorPubId = respuestaTextoPorPubId + (pub.id to it)
                                            },
                                            etiqueta = "Tu respuesta a ${pub.autorNombre}",
                                            placeholder = "Escribe tu respuesta..."
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.End
                                        ) {
                                            OutlinedButton(
                                                onClick = {
                                                    mostrandoResponderPorPubId = mostrandoResponderPorPubId + (pub.id to false)
                                                },
                                                shape = RoundedCornerShape(50.dp),
                                                border = BorderStroke(1.dp, LineaSuave),
                                                modifier = Modifier.height(34.dp)
                                            ) {
                                                Text("Cancelar", fontSize = 11.sp, color = TextoSecundario)
                                            }
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Button(
                                                onClick = {
                                                    if (textoResp.isNotBlank()) {
                                                        TramaStore.responderPublicacionForo(
                                                            publicacionId = pub.id,
                                                            autorNombre = nombreTienda,
                                                            esEmprendedor = true,
                                                            texto = textoResp.trim()
                                                        )
                                                        respuestaTextoPorPubId = respuestaTextoPorPubId + (pub.id to "")
                                                        mostrandoResponderPorPubId = mostrandoResponderPorPubId + (pub.id to false)
                                                        avisar("¡Respuesta enviada!")
                                                    }
                                                },
                                                enabled = textoResp.isNotBlank(),
                                                shape = RoundedCornerShape(50.dp),
                                                colors = ButtonDefaults.buttonColors(containerColor = RojoAlerce),
                                                modifier = Modifier.height(34.dp)
                                            ) {
                                                Text("Enviar Respuesta", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
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
    }
}



