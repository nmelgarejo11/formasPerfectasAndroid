// Ruta: app/src/main/java/com/spa/appointments/ui/admin/horarios/HorariosDialogs.kt
package com.spa.appointments.ui.admin.horarios

import android.app.DatePickerDialog
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
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.spa.appointments.domain.model.ProfesionalAdmin
import java.util.Calendar

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
        is24Hour      = false
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
    val context = LocalContext.current
    var motivo           by remember { mutableStateOf("") }
    var fechaInicioDate  by remember { mutableStateOf("") }
    var horaInicioTime   by remember { mutableStateOf("08:00") }
    var fechaFinDate     by remember { mutableStateOf("") }
    var horaFinTime      by remember { mutableStateOf("18:00") }

    var showPickerInicio     by remember { mutableStateOf(false) }
    var showPickerFin        by remember { mutableStateOf(false) }
    var showTimePickerInicio by remember { mutableStateOf(false) }
    var showTimePickerFin    by remember { mutableStateOf(false) }

    val esValido = motivo.isNotBlank() && fechaInicioDate.isNotBlank() && fechaFinDate.isNotBlank()

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
        title = { Text("Nuevo Bloqueo / Ausencia", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) },
        text  = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value         = motivo,
                    onValueChange = { motivo = it },
                    label         = { Text("Motivo *") },
                    modifier      = Modifier.fillMaxWidth(),
                    singleLine    = true,
                    placeholder   = { Text("Ej: Vacaciones, Cita médica...") },
                    shape         = RoundedCornerShape(14.dp),
                    colors        = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor   = MaterialTheme.colorScheme.surfaceContainerLow,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    )
                )

                Text(
                    text       = "Inicio del bloqueo",
                    style      = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color      = MaterialTheme.colorScheme.primary
                )

                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick  = { showPickerInicio = true },
                        modifier = Modifier.weight(1.2f),
                        shape    = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Outlined.CalendarMonth, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text       = if (fechaInicioDate.isBlank()) "Fecha *" else fechaInicioDate,
                            style      = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    OutlinedButton(
                        onClick  = { showTimePickerInicio = true },
                        modifier = Modifier.weight(1f),
                        shape    = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Default.Schedule, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text       = formatearHora12hSimple(horaInicioTime),
                            style      = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Text(
                    text       = "Fin del bloqueo",
                    style      = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color      = MaterialTheme.colorScheme.primary
                )

                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick  = { showPickerFin = true },
                        modifier = Modifier.weight(1.2f),
                        shape    = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Outlined.CalendarMonth, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text       = if (fechaFinDate.isBlank()) "Fecha *" else fechaFinDate,
                            style      = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    OutlinedButton(
                        onClick  = { showTimePickerFin = true },
                        modifier = Modifier.weight(1f),
                        shape    = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Default.Schedule, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text       = formatearHora12hSimple(horaFinTime),
                            style      = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick  = {
                    val fullInicio = "${fechaInicioDate}T${horaInicioTime}:00"
                    val fullFin    = "${fechaFinDate}T${horaFinTime}:00"
                    onGuardar(fullInicio, fullFin, motivo.trim())
                },
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

    // Date picker nativo Android inicio
    if (showPickerInicio) {
        val cal = Calendar.getInstance()
        if (fechaInicioDate.isNotBlank() && fechaInicioDate.contains("-")) {
            try {
                val partes = fechaInicioDate.split("-")
                cal.set(partes[0].toInt(), partes[1].toInt() - 1, partes[2].toInt())
            } catch (_: Exception) {}
        }
        val dpd = DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                fechaInicioDate = "%04d-%02d-%02d".format(year, month + 1, dayOfMonth)
                if (fechaFinDate.isBlank()) {
                    fechaFinDate = fechaInicioDate
                }
                showPickerInicio = false
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        )
        dpd.setOnCancelListener { showPickerInicio = false }
        dpd.setOnDismissListener { showPickerInicio = false }
        LaunchedEffect(Unit) {
            dpd.show()
        }
    }

    // Time picker inicio
    if (showTimePickerInicio) {
        HoraPickerDialog(
            horaActual = horaInicioTime,
            titulo     = "Hora de Inicio",
            onConfirm  = {
                horaInicioTime = it
                showTimePickerInicio = false
            },
            onDismiss  = { showTimePickerInicio = false }
        )
    }

    // Date picker nativo Android fin
    if (showPickerFin) {
        val cal = Calendar.getInstance()
        if (fechaFinDate.isNotBlank() && fechaFinDate.contains("-")) {
            try {
                val partes = fechaFinDate.split("-")
                cal.set(partes[0].toInt(), partes[1].toInt() - 1, partes[2].toInt())
            } catch (_: Exception) {}
        }
        val dpd = DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                fechaFinDate = "%04d-%02d-%02d".format(year, month + 1, dayOfMonth)
                showPickerFin = false
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        )
        dpd.setOnCancelListener { showPickerFin = false }
        dpd.setOnDismissListener { showPickerFin = false }
        LaunchedEffect(Unit) {
            dpd.show()
        }
    }

    // Time picker fin
    if (showTimePickerFin) {
        HoraPickerDialog(
            horaActual = horaFinTime,
            titulo     = "Hora de Fin",
            onConfirm  = {
                horaFinTime = it
                showTimePickerFin = false
            },
            onDismiss  = { showTimePickerFin = false }
        )
    }
}

// ─── Utilidad ─────────────────────────────────────────────────────────────────

private fun formatearHora12hSimple(horaStr: String): String {
    if (horaStr.isBlank()) return ""
    return try {
        val cleanStr = if (horaStr.contains("T")) horaStr.substring(11, 16) else horaStr.trim()
        val partes = cleanStr.split(":")
        val h = partes[0].toInt()
        val m = partes[1]
        val amPm = if (h >= 12) "PM" else "AM"
        val h12 = when {
            h == 0 -> 12
            h > 12 -> h - 12
            else -> h
        }
        String.format(java.util.Locale.US, "%02d:%s %s", h12, m, amPm)
    } catch (_: Exception) {
        horaStr.take(5)
    }
}
