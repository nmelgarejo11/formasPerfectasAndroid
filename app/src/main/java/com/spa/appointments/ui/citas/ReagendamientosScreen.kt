// Ruta: app/src/main/java/com/spa/appointments/ui/citas/ReagendamientosScreen.kt
package com.spa.appointments.ui.citas

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
                                cita      = cita,
                                isLoading = actionState is ReagendamientoActionState.Loading,
                                onAprobar = { nuevaInicio, nuevaFin ->
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
        AprobarDialog(
            duracionMin = calcularDuracion(cita.fechaHoraInicio, cita.fechaHoraFin),
            onConfirm   = { nuevaInicio, nuevaFin ->
                showAprobarDialog = false
                onAprobar(nuevaInicio, nuevaFin)
            },
            onDismiss = { showAprobarDialog = false }
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
private fun AprobarDialog(
    duracionMin: Int,
    onConfirm:   (String, String) -> Unit,
    onDismiss:   () -> Unit
) {
    var paso            by remember { mutableIntStateOf(0) }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = System.currentTimeMillis()
    )
    val timePickerState = rememberTimePickerState(
        initialHour   = 9,
        initialMinute = 0,
        is24Hour      = true
    )
    var errorMsg by remember { mutableStateOf("") }

    if (paso == 0) {
        DatePickerDialog(
            onDismissRequest = onDismiss,
            confirmButton    = {
                Row(
                    modifier              = Modifier
                        .fillMaxWidth()
                        .padding(PaddingValues(start = 16.dp,end = 16.dp,bottom = 8.dp)),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    // Indicador de paso
                    Text(
                        text  = "Paso 1 de 2 · Fecha",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row {
                        TextButton(onClick = onDismiss) { Text("Cancelar") }
                        Spacer(Modifier.width(4.dp))
                        Button(
                            onClick = {
                                if (datePickerState.selectedDateMillis == null) {
                                    errorMsg = "Selecciona una fecha"
                                } else {
                                    errorMsg = ""
                                    paso = 1
                                }
                            },
                            shape = RoundedCornerShape(10.dp)
                        ) { Text("Siguiente →") }
                    }
                }
            },
            dismissButton = {}
        ) {
            DatePicker(state = datePickerState)
            if (errorMsg.isNotEmpty()) {
                Text(
                    text     = errorMsg,
                    color    = MaterialTheme.colorScheme.error,
                    style    = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp)
                )
            }
        }
    } else {
        AlertDialog(
            onDismissRequest = onDismiss,
            shape = RoundedCornerShape(16.dp),
            icon  = {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Icon(
                        Icons.Default.Schedule, null,
                        tint     = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(10.dp).size(22.dp)
                    )
                }
            },
            title = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Selecciona la hora", fontWeight = FontWeight.Bold)
                    Text(
                        text  = "Paso 2 de 2",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            text  = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier            = Modifier.fillMaxWidth()
                ) {
                    TimePicker(state = timePickerState)
                    if (errorMsg.isNotEmpty()) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text  = errorMsg,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val millis = datePickerState.selectedDateMillis ?: return@Button
                        val fecha  = java.time.Instant
                            .ofEpochMilli(millis)
                            .atZone(java.time.ZoneId.of("UTC"))
                            .toLocalDate()
                        val inicio = fecha.atTime(
                            timePickerState.hour,
                            timePickerState.minute
                        )
                        if (inicio.isBefore(java.time.LocalDateTime.now())) {
                            errorMsg = "La fecha y hora deben ser futuras"
                            return@Button
                        }
                        val fin       = inicio.plusMinutes(duracionMin.toLong())
                        val isoFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
                        onConfirm(inicio.format(isoFormat), fin.format(isoFormat))
                    },
                    shape = RoundedCornerShape(10.dp)
                ) { Text("Confirmar") }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { paso = 0; errorMsg = "" },
                    shape   = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.ArrowBack, null, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Atrás")
                }
            }
        )
    }
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