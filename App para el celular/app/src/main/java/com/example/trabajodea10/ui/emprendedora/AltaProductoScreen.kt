package com.example.trabajodea10.ui.emprendedora

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.trabajodea10.data.AlAgotarse
import com.example.trabajodea10.data.Categoria
import com.example.trabajodea10.data.EstadoPublico
import com.example.trabajodea10.data.FormularioProducto
import com.example.trabajodea10.data.FotoProductoStore
import com.example.trabajodea10.data.Modalidad
import com.example.trabajodea10.data.Producto
import com.example.trabajodea10.data.ReglasCatalogo
import com.example.trabajodea10.data.TramaStore
import com.example.trabajodea10.data.Variante
import com.example.trabajodea10.data.formatearMoneda
import com.example.trabajodea10.data.remoto.Sincronizador
import com.example.trabajodea10.ui.componentes.BarraSuperior
import com.example.trabajodea10.ui.componentes.BotonMicrofono
import com.example.trabajodea10.ui.componentes.ContadorCantidad
import com.example.trabajodea10.ui.componentes.ContenedorChips
import com.example.trabajodea10.ui.componentes.FotoProducto
import com.example.trabajodea10.ui.componentes.Tarjeta
import com.example.trabajodea10.ui.componentes.Vacio
import com.example.trabajodea10.ui.componentes.rememberDictadoPorVoz
import com.example.trabajodea10.ui.navegacion.ControladorNavegacion
import com.example.trabajodea10.ui.theme.Berenjena
import com.example.trabajodea10.ui.theme.GrisBorde
import com.example.trabajodea10.ui.theme.NegroApple
import com.example.trabajodea10.ui.theme.RojoError
import com.example.trabajodea10.ui.theme.TextoSecundario
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

/** Las cinco etapas del alta (A01): foto, datos, opciones, disponibilidad y publicación. */
private val PASOS = listOf("Foto", "Datos", "Opciones", "Disponibilidad", "Revisión")

/**
 * Alta y edición guiada de un producto (RF-PRO-01). Todo lo ingresado se
 * guarda en el celular a cada cambio, desde el primer paso y aunque falte el
 * nombre (RNF-USA-04). Recién al tocar «Guardar» queda en «Mis productos» y
 * entra a la cola para subir al servidor.
 *
 * Las preguntas simples (¿tiene opciones?, ¿cómo lo vendes?) solo cambian lo
 * que se muestra: no borran datos ni cambian cantidades. Las reglas de la
 * pieza única se aplican al guardar (ReglasCatalogo.normalizar).
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
    // Los errores se muestran junto al campo después de intentar avanzar o guardar.
    var mostrarErrores by remember { mutableStateOf(false) }
    var menuAbierto by remember { mutableStateOf(false) }
    var confirmarDescartar by remember { mutableStateOf(false) }
    var confirmarBorrar by remember { mutableStateOf(false) }

    val tipo = TramaStore.tipoPorId(producto.tipo)
    val erroresPaso = if (mostrarErrores) FormularioProducto.erroresDelPaso(paso, producto, tipo) else emptyList()
    val desplazamiento = rememberScrollState()

    // Autoguardado en el celular: cada cambio queda aunque se cierre la app.
    LaunchedEffect(producto, paso) {
        delay(300)
        TramaStore.actualizarBorrador(producto, paso)
    }
    LaunchedEffect(paso) { desplazamiento.scrollTo(0) }

    // El botón atrás del celular vuelve al paso anterior; desde el primero sale (lo ingresado queda guardado).
    BackHandler(enabled = paso > 0) { mostrarErrores = false; paso-- }

    // ---------- Fotos: cámara y galería, para el producto o para una opción ----------
    var destinoFoto by remember { mutableStateOf<String?>(null) } // null = producto; si no, id de la opción
    var archivoCamara by remember { mutableStateOf<File?>(null) }

    fun recibirFoto(uri: Uri) {
        val destino = destinoFoto
        scope.launch {
            val ruta = withContext(Dispatchers.IO) {
                FotoProductoStore.copiarDesdeUri(context, uri, producto.id + (destino?.let { "_$it" } ?: ""))
            }
            if (ruta == null) { avisar("No se pudo guardar la foto. Intenta otra vez."); return@launch }
            producto = if (destino == null) producto.copy(fotoUrl = ruta)
            else producto.copy(variantes = producto.variantes.map { if (it.id == destino) it.copy(fotoUrl = ruta) else it })
        }
    }

    val galeria = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri -> uri?.let { recibirFoto(it) } }
    val camara = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { ok ->
        val archivo = archivoCamara
        if (ok && archivo != null && archivo.length() > 0) recibirFoto(Uri.fromFile(archivo))
    }
    fun abrirCamara() {
        val archivo = archivoTemporalCamara(context)
        archivoCamara = archivo
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", archivo)
        runCatching { camara.launch(uri) }.onFailure { avisar("Este celular no tiene una app de cámara disponible.") }
    }
    val permisoCamara = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { concedido ->
        if (concedido) abrirCamara() else avisar("Sin permiso para la cámara. Puedes elegir una foto de la galería.")
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

    fun avanzar() {
        val errores = FormularioProducto.erroresDelPaso(paso, producto, tipo)
        if (errores.isEmpty()) { mostrarErrores = false; paso++ } else mostrarErrores = true
    }

    fun guardar() {
        val p = producto.copy(nombre = producto.nombre.trim())
        val errores = FormularioProducto.errores(p, tipo)
        if (errores.isNotEmpty()) {
            // Lleva al primer paso con problemas y marca el campo.
            mostrarErrores = true
            paso = errores.minOf { it.paso }
            return
        }
        TramaStore.actualizarBorrador(p, paso)
        val guardado = TramaStore.finalizarBorrador() ?: return
        avisar(FormularioProducto.mensajeGuardado(Sincronizador.ventasConServidor, guardado))
        nav.volver()
    }

    if (confirmarDescartar) {
        AlertDialog(
            onDismissRequest = { confirmarDescartar = false },
            title = { Text(if (esNuevo) "¿Descartar este producto?" else "¿Descartar los cambios?", fontWeight = FontWeight.Bold) },
            text = { Text(if (esNuevo) "Se borrará lo que ingresaste, incluidas las fotos nuevas." else "El producto queda como estaba antes de editarlo.") },
            confirmButton = { TextButton(onClick = { confirmarDescartar = false; TramaStore.descartarBorrador(); nav.volver() }) { Text("Sí, descartar", color = RojoError, fontWeight = FontWeight.Bold) } },
            dismissButton = { TextButton(onClick = { confirmarDescartar = false }) { Text("No, seguir") } }
        )
    }
    if (confirmarBorrar) {
        AlertDialog(
            onDismissRequest = { confirmarBorrar = false },
            title = { Text("¿Borrar «${producto.nombre}»?", fontWeight = FontWeight.Bold) },
            text = { Text("Se quitará de Mis productos y no se puede recuperar. Si solo se acabó o no lo tienes por ahora, mejor márcalo como no disponible en el paso Disponibilidad.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmarBorrar = false
                    TramaStore.descartarBorrador()
                    TramaStore.eliminarProducto(producto.id)
                    avisar("Producto borrado de Mis productos")
                    nav.volver()
                }) { Text("Sí, borrar", color = RojoError, fontWeight = FontWeight.Bold) }
            },
            dismissButton = { TextButton(onClick = { confirmarBorrar = false }) { Text("No, dejarlo") } }
        )
    }

    // Compartir solo lo que el público ya puede ver (la versión guardada, no la que se está editando).
    val guardadoPublico = if (esNuevo) null else TramaStore.productoPorId(productoId)?.takeIf { ReglasCatalogo.estadoPublico(it) != EstadoPublico.OCULTO }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.ime.exclude(WindowInsets.navigationBars))
    ) {
        BarraSuperior(
            titulo = if (esNuevo) "Nuevo producto" else "Editar producto",
            subtitulo = "Se guarda solo en tu celular",
            onVolver = { nav.volver() },
            acciones = {
                Box {
                    IconButton(onClick = { menuAbierto = true }, modifier = Modifier.size(48.dp)) {
                        Icon(Icons.Filled.MoreVert, contentDescription = "Más acciones")
                    }
                    DropdownMenu(expanded = menuAbierto, onDismissRequest = { menuAbierto = false }) {
                        guardadoPublico?.let { p ->
                            DropdownMenuItem(text = { Text("Compartir por WhatsApp") }, onClick = { menuAbierto = false; compartirProducto(context, p) })
                        }
                        DropdownMenuItem(text = { Text(if (esNuevo) "Descartar producto" else "Descartar cambios") }, onClick = { menuAbierto = false; confirmarDescartar = true })
                        if (!esNuevo) DropdownMenuItem(text = { Text("Borrar producto", color = RojoError) }, onClick = { menuAbierto = false; confirmarBorrar = true })
                    }
                }
            }
        )
        IndicadorPasos(paso = paso, onIrA = { destino -> if (destino < paso) { mostrarErrores = false; paso = destino } })

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(desplazamiento)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            when (paso) {
                0 -> PasoFoto(producto, onCamara = { pedirCamara(null) }, onGaleria = { pedirGaleria(null) }, onQuitar = { producto = producto.copy(fotoUrl = null) })
                1 -> PasoDatos(producto, erroresPaso) { producto = it }
                2 -> PasoOpciones(producto, erroresPaso, onCambio = { producto = it }, onCamara = { pedirCamara(it) }, onGaleria = { pedirGaleria(it) })
                3 -> PasoDisponibilidad(producto, erroresPaso, onCambio = { producto = it }, onIrAOpciones = { paso = 2 })
                else -> PasoRevision(producto, onCambio = { producto = it })
            }
        }

        // Una acción principal por etapa; «Atrás» queda como secundaria.
        Surface(shadowElevation = 6.dp, color = MaterialTheme.colorScheme.surface) {
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                if (paso == PASOS.lastIndex) {
                    Text(
                        if (ReglasCatalogo.estadoPublico(producto) == EstadoPublico.OCULTO) "Se guardará en Mis productos. Solo tú lo verás."
                        else "Se guardará en Mis productos y lo verá el público.",
                        style = MaterialTheme.typography.bodyMedium, color = TextoSecundario
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    if (paso > 0) {
                        BotonSecundario(texto = "Atrás", onClick = { mostrarErrores = false; paso-- }, modifier = Modifier.weight(0.38f))
                    }
                    if (paso < PASOS.lastIndex) {
                        BotonPrimario(texto = "Siguiente", onClick = { avanzar() }, modifier = Modifier.weight(0.62f))
                    } else {
                        BotonPrimario(texto = "Guardar en Mis productos", onClick = { guardar() }, modifier = Modifier.weight(0.62f))
                    }
                }
            }
        }
    }
}

// ============================================================ Piezas comunes

/**
 * Indicador de las cinco etapas: un círculo por paso (número, o ✓ si ya se
 * completó) y debajo el nombre completo del paso actual. Así no se corta el
 * texto con letra grande y el estado no depende solo del color.
 */
@Composable
private fun IndicadorPasos(paso: Int, onIrA: (Int) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Row(modifier = Modifier.fillMaxWidth()) {
            PASOS.forEachIndexed { i, nombre ->
                val estado = when { i < paso -> "completado"; i == paso -> "actual"; else -> "pendiente" }
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 48.dp)
                        .then(if (i < paso) Modifier.selectable(selected = false, role = Role.Tab, onClick = { onIrA(i) }) else Modifier)
                        .semantics(mergeDescendants = true) {
                            contentDescription = "Paso ${i + 1} de ${PASOS.size}: $nombre"
                            stateDescription = estado
                        }
                ) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = if (i <= paso) NegroApple else Color.Transparent,
                        border = if (i > paso) BorderStroke(1.dp, GrisBorde) else null
                    ) {
                        Box(modifier = Modifier.size(30.dp), contentAlignment = Alignment.Center) {
                            if (i < paso) Icon(Icons.Filled.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                            else Text("${i + 1}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = if (i == paso) Color.White else TextoSecundario)
                        }
                    }
                }
            }
        }
        Text(
            "Paso ${paso + 1} de ${PASOS.size}: ${PASOS[paso]}",
            fontWeight = FontWeight.SemiBold, color = NegroApple,
            modifier = Modifier.clearAndSetSemantics { }
        )
    }
}

/**
 * Botones del asistente: mismo aspecto que los de la app, pero con alto mínimo
 * de 52 dp que crece con la letra del sistema (el texto nunca se corta).
 */
@Composable
private fun BotonPrimario(texto: String, onClick: () -> Unit, modifier: Modifier = Modifier, icono: (@Composable () -> Unit)? = null) {
    Button(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().heightIn(min = 52.dp),
        shape = RoundedCornerShape(50),
        colors = ButtonDefaults.buttonColors(containerColor = NegroApple, contentColor = Color.White),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 18.dp, vertical = 10.dp)
    ) {
        icono?.invoke()
        if (icono != null) Spacer(modifier = Modifier.width(8.dp))
        Text(texto, fontWeight = FontWeight.Bold, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
    }
}

@Composable
private fun BotonSecundario(texto: String, onClick: () -> Unit, modifier: Modifier = Modifier, icono: (@Composable () -> Unit)? = null) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().heightIn(min = 52.dp),
        shape = RoundedCornerShape(50),
        border = BorderStroke(1.dp, GrisBorde),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = NegroApple),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp, vertical = 10.dp)
    ) {
        icono?.invoke()
        if (icono != null) Spacer(modifier = Modifier.width(8.dp))
        Text(texto, fontWeight = FontWeight.SemiBold, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
    }
}

@Composable
private fun TituloPaso(titulo: String, ayuda: String? = null) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(titulo, fontWeight = FontWeight.Bold, fontSize = 20.sp, modifier = Modifier.semantics { heading() })
        ayuda?.let { Text(it, color = TextoSecundario, style = MaterialTheme.typography.bodyMedium) }
    }
}

@Composable
private fun Subtitulo(texto: String) {
    Text(texto, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, modifier = Modifier.semantics { heading() })
}

/**
 * Campo con etiqueta permanente arriba (no desaparece al escribir), ejemplo
 * dentro del campo y error justo debajo, anunciado por el lector de pantalla.
 */
@Composable
private fun CampoEtiquetado(
    etiqueta: String,
    valor: String,
    onValorChange: (String) -> Unit,
    ejemplo: String,
    modifier: Modifier = Modifier,
    ayuda: String? = null,
    error: String? = null,
    teclado: KeyboardType = KeyboardType.Text,
    mayusculas: KeyboardCapitalization = KeyboardCapitalization.Sentences,
    multilinea: Boolean = false,
    prefijo: String? = null,
    iconoFinal: (@Composable () -> Unit)? = null
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(etiqueta, fontWeight = FontWeight.SemiBold, modifier = Modifier.clearAndSetSemantics { })
        OutlinedTextField(
            value = valor,
            onValueChange = onValorChange,
            modifier = Modifier
                .fillMaxWidth()
                .semantics {
                    contentDescription = etiqueta
                    if (error != null) error(error)
                },
            placeholder = { Text(ejemplo) },
            prefix = prefijo?.let { { Text(it) } },
            isError = error != null,
            supportingText = (error ?: ayuda)?.let { texto -> { Text(texto, color = if (error != null) RojoError else TextoSecundario) } },
            singleLine = !multilinea,
            minLines = if (multilinea) 3 else 1,
            maxLines = if (multilinea) 6 else 1,
            keyboardOptions = KeyboardOptions(capitalization = mayusculas, keyboardType = teclado, imeAction = if (multilinea) ImeAction.Default else ImeAction.Next),
            trailingIcon = iconoFinal,
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Berenjena, unfocusedBorderColor = GrisBorde)
        )
    }
}

/** Fila de elección única (radio) con área táctil completa y anuncio de selección. */
@Composable
private fun OpcionRadio(titulo: String, descripcion: String?, seleccionada: Boolean, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(if (seleccionada) 2.dp else 1.dp, if (seleccionada) NegroApple else GrisBorde),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .fillMaxWidth()
            .selectable(selected = seleccionada, role = Role.RadioButton, onClick = onClick)
    ) {
        Row(modifier = Modifier.heightIn(min = 56.dp).padding(horizontal = 8.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            RadioButton(selected = seleccionada, onClick = null)
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(titulo, fontWeight = if (seleccionada) FontWeight.Bold else FontWeight.Medium)
                descripcion?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = TextoSecundario) }
            }
        }
    }
}

@Composable
private fun GrupoRadios(contenido: @Composable () -> Unit) {
    Column(modifier = Modifier.selectableGroup(), verticalArrangement = Arrangement.spacedBy(8.dp)) { contenido() }
}

/** Interruptor con la fila completa tocable y estado leído por el lector de pantalla. */
@Composable
private fun FilaInterruptor(titulo: String, descripcion: String, activo: Boolean, onCambio: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .toggleable(value = activo, role = Role.Switch, onValueChange = onCambio)
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(titulo, fontWeight = FontWeight.Medium)
            Text(descripcion, style = MaterialTheme.typography.bodySmall, color = TextoSecundario)
        }
        Spacer(modifier = Modifier.width(12.dp))
        Switch(checked = activo, onCheckedChange = null, colors = SwitchDefaults.colors(checkedTrackColor = Berenjena))
    }
}

/** Chip con marca visible al estar elegido (no depende solo del color). */
@Composable
private fun ChipElegible(texto: String, elegido: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = elegido,
        onClick = onClick,
        label = { Text(texto) },
        leadingIcon = if (elegido) { { Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(18.dp)) } } else null,
        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = NegroApple, selectedLabelColor = Color.White, selectedLeadingIconColor = Color.White)
    )
}

@Composable
private fun AvisoError(mensaje: String, accion: String? = null, onAccion: () -> Unit = {}) {
    Surface(shape = RoundedCornerShape(12.dp), color = Color(0xFFFDECEC), modifier = Modifier.fillMaxWidth().semantics { error(mensaje) }) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(mensaje, color = RojoError, fontWeight = FontWeight.SemiBold)
            if (accion != null) OutlinedButton(onClick = onAccion, modifier = Modifier.heightIn(min = 48.dp)) { Text(accion) }
        }
    }
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
    TituloPaso("Foto del producto", "Una foto clara ayuda a vender. Si no la tienes ahora, puedes agregarla después.")
    if (p.fotoUrl != null) {
        FotoProducto(
            fotoUrl = p.fotoUrl, colorRespaldo = p.colorFoto, inicial = p.nombre.ifBlank { " " },
            modifier = Modifier.fillMaxWidth().height(240.dp).semantics { contentDescription = "Foto elegida del producto" },
            forma = RoundedCornerShape(16.dp), tamanoInicial = 40.sp
        )
    } else {
        Surface(
            shape = RoundedCornerShape(16.dp), border = BorderStroke(1.dp, GrisBorde), color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth().height(170.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                Icon(Icons.Filled.PhotoCamera, contentDescription = null, tint = TextoSecundario, modifier = Modifier.size(40.dp))
                Spacer(modifier = Modifier.height(6.dp))
                Text("Aún no tiene foto", color = TextoSecundario)
            }
        }
    }
    BotonPrimario(texto = if (p.fotoUrl == null) "Tomar foto" else "Tomar otra foto", onClick = onCamara,
        icono = { Icon(Icons.Filled.PhotoCamera, contentDescription = null) })
    BotonSecundario(texto = if (p.fotoUrl == null) "Elegir de la galería" else "Cambiar por una de la galería", onClick = onGaleria,
        icono = { Icon(Icons.Filled.PhotoLibrary, contentDescription = null, tint = NegroApple) })
    if (p.fotoUrl != null) {
        TextButton(onClick = onQuitar, modifier = Modifier.heightIn(min = 48.dp)) { Text("Quitar la foto", color = TextoSecundario) }
    }
}

// ============================================================ Paso 2: datos

@Composable
private fun PasoDatos(p: Producto, errores: List<FormularioProducto.Error>, onCambio: (Producto) -> Unit) {
    val dictarNombre = rememberDictadoPorVoz { texto -> onCambio(p.copy(nombre = texto.replaceFirstChar { it.uppercase() })) }
    val dictarDescripcion = rememberDictadoPorVoz { texto ->
        onCambio(p.copy(descripcion = if (p.descripcion.isBlank()) texto.replaceFirstChar { it.uppercase() } else p.descripcion.trimEnd() + " " + texto))
    }
    fun error(c: FormularioProducto.Campo) = errores.firstOrNull { it.campo == c }?.mensaje

    TituloPaso("Cuéntanos qué vendes")
    CampoEtiquetado(
        etiqueta = "Nombre del producto", valor = p.nombre, onValorChange = { onCambio(p.copy(nombre = it.take(90))) },
        ejemplo = "Ej: Pan amasado", error = error(FormularioProducto.Campo.NOMBRE),
        iconoFinal = { BotonMicrofono(onClick = dictarNombre) }
    )
    CampoEtiquetado(
        etiqueta = "Precio", valor = if (p.precio > 0) p.precio.toLong().toString() else "",
        onValorChange = { t -> onCambio(p.copy(precio = t.filter { it.isDigit() }.take(9).toDoubleOrNull() ?: 0.0)) },
        ejemplo = "2500", prefijo = "$ ", teclado = KeyboardType.Number, mayusculas = KeyboardCapitalization.None,
        ayuda = "En pesos, sin puntos.", error = error(FormularioProducto.Campo.PRECIO)
    )
    CampoEtiquetado(
        etiqueta = "Descripción (opcional)", valor = p.descripcion, onValorChange = { onCambio(p.copy(descripcion = it.take(600))) },
        ejemplo = "Ej: hecho con harina integral, para 6 personas", multilinea = true,
        iconoFinal = { BotonMicrofono(onClick = dictarDescripcion) }
    )

    Subtitulo("Categoría")
    ContenedorChips(modifier = Modifier.fillMaxWidth(), espacioHorizontal = 8.dp) {
        Categoria.entries.forEach { c ->
            ChipElegible(texto = c.etiqueta, elegido = p.categoria == c, onClick = {
                // Al cambiar de categoría se conserva el tipo solo si pertenece a la nueva categoría.
                val tipoSigue = TramaStore.tipoPorId(p.tipo)?.categoria == c.name
                onCambio(p.copy(categoria = c, tipo = if (tipoSigue) p.tipo else null, atributos = if (tipoSigue) p.atributos else emptyMap()))
            })
        }
    }

    // Tipo y campos específicos (RF-PRO-03): los define la administración en el servidor.
    val tipos = TramaStore.tiposProducto.filter { it.categoria == p.categoria.name }
    Subtitulo("Tipo de producto")
    if (tipos.isEmpty()) {
        Text(
            "Todavía no hay tipos definidos para «${p.categoria.etiqueta}». Cuando la administración los agregue, podrás elegirlos aquí con sus datos propios.",
            color = TextoSecundario, style = MaterialTheme.typography.bodyMedium
        )
    } else {
        ContenedorChips(modifier = Modifier.fillMaxWidth(), espacioHorizontal = 8.dp) {
            ChipElegible(texto = "Sin tipo", elegido = p.tipo == null, onClick = { onCambio(p.copy(tipo = null)) })
            tipos.forEach { t -> ChipElegible(texto = t.nombre, elegido = p.tipo == t.id, onClick = { onCambio(p.copy(tipo = t.id)) }) }
        }
        TramaStore.tipoPorId(p.tipo)?.campos?.forEach { campo ->
            val valor = p.atributos[campo.clave].orEmpty()
            val err = errores.firstOrNull { it.clave == campo.clave }?.mensaje
            fun poner(nuevo: String) = onCambio(p.copy(atributos = if (nuevo.isBlank()) p.atributos - campo.clave else p.atributos + (campo.clave to nuevo)))
            if (campo.tipo == "opciones" && campo.opciones.isNotEmpty()) {
                Text(campo.etiqueta + if (campo.obligatorio) " (obligatorio)" else "", fontWeight = FontWeight.SemiBold)
                ContenedorChips(modifier = Modifier.fillMaxWidth(), espacioHorizontal = 8.dp) {
                    campo.opciones.forEach { o -> ChipElegible(texto = o, elegido = valor == o, onClick = { poner(if (valor == o) "" else o) }) }
                }
                err?.let { Text(it, color = RojoError, modifier = Modifier.semantics { error(it) }) }
            } else {
                CampoEtiquetado(
                    etiqueta = campo.etiqueta + if (campo.obligatorio) " (obligatorio)" else "", valor = valor, onValorChange = { poner(it.take(120)) },
                    ejemplo = "", error = err, teclado = if (campo.tipo == "numero") KeyboardType.Number else KeyboardType.Text
                )
            }
        }
    }
}

// ============================================================ Paso 3: opciones

@Composable
private fun PasoOpciones(
    p: Producto,
    errores: List<FormularioProducto.Error>,
    onCambio: (Producto) -> Unit,
    onCamara: (String) -> Unit,
    onGaleria: (String) -> Unit
) {
    val conOpciones = FormularioProducto.tieneOpciones(p)
    var perdidas by remember { mutableStateOf<List<String>?>(null) }
    var quitar by remember { mutableStateOf<Variante?>(null) }

    perdidas?.let { lista ->
        AlertDialog(
            onDismissRequest = { perdidas = null },
            title = { Text("¿Dejarlo como uno solo?", fontWeight = FontWeight.Bold) },
            text = { Text("Se quitará:\n" + lista.joinToString("\n") { "• $it" } + "\n\nSe mantiene la primera opción con su cantidad.") },
            confirmButton = { TextButton(onClick = { perdidas = null; onCambio(FormularioProducto.simplificar(p)) }) { Text("Sí, dejar uno solo", color = RojoError, fontWeight = FontWeight.Bold) } },
            dismissButton = { TextButton(onClick = { perdidas = null }) { Text("No, mantener las opciones") } }
        )
    }
    quitar?.let { v ->
        AlertDialog(
            onDismissRequest = { quitar = null },
            title = { Text("¿Quitar «${v.nombre.ifBlank { "esta opción" }}»?", fontWeight = FontWeight.Bold) },
            text = { Text("Se perderán su cantidad (${v.stock})" + (if (v.precio != null) ", su precio" else "") + (if (v.fotoUrl != null) " y su foto" else "") + ".") },
            confirmButton = { TextButton(onClick = { quitar = null; onCambio(p.copy(variantes = p.variantes.filterNot { it.id == v.id })) }) { Text("Sí, quitar", color = RojoError, fontWeight = FontWeight.Bold) } },
            dismissButton = { TextButton(onClick = { quitar = null }) { Text("No") } }
        )
    }

    TituloPaso("¿Tiene distintos tamaños, colores o sabores?")
    GrupoRadios {
        OpcionRadio("No, es uno solo", "Usa la foto, el precio y la cantidad del producto.", seleccionada = !conOpciones, onClick = {
            if (conOpciones) {
                val lista = FormularioProducto.perdidasAlSimplificar(p)
                if (lista.isEmpty()) onCambio(FormularioProducto.simplificar(p)) else perdidas = lista
            }
        })
        OpcionRadio("Sí, tiene opciones", "Cada opción puede tener su propia cantidad, precio y foto.", seleccionada = conOpciones, onClick = {
            if (!conOpciones) onCambio(FormularioProducto.activarOpciones(p))
        })
    }

    if (!conOpciones) {
        Text(
            if (FormularioProducto.pideCantidades(p)) "La cantidad la indicas en el paso siguiente." else "Puedes seguir al paso siguiente.",
            color = TextoSecundario, style = MaterialTheme.typography.bodyMedium
        )
        return
    }

    errores.firstOrNull { it.campo == FormularioProducto.Campo.PIEZA_UNICA }?.let { AvisoError(it.mensaje) }
    p.variantes.forEachIndexed { i, v ->
        TarjetaOpcion(
            p = p, v = v, numero = i + 1,
            error = errores.firstOrNull { it.opcionId == v.id }?.mensaje,
            puedeQuitar = p.variantes.size > 1,
            onCambio = { nueva -> onCambio(p.copy(variantes = p.variantes.map { if (it.id == v.id) nueva else it })) },
            onQuitar = { quitar = v },
            onCamara = { onCamara(v.id) }, onGaleria = { onGaleria(v.id) }
        )
    }
    BotonSecundario(texto = "Agregar otra opción", onClick = {
        val id = "${p.id}_v" + java.util.UUID.randomUUID().toString().take(8)
        onCambio(p.copy(variantes = p.variantes + Variante(id, "", 0)))
    })
}

@Composable
private fun TarjetaOpcion(
    p: Producto,
    v: Variante,
    numero: Int,
    error: String?,
    puedeQuitar: Boolean,
    onCambio: (Variante) -> Unit,
    onQuitar: () -> Unit,
    onCamara: () -> Unit,
    onGaleria: () -> Unit
) {
    var precioPropio by remember(v.id) { mutableStateOf(v.precio != null) }
    Tarjeta {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Opción $numero", fontWeight = FontWeight.Bold, modifier = Modifier.semantics { heading() })
            CampoEtiquetado(
                etiqueta = "Nombre de la opción", valor = v.nombre, onValorChange = { onCambio(v.copy(nombre = it.take(60))) },
                ejemplo = "Ej: Talla M, Azul, Frambuesa", error = error
            )
            if (FormularioProducto.pideCantidades(p)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("¿Cuántos tienes?", fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                    ContadorCantidad(cantidad = v.stock, onCambio = { onCambio(v.copy(stock = it.coerceAtLeast(0))) }, maximo = 9999)
                }
            }

            // Precio: por defecto usa el del producto; personalizarlo es una elección explícita.
            if (!precioPropio) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Precio: ${formatearMoneda(p.precio)} (el del producto)", modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                    TextButton(onClick = { precioPropio = true }, modifier = Modifier.heightIn(min = 48.dp)) { Text("Ponerle otro") }
                }
            } else {
                CampoEtiquetado(
                    etiqueta = "Precio de esta opción", valor = v.precio?.toLong()?.toString().orEmpty(),
                    onValorChange = { t -> onCambio(v.copy(precio = t.filter { it.isDigit() }.take(9).toDoubleOrNull())) },
                    ejemplo = p.precio.toLong().toString(), prefijo = "$ ", teclado = KeyboardType.Number, mayusculas = KeyboardCapitalization.None,
                    ayuda = "Si lo dejas vacío, usa el del producto."
                )
                TextButton(onClick = { precioPropio = false; onCambio(v.copy(precio = null)) }, modifier = Modifier.heightIn(min = 48.dp)) {
                    Text("Usar el precio del producto")
                }
            }

            // Foto: si no tiene una propia, se muestra la foto principal.
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                FotoProducto(
                    fotoUrl = v.fotoUrl ?: p.fotoUrl, colorRespaldo = p.colorFoto, inicial = v.nombre.ifBlank { " " },
                    modifier = Modifier.size(64.dp), forma = RoundedCornerShape(12.dp), tamanoInicial = 20.sp
                )
                Text(
                    when { v.fotoUrl != null -> "Foto propia de esta opción"; p.fotoUrl != null -> "Usa la foto principal"; else -> "Sin foto" },
                    modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                BotonSecundario(texto = "Tomar foto", onClick = onCamara, modifier = Modifier.weight(1f),
                    icono = { Icon(Icons.Filled.PhotoCamera, contentDescription = null, tint = NegroApple) })
                BotonSecundario(texto = "Galería", onClick = onGaleria, modifier = Modifier.weight(1f),
                    icono = { Icon(Icons.Filled.PhotoLibrary, contentDescription = null, tint = NegroApple) })
            }
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                if (v.fotoUrl != null) TextButton(onClick = { onCambio(v.copy(fotoUrl = null)) }, modifier = Modifier.heightIn(min = 48.dp)) { Text("Usar la foto principal") }
                else Spacer(modifier = Modifier.width(1.dp))
                if (puedeQuitar) TextButton(onClick = onQuitar, modifier = Modifier.heightIn(min = 48.dp)) { Text("Quitar opción", color = TextoSecundario) }
            }
        }
    }
}

// ============================================================ Paso 4: disponibilidad

@Composable
private fun PasoDisponibilidad(
    p: Producto,
    errores: List<FormularioProducto.Error>,
    onCambio: (Producto) -> Unit,
    onIrAOpciones: () -> Unit
) {
    val conOpciones = FormularioProducto.tieneOpciones(p)
    TituloPaso("¿Cómo lo vendes?")
    GrupoRadios {
        OpcionRadio("Lo tengo listo", "Se vende de inmediato mientras te queden.", p.modalidad == Modalidad.REGULAR) {
            onCambio(FormularioProducto.cambiarModalidad(p, Modalidad.REGULAR))
        }
        OpcionRadio("Lo hago a pedido", "Lo preparas cuando te lo encargan. No se acaba.", p.modalidad == Modalidad.A_PEDIDO) {
            onCambio(FormularioProducto.cambiarModalidad(p, Modalidad.A_PEDIDO))
        }
        OpcionRadio("Es una pieza única", "Hay una sola. Cuando la vendas, deja de mostrarse para siempre.", p.modalidad == Modalidad.PIEZA_UNICA) {
            onCambio(FormularioProducto.cambiarModalidad(p, Modalidad.PIEZA_UNICA))
        }
    }

    if (p.modalidad == Modalidad.REGULAR) {
        FilaInterruptor(
            titulo = "Llevar la cuenta de cuántos tengo",
            descripcion = if (p.manejaStock) "Cada venta descuenta de lo que tienes" else "No se descuenta nada al vender",
            activo = p.manejaStock, onCambio = { onCambio(p.copy(manejaStock = it)) }
        )
    }

    if (FormularioProducto.pideCantidades(p)) {
        errores.firstOrNull { it.campo == FormularioProducto.Campo.PIEZA_UNICA }?.let { e ->
            AvisoError(e.mensaje, accion = if (conOpciones) "Revisar las cantidades de las opciones" else null, onAccion = onIrAOpciones)
        }
        if (conOpciones) {
            Subtitulo("Cantidades")
            Text(p.variantes.joinToString(" · ") { "${it.nombre.ifBlank { "Sin nombre" }}: ${it.stock}" }, style = MaterialTheme.typography.bodyMedium)
            TextButton(onClick = onIrAOpciones, modifier = Modifier.heightIn(min = 48.dp)) { Text("Cambiar en Opciones") }
        } else p.variantes.firstOrNull()?.let { v ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(if (p.modalidad == Modalidad.PIEZA_UNICA) "¿La tienes ahora?" else "¿Cuántos tienes?", fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                ContadorCantidad(
                    cantidad = v.stock,
                    onCambio = { onCambio(p.copy(variantes = listOf(v.copy(stock = it.coerceAtLeast(0))) + p.variantes.drop(1))) },
                    maximo = if (p.modalidad == Modalidad.PIEZA_UNICA) maxOf(1, v.stock) else 9999
                )
            }
        }
    }

    if (FormularioProducto.pideAlAgotarse(p)) {
        Subtitulo("Cuando se acaben")
        GrupoRadios {
            OpcionRadio("Mostrar «Se acabó»", "Sigue a la vista, marcado como agotado.", p.alAgotarse == AlAgotarse.MOSTRAR_AGOTADO) { onCambio(p.copy(alAgotarse = AlAgotarse.MOSTRAR_AGOTADO)) }
            OpcionRadio("Dejar de mostrarlo al público", "Sigue en Mis productos; solo deja de verse.", p.alAgotarse == AlAgotarse.OCULTAR) { onCambio(p.copy(alAgotarse = AlAgotarse.OCULTAR)) }
            OpcionRadio("Mostrarlo como «A pedido»", "Lo pueden encargar aunque no te quede.", p.alAgotarse == AlAgotarse.PASAR_A_PEDIDO) { onCambio(p.copy(alAgotarse = AlAgotarse.PASAR_A_PEDIDO)) }
        }
        if (conOpciones) Text("Se aplica cuando no queda ninguna opción. Una opción sin unidades no se vende de inmediato.", style = MaterialTheme.typography.bodySmall, color = TextoSecundario)
    }

    Subtitulo("¿Está disponible para vender?")
    FilaInterruptor(
        titulo = if (p.disponible) "Sí, está disponible" else "No disponible por ahora",
        descripcion = if (p.disponible) "Quién lo ve lo eliges en el paso siguiente." else "Sigue en Mis productos, pero el público no lo ve.",
        activo = p.disponible, onCambio = { onCambio(p.copy(disponible = it)) }
    )
}

// ============================================================ Paso 5: revisión

@Composable
private fun PasoRevision(p: Producto, onCambio: (Producto) -> Unit) {
    TituloPaso("Revisa tu producto")
    Subtitulo("¿Quién lo ve?")
    GrupoRadios {
        OpcionRadio("Solo yo lo veo", "Queda en Mis productos; el público no lo ve.", !p.publicado) { onCambio(p.copy(publicado = false)) }
        OpcionRadio("Lo ven todos", "Aparece en tu tienda.", p.publicado) { onCambio(p.copy(publicado = true)) }
    }
    val estado = ReglasCatalogo.estadoPublico(p)
    if (p.publicado && estado == EstadoPublico.OCULTO) {
        Text("Aunque elegiste «Lo ven todos», no se mostrará: ${ReglasCatalogo.motivoOculto(p)?.replaceFirstChar { it.lowercase() }}.",
            color = Color(0xFF8A4B08), fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
    }
    Subtitulo("Así lo verán")
    // El motivo ya se explicó arriba: la ficha no lo repite.
    TarjetaVistaPrevia(producto = ReglasCatalogo.normalizar(p), mostrarMotivoOculto = !(p.publicado && estado == EstadoPublico.OCULTO))
    Text(resumen(p), style = MaterialTheme.typography.bodyMedium, color = TextoSecundario)
    FilaInterruptor(titulo = "Mostrar primero en mi tienda", descripcion = "Aparece al inicio de tu tienda", activo = p.destacado, onCambio = { onCambio(p.copy(destacado = it)) })
}

private fun resumen(p: Producto): String = buildString {
    append(when (p.modalidad) { Modalidad.REGULAR -> "Lo tienes listo"; Modalidad.A_PEDIDO -> "A pedido"; Modalidad.PIEZA_UNICA -> "Pieza única" })
    if (FormularioProducto.pideCantidades(p)) append(" · Quedan ${ReglasCatalogo.unidades(p)}")
    if (FormularioProducto.pideAlAgotarse(p)) append(" · Al acabarse: " + when (p.alAgotarse) {
        AlAgotarse.MOSTRAR_AGOTADO -> "se muestra «Se acabó»"
        AlAgotarse.OCULTAR -> "deja de mostrarse"
        AlAgotarse.PASAR_A_PEDIDO -> "pasa a «A pedido»"
    })
    if (FormularioProducto.tieneOpciones(p)) append(" · ${p.variantes.size} opciones")
    if (!p.disponible) append(" · No disponible")
}
