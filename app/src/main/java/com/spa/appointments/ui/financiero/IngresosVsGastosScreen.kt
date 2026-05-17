package com.spa.appointments.ui.financiero

import android.app.DatePickerDialog
import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
    val context       = LocalContext.current
    val descargaState by vm.descargaState.collectAsState()
    var mostrarError  by remember { mutableStateOf<String?>(null) }
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
            title = { Text("Exportar reporte") },
            text  = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {

                    // Opción 1 — Guardar en dispositivo
                    OutlinedButton(
                        onClick  = {
                            mostrarOpcionesExcel = false
                            vm.guardarExcel(context)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector        = Icons.Default.FileDownload,
                            contentDescription = null,
                            modifier           = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Guardar en dispositivo")
                    }

                    // Opción 2 — Compartir (WhatsApp, correo, Drive…)
                    OutlinedButton(
                        onClick  = {
                            mostrarOpcionesExcel = false
                            vm.compartirExcel(context)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector        = Icons.Default.Share,
                            contentDescription = null,
                            modifier           = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Compartir…")
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { mostrarOpcionesExcel = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ingresos vs gastos") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    val tienedatos = uiState is IngresosVsGastosUiState.Success

                    if (descargaState is DescargaState.Cargando) {
                        CircularProgressIndicator(
                            modifier  = Modifier.size(24.dp).padding(end = 12.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        IconButton(
                            onClick = { if (tienedatos) mostrarOpcionesExcel = true },
                            enabled = tienedatos
                        ) {
                            Icon(
                                imageVector        = Icons.Default.FileDownload,
                                contentDescription = "Exportar Excel"
                            )
                        }
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

            SelectorSede(
                sedes              = sedes,
                sedeSeleccionada   = sedeSeleccionada,
                onSedeSeleccionada = { vm.seleccionarSede(it) }
            )

            Spacer(Modifier.height(8.dp))

            SelectorRango(
                fechaInicio    = fechaInicio,
                fechaFin       = fechaFin,
                onCambiarRango = { ini, fin -> vm.cambiarRango(ini, fin) }
            )

            Spacer(Modifier.height(16.dp))

            when (val state = uiState) {
                is IngresosVsGastosUiState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is IngresosVsGastosUiState.Error -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(state.mensaje, color = MaterialTheme.colorScheme.error)
                            Spacer(Modifier.height(12.dp))
                            Button(onClick = { vm.cargar() }) { Text("Reintentar") }
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
            title   = { Text("Error al exportar") },
            text    = { Text(msg) },
            confirmButton = {
                TextButton(onClick = { mostrarError = null }) { Text("Aceptar") }
            }
        )
    }
}

// ─── Selector sede ────────────────────────────────────────────────────────────

@Composable
private fun SelectorSede(
    sedes              : List<Sede>,
    sedeSeleccionada   : Sede?,
    onSedeSeleccionada : (Sede) -> Unit
) {
    var expandido by remember { mutableStateOf(false) }

    OutlinedCard(
        onClick  = { expandido = true },
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier          = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Sede", fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    sedeSeleccionada?.nombre ?: "Seleccionar sede",
                    fontWeight = FontWeight.Medium, fontSize = 14.sp
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
                text    = { Text(sede.nombre, fontWeight = FontWeight.Medium, fontSize = 14.sp) },
                onClick = { onSedeSeleccionada(sede); expandido = false }
            )
        }
    }
}

// ─── Selector rango con dos DatePicker ────────────────────────────────────────

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
            // Picker de fin — se crea con la fecha de inicio ya elegida
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

            // Picker de inicio
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
        modifier = Modifier.fillMaxWidth()
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
                modifier = Modifier.size(18.dp),
                tint     = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Período",
                    fontSize = 11.sp,
                    color    = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "${fechaInicio.format(formatter)}  →  ${fechaFin.format(formatter)}",
                    fontWeight = FontWeight.Medium,
                    fontSize   = 14.sp
                )
            }
            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
        }
    }
}

// ─── Contenido ────────────────────────────────────────────────────────────────

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
        // Tarjetas resumen del período
        item { TarjetasResumenPeriodo(resumen) }
        item { TarjetaUtilidadPeriodo(resumen.utilidadNeta) }

        // Cabecera tabla detalle
        item {
            Text("Detalle por día",
                fontWeight = FontWeight.SemiBold,
                fontSize   = 15.sp,
                modifier   = Modifier.padding(bottom = 4.dp))
        }

        // Cabecera columnas
        item { CabeceraTabla() }

        // Filas
        itemsIndexed(detalle) { _, dia ->
            FilaDia(dia)
        }
    }
}

// ─── Tarjetas resumen período ─────────────────────────────────────────────────

@Composable
private fun TarjetasResumenPeriodo(resumen: IngresosVsGastosResumen) {
    Row(
        modifier              = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(
            modifier = Modifier.weight(1f),
            colors   = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer),
            shape    = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text("Ingresos", fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(4.dp))
                Text("$${"%,.0f".format(resumen.totalIngresos)}",
                    fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text("${resumen.totalCitas} citas", fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Card(
            modifier = Modifier.weight(1f),
            colors   = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer),
            shape    = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text("Gastos", fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(4.dp))
                Text("$${"%,.0f".format(resumen.totalGastos)}",
                    fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text("del período", fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun TarjetaUtilidadPeriodo(utilidad: Double) {
    val esPositiva = utilidad >= 0
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors   = CardDefaults.cardColors(
            containerColor = if (esPositiva)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.errorContainer
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier              = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Text("Utilidad del período", fontWeight = FontWeight.Medium, fontSize = 14.sp)
            Text(
                "$${"%,.0f".format(utilidad)}",
                fontSize   = 20.sp,
                fontWeight = FontWeight.Bold,
                color      = if (esPositiva) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.error
            )
        }
    }
}

// ─── Tabla detalle ────────────────────────────────────────────────────────────

@Composable
private fun CabeceraTabla() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text("Fecha",     fontSize = 12.sp, fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1.2f))
        Text("Ingresos",  fontSize = 12.sp, fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f), textAlign = TextAlign.End)
        Text("Gastos",    fontSize = 12.sp, fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f), textAlign = TextAlign.End)
        Text("Utilidad",  fontSize = 12.sp, fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f), textAlign = TextAlign.End)
    }
    HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
}

@Composable
private fun FilaDia(dia: IngresosVsGastosDia) {
    val formatter    = DateTimeFormatter.ofPattern("dd/MM")
    val esPositiva   = dia.utilidadNeta >= 0
    val colorUtilidad = if (esPositiva) MaterialTheme.colorScheme.primary
    else MaterialTheme.colorScheme.error

    val fechaDisplay = runCatching {
        LocalDate.parse(dia.fecha).format(formatter)
    }.getOrElse { dia.fecha }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Text(fechaDisplay,
            fontSize = 13.sp,
            modifier = Modifier.weight(1.2f))
        Text("$${"%,.0f".format(dia.totalIngresos)}",
            fontSize = 13.sp,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.End)
        Text("$${"%,.0f".format(dia.totalGastos)}",
            fontSize = 13.sp,
            color    = MaterialTheme.colorScheme.error,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.End)
        Text("$${"%,.0f".format(dia.utilidadNeta)}",
            fontSize   = 13.sp,
            fontWeight = FontWeight.Medium,
            color      = colorUtilidad,
            modifier   = Modifier.weight(1f),
            textAlign  = TextAlign.End)
    }
    HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
}