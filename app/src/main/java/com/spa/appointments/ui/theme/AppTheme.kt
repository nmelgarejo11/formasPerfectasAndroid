package com.spa.appointments.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import com.spa.appointments.core.theme.TemaStore
import com.spa.appointments.core.theme.hexToColor

@Composable
fun AppDynamicTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content:   @Composable () -> Unit
) {
    val tema by TemaStore.tema.collectAsState()

    val colorPrimario   = tema?.let { hexToColor(it.colorPrimario) }
        ?: Color(0xFF6650A4)
    val colorSecundario = tema?.let { hexToColor(it.colorSecundario) }
        ?: Color(0xFF625B71)
    val colorTerciario  = tema?.let { hexToColor(it.colorTerciario) }
        ?: Color(0xFF7D5260)

    val lightColors = lightColorScheme(
        primary              = colorPrimario,
        onPrimary            = Color.White,
        primaryContainer     = colorPrimario.copy(alpha = 0.12f),
        onPrimaryContainer   = colorPrimario,
        secondary            = colorSecundario,
        onSecondary          = Color.White,
        secondaryContainer   = colorSecundario.copy(alpha = 0.12f),
        onSecondaryContainer = colorSecundario,
        tertiary             = colorTerciario,
        onTertiary           = Color.White,
        tertiaryContainer    = colorTerciario.copy(alpha = 0.12f),
        onTertiaryContainer  = colorTerciario,
        // Fondos siempre claros en modo claro
        background           = Color(0xFFFFFBFE),
        onBackground         = Color(0xFF1C1B1F),
        surface              = Color(0xFFFFFFFF),  // cards blancas
        onSurface            = Color(0xFF1C1B1F),  // texto negro
        surfaceVariant       = Color(0xFFF5F5F5),  // fondo suave
        onSurfaceVariant     = Color(0xFF49454F),  // texto secundario
        outline              = Color(0xFF79747E),

    )

    val darkColors = darkColorScheme(
        //primary              = colorPrimario.copy(alpha = 0.8f),
        primary              = colorPrimario,
        onPrimary            = Color.White,
        primaryContainer     = colorPrimario.copy(alpha = 0.2f),
        onPrimaryContainer   = Color.White,
        secondary            = colorSecundario,
        onSecondary          = Color.White,
        tertiary             = colorTerciario,
        onTertiary           = Color.White,
        background           = Color(0xFF1C1B1F),
        onBackground         = Color(0xFFE6E1E5),
        surface              = Color(0xFF1C1B1F),
        onSurface            = Color(0xFFE6E1E5),
        surfaceVariant       = Color(0xFF2A2830),//Color(0xFF49454F),
        onSurfaceVariant     = Color(0xFFCAC4D0),
        outline              = Color(0xFF938F99)
    )

    MaterialTheme(
        colorScheme = if (darkTheme) darkColors else lightColors,
        typography  = Typography,
        content     = content
    )
}