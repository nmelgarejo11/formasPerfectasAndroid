package com.spa.appointments.ui.citas

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.spa.appointments.R
import com.spa.appointments.domain.model.AsignarCitaGrupalRequest
import com.spa.appointments.domain.model.Cita
import com.spa.appointments.domain.model.EstadoCita
import com.spa.appointments.domain.model.MetodoPago
import com.spa.appointments.domain.model.MetodoPagoDetalle
import com.spa.appointments.domain.model.Profesional
import com.spa.appointments.domain.model.ServicioCita
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.time.LocalDate
import java.time.ZoneId
import java.util.Calendar
import java.util.TimeZone

// ─── Screen principal ─────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MisCitasScreen(
    onBack:               () -> Unit,
    onVerHistorial:       () -> Unit,
    onVerReagendamientos: () -> Unit,
    vm: MisCitasViewModel = hiltViewModel()
) {
    val uiState     by vm.uiState.collectAsState()
    val accionState by vm.accionState.collectAsState()
    val metodosPago by vm.metodosPago.collectAsState()
    val filtros     by vm.filtros.collectAsState()
    val estados     by vm.estados.collectAsState()
    val showFiltros by vm.mostrarFiltros.collectAsState()
    val context = LocalContext.current

    var filtrosTemp by remember { mutableStateOf(FiltrosMisCitas()) }

    var citaAccion             by remember { mutableStateOf<Cita?>(null) }
    var mostrarCancelar        by remember { mutableStateOf(false) }
    var mostrarReagendar       by remember { mutableStateOf(false) }
    var mostrarFinalizar       by remember { mutableStateOf(false) }
    var mostrarAsignar         by remember { mutableStateOf(false) }
    var motivoReagendar        by remember { mutableStateOf("") }
    var metodoPagoSeleccionado by remember { mutableStateOf<MetodoPago?>(null) }

    val snackbarHost = remember { SnackbarHostState() }
    val totalCitas   = (uiState as? MisCitasUiState.Success)?.citas?.size

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) vm.cargar()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(accionState) {
        when (val s = accionState) {
            is AccionUiState.Success -> { snackbarHost.showSnackbar(s.mensaje); vm.resetAccion() }
            is AccionUiState.Error   -> { snackbarHost.showSnackbar(s.mensaje); vm.resetAccion() }
            else -> Unit
        }
    }

    // ── Diálogo: Cancelar ────────────────────────────────────────────────────
    if (mostrarCancelar && citaAccion != null) {
        AlertDialog(
            onDismissRequest = { mostrarCancelar = false },
            shape            = RoundedCornerShape(20.dp),
            containerColor   = MaterialTheme.colorScheme.surface,
            icon = {
                Box(
                    modifier          = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.errorContainer),
                    contentAlignment  = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Cancel, null,
                        tint     = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.size(28.dp)
                    )
                }
            },
            title = {
                Text(
                    "Cancelar cita",
                    fontWeight = FontWeight.Bold,
                    textAlign  = TextAlign.Center,
                    modifier   = Modifier.fillMaxWidth()
                )
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text       = "¿Deseas cancelar la cita con ${citaAccion?.profesional}?",
                        style      = MaterialTheme.typography.bodyMedium,
                        color      = MaterialTheme.colorScheme.onSurface,
                        textAlign  = TextAlign.Center,
                        fontWeight = FontWeight.Medium
                    )
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)
                    ) {
                        Row(
                            modifier              = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment     = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                Icons.Default.Warning, null,
                                modifier = Modifier.size(14.dp),
                                tint     = MaterialTheme.colorScheme.error
                            )
                            Text(
                                text  = "Esta acción no se puede deshacer.",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick  = { mostrarCancelar = false; vm.cancelarCita(citaAccion!!.id) },
                    shape    = RoundedCornerShape(12.dp),
                    colors   = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Cancel, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Sí, cancelar cita", fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick  = { mostrarCancelar = false },
                    shape    = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Volver") }
            }
        )
    }

    // ── Diálogo: Reagendar ───────────────────────────────────────────────────
    if (mostrarReagendar && citaAccion != null) {
        AlertDialog(
            onDismissRequest = { mostrarReagendar = false },
            shape            = RoundedCornerShape(20.dp),
            containerColor   = MaterialTheme.colorScheme.surface,
            icon = {
                Box(
                    modifier         = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.EditCalendar, null,
                        tint     = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(28.dp)
                    )
                }
            },
            title = {
                Text(
                    "Solicitar reagendamiento",
                    fontWeight = FontWeight.Bold,
                    textAlign  = TextAlign.Center,
                    modifier   = Modifier.fillMaxWidth()
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f)
                    ) {
                        Row(
                            modifier              = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment     = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                Icons.Default.Info, null,
                                modifier = Modifier.size(14.dp),
                                tint     = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Text(
                                text  = "Tu solicitud será revisada por el negocio.",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                    OutlinedTextField(
                        value         = motivoReagendar,
                        onValueChange = { motivoReagendar = it },
                        label         = { Text("Motivo (opcional)") },
                        placeholder   = { Text("Ej: Tengo un compromiso…") },
                        modifier      = Modifier.fillMaxWidth(),
                        shape         = RoundedCornerShape(12.dp),
                        minLines      = 2,
                        maxLines      = 3
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick  = {
                        mostrarReagendar = false
                        vm.reagendarCita(citaAccion!!.id, motivoReagendar.ifBlank { null })
                        motivoReagendar = ""
                    },
                    shape    = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Send, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Enviar solicitud", fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick  = { mostrarReagendar = false; motivoReagendar = "" },
                    shape    = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Cancelar") }
            }
        )
    }

    // ── Diálogo: Finalizar ───────────────────────────────────────────────────
    if (mostrarFinalizar && citaAccion != null) {
        FinalizarCitaDialog(
            metodos = metodosPago,
            onConfirm = { idMetodo, idDetalle ->
                mostrarFinalizar = false
                vm.finalizarCita(
                    citaAccion!!.id,
                    idMetodo,
                    idDetalle
                )
            },
            onDismiss = {
                mostrarFinalizar = false
            }
        )
    }

    // ── Diálogo: Asignar Horario/Profesional Grupal ─────────────────────────
    if (mostrarAsignar && citaAccion != null) {
        AsignarCitaGrupalDialog(
            cita = citaAccion!!,
            onConfirm = { ids, fechaInicio, fechaFin ->
                mostrarAsignar = false
                vm.asignarCitaGrupal(citaAccion!!.id, ids, fechaInicio, fechaFin)
            },
            onDismiss = { mostrarAsignar = false }
        )
    }

    // ── Scaffold ─────────────────────────────────────────────────────────────
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text       = "Mis citas",
                            style      = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        AnimatedVisibility(visible = totalCitas != null) {
                            if (totalCitas != null) {
                                Text(
                                    text  = "$totalCitas ${if (totalCitas == 1) "cita activa" else "citas activas"}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Volver")
                    }
                },
                actions = {
                    if (filtros.activo) {
                        IconButton(onClick = { vm.limpiarFiltros() }) {
                            BadgedBox(badge = { Badge() }) {
                                Icon(Icons.Default.FilterAltOff, "Limpiar filtros")
                            }
                        }
                    }
                    IconButton(onClick = { filtrosTemp = filtros; vm.toggleFiltros() }) {
                        Icon(
                            imageVector        = if (filtros.activo) Icons.Default.FilterAlt else Icons.Default.FilterList,
                            contentDescription = "Filtrar",
                            tint               = if (filtros.activo) MaterialTheme.colorScheme.primary else LocalContentColor.current
                        )
                    }
                    var menuExpanded by remember { mutableStateOf(false) }
                    Box {
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(Icons.Default.MoreVert, "Más opciones")
                        }
                        DropdownMenu(
                            expanded         = menuExpanded,
                            onDismissRequest = { menuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text        = { Text("Reagendamientos") },
                                leadingIcon = { Icon(Icons.Default.EditCalendar, null) },
                                onClick     = { menuExpanded = false; onVerReagendamientos() }
                            )
                            DropdownMenuItem(
                                text        = { Text("Historial") },
                                leadingIcon = { Icon(Icons.Default.History, null) },
                                onClick     = { menuExpanded = false; onVerHistorial() }
                            )
                            HorizontalDivider()
                            DropdownMenuItem(
                                text        = { Text("Recargar") },
                                leadingIcon = { Icon(Icons.Default.Refresh, null) },
                                onClick     = { menuExpanded = false; vm.cargar() }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHost) }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {

            AnimatedVisibility(visible = showFiltros, enter = expandVertically() + fadeIn(), exit = shrinkVertically() + fadeOut()) {
                FiltrosMisCitasPanel(
                    filtros   = filtrosTemp,
                    estados   = estados,
                    onChange  = { filtrosTemp = it },
                    onAplicar = { vm.aplicarFiltros(filtrosTemp) },
                    onCerrar  = { vm.toggleFiltros() }
                )
            }

            AnimatedVisibility(visible = filtros.activo && !showFiltros, enter = expandVertically() + fadeIn(), exit = shrinkVertically() + fadeOut()) {
                Surface(color = MaterialTheme.colorScheme.secondaryContainer, modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.FilterAlt, null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSecondaryContainer)
                        Spacer(Modifier.width(6.dp))
                        Text("Filtros aplicados", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSecondaryContainer, modifier = Modifier.weight(1f))
                        TextButton(onClick = { vm.limpiarFiltros() }, contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)) {
                            Text("Limpiar", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSecondaryContainer)
                        }
                    }
                }
            }

            Box(modifier = Modifier.fillMaxSize()) {

                when (val state = uiState) {

                    is MisCitasUiState.Loading -> {
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            CircularProgressIndicator()
                            Text(
                                text = "Cargando citas…",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    is MisCitasUiState.Empty -> {
                        EmptyMisCitas(
                            conFiltros = filtros.activo,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }

                    is MisCitasUiState.Error -> {
                        Column(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.errorContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.CloudOff,
                                    contentDescription = null,
                                    modifier = Modifier.size(36.dp),
                                    tint = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                            Text(
                                text = "Sin conexión",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = state.mensaje,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.bodySmall
                            )
                            Button(
                                onClick = { vm.cargar() },
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(
                                    Icons.Default.Refresh,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text("Reintentar")
                            }
                        }
                    }

                    is MisCitasUiState.Success -> {
                        LazyColumn(
                            contentPadding = PaddingValues(
                                horizontal = 16.dp,
                                vertical = 12.dp
                            ),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(
                                state.citas,
                                key = { it.id }
                            ) { cita ->
                                CitaCard(
                                    cita = cita,
                                    onCancelar = {
                                        citaAccion = cita
                                        mostrarCancelar = true
                                    },
                                    onReagendar = {
                                        citaAccion = cita
                                        mostrarReagendar = true
                                    },
                                    onFinalizar = {
                                        citaAccion = cita
                                        mostrarFinalizar = true
                                    },
                                    onAsignar = {
                                        citaAccion = cita
                                        mostrarAsignar = true
                                    },
                                    onWhatsApp = {
                                        vm.abrirWhatsApp(cita.id, context)
                                    },
                                    accionCargando = accionState is AccionUiState.Loading
                                )
                            }
                        }
                    }
                    else -> Unit
                }
            }
        }
    }
}

// ─── Panel de filtros ─────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FiltrosMisCitasPanel(
    filtros:   FiltrosMisCitas,
    estados:   List<EstadoCita>,
    onChange:  (FiltrosMisCitas) -> Unit,
    onAplicar: () -> Unit,
    onCerrar:  () -> Unit
) {
    val context = LocalContext.current

    Surface(
        tonalElevation  = 4.dp,
        shadowElevation = 4.dp,
        shape           = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.FilterList, null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                    Text("Filtrar citas", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                }
                IconButton(onClick = onCerrar, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Close, "Cerrar", modifier = Modifier.size(18.dp))
                }
            }

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value         = filtros.nombreCliente ?: "",
                onValueChange = { onChange(filtros.copy(nombreCliente = it.ifBlank { null })) },
                label         = { Text("Buscar cliente") },
                leadingIcon   = { Icon(Icons.Default.Search, null) },
                trailingIcon  = {
                    if (!filtros.nombreCliente.isNullOrBlank()) {
                        IconButton(onClick = { onChange(filtros.copy(nombreCliente = null)) }) {
                            Icon(Icons.Default.Clear, "Limpiar")
                        }
                    }
                },
                singleLine = true,
                shape      = RoundedCornerShape(12.dp),
                modifier   = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(10.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MisCitasDatePicker(label = "Desde", value = filtros.fechaDesde, modifier = Modifier.weight(1f), context = context, onDate = { onChange(filtros.copy(fechaDesde = it)) })
                MisCitasDatePicker(label = "Hasta", value = filtros.fechaHasta, modifier = Modifier.weight(1f), context = context, onDate = { onChange(filtros.copy(fechaHasta = it)) })
            }

            Spacer(Modifier.height(10.dp))

            MisCitasEstadoDropdown(
                estados            = estados,
                seleccionadoId     = filtros.idEstado,
                seleccionadoNombre = filtros.nombreEstado,
                onSelect           = { id, nombre -> onChange(filtros.copy(idEstado = id, nombreEstado = nombre)) } // Nota: Asegúrate si tu modelo usa nombreEstado o fontNameEstado, se mantuvo la lógica original.
            )

            Spacer(Modifier.height(14.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedButton(onClick = onCerrar, shape = RoundedCornerShape(10.dp), modifier = Modifier.weight(1f)) { Text("Cancelar") }
                Button(onClick = onAplicar, shape = RoundedCornerShape(10.dp), modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Aplicar")
                }
            }
        }
    }
}

// ─── DatePicker ───────────────────────────────────────────────────────────────

@Composable
private fun MisCitasDatePicker(
    label:    String,
    value:    String?,
    modifier: Modifier = Modifier,
    context:  android.content.Context,
    onDate:   (String?) -> Unit
) {
    val calendar = remember { java.util.Calendar.getInstance() }

    OutlinedTextField(
        value         = value?.let { formatearFecha(it) } ?: "",
        onValueChange = {},
        readOnly      = true,
        label         = { Text(label) },
        shape         = RoundedCornerShape(12.dp),
        trailingIcon  = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = {
                    DatePickerDialog(
                        context,
                        { _, y, m, d -> onDate("%04d-%02d-%02d".format(y, m + 1, d)) },
                        calendar.get(java.util.Calendar.YEAR),
                        calendar.get(java.util.Calendar.MONTH),
                        calendar.get(java.util.Calendar.DAY_OF_MONTH)
                    ).show()
                }) { Icon(Icons.Default.CalendarMonth, "Seleccionar fecha") }
                if (value != null) {
                    IconButton(onClick = { onDate(null) }) {
                        Icon(Icons.Default.Clear, "Limpiar", modifier = Modifier.size(16.dp))
                    }
                }
            }
        },
        singleLine = true,
        modifier   = modifier
    )
}

// ─── Dropdown de estados ──────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MisCitasEstadoDropdown(
    estados:            List<EstadoCita>,
    seleccionadoId:     Int?,
    seleccionadoNombre: String?,
    onSelect:           (Int?, String?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
        OutlinedTextField(
            value         = seleccionadoNombre ?: "Todos los estados",
            onValueChange = {},
            readOnly      = true,
            label         = { Text("Estado") },
            shape         = RoundedCornerShape(12.dp),
            trailingIcon  = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier      = Modifier.fillMaxWidth().menuAnchor()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                text        = { Text("Todos los estados") },
                onClick     = { onSelect(null, null); expanded = false },
                leadingIcon = { if (seleccionadoId == null) Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp)) }
            )
            if (estados.isNotEmpty()) HorizontalDivider()
            estados.forEach { estado ->
                val colorEstado = remember(estado.color) {
                    runCatching { Color(android.graphics.Color.parseColor(estado.color)) }.getOrDefault(Color.Gray)
                }
                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(modifier = Modifier.size(8.dp), shape = CircleShape, color = colorEstado) {}
                            Spacer(Modifier.width(8.dp))
                            Text(estado.nombre)
                        }
                    },
                    onClick     = { onSelect(estado.id, estado.nombre); expanded = false },
                    leadingIcon = { if (seleccionadoId == estado.id) Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp)) }
                )
            }
        }
    }
}

// ─── CitaCard ─────────────────────────────────────────────────────────────────

@Composable
private fun CitaCard(
    cita:           Cita,
    onCancelar:     () -> Unit,
    onReagendar:    () -> Unit,
    onFinalizar:    () -> Unit,
    onAsignar:      () -> Unit,
    onWhatsApp:     () -> Unit,
    accionCargando: Boolean
) {
    val colorEstado = remember(cita.colorEstado) {
        runCatching {
            Color(android.graphics.Color.parseColor(cita.colorEstado ?: "#888888"))
        }.getOrDefault(Color.Gray)
    }

    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border    = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {

            // ── Barra lateral de color con altura mínima garantizada ───────
            Box(
                modifier = Modifier
                    .width(5.dp)
                    .defaultMinSize(minHeight = 80.dp)
                    .fillMaxHeight()
                    .background(colorEstado)
            )

            Column(modifier = Modifier.weight(1f).padding(horizontal = 14.dp, vertical = 12.dp)) {

                // ── Header: Estado + Fecha + WhatsApp (todo en un Row) ─────
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Surface(shape = RoundedCornerShape(6.dp), color = colorEstado.copy(alpha = 0.12f)) {
                        Text(
                            text       = cita.estado,
                            color      = colorEstado,
                            style      = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            modifier   = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CalendarToday, null, modifier = Modifier.size(11.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.width(3.dp))
                        Text(text = formatearFecha(cita.fechaHoraInicio), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.width(8.dp))

                        // ── WhatsApp IconButton compacto ──────────────────
                        Box(
                            modifier         = Modifier
                                .size(30.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF25D366).copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            IconButton(onClick = onWhatsApp, enabled = !accionCargando, modifier = Modifier.size(30.dp)) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_whatsapp),
                                    contentDescription = "WhatsApp",
                                    modifier = Modifier.size(16.dp),
                                    tint = Color.Unspecified
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(10.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Spacer(Modifier.height(10.dp))

                // ── Cliente ───────────────────────────────────────────────
                if (!cita.nombreCliente.isNullOrBlank()) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 6.dp)) {
                        Icon(Icons.Default.Person, null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(5.dp))
                        Text(text = cita.nombreCliente, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                    }
                }

                // ── Profesional ───────────────────────────────────────────
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 4.dp)) {
                    Icon(Icons.Default.Badge, null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.width(5.dp))
                    Text(
                        text       = buildString { append(cita.profesional); cita.cargoProfesional?.let { append(" · $it") } },
                        style      = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // ── Hora y sede ───────────────────────────────────────────
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 6.dp)) {
                    Icon(Icons.Default.Schedule, null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.width(5.dp))
                    Text(
                        text  = "${formatearHora(cita.fechaHoraInicio)} – ${formatearHora(cita.fechaHoraFin)} · ${cita.sede}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // ── Total ─────────────────────────────────────────────────
                Surface(shape = RoundedCornerShape(10.dp), color = MaterialTheme.colorScheme.primaryContainer, modifier = Modifier.padding(bottom = 4.dp)) {
                    Row(
                        modifier              = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 7.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Default.Payments, null, modifier = Modifier.size(13.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer)
                            Text("Total", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                        Text(
                            text       = "${"$%,.0f".format(cita.total)}",
                            style      = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color      = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                // ── Notas ─────────────────────────────────────────────────
                cita.notas?.let { nota ->
                    Spacer(Modifier.height(4.dp))
                    Surface(shape = RoundedCornerShape(10.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
                        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 7.dp), verticalAlignment = Alignment.Top) {
                            Icon(Icons.Default.Notes, null, modifier = Modifier.size(12.dp).padding(top = 1.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(Modifier.width(5.dp))
                            Text(text = nota, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                // ── Servicios (NUEVO) ─────────────────────────────────────
                val servicios = remember(cita.servicios) { parsearServicios(cita.servicios) }
                if (servicios.isNotEmpty()) {
                    Spacer(Modifier.height(6.dp))
                    Surface(shape = RoundedCornerShape(10.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
                        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Spa, null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.primary)
                                Spacer(Modifier.width(5.dp))
                                Text("Servicios", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                            Spacer(Modifier.height(6.dp))
                            servicios.forEach { s ->
                                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Text("• ${s.nombre} (${s.duracion} min)", style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                                    Text("${"$%,.0f".format(s.precio)}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }
                }

                // ── CantidadPersonas (NUEVO) ──────────────────────────────
                if (cita.cantidadPersonas != null && cita.cantidadPersonas > 0) {
                    Spacer(Modifier.height(4.dp))
                    Surface(shape = RoundedCornerShape(10.dp), color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)) {
                        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 7.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Group, null, modifier = Modifier.size(13.dp), tint = MaterialTheme.colorScheme.onSecondaryContainer)
                            Spacer(Modifier.width(6.dp))
                            Text("${cita.cantidadPersonas} personas", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSecondaryContainer, fontWeight = FontWeight.Medium)
                        }
                    }
                }

                // ── Zona de acciones agrupada: Programada=1 o Confirmada=2 ─
                if (cita.idEstado in listOf(1, 2)) {
                    Spacer(Modifier.height(12.dp))
                    Surface(
                        shape    = RoundedCornerShape(12.dp),
                        color    = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        border   = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(onClick = onReagendar, enabled = !accionCargando, modifier = Modifier.weight(1f), shape = RoundedCornerShape(10.dp)) {
                                    Icon(Icons.Default.EditCalendar, null, modifier = Modifier.size(15.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Reagendar", style = MaterialTheme.typography.labelMedium)
                                }
                                OutlinedButton(
                                    onClick  = onCancelar,
                                    enabled  = !accionCargando,
                                    modifier = Modifier.weight(1f),
                                    shape    = RoundedCornerShape(10.dp),
                                    colors   = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                                    border   = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f))
                                ) {
                                    if (accionCargando) {
                                        CircularProgressIndicator(modifier = Modifier.size(15.dp), strokeWidth = 2.dp)
                                    } else {
                                        Icon(Icons.Default.Cancel, null, modifier = Modifier.size(15.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text("Cancelar", style = MaterialTheme.typography.labelMedium)
                                    }
                                }
                            }
                            Button(
                                onClick  = onFinalizar,
                                enabled  = !accionCargando,
                                modifier = Modifier.fillMaxWidth().height(44.dp),
                                shape    = RoundedCornerShape(10.dp),
                                colors   = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                            ) {
                                Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Finalizar cita", fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }

                // ── Zona de acciones agrupada: Estado 8 (NUEVO) ───────────
                if (cita.idEstado == 8) {
                    Spacer(Modifier.height(12.dp))
                    Surface(
                        shape    = RoundedCornerShape(12.dp),
                        color    = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        border   = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFFFFF8E1)) {
                                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.HourglassTop, null, modifier = Modifier.size(13.dp), tint = Color(0xFFF57F17))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Pendiente asignar horario y profesional", style = MaterialTheme.typography.labelSmall, color = Color(0xFFF57F17))
                                }
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick  = onAsignar,
                                    enabled  = !accionCargando,
                                    modifier = Modifier.weight(1f),
                                    shape    = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Default.EditCalendar, null, modifier = Modifier.size(15.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Asignar", style = MaterialTheme.typography.labelMedium)
                                }
                                OutlinedButton(
                                    onClick  = onCancelar,
                                    enabled  = !accionCargando,
                                    modifier = Modifier.weight(1f),
                                    shape    = RoundedCornerShape(10.dp),
                                    colors   = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                                    border   = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f))
                                ) {
                                    Icon(Icons.Default.Cancel, null, modifier = Modifier.size(15.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Cancelar", style = MaterialTheme.typography.labelMedium)
                                }
                            }
                        }
                    }
                }

                // ── Pendiente de cambio ───────────────────────────────────
                if (cita.idEstado == 4) {
                    Spacer(Modifier.height(8.dp))
                    Surface(shape = RoundedCornerShape(10.dp), color = MaterialTheme.colorScheme.secondaryContainer) {
                        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.HourglassTop, null, modifier = Modifier.size(13.dp), tint = MaterialTheme.colorScheme.onSecondaryContainer)
                            Spacer(Modifier.width(6.dp))
                            Text("Solicitud de cambio en revisión", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSecondaryContainer)
                        }
                    }
                }
            }
        }
    }
}

// ─── Empty state ──────────────────────────────────────────────────────────────

@Composable
private fun EmptyMisCitas(
    conFiltros: Boolean,
    modifier:   Modifier = Modifier
) {
    Column(modifier = modifier.padding(40.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(
            modifier = Modifier.size(88.dp).clip(RoundedCornerShape(24.dp)).background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (conFiltros) Icons.Default.SearchOff else Icons.Default.CalendarMonth,
                contentDescription = null,
                modifier = Modifier.size(44.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(text = if (conFiltros) "Sin resultados" else "No hay citas activas", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(
            text = if (conFiltros) "Ninguna cita coincide con los filtros aplicados." else "Reserva tu primera cita desde el menú principal.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

// ─── Diálogo: FinalizarCitaDialog ─────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FinalizarCitaDialog(
    metodos:   List<MetodoPago>,
    onConfirm: (Int, Int?) -> Unit,
    onDismiss: () -> Unit
) {
    var seleccionado by remember { mutableStateOf<MetodoPago?>(null) }
    var detalleSeleccionado by remember { mutableStateOf<MetodoPagoDetalle?>(null) }
    var expanded by remember { mutableStateOf(false) }
    var expandedDetalle by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape            = RoundedCornerShape(20.dp),
        containerColor   = MaterialTheme.colorScheme.surface,
        icon = {
            Box(
                modifier         = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.tertiaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onTertiaryContainer,
                    modifier = Modifier.size(28.dp)
                )
            }
        },
        title = {
            Text(
                text       = "Finalizar cita",
                fontWeight = FontWeight.Bold,
                textAlign  = TextAlign.Center,
                modifier   = Modifier.fillMaxWidth()
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text  = "Selecciona el método de pago aplicado:",
                    style = MaterialTheme.typography.bodyMedium
                )
                // ── Selector principal ────────────────────────────────
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it }
                ) {
                    OutlinedTextField(
                        value      = seleccionado?.nombre ?: "Seleccionar...",
                        onValueChange = {},
                        readOnly   = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                        modifier   = Modifier.menuAnchor().fillMaxWidth(),
                        shape      = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        metodos.forEach { mp ->
                            DropdownMenuItem(
                                text = { Text(mp.nombre) },
                                onClick = {
                                    seleccionado = mp
                                    detalleSeleccionado = null
                                    expanded = false
                                }
                            )
                        }
                    }
                }
                // ── Selector detalle SOLO si es OTROS (ID = 8) ───────
                if (seleccionado?.nombre == "Otro") {
                    Text(
                        text  = "Especifique el método:",
                        style = MaterialTheme.typography.labelSmall
                    )
                    ExposedDropdownMenuBox(
                        expanded = expandedDetalle,
                        onExpandedChange = { expandedDetalle = it }
                    ) {
                        OutlinedTextField(
                            value      = detalleSeleccionado?.nombre ?: "Seleccionar detalle...",
                            onValueChange = {},
                            readOnly   = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expandedDetalle) },
                            modifier   = Modifier.menuAnchor().fillMaxWidth(),
                            shape      = RoundedCornerShape(12.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = expandedDetalle,
                            onDismissRequest = { expandedDetalle = false }
                        ) {
                            seleccionado?.detalles?.forEach { detalle ->
                                DropdownMenuItem(
                                    text = { Text(detalle.nombre) },
                                    onClick = {
                                        detalleSeleccionado = detalle
                                        expandedDetalle = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick  = { seleccionado?.let { onConfirm(it.id, detalleSeleccionado?.id) } },
                enabled  = seleccionado != null && (seleccionado?.id != 8 || detalleSeleccionado != null),
                shape    = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
                colors   = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text(text = "Confirmar y finalizar", fontWeight = FontWeight.SemiBold)
            }
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

// ─── Helpers de formato ───────────────────────────────────────────────────────

private fun formatearFecha(fechaIso: String): String = try {
    val p = fechaIso.substring(0, 10).split("-")
    "${p[2]}/${p[1]}/${p[0]}"
} catch (e: Exception) {
    fechaIso
}

private fun formatearHora(fechaIso: String): String = try {
    fechaIso.substring(11, 16)
} catch (e: Exception) {
    fechaIso
}

// ─── Helper parsearServicios (NUEVO) ──────────────────────────────────────────

private fun parsearServicios(json: String?): List<ServicioCita> {
    if (json.isNullOrBlank()) return emptyList()
    return try {
        val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
        val type = Types.newParameterizedType(List::class.java, ServicioCita::class.java)
        moshi.adapter<List<ServicioCita>>(type).fromJson(json) ?: emptyList()
    } catch (e: Exception) {
        emptyList()
    }
}

// ─── AsignarCitaGrupalDialog (NUEVO) ───────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AsignarCitaGrupalDialog(
    cita: Cita,
    onConfirm: (idsProfesionales: List<Int>?, fechaHoraInicio: String?, fechaHoraFin: String?) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val servicios = remember(cita.servicios) { parsearServicios(cita.servicios) }
    val duracion = servicios.firstOrNull()?.duracion ?: 60

    // 1. Calculamos el inicio del día de hoy directamente en UTC para el DatePicker
    val hoyUtcMillis = remember {
        Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            val local = Calendar.getInstance()
            clear()
            set(local.get(Calendar.YEAR), local.get(Calendar.MONTH), local.get(Calendar.DAY_OF_MONTH))
        }.timeInMillis
    }

    // 2. Bloqueo de días pasados
    val datePickerState = rememberDatePickerState(
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                return utcTimeMillis >= hoyUtcMillis
            }
        }
    )

    // 3. MEJORA: El reloj ahora abre por defecto en la hora y minutos actuales del sistema
    val ahoraInstante = remember { Calendar.getInstance() }
    val timePickerState = rememberTimePickerState(
        initialHour = ahoraInstante.get(Calendar.HOUR_OF_DAY),
        initialMinute = ahoraInstante.get(Calendar.MINUTE),
        is24Hour = false
    )

    var mostrarDatePicker by remember { mutableStateOf(false) }
    var mostrarTimePicker by remember { mutableStateOf(false) }

    var fechaSeleccionada by remember { mutableStateOf<String?>(null) }
    var horaSeleccionada by remember { mutableStateOf<String?>(null) }
    var profesionalesSel by remember { mutableStateOf<List<Int>>(emptyList()) }
    var mostrarSelProf by remember { mutableStateOf(false) }

    // Convertir la fecha seleccionada a String cuando cambie
    LaunchedEffect(datePickerState.selectedDateMillis) {
        datePickerState.selectedDateMillis?.let { millis ->
            val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply { timeInMillis = millis }
            fechaSeleccionada = "%04d-%02d-%02d".format(
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH) + 1,
                cal.get(Calendar.DAY_OF_MONTH)
            )
            horaSeleccionada = null // Resetea la hora para forzar a elegir una nueva válida
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(25.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Text(
                "Asignar Horario",
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Botón Selector de Fecha
                OutlinedButton(
                    onClick = { mostrarDatePicker = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.CalendarMonth, null)
                    Spacer(Modifier.width(8.dp))
                    Text(fechaSeleccionada?.let { formatearFecha(it) } ?: "Seleccionar Fecha")
                }

                // Botón Selector de Hora
                OutlinedButton(
                    onClick = { mostrarTimePicker = true },
                    enabled = fechaSeleccionada != null,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Schedule, null)
                    Spacer(Modifier.width(8.dp))
                    Text(horaSeleccionada ?: "Seleccionar Hora de Inicio")
                }

                // Botón Profesionales
                OutlinedButton(
                    onClick = { mostrarSelProf = true },
                    enabled = horaSeleccionada != null,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.People, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Profesionales (${profesionalesSel.size})")
                }

                if (mostrarSelProf) {
                    ProfesionalesSeleccionDialog(
                        sedeId = cita.idSede,
                        fecha = fechaSeleccionada!!,
                        hora = horaSeleccionada!!,
                        duracion = duracion,
                        seleccionados = profesionalesSel,
                        onDismiss = { mostrarSelProf = false },
                        onConfirm = {
                            profesionalesSel = it
                            mostrarSelProf = false
                        }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val inicioIso = "${fechaSeleccionada}T${horaSeleccionada}:00"
                    val calFin = Calendar.getInstance().apply {
                        val partesF = fechaSeleccionada!!.split("-")
                        val partesH = horaSeleccionada!!.split(":")
                        set(partesF[0].toInt(), partesF[1].toInt() - 1, partesF[2].toInt(), partesH[0].toInt(), partesH[1].toInt())
                        add(Calendar.MINUTE, duracion)
                    }
                    val finIso = "%04d-%02d-%02dT%02d:%02d:00".format(
                        calFin.get(Calendar.YEAR), calFin.get(Calendar.MONTH) + 1, calFin.get(Calendar.DAY_OF_MONTH),
                        calFin.get(Calendar.HOUR_OF_DAY), calFin.get(Calendar.MINUTE)
                    )
                    onConfirm(profesionalesSel.ifEmpty { null }, inicioIso, finIso)
                },
                enabled = fechaSeleccionada != null && horaSeleccionada != null,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Confirmar Asignación")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                Text("Cancelar")
            }
        }
    )

    // --- DIÁLOGO DE FECHA (M3) ---
    if (mostrarDatePicker) {
        DatePickerDialog(
            onDismissRequest = { mostrarDatePicker = false },
            confirmButton = {
                TextButton(onClick = { mostrarDatePicker = false }) { Text("Aceptar") }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDatePicker = false }) { Text("Cancelar") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // --- DIÁLOGO DE HORA CON CONTROL DE HORAS PASADAS ---
    if (mostrarTimePicker) {
        AlertDialog(
            onDismissRequest = { mostrarTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val horaElegida = timePickerState.hour
                    val minutoElegido = timePickerState.minute

                    // 1. Verificamos si la fecha seleccionada en el calendario es HOY
                    val esHoy = datePickerState.selectedDateMillis?.let {
                        val calSel = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply { timeInMillis = it }
                        val calActual = Calendar.getInstance()
                        calSel.get(Calendar.YEAR) == calActual.get(Calendar.YEAR) &&
                                calSel.get(Calendar.DAY_OF_YEAR) == calActual.get(Calendar.DAY_OF_YEAR)
                    } ?: false

                    // 2. Si es hoy, validamos que la hora/minuto no sean menores al tiempo actual
                    if (esHoy) {
                        val ahora = Calendar.getInstance()
                        val horaActual = ahora.get(Calendar.HOUR_OF_DAY)
                        val minutoActual = ahora.get(Calendar.MINUTE)

                        if (horaElegida < horaActual || (horaElegida == horaActual && minutoElegido < minutoActual)) {
                            Toast.makeText(context, "No puedes elegir una hora pasada", Toast.LENGTH_SHORT).show()
                            return@TextButton // Rompe la ejecución aquí, impidiendo que cierre el diálogo
                        }
                    }

                    // Si pasa el filtro (o es un día futuro), se guarda el dato exitosamente
                    horaSeleccionada = "%02d:%02d".format(horaElegida, minutoElegido)
                    mostrarTimePicker = false
                }) { Text("Aceptar") }
            },
            dismissButton = {
                TextButton(onClick = { mostrarTimePicker = false }) { Text("Cancelar") }
            },
            text = {
                TimePicker(state = timePickerState)
            }
        )
    }
}

// ─── ProfesionalesSeleccionDialog (NUEVO) ──────────────────────────────────

@Composable
private fun ProfesionalesSeleccionDialog(
    sedeId:        Int,
    fecha:         String,
    hora:          String,
    duracion:      Int,
    seleccionados: List<Int>,
    onDismiss:     () -> Unit,
    onConfirm:     (List<Int>) -> Unit,
    vm:            MisCitasViewModel = hiltViewModel()
) {
    var tempSel by remember { mutableStateOf(seleccionados) }
    val profesionales = vm.profesionales.collectAsState()

    LaunchedEffect(sedeId) {
        vm.cargarProfesionales(sedeId)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape            = RoundedCornerShape(20.dp),
        title            = { Text("Asignar profesionales", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) },
        confirmButton    = {},
        text = {
            Column {
                if (profesionales.value.isEmpty()) {
                    Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                        Text("Sin profesionales disponibles", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    profesionales.value.forEach { prof ->
                        val checked = prof.id in tempSel
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = checked,
                                onCheckedChange = {
                                    tempSel = if (it) tempSel + prof.id else tempSel - prof.id
                                }
                            )
                            Spacer(Modifier.width(8.dp))
                            Column {
                                Text(prof.nombreCompleto, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                                Text(prof.cargo, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))
                Button(
                    onClick  = { onConfirm(tempSel) },
                    modifier = Modifier.fillMaxWidth(),
                    shape    = RoundedCornerShape(12.dp)
                ) {
                    Text("Confirmar selección")
                }
            }
        }
    )
}