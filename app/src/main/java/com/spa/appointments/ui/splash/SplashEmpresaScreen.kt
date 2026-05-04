package com.spa.appointments.ui.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Store
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import com.spa.appointments.core.theme.TemaStore
import com.spa.appointments.core.utils.Constants
import kotlinx.coroutines.delay

@Composable
fun SplashEmpresaScreen(
    onContinuar: () -> Unit
) {
    val tema by TemaStore.tema.collectAsState()

    // ── Animaciones de entrada ────────────────────────────────────────────────
    var visible by remember { mutableStateOf(false) }

    val alpha by animateFloatAsState(
        targetValue   = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 700, easing = EaseOut),
        label         = "splash_alpha"
    )
    val scale by animateFloatAsState(
        targetValue   = if (visible) 1f else 0.82f,
        animationSpec = tween(durationMillis = 700, easing = EaseOutBack),
        label         = "splash_scale"
    )

    // ── Pulso suave en el indicador de carga ─────────────────────────────────
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val indicatorAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue  = 1f,
        animationSpec = infiniteRepeatable(
            animation  = tween(900, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "indicator_alpha"
    )

    LaunchedEffect(Unit) {
        visible = true
        delay(Constants.SPLASH_DELAY_MS)
        onContinuar()
    }

    Box(
        modifier         = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primary),
        contentAlignment = Alignment.Center
    ) {
        // ── Contenido central ─────────────────────────────────────────────
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .alpha(alpha)
                .scale(scale)
                .padding(32.dp)
        ) {
            // Logo
            LogoEmpresa(
                logoUrl  = tema?.logoUrl,
                nombreApp = tema?.nombreApp ?: Constants.APP_NAME
            )

            Spacer(Modifier.height(24.dp))

            // Nombre
            Text(
                text       = tema?.nombreApp ?: Constants.APP_NAME,
                color      = MaterialTheme.colorScheme.onPrimary,
                style      = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                textAlign  = TextAlign.Center
            )

            // Slogan
            tema?.slogan?.takeIf { it.isNotBlank() }?.let { slogan ->
                Spacer(Modifier.height(8.dp))
                Text(
                    text      = slogan,
                    color     = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.80f),
                    style     = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
            }
        }

        // ── Indicador de carga abajo ──────────────────────────────────────
        CircularProgressIndicator(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp)
                .alpha(indicatorAlpha),
            color       = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f),
            strokeWidth = 2.dp
        )
    }
}

// ── Logo con fallback ─────────────────────────────────────────────────────────
@Composable
private fun LogoEmpresa(logoUrl: String?, nombreApp: String) {
    val logoModifier = Modifier
        .size(120.dp)
        .clip(CircleShape)

    if (logoUrl != null) {
        // Fondo suave detrás del logo (para logos con transparencia)
        Box(
            modifier         = logoModifier.background(
                Color.White.copy(alpha = 0.15f),
                shape = CircleShape
            ),
            contentAlignment = Alignment.Center
        ) {
            SubcomposeAsyncImage(
                model              = logoUrl,
                contentDescription = nombreApp,
                modifier           = Modifier
                    .size(96.dp)
                    .clip(CircleShape),
                contentScale       = ContentScale.Fit,
                loading = {
                    CircularProgressIndicator(
                        modifier    = Modifier.size(32.dp),
                        color       = Color.White,
                        strokeWidth = 2.dp
                    )
                },
                error = {
                    // Fallback al ícono si la URL falla
                    LogoFallback()
                }
            )
        }
    } else {
        // Sin URL configurada — mostrar fallback directamente
        Box(
            modifier         = logoModifier.background(
                Color.White.copy(alpha = 0.15f),
                shape = CircleShape
            ),
            contentAlignment = Alignment.Center
        ) {
            LogoFallback()
        }
    }
}

@Composable
private fun LogoFallback() {
    Icon(
        imageVector        = Icons.Outlined.Store,
        contentDescription = null,
        tint               = Color.White,
        modifier           = Modifier.size(56.dp)
    )
}