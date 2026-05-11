package com.spa.appointments.ui.metodopago

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.spa.appointments.domain.model.MetodoPagoDetalleAdmin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MetodoPagoDetalleScreen(
    metodoId: Int,
    metodoNombre: String,
    viewModel: MetodoPagoViewModel = hiltViewModel()
) {
    val detalles by viewModel.detalles.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()

    var mostrarDialogo by remember { mutableStateOf(false) }
    var detalleEditar by remember { mutableStateOf<MetodoPagoDetalleAdmin?>(null) }

    LaunchedEffect(metodoId) { viewModel.cargarDetalles(metodoId) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Detalles: $metodoNombre") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                detalleEditar = null
                mostrarDialogo = true
            }) { Icon(Icons.Default.Add, contentDescription = "Agregar") }
        }
    ) { padding ->
        Box(Modifier.padding(padding)) {
            LazyColumn {
                items(detalles, key = { it.id }) { detalle ->
                    DetalleItem(
                        detalle = detalle,
                        onEditar = { detalleEditar = it; mostrarDialogo = true }
                    )
                }
            }
            error?.let {
                Snackbar(Modifier.align(Alignment.BottomCenter)) { Text(it) }
            }
        }
    }

    if (mostrarDialogo) {
        DetalleDialog(
            detalle = detalleEditar,
            onDismiss = { mostrarDialogo = false },
            onConfirmar = { nombre, activo ->
                if (detalleEditar == null) viewModel.crearDetalle(metodoId, nombre)
                else viewModel.actualizarDetalle(metodoId, detalleEditar!!.id, nombre, activo)
                mostrarDialogo = false
            }
        )
    }
}

@Composable
fun DetalleItem(detalle: MetodoPagoDetalleAdmin, onEditar: (MetodoPagoDetalleAdmin) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(detalle.nombre, style = MaterialTheme.typography.titleMedium)
                Text(
                    if (detalle.activo) "Activo" else "Inactivo",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (detalle.activo) Color.Green else Color.Red
                )
            }
            IconButton(onClick = { onEditar(detalle) }) {
                Icon(Icons.Default.Edit, contentDescription = "Editar")
            }
        }
    }
}

@Composable
fun DetalleDialog(
    detalle: MetodoPagoDetalleAdmin?,
    onDismiss: () -> Unit,
    onConfirmar: (String, Boolean) -> Unit
) {
    var nombre by remember { mutableStateOf(detalle?.nombre ?: "") }
    var activo by remember { mutableStateOf(detalle?.activo ?: true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (detalle == null) "Nuevo Detalle" else "Editar Detalle") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre") },
                    singleLine = true
                )
                if (detalle != null) {
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