package com.spa.appointments.ui.admin.horarios

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.spa.appointments.domain.model.BloqueoResponse
import com.spa.appointments.domain.model.ProfesionalAdmin
import com.spa.appointments.ui.admin.profesionales.ProfesionalesAdminViewModel

// ─── Screen principal ─────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HorariosScreen(
    idProfesional: Int,
    profesional:   ProfesionalAdmin?,
    onBack:        () -> Unit,
    viewModel:     HorariosViewModel          = hiltViewModel(),
    profViewModel: ProfesionalesAdminViewModel = hiltViewModel()
) {
    val diasHorario by viewModel.diasHorario.collectAsState()
    val bloqueos    by viewModel.bloqueos.collectAsState()
    val uiState     by viewModel.uiState.collectAsState()

    var idSedeSeleccionada  by remember { mutableStateOf(1) }
    var showCopiarDialog    by remember { mutableStateOf(false) }
    var showBloqueoDialog   by remember { mutableStateOf(false) }
    var showEliminarBloqueo by remember { mutableStateOf<BloqueoResponse?>(null) }

    val profesionales    by profViewModel.profesionales.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(idProfesional) {
        viewModel.cargar(idProfesional)
        profViewModel.cargarDatos()
    }

    LaunchedEffect(uiState) {
        when (val s = uiState) {
            is HorariosUiState.Success -> {
                showCopiarDialog  = false
                showBloqueoDialog = false
                viewModel.resetState()
            }
            is HorariosUiState.Error -> {
                snackbarHostState.showSnackbar(s.mensaje)
                viewModel.resetState()
            }
            else -> Unit
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text       = "Horarios",
                            style      = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        if (profesional != null) {
                            Text(
                                text  = "${profesional.nombre} ${profesional.apellido}",
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
                    IconButton(onClick = { showCopiarDialog = true }) {
                        Icon(Icons.Default.CopyAll, "Copiar horario")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->

        when {
            uiState is HorariosUiState.Loading && diasHorario.isEmpty() -> {
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
                            "Cargando horarios…",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {

                    // ── Card: Horario semanal ─────────────────────────────
                    Card(
                        modifier  = Modifier.fillMaxWidth(),
                        shape     = RoundedCornerShape(14.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        border    = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier          = Modifier.padding(bottom = 12.dp)
                            ) {
                                Icon(
                                    Icons.Default.CalendarMonth, null,
                                    modifier = Modifier.size(18.dp),
                                    tint     = MaterialTheme.colorScheme.primary
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text       = "Horario semanal",
                                    style      = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            HorizontalDivider(
                                color    = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

                            diasHorario.forEach { dia ->
                                DiaHorarioRow(
                                    dia      = dia,
                                    onToggle = { viewModel.toggleDia(dia.diaSemana, it) },
                                    onHoras  = { inicio, fin ->
                                        viewModel.actualizarHora(dia.diaSemana, inicio, fin)
                                    }
                                )
                                if (dia != diasHorario.last()) {
                                    HorizontalDivider(
                                        color    = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                                        modifier = Modifier.padding(vertical = 2.dp)
                                    )
                                }
                            }

                            Spacer(Modifier.height(12.dp))

                            Button(
                                onClick  = {
                                    viewModel.guardarHorario(idProfesional, idSedeSeleccionada)
                                },
                                enabled  = uiState !is HorariosUiState.Loading,
                                modifier = Modifier.align(Alignment.End).height(40.dp),
                                shape    = RoundedCornerShape(10.dp)
                            ) {
                                if (uiState is HorariosUiState.Loading) {
                                    CircularProgressIndicator(
                                        modifier    = Modifier.size(16.dp),
                                        strokeWidth = 2.dp,
                                        color       = MaterialTheme.colorScheme.onPrimary
                                    )
                                } else {
                                    Icon(Icons.Default.Save, null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Guardar horario")
                                }
                            }
                        }
                    }

                    // ── Card: Bloqueos ────────────────────────────────────
                    Card(
                        modifier  = Modifier.fillMaxWidth(),
                        shape     = RoundedCornerShape(14.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        border    = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier          = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.EventBusy, null,
                                    modifier = Modifier.size(18.dp),
                                    tint     = MaterialTheme.colorScheme.primary
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text       = "Bloqueos",
                                    style      = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    modifier   = Modifier.weight(1f)
                                )
                                FilledTonalButton(
                                    onClick = { showBloqueoDialog = true },
                                    shape   = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Default.Add, null, modifier = Modifier.size(15.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Agregar", style = MaterialTheme.typography.labelMedium)
                                }
                            }

                            HorizontalDivider(
                                color    = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                modifier = Modifier.padding(vertical = 10.dp)
                            )

                            if (bloqueos.isEmpty()) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant
                                ) {
                                    Row(
                                        modifier          = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 12.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Default.CheckCircleOutline, null,
                                            modifier = Modifier.size(16.dp),
                                            tint     = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text(
                                            text  = "Sin bloqueos activos",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            } else {
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    bloqueos.forEachIndexed { index, bloqueo ->
                                        BloqueoRow(
                                            bloqueo    = bloqueo,
                                            onEliminar = { showEliminarBloqueo = bloqueo }
                                        )
                                        if (index < bloqueos.lastIndex) {
                                            HorizontalDivider(
                                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }

    // ── Diálogos ──────────────────────────────────────────────────────────────

    if (showCopiarDialog) {
        CopiarHorarioDialog(
            idProfesionalDestino = idProfesional,
            guardando            = uiState is HorariosUiState.Loading,
            profesionales        = profesionales,
            onCopiar             = { idOrigen ->
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
            shape = RoundedCornerShape(16.dp),
            icon  = {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.errorContainer
                ) {
                    Icon(
                        Icons.Default.DeleteForever, null,
                        tint     = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(10.dp).size(22.dp)
                    )
                }
            },
            title = { Text("Eliminar bloqueo", fontWeight = FontWeight.Bold) },
            text  = {
                Text(
                    text  = "¿Eliminar el bloqueo \"${bloqueo.motivo}\"? Esta acción no se puede deshacer.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.eliminarBloqueo(idProfesional, bloqueo.id)
                        showEliminarBloqueo = null
                    },
                    shape  = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) { Text("Eliminar") }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showEliminarBloqueo = null },
                    shape   = RoundedCornerShape(10.dp)
                ) { Text("Cancelar") }
            }
        )
    }
}

// ─── Fila de día ──────────────────────────────────────────────────────────────

@Composable
private fun DiaHorarioRow(
    dia:      DiaHorario,
    onToggle: (Boolean) -> Unit,
    onHoras:  (inicio: String, fin: String) -> Unit
) {
    var showTimePicker by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier          = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked         = dia.activo,
                onCheckedChange = onToggle
            )
            Text(
                text       = dia.nombreDia,
                style      = MaterialTheme.typography.bodyMedium,
                fontWeight = if (dia.activo) FontWeight.SemiBold else FontWeight.Normal,
                modifier   = Modifier.weight(1f)
            )
            if (dia.activo) {
                FilledTonalButton(
                    onClick        = { showTimePicker = "inicio" },
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier       = Modifier.height(32.dp),
                    shape          = RoundedCornerShape(8.dp)
                ) {
                    Text(dia.horaInicio, style = MaterialTheme.typography.labelMedium)
                }
                Text(
                    " – ",
                    style    = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(horizontal = 2.dp)
                )
                FilledTonalButton(
                    onClick        = { showTimePicker = "fin" },
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier       = Modifier.height(32.dp),
                    shape          = RoundedCornerShape(8.dp)
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
            onDismiss = { showTimePicker = null }
        )
    }
}

// ─── Fila de bloqueo ──────────────────────────────────────────────────────────

@Composable
private fun BloqueoRow(
    bloqueo:    BloqueoResponse,
    onEliminar: () -> Unit
) {
    Row(
        modifier          = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f),
            modifier = Modifier.size(36.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Default.Block, null,
                    modifier = Modifier.size(16.dp),
                    tint     = MaterialTheme.colorScheme.error
                )
            }
        }

        Spacer(Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text       = bloqueo.motivo,
                style      = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.DateRange, null,
                    modifier = Modifier.size(11.dp),
                    tint     = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.width(3.dp))
                Text(
                    text  = "${bloqueo.fechaInicio.take(10)} → ${bloqueo.fechaFin.take(10)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        IconButton(
            onClick  = onEliminar,
            modifier = Modifier.size(36.dp)
        ) {
            Icon(
                Icons.Default.DeleteOutline, "Eliminar",
                modifier = Modifier.size(18.dp),
                tint     = MaterialTheme.colorScheme.error
            )
        }
    }
}