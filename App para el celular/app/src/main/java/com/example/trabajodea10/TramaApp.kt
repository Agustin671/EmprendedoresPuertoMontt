package com.example.trabajodea10

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.trabajodea10.data.TramaStore
import com.example.trabajodea10.ui.admin.PantallaAdminEmprendedoras
import com.example.trabajodea10.ui.admin.PantallaAdminFerias
import com.example.trabajodea10.ui.admin.PantallaAdminInicio
import com.example.trabajodea10.ui.admin.PantallaFeriaDetalle
import com.example.trabajodea10.ui.ajustes.PantallaAjustes
import com.example.trabajodea10.ui.auth.PantallaLogin
import com.example.trabajodea10.ui.emprendedora.PantallaCatalogo
import com.example.trabajodea10.ui.emprendedora.PantallaEmprendedoraInicio
import com.example.trabajodea10.ui.emprendedora.PantallaMisFerias
import com.example.trabajodea10.ui.emprendedora.PantallaProductoDetalle
import com.example.trabajodea10.ui.emprendedora.PantallaTienda
import com.example.trabajodea10.ui.emprendedora.PantallaVenta
import com.example.trabajodea10.ui.navegacion.BarraInferior
import com.example.trabajodea10.ui.navegacion.ControladorNavegacion
import com.example.trabajodea10.ui.navegacion.Ruta
import com.example.trabajodea10.ui.navegacion.rememberControladorNavegacion
import com.example.trabajodea10.ui.publico.PantallaVisAyuda
import com.example.trabajodea10.ui.publico.PantallaVisFerias
import com.example.trabajodea10.ui.publico.PantallaVisProductos
import com.example.trabajodea10.ui.theme.Berenjena
import kotlinx.coroutines.launch

@Composable
fun TramaApp() {
    val nav = rememberControladorNavegacion()
    val snackbarHostState = remember { SnackbarHostState() }
    val ambito = rememberCoroutineScope()
    val avisar: (String) -> Unit = { mensaje ->
        ambito.launch { snackbarHostState.showSnackbar(mensaje) }
    }

    val rol = TramaStore.rol
    val ruta = nav.actual

    BackHandler(enabled = nav.pila.size > 1) {
        nav.volver()
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (rol != null && ruta.grafica != null) {
                BarraInferior(
                    rol = rol,
                    rutaActual = ruta,
                    onSeleccionar = { nav.seleccionarTab(it) }
                )
            }
        },
        floatingActionButton = {
            FabSegunRuta(ruta = ruta, nav = nav)
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            ContenidoSegunRuta(ruta = ruta, nav = nav, avisar = avisar)
        }
    }
}

@Composable
private fun FabSegunRuta(
    ruta: Ruta,
    nav: ControladorNavegacion
) {
    when (ruta) {
        Ruta.AdminFerias -> Fab(
            descripcion = "Nueva feria",
            icono = { Icon(Icons.Filled.Add, contentDescription = "Nueva feria") },
            onClick = {
                val id = TramaStore.crearFeriaBorrador()
                nav.irA(Ruta.FeriaDetalle(id))
            }
        )

        Ruta.EmpInicio -> Fab(
            descripcion = "Registrar venta",
            icono = { Icon(Icons.Filled.PointOfSale, contentDescription = "Registrar venta") },
            onClick = { nav.seleccionarTab(Ruta.EmpVenta) }
        )

        Ruta.EmpCatalogo -> Fab(
            descripcion = "Nuevo producto",
            icono = { Icon(Icons.Filled.Add, contentDescription = "Nuevo producto") },
            onClick = {
                val id = TramaStore.crearProductoBorrador()
                nav.irA(Ruta.ProductoDetalle(id))
            }
        )

        else -> Unit
    }
}

@Composable
private fun Fab(
    descripcion: String,
    icono: @Composable () -> Unit,
    onClick: () -> Unit
) {
    FloatingActionButton(
        onClick = onClick,
        containerColor = Berenjena,
        contentColor = Color.White
    ) {
        icono()
    }
}

@Composable
private fun ContenidoSegunRuta(
    ruta: Ruta,
    nav: ControladorNavegacion,
    avisar: (String) -> Unit
) {
    when (ruta) {
        Ruta.Login -> PantallaLogin(nav = nav)

        Ruta.AdminInicio -> PantallaAdminInicio(nav = nav, avisar = avisar)
        Ruta.AdminFerias -> PantallaAdminFerias(nav = nav, avisar = avisar)
        Ruta.AdminEmprendedoras -> PantallaAdminEmprendedoras(nav = nav, avisar = avisar)
        Ruta.Ajustes -> PantallaAjustes(nav = nav, avisar = avisar)
        is Ruta.FeriaDetalle -> PantallaFeriaDetalle(
            feriaId = ruta.feriaId,
            nav = nav,
            avisar = avisar
        )

        Ruta.EmpInicio -> PantallaEmprendedoraInicio(nav = nav, avisar = avisar)
        Ruta.EmpCatalogo -> PantallaCatalogo(nav = nav, avisar = avisar)
        Ruta.EmpVenta -> PantallaVenta(nav = nav, avisar = avisar)
        Ruta.EmpFerias -> PantallaMisFerias(nav = nav, avisar = avisar)
        Ruta.EmpTienda -> PantallaTienda(nav = nav, avisar = avisar)
        is Ruta.ProductoDetalle -> PantallaProductoDetalle(
            productoId = ruta.productoId,
            nav = nav,
            avisar = avisar
        )

        Ruta.VisInicio -> PantallaVisFerias(avisar = avisar)
        Ruta.VisProductos -> PantallaVisProductos()
        Ruta.VisAyuda -> PantallaVisAyuda()
    }
}
