package com.spa.appointments.ui.licencia

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.spa.appointments.domain.model.ContactoSoporte

@Composable
fun DemoExpiradoScreen(
    onCerrarSesion: () -> Unit,
    vm: DemoExpiradoViewModel = hiltViewModel()
) {
    val contacto       by vm.contacto.collectAsState()
    val cargando       by vm.cargando.collectAsState()
    var mostrarContacto by remember { mutableStateOf(false) }

    val context = LocalContext.current

    // Popup de contacto
    if (mostrarContacto && contacto != null) {
        ContactoDialog(
            contacto  = contacto!!,
            onDismiss = { mostrarContacto = false },
            onLlamar  = {
                val intent = Intent(Intent.ACTION_DIAL).apply {
                    data = Uri.parse("tel:${contacto!!.celular}")
                }
                context.startActivity(intent)
            },
            onEmail = {
                val intent = Intent(Intent.ACTION_SENDTO).apply {
                    data    = Uri.parse("mailto:${contacto!!.email}")
                    putExtra(Intent.EXTRA_SUBJECT, "Solicitud de licencia - Gestión de Servicios")
                }
                context.startActivity(intent)
            },
            onWhatsapp = {
                val numero  = contacto!!.whatsapp?.replace(Regex("[^0-9]"), "") ?: ""
                val mensaje = "Hola, me interesa adquirir la licencia completa del sistema de gestión de servicios."
                val intent  = Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse("https://wa.me/$numero?text=${Uri.encode(mensaje)}")
                }
                context.startActivity(intent)
            }
        )
    }

    Box(
        modifier         = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier            = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Ícono
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

            // Botón principal
            Button(
                onClick  = { mostrarContacto = true },
                enabled  = !cargando,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (cargando) {
                    CircularProgressIndicator(
                        modifier    = Modifier.size(20.dp),
                        color       = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        Icons.Default.ContactPhone,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Adquirir licencia completa")
                }
            }

            // Botón cerrar sesión
            OutlinedButton(
                onClick  = onCerrarSesion,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cerrar sesión")
            }
        }
    }
}

// ── Popup de contacto ─────────────────────────────────────────────────────────
@Composable
private fun ContactoDialog(
    contacto:   ContactoSoporte,
    onDismiss:  () -> Unit,
    onLlamar:   () -> Unit,
    onEmail:    () -> Unit,
    onWhatsapp: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector        = Icons.Default.SupportAgent,
                contentDescription = null,
                tint               = MaterialTheme.colorScheme.primary,
                modifier           = Modifier.size(40.dp)
            )
        },
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text       = contacto.nombre,
                    style      = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign  = TextAlign.Center
                )
                contacto.cargo?.let {
                    Text(
                        text  = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

                // Mensaje personalizado
                contacto.mensaje?.let {
                    Text(
                        text      = it,
                        style     = MaterialTheme.typography.bodySmall,
                        color     = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }

                HorizontalDivider()

                // Botones de contacto
                contacto.celular?.let {
                    ContactoBoton(
                        icono    = Icons.Default.Phone,
                        texto    = it,
                        etiqueta = "Llamar",
                        onClick  = onLlamar
                    )
                }

                contacto.whatsapp?.let {
                    ContactoBoton(
                        icono    = Icons.Default.Chat,
                        texto    = it,
                        etiqueta = "WhatsApp",
                        onClick  = onWhatsapp
                    )
                }

                contacto.email?.let {
                    ContactoBoton(
                        icono    = Icons.Default.Email,
                        texto    = it,
                        etiqueta = "Email",
                        onClick  = onEmail
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cerrar")
            }
        }
    )
}

@Composable
private fun ContactoBoton(
    icono:    androidx.compose.ui.graphics.vector.ImageVector,
    texto:    String,
    etiqueta: String,
    onClick:  () -> Unit
) {
    OutlinedButton(
        onClick  = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector        = icono,
            contentDescription = null,
            modifier           = Modifier.size(18.dp)
        )
        Spacer(Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text  = etiqueta,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text  = texto,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}