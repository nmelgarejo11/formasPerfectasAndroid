package com.spa.appointments.ui.splash

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.spa.appointments.core.utils.Constants

@Composable
fun SplashScreen(
    // Estas dos funciones las recibe desde AppNav
    // El splash no sabe nada de navegación, solo avisa a quién lo llama
    onGoLogin: () -> Unit,
    onGoHome:  () -> Unit,
    onGoExpired: () -> Unit,
    vm: SplashViewModel = hiltViewModel()
) {
    // Observamos el estado del ViewModel
    // "collectAsState" convierte el StateFlow en algo que Compose entiende
    val destination by vm.destination.collectAsState()

    // Animación de aparición del logo (fade-in)
    // Empieza en 0 (invisible) y sube a 1 (visible) en 800ms
    var visible by remember { mutableStateOf(false) }
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 800),
        label = "splash_fade"
    )

    // Activamos la animación al entrar a la pantalla
    LaunchedEffect(Unit) { visible = true }



    // Reaccionamos cuando el ViewModel decide a dónde ir
    LaunchedEffect(destination) {
        when (destination) {
            is SplashDestination.GoLogin -> onGoLogin()
            is SplashDestination.GoHome  -> onGoHome()
            is SplashDestination.GoExpired -> onGoExpired()
            else -> Unit // Loading: no hacemos nada, esperamos
        }
    }

    // UI del splash
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primary),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.alpha(alpha) // Aplicamos el fade-in
        ) {
            // Aquí puedes reemplazar este Text por un Image con tu logo
            Text(
                text = Constants.APP_NAME,
                color = MaterialTheme.colorScheme.onPrimary,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Gestión de citas",
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                fontSize = 16.sp
            )
        }
    }
}