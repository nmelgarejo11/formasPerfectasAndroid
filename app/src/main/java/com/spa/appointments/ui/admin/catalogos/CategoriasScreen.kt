package com.spa.appointments.ui.admin.catalogos

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.spa.appointments.domain.model.CategoriaAdmin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriasScreen(
    onBack: () -> Unit,
    viewModel: CatalogosViewModel = hiltViewModel()
) {
    val categorias by viewModel.categorias.collectAsState()
    val uiState   by viewModel.uiState.collectAsState()

    var showDialog by remember { mutableStateOf(false) }
    var editando   by remember { mutableStateOf<CategoriaAdmin?>(null) }
    var busqueda   by remember { mutableStateOf("") }

    val filtradas = remember(categorias, busqueda) {
        if (busqueda.isBlank()) categorias
        else categorias.filter { it.nombre.contains(busqueda, ignoreCase = true) }
    }

    LaunchedEffect(Unit) { viewModel.cargarCategorias() }

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
                title = { Text("Categorías") },
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
                Icon(Icons.Default.Add, contentDescription = "Nueva categoría")
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
                label = { Text("Buscar categoría") },
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
                    uiState is CatalogosUiState.Loading && categorias.isEmpty() -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                    filtradas.isEmpty() -> {
                        Text(
                            text = if (busqueda.isBlank()) "Sin categorías. Crea la primera."
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
                            items(filtradas, key = { it.id }) { cat ->
                                CategoriaCard(
                                    categoria = cat,
                                    onEditar  = { editando = cat; showDialog = true },
                                    onToggle  = { viewModel.toggleCategoria(cat.id) }
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
        CategoriaDialog(
            categoria = editando,
            guardando = uiState is CatalogosUiState.Loading,
            onGuardar = { nombre, icono -> viewModel.guardarCategoria(editando?.id, nombre, icono) },
            onDismiss = { showDialog = false; editando = null; viewModel.resetState() }
        )
    }
}

@Composable
private fun CategoriaCard(
    categoria: CategoriaAdmin,
    onEditar: () -> Unit,
    onToggle: () -> Unit
) {
    val iconoOpcion = ICONOS_DISPONIBLES.firstOrNull { it.clave == categoria.icono }

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
            // Ícono visual
            if (iconoOpcion != null) {
                Icon(
                    imageVector = iconoOpcion.icono,
                    contentDescription = null,
                    modifier = Modifier
                        .size(36.dp)
                        .padding(end = 4.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            } else {
                Spacer(Modifier.size(36.dp))
            }

            Spacer(Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = categoria.nombre,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }

            IconButton(onClick = onEditar) {
                Icon(Icons.Default.Edit, contentDescription = "Editar")
            }
            Switch(
                checked = categoria.estado,
                onCheckedChange = { onToggle() }
            )
        }
    }
}

@Composable
private fun CategoriaDialog(
    categoria: CategoriaAdmin?,
    guardando: Boolean,
    onGuardar: (nombre: String, icono: String?) -> Unit,
    onDismiss: () -> Unit
) {
    var nombre         by remember(categoria) { mutableStateOf(categoria?.nombre ?: "") }
    var iconoSeleccionado by remember(categoria) {
        mutableStateOf(ICONOS_DISPONIBLES.firstOrNull { it.clave == categoria?.icono })
    }
    var showIconoPicker by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = { if (!guardando) onDismiss() },
        title = { Text(if (categoria != null) "Editar categoría" else "Nueva categoría") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                // Selector de ícono
                OutlinedButton(
                    onClick = { showIconoPicker = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (iconoSeleccionado != null) {
                        Icon(
                            imageVector = iconoSeleccionado!!.icono,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(iconoSeleccionado!!.etiqueta)
                    } else {
                        Icon(Icons.Default.Add, null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Seleccionar ícono")
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onGuardar(nombre.trim(), iconoSeleccionado?.clave) },
                enabled = nombre.isNotBlank() && !guardando
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
            iconoActual = iconoSeleccionado?.clave,
            onSeleccionar = { opcion ->
                iconoSeleccionado = opcion
                showIconoPicker = false
            },
            onDismiss = { showIconoPicker = false }
        )
    }
}