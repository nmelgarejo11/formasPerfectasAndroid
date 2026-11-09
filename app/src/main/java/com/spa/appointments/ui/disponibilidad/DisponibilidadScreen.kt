// Ruta: app/src/main/java/com/spa/appointments/ui/disponibilidad/DisponibilidadScreen.kt
package com.spa.appointments.ui.disponibilidad

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
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
    profesional:  Profesional?,
    onBack:       () -> Unit,
    onCitaCreada: () -> Unit,
    sharedVm:     ReservaSharedViewModel,
    vm: DisponibilidadViewModel = hiltViewModel()
) {
    val profActual = if (sharedVm.esGrupal && sharedVm.asistentesGrupales.isNotEmpty() && sharedVm.asistenteActualIndex < sharedVm.asistentesGrupales.size) {
        sharedVm.asistentesGrupales[sharedVm.asistenteActualIndex].profesional ?: profesional
    } else {
        profesional
    }

    LaunchedEffect(servicio, sharedVm.cantidadPersonas, sharedVm.asistenteActualIndex) {
        vm.servicio    = servicio
        vm.profesional = profActual
        vm.cargarPrecioServicio(servicio.id, sharedVm.cantidadPersonas)
        vm.seleccionarFecha(LocalDate.now(), sharedVm)
    }

    val uiState           by vm.uiState.collectAsState()
    val fechaSeleccionada by vm.fechaSeleccionada.collectAsState()
    val slotSeleccionado  by vm.slotSeleccionado.collectAsState()
    val precioCalculado   by vm.precioCalculado.collectAsState()

    val precioMostrar = if (precioCalculado > 0.0) precioCalculado else (if (sharedVm.esGrupal) servicio.precioBase * sharedVm.cantidadPersonas else servicio.precioBase)

    var notas          by remember { mutableStateOf("") }
    var mostrarDialogo by remember { mutableStateOf(false) }
    var mostrarExito   by remember { mutableStateOf(false) }

    val esUltimoAsistente = !sharedVm.esGrupal || (sharedVm.asistenteActualIndex >= sharedVm.cantidadPersonas - 1)

    LaunchedEffect(uiState) {
        if (uiState is DisponibilidadUiState.CitaCreada) mostrarExito = true
    }

    // Botón habilitado si se seleccionó slot de horario
    val botonHabilitado = slotSeleccionado != null && uiState !is DisponibilidadUiState.CreandoCita

    // ── Diálogo Confirmar Reserva M3 ──────────────────────────────────────────
    if (mostrarDialogo) {
        AlertDialog(
            onDismissRequest = { mostrarDialogo = false },
            shape            = RoundedCornerShape(28.dp),
            containerColor   = MaterialTheme.colorScheme.surfaceContainerHigh,
            icon = {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Icon(
                        Icons.Default.EventAvailable, null,
                        tint     = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(12.dp).size(26.dp)
                    )
                }
            },
            title = {
                Text(
                    text       = if (sharedVm.esGrupal) "Confirmar Citas Grupales" else "Confirmar Reserva",
                    fontWeight = FontWeight.Bold,
                    textAlign  = TextAlign.Center,
                    modifier   = Modifier.fillMaxWidth(),
                    style      = MaterialTheme.typography.titleMedium
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    ConfirmacionFila(
                        icono = Icons.Default.Spa,
                        label = "Servicio",
                        valor = servicio.nombre
                    )

                    ConfirmacionFila(
                        icono = Icons.Default.Person,
                        label = "Cliente",
                        valor = sharedVm.clienteSeleccionado?.nombreCompleto ?: sharedVm.responsableNombre
                    )

                    if (!sharedVm.esGrupal) {
                        ConfirmacionFila(
                            icono = Icons.Default.Badge,
                            label = "Profesional",
                            valor = profActual?.nombreCompleto ?: "Asignación abierta"
                        )

                        ConfirmacionFila(
                            icono = Icons.Default.CalendarToday,
                            label = "Fecha",
                            valor = fechaSeleccionada.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                        )

                        ConfirmacionFila(
                            icono = Icons.Default.Schedule,
                            label = "Hora",
                            valor = "${formatearHora12h(slotSeleccionado?.horaInicio ?: "")} – ${formatearHora12h(slotSeleccionado?.horaFin ?: "")}"
                        )
                    } else {
                        ConfirmacionFila(
                            icono = Icons.Default.Group,
                            label = "Asistentes",
                            valor = "${sharedVm.cantidadPersonas} personas (${sharedVm.asistentesGrupales.size} citas)"
                        )

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                        sharedVm.asistentesGrupales.forEachIndexed { idx, config ->
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    text = "Asistente ${idx + 1}:",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "• Profesional: ${config.profesional?.nombreCompleto ?: "Sin asignar"}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Text(
                                    text = "• Fecha: ${config.fecha?.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) ?: "-"}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Text(
                                    text = "• Hora: ${formatearHora12h(config.slot?.horaInicio ?: "")} – ${formatearHora12h(config.slot?.horaFin ?: "")}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    // Total
                    ConfirmacionFila(
                        icono   = Icons.Default.AttachMoney,
                        label   = "Total",
                        valor   = "$%,.0f".format(precioMostrar),
                        negrita = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        mostrarDialogo = false
                        if (sharedVm.esGrupal) {
                            vm.confirmarCitasGrupales(sharedVm, notas.ifBlank { null })
                        } else {
                            vm.confirmarCita(sharedVm, notas.ifBlank { null })
                        }
                    },
                    shape    = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) { Text(if (sharedVm.esGrupal) "Guardar ${sharedVm.cantidadPersonas} citas" else "Confirmar cita", fontWeight = FontWeight.SemiBold) }
            },
            dismissButton = {
                OutlinedButton(
                    onClick  = { mostrarDialogo = false },
                    shape    = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Cancelar") }
            }
        )
    }

    // ── Diálogo Éxito M3 ──────────────────────────────────────────────────────
    if (mostrarExito) {
        AlertDialog(
            onDismissRequest = {},
            shape            = RoundedCornerShape(28.dp),
            containerColor   = MaterialTheme.colorScheme.surfaceContainerHigh,
            icon = {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Icon(
                        Icons.Default.CheckCircle, null,
                        tint     = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(14.dp).size(32.dp)
                    )
                }
            },
            title = {
                Text(
                    "¡Citas reservadas con éxito!",
                    fontWeight = FontWeight.Bold,
                    textAlign  = TextAlign.Center,
                    modifier   = Modifier.fillMaxWidth(),
                    style      = MaterialTheme.typography.titleMedium
                )
            },
            text = {
                Text(
                    text      = (uiState as? DisponibilidadUiState.CitaCreada)?.mensaje ?: "Tus citas fueron creadas exitosamente.",
                    textAlign = TextAlign.Center,
                    color     = MaterialTheme.colorScheme.onSurfaceVariant,
                    style     = MaterialTheme.typography.bodyMedium,
                    modifier  = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        mostrarExito = false
                        vm.resetear()
                        sharedVm.limpiarReserva()
                        onCitaCreada()
                    },
                    shape    = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Ver mis citas") }
            }
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Column {
                if (sharedVm.esGrupal) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Group,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "Cita Grupal — Asistente ${sharedVm.asistenteActualIndex + 1} de ${sharedVm.cantidadPersonas}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }

                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text       = "Elige fecha y hora",
                                style      = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Surface(
                                shape    = RoundedCornerShape(50),
                                color    = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.padding(top = 2.dp)
                            ) {
                                Text(
                                    text     = servicio.nombre,
                                    style    = MaterialTheme.typography.labelSmall,
                                    color    = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 1.dp)
                                )
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )

                // 🎨 Canvas: Acento decorativo debajo de la TopAppBar
                val primaryColor   = MaterialTheme.colorScheme.primary
                val secondaryColor = MaterialTheme.colorScheme.secondary
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                ) {
                    drawRect(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                primaryColor.copy(alpha = 0.4f),
                                secondaryColor.copy(alpha = 0.2f),
                                primaryColor.copy(alpha = 0.05f)
                            )
                        )
                    )
                }

                HorizontalDivider(
                    thickness = 0.5.dp,
                    color     = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )
            }
        },
        bottomBar = {
            Surface(
                shadowElevation = 8.dp,
                tonalElevation  = 6.dp,
                shape           = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                color           = MaterialTheme.colorScheme.surfaceContainerHigh
            ) {
                AnimatedContent(
                    targetState = slotSeleccionado,
                    label       = "boton_reservar"
                ) { slot ->
                    Button(
                        onClick  = {
                            if (sharedVm.esGrupal && slot != null && sharedVm.asistentesGrupales.isNotEmpty()) {
                                val config = sharedVm.asistentesGrupales[sharedVm.asistenteActualIndex]
                                config.fecha = fechaSeleccionada
                                config.slot = slot
                                if (!esUltimoAsistente) {
                                    sharedVm.asistenteActualIndex++
                                    onBack() // vuelve a la pantalla de selección de profesional para el siguiente asistente
                                } else {
                                    mostrarDialogo = true
                                }
                            } else {
                                mostrarDialogo = true
                            }
                        },
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
                                modifier           = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            val textoBoton = when {
                                slot == null -> "Selecciona un horario"
                                !sharedVm.esGrupal -> "Confirmar a las ${formatearHora12h(slot.horaInicio)}"
                                !esUltimoAsistente -> "Guardar Asistente ${sharedVm.asistenteActualIndex + 1} y continuar"
                                else -> "Revisar y confirmar ${sharedVm.cantidadPersonas} citas"
                            }
                            Text(
                                text       = textoBoton,
                                fontWeight = FontWeight.Bold,
                                fontSize   = 15.sp
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
                .padding(bottom = 12.dp)
        ) {
            ResumenReserva(servicio = servicio, profesional = profActual, precioTotal = precioMostrar)

            Spacer(Modifier.height(8.dp))

            SelectorFecha(
                fechaSeleccionada   = fechaSeleccionada,
                onFechaSeleccionada = { vm.seleccionarFecha(it, sharedVm) }
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
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            CircularProgressIndicator(
                                color       = MaterialTheme.colorScheme.primary,
                                trackColor  = MaterialTheme.colorScheme.primaryContainer,
                                strokeWidth = 3.dp,
                                modifier    = Modifier.size(36.dp)
                            )
                            Text(
                                "Buscando horarios disponibles…",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
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
                    OutlinedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        shape  = RoundedCornerShape(16.dp),
                        colors = CardDefaults.outlinedCardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                        )
                    ) {
                        Row(
                            modifier          = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.CloudOff, null,
                                modifier = Modifier.size(20.dp),
                                tint     = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Spacer(Modifier.width(10.dp))
                            Text(
                                text  = state.mensaje,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyMedium
                            )
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
                label         = { Text("Notas de la reserva (opcional)") },
                placeholder   = { Text("Ej: Alergia a ciertos productos…") },
                leadingIcon   = { Icon(Icons.Default.Notes, null, modifier = Modifier.size(18.dp)) },
                modifier      = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                shape         = RoundedCornerShape(14.dp),
                minLines      = 2,
                maxLines      = 4,
                colors        = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor   = MaterialTheme.colorScheme.surfaceContainerLow,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow
                )
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
        Text(text = valor, style = MaterialTheme.typography.bodySmall, fontWeight = if (negrita) FontWeight.Bold else FontWeight.Medium, color = if (negrita) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
private fun ResumenReserva(servicio: Servicio, profesional: Profesional?, precioTotal: Double) {
    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape  = RoundedCornerShape(18.dp),
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text       = servicio.nombre,
                style      = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color      = MaterialTheme.colorScheme.onSurface
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Badge, null,
                    modifier = Modifier.size(15.dp),
                    tint     = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text  = profesional?.nombreCompleto ?: "Asignación Abierta (Grupal)",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Timer, null,
                        modifier = Modifier.size(15.dp),
                        tint     = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text  = "${servicio.duracionMinutos} min",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Row(
                        modifier          = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.AttachMoney, null,
                            modifier = Modifier.size(14.dp),
                            tint     = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text       = "$%,.0f".format(precioTotal),
                            style      = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color      = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
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
        Text(
            text       = "Selecciona la fecha",
            style      = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            modifier   = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(dias) { dia ->
                val seleccionado = dia == fechaSeleccionada
                val esHoy        = dia == today
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    if (dia.dayOfMonth == 1 || dia == today) {
                        Text(
                            text     = if (esHoy) "HOY" else dia.month.getDisplayName(TextStyle.SHORT, Locale("es")).uppercase(),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (seleccionado) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 2.dp)
                        )
                    } else { Spacer(Modifier.height(12.dp)) }

                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (seleccionado) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerLow,
                        border = if (!seleccionado && esHoy) androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                        modifier = Modifier.clickable { onFechaSeleccionada(dia) }
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier            = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Text(
                                text     = dia.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale("es")),
                                fontSize = 11.sp,
                                color    = if (seleccionado) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text       = dia.dayOfMonth.toString(),
                                fontWeight = FontWeight.Bold,
                                fontSize   = 18.sp,
                                color      = if (seleccionado) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SlotsGrid(slots: List<SlotDisponible>, slotSeleccionado: SlotDisponible?, onSlotClick: (SlotDisponible) -> Unit) {
    val slotsDisponibles = slots.filter { it.disponible }
    Column(modifier = Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(text = "Horarios disponibles", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            if (slotsDisponibles.isNotEmpty()) {
                Surface(shape = RoundedCornerShape(50), color = MaterialTheme.colorScheme.primaryContainer) {
                    Text(
                        text     = "${slotsDisponibles.size} disponibles",
                        style    = MaterialTheme.typography.labelSmall,
                        color    = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }
        }

        if (slotsDisponibles.isEmpty()) {
            OutlinedCard(
                shape  = RoundedCornerShape(16.dp),
                colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.EventBusy, null, modifier = Modifier.size(22.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.width(12.dp))
                    Text(text = "No hay horarios disponibles para este día.", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
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
                        shape  = RoundedCornerShape(12.dp),
                        color  = if (seleccionado) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerLow,
                        border = if (seleccionado) null else androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                        modifier = Modifier.weight(1f).clickable { onSlotClick(slot) }
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(vertical = 12.dp)) {
                            Text(
                                text      = formatearHora12h(slot.horaInicio),
                                textAlign = TextAlign.Center,
                                fontWeight = if (seleccionado) FontWeight.Bold else FontWeight.Medium,
                                fontSize  = 13.sp,
                                color     = if (seleccionado) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                            )
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
        val partes   = cleanStr.split(":")
        val h        = partes[0].toInt()
        val m        = partes[1]
        val amPm     = if (h >= 12) "PM" else "AM"
        val h12      = when {
            h == 0 -> 12
            h > 12 -> h - 12
            else -> h
        }
        String.format(Locale.US, "%02d:%s %s", h12, m, amPm)
    } catch (e: Exception) {
        horaStr.take(5)
    }
}
