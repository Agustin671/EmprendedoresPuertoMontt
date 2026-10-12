package com.example.trabajodea10.ui.admin

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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.trabajodea10.data.EstadoFeria
import com.example.trabajodea10.data.Feria
import com.example.trabajodea10.data.TramaStore
import com.example.trabajodea10.data.rangoDeFechas
import com.example.trabajodea10.ui.componentes.BarraSuperior
import com.example.trabajodea10.ui.componentes.BotonPrimario
import com.example.trabajodea10.ui.componentes.BotonSecundario
import com.example.trabajodea10.ui.componentes.ChipEstado
import com.example.trabajodea10.ui.componentes.Tarjeta
import com.example.trabajodea10.ui.componentes.TarjetaMetrica
import com.example.trabajodea10.ui.componentes.TituloSeccion
import com.example.trabajodea10.ui.componentes.Vacio
import com.example.trabajodea10.ui.componentes.colorDeFeria
import com.example.trabajodea10.ui.navegacion.ControladorNavegacion
import com.example.trabajodea10.ui.navegacion.Ruta
import com.example.trabajodea10.ui.theme.BlancoPuro
import com.example.trabajodea10.ui.theme.FondoLimpio
import com.example.trabajodea10.ui.theme.GrisChip
import com.example.trabajodea10.ui.theme.GrisTexto
import com.example.trabajodea10.ui.theme.NaranjaAlerta
import com.example.trabajodea10.ui.theme.NegroApple
import com.example.trabajodea10.ui.theme.RojoAlerce
import com.example.trabajodea10.ui.theme.VerdeOk

@Composable
fun PantallaAdminInicio(
    nav: ControladorNavegacion,
    avisar: (String) -> Unit
) {
    val enCurso = TramaStore.feriasEnCurso()
    val proximas = TramaStore.proximasFerias()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FondoLimpio)
    ) {
        BarraSuperior(
            titulo = "Bienvenido, Paul",
            subtitulo = "Panel de administración · Alerce Emprende",
            acciones = {
                IconButton(onClick = { nav.irA(Ruta.Ajustes) }) {
                    Icon(Icons.Filled.Settings, contentDescription = "Ajustes", tint = NegroApple)
                }
            }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                TarjetaMetrica(
                    titulo = "Ferias en curso",
                    valor = enCurso.size.toString(),
                    icono = Icons.Filled.CalendarMonth,
                    colorIcono = VerdeOk,
                    modifier = Modifier.weight(1f)
                )
                TarjetaMetrica(
                    titulo = "Próximas ferias",
                    valor = proximas.size.toString(),
                    icono = Icons.Filled.EventAvailable,
                    colorIcono = NegroApple,
                    modifier = Modifier.weight(1f)
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                TarjetaMetrica(
                    titulo = "Emprendedoras activas",
                    valor = TramaStore.cuentasActivas().toString(),
                    icono = Icons.Filled.Groups,
                    colorIcono = NegroApple,
                    modifier = Modifier.weight(1f)
                )
                TarjetaMetrica(
                    titulo = "Invitaciones pendientes",
                    valor = TramaStore.invitacionesPendientes().toString(),
                    icono = Icons.Filled.Send,
                    colorIcono = NaranjaAlerta,
                    modifier = Modifier.weight(1f)
                )
            }

            TituloSeccion(texto = "En curso ahora")

            val activa = enCurso.firstOrNull()
            if (activa == null) {
                Vacio(
                    titulo = "No hay ferias en curso",
                    mensaje = "Cuando una feria esté abierta vas a poder hacer el pase de asistencia rápido desde acá.",
                    icono = { Icon(Icons.Filled.CalendarMonth, contentDescription = null, tint = GrisTexto) }
                )
            } else {
                Tarjeta(onClick = { nav.irA(Ruta.FeriaDetalle(activa.id)) }) {
                    FeriaEnCurso(activa, nav)
                }
            }

            if (proximas.isNotEmpty()) {
                TituloSeccion(
                    texto = "Próximas ferias",
                    accion = "Ver todas",
                    onAccion = { nav.seleccionarTab(Ruta.AdminFerias) }
                )
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    proximas.forEach { feria ->
                        Tarjeta(onClick = { nav.irA(Ruta.FeriaDetalle(feria.id)) }) {
                            FeriaCompacta(feria)
                        }
                    }
                }
            }

            TituloSeccion(texto = "Acciones rápidas")
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                BotonPrimario(
                    texto = "Nueva feria",
                    onClick = {
                        val id = TramaStore.crearFeriaBorrador()
                        nav.irA(Ruta.FeriaDetalle(id))
                    },
                    modifier = Modifier.weight(1f),
                    icono = {
                        Icon(Icons.Filled.Add, contentDescription = null, tint = BlancoPuro, modifier = Modifier.size(18.dp))
                    }
                )
                BotonSecundario(
                    texto = "Directorio",
                    onClick = { nav.seleccionarTab(Ruta.AdminEmprendedoras) },
                    modifier = Modifier.weight(1f),
                    icono = {
                        Icon(Icons.Filled.Groups, contentDescription = null, tint = NegroApple, modifier = Modifier.size(18.dp))
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun FeriaEnCurso(feria: Feria, nav: ControladorNavegacion) {
    val total = feria.participaciones.size
    val presentes = feria.participaciones.count { it.asistencia == com.example.trabajodea10.data.EstadoAsistencia.PRESENTE }
    Column(modifier = Modifier.padding(18.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ChipEstado(texto = feria.estado.etiqueta, color = colorDeFeria(feria.estado))
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = formatearCupos(presentes, total),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = GrisTexto
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = feria.nombre,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = NegroApple
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Filled.Place,
                contentDescription = null,
                tint = GrisTexto,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = feria.ubicacion,
                style = MaterialTheme.typography.bodySmall,
                color = GrisTexto
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = rangoDeFechas(feria.fechaInicio, feria.fechaFin),
            style = MaterialTheme.typography.bodySmall,
            color = GrisTexto
        )
        Spacer(modifier = Modifier.height(14.dp))
        LinearProgressIndicator(
            progress = { if (total == 0) 0f else presentes.toFloat() / total },
            modifier = Modifier.fillMaxWidth().height(6.dp),
            color = VerdeOk,
            trackColor = GrisChip
        )
        Spacer(modifier = Modifier.height(16.dp))
        BotonPrimario(
            texto = "Pase de asistencia",
            onClick = { nav.irA(Ruta.FeriaDetalle(feria.id)) },
            icono = {
                Icon(Icons.Filled.Check, contentDescription = null, tint = BlancoPuro, modifier = Modifier.size(18.dp))
            }
        )
    }
}

@Composable
fun FeriaCompacta(feria: Feria) {
    Column(modifier = Modifier.padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = feria.nombre,
                modifier = Modifier.weight(1f),
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleSmall,
                color = NegroApple
            )
            ChipEstado(texto = feria.estado.etiqueta, color = colorDeFeria(feria.estado))
        }
        Spacer(modifier = Modifier.height(6.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Filled.Place,
                contentDescription = null,
                tint = GrisTexto,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "${feria.ubicacion} · ${rangoDeFechas(feria.fechaInicio, feria.fechaFin)}",
                style = MaterialTheme.typography.bodySmall,
                color = GrisTexto
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "${feria.participaciones.size}/${feria.cupos} cupos ocupados",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = GrisTexto
        )
    }
}

private fun formatearCupos(presentes: Int, total: Int): String = "$presentes de $total presentes"
