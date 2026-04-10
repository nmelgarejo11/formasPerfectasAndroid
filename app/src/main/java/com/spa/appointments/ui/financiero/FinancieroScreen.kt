package com.spa.appointments.ui.financiero

import android.graphics.Color as AndroidColor
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.spa.appointments.domain.model.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinancieroScreen(
    onBack: () -> Unit,
    vm: FinancieroViewModel = hiltViewModel()
) {
    val uiState          by vm.uiState.collectAsState()
    val periodoSeleccionado by vm.periodoSeleccionado.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Módulo financiero") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Volver")
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
            // ── Selector de período ──────────────────────────────────────
            ScrollableTabRow(
                selectedTabIndex = Periodo.entries.indexOf(periodoSeleccionado),
                edgePadding      = 16.dp
            ) {
                Periodo.entries.forEach { periodo ->
                    Tab(
                        selected = periodo == periodoSeleccionado,
                        onClick  = { vm.seleccionarPeriodo(periodo) },
                        text     = { Text(periodo.label, fontSize = 13.sp) }
                    )
                }
            }

            // ── Contenido ───────────────────────────────────────────────
            when (val state = uiState) {

                is FinancieroUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) { CircularProgressIndicator() }
                }

                is FinancieroUiState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text      = state.mensaje,
                            color     = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center,
                            modifier  = Modifier.padding(32.dp)
                        )
                    }
                }

                is FinancieroUiState.Success -> {
                    ContenidoFinanciero(data = state.data)
                }
            }
        }
    }
}

@Composable
private fun ContenidoFinanciero(data: FinancieroUiData) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ── Tarjetas de resumen ──────────────────────────────────────────
        TarjetasResumen(resumen = data.resumen)

        // ── Gráfico de barras: ingresos por día ──────────────────────────
        if (data.ingresosDia.isNotEmpty()) {
            SeccionCard(titulo = "Ingresos por día") {
                GraficoBarras(datos = data.ingresosDia)
            }
        }

        // ── Gráfico de línea: ingresos por mes ───────────────────────────
        if (data.ingresosMes.isNotEmpty()) {
            SeccionCard(titulo = "Tendencia mensual") {
                GraficoLinea(datos = data.ingresosMes)
            }
        }

        // ── Gráfico de torta: servicios más vendidos ──────────────────────
        if (data.serviciosVendidos.isNotEmpty()) {
            SeccionCard(titulo = "Servicios más vendidos") {
                GraficoTorta(datos = data.serviciosVendidos)
            }
        }

        // ── Ranking de profesionales ─────────────────────────────────────
        if (data.profesionalesRanking.isNotEmpty()) {
            SeccionCard(titulo = "Ranking de profesionales") {
                RankingProfesionales(datos = data.profesionalesRanking)
            }
        }

        Spacer(Modifier.height(16.dp))
    }
}

// ── Tarjetas de resumen ───────────────────────────────────────────────────────
@Composable
private fun TarjetasResumen(resumen: ResumenFinanciero) {
    val variacion      = resumen.variacionPorcentual
    val colorVariacion = if (variacion >= 0) Color(0xFF4CAF50) else Color(0xFFF44336)
    val iconVariacion  = if (variacion >= 0) Icons.Default.TrendingUp
    else Icons.Default.TrendingDown

    Row(
        modifier              = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Total ingresos
        TarjetaMetrica(
            modifier = Modifier.weight(1f),
            icono    = Icons.Default.AttachMoney,
            titulo   = "Ingresos",
            valor    = "${"$%,.0f".format(resumen.totalActual)}",
            color    = MaterialTheme.colorScheme.primary
        )

        // Total citas
        TarjetaMetrica(
            modifier = Modifier.weight(1f),
            icono    = Icons.Default.CalendarMonth,
            titulo   = "Citas",
            valor    = "${resumen.totalCitas}",
            color    = MaterialTheme.colorScheme.secondary
        )
    }

    // Variación vs período anterior
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors   = CardDefaults.cardColors(
            containerColor = colorVariacion.copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier            = Modifier.padding(16.dp),
            verticalAlignment   = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text  = "vs período anterior",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text       = "${"$%,.0f".format(resumen.totalAnterior)}",
                    style      = MaterialTheme.typography.bodyMedium,
                    color      = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector        = iconVariacion,
                    contentDescription = null,
                    tint               = colorVariacion,
                    modifier           = Modifier.size(24.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text       = "${"%.1f".format(variacion)}%",
                    style      = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color      = colorVariacion
                )
            }
        }
    }
}

@Composable
private fun TarjetaMetrica(
    modifier: Modifier,
    icono:    androidx.compose.ui.graphics.vector.ImageVector,
    titulo:   String,
    valor:    String,
    color:    Color
) {
    Card(modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(
                imageVector        = icono,
                contentDescription = null,
                tint               = color,
                modifier           = Modifier.size(24.dp)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text  = titulo,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text       = valor,
                style      = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color      = color
            )
        }
    }
}

// ── Contenedor de sección ─────────────────────────────────────────────────────
@Composable
private fun SeccionCard(
    titulo:   String,
    content:  @Composable () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text       = titulo,
                style      = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}

// ── Gráfico de barras ─────────────────────────────────────────────────────────
@Composable
private fun GraficoBarras(datos: List<IngresoDia>) {
    val colorPrimary = MaterialTheme.colorScheme.primary.toArgb()

    AndroidView(
        factory = { ctx ->
            BarChart(ctx).apply {
                description.isEnabled  = false
                legend.isEnabled       = false
                setDrawGridBackground(false)
                setDrawBarShadow(false)
                setFitBars(true)
                setPinchZoom(false)
                isDoubleTapToZoomEnabled = false

                xAxis.apply {
                    position        = XAxis.XAxisPosition.BOTTOM
                    granularity     = 1f
                    setDrawGridLines(false)
                    textColor       = AndroidColor.GRAY
                    textSize        = 9f
                    labelRotationAngle = -45f
                }
                axisLeft.apply {
                    setDrawGridLines(true)
                    textColor = AndroidColor.GRAY
                    textSize  = 9f
                }
                axisRight.isEnabled = false
            }
        },
        update = { chart ->
            val entries = datos.mapIndexed { i, d ->
                BarEntry(i.toFloat(), d.total.toFloat())
            }
            val labels = datos.map { it.fecha.substring(8, 10) } // día del mes

            val dataSet = BarDataSet(entries, "").apply {
                color      = colorPrimary
                valueTextSize = 0f // ocultar valores sobre las barras
            }

            chart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
            chart.data                 = BarData(dataSet)
            chart.animateY(800)
            chart.invalidate()
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
    )
}

// ── Gráfico de línea ──────────────────────────────────────────────────────────
@Composable
private fun GraficoLinea(datos: List<IngresoMes>) {
    val colorPrimary   = MaterialTheme.colorScheme.primary.toArgb()
    val colorSecondary = MaterialTheme.colorScheme.secondary.toArgb()

    AndroidView(
        factory = { ctx ->
            LineChart(ctx).apply {
                description.isEnabled    = false
                legend.isEnabled         = false
                setDrawGridBackground(false)
                setPinchZoom(false)
                isDoubleTapToZoomEnabled = false

                xAxis.apply {
                    position        = XAxis.XAxisPosition.BOTTOM
                    granularity     = 1f
                    setDrawGridLines(false)
                    textColor       = AndroidColor.GRAY
                    textSize        = 9f
                }
                axisLeft.apply {
                    textColor = AndroidColor.GRAY
                    textSize  = 9f
                }
                axisRight.isEnabled = false
            }
        },
        update = { chart ->
            val entries = datos.mapIndexed { i, d ->
                Entry(i.toFloat(), d.total.toFloat())
            }
            val labels = datos.map { it.nombreMes.take(3) }

            val dataSet = LineDataSet(entries, "").apply {
                color                = colorPrimary
                setCircleColor(colorPrimary)
                lineWidth            = 2f
                circleRadius         = 4f
                setDrawValues(false)
                mode                 = LineDataSet.Mode.CUBIC_BEZIER
                fillColor            = colorSecondary
                fillAlpha            = 50
                setDrawFilled(true)
            }

            chart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
            chart.data                 = LineData(dataSet)
            chart.animateX(800)
            chart.invalidate()
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
    )
}

// ── Gráfico de torta ──────────────────────────────────────────────────────────
@Composable
private fun GraficoTorta(datos: List<ServicioVendido>) {
    val colores = listOf(
        AndroidColor.parseColor("#6650A4"),
        AndroidColor.parseColor("#625B71"),
        AndroidColor.parseColor("#7D5260"),
        AndroidColor.parseColor("#B5838D"),
        AndroidColor.parseColor("#9C27B0")
    )

    AndroidView(
        factory = { ctx ->
            PieChart(ctx).apply {
                description.isEnabled = false
                isDrawHoleEnabled     = true
                holeRadius            = 50f
                transparentCircleRadius = 55f
                setDrawEntryLabels(false)
                legend.apply {
                    isEnabled  = true
                    textSize   = 10f
                    textColor  = AndroidColor.GRAY
                }
            }
        },
        update = { chart ->
            val entries = datos.map { d ->
                PieEntry(d.cantidad.toFloat(), d.servicio)
            }
            val dataSet = PieDataSet(entries, "").apply {
                colors     = colores
                valueTextSize  = 11f
                valueTextColor = AndroidColor.WHITE
            }
            chart.data = PieData(dataSet)
            chart.animateY(800)
            chart.invalidate()
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp)
    )
}

// ── Ranking de profesionales ──────────────────────────────────────────────────
@Composable
private fun RankingProfesionales(datos: List<ProfesionalRanking>) {
    val maxCitas = datos.maxOfOrNull { it.totalCitas } ?: 1

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        datos.forEachIndexed { index, prof ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier          = Modifier.fillMaxWidth()
            ) {
                // Posición
                Text(
                    text       = "${index + 1}",
                    style      = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color      = MaterialTheme.colorScheme.primary,
                    modifier   = Modifier.width(24.dp)
                )

                Spacer(Modifier.width(8.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text  = prof.profesional,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text  = "${prof.totalCitas} citas",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Text(
                        text  = prof.cargo,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(Modifier.height(4.dp))

                    // Barra de progreso
                    LinearProgressIndicator(
                        progress = { prof.totalCitas.toFloat() / maxCitas },
                        modifier = Modifier.fillMaxWidth(),
                        color    = MaterialTheme.colorScheme.primary
                    )

                    Text(
                        text  = "${"$%,.0f".format(prof.totalIngresos)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}