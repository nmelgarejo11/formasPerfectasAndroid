// Ruta: app/src/main/java/com/spa/appointments/ui/financiero/IngresosVsGastosScreen.kt
package com.spa.appointments.ui.financiero

import android.app.DatePickerDialog
import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.spa.appointments.domain.model.FiltrosBalance
import com.spa.appointments.domain.model.IngresosVsGastosDatos
import com.spa.appointments.domain.model.IngresosVsGastosDia
import com.spa.appointments.domain.model.IngresosVsGastosResumen
import com.spa.appointments.domain.model.MetodoPago
import com.spa.appointments.domain.model.MovimientoBalance
import com.spa.appointments.domain.model.Profesional
import com.spa.appointments.domain.model.Sede
import com.spa.appointments.domain.model.TipoMovimientoFiltro
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IngresosVsGastosScreen(
    onBack : () -> Unit,
    vm     : IngresosVsGastosViewModel = hiltViewModel()
) {
    val uiState              by vm.uiState.collectAsState()
    val sedes                by vm.sedes.collectAsState()
    val sedeSeleccionada     by vm.sedeSeleccionada.collectAsState()
    val fechaInicio          by vm.fechaInicio.collectAsState()
    val fechaFin             by vm.fechaFin.collectAsState()
    val filtros              by vm.filtros.collectAsState()
    val metodosPago          by vm.metodosPago.collectAsState()
    val profesionales        by vm.profesionales.collectAsState()
    val mostrarDialogFiltros by vm.mostrarDialogFiltros.collectAsState()
    val context              = LocalContext.current
    val descargaState        by vm.descargaState.collectAsState()

    var mostrarError         by remember { mutableStateOf<String?>(null) }
    var mostrarOpcionesExcel by remember { mutableStateOf(false) }

    LaunchedEffect(descargaState) {
        when (val s = descargaState) {
            is DescargaState.Listo -> {
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(
                        s.uri,
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                    )
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(intent, "Abrir con…"))
                vm.resetDescarga()
            }
            is DescargaState.Error -> {
                mostrarError = s.mensaje
                vm.resetDescarga()
            }
            else -> Unit
        }
    }

    if (mostrarOpcionesExcel) {
        AlertDialog(
            onDismissRequest = { mostrarOpcionesExcel = false },
            shape            = RoundedCornerShape(28.dp),
            containerColor   = MaterialTheme.colorScheme.surfaceContainerHigh,
            icon = {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Icon(
                        Icons.Default.FileDownload, null,
                        tint     = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(12.dp).size(26.dp)
                    )
                }
            },
            title = { Text("Exportar Reporte Excel", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) },
            text  = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(
                        onClick  = {
                            mostrarOpcionesExcel = false
                            vm.guardarExcel(context)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape    = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector        = Icons.Default.FileDownload,
                            contentDescription = null,
                            modifier           = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Guardar en dispositivo", fontWeight = FontWeight.SemiBold)
                    }

                    OutlinedButton(
                        onClick  = {
                            mostrarOpcionesExcel = false
                            vm.compartirExcel(context)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape    = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector        = Icons.Default.Share,
                            contentDescription = null,
                            modifier           = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Compartir reporte", fontWeight = FontWeight.SemiBold)
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                OutlinedButton(
                    onClick  = { mostrarOpcionesExcel = false },
                    shape    = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (mostrarDialogFiltros) {
        FiltrosBalanceBottomSheet(
            filtrosIniciales = filtros,
            metodosPago      = metodosPago,
            profesionales    = profesionales,
            onDismiss        = { vm.toggleDialogFiltros() },
            onAplicar        = { nuevos -> vm.aplicarFiltros(nuevos) },
            onLimpiar        = { vm.limpiarFiltros() }
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Text(
                            text       = "Balance financiero",
                            style      = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                        }
                    },
                    actions = {
                        val tienedatos = uiState is IngresosVsGastosUiState.Success

                        IconButton(onClick = { vm.toggleDialogFiltros() }) {
                            BadgedBox(
                                badge = {
                                    if (filtros.activoCount > 0) {
                                        Badge { Text("${filtros.activoCount}") }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector        = Icons.Default.FilterList,
                                    contentDescription = "Filtrar balance",
                                    tint               = if (filtros.esFiltroActivo) MaterialTheme.colorScheme.primary else LocalContentColor.current
                                )
                            }
                        }

                        if (descargaState is DescargaState.Cargando) {
                            CircularProgressIndicator(
                                modifier    = Modifier.size(24.dp).padding(end = 12.dp),
                                color       = MaterialTheme.colorScheme.primary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            IconButton(
                                onClick = { if (tienedatos) mostrarOpcionesExcel = true },
                                enabled = tienedatos
                            ) {
                                Icon(
                                    imageVector        = Icons.Default.FileDownload,
                                    contentDescription = "Exportar Excel",
                                    tint               = if (tienedatos) MaterialTheme.colorScheme.primary else LocalContentColor.current
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )

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

            SelectorSede(
                sedes              = sedes,
                sedeSeleccionada   = sedeSeleccionada,
                onSedeSeleccionada = { vm.seleccionarSede(it) }
            )

            Spacer(Modifier.height(10.dp))

            SelectorRango(
                fechaInicio    = fechaInicio,
                fechaFin       = fechaFin,
                onCambiarRango = { ini, fin -> vm.cambiarRango(ini, fin) }
            )

            AnimatedVisibility(visible = filtros.esFiltroActivo) {
                Column {
                    Spacer(Modifier.height(10.dp))
                    BarraFiltrosActivos(
                        filtros   = filtros,
                        onLimpiar = { vm.limpiarFiltros() }
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            when (val state = uiState) {
                is IngresosVsGastosUiState.Loading -> {
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
                                "Cargando balance del período…",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                is IngresosVsGastosUiState.Error -> {
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
                                onClick = { vm.cargar() },
                                shape   = RoundedCornerShape(12.dp)
                            ) { Text("Reintentar") }
                        }
                    }
                }
                is IngresosVsGastosUiState.Success -> {
                    IngresosVsGastosContent(
                        datos = state.datos
                    )
                }
                else -> Unit
            }
        }
    }

    mostrarError?.let { msg ->
        AlertDialog(
            onDismissRequest = { mostrarError = null },
            shape            = RoundedCornerShape(28.dp),
            containerColor   = MaterialTheme.colorScheme.surfaceContainerHigh,
            icon             = {
                Surface(shape = CircleShape, color = MaterialTheme.colorScheme.errorContainer) {
                    Icon(Icons.Default.CloudOff, null, modifier = Modifier.padding(12.dp).size(26.dp), tint = MaterialTheme.colorScheme.onErrorContainer)
                }
            },
            title   = { Text("Error al exportar", fontWeight = FontWeight.Bold) },
            text    = { Text(msg, style = MaterialTheme.typography.bodyMedium) },
            confirmButton = {
                Button(onClick = { mostrarError = null }, shape = RoundedCornerShape(12.dp)) { Text("Aceptar") }
            }
        )
    }
}

// ─── Barra de Filtros Activos ──────────────────────────────────────────────────

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun BarraFiltrosActivos(
    filtros   : FiltrosBalance,
    onLimpiar : () -> Unit
) {
    Surface(
        shape    = RoundedCornerShape(12.dp),
        color    = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier          = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.FilterAlt,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint     = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "Filtros aplicados (${filtros.activoCount})",
                        style      = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color      = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
                TextButton(
                    onClick = onLimpiar,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text("Limpiar todo", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(Modifier.height(4.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement   = Arrangement.spacedBy(6.dp)
            ) {
                if (filtros.tipoMovimiento != TipoMovimientoFiltro.TODOS) {
                    AssistChip(
                        onClick = {},
                        label   = { Text("Tipo: ${filtros.tipoMovimiento.titulo}", fontSize = 11.sp) },
                        leadingIcon = { Icon(Icons.Default.Category, null, modifier = Modifier.size(14.dp)) }
                    )
                }
                if (!filtros.nombreMetodoPago.isNullOrBlank()) {
                    AssistChip(
                        onClick = {},
                        label   = { Text("Pago: ${filtros.nombreMetodoPago}", fontSize = 11.sp) },
                        leadingIcon = { Icon(Icons.Default.Payments, null, modifier = Modifier.size(14.dp)) }
                    )
                }
                if (filtros.conceptoCategoria.isNotBlank()) {
                    AssistChip(
                        onClick = {},
                        label   = { Text("Concepto: \"${filtros.conceptoCategoria}\"", fontSize = 11.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, null, modifier = Modifier.size(14.dp)) }
                    )
                }
                if (filtros.cliente.isNotBlank()) {
                    AssistChip(
                        onClick = {},
                        label   = { Text("Cliente: ${filtros.cliente}", fontSize = 11.sp) },
                        leadingIcon = { Icon(Icons.Default.Person, null, modifier = Modifier.size(14.dp)) }
                    )
                }
                if (!filtros.nombreProfesional.isNullOrBlank()) {
                    AssistChip(
                        onClick = {},
                        label   = { Text("Prof: ${filtros.nombreProfesional}", fontSize = 11.sp) },
                        leadingIcon = { Icon(Icons.Default.Badge, null, modifier = Modifier.size(14.dp)) }
                    )
                }
            }
        }
    }
}

// ─── Selector Sede M3 ─────────────────────────────────────────────────────────

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
                    "Sede",
                    fontSize = 11.sp,
                    color    = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    sedeSeleccionada?.nombre ?: "Seleccionar sede",
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
                text    = { Text(sede.nombre, fontWeight = FontWeight.SemiBold, fontSize = 14.sp) },
                onClick = { onSedeSeleccionada(sede); expandido = false }
            )
        }
    }
}

// ─── Selector Rango de Fechas M3 ──────────────────────────────────────────────

@Composable
private fun SelectorRango(
    fechaInicio    : LocalDate,
    fechaFin       : LocalDate,
    onCambiarRango : (LocalDate, LocalDate) -> Unit
) {
    val context   = LocalContext.current
    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    OutlinedCard(
        onClick = {
            fun abrirPickerFin(inicioElegido: LocalDate) {
                DatePickerDialog(
                    context,
                    { _, y, m, d ->
                        val fin = LocalDate.of(y, m + 1, d)
                        onCambiarRango(inicioElegido, fin)
                    },
                    fechaFin.year,
                    fechaFin.monthValue - 1,
                    fechaFin.dayOfMonth
                ).apply {
                    datePicker.minDate = inicioElegido
                        .atStartOfDay(java.time.ZoneId.systemDefault())
                        .toInstant()
                        .toEpochMilli()
                    datePicker.maxDate = System.currentTimeMillis()
                }.show()
            }

            DatePickerDialog(
                context,
                { _, y, m, d ->
                    val inicio = LocalDate.of(y, m + 1, d)
                    abrirPickerFin(inicio)
                },
                fechaInicio.year,
                fechaInicio.monthValue - 1,
                fechaInicio.dayOfMonth
            ).apply {
                datePicker.maxDate = System.currentTimeMillis()
            }.show()
        },
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
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint     = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Período del reporte",
                    fontSize = 11.sp,
                    color    = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "${fechaInicio.format(formatter)}  →  ${fechaFin.format(formatter)}",
                    fontWeight = FontWeight.Bold,
                    fontSize   = 14.sp,
                    color      = MaterialTheme.colorScheme.onSurface
                )
            }
            Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

// ─── Contenido M3 con Totales Recalculados ────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun IngresosVsGastosContent(
    datos: IngresosVsGastosDatos
) {
    var tabSeleccionado by remember { mutableIntStateOf(0) }

    LazyColumn(
        modifier            = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding      = PaddingValues(bottom = 24.dp)
    ) {
        item { TarjetasResumenPeriodo(datos.resumen) }
        item { TarjetaUtilidadPeriodo(datos.resumen.utilidadNeta) }

        item {
            PrimaryTabRow(selectedTabIndex = tabSeleccionado) {
                Tab(
                    selected = tabSeleccionado == 0,
                    onClick  = { tabSeleccionado = 0 },
                    text     = {
                        Text(
                            "Movimientos (${datos.movimientosFiltrados.size})",
                            fontWeight = FontWeight.Bold
                        )
                    }
                )
                Tab(
                    selected = tabSeleccionado == 1,
                    onClick  = { tabSeleccionado = 1 },
                    text     = {
                        Text(
                            "Detalle por día",
                            fontWeight = FontWeight.Bold
                        )
                    }
                )
            }
        }

        if (tabSeleccionado == 0) {
            if (datos.movimientosFiltrados.isEmpty()) {
                item {
                    Surface(
                        shape    = RoundedCornerShape(14.dp),
                        color    = MaterialTheme.colorScheme.surfaceContainerLow,
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                    ) {
                        Column(
                            modifier            = Modifier.padding(24.dp).fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.SearchOff,
                                contentDescription = null,
                                modifier = Modifier.size(36.dp),
                                tint     = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                "Sin movimientos encontrados",
                                fontWeight = FontWeight.Bold,
                                style      = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                "No hay registros de ingresos o gastos que coincidan con los filtros aplicados.",
                                style     = MaterialTheme.typography.bodyMedium,
                                color     = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(datos.movimientosFiltrados) { mov ->
                    FilaMovimientoBalance(mov)
                }
            }
        } else {
            item { CabeceraTabla() }
            itemsIndexed(datos.detalle) { _, dia ->
                FilaDia(dia)
            }
        }
    }
}

// ─── Tarjetas Resumen Período M3 ──────────────────────────────────────────────

@Composable
private fun TarjetasResumenPeriodo(resumen: IngresosVsGastosResumen) {
    Row(
        modifier              = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(
            modifier = Modifier.weight(1f),
            colors   = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ),
            shape    = RoundedCornerShape(18.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Text("Ingresos", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f))
                    Icon(Icons.AutoMirrored.Filled.TrendingUp, null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer)
                }
                Spacer(Modifier.height(8.dp))
                Text("$${"%,.0f".format(resumen.totalIngresos)}", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                Spacer(Modifier.height(2.dp))
                Text("${resumen.totalCitas} citas / registros", fontSize = 12.sp, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f))
            }
        }
        Card(
            modifier = Modifier.weight(1f),
            colors   = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer
            ),
            shape    = RoundedCornerShape(18.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Text("Gastos", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f))
                    Icon(Icons.AutoMirrored.Filled.TrendingDown, null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onErrorContainer)
                }
                Spacer(Modifier.height(8.dp))
                Text("$${"%,.0f".format(resumen.totalGastos)}", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onErrorContainer)
                Spacer(Modifier.height(2.dp))
                Text("del período", fontSize = 12.sp, color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f))
            }
        }
    }
}

@Composable
private fun TarjetaUtilidadPeriodo(utilidad: Double) {
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
            modifier              = Modifier.fillMaxWidth().padding(18.dp),
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
                Text("Utilidad del período", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = textColor)
            }

            Text(
                "$${"%,.0f".format(utilidad)}",
                fontSize   = 20.sp,
                fontWeight = FontWeight.Bold,
                color      = textColor
            )
        }
    }
}

// ─── Item de Movimiento de Balance ─────────────────────────────────────────────

@Composable
private fun FilaMovimientoBalance(mov: MovimientoBalance) {
    val esIngreso = mov.tipo == TipoMovimientoFiltro.INGRESO
    val colorMonto = if (esIngreso) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
    val icono = if (esIngreso) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown

    val fechaDisplay = runCatching {
        val parsed = LocalDate.parse(mov.fecha)
        parsed.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
    }.getOrElse { mov.fecha }

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
                shape = CircleShape,
                color = if (esIngreso) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer,
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icono,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = if (esIngreso) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text       = mov.concepto,
                    fontWeight = FontWeight.Bold,
                    fontSize   = 14.sp,
                    color      = MaterialTheme.colorScheme.onSurface
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Text(
                        text  = fechaDisplay,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    mov.metodoPago?.let { mp ->
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerHigh
                        ) {
                            Text(
                                text     = mp,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color    = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                            )
                        }
                    }
                }
            }

            Text(
                text       = "${if (esIngreso) "+" else "-"}$${"%,.0f".format(mov.valor)}",
                fontWeight = FontWeight.Bold,
                fontSize   = 15.sp,
                color      = colorMonto
            )
        }
    }
}

// ─── Tabla Detalle M3 ─────────────────────────────────────────────────────────

@Composable
private fun CabeceraTabla() {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Fecha",     fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1.2f))
            Text("Ingresos",  fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
            Text("Gastos",    fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
            Text("Utilidad",  fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
        }
    }
}

@Composable
private fun FilaDia(dia: IngresosVsGastosDia) {
    val formatter     = DateTimeFormatter.ofPattern("dd/MM")
    val esPositiva    = dia.utilidadNeta >= 0
    val colorUtilidad = if (esPositiva) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error

    val fechaDisplay = runCatching {
        LocalDate.parse(dia.fecha).format(formatter)
    }.getOrElse { dia.fecha }

    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(12.dp),
        colors   = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        border   = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Text(fechaDisplay, fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.2f))
            Text("$${"%,.0f".format(dia.totalIngresos)}", fontSize = 13.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
            Text("$${"%,.0f".format(dia.totalGastos)}", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.error, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
            Text("$${"%,.0f".format(dia.utilidadNeta)}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = colorUtilidad, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
        }
    }
}

// ─── Modal Bottom Sheet de Filtros ─────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FiltrosBalanceBottomSheet(
    filtrosIniciales : FiltrosBalance,
    metodosPago      : List<MetodoPago>,
    profesionales    : List<Profesional>,
    onDismiss        : () -> Unit,
    onAplicar        : (FiltrosBalance) -> Unit,
    onLimpiar        : () -> Unit
) {
    var tipoMovimiento     by remember { mutableStateOf(filtrosIniciales.tipoMovimiento) }
    var idMetodoPago       by remember { mutableStateOf(filtrosIniciales.idMetodoPago) }
    var nombreMetodoPago   by remember { mutableStateOf(filtrosIniciales.nombreMetodoPago) }
    var conceptoCategoria  by remember { mutableStateOf(filtrosIniciales.conceptoCategoria) }
    var cliente            by remember { mutableStateOf(filtrosIniciales.cliente) }
    var idProfesional      by remember { mutableStateOf(filtrosIniciales.idProfesional) }
    var nombreProfesional  by remember { mutableStateOf(filtrosIniciales.nombreProfesional) }

    var expandidoMp        by remember { mutableStateOf(false) }
    var expandidoProf      by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState       = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor   = MaterialTheme.colorScheme.surfaceContainerHigh
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Text(
                    "Filtros de Balance",
                    style      = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Cerrar")
                }
            }

            // 1. Tipo de Movimiento
            Column {
                Text(
                    "Tipo de movimiento",
                    style      = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color      = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TipoMovimientoFiltro.entries.forEach { op ->
                        FilterChip(
                            selected = tipoMovimiento == op,
                            onClick  = { tipoMovimiento = op },
                            label    = { Text(op.titulo) }
                        )
                    }
                }
            }

            // 2. Método de Pago
            Column {
                Text(
                    "Método de pago",
                    style      = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color      = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(6.dp))
                ExposedDropdownMenuBox(
                    expanded        = expandidoMp,
                    onExpandedChange = { expandidoMp = !expandidoMp }
                ) {
                    OutlinedTextField(
                        value         = nombreMetodoPago ?: "Todos los métodos",
                        onValueChange = {},
                        readOnly      = true,
                        modifier      = Modifier.fillMaxWidth().menuAnchor(),
                        trailingIcon  = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandidoMp) },
                        shape         = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded         = expandidoMp,
                        onDismissRequest = { expandidoMp = false }
                    ) {
                        DropdownMenuItem(
                            text    = { Text("Todos los métodos") },
                            onClick = {
                                idMetodoPago = null
                                nombreMetodoPago = null
                                expandidoMp = false
                            }
                        )
                        metodosPago.forEach { mp ->
                            DropdownMenuItem(
                                text    = { Text(mp.nombre) },
                                onClick = {
                                    idMetodoPago = mp.id
                                    nombreMetodoPago = mp.nombre
                                    expandidoMp = false
                                }
                            )
                        }
                    }
                }
            }

            // 3. Concepto / Categoría
            Column {
                Text(
                    "Concepto o categoría",
                    style      = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color      = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(6.dp))
                OutlinedTextField(
                    value         = conceptoCategoria,
                    onValueChange = { conceptoCategoria = it },
                    placeholder   = { Text("Buscar por concepto...") },
                    modifier      = Modifier.fillMaxWidth(),
                    shape         = RoundedCornerShape(12.dp),
                    trailingIcon  = {
                        if (conceptoCategoria.isNotEmpty()) {
                            IconButton(onClick = { conceptoCategoria = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Limpiar")
                            }
                        }
                    }
                )
            }

            // 4. Cliente
            Column {
                Text(
                    "Cliente",
                    style      = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color      = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(6.dp))
                OutlinedTextField(
                    value         = cliente,
                    onValueChange = { cliente = it },
                    placeholder   = { Text("Nombre del cliente...") },
                    modifier      = Modifier.fillMaxWidth(),
                    shape         = RoundedCornerShape(12.dp),
                    trailingIcon  = {
                        if (cliente.isNotEmpty()) {
                            IconButton(onClick = { cliente = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Limpiar")
                            }
                        }
                    }
                )
            }

            // 5. Profesional
            Column {
                Text(
                    "Profesional",
                    style      = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color      = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(6.dp))
                ExposedDropdownMenuBox(
                    expanded        = expandidoProf,
                    onExpandedChange = { expandidoProf = !expandidoProf }
                ) {
                    OutlinedTextField(
                        value         = nombreProfesional ?: "Todos los profesionales",
                        onValueChange = {},
                        readOnly      = true,
                        modifier      = Modifier.fillMaxWidth().menuAnchor(),
                        trailingIcon  = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandidoProf) },
                        shape         = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded         = expandidoProf,
                        onDismissRequest = { expandidoProf = false }
                    ) {
                        DropdownMenuItem(
                            text    = { Text("Todos los profesionales") },
                            onClick = {
                                idProfesional = null
                                nombreProfesional = null
                                expandidoProf = false
                            }
                        )
                        profesionales.forEach { prof ->
                            DropdownMenuItem(
                                text    = { Text(prof.nombre) },
                                onClick = {
                                    idProfesional = prof.id
                                    nombreProfesional = prof.nombre
                                    expandidoProf = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // Botones Acción
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick  = onLimpiar,
                    modifier = Modifier.weight(1f),
                    shape    = RoundedCornerShape(12.dp)
                ) {
                    Text("Limpiar todo")
                }

                Button(
                    onClick = {
                        onAplicar(
                            filtrosIniciales.copy(
                                tipoMovimiento    = tipoMovimiento,
                                idMetodoPago      = idMetodoPago,
                                nombreMetodoPago  = nombreMetodoPago,
                                conceptoCategoria = conceptoCategoria,
                                cliente           = cliente,
                                idProfesional     = idProfesional,
                                nombreProfesional = nombreProfesional
                            )
                        )
                    },
                    modifier = Modifier.weight(1f),
                    shape    = RoundedCornerShape(12.dp)
                ) {
                    Text("Aplicar Filtros")
                }
            }
        }
    }
}
