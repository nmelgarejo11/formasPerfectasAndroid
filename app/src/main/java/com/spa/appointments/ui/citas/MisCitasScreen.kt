package com.spa.appointments.ui.citas

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.spa.appointments.R
import com.spa.appointments.domain.model.Cita
import com.spa.appointments.domain.model.Cliente
import com.spa.appointments.domain.model.EstadoCita
import com.spa.appointments.domain.model.MetodoPago
import com.spa.appointments.domain.model.MetodoPagoDetalle
import com.spa.appointments.domain.model.Profesional
import com.spa.appointments.domain.model.ServicioCita
import com.spa.appointments.domain.model.SlotDisponible
import com.spa.appointments.ui.citas.components.AsignarCitaGrupalClienteDialog
import com.spa.appointments.ui.citas.components.CalendarioDiaView
import com.spa.appointments.ui.citas.components.ReagendarCitaDirectoDialog
import com.spa.appointments.ui.theme.AppDynamicTheme
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.time.LocalDate
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

// ─── Screen Principal (Stateful) ──────────────────────────────────────────────

@Composable
fun MisCitasScreen(
    onBack:               () -> Unit,
    onVerHistorial:       () -> Unit,
    onVerReagendamientos: () -> Unit,
    vm: MisCitasViewModel = hiltViewModel()
) {
    val uiState             by vm.uiState.collectAsState()
    val accionState         by vm.accionState.collectAsState()
    val metodosPago         by vm.metodosPago.collectAsState()
    val filtros             by vm.filtros.collectAsState()
    val estados             by vm.estados.collectAsState()
    val showFiltros         by vm.mostrarFiltros.collectAsState()
    val profesionales       by vm.profesionales.collectAsState()
    val modoVista           by vm.modoVista.collectAsState()
    val fechaCalendario     by vm.fechaCalendario.collectAsState()
    val slotsDisponibles    by vm.slotsDisponibles.collectAsState()
    val cargandoSlots       by vm.cargandoSlots.collectAsState()
    val clientesEncontrados by vm.clientesEncontrados.collectAsState()
    val context = LocalContext.current

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) vm.cargar()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    MisCitasContent(
        uiState                   = uiState,
        accionState               = accionState,
        metodosPago               = metodosPago,
        filtros                   = filtros,
        estados                   = estados,
        showFiltros               = showFiltros,
        profesionales             = profesionales,
        modoVista                 = modoVista,
        fechaCalendario           = fechaCalendario,
        slotsDisponibles          = slotsDisponibles,
        cargandoSlots             = cargandoSlots,
        clientesEncontrados       = clientesEncontrados,
        onBack                    = onBack,
        onVerHistorial            = onVerHistorial,
        onVerReagendamientos      = onVerReagendamientos,
        onCargar                  = { vm.cargar() },
        onLimpiarFiltros          = { vm.limpiarFiltros() },
        onToggleFiltros           = { vm.toggleFiltros() },
        onAplicarFiltros          = { vm.aplicarFiltros(it) },
        onToggleModoVista         = { vm.toggleModoVista() },
        onFechaCalendarioChange   = { vm.setFechaCalendario(it) },
        onCancelarCita            = { vm.cancelarCita(it) },
        onReagendarCitaDirecto    = { id, idProf, ini, fin -> vm.reagendarCitaDirecto(id, idProf, ini, fin) },
        onFinalizarCita           = { idCita, idMetodo, idDetalle, pFinal, obs -> vm.finalizarCita(idCita, idMetodo, idDetalle, pFinal, obs) },
        onAsignarCitaGrupal       = { idCita, ids, ini, fin -> vm.asignarCitaGrupal(idCita, ids, ini, fin) },
        onCargarDisponibilidad    = { idProf, idSede, fecha, duracion -> vm.cargarDisponibilidad(idProf, idSede, fecha, duracion) },
        onBuscarClientes          = { query -> vm.buscarClientes(query) },
        onWhatsApp                = { idCita -> vm.abrirWhatsApp(idCita, context) },
        onResetAccion             = { vm.resetAccion() },
        onCargarProfesionales     = { sedeId, servId -> vm.cargarProfesionales(sedeId, servId) }
    )
}

// ─── Content Principal (Stateless respecto al ViewModel) ──────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MisCitasContent(
    uiState:                   MisCitasUiState,
    accionState:               AccionUiState,
    metodosPago:               List<MetodoPago>,
    filtros:                   FiltrosMisCitas,
    estados:                   List<EstadoCita>,
    showFiltros:               Boolean,
    profesionales:             List<Profesional>,
    modoVista:                 ModoVistaCitas,
    fechaCalendario:           LocalDate,
    slotsDisponibles:          List<SlotDisponible>,
    cargandoSlots:             Boolean,
    clientesEncontrados:       List<Cliente>,
    onBack:                    () -> Unit,
    onVerHistorial:            () -> Unit,
    onVerReagendamientos:      () -> Unit,
    onCargar:                  () -> Unit,
    onLimpiarFiltros:          () -> Unit,
    onToggleFiltros:           () -> Unit,
    onAplicarFiltros:          (FiltrosMisCitas) -> Unit,
    onToggleModoVista:         () -> Unit,
    onFechaCalendarioChange:   (LocalDate) -> Unit,
    onCancelarCita:            (Int) -> Unit,
    onReagendarCitaDirecto:    (idCita: Int, idProfesional: Int, inicioIso: String, finIso: String) -> Unit,
    onFinalizarCita:           (idCita: Int, idMetodo: Int, idDetalle: Int?, precioFinal: Double?, observaciones: String?) -> Unit,
    onAsignarCitaGrupal:       (idCita: Int, idsProfesionales: List<Int>?, fechaInicio: String?, fechaFin: String?) -> Unit,
    onCargarDisponibilidad:    (idProfesional: Int, idSede: Int, fecha: String, duracion: Int) -> Unit,
    onBuscarClientes:          (String) -> Unit,
    onWhatsApp:                (Int) -> Unit,
    onResetAccion:             () -> Unit,
    onCargarProfesionales:     (idSede: Int, idServicio: Int?) -> Unit
) {
    var filtrosTemp by remember { mutableStateOf(FiltrosMisCitas()) }

    var citaAccion         by remember { mutableStateOf<Cita?>(null) }
    var mostrarCancelar    by remember { mutableStateOf(false) }
    var mostrarReagendar   by remember { mutableStateOf(false) }
    var mostrarFinalizar   by remember { mutableStateOf(false) }
    var mostrarAsignar     by remember { mutableStateOf(false) }
    var mostrarDetalleCita by remember { mutableStateOf(false) }

    val snackbarHost = remember { SnackbarHostState() }
    val totalCitas   = (uiState as? MisCitasUiState.Success)?.citas?.size

    LaunchedEffect(accionState) {
        when (val s = accionState) {
            is AccionUiState.Success -> { snackbarHost.showSnackbar(s.mensaje); onResetAccion() }
            is AccionUiState.Error   -> { snackbarHost.showSnackbar(s.mensaje); onResetAccion() }
            else -> Unit
        }
    }

    // ── Diálogo: Cancelar M3 ──────────────────────────────────────────────────
    if (mostrarCancelar && citaAccion != null) {
        AlertDialog(
            onDismissRequest = { mostrarCancelar = false },
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
            title = {
                Text(
                    "Cancelar Cita",
                    fontWeight = FontWeight.Bold,
                    textAlign  = TextAlign.Center,
                    modifier   = Modifier.fillMaxWidth(),
                    style      = MaterialTheme.typography.titleMedium
                )
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text       = "¿Deseas cancelar la cita agendada?",
                        style      = MaterialTheme.typography.bodyMedium,
                        color      = MaterialTheme.colorScheme.onSurface,
                        textAlign  = TextAlign.Center,
                        fontWeight = FontWeight.Medium
                    )
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)
                    ) {
                        Row(
                            modifier              = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment     = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                Icons.Default.Warning, null,
                                modifier = Modifier.size(16.dp),
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
                    onClick  = { mostrarCancelar = false; onCancelarCita(citaAccion!!.id) },
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

    // ── Diálogo: Reagendar Directo ──────────────────────────────────────────
    if (mostrarReagendar && citaAccion != null) {
        ReagendarCitaDirectoDialog(
            cita = citaAccion!!,
            profesionales = profesionales,
            cargandoSlots = cargandoSlots,
            slotsDisponibles = slotsDisponibles,
            onCargarDisponibilidad = onCargarDisponibilidad,
            onConfirmarReagendamiento = { idCita, idProf, iniIso, finIso ->
                mostrarReagendar = false
                onReagendarCitaDirecto(idCita, idProf, iniIso, finIso)
            },
            onDismiss = { mostrarReagendar = false }
        )
    }

    // ── Diálogo: Finalizar ───────────────────────────────────────────────────
    if (mostrarFinalizar && citaAccion != null) {
        FinalizarCitaDialog(
            metodos = metodosPago,
            precioSugerido = citaAccion!!.total ?: 0.0,
            onConfirm = { idMetodo, idDetalle, pFinal, obs ->
                mostrarFinalizar = false
                onFinalizarCita(
                    citaAccion!!.id,
                    idMetodo,
                    idDetalle,
                    pFinal,
                    obs
                )
            },
            onDismiss = {
                mostrarFinalizar = false
            }
        )
    }

    // ── Diálogo: Asignar Horario/Profesional ──────────────────────────
    if (mostrarAsignar && citaAccion != null) {
        val c = citaAccion!!
        if (c.cantidadPersonas != null && c.cantidadPersonas > 1 && c.nombreCliente?.contains("Cliente por Asignar", ignoreCase = true) == true) {
            AsignarCitaGrupalClienteDialog(
                cita = c,
                clientesDisponibles = clientesEncontrados,
                profesionales = profesionales,
                onBuscarCliente = onBuscarClientes,
                onConfirmar = { idCita, idCliente, cantidadPersonas, idsProfesionales ->
                    mostrarAsignar = false
                    onAsignarCitaGrupal(idCita, idsProfesionales, null, null)
                },
                onDismiss = { mostrarAsignar = false }
            )
        } else {
            AsignarCitaGrupalDialog(
                cita = c,
                profesionales = profesionales,
                slotsDisponibles = slotsDisponibles,
                cargandoSlots = cargandoSlots,
                onCargarProfesionales = { idSede -> onCargarProfesionales(idSede, null) },
                onCargarDisponibilidad = onCargarDisponibilidad,
                onConfirm = { ids, inicioIso, finIso ->
                    mostrarAsignar = false
                    onAsignarCitaGrupal(c.id, ids, inicioIso, finIso)
                },
                onDismiss = { mostrarAsignar = false }
            )
        }
    }

    // ── Pop-Up Detalle Cita (Desde vista Calendario) ────────────────────────
    if (mostrarDetalleCita && citaAccion != null) {
        val c = citaAccion!!
        AlertDialog(
            onDismissRequest = { mostrarDetalleCita = false },
            shape = RoundedCornerShape(28.dp),
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            text = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    CitaCard(
                        cita = c,
                        onCancelar = {
                            mostrarDetalleCita = false
                            mostrarCancelar = true
                        },
                        onReagendar = {
                            mostrarDetalleCita = false
                            val idServicioExtraido = parsearServicios(c.servicios).firstOrNull()?.id
                            onCargarProfesionales(c.idSede, idServicioExtraido)
                            mostrarReagendar = true
                        },
                        onFinalizar = {
                            mostrarDetalleCita = false
                            mostrarFinalizar = true
                        },
                        onAsignar = {
                            mostrarDetalleCita = false
                            val idServicioExtraido = parsearServicios(c.servicios).firstOrNull()?.id
                            onCargarProfesionales(c.idSede, idServicioExtraido)
                            onBuscarClientes("")
                            mostrarAsignar = true
                        },
                        onWhatsApp = {
                            onWhatsApp(c.id)
                        },
                        accionCargando = accionState is AccionUiState.Loading
                    )
                }
            },
            confirmButton = {
                OutlinedButton(
                    onClick = { mostrarDetalleCita = false },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Cerrar")
                }
            }
        )
    }

    // ── Scaffold M3 ──────────────────────────────────────────────────────────
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text(
                                text       = "Mis citas",
                                style      = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            AnimatedVisibility(visible = totalCitas != null) {
                                if (totalCitas != null) {
                                    Surface(
                                        shape = RoundedCornerShape(50),
                                        color = MaterialTheme.colorScheme.primaryContainer,
                                        modifier = Modifier.padding(top = 2.dp)
                                    ) {
                                        Text(
                                            text     = "$totalCitas ${if (totalCitas == 1) "cita activa" else "citas activas"}",
                                            style    = MaterialTheme.typography.labelSmall,
                                            color    = MaterialTheme.colorScheme.onPrimaryContainer,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 1.dp)
                                        )
                                    }
                                }
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
                    // Toggle entre Vista Lista y Vista Día (Calendario)
                    IconButton(
                        onClick  = onToggleModoVista,
                        modifier = Modifier
                            .padding(end = 4.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                    ) {
                        Icon(
                            imageVector = if (modoVista == ModoVistaCitas.CALENDARIO_DIA) Icons.Default.FormatListBulleted else Icons.Default.CalendarToday,
                            contentDescription = if (modoVista == ModoVistaCitas.CALENDARIO_DIA) "Vista Lista" else "Vista Calendario",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    if (modoVista == ModoVistaCitas.LISTA) {
                        if (filtros.activo) {
                            IconButton(onClick = onLimpiarFiltros) {
                                BadgedBox(badge = { Badge() }) {
                                    Icon(Icons.Default.FilterAltOff, "Limpiar filtros")
                                }
                            }
                        }
                        IconButton(onClick = { filtrosTemp = filtros; onToggleFiltros() }) {
                            Icon(
                                imageVector        = if (filtros.activo) Icons.Default.FilterAlt else Icons.Default.FilterList,
                                contentDescription = "Filtrar",
                                tint               = if (filtros.activo) MaterialTheme.colorScheme.primary else LocalContentColor.current
                            )
                        }
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
                                onClick     = { menuExpanded = false; onCargar() }
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

            // 🎨 Canvas: Acento sutil en barra decorativa superior
            val primaryColor   = MaterialTheme.colorScheme.primary
            val secondaryColor = MaterialTheme.colorScheme.secondary
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
            ) {
                val width = size.width
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

            AnimatedVisibility(visible = showFiltros && modoVista == ModoVistaCitas.LISTA, enter = expandVertically() + fadeIn(), exit = shrinkVertically() + fadeOut()) {
                FiltrosMisCitasPanel(
                    filtros       = filtrosTemp,
                    estados       = estados,
                    profesionales = profesionales,
                    onChange      = { filtrosTemp = it },
                    onAplicar     = { onAplicarFiltros(filtrosTemp) },
                    onCerrar      = onToggleFiltros
                )
            }

            AnimatedVisibility(visible = filtros.activo && !showFiltros, enter = expandVertically() + fadeIn(), exit = shrinkVertically() + fadeOut()) {
                Surface(color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f), modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.FilterAlt, null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSecondaryContainer)
                        Spacer(Modifier.width(6.dp))
                        Text("Filtros aplicados", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSecondaryContainer, modifier = Modifier.weight(1f))
                        TextButton(onClick = onLimpiarFiltros, contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)) {
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
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.primaryContainer
                            )
                            Text(
                                text = "Cargando citas…",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    is MisCitasUiState.Empty -> {
                        if (modoVista == ModoVistaCitas.CALENDARIO_DIA) {
                            CalendarioDiaView(
                                citas = emptyList(),
                                fechaSeleccionada = fechaCalendario,
                                onFechaChange = onFechaCalendarioChange,
                                onCitaClick = { cita ->
                                    citaAccion = cita
                                    mostrarDetalleCita = true
                                }
                            )
                        } else {
                            EmptyMisCitas(
                                conFiltros = filtros.activo,
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
                    }

                    is MisCitasUiState.Error -> {
                        OutlinedCard(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(24.dp)
                                .fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.outlinedCardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                            )
                        ) {
                            Column(
                                modifier = Modifier
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
                                    onClick = onCargar,
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
                    }

                    is MisCitasUiState.Success -> {
                        if (modoVista == ModoVistaCitas.CALENDARIO_DIA) {
                            CalendarioDiaView(
                                citas = state.citas,
                                fechaSeleccionada = fechaCalendario,
                                onFechaChange = onFechaCalendarioChange,
                                onCitaClick = { cita ->
                                    citaAccion = cita
                                    mostrarDetalleCita = true
                                }
                            )
                        } else {
                            LazyColumn(
                                contentPadding = PaddingValues(
                                    horizontal = 16.dp,
                                    vertical = 14.dp
                                ),
                                verticalArrangement = Arrangement.spacedBy(14.dp)
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
                                            val idServicioExtraido = parsearServicios(cita.servicios).firstOrNull()?.id
                                            onCargarProfesionales(cita.idSede, idServicioExtraido)
                                            mostrarReagendar = true
                                        },
                                        onFinalizar = {
                                            citaAccion = cita
                                            mostrarFinalizar = true
                                        },
                                        onAsignar = {
                                            citaAccion = cita
                                            val idServicioExtraido = parsearServicios(cita.servicios).firstOrNull()?.id
                                            onCargarProfesionales(cita.idSede, idServicioExtraido)
                                            onBuscarClientes("")
                                            mostrarAsignar = true
                                        },
                                        onWhatsApp = {
                                            onWhatsApp(cita.id)
                                        },
                                        accionCargando = accionState is AccionUiState.Loading
                                    )
                                }
                            }
                        }
                    }
                    else -> Unit
                }
            }
        }
    }
}

// ─── Panel de Filtros ─────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FiltrosMisCitasPanel(
    filtros:       FiltrosMisCitas,
    estados:       List<EstadoCita>,
    profesionales: List<Profesional>,
    onChange:      (FiltrosMisCitas) -> Unit,
    onAplicar:     () -> Unit,
    onCerrar:      () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Filtrar citas", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                IconButton(onClick = onCerrar, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Close, "Cerrar", modifier = Modifier.size(18.dp))
                }
            }

            // Filtro: Nombre del Cliente
            OutlinedTextField(
                value = filtros.nombreCliente ?: "",
                onValueChange = { onChange(filtros.copy(nombreCliente = it.ifBlank { null })) },
                label = { Text("Nombre del cliente") },
                leadingIcon = { Icon(Icons.Default.Search, null, modifier = Modifier.size(18.dp)) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            // Filtro: Fechas
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                MisCitasDatePicker(
                    label = "Desde",
                    valorIso = filtros.fechaDesde,
                    modifier = Modifier.weight(1f),
                    onSelect = { onChange(filtros.copy(fechaDesde = it)) }
                )
                MisCitasDatePicker(
                    label = "Hasta",
                    valorIso = filtros.fechaHasta,
                    modifier = Modifier.weight(1f),
                    onSelect = { onChange(filtros.copy(fechaHasta = it)) }
                )
            }

            // Filtro: Estado Dropdown
            MisCitasEstadoDropdown(
                estados = estados,
                estadoIdSeleccionado = filtros.idEstado,
                estadoNombreSeleccionado = filtros.nombreEstado,
                onSelect = { id, nom -> onChange(filtros.copy(idEstado = id, nombreEstado = nom)) }
            )

            // Filtro: Profesional Dropdown
            MisCitasProfesionalDropdown(
                profesionales = profesionales,
                profesionalIdSeleccionado = filtros.idProfesional,
                profesionalNombreSeleccionado = filtros.nombreProfesional,
                onSelect = { id, nom -> onChange(filtros.copy(idProfesional = id, nombreProfesional = nom)) }
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(
                    onClick = { onChange(FiltrosMisCitas()); onAplicar() },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) { Text("Limpiar") }
                Button(
                    onClick = onAplicar,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) { Text("Aplicar") }
            }
        }
    }
}

// ─── Selector de Fecha para Filtros ──────────────────────────────────────────

@Composable
private fun MisCitasDatePicker(
    label:    String,
    valorIso: String?,
    modifier: Modifier = Modifier,
    onSelect: (String?) -> Unit
) {
    val context = LocalContext.current
    OutlinedButton(
        onClick = {
            val cal = Calendar.getInstance()
            android.app.DatePickerDialog(
                context,
                { _, year, month, dayOfMonth ->
                    val selIso = String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, dayOfMonth)
                    onSelect(selIso)
                },
                cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)
            ).show()
        },
        modifier = modifier,
        shape    = RoundedCornerShape(12.dp),
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 10.dp)
    ) {
        Icon(Icons.Default.CalendarToday, null, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(6.dp))
        Text(
            text     = valorIso?.let { formatearFecha(it) } ?: label,
            style    = MaterialTheme.typography.bodySmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

// ─── Selector de Estado para Filtros ──────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MisCitasEstadoDropdown(
    estados:                  List<EstadoCita>,
    estadoIdSeleccionado:     Int?,
    estadoNombreSeleccionado: String?,
    onSelect:                 (Int?, String?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val textoDisplay = estadoNombreSeleccionado ?: "Todos los estados"

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = textoDisplay,
            onValueChange = {},
            readOnly = true,
            label = { Text("Estado") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("Todos los estados") },
                onClick = { onSelect(null, null); expanded = false },
                leadingIcon = {
                    if (estadoIdSeleccionado == null)
                        Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp))
                }
            )
            if (estados.isNotEmpty()) HorizontalDivider()
            estados.forEach { est ->
                DropdownMenuItem(
                    text = { Text(est.nombre) },
                    onClick = { onSelect(est.id, est.nombre); expanded = false },
                    leadingIcon = {
                        if (estadoIdSeleccionado == est.id)
                            Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp))
                    }
                )
            }
        }
    }
}

// ─── Selector de Profesional para Filtros ──────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MisCitasProfesionalDropdown(
    profesionales:                 List<Profesional>,
    profesionalIdSeleccionado:     Int?,
    profesionalNombreSeleccionado: String?,
    onSelect:                      (Int?, String?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val textoDisplay = profesionalNombreSeleccionado ?: "Todos los profesionales"

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = textoDisplay,
            onValueChange = {},
            readOnly = true,
            label = { Text("Profesional") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("Todos los profesionales") },
                onClick = { onSelect(null, null); expanded = false },
                leadingIcon = {
                    if (profesionalIdSeleccionado == null)
                        Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp))
                }
            )
            if (profesionales.isNotEmpty()) HorizontalDivider()
            profesionales.forEach { prof ->
                val nombreMostrar = prof.nombreCompleto.ifBlank { "${prof.nombre} ${prof.apellido}" }
                DropdownMenuItem(
                    text = { Text(nombreMostrar) },
                    onClick = { onSelect(prof.id, nombreMostrar); expanded = false },
                    leadingIcon = {
                        if (profesionalIdSeleccionado == prof.id)
                            Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp))
                    }
                )
            }
        }
    }
}

// ─── Card de Cita Acentuada M3 ────────────────────────────────────────────────

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
    val defaultColor = MaterialTheme.colorScheme.primary
    val colorEstado  = remember(cita.colorEstado, defaultColor) {
        runCatching {
            Color(android.graphics.Color.parseColor(cita.colorEstado ?: "#888888"))
        }.getOrDefault(defaultColor)
    }

    OutlinedCard(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(18.dp),
        colors    = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        border    = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
    ) {
        Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {

            // ── Borde indicador lateral de Estado ───────────────────────────
            Box(
                modifier = Modifier
                    .width(6.dp)
                    .fillMaxHeight()
                    .background(colorEstado)
            )

            Column(modifier = Modifier.weight(1f).padding(horizontal = 14.dp, vertical = 12.dp)) {

                // ── Encabezado: Estado + Fecha + WhatsApp ───────────────────
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Surface(shape = RoundedCornerShape(8.dp), color = colorEstado.copy(alpha = 0.15f)) {
                        Text(
                            text       = cita.estado,
                            color      = colorEstado,
                            style      = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            modifier   = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.CalendarToday,
                            contentDescription = null,
                            modifier = Modifier.size(13.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text  = formatearFecha(cita.fechaHoraInicio),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(Modifier.width(10.dp))

                        // Botón de WhatsApp
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFF25D366).copy(alpha = 0.15f),
                            modifier = Modifier.size(32.dp)
                        ) {
                            IconButton(
                                onClick  = onWhatsApp,
                                enabled  = !accionCargando,
                                modifier = Modifier.fillMaxSize()
                            ) {
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

                Spacer(Modifier.height(8.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                Spacer(Modifier.height(8.dp))

                // ── Información Principal ───────────────────────────────────
                if (!cita.nombreCliente.isNullOrBlank()) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 4.dp)) {
                        Icon(Icons.Default.Person, null, modifier = Modifier.size(15.dp), tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text       = cita.nombreCliente,
                            style      = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color      = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 4.dp)) {
                    Icon(Icons.Default.Badge, null, modifier = Modifier.size(15.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text       = buildString { append(cita.profesional); cita.cargoProfesional?.let { append(" · $it") } },
                        style      = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color      = MaterialTheme.colorScheme.onSurface
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 6.dp)) {
                    Icon(Icons.Default.Schedule, null, modifier = Modifier.size(15.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text  = "${formatearHora(cita.fechaHoraInicio)} – ${formatearHora(cita.fechaHoraFin)} · ${cita.sede}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // ── Fila de Resumen: Total + Cantidad de Personas ───────────
                Row(
                    modifier              = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Row(
                            modifier              = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment     = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.Payments, null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer)
                            Text(
                                text       = "Total: ${"$%,.0f".format(cita.total)}",
                                style      = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color      = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }

                    if (cita.cantidadPersonas != null && cita.cantidadPersonas > 0) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Group, null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text  = "${cita.cantidadPersonas} ${if (cita.cantidadPersonas == 1) "persona" else "personas"}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                // ── Notas ───────────────────────────────────────────────────
                cita.notas?.let { nota ->
                    Spacer(Modifier.height(4.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier          = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(Icons.Default.Notes, null, modifier = Modifier.size(14.dp).padding(top = 2.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text  = nota,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // ── Servicios ───────────────────────────────────────────────
                val servicios = remember(cita.servicios) { parsearServicios(cita.servicios) }
                if (servicios.isNotEmpty()) {
                    Spacer(Modifier.height(6.dp))
                    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Spa, null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text       = "Servicios:",
                                style      = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color      = MaterialTheme.colorScheme.primary
                            )
                        }
                        servicios.forEach { s ->
                            Text(
                                text     = "• ${s.nombre} (${s.duracion} min)",
                                style    = MaterialTheme.typography.bodySmall,
                                color    = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(start = 20.dp, top = 2.dp)
                            )
                        }
                    }
                }

                // ── Zona de Acciones: Programada=1 o Confirmada=2 ──────────
                if (cita.idEstado in listOf(1, 2)) {
                    Spacer(Modifier.height(10.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                    Spacer(Modifier.height(10.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick  = onReagendar,
                            enabled  = !accionCargando,
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            shape    = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.EditCalendar, null, modifier = Modifier.size(15.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(
                                "Reagendar",
                                style = MaterialTheme.typography.labelMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        OutlinedButton(
                            onClick  = onCancelar,
                            enabled  = !accionCargando,
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            shape    = RoundedCornerShape(12.dp),
                            colors   = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                            border   = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f))
                        ) {
                            if (accionCargando) {
                                CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                            } else {
                                Icon(Icons.Default.Cancel, null, modifier = Modifier.size(15.dp))
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    "Cancelar",
                                    style = MaterialTheme.typography.labelMedium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick  = onFinalizar,
                        enabled  = !accionCargando,
                        modifier = Modifier.fillMaxWidth().height(42.dp),
                        shape    = RoundedCornerShape(12.dp),
                        colors   = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                    ) {
                        Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Finalizar cita", fontWeight = FontWeight.SemiBold)
                    }
                }

                // ── Zona de Acciones: Estado 8 (Pendiente Asignación) ───────
                if (cita.idEstado == 8) {
                    Spacer(Modifier.height(10.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                    Spacer(Modifier.height(10.dp))

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.HourglassTop, null, modifier = Modifier.size(15.dp), tint = MaterialTheme.colorScheme.onTertiaryContainer)
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text  = "Pendiente asignar horario y profesional",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onTertiaryContainer,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick  = onAsignar,
                            enabled  = !accionCargando,
                            modifier = Modifier.weight(1f),
                            shape    = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.EditCalendar, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Asignar", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        }
                        OutlinedButton(
                            onClick  = onCancelar,
                            enabled  = !accionCargando,
                            modifier = Modifier.weight(1f),
                            shape    = RoundedCornerShape(12.dp),
                            colors   = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                            border   = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f))
                        ) {
                            Icon(Icons.Default.Cancel, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Cancelar", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // ── Estado 4 (Pendiente de cambio) ──────────────────────────
                if (cita.idEstado == 4) {
                    Spacer(Modifier.height(8.dp))
                    Surface(shape = RoundedCornerShape(10.dp), color = MaterialTheme.colorScheme.secondaryContainer, modifier = Modifier.fillMaxWidth()) {
                        Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.HourglassTop, null, modifier = Modifier.size(15.dp), tint = MaterialTheme.colorScheme.onSecondaryContainer)
                            Spacer(Modifier.width(6.dp))
                            Text("Solicitud de cambio en revisión", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSecondaryContainer, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        }
    }
}

// ─── Empty State con Acento Canvas ────────────────────────────────────────────

@Composable
private fun EmptyMisCitas(
    conFiltros: Boolean,
    modifier:   Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary

    OutlinedCard(
        modifier = modifier
            .padding(24.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
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
                // Anillo de brillo en Canvas
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawCircle(color = primaryColor.copy(alpha = 0.15f))
                }
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceContainerHigh
                ) {
                    Icon(
                        imageVector        = if (conFiltros) Icons.Default.SearchOff else Icons.Default.CalendarMonth,
                        contentDescription = null,
                        modifier           = Modifier.padding(18.dp).size(36.dp),
                        tint               = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Text(
                text       = if (conFiltros) "Sin resultados" else "No hay citas activas",
                style      = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text      = if (conFiltros) "Ninguna cita coincide con los filtros aplicados." else "Reserva tu primera cita desde el menú principal.",
                style     = MaterialTheme.typography.bodyMedium,
                color     = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

// ─── Diálogo: FinalizarCitaDialog ─────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FinalizarCitaDialog(
    metodos:        List<MetodoPago>,
    precioSugerido: Double,
    onConfirm:      (idMetodo: Int, idDetalle: Int?, precioFinal: Double?, observaciones: String?) -> Unit,
    onDismiss:      () -> Unit
) {
    var seleccionado        by remember { mutableStateOf<MetodoPago?>(null) }
    var detalleSeleccionado by remember { mutableStateOf<MetodoPagoDetalle?>(null) }
    var precioFinalText     by remember(precioSugerido) {
        mutableStateOf(if (precioSugerido > 0) String.format(Locale.US, "%.0f", precioSugerido) else "")
    }
    var observacionesText by remember { mutableStateOf("") }
    var expanded          by remember { mutableStateOf(false) }
    var expandedDetalle   by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape            = RoundedCornerShape(28.dp),
        containerColor   = MaterialTheme.colorScheme.surfaceContainerHigh,
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
                    tint               = MaterialTheme.colorScheme.onTertiaryContainer,
                    modifier           = Modifier.size(28.dp)
                )
            }
        },
        title = {
            Text(
                text       = "Finalizar Cita",
                fontWeight = FontWeight.Bold,
                textAlign  = TextAlign.Center,
                modifier   = Modifier.fillMaxWidth(),
                style      = MaterialTheme.typography.titleMedium
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // 1. Método de Pago
                Text(
                    text       = "Método de Pago *",
                    style      = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )

                ExposedDropdownMenuBox(
                    expanded         = expanded,
                    onExpandedChange = { expanded = it }
                ) {
                    OutlinedTextField(
                        value        = seleccionado?.nombre ?: "Seleccionar método...",
                        onValueChange = {},
                        readOnly     = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                        modifier     = Modifier.menuAnchor().fillMaxWidth(),
                        shape        = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded         = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        metodos.forEach { mp ->
                            DropdownMenuItem(
                                text    = { Text(mp.nombre) },
                                onClick = {
                                    seleccionado        = mp
                                    detalleSeleccionado = null
                                    expanded            = false
                                }
                            )
                        }
                    }
                }

                if (seleccionado?.detalles?.isNotEmpty() == true) {
                    Text(
                        text  = "Detalle de pago *",
                        style = MaterialTheme.typography.labelSmall
                    )
                    ExposedDropdownMenuBox(
                        expanded         = expandedDetalle,
                        onExpandedChange = { expandedDetalle = it }
                    ) {
                        OutlinedTextField(
                            value        = detalleSeleccionado?.nombre ?: "Seleccionar detalle...",
                            onValueChange = {},
                            readOnly     = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expandedDetalle) },
                            modifier     = Modifier.menuAnchor().fillMaxWidth(),
                            shape        = RoundedCornerShape(12.dp)
                        )
                        ExposedDropdownMenu(
                            expanded         = expandedDetalle,
                            onDismissRequest = { expandedDetalle = false }
                        ) {
                            (seleccionado?.detalles ?: emptyList()).forEach { detalle ->
                                DropdownMenuItem(
                                    text    = { Text(detalle.nombre) },
                                    onClick = {
                                        detalleSeleccionado = detalle
                                        expandedDetalle     = false
                                    }
                                )
                            }
                        }
                    }
                }

                // 2. Precio Final Cobrado (Editable)
                Text(
                    text       = "Precio Final Cobrado ($):",
                    style      = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                OutlinedTextField(
                    value           = precioFinalText,
                    onValueChange   = { precioFinalText = it.filter { c -> c.isDigit() || c == '.' } },
                    label           = { Text("Valor final") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier        = Modifier.fillMaxWidth(),
                    shape           = RoundedCornerShape(12.dp),
                    singleLine      = true
                )

                // 3. Observaciones
                Text(
                    text       = "Observaciones de finalización (opcional):",
                    style      = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                OutlinedTextField(
                    value         = observacionesText,
                    onValueChange = { observacionesText = it },
                    label         = { Text("Notas...") },
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
                    val pFinal = precioFinalText.toDoubleOrNull()
                    seleccionado?.let {
                        onConfirm(
                            it.id,
                            detalleSeleccionado?.id,
                            pFinal,
                            observacionesText.trim().ifEmpty { null }
                        )
                    }
                },
                enabled  = seleccionado != null && (seleccionado?.detalles?.isEmpty() == true || detalleSeleccionado != null),
                shape    = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
                colors   = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text(text = "Confirmar y Finalizar Cita", fontWeight = FontWeight.SemiBold)
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

// ─── Helpers de Formato ───────────────────────────────────────────────────────

private fun formatearFecha(fechaIso: String): String = try {
    val p = fechaIso.substring(0, 10).split("-")
    "${p[2]}/${p[1]}/${p[0]}"
} catch (e: Exception) {
    fechaIso
}

private fun formatearHora(fechaIso: String): String = try {
    if (fechaIso.isBlank()) {
        ""
    } else {
        val horaStr = if (fechaIso.contains("T")) fechaIso.substring(11, 16) else fechaIso.take(5)
        val partes  = horaStr.trim().split(":")
        val h       = partes[0].toInt()
        val m       = partes[1]
        val amPm    = if (h >= 12) "PM" else "AM"
        val h12     = when {
            h == 0 -> 12
            h > 12 -> h - 12
            else -> h
        }
        String.format(Locale.US, "%02d:%s %s", h12, m, amPm)
    }
} catch (e: Exception) {
    fechaIso
}

private fun parsearServicios(json: String?): List<ServicioCita> {
    if (json.isNullOrBlank()) return emptyList()
    return try {
        val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
        val type  = Types.newParameterizedType(List::class.java, ServicioCita::class.java)
        moshi.adapter<List<ServicioCita>>(type).fromJson(json) ?: emptyList()
    } catch (e: Exception) {
        emptyList()
    }
}

// ─── AsignarCitaGrupalDialog ─────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AsignarCitaGrupalDialog(
    cita:                  Cita,
    profesionales:         List<Profesional>,
    slotsDisponibles:      List<SlotDisponible>,
    cargandoSlots:         Boolean,
    onCargarProfesionales: (Int) -> Unit,
    onCargarDisponibilidad:(idProfesional: Int, idSede: Int, fecha: String, duracion: Int) -> Unit,
    onConfirm:             (idsProfesionales: List<Int>?, fechaHoraInicio: String?, fechaHoraFin: String?) -> Unit,
    onDismiss:             () -> Unit
) {
    val context   = LocalContext.current
    val servicios = remember(cita.servicios) { parsearServicios(cita.servicios) }
    val duracion  = servicios.firstOrNull()?.duracion ?: 60

    var fechaSeleccionada by remember { mutableStateOf<String?>(null) }
    var horaSeleccionada  by remember { mutableStateOf<String?>(null) }
    var profesionalesSel  by remember { mutableStateOf<List<Int>>(emptyList()) }

    var mostrarDatePicker by remember { mutableStateOf(false) }
    var mostrarTimePicker by remember { mutableStateOf(false) }
    var mostrarSelProf    by remember { mutableStateOf(false) }

    LaunchedEffect(fechaSeleccionada) {
        val f = fechaSeleccionada
        if (!f.isNullOrBlank()) {
            horaSeleccionada = null
            onCargarDisponibilidad(cita.idProfesional, cita.idSede, f, duracion)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape            = RoundedCornerShape(28.dp),
        containerColor   = MaterialTheme.colorScheme.surfaceContainerHigh,
        title = {
            Text(
                "Asignar Horario",
                fontWeight = FontWeight.Bold,
                textAlign  = TextAlign.Center,
                modifier   = Modifier.fillMaxWidth(),
                style      = MaterialTheme.typography.titleMedium
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick  = { mostrarDatePicker = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape    = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.CalendarMonth, null)
                    Spacer(Modifier.width(8.dp))
                    Text(fechaSeleccionada?.let { formatearFecha(it) } ?: "Seleccionar Fecha")
                }

                OutlinedButton(
                    onClick  = { mostrarTimePicker = true },
                    enabled  = fechaSeleccionada != null,
                    modifier = Modifier.fillMaxWidth(),
                    shape    = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Schedule, null)
                    Spacer(Modifier.width(8.dp))
                    Text(horaSeleccionada?.let { formatearHora(it) } ?: "Seleccionar Hora de Inicio")
                }

                OutlinedButton(
                    onClick  = { mostrarSelProf = true },
                    enabled  = horaSeleccionada != null,
                    modifier = Modifier.fillMaxWidth(),
                    shape    = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.People, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Profesionales (${profesionalesSel.size})")
                }

                if (mostrarSelProf) {
                    ProfesionalesSeleccionDialog(
                        sedeId                = cita.idSede,
                        fecha                 = fechaSeleccionada!!,
                        hora                  = horaSeleccionada!!,
                        duracion              = duracion,
                        seleccionados         = profesionalesSel,
                        profesionales         = profesionales,
                        onCargarProfesionales = onCargarProfesionales,
                        onDismiss             = { mostrarSelProf = false },
                        onConfirm             = {
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
                enabled  = fechaSeleccionada != null && horaSeleccionada != null,
                modifier = Modifier.fillMaxWidth(),
                shape    = RoundedCornerShape(12.dp)
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

    if (mostrarDatePicker) {
        val cal = Calendar.getInstance()
        if (fechaSeleccionada != null && fechaSeleccionada!!.contains("-")) {
            try {
                val partes = fechaSeleccionada!!.split("-")
                cal.set(partes[0].toInt(), partes[1].toInt() - 1, partes[2].toInt())
            } catch (_: Exception) {}
        }
        val dpd = android.app.DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                fechaSeleccionada = "%04d-%02d-%02d".format(year, month + 1, dayOfMonth)
                horaSeleccionada = null
                mostrarDatePicker = false
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        )
        dpd.setOnCancelListener { mostrarDatePicker = false }
        dpd.setOnDismissListener { mostrarDatePicker = false }
        LaunchedEffect(Unit) {
            dpd.show()
        }
    }

    if (mostrarTimePicker) {
        val disponibles = slotsDisponibles.filter { it.disponible }
        AlertDialog(
            onDismissRequest = { mostrarTimePicker = false },
            shape            = RoundedCornerShape(20.dp),
            containerColor   = MaterialTheme.colorScheme.surface,
            title = {
                Text(
                    "Horarios Disponibles",
                    fontWeight = FontWeight.Bold,
                    textAlign  = TextAlign.Center,
                    modifier   = Modifier.fillMaxWidth(),
                    style      = MaterialTheme.typography.titleMedium
                )
            },
            text = {
                if (cargandoSlots) {
                    Box(Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    }
                } else if (disponibles.isEmpty()) {
                    Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                        Text(
                            "No hay horarios disponibles para esta fecha.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth().heightIn(max = 220.dp)
                    ) {
                        items(disponibles) { slot ->
                            val esSeleccionado = horaSeleccionada == slot.horaInicio
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (esSeleccionado) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerLow,
                                modifier = Modifier.clickable {
                                    horaSeleccionada = slot.horaInicio
                                    mostrarTimePicker = false
                                }
                            ) {
                                Text(
                                    text = formatearHora(slot.horaInicio),
                                    textAlign = TextAlign.Center,
                                    fontSize = 12.sp,
                                    fontWeight = if (esSeleccionado) FontWeight.Bold else FontWeight.Medium,
                                    color = if (esSeleccionado) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(vertical = 10.dp)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { mostrarTimePicker = false }) { Text("Cancelar") }
            }
        )
    }
}

// ─── ProfesionalesSeleccionDialog ──────────────────────────────────────────

@Composable
private fun ProfesionalesSeleccionDialog(
    sedeId:                Int,
    fecha:                 String,
    hora:                  String,
    duracion:              Int,
    seleccionados:         List<Int>,
    profesionales:         List<Profesional>,
    onCargarProfesionales: (Int) -> Unit,
    onDismiss:             () -> Unit,
    onConfirm:             (List<Int>) -> Unit
) {
    var tempSel by remember { mutableStateOf(seleccionados) }

    LaunchedEffect(sedeId) {
        onCargarProfesionales(sedeId)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape            = RoundedCornerShape(28.dp),
        containerColor   = MaterialTheme.colorScheme.surfaceContainerHigh,
        title            = { Text("Asignar profesionales", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) },
        confirmButton    = {},
        text = {
            Column {
                if (profesionales.isEmpty()) {
                    Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                        Text("Sin profesionales disponibles", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    profesionales.forEach { prof ->
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

// ─── Previews y Datos Ficticios ───────────────────────────────────────────────

private object MisCitasPreviewData {
    val mockCitas = listOf(
        Cita(
            id                      = 101,
            fechaHoraInicio         = "2025-05-20T10:00:00",
            fechaHoraFin            = "2025-05-20T11:00:00",
            notas                   = "Cliente prefiere atención puntual.",
            idEstado                = 1,
            estado                  = "Programada",
            colorEstado             = "#2196F3",
            idProfesional           = 10,
            profesional             = "Dra. Maria Lopez",
            fotoProfesional         = null,
            cargoProfesional        = "Especialista Corporal",
            sede                    = "Sede Principal",
            servicios               = "[{\"id\":1,\"nombre\":\"Masaje Relajante\",\"duracion\":60,\"precio\":120000.0}]",
            total                   = 120000.0,
            idCitaOriginal          = null,
            fechaHoraInicioOriginal = null,
            nombreCliente           = "Ana Gomez",
            cantidadPersonas        = 1,
            idSede                  = 1
        ),
        Cita(
            id                      = 102,
            fechaHoraInicio         = "2025-05-21T15:00:00",
            fechaHoraFin            = "2025-05-21T16:00:00",
            notas                   = null,
            idEstado                = 8,
            estado                  = "Pendiente Asignación",
            colorEstado             = "#FF9800",
            idProfesional           = 0,
            profesional             = "Sin Asignar",
            fotoProfesional         = null,
            cargoProfesional        = null,
            sede                    = "Sede Principal",
            servicios               = "[{\"id\":2,\"nombre\":\"Spa de Parejas\",\"duracion\":90,\"precio\":250000.0}]",
            total                   = 250000.0,
            idCitaOriginal          = null,
            fechaHoraInicioOriginal = null,
            nombreCliente           = "Carlos Perez",
            cantidadPersonas        = 2,
            idSede                  = 1
        )
    )

    val mockEstados = listOf(
        EstadoCita(id = 1, nombre = "Programada", color = "#2196F3", grupo = "Activas"),
        EstadoCita(id = 2, nombre = "Confirmada", color = "#4CAF50", grupo = "Activas"),
        EstadoCita(id = 3, nombre = "Cancelada", color = "#F44336", grupo = "Inactivas")
    )

    val mockMetodosPago = listOf(
        MetodoPago(id = 1, nombre = "Efectivo", detalles = emptyList()),
        MetodoPago(id = 2, nombre = "Transferencia", detalles = listOf(MetodoPagoDetalle(id = 10, nombre = "Bancolombia")))
    )
}

@Preview(showBackground = true)
@Composable
private fun MisCitasPreviewLoading() {
    AppDynamicTheme {
        MisCitasContent(
            uiState                   = MisCitasUiState.Loading,
            accionState               = AccionUiState.Idle,
            metodosPago               = emptyList(),
            filtros                   = FiltrosMisCitas(),
            estados                   = emptyList(),
            showFiltros               = false,
            profesionales             = emptyList(),
            modoVista                 = ModoVistaCitas.LISTA,
            fechaCalendario           = LocalDate.now(),
            slotsDisponibles          = emptyList(),
            cargandoSlots             = false,
            clientesEncontrados       = emptyList(),
            onBack                    = {},
            onVerHistorial            = {},
            onVerReagendamientos      = {},
            onCargar                  = {},
            onLimpiarFiltros          = {},
            onToggleFiltros           = {},
            onAplicarFiltros          = {},
            onToggleModoVista         = {},
            onFechaCalendarioChange   = {},
            onCancelarCita            = {},
            onReagendarCitaDirecto    = { _, _, _, _ -> },
            onFinalizarCita           = { _, _, _, _, _ -> },
            onAsignarCitaGrupal       = { _, _, _, _ -> },
            onCargarDisponibilidad    = { _, _, _, _ -> },
            onBuscarClientes          = {},
            onWhatsApp                = {},
            onResetAccion             = {},
            onCargarProfesionales     = { _, _ -> }
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MisCitasPreviewEmptySinFiltros() {
    AppDynamicTheme {
        MisCitasContent(
            uiState                   = MisCitasUiState.Empty,
            accionState               = AccionUiState.Idle,
            metodosPago               = emptyList(),
            filtros                   = FiltrosMisCitas(esFiltroManual = false),
            estados                   = emptyList(),
            showFiltros               = false,
            profesionales             = emptyList(),
            modoVista                 = ModoVistaCitas.LISTA,
            fechaCalendario           = LocalDate.now(),
            slotsDisponibles          = emptyList(),
            cargandoSlots             = false,
            clientesEncontrados       = emptyList(),
            onBack                    = {},
            onVerHistorial            = {},
            onVerReagendamientos      = {},
            onCargar                  = {},
            onLimpiarFiltros          = {},
            onToggleFiltros           = {},
            onAplicarFiltros          = {},
            onToggleModoVista         = {},
            onFechaCalendarioChange   = {},
            onCancelarCita            = {},
            onReagendarCitaDirecto    = { _, _, _, _ -> },
            onFinalizarCita           = { _, _, _, _, _ -> },
            onAsignarCitaGrupal       = { _, _, _, _ -> },
            onCargarDisponibilidad    = { _, _, _, _ -> },
            onBuscarClientes          = {},
            onWhatsApp                = {},
            onResetAccion             = {},
            onCargarProfesionales     = { _, _ -> }
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MisCitasPreviewEmptyConFiltros() {
    AppDynamicTheme {
        MisCitasContent(
            uiState                   = MisCitasUiState.Empty,
            accionState               = AccionUiState.Idle,
            metodosPago               = emptyList(),
            filtros                   = FiltrosMisCitas(esFiltroManual = true, nombreCliente = "Ana"),
            estados                   = emptyList(),
            showFiltros               = false,
            profesionales             = emptyList(),
            modoVista                 = ModoVistaCitas.LISTA,
            fechaCalendario           = LocalDate.now(),
            slotsDisponibles          = emptyList(),
            cargandoSlots             = false,
            clientesEncontrados       = emptyList(),
            onBack                    = {},
            onVerHistorial            = {},
            onVerReagendamientos      = {},
            onCargar                  = {},
            onLimpiarFiltros          = {},
            onToggleFiltros           = {},
            onAplicarFiltros          = {},
            onToggleModoVista         = {},
            onFechaCalendarioChange   = {},
            onCancelarCita            = {},
            onReagendarCitaDirecto    = { _, _, _, _ -> },
            onFinalizarCita           = { _, _, _, _, _ -> },
            onAsignarCitaGrupal       = { _, _, _, _ -> },
            onCargarDisponibilidad    = { _, _, _, _ -> },
            onBuscarClientes          = {},
            onWhatsApp                = {},
            onResetAccion             = {},
            onCargarProfesionales     = { _, _ -> }
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MisCitasPreviewError() {
    AppDynamicTheme {
        MisCitasContent(
            uiState                   = MisCitasUiState.Error("No se pudo establecer conexión con el servidor. Por favor reintenta."),
            accionState               = AccionUiState.Idle,
            metodosPago               = emptyList(),
            filtros                   = FiltrosMisCitas(),
            estados                   = emptyList(),
            showFiltros               = false,
            profesionales             = emptyList(),
            modoVista                 = ModoVistaCitas.LISTA,
            fechaCalendario           = LocalDate.now(),
            slotsDisponibles          = emptyList(),
            cargandoSlots             = false,
            clientesEncontrados       = emptyList(),
            onBack                    = {},
            onVerHistorial            = {},
            onVerReagendamientos      = {},
            onCargar                  = {},
            onLimpiarFiltros          = {},
            onToggleFiltros           = {},
            onAplicarFiltros          = {},
            onToggleModoVista         = {},
            onFechaCalendarioChange   = {},
            onCancelarCita            = {},
            onReagendarCitaDirecto    = { _, _, _, _ -> },
            onFinalizarCita           = { _, _, _, _, _ -> },
            onAsignarCitaGrupal       = { _, _, _, _ -> },
            onCargarDisponibilidad    = { _, _, _, _ -> },
            onBuscarClientes          = {},
            onWhatsApp                = {},
            onResetAccion             = {},
            onCargarProfesionales     = { _, _ -> }
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MisCitasPreviewSuccess() {
    AppDynamicTheme {
        MisCitasContent(
            uiState                   = MisCitasUiState.Success(MisCitasPreviewData.mockCitas),
            accionState               = AccionUiState.Idle,
            metodosPago               = MisCitasPreviewData.mockMetodosPago,
            filtros                   = FiltrosMisCitas(),
            estados                   = MisCitasPreviewData.mockEstados,
            showFiltros               = false,
            profesionales             = emptyList(),
            modoVista                 = ModoVistaCitas.LISTA,
            fechaCalendario           = LocalDate.now(),
            slotsDisponibles          = emptyList(),
            cargandoSlots             = false,
            clientesEncontrados       = emptyList(),
            onBack                    = {},
            onVerHistorial            = {},
            onVerReagendamientos      = {},
            onCargar                  = {},
            onLimpiarFiltros          = {},
            onToggleFiltros           = {},
            onAplicarFiltros          = {},
            onToggleModoVista         = {},
            onFechaCalendarioChange   = {},
            onCancelarCita            = {},
            onReagendarCitaDirecto    = { _, _, _, _ -> },
            onFinalizarCita           = { _, _, _, _, _ -> },
            onAsignarCitaGrupal       = { _, _, _, _ -> },
            onCargarDisponibilidad    = { _, _, _, _ -> },
            onBuscarClientes          = {},
            onWhatsApp                = {},
            onResetAccion             = {},
            onCargarProfesionales     = { _, _ -> }
        )
    }
}
