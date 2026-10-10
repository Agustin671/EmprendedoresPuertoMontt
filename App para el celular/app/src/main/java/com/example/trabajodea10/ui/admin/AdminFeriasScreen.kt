package com.example.trabajodea10.ui.admin

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.trabajodea10.data.EstadoFeria
import com.example.trabajodea10.data.Feria
import com.example.trabajodea10.data.TramaStore
import com.example.trabajodea10.data.formatearMoneda
import com.example.trabajodea10.data.rangoDeFechas
import com.example.trabajodea10.ui.componentes.BarraSuperior
import com.example.trabajodea10.ui.componentes.ChipEstado
import com.example.trabajodea10.ui.componentes.ChipFiltro
import com.example.trabajodea10.ui.componentes.Tarjeta
import com.example.trabajodea10.ui.componentes.Vacio
import com.example.trabajodea10.ui.componentes.colorDeFeria
import com.example.trabajodea10.ui.navegacion.ControladorNavegacion
import com.example.trabajodea10.ui.navegacion.Ruta
import com.example.trabajodea10.ui.theme.TextoSecundario
import com.example.trabajodea10.ui.theme.VerdeOk

@Composable
fun PantallaAdminFerias(
    nav: ControladorNavegacion,
    avisar: (String) -> Unit
) {
    var filtro by remember { mutableStateOf<EstadoFeria?>(null) }
    val todas = TramaStore.ferias
    val visibles = if (filtro == null) todas else todas.filter { it.estado == filtro }

    Column(modifier = Modifier.fillMaxSize()) {
        BarraSuperior(
            titulo = "Ferias",
            subtitulo = "${todas.size} eventos creados",
            acciones = {
                IconButton(onClick = { nav.irA(Ruta.Ajustes) }) {
                    Icon(Icons.Filled.Settings, contentDescription = "Ajustes")
                }
            }
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ChipFiltro(texto = "Todas", seleccionado = filtro == null, onClick = { filtro = null })
            EstadoFeria.entries.forEach { estado ->
                ChipFiltro(
                    texto = estado.etiqueta,
                    seleccionado = filtro == estado,
                    onClick = { filtro = estado }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (visibles.isEmpty()) {
            Vacio(
                titulo = "Sin ferias en esta vista",
                mensaje = "Creá una nueva feria o cambiá el filtro para ver otros eventos.",
                icono = { Icon(Icons.Filled.CalendarMonth, contentDescription = null, tint = TextoSecundario) }
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    start = 16.dp, end = 16.dp, top = 4.dp, bottom = 96.dp
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(visibles, key = { it.id }) { feria ->
                    Tarjeta(onClick = { nav.irA(Ruta.FeriaDetalle(feria.id)) }) {
                        TarjetaFeria(feria)
                    }
                }
            }
        }
    }
}

@Composable
fun TarjetaFeria(feria: Feria) {
    Column(modifier = Modifier.padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            ChipEstado(texto = feria.estado.etiqueta, color = colorDeFeria(feria.estado))
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = if (feria.precioEntrada == 0.0) "Entrada libre" else formatearMoneda(feria.precioEntrada),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = VerdeOk
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = if (feria.nombre.isBlank()) "Feria sin nombre" else feria.nombre,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            maxLines = 2
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Filled.Place,
                contentDescription = null,
                tint = TextoSecundario,
                modifier = Modifier.height(15.dp)
            )
            Text(
                text = "  ${feria.ubicacion.ifBlank { "Sin ubicación" }}",
                style = MaterialTheme.typography.bodySmall,
                color = TextoSecundario
            )
        }
        Text(
            text = rangoDeFechas(feria.fechaInicio, feria.fechaFin),
            style = MaterialTheme.typography.bodySmall,
            color = TextoSecundario
        )
        Spacer(modifier = Modifier.height(12.dp))
        val ocupados = feria.participaciones.size
        val proporcion = if (feria.cupos == 0) 0f else ocupados.toFloat() / feria.cupos
        LinearProgressIndicator(
            progress = { proporcion },
            modifier = Modifier.fillMaxWidth(),
            color = if (proporcion >= 1f) com.example.trabajodea10.ui.theme.NaranjaAlerta else VerdeOk
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "$ocupados de ${feria.cupos} cupos ocupados",
            style = MaterialTheme.typography.bodySmall,
            color = TextoSecundario
        )
    }
}
