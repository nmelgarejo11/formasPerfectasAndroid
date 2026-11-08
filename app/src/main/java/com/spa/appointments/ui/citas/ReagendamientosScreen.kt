// Ruta: app/src/main/java/com/spa/appointments/ui/citas/ReagendamientosScreen.kt
package com.spa.appointments.ui.citas

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import java.util.Locale

// ─── Screen Principal (Stateful) ──────────────────────────────────────────────

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
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text       = "Reagendamientos",
                                style      = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            if (totalPendientes != null) {
                                Surface(
                                    shape    = RoundedCornerShape(50),
                                    color    = MaterialTheme.colorScheme.primaryContainer,
                                    modifier = Modifier.padding(top = 2.dp)
                                ) {
                                    Text(
                                        text     = "$totalPendientes ${if (totalPendientes == 1) "pendiente" else "pendientes"}",
                                        style    = MaterialTheme.typography.labelSmall,
                                        color    = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 1.dp)
                                    )
                                }
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

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (val state = uiState) {

                is ReagendamientoUiState.Loading -> {
                    Column(
                        modifier            = Modifier.align(Alignment.Center),
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
                            "Cargando solicitudes…",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                is ReagendamientoUiState.Error -> {
                    OutlinedCard(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(24.dp)
                            .fillMaxWidth(),
                        shape  = RoundedCornerShape(24.dp),
                        colors = CardDefaults.outlinedCardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                        )
                    ) {
                        Column(
                            modifier            = Modifier
                                .padding(32.dp)
                                .fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.errorContainer
                            ) {
                                Icon(
                                    Icons.Default.CloudOff, null,
                                    modifier = Modifier.padding(16.dp).size(36.dp),
                                    tint     = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                            Text(
                                text       = "Error al cargar",
                                style      = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text      = state.mensaje,
                                color     = MaterialTheme.colorScheme.error,
                                textAlign = TextAlign.Center,
                                style     = MaterialTheme.typography.bodyMedium
                            )
                            Button(
                                onClick = { viewModel.cargar() },
                                shape   = RoundedCornerShape(12.dp)
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
                        val primaryColor = MaterialTheme.colorScheme.primary

                        OutlinedCard(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(24.dp)
                                .fillMaxWidth(),
                            shape  = RoundedCornerShape(24.dp),
                            colors = CardDefaults.outlinedCardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                            )
                        ) {
                            Column(
                                modifier            = Modifier
                                    .padding(32.dp)
                                    .fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                Box(
                                    modifier         = Modifier.size(80.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Canvas(modifier = Modifier.fillMaxSize()) {
                                        drawCircle(color = primaryColor.copy(alpha = 0.15f))
                                    }
                                    Surface(
                                        shape = CircleShape,
                                        color = MaterialTheme.colorScheme.surfaceContainerHigh
                                    ) {
                                        Icon(
                                            Icons.Default.EditCalendar, null,
                                            modifier = Modifier.padding(18.dp).size(36.dp),
                                            tint     = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
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
                            modifier            = Modifier.fillMaxSize(),
                            contentPadding      = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            items(state.pendientes) { cita ->
                                CitaPendienteCard(
                                    cita       = cita,
                                    viewModel  = viewModel,
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
}

// ─── Card de Cita Pendiente M3 ────────────────────────────────────────────────

@Composable
private fun CitaPendienteCard(
    cita:      CitaPendiente,
    viewModel: ReagendamientoViewModel,
    isLoading: Boolean,
    onAprobar: (String, String) -> Unit,
    onRechazar: (String) -> Unit
) {
    var showAprobarDialog  by remember { mutableStateOf(false) }
    var showRechazarDialog by remember { mutableStateOf(false) }

    val formatter   = DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm a", Locale.US)
    val fechaActual = try {
        val raw = cita.fechaHoraInicio.substring(0, 16).replace("T", " ")
        LocalDate.parse(raw.substring(0, 10))
            .atTime(LocalTime.parse(raw.substring(11, 16)))
            .format(formatter)
    } catch (e: Exception) { cita.fechaHoraInicio }

    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(18.dp),
        colors   = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // ── Encabezado: Cliente + Profesional ─────────────────────────
            Row(
                modifier          = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape    = CircleShape,
                    color    = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text       = cita.cliente.firstOrNull()?.uppercase() ?: "?",
                            fontWeight = FontWeight.Bold,
                            style      = MaterialTheme.typography.titleMedium,
                            color      = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text       = cita.cliente,
                        fontWeight = FontWeight.Bold,
                        style      = MaterialTheme.typography.bodyLarge,
                        color      = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Badge, null,
                            modifier = Modifier.size(13.dp),
                            tint     = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text  = cita.profesional,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(Modifier.height(10.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            Spacer(Modifier.height(10.dp))

            // ── Fecha Actual ──────────────────────────────────────────────
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
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Medium
                )
            }

            // ── Servicios ─────────────────────────────────────────────────
            cita.servicios?.let {
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Spa, null,
                        modifier = Modifier.size(14.dp),
                        tint     = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text  = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // ── Motivo Solicitud ──────────────────────────────────────────
            cita.motivoSolicitud?.let { motivo ->
                Spacer(Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.6f)
                ) {
                    Row(
                        modifier          = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            Icons.Default.ChatBubbleOutline, null,
                            modifier = Modifier.size(14.dp).padding(top = 2.dp),
                            tint     = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text  = motivo,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(Modifier.height(14.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            Spacer(Modifier.height(12.dp))

            // ── Botones de Acción M3 ──────────────────────────────────────
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick  = { showRechazarDialog = true },
                    modifier = Modifier.weight(1f),
                    enabled  = !isLoading,
                    shape    = RoundedCornerShape(12.dp),
                    colors   = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                    border = BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
                    )
                ) {
                    Icon(Icons.Default.Close, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Rechazar", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick  = { showAprobarDialog = true },
                    modifier = Modifier.weight(1f),
                    enabled  = !isLoading,
                    shape    = RoundedCornerShape(12.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier    = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color       = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Aprobar", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
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

// ─── Diálogo: Aprobar (2 Pasos) M3 ───────────────────────────────────────────

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

    LaunchedEffect(Unit) {
        viewModel.cargarSlotsParaReagendar(
            idProfesional = cita.idProfesional,
            idSede        = cita.idSede,
            duracionMin   = duracionMin
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape            = RoundedCornerShape(28.dp),
        containerColor   = MaterialTheme.colorScheme.surfaceContainerHigh,
        icon = {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Icon(
                    Icons.Default.EditCalendar, null,
                    tint     = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(12.dp).size(26.dp)
                )
            }
        },
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Aprobar Reagendamiento", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
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
                // ── Selector de Fecha (30 Días) ────────────────────────────
                Text(
                    text       = "Selecciona una fecha",
                    style      = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )

                val dias = (0..29).map { LocalDate.now().plusDays(it.toLong()) }

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding        = PaddingValues(vertical = 4.dp)
                ) {
                    items(dias) { dia ->
                        val seleccionado = dia == fechaSeleccionada
                        Surface(
                            shape  = RoundedCornerShape(12.dp),
                            color  = if (seleccionado) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerLow,
                            border = if (seleccionado) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
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
                                modifier            = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text     = dia.dayOfWeek.getDisplayName(java.time.format.TextStyle.SHORT, Locale("es")),
                                    fontSize = 10.sp,
                                    color    = if (seleccionado) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text       = dia.dayOfMonth.toString(),
                                    fontWeight = FontWeight.Bold,
                                    fontSize   = 16.sp,
                                    color      = if (seleccionado) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                // ── Slots ──────────────────────────────────────────────────
                when (val state = dispState) {
                    is DisponibilidadDialogState.Loading -> {
                        Box(
                            modifier         = Modifier.fillMaxWidth().height(80.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }

                    is DisponibilidadDialogState.Loaded -> {
                        val disponibles = state.slots.filter { it.disponible }

                        if (disponibles.isEmpty()) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceContainerLow
                            ) {
                                Row(
                                    modifier          = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.EventBusy, null,
                                        modifier = Modifier.size(18.dp),
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
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Text(
                                    text     = "${disponibles.size} horarios disponibles",
                                    style    = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color    = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }

                            disponibles.chunked(3).forEach { fila ->
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier              = Modifier.fillMaxWidth()
                                ) {
                                    fila.forEach { slot ->
                                        val sel = slot == slotSeleccionado
                                        Surface(
                                            shape  = RoundedCornerShape(10.dp),
                                            color  = if (sel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerLow,
                                            border = if (sel) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                                            modifier = Modifier
                                                .weight(1f)
                                                .clickable { viewModel.seleccionarSlotDialog(slot) }
                                        ) {
                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                modifier            = Modifier.padding(vertical = 10.dp)
                                            ) {
                                                Text(
                                                    text       = formatearHora12h(slot.horaInicio),
                                                    fontSize   = 12.sp,
                                                    fontWeight = if (sel) FontWeight.Bold else FontWeight.Medium,
                                                    color      = if (sel) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                                )
                                                if (sel) Icon(
                                                    Icons.Default.Check, null,
                                                    modifier = Modifier.size(12.dp),
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
                            shape = RoundedCornerShape(12.dp),
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
                shape    = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) { Text("Confirmar Aprobar Cita", fontWeight = FontWeight.SemiBold) }
        },
        dismissButton = {
            OutlinedButton(
                onClick  = onDismiss,
                shape    = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cancelar")
            }
        }
    )
}

// ─── Diálogo: Rechazar M3 ─────────────────────────────────────────────────────

@Composable
private fun RechazarDialog(
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var motivo by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape            = RoundedCornerShape(28.dp),
        containerColor   = MaterialTheme.colorScheme.surfaceContainerHigh,
        icon = {
            Box(
                modifier         = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.errorContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Cancel, null,
                    tint     = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.size(28.dp)
                )
            }
        },
        title = { Text("Rechazar Solicitud", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium) },
        text  = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
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
                    shape         = RoundedCornerShape(14.dp),
                    minLines      = 2,
                    isError       = motivo.isBlank(),
                    colors        = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor   = MaterialTheme.colorScheme.surfaceContainerLow,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick  = { if (motivo.isNotBlank()) onConfirm(motivo) },
                enabled  = motivo.isNotBlank(),
                shape    = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
                colors   = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) { Text("Rechazar Solicitud", fontWeight = FontWeight.SemiBold) }
        },
        dismissButton = {
            OutlinedButton(
                onClick  = onDismiss,
                shape    = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) { Text("Cancelar") }
        }
    )
}

// ─── Helpers de Formato ───────────────────────────────────────────────────────

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

private fun calcularDuracion(inicio: String, fin: String): Int {
    return try {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
        val i = java.time.LocalDateTime.parse(inicio, formatter)
        val f = java.time.LocalDateTime.parse(fin, formatter)
        java.time.Duration.between(i, f).toMinutes().toInt().coerceAtLeast(30)
    } catch (e: Exception) { 30 }
}
