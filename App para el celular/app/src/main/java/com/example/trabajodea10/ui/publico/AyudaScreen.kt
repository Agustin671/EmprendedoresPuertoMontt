package com.example.trabajodea10.ui.publico

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.trabajodea10.ui.componentes.BarraSuperior
import com.example.trabajodea10.ui.componentes.BotonPrimario
import com.example.trabajodea10.ui.componentes.BotonSecundario
import com.example.trabajodea10.ui.componentes.BotonesTamanoTexto
import com.example.trabajodea10.ui.componentes.Tarjeta
import com.example.trabajodea10.ui.theme.VerdeOk

/** Números de contacto del soporte de Red Trama (de ejemplo). */
private const val TELEFONO_SOPORTE = "+54 9 341 555 0000"
private const val WHATSAPP_SOPORTE = "+54 9 341 555 0000"

/**
 * PANTALLA PARA VISITANTES: "Ayuda".
 * Si la persona se confunde, puede llamar o escribir a un humano con un solo toque.
 * También incluye los botones para agrandar el texto.
 */
@Composable
fun PantallaVisAyuda() {
    val contexto = LocalContext.current

    Column(modifier = Modifier.fillMaxSize()) {
        BarraSuperior(
            titulo = "Ayuda",
            subtitulo = "Estamos para ayudarte",
            acciones = { BotonesTamanoTexto() }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(PaddingValues(horizontal = 16.dp, vertical = 8.dp)),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "¿Necesitás una mano?",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Tocá un botón grande y te atendemos. No hace falta escribir nada.",
                style = MaterialTheme.typography.bodyLarge
            )

            // Botón grande para llamar por teléfono.
            BotonPrimario(
                texto = "Llamar por teléfono",
                onClick = { llamarTelefono(contexto, TELEFONO_SOPORTE) },
                icono = { Icon(Icons.Filled.Phone, contentDescription = null, tint = Color.White) }
            )

            // Botón grande para abrir WhatsApp con mensaje listo.
            BotonSecundario(
                texto = "Escribir por WhatsApp",
                onClick = {
                    abrirWhatsApp(
                        contexto = contexto,
                        numero = WHATSAPP_SOPORTE,
                        mensaje = "Hola, necesito ayuda con la app Trama."
                    )
                },
                icono = { Icon(Icons.Filled.Chat, contentDescription = null, tint = VerdeOk) }
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Recordatorio de accesibilidad: cómo agrandar el texto.
            Tarjeta {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "¿El texto te parece chico?",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Usá estos botones para agrandarlo o achicarlo.",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    BotonesTamanoTexto()
                }
            }

            Text(
                text = "Red Trama · Ferias artesanales y emprendedoras",
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center
            )
        }
    }
}