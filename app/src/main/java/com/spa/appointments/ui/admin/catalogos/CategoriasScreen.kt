package com.spa.appointments.ui.admin.catalogos

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
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
                        Icon(Icons.Default.Edit, contentDescription = "Volver")
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                uiState is CatalogosUiState.Loading && categorias.isEmpty() -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                categorias.isEmpty() -> {
                    Text(
                        text = "Sin categorías. Crea la primera.",
                        modifier = Modifier.align(Alignment.Center),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(categorias, key = { it.id }) { cat ->
                            CategoriaCard(
                                categoria = cat,
                                onEditar = {
                                    editando = cat
                                    showDialog = true
                                },
                                onToggle = { viewModel.toggleCategoria(cat.id) }
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

    if (showDialog) {
        CategoriaDialog(
            categoria = editando,
            guardando = uiState is CatalogosUiState.Loading,
            onGuardar = { nombre, icono ->
                viewModel.guardarCategoria(editando?.id, nombre, icono)
            },
            onDismiss = {
                showDialog = false
                editando = null
                viewModel.resetState()
            }
        )
    }
}

@Composable
private fun CategoriaCard(
    categoria: CategoriaAdmin,
    onEditar: () -> Unit,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
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
                    text = categoria.nombre,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
                if (!categoria.icono.isNullOrBlank()) {
                    Text(
                        text = categoria.icono,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
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
    var nombre by remember(categoria) { mutableStateOf(categoria?.nombre ?: "") }
    var icono  by remember(categoria) { mutableStateOf(categoria?.icono ?: "") }
    val esEdicion = categoria != null

    AlertDialog(
        onDismissRequest = { if (!guardando) onDismiss() },
        title = { Text(if (esEdicion) "Editar categoría" else "Nueva categoría") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = icono,
                    onValueChange = { icono = it },
                    label = { Text("Ícono (opcional)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onGuardar(nombre.trim(), icono.trim().ifBlank { null }) },
                enabled = nombre.isNotBlank() && !guardando
            ) {
                if (guardando) CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp
                )
                else Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !guardando) {
                Text("Cancelar")
            }
        }
    )
}