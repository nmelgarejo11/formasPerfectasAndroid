// Ruta: app/src/main/java/com/spa/appointments/ui/admin/horarios/HorariosScreen.kt
package com.spa.appointments.ui.admin.horarios

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.spa.appointments.domain.model.BloqueoResponse
import com.spa.appointments.domain.model.ProfesionalAdmin
import com.spa.appointments.ui.admin.profesionales.ProfesionalesAdminViewModel

// ─── Screen Principal ─────────────────────────────────────────────────────────

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
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Column {
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
                            Icon(Icons.Default.CopyAll, "Copiar horario", tint = MaterialTheme.colorScheme.primary)
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
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator(
                            color       = MaterialTheme.colorScheme.primary,
                            trackColor  = MaterialTheme.colorScheme.primaryContainer,
                            strokeWidth = 3.dp,
                            modifier    = Modifier.size(36.dp)
                        )
                        Text(
                            "Cargando horarios…",
                            style = MaterialTheme.typography.bodyMedium,
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
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {

                    // ── Card: Horario semanal M3 ─────────────────────────────
                    OutlinedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape    = RoundedCornerShape(18.dp),
                        colors   = CardDefaults.outlinedCardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier          = Modifier.padding(bottom = 12.dp)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primaryContainer
                                ) {
                                    Icon(
                                        Icons.Default.CalendarMonth, null,
                                        modifier = Modifier.padding(8.dp).size(20.dp),
                                        tint     = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                                Spacer(Modifier.width(10.dp))
                                Text(
                                    text       = "Horario semanal",
                                    style      = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            HorizontalDivider(
                                color    = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
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
                                        modifier = Modifier.padding(vertical = 4.dp)
                                    )
                                }
                            }

                            Spacer(Modifier.height(16.dp))

                            Button(
                                onClick  = {
                                    viewModel.guardarHorario(idProfesional, idSedeSeleccionada)
                                },
                                enabled  = uiState !is HorariosUiState.Loading,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                shape    = RoundedCornerShape(12.dp)
                            ) {
                                if (uiState is HorariosUiState.Loading) {
                                    CircularProgressIndicator(
                                        modifier    = Modifier.size(18.dp),
                                        strokeWidth = 2.dp,
                                        color       = MaterialTheme.colorScheme.onPrimary
                                    )
                                } else {
                                    Icon(Icons.Default.Save, null, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text("Guardar horario", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    // ── Card: Bloqueos M3 ────────────────────────────────────
                    OutlinedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape    = RoundedCornerShape(18.dp),
                        colors   = CardDefaults.outlinedCardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier          = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.errorContainer
                                ) {
                                    Icon(
                                        Icons.Default.EventBusy, null,
                                        modifier = Modifier.padding(8.dp).size(20.dp),
                                        tint     = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                }
                                Spacer(Modifier.width(10.dp))
                                Text(
                                    text       = "Bloqueos y ausencias",
                                    style      = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier   = Modifier.weight(1f)
                                )
                                FilledTonalButton(
                                    onClick = { showBloqueoDialog = true },
                                    shape   = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Agregar", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                                }
                            }

                            HorizontalDivider(
                                color    = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                                modifier = Modifier.padding(vertical = 12.dp)
                            )

                            if (bloqueos.isEmpty()) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.surfaceContainerHigh
                                ) {
                                    Row(
                                        modifier          = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 14.dp, vertical = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Default.CheckCircleOutline, null,
                                            modifier = Modifier.size(18.dp),
                                            tint     = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(Modifier.width(10.dp))
                                        Text(
                                            text  = "Sin bloqueos activos para este profesional",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            } else {
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
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
            shape            = RoundedCornerShape(28.dp),
            containerColor   = MaterialTheme.colorScheme.surfaceContainerHigh,
            icon  = {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.errorContainer
                ) {
                    Icon(
                        Icons.Default.DeleteForever, null,
                        tint     = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(12.dp).size(26.dp)
                    )
                }
            },
            title = { Text("Eliminar bloqueo", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) },
            text  = {
                Text(
                    text      = "¿Eliminar el bloqueo \"${bloqueo.motivo}\"? Esta acción no se puede deshacer.",
                    style     = MaterialTheme.typography.bodyMedium,
                    color     = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.eliminarBloqueo(idProfesional, bloqueo.id)
                        showEliminarBloqueo = null
                    },
                    shape    = RoundedCornerShape(12.dp),
                    colors   = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Eliminar bloqueo", fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                OutlinedButton(
                    onClick  = { showEliminarBloqueo = null },
                    shape    = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
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
                style      = MaterialTheme.typography.titleMedium,
                fontWeight = if (dia.activo) FontWeight.Bold else FontWeight.Normal,
                color      = if (dia.activo) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier   = Modifier.weight(1f)
            )
            if (dia.activo) {
                FilledTonalButton(
                    onClick        = { showTimePicker = "inicio" },
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier       = Modifier.height(34.dp),
                    shape          = RoundedCornerShape(10.dp)
                ) {
                    Text(formatearHora12h(dia.horaInicio), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                }
                Text(
                    " – ",
                    style    = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
                FilledTonalButton(
                    onClick        = { showTimePicker = "fin" },
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier       = Modifier.height(34.dp),
                    shape          = RoundedCornerShape(10.dp)
                ) {
                    Text(formatearHora12h(dia.horaFin), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
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
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.errorContainer,
            modifier = Modifier.size(38.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Default.Block, null,
                    modifier = Modifier.size(18.dp),
                    tint     = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text       = bloqueo.motivo,
                style      = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color      = MaterialTheme.colorScheme.onSurface
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.DateRange, null,
                    modifier = Modifier.size(12.dp),
                    tint     = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.width(4.dp))
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
    } catch (_: Exception) {
        horaStr.take(5)
    }
}
