package com.spa.appointments.ui.admin.profesionales

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.spa.appointments.domain.model.ServicioProfesionalItem
import com.spa.appointments.domain.model.ServicioAdmin
import com.spa.appointments.domain.model.SedeAdmin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfesionalDetalleScreen(
    idProfesional: Int,
    onBack: () -> Unit,
    viewModel: ProfesionalesAdminViewModel = hiltViewModel(),
    catalogosViewModel: com.spa.appointments.ui.admin.catalogos.CatalogosViewModel = hiltViewModel()
) {
    val sedes             by viewModel.sedes.collectAsState()
    val sedesAsignadas    by viewModel.sedesAsignadas.collectAsState()
    val serviciosAsignados by viewModel.serviciosAsignados.collectAsState()
    val serviciosCatalogo  by catalogosViewModel.servicios.collectAsState()
    val uiState            by viewModel.uiState.collectAsState()

    // Estado local de checkboxes de sedes
    var sedesSeleccionadas by remember(sedesAsignadas) {
        mutableStateOf(sedesAsignadas.toSet())
    }

    // Estado local de servicios con precio editable
    var serviciosEditados by remember(serviciosAsignados, serviciosCatalogo) {
        mutableStateOf(
            serviciosCatalogo.map { srv ->
                val asignado = serviciosAsignados.firstOrNull { it.idServicio == srv.id }
                Triple(srv, asignado != null, asignado?.precio?.toString() ?: srv.precioBase.toString())
            }
        )
    }

    LaunchedEffect(Unit) {
        viewModel.cargarDatos()
        viewModel.cargarAsignaciones(idProfesional)
        catalogosViewModel.cargarServicios()
    }

    LaunchedEffect(uiState) {
        if (uiState is ProfesionalesUiState.Success) viewModel.resetState()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Asignaciones") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                // ─── SEDES ────────────────────────────────────────
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Sedes asignadas",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(Modifier.height(8.dp))

                        if (sedes.isEmpty()) {
                            Text(
                                "Sin sedes configuradas",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            sedes.forEach { sede ->
                                SedeCheckRow(
                                    sede       = sede,
                                    checked    = sede.id in sedesSeleccionadas,
                                    onChecked  = { checked ->
                                        sedesSeleccionadas = if (checked)
                                            sedesSeleccionadas + sede.id
                                        else
                                            sedesSeleccionadas - sede.id
                                    }
                                )
                            }
                        }

                        Spacer(Modifier.height(8.dp))
                        Button(
                            onClick = {
                                viewModel.guardarSedes(idProfesional, sedesSeleccionadas.toList())
                            },
                            enabled = uiState !is ProfesionalesUiState.Loading,
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Guardar sedes")
                        }
                    }
                }

                // ─── SERVICIOS ────────────────────────────────────
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Servicios asignados",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(Modifier.height(8.dp))

                        if (serviciosCatalogo.isEmpty()) {
                            Text(
                                "Sin servicios en catálogo",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
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
                                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                                }
                            }
                        }

                        Spacer(Modifier.height(8.dp))
                        Button(
                            onClick = {
                                val items = serviciosEditados
                                    .filter { it.second }
                                    .mapNotNull { (srv, _, precioStr) ->
                                        precioStr.toDoubleOrNull()?.let {
                                            ServicioProfesionalItem(srv.id, it)
                                        }
                                    }
                                viewModel.guardarServicios(idProfesional, items)
                            },
                            enabled = uiState !is ProfesionalesUiState.Loading,
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Guardar servicios")
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))
            }

            if (uiState is ProfesionalesUiState.Loading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }

            if (uiState is ProfesionalesUiState.Error) {
                Snackbar(
                    modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp)
                ) {
                    Text((uiState as ProfesionalesUiState.Error).mensaje)
                }
            }
        }
    }
}

@Composable
private fun SedeCheckRow(
    sede: SedeAdmin,
    checked: Boolean,
    onChecked: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(checked = checked, onCheckedChange = onChecked)
        Spacer(Modifier.width(8.dp))
        Column {
            Text(sede.nombre, style = MaterialTheme.typography.bodyMedium)
            if (!sede.direccion.isNullOrBlank()) {
                Text(
                    sede.direccion,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ServicioCheckRow(
    servicio: ServicioAdmin,
    checked:  Boolean,
    precio:   String,
    onChecked: (Boolean) -> Unit,
    onPrecio:  (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(checked = checked, onCheckedChange = onChecked)
        Spacer(Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(servicio.nombre, style = MaterialTheme.typography.bodyMedium)
            Text(
                servicio.nombreCategoria,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (checked) {
            OutlinedTextField(
                value = precio,
                onValueChange = onPrecio,
                label = { Text("Precio") },
                modifier = Modifier.width(100.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )
        }
    }
}