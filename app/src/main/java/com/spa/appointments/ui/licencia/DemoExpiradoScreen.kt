package com.spa.appointments.ui.licencia

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun DemoExpiradoScreen(
    onContactar: () -> Unit,
    onCerrarSesion: () -> Unit
) {
    Box(
        modifier         = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier            = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Ícono de candado
            Surface(
                modifier = Modifier.size(96.dp),
                shape    = MaterialTheme.shapes.extraLarge,
                color    = MaterialTheme.colorScheme.errorContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector        = Icons.Default.Lock,
                        contentDescription = null,
                        modifier           = Modifier.size(48.dp),
                        tint               = MaterialTheme.colorScheme.error
                    )
                }
            }

            Text(
                text       = "Período de prueba expirado",
                style      = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign  = TextAlign.Center
            )

            Text(
                text      = "Tu período de prueba ha finalizado. " +
                        "Adquiere una licencia para continuar " +
                        "usando todas las funcionalidades.",
                style     = MaterialTheme.typography.bodyMedium,
                color     = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(8.dp))

            // Botón principal — contactar/pagar
            Button(
                onClick  = onContactar,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Adquirir licencia completa")
            }

            // Botón secundario — cerrar sesión
            OutlinedButton(
                onClick  = onCerrarSesion,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cerrar sesión")
            }
        }
    }
}