package com.example.trabajodea10

import com.example.trabajodea10.TramaApp


import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.trabajodea10.ui.theme.BlancoPuro
import com.example.trabajodea10.ui.theme.NegroApple
import com.example.trabajodea10.ui.theme.RojoAlerce
import com.example.trabajodea10.ui.theme.TramaAppTheme
import com.google.firebase.FirebaseApp
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = getSharedPreferences("app_diagnostico", Context.MODE_PRIVATE)
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                Log.e("MainActivity", "CRASH INTERCEPTADO: ${throwable.message}", throwable)
                val resumen = "${throwable.javaClass.simpleName}: ${throwable.message}\n" +
                    throwable.stackTrace.take(6).joinToString("\n") { "  en ${it.className}.${it.methodName}(${it.fileName}:${it.lineNumber})" }
                prefs.edit().putString("ultimo_error", resumen).commit()
            } catch (_: Throwable) {
            }
            defaultHandler?.uncaughtException(thread, throwable)
        }
        try {
            FirebaseApp.initializeApp(this)
        } catch (e: Throwable) {
            Log.e("MainActivity", "Error inicializando FirebaseApp: ${e.message}")
        }
        enableEdgeToEdge()
        setContent {
            com.example.trabajodea10.ui.theme.TramaAppTheme {
                val errorPrevio = remember { prefs.getString("ultimo_error", null) }
                var mostrarDialogoError by remember { mutableStateOf(errorPrevio != null) }
                TramaApp()
                if (mostrarDialogoError && errorPrevio != null) {
                    AlertDialog(
                        onDismissRequest = {
                            prefs.edit().remove("ultimo_error").apply()
                            mostrarDialogoError = false
                        },
                        title = {
                            Text(
                                text = "Diagnóstico de Estabilidad",
                                fontWeight = FontWeight.Bold,
                                color = RojoAlerce,
                                fontSize = 18.sp
                            )
                        },
                        text = {
                            Text(
                                text = "Se detectó un reporte previo:\n\n$errorPrevio",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp,
                                color = NegroApple
                            )
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    prefs.edit().remove("ultimo_error").apply()
                                    mostrarDialogoError = false
                                },
                                shape = RoundedCornerShape(50.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = NegroApple)
                            ) {
                                Text("Limpiar y Continuar", color = BlancoPuro, fontWeight = FontWeight.Bold)
                            }
                        },
                        containerColor = BlancoPuro,
                        shape = RoundedCornerShape(20.dp)
                    )
                }
            }
        }
    }
}