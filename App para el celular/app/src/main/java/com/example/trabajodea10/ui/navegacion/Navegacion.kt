package com.example.trabajodea10.ui.navegacion

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import com.example.trabajodea10.data.Rol
import com.example.trabajodea10.data.TramaStore
import com.example.trabajodea10.ui.theme.Berenjena
import com.example.trabajodea10.ui.theme.SuperficieBlanca
import com.example.trabajodea10.ui.theme.TextoSecundario

sealed class Ruta(val grafica: String? = null) {

    data object Login : Ruta()

    data object AdminInicio : Ruta("admin_inicio")
    data object AdminFerias : Ruta("admin_ferias")
    data object AdminEmprendedoras : Ruta("admin_emprendedoras")
    data object Ajustes : Ruta("ajustes")
    data class FeriaDetalle(val feriaId: String) : Ruta()

    data object EmpInicio : Ruta("emp_inicio")
    data object EmpCatalogo : Ruta("emp_catalogo")
    data object EmpVenta : Ruta("emp_venta")
    data object EmpFerias : Ruta("emp_ferias")
    data object EmpTienda : Ruta("emp_tienda")
    data class ProductoDetalle(val productoId: String) : Ruta()

    data object VisInicio : Ruta("vis_inicio")
    data object VisProductos : Ruta("vis_productos")
    data object VisAyuda : Ruta("vis_ayuda")
}

data class ItemNavegacion(
    val ruta: Ruta,
    val etiqueta: String,
    val icono: ImageVector
)

fun itemsSegunRol(rol: Rol): List<ItemNavegacion> = when (rol) {
    Rol.ADMIN -> listOf(
        ItemNavegacion(Ruta.AdminInicio, "Inicio", Icons.Filled.Home),
        ItemNavegacion(Ruta.AdminFerias, "Ferias", Icons.Filled.CalendarMonth),
        ItemNavegacion(Ruta.AdminEmprendedoras, "Emprendedoras", Icons.Filled.People),
        ItemNavegacion(Ruta.Ajustes, "Ajustes", Icons.Filled.Settings)
    )
    Rol.EMPRENDEDORA -> listOf(
        ItemNavegacion(Ruta.EmpInicio, "Inicio", Icons.Filled.Home),
        ItemNavegacion(Ruta.EmpCatalogo, "Catálogo", Icons.Filled.Inventory2),
        ItemNavegacion(Ruta.EmpVenta, "Venta", Icons.Filled.PointOfSale),
        ItemNavegacion(Ruta.EmpFerias, "Ferias", Icons.Filled.EventAvailable),
        ItemNavegacion(Ruta.EmpTienda, "Tienda", Icons.Filled.Store)
    )
    Rol.VISITANTE -> listOf(
        ItemNavegacion(Ruta.VisInicio, "Ferias", Icons.Filled.CalendarMonth),
        ItemNavegacion(Ruta.VisProductos, "Productos", Icons.Filled.Store),
        ItemNavegacion(Ruta.VisAyuda, "Ayuda", Icons.Filled.Phone)
    )
}

class ControladorNavegacion {

    val pila = mutableStateListOf<Ruta>(Ruta.Login)

    val actual: Ruta get() = pila.last()

    fun irA(ruta: Ruta) {
        if (actual != ruta) pila.add(ruta)
    }

    fun volver() {
        if (pila.size <= 1) return
        val saliente = pila.last()
        pila.removeAt(pila.lastIndex)
        when (saliente) {
            is Ruta.FeriaDetalle -> TramaStore.descartarSiEstaVacio(saliente.feriaId)
            is Ruta.ProductoDetalle -> TramaStore.descartarSiEstaVacio(saliente.productoId)
            else -> Unit
        }
    }

    fun seleccionarTab(ruta: Ruta) {
        if (actual == ruta) return
        pila.clear()
        pila.add(ruta)
    }

    fun iniciarSesion(rol: Rol) {
        TramaStore.iniciarSesion(rol)
        pila.clear()
        pila.add(
            when (rol) {
                Rol.ADMIN -> Ruta.AdminInicio
                Rol.EMPRENDEDORA -> Ruta.EmpInicio
                Rol.VISITANTE -> Ruta.VisInicio
            }
        )
    }

    fun cerrarSesion() {
        TramaStore.cerrarSesion()
        pila.clear()
        pila.add(Ruta.Login)
    }
}

@Composable
fun rememberControladorNavegacion(): ControladorNavegacion = remember { ControladorNavegacion() }

@Composable
fun BarraInferior(
    rol: Rol,
    rutaActual: Ruta,
    onSeleccionar: (Ruta) -> Unit
) {
    val items = itemsSegunRol(rol)
    NavigationBar(containerColor = SuperficieBlanca) {
        items.forEach { item ->
            val seleccionado = rutaActual.grafica != null && rutaActual.grafica == item.ruta.grafica
            NavigationBarItem(
                selected = seleccionado,
                onClick = { onSeleccionar(item.ruta) },
                icon = { Icon(item.icono, contentDescription = item.etiqueta) },
                label = {
                    Text(
                        text = item.etiqueta,
                        fontWeight = if (seleccionado) FontWeight.SemiBold else FontWeight.Normal
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Berenjena,
                    selectedTextColor = Berenjena,
                    indicatorColor = com.example.trabajodea10.ui.theme.BerenjenaContenedor,
                    unselectedIconColor = TextoSecundario,
                    unselectedTextColor = TextoSecundario
                )
            )
        }
    }
}
