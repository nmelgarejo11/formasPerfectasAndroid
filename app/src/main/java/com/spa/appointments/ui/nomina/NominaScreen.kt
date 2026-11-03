package com.spa.appointments.ui.nomina

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.spa.appointments.R
import com.spa.appointments.domain.model.ContratoElegibleNomina
import com.spa.appointments.domain.model.PeriodoNominaItem
import com.spa.appointments.domain.model.PreliquidacionDetalleResponse
import com.spa.appointments.domain.model.PreliquidacionResponse
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NominaScreen(
    onBack: () -> Unit,
    viewModel: NominaViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    var contratoParaLiquidar by remember { mutableStateOf<ContratoElegibleNomina?>(null) }
    var idLiquidacionParaAnular by remember { mutableStateOf<Int?>(null) }
    var motivoAnulacion by remember { mutableStateOf("") }
    var menuPeriodosExpandido by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.eventos.collect { evento ->
            when (evento) {
                is NominaEvent.MostrarMensaje -> {
                    snackbarHostState.showSnackbar(evento.mensaje)
                }
                is NominaEvent.MostrarRecalcular -> {
                    snackbarHostState.showSnackbar(evento.mensaje)
                }
                is NominaEvent.AbrirPdfDesprendible -> {
                    abrirPdfConIntent(context, evento.file)
                }
                is NominaEvent.LiquidacionExitosa -> {
                    snackbarHostState.showSnackbar(evento.mensaje)
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.titulo_nomina), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            // Selector de Periodos
            if (uiState.cargandoPeriodos) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            } else if (uiState.periodos.isEmpty()) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.msg_sin_periodos),
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            } else {
                ExposedDropdownMenuBox(
                    expanded = menuPeriodosExpandido,
                    onExpandedChange = { menuPeriodosExpandido = it },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                ) {
                    val pSel = uiState.periodoSeleccionado
                    val textoPeriodo = if (pSel != null) {
                        "${pSel.nombre} (${formatearFechaDisplay(pSel.fechaInicio)} - ${formatearFechaDisplay(pSel.fechaFin)})"
                    } else "Seleccionar periodo"

                    OutlinedTextField(
                        value = textoPeriodo,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Periodo de Nómina") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = menuPeriodosExpandido) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    ExposedDropdownMenu(
                        expanded = menuPeriodosExpandido,
                        onDismissRequest = { menuPeriodosExpandido = false }
                    ) {
                        uiState.periodos.forEach { periodo ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(periodo.nombre, fontWeight = FontWeight.Bold)
                                        Text(
                                            "${formatearFechaDisplay(periodo.fechaInicio)} al ${formatearFechaDisplay(periodo.fechaFin)} - ${periodo.nombreEstado}",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                },
                                onClick = {
                                    viewModel.seleccionarPeriodo(periodo)
                                    menuPeriodosExpandido = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Campo de búsqueda
            OutlinedTextField(
                value = uiState.busqueda,
                onValueChange = { viewModel.actualizarBusqueda(it) },
                placeholder = { Text(stringResource(R.string.label_buscar_profesional)) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Buscar") },
                trailingIcon = {
                    if (uiState.busqueda.isNotBlank()) {
                        IconButton(onClick = { viewModel.actualizarBusqueda("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Limpiar búsqueda")
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Lista de Contratos / Indicadores
            if (uiState.cargandoContratos) {
                Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (uiState.contratos.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                    Text(
                        text = stringResource(R.string.msg_sin_contratos),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(uiState.contratos, key = { it.idContratoLaboral }) { contrato ->
                        ContratoNominaCard(
                            contrato = contrato,
                            enAccion = uiState.contratosEnAccion.contains(contrato.idContratoLaboral),
                            onPreliquidar = { viewModel.preliquidar(contrato) },
                            onVerDetalle = {
                                contrato.idPreliquidacionVigente?.let { viewModel.verDetallePreliquidacion(it) }
                            },
                            onLiquidar = { contratoParaLiquidar = contrato },
                            onRecalcular = { viewModel.preliquidar(contrato) },
                            onDesprendible = {
                                contrato.idLiquidacionVigente?.let { viewModel.descargarDesprendible(it) }
                            },
                            onAnular = {
                                contrato.idLiquidacionVigente?.let { idLiquidacionParaAnular = it }
                            }
                        )
                    }
                }
            }
        }
    }

    // BottomSheet de Detalle de Preliquidación
    if (uiState.detallePreliquidacion != null || uiState.cargandoDetalle) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.cerrarDetallePreliquidacion() }
        ) {
            val detalle = uiState.detallePreliquidacion
            if (uiState.cargandoDetalle || detalle == null) {
                Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                DetallePreliquidacionSheetContent(
                    detalle = detalle,
                    onLiquidarClick = {
                        val contratoCoincidente = uiState.contratos.find { it.idContratoLaboral == detalle.idContratoLaboral }
                        viewModel.cerrarDetallePreliquidacion()
                        if (contratoCoincidente != null) {
                            contratoParaLiquidar = contratoCoincidente
                        }
                    }
                )
            }
        }
    }

    // Diálogo de Confirmación para Liquidar
    contratoParaLiquidar?.let { contrato ->
        AlertDialog(
            onDismissRequest = { contratoParaLiquidar = null },
            title = { Text(stringResource(R.string.titulo_confirmar_liquidacion)) },
            text = {
                Text(
                    stringResource(
                        R.string.msg_confirmar_liquidacion,
                        contrato.profesionalNombre,
                        formatearCop(contrato.netoPreliquidado)
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val c = contrato
                        contratoParaLiquidar = null
                        viewModel.liquidar(c)
                    },
                    modifier = Modifier.defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                ) {
                    Text(stringResource(R.string.btn_confirmar))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { contratoParaLiquidar = null },
                    modifier = Modifier.defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                ) {
                    Text(stringResource(R.string.btn_cancelar))
                }
            }
        )
    }

    // Diálogo de Confirmación para Anular
    idLiquidacionParaAnular?.let { idLiq ->
        AlertDialog(
            onDismissRequest = {
                idLiquidacionParaAnular = null
                motivoAnulacion = ""
            },
            title = { Text(stringResource(R.string.titulo_anular_liquidacion)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        stringResource(R.string.nota_anulacion_prestamos),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                    OutlinedTextField(
                        value = motivoAnulacion,
                        onValueChange = { if (it.length <= 1000) motivoAnulacion = it },
                        label = { Text(stringResource(R.string.label_motivo_anulacion)) },
                        modifier = Modifier.fillMaxWidth(),
                        supportingText = { Text("${motivoAnulacion.length}/1000") },
                        isError = motivoAnulacion.isNotBlank() && motivoAnulacion.trim().isEmpty()
                    )
                }
            },
            confirmButton = {
                Button(
                    enabled = motivoAnulacion.trim().isNotBlank(),
                    onClick = {
                        val motivo = motivoAnulacion
                        val id = idLiq
                        idLiquidacionParaAnular = null
                        motivoAnulacion = ""
                        viewModel.anularLiquidacion(id, motivo)
                    },
                    modifier = Modifier.defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                ) {
                    Text(stringResource(R.string.btn_confirmar))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        idLiquidacionParaAnular = null
                        motivoAnulacion = ""
                    },
                    modifier = Modifier.defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                ) {
                    Text(stringResource(R.string.btn_cancelar))
                }
            }
        )
    }
}

@Composable
fun ContratoNominaCard(
    contrato: ContratoElegibleNomina,
    enAccion: Boolean,
    onPreliquidar: () -> Unit,
    onVerDetalle: () -> Unit,
    onLiquidar: () -> Unit,
    onRecalcular: () -> Unit,
    onDesprendible: () -> Unit,
    onAnular: () -> Unit
) {
    val estado = EstadoNominaContrato.desde(contrato)

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = contrato.profesionalNombre,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (!contrato.cargo.isNullOrBlank()) {
                        Text(
                            text = contrato.cargo,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (!contrato.profesionalDocumento.isNullOrBlank()) {
                        Text(
                            text = "Doc: ${contrato.profesionalDocumento}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))
                EstadoBadge(estado = estado)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Salario base:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = formatearCop(contrato.salarioBase),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            if (estado is EstadoNominaContrato.Liquidada && contrato.netoLiquidado != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Neto liquidado:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = formatearCop(contrato.netoLiquidado),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            if (estado is EstadoNominaContrato.PreliquidacionDesactualizada) {
                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Advertencia: ${textoMotivoNomina(estado.motivo)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Botones de acción por estado
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (enAccion) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                } else {
                    when (estado) {
                        is EstadoNominaContrato.SinPreliquidar -> {
                            Button(
                                onClick = onPreliquidar,
                                modifier = Modifier.defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                            ) {
                                Text(stringResource(R.string.btn_preliquidar))
                            }
                        }
                        is EstadoNominaContrato.PreliquidadaLista -> {
                            TextButton(
                                onClick = onVerDetalle,
                                modifier = Modifier.defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                            ) {
                                Text(stringResource(R.string.btn_ver_detalle))
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            OutlinedButton(
                                onClick = onRecalcular,
                                modifier = Modifier.defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                            ) {
                                Text(stringResource(R.string.btn_recalcular))
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            Button(
                                onClick = onLiquidar,
                                modifier = Modifier.defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                            ) {
                                Text(stringResource(R.string.btn_liquidar))
                            }
                        }
                        is EstadoNominaContrato.PreliquidacionDesactualizada -> {
                            Button(
                                onClick = onRecalcular,
                                modifier = Modifier.defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                            ) {
                                Text(stringResource(R.string.btn_recalcular))
                            }
                        }
                        is EstadoNominaContrato.Liquidada -> {
                            OutlinedButton(
                                onClick = onDesprendible,
                                modifier = Modifier.defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                            ) {
                                Icon(Icons.Default.Download, contentDescription = "Descargar PDF", modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(stringResource(R.string.btn_desprendible))
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            TextButton(
                                onClick = onAnular,
                                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                                modifier = Modifier.defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                            ) {
                                Text(stringResource(R.string.btn_anular))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EstadoBadge(estado: EstadoNominaContrato) {
    val (texto, containerColor, contentColor) = when (estado) {
        is EstadoNominaContrato.SinPreliquidar -> Triple(
            stringResource(R.string.estado_sin_preliquidar),
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.onSurfaceVariant
        )
        is EstadoNominaContrato.PreliquidadaLista -> Triple(
            stringResource(R.string.estado_preliquidada_lista),
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.onPrimaryContainer
        )
        is EstadoNominaContrato.PreliquidacionDesactualizada -> Triple(
            stringResource(R.string.estado_desactualizada),
            MaterialTheme.colorScheme.errorContainer,
            MaterialTheme.colorScheme.onErrorContainer
        )
        is EstadoNominaContrato.Liquidada -> Triple(
            stringResource(R.string.estado_liquidada),
            MaterialTheme.colorScheme.tertiaryContainer,
            MaterialTheme.colorScheme.onTertiaryContainer
        )
    }

    Surface(
        color = containerColor,
        contentColor = contentColor,
        shape = RoundedCornerShape(16.dp)
    ) {
        Text(
            text = texto,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun DetallePreliquidacionSheetContent(
    detalle: PreliquidacionResponse,
    onLiquidarClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "Detalle de Preliquidación",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        if (!detalle.profesionalNombre.isNullOrBlank()) {
            Text(
                text = detalle.profesionalNombre,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Totales Resumen
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Total Devengado:")
                    Text(formatearCop(detalle.totalDevengado), fontWeight = FontWeight.Bold)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Total Deducciones:")
                    Text(formatearCop(detalle.totalDeducciones), fontWeight = FontWeight.Bold)
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Neto a Pagar:", fontWeight = FontWeight.Bold)
                    Text(formatearCop(detalle.netoPagar), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("IBC Calculado:", style = MaterialTheme.typography.bodySmall)
                    Text(formatearCop(detalle.ibcCalculado), style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Lista de conceptos agrupados
        Text("Conceptos", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))

        val devengados = detalle.detalles.filter { it.tipoConcepto == 1 }
        val deducciones = detalle.detalles.filter { it.tipoConcepto == 2 }

        LazyColumn(
            modifier = Modifier.fillMaxWidth().weight(1f, fill = false),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (devengados.isNotEmpty()) {
                item {
                    Text("Devengados", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                }
                items(devengados) { item -> ConceptoItemRow(item) }
            }

            if (deducciones.isNotEmpty()) {
                item {
                    Text("Deducciones", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.error)
                }
                items(deducciones) { item -> ConceptoItemRow(item) }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Botón Liquidar / Advertencia
        if (detalle.liquidable == true) {
            Button(
                onClick = onLiquidarClick,
                modifier = Modifier.fillMaxWidth().defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
            ) {
                Text(stringResource(R.string.btn_liquidar))
            }
        } else {
            Surface(
                color = MaterialTheme.colorScheme.errorContainer,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "No liquidable: ${textoMotivoNomina(detalle.motivo)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }
    }
}

@Composable
fun ConceptoItemRow(item: PreliquidacionDetalleResponse) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(item.nombreConcepto ?: item.codigoConcepto ?: "Concepto", style = MaterialTheme.typography.bodyMedium)
            if (item.cantidad != null && item.cantidad > 0) {
                Text("Cantidad: ${item.cantidad}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Text(formatearCop(item.valor), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
    }
}

private fun abrirPdfConIntent(context: Context, file: File) {
    try {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(intent)
    } catch (_: ActivityNotFoundException) {
        Toast.makeText(context, "No hay aplicación instalada para abrir archivos PDF", Toast.LENGTH_LONG).show()
    } catch (e: Exception) {
        Toast.makeText(context, "Error al abrir el PDF: ${e.message}", Toast.LENGTH_LONG).show()
    }
}
