package com.spa.appointments.ui.admin.horarios

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.spa.appointments.domain.model.BloqueoResponse
import com.spa.appointments.domain.model.ProfesionalAdmin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HorariosScreen(
    idProfesional: Int,
    profesional: ProfesionalAdmin?,
    onBack: () -> Unit,
    viewModel: HorariosViewModel = hiltViewModel()
) {
    val diasHorario by viewModel.diasHorario.collectAsState()
    val bloqueos    by viewModel.bloqueos.collectAsState()
    val uiState     by viewModel.uiState.collectAsState()

    var idSedeSeleccionada  by remember { mutableStateOf(1) }
    var showCopiarDialog    by remember { mutableStateOf(false) }
    var showBloqueoDialog   by remember { mutableStateOf(false) }
    var showEliminarBloqueo by remember { mutableStateOf<BloqueoResponse?>(null) }

    LaunchedEffect(idProfesional) { viewModel.cargar(idProfesional) }

    LaunchedEffect(uiState) {
        if (uiState is HorariosUiState.Success) {
            showCopiarDialog  = false
            showBloqueoDialog = false
            viewModel.resetState()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Horarios")
                        if (profesional != null) {
                            Text(
                                text = "${profesional.nombre} ${profesional.apellido}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    // Copiar horario
                    IconButton(onClick = { showCopiarDialog = true }) {
                        Icon(Icons.Default.CopyAll, contentDescription = "Copiar horario")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {

            when {
                uiState is HorariosUiState.Loading && diasHorario.all { !it.activo } -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                else -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {

                        // ─── HORARIO SEMANAL ───────────────────────────────
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Horario semanal",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(Modifier.height(12.dp))

                                diasHorario.forEach { dia ->
                                    DiaHorarioRow(
                                        dia      = dia,
                                        onToggle = { viewModel.toggleDia(dia.diaSemana, it) },
                                        onHoras  = { inicio, fin ->
                                            viewModel.actualizarHora(dia.diaSemana, inicio, fin)
                                        }
                                    )
                                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                                }

                                Spacer(Modifier.height(8.dp))
                                Button(
                                    onClick = {
                                        viewModel.guardarHorario(idProfesional, idSedeSeleccionada)
                                    },
                                    enabled = uiState !is HorariosUiState.Loading,
                                    modifier = Modifier.align(Alignment.End)
                                ) {
                                    Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Guardar horario")
                                }
                            }
                        }

                        // ─── BLOQUEOS ─────────────────────────────────────
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Bloqueos",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.weight(1f)
                                    )
                                    FilledTonalButton(onClick = { showBloqueoDialog = true }) {
                                        Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text("Agregar")
                                    }
                                }

                                Spacer(Modifier.height(8.dp))

                                if (bloqueos.isEmpty()) {
                                    Text(
                                        text = "Sin bloqueos activos",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                } else {
                                    bloqueos.forEach { bloqueo ->
                                        BloqueoRow(
                                            bloqueo   = bloqueo,
                                            onEliminar = { showEliminarBloqueo = bloqueo }
                                        )
                                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(24.dp))
                    }
                }
            }

            if (uiState is HorariosUiState.Error) {
                Snackbar(
                    modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp)
                ) {
                    Text((uiState as HorariosUiState.Error).mensaje)
                }
            }
        }
    }

    // ─── Dialogs ──────────────────────────────────────────────

    if (showCopiarDialog) {
        CopiarHorarioDialog(
            idProfesionalDestino = idProfesional,
            guardando = uiState is HorariosUiState.Loading,
            onCopiar  = { idOrigen ->
                viewModel.copiarHorario(idProfesional, idOrigen, idSedeSeleccionada)
            },
            onDismiss = { showCopiarDialog = false }
        )
    }

    if (showBloqueoDialog) {
        BloqueoDialog(
            guardando = uiState is HorariosUiState.Loading,
            onGuardar = { fechaInicio, fechaFin, motivo ->
                viewModel.crearBloqueo(idProfesional, fechaInicio, fechaFin, motivo)
            },
            onDismiss = { showBloqueoDialog = false }
        )
    }

    showEliminarBloqueo?.let { bloqueo ->
        AlertDialog(
            onDismissRequest = { showEliminarBloqueo = null },
            title = { Text("Eliminar bloqueo") },
            text  = { Text("¿Eliminar el bloqueo \"${bloqueo.motivo}\"?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.eliminarBloqueo(idProfesional, bloqueo.id)
                        showEliminarBloqueo = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) { Text("Eliminar") }
            },
            dismissButton = {
                TextButton(onClick = { showEliminarBloqueo = null }) { Text("Cancelar") }
            }
        )
    }
}

// ─── Componentes ──────────────────────────────────────────────

@Composable
private fun DiaHorarioRow(
    dia: DiaHorario,
    onToggle: (Boolean) -> Unit,
    onHoras: (inicio: String, fin: String) -> Unit
) {
    var showTimePicker by remember { mutableStateOf<String?>(null) } // "inicio" o "fin"

    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(checked = dia.activo, onCheckedChange = onToggle)
            Text(
                text = dia.nombreDia,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (dia.activo) FontWeight.SemiBold else FontWeight.Normal,
                modifier = Modifier.weight(1f)
            )
            if (dia.activo) {
                // Hora inicio
                FilledTonalButton(
                    onClick = { showTimePicker = "inicio" },
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text(dia.horaInicio, style = MaterialTheme.typography.labelMedium)
                }
                Text(" – ", style = MaterialTheme.typography.bodySmall)
                // Hora fin
                FilledTonalButton(
                    onClick = { showTimePicker = "fin" },
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text(dia.horaFin, style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }

    if (showTimePicker != null) {
        HoraPickerDialog(
            horaActual = if (showTimePicker == "inicio") dia.horaInicio else dia.horaFin,
            titulo     = if (showTimePicker == "inicio") "Hora de inicio" else "Hora de fin",
            onConfirm  = { hora ->
                if (showTimePicker == "inicio") onHoras(hora, dia.horaFin)
                else onHoras(dia.horaInicio, hora)
                showTimePicker = null
            },
            onDismiss  = { showTimePicker = null }
        )
    }
}

@Composable
private fun BloqueoRow(
    bloqueo: BloqueoResponse,
    onEliminar: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(bloqueo.motivo, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            Text(
                text = "${bloqueo.fechaInicio.take(10)}  →  ${bloqueo.fechaFin.take(10)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        IconButton(onClick = onEliminar) {
            Icon(
                Icons.Default.DeleteOutline,
                contentDescription = "Eliminar",
                tint = MaterialTheme.colorScheme.error
            )
        }
    }
}