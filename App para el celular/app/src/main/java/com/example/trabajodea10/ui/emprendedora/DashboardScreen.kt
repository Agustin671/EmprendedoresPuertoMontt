package com.example.trabajodea10.ui.emprendedora

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.trabajodea10.data.EstadoInvitacion
import com.example.trabajodea10.data.TramaStore
import com.example.trabajodea10.data.fechaLegible
import com.example.trabajodea10.data.formatearMoneda
import com.example.trabajodea10.data.rangoDeFechas
import com.example.trabajodea10.ui.componentes.BarraSuperior
import com.example.trabajodea10.ui.componentes.ChipEstado
import com.example.trabajodea10.ui.componentes.Tarjeta
import com.example.trabajodea10.ui.componentes.TarjetaMetrica
import com.example.trabajodea10.ui.componentes.TituloSeccion
import com.example.trabajodea10.ui.componentes.Vacio
import com.example.trabajodea10.ui.componentes.colorDeInvitacion
import com.example.trabajodea10.ui.navegacion.ControladorNavegacion
import com.example.trabajodea10.ui.navegacion.Ruta
import com.example.trabajodea10.ui.theme.NaranjaAlerta
import com.example.trabajodea10.ui.theme.NaranjaContenedor
import com.example.trabajodea10.ui.theme.RojoError
import com.example.trabajodea10.ui.theme.TextoSecundario
import com.example.trabajodea10.ui.theme.VerdeOk

@Composable
fun PantallaEmprendedoraInicio(
    nav: ControladorNavegacion,
    avisar: (String) -> Unit
) {
    val tienda = TramaStore.tienda
    val alertas = TramaStore.alertasStock()
    val proxima = TramaStore.proximaFeriaDeLaEmprendedora()

    Column(modifier = Modifier.fillMaxSize()) {
        BarraSuperior(
            titulo = "Hola, Camila",
            subtitulo = tienda.nombre,
            acciones = {
                IconButton(onClick = { avisar("No tenés notificaciones nuevas") }) {
                    Icon(Icons.Filled.Notifications, contentDescription = "Notificaciones")
                }
                IconButton(onClick = { nav.irA(Ruta.Ajustes) }) {
                    Icon(Icons.Filled.Settings, contentDescription = "Ajustes")
                }
            }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                TarjetaMetrica(
                    titulo = "Ventas del mes",
                    valor = formatearMoneda(TramaStore.ventasDelMes()),
                    icono = Icons.Filled.PointOfSale,
                    colorIcono = VerdeOk,
                    modifier = Modifier.weight(1f)
                )
                TarjetaMetrica(
                    titulo = "Productos publicados",
                    valor = TramaStore.productosPublicados().toString(),
                    icono = Icons.Filled.Inventory2,
                    colorIcono = com.example.trabajodea10.ui.theme.Berenjena,
                    modifier = Modifier.weight(1f)
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                TarjetaMetrica(
                    titulo = "Stock total",
                    valor = TramaStore.stockTotal().toString(),
                    icono = Icons.Filled.Layers,
                    colorIcono = NaranjaAlerta,
                    modifier = Modifier.weight(1f)
                )
                TarjetaMetrica(
                    titulo = "Seguidores",
                    valor = TramaStore.seguidores().toString(),
                    icono = Icons.Filled.People,
                    colorIcono = com.example.trabajodea10.ui.theme.Berenjena,
                    modifier = Modifier.weight(1f)
                )
            }

            if (alertas.isNotEmpty()) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = NaranjaContenedor,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.Warning,
                            contentDescription = null,
                            tint = NaranjaAlerta,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${alertas.size} productos con stock bajo o agotado",
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = alertas.take(3).joinToString(", ") { it.nombre }.let {
                                    if (alertas.size > 3) "$it…" else it
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = TextoSecundario,
                                maxLines = 2
                            )
                        }
                        TextButton(onClick = { nav.seleccionarTab(Ruta.EmpCatalogo) }) {
                            Text("Revisar", fontWeight = FontWeight.SemiBold, color = RojoError)
                        }
                    }
                }
            }

            TituloSeccion(texto = "Próxima feria")
            val participacion = proxima?.let { feria ->
                feria to TramaStore.misFerias().firstOrNull { it.first.id == feria.id }?.second
            }
            if (participacion == null) {
                Vacio(
                    titulo = "No tenés ferias confirmadas",
                    mensaje = "Cuando te confirmen una invitación vas a verla acá con la fecha y la ubicación.",
                    icono = { Icon(Icons.Filled.EventAvailable, contentDescription = null, tint = TextoSecundario) }
                )
            } else {
                val (feria, estado) = participacion
                Tarjeta(onClick = { nav.irA(Ruta.EmpFerias) }) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = feria.nombre,
                                modifier = Modifier.weight(1f),
                                fontWeight = FontWeight.SemiBold,
                                style = MaterialTheme.typography.titleSmall
                            )
                            ChipEstado(
                                texto = estado?.invitacion?.etiqueta ?: "Confirmada",
                                color = colorDeInvitacion(estado?.invitacion ?: EstadoInvitacion.CONFIRMADA)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = rangoDeFechas(feria.fechaInicio, feria.fechaFin),
                            style = MaterialTheme.typography.bodySmall,
                            color = TextoSecundario
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Filled.Place,
                                contentDescription = null,
                                tint = TextoSecundario,
                                modifier = Modifier.height(15.dp)
                            )
                            Text(
                                text = "  ${feria.ubicacion}",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextoSecundario
                            )
                        }
                        if (estado?.asistencia != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            ChipEstado(
                                texto = "Asistencia: ${estado.asistencia.etiqueta}",
                                color = com.example.trabajodea10.ui.componentes.colorDeAsistencia(estado.asistencia)
                            )
                        }
                    }
                }
            }

            TituloSeccion(
                texto = "Últimas ventas",
                accion = "Registrar venta",
                onAccion = { nav.seleccionarTab(Ruta.EmpVenta) }
            )
            val ultimas = TramaStore.ultimasVentas(3)
            if (ultimas.isEmpty()) {
                Text(
                    text = "Todavía no registraste ventas en el mes.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextoSecundario
                )
            } else {
                Tarjeta {
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                        ultimas.forEachIndexed { index, venta ->
                            if (index > 0) {
                                androidx.compose.material3.HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = TramaStore.feriaDeLaVenta(venta.feriaId),
                                        fontWeight = FontWeight.Medium,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        text = "${fechaLegible(venta.fecha)} · ${venta.medioPago.etiqueta}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextoSecundario
                                    )
                                }
                                Text(
                                    text = formatearMoneda(venta.total),
                                    fontWeight = FontWeight.SemiBold,
                                    color = VerdeOk
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(72.dp))
        }
    }
}
