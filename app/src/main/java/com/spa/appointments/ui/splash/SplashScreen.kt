package com.spa.appointments.ui.splash

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.spa.appointments.core.theme.TemaStore
import com.spa.appointments.core.utils.Constants

@Composable
fun SplashScreen(
    onGoLogin:   () -> Unit,
    onGoHome:    () -> Unit,
    onGoExpired: () -> Unit,
    vm: SplashViewModel = hiltViewModel()
) {
    val destination by vm.destination.collectAsState()
    val modo        by vm.modo.collectAsState()
    val tema        by TemaStore.tema.collectAsState()

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
        // AnimatedContent hace el fade entre modo genérico y modo empresa
        AnimatedContent(
            targetState   = modo,
            transitionSpec = { fadeIn(tween(800)) togetherWith fadeOut(tween(400)) },
            label         = "splash_content"
        ) { modoActual ->
            when (modoActual) {

                SplashModo.GENERICO -> {
                    // Texto genérico del sistema
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier            = Modifier.padding(32.dp)
                    ) {
                        Text(
                            text       = "Gestión de Servicios",
                            color      = MaterialTheme.colorScheme.onPrimary,
                            fontSize   = 32.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign  = TextAlign.Center
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text      = "Asignación de citas",
                            color     = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f),
                            fontSize  = 15.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                SplashModo.EMPRESA -> {
                    // Datos reales de la empresa con fade in
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier            = Modifier.padding(32.dp)
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
                            text       = tema?.nombreApp ?: Constants.APP_NAME,
                            color      = MaterialTheme.colorScheme.onPrimary,
                            fontSize   = 32.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign  = TextAlign.Center
                        )

                        Spacer(Modifier.height(8.dp))

                        // Slogan
                        tema?.slogan?.let {
                            Text(
                                text      = it,
                                color     = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f),
                                fontSize  = 15.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}