package com.example.trabajodea10.ui.emprendedora

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.trabajodea10.data.EstadoAsistencia
import com.example.trabajodea10.data.EstadoInvitacion
import com.example.trabajodea10.data.TramaStore
import com.example.trabajodea10.data.rangoDeFechas
import com.example.trabajodea10.ui.componentes.BarraSuperior
import com.example.trabajodea10.ui.componentes.BotonPrimario
import com.example.trabajodea10.ui.componentes.BotonSecundario
import com.example.trabajodea10.ui.componentes.ChipEstado
import com.example.trabajodea10.ui.componentes.Tarjeta
import com.example.trabajodea10.ui.componentes.Vacio
import com.example.trabajodea10.ui.componentes.colorDeAsistencia
import com.example.trabajodea10.ui.componentes.colorDeFeria
import com.example.trabajodea10.ui.componentes.colorDeInvitacion
import com.example.trabajodea10.ui.navegacion.ControladorNavegacion
import com.example.trabajodea10.ui.navegacion.Ruta
import com.example.trabajodea10.ui.theme.TextoSecundario

@Composable
fun PantallaMisFerias(
    nav: ControladorNavegacion,
    avisar: (String) -> Unit
) {
    val mias = TramaStore.misFerias()

    Column(modifier = Modifier.fillMaxSize()) {
        BarraSuperior(
            titulo = "Mis ferias",
            subtitulo = "${mias.size} participaciones",
            acciones = {
                IconButton(onClick = { nav.irA(Ruta.Ajustes) }) {
                    Icon(Icons.Filled.Settings, contentDescription = "Ajustes")
                }
            }
        )

        if (mias.isEmpty()) {
            Vacio(
                titulo = "Todavía no tenés ferias",
                mensaje = "Cuando una administradora te invite a un evento vas a verlo acá.",
                icono = { Icon(Icons.Filled.EventAvailable, contentDescription = null, tint = TextoSecundario) }
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    start = 16.dp, end = 16.dp, top = 4.dp, bottom = 96.dp
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(mias, key = { it.first.id }) { (feria, participacion) ->
                    Tarjeta {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = feria.nombre,
                                    modifier = Modifier.weight(1f),
                                    fontWeight = FontWeight.SemiBold,
                                    style = MaterialTheme.typography.titleSmall
                                )
                                ChipEstado(
                                    texto = participacion.invitacion.etiqueta,
                                    color = colorDeInvitacion(participacion.invitacion)
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
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                ChipEstado(
                                    texto = feria.estado.etiqueta,
                                    color = colorDeFeria(feria.estado)
                                )
                                if (participacion.asistencia != EstadoAsistencia.PENDIENTE) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    ChipEstado(
                                        texto = "Asistencia: ${participacion.asistencia.etiqueta}",
                                        color = colorDeAsistencia(participacion.asistencia)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            when (participacion.invitacion) {
                                EstadoInvitacion.PENDIENTE -> {
                                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                        BotonPrimario(
                                            texto = "Aceptar",
                                            onClick = {
                                                TramaStore.responderInvitacion(
                                                    feria.id,
                                                    EstadoInvitacion.CONFIRMADA
                                                )
                                                avisar("Invitación aceptada")
                                            },
                                            modifier = Modifier.weight(1f)
                                        )
                                        BotonSecundario(
                                            texto = "Rechazar",
                                            onClick = {
                                                TramaStore.responderInvitacion(
                                                    feria.id,
                                                    EstadoInvitacion.RECHAZADA
                                                )
                                                avisar("Invitación rechazada")
                                            },
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }

                                EstadoInvitacion.CONFIRMADA -> {
                                    Text(
                                        text = if (feria.estado == com.example.trabajodea10.data.EstadoFeria.EN_CURSO) {
                                            "La feria está en curso: la administradora hará el pase de asistencia."
                                        } else {
                                            "Confirmada. Nos vemos en la feria."
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextoSecundario
                                    )
                                }

                                EstadoInvitacion.RECHAZADA -> {
                                    Text(
                                        text = "Rechazaste esta invitación.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextoSecundario
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
