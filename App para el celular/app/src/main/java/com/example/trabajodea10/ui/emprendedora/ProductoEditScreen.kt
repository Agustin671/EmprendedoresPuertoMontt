package com.example.trabajodea10.ui.emprendedora

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.PhotoCamera
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import com.example.trabajodea10.R
import coil.compose.AsyncImage
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

val PhonkFontFamily = FontFamily(Font(R.font.phonk_regular, FontWeight.Normal))
val BrittiFontFamily = FontFamily(Font(R.font.britti_bold, FontWeight.Bold))

enum class TipoFuente(val id: String, val etiqueta: String) {
    PHONK("phonk", "Phonk"),
    BRITTI("britti", "Britti Sans"),
    SANS("sans", "Sans Serif"),
    EDITORIAL("editorial", "Editorial")
}

fun obtenerFontFamily(tipo: TipoFuente): FontFamily {
    return when (tipo) {
        TipoFuente.PHONK -> PhonkFontFamily
        TipoFuente.BRITTI -> BrittiFontFamily
        TipoFuente.SANS -> FontFamily.SansSerif
        TipoFuente.EDITORIAL -> FontFamily.Serif
    }
}

data class OpcionColorFlyer(
    val id: String,
    val etiqueta: String,
    val color: Color?
)

val OpcionesColoresFlyer = listOf(
    OpcionColorFlyer("original", "Original", null),
    OpcionColorFlyer("carbon", "Carbón", Color(0xFF18181B)),
    OpcionColorFlyer("carmin", "Carmín", Color(0xFFDC2626)),
    OpcionColorFlyer("ambar", "Ámbar", Color(0xFFF59E0B)),
    OpcionColorFlyer("cobalto", "Cobalto", Color(0xFF1D4ED8)),
    OpcionColorFlyer("bosque", "Bosque", Color(0xFF047857)),
    OpcionColorFlyer("violeta", "Violeta", Color(0xFF7C3AED)),
    OpcionColorFlyer("fucsia", "Fucsia", Color(0xFFDB2777))
)

fun oscurecerColorCompose(color: Color, factor: Float = 0.4f): Color {
    return Color(
        red = (color.red * (1f - factor)).coerceIn(0f, 1f),
        green = (color.green * (1f - factor)).coerceIn(0f, 1f),
        blue = (color.blue * (1f - factor)).coerceIn(0f, 1f),
        alpha = color.alpha
    )
}

fun aclararColorCompose(color: Color, factor: Float = 0.4f): Color {
    return Color(
        red = (color.red + (1f - color.red) * factor).coerceIn(0f, 1f),
        green = (color.green + (1f - color.green) * factor).coerceIn(0f, 1f),
        blue = (color.blue + (1f - color.blue) * factor).coerceIn(0f, 1f),
        alpha = color.alpha
    )
}

enum class EstiloFlyer(
    val id: String,
    val nombre: String,
    val emoji: String
) {
    ESTILO_1("estilo1", "Estilo 1", "⚡"),
    ESTILO_2("estilo2", "Estilo 2", "🔥"),
    ESTILO_3("estilo3", "Estilo 3", "✨"),
    ESTILO_4("estilo4", "Estilo 4", "🏷️"),
    ESTILO_5("estilo5", "Estilo 5", "🌲")
}

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
    var estiloSeleccionado by remember { mutableStateOf(EstiloFlyer.ESTILO_1) }
    var colorSeleccionado by remember { mutableStateOf(OpcionesColoresFlyer[0]) }
    var fuenteSeleccionada by remember { mutableStateOf(TipoFuente.PHONK) }
    var selloFlyer by remember { mutableStateOf("EDICIÓN LIMITADA") }
    var tipoDisponibilidad by remember {
        mutableStateOf(if (original.variantes.any { it.stock > 0 }) "inmediato" else "pedido")
    }
    var detalles by remember { mutableStateOf("") }
    var fotoUri by remember { mutableStateOf<String?>(null) }

    val launcherGaleria = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            fotoUri = uri.toString()
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        BarraSuperior(
            titulo = if (esNuevo) "Nuevo producto" else "Editar producto",
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
            // SECCIÓN AFICHE
            // ==========================================
            Tarjeta {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Afiche",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = NegroApple
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Selector de Estilos
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        EstiloFlyer.entries.forEach { estilo ->
                            val activa = estilo == estiloSeleccionado
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (activa) NegroApple else SuperficieVariante,
                                border = BorderStroke(1.dp, if (activa) NegroApple else GrisBorde),
                                modifier = Modifier.clickable { estiloSeleccionado = estilo }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    Text(estilo.emoji, fontSize = 14.sp)
                                    Text(
                                        text = estilo.nombre,
                                        fontWeight = if (activa) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 12.sp,
                                        color = if (activa) Color.White else NegroApple
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Selector de Color
                    Text(
                        text = "Color",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        color = NegroApple
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OpcionesColoresFlyer.forEach { opcion ->
                            val activa = opcion == colorSeleccionado
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (activa) NegroApple else SuperficieVariante,
                                border = BorderStroke(1.dp, if (activa) NegroApple else GrisBorde),
                                modifier = Modifier.clickable { colorSeleccionado = opcion }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    if (opcion.color != null) {
                                        Box(
                                            modifier = Modifier
                                                .size(13.dp)
                                                .background(opcion.color, CircleShape)
                                                .border(1.dp, Color(0x33000000), CircleShape)
                                        )
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .size(13.dp)
                                                .background(
                                                    Brush.linearGradient(
                                                        listOf(Color(0xFF111111), Color(0xFF888888), Color(0xFFFFFFFF))
                                                    ),
                                                    CircleShape
                                                )
                                                .border(1.dp, Color(0x33000000), CircleShape)
                                        )
                                    }
                                    Text(
                                        text = opcion.etiqueta,
                                        fontWeight = if (activa) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 11.5.sp,
                                        color = if (activa) Color.White else NegroApple
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Selector de Tipografía
                    Text(
                        text = "Tipografía",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        color = NegroApple
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TipoFuente.entries.forEach { tipo ->
                            val activa = tipo == fuenteSeleccionada
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (activa) NegroApple else SuperficieVariante,
                                border = BorderStroke(1.dp, if (activa) NegroApple else GrisBorde),
                                modifier = Modifier.clickable { fuenteSeleccionada = tipo }
                            ) {
                                Text(
                                    text = tipo.etiqueta,
                                    fontFamily = obtenerFontFamily(tipo),
                                    fontWeight = if (activa) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 12.sp,
                                    color = if (activa) Color.White else NegroApple,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // LIENZO VIVO DEL FLYER (Letras detrás, producto al frente)
                    FlyerPreviewCard(
                        estilo = estiloSeleccionado,
                        titulo = nombre,
                        precio = precio,
                        sello = selloFlyer,
                        fotoUri = fotoUri,
                        color = colorSeleccionado.color,
                        fuente = fuenteSeleccionada
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Botón para subir / cambiar foto
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        BotonSecundario(
                            texto = if (fotoUri != null) "Cambiar foto" else "Subir foto del producto",
                            onClick = { launcherGaleria.launch("image/*") },
                            modifier = Modifier.weight(1f),
                            icono = { Icon(Icons.Filled.PhotoCamera, contentDescription = null, tint = NegroApple) }
                        )
                        if (fotoUri != null) {
                            BotonTexto(
                                texto = "Quitar foto",
                                color = RojoError,
                                onClick = { fotoUri = null }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    CampoTexto(
                        valor = selloFlyer,
                        onValorChange = { selloFlyer = it },
                        etiqueta = "Texto del afiche",
                        placeholder = "Ej: EDICIÓN LIMITADA o ¡HOY EN ALERCE!"
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    BotonSecundario(
                        texto = "Compartir afiche en WhatsApp",
                        onClick = {
                            val valorCLP = formatearPrecio(precio)
                            val textoCompartir = buildString {
                                append("🔥 *${selloFlyer.ifBlank { "¡OFERTA EN ALERCE!" }}* 🔥\n\n")
                                append("🛍️ *${nombre.ifBlank { "Producto" }}*\n")
                                append("💰 *Precio:* \$$valorCLP CLP\n")
                                if (detalles.isNotBlank()) {
                                    append("📝 ${detalles.trim()}\n")
                                }
                                append("📍 Alerce · Red Trama\n\n")
                                append("📲 *Consulta o pide directo por WhatsApp*")
                            }
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, textoCompartir)
                            }
                            context.startActivity(Intent.createChooser(intent, "Compartir afiche"))
                        },
                        icono = {
                            Icon(Icons.Filled.Share, contentDescription = null, tint = NegroApple)
                        }
                    )
                }
            }

            // ==========================================
            // SECCIÓN DATOS DEL PRODUCTO
            // ==========================================
            CampoTexto(
                valor = nombre,
                onValorChange = { nombre = it },
                etiqueta = "Nombre del producto",
                placeholder = "Ej: Pan amasado artesanal"
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                CampoTexto(
                    valor = precio,
                    onValorChange = { precio = it.filter { c -> c.isDigit() } },
                    etiqueta = "Precio ($)",
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
                            text = "CLP",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextoSecundario
                        )
                    }
                }
            }

            // Disponibilidad
            Text(
                text = "Disponibilidad",
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.5.sp,
                color = NegroApple
            )

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                val inmediato = tipoDisponibilidad == "inmediato"
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (inmediato) NegroApple else SuperficieVariante,
                    border = BorderStroke(1.dp, if (inmediato) NegroApple else GrisBorde),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { tipoDisponibilidad = "inmediato" }
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("Con stock", fontWeight = FontWeight.Bold, color = if (inmediato) Color.White else NegroApple, fontSize = 13.sp)
                        Text("Entrega hoy", fontSize = 11.sp, color = if (inmediato) Color(0xFFCCCCCC) else GrisTexto)
                    }
                }

                val pedido = tipoDisponibilidad == "pedido"
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (pedido) NegroApple else SuperficieVariante,
                    border = BorderStroke(1.dp, if (pedido) NegroApple else GrisBorde),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { tipoDisponibilidad = "pedido" }
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("A pedido", fontWeight = FontWeight.Bold, color = if (pedido) Color.White else NegroApple, fontSize = 13.sp)
                        Text("Por encargo", fontSize = 11.sp, color = if (pedido) Color(0xFFCCCCCC) else GrisTexto)
                    }
                }
            }

            // Categoría
            Text(
                text = "Categoría",
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.5.sp,
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

            CampoTexto(
                valor = detalles,
                onValorChange = { detalles = it },
                etiqueta = "Descripción (opcional)",
                placeholder = "Detalles para tus clientes o entrega"
            )

            Tarjeta {
                Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)) {
                    FilaSwitch(
                        titulo = "Publicado en tienda",
                        descripcion = "Visible para clientes",
                        checked = publicado,
                        onCheckedChange = { publicado = it }
                    )
                    FilaSwitch(
                        titulo = "Destacado en portada",
                        descripcion = "Aparece primero",
                        checked = destacado,
                        onCheckedChange = { destacado = it }
                    )
                }
            }

            BotonPrimario(
                texto = if (esNuevo) "Publicar producto" else "Guardar cambios",
                onClick = {
                    if (nombre.isBlank()) {
                        avisar("Escribe el nombre del producto")
                    } else if (precio.isBlank() || precio.toLongOrNull() == null || precio.toLong() <= 0) {
                        avisar("Ingresa un precio válido")
                    } else {
                        val stockFinal = if (tipoDisponibilidad == "inmediato") 15 else 0
                        val variantesFinales = listOf(Variante("${original.id}v1", "Unidad", stockFinal))

                        TramaStore.guardarProducto(
                            original.copy(
                                nombre = nombre.trim(),
                                precio = precio.toDoubleOrNull() ?: original.precio,
                                categoria = categoria,
                                publicado = publicado,
                                destacado = destacado,
                                variantes = variantesFinales
                            )
                        )
                        avisar("Producto guardado")
                        nav.volver()
                    }
                }
            )

            if (!esNuevo) {
                BotonTexto(
                    texto = "Eliminar producto",
                    color = RojoError,
                    onClick = {
                        TramaStore.eliminarProducto(original.id)
                        avisar("Producto eliminado")
                        nav.volver()
                    }
                )
            }

            Spacer(modifier = Modifier.height(56.dp))
        }
    }
}

// ============================================================
// COMPONENTE PREVIEW DE AFICHE EN VIVO (LETRAS DETRÁS, PRODUCTO AL FRENTE)
// ============================================================

@Composable
fun FlyerPreviewCard(
    estilo: EstiloFlyer,
    titulo: String,
    precio: String,
    sello: String,
    fotoUri: String?,
    color: Color? = null,
    fuente: TipoFuente = TipoFuente.PHONK
) {
    val tituloFinal = titulo.ifBlank { "Producto" }
    val precioFinal = formatearPrecio(precio)
    val selloFinal = sello.ifBlank { "OFERTA" }
    val fontFamily = obtenerFontFamily(fuente)

    when (estilo) {
        // ------------------------------------------------------------
        // ESTILO 1: SPLIT URBANO (Fondo dividido, letras verticales detrás, producto al frente)
        // ------------------------------------------------------------
        EstiloFlyer.ESTILO_1 -> {
            val colIzq = color ?: Color(0xFF33353A)
            val colDer = if (color != null) oscurecerColorCompose(color, 0.45f) else Color(0xFF0D0E10)
            val colLetrasAtras = if (color != null) aclararColorCompose(color, 0.25f) else Color(0xFF6E7178)

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(310.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = colDer)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    // Fondo dividido en 2 columnas
                    Row(modifier = Modifier.fillMaxSize()) {
                        Box(
                            modifier = Modifier
                                .weight(0.48f)
                                .fillMaxHeight()
                                .background(colIzq)
                        )
                        Box(
                            modifier = Modifier
                                .weight(0.52f)
                                .fillMaxHeight()
                                .background(colDer)
                        )
                    }

                    // CAPA DETRÁS: Letras gigantes verticales en el lado derecho
                    Column(
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(end = 12.dp)
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.SpaceAround,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        listOf("E", "D", "I", "T", "I", "O", "N").forEach { letra ->
                            Text(
                                text = letra,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Black,
                                color = colLetrasAtras,
                                fontFamily = fontFamily,
                                lineHeight = 28.sp
                            )
                        }
                    }

                    // CAPA AL FRENTE: Producto centrado flotando sobre la división y letras
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(start = 28.dp)
                            .size(170.dp)
                            .rotate(-14f),
                        contentAlignment = Alignment.Center
                    ) {
                        if (fotoUri != null) {
                            AsyncImage(
                                model = fotoUri,
                                contentDescription = null,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .shadow(16.dp, RoundedCornerShape(12.dp))
                                    .clip(RoundedCornerShape(12.dp)),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            // Elemento demostrativo con profundidad
                            Box(
                                modifier = Modifier
                                    .size(140.dp)
                                    .shadow(18.dp, RoundedCornerShape(20.dp))
                                    .background(Color.White, RoundedCornerShape(20.dp))
                                    .border(3.dp, Color(0xFFEEEEEE), RoundedCornerShape(20.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "👟",
                                    fontSize = 64.sp
                                )
                            }
                        }
                    }

                    // CAPA SUPERIOR: Textos y Precios en el lado izquierdo
                    Column(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(18.dp)
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = tituloFinal.uppercase(),
                                fontWeight = FontWeight.Black,
                                fontStyle = FontStyle.Italic,
                                fontSize = 22.sp,
                                fontFamily = fontFamily,
                                color = Color.White
                            )
                            Text(
                                text = selloFinal,
                                fontSize = 12.sp,
                                color = Color(0xFFE0E0E0),
                                fontStyle = FontStyle.Italic
                            )
                        }

                        // Caja de precio estilo poster urbano
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .background(Color.White, RoundedCornerShape(4.dp))
                                    .border(1.5.dp, Color.White, RoundedCornerShape(4.dp))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "$$precioFinal",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 17.sp,
                                    fontFamily = fontFamily,
                                    color = Color.Black
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("🔥", fontSize = 16.sp)
                        }
                    }
                }
            }
        }

        // ------------------------------------------------------------
        // ESTILO 2: IMPACTO TIPOGRÁFICO (Fondo en degradé, letras huecas gigantes detrás, producto al frente)
        // ------------------------------------------------------------
        EstiloFlyer.ESTILO_2 -> {
            val baseCol = color ?: Color(0xFFE59807)
            val gradientCol1 = if (color != null) aclararColorCompose(color, 0.18f) else Color(0xFFF3A410)
            val gradientCol2 = if (color != null) oscurecerColorCompose(color, 0.2f) else Color(0xFFD68700)
            val badgeBg = if (color != null) oscurecerColorCompose(color, 0.65f) else Color(0xFF421C05)
            val bannerBg = if (color != null) oscurecerColorCompose(color, 0.7f) else Color(0xFF381D0F)
            val textAccent = if (color != null) aclararColorCompose(color, 0.6f) else Color(0xFFFFC555)

            val palabras = tituloFinal.uppercase().split(" ").filter { it.isNotBlank() }
            val linea1 = if (palabras.isNotEmpty()) palabras[0].take(4) else "TOP"
            val linea2 = if (palabras.size > 1) palabras[1].take(4) else if (tituloFinal.length > 3) tituloFinal.uppercase().drop(3).take(4) else "OFR"

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(310.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = baseCol)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(gradientCol1, gradientCol2)
                            )
                        )
                ) {
                    // CAPA DETRÁS: Enormes letras en outline blanco detrás del producto
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 14.dp, vertical = 20.dp),
                        verticalArrangement = Arrangement.SpaceBetween,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = linea1,
                            fontSize = 76.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = fontFamily,
                            color = Color(0x35FFFFFF),
                            letterSpacing = 6.sp,
                            lineHeight = 76.sp
                        )
                        Text(
                            text = linea2,
                            fontSize = 76.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = fontFamily,
                            color = Color(0x35FFFFFF),
                            letterSpacing = 6.sp,
                            lineHeight = 76.sp
                        )
                    }

                    // CAPA AL FRENTE: Producto en el centro sobre las letras gigantes
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(190.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (fotoUri != null) {
                            AsyncImage(
                                model = fotoUri,
                                contentDescription = null,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .shadow(20.dp, RoundedCornerShape(18.dp))
                                    .clip(RoundedCornerShape(18.dp)),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(150.dp)
                                    .shadow(22.dp, CircleShape)
                                    .background(Color(0xFFFFF7E6), CircleShape)
                                    .border(4.dp, Color.White, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("🍔", fontSize = 72.sp)
                            }
                        }
                    }

                    // CAPA SUPERIOR: Badge de antojo en esquina superior derecha
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(14.dp)
                            .background(badgeBg, RoundedCornerShape(50))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = selloFinal.uppercase(),
                            color = textAccent,
                            fontWeight = FontWeight.Black,
                            fontFamily = fontFamily,
                            fontSize = 9.sp
                        )
                    }

                    // Badge de precio inferior izquierdo estilo banner chocolate
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(14.dp)
                            .background(bannerBg, RoundedCornerShape(10.dp))
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Column {
                            Text(
                                text = tituloFinal.uppercase(),
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                fontFamily = fontFamily,
                                color = textAccent
                            )
                            Text(
                                text = "$$precioFinal",
                                fontWeight = FontWeight.Black,
                                fontSize = 20.sp,
                                fontFamily = fontFamily,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }

        // ------------------------------------------------------------
        // ESTILO 3: SPLASH POP (Fondo vibrante, explosión de contraste detrás, producto al frente)
        // ------------------------------------------------------------
        EstiloFlyer.ESTILO_3 -> {
            val bgCol = color ?: Color(0xFF0055D4)
            val sunCol = if (color != null) aclararColorCompose(color, 0.5f) else Color(0xFFFFB800)
            val btnTextCol = if (color != null) oscurecerColorCompose(color, 0.6f) else Color(0xFF00388C)

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(310.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = bgCol)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(bgCol)
                ) {
                    // CAPA DETRÁS: Explosión / Sol orgánico de contraste
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(230.dp)
                            .rotate(18f)
                            .background(sunCol, RoundedCornerShape(55.dp))
                    )

                    // CAPA AL FRENTE: Producto en el centro
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(170.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (fotoUri != null) {
                            AsyncImage(
                                model = fotoUri,
                                contentDescription = null,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .shadow(16.dp, RoundedCornerShape(16.dp))
                                    .clip(RoundedCornerShape(16.dp)),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(136.dp)
                                    .shadow(18.dp, CircleShape)
                                    .background(Color.White, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("🥐", fontSize = 64.sp)
                            }
                        }
                    }

                    // Título superior estilo pop
                    Column(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "SUPER PROMO",
                            fontWeight = FontWeight.Black,
                            fontFamily = fontFamily,
                            fontSize = 11.sp,
                            color = Color.White,
                            letterSpacing = 2.sp
                        )
                        Text(
                            text = tituloFinal.uppercase(),
                            fontWeight = FontWeight.Black,
                            fontFamily = fontFamily,
                            fontSize = 20.sp,
                            color = sunCol
                        )
                    }

                    // Badge de descuento lateral dentado
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(end = 12.dp)
                            .rotate(12f)
                            .background(Color.White, RoundedCornerShape(10.dp))
                            .border(2.dp, bgCol, RoundedCornerShape(10.dp))
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(selloFinal, fontSize = 8.sp, fontWeight = FontWeight.Bold, fontFamily = fontFamily, color = bgCol)
                            Text("$$precioFinal", fontSize = 14.sp, fontWeight = FontWeight.Black, fontFamily = fontFamily, color = bgCol)
                        }
                    }

                    // Botón inferior
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 12.dp)
                            .background(sunCol, RoundedCornerShape(50))
                            .padding(horizontal = 24.dp, vertical = 7.dp)
                    ) {
                        Text(
                            text = "➔ PEDIR AHORA",
                            fontWeight = FontWeight.Black,
                            fontFamily = fontFamily,
                            fontSize = 12.sp,
                            color = btnTextCol
                        )
                    }
                }
            }
        }

        // ------------------------------------------------------------
        // ESTILO 4: EDITORIAL MINIMAL (Apple / Farfetch)
        // ------------------------------------------------------------
        EstiloFlyer.ESTILO_4 -> {
            val accentCol = color ?: Color(0xFF1A1A1A)

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(310.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F7F4)),
                border = BorderStroke(1.5.dp, accentCol)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "EDICIÓN ARTESANAL",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = fontFamily,
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
                                .size(96.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFEBE8E1)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (fotoUri != null) {
                                AsyncImage(
                                    model = fotoUri,
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Text("☕", fontSize = 36.sp)
                            }
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = accentCol
                            ) {
                                Text(
                                    text = selloFinal.uppercase(),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = fontFamily,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = tituloFinal,
                                fontWeight = FontWeight.Bold,
                                fontFamily = fontFamily,
                                fontSize = 17.sp,
                                color = Color(0xFF111111)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "$$precioFinal CLP",
                                fontWeight = FontWeight.SemiBold,
                                fontFamily = fontFamily,
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
                        text = "COMPRA DIRECTA · ALERCE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF222222)
                    )
                }
            }
        }

        // ------------------------------------------------------------
        // ESTILO 5: TRADICIÓN (Bosque nativo y acento dorado o dinámico)
        // ------------------------------------------------------------
        EstiloFlyer.ESTILO_5 -> {
            val bgCol = color ?: Color(0xFF163024)
            val goldCol = if (color != null) aclararColorCompose(color, 0.45f) else Color(0xFFD4AF37)
            val innerBoxCol = if (color != null) oscurecerColorCompose(color, 0.2f) else Color(0xFF224433)
            val subtitleCol = if (color != null) aclararColorCompose(color, 0.7f) else Color(0xFFA7D3BA)

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(310.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = bgCol),
                border = BorderStroke(2.dp, goldCol)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "TRADICIÓN Y SABOR",
                        fontWeight = FontWeight.Black,
                        fontFamily = fontFamily,
                        fontSize = 11.sp,
                        color = goldCol,
                        letterSpacing = 1.5.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Box(
                        modifier = Modifier
                            .size(90.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .border(2.dp, goldCol, RoundedCornerShape(10.dp))
                            .background(innerBoxCol),
                        contentAlignment = Alignment.Center
                    ) {
                        if (fotoUri != null) {
                            AsyncImage(
                                model = fotoUri,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Text("🌲", fontSize = 36.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = tituloFinal,
                        fontWeight = FontWeight.Bold,
                        fontFamily = fontFamily,
                        fontSize = 17.sp,
                        color = Color(0xFFFFF9E6),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    Surface(
                        shape = RoundedCornerShape(50),
                        color = goldCol
                    ) {
                        Text(
                            text = "$$precioFinal · ARTESANAL",
                            fontWeight = FontWeight.Black,
                            fontFamily = fontFamily,
                            fontSize = 13.sp,
                            color = bgCol,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Alerce · Puerto Montt",
                        fontSize = 10.5.sp,
                        color = subtitleCol
                    )
                }
            }
        }
    }
}

