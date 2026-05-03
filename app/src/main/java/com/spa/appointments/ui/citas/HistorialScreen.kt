// Ruta: app/src/main/java/com/spa/appointments/ui/citas/HistorialScreen.kt
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.spa.appointments.domain.model.Cita
import com.spa.appointments.domain.model.EstadoCita

// ─── Screen principal ─────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistorialScreen(
    onBack: () -> Unit,
    vm: HistorialViewModel = hiltViewModel()
) {
    val uiState     by vm.uiState.collectAsState()
    val filtros     by vm.filtros.collectAsState()
    val estados     by vm.estados.collectAsState()
    val showFiltros by vm.mostrarFiltros.collectAsState()

    var filtrosTemp by remember { mutableStateOf(FiltrosHistorial()) }

    // Conteo para subtítulo
    val totalCitas = (uiState as? HistorialUiState.Success)?.citas?.size

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text       = "Historial de citas",
                            style      = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        if (totalCitas != null) {
                            Text(
                                text  = "$totalCitas ${if (totalCitas == 1) "registro" else "registros"}",
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
                            contentDescription = "Filtrar",
                            tint = if (filtros.activo) MaterialTheme.colorScheme.primary
                            else LocalContentColor.current
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
                    estados   = estados,
                    onChange  = { filtrosTemp = it },
                    onAplicar = { vm.aplicarFiltros(filtrosTemp) },
                    onCerrar  = { vm.toggleFiltros() }
                )
            }

            // Banner de filtros activos
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
                            onClick      = { vm.limpiarFiltros() },
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
                            modifier            = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            CircularProgressIndicator()
                            Text(
                                text  = "Cargando historial…",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    is HistorialUiState.Empty -> {
                        EmptyHistorial(
                            conFiltros = filtros.activo,
                            modifier   = Modifier.align(Alignment.Center)
                        )
                    }

                    is HistorialUiState.Error -> {
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
                                    imageVector        = Icons.Default.CloudOff,
                                    contentDescription = null,
                                    modifier           = Modifier
                                        .padding(16.dp)
                                        .size(32.dp),
                                    tint               = MaterialTheme.colorScheme.onErrorContainer
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

                    is HistorialUiState.Success -> {
                        LazyColumn(
                            contentPadding      = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
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

// ─── Panel de filtros ─────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FiltrosPanel(
    filtros:   FiltrosHistorial,
    estados:   List<EstadoCita>,
    onChange:  (FiltrosHistorial) -> Unit,
    onAplicar: () -> Unit,
    onCerrar:  () -> Unit
) {
    val context = LocalContext.current

    Surface(
        tonalElevation  = 4.dp,
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {

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
                IconButton(onClick = onCerrar, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Cerrar", modifier = Modifier.size(18.dp))
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

            Spacer(Modifier.height(10.dp))

            EstadoDropdown(
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
                OutlinedButton(
                    onClick = onCerrar,
                    shape   = RoundedCornerShape(10.dp)
                ) { Text("Cancelar") }
                Button(
                    onClick = onAplicar,
                    shape   = RoundedCornerShape(10.dp)
                ) { Text("Aplicar filtros") }
            }
        }
    }
}

// ─── DatePicker ───────────────────────────────────────────────────────────────

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
        shape         = RoundedCornerShape(12.dp),
        trailingIcon  = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = {
                    DatePickerDialog(
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
                        Icon(
                            imageVector        = Icons.Default.Clear,
                            contentDescription = "Limpiar fecha",
                            modifier           = Modifier.size(16.dp)
                        )
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

// ─── Tarjeta de cita ──────────────────────────────────────────────────────────

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
        shape     = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors    = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        // Barra de color del estado en el borde izquierdo
        Row(modifier = Modifier.fillMaxWidth()) {
            // Acento de color lateral
            Surface(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight(),
                color    = colorEstado
            ) {}

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 14.dp, vertical = 12.dp)
            ) {

                // ── Fila superior: chip de estado + fecha ─────────────────
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
                            imageVector        = Icons.Default.CalendarToday,
                            contentDescription = null,
                            modifier           = Modifier.size(11.dp),
                            tint               = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.width(3.dp))
                        Text(
                            text  = formatearFechaHistorial(cita.fechaHoraInicio),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // ── Banner de reagendamiento ───────────────────────────────
                if (esReagendada && cita.fechaHoraInicioOriginal != null) {
                    Spacer(Modifier.height(8.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
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
                                text  = "Reagendada · original: ${formatearFechaHistorial(cita.fechaHoraInicioOriginal)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                    }
                }

                Spacer(Modifier.height(10.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Spacer(Modifier.height(10.dp))

                // ── Cliente ───────────────────────────────────────────────
                if (!cita.nombreCliente.isNullOrBlank()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier          = Modifier.padding(bottom = 6.dp)
                    ) {
                        Icon(
                            imageVector        = Icons.Default.Person,
                            contentDescription = null,
                            modifier           = Modifier.size(14.dp),
                            tint               = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.width(5.dp))
                        Text(
                            text       = cita.nombreCliente,
                            style      = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
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
                        modifier           = Modifier.size(14.dp),
                        tint               = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.width(5.dp))
                    Text(
                        text       = cita.profesional,
                        style      = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color      = MaterialTheme.colorScheme.onSurface
                    )
                }

                // ── Hora y sede ───────────────────────────────────────────
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier          = Modifier.padding(bottom = 8.dp)
                ) {
                    Icon(
                        imageVector        = Icons.Default.AccessTime,
                        contentDescription = null,
                        modifier           = Modifier.size(14.dp),
                        tint               = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.width(5.dp))
                    Text(
                        text  = "${formatearHoraHistorial(cita.fechaHoraInicio)} · ${cita.sede}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // ── Total con fondo destacado ─────────────────────────────
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
                            text  = "Total",
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

// ─── Empty state ──────────────────────────────────────────────────────────────

@Composable
private fun EmptyHistorial(
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
                else Icons.Default.History,
                contentDescription = null,
                modifier           = Modifier
                    .padding(20.dp)
                    .size(48.dp),
                tint               = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text       = if (conFiltros) "Sin resultados" else "Sin historial aún",
            style      = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text      = if (conFiltros) "Ninguna cita coincide con los filtros aplicados."
            else "Aquí aparecerán las citas pasadas.",
            style     = MaterialTheme.typography.bodyMedium,
            color     = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

// ─── Helpers de formato ───────────────────────────────────────────────────────

private fun formatearFechaHistorial(fechaIso: String): String = try {
    val p = fechaIso.substring(0, 10).split("-")
    "${p[2]}/${p[1]}/${p[0]}"
} catch (e: Exception) { fechaIso }

private fun formatearHoraHistorial(fechaIso: String): String = try {
    fechaIso.substring(11, 16)
} catch (e: Exception) { fechaIso }