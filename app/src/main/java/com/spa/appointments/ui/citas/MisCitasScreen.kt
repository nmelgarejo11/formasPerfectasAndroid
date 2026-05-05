package com.spa.appointments.ui.citas

import android.app.DatePickerDialog
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
    val context = LocalContext.current

    var filtrosTemp by remember { mutableStateOf(FiltrosMisCitas()) }

    var citaAccion             by remember { mutableStateOf<Cita?>(null) }
    var mostrarCancelar        by remember { mutableStateOf(false) }
    var mostrarReagendar       by remember { mutableStateOf(false) }
    var mostrarFinalizar       by remember { mutableStateOf(false) }
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
            shape = RoundedCornerShape(16.dp),
            icon  = {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.errorContainer
                ) {
                    Icon(
                        Icons.Default.Cancel, null,
                        tint     = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(10.dp).size(24.dp)
                    )
                }
            },
            title = { Text("Cancelar cita", fontWeight = FontWeight.Bold) },
            text  = {
                Text(
                    text  = "¿Deseas cancelar la cita con ${citaAccion?.profesional}? Esta acción no se puede deshacer.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                Button(
                    onClick = { mostrarCancelar = false; vm.cancelarCita(citaAccion!!.id) },
                    shape   = RoundedCornerShape(10.dp),
                    colors  = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) { Text("Sí, cancelar") }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { mostrarCancelar = false },
                    shape   = RoundedCornerShape(10.dp)
                ) { Text("Volver") }
            }
        )
    }

    // ── Diálogo: Reagendar ───────────────────────────────────────────────────
    if (mostrarReagendar && citaAccion != null) {
        AlertDialog(
            onDismissRequest = { mostrarReagendar = false },
            shape = RoundedCornerShape(16.dp),
            icon  = {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Icon(
                        Icons.Default.EditCalendar, null,
                        tint     = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(10.dp).size(24.dp)
                    )
                }
            },
            title = { Text("Solicitar reagendamiento", fontWeight = FontWeight.Bold) },
            text  = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Row(
                            modifier          = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Info, null,
                                modifier = Modifier.size(14.dp),
                                tint     = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text  = "Tu solicitud será revisada por el negocio.",
                                style = MaterialTheme.typography.bodySmall,
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
                    onClick = {
                        mostrarReagendar = false
                        vm.reagendarCita(citaAccion!!.id, motivoReagendar.ifBlank { null })
                        motivoReagendar = ""
                    },
                    shape = RoundedCornerShape(10.dp)
                ) { Text("Enviar solicitud") }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { mostrarReagendar = false; motivoReagendar = "" },
                    shape   = RoundedCornerShape(10.dp)
                ) { Text("Cancelar") }
            }
        )
    }

    // ── Diálogo: Finalizar ───────────────────────────────────────────────────
    if (mostrarFinalizar && citaAccion != null) {
        AlertDialog(
            onDismissRequest = { mostrarFinalizar = false; metodoPagoSeleccionado = null },
            shape = RoundedCornerShape(16.dp),
            icon  = {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.tertiaryContainer
                ) {
                    Icon(
                        Icons.Default.CheckCircle, null,
                        tint     = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier.padding(10.dp).size(24.dp)
                    )
                }
            },
            title = { Text("Finalizar cita", fontWeight = FontWeight.Bold) },
            text  = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text  = "Selecciona el método de pago utilizado.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (metodosPago.isEmpty()) {
                        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        }
                    } else {
                        metodosPago.forEach { metodo ->
                            val seleccionado = metodoPagoSeleccionado?.id == metodo.id
                            Surface(
                                shape  = RoundedCornerShape(10.dp),
                                color  = if (seleccionado)
                                    MaterialTheme.colorScheme.primaryContainer
                                else
                                    MaterialTheme.colorScheme.surfaceVariant,
                                border = if (seleccionado) BorderStroke(
                                    1.5.dp,
                                    MaterialTheme.colorScheme.primary
                                ) else null,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier          = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 10.dp)
                                ) {
                                    RadioButton(
                                        selected = seleccionado,
                                        onClick  = { metodoPagoSeleccionado = metodo }
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text(
                                        text       = metodo.nombre,
                                        style      = MaterialTheme.typography.bodyMedium,
                                        fontWeight = if (seleccionado) FontWeight.SemiBold
                                        else FontWeight.Normal,
                                        color      = if (seleccionado)
                                            MaterialTheme.colorScheme.onPrimaryContainer
                                        else
                                            MaterialTheme.colorScheme.onSurface
                                    )
                                }
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
                    shape    = RoundedCornerShape(10.dp),
                    colors   = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.tertiary
                    )
                ) { Text("Finalizar") }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { mostrarFinalizar = false; metodoPagoSeleccionado = null },
                    shape   = RoundedCornerShape(10.dp)
                ) { Text("Cancelar") }
            }
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
                        if (totalCitas != null) {
                            Text(
                                text  = "$totalCitas ${if (totalCitas == 1) "activa" else "activas"}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Volver")
                    }
                },
                actions = {
                    // Filtro activo: badge
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
                            contentDescription = "Filtrar",
                            tint               = if (filtros.activo) MaterialTheme.colorScheme.primary
                            else LocalContentColor.current
                        )
                    }
                    // Menú de navegación secundaria colapsado
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

            // Banner filtros activos
            AnimatedVisibility(
                visible = filtros.activo && !showFiltros,
                enter   = expandVertically() + fadeIn(),
                exit    = shrinkVertically() + fadeOut()
            ) {
                Surface(
                    color    = MaterialTheme.colorScheme.secondaryContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier          = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.FilterAlt, null,
                            modifier = Modifier.size(14.dp),
                            tint     = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text     = "Filtros aplicados",
                            style    = MaterialTheme.typography.labelSmall,
                            color    = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(
                            onClick        = { vm.limpiarFiltros() },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                        ) {
                            Text(
                                "Limpiar",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }
            }

            Box(modifier = Modifier.fillMaxSize()) {
                when (val state = uiState) {
                    is MisCitasUiState.Loading -> {
                        Column(
                            modifier            = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            CircularProgressIndicator()
                            Text(
                                "Cargando citas…",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    is MisCitasUiState.Empty -> {
                        EmptyMisCitas(
                            conFiltros = filtros.activo,
                            modifier   = Modifier.align(Alignment.Center)
                        )
                    }

                    is MisCitasUiState.Error -> {
                        Column(
                            modifier            = Modifier
                                .align(Alignment.Center)
                                .padding(32.dp),
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
                                onClick = { vm.cargar() },
                                shape   = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Refresh, null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Reintentar")
                            }
                        }
                    }

                    is MisCitasUiState.Success -> {
                        LazyColumn(
                            contentPadding      = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(state.citas, key = { it.id }) { cita ->
                                CitaCard(
                                    cita           = cita,
                                    onCancelar     = { citaAccion = cita; mostrarCancelar  = true },
                                    onReagendar    = { citaAccion = cita; mostrarReagendar = true },
                                    onFinalizar    = { citaAccion = cita; mostrarFinalizar = true },
                                    onWhatsApp     = { vm.abrirWhatsApp(cita.id, context) },
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
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {

            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier              = Modifier.fillMaxWidth()
            ) {
                Text(
                    text       = "Filtrar citas",
                    style      = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onCerrar, modifier = Modifier.size(28.dp)) {
                    Icon(
                        Icons.Default.Close, "Cerrar",
                        modifier = Modifier.size(18.dp)
                    )
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

            Spacer(Modifier.height(10.dp))

            MisCitasEstadoDropdown(
                estados            = estados,
                seleccionadoId     = filtros.idEstado,
                seleccionadoNombre = filtros.nombreEstado,
                onSelect           = { id, nombre ->
                    onChange(filtros.copy(idEstado = id, nombreEstado = nombre))
                }
            )

            Spacer(Modifier.height(14.dp))

            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                verticalAlignment     = Alignment.CenterVertically
            ) {
                OutlinedButton(onClick = onCerrar, shape = RoundedCornerShape(10.dp)) {
                    Text("Cancelar")
                }
                Button(onClick = onAplicar, shape = RoundedCornerShape(10.dp)) {
                    Text("Aplicar filtros")
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
                }) {
                    Icon(Icons.Default.CalendarMonth, "Seleccionar fecha")
                }
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

    ExposedDropdownMenuBox(
        expanded         = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value         = seleccionadoNombre ?: "Todos los estados",
            onValueChange = {},
            readOnly      = true,
            label         = { Text("Estado") },
            shape         = RoundedCornerShape(12.dp),
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
        shape     = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors    = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {

            // Barra de color lateral por estado
            Surface(
                modifier = Modifier.width(4.dp).fillMaxHeight(),
                color    = colorEstado
            ) {}

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 14.dp, vertical = 12.dp)
            ) {

                // ── Estado + fecha ────────────────────────────────────────
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = colorEstado.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text       = cita.estado,
                            color      = colorEstado,
                            style      = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            modifier   = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.CalendarToday, null,
                            modifier = Modifier.size(11.dp),
                            tint     = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.width(3.dp))
                        Text(
                            text  = formatearFecha(cita.fechaHoraInicio),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(Modifier.height(10.dp))
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )
                Spacer(Modifier.height(10.dp))

                // ── Cliente ───────────────────────────────────────────────
                if (!cita.nombreCliente.isNullOrBlank()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier          = Modifier.padding(bottom = 6.dp)
                    ) {
                        Icon(
                            Icons.Default.Person, null,
                            modifier = Modifier.size(14.dp),
                            tint     = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.width(5.dp))
                        Text(
                            text       = cita.nombreCliente,
                            style      = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // ── Profesional ───────────────────────────────────────────
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier          = Modifier.padding(bottom = 4.dp)
                ) {
                    Icon(
                        Icons.Default.Badge, null,
                        modifier = Modifier.size(14.dp),
                        tint     = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.width(5.dp))
                    Text(
                        text       = buildString {
                            append(cita.profesional)
                            cita.cargoProfesional?.let { append(" · $it") }
                        },
                        style      = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // ── Hora y sede ───────────────────────────────────────────
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier          = Modifier.padding(bottom = 4.dp)
                ) {
                    Icon(
                        Icons.Default.Schedule, null,
                        modifier = Modifier.size(14.dp),
                        tint     = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.width(5.dp))
                    Text(
                        text  = "${formatearHora(cita.fechaHoraInicio)} – ${formatearHora(cita.fechaHoraFin)} · ${cita.sede}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // ── Total ─────────────────────────────────────────────────
                Surface(
                    shape    = RoundedCornerShape(8.dp),
                    color    = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
                ) {
                    Row(
                        modifier              = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        Text(
                            "Total",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
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
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Row(
                            modifier          = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                Icons.Default.Notes, null,
                                modifier = Modifier.size(12.dp).padding(top = 1.dp),
                                tint     = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.width(5.dp))
                            Text(
                                text  = nota,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // ── WhatsApp ──────────────────────────────────────────────────
                Spacer(Modifier.height(6.dp))
                OutlinedButton(
                    onClick  = onWhatsApp,
                    enabled  = !accionCargando,
                    modifier = Modifier.fillMaxWidth(),
                    shape    = RoundedCornerShape(10.dp),
                    colors   = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFF25D366)
                    ),
                    border = BorderStroke(1.dp, Color(0xFF25D366).copy(alpha = 0.6f))
                ) {
                    Icon(Icons.Default.Chat, null, modifier = Modifier.size(15.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("WhatsApp", style = MaterialTheme.typography.labelMedium)
                }

                // ── Botones de acción (Programada=1 o Confirmada=2) ───────
                if (cita.idEstado in listOf(1, 2)) {
                    Spacer(Modifier.height(12.dp))
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                    Spacer(Modifier.height(10.dp))

                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick  = onReagendar,
                            enabled  = !accionCargando,
                            modifier = Modifier.weight(1f),
                            shape    = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.EditCalendar, null, modifier = Modifier.size(15.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Reagendar", style = MaterialTheme.typography.labelMedium)
                        }
                        OutlinedButton(
                            onClick  = onCancelar,
                            enabled  = !accionCargando,
                            modifier = Modifier.weight(1f),
                            shape    = RoundedCornerShape(10.dp),
                            colors   = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            ),
                            border = BorderStroke(
                                1.dp,
                                MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
                            )
                        ) {
                            if (accionCargando) {
                                CircularProgressIndicator(
                                    modifier    = Modifier.size(15.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(Icons.Default.Cancel, null, modifier = Modifier.size(15.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Cancelar", style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }

                    Spacer(Modifier.height(6.dp))

                    Button(
                        onClick  = onFinalizar,
                        enabled  = !accionCargando,
                        modifier = Modifier.fillMaxWidth().height(44.dp),
                        shape    = RoundedCornerShape(10.dp),
                        colors   = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.tertiary
                        )
                    ) {
                        Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Finalizar cita", fontWeight = FontWeight.SemiBold)
                    }
                }

                // ── Pendiente de cambio ───────────────────────────────────
                if (cita.idEstado == 4) {
                    Spacer(Modifier.height(8.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Row(
                            modifier          = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.HourglassTop, null,
                                modifier = Modifier.size(13.dp),
                                tint     = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Spacer(Modifier.width(5.dp))
                            Text(
                                text  = "Solicitud de cambio en revisión",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
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
    Column(
        modifier            = modifier.padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Icon(
                imageVector        = if (conFiltros) Icons.Default.SearchOff
                else Icons.Default.CalendarMonth,
                contentDescription = null,
                modifier           = Modifier.padding(20.dp).size(48.dp),
                tint               = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text       = if (conFiltros) "Sin resultados" else "No hay citas activas",
            style      = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
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