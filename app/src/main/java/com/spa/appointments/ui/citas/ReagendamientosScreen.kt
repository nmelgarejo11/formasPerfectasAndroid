// Ruta: app/src/main/java/com/spa/appointments/ui/citas/ReagendamientosScreen.kt
package com.spa.appointments.ui.citas

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.spa.appointments.domain.model.CitaPendiente
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

// ─── Screen principal ─────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReagendamientosScreen(
    onBack:    () -> Unit,
    viewModel: ReagendamientoViewModel = hiltViewModel()
) {
    val uiState           by viewModel.uiState.collectAsState()
    val actionState       by viewModel.actionState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    val totalPendientes = (uiState as? ReagendamientoUiState.Success)?.pendientes?.size

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.cargar()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(actionState) {
        when (actionState) {
            is ReagendamientoActionState.Success ->
                snackbarHostState.showSnackbar("Acción realizada correctamente")
            is ReagendamientoActionState.Error   ->
                snackbarHostState.showSnackbar(
                    (actionState as ReagendamientoActionState.Error).mensaje
                )
            else -> Unit
        }
        if (actionState !is ReagendamientoActionState.Idle) viewModel.resetActionState()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text       = "Reagendamientos",
                            style      = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        if (totalPendientes != null) {
                            Text(
                                text  = "$totalPendientes ${if (totalPendientes == 1) "pendiente" else "pendientes"}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.cargar() }) {
                        Icon(Icons.Default.Refresh, "Recargar")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->

        when (val state = uiState) {

            is ReagendamientoUiState.Loading -> {
                Box(
                    modifier         = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CircularProgressIndicator()
                        Text(
                            "Cargando solicitudes…",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            is ReagendamientoUiState.Error -> {
                Box(
                    modifier         = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        modifier            = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = MaterialTheme.colorScheme.errorContainer
                        ) {
                            Icon(
                                Icons.Default.CloudOff, null,
                                modifier = Modifier.padding(16.dp).size(32.dp),
                                tint     = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                        Text(
                            text      = state.mensaje,
                            color     = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center,
                            style     = MaterialTheme.typography.bodyMedium
                        )
                        Button(
                            onClick = { viewModel.cargar() },
                            shape   = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Refresh, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Reintentar")
                        }
                    }
                }
            }

            is ReagendamientoUiState.Success -> {
                if (state.pendientes.isEmpty()) {
                    Box(
                        modifier         = Modifier.fillMaxSize().padding(padding),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            modifier            = Modifier.padding(40.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(24.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Icon(
                                    Icons.Default.EditCalendar, null,
                                    modifier = Modifier.padding(20.dp).size(48.dp),
                                    tint     = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text       = "Sin solicitudes pendientes",
                                style      = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text      = "Cuando los clientes soliciten un cambio de fecha, aparecerán aquí.",
                                style     = MaterialTheme.typography.bodyMedium,
                                color     = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier            = Modifier.fillMaxSize().padding(padding),
                        contentPadding      = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(state.pendientes) { cita ->
                            CitaPendienteCard(
                                cita       = cita,
                                viewModel  = viewModel,            // ← NUEVO
                                isLoading  = actionState is ReagendamientoActionState.Loading,
                                onAprobar  = { nuevaInicio, nuevaFin ->
                                    viewModel.aprobar(cita.id, nuevaInicio, nuevaFin)
                                },
                                onRechazar = { motivo ->
                                    viewModel.rechazar(cita.id, motivo)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

// ─── Card de cita pendiente ───────────────────────────────────────────────────

@Composable
private fun CitaPendienteCard(
    cita:      CitaPendiente,
    viewModel:  ReagendamientoViewModel,
    isLoading: Boolean,
    onAprobar: (String, String) -> Unit,
    onRechazar: (String) -> Unit
) {
    var showAprobarDialog  by remember { mutableStateOf(false) }
    var showRechazarDialog by remember { mutableStateOf(false) }

    val formatter   = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
    val fechaActual = try {
        val raw = cita.fechaHoraInicio.substring(0, 16).replace("T", " ")
        LocalDate.parse(raw.substring(0, 10))
            .atTime(LocalTime.parse(raw.substring(11, 16)))
            .format(formatter)
    } catch (e: Exception) { cita.fechaHoraInicio }

    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors    = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {

            // ── Encabezado: cliente + profesional ─────────────────────────
            Row(
                modifier          = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text       = cita.cliente.firstOrNull()?.uppercase() ?: "?",
                            fontWeight = FontWeight.Bold,
                            style      = MaterialTheme.typography.titleSmall,
                            color      = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(
                        text       = cita.cliente,
                        fontWeight = FontWeight.Bold,
                        style      = MaterialTheme.typography.bodyMedium
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Badge, null,
                            modifier = Modifier.size(11.dp),
                            tint     = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.width(3.dp))
                        Text(
                            text  = cita.profesional,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(Modifier.height(10.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(Modifier.height(10.dp))

            // ── Fecha actual ──────────────────────────────────────────────
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.CalendarToday, null,
                    modifier = Modifier.size(14.dp),
                    tint     = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text  = "Fecha actual: $fechaActual",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // ── Servicios ─────────────────────────────────────────────────
            cita.servicios?.let {
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Spa, null,
                        modifier = Modifier.size(14.dp),
                        tint     = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text  = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // ── Motivo solicitud ──────────────────────────────────────────
            cita.motivoSolicitud?.let { motivo ->
                Spacer(Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Row(
                        modifier          = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            Icons.Default.ChatBubbleOutline, null,
                            modifier = Modifier.size(13.dp).padding(top = 1.dp),
                            tint     = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text  = motivo,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(Modifier.height(10.dp))

            // ── Botones de acción ─────────────────────────────────────────
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick  = { showRechazarDialog = true },
                    modifier = Modifier.weight(1f),
                    enabled  = !isLoading,
                    shape    = RoundedCornerShape(10.dp),
                    colors   = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                    border = BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
                    )
                ) {
                    Icon(Icons.Default.Close, null, modifier = Modifier.size(15.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Rechazar", style = MaterialTheme.typography.labelMedium)
                }

                Button(
                    onClick  = { showAprobarDialog = true },
                    modifier = Modifier.weight(1f),
                    enabled  = !isLoading,
                    shape    = RoundedCornerShape(10.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier    = Modifier.size(15.dp),
                            strokeWidth = 2.dp,
                            color       = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Icon(Icons.Default.Check, null, modifier = Modifier.size(15.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Aprobar", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
    }

    if (showAprobarDialog) {
        AprobarConDisponibilidadDialog(
            cita      = cita,
            viewModel = viewModel,
            onConfirm = { nuevaInicio, nuevaFin ->
                showAprobarDialog = false
                onAprobar(nuevaInicio, nuevaFin)
            },
            onDismiss = {
                showAprobarDialog = false
                viewModel.resetDisponibilidad()
            }
        )
    }

    if (showRechazarDialog) {
        RechazarDialog(
            onConfirm = { motivo ->
                showRechazarDialog = false
                onRechazar(motivo)
            },
            onDismiss = { showRechazarDialog = false }
        )
    }
}

// ─── Diálogo: Aprobar (2 pasos) ───────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AprobarConDisponibilidadDialog(
    cita:      CitaPendiente,
    viewModel: ReagendamientoViewModel,
    onConfirm: (String, String) -> Unit,
    onDismiss: () -> Unit
) {
    val dispState         by viewModel.dispState.collectAsState()
    val fechaSeleccionada by viewModel.fechaDialog.collectAsState()
    val slotSeleccionado  by viewModel.slotDialog.collectAsState()
    val duracionMin = calcularDuracion(cita.fechaHoraInicio, cita.fechaHoraFin)

    // Extraer idProfesional e idSede de CitaPendiente
    // (asegúrate de que CitaPendiente tenga estos campos; ver nota abajo)
    LaunchedEffect(Unit) {
        viewModel.cargarSlotsParaReagendar(
            idProfesional = cita.idProfesional,
            idSede        = cita.idSede,
            duracionMin   = duracionMin
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(16.dp),
        icon = {
            Surface(
                shape = RoundedCornerShape(50),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Icon(
                    Icons.Default.EditCalendar, null,
                    tint     = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(10.dp).size(22.dp)
                )
            }
        },
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Aprobar reagendamiento", fontWeight = FontWeight.Bold)
                Text(
                    text  = cita.cliente,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(
                modifier            = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // ── Selector de fecha (30 días) ────────────────────────────
                Text(
                    text       = "Selecciona una fecha",
                    style      = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )

                val dias = (0..29).map { LocalDate.now().plusDays(it.toLong()) }

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    contentPadding        = PaddingValues(vertical = 4.dp)
                ) {
                    items(dias) { dia ->
                        val seleccionado = dia == fechaSeleccionada
                        Surface(
                            shape    = RoundedCornerShape(10.dp),
                            color    = if (seleccionado) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.clickable {
                                viewModel.cargarSlotsParaReagendar(
                                    idProfesional = cita.idProfesional,
                                    idSede        = cita.idSede,
                                    duracionMin   = duracionMin,
                                    fecha         = dia
                                )
                            }
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier            = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text     = dia.dayOfWeek
                                        .getDisplayName(java.time.format.TextStyle.SHORT, java.util.Locale("es")),
                                    fontSize = 9.sp,
                                    color    = if (seleccionado) MaterialTheme.colorScheme.onPrimary
                                    else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text       = dia.dayOfMonth.toString(),
                                    fontWeight = FontWeight.Bold,
                                    fontSize   = 16.sp,
                                    color      = if (seleccionado) MaterialTheme.colorScheme.onPrimary
                                    else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                HorizontalDivider()

                // ── Slots ──────────────────────────────────────────────────
                when (val state = dispState) {
                    is DisponibilidadDialogState.Loading -> {
                        Box(
                            modifier         = Modifier.fillMaxWidth().height(80.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        }
                    }

                    is DisponibilidadDialogState.Loaded -> {
                        val disponibles = state.slots.filter { it.disponible }

                        if (disponibles.isEmpty()) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Row(
                                    modifier          = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.EventBusy, null,
                                        modifier = Modifier.size(16.dp),
                                        tint     = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        "Sin horarios disponibles para este día.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        } else {
                            Text(
                                text  = "${disponibles.size} horarios disponibles",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            disponibles.chunked(3).forEach { fila ->
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier              = Modifier.fillMaxWidth()
                                ) {
                                    fila.forEach { slot ->
                                        val sel = slot == slotSeleccionado
                                        Surface(
                                            shape    = RoundedCornerShape(8.dp),
                                            color    = if (sel) MaterialTheme.colorScheme.primary
                                            else MaterialTheme.colorScheme.surfaceVariant,
                                            modifier = Modifier
                                                .weight(1f)
                                                .clickable { viewModel.seleccionarSlotDialog(slot) }
                                        ) {
                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                modifier            = Modifier.padding(vertical = 10.dp)
                                            ) {
                                                Text(
                                                    text       = slot.horaInicio.substring(0, 5),
                                                    fontSize   = 13.sp,
                                                    fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal,
                                                    color      = if (sel) MaterialTheme.colorScheme.onPrimary
                                                    else MaterialTheme.colorScheme.onSurface
                                                )
                                                if (sel) Icon(
                                                    Icons.Default.Check, null,
                                                    modifier = Modifier.size(11.dp),
                                                    tint     = MaterialTheme.colorScheme.onPrimary
                                                )
                                            }
                                        }
                                    }
                                    repeat(3 - fila.size) { Spacer(Modifier.weight(1f)) }
                                }
                            }
                        }
                    }

                    is DisponibilidadDialogState.Error -> {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.errorContainer
                        ) {
                            Text(
                                text     = state.mensaje,
                                color    = MaterialTheme.colorScheme.onErrorContainer,
                                style    = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }

                    else -> Unit
                }
            }
        },
        confirmButton = {
            Button(
                onClick  = {
                    val slot  = slotSeleccionado ?: return@Button
                    val fecha = fechaSeleccionada
                    val fmt   = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
                    val inicio = java.time.LocalDateTime.parse(
                        "${fecha} ${slot.horaInicio}",
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                    )
                    val fin = inicio.plusMinutes(duracionMin.toLong())
                    onConfirm(inicio.format(fmt), fin.format(fmt))
                },
                enabled  = slotSeleccionado != null,
                shape    = RoundedCornerShape(10.dp)
            ) { Text("Confirmar") }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(10.dp)) {
                Text("Cancelar")
            }
        }
    )
}

// ─── Diálogo: Rechazar ────────────────────────────────────────────────────────

@Composable
private fun RechazarDialog(
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var motivo by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(16.dp),
        icon  = {
            Surface(
                shape = RoundedCornerShape(50),
                color = MaterialTheme.colorScheme.errorContainer
            ) {
                Icon(
                    Icons.Default.Cancel, null,
                    tint     = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.padding(10.dp).size(22.dp)
                )
            }
        },
        title = { Text("Rechazar solicitud", fontWeight = FontWeight.Bold) },
        text  = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text  = "Indica el motivo del rechazo. El cliente será notificado.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value         = motivo,
                    onValueChange = { motivo = it },
                    label         = { Text("Motivo del rechazo") },
                    modifier      = Modifier.fillMaxWidth(),
                    shape         = RoundedCornerShape(12.dp),
                    minLines      = 2,
                    isError       = motivo.isBlank()
                )
            }
        },
        confirmButton = {
            Button(
                onClick  = { if (motivo.isNotBlank()) onConfirm(motivo) },
                enabled  = motivo.isNotBlank(),
                shape    = RoundedCornerShape(10.dp),
                colors   = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) { Text("Rechazar") }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape   = RoundedCornerShape(10.dp)
            ) { Text("Cancelar") }
        }
    )
}

// ─── Helper ───────────────────────────────────────────────────────────────────

private fun calcularDuracion(inicio: String, fin: String): Int {
    return try {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
        val i = java.time.LocalDateTime.parse(inicio, formatter)
        val f = java.time.LocalDateTime.parse(fin, formatter)
        java.time.Duration.between(i, f).toMinutes().toInt().coerceAtLeast(30)
    } catch (e: Exception) { 30 }
}