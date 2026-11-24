package com.spa.appointments.ui.citas.components

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.spa.appointments.domain.model.Cita
import com.spa.appointments.domain.model.EstadoCita

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AjustarEstadoCitaDialog(
    cita: Cita,
    estadosActivas: List<EstadoCita>,
    guardando: Boolean,
    haySolapamiento: Boolean = false,
    verificandoSolapamiento: Boolean = false,
    onDismiss: () -> Unit,
    onConfirmar: (idEstado: Int, motivo: String) -> Unit
) {
    var estadoSeleccionado by remember {
        mutableStateOf(estadosActivas.firstOrNull())
    }

    LaunchedEffect(estadosActivas) {
        if (estadoSeleccionado == null && estadosActivas.isNotEmpty()) {
            estadoSeleccionado = estadosActivas.first()
        }
    }

    var motivo by remember { mutableStateOf("") }
    var menuExpandido by remember { mutableStateOf(false) }
    var errorMotivo by remember { mutableStateOf(false) }

    val colorEstadoActual = remember(cita.colorEstado) {
        runCatching {
            Color(android.graphics.Color.parseColor(cita.colorEstado ?: "#888888"))
        }.getOrDefault(Color.Gray)
    }

    AlertDialog(
        onDismissRequest = { if (!guardando) onDismiss() },
        shape = RoundedCornerShape(24.dp),
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Ajustar estado de cita",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Seleccione un nuevo estado activo",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // ── Tarjeta Resumen Cita Actual ──────────────────
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Estado actual:",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = colorEstadoActual.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = cita.estado,
                                    color = colorEstadoActual,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }

                        if (!cita.nombreCliente.isNullOrBlank()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = cita.nombreCliente,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = "${cita.profesional} · ${cita.sede}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // ── Selector de Nuevo Estado (Grupo: ACTIVAS) ───
                Column {
                    Text(
                        text = "Nuevo estado (Activas):",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(6.dp))

                    ExposedDropdownMenuBox(
                        expanded = menuExpandido,
                        onExpandedChange = { if (!guardando) menuExpandido = !menuExpandido }
                    ) {
                        OutlinedTextField(
                            value = estadoSeleccionado?.nombre ?: "Seleccionar estado",
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = menuExpandido)
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )

                        ExposedDropdownMenu(
                            expanded = menuExpandido,
                            onDismissRequest = { menuExpandido = false }
                        ) {
                            estadosActivas.forEach { est ->
                                val colorEst = remember(est.color) {
                                    runCatching {
                                        Color(android.graphics.Color.parseColor(est.color))
                                    }.getOrDefault(Color.Gray)
                                }
                                DropdownMenuItem(
                                    text = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(10.dp)
                                                    .background(colorEst, CircleShape)
                                            )
                                            Spacer(Modifier.width(8.dp))
                                            Text(
                                                text = est.nombre,
                                                fontWeight = if (est.id == estadoSeleccionado?.id) FontWeight.Bold else FontWeight.Normal
                                            )
                                        }
                                    },
                                    onClick = {
                                        estadoSeleccionado = est
                                        menuExpandido = false
                                    }
                                )
                            }
                        }
                    }
                }

                // ── Advertencia Informativa de Solapamiento (Alta Legibilidad) ──
                if (haySolapamiento && estadoSeleccionado?.nombre?.contains("Programada", ignoreCase = true) == true) {
                    val esModoOscuro = isSystemInDarkTheme()
                    val warningBg = if (esModoOscuro) Color(0xFF3D2E00) else Color(0xFFFFF8E1)
                    val warningBorder = if (esModoOscuro) Color(0xFF8A6D00) else Color(0xFFFFE082)
                    val warningText = if (esModoOscuro) Color(0xFFFFE082) else Color(0xFF6A4B00)
                    val warningIcon = if (esModoOscuro) Color(0xFFFFC107) else Color(0xFFF57F17)

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = warningBg,
                        border = androidx.compose.foundation.BorderStroke(1.dp, warningBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Aviso de solapamiento",
                                tint = warningIcon,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(10.dp))
                            Text(
                                text = "Aviso: Al cambiar a Programada, esta cita se solapa en horario con otra cita agendada para el profesional.",
                                style = MaterialTheme.typography.bodySmall,
                                color = warningText,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                // ── Campo de Motivo u Observación ─────────────────
                Column {
                    Text(
                        text = "Motivo del ajuste / Observación:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(6.dp))
                    OutlinedTextField(
                        value = motivo,
                        onValueChange = {
                            motivo = it
                            if (it.isNotBlank()) errorMotivo = false
                        },
                        placeholder = {
                            Text(
                                "Ej: Se marcó cancelada por equivocación",
                                style = MaterialTheme.typography.bodySmall
                            )
                        },
                        isError = errorMotivo,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3,
                        enabled = !guardando,
                        supportingText = {
                            if (errorMotivo) {
                                Text("El motivo es requerido para auditoría")
                            }
                        }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (motivo.isBlank()) {
                        errorMotivo = true
                        return@Button
                    }
                    estadoSeleccionado?.let { est ->
                        onConfirmar(est.id, motivo.trim())
                    }
                },
                enabled = !guardando && estadoSeleccionado != null,
                shape = RoundedCornerShape(12.dp)
            ) {
                if (guardando) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Guardando...")
                } else {
                    Icon(
                        imageVector = Icons.Default.EditNote,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text("Guardar cambio")
                }
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = { if (!guardando) onDismiss() },
                enabled = !guardando,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Cancelar")
            }
        }
    )
}
