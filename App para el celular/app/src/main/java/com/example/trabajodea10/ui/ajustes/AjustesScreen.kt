package com.example.trabajodea10.ui.ajustes

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Public
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
import androidx.compose.ui.unit.dp
import com.example.trabajodea10.data.Rol
import com.example.trabajodea10.data.TramaStore
import com.example.trabajodea10.ui.componentes.AvatarIniciales
import com.example.trabajodea10.ui.componentes.BarraSuperior
import com.example.trabajodea10.ui.componentes.BotonSecundario
import com.example.trabajodea10.ui.componentes.BotonesTamanoTexto
import com.example.trabajodea10.ui.componentes.ChipEstado
import com.example.trabajodea10.ui.componentes.DatoFila
import com.example.trabajodea10.ui.componentes.FilaSwitch
import com.example.trabajodea10.ui.componentes.LineaSeparadora
import com.example.trabajodea10.ui.componentes.Tarjeta
import com.example.trabajodea10.ui.componentes.TituloSeccion
import com.example.trabajodea10.ui.componentes.colorDeCuenta
import com.example.trabajodea10.ui.navegacion.ControladorNavegacion
import com.example.trabajodea10.ui.navegacion.Ruta
import com.example.trabajodea10.ui.theme.LineaSuave
import com.example.trabajodea10.ui.theme.RojoError
import com.example.trabajodea10.ui.theme.SuperficieVariante
import com.example.trabajodea10.ui.theme.TextoSecundario

@Composable
fun PantallaAjustes(
    nav: ControladorNavegacion,
    avisar: (String) -> Unit
) {
    val rol = TramaStore.rol
    val puedeVolver = nav.pila.size > 1

    Column(modifier = Modifier.fillMaxSize()) {
        BarraSuperior(
            titulo = "Ajustes",
            subtitulo = when (rol) {
                Rol.ADMIN -> "Configuración central"
                Rol.EMPRENDEDORA -> "Perfil, tienda y notificaciones"
                Rol.VISITANTE -> "Tamaño de texto y ayuda"
                null -> ""
            },
            onVolver = if (puedeVolver) ({ nav.volver() }) else null
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            when (rol) {
                Rol.ADMIN -> PerfilAdmin()
                Rol.EMPRENDEDORA -> PerfilEmprendedora(nav)
                Rol.VISITANTE -> PerfilVisitante()
                null -> Unit
            }

            if (rol == Rol.ADMIN) {
                AjustesSistema()
            } else if (rol == Rol.EMPRENDEDORA) {
                AjustesNotificaciones()
            }

            // Accesibilidad: disponible para todos los perfiles.
            AjustesAccesibilidad()

            BotonSecundario(
                texto = "Cerrar sesión",
                onClick = { nav.cerrarSesion() },
                icono = { Icon(Icons.Filled.Logout, contentDescription = null, tint = RojoError) },
                modifier = Modifier.padding(top = 4.dp)
            )

            Text(
                text = "Trama · versión 1.0.0 (demo con datos locales)",
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                style = MaterialTheme.typography.bodySmall,
                color = TextoSecundario,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
private fun PerfilAdmin() {
    Tarjeta {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AvatarIniciales(nombre = "Valentina Duarte", color = 0xFF5C315B, tamano = 52)
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Valentina Duarte", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = "valentina@trama.app",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextoSecundario
                )
            }
            ChipEstado(
                texto = "Super Admin",
                color = com.example.trabajodea10.ui.componentes.ColorEstado(
                    MaterialTheme.colorScheme.primaryContainer,
                    MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    }
}

@Composable
private fun PerfilEmprendedora(nav: ControladorNavegacion) {
    val emprendedora = TramaStore.emprendedoraActual
    Tarjeta {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AvatarIniciales(nombre = emprendedora.nombre, color = emprendedora.color, tamano = 52)
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(emprendedora.nombre, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = TramaStore.tienda.nombre,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextoSecundario
                )
            }
            ChipEstado(
                texto = emprendedora.estado.etiqueta,
                color = colorDeCuenta(emprendedora.estado)
            )
        }
    }

    Tarjeta {
        Column(modifier = Modifier.padding(vertical = 4.dp)) {
            FilaNavegacion(
                titulo = "Mi tienda",
                descripcion = "Logo, portada, bio y contacto",
                icono = { Icon(Icons.Filled.Store, contentDescription = null, tint = TextoSecundario) },
                onClick = { nav.irA(Ruta.EmpTienda) }
            )
            LineaSeparadora()
            FilaNavegacion(
                titulo = "Mis ferias",
                descripcion = "Invitaciones y estados de participación",
                icono = { Icon(Icons.Filled.Public, contentDescription = null, tint = TextoSecundario) },
                onClick = { nav.irA(Ruta.EmpFerias) }
            )
        }
    }
}

@Composable
private fun FilaNavegacion(
    titulo: String,
    descripcion: String,
    icono: @Composable () -> Unit,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(24.dp), contentAlignment = Alignment.Center) { icono() }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(titulo, fontWeight = FontWeight.Medium)
            Text(descripcion, style = MaterialTheme.typography.bodySmall, color = TextoSecundario)
        }
        Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = TextoSecundario)
    }
}

@Composable
private fun AjustesSistema() {
    val config = TramaStore.config

    TituloSeccion(texto = "Sistema")
    Tarjeta {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            DatoFila(etiqueta = "Moneda", valor = config.moneda)
            LineaSeparadora()
            DatoFila(etiqueta = "Zona horaria", valor = config.zonaHoraria)
            LineaSeparadora()
            DatoFila(etiqueta = "Idioma", valor = "Español (Argentina)")
        }
    }

    TituloSeccion(texto = "Interacción pública")
    Tarjeta {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            FilaSwitch(
                titulo = "Registro público de cuentas",
                descripcion = "Permite que emprendedoras se registren desde la web",
                checked = config.registroPublico,
                onCheckedChange = {
                    TramaStore.guardarConfig(config.copy(registroPublico = it))
                }
            )
            FilaSwitch(
                titulo = "Catálogo visible para visitantes",
                descripcion = "Muestra los productos publicados sin iniciar sesión",
                checked = config.catalogoPublico,
                onCheckedChange = {
                    TramaStore.guardarConfig(config.copy(catalogoPublico = it))
                }
            )
            FilaSwitch(
                titulo = "Chat directo",
                descripcion = "Habilita mensajes entre emprendedoras",
                checked = config.chatDirecto,
                onCheckedChange = {
                    TramaStore.guardarConfig(config.copy(chatDirecto = it))
                }
            )
            FilaSwitch(
                titulo = "Ferias visibles públicamente",
                descripcion = "Las ferias aparecen en el calendario público",
                checked = config.feriasPublicas,
                onCheckedChange = {
                    TramaStore.guardarConfig(config.copy(feriasPublicas = it))
                }
            )
        }
    }
}

@Composable
private fun AjustesNotificaciones() {
    val prefs = TramaStore.prefs

    TituloSeccion(texto = "Notificaciones")
    Tarjeta {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            FilaSwitch(
                titulo = "Alertas de stock bajo",
                descripcion = "Avisa cuando un producto queda con 3 unidades o menos",
                checked = prefs.alertasStock,
                onCheckedChange = { TramaStore.guardarPrefs(prefs.copy(alertasStock = it)) }
            )
            FilaSwitch(
                titulo = "Recordatorios de ferias",
                descripcion = "Te avisa 24 horas antes de una feria confirmada",
                checked = prefs.recordatoriosFeria,
                onCheckedChange = { TramaStore.guardarPrefs(prefs.copy(recordatoriosFeria = it)) }
            )
            FilaSwitch(
                titulo = "Resumen semanal de ventas",
                descripcion = "Envía un resumen los lunes",
                checked = prefs.resumenVentas,
                onCheckedChange = { TramaStore.guardarPrefs(prefs.copy(resumenVentas = it)) }
            )
        }
    }
}

@Composable
private fun PerfilVisitante() {
    Tarjeta {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AvatarIniciales(nombre = "Invitada", color = 0xFF6B7FB3, tamano = 52)
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Visitante", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = "Recorré las ferias y productos sin cuenta",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextoSecundario
                )
            }
            ChipEstado(
                texto = "Público",
                color = com.example.trabajodea10.ui.componentes.ColorEstado(
                    MaterialTheme.colorScheme.primaryContainer,
                    MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    }
}

/** Control de accesibilidad para agrandar el texto, disponible para todos los perfiles. */
@Composable
private fun AjustesAccesibilidad() {
    TituloSeccion(texto = "Accesibilidad")
    Tarjeta {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Tamaño del texto",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Agrandá todo el texto de la app para leerlo más fácil.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextoSecundario,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(modifier = Modifier.height(10.dp))
            BotonesTamanoTexto()
        }
    }
}
