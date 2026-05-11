package com.spa.appointments.ui.metodopago

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.spa.appointments.domain.model.MetodoPagoAdmin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MetodoPagoScreen(
    viewModel: MetodoPagoViewModel = hiltViewModel(),
    onVerDetalles: (MetodoPagoAdmin) -> Unit
) {
    val metodos by viewModel.metodos.collectAsStateWithLifecycle()
    val loading by viewModel.loading.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()

    var mostrarDialogo by remember { mutableStateOf(false) }
    var metodoEditar by remember { mutableStateOf<MetodoPagoAdmin?>(null) }

    LaunchedEffect(Unit) { viewModel.cargarMetodos() }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Métodos de Pago") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                metodoEditar = null
                mostrarDialogo = true
            }) { Icon(Icons.Default.Add, contentDescription = "Agregar") }
        }
    ) { padding ->
        Box(Modifier.padding(padding)) {
            if (loading) {
                CircularProgressIndicator(Modifier.align(Alignment.Center))
            } else {
                LazyColumn {
                    items(metodos, key = { it.id }) { metodo ->
                        MetodoPagoItem(
                            metodo = metodo,
                            onEditar = { metodoEditar = it; mostrarDialogo = true },
                            onVerDetalles = onVerDetalles
                        )
                    }
                }
            }
            error?.let {
                Snackbar(
                    modifier = Modifier.align(Alignment.BottomCenter),
                    action = { TextButton(onClick = viewModel::limpiarError) { Text("OK") } }
                ) { Text(it) }
            }
        }
    }

    if (mostrarDialogo) {
        MetodoPagoDialog(
            metodo = metodoEditar,
            onDismiss = { mostrarDialogo = false },
            onConfirmar = { nombre, activo ->
                if (metodoEditar == null) viewModel.crearMetodo(nombre)
                else viewModel.actualizarMetodo(metodoEditar!!.id, nombre, activo)
                mostrarDialogo = false
            }
        )
    }
}

@Composable
fun MetodoPagoItem(
    metodo: MetodoPagoAdmin,
    onEditar: (MetodoPagoAdmin) -> Unit,
    onVerDetalles: (MetodoPagoAdmin) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        Row(
            Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(metodo.nombre, style = MaterialTheme.typography.titleMedium)
                Text(
                    if (metodo.activo) "Activo" else "Inactivo",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (metodo.activo) Color.Green else Color.Red
                )
            }
            IconButton(onClick = { onVerDetalles(metodo) }) {
                Icon(Icons.Default.List, contentDescription = "Detalles")
            }
            IconButton(onClick = { onEditar(metodo) }) {
                Icon(Icons.Default.Edit, contentDescription = "Editar")
            }
        }
    }
}

@Composable
fun MetodoPagoDialog(
    metodo: MetodoPagoAdmin?,
    onDismiss: () -> Unit,
    onConfirmar: (String, Boolean) -> Unit
) {
    var nombre by remember { mutableStateOf(metodo?.nombre ?: "") }
    var activo by remember { mutableStateOf(metodo?.activo ?: true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (metodo == null) "Nuevo Método de Pago" else "Editar Método") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre") },
                    singleLine = true
                )
                if (metodo != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = activo, onCheckedChange = { activo = it })
                        Text("Activo")
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { if (nombre.isNotBlank()) onConfirmar(nombre, activo) },
                enabled = nombre.isNotBlank()
            ) { Text("Guardar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}