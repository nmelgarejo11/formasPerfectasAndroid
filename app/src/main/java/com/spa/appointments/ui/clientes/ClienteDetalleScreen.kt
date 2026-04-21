package com.spa.appointments.ui.clientes

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.spa.appointments.domain.model.ActualizarClienteRequest
import com.spa.appointments.domain.model.CrearClienteRequest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClienteDetalleScreen(
    idCliente: Int,
    esNuevo: Boolean = false,
    onBack: () -> Unit,
    viewModel: ClientesViewModel = hiltViewModel()
) {
    val detalleState by viewModel.detalleState.collectAsState()
    val actionState  by viewModel.actionState.collectAsState()

    var nombre   by remember { mutableStateOf("") }
    var apellido by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var email    by remember { mutableStateOf("") }
    var initialized by remember { mutableStateOf(false) }

    var showConfirmDesactivar by remember { mutableStateOf(false) }

    // Cargar datos si es edición
    LaunchedEffect(idCliente) {
        if (!esNuevo) viewModel.cargarCliente(idCliente)
    }

    // Inicializar campos cuando llegan
    if (detalleState is ClienteDetalleState.Success && !initialized) {
        val c = (detalleState as ClienteDetalleState.Success).cliente
        nombre      = c.nombre
        apellido    = c.apellido
        telefono    = c.telefono ?: ""
        email       = c.email ?: ""
        initialized = true
    }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(actionState) {
        when (actionState) {
            is ClienteActionState.Success -> {
                snackbarHostState.showSnackbar(
                    if (esNuevo) "Cliente creado correctamente"
                    else "Cambios guardados correctamente"
                )
                viewModel.resetActionState()
                onBack()
            }
            is ClienteActionState.Error -> {
                snackbarHostState.showSnackbar(
                    (actionState as ClienteActionState.Error).mensaje
                )
                viewModel.resetActionState()
            }
            else -> Unit
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (esNuevo) "Nuevo cliente" else "Editar cliente",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->

        when {
            !esNuevo && detalleState is ClienteDetalleState.Loading -> {
                Box(
                    Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            !esNuevo && detalleState is ClienteDetalleState.Error -> {
                Box(
                    Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        (detalleState as ClienteDetalleState.Error).mensaje,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp, vertical = 20.dp)
                ) {
                    OutlinedTextField(
                        value = nombre,
                        onValueChange = { nombre = it },
                        label = { Text("Nombre") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(Modifier.height(12.dp))

                    OutlinedTextField(
                        value = apellido,
                        onValueChange = { apellido = it },
                        label = { Text("Apellido") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(Modifier.height(12.dp))

                    OutlinedTextField(
                        value = telefono,
                        onValueChange = { telefono = it },
                        label = { Text("Teléfono") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(Modifier.height(12.dp))

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(Modifier.height(32.dp))

                    // Botón guardar (AJUSTADO)
                    Button(
                        onClick = {
                            if (esNuevo) {
                                viewModel.crearCliente(
                                    CrearClienteRequest(
                                        nombre   = nombre,
                                        apellido = apellido,
                                        telefono = telefono.ifBlank { null },
                                        email    = email.ifBlank { null }
                                    )
                                )
                            } else {
                                viewModel.actualizarCliente(
                                    idCliente,
                                    ActualizarClienteRequest(
                                        nombre   = nombre,
                                        apellido = apellido,
                                        telefono = telefono.ifBlank { null },
                                        email    = email.ifBlank { null }
                                    )
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        enabled = actionState !is ClienteActionState.Loading
                    ) {
                        if (actionState is ClienteActionState.Loading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Guardar", fontWeight = FontWeight.Bold)
                        }
                    }

                    // Botón desactivar (solo en edición)
                    if (!esNuevo) {
                        Spacer(Modifier.height(12.dp))
                        OutlinedButton(
                            onClick = { showConfirmDesactivar = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                MaterialTheme.colorScheme.error
                            ),
                            enabled = actionState !is ClienteActionState.Loading
                        ) {
                            Text("Desactivar cliente", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // Diálogo de confirmación
    if (showConfirmDesactivar) {
        AlertDialog(
            onDismissRequest = { showConfirmDesactivar = false },
            title = { Text("¿Desactivar cliente?") },
            text = {
                Text("El cliente no podrá realizar reservas. Esta acción se puede revertir desde la base de datos.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showConfirmDesactivar = false
                        viewModel.desactivarCliente(idCliente)
                    }
                ) {
                    Text("Desactivar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDesactivar = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}