package com.spa.appointments

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.mutableStateOf
import dagger.hilt.android.AndroidEntryPoint
import com.spa.appointments.navigation.AppNav
import com.spa.appointments.ui.theme.AppDynamicTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val pendingDestination = mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppDynamicTheme {
                AppNav(pendingDestination = pendingDestination)
            }
        }
        // Llamar DESPUÉS de setContent para que Compose ya esté listo
        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent) // actualizar el intent actual
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        android.util.Log.d("MainActivity", "handleIntent action=${intent?.action}")
        val destino = intent?.getStringExtra("destino")
        android.util.Log.d("MainActivity", "destino=$destino")
        if (!destino.isNullOrBlank()) {
            pendingDestination.value = destino
        }
    }
}