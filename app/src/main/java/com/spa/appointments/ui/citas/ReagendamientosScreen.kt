package com.spa.appointments.ui.citas

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.spa.appointments.domain.model.CitaPendiente
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalLifecycleOwner


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReagendamientosScreen(
    onBack: () -> Unit,
    viewModel: ReagendamientoViewModel = hiltViewModel()
) {
    val uiState     by viewModel.uiState.collectAsState()
    val actionState by viewModel.actionState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Refresca la lista cada vez que el screen vuelve a estar activo (onResume)
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.cargar()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(actionState) {
        when (actionState) {
            is ReagendamientoActionState.Success ->
                snackbarHostState.showSnackbar("Acción realizada correctamente")
            is ReagendamientoActionState.Error ->
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
                title = { Text("Reagendamientos pendientes", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        when (uiState) {
            is ReagendamientoUiState.Loading -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is ReagendamientoUiState.Error -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Text(
                        (uiState as ReagendamientoUiState.Error).mensaje,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            is ReagendamientoUiState.Success -> {
                val pendientes = (uiState as ReagendamientoUiState.Success).pendientes
                if (pendientes.isEmpty()) {
                    Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                        Text(
                            "No hay reagendamientos pendientes",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(padding),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(pendientes) { cita ->
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


@Composable
private fun CitaPendienteCard(
    cita: CitaPendiente,
    isLoading: Boolean,
    onAprobar: (String, String) -> Unit,
    onRechazar: (String) -> Unit
) {
    var showAprobarDialog  by remember { mutableStateOf(false) }
    var showRechazarDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // Cliente y profesional
            Text(cita.cliente, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(
                "Prof: ${cita.profesional}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(6.dp))

            // Fecha actual de la cita
            val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
            val fechaActual = try {
                cita.fechaHoraInicio.substring(0, 16)
                    .replace("T", " ")
                    .let { LocalDate.parse(it.substring(0,10))
                        .atTime(LocalTime.parse(it.substring(11,16)))
                        .format(formatter) }
            } catch (e: Exception) { cita.fechaHoraInicio }

            Text("Fecha actual: $fechaActual", style = MaterialTheme.typography.bodyMedium)

            // Servicios
            cita.servicios?.let {
                Text("Servicios: $it", style = MaterialTheme.typography.bodySmall)
            }

            // Motivo solicitud
            cita.motivoSolicitud?.let {
                Spacer(Modifier.height(6.dp))
                Text(
                    "Motivo: $it",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(Modifier.height(12.dp))

            // Botones
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = { showRechazarDialog = true },
                    modifier = Modifier.weight(1f),
                    enabled = !isLoading,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp, MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(Icons.Default.Close, contentDescription = null,
                        modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Rechazar")
                }

                Button(
                    onClick = { showAprobarDialog = true },
                    modifier = Modifier.weight(1f),
                    enabled = !isLoading
                ) {
                    Icon(Icons.Default.Check, contentDescription = null,
                        modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Aprobar")
                }
            }
        }
    }

    // Diálogo aprobar — elige nueva fecha y hora
    if (showAprobarDialog) {
        AprobarDialog(
            duracionMin = calcularDuracion(cita.fechaHoraInicio, cita.fechaHoraFin),
            onConfirm = { nuevaInicio, nuevaFin ->
                showAprobarDialog = false
                onAprobar(nuevaInicio, nuevaFin)
            },
            onDismiss = { showAprobarDialog = false }
        )
    }

    // Diálogo rechazar — ingresa motivo
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AprobarDialog(
    duracionMin: Int,
    onConfirm: (String, String) -> Unit,
    onDismiss: () -> Unit
) {
    // Paso: 0 = seleccionar fecha, 1 = seleccionar hora
    var paso by remember { mutableIntStateOf(0) }

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
        // ── Paso 1: Fecha ────────────────────────────────────────────
        DatePickerDialog(
            onDismissRequest = onDismiss,
            confirmButton = {
                TextButton(onClick = {
                    if (datePickerState.selectedDateMillis == null) {
                        errorMsg = "Selecciona una fecha"
                    } else {
                        errorMsg = ""
                        paso = 1
                    }
                }) { Text("Siguiente") }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) { Text("Cancelar") }
            }
        ) {
            DatePicker(state = datePickerState)
            if (errorMsg.isNotEmpty()) {
                Text(
                    errorMsg,
                    color    = MaterialTheme.colorScheme.error,
                    style    = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
            }
        }
    } else {
        // ── Paso 2: Hora ─────────────────────────────────────────────
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Selecciona la hora") },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()) {
                    TimePicker(state = timePickerState)
                    if (errorMsg.isNotEmpty()) {
                        Text(
                            errorMsg,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val millis = datePickerState.selectedDateMillis ?: return@TextButton

                    // Construir LocalDateTime desde los selectores
                    val fecha  = java.time.Instant
                        .ofEpochMilli(millis)
                        .atZone(java.time.ZoneId.of("UTC"))
                        .toLocalDate()

                    val inicio = fecha.atTime(
                        timePickerState.hour,
                        timePickerState.minute
                    )

                    // Validar que sea en el futuro
                    if (inicio.isBefore(java.time.LocalDateTime.now())) {
                        errorMsg = "La fecha y hora deben ser futuras"
                        return@TextButton
                    }

                    val fin       = inicio.plusMinutes(duracionMin.toLong())
                    val isoFormat = DateTimeFormatter
                        .ofPattern("yyyy-MM-dd'T'HH:mm:ss")

                    onConfirm(inicio.format(isoFormat), fin.format(isoFormat))
                }) { Text("Confirmar") }
            },
            dismissButton = {
                TextButton(onClick = { paso = 0 }) { Text("Atrás") }
            }
        )
    }
}

private fun calcularDuracion(inicio: String, fin: String): Int {
    return try {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
        val i = java.time.LocalDateTime.parse(inicio, formatter)
        val f = java.time.LocalDateTime.parse(fin, formatter)
        java.time.Duration.between(i, f).toMinutes().toInt().coerceAtLeast(30)
    } catch (e: Exception) { 30 }
}

@Composable
private fun RechazarDialog(
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var motivo by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Rechazar reagendamiento") },
        text = {
            OutlinedTextField(
                value = motivo,
                onValueChange = { motivo = it },
                label = { Text("Motivo del rechazo") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )
        },
        confirmButton = {
            TextButton(
                onClick = { if (motivo.isNotBlank()) onConfirm(motivo) },
                enabled = motivo.isNotBlank()
            ) { Text("Rechazar", color = MaterialTheme.colorScheme.error) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}