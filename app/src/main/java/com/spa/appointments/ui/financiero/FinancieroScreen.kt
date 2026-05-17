package com.spa.appointments.ui.financiero

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.spa.appointments.domain.model.CierreCajaGasto
import com.spa.appointments.domain.model.CierreCajaProfesional
import com.spa.appointments.domain.model.CierreCajaResumen
import com.spa.appointments.domain.model.Sede
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinancieroScreen(
    onBack: () -> Unit,
    vm: FinancieroViewModel = hiltViewModel()
) {
    val uiState           by vm.uiState.collectAsState()
    val fechaSeleccionada by vm.fechaSeleccionada.collectAsState()
    val sedes             by vm.sedes.collectAsState()
    val sedeSeleccionada  by vm.sedeSeleccionada.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Resumen financiero",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            // ── Selector de sede ──────────────────────────────────────
            SelectorSede(
                sedes            = sedes,
                sedeSeleccionada = sedeSeleccionada,
                onSedeSeleccionada = { vm.seleccionarSede(it) }
            )

            Spacer(Modifier.height(8.dp))

            // ── Selector de fecha con calendario ─────────────────────
            SelectorFechaCalendario(
                fecha          = fechaSeleccionada,
                onCambiarFecha = { vm.cambiarFecha(it) }
            )

            Spacer(Modifier.height(16.dp))

            // ── Contenido ─────────────────────────────────────────────
            when (val state = uiState) {
                is FinancieroUiState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is FinancieroUiState.Error -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(state.mensaje, color = MaterialTheme.colorScheme.error)
                            Spacer(Modifier.height(12.dp))
                            Button(onClick = { vm.cargarCierreCaja() }) { Text("Reintentar") }
                        }
                    }
                }
                is FinancieroUiState.Success -> {
                    CierreCajaContent(
                        resumen       = state.data.resumen,
                        profesionales = state.data.profesionales,
                        ultimosGastos = state.data.ultimosGastos
                    )
                }
                else -> Unit
            }
        }
    }
}

// ─── Selector de sede (Dropdown) ──────────────────────────────────────────────

@Composable
private fun SelectorSede(
    sedes              : List<Sede>,
    sedeSeleccionada   : Sede?,
    onSedeSeleccionada : (Sede) -> Unit
) {
    var expandido by remember { mutableStateOf(false) }

    OutlinedCard(
        onClick = { expandido = true },
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier          = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text     = "Sede",
                    fontSize = 11.sp,
                    color    = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text       = sedeSeleccionada?.nombre ?: "Seleccionar sede",
                    fontWeight = FontWeight.Medium,
                    fontSize   = 14.sp
                )
            }
            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
        }
    }

    DropdownMenu(
        expanded         = expandido,
        onDismissRequest = { expandido = false },
        modifier         = Modifier.fillMaxWidth(0.9f)
    ) {
        sedes.forEach { sede ->
            DropdownMenuItem(
                text = {
                    Column {
                        Text(sede.nombre, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                    }
                },
                onClick = {
                    onSedeSeleccionada(sede)
                    expandido = false
                }
            )
        }
    }
}

// ─── Selector de fecha con DatePickerDialog ───────────────────────────────────

@Composable
private fun SelectorFechaCalendario(
    fecha          : LocalDate,
    onCambiarFecha : (LocalDate) -> Unit
) {
    val context   = LocalContext.current
    val formatter = DateTimeFormatter.ofPattern("dd 'de' MMMM, yyyy")

    val datePickerDialog = remember(fecha) {
        DatePickerDialog(
            context,
            { _, year, month, day ->
                onCambiarFecha(LocalDate.of(year, month + 1, day))
            },
            fecha.year,
            fecha.monthValue - 1,
            fecha.dayOfMonth
        ).apply {
            // No permite fechas futuras
            datePicker.maxDate = System.currentTimeMillis()
        }
    }

    OutlinedCard(
        onClick  = { datePickerDialog.show() },
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier          = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text     = "Fecha",
                    fontSize = 11.sp,
                    color    = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text       = fecha.format(formatter),
                    fontWeight = FontWeight.Medium,
                    fontSize   = 14.sp
                )
                if (fecha == LocalDate.now()) {
                    Text(
                        text     = "Hoy",
                        fontSize = 12.sp,
                        color    = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Icon(Icons.Default.CalendarMonth, contentDescription = "Abrir calendario")
        }
    }
}

// ─── Contenido principal ──────────────────────────────────────────────────────

@Composable
private fun CierreCajaContent(
    resumen       : CierreCajaResumen,
    profesionales : List<CierreCajaProfesional>,
    ultimosGastos : List<CierreCajaGasto>
) {
    LazyColumn(
        modifier            = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding      = PaddingValues(bottom = 24.dp)
    ) {
        item { TarjetasResumen(resumen) }
        item { TarjetaUtilidadNeta(resumen.utilidadNeta) }

        item { SeccionTitulo("Ingresos por profesional") }
        if (profesionales.isEmpty()) {
            item {
                Text(
                    "Sin registros para esta fecha",
                    color    = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp
                )
            }
        } else {
            itemsIndexed(profesionales) { index, prof ->
                FilaProfesional(index + 1, prof)
            }
        }

        item { Spacer(Modifier.height(4.dp)) }
        item { SeccionTitulo("Gastos del día") }
        if (ultimosGastos.isEmpty()) {
            item {
                Text(
                    "Sin gastos registrados",
                    color    = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp
                )
            }
        } else {
            itemsIndexed(ultimosGastos) { _, gasto ->
                FilaGasto(gasto)
            }
        }
    }
}

// ─── Tarjetas resumen ─────────────────────────────────────────────────────────

@Composable
private fun TarjetasResumen(resumen: CierreCajaResumen) {
    Row(
        modifier              = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        TarjetaMetrica(
            modifier = Modifier.weight(1f),
            label    = "Ingresos",
            valor    = "$${"%,.0f".format(resumen.totalIngresos)}",
            subLabel = "${resumen.totalCitas} citas",
            color    = MaterialTheme.colorScheme.primaryContainer
        )
        TarjetaMetrica(
            modifier = Modifier.weight(1f),
            label    = "Gastos",
            valor    = "$${"%,.0f".format(resumen.totalGastos)}",
            subLabel = "${resumen.totalRegistrosGasto} registros",
            color    = MaterialTheme.colorScheme.errorContainer
        )
    }
}

@Composable
private fun TarjetaMetrica(
    modifier : Modifier,
    label    : String,
    valor    : String,
    subLabel : String,
    color    : androidx.compose.ui.graphics.Color
) {
    Card(
        modifier = modifier,
        colors   = CardDefaults.cardColors(containerColor = color),
        shape    = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(label,    fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(4.dp))
            Text(valor,    fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Text(subLabel, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

// ─── Utilidad neta ────────────────────────────────────────────────────────────

@Composable
private fun TarjetaUtilidadNeta(utilidad: Double) {
    val esPositiva     = utilidad >= 0
    val containerColor = if (esPositiva)
        MaterialTheme.colorScheme.primaryContainer
    else
        MaterialTheme.colorScheme.errorContainer

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors   = CardDefaults.cardColors(containerColor = containerColor),
        shape    = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier              = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Text("Utilidad neta del día", fontWeight = FontWeight.Medium, fontSize = 14.sp)
            Text(
                text       = "$${"%,.0f".format(utilidad)}",
                fontSize   = 22.sp,
                fontWeight = FontWeight.Bold,
                color      = if (esPositiva)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.error
            )
        }
    }
}

// ─── Ranking profesionales ────────────────────────────────────────────────────

@Composable
private fun FilaProfesional(posicion: Int, prof: CierreCajaProfesional) {
    Row(
        modifier          = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier         = Modifier
                .size(28.dp)
                .background(
                    MaterialTheme.colorScheme.secondaryContainer,
                    RoundedCornerShape(6.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text("$posicion", fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(prof.nombreProfesional, fontWeight = FontWeight.Medium, fontSize = 14.sp)
            Text("${prof.totalCitas} citas", fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text(
            text       = "$${"%,.0f".format(prof.totalGenerado)}",
            fontWeight = FontWeight.Bold,
            fontSize   = 15.sp
        )
    }
    HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
}

// ─── Lista gastos ─────────────────────────────────────────────────────────────

@Composable
private fun FilaGasto(gasto: CierreCajaGasto) {
    Row(
        modifier          = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(gasto.concepto, fontWeight = FontWeight.Medium, fontSize = 14.sp)
            val fechaDisplay = runCatching {
                LocalDate.parse(gasto.fechaGasto)
                    .format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
            }.getOrElse { gasto.fechaGasto }
            Text(fechaDisplay, fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text(
            text     = "$${"%,.0f".format(gasto.valor)}",
            fontSize = 14.sp,
            color    = MaterialTheme.colorScheme.error
        )
    }
    HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
}

// ─── Helper ───────────────────────────────────────────────────────────────────

@Composable
private fun SeccionTitulo(texto: String) {
    Text(
        text       = texto,
        fontWeight = FontWeight.SemiBold,
        fontSize   = 15.sp,
        modifier   = Modifier.padding(bottom = 4.dp)
    )
}