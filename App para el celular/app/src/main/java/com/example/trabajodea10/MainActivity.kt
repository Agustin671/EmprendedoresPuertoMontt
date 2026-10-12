package com.example.trabajodea10

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.trabajodea10.ui.theme.TramaAppTheme
import com.google.firebase.FirebaseApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            FirebaseApp.initializeApp(this)
        } catch (e: Throwable) {
            Log.e("MainActivity", "Error inicializando FirebaseApp: ${e.message}")
        }
        enableEdgeToEdge()
        setContent {
            TramaAppTheme {
                TramaApp()
            }
        }
    }
}
