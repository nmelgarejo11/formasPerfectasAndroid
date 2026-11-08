// Ruta: app/src/main/java/com/spa/appointments/ui/financiero/FinancieroScreen.kt
package com.spa.appointments.ui.financiero

import android.app.DatePickerDialog
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Text(
                            text       = "Resumen financiero",
                            style      = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
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
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(12.dp))

            // ── Selector de sede ──────────────────────────────────────
            SelectorSede(
                sedes              = sedes,
                sedeSeleccionada   = sedeSeleccionada,
                onSedeSeleccionada = { vm.seleccionarSede(it) }
            )

            Spacer(Modifier.height(10.dp))

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
                                "Cargando resumen financiero…",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                is FinancieroUiState.Error -> {
                    OutlinedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 24.dp),
                        shape  = RoundedCornerShape(24.dp),
                        colors = CardDefaults.outlinedCardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                        )
                    ) {
                        Column(
                            modifier            = Modifier.padding(32.dp).fillMaxWidth(),
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
                                text  = state.mensaje,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Button(
                                onClick = { vm.cargarCierreCaja() },
                                shape   = RoundedCornerShape(12.dp)
                            ) { Text("Reintentar") }
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

// ─── Selector de Sede M3 ──────────────────────────────────────────────────────

@Composable
private fun SelectorSede(
    sedes              : List<Sede>,
    sedeSeleccionada   : Sede?,
    onSedeSeleccionada : (Sede) -> Unit
) {
    var expandido by remember { mutableStateOf(false) }

    OutlinedCard(
        onClick  = { expandido = true },
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(14.dp),
        colors   = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
    ) {
        Row(
            modifier          = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Storefront, null,
                modifier = Modifier.size(20.dp),
                tint     = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text     = "Sede",
                    fontSize = 11.sp,
                    color    = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text       = sedeSeleccionada?.nombre ?: "Seleccionar sede",
                    fontWeight = FontWeight.Bold,
                    fontSize   = 14.sp,
                    color      = MaterialTheme.colorScheme.onSurface
                )
            }
            Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }

    DropdownMenu(
        expanded         = expandido,
        onDismissRequest = { expandido = false },
        modifier         = Modifier.fillMaxWidth(0.85f)
    ) {
        sedes.forEach { sede ->
            DropdownMenuItem(
                text = {
                    Text(sede.nombre, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                },
                onClick = {
                    onSedeSeleccionada(sede)
                    expandido = false
                }
            )
        }
    }
}

// ─── Selector de Fecha M3 ─────────────────────────────────────────────────────

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
            datePicker.maxDate = System.currentTimeMillis()
        }
    }

    OutlinedCard(
        onClick  = { datePickerDialog.show() },
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(14.dp),
        colors   = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
    ) {
        Row(
            modifier          = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.CalendarMonth,
                contentDescription = "Abrir calendario",
                modifier           = Modifier.size(20.dp),
                tint               = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text     = "Fecha de consulta",
                    fontSize = 11.sp,
                    color    = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text       = fecha.format(formatter),
                        fontWeight = FontWeight.Bold,
                        fontSize   = 14.sp,
                        color      = MaterialTheme.colorScheme.onSurface
                    )
                    if (fecha == LocalDate.now()) {
                        Spacer(Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text     = "HOY",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color    = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                            )
                        }
                    }
                }
            }
            Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

// ─── Contenido Principal ──────────────────────────────────────────────────────

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

        item { SeccionTitulo("Ingresos por profesional", Icons.Default.Groups) }
        if (profesionales.isEmpty()) {
            item {
                Surface(
                    shape    = RoundedCornerShape(12.dp),
                    color    = MaterialTheme.colorScheme.surfaceContainerLow,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text     = "Sin registros de ingresos para esta fecha",
                        color    = MaterialTheme.colorScheme.onSurfaceVariant,
                        style    = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        } else {
            itemsIndexed(profesionales) { index, prof ->
                FilaProfesional(index + 1, prof)
            }
        }

        item { SeccionTitulo("Gastos del día", Icons.Default.Receipt) }
        if (ultimosGastos.isEmpty()) {
            item {
                Surface(
                    shape    = RoundedCornerShape(12.dp),
                    color    = MaterialTheme.colorScheme.surfaceContainerLow,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text     = "Sin gastos registrados para esta fecha",
                        color    = MaterialTheme.colorScheme.onSurfaceVariant,
                        style    = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        } else {
            itemsIndexed(ultimosGastos) { _, gasto ->
                FilaGasto(gasto)
            }
        }
    }
}

// ─── Tarjetas Resumen ─────────────────────────────────────────────────────────

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
            color    = MaterialTheme.colorScheme.primaryContainer,
            onColor  = MaterialTheme.colorScheme.onPrimaryContainer,
            icono    = Icons.Default.TrendingUp
        )
        TarjetaMetrica(
            modifier = Modifier.weight(1f),
            label    = "Gastos",
            valor    = "$${"%,.0f".format(resumen.totalGastos)}",
            subLabel = "${resumen.totalRegistrosGasto} registros",
            color    = MaterialTheme.colorScheme.errorContainer,
            onColor  = MaterialTheme.colorScheme.onErrorContainer,
            icono    = Icons.Default.TrendingDown
        )
    }
}

@Composable
private fun TarjetaMetrica(
    modifier : Modifier,
    label    : String,
    valor    : String,
    subLabel : String,
    color    : Color,
    onColor  : Color,
    icono    : androidx.compose.ui.graphics.vector.ImageVector
) {
    Card(
        modifier = modifier,
        colors   = CardDefaults.cardColors(containerColor = color),
        shape    = RoundedCornerShape(18.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Text(label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = onColor.copy(alpha = 0.8f))
                Icon(icono, null, modifier = Modifier.size(18.dp), tint = onColor)
            }
            Spacer(Modifier.height(8.dp))
            Text(valor, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = onColor)
            Spacer(Modifier.height(2.dp))
            Text(subLabel, fontSize = 12.sp, color = onColor.copy(alpha = 0.8f))
        }
    }
}

// ─── Utilidad Neta ────────────────────────────────────────────────────────────

@Composable
private fun TarjetaUtilidadNeta(utilidad: Double) {
    val esPositiva     = utilidad >= 0
    val containerColor = if (esPositiva)
        MaterialTheme.colorScheme.primaryContainer
    else
        MaterialTheme.colorScheme.errorContainer

    val textColor = if (esPositiva)
        MaterialTheme.colorScheme.onPrimaryContainer
    else
        MaterialTheme.colorScheme.onErrorContainer

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors   = CardDefaults.cardColors(containerColor = containerColor),
        shape    = RoundedCornerShape(18.dp)
    ) {
        Row(
            modifier              = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector        = if (esPositiva) Icons.Default.AccountBalanceWallet else Icons.Default.Warning,
                    contentDescription = null,
                    modifier           = Modifier.size(22.dp),
                    tint               = textColor
                )
                Spacer(Modifier.width(10.dp))
                Text("Utilidad neta del día", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = textColor)
            }

            Text(
                text       = "$${"%,.0f".format(utilidad)}",
                fontSize   = 20.sp,
                fontWeight = FontWeight.Bold,
                color      = textColor
            )
        }
    }
}

// ─── Ranking Profesionales ────────────────────────────────────────────────────

@Composable
private fun FilaProfesional(posicion: Int, prof: CierreCajaProfesional) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(14.dp),
        colors   = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier          = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier.size(32.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("$posicion", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSecondaryContainer)
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(prof.nombreProfesional, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                Text("${prof.totalCitas} citas atendidas", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(
                text       = "$${"%,.0f".format(prof.totalGenerado)}",
                fontWeight = FontWeight.Bold,
                fontSize   = 15.sp,
                color      = MaterialTheme.colorScheme.primary
            )
        }
    }
}

// ─── Lista Gastos ─────────────────────────────────────────────────────────────

@Composable
private fun FilaGasto(gasto: CierreCajaGasto) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(14.dp),
        colors   = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier          = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(gasto.concepto, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                val fechaDisplay = runCatching {
                    LocalDate.parse(gasto.fechaGasto)
                        .format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                }.getOrElse { gasto.fechaGasto }
                Text(fechaDisplay, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(
                text       = "$${"%,.0f".format(gasto.valor)}",
                fontSize   = 14.sp,
                fontWeight = FontWeight.Bold,
                color      = MaterialTheme.colorScheme.error
            )
        }
    }
}

// ─── Helper ───────────────────────────────────────────────────────────────────

@Composable
private fun SeccionTitulo(
    texto: String,
    icono: androidx.compose.ui.graphics.vector.ImageVector? = null
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier          = Modifier.padding(bottom = 6.dp, top = 4.dp)
    ) {
        if (icono != null) {
            Icon(
                imageVector        = icono,
                contentDescription = null,
                modifier           = Modifier.size(18.dp),
                tint               = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.width(6.dp))
        }
        Text(
            text       = texto,
            fontWeight = FontWeight.Bold,
            fontSize   = 15.sp,
            color      = MaterialTheme.colorScheme.onBackground
        )
    }
}
