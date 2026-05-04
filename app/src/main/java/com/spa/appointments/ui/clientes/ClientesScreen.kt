package com.spa.appointments.ui.clientes

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onCrearCliente,
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("Nuevo cliente") }
            )
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

            // ── Buscador ──────────────────────────────────────────────────
            OutlinedTextField(
                value = viewModel.query,
                onValueChange = viewModel::onQueryChange,
                placeholder = { Text("Buscar cliente") },
                leadingIcon = {
                    Icon(Icons.Outlined.Search, contentDescription = null)
                },
                trailingIcon = {
                    if (viewModel.query.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onQueryChange("") }) {
                            Icon(
                                Icons.Outlined.Close,
                                contentDescription = "Limpiar búsqueda"
                            )
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = MaterialTheme.shapes.medium
            )

            Spacer(Modifier.height(12.dp))

            // ── Contenido según estado ────────────────────────────────────
            when (listState) {

                is ClientesUiState.Idle -> {
                    EmptyHint(
                        icon = Icons.Outlined.PersonSearch,
                        titulo = "Busca un cliente",
                        subtitulo = "Ingresa nombre, apellido o email para comenzar"
                    )
                }

                is ClientesUiState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                is ClientesUiState.Error -> {
                    EmptyHint(
                        icon = Icons.Outlined.ErrorOutline,
                        titulo = "Ocurrió un error",
                        subtitulo = (listState as ClientesUiState.Error).mensaje,
                        esError = true,
                        accionLabel = "Reintentar",
                        onAccion = { viewModel.onQueryChange(viewModel.query) }
                    )
                }

                is ClientesUiState.Results -> {
                    val clientes = (listState as ClientesUiState.Results).clientes

                    if (clientes.isEmpty()) {
                        EmptyHint(
                            icon = Icons.Outlined.SearchOff,
                            titulo = "Sin resultados",
                            subtitulo = "No encontramos clientes con \"${viewModel.query}\""
                        )
                    } else {
                        // Contador de resultados
                        Text(
                            "${clientes.size} cliente${if (clientes.size != 1) "s" else ""} encontrado${if (clientes.size != 1) "s" else ""}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(8.dp))

                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(bottom = 88.dp) // espacio para FAB
                        ) {
                            items(clientes, key = { it.id }) { cliente ->
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

// ── Item de cliente ───────────────────────────────────────────────────────────
@Composable
private fun ClienteItem(cliente: Cliente, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar con inicial
            Surface(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = cliente.nombre.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(Modifier.width(12.dp))

            // Datos
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${cliente.nombre} ${cliente.apellido}",
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.bodyLarge
                )
                cliente.telefono?.let {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Outlined.Phone,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            it,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                cliente.email?.let {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Outlined.Email,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            it,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Icon(
                Icons.Outlined.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ── Componente estado vacío / error ──────────────────────────────────────────
@Composable
private fun EmptyHint(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    titulo: String,
    subtitulo: String,
    esError: Boolean = false,
    accionLabel: String? = null,
    onAccion: (() -> Unit)? = null
) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(56.dp),
                tint = if (esError) MaterialTheme.colorScheme.error
                else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                titulo,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = if (esError) MaterialTheme.colorScheme.error
                else MaterialTheme.colorScheme.onSurface
            )
            Text(
                subtitulo,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (accionLabel != null && onAccion != null) {
                Spacer(Modifier.height(4.dp))
                OutlinedButton(onClick = onAccion) {
                    Text(accionLabel)
                }
            }
        }
    }
}