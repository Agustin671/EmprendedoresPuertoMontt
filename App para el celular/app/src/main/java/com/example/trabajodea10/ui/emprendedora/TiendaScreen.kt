package com.example.trabajodea10.ui.emprendedora

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.trabajodea10.data.Tienda
import com.example.trabajodea10.data.TramaStore
import com.example.trabajodea10.ui.componentes.AvatarIniciales
import com.example.trabajodea10.ui.componentes.BarraSuperior
import com.example.trabajodea10.ui.componentes.BotonPrimario
import com.example.trabajodea10.ui.componentes.CampoTexto
import com.example.trabajodea10.ui.componentes.Tarjeta
import com.example.trabajodea10.ui.componentes.TituloSeccion
import com.example.trabajodea10.ui.navegacion.ControladorNavegacion
import com.example.trabajodea10.ui.theme.LineaSuave
import com.example.trabajodea10.ui.theme.SuperficieVariante
import com.example.trabajodea10.ui.theme.TextoSecundario

private val COLORES_TEMA = listOf(
    0xFF5C315B, 0xFF3E7D6E, 0xFFC07A3E, 0xFF6B7FB3, 0xFF9B4A6B, 0xFF7FA66B
)

@Composable
fun PantallaTienda(
    nav: ControladorNavegacion,
    avisar: (String) -> Unit
) {
    val tienda = TramaStore.tienda
    var nombre by remember { mutableStateOf(tienda.nombre) }
    var bio by remember { mutableStateOf(tienda.bio) }
    var whatsapp by remember { mutableStateOf(tienda.whatsapp) }
    var instagram by remember { mutableStateOf(tienda.instagram) }
    var colorPortada by remember { mutableStateOf(tienda.colorPortada) }
    var colorLogo by remember { mutableStateOf(tienda.colorLogo) }

    Column(modifier = Modifier.fillMaxSize()) {
        BarraSuperior(
            titulo = "Mi tienda",
            subtitulo = "Ficha pública visible para tus clientes",
            onVolver = if (nav.pila.size > 1) ({ nav.volver() }) else null
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Tarjeta {
                Column {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                            .background(Color(colorPortada), RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Portada",
                            color = Color.White.copy(alpha = 0.75f),
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(shape = CircleShape, color = Color(colorLogo)) {
                            Box(
                                modifier = Modifier.size(56.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = nombre.firstOrNull()?.uppercase() ?: "T",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 24.sp
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = nombre.ifBlank { "Tu tienda" },
                                fontWeight = FontWeight.SemiBold,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = instagram,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextoSecundario
                            )
                        }
                    }
                }
            }

            CampoTexto(valor = nombre, onValorChange = { nombre = it }, etiqueta = "Nombre de la tienda")
            CampoTexto(
                valor = bio,
                onValorChange = { bio = it },
                etiqueta = "Descripción",
                placeholder = "Contá qué vendés…",
                esMultilinea = true
            )
            CampoTexto(
                valor = whatsapp,
                onValorChange = { whatsapp = it },
                etiqueta = "WhatsApp de contacto",
                tipoTeclado = androidx.compose.ui.text.input.KeyboardType.Phone
            )
            CampoTexto(
                valor = instagram,
                onValorChange = { instagram = it },
                etiqueta = "Instagram",
                placeholder = "@tutienda"
            )

            TituloSeccion(texto = "Color de portada")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                COLORES_TEMA.forEach { color ->
                    SelectorColor(
                        color = color,
                        seleccionado = color == colorPortada,
                        onClick = { colorPortada = color }
                    )
                }
            }

            TituloSeccion(texto = "Color del logo")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                COLORES_TEMA.forEach { color ->
                    SelectorColor(
                        color = color,
                        seleccionado = color == colorLogo,
                        onClick = { colorLogo = color }
                    )
                }
            }

            BotonPrimario(
                texto = "Guardar cambios",
                onClick = {
                    TramaStore.guardarTienda(
                        Tienda(
                            nombre = nombre.trim().ifBlank { "Mi tienda" },
                            bio = bio.trim(),
                            whatsapp = whatsapp.trim(),
                            instagram = instagram.trim(),
                            colorPortada = colorPortada,
                            colorLogo = colorLogo
                        )
                    )
                    avisar("Tienda actualizada")
                }
            )

            TituloSeccion(texto = "Vista previa pública")
            Tarjeta {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AvatarIniciales(
                        nombre = nombre.ifBlank { "T" },
                        color = colorLogo,
                        tamano = 48
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = nombre.ifBlank { "Tu tienda" },
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = bio.ifBlank { "Sin descripción todavía." },
                            style = MaterialTheme.typography.bodySmall,
                            color = TextoSecundario,
                            maxLines = 2
                        )
                    }
                    Icon(Icons.Filled.Chat, contentDescription = "WhatsApp", tint = TextoSecundario)
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
private fun SelectorColor(
    color: Long,
    seleccionado: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .background(
                if (seleccionado) SuperficieVariante else Color.Transparent,
                RoundedCornerShape(50)
            )
            .clickable(onClick = onClick)
            .padding(5.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(color), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (seleccionado) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(Color.White, CircleShape)
                )
            }
        }
    }
}
