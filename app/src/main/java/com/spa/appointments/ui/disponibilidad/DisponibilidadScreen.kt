package com.spa.appointments.ui.disponibilidad

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.spa.appointments.domain.model.Profesional
import com.spa.appointments.domain.model.Servicio
import com.spa.appointments.domain.model.SlotDisponible
import com.spa.appointments.ui.reserva.ReservaSharedViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DisponibilidadScreen(
    servicio:     Servicio,
    profesional:  Profesional?, // Permitimos nulo si es una asignación grupal abierta
    onBack:       () -> Unit,
    onCitaCreada: () -> Unit,
    sharedVm:     ReservaSharedViewModel, // Recibimos el SharedViewModel desde el AppNav
    vm: DisponibilidadViewModel = hiltViewModel()
) {
    LaunchedEffect(servicio, sharedVm.cantidadPersonas) {
        vm.servicio    = servicio
        vm.profesional = profesional
        vm.cargarPrecioServicio(servicio.id, sharedVm.cantidadPersonas)
        vm.seleccionarFecha(LocalDate.now())
    }

    val uiState           by vm.uiState.collectAsState()
    val fechaSeleccionada by vm.fechaSeleccionada.collectAsState()
    val slotSeleccionado  by vm.slotSeleccionado.collectAsState()
    val precioCalculado   by vm.precioCalculado.collectAsState()

    val precioMostrar = if (precioCalculado > 0.0) precioCalculado else (if (sharedVm.esGrupal) servicio.precioBase * sharedVm.cantidadPersonas else servicio.precioBase)

    var notas          by remember { mutableStateOf("") }
    var mostrarDialogo by remember { mutableStateOf(false) }
    var mostrarExito   by remember { mutableStateOf(false) }

    LaunchedEffect(uiState) {
        if (uiState is DisponibilidadUiState.CitaCreada) mostrarExito = true
    }

    // Botón habilitado si se seleccionó slot de horario
    val botonHabilitado = slotSeleccionado != null && uiState !is DisponibilidadUiState.CreandoCita

    if (mostrarDialogo) {
        AlertDialog(
            onDismissRequest = { mostrarDialogo = false },
            shape = RoundedCornerShape(16.dp),
            icon  = {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Icon(
                        Icons.Default.EventAvailable, null,
                        tint     = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(10.dp).size(24.dp)
                    )
                }
            },
            title = { Text(if(sharedVm.esGrupal) "Confirmar Cita Grupal" else "Confirmar reserva", fontWeight = FontWeight.Bold) },
            text  = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ConfirmacionFila(
                        icono  = Icons.Default.Spa,
                        label  = "Servicio",
                        valor  = servicio.nombre
                    )

                    ConfirmacionFila(
                        icono  = Icons.Default.Person,
                        label  = "Cliente",
                        valor  = sharedVm.clienteSeleccionado?.nombreCompleto ?: sharedVm.responsableNombre
                    )

                    ConfirmacionFila(
                        icono  = Icons.Default.Badge,
                        label  = "Profesional",
                        valor  = profesional?.nombreCompleto ?: ""
                    )

                    if (sharedVm.esGrupal) {
                        ConfirmacionFila(
                            icono  = Icons.Default.Group,
                            label  = "Asistentes",
                            valor  = "${sharedVm.cantidadPersonas} personas"
                        )
                    }

                    ConfirmacionFila(
                        icono  = Icons.Default.CalendarToday,
                        label  = "Fecha",
                        valor  = fechaSeleccionada.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                    )

                    ConfirmacionFila(
                        icono  = Icons.Default.Schedule,
                        label  = "Hora",
                        valor  = "${formatearHora12h(slotSeleccionado?.horaInicio ?: "")} – ${formatearHora12h(slotSeleccionado?.horaFin ?: "")}"
                    )

                    HorizontalDivider()

                    // Total
                    ConfirmacionFila(
                        icono  = Icons.Default.AttachMoney,
                        label  = "Total",
                        valor  = "${"$%,.0f".format(precioMostrar)}",
                        negrita = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        mostrarDialogo = false
                        vm.confirmarCita(sharedVm, notas.ifBlank { null })
                    },
                    shape   = RoundedCornerShape(10.dp)
                ) { Text("Confirmar") }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { mostrarDialogo = false },
                    shape   = RoundedCornerShape(10.dp)
                ) { Text("Cancelar") }
            }
        )
    }

    if (mostrarExito) {
        AlertDialog(
            onDismissRequest = {},
            shape = RoundedCornerShape(16.dp),
            icon  = {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Icon(
                        Icons.Default.CheckCircle, null,
                        tint     = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(12.dp).size(32.dp)
                    )
                }
            },
            title = {
                Text(
                    "¡Cita reservada!",
                    fontWeight = FontWeight.Bold,
                    textAlign  = TextAlign.Center,
                    modifier   = Modifier.fillMaxWidth()
                )
            },
            text  = {
                Text(
                    text      = (uiState as? DisponibilidadUiState.CitaCreada)?.mensaje ?: "Tu cita fue creada exitosamente.",
                    textAlign = TextAlign.Center,
                    color     = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier  = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        mostrarExito = false
                        vm.resetear()
                        sharedVm.limpiarReserva() // Limpieza del estado global al terminar
                        onCitaCreada()
                    },
                    shape   = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Ver mis citas") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text       = "Elige fecha y hora",
                            style      = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text  = servicio.nombre,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Volver")
                    }
                }
            )
        },
        bottomBar = {
            Surface(
                shadowElevation = 8.dp,
                tonalElevation  = 2.dp
            ) {
                AnimatedContent(
                    targetState = slotSeleccionado,
                    label       = "boton_reservar"
                ) { slot ->
                    Button(
                        onClick  = { mostrarDialogo = true },
                        enabled  = botonHabilitado,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                            .height(52.dp),
                        shape    = RoundedCornerShape(12.dp)
                    ) {
                        if (uiState is DisponibilidadUiState.CreandoCita) {
                            CircularProgressIndicator(
                                modifier    = Modifier.size(20.dp),
                                color       = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector        = Icons.Default.EventAvailable,
                                contentDescription = null,
                                modifier           = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text       = slot?.let { "Confirmar a las ${formatearHora12h(it.horaInicio)}" } ?: "Selecciona un horario",
                                fontWeight = FontWeight.SemiBold
                            )
                        }
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
                .padding(bottom = 8.dp)
        ) {
            ResumenReserva(servicio = servicio, profesional = profesional, precioTotal = precioMostrar)

            Spacer(Modifier.height(8.dp))

            SelectorFecha(
                fechaSeleccionada   = fechaSeleccionada,
                onFechaSeleccionada = { vm.seleccionarFecha(it) }
            )

            Spacer(Modifier.height(16.dp))

            // Slots de disponibilidad horaria
            when (val state = uiState) {
                is DisponibilidadUiState.LoadingSlots -> {
                    Box(
                        modifier         = Modifier.fillMaxWidth().height(180.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            CircularProgressIndicator()
                            Text("Buscando horarios disponibles…", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
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
                    Surface(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.errorContainer
                    ) {
                        Row(
                            modifier          = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.CloudOff, null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onErrorContainer)
                            Spacer(Modifier.width(8.dp))
                            Text(text = state.mensaje, color = MaterialTheme.colorScheme.onErrorContainer, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
                else -> Unit
            }

            // ── Notas ─────────────────────────────────────────────────────
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value         = notas,
                onValueChange = { notas = it },
                label         = { Text("Notas (opcional)") },
                placeholder   = { Text("Ej: Alergia a ciertos productos…") },
                leadingIcon   = { Icon(Icons.Default.Notes, null) },
                modifier      = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                shape         = RoundedCornerShape(12.dp),
                minLines      = 2,
                maxLines      = 4
            )
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ConfirmacionFila(
    icono:   androidx.compose.ui.graphics.vector.ImageVector,
    label:   String,
    valor:   String,
    negrita: Boolean = false
) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Icon(imageVector = icono, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.width(8.dp))
        Text(text = "$label:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.width(85.dp))
        Text(text = valor, style = MaterialTheme.typography.bodySmall, fontWeight = if (negrita) FontWeight.Bold else FontWeight.Normal, color = if (negrita) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
private fun ResumenReserva(servicio: Servicio, profesional: Profesional?, precioTotal: Double) { // <-- Ahora acepta null y precioTotal
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape  = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text       = servicio.nombre,
                style      = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color      = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Badge, null,
                    modifier = Modifier.size(13.dp),
                    tint     = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    // Muestra el nombre del profesional o el aviso de asignación abierta si es grupal
                    text  = profesional?.nombreCompleto ?: "Asignación Abierta (Grupal)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            HorizontalDivider(
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.15f)
            )
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Timer, null,
                        modifier = Modifier.size(13.dp),
                        tint     = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text  = "${servicio.duracionMinutos} min",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.AttachMoney, null,
                        modifier = Modifier.size(13.dp),
                        tint     = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                    Text(
                        text       = "${"$%,.0f".format(precioTotal)}",
                        style      = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }
}

@Composable
private fun SelectorFecha(fechaSeleccionada: LocalDate, onFechaSeleccionada: (LocalDate) -> Unit) {
    val dias  = (0..29).map { LocalDate.now().plusDays(it.toLong()) }
    val today = LocalDate.now()

    Column {
        Text(text = "Selecciona la fecha", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
        LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(dias) { dia ->
                val seleccionado = dia == fechaSeleccionada
                val esHoy        = dia == today
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    if (dia.dayOfMonth == 1 || dia == today) {
                        Text(
                            text     = if (esHoy) "HOY" else dia.month.getDisplayName(TextStyle.SHORT, Locale("es")).uppercase(),
                            fontSize = 8.sp, fontWeight = FontWeight.Bold, color = if (seleccionado) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.padding(bottom = 2.dp)
                        )
                    } else { Spacer(Modifier.height(12.dp)) }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (seleccionado) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                            .then(if (!seleccionado && esHoy) Modifier.border(1.5.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp)) else Modifier)
                            .clickable { onFechaSeleccionada(dia) }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text(text = dia.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale("es")), fontSize = 10.sp, color = if (seleccionado) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(text = dia.dayOfMonth.toString(), fontWeight = FontWeight.Bold, fontSize = 18.sp, color = if (seleccionado) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
        }
    }
}

@Composable
private fun SlotsGrid(slots: List<SlotDisponible>, slotSeleccionado: SlotDisponible?, onSlotClick: (SlotDisponible) -> Unit) {
    val slotsDisponibles = slots.filter { it.disponible }
    Column(modifier = Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(text = "Horarios disponibles", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            if (slotsDisponibles.isNotEmpty()) {
                Text(text = "${slotsDisponibles.size} disponibles", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        if (slotsDisponibles.isEmpty()) {
            Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.EventBusy, null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.width(10.dp))
                    Text(text = "No hay horarios disponibles para este día.", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                }
            }
            return
        }

        val filas = slotsDisponibles.chunked(3)
        filas.forEach { fila ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                fila.forEach { slot ->
                    val seleccionado = slot == slotSeleccionado
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (seleccionado) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        border = if (seleccionado) null else androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                        modifier = Modifier.weight(1f).clickable { onSlotClick(slot) }
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(vertical = 12.dp)) {
                            Text(text = formatearHora12h(slot.horaInicio), textAlign = TextAlign.Center, fontWeight = if (seleccionado) FontWeight.Bold else FontWeight.Normal, fontSize = 13.sp, color = if (seleccionado) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
            }
        }
    }
}

private fun formatearHora12h(horaStr: String): String {
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
    } catch (e: Exception) {
        horaStr.take(5)
    }
}