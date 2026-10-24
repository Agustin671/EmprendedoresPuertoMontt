package com.example.trabajodea10.ui.emprendedora

import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.example.trabajodea10.data.ItemVenta
import com.example.trabajodea10.data.MedioPago
import com.example.trabajodea10.data.TramaStore
import com.example.trabajodea10.data.Venta
import com.example.trabajodea10.data.formatearMoneda
import com.example.trabajodea10.ui.componentes.BarraSuperior
import com.example.trabajodea10.ui.componentes.BotonPrimario
import com.example.trabajodea10.ui.componentes.ChipFiltro
import com.example.trabajodea10.ui.componentes.Tarjeta
import com.example.trabajodea10.ui.componentes.TituloSeccion
import com.example.trabajodea10.ui.navegacion.ControladorNavegacion
import com.example.trabajodea10.ui.navegacion.Ruta
import com.example.trabajodea10.ui.theme.Berenjena
import com.example.trabajodea10.ui.theme.TextoSecundario
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun PantallaPagoVenta(
    nav: ControladorNavegacion,
    avisar: (String) -> Unit
) {
    val context = LocalContext.current
    val carrito = TramaStore.carritoVenta
    var medio by remember { mutableStateOf<MedioPago?>(null) }
    var ventaConfirmada by remember { mutableStateOf<Venta?>(null) }
    val total = carrito.sumOf { it.precio * it.cantidad }

    LaunchedEffect(carrito.size) {
        if (carrito.isEmpty() && ventaConfirmada == null) {
            nav.volver()
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        BarraSuperior(
            titulo = "Confirmar pago",
            subtitulo = TramaStore.tienda.nombre,
            onVolver = { nav.volver() }
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TituloSeccion(texto = "Resumen de compra")

            Tarjeta {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    carrito.forEach { item ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = item.nombre, fontWeight = FontWeight.Medium)
                                Text(
                                    text = "${item.detalle} · x${item.cantidad}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextoSecundario
                                )
                            }
                            Text(
                                text = formatearMoneda(item.precio * item.cantidad),
                                fontWeight = FontWeight.SemiBold,
                                color = Berenjena
                            )
                        }
                    }
                    androidx.compose.material3.HorizontalDivider()
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text(text = "Total", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                        Text(text = formatearMoneda(total), fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Berenjena)
                    }
                }
            }

            TituloSeccion(texto = "Método de pago")

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MedioPago.entries.forEach { opcion ->
                    ChipFiltro(
                        texto = opcion.etiqueta,
                        seleccionado = medio == opcion,
                        onClick = { medio = opcion },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            BotonPrimario(
                texto = "Confirmar y generar PDF · ${formatearMoneda(total)}",
                enabled = carrito.isNotEmpty() && medio != null,
                onClick = {
                    val pago = medio ?: return@BotonPrimario
                    val venta = TramaStore.registrarVenta(carrito.toList(), pago, null)
                    val archivo = generarPdfVenta(context, venta)
                    carrito.clear()
                    medio = null
                    if (archivo != null) {
                        compartirPdf(context, archivo)
                        avisar("Venta registrada. PDF generado y listo para compartir")
                    } else {
                        avisar("Venta registrada")
                    }
                    ventaConfirmada = venta
                }
            )
            if (medio == null) {
                Text(
                    text = "Selecciona un método de pago para continuar",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextoSecundario
                )
            }
        }
    }

    ventaConfirmada?.let { venta ->
        HojaVentaConfirmadaPago(
            venta = venta,
            onNueva = {
                ventaConfirmada = null
                nav.seleccionarTab(Ruta.EmpVenta)
            },
            onInicio = {
                ventaConfirmada = null
                nav.seleccionarTab(Ruta.EmpInicio)
            }
        )
    }
}

private fun generarPdfVenta(context: Context, venta: Venta): File? {
    return try {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas
        val paint = Paint()
        val titulo = Paint()
        titulo.textSize = 18f
        titulo.isFakeBoldText = true
        val subtitulo = Paint()
        subtitulo.textSize = 12f
        val texto = Paint()
        texto.textSize = 10f

        var y = 60f
        canvas.drawText("COMPROBANTE DE VENTA - TRAMA/ALERCE", 40f, y, titulo)
        y += 24f
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        canvas.drawText("Fecha: ${venta.fecha}", 40f, y, subtitulo)
        y += 18f
        canvas.drawText("Venta ID: ${venta.id}", 40f, y, subtitulo)
        y += 18f
        canvas.drawText("Método de pago: ${venta.medioPago.etiqueta}", 40f, y, subtitulo)
        y += 30f
        canvas.drawText("DETALLE", 40f, y, titulo)
        y += 24f
        canvas.drawText("Producto", 40f, y, subtitulo)
        canvas.drawText("Cant.", 340f, y, subtitulo)
        canvas.drawText("P.Unit", 400f, y, subtitulo)
        canvas.drawText("Subtotal", 480f, y, subtitulo)
        y += 16f
        venta.items.forEach { item ->
            canvas.drawText(item.nombre.take(32), 40f, y, texto)
            canvas.drawText(item.detalle.take(16), 40f, y + 12f, Paint().apply { textSize = 9f })
            canvas.drawText("${item.cantidad}", 345f, y, texto)
            canvas.drawText("$${String.format(Locale.getDefault(), "%,.0f", item.precio)}", 400f, y, texto)
            canvas.drawText("$${String.format(Locale.getDefault(), "%,.0f", item.precio * item.cantidad)}", 480f, y, texto)
            y += 28f
        }
        y += 10f
        canvas.drawText("TOTAL: $${String.format(Locale.getDefault(), "%,.0f", venta.total)}", 400f, y, titulo)
        document.finishPage(page)

        val dir = File(context.getExternalFilesDir(null), "Comprobantes")
        if (!dir.exists()) dir.mkdirs()
        val archivo = File(dir, "venta_${venta.id}.pdf")
        FileOutputStream(archivo).use { out -> document.writeTo(out) }
        document.close()
        archivo
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

private fun compartirPdf(context: Context, archivo: File) {
    try {
        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            archivo
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Compartir comprobante PDF"))
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HojaVentaConfirmadaPago(
    venta: Venta,
    onNueva: () -> Unit,
    onInicio: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onNueva,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "¡Venta registrada!",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = formatearMoneda(venta.total),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Berenjena
            )
            Text(
                text = "Método de pago: ${venta.medioPago.etiqueta}",
                style = MaterialTheme.typography.bodyMedium,
                color = TextoSecundario
            )
            BotonPrimario(texto = "Registrar otra venta", onClick = onNueva)
            BotonPrimario(texto = "Volver al inicio", onClick = onInicio)
        }
    }
}
