package com.example.trabajodea10.ui.emprendedora

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.trabajodea10.data.AlAgotarse
import com.example.trabajodea10.data.Categoria
import com.example.trabajodea10.data.FotoProductoStore
import com.example.trabajodea10.data.Modalidad
import com.example.trabajodea10.data.Producto
import com.example.trabajodea10.data.ReglasCatalogo
import com.example.trabajodea10.data.TramaStore
import com.example.trabajodea10.data.Variante
import com.example.trabajodea10.data.formatearMoneda
import com.example.trabajodea10.ui.componentes.BarraSuperior
import com.example.trabajodea10.ui.componentes.BotonMicrofono
import com.example.trabajodea10.ui.componentes.BotonPrimario
import com.example.trabajodea10.ui.componentes.BotonSecundario
import com.example.trabajodea10.ui.componentes.BotonTexto
import com.example.trabajodea10.ui.componentes.CampoTexto
import com.example.trabajodea10.ui.componentes.ChipFiltro
import com.example.trabajodea10.ui.componentes.ContadorCantidad
import com.example.trabajodea10.ui.componentes.ContenedorChips
import com.example.trabajodea10.ui.componentes.FilaSwitch
import com.example.trabajodea10.ui.componentes.FotoProducto
import com.example.trabajodea10.ui.componentes.Tarjeta
import com.example.trabajodea10.ui.componentes.Vacio
import com.example.trabajodea10.ui.componentes.rememberDictadoPorVoz
import com.example.trabajodea10.ui.navegacion.ControladorNavegacion
import com.example.trabajodea10.ui.theme.NegroApple
import com.example.trabajodea10.ui.theme.RojoError
import com.example.trabajodea10.ui.theme.TextoSecundario
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

private val PASOS = listOf("Foto", "Datos", "Opciones", "Disponibilidad", "Revisar y publicar")

/**
 * Alta y edición guiada de un producto (RF-PRO-01): foto, datos, opciones,
 * disponibilidad y revisión final. Todo lo ingresado se guarda en el celular
 * a cada cambio, desde el primer paso y aunque falte el nombre (RNF-USA-04).
 * Recién al tocar «Guardar» queda en el catálogo y se sube al servidor.
 */
@Composable
fun PantallaProductoDetalle(
    productoId: String,
    nav: ControladorNavegacion,
    avisar: (String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val inicial = remember(productoId) {
        TramaStore.borrador?.takeIf { it.producto.id == productoId } ?: TramaStore.abrirEdicion(productoId)
    }
    if (inicial == null) {
        Column(modifier = Modifier.fillMaxSize()) {
            BarraSuperior(titulo = "Producto", onVolver = { nav.volver() })
            Vacio(titulo = "Producto no encontrado", mensaje = "Puede haber sido borrado.")
        }
        return
    }
    val esNuevo = inicial.esNuevo
    var producto by remember(productoId) { mutableStateOf(inicial.producto) }
    var paso by remember(productoId) { mutableStateOf(inicial.paso.coerceIn(0, PASOS.lastIndex)) }
    var problema by remember { mutableStateOf<String?>(null) }
    var confirmarDescartar by remember { mutableStateOf(false) }
    var confirmarBorrar by remember { mutableStateOf(false) }

    // Autoguardado: cada cambio queda en el celular (aunque se cierre la app).
    LaunchedEffect(producto, paso) {
        delay(300)
        TramaStore.actualizarBorrador(producto, paso)
    }

    // ---------- Fotos: cámara y galería, para el producto o para una opción ----------
    var destinoFoto by remember { mutableStateOf<String?>(null) } // null = producto; si no, id de la opción
    var archivoCamara by remember { mutableStateOf<File?>(null) }

    fun recibirFoto(uri: Uri) {
        val destino = destinoFoto
        scope.launch {
            val ruta = withContext(Dispatchers.IO) {
                FotoProductoStore.copiarDesdeUri(context, uri, producto.id + (destino?.let { "_$it" } ?: ""))
            }
            if (ruta == null) { avisar("No se pudo guardar la foto. Intenta nuevamente."); return@launch }
            producto = if (destino == null) producto.copy(fotoUrl = ruta)
            else producto.copy(variantes = producto.variantes.map { if (it.id == destino) it.copy(fotoUrl = ruta) else it })
        }
    }

    val galeria = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri -> uri?.let { recibirFoto(it) } }
    val camara = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { ok ->
        val archivo = archivoCamara
        if (ok && archivo != null && archivo.length() > 0) recibirFoto(Uri.fromFile(archivo))
        else if (!ok) avisar("No se tomó la foto.")
    }
    fun abrirCamara() {
        val archivo = archivoTemporalCamara(context)
        archivoCamara = archivo
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", archivo)
        runCatching { camara.launch(uri) }.onFailure { avisar("Este celular no tiene una app de cámara disponible.") }
    }
    val permisoCamara = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { concedido ->
        if (concedido) abrirCamara() else avisar("Sin permiso de cámara. Puedes elegir una foto de la galería.")
    }
    fun pedirCamara(destino: String?) {
        destinoFoto = destino
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) abrirCamara()
        else permisoCamara.launch(Manifest.permission.CAMERA)
    }
    fun pedirGaleria(destino: String?) {
        destinoFoto = destino
        galeria.launch("image/*")
    }

    if (confirmarDescartar) {
        AlertDialog(
            onDismissRequest = { confirmarDescartar = false },
            title = { Text(if (esNuevo) "¿Descartar este producto?" else "¿Descartar los cambios?", fontWeight = FontWeight.Bold) },
            text = { Text(if (esNuevo) "Se borrará lo que ingresaste." else "El producto queda como estaba antes de editarlo.") },
            confirmButton = { TextButton(onClick = { confirmarDescartar = false; TramaStore.descartarBorrador(); nav.volver() }) { Text("Sí, descartar", color = RojoError, fontWeight = FontWeight.Bold) } },
            dismissButton = { TextButton(onClick = { confirmarDescartar = false }) { Text("No, seguir") } }
        )
    }
    if (confirmarBorrar) {
        AlertDialog(
            onDismissRequest = { confirmarBorrar = false },
            title = { Text("¿Borrar «${producto.nombre}»?", fontWeight = FontWeight.Bold) },
            text = { Text("Se quitará de tu tienda y no se puede recuperar. Si solo se acabó o no lo tienes por ahora, mejor márcalo como no disponible.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmarBorrar = false
                    TramaStore.descartarBorrador()
                    TramaStore.eliminarProducto(producto.id)
                    avisar("Producto borrado")
                    nav.volver()
                }) { Text("Sí, borrar", color = RojoError, fontWeight = FontWeight.Bold) }
            },
            dismissButton = { TextButton(onClick = { confirmarBorrar = false }) { Text("No, dejarlo") } }
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        BarraSuperior(
            titulo = if (esNuevo) "Nuevo producto" else "Editar producto",
            subtitulo = "Paso ${paso + 1} de ${PASOS.size}: ${PASOS[paso]} · se guarda solo",
            onVolver = { nav.volver() }
        )
        LinearProgressIndicator(
            progress = { (paso + 1) / PASOS.size.toFloat() },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(PASOS[paso], fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.semantics { heading() })
            when (paso) {
                0 -> PasoFoto(producto, onCamara = { pedirCamara(null) }, onGaleria = { pedirGaleria(null) }, onQuitar = { producto = producto.copy(fotoUrl = null) })
                1 -> PasoDatos(producto) { producto = it }
                2 -> PasoOpciones(producto, onCambio = { producto = it }, onCamara = { pedirCamara(it) }, onGaleria = { pedirGaleria(it) })
                3 -> PasoDisponibilidad(producto) { producto = ReglasCatalogo.normalizar(it) }
                else -> PasoRevision(producto, onCambio = { producto = it }, onCompartir = { compartirProducto(context, producto) })
            }
            problema?.let { Text(it, color = RojoError, fontWeight = FontWeight.SemiBold) }
        }
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                BotonSecundario(
                    texto = "Atrás", enabled = paso > 0,
                    onClick = { problema = null; paso-- },
                    modifier = Modifier.weight(1f)
                )
                if (paso < PASOS.lastIndex) {
                    BotonPrimario(
                        texto = "Siguiente",
                        onClick = {
                            problema = problemaDelPaso(paso, producto)
                            if (problema == null) paso++
                        },
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    BotonPrimario(
                        texto = if (producto.publicado) "Guardar y mostrar" else "Guardar",
                        onClick = {
                            val p = ReglasCatalogo.normalizar(producto.copy(nombre = producto.nombre.trim()))
                            problema = ReglasCatalogo.validarProducto(p, TramaStore.tipoPorId(p.tipo))
                            if (problema == null) {
                                TramaStore.actualizarBorrador(p, paso)
                                val guardado = TramaStore.finalizarBorrador()
                                avisar(if (guardado != null && ReglasCatalogo.estadoPublico(guardado).name != "OCULTO") "Guardado. Ya se ve en tu tienda" else "Guardado. Por ahora el público no lo ve")
                                nav.volver()
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                BotonTexto(texto = if (esNuevo) "Descartar producto" else "Descartar cambios", color = TextoSecundario, onClick = { confirmarDescartar = true })
                if (!esNuevo) BotonTexto(texto = "Borrar producto", color = RojoError, onClick = { confirmarBorrar = true })
            }
        }
    }
}

/** Revisión ligera al avanzar; la validación completa se hace al guardar. */
private fun problemaDelPaso(paso: Int, p: Producto): String? = when (paso) {
    1 -> when {
        p.nombre.isBlank() -> "Escribe el nombre del producto."
        p.precio <= 0.0 -> "Escribe el precio, por ejemplo 2500."
        else -> null
    }
    2 -> p.variantes.indexOfFirst { it.nombre.isBlank() }.takeIf { it >= 0 }?.let { "Escribe el nombre de la opción ${it + 1}." }
        ?: if (p.modalidad == Modalidad.PIEZA_UNICA && ReglasCatalogo.unidades(p) > 1) "Una pieza única tiene una sola unidad en total." else null
    else -> null
}

private fun archivoTemporalCamara(context: Context): File {
    val carpeta = File(context.cacheDir, "camara").apply { mkdirs() }
    return File(carpeta, "foto_${System.currentTimeMillis()}.jpg")
}

private fun compartirProducto(context: Context, p: Producto) {
    val texto = buildString {
        append("🛍️ *${p.nombre}*\n")
        append("💰 ${formatearMoneda(p.precio)}\n")
        if (p.descripcion.isNotBlank()) append("📝 ${p.descripcion}\n")
        append("📍 Alerce")
    }
    val intent = Intent(Intent.ACTION_SEND).apply { type = "text/plain"; putExtra(Intent.EXTRA_TEXT, texto) }
    context.startActivity(Intent.createChooser(intent, "Compartir producto"))
}

// ============================================================ Paso 1: foto

@Composable
private fun PasoFoto(p: Producto, onCamara: () -> Unit, onGaleria: () -> Unit, onQuitar: () -> Unit) {
    Text("Una buena foto ayuda a vender. Puedes tomarla ahora o elegirla de tu galería.", color = TextoSecundario)
    FotoProducto(
        fotoUrl = p.fotoUrl, colorRespaldo = p.colorFoto, inicial = p.nombre.ifBlank { "?" },
        modifier = Modifier.fillMaxWidth().height(220.dp), forma = RoundedCornerShape(16.dp), tamanoInicial = 48.sp
    )
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        BotonPrimario(texto = "Tomar foto", onClick = onCamara, modifier = Modifier.weight(1f),
            icono = { Icon(Icons.Filled.PhotoCamera, contentDescription = null) })
        BotonSecundario(texto = "Galería", onClick = onGaleria, modifier = Modifier.weight(1f),
            icono = { Icon(Icons.Filled.PhotoLibrary, contentDescription = null, tint = NegroApple) })
    }
    if (p.fotoUrl != null) BotonTexto(texto = "Quitar foto", color = RojoError, onClick = onQuitar)
}

// ============================================================ Paso 2: datos

@Composable
private fun PasoDatos(p: Producto, onCambio: (Producto) -> Unit) {
    val dictarNombre = rememberDictadoPorVoz { texto -> onCambio(p.copy(nombre = texto.replaceFirstChar { it.uppercase() })) }
    val dictarDescripcion = rememberDictadoPorVoz { texto ->
        onCambio(p.copy(descripcion = if (p.descripcion.isBlank()) texto.replaceFirstChar { it.uppercase() } else p.descripcion.trimEnd() + " " + texto))
    }
    CampoTexto(valor = p.nombre, onValorChange = { onCambio(p.copy(nombre = it.take(90))) }, etiqueta = "¿Qué vendes?",
        placeholder = "Ej: Pan amasado", iconoFinal = { BotonMicrofono(onClick = dictarNombre) })
    CampoTexto(
        valor = if (p.precio > 0) p.precio.toLong().toString() else "",
        onValorChange = { t -> onCambio(p.copy(precio = t.filter { it.isDigit() }.take(9).toDoubleOrNull() ?: 0.0)) },
        etiqueta = "Precio en pesos", placeholder = "2500", tipoTeclado = KeyboardType.Number
    )
    CampoTexto(valor = p.descripcion, onValorChange = { onCambio(p.copy(descripcion = it.take(600))) }, etiqueta = "Cuéntale a tus clientas",
        placeholder = "De qué está hecho, tamaño, cómo se entrega", esMultilinea = true, iconoFinal = { BotonMicrofono(onClick = dictarDescripcion) })

    Text("Categoría", fontWeight = FontWeight.SemiBold)
    ContenedorChips(modifier = Modifier.fillMaxWidth()) {
        Categoria.entries.forEach { c ->
            ChipFiltro(texto = c.etiqueta, seleccionado = p.categoria == c, onClick = {
                onCambio(p.copy(categoria = c, tipo = if (TramaStore.tipoPorId(p.tipo)?.categoria == c.name) p.tipo else null, atributos = if (TramaStore.tipoPorId(p.tipo)?.categoria == c.name) p.atributos else emptyMap()))
            })
        }
    }

    // Tipo y campos específicos (RF-PRO-03): los define la administración en el servidor.
    val tipos = TramaStore.tiposProducto.filter { it.categoria == p.categoria.name }
    Text("Tipo de producto", fontWeight = FontWeight.SemiBold)
    if (tipos.isEmpty()) {
        Text("La administración aún no definió tipos para «${p.categoria.etiqueta}».", color = TextoSecundario, style = MaterialTheme.typography.bodySmall)
    } else {
        ContenedorChips(modifier = Modifier.fillMaxWidth()) {
            ChipFiltro(texto = "Sin tipo", seleccionado = p.tipo == null, onClick = { onCambio(p.copy(tipo = null, atributos = emptyMap())) })
            tipos.forEach { t -> ChipFiltro(texto = t.nombre, seleccionado = p.tipo == t.id, onClick = { onCambio(p.copy(tipo = t.id, atributos = emptyMap())) }) }
        }
        TramaStore.tipoPorId(p.tipo)?.campos?.forEach { campo ->
            val valor = p.atributos[campo.clave].orEmpty()
            fun poner(nuevo: String) = onCambio(p.copy(atributos = if (nuevo.isBlank()) p.atributos - campo.clave else p.atributos + (campo.clave to nuevo)))
            if (campo.tipo == "opciones" && campo.opciones.isNotEmpty()) {
                Text(campo.etiqueta + if (campo.obligatorio) " *" else "", fontWeight = FontWeight.Medium)
                ContenedorChips(modifier = Modifier.fillMaxWidth()) {
                    campo.opciones.forEach { o -> ChipFiltro(texto = o, seleccionado = valor == o, onClick = { poner(if (valor == o) "" else o) }) }
                }
            } else {
                CampoTexto(valor = valor, onValorChange = { poner(it.take(120)) }, etiqueta = campo.etiqueta + if (campo.obligatorio) " *" else "",
                    tipoTeclado = if (campo.tipo == "numero") KeyboardType.Number else KeyboardType.Text)
            }
        }
    }
}

// ============================================================ Paso 3: opciones

@Composable
private fun PasoOpciones(p: Producto, onCambio: (Producto) -> Unit, onCamara: (String) -> Unit, onGaleria: (String) -> Unit) {
    Text(
        if (p.modalidad == Modalidad.PIEZA_UNICA) "Una pieza única tiene una sola unidad en total, aunque tenga varias opciones."
        else "Si tienes tallas, colores o sabores, agrega una opción por cada uno. Cada opción puede tener su propio precio, foto y cantidad.",
        color = TextoSecundario
    )
    p.variantes.forEachIndexed { i, v ->
        fun cambiar(nueva: Variante) = onCambio(p.copy(variantes = p.variantes.map { if (it.id == v.id) nueva else it }))
        Tarjeta {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Opción ${i + 1}", fontWeight = FontWeight.Bold)
                CampoTexto(valor = v.nombre, onValorChange = { cambiar(v.copy(nombre = it.take(60))) }, etiqueta = "Nombre", placeholder = "Ej: Talla M, Azul, Frambuesa")
                CampoTexto(
                    valor = v.precio?.toLong()?.toString().orEmpty(),
                    onValorChange = { t -> cambiar(v.copy(precio = t.filter { it.isDigit() }.take(9).toDoubleOrNull())) },
                    etiqueta = "Precio propio (opcional)", placeholder = "Igual al del producto: ${formatearMoneda(p.precio)}",
                    tipoTeclado = KeyboardType.Number
                )
                if (p.manejaStock && p.modalidad != Modalidad.A_PEDIDO) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("¿Cuántos tienes?", fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                        ContadorCantidad(cantidad = v.stock, onCambio = { cambiar(v.copy(stock = it.coerceAtLeast(0))) },
                            maximo = if (p.modalidad == Modalidad.PIEZA_UNICA) 1 else 9999)
                    }
                } else {
                    Text(if (p.modalidad == Modalidad.A_PEDIDO) "A pedido: no lleva cuenta de unidades." else "Sin cuenta de unidades.", color = TextoSecundario, style = MaterialTheme.typography.bodySmall)
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FotoProducto(fotoUrl = v.fotoUrl, colorRespaldo = p.colorFoto, inicial = v.nombre.ifBlank { "?" },
                        modifier = Modifier.size(56.dp), forma = RoundedCornerShape(10.dp), tamanoInicial = 18.sp)
                    BotonSecundario(texto = "Foto", onClick = { onCamara(v.id) }, modifier = Modifier.weight(1f),
                        icono = { Icon(Icons.Filled.PhotoCamera, contentDescription = "Tomar foto de esta opción", tint = NegroApple) })
                    BotonSecundario(texto = "Galería", onClick = { onGaleria(v.id) }, modifier = Modifier.weight(1f),
                        icono = { Icon(Icons.Filled.PhotoLibrary, contentDescription = "Elegir foto de esta opción", tint = NegroApple) })
                }
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    if (v.fotoUrl != null) BotonTexto(texto = "Quitar foto", color = TextoSecundario, onClick = { cambiar(v.copy(fotoUrl = null)) })
                    if (p.variantes.size > 1) BotonTexto(texto = "Quitar opción", color = RojoError, onClick = { onCambio(p.copy(variantes = p.variantes.filterNot { it.id == v.id })) })
                }
            }
        }
    }
    BotonSecundario(texto = "Agregar opción", onClick = {
        val id = "${p.id}_v" + java.util.UUID.randomUUID().toString().take(8)
        onCambio(p.copy(variantes = p.variantes + Variante(id, "", 0)))
    })
}

// ============================================================ Paso 4: disponibilidad

@Composable
private fun PasoDisponibilidad(p: Producto, onCambio: (Producto) -> Unit) {
    Text("Modalidad", fontWeight = FontWeight.SemiBold)
    ContenedorChips(modifier = Modifier.fillMaxWidth()) {
        Modalidad.entries.forEach { m -> ChipFiltro(texto = m.etiqueta, seleccionado = p.modalidad == m, onClick = {
            onCambio(if (m == Modalidad.PIEZA_UNICA) p.copy(modalidad = m, variantes = p.variantes.mapIndexed { i, v -> v.copy(stock = if (i == 0) 1 else 0) }) else p.copy(modalidad = m))
        }) }
    }
    Text(p.modalidad.explicacion, color = TextoSecundario, style = MaterialTheme.typography.bodySmall)
    Tarjeta {
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)) {
            FilaSwitch(
                titulo = "Llevar la cuenta de unidades",
                descripcion = when {
                    p.modalidad == Modalidad.PIEZA_UNICA -> "Una pieza única siempre cuenta su unidad"
                    p.manejaStock -> "Cada venta descuenta de la opción vendida"
                    else -> "No se descuenta nada al vender"
                },
                checked = p.manejaStock,
                onCheckedChange = { if (p.modalidad != Modalidad.PIEZA_UNICA) onCambio(p.copy(manejaStock = it)) }
            )
            FilaSwitch(
                titulo = "Disponible",
                descripcion = if (p.disponible) "Se muestra según lo que elijas al publicar" else "No disponible: sigue en tu tienda, pero el público no lo ve",
                checked = p.disponible,
                onCheckedChange = { onCambio(p.copy(disponible = it)) }
            )
        }
    }
    val aplicaAgotar = p.manejaStock && p.modalidad == Modalidad.REGULAR
    Text("Cuando se acaben las unidades", fontWeight = FontWeight.SemiBold)
    if (aplicaAgotar) {
        ContenedorChips(modifier = Modifier.fillMaxWidth()) {
            AlAgotarse.entries.forEach { a -> ChipFiltro(texto = a.etiqueta, seleccionado = p.alAgotarse == a, onClick = { onCambio(p.copy(alAgotarse = a)) }) }
        }
        Text("Se aplica cuando no quedan unidades en ninguna opción. Una opción sin unidades no se puede vender de inmediato.", color = TextoSecundario, style = MaterialTheme.typography.bodySmall)
    } else {
        Text(
            when (p.modalidad) {
                Modalidad.PIEZA_UNICA -> "Una pieza única vendida se oculta para siempre."
                Modalidad.A_PEDIDO -> "Los productos a pedido no se agotan."
                else -> "Sin cuenta de unidades no se agota."
            }, color = TextoSecundario, style = MaterialTheme.typography.bodySmall
        )
    }
}

// ============================================================ Paso 5: revisión

@Composable
private fun PasoRevision(p: Producto, onCambio: (Producto) -> Unit, onCompartir: () -> Unit) {
    Tarjeta {
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)) {
            FilaSwitch(titulo = "Lo ven todos", descripcion = if (p.publicado) "Se publica en tu tienda" else "Solo tú lo ves por ahora",
                checked = p.publicado, onCheckedChange = { onCambio(p.copy(publicado = it)) })
            FilaSwitch(titulo = "Mostrar primero", descripcion = "Aparece al inicio de tu tienda",
                checked = p.destacado, onCheckedChange = { onCambio(p.copy(destacado = it)) })
        }
    }
    Text("Así lo verán tus clientas", fontWeight = FontWeight.SemiBold)
    TarjetaVistaPrevia(producto = p)
    BotonSecundario(texto = "Compartir por WhatsApp", onClick = onCompartir,
        icono = { Icon(Icons.Filled.Share, contentDescription = null, tint = NegroApple) })
    Spacer(modifier = Modifier.height(4.dp))
}
