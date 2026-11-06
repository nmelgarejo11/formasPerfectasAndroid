package com.spa.appointments.ui.citas.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spa.appointments.domain.model.Cita
import com.spa.appointments.domain.model.Profesional
import com.spa.appointments.domain.model.SlotDisponible
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReagendarCitaDirectoDialog(
    cita: Cita,
    profesionales: List<Profesional>,
    cargandoSlots: Boolean,
    slotsDisponibles: List<SlotDisponible>,
    onCargarDisponibilidad: (idProfesional: Int, idSede: Int, fecha: String, duracion: Int) -> Unit,
    onConfirmarReagendamiento: (idCita: Int, idProfesional: Int, fechaHoraInicio: String, fechaHoraFin: String) -> Unit,
    onDismiss: () -> Unit
) {
    var profesionalSeleccionado by remember(cita, profesionales) {
        mutableStateOf(profesionales.firstOrNull { it.id == cita.idProfesional } ?: profesionales.firstOrNull())
    }
    var expandedProf by remember { mutableStateOf(false) }

    var fechaSeleccionada by remember { mutableStateOf(LocalDate.now()) }
    var slotSeleccionado by remember { mutableStateOf<SlotDisponible?>(null) }

    val localeEs = Locale("es", "ES")
    val formatterFechaIso = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val formatterFechaDisplay = DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM", localeEs)

    // Cargar slots cuando cambia profesional o fecha
    LaunchedEffect(profesionalSeleccionado, fechaSeleccionada) {
        val prof = profesionalSeleccionado
        if (prof != null) {
            slotSeleccionado = null
            onCargarDisponibilidad(
                prof.id,
                cita.idSede,
                fechaSeleccionada.format(formatterFechaIso),
                60 // duracion estimativa de slot
            )
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        icon = {
            Icon(
                Icons.Default.EditCalendar,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
        },
        title = {
            Text(
                "Reagendar Cita",
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Selecciona el profesional, fecha y nuevo horario disponible.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // 1. Selector de Profesional
                ExposedDropdownMenuBox(
                    expanded = expandedProf,
                    onExpandedChange = { expandedProf = it }
                ) {
                    OutlinedTextField(
                        value = profesionalSeleccionado?.nombreCompleto ?: "Seleccionar profesional",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Profesional") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expandedProf) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    ExposedDropdownMenu(
                        expanded = expandedProf,
                        onDismissRequest = { expandedProf = false }
                    ) {
                        profesionales.forEach { prof ->
                            DropdownMenuItem(
                                text = { Text(prof.nombreCompleto) },
                                onClick = {
                                    profesionalSeleccionado = prof
                                    expandedProf = false
                                }
                            )
                        }
                    }
                }

                // 2. Selector de Fecha (Día Anterior / Siguiente + Display)
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        IconButton(
                            onClick = { fechaSeleccionada = fechaSeleccionada.minusDays(1) },
                            enabled = fechaSeleccionada > LocalDate.now()
                        ) {
                            Text("<", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    Icons.Default.CalendarMonth,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = fechaSeleccionada.format(formatterFechaDisplay).replaceFirstChar { it.uppercase() },
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        IconButton(
                            onClick = { fechaSeleccionada = fechaSeleccionada.plusDays(1) }
                        ) {
                            Text(">", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        }
                    }
                }

                // 3. Grid de Slots de Horario
                Text(
                    text = "Horarios disponibles:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (cargandoSlots) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    }
                } else if (slotsDisponibles.none { it.disponible }) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "No hay horarios disponibles para la fecha y profesional seleccionados.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                } else {
                    val slotsValidos = slotsDisponibles.filter { it.disponible }

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 180.dp)
                    ) {
                        items(slotsValidos) { slot ->
                            val esSeleccionado = slotSeleccionado == slot

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (esSeleccionado) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surfaceContainerHigh,
                                modifier = Modifier
                                    .clickable { slotSeleccionado = slot }
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                ) {
                                    Text(
                                        text = slot.horaInicio,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (esSeleccionado) MaterialTheme.colorScheme.onPrimary
                                        else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val slot = slotSeleccionado
                    val prof = profesionalSeleccionado
                    if (slot != null && prof != null) {
                        val fechaIso = fechaSeleccionada.format(formatterFechaIso)
                        val inicioIso = "${fechaIso}T${slot.horaInicio}:00"
                        val finIso = "${fechaIso}T${slot.horaFin}:00"
                        onConfirmarReagendamiento(cita.id, prof.id, inicioIso, finIso)
                    }
                },
                enabled = slotSeleccionado != null && profesionalSeleccionado != null,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Confirmar Reagendamiento", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cancelar")
            }
        }
    )
}
