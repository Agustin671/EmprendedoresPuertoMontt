package com.example.trabajodea10.ui.emprendedora

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.trabajodea10.data.ItemVenta
import com.example.trabajodea10.data.MedioPago
import com.example.trabajodea10.data.Producto
import com.example.trabajodea10.data.TramaStore
import com.example.trabajodea10.data.Venta
import com.example.trabajodea10.data.formatearMoneda
import com.example.trabajodea10.ui.componentes.BarraSuperior
import com.example.trabajodea10.ui.componentes.BotonPrimario
import com.example.trabajodea10.ui.componentes.CampoBusqueda
import com.example.trabajodea10.ui.componentes.ChipEstado
import com.example.trabajodea10.ui.componentes.ChipFiltro
import com.example.trabajodea10.ui.componentes.ColorEstado
import com.example.trabajodea10.ui.componentes.ContadorCantidad
import com.example.trabajodea10.ui.componentes.Tarjeta
import com.example.trabajodea10.ui.componentes.TituloSeccion
import com.example.trabajodea10.ui.componentes.Vacio
import com.example.trabajodea10.ui.componentes.colorDeStock
import com.example.trabajodea10.ui.navegacion.ControladorNavegacion
import com.example.trabajodea10.ui.navegacion.Ruta
import com.example.trabajodea10.ui.theme.Berenjena
import com.example.trabajodea10.ui.theme.LineaSuave
import com.example.trabajodea10.ui.theme.SuperficieVariante
import com.example.trabajodea10.ui.theme.TextoSecundario
import com.example.trabajodea10.ui.theme.VerdeOk

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaVenta(
    nav: ControladorNavegacion,
    avisar: (String) -> Unit
) {
    var feriaId by remember { mutableStateOf(TramaStore.feriasEnCurso().firstOrNull()?.id) }
    var carrito by remember { mutableStateOf(listOf<ItemVenta>()) }
    var medio by remember { mutableStateOf<MedioPago?>(null) }
    var busqueda by remember { mutableStateOf("") }
    var productoSel by remember { mutableStateOf<Producto?>(null) }
    var ventaConfirmada by remember { mutableStateOf<Venta?>(null) }

    val publicados = TramaStore.productos.filter { it.publicado }
    val visibles = publicados.filter { it.nombre.contains(busqueda, true) }
    val total = carrito.sumOf { it.precio * it.cantidad }
    val feriasDisponibles = TramaStore.feriasEnCurso() + TramaStore.proximasFerias()

    fun agregarAlCarrito(item: ItemVenta) {
        val existente = carrito.firstOrNull {
            it.productoId == item.productoId && it.varianteId == item.varianteId
        }
        carrito = if (existente != null) {
            carrito.map {
                if (it === existente) it.copy(cantidad = it.cantidad + item.cantidad) else it
            }
        } else {
            carrito + item
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        BarraSuperior(
            titulo = "Registrar venta",
            subtitulo = TramaStore.tienda.nombre,
            onVolver = if (nav.pila.size > 1) ({ nav.volver() }) else null
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                start = 16.dp, end = 16.dp, top = 4.dp, bottom = 24.dp
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                TituloSeccion(texto = "1 · Feria vinculada")
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ChipFiltro(
                        texto = "Sin feria",
                        seleccionado = feriaId == null,
                        onClick = { feriaId = null }
                    )
                    feriasDisponibles.forEach { feria ->
                        ChipFiltro(
                            texto = feria.nombre,
                            seleccionado = feriaId == feria.id,
                            onClick = { feriaId = feria.id }
                        )
                    }
                }
            }

            item {
                TituloSeccion(texto = "2 · Productos")
                CampoBusqueda(
                    valor = busqueda,
                    onValorChange = { busqueda = it },
                    placeholder = "Buscar producto…"
                )
            }

            if (visibles.isEmpty()) {
                item {
                    Vacio(
                        titulo = "Sin productos publicados",
                        mensaje = "Publicá productos desde tu catálogo para poder venderlos.",
                        icono = {
                            Icon(
                                Icons.Filled.Search,
                                contentDescription = null,
                                tint = TextoSecundario
                            )
                        }
                    )
                }
            } else {
                items(visibles, key = { it.id }) { producto ->
                    Tarjeta(
                        onClick = {
                            if (producto.stockTotal > 0) {
                                productoSel = producto
                            } else {
                                avisar("${producto.nombre} está agotado")
                            }
                        }
                    ) {
                        FilaProductoVenta(producto)
                    }
                }
            }

            if (carrito.isNotEmpty()) {
                item {
                    TituloSeccion(texto = "Carrito")
                    Tarjeta {
                        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)) {
                            carrito.forEachIndexed { index, item ->
                                if (index > 0) {
                                    androidx.compose.material3.HorizontalDivider(
                                        color = LineaSuave
                                    )
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = item.nombre,
                                            fontWeight = FontWeight.Medium,
                                            maxLines = 1
                                        )
                                        Text(
                                            text = "${item.detalle} · ${formatearMoneda(item.precio)} c/u",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TextoSecundario
                                        )
                                        Text(
                                            text = "Subtotal ${formatearMoneda(item.precio * item.cantidad)}",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Berenjena
                                        )
                                    }
                                    ContadorCantidad(
                                        cantidad = item.cantidad,
                                        onCambio = { nueva ->
                                            carrito = if (nueva <= 0) {
                                                carrito.filterNot { it === item }
                                            } else {
                                                carrito.map {
                                                    if (it === item) it.copy(cantidad = nueva) else it
                                                }
                                            }
                                        }
                                    )
                                    IconButton(
                                        onClick = { carrito = carrito.filterNot { it === item } },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            Icons.Filled.Delete,
                                            contentDescription = "Quitar",
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

            item {
                TituloSeccion(texto = "3 · Medio de pago")
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MedioPago.entries.forEach { opcion ->
                        ChipFiltro(
                            texto = opcion.etiqueta,
                            seleccionado = medio == opcion,
                            onClick = { medio = opcion }
                        )
                    }
                }
            }

            item {
                Tarjeta {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Total", color = TextoSecundario, style = MaterialTheme.typography.bodyMedium)
                            Text(
                                text = "${carrito.sumOf { it.cantidad }} artículos",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextoSecundario
                            )
                        }
                        Text(
                            text = formatearMoneda(total),
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            color = Berenjena
                        )
                    }
                }
            }

            item {
                BotonPrimario(
                    texto = if (carrito.isEmpty()) "Agregá productos" else "Confirmar venta · ${formatearMoneda(total)}",
                    enabled = carrito.isNotEmpty() && medio != null,
                    onClick = {
                        val pago = medio ?: return@BotonPrimario
                        val venta = TramaStore.registrarVenta(carrito, pago, feriaId)
                        carrito = emptyList()
                        medio = null
                        busqueda = ""
                        ventaConfirmada = venta
                    }
                )
                if (medio == null && carrito.isNotEmpty()) {
                    Text(
                        text = "Elegí el medio de pago para continuar.",
                        modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = TextoSecundario,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }
    }

    productoSel?.let { producto ->
        HojaProductoVenta(
            producto = producto,
            onCerrar = { productoSel = null },
            onAgregar = { item ->
                agregarAlCarrito(item)
                productoSel = null
                avisar("${item.nombre} agregado al carrito")
            }
        )
    }

    ventaConfirmada?.let { venta ->
        HojaVentaConfirmada(
            venta = venta,
            onNueva = { ventaConfirmada = null },
            onInicio = {
                ventaConfirmada = null
                nav.seleccionarTab(Ruta.EmpInicio)
            }
        )
    }
}

@Composable
private fun FilaProductoVenta(producto: Producto) {
    val agotado = producto.stockTotal == 0
    Row(
        modifier = Modifier.fillMaxWidth().padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .background(Color(producto.colorFoto), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = producto.nombre.firstOrNull()?.uppercase() ?: "?",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = producto.nombre,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                color = if (agotado) TextoSecundario else MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = formatearMoneda(producto.precio),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color = Berenjena
            )
        }
        if (agotado) {
            ChipEstado(
                texto = "Agotado",
                color = ColorEstado(SuperficieVariante, TextoSecundario)
            )
        } else {
            Text(
                text = "Stock ${producto.stockTotal}",
                style = MaterialTheme.typography.labelSmall,
                color = colorDeStock(producto.stockTotal),
                modifier = Modifier.padding(end = 8.dp)
            )
            Icon(
                Icons.Filled.AddShoppingCart,
                contentDescription = "Agregar al carrito",
                tint = Berenjena,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HojaProductoVenta(
    producto: Producto,
    onCerrar: () -> Unit,
    onAgregar: (ItemVenta) -> Unit
) {
    var varianteSel by remember(producto.id) {
        mutableStateOf(producto.variantes.firstOrNull { it.stock > 0 } ?: producto.variantes.first())
    }
    var cantidad by remember(producto.id) { mutableStateOf(1) }

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
            TituloSeccion(texto = producto.nombre, accion = "Cerrar", onAccion = onCerrar)
            Text(
                text = formatearMoneda(producto.precio),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Berenjena
            )

            if (producto.variantes.isNotEmpty()) {
                TituloSeccion(texto = "Variante")
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    producto.variantes.forEach { variante ->
                        ChipFiltro(
                            texto = "${variante.nombre} (${variante.stock})",
                            seleccionado = variante.id == varianteSel.id,
                            onClick = { varianteSel = variante }
                        )
                    }
                }
            }

            TituloSeccion(texto = "Cantidad")
            ContadorCantidad(
                cantidad = cantidad,
                onCambio = { cantidad = it.coerceIn(0, varianteSel.stock) },
                maximo = varianteSel.stock
            )

            BotonPrimario(
                texto = "Agregar al carrito",
                enabled = cantidad > 0 && varianteSel.stock > 0,
                onClick = {
                    onAgregar(
                        ItemVenta(
                            productoId = producto.id,
                            varianteId = varianteSel.id,
                            nombre = producto.nombre,
                            detalle = varianteSel.nombre,
                            precio = producto.precio,
                            cantidad = cantidad
                        )
                    )
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HojaVentaConfirmada(
    venta: Venta,
    onNueva: () -> Unit,
    onInicio: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onNueva,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .navigationBarsPadding()
                .padding(bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                Icons.Filled.CheckCircle,
                contentDescription = null,
                tint = VerdeOk,
                modifier = Modifier.size(56.dp)
            )
            Text(
                text = "¡Venta registrada!",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = formatearMoneda(venta.total),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Berenjena
            )

            Tarjeta {
                Column(modifier = Modifier.padding(16.dp)) {
                    FilaResumen("Medio de pago", venta.medioPago.etiqueta)
                    FilaResumen("Feria vinculada", TramaStore.feriaDeLaVenta(venta.feriaId))
                    FilaResumen(
                        "Artículos",
                        venta.items.sumOf { it.cantidad }.toString()
                    )
                    Text(
                        text = "El stock se descontó automáticamente.",
                        modifier = Modifier.padding(top = 8.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = TextoSecundario
                    )
                }
            }

            BotonPrimario(texto = "Registrar otra venta", onClick = onNueva)
            BotonPrimario(
                texto = "Volver al inicio",
                onClick = onInicio
            )
        }
    }
}

@Composable
private fun FilaResumen(etiqueta: String, valor: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = etiqueta,
            modifier = Modifier.weight(1f),
            color = TextoSecundario,
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = valor,
            fontWeight = FontWeight.Medium,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
