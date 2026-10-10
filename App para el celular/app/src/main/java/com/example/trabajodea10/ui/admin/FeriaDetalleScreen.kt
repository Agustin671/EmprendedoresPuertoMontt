package com.example.trabajodea10.ui.admin

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.trabajodea10.data.EstadoAsistencia
import com.example.trabajodea10.data.EstadoFeria
import com.example.trabajodea10.data.Feria
import com.example.trabajodea10.data.Participacion
import com.example.trabajodea10.data.TramaStore
import com.example.trabajodea10.data.isoALegible
import com.example.trabajodea10.data.legibleAIso
import com.example.trabajodea10.data.rangoDeFechas
import com.example.trabajodea10.ui.componentes.AvatarIniciales
import com.example.trabajodea10.ui.componentes.BarraSuperior
import com.example.trabajodea10.ui.componentes.BotonPrimario
import com.example.trabajodea10.ui.componentes.BotonSecundario
import com.example.trabajodea10.ui.componentes.CampoBusqueda
import com.example.trabajodea10.ui.componentes.CampoTexto
import com.example.trabajodea10.ui.componentes.ChipEstado
import com.example.trabajodea10.ui.componentes.ChipFiltro
import com.example.trabajodea10.ui.componentes.Tarjeta
import com.example.trabajodea10.ui.componentes.TituloSeccion
import com.example.trabajodea10.ui.componentes.colorDeAsistencia
import com.example.trabajodea10.ui.componentes.colorDeFeria
import com.example.trabajodea10.ui.navegacion.ControladorNavegacion
import com.example.trabajodea10.ui.theme.Berenjena
import com.example.trabajodea10.ui.theme.NaranjaAlerta
import com.example.trabajodea10.ui.theme.RojoError
import com.example.trabajodea10.ui.theme.SuperficieVariante
import com.example.trabajodea10.ui.theme.TextoSecundario
import com.example.trabajodea10.ui.theme.VerdeOk

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaFeriaDetalle(
    feriaId: String,
    nav: ControladorNavegacion,
    avisar: (String) -> Unit
) {
    val feria = TramaStore.feriaPorId(feriaId)
    var pestana by remember { mutableStateOf(0) }
    var busqueda by remember { mutableStateOf("") }
    var participanteSel by remember { mutableStateOf<Participacion?>(null) }
    var mostrarInvitaciones by remember { mutableStateOf(false) }

    if (feria == null) {
        Column(modifier = Modifier.fillMaxSize()) {
            BarraSuperior(titulo = "Feria", onVolver = { nav.volver() })
            com.example.trabajodea10.ui.componentes.Vacio(
                titulo = "Feria no encontrada",
                mensaje = "El evento fue eliminado.",
                icono = { Icon(Icons.Filled.Close, contentDescription = null, tint = TextoSecundario) }
            )
        }
        return
    }

    Column(modifier = Modifier.fillMaxSize()) {
        BarraSuperior(
            titulo = feria.nombre.ifBlank { "Nueva feria" },
            subtitulo = rangoDeFechas(feria.fechaInicio, feria.fechaFin),
            onVolver = { nav.volver() }
        )

        SingleChoiceSegmentedButtonRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            SegmentedButton(
                selected = pestana == 0,
                onClick = { pestana = 0 },
                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
            ) { Text("Asistencia") }
            SegmentedButton(
                selected = pestana == 1,
                onClick = { pestana = 1 },
                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
            ) { Text("Datos del evento") }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (pestana == 0) {
            PestañaAsistencia(
                feria = feria,
                busqueda = busqueda,
                onBusqueda = { busqueda = it },
                onSeleccionar = { participanteSel = it },
                onInvitar = { mostrarInvitaciones = true }
            )
        } else {
            PestañaDatos(
                feria = feria,
                onGuardar = { guardada ->
                    TramaStore.guardarFeria(guardada)
                    avisar("Cambios guardados")
                    nav.volver()
                }
            )
        }
    }

    participanteSel?.let { participacion ->
        val emprendedora = TramaStore.emprendedoraPorId(participacion.emprendedoraId)
        ModalBottomSheet(
            onDismissRequest = { participanteSel = null },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .navigationBarsPadding()
                    .padding(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AvatarIniciales(
                        nombre = emprendedora?.nombre ?: "—",
                        color = emprendedora?.color ?: 0xFF8E6B94
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = emprendedora?.nombre ?: "Emprendedora",
                            fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = TramaStore.nombreAsociacion(emprendedora?.asociacionId),
                            style = MaterialTheme.typography.bodySmall,
                            color = TextoSecundario
                        )
                    }
                }
                TituloSeccion(texto = "Marcar asistencia")
                EstadoAsistencia.entries.forEach { estado ->
                    val color = colorDeAsistencia(estado)
                    val activo = participacion.asistencia == estado
                    Tarjeta(onClick = {
                        TramaStore.setAsistencia(feria.id, participacion.emprendedoraId, estado)
                        participanteSel = null
                        avisar("${emprendedora?.nombre ?: "Emprendedora"}: ${estado.etiqueta}")
                    }) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ChipEstado(texto = estado.etiqueta, color = color)
                            Spacer(modifier = Modifier.weight(1f))
                            if (activo) {
                                Icon(Icons.Filled.Check, contentDescription = "Actual", tint = VerdeOk)
                            }
                        }
                    }
                }
            }
        }
    }

    if (mostrarInvitaciones) {
        HojaInvitaciones(
            feria = feria,
            onCerrar = { mostrarInvitaciones = false },
            onEnviar = { ids ->
                TramaStore.enviarInvitaciones(feria.id, ids)
                mostrarInvitaciones = false
                avisar("Se enviaron ${ids.size} invitaciones")
            }
        )
    }
}

@Composable
private fun PestañaAsistencia(
    feria: Feria,
    busqueda: String,
    onBusqueda: (String) -> Unit,
    onSeleccionar: (Participacion) -> Unit,
    onInvitar: () -> Unit
) {
    val filtradas = feria.participaciones.filter {
        val nombre = TramaStore.emprendedoraPorId(it.emprendedoraId)?.nombre ?: ""
        nombre.contains(busqueda, ignoreCase = true)
    }
    val presentes = feria.participaciones.count { it.asistencia == EstadoAsistencia.PRESENTE }
    val pendientes = feria.participaciones.count { it.asistencia == EstadoAsistencia.PENDIENTE }
    val rechazadas = feria.participaciones.count { it.asistencia == EstadoAsistencia.RECHAZADA }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ResumenAsistencia("Presentes", presentes, VerdeOk, Modifier.weight(1f))
            ResumenAsistencia("Pendientes", pendientes, NaranjaAlerta, Modifier.weight(1f))
            ResumenAsistencia("Rechazadas", rechazadas, RojoError, Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(12.dp))

        CampoBusqueda(
            valor = busqueda,
            onValorChange = onBusqueda,
            modifier = Modifier.padding(horizontal = 16.dp),
            placeholder = "Buscar emprendedora…"
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (feria.participaciones.isEmpty()) {
            com.example.trabajodea10.ui.componentes.Vacio(
                titulo = "Sin inscripciones",
                mensaje = "Invitá a emprendedoras registradas para empezar el pase de asistencia.",
                icono = { Icon(Icons.Filled.Send, contentDescription = null, tint = TextoSecundario) }
            )
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    start = 16.dp, end = 16.dp, bottom = 80.dp
                ),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filtradas, key = { it.emprendedoraId }) { participacion ->
                    Tarjeta(onClick = { onSeleccionar(participacion) }) {
                        FilaParticipacion(participacion)
                    }
                }
            }
        }

        BotonSecundario(
            texto = "Invitar emprendedoras",
            onClick = onInvitar,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Composable
private fun ResumenAsistencia(
    titulo: String,
    cantidad: Int,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    androidx.compose.material3.Surface(
        modifier = modifier,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
        color = SuperficieVariante
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(cantidad.toString(), fontWeight = FontWeight.Bold, color = color)
            Text(titulo, style = MaterialTheme.typography.labelSmall, color = TextoSecundario)
        }
    }
}

@Composable
private fun FilaParticipacion(participacion: Participacion) {
    val emprendedora = TramaStore.emprendedoraPorId(participacion.emprendedoraId)
    Row(
        modifier = Modifier.fillMaxWidth().padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AvatarIniciales(
            nombre = emprendedora?.nombre ?: "??",
            color = emprendedora?.color ?: 0xFF8E6B94,
            tamano = 42
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = emprendedora?.nombre ?: "Emprendedora",
                fontWeight = FontWeight.Medium,
                maxLines = 1
            )
            Text(
                text = TramaStore.nombreAsociacion(emprendedora?.asociacionId),
                style = MaterialTheme.typography.bodySmall,
                color = TextoSecundario
            )
        }
        ChipEstado(
            texto = participacion.asistencia.etiqueta,
            color = colorDeAsistencia(participacion.asistencia)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HojaInvitaciones(
    feria: Feria,
    onCerrar: () -> Unit,
    onEnviar: (List<String>) -> Unit
) {
    val yaInvitadas = feria.participaciones.map { it.emprendedoraId }.toSet()
    val candidatas = TramaStore.emprendedoras.filter {
        it.id !in yaInvitadas && it.estado != com.example.trabajodea10.data.EstadoCuenta.SUSPENDIDA
    }
    var seleccion by remember { mutableStateOf(setOf<String>()) }

    ModalBottomSheet(
        onDismissRequest = onCerrar,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .navigationBarsPadding()
                .padding(bottom = 20.dp)
        ) {
            TituloSeccion(
                texto = "Invitar a ${feria.nombre.ifBlank { "la feria" }}",
                accion = "Cerrar",
                onAccion = onCerrar
            )
            if (candidatas.isEmpty()) {
                com.example.trabajodea10.ui.componentes.Vacio(
                    titulo = "Ya invitaste a todas",
                    mensaje = "No quedan emprendedoras disponibles para invitar.",
                    icono = { Icon(Icons.Filled.Check, contentDescription = null, tint = VerdeOk) }
                )
            } else {
                LazyColumn(
                    modifier = Modifier.heightIn(max = 380.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(candidatas, key = { it.id }) { emprendedora ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    seleccion = if (emprendedora.id in seleccion) {
                                        seleccion - emprendedora.id
                                    } else {
                                        seleccion + emprendedora.id
                                    }
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AvatarIniciales(nombre = emprendedora.nombre, color = emprendedora.color, tamano = 38)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(emprendedora.nombre, fontWeight = FontWeight.Medium)
                                Text(
                                    text = "${TramaStore.nombreAsociacion(emprendedora.asociacionId)} · ${emprendedora.estado.etiqueta}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextoSecundario
                                )
                            }
                            androidx.compose.material3.Checkbox(
                                checked = emprendedora.id in seleccion,
                                onCheckedChange = { marcada ->
                                    seleccion = if (marcada) seleccion + emprendedora.id else seleccion - emprendedora.id
                                }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                BotonPrimario(
                    texto = if (seleccion.isEmpty()) "Seleccioná emprendedoras" else "Enviar ${seleccion.size} invitaciones",
                    enabled = seleccion.isNotEmpty(),
                    onClick = { onEnviar(seleccion.toList()) }
                )
            }
        }
    }
}

private fun Modifier.heightInMax(): Modifier = this

@Composable
private fun PestañaDatos(
    feria: Feria,
    onGuardar: (Feria) -> Unit
) {
    var nombre by remember(feria.id) { mutableStateOf(feria.nombre) }
    var ubicacion by remember(feria.id) { mutableStateOf(feria.ubicacion) }
    var inicio by remember(feria.id) { mutableStateOf(isoALegible(feria.fechaInicio)) }
    var fin by remember(feria.id) { mutableStateOf(isoALegible(feria.fechaFin)) }
    var cupos by remember(feria.id) { mutableStateOf(feria.cupos.toString()) }
    var precio by remember(feria.id) { mutableStateOf(feria.precioEntrada.toInt().toString()) }
    var estado by remember(feria.id) { mutableStateOf(feria.estado) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        CampoTexto(valor = nombre, onValorChange = { nombre = it }, etiqueta = "Nombre de la feria")
        CampoTexto(valor = ubicacion, onValorChange = { ubicacion = it }, etiqueta = "Ubicación")

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            CampoTexto(
                valor = inicio,
                onValorChange = { inicio = it },
                etiqueta = "Inicio (dd/mm/aaaa)",
                placeholder = "01/11/2026",
                modifier = Modifier.weight(1f)
            )
            CampoTexto(
                valor = fin,
                onValorChange = { fin = it },
                etiqueta = "Fin (dd/mm/aaaa)",
                placeholder = "02/11/2026",
                modifier = Modifier.weight(1f)
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            CampoTexto(
                valor = cupos,
                onValorChange = { cupos = it.filter { c -> c.isDigit() } },
                etiqueta = "Cupos",
                tipoTeclado = androidx.compose.ui.text.input.KeyboardType.Number,
                modifier = Modifier.weight(1f)
            )
            CampoTexto(
                valor = precio,
                onValorChange = { precio = it.filter { c -> c.isDigit() } },
                etiqueta = "Precio de entrada ($)",
                tipoTeclado = androidx.compose.ui.text.input.KeyboardType.Number,
                modifier = Modifier.weight(1f)
            )
        }

        TituloSeccion(texto = "Estado")
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
        ) {
            EstadoFeria.entries.forEach { opcion ->
                ChipFiltro(
                    texto = opcion.etiqueta,
                    seleccionado = estado == opcion,
                    onClick = { estado = opcion }
                )
            }
        }

        BotonPrimario(
            texto = "Guardar cambios",
            onClick = {
                val nuevoInicio = legibleAIso(inicio) ?: feria.fechaInicio
                val nuevoFin = legibleAIso(fin) ?: feria.fechaFin
                onGuardar(
                    feria.copy(
                        nombre = nombre.trim(),
                        ubicacion = ubicacion.trim(),
                        fechaInicio = nuevoInicio,
                        fechaFin = nuevoFin,
                        cupos = cupos.toIntOrNull() ?: feria.cupos,
                        precioEntrada = precio.toDoubleOrNull() ?: feria.precioEntrada,
                        estado = estado
                    )
                )
            }
        )
        Spacer(modifier = Modifier.height(24.dp))
    }
}

