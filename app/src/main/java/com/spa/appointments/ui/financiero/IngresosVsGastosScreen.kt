// Ruta: app/src/main/java/com/spa/appointments/ui/financiero/IngresosVsGastosScreen.kt
package com.spa.appointments.ui.financiero

import android.app.DatePickerDialog
import android.content.Intent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.spa.appointments.domain.model.IngresosVsGastosDia
import com.spa.appointments.domain.model.IngresosVsGastosResumen
import com.spa.appointments.domain.model.Sede
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IngresosVsGastosScreen(
    onBack : () -> Unit,
    vm     : IngresosVsGastosViewModel = hiltViewModel()
) {
    val uiState          by vm.uiState.collectAsState()
    val sedes            by vm.sedes.collectAsState()
    val sedeSeleccionada by vm.sedeSeleccionada.collectAsState()
    val fechaInicio      by vm.fechaInicio.collectAsState()
    val fechaFin         by vm.fechaFin.collectAsState()
    val context          = LocalContext.current
    val descargaState    by vm.descargaState.collectAsState()
    var mostrarError     by remember { mutableStateOf<String?>(null) }
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

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Text(
                            text       = "Ingresos vs gastos",
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
                        resumen = state.data.resumen,
                        detalle = state.data.detalle
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

// ─── Contenido M3 ─────────────────────────────────────────────────────────────

@Composable
private fun IngresosVsGastosContent(
    resumen : IngresosVsGastosResumen,
    detalle : List<IngresosVsGastosDia>
) {
    LazyColumn(
        modifier            = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding      = PaddingValues(bottom = 24.dp)
    ) {
        item { TarjetasResumenPeriodo(resumen) }
        item { TarjetaUtilidadPeriodo(resumen.utilidadNeta) }

        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.TableChart, null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(6.dp))
                Text(
                    text       = "Detalle por día",
                    fontWeight = FontWeight.Bold,
                    fontSize   = 15.sp,
                    color      = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        item { CabeceraTabla() }

        itemsIndexed(detalle) { _, dia ->
            FilaDia(dia)
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
                Text("${resumen.totalCitas} citas", fontSize = 12.sp, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f))
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
