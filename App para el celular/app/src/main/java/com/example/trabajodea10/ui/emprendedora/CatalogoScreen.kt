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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
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
import com.example.trabajodea10.data.Producto
import com.example.trabajodea10.data.TramaStore
import com.example.trabajodea10.data.formatearMoneda
import com.example.trabajodea10.ui.componentes.BarraSuperior
import com.example.trabajodea10.ui.componentes.CampoBusqueda
import com.example.trabajodea10.ui.componentes.ChipEstado
import com.example.trabajodea10.ui.componentes.ChipFiltro
import com.example.trabajodea10.ui.componentes.ColorEstado
import com.example.trabajodea10.ui.componentes.Tarjeta
import com.example.trabajodea10.ui.componentes.Vacio
import com.example.trabajodea10.ui.componentes.colorDeStock
import com.example.trabajodea10.ui.navegacion.ControladorNavegacion
import com.example.trabajodea10.ui.navegacion.Ruta
import com.example.trabajodea10.ui.theme.Berenjena
import com.example.trabajodea10.ui.theme.NaranjaAlerta
import com.example.trabajodea10.ui.theme.RojoError
import com.example.trabajodea10.ui.theme.SuperficieVariante
import com.example.trabajodea10.ui.theme.TextoSecundario
import com.example.trabajodea10.ui.theme.VerdeOk

private enum class FiltroCatalogo(val etiqueta: String) {
    TODOS("Todos"),
    PUBLICADOS("Publicados"),
    BORRADORES("Borradores"),
    STOCK_BAJO("Stock bajo")
}

@Composable
fun PantallaCatalogo(
    nav: ControladorNavegacion,
    avisar: (String) -> Unit
) {
    var busqueda by remember { mutableStateOf("") }
    var filtro by remember { mutableStateOf(FiltroCatalogo.TODOS) }

    val todos = TramaStore.productos
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
            titulo = "Catálogo",
            subtitulo = "${TramaStore.productosPublicados()} publicados de ${todos.size} · stock total ${TramaStore.stockTotal()}",
            acciones = {
                IconButton(onClick = { nav.irA(Ruta.Ajustes) }) {
                    Icon(Icons.Filled.Settings, contentDescription = "Ajustes")
                }
            }
        )

        CampoBusqueda(
            valor = busqueda,
            onValorChange = { busqueda = it },
            modifier = Modifier.padding(horizontal = 16.dp),
            placeholder = "Buscar producto o categoría…"
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

        Spacer(modifier = Modifier.height(10.dp))

        if (visibles.isEmpty()) {
            Vacio(
                titulo = "Sin productos",
                mensaje = "Creá tu primer producto con el botón + o cambiá el filtro.",
                icono = { Icon(Icons.Filled.Inventory2, contentDescription = null, tint = TextoSecundario) }
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    start = 16.dp, end = 16.dp, top = 4.dp, bottom = 96.dp
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(visibles, key = { it.id }) { producto ->
                    Tarjeta(onClick = { nav.irA(Ruta.ProductoDetalle(producto.id)) }) {
                        TarjetaProducto(producto)
                    }
                }
            }
        }
    }
}

@Composable
fun TarjetaProducto(producto: Producto) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .background(Color(producto.colorFoto), RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = producto.nombre.firstOrNull()?.uppercase() ?: "?",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 26.sp
            )
        }

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
                    text = if (producto.agotado) "Agotado" else "Stock: ${producto.stockTotal}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = colorDeStock(producto.stockTotal)
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = if (producto.publicado) "Publicado" else "Borrador",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (producto.publicado) VerdeOk else NaranjaAlerta,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.width(6.dp))
                Switch(
                    checked = producto.publicado,
                    onCheckedChange = { TramaStore.togglePublicado(producto.id) },
                    colors = SwitchDefaults.colors(
                        checkedTrackColor = Berenjena,
                        uncheckedTrackColor = SuperficieVariante,
                        uncheckedBorderColor = com.example.trabajodea10.ui.theme.LineaSuave
                    )
                )
            }
        }
    }
}

@Composable
fun AlertaStockVacia() {
    Text(
        text = "Sin alertas de stock",
        color = RojoError,
        style = MaterialTheme.typography.bodySmall
    )
}
