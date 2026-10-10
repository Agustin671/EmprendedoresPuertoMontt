package com.example.trabajodea10.ui.componentes

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.text.KeyboardOptions
import com.example.trabajodea10.data.EstadoAsistencia
import com.example.trabajodea10.data.EstadoCuenta
import com.example.trabajodea10.data.EstadoFeria
import com.example.trabajodea10.data.EstadoInvitacion
import com.example.trabajodea10.data.TramaStore
import com.example.trabajodea10.ui.theme.AmarilloAlerta
import com.example.trabajodea10.ui.theme.Berenjena
import com.example.trabajodea10.ui.theme.BerenjenaContenedor
import com.example.trabajodea10.ui.theme.LineaSuave
import com.example.trabajodea10.ui.theme.NaranjaAlerta
import com.example.trabajodea10.ui.theme.NaranjaContenedor
import com.example.trabajodea10.ui.theme.RojoContenedor
import com.example.trabajodea10.ui.theme.RojoError
import com.example.trabajodea10.ui.theme.SuperficieVariante
import com.example.trabajodea10.ui.theme.TextoSecundario
import com.example.trabajodea10.ui.theme.VerdeContenedor
import com.example.trabajodea10.ui.theme.VerdeOk

data class ColorEstado(val fondo: Color, val texto: Color)

fun colorDeCuenta(estado: EstadoCuenta): ColorEstado = when (estado) {
    EstadoCuenta.ACTIVA -> ColorEstado(VerdeContenedor, VerdeOk)
    EstadoCuenta.SUSPENDIDA -> ColorEstado(RojoContenedor, RojoError)
    EstadoCuenta.INVITADA -> ColorEstado(NaranjaContenedor, NaranjaAlerta)
}

fun colorDeFeria(estado: EstadoFeria): ColorEstado = when (estado) {
    EstadoFeria.EN_CURSO -> ColorEstado(VerdeContenedor, VerdeOk)
    EstadoFeria.PROXIMA -> ColorEstado(Color(0xFFEADFEA), Berenjena)
    EstadoFeria.BORRADOR -> ColorEstado(NaranjaContenedor, NaranjaAlerta)
    EstadoFeria.FINALIZADA -> ColorEstado(SuperficieVariante, TextoSecundario)
}

fun colorDeInvitacion(estado: EstadoInvitacion): ColorEstado = when (estado) {
    EstadoInvitacion.CONFIRMADA -> ColorEstado(VerdeContenedor, VerdeOk)
    EstadoInvitacion.PENDIENTE -> ColorEstado(NaranjaContenedor, NaranjaAlerta)
    EstadoInvitacion.RECHAZADA -> ColorEstado(RojoContenedor, RojoError)
}

fun colorDeAsistencia(estado: EstadoAsistencia): ColorEstado = when (estado) {
    EstadoAsistencia.PRESENTE -> ColorEstado(VerdeContenedor, VerdeOk)
    EstadoAsistencia.PENDIENTE -> ColorEstado(NaranjaContenedor, NaranjaAlerta)
    EstadoAsistencia.RECHAZADA -> ColorEstado(RojoContenedor, RojoError)
}

fun colorDeStock(cantidad: Int): Color = when {
    cantidad == 0 -> RojoError
    cantidad <= 3 -> NaranjaAlerta
    else -> VerdeOk
}

@Composable
fun BarraSuperior(
    titulo: String,
    onVolver: (() -> Unit)? = null,
    acciones: @Composable () -> Unit = {},
    subtitulo: String? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (onVolver != null) {
            IconButton(onClick = onVolver) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Volver")
            }
        } else {
            Spacer(modifier = Modifier.width(8.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = titulo,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (subtitulo != null) {
                Text(
                    text = subtitulo,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextoSecundario,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        acciones()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Tarjeta(
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    contenido: @Composable () -> Unit
) {
    if (onClick != null) {
        Card(
            onClick = onClick,
            modifier = modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            border = BorderStroke(1.dp, LineaSuave)
        ) { contenido() }
    } else {
        Card(
            modifier = modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            border = BorderStroke(1.dp, LineaSuave)
        ) { contenido() }
    }
}

@Composable
fun ChipEstado(
    texto: String,
    color: ColorEstado,
    modifier: Modifier = Modifier,
    icono: (@Composable () -> Unit)? = null
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(50),
        color = color.fondo
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            icono?.invoke()
            Text(
                text = texto,
                color = color.texto,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChipFiltro(
    texto: String,
    seleccionado: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(50),
        color = if (seleccionado) Berenjena else MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, if (seleccionado) Berenjena else LineaSuave),
        onClick = onClick
    ) {
        Text(
            text = texto,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = if (seleccionado) Color.White else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun BotonPrimario(
    texto: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icono: (@Composable () -> Unit)? = null
) {
    Button(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        enabled = enabled,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Berenjena),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 20.dp, vertical = 14.dp)
    ) {
        icono?.invoke()
        if (icono != null) Spacer(modifier = Modifier.width(8.dp))
        Text(texto, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
    }
}

@Composable
fun BotonSecundario(
    texto: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icono: (@Composable () -> Unit)? = null
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        enabled = enabled,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.5.dp, Berenjena),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 20.dp, vertical = 14.dp)
    ) {
        icono?.invoke()
        if (icono != null) Spacer(modifier = Modifier.width(8.dp))
        Text(texto, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = Berenjena)
    }
}

@Composable
fun CampoTexto(
    valor: String,
    onValorChange: (String) -> Unit,
    etiqueta: String,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    soloLectura: Boolean = false,
    esMultilinea: Boolean = false,
    tipoTeclado: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = valor,
        onValueChange = onValorChange,
        modifier = modifier.fillMaxWidth(),
        label = { Text(etiqueta) },
        placeholder = { if (placeholder.isNotBlank()) Text(placeholder) },
        readOnly = soloLectura,
        singleLine = !esMultilinea,
        minLines = if (esMultilinea) 3 else 1,
        maxLines = if (esMultilinea) 6 else 1,
        keyboardOptions = KeyboardOptions(keyboardType = tipoTeclado),
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Berenjena,
            unfocusedBorderColor = LineaSuave
        )
    )
}

@Composable
fun CampoBusqueda(
    valor: String,
    onValorChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Buscar…"
) {
    OutlinedTextField(
        value = valor,
        onValueChange = onValorChange,
        modifier = modifier.fillMaxWidth(),
        placeholder = { Text(placeholder) },
        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
        singleLine = true,
        shape = RoundedCornerShape(50),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Berenjena,
            unfocusedBorderColor = LineaSuave,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface
        )
    )
}

@Composable
fun TarjetaMetrica(
    titulo: String,
    valor: String,
    icono: androidx.compose.ui.graphics.vector.ImageVector,
    colorIcono: Color,
    modifier: Modifier = Modifier,
    pie: String? = null
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, LineaSuave)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = colorIcono.copy(alpha = 0.14f)
            ) {
                Icon(
                    imageVector = icono,
                    contentDescription = null,
                    tint = colorIcono,
                    modifier = Modifier.padding(8.dp).size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = valor,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = titulo,
                style = MaterialTheme.typography.bodySmall,
                color = TextoSecundario,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            if (pie != null) {
                Text(
                    text = pie,
                    style = MaterialTheme.typography.labelSmall,
                    color = colorIcono,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun AvatarIniciales(
    nombre: String,
    color: Long,
    modifier: Modifier = Modifier,
    tamano: Int = 44
) {
    val iniciales = nombre.split(" ")
        .filter { it.isNotBlank() }
        .take(2)
        .map { it.first().uppercaseChar() }
        .joinToString("")
    Box(
        modifier = modifier
            .size(tamano.dp)
            .background(Color(color), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = iniciales,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = (tamano / 2.6).sp
        )
    }
}

@Composable
fun TituloSeccion(
    texto: String,
    modifier: Modifier = Modifier,
    accion: String? = null,
    onAccion: (() -> Unit)? = null
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = texto,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f)
        )
        if (accion != null && onAccion != null) {
            TextButton(onClick = onAccion) {
                Text(accion, color = Berenjena, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
fun FilaSwitch(
    titulo: String,
    descripcion: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(titulo, fontWeight = FontWeight.Medium)
            Text(
                descripcion,
                style = MaterialTheme.typography.bodySmall,
                color = TextoSecundario
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedTrackColor = Berenjena)
        )
    }
}

@Composable
fun ContadorCantidad(
    cantidad: Int,
    onCambio: (Int) -> Unit,
    modifier: Modifier = Modifier,
    maximo: Int = 99
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(50),
        color = SuperficieVariante
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = { if (cantidad > 0) onCambio(cantidad - 1) },
                modifier = Modifier.size(38.dp)
            ) {
                Icon(Icons.Filled.Remove, contentDescription = "Restar", modifier = Modifier.size(18.dp))
            }
            Text(
                text = cantidad.toString(),
                fontWeight = FontWeight.Bold,
                modifier = Modifier.width(28.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            IconButton(
                onClick = { if (cantidad < maximo) onCambio(cantidad + 1) },
                modifier = Modifier.size(38.dp)
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Sumar", modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
fun Vacio(
    titulo: String,
    mensaje: String,
    modifier: Modifier = Modifier,
    icono: (@Composable () -> Unit)? = null
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(vertical = 40.dp, horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(shape = CircleShape, color = SuperficieVariante) {
            Box(modifier = Modifier.padding(16.dp)) {
                icono?.invoke()
                    ?: Text("…", fontSize = 22.sp, color = TextoSecundario)
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(titulo, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            mensaje,
            style = MaterialTheme.typography.bodyMedium,
            color = TextoSecundario,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

@Composable
fun BotonTexto(
    texto: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = Berenjena
) {
    TextButton(onClick = onClick, modifier = modifier) {
        Text(texto, color = color, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun LineaSeparadora(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(LineaSuave)
    )
}

@Composable
fun DatoFila(
    etiqueta: String,
    valor: String,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        Text(
            text = etiqueta,
            modifier = Modifier.weight(1f),
            color = TextoSecundario,
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = valor,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * Botones de accesibilidad "+A / −A" para agrandar o achicar el texto de toda la app.
 * Se muestran en la parte superior de las pantallas. Cada botón mide 48dp de alto
 * (mínimo recomendado para personas mayores) para facilitar el toque.
 */
@Composable
fun BotonesTamanoTexto(
    modifier: Modifier = Modifier,
    mostrarEtiqueta: Boolean = true
) {
    val escala = TramaStore.escalaTexto
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(50),
        color = BerenjenaContenedor,
        border = BorderStroke(1.dp, Berenjena)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (mostrarEtiqueta) {
                Text(
                    text = "Texto",
                    fontSize = 11.sp,
                    color = Berenjena,
                    fontWeight = FontWeight.Medium
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextButton(
                    onClick = { TramaStore.reducirTexto() },
                    enabled = escala > 1f,
                    modifier = Modifier.heightIn(min = 48.dp)
                ) {
                    Text(
                        text = "A−",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (escala > 1f) Berenjena else TextoSecundario
                    )
                }
                TextButton(
                    onClick = { TramaStore.aumentarTexto() },
                    enabled = escala < 1.75f,
                    modifier = Modifier.heightIn(min = 48.dp)
                ) {
                    Text(
                        text = "A+",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (escala < 1.75f) Berenjena else TextoSecundario
                    )
                }
            }
        }
    }
}
