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
import kotlinx.coroutines.delay

@Composable
fun SplashEmpresaScreen(
    onContinuar: () -> Unit
) {
    val tema by TemaStore.tema.collectAsState()

    var visible by remember { mutableStateOf(false) }
    val alpha   by animateFloatAsState(
        targetValue   = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 600),
        label         = "empresa_fade"
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