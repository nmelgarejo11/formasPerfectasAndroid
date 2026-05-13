package com.spa.appointments.ui.clientes

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
    val query = viewModel.query

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Clientes",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        // Subtítulo con contador dinámico basado en la lógica existente
                        if (listState is ClientesUiState.Results) {
                            val total = (listState as ClientesUiState.Results).clientes.size
                            Text(
                                text = "$total ${if (total == 1) "cliente" else "clientes"}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onCrearCliente,
                icon = { Icon(Icons.Default.Add, null) },
                text = { Text("Nuevo cliente") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // ── Buscador Estilizado (12.dp) ───────────────────────────────
            OutlinedTextField(
                value = query,
                onValueChange = viewModel::onQueryChange,
                label = { Text("Buscar cliente") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onQueryChange("") }) {
                            Icon(Icons.Default.Clear, null)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Box(modifier = Modifier.fillMaxSize()) {
                when (listState) {
                    is ClientesUiState.Loading -> {
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            CircularProgressIndicator()
                            Text(
                                "Buscando...",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    is ClientesUiState.Idle -> {
                        ClienteEmptyState(
                            icon = Icons.Default.PersonSearch,
                            titulo = "Busca un cliente",
                            subtitulo = "Ingresa nombre, apellido o email para comenzar."
                        )
                    }

                    is ClientesUiState.Error -> {
                        ClienteEmptyState(
                            icon = Icons.Default.ErrorOutline,
                            titulo = "Ocurrió un error",
                            subtitulo = (listState as ClientesUiState.Error).mensaje,
                            colorIcono = MaterialTheme.colorScheme.error,
                            accionLabel = "Reintentar",
                            onAccion = { viewModel.onQueryChange(query) }
                        )
                    }

                    is ClientesUiState.Results -> {
                        val clientes = (listState as ClientesUiState.Results).clientes
                        if (clientes.isEmpty()) {
                            ClienteEmptyState(
                                icon = Icons.Default.SearchOff,
                                titulo = "Sin resultados",
                                subtitulo = "No encontramos clientes que coincidan con \"$query\"."
                            )
                        } else {
                            LazyColumn(
                                contentPadding = PaddingValues(
                                    start = 16.dp,
                                    end = 16.dp,
                                    top = 4.dp,
                                    bottom = 88.dp
                                ),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(clientes, key = { it.id }) { cliente ->
                                    ClienteCard(
                                        cliente = cliente,
                                        onVerDetalle = { onVerCliente(cliente.id) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ─── Card de Cliente ──────────────────────────────────────────────────────────

@Composable
private fun ClienteCard(
    cliente: Cliente,
    onVerDetalle: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }
    val inicial = (cliente.nombre.firstOrNull()?.toString() ?: "?").uppercase()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onVerDetalle() },
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar (Estilo 56.dp)
            Surface(
                modifier = Modifier.size(56.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = inicial,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(Modifier.width(12.dp))

            // Información
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${cliente.nombre} ${cliente.apellido}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )

                cliente.telefono?.let {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Phone, null,
                            modifier = Modifier.size(11.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                cliente.email?.let {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Email, null,
                            modifier = Modifier.size(11.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Menú contextual (Estilo base)
            Box {
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(
                        Icons.Default.MoreVert, "Opciones",
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Ver perfil") },
                        leadingIcon = { Icon(Icons.Default.AccountCircle, null) },
                        onClick = { menuExpanded = false; onVerDetalle() }
                    )
                    DropdownMenuItem(
                        text = { Text("Nueva cita") },
                        leadingIcon = { Icon(Icons.Default.Event, null) },
                        onClick = { menuExpanded = false; /* Implementar si existe */ }
                    )
                }
            }
        }
    }
}

// ─── Empty State Estilizado ──────────────────────────────────────────────────

@Composable
private fun ClienteEmptyState(
    icon: ImageVector,
    titulo: String,
    subtitulo: String,
    colorIcono: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurfaceVariant,
    accionLabel: String? = null,
    onAccion: (() -> Unit)? = null
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.padding(20.dp).size(48.dp),
                tint = colorIcono
            )
        }
        Spacer(Modifier.height(16.dp))
        Text(
            text = titulo,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Text(
            text = subtitulo,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        if (accionLabel != null && onAccion != null) {
            Spacer(Modifier.height(16.dp))
            Button(onClick = onAccion) {
                Text(accionLabel)
            }
        }
    }
}