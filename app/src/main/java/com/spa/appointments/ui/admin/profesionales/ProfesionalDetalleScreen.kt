// Ruta: app/src/main/java/com/spa/appointments/ui/admin/profesionales/ProfesionalDetalleScreen.kt
package com.spa.appointments.ui.admin.profesionales

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.spa.appointments.domain.model.SedeAdmin
import com.spa.appointments.domain.model.ServicioAdmin
import com.spa.appointments.domain.model.ServicioProfesionalItem

// ─── Screen Principal ─────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfesionalDetalleScreen(
    idProfesional:     Int,
    onBack:            () -> Unit,
    viewModel:         ProfesionalesAdminViewModel = hiltViewModel(),
    catalogosViewModel: com.spa.appointments.ui.admin.catalogos.CatalogosViewModel = hiltViewModel()
) {
    val sedes              by viewModel.sedes.collectAsState()
    val sedesAsignadas     by viewModel.sedesAsignadas.collectAsState()
    val serviciosAsignados by viewModel.serviciosAsignados.collectAsState()
    val serviciosCatalogo  by catalogosViewModel.servicios.collectAsState()
    val uiState            by viewModel.uiState.collectAsState()

    val isLoading = uiState is ProfesionalesUiState.Loading

    var sedeSeleccionada by remember(sedesAsignadas) {
        mutableStateOf(sedesAsignadas.firstOrNull())
    }

    var serviciosEditados by remember(serviciosAsignados, serviciosCatalogo) {
        mutableStateOf(
            serviciosCatalogo.map { srv ->
                val asignado = serviciosAsignados.firstOrNull { it.idServicio == srv.id }
                Triple(srv, asignado != null, asignado?.precio?.toString() ?: srv.precioBase.toString())
            }
        )
    }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(idProfesional) {
        viewModel.cargarDatosInicial(idProfesional)
        catalogosViewModel.cargarServicios()
    }

    LaunchedEffect(uiState) {
        if (uiState is ProfesionalesUiState.Error) {
            snackbarHostState.showSnackbar(
                (uiState as ProfesionalesUiState.Error).mensaje
            )
            viewModel.resetState()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text       = "Asignaciones",
                                style      = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text  = "Sedes y servicios del profesional",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver")
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
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // ── Card: Sedes M3 ────────────────────────────────────────────
            OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
                shape    = RoundedCornerShape(18.dp),
                colors   = CardDefaults.outlinedCardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier          = Modifier.padding(bottom = 10.dp)
                    ) {
                        Icon(
                            Icons.Default.Store, null,
                            modifier = Modifier.size(20.dp),
                            tint     = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text       = "Sedes asignadas",
                            style      = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    HorizontalDivider(
                        color    = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    if (sedes.isEmpty()) {
                        EmptySeccionBanner(texto = "Sin sedes configuradas")
                    } else {
                        sedes.forEach { sede ->
                            SedeRadioRow(
                                sede       = sede,
                                selected   = sede.id == sedeSeleccionada,
                                onSelected = { sedeSeleccionada = sede.id }
                            )
                            if (sede != sedes.last()) {
                                HorizontalDivider(
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(14.dp))

                    Button(
                        onClick  = {
                            viewModel.guardarSedes(
                                idProfesional,
                                listOfNotNull(sedeSeleccionada)
                            )
                        },
                        enabled  = !isLoading && sedeSeleccionada != null,
                        shape    = RoundedCornerShape(12.dp),
                        modifier = Modifier.align(Alignment.End).height(42.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier    = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color       = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Icon(Icons.Default.Save, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Guardar sede", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // ── Card: Servicios M3 ────────────────────────────────────────
            OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
                shape    = RoundedCornerShape(18.dp),
                colors   = CardDefaults.outlinedCardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier          = Modifier.padding(bottom = 10.dp)
                    ) {
                        Icon(
                            Icons.Default.Spa, null,
                            modifier = Modifier.size(20.dp),
                            tint     = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text       = "Servicios asignados",
                            style      = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier   = Modifier.weight(1f)
                        )
                        val asignadosCount = serviciosEditados.count { it.second }
                        if (asignadosCount > 0) {
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Text(
                                    text       = "$asignadosCount",
                                    style      = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color      = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier   = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }

                    HorizontalDivider(
                        color    = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    if (serviciosCatalogo.isEmpty()) {
                        EmptySeccionBanner(texto = "Sin servicios en catálogo")
                    } else {
                        serviciosEditados.forEachIndexed { index, (srv, asignado, precio) ->
                            ServicioCheckRow(
                                servicio  = srv,
                                checked   = asignado,
                                precio    = precio,
                                onChecked = { checked ->
                                    serviciosEditados = serviciosEditados.toMutableList().also {
                                        it[index] = Triple(srv, checked, precio)
                                    }
                                },
                                onPrecio  = { nuevoPrecio ->
                                    serviciosEditados = serviciosEditados.toMutableList().also {
                                        it[index] = Triple(srv, asignado, nuevoPrecio)
                                    }
                                }
                            )
                            if (index < serviciosEditados.lastIndex) {
                                HorizontalDivider(
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(14.dp))

                    Button(
                        onClick  = {
                            val items = serviciosEditados
                                .filter { it.second }
                                .mapNotNull { (srv, _, precioStr) ->
                                    precioStr.toDoubleOrNull()?.let {
                                        ServicioProfesionalItem(srv.id, it)
                                    }
                                }
                            viewModel.guardarServicios(idProfesional, items)
                        },
                        enabled  = !isLoading,
                        shape    = RoundedCornerShape(12.dp),
                        modifier = Modifier.align(Alignment.End).height(42.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier    = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color       = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Icon(Icons.Default.Save, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Guardar servicios", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}

// ─── Banner de sección vacía ──────────────────────────────────────────────────

@Composable
private fun EmptySeccionBanner(texto: String) {
    Surface(
        shape    = RoundedCornerShape(12.dp),
        color    = MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier          = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Outlined.Info, null,
                modifier = Modifier.size(16.dp),
                tint     = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text  = texto,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ─── Fila de Sede M3 ──────────────────────────────────────────────────────────

@Composable
private fun SedeRadioRow(
    sede:       SedeAdmin,
    selected:   Boolean,
    onSelected: () -> Unit
) {
    Surface(
        shape    = RoundedCornerShape(12.dp),
        color    = if (selected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        else MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier          = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = selected,
                onClick  = onSelected
            )
            Spacer(Modifier.width(6.dp))
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (selected) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surfaceContainerHigh
            ) {
                Icon(
                    Icons.Default.Store, null,
                    modifier = Modifier.padding(6.dp).size(16.dp),
                    tint     = if (selected) MaterialTheme.colorScheme.onPrimaryContainer
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text       = sede.nombre,
                    style      = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                )
                if (!sede.direccion.isNullOrBlank()) {
                    Text(
                        text  = sede.direccion,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

// ─── Fila de Servicio M3 ──────────────────────────────────────────────────────

@Composable
private fun ServicioCheckRow(
    servicio:  ServicioAdmin,
    checked:   Boolean,
    precio:    String,
    onChecked: (Boolean) -> Unit,
    onPrecio:  (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Row(
            modifier          = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked         = checked,
                onCheckedChange = onChecked
            )
            Spacer(Modifier.width(6.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text       = servicio.nombre,
                    style      = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (checked) FontWeight.Bold else FontWeight.Medium
                )
                Text(
                    text  = servicio.nombreCategoria,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (checked) {
            OutlinedTextField(
                value         = precio,
                onValueChange = onPrecio,
                label         = { Text("Precio personalizado ($)") },
                leadingIcon   = { Icon(Icons.Default.AttachMoney, null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary) },
                modifier      = Modifier
                    .fillMaxWidth()
                    .padding(start = 44.dp, top = 4.dp),
                singleLine    = true,
                shape         = RoundedCornerShape(12.dp),
                colors        = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor   = MaterialTheme.colorScheme.surfaceContainerLow,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )
        }
    }
}
