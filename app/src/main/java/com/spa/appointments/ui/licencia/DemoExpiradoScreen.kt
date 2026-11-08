// Ruta: app/src/main/java/com/spa/appointments/ui/licencia/DemoExpiradoScreen.kt
package com.spa.appointments.ui.licencia

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.spa.appointments.domain.model.ContactoSoporte

// ─── Screen Principal ─────────────────────────────────────────────────────────

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
        val errorColor = MaterialTheme.colorScheme.error

        OutlinedCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            shape  = RoundedCornerShape(28.dp),
            colors = CardDefaults.outlinedCardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
        ) {
            Column(
                modifier            = Modifier
                    .padding(32.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // ── Ícono central con Canvas Glow Rings ───────────────────────
                Box(
                    modifier         = Modifier.size(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawCircle(
                            color = errorColor.copy(alpha = 0.2f),
                            style = Stroke(width = 2.5.dp.toPx())
                        )
                    }
                    Surface(
                        modifier = Modifier.size(86.dp),
                        shape    = CircleShape,
                        color    = MaterialTheme.colorScheme.errorContainer
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector        = Icons.Default.Lock,
                                contentDescription = null,
                                modifier           = Modifier.size(42.dp),
                                tint               = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }

                // ── Textos ────────────────────────────────────────────────────
                Text(
                    text       = "Período de prueba expirado",
                    style      = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign  = TextAlign.Center,
                    color      = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text      = "Tu período de prueba ha finalizado. " +
                            "Adquiere una licencia para continuar " +
                            "usando todas las funcionalidades del sistema.",
                    style     = MaterialTheme.typography.bodyMedium,
                    color     = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(4.dp))

                // ── Botón Principal ───────────────────────────────────────────
                Button(
                    onClick  = { mostrarContacto = true },
                    enabled  = !cargando,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape    = RoundedCornerShape(14.dp)
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
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text       = "Adquirir licencia completa",
                            fontWeight = FontWeight.Bold,
                            style      = MaterialTheme.typography.titleSmall
                        )
                    }
                }

                // ── Botón Cerrar Sesión ───────────────────────────────────────
                OutlinedButton(
                    onClick  = onCerrarSesion,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape    = RoundedCornerShape(14.dp),
                    colors   = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f))
                ) {
                    Icon(
                        Icons.Default.ExitToApp, null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Cerrar sesión", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ─── Diálogo de Contacto M3 ───────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ContactoDialog(
    contacto:   ContactoSoporte,
    onDismiss:  () -> Unit,
    onLlamar:   () -> Unit,
    onEmail:    () -> Unit,
    onWhatsapp: () -> Unit
) {
    BasicAlertDialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            tonalElevation = 6.dp
        ) {
            Column(modifier = Modifier.padding(24.dp)) {

                // Header M3
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier            = Modifier.fillMaxWidth()
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.padding(bottom = 12.dp)
                    ) {
                        Icon(
                            imageVector        = Icons.Default.SupportAgent,
                            contentDescription = null,
                            tint               = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier           = Modifier.padding(12.dp).size(28.dp)
                        )
                    }
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

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                Spacer(modifier = Modifier.height(14.dp))

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {

                    contacto.mensaje?.let { msg ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f)
                        ) {
                            Text(
                                text      = msg,
                                style     = MaterialTheme.typography.bodySmall,
                                color     = MaterialTheme.colorScheme.onSecondaryContainer,
                                textAlign = TextAlign.Center,
                                modifier  = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp)
                            )
                        }
                    }

                    contacto.celular?.let {
                        ContactoBoton(
                            icono       = Icons.Default.Phone,
                            texto       = it,
                            etiqueta    = "Llamar",
                            colorIcono  = MaterialTheme.colorScheme.onPrimaryContainer,
                            colorFondo  = MaterialTheme.colorScheme.primaryContainer,
                            onClick     = onLlamar
                        )
                    }

                    contacto.whatsapp?.let {
                        ContactoBoton(
                            icono       = Icons.AutoMirrored.Filled.Chat,
                            texto       = it,
                            etiqueta    = "WhatsApp",
                            colorIcono  = MaterialTheme.colorScheme.onTertiaryContainer,
                            colorFondo  = MaterialTheme.colorScheme.tertiaryContainer,
                            onClick     = onWhatsapp
                        )
                    }

                    contacto.email?.let {
                        ContactoBoton(
                            icono       = Icons.Default.Email,
                            texto       = it,
                            etiqueta    = "Email",
                            colorIcono  = MaterialTheme.colorScheme.onSecondaryContainer,
                            colorFondo  = MaterialTheme.colorScheme.secondaryContainer,
                            onClick     = onEmail
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                Spacer(modifier = Modifier.height(14.dp))

                OutlinedButton(
                    onClick  = onDismiss,
                    shape    = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Text("Cerrar", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ─── Botón de Canal de Contacto M3 ────────────────────────────────────────────

@Composable
private fun ContactoBoton(
    icono:      androidx.compose.ui.graphics.vector.ImageVector,
    texto:      String,
    etiqueta:   String,
    colorIcono: Color,
    colorFondo: Color,
    onClick:    () -> Unit
) {
    OutlinedCard(
        onClick  = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape    = RoundedCornerShape(14.dp),
        colors   = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        border   = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier          = Modifier.fillMaxSize().padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = colorFondo
            ) {
                Icon(
                    imageVector        = icono,
                    contentDescription = null,
                    modifier           = Modifier.padding(6.dp).size(18.dp),
                    tint               = colorIcono
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(
                modifier            = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text  = etiqueta,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text       = texto,
                    style      = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color      = MaterialTheme.colorScheme.onSurface
                )
            }
            Icon(
                Icons.AutoMirrored.Filled.ArrowForward, null,
                modifier = Modifier.size(18.dp),
                tint     = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
