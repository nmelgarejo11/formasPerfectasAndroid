package com.spa.appointments.ui.clientes

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
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
fun ClientesScreen(
    onBack: () -> Unit,
    onVerCliente: (Int) -> Unit,
    onCrearCliente: () -> Unit,
    viewModel: ClientesViewModel = hiltViewModel()
) {
    val listState by viewModel.listState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Clientes", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onCrearCliente) {
                Icon(Icons.Default.Add, contentDescription = "Nuevo cliente")
            }
        },
        contentWindowInsets = WindowInsets(0)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            // Buscador
            OutlinedTextField(
                value = viewModel.query,
                onValueChange = viewModel::onQueryChange,
                placeholder = { Text("Busqueda...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(Modifier.height(12.dp))

            when (listState) {
                is ClientesUiState.Idle -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            "Buscar por Nombre, Apellido, E-mail...",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                is ClientesUiState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is ClientesUiState.Error -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            (listState as ClientesUiState.Error).mensaje,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
                is ClientesUiState.Results -> {
                    val clientes = (listState as ClientesUiState.Results).clientes
                    if (clientes.isEmpty()) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Sin resultados")
                        }
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(clientes) { cliente ->
                                ClienteItem(
                                    cliente = cliente,
                                    onClick = { onVerCliente(cliente.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ClienteItem(cliente: Cliente, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(40.dp)
            )
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    text = "${cliente.nombre} ${cliente.apellido}",
                    fontWeight = FontWeight.SemiBold
                )
                cliente.telefono?.let {
                    Text(it, style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                cliente.email?.let {
                    Text(it, style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}