package com.spa.appointments.ui.citas.components

import android.app.DatePickerDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spa.appointments.domain.model.Cita
import com.spa.appointments.domain.model.Profesional
import com.spa.appointments.domain.model.Servicio
import com.spa.appointments.domain.model.ServicioCita
import com.spa.appointments.domain.model.SlotDisponible
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModificarCitaDialog(
    cita: Cita,
    servicios: List<Servicio>,
    profesionales: List<Profesional>,
    cargandoSlots: Boolean,
    slotsDisponibles: List<SlotDisponible>,
    onCargarProfesionales: (idSede: Int, idServicio: Int?) -> Unit,
    onCargarDisponibilidad: (idProfesional: Int, idSede: Int, fecha: String, duracion: Int) -> Unit,
    onConfirmarModificacion: (idCita: Int, idProfesional: Int, fechaHoraInicio: String, fechaHoraFin: String, idServicio: Int?) -> Unit,
    onDismiss: () -> Unit
) {
    val idServicioOriginal = remember(cita.servicios) {
        extraerIdServicioOriginal(cita.servicios)
    }

    var servicioSeleccionado by remember(cita, servicios) {
        mutableStateOf(servicios.firstOrNull { it.id == idServicioOriginal } ?: servicios.firstOrNull())
    }
    var expandedServicio by remember { mutableStateOf(false) }

    var profesionalSeleccionado by remember(cita, profesionales) {
        mutableStateOf(profesionales.firstOrNull { it.id == cita.idProfesional } ?: profesionales.firstOrNull())
    }
    var expandedProf by remember { mutableStateOf(false) }

    var fechaSeleccionada by remember { mutableStateOf(LocalDate.now()) }
    var slotSeleccionado by remember { mutableStateOf<SlotDisponible?>(null) }

    val formatterFechaIso = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    val duracionOriginal = remember(cita.servicios) {
        extraerDuracionServicio(cita.servicios)
    }
    var duracionPersonalizada by remember(cita, servicioSeleccionado) {
        mutableIntStateOf(servicioSeleccionado?.duracionMinutos ?: duracionOriginal)
    }

    // Al seleccionar o cambiar servicio, solicitar la lista de profesionales capacitados
    LaunchedEffect(servicioSeleccionado) {
        val idServ = servicioSeleccionado?.id
        if (idServ != null) {
            onCargarProfesionales(cita.idSede, idServ)
        }
    }

    // Cuando se actualice la lista de profesionales, ajustar la selección si el actual ya no está
    LaunchedEffect(profesionales) {
        if (profesionales.isNotEmpty() && profesionales.none { it.id == profesionalSeleccionado?.id }) {
            profesionalSeleccionado = profesionales.firstOrNull()
        }
    }

    // Cargar slots cuando cambia profesional, fecha o duración
    LaunchedEffect(profesionalSeleccionado, fechaSeleccionada, duracionPersonalizada) {
        val prof = profesionalSeleccionado
        if (prof != null) {
            slotSeleccionado = null
            onCargarDisponibilidad(
                prof.id,
                cita.idSede,
                fechaSeleccionada.format(formatterFechaIso),
                duracionPersonalizada
            )
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        icon = {
            Icon(
                Icons.Default.Edit,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
        },
        title = {
            Text(
                "Modificar Cita",
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Ajusta el servicio, profesional, fecha y nuevo horario disponible.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // 1. Selector de Servicio (Si hay lista disponible)
                if (servicios.isNotEmpty()) {
                    ExposedDropdownMenuBox(
                        expanded = expandedServicio,
                        onExpandedChange = { expandedServicio = it }
                    ) {
                        OutlinedTextField(
                            value = servicioSeleccionado?.nombre ?: "Seleccionar servicio",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Servicio") },
                            leadingIcon = { Icon(Icons.Default.Spa, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expandedServicio) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        ExposedDropdownMenu(
                            expanded = expandedServicio,
                            onDismissRequest = { expandedServicio = false }
                        ) {
                            servicios.forEach { serv ->
                                DropdownMenuItem(
                                    text = { Text("${serv.nombre} (${serv.duracionMinutos} min)") },
                                    onClick = {
                                        servicioSeleccionado = serv
                                        duracionPersonalizada = serv.duracionMinutos
                                        expandedServicio = false
                                    }
                                )
                            }
                        }
                    }
                }

                // 2. Selector de Profesional Habilitado
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

                // 3. Selector de Duración de Cita (Ingreso libre de minutos)
                var textoInputDuracion by remember(duracionPersonalizada) {
                    mutableStateOf(duracionPersonalizada.toString())
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Duración de la cita:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Sugerida: ${servicioSeleccionado?.duracionMinutos ?: duracionOriginal} min",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedIconButton(
                        onClick = {
                            val nuevo = (duracionPersonalizada - 5).coerceAtLeast(5)
                            duracionPersonalizada = nuevo
                        },
                        enabled = duracionPersonalizada > 5,
                        modifier = Modifier.size(40.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("-5", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    OutlinedTextField(
                        value = textoInputDuracion,
                        onValueChange = { input ->
                            if (input.all { it.isDigit() } && input.length <= 3) {
                                textoInputDuracion = input
                                val valInt = input.toIntOrNull()
                                if (valInt != null && valInt in 1..480) {
                                    duracionPersonalizada = valInt
                                }
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        textStyle = LocalTextStyle.current.copy(
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        ),
                        suffix = { Text("min", style = MaterialTheme.typography.bodySmall) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    )

                    OutlinedIconButton(
                        onClick = {
                            val nuevo = (duracionPersonalizada + 5).coerceAtMost(480)
                            duracionPersonalizada = nuevo
                        },
                        modifier = Modifier.size(40.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("+5", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }

                // 4. Selector de Fecha (Carrusel de días + Botón Calendario)
                val context = LocalContext.current
                var mostrarDatePickerDialog by remember { mutableStateOf(false) }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Selecciona la fecha:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(
                        onClick = { mostrarDatePickerDialog = true },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = "Elegir fecha",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                val baseDate = if (fechaSeleccionada.isBefore(LocalDate.now())) LocalDate.now() else fechaSeleccionada
                val dias = remember(baseDate) {
                    val inicio = if (baseDate.minusDays(3).isBefore(LocalDate.now())) LocalDate.now() else baseDate.minusDays(3)
                    (0..29).map { inicio.plusDays(it.toLong()) }
                }
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

                if (mostrarDatePickerDialog) {
                    val cal = Calendar.getInstance()
                    cal.set(fechaSeleccionada.year, fechaSeleccionada.monthValue - 1, fechaSeleccionada.dayOfMonth)

                    val dpd = DatePickerDialog(
                        context,
                        { _, year, month, dayOfMonth ->
                            fechaSeleccionada = LocalDate.of(year, month + 1, dayOfMonth)
                            mostrarDatePickerDialog = false
                        },
                        cal.get(Calendar.YEAR),
                        cal.get(Calendar.MONTH),
                        cal.get(Calendar.DAY_OF_MONTH)
                    )
                    dpd.setOnCancelListener { mostrarDatePickerDialog = false }
                    dpd.setOnDismissListener { mostrarDatePickerDialog = false }
                    LaunchedEffect(Unit) {
                        dpd.show()
                    }
                }

                // 5. Grid de Slots de Horario
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
                    val filas = slotsValidos.chunked(3)

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        filas.forEach { fila ->
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                fila.forEach { slot ->
                                    val esSeleccionado = slotSeleccionado == slot

                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = if (esSeleccionado) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.surfaceVariant,
                                        border = if (esSeleccionado) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable { slotSeleccionado = slot }
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
                                repeat(3 - fila.size) {
                                    Spacer(Modifier.weight(1f))
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
                        val horaIniClean = if (hIni.length == 5) "${hIni}:00" else hIni.take(8)

                        val inicioIso = "${fechaIso}T${horaIniClean}"

                        val horaIniLocal = java.time.LocalTime.parse(horaIniClean)
                        val horaFinLocal = horaIniLocal.plusMinutes(duracionPersonalizada.toLong())
                        val horaFinClean = String.format(Locale.US, "%02d:%02d:00", horaFinLocal.hour, horaFinLocal.minute)
                        val finIso = "${fechaIso}T${horaFinClean}"

                        onConfirmarModificacion(cita.id, prof.id, inicioIso, finIso, servicioSeleccionado?.id)
                    }
                },
                enabled = slotSeleccionado != null && profesionalSeleccionado != null,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text(
                    text = slotSeleccionado?.let { "Guardar Cambios (${formatearHora12h(it.horaInicio)})" } ?: "Guardar Cambios",
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
    ModificarCitaDialog(
        cita = cita,
        servicios = emptyList(),
        profesionales = profesionales,
        cargandoSlots = cargandoSlots,
        slotsDisponibles = slotsDisponibles,
        onCargarProfesionales = { _, _ -> },
        onCargarDisponibilidad = onCargarDisponibilidad,
        onConfirmarModificacion = { idCita, idProf, ini, fin, _ ->
            onConfirmarReagendamiento(idCita, idProf, ini, fin)
        },
        onDismiss = onDismiss
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

private fun extraerIdServicioOriginal(json: String?): Int? {
    if (json.isNullOrBlank()) return null
    return try {
        val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
        val type = Types.newParameterizedType(List::class.java, ServicioCita::class.java)
        val lista = moshi.adapter<List<ServicioCita>>(type).fromJson(json) ?: emptyList()
        lista.firstOrNull()?.id
    } catch (e: Exception) {
        null
    }
}
