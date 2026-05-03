package com.spa.appointments.ui.financiero

import android.graphics.Color as AndroidColor
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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

// ─── Screen principal ─────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinancieroScreen(
    onBack: () -> Unit,
    vm: FinancieroViewModel = hiltViewModel()
) {
    val uiState             by vm.uiState.collectAsState()
    val periodoSeleccionado by vm.periodoSeleccionado.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text       = "Módulo financiero",
                            style      = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text  = periodoSeleccionado.label,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
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
                edgePadding      = 16.dp,
                modifier         = Modifier.fillMaxWidth()
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
                        modifier         = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            CircularProgressIndicator()
                            Text(
                                "Cargando datos financieros…",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                is FinancieroUiState.Error -> {
                    Box(
                        modifier         = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            modifier            = Modifier.padding(32.dp),
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
                                onClick = { vm.seleccionarPeriodo(periodoSeleccionado) },
                                shape   = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Refresh, null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Reintentar")
                            }
                        }
                    }
                }

                is FinancieroUiState.Success -> {
                    ContenidoFinanciero(data = state.data)
                }
            }
        }
    }
}

// ─── Contenido principal ──────────────────────────────────────────────────────

@Composable
private fun ContenidoFinanciero(data: FinancieroUiData) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        TarjetasResumen(resumen = data.resumen)

        if (data.ingresosDia.isNotEmpty()) {
            SeccionCard(
                titulo = "Ingresos por día",
                icono  = Icons.Default.BarChart
            ) {
                GraficoBarras(datos = data.ingresosDia)
            }
        }

        if (data.ingresosMes.isNotEmpty()) {
            SeccionCard(
                titulo = "Tendencia mensual",
                icono  = Icons.Default.ShowChart
            ) {
                GraficoLinea(datos = data.ingresosMes)
            }
        }

        if (data.serviciosVendidos.isNotEmpty()) {
            SeccionCard(
                titulo = "Servicios más vendidos",
                icono  = Icons.Default.PieChart
            ) {
                GraficoTorta(datos = data.serviciosVendidos)
            }
        }

        if (data.profesionalesRanking.isNotEmpty()) {
            SeccionCard(
                titulo = "Ranking de profesionales",
                icono  = Icons.Default.EmojiEvents
            ) {
                RankingProfesionales(datos = data.profesionalesRanking)
            }
        }

        Spacer(Modifier.height(8.dp))
    }
}

// ─── Tarjetas de resumen ──────────────────────────────────────────────────────

@Composable
private fun TarjetasResumen(resumen: ResumenFinanciero) {
    val variacion      = resumen.variacionPorcentual
    val esPositiva     = variacion >= 0
    // Usa colores del tema en lugar de hardcoded
    val colorVariacion = if (esPositiva) MaterialTheme.colorScheme.tertiary
    else MaterialTheme.colorScheme.error
    val iconVariacion  = if (esPositiva) Icons.Default.TrendingUp
    else Icons.Default.TrendingDown

    Row(
        modifier              = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        TarjetaMetrica(
            modifier      = Modifier.weight(1f),
            icono         = Icons.Default.AttachMoney,
            titulo        = "Ingresos",
            valor         = "${"$%,.0f".format(resumen.totalActual)}",
            colorIcono    = MaterialTheme.colorScheme.primary,
            colorFondoIcon = MaterialTheme.colorScheme.primaryContainer
        )
        TarjetaMetrica(
            modifier      = Modifier.weight(1f),
            icono         = Icons.Default.CalendarMonth,
            titulo        = "Citas",
            valor         = "${resumen.totalCitas}",
            colorIcono    = MaterialTheme.colorScheme.secondary,
            colorFondoIcon = MaterialTheme.colorScheme.secondaryContainer
        )
    }

    // Variación vs período anterior
    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(14.dp),
        colors    = CardDefaults.cardColors(
            containerColor = colorVariacion.copy(alpha = 0.08f)
        ),
        border    = androidx.compose.foundation.BorderStroke(
            1.dp, colorVariacion.copy(alpha = 0.25f)
        )
    ) {
        Row(
            modifier              = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text  = "vs período anterior",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text  = "${"$%,.0f".format(resumen.totalAnterior)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = colorVariacion.copy(alpha = 0.15f)
                ) {
                    Icon(
                        imageVector        = iconVariacion,
                        contentDescription = null,
                        tint               = colorVariacion,
                        modifier           = Modifier.padding(6.dp).size(18.dp)
                    )
                }
                Text(
                    text       = "${if (esPositiva) "+" else ""}${"%.1f".format(variacion)}%",
                    style      = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color      = colorVariacion
                )
            }
        }
    }
}

// ─── Tarjeta de métrica individual ───────────────────────────────────────────

@Composable
private fun TarjetaMetrica(
    modifier:       Modifier,
    icono:          androidx.compose.ui.graphics.vector.ImageVector,
    titulo:         String,
    valor:          String,
    colorIcono:     Color,
    colorFondoIcon: Color
) {
    Card(
        modifier  = modifier,
        shape     = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border    = androidx.compose.foundation.BorderStroke(
            1.dp, MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = colorFondoIcon
            ) {
                Icon(
                    imageVector        = icono,
                    contentDescription = null,
                    tint               = colorIcono,
                    modifier           = Modifier.padding(8.dp).size(20.dp)
                )
            }
            Text(
                text  = titulo,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text       = valor,
                style      = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color      = colorIcono
            )
        }
    }
}

// ─── Contenedor de sección ────────────────────────────────────────────────────

@Composable
private fun SeccionCard(
    titulo:  String,
    icono:   androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable () -> Unit
) {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border    = androidx.compose.foundation.BorderStroke(
            1.dp, MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier          = Modifier.padding(bottom = 12.dp)
            ) {
                Icon(
                    imageVector        = icono,
                    contentDescription = null,
                    tint               = MaterialTheme.colorScheme.primary,
                    modifier           = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text       = titulo,
                    style      = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                modifier = Modifier.padding(bottom = 12.dp)
            )
            content()
        }
    }
}

// ─── Gráfico de barras ────────────────────────────────────────────────────────

@Composable
private fun GraficoBarras(datos: List<IngresoDia>) {
    val colorPrimary = MaterialTheme.colorScheme.primary.toArgb()
    val colorText    = MaterialTheme.colorScheme.onSurfaceVariant.toArgb()

    AndroidView(
        factory = { ctx ->
            BarChart(ctx).apply {
                description.isEnabled    = false
                legend.isEnabled         = false
                setDrawGridBackground(false)
                setDrawBarShadow(false)
                setFitBars(true)
                setPinchZoom(false)
                isDoubleTapToZoomEnabled = false
                setBackgroundColor(AndroidColor.TRANSPARENT)

                xAxis.apply {
                    position           = XAxis.XAxisPosition.BOTTOM
                    granularity        = 1f
                    setDrawGridLines(false)
                    labelRotationAngle = -45f
                }
                axisLeft.apply {
                    setDrawGridLines(true)
                    gridColor = AndroidColor.argb(30, 128, 128, 128)
                }
                axisRight.isEnabled = false
            }
        },
        update = { chart ->
            chart.xAxis.textColor  = colorText
            chart.axisLeft.textColor = colorText
            chart.axisLeft.textSize  = 9f
            chart.xAxis.textSize     = 9f

            val entries = datos.mapIndexed { i, d ->
                BarEntry(i.toFloat(), d.total.toFloat())
            }
            val labels = datos.map { it.fecha.substring(8, 10) }

            val dataSet = BarDataSet(entries, "").apply {
                color         = colorPrimary
                valueTextSize = 0f
            }

            chart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
            chart.data                 = BarData(dataSet)
            chart.animateY(600)
            chart.invalidate()
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
    )
}

// ─── Gráfico de línea ─────────────────────────────────────────────────────────

@Composable
private fun GraficoLinea(datos: List<IngresoMes>) {
    val colorPrimary = MaterialTheme.colorScheme.primary.toArgb()
    val colorFill    = MaterialTheme.colorScheme.primaryContainer.toArgb()
    val colorText    = MaterialTheme.colorScheme.onSurfaceVariant.toArgb()

    AndroidView(
        factory = { ctx ->
            LineChart(ctx).apply {
                description.isEnabled    = false
                legend.isEnabled         = false
                setDrawGridBackground(false)
                setPinchZoom(false)
                isDoubleTapToZoomEnabled = false
                setBackgroundColor(AndroidColor.TRANSPARENT)

                xAxis.apply {
                    position        = XAxis.XAxisPosition.BOTTOM
                    granularity     = 1f
                    setDrawGridLines(false)
                }
                axisLeft.apply {
                    setDrawGridLines(true)
                    gridColor = AndroidColor.argb(30, 128, 128, 128)
                }
                axisRight.isEnabled = false
            }
        },
        update = { chart ->
            chart.xAxis.textColor    = colorText
            chart.xAxis.textSize     = 9f
            chart.axisLeft.textColor = colorText
            chart.axisLeft.textSize  = 9f

            val entries = datos.mapIndexed { i, d ->
                Entry(i.toFloat(), d.total.toFloat())
            }
            val labels = datos.map { it.nombreMes.take(3) }

            val dataSet = LineDataSet(entries, "").apply {
                color            = colorPrimary
                setCircleColor(colorPrimary)
                lineWidth        = 2.5f
                circleRadius     = 4f
                setDrawValues(false)
                mode             = LineDataSet.Mode.CUBIC_BEZIER
                fillColor        = colorFill
                fillAlpha        = 60
                setDrawFilled(true)
            }

            chart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
            chart.data                 = LineData(dataSet)
            chart.animateX(600)
            chart.invalidate()
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
    )
}

// ─── Gráfico de torta ─────────────────────────────────────────────────────────

@Composable
private fun GraficoTorta(datos: List<ServicioVendido>) {
    // Usa colores del tema en lugar de hardcoded
    val c1 = MaterialTheme.colorScheme.primary.toArgb()
    val c2 = MaterialTheme.colorScheme.secondary.toArgb()
    val c3 = MaterialTheme.colorScheme.tertiary.toArgb()
    val c4 = MaterialTheme.colorScheme.primaryContainer.toArgb()
    val c5 = MaterialTheme.colorScheme.secondaryContainer.toArgb()
    val colores = listOf(c1, c2, c3, c4, c5)

    val colorText = MaterialTheme.colorScheme.onSurfaceVariant.toArgb()

    AndroidView(
        factory = { ctx ->
            PieChart(ctx).apply {
                description.isEnabled   = false
                isDrawHoleEnabled       = true
                holeRadius              = 52f
                transparentCircleRadius = 57f
                setDrawEntryLabels(false)
                setBackgroundColor(AndroidColor.TRANSPARENT)
                legend.apply {
                    isEnabled = true
                    textSize  = 10f
                }
            }
        },
        update = { chart ->
            chart.legend.textColor = colorText
            chart.setHoleColor(AndroidColor.TRANSPARENT)

            val entries = datos.map { d ->
                PieEntry(d.cantidad.toFloat(), d.servicio)
            }
            val dataSet = PieDataSet(entries, "").apply {
                colors         = colores
                valueTextSize  = 11f
                valueTextColor = AndroidColor.WHITE
                sliceSpace     = 2f
            }
            chart.data = PieData(dataSet)
            chart.animateY(600)
            chart.invalidate()
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp)
    )
}

// ─── Ranking de profesionales ─────────────────────────────────────────────────

@Composable
private fun RankingProfesionales(datos: List<ProfesionalRanking>) {
    val maxCitas = datos.maxOfOrNull { it.totalCitas } ?: 1

    // Medallas para top 3
    val medallas = listOf("🥇", "🥈", "🥉")

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        datos.forEachIndexed { index, prof ->

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier          = Modifier.fillMaxWidth()
            ) {
                // Posición / medalla
                Box(
                    contentAlignment = Alignment.Center,
                    modifier         = Modifier.width(32.dp)
                ) {
                    if (index < 3) {
                        Text(
                            text     = medallas[index],
                            fontSize = 18.sp
                        )
                    } else {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text       = "${index + 1}",
                                style      = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color      = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier   = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Spacer(Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        Text(
                            text       = prof.profesional,
                            style      = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text  = "${"$%,.0f".format(prof.totalIngresos)}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        Text(
                            text  = prof.cargo,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text  = "${prof.totalCitas} citas",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(Modifier.height(5.dp))

                    LinearProgressIndicator(
                        progress = { prof.totalCitas.toFloat() / maxCitas },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(5.dp),
                        color        = MaterialTheme.colorScheme.primary,
                        trackColor   = MaterialTheme.colorScheme.surfaceVariant,
                        strokeCap    = androidx.compose.ui.graphics.StrokeCap.Round
                    )
                }
            }

            // Separador entre items (excepto el último)
            if (index < datos.lastIndex) {
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                )
            }
        }
    }
}