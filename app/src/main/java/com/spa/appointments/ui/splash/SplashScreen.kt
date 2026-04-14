package com.spa.appointments.ui.splash

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.spa.appointments.core.theme.TemaStore

@Composable
fun SplashScreen(
    onGoLogin:   () -> Unit,
    onGoHome:    () -> Unit,
    onGoExpired: () -> Unit,
    vm: SplashViewModel = hiltViewModel()
) {
    val destination    by vm.destination.collectAsState()
    val contenidoListo by vm.contenidoListo.collectAsState()
    val tema           by TemaStore.tema.collectAsState()

    var visible by remember { mutableStateOf(false) }
    val alpha   by animateFloatAsState(
        targetValue   = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 600),
        label         = "splash_fade"
    )

    // Activamos fade solo cuando el contenido está listo
    LaunchedEffect(contenidoListo) {
        if (contenidoListo) visible = true
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
        modifier         = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primary),
        contentAlignment = Alignment.Center
    ) {
        if (!contenidoListo) {
            // Mientras carga — solo spinner, sin texto genérico
            CircularProgressIndicator(
                color    = Color.White,
                modifier = Modifier.size(36.dp),
                strokeWidth = 3.dp
            )
        } else {
            // Contenido listo — fade in con datos reales de la empresa
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier            = Modifier
                    .alpha(alpha)
                    .padding(32.dp)
            ) {
                // Logo si existe
                if (tema?.logoUrl != null) {
                    AsyncImage(
                        model              = tema!!.logoUrl,
                        contentDescription = tema!!.nombreApp,
                        modifier           = Modifier
                            .size(120.dp)
                            .clip(MaterialTheme.shapes.medium),
                        contentScale       = ContentScale.Fit
                    )
                    Spacer(Modifier.height(16.dp))
                }

                // Nombre de la empresa
                Text(
                    text       = tema?.nombreApp ?: "Gestión de Servicios",
                    color      = MaterialTheme.colorScheme.onPrimary,
                    fontSize   = 32.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(8.dp))

                // Slogan
                Text(
                    text     = tema?.slogan ?: "Sistema de gestión de citas",
                    color    = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f),
                    fontSize = 15.sp
                )
            }
        }
    }
}