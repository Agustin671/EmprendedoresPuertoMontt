package com.example.trabajodea10.ui.emprendedora

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.trabajodea10.data.ItemVenta
import com.example.trabajodea10.data.Producto
import com.example.trabajodea10.data.ReglasCatalogo
import com.example.trabajodea10.data.TramaStore
import com.example.trabajodea10.data.formatearMoneda
import com.example.trabajodea10.ui.componentes.ChipFiltro
import com.example.trabajodea10.ui.componentes.ContadorCantidad
import com.example.trabajodea10.ui.componentes.ContenedorChips
import com.example.trabajodea10.ui.theme.RojoError
import com.example.trabajodea10.ui.theme.TextoSecundario

/**
 * Elegir opción y cantidad antes de agregar a la venta (RF-VEN-01).
 * Una opción sin unidades no se vende como entrega inmediata; si el producto
 * lo permite, se puede anotar como encargo.
 */
@Composable
fun DialogoElegirOpcion(producto: Producto, onCerrar: () -> Unit, onAgregado: (String) -> Unit) {
    val inicial = producto.variantes.firstOrNull { ReglasCatalogo.opcionVendible(producto, it) } ?: producto.variantes.firstOrNull()
    var varianteId by remember { mutableStateOf(inicial?.id) }
    var cantidad by remember { mutableStateOf(1) }
    var encargo by remember { mutableStateOf(producto.modalidad == com.example.trabajodea10.data.Modalidad.A_PEDIDO) }
    val v = producto.variantes.firstOrNull { it.id == varianteId }
    val enCarrito = TramaStore.carritoVenta.filter { it.productoId == producto.id && it.varianteId == varianteId && it.encargo == encargo }.sumOf { it.cantidad }
    val problema = ReglasCatalogo.validarLinea(producto, v, cantidad + enCarrito, encargo)

    AlertDialog(
        onDismissRequest = onCerrar,
        title = { Text("Vender «${producto.nombre}»", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (producto.variantes.size > 1) {
                    Text("Opción", fontWeight = FontWeight.SemiBold)
                    ContenedorChips(modifier = Modifier.fillMaxWidth()) {
                        producto.variantes.forEach { op ->
                            val etiqueta = op.nombre + when {
                                !producto.manejaStock || producto.modalidad == com.example.trabajodea10.data.Modalidad.A_PEDIDO -> ""
                                op.stock <= 0 -> " (se acabó)"
                                else -> " (${op.stock})"
                            }
                            ChipFiltro(texto = etiqueta, seleccionado = op.id == varianteId, onClick = { varianteId = op.id })
                        }
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Cantidad", fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                    ContadorCantidad(cantidad = cantidad, onCambio = { cantidad = it.coerceAtLeast(1) }, maximo = 999)
                }
                if (ReglasCatalogo.aceptaEncargo(producto) && producto.modalidad != com.example.trabajodea10.data.Modalidad.A_PEDIDO) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.heightIn(min = 48.dp)) {
                        Text("Anotar como encargo (no descuenta unidades)", modifier = Modifier.weight(1f))
                        androidx.compose.material3.Switch(checked = encargo, onCheckedChange = { encargo = it })
                    }
                }
                Text("Precio: ${formatearMoneda(ReglasCatalogo.precioDe(producto, v))} c/u", color = TextoSecundario, style = MaterialTheme.typography.bodySmall)
                problema?.let { Text(it, color = RojoError, fontWeight = FontWeight.SemiBold) }
            }
        },
        confirmButton = {
            TextButton(enabled = problema == null && v != null, onClick = {
                val op = v ?: return@TextButton
                TramaStore.agregarAlCarrito(
                    ItemVenta(
                        productoId = producto.id, varianteId = op.id, nombre = producto.nombre, detalle = op.nombre,
                        precio = ReglasCatalogo.precioDe(producto, op), cantidad = cantidad,
                        fotoUrl = ReglasCatalogo.fotoDe(producto, op), encargo = encargo
                    )
                )
                onAgregado("«${producto.nombre}» agregado a la venta")
            }) { Text("Agregar", fontWeight = FontWeight.Bold) }
        },
        dismissButton = { TextButton(onClick = onCerrar) { Text("Cancelar", color = RojoError) } }
    )
}
