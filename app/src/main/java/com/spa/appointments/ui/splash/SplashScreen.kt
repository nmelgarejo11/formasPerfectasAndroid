package com.spa.appointments.ui.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
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

    // ── Animación de entrada fluida ──────────────────────────────────────────
    var startAnim by remember { mutableStateOf(false) }

    val alphaAnim by animateFloatAsState(
        targetValue   = if (startAnim) 1f else 0f,
        animationSpec = tween(durationMillis = 700, easing = EaseOut),
        label         = "splash_alpha"
    )
    val scaleAnim by animateFloatAsState(
        targetValue   = if (startAnim) 1f else 0.85f,
        animationSpec = tween(durationMillis = 700, easing = EaseOutBack),
        label         = "splash_scale"
    )

    // ── Indicador pulsante ────────────────────────────────────────────────────
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val indicatorAlpha by infiniteTransition.animateFloat(
        initialValue  = 0.4f,
        targetValue   = 1f,
        animationSpec = infiniteRepeatable(
            animation  = tween(900, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "indicator_alpha"
    )

    LaunchedEffect(Unit) {
        startAnim = true
    }

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
            .background(Color(0xFF191C1E)), // Fondo oscuro Slate Premium
        contentAlignment = Alignment.Center
    ) {
        // ── Contenido central ─────────────────────────────────────────────
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier            = Modifier
                .alpha(alphaAnim)
                .scale(scaleAnim)
                .padding(32.dp)
        ) {
            // Insignia / Isotipo de Prioriti
            ElevatedCard(
                shape = RoundedCornerShape(28.dp),
                colors = androidx.compose.material3.CardDefaults.elevatedCardColors(
                    containerColor = Color(0xFF2D3238)
                ),
                elevation = androidx.compose.material3.CardDefaults.elevatedCardElevation(defaultElevation = 8.dp),
                modifier = Modifier.size(104.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "P",
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFFD0BCFF)
                    )
                    Icon(
                        imageVector = Icons.Outlined.AutoAwesome,
                        contentDescription = null,
                        tint = Color(0xFFE8DEF8),
                        modifier = Modifier
                            .size(24.dp)
                            .align(Alignment.TopEnd)
                            .padding(top = 4.dp, end = 4.dp)
                    )
                }
            }

            Spacer(Modifier.height(28.dp))

            Text(
                text       = "Prioriti",
                color      = Color.White,
                style      = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                textAlign  = TextAlign.Center
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text      = "Gestión Inteligente de Citas & Servicios",
                color     = Color(0xFFCAC4D0),
                style     = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
        }

        // ── Indicador de carga (bottom) ───────────────────────────────────
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator(
                modifier    = Modifier
                    .size(28.dp)
                    .alpha(indicatorAlpha),
                color       = Color(0xFFD0BCFF),
                strokeWidth = 2.5.dp
            )

            Text(
                text = "Plataforma Multiempresa",
                style = MaterialTheme.typography.labelMedium,
                color = Color(0xFF938F99)
            )
        }
    }
}
