package com.spa.appointments.ui.citas

import android.app.DatePickerDialog
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.spa.appointments.domain.model.Cita
import com.spa.appointments.domain.model.EstadoCita
import com.spa.appointments.domain.model.MetodoPago

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

    var filtrosTemp by remember { mutableStateOf(FiltrosMisCitas()) }

    var citaAccion             by remember { mutableStateOf<Cita?>(null) }
    var mostrarCancelar        by remember { mutableStateOf(false) }
    var mostrarReagendar       by remember { mutableStateOf(false) }
    var mostrarFinalizar       by remember { mutableStateOf(false) }
    var motivoReagendar        by remember { mutableStateOf("") }
    var metodoPagoSeleccionado by remember { mutableStateOf<MetodoPago?>(null) }

    val snackbarHost = remember { SnackbarHostState() }

    // Refresca al volver a la pantalla
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) vm.cargar()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // Reaccionar a resultados de acciones
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
            icon  = { Icon(Icons.Default.Cancel, null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Cancelar cita") },
            text  = { Text("¿Estás seguro que deseas cancelar la cita con ${citaAccion?.profesional}?") },
            confirmButton = {
                Button(
                    onClick = { mostrarCancelar = false; vm.cancelarCita(citaAccion!!.id) },
                    colors  = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Sí, cancelar") }
            },
            dismissButton = {
                OutlinedButton(onClick = { mostrarCancelar = false }) { Text("No, volver") }
            }
        )
    }

    // ── Diálogo: Reagendar ───────────────────────────────────────────────────
    if (mostrarReagendar && citaAccion != null) {
        AlertDialog(
            onDismissRequest = { mostrarReagendar = false },
            icon  = { Icon(Icons.Default.EditCalendar, null, tint = MaterialTheme.colorScheme.primary) },
            title = { Text("Solicitar reagendamiento") },
            text  = {
                Column {
                    Text(
                        text  = "Tu solicitud será revisada por el negocio. Te notificaremos cuando sea aprobada.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value         = motivoReagendar,
                        onValueChange = { motivoReagendar = it },
                        label         = { Text("Motivo (opcional)") },
                        placeholder   = { Text("Ej: Tengo un compromiso...") },
                        modifier      = Modifier.fillMaxWidth(),
                        minLines      = 2,
                        maxLines      = 3
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    mostrarReagendar = false
                    vm.reagendarCita(citaAccion!!.id, motivoReagendar.ifBlank { null })
                    motivoReagendar = ""
                }) { Text("Enviar solicitud") }
            },
            dismissButton = {
                OutlinedButton(onClick = { mostrarReagendar = false; motivoReagendar = "" }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // ── Diálogo: Finalizar ───────────────────────────────────────────────────
    if (mostrarFinalizar && citaAccion != null) {
        AlertDialog(
            onDismissRequest = { mostrarFinalizar = false; metodoPagoSeleccionado = null },
            icon  = { Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF607D8B)) },
            title = { Text("Finalizar cita") },
            text  = {
                Column {
                    Text(
                        text  = "Selecciona el método de pago utilizado por el cliente.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(12.dp))
                    if (metodosPago.isEmpty()) {
                        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        }
                    } else {
                        metodosPago.forEach { metodo ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier          = Modifier.fillMaxWidth().padding(vertical = 2.dp)
                            ) {
                                RadioButton(
                                    selected = metodoPagoSeleccionado?.id == metodo.id,
                                    onClick  = { metodoPagoSeleccionado = metodo }
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(metodo.nombre, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick  = {
                        mostrarFinalizar = false
                        vm.finalizarCita(citaAccion!!.id, metodoPagoSeleccionado!!.id)
                        metodoPagoSeleccionado = null
                    },
                    enabled  = metodoPagoSeleccionado != null,
                    colors   = ButtonDefaults.buttonColors(containerColor = Color(0xFF607D8B))
                ) { Text("Finalizar") }
            },
            dismissButton = {
                OutlinedButton(onClick = { mostrarFinalizar = false; metodoPagoSeleccionado = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // ── Scaffold ─────────────────────────────────────────────────────────────
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis citas") },
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
                    IconButton(onClick = {
                        filtrosTemp = filtros
                        vm.toggleFiltros()
                    }) {
                        Icon(
                            imageVector        = if (filtros.activo) Icons.Default.FilterAlt
                            else Icons.Default.FilterList,
                            contentDescription = "Filtrar"
                        )
                    }
                    IconButton(onClick = onVerReagendamientos) {
                        Icon(Icons.Default.EditCalendar, "Reagendamientos")
                    }
                    IconButton(onClick = onVerHistorial) {
                        Icon(Icons.Default.History, "Historial")
                    }
                    IconButton(onClick = { vm.cargar() }) {
                        Icon(Icons.Default.Refresh, "Recargar")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHost) }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Panel de filtros animado
            AnimatedVisibility(
                visible = showFiltros,
                enter   = expandVertically() + fadeIn(),
                exit    = shrinkVertically() + fadeOut()
            ) {
                FiltrosMisCitasPanel(
                    filtros   = filtrosTemp,
                    estados   = estados,
                    onChange  = { filtrosTemp = it },
                    onAplicar = { vm.aplicarFiltros(filtrosTemp) },
                    onCerrar  = { vm.toggleFiltros() }
                )
            }

            Box(modifier = Modifier.fillMaxSize()) {
                when (val state = uiState) {
                    is MisCitasUiState.Loading -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                    is MisCitasUiState.Empty -> {
                        EmptyMisCitas(
                            conFiltros = filtros.activo,
                            modifier   = Modifier.align(Alignment.Center)
                        )
                    }
                    is MisCitasUiState.Error -> {
                        Column(
                            modifier            = Modifier.align(Alignment.Center).padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(state.mensaje, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
                            Spacer(Modifier.height(16.dp))
                            Button(onClick = { vm.cargar() }) { Text("Reintentar") }
                        }
                    }
                    is MisCitasUiState.Success -> {
                        LazyColumn(
                            contentPadding      = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(state.citas, key = { it.id }) { cita ->
                                CitaCard(
                                    cita           = cita,
                                    onCancelar     = { citaAccion = cita; mostrarCancelar  = true },
                                    onReagendar    = { citaAccion = cita; mostrarReagendar = true },
                                    onFinalizar    = { citaAccion = cita; mostrarFinalizar = true },
                                    accionCargando = accionState is AccionUiState.Loading
                                )
                            }
                        }
                    }
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

    Surface(tonalElevation = 4.dp, shadowElevation = 2.dp) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {

            Text(
                text       = "Filtrar citas",
                style      = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(12.dp))

            // Búsqueda por cliente
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
                modifier   = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))

            // Fechas
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MisCitasDatePicker(
                    label    = "Desde",
                    value    = filtros.fechaDesde,
                    modifier = Modifier.weight(1f),
                    context  = context,
                    onDate   = { onChange(filtros.copy(fechaDesde = it)) }
                )
                MisCitasDatePicker(
                    label    = "Hasta",
                    value    = filtros.fechaHasta,
                    modifier = Modifier.weight(1f),
                    context  = context,
                    onDate   = { onChange(filtros.copy(fechaHasta = it)) }
                )
            }

            Spacer(Modifier.height(8.dp))

            // Dropdown de estados desde BD
            MisCitasEstadoDropdown(
                estados            = estados,
                seleccionadoId     = filtros.idEstado,
                seleccionadoNombre = filtros.nombreEstado,
                onSelect           = { id, nombre ->
                    onChange(filtros.copy(idEstado = id, nombreEstado = nombre))
                }
            )

            Spacer(Modifier.height(12.dp))

            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                TextButton(onClick = onCerrar) { Text("Cancelar") }
                Spacer(Modifier.width(8.dp))
                Button(onClick = onAplicar)    { Text("Aplicar") }
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
                }) {
                    Icon(Icons.Default.CalendarMonth, "Seleccionar fecha")
                }
                if (value != null) {
                    IconButton(onClick = { onDate(null) }) {
                        Icon(Icons.Default.Clear, "Limpiar fecha", modifier = Modifier.size(16.dp))
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

    ExposedDropdownMenuBox(
        expanded         = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value         = seleccionadoNombre ?: "Todos los estados",
            onValueChange = {},
            readOnly      = true,
            label         = { Text("Estado") },
            trailingIcon  = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier      = Modifier.fillMaxWidth().menuAnchor()
        )
        ExposedDropdownMenu(
            expanded         = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("Todos los estados") },
                onClick = { onSelect(null, null); expanded = false },
                leadingIcon = {
                    if (seleccionadoId == null)
                        Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp))
                }
            )
            if (estados.isNotEmpty()) HorizontalDivider()
            estados.forEach { estado ->
                val colorEstado = remember(estado.color) {
                    runCatching {
                        Color(android.graphics.Color.parseColor(estado.color))
                    }.getOrDefault(Color.Gray)
                }
                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                modifier = Modifier.size(8.dp),
                                shape    = MaterialTheme.shapes.extraSmall,
                                color    = colorEstado
                            ) {}
                            Spacer(Modifier.width(8.dp))
                            Text(estado.nombre)
                        }
                    },
                    onClick = { onSelect(estado.id, estado.nombre); expanded = false },
                    leadingIcon = {
                        if (seleccionadoId == estado.id)
                            Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp))
                    }
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
    accionCargando: Boolean
) {
    val colorEstado = remember(cita.colorEstado) {
        runCatching {
            Color(android.graphics.Color.parseColor(cita.colorEstado ?: "#888888"))
        }.getOrDefault(Color.Gray)
    }

    Card(
        modifier  = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // ── Estado + fecha ────────────────────────────────────────────
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = colorEstado.copy(alpha = 0.15f)
                ) {
                    Text(
                        text       = cita.estado,
                        color      = colorEstado,
                        style      = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        modifier   = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                Text(
                    text  = formatearFecha(cita.fechaHoraInicio),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.height(12.dp))

            // ── Cliente ───────────────────────────────────────────────────
            if (!cita.nombreCliente.isNullOrBlank()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier          = Modifier.padding(bottom = 6.dp)
                ) {
                    Icon(
                        imageVector        = Icons.Default.Person,
                        contentDescription = null,
                        modifier           = Modifier.size(14.dp),
                        tint               = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text  = cita.nombreCliente,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // ── Profesional ───────────────────────────────────────────────
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector        = Icons.Default.Badge,
                    contentDescription = null,
                    modifier           = Modifier.size(16.dp),
                    tint               = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text       = "${cita.profesional} · ${cita.cargoProfesional ?: ""}",
                    style      = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(4.dp))

            // ── Hora y sede ───────────────────────────────────────────────
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector        = Icons.Default.Schedule,
                    contentDescription = null,
                    modifier           = Modifier.size(16.dp),
                    tint               = MaterialTheme.colorScheme.secondary
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text  = "${formatearHora(cita.fechaHoraInicio)} - ${formatearHora(cita.fechaHoraFin)} · ${cita.sede}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.height(4.dp))

            // ── Total ─────────────────────────────────────────────────────
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector        = Icons.Default.AttachMoney,
                    contentDescription = null,
                    modifier           = Modifier.size(16.dp),
                    tint               = MaterialTheme.colorScheme.secondary
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text       = "Total: ${"$%,.0f".format(cita.total)}",
                    style      = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color      = MaterialTheme.colorScheme.primary
                )
            }

            // ── Notas ─────────────────────────────────────────────────────
            cita.notas?.let {
                Spacer(Modifier.height(4.dp))
                Text(
                    text  = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // ── Botones (Programada=1 o Confirmada=2) ─────────────────────
            if (cita.idEstado in listOf(1, 2)) {
                Spacer(Modifier.height(12.dp))
                HorizontalDivider()
                Spacer(Modifier.height(8.dp))

                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick  = onReagendar,
                        enabled  = !accionCargando,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.EditCalendar, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Reagendar")
                    }
                    OutlinedButton(
                        onClick  = onCancelar,
                        enabled  = !accionCargando,
                        modifier = Modifier.weight(1f),
                        colors   = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        if (accionCargando) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.Cancel, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Cancelar")
                        }
                    }
                }

                Spacer(Modifier.height(6.dp))
                Button(
                    onClick  = onFinalizar,
                    enabled  = !accionCargando,
                    modifier = Modifier.fillMaxWidth(),
                    colors   = ButtonDefaults.buttonColors(containerColor = Color(0xFF607D8B))
                ) {
                    Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Finalizar cita")
                }
            }

            // ── Pendiente de cambio ───────────────────────────────────────
            if (cita.idEstado == 4) {
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Info, null,
                        modifier = Modifier.size(14.dp),
                        tint     = MaterialTheme.colorScheme.secondary)
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text  = "Solicitud de cambio en revisión",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
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
    Column(
        modifier            = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector        = if (conFiltros) Icons.Default.SearchOff
            else Icons.Default.CalendarMonth,
            contentDescription = null,
            modifier           = Modifier.size(64.dp),
            tint               = MaterialTheme.colorScheme.secondary
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text       = if (conFiltros) "Sin resultados" else "No hay citas activas",
            style      = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text      = if (conFiltros) "Ninguna cita coincide con los filtros aplicados."
            else "Reserva tu primera cita desde el menú principal.",
            style     = MaterialTheme.typography.bodyMedium,
            color     = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

// ─── Helpers de formato ───────────────────────────────────────────────────────

private fun formatearFecha(fechaIso: String): String = try {
    val p = fechaIso.substring(0, 10).split("-")
    "${p[2]}/${p[1]}/${p[0]}"
} catch (e: Exception) { fechaIso }

private fun formatearHora(fechaIso: String): String = try {
    fechaIso.substring(11, 16)
} catch (e: Exception) { fechaIso }