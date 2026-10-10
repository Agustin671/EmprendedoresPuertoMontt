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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.trabajodea10.data.Categoria
import com.example.trabajodea10.data.TramaStore
import com.example.trabajodea10.data.Variante
import com.example.trabajodea10.data.formatearMoneda
import com.example.trabajodea10.ui.componentes.BarraSuperior
import com.example.trabajodea10.ui.componentes.BotonPrimario
import com.example.trabajodea10.ui.componentes.BotonSecundario
import com.example.trabajodea10.ui.componentes.BotonTexto
import com.example.trabajodea10.ui.componentes.CampoTexto
import com.example.trabajodea10.ui.componentes.ChipFiltro
import com.example.trabajodea10.ui.componentes.FilaSwitch
import com.example.trabajodea10.ui.componentes.LineaSeparadora
import com.example.trabajodea10.ui.componentes.Tarjeta
import com.example.trabajodea10.ui.componentes.TituloSeccion
import com.example.trabajodea10.ui.componentes.Vacio
import com.example.trabajodea10.ui.componentes.colorDeStock
import com.example.trabajodea10.ui.navegacion.ControladorNavegacion
import com.example.trabajodea10.ui.theme.LineaSuave
import com.example.trabajodea10.ui.theme.RojoError
import com.example.trabajodea10.ui.theme.SuperficieVariante
import com.example.trabajodea10.ui.theme.TextoSecundario

private val COLORES_FOTO = listOf(
    0xFF8E6B94, 0xFF3E7D6E, 0xFFC07A3E, 0xFF6B7FB3, 0xFFB36B7F, 0xFF7FA66B, 0xFF7B3E7A
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaProductoDetalle(
    productoId: String,
    nav: ControladorNavegacion,
    avisar: (String) -> Unit
) {
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
    var precio by remember(original.id) { mutableStateOf(original.precio.toInt().toString()) }
    var categoria by remember(original.id) { mutableStateOf(original.categoria) }
    var publicado by remember(original.id) { mutableStateOf(original.publicado) }
    var destacado by remember(original.id) { mutableStateOf(original.destacado) }
    var colorFoto by remember(original.id) { mutableStateOf(original.colorFoto) }
    var variantes by remember(original.id) { mutableStateOf(original.variantes) }
    var mostrarHojaVariante by remember { mutableStateOf(false) }

    val stockTotal = variantes.sumOf { it.stock }

    Column(modifier = Modifier.fillMaxSize()) {
        BarraSuperior(
            titulo = if (esNuevo) "Nuevo producto" else "Editar producto",
            subtitulo = if (esNuevo) "Completá los datos para publicarlo" else "Stock total: $stockTotal",
            onVolver = { nav.volver() }
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Tarjeta {
                Column(modifier = Modifier.padding(16.dp)) {
                    TituloSeccion(texto = "Foto del producto")
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(84.dp)
                                .background(Color(colorFoto), RoundedCornerShape(18.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = nombre.firstOrNull()?.uppercase() ?: "+",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 34.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Row(
                            modifier = Modifier
                                .weight(1f)
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
                                            RoundedCornerShape(50)
                                        )
                                        .padding(4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .background(Color(color), RoundedCornerShape(50))
                                            .clickable { colorFoto = color }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            CampoTexto(valor = nombre, onValorChange = { nombre = it }, etiqueta = "Nombre del producto")

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                CampoTexto(
                    valor = precio,
                    onValorChange = { precio = it.filter { c -> c.isDigit() } },
                    etiqueta = "Precio ($)",
                    tipoTeclado = KeyboardType.Number,
                    modifier = Modifier.weight(1f)
                )
                Tarjeta(modifier = Modifier.weight(1f)) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stockTotal.toString(),
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = colorDeStock(stockTotal)
                        )
                        Text(
                            text = "unidades en stock",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextoSecundario
                        )
                    }
                }
            }

            TituloSeccion(texto = "Categoría")
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

            Tarjeta {
                Column(modifier = Modifier.padding(16.dp)) {
                    TituloSeccion(texto = "Variantes (color / talle)")
                    Spacer(modifier = Modifier.height(8.dp))
                    if (variantes.isEmpty()) {
                        Text(
                            text = "Sin variantes: el producto se vende como única opción.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextoSecundario
                        )
                    }
                    variantes.forEachIndexed { index, variante ->
                        if (index > 0) LineaSeparadora()
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(variante.nombre, fontWeight = FontWeight.Medium)
                                Text(
                                    text = "Stock: ${variante.stock}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colorDeStock(variante.stock)
                                )
                            }
                            IconButton(
                                onClick = {
                                    variantes = variantes.filterNot { it.id == variante.id }
                                }
                            ) {
                                Icon(
                                    Icons.Filled.Delete,
                                    contentDescription = "Eliminar variante",
                                    tint = TextoSecundario,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    BotonSecundario(
                        texto = "Agregar variante",
                        onClick = { mostrarHojaVariante = true },
                        icono = { Icon(Icons.Filled.Add, contentDescription = null, tint = MaterialTheme.colorScheme.primary) }
                    )
                }
            }

            Tarjeta {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    FilaSwitch(
                        titulo = "Publicar en el catálogo",
                        descripcion = "Visible para clientes y en tu tienda",
                        checked = publicado,
                        onCheckedChange = { publicado = it }
                    )
                    FilaSwitch(
                        titulo = "Producto destacado",
                        descripcion = "Aparece primero en tu vidriera",
                        checked = destacado,
                        onCheckedChange = { destacado = it }
                    )
                }
            }

            BotonPrimario(
                texto = "Guardar producto",
                onClick = {
                    if (nombre.isBlank()) {
                        avisar("Completá el nombre del producto")
                    } else {
                        TramaStore.guardarProducto(
                            original.copy(
                                nombre = nombre.trim(),
                                precio = precio.toDoubleOrNull() ?: original.precio,
                                categoria = categoria,
                                publicado = publicado,
                                destacado = destacado,
                                colorFoto = colorFoto,
                                variantes = variantes
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

            Spacer(modifier = Modifier.height(72.dp))
        }
    }

    if (mostrarHojaVariante) {
        HojaNuevaVariante(
            onCerrar = { mostrarHojaVariante = false },
            onAgregar = { nombreVariante, stock ->
                val limpia = nombreVariante.trim()
                if (limpia.isNotBlank()) {
                    variantes = variantes + Variante(
                        id = "${original.id}v${System.currentTimeMillis()}",
                        nombre = limpia,
                        stock = stock
                    )
                    mostrarHojaVariante = false
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HojaNuevaVariante(
    onCerrar: () -> Unit,
    onAgregar: (String, Int) -> Unit
) {
    var nombreVariante by remember { mutableStateOf("") }
    var stock by remember { mutableStateOf("1") }

    ModalBottomSheet(
        onDismissRequest = onCerrar,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .navigationBarsPadding()
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TituloSeccion(texto = "Nueva variante", accion = "Cerrar", onAccion = onCerrar)
            CampoTexto(
                valor = nombreVariante,
                onValorChange = { nombreVariante = it },
                etiqueta = "Color / talle",
                placeholder = "Verde / M"
            )
            CampoTexto(
                valor = stock,
                onValorChange = { stock = it.filter { c -> c.isDigit() } },
                etiqueta = "Stock inicial",
                tipoTeclado = KeyboardType.Number
            )
            BotonPrimario(
                texto = "Agregar",
                onClick = { onAgregar(nombreVariante, stock.toIntOrNull() ?: 0) }
            )
        }
    }
}
