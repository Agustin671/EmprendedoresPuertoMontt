package com.example.trabajodea10.ui.emprendedora

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.trabajodea10.data.TramaStore
import com.example.trabajodea10.data.formatearMoneda
import com.example.trabajodea10.ui.componentes.BarraSuperior
import com.example.trabajodea10.ui.componentes.BotonPrimario
import com.example.trabajodea10.ui.componentes.ContadorCantidad
import com.example.trabajodea10.ui.componentes.FotoProducto
import com.example.trabajodea10.ui.componentes.Tarjeta
import com.example.trabajodea10.ui.componentes.TituloSeccion
import com.example.trabajodea10.ui.componentes.Vacio
import com.example.trabajodea10.ui.navegacion.ControladorNavegacion
import com.example.trabajodea10.ui.navegacion.Ruta
import com.example.trabajodea10.ui.theme.Berenjena
import com.example.trabajodea10.ui.theme.LineaSuave
import com.example.trabajodea10.ui.theme.TextoSecundario

@Composable
fun PantallaVenta(
    nav: ControladorNavegacion,
    avisar: (String) -> Unit
) {
    val carrito = TramaStore.carritoVenta
    val total = carrito.sumOf { it.precio * it.cantidad }

    Column(modifier = Modifier.fillMaxSize()) {
        BarraSuperior(
            titulo = "Carrito de venta",
            subtitulo = TramaStore.tienda.nombre,
            onVolver = if (nav.pila.size > 1) ({ nav.volver() }) else null
        )

        if (carrito.isEmpty()) {
            Vacio(
                titulo = "El carrito está vacío",
                mensaje = "Agrega productos desde «Mis productos» para armar la venta.",
                icono = {
                    Icon(
                        Icons.Filled.ShoppingCart,
                        contentDescription = null,
                        tint = TextoSecundario
                    )
                }
            )
            var elegir by remember { mutableStateOf<com.example.trabajodea10.data.Producto?>(null) }
            elegir?.let { p -> DialogoElegirOpcion(producto = p, onCerrar = { elegir = null }, onAgregado = { elegir = null; avisar(it) }) }
            Column(modifier = Modifier.padding(horizontal = 16.dp).verticalScroll(rememberScrollState())) {
                TituloSeccion(texto = "Elige qué vendiste")
                TramaStore.productosDeMiTienda().filter { it.vendidaEn == null && it.nombre.isNotBlank() }.forEach { p ->
                    androidx.compose.material3.TextButton(onClick = { elegir = p }, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
                        Text("${p.nombre} · ${formatearMoneda(p.precio)}", modifier = Modifier.weight(1f))
                    }
                }
                BotonPrimario(
                    texto = "Ir al catálogo",
                    onClick = { nav.seleccionarTab(Ruta.EmpCatalogo) }
                )
            }
            return@Column
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                start = 16.dp, end = 16.dp, top = 4.dp, bottom = 24.dp
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                TituloSeccion(texto = "Productos en el carrito")
                Tarjeta {
                    Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)) {
                        carrito.forEachIndexed { index, item ->
                            if (index > 0) {
                                androidx.compose.material3.HorizontalDivider(color = LineaSuave)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                FotoProducto(
                                    fotoUrl = item.fotoUrl
                                        ?: TramaStore.productoPorId(item.productoId)?.fotoUrl,
                                    colorRespaldo = TramaStore.productoPorId(item.productoId)
                                        ?.colorFoto ?: 0xFF8E6B94,
                                    inicial = item.nombre,
                                    modifier = Modifier.size(44.dp),
                                    forma = RoundedCornerShape(10.dp),
                                    tamanoInicial = 16.sp
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.nombre,
                                        fontWeight = FontWeight.Medium,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = "${item.detalle} · ${formatearMoneda(item.precio)} c/u" + if (item.encargo) " · encargo" else "",
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
                                        if (nueva <= 0) {
                                            carrito.remove(item)
                                        } else {
                                            // No se puede vender más de lo que queda en esa opción.
                                            val p = TramaStore.productoPorId(item.productoId)
                                            val problema = p?.let { com.example.trabajodea10.data.ReglasCatalogo.validarLinea(it, it.variantes.firstOrNull { v -> v.id == item.varianteId }, nueva, item.encargo) }
                                                ?: "El producto ya no está en tu catálogo."
                                            if (problema != null && nueva > item.cantidad) avisar(problema)
                                            else {
                                                val idx = carrito.indexOf(item)
                                                if (idx >= 0) carrito[idx] = item.copy(cantidad = nueva)
                                            }
                                        }
                                    }
                                )
                                IconButton(
                                    onClick = { carrito.removeAll { it === item } },
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
                    texto = "Continuar al pago · ${formatearMoneda(total)}",
                    onClick = {
                        val problema = TramaStore.validarVenta(carrito.toList())
                        if (problema != null) avisar(problema) else nav.irA(Ruta.PagoVenta)
                    }
                )
            }

            item {
                var productoVenta by remember { mutableStateOf<com.example.trabajodea10.data.Producto?>(null) }
                productoVenta?.let { p ->
                    DialogoElegirOpcion(producto = p, onCerrar = { productoVenta = null }, onAgregado = { productoVenta = null; avisar(it) })
                }
                TituloSeccion(texto = "Agregar otro producto")
                TramaStore.productosDeMiTienda().filter { it.vendidaEn == null && it.nombre.isNotBlank() }.forEach { p ->
                    androidx.compose.material3.TextButton(onClick = { productoVenta = p }, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
                        Text("${p.nombre} · ${formatearMoneda(p.precio)}", modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}
