package com.spa.appointments.ui.disponibilidad

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.spa.appointments.domain.model.Profesional
import com.spa.appointments.domain.model.Servicio
import com.spa.appointments.domain.model.SlotDisponible
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DisponibilidadScreen(
    servicio: Servicio,
    profesional: Profesional,
    onBack: () -> Unit,
    onCitaCreada: () -> Unit,
    vm: DisponibilidadViewModel = hiltViewModel()
) {
    // Pasamos los datos al ViewModel al entrar a la pantalla
    LaunchedEffect(Unit) {
        vm.servicio     = servicio
        vm.profesional  = profesional
        vm.seleccionarFecha(LocalDate.now())
    }

    val uiState          by vm.uiState.collectAsState()
    val fechaSeleccionada by vm.fechaSeleccionada.collectAsState()
    val slotSeleccionado  by vm.slotSeleccionado.collectAsState()

    var notas            by remember { mutableStateOf("") }
    var mostrarDialogo   by remember { mutableStateOf(false) }
    var mostrarExito     by remember { mutableStateOf(false) }

    // Cuando la cita se crea exitosamente
    LaunchedEffect(uiState) {
        if (uiState is DisponibilidadUiState.CitaCreada) {
            mostrarExito = true
        }
    }

    // Diálogo de confirmación
    if (mostrarDialogo) {
        AlertDialog(
            onDismissRequest = { mostrarDialogo = false },
            title   = { Text("Confirmar cita") },
            text    = {
                Column {
                    Text("Servicio: ${servicio.nombre}")
                    Text("Profesional: ${profesional.nombreCompleto}")
                    Text("Fecha: ${fechaSeleccionada.format(
                        DateTimeFormatter.ofPattern("dd/MM/yyyy"))}")
                    Text("Hora: ${slotSeleccionado?.horaInicio} - ${slotSeleccionado?.horaFin}")
                    Text("Total: ${"$%,.0f".format(servicio.precioBase)}")
                }
            },
            confirmButton = {
                Button(onClick = {
                    mostrarDialogo = false
                    vm.confirmarCita(notas.ifBlank { null })
                }) { Text("Confirmar") }
            },
            dismissButton = {
                OutlinedButton(onClick = { mostrarDialogo = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Diálogo de éxito
    if (mostrarExito) {
        AlertDialog(
            onDismissRequest = {},
            icon  = {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(48.dp)
                )
            },
            title = { Text("¡Cita reservada!") },
            text  = {
                Text(
                    (uiState as? DisponibilidadUiState.CitaCreada)?.mensaje
                        ?: "Tu cita fue creada exitosamente."
                )
            },
            confirmButton = {
                Button(onClick = {
                    mostrarExito = false
                    vm.resetear()
                    onCitaCreada()
                }) { Text("Ver mis citas") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Elige fecha y hora") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Volver")
                    }
                }
            )
        },
        bottomBar = {
            // Botón de confirmar fijo abajo
            Surface(shadowElevation = 8.dp) {
                Button(
                    onClick = { mostrarDialogo = true },
                    enabled = slotSeleccionado != null &&
                            uiState !is DisponibilidadUiState.CreandoCita,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    if (uiState is DisponibilidadUiState.CreandoCita) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color    = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = slotSeleccionado?.let {
                                "Reservar a las ${it.horaInicio}"
                            } ?: "Selecciona un horario"
                        )
                    }
                }
            }
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {

            // ── Resumen del servicio y profesional ──
            ResumenReserva(servicio = servicio, profesional = profesional)

            Spacer(Modifier.height(8.dp))

            // ── Selector de fecha (próximos 30 días) ──
            SelectorFecha(
                fechaSeleccionada = fechaSeleccionada,
                onFechaSeleccionada = { vm.seleccionarFecha(it) }
            )

            Spacer(Modifier.height(16.dp))

            // ── Slots de tiempo ──
            when (val state = uiState) {
                is DisponibilidadUiState.LoadingSlots -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                is DisponibilidadUiState.SlotsLoaded -> {
                    SlotsGrid(
                        slots            = state.slots,
                        slotSeleccionado = slotSeleccionado,
                        onSlotClick      = { vm.seleccionarSlot(it) }
                    )
                }

                is DisponibilidadUiState.Error -> {
                    Text(
                        text     = state.mensaje,
                        color    = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(24.dp)
                    )
                }

                else -> Unit
            }

            // ── Notas opcionales ──
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value         = notas,
                onValueChange = { notas = it },
                label         = { Text("Notas (opcional)") },
                placeholder   = { Text("Ej: Alergia a ciertos productos...") },
                modifier      = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                minLines      = 2,
                maxLines      = 4
            )

            Spacer(Modifier.height(100.dp)) // espacio para el botón fijo
        }
    }
}

// ── Componente: Resumen ───────────────────────────────────────────────────────
@Composable
private fun ResumenReserva(servicio: Servicio, profesional: Profesional) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text  = servicio.nombre,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text  = "con ${profesional.nombreCompleto}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text  = "${servicio.duracionMinutos} min",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text  = "${"$%,.0f".format(servicio.precioBase)}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

// ── Componente: Selector de fecha ─────────────────────────────────────────────
@Composable
private fun SelectorFecha(
    fechaSeleccionada: LocalDate,
    onFechaSeleccionada: (LocalDate) -> Unit
) {
    // Generamos los próximos 30 días
    val dias = (0..29).map { LocalDate.now().plusDays(it.toLong()) }

    Column {
        Text(
            text     = "Selecciona la fecha",
            style    = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        LazyRow(
            contentPadding    = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(dias) { dia ->
                val seleccionado = dia == fechaSeleccionada
                val esHoy        = dia == LocalDate.now()

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (seleccionado) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                        .clickable { onFechaSeleccionada(dia) }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        text  = dia.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale("es")),
                        fontSize = 11.sp,
                        color = if (seleccionado) MaterialTheme.colorScheme.onPrimary
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text  = dia.dayOfMonth.toString(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = if (seleccionado) MaterialTheme.colorScheme.onPrimary
                        else MaterialTheme.colorScheme.onSurface
                    )
                    if (esHoy) {
                        Text(
                            text  = "Hoy",
                            fontSize = 9.sp,
                            color = if (seleccionado) MaterialTheme.colorScheme.onPrimary
                            else MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

// ── Componente: Grid de slots ─────────────────────────────────────────────────
@Composable
private fun SlotsGrid(
    slots: List<SlotDisponible>,
    slotSeleccionado: SlotDisponible?,
    onSlotClick: (SlotDisponible) -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text(
            text       = "Horarios disponibles",
            style      = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            modifier   = Modifier.padding(bottom = 8.dp)
        )

        val slotsDisponibles = slots.filter { it.disponible }

        if (slotsDisponibles.isEmpty()) {
            Text(
                text     = "No hay horarios disponibles para este día.",
                color    = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 16.dp)
            )
            return
        }

        // Dividimos los slots en filas de 3 columnas manualmente
        // Así el Column crece según el contenido sin altura fija
        val filas = slotsDisponibles.chunked(3)

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            filas.forEach { fila ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    fila.forEach { slot ->
                        val seleccionado = slot == slotSeleccionado

                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (seleccionado) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                                .border(
                                    width = if (seleccionado) 0.dp else 1.dp,
                                    color = MaterialTheme.colorScheme.outline,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable { onSlotClick(slot) }
                                .padding(vertical = 12.dp)
                        ) {
                            Text(
                                text       = slot.horaInicio.substring(0, 5),
                                textAlign  = TextAlign.Center,
                                fontWeight = if (seleccionado) FontWeight.Bold
                                else FontWeight.Normal,
                                color      = if (seleccionado) MaterialTheme.colorScheme.onPrimary
                                else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    // Si la fila tiene menos de 3 slots, rellenamos con espacios
                    // para que los slots existentes mantengan el mismo ancho
                    repeat(3 - fila.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}