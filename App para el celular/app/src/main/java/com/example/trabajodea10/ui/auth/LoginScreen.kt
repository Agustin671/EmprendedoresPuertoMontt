package com.example.trabajodea10.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.trabajodea10.data.Rol
import com.example.trabajodea10.ui.componentes.Tarjeta
import com.example.trabajodea10.ui.navegacion.ControladorNavegacion
import com.example.trabajodea10.ui.theme.Berenjena
import com.example.trabajodea10.ui.theme.BerenjenaOscuro
import com.example.trabajodea10.ui.theme.SuperficieVariante
import com.example.trabajodea10.ui.theme.TextoSecundario

@Composable
fun PantallaLogin(nav: ControladorNavegacion) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Berenjena)
                .padding(top = 56.dp, bottom = 40.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Surface(shape = CircleShape, color = Color.White) {
                    Text(
                        text = "T",
                        modifier = Modifier.padding(horizontal = 22.dp, vertical = 14.dp),
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Black,
                        color = Berenjena
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Trama",
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "Ferias artesanales y emprendedoras",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.85f)
                )
            }
        }

        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Ingresá con tu perfil",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            // Opción principal, pensada para el público general y personas mayores.
            Tarjeta(onClick = { nav.iniciarSesion(Rol.VISITANTE) }) {
                FilaRol(
                    titulo = "Visitante",
                    descripcion = "Ver las próximas ferias, productos y anotarte con un solo toque.",
                    icono = { Icon(Icons.Filled.Celebration, contentDescription = null, tint = Berenjena, modifier = Modifier.size(28.dp)) }
                )
            }

            Tarjeta(onClick = { nav.iniciarSesion(Rol.ADMIN) }) {
                FilaRol(
                    titulo = "Administradora",
                    descripcion = "Gestionar ferias, emprendedoras, asociaciones y ajustes del sistema.",
                    icono = { Icon(Icons.Filled.Groups, contentDescription = null, tint = Berenjena, modifier = Modifier.size(28.dp)) }
                )
            }

            Tarjeta(onClick = { nav.iniciarSesion(Rol.EMPRENDEDORA) }) {
                FilaRol(
                    titulo = "Emprendedora",
                    descripcion = "Catálogo, ventas en el punto de venta, tienda y mis ferias.",
                    icono = { Icon(Icons.Filled.Store, contentDescription = null, tint = Berenjena, modifier = Modifier.size(28.dp)) }
                )
            }

            Text(
                text = "Plataforma de gestión para asociaciones y ferias locales.\nAcceso de demostración con datos de ejemplo.",
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                style = MaterialTheme.typography.bodySmall,
                color = TextoSecundario,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun FilaRol(
    titulo: String,
    descripcion: String,
    icono: @Composable () -> Unit
) {
    androidx.compose.foundation.layout.Row(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(shape = RoundedCornerShape(14.dp), color = SuperficieVariante) {
            Box(modifier = Modifier.padding(10.dp)) { icono() }
        }
        Spacer(modifier = Modifier.size(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(titulo, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            Text(
                descripcion,
                style = MaterialTheme.typography.bodySmall,
                color = TextoSecundario
            )
        }
        Icon(Icons.Filled.ArrowForward, contentDescription = null, tint = BerenjenaOscuro)
    }
}
