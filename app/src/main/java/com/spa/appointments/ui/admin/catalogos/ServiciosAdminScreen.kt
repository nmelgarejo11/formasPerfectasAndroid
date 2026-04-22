package com.spa.appointments.ui.admin.catalogos

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.spa.appointments.domain.model.CategoriaAdmin
import com.spa.appointments.domain.model.ServicioAdmin
import com.spa.appointments.domain.model.ServicioRequest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiciosAdminScreen(
    onBack: () -> Unit,
    viewModel: CatalogosViewModel = hiltViewModel()
) {
    val servicios  by viewModel.servicios.collectAsState()
    val categorias by viewModel.categorias.collectAsState()
    val uiState    by viewModel.uiState.collectAsState()

    var showDialog by remember { mutableStateOf(false) }
    var editando   by remember { mutableStateOf<ServicioAdmin?>(null) }
    var busqueda   by remember { mutableStateOf("") }

    val filtrados = remember(servicios, busqueda) {
        if (busqueda.isBlank()) servicios
        else servicios.filter {
            it.nombre.contains(busqueda, ignoreCase = true) ||
                    it.nombreCategoria.contains(busqueda, ignoreCase = true)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.cargarServicios()
        viewModel.cargarCategorias()
    }

    LaunchedEffect(uiState) {
        if (uiState is CatalogosUiState.Success) {
            showDialog = false
            editando = null
            viewModel.resetState()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Servicios") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                editando = null
                showDialog = true
            }) {
                Icon(Icons.Default.Add, contentDescription = "Nuevo servicio")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Buscador
            OutlinedTextField(
                value = busqueda,
                onValueChange = { busqueda = it },
                label = { Text("Buscar servicio o categoría") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                trailingIcon = {
                    if (busqueda.isNotBlank()) {
                        IconButton(onClick = { busqueda = "" }) {
                            Icon(Icons.Default.Clear, null)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                singleLine = true
            )

            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    uiState is CatalogosUiState.Loading && servicios.isEmpty() -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                    filtrados.isEmpty() -> {
                        Text(
                            text = if (busqueda.isBlank()) "Sin servicios. Crea el primero."
                            else "Sin resultados para \"$busqueda\"",
                            modifier = Modifier.align(Alignment.Center),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    else -> {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(filtrados, key = { it.id }) { srv ->
                                ServicioAdminCard(
                                    servicio = srv,
                                    onEditar = { editando = srv; showDialog = true },
                                    onToggle = { viewModel.toggleServicio(srv.id) }
                                )
                            }
                        }
                    }
                }

                if (uiState is CatalogosUiState.Error) {
                    Snackbar(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(16.dp)
                    ) {
                        Text((uiState as CatalogosUiState.Error).mensaje)
                    }
                }
            }
        }
    }

    if (showDialog) {
        ServicioDialog(
            servicio   = editando,
            categorias = categorias,
            guardando  = uiState is CatalogosUiState.Loading,
            onGuardar  = { req -> viewModel.guardarServicio(editando?.id, req) },
            onDismiss  = { showDialog = false; editando = null; viewModel.resetState() }
        )
    }
}

@Composable
private fun ServicioAdminCard(
    servicio: ServicioAdmin,
    onEditar: () -> Unit,
    onToggle: () -> Unit
) {
    val iconoOpcion = ICONOS_DISPONIBLES.firstOrNull { it.clave == servicio.icono }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {


            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = servicio.nombre,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = servicio.nombreCategoria,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${servicio.duracionMinutos} min  •  ${"%.0f".format(servicio.precioBase)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(onClick = onEditar) {
                Icon(Icons.Default.Edit, contentDescription = "Editar")
            }
            Switch(
                checked = servicio.estado,
                onCheckedChange = { onToggle() }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ServicioDialog(
    servicio: ServicioAdmin?,
    categorias: List<CategoriaAdmin>,
    guardando: Boolean,
    onGuardar: (ServicioRequest) -> Unit,
    onDismiss: () -> Unit
) {
    var nombre      by remember(servicio) { mutableStateOf(servicio?.nombre ?: "") }
    var descripcion by remember(servicio) { mutableStateOf(servicio?.descripcion ?: "") }
    var duracion    by remember(servicio) { mutableStateOf(servicio?.duracionMinutos?.toString() ?: "") }
    var precio      by remember(servicio) { mutableStateOf(servicio?.precioBase?.toString() ?: "") }

    var iconoSeleccionado by remember(servicio) {
        mutableStateOf(ICONOS_DISPONIBLES.firstOrNull { it.clave == servicio?.icono })
    }
    var showIconoPicker by remember { mutableStateOf(false) }

    val categoriaInicial = categorias.firstOrNull { it.id == servicio?.idCategoria }
    var categoriaSeleccionada by remember(servicio) { mutableStateOf(categoriaInicial) }
    var expandedCat           by remember { mutableStateOf(false) }

    val esValido = nombre.isNotBlank()
            && duracion.toIntOrNull() != null
            && precio.toDoubleOrNull() != null
            && categoriaSeleccionada != null

    AlertDialog(
        onDismissRequest = { if (!guardando) onDismiss() },
        title = { Text(if (servicio != null) "Editar servicio" else "Nuevo servicio") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Dropdown categoría
                ExposedDropdownMenuBox(
                    expanded = expandedCat,
                    onExpandedChange = { expandedCat = it }
                ) {
                    OutlinedTextField(
                        value = categoriaSeleccionada?.nombre ?: "Selecciona categoría",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Categoría *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expandedCat) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedCat,
                        onDismissRequest = { expandedCat = false }
                    ) {
                        categorias.filter { it.estado }.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat.nombre) },
                                onClick = { categoriaSeleccionada = cat; expandedCat = false }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    label = { Text("Descripción") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = duracion,
                        onValueChange = { duracion = it },
                        label = { Text("Minutos *") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    OutlinedTextField(
                        value = precio,
                        onValueChange = { precio = it },
                        label = { Text("Precio *") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                }

                // Selector de ícono
//                OutlinedButton(
//                    onClick = { showIconoPicker = true },
//                    modifier = Modifier.fillMaxWidth()
//                ) {
//                    if (iconoSeleccionado != null) {
//                        Icon(iconoSeleccionado!!.icono, null, modifier = Modifier.size(20.dp))
//                        Spacer(Modifier.width(8.dp))
//                        Text(iconoSeleccionado!!.etiqueta)
//                    } else {
//                        Icon(Icons.Default.Add, null, modifier = Modifier.size(20.dp))
//                        Spacer(Modifier.width(8.dp))
//                        Text("Seleccionar ícono")
//                    }
//                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onGuardar(
                        ServicioRequest(
                            idCategoria     = categoriaSeleccionada!!.id,
                            nombre          = nombre.trim(),
                            descripcion     = descripcion.trim().ifBlank { null },
                            duracionMinutos = duracion.toInt(),
                            precioBase      = precio.toDouble(),
                            icono           = iconoSeleccionado?.clave
                        )
                    )
                },
                enabled = esValido && !guardando
            ) {
                if (guardando) CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                else Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !guardando) { Text("Cancelar") }
        }
    )

    if (showIconoPicker) {
        IconoPickerDialog(
            iconoActual   = iconoSeleccionado?.clave,
            onSeleccionar = { opcion -> iconoSeleccionado = opcion; showIconoPicker = false },
            onDismiss     = { showIconoPicker = false }
        )
    }
}