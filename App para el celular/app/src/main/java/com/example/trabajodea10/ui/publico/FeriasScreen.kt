package com.example.trabajodea10.ui.publico

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Store
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.trabajodea10.data.EstadoFeria
import com.example.trabajodea10.data.Feria
import com.example.trabajodea10.data.FirebaseManager
import com.example.trabajodea10.data.TramaStore
import com.example.trabajodea10.data.formatearMoneda
import com.example.trabajodea10.ui.componentes.BarraSuperior
import com.example.trabajodea10.ui.componentes.BotonesTamanoTexto
import com.example.trabajodea10.ui.componentes.BotonPrimario
import com.example.trabajodea10.ui.componentes.BotonSecundario
import com.example.trabajodea10.ui.componentes.ChipEstado
import com.example.trabajodea10.ui.componentes.Tarjeta
import com.example.trabajodea10.ui.componentes.Vacio
import com.example.trabajodea10.ui.componentes.colorDeFeria
import com.example.trabajodea10.ui.theme.BlancoPuro
import com.example.trabajodea10.ui.theme.NegroApple
import com.example.trabajodea10.ui.theme.RojoAlerce
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
    // Si la administradora desactiva "Ferias visibles públicamente", dejan de mostrarse.
    val ferias = if (TramaStore.config.feriasPublicas) {
        TramaStore.ferias
            .filter { it.estado == EstadoFeria.PROXIMA || it.estado == EstadoFeria.EN_CURSO }
            .sortedBy { it.fechaInicio }
    } else {
        emptyList()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        BarraSuperior(
            titulo = if (FirebaseManager.nombreUsuarioActual.isNotBlank()) "Bienvenido(a), ${FirebaseManager.nombreUsuarioActual}" else "Próximas Ferias",
            subtitulo = "Explora ferias artesanales cerca de ti · Alerce",
            acciones = { BotonesTamanoTexto() }
        )

        if (ferias.isEmpty()) {
            Vacio(
                titulo = "No hay ferias por ahora",
                mensaje = "Pronto vas a ver acá las próximas ferias artesanales de la zona.",
                icono = { Icon(Icons.Filled.Store, contentDescription = null, tint = NegroApple, modifier = Modifier.size(44.dp)) }
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
 * Vista auténtica estilo Google Maps arriba + datos clave abajo con iconos Material limpios.
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
            // Vista Google Maps Satelital / Terreno del lugar
            VistaMapaGoogleFeria(
                feria = feria,
                onComoLlegar = onComoLlegar
            )

            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = feria.nombre.ifBlank { "Feria sin nombre" },
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(10.dp))

                FilaDatoMaterial(
                    icono = Icons.Filled.CalendarMonth,
                    colorIcono = NegroApple,
                    texto = fechaAmigable(feria.fechaInicio)
                )
                FilaDatoMaterial(
                    icono = Icons.Filled.Schedule,
                    colorIcono = NegroApple,
                    texto = "Horario: ${feria.horario}"
                )

                // Precio de entrada en texto grande y color de alto contraste.
                if (feria.precioEntrada > 0) {
                    Text(
                        text = "Entrada: ${formatearMoneda(feria.precioEntrada)}",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = TextoSecundario,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                } else {
                    Text(
                        text = "Entrada gratuita",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = VerdeOk,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }

                FilaDatoMaterial(
                    icono = Icons.Filled.LocationOn,
                    colorIcono = RojoAlerce,
                    texto = feria.ubicacion.ifBlank { "Dirección a confirmar" }
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Botón destacado: inscribirse (o confirmación verde si ya está anotada).
                if (inscrita) {
                    Surface(
                        modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
                        shape = RoundedCornerShape(16.dp),
                        color = VerdeContenedor
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 20.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = VerdeOk, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Ya estás anotada", fontWeight = FontWeight.Bold, color = VerdeOk, fontSize = 16.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                } else {
                    BotonPrimario(
                        texto = "Inscribirme a esta feria",
                        onClick = onInscribirse,
                        icono = {
                            Icon(Icons.Filled.Email, contentDescription = null, tint = BlancoPuro, modifier = Modifier.size(18.dp))
                        }
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }

                BotonSecundario(
                    texto = "Abrir en Google Maps / Cómo llegar",
                    onClick = onComoLlegar,
                    icono = {
                        Icon(Icons.Filled.Place, contentDescription = null, tint = VerdeOk)
                    }
                )
            }
        }
    }
}

/** Componente auténtico estilo Google Maps (Satelital / Terreno) */
@Composable
fun VistaMapaGoogleFeria(
    feria: Feria,
    onComoLlegar: () -> Unit,
    modifier: Modifier = Modifier
) {
    val esSatelite = feria.estiloMapa == "Satelital"

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(175.dp)
            .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
            .clickable(onClick = onComoLlegar)
    ) {
        // Lienzo geométrico que simula vista satelital/calles de Google Maps
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // Fondo base mapa
            drawRect(
                color = if (esSatelite) Color(0xFF263238) else Color(0xFFF1F5F9)
            )

            // Zonas verdes / plazas / parques
            drawCircle(
                color = if (esSatelite) Color(0xFF1E3A2F) else Color(0xFFDCFCE7),
                radius = width * 0.38f,
                center = androidx.compose.ui.geometry.Offset(width * 0.28f, height * 0.42f)
            )
            drawCircle(
                color = if (esSatelite) Color(0xFF162D24) else Color(0xFFBBF7D0),
                radius = width * 0.26f,
                center = androidx.compose.ui.geometry.Offset(width * 0.78f, height * 0.65f)
            )

            // Red de avenidas y calles
            val colorCalle = if (esSatelite) Color(0xFF455A64) else Color(0xFFFFFFFF)
            val colorAvenida = if (esSatelite) Color(0xFF546E7A) else Color(0xFFFEF3C7)

            // Avenida principal
            drawLine(
                color = colorAvenida,
                start = androidx.compose.ui.geometry.Offset(0f, height * 0.52f),
                end = androidx.compose.ui.geometry.Offset(width, height * 0.48f),
                strokeWidth = 14f
            )
            // Calle transversal
            drawLine(
                color = colorCalle,
                start = androidx.compose.ui.geometry.Offset(width * 0.50f, 0f),
                end = androidx.compose.ui.geometry.Offset(width * 0.50f, height),
                strokeWidth = 11f
            )
            // Calles secundarias
            drawLine(
                color = colorCalle,
                start = androidx.compose.ui.geometry.Offset(0f, height * 0.22f),
                end = androidx.compose.ui.geometry.Offset(width * 0.55f, height * 0.85f),
                strokeWidth = 7f
            )
            drawLine(
                color = colorCalle,
                start = androidx.compose.ui.geometry.Offset(width * 0.28f, height),
                end = androidx.compose.ui.geometry.Offset(width * 0.95f, height * 0.15f),
                strokeWidth = 7f
            )
        }

        // Pin clásico de Google Maps en el centro
        Box(
            modifier = Modifier.align(Alignment.Center),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                shape = CircleShape,
                color = Color(0xFFEA4335).copy(alpha = 0.25f),
                modifier = Modifier.size(54.dp)
            ) {}
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color.White,
                    shadowElevation = 3.dp,
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFEA4335))
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = feria.nombre.ifBlank { "Feria" },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Icon(
                    imageVector = Icons.Filled.Place,
                    contentDescription = null,
                    tint = Color(0xFFEA4335), // Rojo Google Maps
                    modifier = Modifier.size(34.dp)
                )
            }
        }

        // Badge superior izquierdo estilo Google Maps
        Surface(
            shape = RoundedCornerShape(50.dp),
            color = Color.White.copy(alpha = 0.95f),
            shadowElevation = 2.dp,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(10.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.LocationOn,
                    contentDescription = null,
                    tint = Color(0xFFEA4335),
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Google Maps · ${feria.estiloMapa}",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF334155)
                )
            }
        }

        // Estado superior derecho
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(10.dp)
        ) {
            ChipEstado(
                texto = feria.estado.etiqueta,
                color = colorDeFeria(feria.estado)
            )
        }

        // Barra inferior con ubicación y botón para navegar
        Surface(
            color = Color.Black.copy(alpha = 0.72f),
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Place,
                        contentDescription = null,
                        tint = Color(0xFFFCA5A5),
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = feria.ubicacion.ifBlank { "Alerce, Puerto Montt" },
                        color = Color.White,
                        fontSize = 11.sp,
                        maxLines = 1
                    )
                }
                Text(
                    text = "Abrir GPS ➔",
                    color = Color(0xFF93C5FD),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/** Línea de dato clave con icono Material y texto limpio */
@Composable
private fun FilaDatoMaterial(
    icono: ImageVector,
    colorIcono: Color,
    texto: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icono, contentDescription = null, tint = colorIcono, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = texto,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
    }
}