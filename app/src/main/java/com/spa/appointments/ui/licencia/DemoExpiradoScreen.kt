package com.spa.appointments.ui.licencia

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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

// ─── Screen principal ─────────────────────────────────────────────────────────

@Composable
fun DemoExpiradoScreen(
    onCerrarSesion: () -> Unit,
    vm: DemoExpiradoViewModel = hiltViewModel()
) {
    val contacto        by vm.contacto.collectAsState()
    val cargando        by vm.cargando.collectAsState()
    var mostrarContacto by remember { mutableStateOf(false) }
    val context         = LocalContext.current

    if (mostrarContacto && contacto != null) {
        ContactoDialog(
            contacto  = contacto!!,
            onDismiss = { mostrarContacto = false },
            onLlamar  = {
                context.startActivity(
                    Intent(Intent.ACTION_DIAL).apply {
                        data = Uri.parse("tel:${contacto!!.celular}")
                    }
                )
            },
            onEmail = {
                context.startActivity(
                    Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("mailto:${contacto!!.email}")
                        putExtra(Intent.EXTRA_SUBJECT, "Solicitud de licencia - Gestión de Servicios")
                    }
                )
            },
            onWhatsapp = {
                val numero  = contacto!!.whatsapp?.replace(Regex("[^0-9]"), "") ?: ""
                val mensaje = "Hola, me interesa adquirir la licencia completa del sistema de gestión de servicios."
                context.startActivity(
                    Intent(Intent.ACTION_VIEW).apply {
                        data = Uri.parse("https://wa.me/$numero?text=${Uri.encode(mensaje)}")
                    }
                )
            }
        )
    }

    Box(
        modifier         = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier            = Modifier
                .padding(32.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── Ícono central ─────────────────────────────────────────────
            Surface(
                modifier = Modifier.size(96.dp),
                shape    = RoundedCornerShape(28.dp),
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

            // ── Textos ────────────────────────────────────────────────────
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

            Spacer(Modifier.height(4.dp))

            // ── Botón principal ───────────────────────────────────────────
            Button(
                onClick  = { mostrarContacto = true },
                enabled  = !cargando,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape    = RoundedCornerShape(12.dp)
            ) {
                if (cargando) {
                    CircularProgressIndicator(
                        modifier    = Modifier.size(20.dp),
                        color       = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        Icons.Default.ContactPhone, null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text       = "Adquirir licencia completa",
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // ── Botón cerrar sesión ───────────────────────────────────────
            OutlinedButton(
                onClick  = onCerrarSesion,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape    = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    Icons.Default.ExitToApp, null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text("Cerrar sesión")
            }
        }
    }
}

// ─── Diálogo de contacto ──────────────────────────────────────────────────────

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
        shape = RoundedCornerShape(16.dp),
        icon  = {
            Surface(
                shape = RoundedCornerShape(50),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Icon(
                    imageVector        = Icons.Default.SupportAgent,
                    contentDescription = null,
                    tint               = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier           = Modifier.padding(12.dp).size(28.dp)
                )
            }
        },
        title = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier            = Modifier.fillMaxWidth()
            ) {
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
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {

                // Mensaje personalizado en Surface
                contacto.mensaje?.let { msg ->
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Text(
                            text      = msg,
                            style     = MaterialTheme.typography.bodySmall,
                            color     = MaterialTheme.colorScheme.onSecondaryContainer,
                            textAlign = TextAlign.Center,
                            modifier  = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }
                }

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )

                contacto.celular?.let {
                    ContactoBoton(
                        icono       = Icons.Default.Phone,
                        texto       = it,
                        etiqueta    = "Llamar",
                        colorIcono  = MaterialTheme.colorScheme.primary,
                        colorFondo  = MaterialTheme.colorScheme.primaryContainer,
                        onClick     = onLlamar
                    )
                }

                contacto.whatsapp?.let {
                    ContactoBoton(
                        icono       = Icons.Default.Chat,
                        texto       = it,
                        etiqueta    = "WhatsApp",
                        colorIcono  = MaterialTheme.colorScheme.tertiary,
                        colorFondo  = MaterialTheme.colorScheme.tertiaryContainer,
                        onClick     = onWhatsapp
                    )
                }

                contacto.email?.let {
                    ContactoBoton(
                        icono       = Icons.Default.Email,
                        texto       = it,
                        etiqueta    = "Email",
                        colorIcono  = MaterialTheme.colorScheme.secondary,
                        colorFondo  = MaterialTheme.colorScheme.secondaryContainer,
                        onClick     = onEmail
                    )
                }
            }
        },
        confirmButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape   = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) { Text("Cerrar") }
        }
    )
}

// ─── Botón de canal de contacto ───────────────────────────────────────────────

@Composable
private fun ContactoBoton(
    icono:      androidx.compose.ui.graphics.vector.ImageVector,
    texto:      String,
    etiqueta:   String,
    colorIcono: androidx.compose.ui.graphics.Color,
    colorFondo: androidx.compose.ui.graphics.Color,
    onClick:    () -> Unit
) {
    OutlinedButton(
        onClick  = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        shape    = RoundedCornerShape(12.dp),
        contentPadding = PaddingValues(horizontal = 12.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = colorFondo
        ) {
            Icon(
                imageVector        = icono,
                contentDescription = null,
                modifier           = Modifier.padding(6.dp).size(16.dp),
                tint               = colorIcono
            )
        }
        Spacer(Modifier.width(10.dp))
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text  = etiqueta,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text  = texto,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium
            )
        }
        Icon(
            Icons.Default.ChevronRight, null,
            modifier = Modifier.size(16.dp),
            tint     = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}