package com.example.trabajodea10.ui.emprendedora

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.trabajodea10.data.EstadoPublico
import com.example.trabajodea10.data.Modalidad
import com.example.trabajodea10.data.Producto
import com.example.trabajodea10.data.ReglasCatalogo
import com.example.trabajodea10.data.TramaStore
import com.example.trabajodea10.data.formatearMoneda
import com.example.trabajodea10.ui.componentes.BarraSuperior
import com.example.trabajodea10.ui.componentes.ChipEstado
import com.example.trabajodea10.ui.componentes.ChipFiltro
import com.example.trabajodea10.ui.componentes.ColorEstado
import com.example.trabajodea10.ui.componentes.ContenedorChips
import com.example.trabajodea10.ui.componentes.FotoProducto
import com.example.trabajodea10.ui.componentes.Tarjeta
import com.example.trabajodea10.ui.componentes.Vacio
import com.example.trabajodea10.ui.navegacion.ControladorNavegacion
import com.example.trabajodea10.ui.navegacion.Ruta
import com.example.trabajodea10.ui.theme.Berenjena
import com.example.trabajodea10.ui.theme.TextoSecundario

/** Colores del estado público (los mismos textos que ve el público en la web). */
fun colorEstadoPublico(e: EstadoPublico): ColorEstado = when (e) {
    EstadoPublico.DISPONIBLE -> ColorEstado(androidx.compose.ui.graphics.Color(0xFFE8F5EE), androidx.compose.ui.graphics.Color(0xFF1B6B47))
    EstadoPublico.AGOTADO -> ColorEstado(androidx.compose.ui.graphics.Color(0xFFFDECEC), androidx.compose.ui.graphics.Color(0xFF9B1C1C))
    EstadoPublico.A_PEDIDO -> ColorEstado(androidx.compose.ui.graphics.Color(0xFFFFF3E6), androidx.compose.ui.graphics.Color(0xFF8A4B08))
    EstadoPublico.OCULTO -> ColorEstado(androidx.compose.ui.graphics.Color(0xFFF1F1F3), androidx.compose.ui.graphics.Color(0xFF52525B))
}

/**
 * Tarjeta del producto tal como la ve el público, aplicando modalidad,
 * unidades, «al agotarse» y visibilidad con ReglasCatalogo (RF-PRO-12).
 */
@Composable
fun TarjetaVistaPrevia(producto: Producto, mostrarMotivoOculto: Boolean = true) {
    val estado = ReglasCatalogo.estadoPublico(producto)
    var elegida by remember(producto.id) { mutableStateOf(producto.variantes.firstOrNull { ReglasCatalogo.opcionVendible(producto, it) }?.id ?: producto.variantes.firstOrNull()?.id) }
    val opcion = producto.variantes.firstOrNull { it.id == elegida }
    Tarjeta {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                FotoProducto(
                    fotoUrl = ReglasCatalogo.fotoDe(producto, opcion), colorRespaldo = producto.colorFoto,
                    inicial = producto.nombre.ifBlank { "?" }, modifier = Modifier.size(72.dp),
                    forma = RoundedCornerShape(14.dp), tamanoInicial = 24.sp
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(producto.nombre.ifBlank { "Producto sin nombre" }, fontWeight = FontWeight.Bold)
                    val tipo = TramaStore.tipoPorId(producto.tipo)?.nombre
                    Text(listOfNotNull(producto.categoria.etiqueta, tipo).joinToString(" · "), color = TextoSecundario, style = MaterialTheme.typography.bodySmall)
                    Text(formatearMoneda(ReglasCatalogo.precioDe(producto, opcion)), fontWeight = FontWeight.Bold, color = Berenjena, fontSize = 16.sp)
                }
                ChipEstado(texto = estado.etiqueta, color = colorEstadoPublico(estado))
            }
            if (producto.descripcion.isNotBlank()) Text(producto.descripcion, style = MaterialTheme.typography.bodyMedium)
            if (producto.variantes.size > 1) {
                ContenedorChips(modifier = Modifier.fillMaxWidth()) {
                    producto.variantes.forEach { v ->
                        val agotada = estado != EstadoPublico.A_PEDIDO && !ReglasCatalogo.opcionVendible(producto, v)
                        // La opción elegida lleva una marca además del color.
                        ChipFiltro(texto = (if (v.id == elegida) "✓ " else "") + v.nombre.ifBlank { "Opción" } + if (agotada) " (se acabó)" else "", seleccionado = v.id == elegida, onClick = { elegida = v.id })
                    }
                }
            }
            val detalle = when {
                estado == EstadoPublico.OCULTO -> if (mostrarMotivoOculto) "No se muestra: ${ReglasCatalogo.motivoOculto(producto)}" else ""
                producto.modalidad == Modalidad.PIEZA_UNICA -> "Pieza única"
                estado == EstadoPublico.DISPONIBLE && producto.manejaStock && opcion != null ->
                    if (opcion.stock > 0) "Quedan ${opcion.stock}" else "Esta opción se acabó"
                estado == EstadoPublico.A_PEDIDO -> "Se vende por encargo"
                estado == EstadoPublico.AGOTADO -> "Se muestra como agotado"
                else -> ""
            }
            if (detalle.isNotBlank()) Text(detalle, color = TextoSecundario, style = MaterialTheme.typography.bodySmall)
        }
    }
}

/** Vista previa de todo el catálogo: lo que ve el público y lo que no (con el motivo). */
@Composable
fun PantallaVistaPrevia(nav: ControladorNavegacion) {
    val mios = TramaStore.productosDeMiTienda()
    val visibles = mios.filter { ReglasCatalogo.estadoPublico(it) != EstadoPublico.OCULTO }.sortedByDescending { it.destacado }
    val ocultos = mios.filter { ReglasCatalogo.estadoPublico(it) == EstadoPublico.OCULTO }
    Column(modifier = Modifier.fillMaxSize()) {
        BarraSuperior(titulo = "Vista previa de mi tienda", subtitulo = "${visibles.size} a la vista · ${ocultos.size} no se muestran", onVolver = { nav.volver() })
        if (mios.isEmpty()) {
            Vacio(titulo = "Aún no tienes productos", mensaje = "Cuando agregues productos verás aquí cómo los ve el público.")
            return@Column
        }
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item { Text("Lo que ve el público", fontWeight = FontWeight.Bold, modifier = Modifier.semantics { heading() }) }
            if (visibles.isEmpty()) item { Text("Ningún producto se muestra todavía.", color = TextoSecundario) }
            items(visibles, key = { "v" + it.id }) { p -> TarjetaVistaPrevia(p) }
            if (ocultos.isNotEmpty()) {
                item { Text("No se muestran", fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp).semantics { heading() }) }
                items(ocultos, key = { "o" + it.id }) { p ->
                    Tarjeta(onClick = { nav.irA(Ruta.ProductoDetalle(p.id)) }) {
                        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(p.nombre.ifBlank { "Producto sin nombre" }, fontWeight = FontWeight.SemiBold)
                                Text(ReglasCatalogo.motivoOculto(p) ?: "", color = TextoSecundario, style = MaterialTheme.typography.bodySmall)
                            }
                            Text("Editar", color = Berenjena, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}
