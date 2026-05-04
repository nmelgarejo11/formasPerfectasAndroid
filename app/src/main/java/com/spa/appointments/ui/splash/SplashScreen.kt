package com.spa.appointments.ui.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BookOnline
import androidx.compose.material.icons.outlined.EventNote
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun SplashScreen(
    onGoLogin:   () -> Unit,
    onGoHome:    () -> Unit,
    onGoExpired: () -> Unit,
    vm: SplashViewModel = hiltViewModel()
) {
    val destination by vm.destination.collectAsState()

    // ── Indicador pulsante ────────────────────────────────────────────────────
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val indicatorAlpha by infiniteTransition.animateFloat(
        initialValue  = 0.35f,
        targetValue   = 0.9f,
        animationSpec = infiniteRepeatable(
            animation  = tween(900, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "indicator_alpha"
    )

    LaunchedEffect(destination) {
        when (destination) {
            is SplashDestination.GoLogin   -> onGoLogin()
            is SplashDestination.GoHome    -> onGoHome()
            is SplashDestination.GoExpired -> onGoExpired()
            else                           -> Unit
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background), // fondo oscuro del tema
        contentAlignment = Alignment.Center
    ) {
        // ── Contenido central ─────────────────────────────────────────────
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier            = Modifier.padding(32.dp)
        ) {
            // Ícono en círculo
            Box(
                modifier         = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector        = Icons.Outlined.Schedule,
                    contentDescription = null,
                    tint               = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier           = Modifier.size(52.dp)
                )
            }

            Spacer(Modifier.height(28.dp))

            Text(
                text       = "Gestión de Servicios",
                color      = MaterialTheme.colorScheme.onBackground,
                style      = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                textAlign  = TextAlign.Center
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text      = "Asignación de citas",
                color     = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.65f),
                style     = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
        }

        // ── Indicador de carga (bottom) ───────────────────────────────────
        CircularProgressIndicator(
            modifier    = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp)
                .alpha(indicatorAlpha),
            color       = MaterialTheme.colorScheme.primary,
            strokeWidth = 2.dp
        )
    }
}