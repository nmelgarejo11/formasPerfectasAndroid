package com.spa.appointments.ui.nomina

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.spa.appointments.R
import com.spa.appointments.domain.model.ContratoElegibleNomina
import com.spa.appointments.domain.model.PreliquidacionDetalleResponse
import com.spa.appointments.domain.model.PreliquidacionResponse
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NominaScreen(
    onBack: () -> Unit,
    viewModel: NominaViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var idContratoParaLiquidar by rememberSaveable { mutableStateOf<Int?>(null) }
    var idLiquidacionParaAnular by rememberSaveable { mutableStateOf<Int?>(null) }
    var motivoAnulacion by rememberSaveable { mutableStateOf("") }
    var menuPeriodosExpandido by rememberSaveable { mutableStateOf(false) }

    val contratoParaLiquidar = remember(idContratoParaLiquidar, uiState.contratos) {
        uiState.contratos.find { it.idContratoLaboral == idContratoParaLiquidar }
    }

    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.eventos.collect { evento ->
                when (evento) {
                    is NominaEvent.MostrarMensaje -> {
                        scope.launch { snackbarHostState.showSnackbar(evento.mensaje) }
                    }
                    is NominaEvent.MostrarRecalcular -> {
                        scope.launch {
                            val result = snackbarHostState.showSnackbar(
                                message = evento.mensaje,
                                actionLabel = context.getString(R.string.btn_recalcular),
                                duration = SnackbarDuration.Long
                            )
                            if (result == SnackbarResult.ActionPerformed) {
                                uiState.contratos.find { it.idContratoLaboral == evento.idContratoLaboral }?.let {
                                    viewModel.preliquidar(it)
                                }
                            }
                        }
                    }
                    is NominaEvent.AbrirPdfDesprendible -> {
                        abrirPdfConIntent(context, evento.file)
                    }
                    is NominaEvent.LiquidacionExitosa -> {
                        scope.launch { snackbarHostState.showSnackbar(evento.mensaje) }
                    }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.titulo_nomina),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.cd_volver)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
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
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp))
            } else if (uiState.periodos.isEmpty()) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.msg_sin_periodos),
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
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
                    } else stringResource(R.string.placeholder_seleccionar_periodo)

                    OutlinedTextField(
                        value = textoPeriodo,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.label_periodo_nomina)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = menuPeriodosExpandido) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp)
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
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
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

            // Campo de búsqueda de profesionales
            OutlinedTextField(
                value = uiState.busqueda,
                onValueChange = { viewModel.actualizarBusqueda(it) },
                placeholder = { Text(stringResource(R.string.label_buscar_profesional)) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = stringResource(R.string.cd_buscar),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                trailingIcon = {
                    if (uiState.busqueda.isNotBlank()) {
                        IconButton(
                            onClick = { viewModel.actualizarBusqueda("") },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = stringResource(R.string.cd_limpiar_busqueda)
                            )
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(14.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Indicador de recarga sobre la lista
            if (uiState.cargandoContratos && uiState.contratos.isNotEmpty()) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp))
            }

            // Lista de Contratos / Indicadores
            if (uiState.cargandoContratos && uiState.contratos.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CircularProgressIndicator(strokeWidth = 3.dp)
                        Text(
                            text = "Cargando nómina…",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else if (!uiState.cargandoContratos && uiState.contratos.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ReceiptLong,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = stringResource(R.string.msg_sin_contratos),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(uiState.contratos, key = { it.idContratoLaboral }) { contrato ->
                        val enAccion = uiState.contratosEnAccion.contains(contrato.idContratoLaboral) ||
                                (contrato.idLiquidacionVigente != null && uiState.liquidacionesEnAccion.contains(contrato.idLiquidacionVigente))

                        ContratoNominaCard(
                            contrato = contrato,
                            enAccion = enAccion,
                            onPreliquidar = { viewModel.preliquidar(contrato) },
                            onVerDetalle = {
                                contrato.idPreliquidacionVigente?.let { viewModel.verDetallePreliquidacion(it) }
                            },
                            onLiquidar = { idContratoParaLiquidar = contrato.idContratoLaboral },
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
            onDismissRequest = { viewModel.cerrarDetallePreliquidacion() },
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            val detalle = uiState.detallePreliquidacion
            if (uiState.cargandoDetalle || detalle == null) {
                Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(strokeWidth = 3.dp)
                }
            } else {
                DetallePreliquidacionSheetContent(
                    detalle = detalle,
                    onLiquidarClick = {
                        val idContrato = detalle.idContratoLaboral
                        viewModel.cerrarDetallePreliquidacion()
                        idContratoParaLiquidar = idContrato
                    }
                )
            }
        }
    }

    // Diálogo de Confirmación para Liquidar
    contratoParaLiquidar?.let { contrato ->
        AlertDialog(
            onDismissRequest = { idContratoParaLiquidar = null },
            shape = RoundedCornerShape(20.dp),
            icon = {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Icon(
                        imageVector = Icons.Default.Payments,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(12.dp).size(24.dp)
                    )
                }
            },
            title = {
                Text(
                    text = stringResource(R.string.titulo_confirmar_liquidacion),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = stringResource(
                        R.string.msg_confirmar_liquidacion,
                        contrato.profesionalNombre,
                        formatearCop(contrato.netoPreliquidado)
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val c = contrato
                        idContratoParaLiquidar = null
                        viewModel.liquidar(c)
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier.defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                ) {
                    Text(stringResource(R.string.btn_confirmar), fontWeight = FontWeight.Medium)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { idContratoParaLiquidar = null },
                    shape = RoundedCornerShape(12.dp),
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
            shape = RoundedCornerShape(20.dp),
            icon = {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.errorContainer
                ) {
                    Icon(
                        imageVector = Icons.Default.Cancel,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(12.dp).size(24.dp)
                    )
                }
            },
            title = {
                Text(
                    text = stringResource(R.string.titulo_anular_liquidacion),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = stringResource(R.string.nota_anulacion_prestamos),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                    OutlinedTextField(
                        value = motivoAnulacion,
                        onValueChange = { if (it.length <= 1000) motivoAnulacion = it },
                        label = { Text(stringResource(R.string.label_motivo_anulacion)) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
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
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    ),
                    modifier = Modifier.defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                ) {
                    Text(stringResource(R.string.btn_confirmar), fontWeight = FontWeight.Medium)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        idLiquidacionParaAnular = null
                        motivoAnulacion = ""
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                ) {
                    Text(stringResource(R.string.btn_cancelar))
                }
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
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
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header del profesional + Avatar M3 + Badge de estado
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = contrato.profesionalNombre.firstOrNull()?.uppercase() ?: "P",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = contrato.profesionalNombre,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface
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
                            text = stringResource(R.string.lbl_doc_prefix, contrato.profesionalDocumento),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))
                EstadoBadge(estado = estado)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Resumen de Salario Base y Neto Liquidado
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.lbl_salario_base),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = formatearCop(contrato.salarioBase),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    if (estado is EstadoNominaContrato.Liquidada && contrato.netoLiquidado != null) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.lbl_neto_liquidado),
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = formatearCop(contrato.netoLiquidado),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            if (estado is EstadoNominaContrato.PreliquidacionDesactualizada) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Text(
                            text = stringResource(R.string.lbl_advertencia_prefix, textoMotivoNomina(estado.motivo)),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (enAccion) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                }
            } else {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.End),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    when (estado) {
                        is EstadoNominaContrato.SinPreliquidar -> {
                            Button(
                                onClick = onPreliquidar,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                            ) {
                                Text(stringResource(R.string.btn_preliquidar), fontWeight = FontWeight.Medium)
                            }
                        }
                        is EstadoNominaContrato.PreliquidadaLista -> {
                            TextButton(
                                onClick = onVerDetalle,
                                modifier = Modifier.defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                            ) {
                                Text(stringResource(R.string.btn_ver_detalle))
                            }
                            OutlinedButton(
                                onClick = onRecalcular,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                            ) {
                                Text(stringResource(R.string.btn_recalcular))
                            }
                            Button(
                                onClick = onLiquidar,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                            ) {
                                Text(stringResource(R.string.btn_liquidar), fontWeight = FontWeight.Medium)
                            }
                        }
                        is EstadoNominaContrato.PreliquidacionDesactualizada -> {
                            Button(
                                onClick = onRecalcular,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                            ) {
                                Text(stringResource(R.string.btn_recalcular), fontWeight = FontWeight.Medium)
                            }
                        }
                        is EstadoNominaContrato.Liquidada -> {
                            OutlinedButton(
                                onClick = onDesprendible,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                            ) {
                                Icon(
                                    Icons.Default.Download,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(stringResource(R.string.btn_desprendible))
                            }
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

// ─── Badge Accesible de Estado M3 ─────────────────────────────────────────────

private data class Quadruple<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)

@Composable
fun EstadoBadge(estado: EstadoNominaContrato) {
    val (texto, icono, containerColor, contentColor) = when (estado) {
        is EstadoNominaContrato.SinPreliquidar -> Quadruple(
            stringResource(R.string.estado_sin_preliquidar),
            Icons.Default.HourglassTop,
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.onSurfaceVariant
        )
        is EstadoNominaContrato.PreliquidadaLista -> Quadruple(
            stringResource(R.string.estado_preliquidada_lista),
            Icons.Default.CheckCircle,
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.onPrimaryContainer
        )
        is EstadoNominaContrato.PreliquidacionDesactualizada -> Quadruple(
            stringResource(R.string.estado_desactualizada),
            Icons.Default.Warning,
            MaterialTheme.colorScheme.errorContainer,
            MaterialTheme.colorScheme.onErrorContainer
        )
        is EstadoNominaContrato.Liquidada -> Quadruple(
            stringResource(R.string.estado_liquidada),
            Icons.Default.Verified,
            MaterialTheme.colorScheme.tertiaryContainer,
            MaterialTheme.colorScheme.onTertiaryContainer
        )
    }

    Surface(
        color = containerColor,
        contentColor = contentColor,
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        ) {
            Icon(
                imageVector = icono,
                contentDescription = null,
                modifier = Modifier.size(12.dp)
            )
            Text(
                text = texto,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold
            )
        }
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
            .padding(20.dp)
    ) {
        Text(
            text = stringResource(R.string.titulo_detalle_preliquidacion),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        if (!detalle.profesionalNombre.isNullOrBlank()) {
            Text(
                text = detalle.profesionalNombre,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Card Prominente de Neto a Pagar M3
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.lbl_neto_pagar),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = formatearCop(detalle.netoPagar),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                HorizontalDivider(
                    thickness = 0.5.dp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f)
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(stringResource(R.string.lbl_total_devengado), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    Text(formatearCop(detalle.totalDevengado), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(stringResource(R.string.lbl_total_deducciones), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    Text(formatearCop(detalle.totalDeducciones), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(stringResource(R.string.lbl_ibc_calculado), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f))
                    Text(formatearCop(detalle.ibcCalculado), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f))
                }
                if (detalle.diasComerciales != null) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(stringResource(R.string.lbl_dias_comerciales), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f))
                        Text(formatearCantidad(detalle.diasComerciales), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Lista de conceptos agrupados
        Text(
            text = stringResource(R.string.lbl_conceptos),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))

        val devengados = detalle.detalles.filter { it.tipoConcepto == 1 }
        val deducciones = detalle.detalles.filter { it.tipoConcepto == 2 }

        LazyColumn(
            modifier = Modifier.fillMaxWidth().weight(1f, fill = false),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (devengados.isNotEmpty()) {
                item {
                    Text(
                        text = stringResource(R.string.lbl_devengados).uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                items(devengados) { item -> ConceptoItemRow(item) }
            }

            if (deducciones.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.lbl_deducciones).uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                items(deducciones) { item -> ConceptoItemRow(item) }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Botón Liquidar / Advertencia
        if (detalle.liquidable == true) {
            Button(
                onClick = onLiquidarClick,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                modifier = Modifier.fillMaxWidth().defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
            ) {
                Icon(imageVector = Icons.Default.Payments, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.btn_liquidar), fontWeight = FontWeight.Bold)
            }
        } else {
            Surface(
                color = MaterialTheme.colorScheme.errorContainer,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.lbl_no_liquidable_prefix, textoMotivoNomina(detalle.motivo)),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.padding(14.dp)
                )
            }
        }
    }
}

@Composable
fun ConceptoItemRow(item: PreliquidacionDetalleResponse) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.nombreConcepto ?: item.codigoConcepto ?: stringResource(R.string.lbl_concepto_default),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (item.cantidad != null && item.cantidad > 0) {
                    Text(
                        text = stringResource(R.string.lbl_cantidad_prefix, formatearCantidad(item.cantidad)),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Text(
                text = formatearCop(item.valor),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

fun formatearCantidad(cantidad: Double?): String {
    if (cantidad == null || cantidad <= 0) return ""
    return if (cantidad % 1.0 == 0.0) {
        cantidad.toLong().toString()
    } else {
        cantidad.toString()
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
        Toast.makeText(context, context.getString(R.string.msg_error_abrir_pdf), Toast.LENGTH_LONG).show()
    } catch (e: Exception) {
        Toast.makeText(context, context.getString(R.string.msg_error_pdf_prefix, e.message ?: ""), Toast.LENGTH_LONG).show()
    }
}