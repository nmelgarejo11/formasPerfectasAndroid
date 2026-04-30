package com.spa.appointments.ui.clientes

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.spa.appointments.domain.model.Cliente

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeleccionarClienteScreen(
    onBack: () -> Unit,
    onClienteSeleccionado: (Cliente) -> Unit,
    vm: SeleccionarClienteViewModel = hiltViewModel()
) {
    val uiState by vm.uiState.collectAsState()
    val textoBusqueda by vm.textoBusqueda.collectAsState()

    var mostrarFormNuevo by remember { mutableStateOf(false) }

    // Formulario nuevo cliente
    var nuevoNombre   by remember { mutableStateOf("") }
    var nuevoApellido by remember { mutableStateOf("") }
    var nuevoTelefono by remember { mutableStateOf("") }
    var nuevoEmail    by remember { mutableStateOf("") }

    if (mostrarFormNuevo) {
        AlertDialog(
            onDismissRequest = { mostrarFormNuevo = false },
            icon  = { Icon(Icons.Default.PersonAdd, contentDescription = null) },
            title = { Text("Nuevo cliente") },
            text  = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value         = nuevoNombre,
                        onValueChange = { nuevoNombre = it },
                        label         = { Text("Nombre *") },
                        modifier      = Modifier.fillMaxWidth(),
                        singleLine    = true
                    )
                    OutlinedTextField(
                        value         = nuevoApellido,
                        onValueChange = { nuevoApellido = it },
                        label         = { Text("Apellido *") },
                        modifier      = Modifier.fillMaxWidth(),
                        singleLine    = true
                    )
                    OutlinedTextField(
                        value         = nuevoTelefono,
                        onValueChange = { nuevoTelefono = it },
                        label         = { Text("Teléfono") },
                        modifier      = Modifier.fillMaxWidth(),
                        singleLine    = true
                    )
                    OutlinedTextField(
                        value         = nuevoEmail,
                        onValueChange = { nuevoEmail = it },
                        label         = { Text("Email") },
                        modifier      = Modifier.fillMaxWidth(),
                        singleLine    = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (nuevoNombre.isBlank() || nuevoApellido.isBlank()) return@Button
                        vm.crearCliente(
                            nombre   = nuevoNombre.trim(),
                            apellido = nuevoApellido.trim(),
                            telefono = nuevoTelefono.trim(),
                            email    = nuevoEmail.trim()
                        ) { cliente ->
                            mostrarFormNuevo = false
                            onClienteSeleccionado(cliente)
                        }
                    },
                    enabled = nuevoNombre.isNotBlank() && nuevoApellido.isNotBlank()
                            && uiState !is SeleccionarClienteUiState.Creando
                ) {
                    if (uiState is SeleccionarClienteUiState.Creando) {
                        CircularProgressIndicator(
                            modifier    = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color       = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("Crear")
                    }
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { mostrarFormNuevo = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Seleccionar cliente") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = { mostrarFormNuevo = true }) {
                        Icon(Icons.Default.PersonAdd, "Nuevo cliente")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // ── Buscador ──────────────────────────────────────────────
            OutlinedTextField(
                value         = textoBusqueda,
                onValueChange = {
                    vm.buscar(it)
                },
                label         = { Text("Busqueda...") },
                leadingIcon   = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon  = {
                    if (textoBusqueda.isNotEmpty()) {
                        IconButton(onClick = { vm.resetear() }) {  // ← VM limpia todo
                            Icon(Icons.Default.Clear, "Limpiar")
                        }
                    }
                },
                modifier      = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                singleLine    = true
            )

            // ── Contenido ─────────────────────────────────────────────
            Box(modifier = Modifier.fillMaxSize()) {
                when (val state = uiState) {

                    is SeleccionarClienteUiState.Idle -> {
                        Column(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.secondary
                            )
                            Spacer(Modifier.height(12.dp))
                            Text(
                                "Busca por nombre o telefono",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(24.dp))
                            OutlinedButton(onClick = { mostrarFormNuevo = true }) {
                                Icon(Icons.Default.PersonAdd, null, Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Crear nuevo cliente")
                            }
                        }
                    }

                    is SeleccionarClienteUiState.Buscando -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }

                    is SeleccionarClienteUiState.SinResultados -> {
                        Column(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "No se encontraron clientes con \"$textoBusqueda\"",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(16.dp))
                            OutlinedButton(onClick = { mostrarFormNuevo = true }) {
                                Icon(Icons.Default.PersonAdd, null, Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Crear nuevo cliente")
                            }
                        }
                    }

                    is SeleccionarClienteUiState.Resultados -> {
                        LazyColumn(contentPadding = PaddingValues(horizontal = 16.dp)) {
                            items(state.clientes) { cliente ->
                                ClienteItem(
                                    cliente = cliente,
                                    onClick = { onClienteSeleccionado(cliente) }
                                )
                                HorizontalDivider()
                            }
                        }
                    }

                    is SeleccionarClienteUiState.Error -> {
                        Text(
                            text     = state.mensaje,
                            color    = MaterialTheme.colorScheme.error,
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(24.dp)
                        )
                    }

                    else -> Unit
                }
            }
        }
    }
}

@Composable
private fun ClienteItem(
    cliente: Cliente,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Default.Person,
            contentDescription = null,
            tint     = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(40.dp)
        )
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text       = cliente.nombreCompleto,
                style      = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
            cliente.telefono?.let {
                Text(
                    text  = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            cliente.email?.let {
                Text(
                    text  = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Icon(
            Icons.Default.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}