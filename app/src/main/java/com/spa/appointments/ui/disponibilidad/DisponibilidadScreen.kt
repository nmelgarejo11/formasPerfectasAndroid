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
    LaunchedEffect(Unit) {
        vm.servicio    = servicio
        vm.profesional = profesional
        vm.seleccionarFecha(LocalDate.now())
    }

    val uiState           by vm.uiState.collectAsState()
    val fechaSeleccionada by vm.fechaSeleccionada.collectAsState()
    val slotSeleccionado  by vm.slotSeleccionado.collectAsState()

    var notas          by remember { mutableStateOf("") }
    var mostrarDialogo by remember { mutableStateOf(false) }
    var mostrarExito   by remember { mutableStateOf(false) }

    LaunchedEffect(uiState) {
        if (uiState is DisponibilidadUiState.CitaCreada) mostrarExito = true
    }

    // Botón habilitado si se seleccionó slot (Individual) o si es un flujo de Cita Grupal (Solo requiere fecha)
    val botonHabilitado = (slotSeleccionado != null || sharedVm.esGrupal) && uiState !is DisponibilidadUiState.CreandoCita

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

                    if (sharedVm.esGrupal) {
                        ConfirmacionFila(
                            icono  = Icons.Default.Person,
                            label  = "Responsable",
                            valor  = sharedVm.responsableNombre
                        )
                        ConfirmacionFila(
                            icono  = Icons.Default.Group,
                            label  = "Asistentes",
                            valor  = "${sharedVm.cantidadPersonas} personas"
                        )
                    } else {
                        ConfirmacionFila(
                            icono  = Icons.Default.Badge,
                            label  = "Profesional",
                            valor  = profesional?.nombreCompleto ?: ""
                        )
                    }

                    ConfirmacionFila(
                        icono  = Icons.Default.CalendarToday,
                        label  = "Fecha",
                        valor  = fechaSeleccionada.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                    )

                    if (!sharedVm.esGrupal) {
                        ConfirmacionFila(
                            icono  = Icons.Default.Schedule,
                            label  = "Hora",
                            valor  = "${slotSeleccionado?.horaInicio?.substring(0,5)} – ${slotSeleccionado?.horaFin?.substring(0,5)}"
                        )
                    }

                    HorizontalDivider()

                    // Cálculo multiplicador del total para grupos
                    val precioCalculado = if (sharedVm.esGrupal) servicio.precioBase * sharedVm.cantidadPersonas else servicio.precioBase
                    ConfirmacionFila(
                        icono  = Icons.Default.AttachMoney,
                        label  = "Total",
                        valor  = "${"$%,.0f".format(precioCalculado)}",
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
                            text       = if (sharedVm.esGrupal) "Elige la fecha grupal" else "Elige fecha y hora",
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
                                text       = if (sharedVm.esGrupal) "Solicitar Cita Grupal"
                                else slot?.let { "Reservar a las ${it.horaInicio.substring(0,5)}" } ?: "Selecciona un horario",
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
            // Reutilizamos el card superior, enviando datos dummy de profesional si es grupal
            ResumenReserva(servicio = servicio, profesional = profesional)

            Spacer(Modifier.height(8.dp))

            SelectorFecha(
                fechaSeleccionada   = fechaSeleccionada,
                onFechaSeleccionada = { vm.seleccionarFecha(it) }
            )

            Spacer(Modifier.height(16.dp))

            // ── Bloque condicional de slots: Solo renderiza si NO es una cita grupal ───────────────────
            if (!sharedVm.esGrupal) {
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
            } else {
                // Info para la vista grupal indicando que el día queda registrado para gestión administrativa
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Info, null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = "Las citas grupales se agendan para el día seleccionado. El equipo se contactará para coordinar la hora exacta del bloque.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
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
private fun ResumenReserva(servicio: Servicio, profesional: Profesional?) { // <-- Ahora acepta null
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
                        text       = "${"$%,.0f".format(servicio.precioBase)}",
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
                            Text(text = slot.horaInicio.substring(0, 5), textAlign = TextAlign.Center, fontWeight = if (seleccionado) FontWeight.Bold else FontWeight.Normal, fontSize = 14.sp, color = if (seleccionado) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
            }
        }
    }
}