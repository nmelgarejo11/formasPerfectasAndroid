// Ruta: app/src/main/java/com/spa/appointments/ui/admin/horarios/HorariosDialogs.kt
package com.spa.appointments.ui.admin.horarios

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CopyAll
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.spa.appointments.domain.model.ProfesionalAdmin

// ─── Selector de Hora M3 ──────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HoraPickerDialog(
    horaActual: String,
    titulo: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val partes = horaActual.split(":").map { it.toIntOrNull() ?: 0 }
    val state  = rememberTimePickerState(
        initialHour   = partes.getOrNull(0) ?: 8,
        initialMinute = partes.getOrNull(1) ?: 0,
        is24Hour      = true
    )

    BasicAlertDialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text       = titulo,
                    fontWeight = FontWeight.Bold,
                    style      = MaterialTheme.typography.titleMedium,
                    modifier   = Modifier.padding(bottom = 16.dp)
                )

                TimePicker(state = state)

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cancelar")
                    }

                    Button(
                        onClick = {
                            val hora = "%02d:%02d".format(state.hour, state.minute)
                            onConfirm(hora)
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Aceptar", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ─── Copiar Horario M3 ────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CopiarHorarioDialog(
    idProfesionalDestino: Int,
    guardando: Boolean,
    profesionales: List<ProfesionalAdmin>,
    onCopiar: (idOrigen: Int) -> Unit,
    onDismiss: () -> Unit
) {
    var seleccionado by remember { mutableStateOf<ProfesionalAdmin?>(null) }
    var busqueda     by remember { mutableStateOf("") }

    val filtrados = remember(profesionales, busqueda) {
        profesionales
            .filter { it.estado && it.id != idProfesionalDestino }
            .filter {
                busqueda.isBlank() ||
                        it.nombre.contains(busqueda, ignoreCase = true) ||
                        it.apellido.contains(busqueda, ignoreCase = true)
            }
    }

    AlertDialog(
        onDismissRequest = { if (!guardando) onDismiss() },
        shape            = RoundedCornerShape(28.dp),
        containerColor   = MaterialTheme.colorScheme.surfaceContainerHigh,
        icon = {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Icon(
                    imageVector        = Icons.Default.CopyAll,
                    contentDescription = null,
                    tint               = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier           = Modifier.padding(12.dp).size(26.dp)
                )
            }
        },
        title = {
            Text(
                "Copiar horario de...",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {

                // Buscador M3
                OutlinedTextField(
                    value         = busqueda,
                    onValueChange = { busqueda = it },
                    label         = { Text("Buscar profesional") },
                    leadingIcon   = { Icon(Icons.Default.Search, null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                    trailingIcon  = {
                        if (busqueda.isNotBlank()) {
                            IconButton(onClick = { busqueda = "" }) {
                                Icon(Icons.Default.Clear, null)
                            }
                        }
                    },
                    modifier   = Modifier.fillMaxWidth(),
                    shape      = RoundedCornerShape(14.dp),
                    colors     = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor   = MaterialTheme.colorScheme.surfaceContainerLow,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    ),
                    singleLine = true
                )

                // Lista de profesionales M3
                if (filtrados.isEmpty()) {
                    Text(
                        "Sin profesionales disponibles para copiar",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 260.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filtrados, key = { it.id }) { prof ->
                            val estaSeleccionado = seleccionado?.id == prof.id
                            OutlinedCard(
                                onClick  = { seleccionado = prof },
                                modifier = Modifier.fillMaxWidth(),
                                shape    = RoundedCornerShape(14.dp),
                                colors   = CardDefaults.outlinedCardColors(
                                    containerColor = if (estaSeleccionado)
                                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                                    else
                                        MaterialTheme.colorScheme.surfaceContainerLow
                                ),
                                border = BorderStroke(
                                    1.dp,
                                    if (estaSeleccionado) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = estaSeleccionado,
                                        onClick  = { seleccionado = prof }
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            "${prof.nombre} ${prof.apellido}",
                                            style      = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color      = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            prof.nombreCargo,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick  = { onCopiar(seleccionado!!.id) },
                enabled  = seleccionado != null && !guardando,
                shape    = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (guardando) {
                    CircularProgressIndicator(
                        modifier    = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color       = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Copiar horario", fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick  = onDismiss,
                enabled  = !guardando,
                shape    = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) { Text("Cancelar") }
        }
    )
}

// ─── Nuevo Bloqueo M3 ─────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BloqueoDialog(
    guardando: Boolean,
    onGuardar: (fechaInicio: String, fechaFin: String, motivo: String) -> Unit,
    onDismiss: () -> Unit
) {
    var motivo       by remember { mutableStateOf("") }
    var fechaInicio  by remember { mutableStateOf("") }
    var fechaFin     by remember { mutableStateOf("") }
    var showPickerInicio by remember { mutableStateOf(false) }
    var showPickerFin    by remember { mutableStateOf(false) }

    val estadoInicio = rememberDatePickerState()
    val estadoFin    = rememberDatePickerState()

    val esValido = motivo.isNotBlank() && fechaInicio.isNotBlank() && fechaFin.isNotBlank()

    AlertDialog(
        onDismissRequest = { if (!guardando) onDismiss() },
        shape            = RoundedCornerShape(28.dp),
        containerColor   = MaterialTheme.colorScheme.surfaceContainerHigh,
        icon = {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.errorContainer
            ) {
                Icon(
                    imageVector        = Icons.Default.EventBusy,
                    contentDescription = null,
                    tint               = MaterialTheme.colorScheme.onErrorContainer,
                    modifier           = Modifier.padding(12.dp).size(26.dp)
                )
            }
        },
        title = { Text("Nuevo Bloqueo", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) },
        text  = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value         = motivo,
                    onValueChange = { motivo = it },
                    label         = { Text("Motivo *") },
                    modifier      = Modifier.fillMaxWidth(),
                    singleLine    = true,
                    placeholder   = { Text("Ej: Vacaciones, Incapacidad...") },
                    shape         = RoundedCornerShape(14.dp),
                    colors        = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor   = MaterialTheme.colorScheme.surfaceContainerLow,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    )
                )

                // Fecha inicio M3
                OutlinedButton(
                    onClick  = { showPickerInicio = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape    = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Outlined.CalendarMonth, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = if (fechaInicio.isBlank()) "Fecha de inicio *" else "Inicio: $fechaInicio",
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Fecha fin M3
                OutlinedButton(
                    onClick  = { showPickerFin = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape    = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Outlined.CalendarMonth, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = if (fechaFin.isBlank()) "Fecha de fin *" else "Fin: $fechaFin",
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick  = { onGuardar(fechaInicio, fechaFin, motivo.trim()) },
                enabled  = esValido && !guardando,
                shape    = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (guardando) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                } else {
                    Text("Guardar bloqueo", fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick  = onDismiss,
                enabled  = !guardando,
                shape    = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) { Text("Cancelar") }
        }
    )

    // Date picker inicio
    if (showPickerInicio) {
        DatePickerDialog(
            onDismissRequest = { showPickerInicio = false },
            confirmButton = {
                Button(onClick = {
                    estadoInicio.selectedDateMillis?.let {
                        fechaInicio = millisToFecha(it)
                    }
                    showPickerInicio = false
                }) { Text("Aceptar") }
            },
            dismissButton = {
                TextButton(onClick = { showPickerInicio = false }) { Text("Cancelar") }
            }
        ) {
            DatePicker(state = estadoInicio)
        }
    }

    // Date picker fin
    if (showPickerFin) {
        DatePickerDialog(
            onDismissRequest = { showPickerFin = false },
            confirmButton = {
                Button(onClick = {
                    estadoFin.selectedDateMillis?.let {
                        fechaFin = millisToFecha(it)
                    }
                    showPickerFin = false
                }) { Text("Aceptar") }
            },
            dismissButton = {
                TextButton(onClick = { showPickerFin = false }) { Text("Cancelar") }
            }
        ) {
            DatePicker(state = estadoFin)
        }
    }
}

// ─── Utilidad ─────────────────────────────────────────────────────────────────

private fun millisToFecha(millis: Long): String {
    val calendar = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC"))
    calendar.timeInMillis = millis
    return "%04d-%02d-%02d".format(
        calendar.get(java.util.Calendar.YEAR),
        calendar.get(java.util.Calendar.MONTH) + 1,
        calendar.get(java.util.Calendar.DAY_OF_MONTH)
    )
}
