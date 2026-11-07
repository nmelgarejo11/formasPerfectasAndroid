package com.spa.appointments.ui.citas.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spa.appointments.domain.model.Cita
import com.spa.appointments.domain.model.Profesional
import com.spa.appointments.domain.model.ServicioCita
import com.spa.appointments.domain.model.SlotDisponible
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
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

    val formatterFechaIso = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    val duracionServicio = remember(cita.servicios) {
        extraerDuracionServicio(cita.servicios)
    }

    // Cargar slots cuando cambia profesional o fecha
    LaunchedEffect(profesionalSeleccionado, fechaSeleccionada) {
        val prof = profesionalSeleccionado
        if (prof != null) {
            slotSeleccionado = null
            onCargarDisponibilidad(
                prof.id,
                cita.idSede,
                fechaSeleccionada.format(formatterFechaIso),
                duracionServicio
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
                    text = "Selecciona el profesional habilitado, fecha y nuevo horario disponible.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // 1. Selector de Profesional Habilitado
                ExposedDropdownMenuBox(
                    expanded = expandedProf,
                    onExpandedChange = { expandedProf = it }
                ) {
                    OutlinedTextField(
                        value = profesionalSeleccionado?.nombreCompleto ?: "Seleccionar profesional",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Profesional Habilitado") },
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

                // 2. Selector de Fecha (Carrusel de días idéntico a DisponibilidadScreen)
                Text(
                    text = "Selecciona la fecha:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                val dias = remember { (0..29).map { LocalDate.now().plusDays(it.toLong()) } }
                val today = LocalDate.now()

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(dias) { dia ->
                        val seleccionado = dia == fechaSeleccionada
                        val esHoy = dia == today
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            if (dia.dayOfMonth == 1 || esHoy) {
                                Text(
                                    text = if (esHoy) "HOY" else dia.month.getDisplayName(TextStyle.SHORT, Locale("es")).uppercase(),
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (seleccionado) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                    modifier = Modifier.padding(bottom = 2.dp)
                                )
                            } else {
                                Spacer(Modifier.height(12.dp))
                            }

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (seleccionado) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                                    .then(if (!seleccionado && esHoy) Modifier.border(1.5.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp)) else Modifier)
                                    .clickable { fechaSeleccionada = dia }
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = dia.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale("es")),
                                    fontSize = 10.sp,
                                    color = if (seleccionado) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = dia.dayOfMonth.toString(),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = if (seleccionado) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                )
                            }
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
                            .height(90.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    }
                } else if (slotsDisponibles.none { it.disponible }) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.EventBusy,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "No hay horarios disponibles para esta fecha.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    val slotsValidos = slotsDisponibles.filter { it.disponible }

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 160.dp)
                    ) {
                        items(slotsValidos) { slot ->
                            val esSeleccionado = slotSeleccionado == slot

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (esSeleccionado) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surfaceVariant,
                                border = if (esSeleccionado) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                                modifier = Modifier.clickable { slotSeleccionado = slot }
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.padding(vertical = 10.dp)
                                ) {
                                    Text(
                                        text = formatearHora12h(slot.horaInicio),
                                        style = MaterialTheme.typography.labelSmall,
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
                        val hIni = slot.horaInicio.trim()
                        val hFin = slot.horaFin.trim()
                        val horaIniClean = if (hIni.length == 5) "${hIni}:00" else hIni.take(8)
                        val horaFinClean = if (hFin.length == 5) "${hFin}:00" else hFin.take(8)

                        val inicioIso = "${fechaIso}T${horaIniClean}"
                        val finIso = "${fechaIso}T${horaFinClean}"
                        onConfirmarReagendamiento(cita.id, prof.id, inicioIso, finIso)
                    }
                },
                enabled = slotSeleccionado != null && profesionalSeleccionado != null,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text(
                    text = slotSeleccionado?.let { "Confirmar a las ${formatearHora12h(it.horaInicio)}" } ?: "Confirmar Reagendamiento",
                    fontWeight = FontWeight.Bold
                )
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

private fun formatearHora12h(horaStr: String): String {
    if (horaStr.isBlank()) return ""
    return try {
        val partes = horaStr.trim().split(":")
        val h = partes[0].toInt()
        val m = partes[1]
        val amPm = if (h >= 12) "PM" else "AM"
        val h12 = when {
            h == 0 -> 12
            h > 12 -> h - 12
            else -> h
        }
        String.format(Locale.US, "%02d:%s %s", h12, m, amPm)
    } catch (e: Exception) {
        horaStr.take(5)
    }
}

private fun extraerDuracionServicio(json: String?): Int {
    if (json.isNullOrBlank()) return 60
    return try {
        val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
        val type = Types.newParameterizedType(List::class.java, ServicioCita::class.java)
        val lista = moshi.adapter<List<ServicioCita>>(type).fromJson(json) ?: emptyList()
        lista.firstOrNull()?.duracion ?: 60
    } catch (e: Exception) {
        60
    }
}
