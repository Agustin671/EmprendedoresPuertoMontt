package com.example.trabajodea10.ui.publico

import android.content.Context
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.trabajodea10.data.EstadoFeria
import com.example.trabajodea10.data.Feria
import com.example.trabajodea10.data.TramaStore
import com.example.trabajodea10.ui.componentes.BarraSuperior
import com.example.trabajodea10.ui.componentes.BotonesTamanoTexto
import com.example.trabajodea10.ui.componentes.BotonPrimario
import com.example.trabajodea10.ui.componentes.BotonSecundario
import com.example.trabajodea10.ui.componentes.ChipEstado
import com.example.trabajodea10.ui.componentes.Tarjeta
import com.example.trabajodea10.ui.componentes.Vacio
import com.example.trabajodea10.ui.componentes.colorDeFeria
import com.example.trabajodea10.ui.theme.VerdeContenedor
import com.example.trabajodea10.ui.theme.VerdeOk
import com.example.trabajodea10.ui.theme.TextoSecundario
import java.util.Calendar

/**
 * PANTALLA PRINCIPAL PARA VISITANTES: "Próximas Ferias".
 *
 * Pensada para personas mayores:
 *  - Tarjetas verticales grandes, una por feria, con foto de color y datos bien visibles.
 *  - Botones de 48dp+ de alto y bien separados.
 *  - Confirmación en verde y grande cuando la persona se anota.
 *  - Botón "+A / −A" arriba para agrandar todo el texto de la app.
 */

private val DIAS = listOf("Domingo", "Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado")
private val MESES_LARGOS = listOf(
    "enero", "febrero", "marzo", "abril", "mayo", "junio",
    "julio", "agosto", "septiembre", "octubre", "noviembre", "diciembre"
)

private val COLORES_IMAGEN = listOf(
    Color(0xFF5C315B), Color(0xFF3E7D6E), Color(0xFFC07A3E),
    Color(0xFF6B7FB3), Color(0xFF9B4A6B), Color(0xFF7A6B3E)
)

/** Convierte "2026-10-24" en "Sábado 24 de octubre de 2026". */
fun fechaAmigable(iso: String): String {
    val partes = iso.split(" ")[0].split("-")
    if (partes.size != 3) return iso
    val anio = partes[0].toIntOrNull()
    val mes = partes[1].toIntOrNull()
    val dia = partes[2].toIntOrNull()
    if (anio == null || mes == null || dia == null) return iso
    return runCatching {
        val calendario = Calendar.getInstance()
        calendario.clear()
        calendario.set(anio, mes - 1, dia)
        val diaNombre = DIAS[calendario.get(Calendar.DAY_OF_WEEK) - 1]
        "$diaNombre $dia de ${MESES_LARGOS.getOrElse(mes - 1) { "" }}"
    }.getOrDefault(iso)
}

@Composable
fun PantallaVisFerias(
    avisar: (String) -> Unit
) {
    val contexto = LocalContext.current

    // Guardamos el id de la feria recién anotada para mostrar la confirmación en verde.
    var anotadaRecien by remember { mutableStateOf<String?>(null) }

    // Ferias públicas: en curso y próximas, ordenadas por fecha.
    val ferias = TramaStore.ferias
        .filter { it.estado == EstadoFeria.PROXIMA || it.estado == EstadoFeria.EN_CURSO }
        .sortedBy { it.fechaInicio }

    Column(modifier = Modifier.fillMaxSize()) {
        BarraSuperior(
            titulo = "Próximas ferias",
            subtitulo = "Ferias artesanales cerca tuyo",
            acciones = { BotonesTamanoTexto() }
        )

        if (ferias.isEmpty()) {
            Vacio(
                titulo = "No hay ferias por ahora",
                mensaje = "Pronto vas a ver acá las próximas ferias artesanales de la zona.",
                icono = { Text("🎪", fontSize = 44.sp) }
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    ConfirmacionAnotada(feriaId = anotadaRecien)
                }
                items(ferias, key = { it.id }) { feria ->
                    TarjetaFeriaGrande(
                        feria = feria,
                        inscrita = TramaStore.estaInscrito(feria.id),
                        onInscribirse = {
                            TramaStore.inscribirse(feria.id)
                            anotadaRecien = feria.id
                            avisar("¡Anotación confirmada!")
                        },
                        onComoLlegar = { abrirMapa(contexto, feria.ubicacion) }
                    )
                }
            }
        }
    }
}

/**
 * Mensaje de confirmación grande y en verde que aparece justo después de anotarse.
 * Solo se muestra si el id guardado coincide con una feria todavía inscrita.
 */
@Composable
private fun ConfirmacionAnotada(feriaId: String?) {
    val feria = feriaId?.let { TramaStore.feriaPorId(it) }
    when (feria) {
        null -> Unit
        else -> Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = VerdeContenedor,
            border = BorderStroke(2.dp, VerdeOk)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Filled.CheckCircle,
                    contentDescription = null,
                    tint = VerdeOk,
                    modifier = Modifier.size(36.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "¡Te has anotado a la feria con éxito!\nNos vemos en ${feria.nombre.ifBlank { "la feria" }}.",
                    style = MaterialTheme.typography.titleMedium,
                    color = VerdeOk,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/**
 * Tarjeta grande y vertical de una feria:
 * imagen de color arriba + datos clave abajo con textos grandes.
 */
@Composable
private fun TarjetaFeriaGrande(
    feria: Feria,
    inscrita: Boolean,
    onInscribirse: () -> Unit,
    onComoLlegar: () -> Unit
) {
    Tarjeta {
        Column {
            // "Foto" de la feria: bloque de color con un emoji.
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(170.dp)
                    .background(colorImagenDe(feria), RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "🎪", fontSize = 64.sp)
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(12.dp)
                ) {
                    ChipEstado(
                        texto = feria.estado.etiqueta,
                        color = colorDeFeria(feria.estado)
                    )
                }
            }

            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = feria.nombre.ifBlank { "Feria sin nombre" },
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                FilaDatoAccesible(icono = "📅", texto = fechaAmigable(feria.fechaInicio))
                FilaDatoAccesible(icono = "🕘", texto = "Horario: ${feria.horario}")

                // Precio de entrada en texto grande y color de alto contraste.
                if (feria.precioEntrada > 0) {
                    Text(
                        text = "Entrada: \$${feria.precioEntrada.toInt()}",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = TextoSecundario
                    )
                } else {
                    Text(
                        text = "Entrada gratuita",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = VerdeOk
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                FilaDatoAccesible(icono = "📍", texto = feria.ubicacion.ifBlank { "Dirección a confirmar" })

                Spacer(modifier = Modifier.height(16.dp))

                // Botón destacado: inscribirse (o confirmación verde si ya está anotada).
                if (inscrita) {
                    Surface(
                        modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
                        shape = RoundedCornerShape(16.dp),
                        color = VerdeContenedor
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 20.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text("✓ Ya estás anotada", fontWeight = FontWeight.Bold, color = VerdeOk, fontSize = 18.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                } else {
                    BotonPrimario(
                        texto = "Inscribirme a esta feria",
                        onClick = onInscribirse,
                        icono = {
                            Text("✉️", fontSize = 18.sp)
                        }
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }

                BotonSecundario(
                    texto = "Ver mapa / Cómo llegar",
                    onClick = onComoLlegar,
                    icono = {
                        Icon(Icons.Filled.Place, contentDescription = null, tint = VerdeOk)
                    }
                )
            }
        }
    }
}

/** Una línea de "dato clave" con icono emoji y texto grande. */
@Composable
private fun FilaDatoAccesible(icono: String, texto: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = icono, fontSize = 24.sp)
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = texto,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
    }
}

fun colorImagenDe(feria: Feria): Color {
    val indice = (feria.id.hashCode() and Int.MAX_VALUE) % COLORES_IMAGEN.size
    return COLORES_IMAGEN[indice]
}