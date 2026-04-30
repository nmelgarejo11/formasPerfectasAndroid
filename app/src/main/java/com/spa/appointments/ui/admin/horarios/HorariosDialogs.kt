package com.spa.appointments.ui.admin.horarios

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import com.spa.appointments.domain.model.ProfesionalAdmin
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Clear

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
    profesionales: List<com.spa.appointments.domain.model.ProfesionalAdmin>,  // ← Recibe la lista
    onCopiar: (idOrigen: Int) -> Unit,
    onDismiss: () -> Unit
) {
    var seleccionado by remember { mutableStateOf<com.spa.appointments.domain.model.ProfesionalAdmin?>(null) }
    var busqueda     by remember { mutableStateOf("") }

    val filtrados = remember(profesionales, busqueda) {
        profesionales
            .filter { it.estado && it.id != idProfesionalDestino }  // ← Excluye el destino
            .filter {
                busqueda.isBlank() ||
                        it.nombre.contains(busqueda, ignoreCase = true) ||
                        it.apellido.contains(busqueda, ignoreCase = true)
            }
    }

    AlertDialog(
        onDismissRequest = { if (!guardando) onDismiss() },
        title = { Text("Copiar horario de...") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {

                // Buscador
                OutlinedTextField(
                    value         = busqueda,
                    onValueChange = { busqueda = it },
                    label         = { Text("Buscar profesional") },
                    leadingIcon   = { Icon(androidx.compose.material.icons.Icons.Default.Search, null) },
                    trailingIcon  = {
                        if (busqueda.isNotBlank()) {
                            IconButton(onClick = { busqueda = "" }) {
                                Icon(androidx.compose.material.icons.Icons.Default.Clear, null)
                            }
                        }
                    },
                    modifier   = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Lista de profesionales
                if (filtrados.isEmpty()) {
                    Text(
                        "Sin profesionales disponibles",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                } else {
                    androidx.compose.foundation.lazy.LazyColumn(
                        modifier = Modifier.heightIn(max = 300.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(filtrados, key = { it.id }) { prof: ProfesionalAdmin ->
                            val estaSeleccionado = seleccionado?.id == prof.id
                            Card(
                                onClick = { seleccionado = prof },
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (estaSeleccionado)
                                        MaterialTheme.colorScheme.primaryContainer
                                    else
                                        MaterialTheme.colorScheme.surface
                                ),
                                elevation = CardDefaults.cardElevation(if (estaSeleccionado) 4.dp else 1.dp)
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
                                            fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
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
                enabled  = seleccionado != null && !guardando
            ) {
                if (guardando) CircularProgressIndicator(
                    modifier    = Modifier.size(16.dp),
                    strokeWidth = 2.dp
                )
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