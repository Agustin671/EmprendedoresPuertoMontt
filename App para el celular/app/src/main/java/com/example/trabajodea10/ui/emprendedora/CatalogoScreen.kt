package com.example.trabajodea10.ui.emprendedora

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.trabajodea10.data.Producto
import com.example.trabajodea10.data.TramaStore
import com.example.trabajodea10.data.Variante
import com.example.trabajodea10.data.formatearMoneda
import com.example.trabajodea10.data.remoto.Sincronizador
import kotlinx.coroutines.delay
import com.example.trabajodea10.ui.componentes.BarraSuperior
import com.example.trabajodea10.ui.componentes.CampoBusqueda
import com.example.trabajodea10.ui.componentes.ChipEstado
import com.example.trabajodea10.ui.componentes.ChipFiltro
import com.example.trabajodea10.ui.componentes.ColorEstado
import com.example.trabajodea10.ui.componentes.FotoProducto
import com.example.trabajodea10.ui.componentes.Tarjeta
import com.example.trabajodea10.ui.componentes.Vacio
import com.example.trabajodea10.ui.componentes.colorDeStock
import com.example.trabajodea10.ui.navegacion.ControladorNavegacion
import com.example.trabajodea10.ui.navegacion.Ruta
import com.example.trabajodea10.ui.theme.Berenjena
import com.example.trabajodea10.ui.theme.NaranjaAlerta
import com.example.trabajodea10.ui.theme.NegroApple
import com.example.trabajodea10.ui.theme.SuperficieVariante
import com.example.trabajodea10.ui.theme.TextoSecundario
import com.example.trabajodea10.ui.theme.VerdeOk

private enum class FiltroCatalogo(val etiqueta: String) {
    TODOS("Todos"),
    PUBLICADOS("Los ven todos"),
    BORRADORES("Solo yo los veo"),
    STOCK_BAJO("Quedan pocos")
}

@Composable
fun PantallaCatalogo(
    nav: ControladorNavegacion,
    avisar: (String) -> Unit
) {
    var busqueda by remember { mutableStateOf("") }
    var filtro by remember { mutableStateOf(FiltroCatalogo.TODOS) }
    // Cambio masivo de disponibilidad (RF-PRO-10): por selección, por categoría o todo.
    var seleccionando by remember { mutableStateOf(false) }
    val seleccion = remember { androidx.compose.runtime.mutableStateListOf<String>() }
    var productoVenta by remember { mutableStateOf<Producto?>(null) }
    // Último producto marcado como "Se acabó", para poder deshacerlo.
    var ultimoAgotado by remember { mutableStateOf<Pair<Producto, List<Variante>>?>(null) }
    // El aviso con "Deshacer" se esconde solo después de 8 segundos.
    LaunchedEffect(ultimoAgotado) {
        if (ultimoAgotado != null) {
            delay(8000)
            ultimoAgotado = null
        }
    }

    // Solo los productos de la tienda con la sesión iniciada.
    val todos = TramaStore.productosDeMiTienda()
    val visibles = todos.filter { producto ->
        val coincideBusqueda = producto.nombre.contains(busqueda, true) ||
            producto.categoria.etiqueta.contains(busqueda, true)
        val coincideFiltro = when (filtro) {
            FiltroCatalogo.TODOS -> true
            FiltroCatalogo.PUBLICADOS -> producto.publicado
            FiltroCatalogo.BORRADORES -> !producto.publicado
            FiltroCatalogo.STOCK_BAJO -> producto.agotado || producto.stockBajo
        }
        coincideBusqueda && coincideFiltro
    }

    Column(modifier = Modifier.fillMaxSize()) {
        BarraSuperior(
            titulo = "Mis productos",
            subtitulo = "${TramaStore.productosPublicados()} a la vista de tus clientas · ${todos.size} en total" +
                if (Sincronizador.pendientes > 0) " · ${Sincronizador.pendientes} cambios por subir (sin señal)" else "",
            acciones = {
                IconButton(onClick = { nav.irA(Ruta.VistaPrevia) }, modifier = Modifier.size(48.dp)) {
                    Icon(Icons.Filled.Visibility, contentDescription = "Vista previa de mi tienda")
                }
                IconButton(onClick = { nav.irA(Ruta.HistorialVentas) }, modifier = Modifier.size(48.dp)) {
                    Icon(Icons.Filled.History, contentDescription = "Historial de ventas")
                }
                IconButton(onClick = { nav.irA(Ruta.ConfigVentas) }, modifier = Modifier.size(48.dp)) {
                    Icon(Icons.Filled.Tune, contentDescription = "Ventas y productos nuevos")
                }
            }
        )

        productoVenta?.let { p ->
            com.example.trabajodea10.ui.emprendedora.DialogoElegirOpcion(
                producto = p,
                onCerrar = { productoVenta = null },
                onAgregado = { productoVenta = null; avisar(it) }
            )
        }

        // Producto sin terminar (se guardó solo): se puede retomar tras cerrar la app.
        TramaStore.borrador?.let { b ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .background(Color(0xFFFFF3E6), RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = (if (b.esNuevo) "Producto sin terminar" else "Cambios sin guardar") + ": «" + b.producto.nombre.ifBlank { "sin nombre" } + "»",
                    modifier = Modifier.weight(1f), fontWeight = FontWeight.SemiBold
                )
                TextButton(onClick = { nav.irA(Ruta.ProductoDetalle(b.producto.id)) }, modifier = Modifier.heightIn(min = 48.dp)) {
                    Text("Continuar", fontWeight = FontWeight.Bold)
                }
                TextButton(onClick = { TramaStore.descartarBorrador(); avisar("Descartado") }, modifier = Modifier.heightIn(min = 48.dp)) {
                    Text("Descartar", color = TextoSecundario)
                }
            }
        }
        TramaStore.avisosSync.toList().forEach { aviso ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .background(Color(0xFFFDECEC), RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(aviso, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                TextButton(onClick = { TramaStore.descartarAviso(aviso) }, modifier = Modifier.heightIn(min = 48.dp)) { Text("Entendido") }
            }
        }
        Sincronizador.ultimoProblema?.let { problema ->
            Text(problema, color = TextoSecundario, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp))
        }

        // Botón sobrio para añadir producto
        androidx.compose.material3.Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .clickable {
                    val id = TramaStore.crearProductoBorrador()
                    nav.irA(Ruta.ProductoDetalle(id))
                },
            colors = androidx.compose.material3.CardDefaults.cardColors(
                containerColor = NegroApple
            ),
            shape = RoundedCornerShape(14.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color(0xFF2E7D5B), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("➕", fontSize = 16.sp)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Añadir producto",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color.White,
                    modifier = Modifier.weight(1f)
                )
                Text("➔", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }

        CampoBusqueda(
            valor = busqueda,
            onValorChange = { busqueda = it },
            modifier = Modifier.padding(horizontal = 16.dp),
            placeholder = "Buscar por nombre o tipo…"
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FiltroCatalogo.entries.forEach { opcion ->
                ChipFiltro(
                    texto = opcion.etiqueta,
                    seleccionado = filtro == opcion,
                    onClick = { filtro = opcion }
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (seleccionando) "${seleccion.size} elegidos" else "Cambiar varios a la vez",
                modifier = Modifier.weight(1f), fontWeight = FontWeight.SemiBold
            )
            TextButton(onClick = { seleccionando = !seleccionando; seleccion.clear() }, modifier = Modifier.heightIn(min = 48.dp)) {
                Text(if (seleccionando) "Listo" else "Elegir varios", fontWeight = FontWeight.Bold)
            }
        }
        if (seleccionando) {
            com.example.trabajodea10.ui.componentes.ContenedorChips(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                ChipFiltro(texto = "Todo el catálogo", seleccionado = seleccion.size == todos.size && todos.isNotEmpty(), onClick = {
                    seleccion.clear(); seleccion.addAll(todos.map { it.id })
                })
                todos.map { it.categoria }.distinct().forEach { c ->
                    ChipFiltro(texto = "Toda la categoría " + c.etiqueta, seleccionado = false, onClick = {
                        seleccion.removeAll { id -> todos.any { it.id == id && it.categoria == c } }
                        seleccion.addAll(todos.filter { it.categoria == c }.map { it.id })
                    })
                }
            }
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = { val n = TramaStore.cambiarDisponibilidad(seleccion.toList(), true); avisar("$n productos disponibles"); seleccion.clear() },
                    enabled = seleccion.isNotEmpty(), modifier = Modifier.weight(1f).heightIn(min = 48.dp)
                ) { Text("Disponibles") }
                OutlinedButton(
                    onClick = { val n = TramaStore.cambiarDisponibilidad(seleccion.toList(), false); avisar("$n productos no disponibles (siguen en tu tienda)"); seleccion.clear() },
                    enabled = seleccion.isNotEmpty(), modifier = Modifier.weight(1f).heightIn(min = 48.dp)
                ) { Text("No disponibles") }
            }
        }

        ultimoAgotado?.let { (producto, variantes) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .background(NegroApple, RoundedCornerShape(12.dp))
                    .padding(start = 14.dp, end = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Marcaste «${producto.nombre}» como agotado",
                    color = Color.White,
                    fontSize = 14.sp,
                    modifier = Modifier.weight(1f)
                )
                TextButton(
                    onClick = {
                        TramaStore.restaurarVariantes(producto.id, variantes)
                        ultimoAgotado = null
                        avisar("Listo, «${producto.nombre}» vuelve a tener unidades")
                    },
                    modifier = Modifier.heightIn(min = 48.dp)
                ) {
                    Text("Deshacer", color = Color(0xFFFFD54F), fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
        }

        if (visibles.isEmpty()) {
            Vacio(
                titulo = "Sin productos",
                mensaje = "Crea tu primer producto con el botón «Añadir producto» o cambia el filtro.",
                icono = { Icon(Icons.Filled.Inventory2, contentDescription = null, tint = TextoSecundario) }
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 16.dp, end = 16.dp, top = 4.dp, bottom = 96.dp
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(visibles, key = { it.id }) { producto ->
                    val elegido = producto.id in seleccion
                    Tarjeta(onClick = {
                        if (seleccionando) { if (elegido) seleccion.remove(producto.id) else seleccion.add(producto.id) }
                        else nav.irA(Ruta.ProductoDetalle(producto.id))
                    }) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (seleccionando) {
                                androidx.compose.material3.Checkbox(
                                    checked = elegido,
                                    onCheckedChange = { if (it) seleccion.add(producto.id) else seleccion.remove(producto.id) },
                                    modifier = Modifier.padding(start = 6.dp)
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                TarjetaProducto(
                                    producto = producto,
                                    nav = nav,
                                    avisar = avisar,
                                    onSeAcabo = {
                                        ultimoAgotado = producto to producto.variantes
                                        TramaStore.marcarAgotado(producto.id)
                                    },
                                    onVender = { productoVenta = producto }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TarjetaProducto(
    producto: Producto,
    nav: ControladorNavegacion,
    avisar: (String) -> Unit,
    onSeAcabo: () -> Unit = { TramaStore.marcarAgotado(producto.id) },
    onVender: () -> Unit = {}
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        FotoProducto(
            fotoUrl = producto.fotoUrl,
            colorRespaldo = producto.colorFoto,
            inicial = producto.nombre,
            modifier = Modifier.size(64.dp),
            forma = RoundedCornerShape(16.dp),
            tamanoInicial = 26.sp
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = producto.nombre.ifBlank { "Producto sin nombre" },
                    modifier = Modifier.weight(1f),
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1
                )
                IconButton(
                    onClick = { TramaStore.toggleDestacado(producto.id) },
                    modifier = Modifier.size(34.dp)
                ) {
                    Icon(
                        imageVector = if (producto.destacado) Icons.Filled.Star else Icons.Filled.StarBorder,
                        contentDescription = "Destacar",
                        tint = if (producto.destacado) NaranjaAlerta else TextoSecundario,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                ChipEstado(
                    texto = producto.categoria.etiqueta,
                    color = ColorEstado(SuperficieVariante, TextoSecundario)
                )
                Spacer(modifier = Modifier.width(6.dp))
                val estadoPublico = com.example.trabajodea10.data.ReglasCatalogo.estadoPublico(producto)
                ChipEstado(texto = if (!producto.disponible) "No disponible" else estadoPublico.etiqueta, color = colorEstadoPublico(estadoPublico))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = formatearMoneda(producto.precio),
                    fontWeight = FontWeight.SemiBold,
                    color = Berenjena
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = when {
                        producto.modalidad == com.example.trabajodea10.data.Modalidad.A_PEDIDO -> "A pedido"
                        producto.vendidaEn != null -> "Pieza única vendida"
                        !producto.manejaStock -> "Sin conteo"
                        producto.agotado -> "Se acabó"
                        else -> "Quedan ${producto.stockTotal}" + if (producto.variantes.size > 1) " en ${producto.variantes.size} opciones" else ""
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = colorDeStock(producto.stockTotal)
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = if (producto.publicado) "Lo ven todos" else "Solo yo lo veo",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (producto.publicado) VerdeOk else NaranjaAlerta,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.width(6.dp))
                Switch(
                    checked = producto.publicado,
                    onCheckedChange = {
                        TramaStore.togglePublicado(producto.id)
                        avisar(
                            if (producto.publicado) "«${producto.nombre}» ya no se muestra en tu tienda"
                            else "«${producto.nombre}» ahora se muestra en tu tienda"
                        )
                    },
                    colors = SwitchDefaults.colors(
                        checkedTrackColor = Berenjena,
                        uncheckedTrackColor = SuperficieVariante,
                        uncheckedBorderColor = com.example.trabajodea10.ui.theme.LineaSuave
                    )
                )
            }
            if (!producto.agotado) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onSeAcabo,
                        modifier = Modifier.heightIn(min = 48.dp),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp)
                    ) {
                        Text("Se acabó", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = NegroApple)
                    }
                    if (producto.vendidaEn == null) {
                        TextButton(
                            onClick = onVender,
                            modifier = Modifier.heightIn(min = 48.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp)
                        ) {
                            Text("Agregar a la venta", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}
