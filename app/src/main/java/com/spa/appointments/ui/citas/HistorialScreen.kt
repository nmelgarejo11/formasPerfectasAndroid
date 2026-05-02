package com.spa.appointments.ui.citas

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.spa.appointments.domain.model.Cita

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistorialScreen(
    onBack: () -> Unit,
    vm: HistorialViewModel = hiltViewModel()
) {
    val uiState     by vm.uiState.collectAsState()
    val filtros     by vm.filtros.collectAsState()
    val showFiltros by vm.mostrarFiltros.collectAsState()

    // Estado temporal del panel de filtros (no se aplica hasta confirmar)
    var filtrosTemp by remember { mutableStateOf(FiltrosHistorial()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Historial de citas") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Volver")
                    }
                },
                actions = {
                    // Indicador visual cuando hay filtros activos
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
                            imageVector = if (filtros.activo) Icons.Default.FilterAlt
                            else Icons.Default.FilterList,
                            contentDescription = "Filtrar"
                        )
                    }
                    IconButton(onClick = { vm.cargar() }) {
                        Icon(Icons.Default.Refresh, "Recargar")
                    }
                }
            )
        }
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
                FiltrosPanel(
                    filtros   = filtrosTemp,
                    onChange  = { filtrosTemp = it },
                    onAplicar = { vm.aplicarFiltros(filtrosTemp) },
                    onCerrar  = { vm.toggleFiltros() }
                )
            }

            Box(modifier = Modifier.fillMaxSize()) {
                when (val state = uiState) {
                    is HistorialUiState.Loading -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                    is HistorialUiState.Empty -> {
                        EmptyHistorial(
                            conFiltros = filtros.activo,
                            modifier   = Modifier.align(Alignment.Center)
                        )
                    }
                    is HistorialUiState.Error -> {
                        Column(
                            modifier = Modifier.align(Alignment.Center).padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(state.mensaje, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
                            Spacer(Modifier.height(16.dp))
                            Button(onClick = { vm.cargar() }) { Text("Reintentar") }
                        }
                    }
                    is HistorialUiState.Success -> {
                        LazyColumn(
                            contentPadding     = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
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

// ─── Panel de filtros ────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FiltrosPanel(
    filtros:   FiltrosHistorial,
    onChange:  (FiltrosHistorial) -> Unit,
    onAplicar: () -> Unit,
    onCerrar:  () -> Unit
) {
    val context = LocalContext.current

    Surface(tonalElevation = 4.dp, shadowElevation = 2.dp) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {

            Text("Filtrar historial",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold)

            Spacer(Modifier.height(12.dp))

            // ── Búsqueda por cliente ──────────────────────────────────────
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
                singleLine    = true,
                modifier      = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))

            // ── Fechas con DatePickerDialog ───────────────────────────────
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FiltroDatePicker(
                    label    = "Desde",
                    value    = filtros.fechaDesde,
                    modifier = Modifier.weight(1f),
                    context  = context,
                    onDate   = { onChange(filtros.copy(fechaDesde = it)) }
                )
                FiltroDatePicker(
                    label    = "Hasta",
                    value    = filtros.fechaHasta,
                    modifier = Modifier.weight(1f),
                    context  = context,
                    onDate   = { onChange(filtros.copy(fechaHasta = it)) }
                )
            }

            Spacer(Modifier.height(8.dp))

            // ── Estado — lista desplegable ────────────────────────────────
            EstadoDropdown(
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

// ── DatePicker ────────────────────────────────────────────────────────────────

@Composable
private fun FiltroDatePicker(
    label:    String,
    value:    String?,
    modifier: Modifier = Modifier,
    context:  android.content.Context,
    onDate:   (String?) -> Unit
) {
    val calendar = remember { java.util.Calendar.getInstance() }

    OutlinedTextField(
        value         = value?.let { formatearFechaHistorial(it) } ?: "",
        onValueChange = {},
        readOnly      = true,
        label         = { Text(label) },
        trailingIcon  = {
            Row {
                IconButton(onClick = {
                    android.app.DatePickerDialog(
                        context,
                        { _, y, m, d ->
                            onDate("%04d-%02d-%02d".format(y, m + 1, d))
                        },
                        calendar.get(java.util.Calendar.YEAR),
                        calendar.get(java.util.Calendar.MONTH),
                        calendar.get(java.util.Calendar.DAY_OF_MONTH)
                    ).show()
                }) {
                    Icon(Icons.Default.CalendarMonth, "Seleccionar fecha")
                }
                if (value != null) {
                    IconButton(onClick = { onDate(null) }) {
                        Icon(Icons.Default.Clear, "Limpiar fecha",
                            modifier = Modifier.size(16.dp))
                    }
                }
            }
        },
        singleLine    = true,
        modifier      = modifier
    )
}

// ── Dropdown de estados ───────────────────────────────────────────────────────

private val estadosHistorial = listOf(
    null  to "Todos",
    3     to "Cancelada",
    5     to "Reagendada",
    6     to "Rechazada",
    7     to "Completada"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EstadoDropdown(
    seleccionadoId:     Int?,
    seleccionadoNombre: String?,
    onSelect:           (Int?, String?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val textoMostrado = seleccionadoNombre ?: "Todos los estados"

    ExposedDropdownMenuBox(
        expanded         = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value         = textoMostrado,
            onValueChange = {},
            readOnly      = true,
            label         = { Text("Estado") },
            trailingIcon  = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier      = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )
        ExposedDropdownMenu(
            expanded         = expanded,
            onDismissRequest = { expanded = false }
        ) {
            estadosHistorial.forEach { (id, nombre) ->
                DropdownMenuItem(
                    text    = { Text(nombre) },
                    onClick = {
                        onSelect(id, if (id == null) null else nombre)
                        expanded = false
                    },
                    leadingIcon = {
                        if (seleccionadoId == id) {
                            Icon(Icons.Default.Check, null,
                                modifier = Modifier.size(16.dp))
                        }
                    }
                )
            }
        }
    }
}

// ─── Tarjeta ─────────────────────────────────────────────────────────────────

@Composable
private fun HistorialCitaCard(cita: Cita) {
    val colorEstado = remember(cita.colorEstado) {
        runCatching {
            Color(android.graphics.Color.parseColor(cita.colorEstado ?: "#888888"))
        }.getOrDefault(Color.Gray)
    }

    val esReagendada = cita.idCitaOriginal != null

    Card(
        modifier  = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors    = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // Fila superior: estado + fecha
            Row(
                modifier = Modifier.fillMaxWidth(),
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
                    text  = formatearFechaHistorial(cita.fechaHoraInicio),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Banner de reagendamiento
            if (esReagendada && cita.fechaHoraInicioOriginal != null) {
                Spacer(Modifier.height(8.dp))
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.tertiaryContainer
                ) {
                    Row(
                        modifier          = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector        = Icons.Default.Update,
                            contentDescription = null,
                            modifier           = Modifier.size(12.dp),
                            tint               = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text  = "Reagendada desde el ${formatearFechaHistorial(cita.fechaHoraInicioOriginal)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            Text(
                text       = cita.profesional,
                style      = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text  = "${formatearHoraHistorial(cita.fechaHoraInicio)} · ${cita.sede}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text       = "Total: ${"$%,.0f".format(cita.total)}",
                style      = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color      = MaterialTheme.colorScheme.primary
            )
        }
    }
}

// ─── Empty state ─────────────────────────────────────────────────────────────

@Composable
private fun EmptyHistorial(conFiltros: Boolean, modifier: Modifier = Modifier) {
    Column(
        modifier           = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector        = if (conFiltros) Icons.Default.SearchOff else Icons.Default.History,
            contentDescription = null,
            modifier           = Modifier.size(64.dp),
            tint               = MaterialTheme.colorScheme.secondary
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text       = if (conFiltros) "Sin resultados" else "Sin historial aún",
            style      = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text      = if (conFiltros) "Ninguna cita coincide con los filtros aplicados."
            else "Aquí aparecerán tus citas pasadas.",
            style     = MaterialTheme.typography.bodyMedium,
            color     = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

// ─── Helpers ─────────────────────────────────────────────────────────────────

private fun formatearFechaHistorial(fechaIso: String): String = try {
    val p = fechaIso.substring(0, 10).split("-")
    "${p[2]}/${p[1]}/${p[0]}"
} catch (e: Exception) { fechaIso }

private fun formatearHoraHistorial(fechaIso: String): String = try {
    fechaIso.substring(11, 16)
} catch (e: Exception) { fechaIso }