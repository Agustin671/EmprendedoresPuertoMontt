package com.example.trabajodea10.ui.emprendedora

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.trabajodea10.data.AlAgotarse
import com.example.trabajodea10.data.Categoria
import com.example.trabajodea10.data.Modalidad
import com.example.trabajodea10.data.TramaStore
import com.example.trabajodea10.ui.componentes.BarraSuperior
import com.example.trabajodea10.ui.componentes.BotonPrimario
import com.example.trabajodea10.ui.componentes.ChipFiltro
import com.example.trabajodea10.ui.componentes.ContenedorChips
import com.example.trabajodea10.ui.componentes.FilaSwitch
import com.example.trabajodea10.ui.componentes.Tarjeta
import com.example.trabajodea10.ui.navegacion.ControladorNavegacion
import com.example.trabajodea10.ui.theme.TextoSecundario

/**
 * Configuración de ventas: interruptor del historial (RF-CFG-06) y valores
 * con los que nace un producto nuevo (RF-PRO-11).
 */
@Composable
fun PantallaConfigVentas(nav: ControladorNavegacion, avisar: (String) -> Unit) {
    val tienda = TramaStore.tienda
    var historial by remember { mutableStateOf(tienda.historialActivo) }
    var valores by remember { mutableStateOf(tienda.valoresProducto) }
    var confirmarApagar by remember { mutableStateOf(false) }

    fun guardar() {
        TramaStore.guardarConfiguracionVentas(valores, historial)
        avisar("Configuración guardada")
        nav.volver()
    }

    if (confirmarApagar) {
        AlertDialog(
            onDismissRequest = { confirmarApagar = false },
            title = { Text("¿Desactivar el historial?", fontWeight = FontWeight.Bold) },
            text = { Text("Las próximas ventas solo guardarán lo mínimo para descontar unidades (sin precio ni lugar). Las ventas anteriores no se borran.") },
            confirmButton = { TextButton(onClick = { confirmarApagar = false; historial = false }) { Text("Sí, desactivar", fontWeight = FontWeight.Bold) } },
            dismissButton = { TextButton(onClick = { confirmarApagar = false }) { Text("No") } }
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        BarraSuperior(titulo = "Ventas y productos nuevos", onVolver = { nav.volver() })
        Column(
            modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Historial de ventas", fontWeight = FontWeight.Bold, modifier = Modifier.semantics { heading() })
            Tarjeta {
                Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)) {
                    FilaSwitch(
                        titulo = "Guardar historial",
                        descripcion = if (historial) "Cada venta guarda precio, fecha y lugar" else "Solo se descuentan unidades",
                        checked = historial,
                        onCheckedChange = { if (!it) confirmarApagar = true else historial = true }
                    )
                }
            }

            Text("Un producto nuevo nace con…", fontWeight = FontWeight.Bold, modifier = Modifier.semantics { heading() })
            Text("Categoría", fontWeight = FontWeight.SemiBold)
            ContenedorChips(modifier = Modifier.fillMaxWidth()) {
                Categoria.entries.forEach { c -> ChipFiltro(texto = c.etiqueta, seleccionado = valores.categoria == c, onClick = { valores = valores.copy(categoria = c) }) }
            }
            Text("Modalidad", fontWeight = FontWeight.SemiBold)
            ContenedorChips(modifier = Modifier.fillMaxWidth()) {
                Modalidad.entries.forEach { m -> ChipFiltro(texto = m.etiqueta, seleccionado = valores.modalidad == m, onClick = { valores = valores.copy(modalidad = m) }) }
            }
            Text("Cuando se acaben las unidades", fontWeight = FontWeight.SemiBold)
            ContenedorChips(modifier = Modifier.fillMaxWidth()) {
                AlAgotarse.entries.forEach { a -> ChipFiltro(texto = a.etiqueta, seleccionado = valores.alAgotarse == a, onClick = { valores = valores.copy(alAgotarse = a) }) }
            }
            Tarjeta {
                Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)) {
                    FilaSwitch(titulo = "Llevar la cuenta de unidades", descripcion = "Cada venta descuenta unidades", checked = valores.manejaStock,
                        onCheckedChange = { valores = valores.copy(manejaStock = it) })
                    FilaSwitch(titulo = "Lo ven todos al crearlo", descripcion = if (valores.publicado) "Se publica al guardar" else "Queda «Solo yo lo veo» hasta que lo publiques",
                        checked = valores.publicado, onCheckedChange = { valores = valores.copy(publicado = it) })
                    FilaSwitch(titulo = "Disponible", descripcion = "Si lo apagas, los productos nuevos nacen no disponibles", checked = valores.disponible,
                        onCheckedChange = { valores = valores.copy(disponible = it) })
                }
            }
            Text("Siempre puedes cambiar estos datos en cada producto.", color = TextoSecundario, style = MaterialTheme.typography.bodySmall)
        }
        BotonPrimario(texto = "Guardar", onClick = { guardar() }, modifier = Modifier.padding(16.dp))
    }
}
