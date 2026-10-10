package com.example.trabajodea10.ui.publico

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.trabajodea10.data.Producto
import com.example.trabajodea10.data.TramaStore
import com.example.trabajodea10.data.formatearMoneda
import com.example.trabajodea10.ui.componentes.BarraSuperior
import com.example.trabajodea10.ui.componentes.BotonPrimario
import com.example.trabajodea10.ui.componentes.BotonesTamanoTexto
import com.example.trabajodea10.ui.componentes.Vacio
import com.example.trabajodea10.ui.theme.Berenjena

/**
 * PANTALLA PARA VISITANTES: "Productos".
 * Muestra el catálogo público con fotos grandes, nombre y precio en texto grande.
 * Cada producto tiene un botón para consultar por WhatsApp (un solo toque).
 */
@Composable
fun PantallaVisProductos() {
    val contexto = LocalContext.current
    val publicados = TramaStore.productos.filter { it.publicado }

    Column(modifier = Modifier.fillMaxSize()) {
        BarraSuperior(
            titulo = "Productos",
            subtitulo = "Hechos a mano por emprendedoras de Trama",
            acciones = { BotonesTamanoTexto() }
        )

        if (publicados.isEmpty()) {
            Vacio(
                titulo = "Todavía no hay productos",
                mensaje = "Las emprendedoras están preparando sus productos.",
                icono = { Text("🧶", fontSize = 44.sp) }
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(publicados, key = { it.id }) { producto ->
                    TarjetaProductoGrande(producto) {
                        abrirWhatsApp(
                            contexto = contexto,
                            numero = TramaStore.tienda.whatsapp,
                            mensaje = "Hola, me interesa el producto: ${producto.nombre}. ¿Lo tenés disponible?"
                        )
                    }
                }
            }
        }
    }
}

/** Tarjeta de producto accesible: foto grande, nombre y precio grandes, botón de consulta. */
@Composable
private fun TarjetaProductoGrande(producto: Producto, onConsultar: () -> Unit) {
    androidx.compose.material3.Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            androidx.compose.material3.MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Column {
            // "Foto" del producto: bloque de color con la inicial en grande.
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .background(Color(producto.colorFoto), RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🧶",
                    fontSize = 56.sp
                )
            }
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = producto.nombre,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = producto.categoria.etiqueta,
                        style = MaterialTheme.typography.bodyMedium,
                        color = androidx.compose.ui.graphics.Color(0xFF6B616A)
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = formatearMoneda(producto.precio),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Berenjena
                    )
                }
                Spacer(modifier = Modifier.height(14.dp))
                BotonPrimario(
                    texto = "Consultar por WhatsApp",
                    onClick = onConsultar,
                    icono = { Icon(Icons.Filled.Chat, contentDescription = null, tint = Color.White) }
                )
            }
        }
    }
}