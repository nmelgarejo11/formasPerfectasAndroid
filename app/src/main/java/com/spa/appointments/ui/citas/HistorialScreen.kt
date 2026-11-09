// Ruta: app/src/main/java/com/spa/appointments/ui/citas/HistorialScreen.kt
package com.spa.appointments.ui.citas

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.spa.appointments.domain.model.Cita
import com.spa.appointments.domain.model.EstadoCita
import com.spa.appointments.domain.model.Profesional
import com.spa.appointments.ui.citas.components.CalendarioDiaView
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

// ─── Screen Principal (Stateful) ──────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistorialScreen(
    onBack: () -> Unit,
    vm: HistorialViewModel = hiltViewModel()
) {
    val uiState         by vm.uiState.collectAsState()
    val filtros         by vm.filtros.collectAsState()
    val estados         by vm.estados.collectAsState()
    val profesionales   by vm.profesionales.collectAsState()
    val showFiltros     by vm.mostrarFiltros.collectAsState()
    val modoVista       by vm.modoVista.collectAsState()
    val fechaCalendario by vm.fechaCalendario.collectAsState()

    var filtrosTemp      by remember { mutableStateOf(FiltrosHistorial()) }
    var citaSeleccionada by remember { mutableStateOf<Cita?>(null) }
    var mostrarDetalle    by remember { mutableStateOf(false) }

    // Conteo para subtítulo
    val totalCitas = (uiState as? HistorialUiState.Success)?.citas?.size

    // ── Pop-Up Detalle Cita (Desde vista Calendario) ──────────────────────────
    if (mostrarDetalle && citaSeleccionada != null) {
        val c = citaSeleccionada!!
        AlertDialog(
            onDismissRequest = { mostrarDetalle = false },
            shape            = RoundedCornerShape(28.dp),
            containerColor   = MaterialTheme.colorScheme.surfaceContainerHigh,
            text = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    HistorialCitaCard(cita = c)
                }
            },
            confirmButton = {
                OutlinedButton(
                    onClick  = { mostrarDetalle = false },
                    shape    = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Cerrar")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text(
                                text       = "Historial de citas",
                                style      = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            if (totalCitas != null) {
                                Surface(
                                    shape    = RoundedCornerShape(50),
                                    color    = MaterialTheme.colorScheme.primaryContainer,
                                    modifier = Modifier.padding(top = 2.dp)
                                ) {
                                    Text(
                                        text     = "$totalCitas ${if (totalCitas == 1) "registro" else "registros"}",
                                        style    = MaterialTheme.typography.labelSmall,
                                        color    = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 1.dp)
                                    )
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
                        onClick  = { vm.toggleModoVista() },
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
                                imageVector        = if (filtros.activo) Icons.Default.FilterAlt else Icons.Default.FilterList,
                                contentDescription = "Filtrar",
                                tint               = if (filtros.activo) MaterialTheme.colorScheme.primary else LocalContentColor.current
                            )
                        }
                    }
                    IconButton(onClick = { vm.cargar() }) {
                        Icon(Icons.Default.Refresh, "Recargar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // 🎨 Canvas: Acento sutil en barra decorativa superior
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

            // Panel de filtros animado
            AnimatedVisibility(
                visible = showFiltros && modoVista == ModoVistaCitas.LISTA,
                enter   = expandVertically() + fadeIn(),
                exit    = shrinkVertically() + fadeOut()
            ) {
                FiltrosPanel(
                    filtros       = filtrosTemp,
                    estados       = estados,
                    profesionales = profesionales,
                    onChange      = { filtrosTemp = it },
                    onAplicar     = { vm.aplicarFiltros(filtrosTemp) },
                    onCerrar      = { vm.toggleFiltros() }
                )
            }

            // Banner de filtros activos
            AnimatedVisibility(
                visible = filtros.activo && !showFiltros && modoVista == ModoVistaCitas.LISTA,
                enter   = expandVertically() + fadeIn(),
                exit    = shrinkVertically() + fadeOut()
            ) {
                Surface(
                    color    = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.8f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier          = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector        = Icons.Default.FilterAlt,
                            contentDescription = null,
                            modifier           = Modifier.size(14.dp),
                            tint               = MaterialTheme.colorScheme.onSecondaryContainer
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
                                text  = "Limpiar",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }
            }

            Box(modifier = Modifier.fillMaxSize()) {
                when (val state = uiState) {
                    is HistorialUiState.Loading -> {
                        Column(
                            modifier            = Modifier
                                .align(Alignment.Center)
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            CircularProgressIndicator(
                                color      = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.primaryContainer
                            )
                            Text(
                                text  = "Cargando historial…",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    is HistorialUiState.Empty -> {
                        if (modoVista == ModoVistaCitas.CALENDARIO_DIA) {
                            CalendarioDiaView(
                                citas             = emptyList(),
                                fechaSeleccionada = fechaCalendario,
                                onFechaChange     = { vm.setFechaCalendario(it) },
                                onCitaClick       = { cita ->
                                    citaSeleccionada = cita
                                    mostrarDetalle   = true
                                },
                                mensajeVacio      = "Sin historial aún para este día"
                            )
                        } else {
                            EmptyHistorial(
                                conFiltros = filtros.activo,
                                modifier   = Modifier.align(Alignment.Center)
                            )
                        }
                    }

                    is HistorialUiState.Error -> {
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
                                        imageVector        = Icons.Default.CloudOff,
                                        contentDescription = null,
                                        modifier           = Modifier
                                            .padding(16.dp)
                                            .size(36.dp),
                                        tint               = MaterialTheme.colorScheme.onErrorContainer
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
                                    style     = MaterialTheme.typography.bodySmall
                                )
                                Button(
                                    onClick = { vm.cargar() },
                                    shape   = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(
                                        imageVector        = Icons.Default.Refresh,
                                        contentDescription = null,
                                        modifier           = Modifier.size(16.dp)
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text("Reintentar")
                                }
                            }
                        }
                    }

                    is HistorialUiState.Success -> {
                        if (modoVista == ModoVistaCitas.CALENDARIO_DIA) {
                            CalendarioDiaView(
                                citas             = state.citas,
                                fechaSeleccionada = fechaCalendario,
                                onFechaChange     = { vm.setFechaCalendario(it) },
                                onCitaClick       = { cita ->
                                    citaSeleccionada = cita
                                    mostrarDetalle   = true
                                },
                                mensajeVacio      = "Sin historial aún para este día"
                            )
                        } else {
                            LazyColumn(
                                contentPadding      = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                items(state.citas, key = { it.id }) { cita ->
                                    HistorialCitaCard(cita = cita)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ─── Panel de Filtros ─────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FiltrosPanel(
    filtros:       FiltrosHistorial,
    estados:       List<EstadoCita>,
    profesionales: List<Profesional>,
    onChange:      (FiltrosHistorial) -> Unit,
    onAplicar:     () -> Unit,
    onCerrar:      () -> Unit
) {
    Surface(
        color    = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier            = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier              = Modifier.fillMaxWidth()
            ) {
                Text(
                    text       = "Filtrar historial",
                    style      = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onCerrar, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Cerrar", modifier = Modifier.size(18.dp))
                }
            }

            OutlinedTextField(
                value         = filtros.nombreCliente ?: "",
                onValueChange = { onChange(filtros.copy(nombreCliente = it.ifBlank { null })) },
                label         = { Text("Buscar cliente") },
                leadingIcon   = { Icon(Icons.Default.Search, null, modifier = Modifier.size(18.dp)) },
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

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                FiltroDatePicker(
                    label    = "Desde",
                    value    = filtros.fechaDesde,
                    modifier = Modifier.weight(1f),
                    onDate   = { onChange(filtros.copy(fechaDesde = it)) }
                )
                FiltroDatePicker(
                    label    = "Hasta",
                    value    = filtros.fechaHasta,
                    modifier = Modifier.weight(1f),
                    onDate   = { onChange(filtros.copy(fechaHasta = it)) }
                )
            }

            EstadoDropdown(
                estados            = estados,
                seleccionadoId     = filtros.idEstado,
                seleccionadoNombre = filtros.nombreEstado,
                onSelect           = { id, nombre ->
                    onChange(filtros.copy(idEstado = id, nombreEstado = nombre))
                }
            )

            ProfesionalDropdown(
                profesionales      = profesionales,
                seleccionadoId     = filtros.idProfesional,
                seleccionadoNombre = filtros.nombreProfesional,
                onSelect           = { id, nombre ->
                    onChange(filtros.copy(idProfesional = id, nombreProfesional = nombre))
                }
            )

            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment     = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick  = { onChange(FiltrosHistorial()); onAplicar() },
                    modifier = Modifier.weight(1f),
                    shape    = RoundedCornerShape(10.dp)
                ) { Text("Limpiar") }
                Button(
                    onClick  = onAplicar,
                    modifier = Modifier.weight(1f),
                    shape    = RoundedCornerShape(10.dp)
                ) { Text("Aplicar filtros") }
            }
        }
    }
}

// ─── Selector de Fecha para Filtros ──────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FiltroDatePicker(
    label:    String,
    value:    String?,
    modifier: Modifier = Modifier,
    onDate:   (String?) -> Unit
) {
    var mostrarPicker by remember { mutableStateOf(false) }

    val initialMillis = remember(value) {
        value?.let { iso ->
            runCatching {
                val (y, m, d) = iso.split("-").map { it.toInt() }
                Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                    clear()
                    set(y, m - 1, d)
                }.timeInMillis
            }.getOrNull()
        }
    }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialMillis
    )

    OutlinedTextField(
        value         = value?.let { formatearFechaHistorial(it) } ?: "",
        onValueChange = {},
        readOnly      = true,
        label         = { Text(label) },
        shape         = RoundedCornerShape(12.dp),
        trailingIcon  = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { mostrarPicker = true }) {
                    Icon(Icons.Default.CalendarMonth, "Seleccionar $label")
                }
                if (value != null) {
                    IconButton(onClick = { onDate(null) }) {
                        Icon(
                            Icons.Default.Clear,
                            "Limpiar $label",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        },
        singleLine = true,
        modifier   = modifier
    )

    if (mostrarPicker) {
        DatePickerDialog(
            onDismissRequest = { mostrarPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply { timeInMillis = millis }
                        val iso = "%04d-%02d-%02d".format(
                            cal.get(Calendar.YEAR),
                            cal.get(Calendar.MONTH) + 1,
                            cal.get(Calendar.DAY_OF_MONTH)
                        )
                        onDate(iso)
                    }
                    mostrarPicker = false
                }) { Text("Aceptar") }
            },
            dismissButton = {
                TextButton(onClick = { mostrarPicker = false }) { Text("Cancelar") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

// ─── Dropdown de Estados ──────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EstadoDropdown(
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
            modifier      = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )
        ExposedDropdownMenu(
            expanded         = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("Todos los estados") },
                onClick = {
                    onSelect(null, null)
                    expanded = false
                },
                leadingIcon = {
                    if (seleccionadoId == null)
                        Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp))
                }
            )
            if (estados.isNotEmpty()) HorizontalDivider()
            estados.forEach { estado ->
                val defaultColor = MaterialTheme.colorScheme.primary
                val colorEstado  = remember(estado.color, defaultColor) {
                    runCatching {
                        Color(android.graphics.Color.parseColor(estado.color))
                    }.getOrDefault(defaultColor)
                }
                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                modifier = Modifier.size(8.dp),
                                shape    = CircleShape,
                                color    = colorEstado
                            ) {}
                            Spacer(Modifier.width(8.dp))
                            Text(estado.nombre)
                        }
                    },
                    onClick = {
                        onSelect(estado.id, estado.nombre)
                        expanded = false
                    },
                    leadingIcon = {
                        if (seleccionadoId == estado.id)
                            Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp))
                    }
                )
            }
        }
    }
}

// ─── Dropdown de Profesional ──────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfesionalDropdown(
    profesionales:      List<Profesional>,
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
            value         = seleccionadoNombre ?: "Todos los profesionales",
            onValueChange = {},
            readOnly      = true,
            label         = { Text("Profesional") },
            shape         = RoundedCornerShape(12.dp),
            trailingIcon  = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier      = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )
        ExposedDropdownMenu(
            expanded         = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("Todos los profesionales") },
                onClick = {
                    onSelect(null, null)
                    expanded = false
                },
                leadingIcon = {
                    if (seleccionadoId == null)
                        Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp))
                }
            )
            if (profesionales.isNotEmpty()) HorizontalDivider()
            profesionales.forEach { prof ->
                val nombreMostrar = prof.nombreCompleto.ifBlank { "${prof.nombre} ${prof.apellido}" }
                DropdownMenuItem(
                    text = { Text(nombreMostrar) },
                    onClick = {
                        onSelect(prof.id, nombreMostrar)
                        expanded = false
                    },
                    leadingIcon = {
                        if (seleccionadoId == prof.id)
                            Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp))
                    }
                )
            }
        }
    }
}

// ─── Tarjeta de Cita M3 ────────────────────────────────────────────────────────

@Composable
private fun HistorialCitaCard(cita: Cita) {
    val defaultPrimary = MaterialTheme.colorScheme.primary
    val colorEstado    = remember(cita.colorEstado, defaultPrimary) {
        runCatching {
            Color(android.graphics.Color.parseColor(cita.colorEstado ?: "#888888"))
        }.getOrDefault(defaultPrimary)
    }

    val esReagendada = cita.idCitaOriginal != null

    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(18.dp),
        colors   = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
        )
    ) {
        Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {

            // Acento de color lateral
            Box(
                modifier = Modifier
                    .width(6.dp)
                    .fillMaxHeight()
                    .background(colorEstado)
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 14.dp, vertical = 12.dp)
            ) {

                // ── Fila Superior: Chip de Estado + Fecha ───────────────────
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = colorEstado.copy(alpha = 0.15f)
                    ) {
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
                            imageVector        = Icons.Default.CalendarToday,
                            contentDescription = null,
                            modifier           = Modifier.size(13.dp),
                            tint               = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text       = formatearFechaHistorial(cita.fechaHoraInicio),
                            style      = MaterialTheme.typography.labelSmall,
                            color      = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // ── Banner de Reagendamiento ───────────────────────────────
                if (esReagendada && cita.fechaHoraInicioOriginal != null) {
                    Spacer(Modifier.height(8.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.7f)
                    ) {
                        Row(
                            modifier          = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector        = Icons.Default.Update,
                                contentDescription = null,
                                modifier           = Modifier.size(14.dp),
                                tint               = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text       = "Reagendada · original: ${formatearFechaHistorial(cita.fechaHoraInicioOriginal)}",
                                style      = MaterialTheme.typography.labelSmall,
                                color      = MaterialTheme.colorScheme.onTertiaryContainer,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                Spacer(Modifier.height(8.dp))

                // ── Cliente ───────────────────────────────────────────────
                if (!cita.nombreCliente.isNullOrBlank()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier          = Modifier.padding(bottom = 4.dp)
                    ) {
                        Icon(
                            imageVector        = Icons.Default.Person,
                            contentDescription = null,
                            modifier           = Modifier.size(15.dp),
                            tint               = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text       = cita.nombreCliente,
                            style      = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color      = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // ── Profesional ───────────────────────────────────────────
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier          = Modifier.padding(bottom = 4.dp)
                ) {
                    Icon(
                        imageVector        = Icons.Default.Badge,
                        contentDescription = null,
                        modifier           = Modifier.size(15.dp),
                        tint               = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text       = cita.profesional,
                        style      = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color      = MaterialTheme.colorScheme.onSurface
                    )
                }

                // ── Hora y Sede ───────────────────────────────────────────
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier          = Modifier.padding(bottom = 8.dp)
                ) {
                    Icon(
                        imageVector        = Icons.Default.AccessTime,
                        contentDescription = null,
                        modifier           = Modifier.size(15.dp),
                        tint               = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text  = "${formatearHoraHistorial(cita.fechaHoraInicio)} · ${cita.sede}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // ── Total con Fondo Destacado ─────────────────────────────
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Row(
                        modifier          = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        Text(
                            text  = "Total Cobrado",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text       = "${"$%,.0f".format(cita.total)}",
                            style      = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color      = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontSize   = 13.sp
                        )
                    }
                }
            }
        }
    }
}

// ─── Empty State con Acento Canvas ────────────────────────────────────────────

@Composable
private fun EmptyHistorial(
    conFiltros: Boolean,
    modifier:   Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary

    OutlinedCard(
        modifier = modifier
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
                        imageVector        = if (conFiltros) Icons.Default.SearchOff else Icons.Default.History,
                        contentDescription = null,
                        modifier           = Modifier
                            .padding(18.dp)
                            .size(36.dp),
                        tint               = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Text(
                text       = if (conFiltros) "Sin resultados" else "Sin historial aún",
                style      = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text      = if (conFiltros) "Ninguna cita coincide con los filtros aplicados." else "Aquí aparecerán los registros pasados.",
                style     = MaterialTheme.typography.bodyMedium,
                color     = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

// ─── Helpers de Formato ───────────────────────────────────────────────────────

private fun formatearFechaHistorial(fechaIso: String): String = try {
    val p = fechaIso.substring(0, 10).split("-")
    "${p[2]}/${p[1]}/${p[0]}"
} catch (e: Exception) { fechaIso }

private fun formatearHoraHistorial(fechaIso: String): String = try {
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
} catch (e: Exception) { fechaIso }
