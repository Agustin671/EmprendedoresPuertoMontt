package com.example.trabajodea10.ui.emprendedora

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.trabajodea10.data.EstadoSync
import com.example.trabajodea10.data.FiltrosHistorial
import com.example.trabajodea10.data.TramaStore
import com.example.trabajodea10.data.Venta
import com.example.trabajodea10.data.fechaLegible
import com.example.trabajodea10.data.formatearMoneda
import com.example.trabajodea10.data.legibleAIso
import com.example.trabajodea10.data.remoto.Sincronizador
import com.example.trabajodea10.ui.componentes.BarraSuperior
import com.example.trabajodea10.ui.componentes.BotonSecundario
import com.example.trabajodea10.ui.componentes.BotonTexto
import com.example.trabajodea10.ui.componentes.CampoTexto
import com.example.trabajodea10.ui.componentes.ChipEstado
import com.example.trabajodea10.ui.componentes.ChipFiltro
import com.example.trabajodea10.ui.componentes.ColorEstado
import com.example.trabajodea10.ui.componentes.Tarjeta
import com.example.trabajodea10.ui.componentes.Vacio
import com.example.trabajodea10.ui.navegacion.ControladorNavegacion
import com.example.trabajodea10.ui.navegacion.Ruta
import com.example.trabajodea10.ui.theme.Berenjena
import com.example.trabajodea10.ui.theme.RojoError
import com.example.trabajodea10.ui.theme.TextoSecundario

/**
 * Historial de ventas con filtros por fecha, feria y producto (RF-VEN-04).
 * Muestra también las ventas pendientes de subir y las que el servidor
 * rechazó (conflictos), con acciones para resolverlas.
 */
@Composable
fun PantallaHistorialVentas(nav: ControladorNavegacion, avisar: (String) -> Unit) {
    var desde by remember { mutableStateOf("") }
    var hasta by remember { mutableStateOf("") }
    var feria by remember { mutableStateOf<String?>(null) }
    var producto by remember { mutableStateOf<String?>(null) }
    val tienda = TramaStore.tienda

    val desdeIso = legibleAIso(desde)
    val hastaIso = legibleAIso(hasta)
    val lista = FiltrosHistorial.filtrar(TramaStore.ventas.toList(), desdeIso, hastaIso, feria, producto)
    val feriasUsadas = TramaStore.ventas.mapNotNull { it.feriaId }.distinct()
    val productosVendidos = TramaStore.ventas.flatMap { v -> v.items.map { it.productoId to it.nombre } }.distinctBy { it.first }

    Column(modifier = Modifier.fillMaxSize()) {
        BarraSuperior(
            titulo = "Historial de ventas",
            subtitulo = if (tienda.historialActivo) "${lista.size} ventas con estos filtros" else "Historial desactivado: las ventas nuevas solo descuentan unidades",
            onVolver = { nav.volver() }
        )
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            item {
                if (!tienda.historialActivo) {
                    BotonSecundario(texto = "Activar el historial", onClick = { nav.irA(Ruta.ConfigVentas) })
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CampoTexto(valor = desde, onValorChange = { desde = it.take(10) }, etiqueta = "Desde (dd/mm/aaaa)", modifier = Modifier.weight(1f), tipoTeclado = KeyboardType.Number)
                    CampoTexto(valor = hasta, onValorChange = { hasta = it.take(10) }, etiqueta = "Hasta (dd/mm/aaaa)", modifier = Modifier.weight(1f), tipoTeclado = KeyboardType.Number)
                }
                if ((desde.isNotBlank() && desdeIso == null) || (hasta.isNotBlank() && hastaIso == null)) {
                    Text("Escribe la fecha como 05/11/2026.", color = RojoError, style = MaterialTheme.typography.bodySmall)
                }
            }
            item {
                Text("Feria o lugar", fontWeight = FontWeight.SemiBold)
                Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ChipFiltro(texto = "Todas", seleccionado = feria == null, onClick = { feria = null })
                    ChipFiltro(texto = "Fuera de feria", seleccionado = feria == FiltrosHistorial.SIN_FERIA, onClick = { feria = FiltrosHistorial.SIN_FERIA })
                    feriasUsadas.forEach { id -> ChipFiltro(texto = TramaStore.feriaDeLaVenta(id), seleccionado = feria == id, onClick = { feria = id }) }
                }
            }
            item {
                Text("Producto", fontWeight = FontWeight.SemiBold)
                Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ChipFiltro(texto = "Todos", seleccionado = producto == null, onClick = { producto = null })
                    productosVendidos.forEach { (id, nombre) -> ChipFiltro(texto = nombre.ifBlank { "Producto" }, seleccionado = producto == id, onClick = { producto = id }) }
                }
                if (desde.isNotBlank() || hasta.isNotBlank() || feria != null || producto != null) {
                    BotonTexto(texto = "Quitar filtros", onClick = { desde = ""; hasta = ""; feria = null; producto = null })
                }
            }
            if (lista.isEmpty()) {
                item { Vacio(titulo = "Sin ventas", mensaje = "No hay ventas con estos filtros.") }
            }
            items(lista, key = { it.id }) { v -> FilaVenta(v, avisar) }
            item {
                if (Sincronizador.pendientes > 0) {
                    Text("${Sincronizador.pendientes} cambios por subir. Se suben solos cuando hay conexión.", color = TextoSecundario, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
private fun FilaVenta(v: Venta, avisar: (String) -> Unit) {
    Tarjeta {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row {
                Column(modifier = Modifier.weight(1f)) {
                    Text(fechaLegible(v.fecha, anio = true) + " · " + v.fecha.drop(11), fontWeight = FontWeight.SemiBold)
                    val lugar = when {
                        v.feriaId != null -> TramaStore.feriaDeLaVenta(v.feriaId)
                        v.lugarLegible.isNotBlank() -> v.lugarLegible
                        else -> "Lugar no indicado"
                    }
                    Text(lugar, color = TextoSecundario, style = MaterialTheme.typography.bodySmall)
                }
                if (v.enHistorial) Text(formatearMoneda(v.total), fontWeight = FontWeight.Bold, color = Berenjena)
            }
            v.items.forEach { i ->
                Text(
                    "• ${i.nombre} (${i.detalle}) x${i.cantidad}" + (if (v.enHistorial) " · ${formatearMoneda(i.precio)} c/u" else "") + if (i.encargo) " · encargo" else "",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            if (!v.enHistorial) Text("Registro mínimo (historial desactivado): sin precio ni lugar.", color = TextoSecundario, style = MaterialTheme.typography.bodySmall)
            when (v.estadoSync) {
                EstadoSync.PENDIENTE -> ChipEstado(texto = "Por subir al servidor", color = ColorEstado(androidx.compose.ui.graphics.Color(0xFFFFF3E6), androidx.compose.ui.graphics.Color(0xFF8A4B08)))
                EstadoSync.CONFLICTO -> {
                    ChipEstado(texto = "El servidor no la aceptó", color = ColorEstado(androidx.compose.ui.graphics.Color(0xFFFDECEC), RojoError))
                    Text(v.mensajeConflicto ?: "", color = RojoError, style = MaterialTheme.typography.bodySmall)
                    Text("Las unidades se devolvieron en tu catálogo. Repón unidades y reintenta, o anúlala si no se concretó.", style = MaterialTheme.typography.bodySmall)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        BotonSecundario(texto = "Reintentar", modifier = Modifier.weight(1f), onClick = {
                            val problema = TramaStore.reintentarVenta(v.id)
                            avisar(problema ?: "Venta enviada de nuevo")
                        })
                        BotonSecundario(texto = "Anular", modifier = Modifier.weight(1f), onClick = {
                            TramaStore.anularVentaEnConflicto(v.id)
                            avisar("Venta anulada en el celular")
                        })
                    }
                }
                EstadoSync.CONFIRMADA -> Unit
            }
        }
    }
}
