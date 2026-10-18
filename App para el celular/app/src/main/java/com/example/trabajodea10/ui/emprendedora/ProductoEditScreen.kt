package com.example.trabajodea10.ui.emprendedora

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.trabajodea10.data.Categoria
import com.example.trabajodea10.data.TramaStore
import com.example.trabajodea10.data.Variante
import com.example.trabajodea10.ui.componentes.BarraSuperior
import com.example.trabajodea10.ui.componentes.BotonPrimario
import com.example.trabajodea10.ui.componentes.BotonSecundario
import com.example.trabajodea10.ui.componentes.BotonTexto
import com.example.trabajodea10.ui.componentes.CampoTexto
import com.example.trabajodea10.ui.componentes.ChipFiltro
import com.example.trabajodea10.ui.componentes.FilaSwitch
import com.example.trabajodea10.ui.componentes.Tarjeta
import com.example.trabajodea10.ui.componentes.TituloSeccion
import com.example.trabajodea10.ui.componentes.Vacio
import com.example.trabajodea10.ui.navegacion.ControladorNavegacion
import com.example.trabajodea10.ui.theme.Berenjena
import com.example.trabajodea10.ui.theme.GrisBorde
import com.example.trabajodea10.ui.theme.GrisTexto
import com.example.trabajodea10.ui.theme.NegroApple
import com.example.trabajodea10.ui.theme.RojoError
import com.example.trabajodea10.ui.theme.SuperficieVariante
import com.example.trabajodea10.ui.theme.TextoSecundario

enum class PlantillaFlyer(
    val id: String,
    val nombre: String,
    val emoji: String,
    val descripcion: String
) {
    BURGER_KING("burgerking", "Burger King", "🍔", "Fondo fuego, precio gigante amarillo"),
    MCDONALDS("mcdonalds", "McDonald's", "🍟", "Rojo vibrante y sellos dorados"),
    EDITORIAL("editorial", "Minimalista", "☕", "Estilo Apple / Farfetch, sobrio"),
    SURENO("sureno", "Tradición Sureña", "🌲", "Bosque nativo y 100% hecho a mano"),
    WHATSAPP("whatsapp", "WhatsApp Viral", "💬", "Para estados y grupos de venta")
}

private val COLORES_FOTO = listOf(
    0xFF8E6B94, 0xFF3E7D6E, 0xFFC07A3E, 0xFF6B7FB3, 0xFFB36B7F, 0xFF7FA66B, 0xFF7B3E7A
)

private fun formatearPrecio(precio: String): String {
    val valor = precio.filter { it.isDigit() }.toLongOrNull() ?: 0L
    return java.text.NumberFormat.getIntegerInstance(java.util.Locale("es", "CL")).format(valor)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaProductoDetalle(
    productoId: String,
    nav: ControladorNavegacion,
    avisar: (String) -> Unit
) {
    val context = LocalContext.current
    val original = TramaStore.productoPorId(productoId)
    if (original == null) {
        Column(modifier = Modifier.fillMaxSize()) {
            BarraSuperior(titulo = "Producto", onVolver = { nav.volver() })
            Vacio(
                titulo = "Producto no encontrado",
                mensaje = "Puede haber sido eliminado.",
                icono = { Icon(Icons.Filled.Delete, contentDescription = null, tint = TextoSecundario) }
            )
        }
        return
    }

    val esNuevo = original.nombre.isBlank()

    var nombre by remember(original.id) { mutableStateOf(original.nombre) }
    var precio by remember(original.id) {
        mutableStateOf(if (original.precio > 0) original.precio.toInt().toString() else "")
    }
    var categoria by remember(original.id) { mutableStateOf(original.categoria) }
    var publicado by remember(original.id) { mutableStateOf(original.publicado) }
    var destacado by remember(original.id) { mutableStateOf(original.destacado) }
    var colorFoto by remember(original.id) { mutableStateOf(original.colorFoto) }
    var variantes by remember(original.id) { mutableStateOf(original.variantes) }

    // Modos inspirados en Facebook Marketplace & Estudio de Flyers
    var plantillaSeleccionada by remember { mutableStateOf(PlantillaFlyer.BURGER_KING) }
    var selloFlyer by remember { mutableStateOf("¡OFERTA DEL DÍA!") }
    var tipoDisponibilidad by remember {
        mutableStateOf(if (original.variantes.any { it.stock > 0 }) "inmediato" else "pedido")
    }
    var detallesMarketplace by remember { mutableStateOf("") }

    val stockTotal = if (tipoDisponibilidad == "inmediato") {
        if (variantes.sumOf { it.stock } > 0) variantes.sumOf { it.stock } else 10
    } else 0

    Column(modifier = Modifier.fillMaxSize()) {
        BarraSuperior(
            titulo = if (esNuevo) "Añadir Producto" else "Editar Producto",
            subtitulo = if (esNuevo) "Estilo Facebook Marketplace · Generador de Afiches" else "Stock actual: $stockTotal",
            onVolver = { nav.volver() }
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // ==========================================
            // 1. ESTUDIO DE AFICHES COMERCIALES (FLYERS)
            // ==========================================
            Tarjeta {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "🎨 Estudio de Afiches Comerciales",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = NegroApple,
                            modifier = Modifier.weight(1f)
                        )
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = Color(0xFFFFC72C)
                        ) {
                            Text(
                                text = "NUEVO",
                                fontWeight = FontWeight.Black,
                                fontSize = 10.sp,
                                color = Color(0xFF801200),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Text(
                        text = "Elige una plantilla inspirada en cadenas comerciales para promocionar en Facebook o WhatsApp:",
                        style = MaterialTheme.typography.bodySmall,
                        color = GrisTexto,
                        modifier = Modifier.padding(top = 4.dp, bottom = 10.dp)
                    )

                    // Selector de Plantillas (Chips con emoji)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        PlantillaFlyer.entries.forEach { p ->
                            val activa = p == plantillaSeleccionada
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (activa) NegroApple else SuperficieVariante,
                                border = BorderStroke(1.dp, if (activa) NegroApple else GrisBorde),
                                modifier = Modifier.clickable { plantillaSeleccionada = p }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(p.emoji, fontSize = 16.sp)
                                    Text(
                                        text = p.nombre,
                                        fontWeight = if (activa) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 12.5.sp,
                                        color = if (activa) Color.White else NegroApple
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // LIENZO VIVO DEL FLYER
                    FlyerPreviewCard(
                        plantilla = plantillaSeleccionada,
                        titulo = nombre,
                        precio = precio,
                        sello = selloFlyer,
                        colorFoto = colorFoto
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Controles rápidos del flyer
                    CampoTexto(
                        valor = selloFlyer,
                        onValorChange = { selloFlyer = it },
                        etiqueta = "Sello o Frase Promocional del Afiche",
                        placeholder = "Ej: ¡OFERTA DEL DÍA! o 100% ARTESANAL"
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Color base para el producto:",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextoSecundario
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        COLORES_FOTO.forEach { color ->
                            val seleccionado = color == colorFoto
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .background(
                                        if (seleccionado) MaterialTheme.colorScheme.primaryContainer
                                        else SuperficieVariante,
                                        CircleShape
                                    )
                                    .padding(4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .background(Color(color), CircleShape)
                                        .clickable { colorFoto = color }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Botón para compartir afiche por WhatsApp / Grupos
                    BotonSecundario(
                        texto = "📲 Compartir Afiche en WhatsApp / Redes",
                        onClick = {
                            val valorCLP = formatearPrecio(precio)
                            val textoCompartir = buildString {
                                append("🔥 *${selloFlyer.ifBlank { "¡OFERTA ESPECIAL EN ALERCE!" }}* 🔥\n\n")
                                append("🛍️ *${nombre.ifBlank { "Producto Artesanal" }}*\n")
                                append("💰 *Precio:* \$$valorCLP CLP\n")
                                append("📦 *Disponibilidad:* ${if (tipoDisponibilidad == "inmediato") "Stock inmediato (Hoy)" else "Elaboración por encargo"}\n")
                                if (detallesMarketplace.isNotBlank()) {
                                    append("📝 *Detalle:* ${detallesMarketplace.trim()}\n")
                                }
                                append("📍 *Ubicación:* Alerce · Red Trama Puerto Montt\n\n")
                                append("📲 *¡Escríbeme por WhatsApp para reservar o pedir el tuyo!*")
                            }
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, textoCompartir)
                            }
                            context.startActivity(Intent.createChooser(intent, "Compartir Afiche Comercial"))
                        },
                        icono = {
                            Icon(Icons.Filled.Share, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        }
                    )
                }
            }

            // ==========================================
            // 2. DATOS ESTILO FACEBOOK MARKETPLACE
            // ==========================================
            TituloSeccion(texto = "2. Datos del Producto (Estilo Marketplace)")

            CampoTexto(
                valor = nombre,
                onValorChange = { nombre = it },
                etiqueta = "¿Qué estás vendiendo? (Título)",
                placeholder = "Ej: Pan amasado caliente recién horneado"
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                CampoTexto(
                    valor = precio,
                    onValorChange = { precio = it.filter { c -> c.isDigit() } },
                    etiqueta = "Precio ($ CLP)",
                    tipoTeclado = KeyboardType.Number,
                    modifier = Modifier.weight(1f),
                    placeholder = "2500"
                )

                Tarjeta(modifier = Modifier.weight(1f)) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp, horizontal = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "$${formatearPrecio(precio)}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = Berenjena
                        )
                        Text(
                            text = "pesos chilenos",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextoSecundario
                        )
                    }
                }
            }

            // ==========================================
            // 3. DISPONIBILIDAD / ENTREGA
            // ==========================================
            Text(
                text = "3. Disponibilidad / Entrega:",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = NegroApple
            )

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                val inmediato = tipoDisponibilidad == "inmediato"
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (inmediato) NegroApple else SuperficieVariante,
                    border = BorderStroke(1.dp, if (inmediato) NegroApple else GrisBorde),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { tipoDisponibilidad = "inmediato" }
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("📦 Con Stock", fontWeight = FontWeight.Bold, color = if (inmediato) Color.White else NegroApple)
                        Text("Para entrega hoy", fontSize = 11.sp, color = if (inmediato) Color(0xFFDDDDDD) else GrisTexto)
                    }
                }

                val pedido = tipoDisponibilidad == "pedido"
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (pedido) NegroApple else SuperficieVariante,
                    border = BorderStroke(1.dp, if (pedido) NegroApple else GrisBorde),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { tipoDisponibilidad = "pedido" }
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("✂️ A Pedido", fontWeight = FontWeight.Bold, color = if (pedido) Color.White else NegroApple)
                        Text("Se elabora por encargo", fontSize = 11.sp, color = if (pedido) Color(0xFFDDDDDD) else GrisTexto)
                    }
                }
            }

            // ==========================================
            // 4. CATEGORÍA
            // ==========================================
            Text(
                text = "4. Categoría del producto:",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = NegroApple
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Categoria.entries.forEach { opcion ->
                    ChipFiltro(
                        texto = opcion.etiqueta,
                        seleccionado = categoria == opcion,
                        onClick = { categoria = opcion }
                    )
                }
            }

            // ==========================================
            // 5. DETALLES ADICIONALES (OPCIONAL)
            // ==========================================
            CampoTexto(
                valor = detallesMarketplace,
                onValorChange = { detallesMarketplace = it },
                etiqueta = "5. Detalles para tus clientes (Opcional)",
                placeholder = "Ej: Bolsa de 5 unidades. Entregas en Alerce Histórico."
            )

            // ==========================================
            // 6. VISIBILIDAD EN TIENDA
            // ==========================================
            Tarjeta {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    FilaSwitch(
                        titulo = "Publicar en mi vitrina y catálogo",
                        descripcion = "Visible para los vecinos de Alerce",
                        checked = publicado,
                        onCheckedChange = { publicado = it }
                    )
                    FilaSwitch(
                        titulo = "Producto destacado en portada",
                        descripcion = "Aparece con una estrella en primer lugar",
                        checked = destacado,
                        onCheckedChange = { destacado = it }
                    )
                }
            }

            // ==========================================
            // BOTÓN GIGANTE PUBLICAR
            // ==========================================
            BotonPrimario(
                texto = if (esNuevo) "🚀 Publicar Producto en Mi Tienda" else "💾 Guardar Cambios",
                onClick = {
                    if (nombre.isBlank()) {
                        avisar("⚠️ Escribe el nombre de tu producto")
                    } else if (precio.isBlank() || precio.toLongOrNull() == null || precio.toLong() <= 0) {
                        avisar("⚠️ Ingresa un precio válido en pesos ($)")
                    } else {
                        val stockFinal = if (tipoDisponibilidad == "inmediato") {
                            if (variantes.sumOf { it.stock } > 0) variantes.sumOf { it.stock } else 15
                        } else 0

                        val variantesFinales = if (variantes.isEmpty()) {
                            listOf(Variante("${original.id}v1", "Unidad", stockFinal))
                        } else {
                            listOf(Variante("${original.id}v1", "Unidad", stockFinal))
                        }

                        TramaStore.guardarProducto(
                            original.copy(
                                nombre = nombre.trim(),
                                precio = precio.toDoubleOrNull() ?: original.precio,
                                categoria = categoria,
                                publicado = publicado,
                                destacado = destacado,
                                colorFoto = colorFoto,
                                variantes = variantesFinales
                            )
                        )
                        avisar("🎉 ¡Producto publicado con éxito en tu tienda!")
                        nav.volver()
                    }
                }
            )

            if (!esNuevo) {
                BotonTexto(
                    texto = "Eliminar este producto",
                    color = RojoError,
                    onClick = {
                        TramaStore.eliminarProducto(original.id)
                        avisar("Producto eliminado")
                        nav.volver()
                    }
                )
            }

            Spacer(modifier = Modifier.height(72.dp))
        }
    }
}

// ============================================================
// COMPONENTE PREVIEW DE AFICHE EN VIVO (BURGER KING, MCDONALDS, ETC)
// ============================================================

@Composable
fun FlyerPreviewCard(
    plantilla: PlantillaFlyer,
    titulo: String,
    precio: String,
    sello: String,
    colorFoto: Long
) {
    val tituloFinal = titulo.ifBlank { "Pan Amasado Caliente" }
    val precioFinal = formatearPrecio(precio)
    val selloFinal = sello.ifBlank { "¡OFERTA DEL DÍA!" }

    when (plantilla) {
        // PLANTILLA 1: BURGER KING PROMO (Cálido, precio amarillo gigante)
        PlantillaFlyer.BURGER_KING -> {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(310.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFD62300))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xFF801200), Color(0xFFD62300), Color(0xFF360600))
                            )
                        )
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Cintillo Sello Flame Grill
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFFFC72C))
                                .padding(vertical = 5.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "🔥 ${selloFinal.uppercase()} · ALERCE 🔥",
                                fontWeight = FontWeight.Black,
                                fontSize = 12.sp,
                                color = Color(0xFF801200)
                            )
                        }

                        // Contenido Central
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            // Círculo de producto
                            Box(
                                modifier = Modifier
                                    .size(92.dp)
                                    .background(Color(colorFoto), CircleShape)
                                    .border(3.dp, Color.White, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = tituloFinal.firstOrNull()?.uppercase() ?: "🍞",
                                    fontSize = 36.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = tituloFinal.uppercase(),
                                fontWeight = FontWeight.Black,
                                fontSize = 19.sp,
                                color = Color.White,
                                textAlign = TextAlign.Center,
                                maxLines = 2
                            )
                        }

                        // Pie con WhatsApp y Círculo Precio
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF1B0401))
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(Color(0xFF25D366), RoundedCornerShape(50))
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "💬 PEDIR POR WHATSAPP",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 12.sp,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }

                    // Sello Gigante de Precio Flotante
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(end = 16.dp)
                            .rotate(-8f)
                            .size(76.dp)
                            .background(Color(0xFFFFC72C), CircleShape)
                            .border(3.dp, Color(0xFF801200), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("SOLO", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color(0xFF801200))
                            Text(
                                text = "$$precioFinal",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF801200)
                            )
                            Text("CLP", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color(0xFF801200))
                        }
                    }
                }
            }
        }

        // PLANTILLA 2: MCDONALDS (Rojo vibrante, arcos y sellos dorados)
        PlantillaFlyer.MCDONALDS -> {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(310.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFDA291C))
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Cabecera McDonald's
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFFFC72C))
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "★ ${selloFinal.uppercase()} ★",
                            fontWeight = FontWeight.Black,
                            fontSize = 13.sp,
                            color = Color(0xFFDA291C)
                        )
                    }

                    // Card blanco central del producto
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(14.dp)
                            .background(Color.White, RoundedCornerShape(14.dp))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .background(Color(colorFoto), RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = tituloFinal.firstOrNull()?.uppercase() ?: "🍟",
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = tituloFinal.uppercase(),
                                    fontWeight = FontWeight.Black,
                                    fontSize = 17.sp,
                                    color = Color(0xFFDA291C),
                                    maxLines = 2
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = Color(0xFFFFC72C)
                                ) {
                                    Text(
                                        text = "$$precioFinal CLP",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 15.sp,
                                        color = Color(0xFFDA291C),
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Botón dorado McDonald's inferior
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF1E0402))
                            .padding(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFFFC72C), RoundedCornerShape(50))
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "📲 ¡LLEVA EL TUYO EN ALERCE!",
                                fontWeight = FontWeight.Black,
                                fontSize = 12.sp,
                                color = Color(0xFFDA291C)
                            )
                        }
                    }
                }
            }
        }

        // PLANTILLA 3: MINIMALISTA EDITORIAL (Farfetch & Apple Style)
        PlantillaFlyer.EDITORIAL -> {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(310.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F7F4)),
                border = BorderStroke(1.5.dp, Color(0xFF1A1A1A))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "TRAMA · EDICIÓN ARTESANAL LOCAL",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF666666),
                        letterSpacing = 2.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(90.dp)
                                .background(Color(colorFoto), RoundedCornerShape(6.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = tituloFinal.firstOrNull()?.uppercase() ?: "☕",
                                fontSize = 34.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Color(0xFF1A1A1A)
                            ) {
                                Text(
                                    text = selloFinal.uppercase(),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = tituloFinal,
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp,
                                color = Color(0xFF111111)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "$$precioFinal CLP",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp,
                                color = Color(0xFF111111)
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(Color(0xFFE0DED9))
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "COMPRA DIRECTA · HECHO A MANO EN ALERCE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF222222)
                    )
                }
            }
        }

        // PLANTILLA 4: TRADICIÓN SUREÑA (Bosque nativo y madera)
        PlantillaFlyer.SURENO -> {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(310.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF163024)),
                border = BorderStroke(2.dp, Color(0xFFD4AF37))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "🌲 TRADICIÓN Y SABOR DE ALERCE 🌲",
                        fontWeight = FontWeight.Black,
                        fontSize = 11.5.sp,
                        color = Color(0xFFD4AF37)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .background(Color(colorFoto), RoundedCornerShape(10.dp))
                            .border(2.dp, Color(0xFFD4AF37), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tituloFinal.firstOrNull()?.uppercase() ?: "🌲",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = tituloFinal,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = Color(0xFFFFF9E6),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    Surface(
                        shape = RoundedCornerShape(50),
                        color = Color(0xFFD4AF37)
                    ) {
                        Text(
                            text = "$$precioFinal · 100% ARTESANAL",
                            fontWeight = FontWeight.Black,
                            fontSize = 13.sp,
                            color = Color(0xFF163024),
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Hecho a mano con amor sureño · Puerto Montt",
                        fontSize = 10.5.sp,
                        color = Color(0xFFA7D3BA)
                    )
                }
            }
        }

        // PLANTILLA 5: WHATSAPP DIRECTO
        PlantillaFlyer.WHATSAPP -> {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(310.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE5DDD5))
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF075E54))
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "🟢 COMPRA DIRECTA · ALERCE",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = Color.White
                        )
                    }

                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(12.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(76.dp)
                                    .background(Color(colorFoto), RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = tituloFinal.firstOrNull()?.uppercase() ?: "🛍️",
                                    fontSize = 30.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = tituloFinal,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = Color(0xFF128C7E)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = Color(0xFF25D366)
                                ) {
                                    Text(
                                        text = "$$precioFinal",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 14.sp,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                            .background(Color(0xFF25D366), RoundedCornerShape(50))
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "💬 PEDIR DIRECTO AL WHATSAPP",
                            fontWeight = FontWeight.Black,
                            fontSize = 12.5.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

