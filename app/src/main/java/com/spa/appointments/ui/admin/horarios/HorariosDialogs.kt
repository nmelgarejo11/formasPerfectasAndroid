package com.spa.appointments.ui.admin.horarios

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

// ─── Selector de hora ─────────────────────────────────────

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
        initialHour   = partes[0],
        initialMinute = partes[1],
        is24Hour      = true
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(titulo) },
        text  = { TimePicker(state = state) },
        confirmButton = {
            Button(onClick = {
                val hora = "%02d:%02d".format(state.hour, state.minute)
                onConfirm(hora)
            }) { Text("Aceptar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

// ─── Copiar horario ───────────────────────────────────────

@Composable
fun CopiarHorarioDialog(
    idProfesionalDestino: Int,
    guardando: Boolean,
    onCopiar: (idOrigen: Int) -> Unit,
    onDismiss: () -> Unit
) {
    var idOrigenTexto by remember { mutableStateOf("") }
    val idOrigen = idOrigenTexto.toIntOrNull()
    val esValido = idOrigen != null && idOrigen != idProfesionalDestino

    AlertDialog(
        onDismissRequest = { if (!guardando) onDismiss() },
        title = { Text("Copiar horario") },
        text  = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "Ingresa el ID del profesional cuyo horario quieres copiar.",
                    style = MaterialTheme.typography.bodySmall
                )
                OutlinedTextField(
                    value = idOrigenTexto,
                    onValueChange = { idOrigenTexto = it },
                    label = { Text("ID profesional origen") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                if (idOrigen == idProfesionalDestino) {
                    Text(
                        "No puedes copiar el horario del mismo profesional.",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onCopiar(idOrigen!!) },
                enabled = esValido && !guardando
            ) {
                if (guardando) CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                else Text("Copiar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !guardando) { Text("Cancelar") }
        }
    )
}

// ─── Nuevo bloqueo ────────────────────────────────────────

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
        title = { Text("Nuevo bloqueo") },
        text  = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = motivo,
                    onValueChange = { motivo = it },
                    label = { Text("Motivo *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text("Ej: Vacaciones, Incapacidad...") }
                )

                // Fecha inicio
                OutlinedButton(
                    onClick = { showPickerInicio = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (fechaInicio.isBlank()) "Fecha inicio *" else "Inicio: $fechaInicio")
                }

                // Fecha fin
                OutlinedButton(
                    onClick = { showPickerFin = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (fechaFin.isBlank()) "Fecha fin *" else "Fin: $fechaFin")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onGuardar(fechaInicio, fechaFin, motivo.trim()) },
                enabled = esValido && !guardando
            ) {
                if (guardando) CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                else Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !guardando) { Text("Cancelar") }
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

// ─── Utilidad ─────────────────────────────────────────────

private fun millisToFecha(millis: Long): String {
    val calendar = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC"))
    calendar.timeInMillis = millis
    return "%04d-%02d-%02d".format(
        calendar.get(java.util.Calendar.YEAR),
        calendar.get(java.util.Calendar.MONTH) + 1,
        calendar.get(java.util.Calendar.DAY_OF_MONTH)
    )
}