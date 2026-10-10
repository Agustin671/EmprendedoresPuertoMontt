package com.example.trabajodea10.ui.admin

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.trabajodea10.data.EstadoCuenta
import com.example.trabajodea10.data.Emprendedora
import com.example.trabajodea10.data.TramaStore
import com.example.trabajodea10.ui.componentes.AvatarIniciales
import com.example.trabajodea10.ui.componentes.BarraSuperior
import com.example.trabajodea10.ui.componentes.BotonPrimario
import com.example.trabajodea10.ui.componentes.BotonSecundario
import com.example.trabajodea10.ui.componentes.CampoBusqueda
import com.example.trabajodea10.ui.componentes.CampoTexto
import com.example.trabajodea10.ui.componentes.ChipEstado
import com.example.trabajodea10.ui.componentes.ChipFiltro
import com.example.trabajodea10.ui.componentes.LineaSeparadora
import com.example.trabajodea10.ui.componentes.Tarjeta
import com.example.trabajodea10.ui.componentes.TituloSeccion
import com.example.trabajodea10.ui.componentes.Vacio
import com.example.trabajodea10.ui.componentes.colorDeCuenta
import com.example.trabajodea10.ui.navegacion.ControladorNavegacion
import com.example.trabajodea10.ui.navegacion.Ruta
import com.example.trabajodea10.ui.theme.Berenjena
import com.example.trabajodea10.ui.theme.SuperficieVariante
import com.example.trabajodea10.ui.theme.TextoSecundario
import com.example.trabajodea10.ui.theme.VerdeOk

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaAdminEmprendedoras(
    nav: ControladorNavegacion,
    avisar: (String) -> Unit
) {
    var pestana by remember { mutableStateOf(0) }
    var mostrarAlta by remember { mutableStateOf(false) }
    var seleccionada by remember { mutableStateOf<Emprendedora?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        BarraSuperior(
            titulo = "Emprendedoras",
            subtitulo = "${TramaStore.emprendedoras.size} cuentas · ${TramaStore.asociaciones.size} asociaciones",
            acciones = {
                IconButton(onClick = { mostrarAlta = true }) {
                    Icon(Icons.Filled.Add, contentDescription = "Alta rápida")
                }
            }
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
            ) { Text("Directorio") }
            SegmentedButton(
                selected = pestana == 1,
                onClick = { pestana = 1 },
                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
            ) { Text("Asociaciones") }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (pestana == 0) {
            Directorio(
                onSeleccionar = { seleccionada = it },
                onAlta = { mostrarAlta = true },
                onIrAConfig = { nav.irA(Ruta.Ajustes) }
            )
        } else {
            ListadoAsociaciones()
        }
    }

    if (mostrarAlta) {
        HojaAltaEmprendedora(
            onCerrar = { mostrarAlta = false },
            onCrear = { nombre, email, tel, asociacionId, estado ->
                val ok = TramaStore.altaEmprendedora(nombre, email, tel, asociacionId, estado)
                mostrarAlta = false
                avisar(if (ok) "Cuenta creada para $nombre" else "Completá nombre y email")
            }
        )
    }

    seleccionada?.let { emprendedora ->
        HojaDetalleEmprendedora(
            emprendedora = emprendedora,
            onCerrar = { seleccionada = null },
            onCambioEstado = { estado ->
                TramaStore.cambiarEstadoEmprendedora(emprendedora.id, estado)
                seleccionada = null
                avisar("${emprendedora.nombre}: ${estado.etiqueta.lowercase()}")
            }
        )
    }
}

@Composable
private fun Directorio(
    onSeleccionar: (Emprendedora) -> Unit,
    onAlta: () -> Unit,
    onIrAConfig: () -> Unit
) {
    var busqueda by remember { mutableStateOf("") }
    var filtro by remember { mutableStateOf<EstadoCuenta?>(null) }

    val todas = TramaStore.emprendedoras
    val visibles = todas.filter { emprendedora ->
        val coincideBusqueda = emprendedora.nombre.contains(busqueda, true) ||
            emprendedora.email.contains(busqueda, true) ||
            TramaStore.nombreAsociacion(emprendedora.asociacionId).contains(busqueda, true)
        val coincideFiltro = filtro == null || emprendedora.estado == filtro
        coincideBusqueda && coincideFiltro
    }

    Column(modifier = Modifier.fillMaxSize()) {
        CampoBusqueda(
            valor = busqueda,
            onValorChange = { busqueda = it },
            modifier = Modifier.padding(horizontal = 16.dp),
            placeholder = "Buscar por nombre, email o asociación…"
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ChipFiltro(texto = "Todas", seleccionado = filtro == null, onClick = { filtro = null })
            EstadoCuenta.entries.forEach { estado ->
                ChipFiltro(
                    texto = estado.etiqueta,
                    seleccionado = filtro == estado,
                    onClick = { filtro = estado }
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (visibles.isEmpty()) {
            Vacio(
                titulo = "Sin resultados",
                mensaje = "Probá con otro nombre o quitá los filtros activos.",
                icono = { Icon(Icons.Filled.Search, contentDescription = null, tint = TextoSecundario) }
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    start = 16.dp, end = 16.dp, top = 4.dp, bottom = 96.dp
                ),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(visibles, key = { it.id }) { emprendedora ->
                    Tarjeta(onClick = { onSeleccionar(emprendedora) }) {
                        FilaEmprendedora(emprendedora)
                    }
                }
            }
        }
    }
}

@Composable
fun FilaEmprendedora(emprendedora: Emprendedora) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AvatarIniciales(nombre = emprendedora.nombre, color = emprendedora.color)
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(emprendedora.nombre, fontWeight = FontWeight.SemiBold, maxLines = 1)
            Text(
                text = emprendedora.email,
                style = MaterialTheme.typography.bodySmall,
                color = TextoSecundario,
                maxLines = 1
            )
            Text(
                text = TramaStore.nombreAsociacion(emprendedora.asociacionId),
                style = MaterialTheme.typography.bodySmall,
                color = TextoSecundario,
                maxLines = 1
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        ChipEstado(
            texto = emprendedora.estado.etiqueta,
            color = colorDeCuenta(emprendedora.estado)
        )
    }
}

@Composable
private fun ListadoAsociaciones() {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            start = 16.dp, end = 16.dp, top = 4.dp, bottom = 96.dp
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(TramaStore.asociaciones, key = { it.id }) { asociacion ->
            val integrantes = TramaStore.emprendedoras.filter { it.asociacionId == asociacion.id }
            Tarjeta {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        androidx.compose.material3.Surface(
                            shape = androidx.compose.foundation.shape.CircleShape,
                            color = Color(asociacion.color),
                            modifier = Modifier.size(14.dp)
                        ) {}
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(asociacion.nombre, fontWeight = FontWeight.SemiBold)
                            Text(
                                text = asociacion.localidad,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextoSecundario
                            )
                        }
                        ChipEstado(
                            texto = "${integrantes.size} integrantes",
                            color = com.example.trabajodea10.ui.componentes.ColorEstado(
                                SuperficieVariante, TextoSecundario
                            )
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    if (integrantes.isEmpty()) {
                        Text(
                            text = "Todavía no hay emprendedoras vinculadas.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextoSecundario
                        )
                    } else {
                        integrantes.forEachIndexed { index, integrante ->
                            if (index > 0) LineaSeparadora()
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AvatarIniciales(
                                    nombre = integrante.nombre,
                                    color = integrante.color,
                                    tamano = 32
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(integrante.nombre, style = MaterialTheme.typography.bodyMedium)
                                    Text(
                                        text = integrante.estado.etiqueta,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextoSecundario
                                    )
                                }
                                if (integrante.estado == EstadoCuenta.ACTIVA) {
                                    Icon(
                                        Icons.Filled.Check,
                                        contentDescription = "Activa",
                                        tint = VerdeOk,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Tarjeta {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.Groups, contentDescription = null, tint = Berenjena)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("¿Creás una asociación?", fontWeight = FontWeight.Medium)
                        Text(
                            text = "Se generan desde la plataforma web y quedan disponibles aquí.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextoSecundario
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HojaAltaEmprendedora(
    onCerrar: () -> Unit,
    onCrear: (String, String, String, String?, EstadoCuenta) -> Unit
) {
    var nombre by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var asociacionId by remember { mutableStateOf<String?>(null) }
    var estado by remember { mutableStateOf(EstadoCuenta.INVITADA) }

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
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TituloSeccion(texto = "Alta rápida de cuenta", accion = "Cerrar", onAccion = onCerrar)

            CampoTexto(valor = nombre, onValorChange = { nombre = it }, etiqueta = "Nombre y apellido")
            CampoTexto(
                valor = email,
                onValorChange = { email = it },
                etiqueta = "Email",
                tipoTeclado = androidx.compose.ui.text.input.KeyboardType.Email
            )
            CampoTexto(
                valor = telefono,
                onValorChange = { telefono = it },
                etiqueta = "Teléfono / WhatsApp",
                tipoTeclado = androidx.compose.ui.text.input.KeyboardType.Phone
            )

            TituloSeccion(texto = "Asociación")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ChipFiltro(
                    texto = "Sin asociación",
                    seleccionado = asociacionId == null,
                    onClick = { asociacionId = null }
                )
                TramaStore.asociaciones.forEach { asociacion ->
                    ChipFiltro(
                        texto = asociacion.nombre,
                        seleccionado = asociacionId == asociacion.id,
                        onClick = { asociacionId = asociacion.id }
                    )
                }
            }

            TituloSeccion(texto = "Estado inicial")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                EstadoCuenta.entries.forEach { opcion ->
                    ChipFiltro(
                        texto = opcion.etiqueta,
                        seleccionado = estado == opcion,
                        onClick = { estado = opcion }
                    )
                }
            }

            BotonPrimario(
                texto = "Crear cuenta",
                enabled = nombre.isNotBlank() && email.isNotBlank(),
                onClick = { onCrear(nombre, email, telefono, asociacionId, estado) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HojaDetalleEmprendedora(
    emprendedora: Emprendedora,
    onCerrar: () -> Unit,
    onCambioEstado: (EstadoCuenta) -> Unit
) {
    val asociacion = TramaStore.nombreAsociacion(emprendedora.asociacionId)
    val activa = emprendedora.estado == EstadoCuenta.ACTIVA

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
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AvatarIniciales(nombre = emprendedora.nombre, color = emprendedora.color, tamano = 52)
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(
                        emprendedora.nombre,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        asociacion,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextoSecundario
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                ChipEstado(
                    texto = emprendedora.estado.etiqueta,
                    color = colorDeCuenta(emprendedora.estado)
                )
            }

            LineaSeparadora()

            TituloSeccion(texto = "Contacto")
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Email, contentDescription = null, tint = TextoSecundario, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(emprendedora.email, style = MaterialTheme.typography.bodyMedium)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Phone, contentDescription = null, tint = TextoSecundario, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = emprendedora.telefono.ifBlank { "Sin teléfono" },
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Person, contentDescription = null, tint = TextoSecundario, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${TramaStore.nombreAsociacion(emprendedora.asociacionId)} · ${emprendedora.seguidores} seguidores",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            if (activa) {
                BotonSecundario(
                    texto = "Suspender cuenta",
                    onClick = { onCambioEstado(EstadoCuenta.SUSPENDIDA) }
                )
            } else {
                BotonPrimario(
                    texto = "Reactivar cuenta",
                    onClick = { onCambioEstado(EstadoCuenta.ACTIVA) }
                )
            }
        }
    }
}
