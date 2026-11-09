// Ruta: app/src/main/java/com/spa/appointments/ui/clientes/ClientesScreen.kt
package com.spa.appointments.ui.clientes

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.spa.appointments.domain.model.Cliente

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientesScreen(
    onBack: () -> Unit,
    viewModel: ClientesViewModel = hiltViewModel()
) {
    val listState by viewModel.listState.collectAsState()
    val query = viewModel.query

    // ── Estado local dialog ───────────────────────────────
    var showClienteDialog by remember { mutableStateOf(false) }
    var clienteSeleccionadoId by remember { mutableStateOf<Int?>(null) }
    var esNuevoCliente by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = "Clientes",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )

                            val subtitulo = when (listState) {
                                is ClientesUiState.Results -> {
                                    val total = (listState as ClientesUiState.Results).clientes.size
                                    "$total ${if (total == 1) "cliente encontrado" else "clientes encontrados"}"
                                }

                                is ClientesUiState.Idle -> "Busca por nombre o teléfono"
                                else -> ""
                            }

                            if (subtitulo.isNotEmpty()) {
                                Text(
                                    text = subtitulo,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Volver"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )

                // 🎨 Canvas: Acento decorativo debajo de la TopAppBar
                val primaryColor   = MaterialTheme.colorScheme.primary
                val secondaryColor = MaterialTheme.colorScheme.secondary
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                ) {
                    drawRect(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                primaryColor.copy(alpha = 0.4f),
                                secondaryColor.copy(alpha = 0.2f),
                                primaryColor.copy(alpha = 0.05f)
                            )
                        )
                    )
                }

                HorizontalDivider(
                    thickness = 0.5.dp,
                    color     = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )
            }
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    esNuevoCliente = true
                    clienteSeleccionadoId = 0
                    showClienteDialog = true
                },
                icon = { Icon(Icons.Outlined.PersonAdd, null) },
                text = { Text("Nuevo cliente", fontWeight = FontWeight.Bold) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(16.dp)
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Spacer(Modifier.height(8.dp))

            // ── Buscador M3 ───────────────────────────
            OutlinedTextField(
                value = query,
                onValueChange = viewModel::onQueryChange,
                placeholder = {
                    Text("Nombre, teléfono o email…", style = MaterialTheme.typography.bodyMedium)
                },
                leadingIcon = {
                    Icon(
                        Icons.Outlined.Search,
                        null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(
                            onClick = { viewModel.onQueryChange("") }
                        ) {
                            Icon(
                                Icons.Outlined.Close,
                                "Limpiar",
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor   = MaterialTheme.colorScheme.surfaceContainerLow,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow
                ),
                singleLine = true
            )

            // ── Contenido ───────────────────────────
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                when (listState) {
                    is ClientesUiState.Loading -> {
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.primaryContainer,
                                strokeWidth = 3.dp,
                                modifier = Modifier.size(36.dp)
                            )
                            Text(
                                text = "Buscando clientes…",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    is ClientesUiState.Idle -> {
                        ClienteEmptyState(
                            icon = Icons.Outlined.PersonSearch,
                            titulo = "Busca un cliente",
                            subtitulo = "Escribe un nombre, teléfono o correo\npara encontrar resultados."
                        )
                    }

                    is ClientesUiState.Error -> {
                        ClienteEmptyState(
                            icon = Icons.Outlined.ErrorOutline,
                            titulo = "Ocurrió un error",
                            subtitulo = (listState as ClientesUiState.Error).mensaje,
                            colorIcono = MaterialTheme.colorScheme.error,
                            accionLabel = "Reintentar",
                            onAccion = { viewModel.onQueryChange(query) }
                        )
                    }

                    is ClientesUiState.Results -> {
                        val resultsState = listState as ClientesUiState.Results
                        val clientes = resultsState.clientes
                        val cargandoMas = resultsState.cargandoMas

                        if (clientes.isEmpty()) {
                            ClienteEmptyState(
                                icon = Icons.Outlined.SearchOff,
                                titulo = "Sin resultados",
                                subtitulo = if (query.isBlank()) "No hay clientes registrados." else "No encontramos clientes que coincidan\ncon \"$query\"."
                            )
                        } else {
                            val lazyListState = androidx.compose.foundation.lazy.rememberLazyListState()
                            val shouldLoadMore by remember {
                                derivedStateOf {
                                    val layoutInfo = lazyListState.layoutInfo
                                    val totalItems = layoutInfo.totalItemsCount
                                    val lastVisibleItem = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
                                    totalItems > 0 && lastVisibleItem >= totalItems - 3
                                }
                            }

                            LaunchedEffect(shouldLoadMore) {
                                if (shouldLoadMore) {
                                    viewModel.cargarMasClientes()
                                }
                            }

                            LazyColumn(
                                state = lazyListState,
                                contentPadding = PaddingValues(
                                    start = 16.dp,
                                    end = 16.dp,
                                    top = 8.dp,
                                    bottom = 96.dp
                                ),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(
                                    clientes,
                                    key = { it.id }
                                ) { cliente ->
                                    ClienteCard(
                                        cliente = cliente,
                                        onVerDetalle = {
                                            esNuevoCliente = false
                                            clienteSeleccionadoId = cliente.id
                                            showClienteDialog = true
                                        }
                                    )
                                }

                                if (cargandoMas) {
                                    item {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(16.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(24.dp),
                                                strokeWidth = 2.dp,
                                                color = MaterialTheme.colorScheme.primary
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
    }

    // ── Dialog Cliente ───────────────────────────
    if (showClienteDialog && clienteSeleccionadoId != null) {
        ClienteDetalleDialog(
            idCliente = clienteSeleccionadoId!!,
            esNuevo   = esNuevoCliente,
            onDismiss = {
                showClienteDialog = false
                clienteSeleccionadoId = null
                viewModel.cargarClientes(reset = true)
            }
        )
    }
}

// ─── Card Cliente M3 ──────────────────────────────────────────────────────────

@Composable
private fun ClienteCard(
    cliente: Cliente,
    onVerDetalle: () -> Unit
) {
    val iniciales = buildString {
        append(cliente.nombre.firstOrNull() ?: "")
        append(cliente.apellido.firstOrNull() ?: "")
    }.uppercase().ifBlank { "?" }

    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onVerDetalle() }
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // ── Avatar ───────────────────────────────
            Surface(
                modifier = Modifier.size(48.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = iniciales,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(Modifier.width(12.dp))

            // ── Información Cliente ─────────────────
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "${cliente.nombre} ${cliente.apellido}".trim(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(Modifier.height(4.dp))

                val contacto = listOfNotNull(cliente.telefono, cliente.email)

                if (contacto.isNotEmpty()) {
                    cliente.telefono?.let {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Outlined.Phone,
                                null,
                                modifier = Modifier.size(13.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = it,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    cliente.email?.let {
                        Spacer(Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Outlined.Email,
                                null,
                                modifier = Modifier.size(13.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = it,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                } else {
                    Text(
                        text = "Sin información de contacto",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }

            // ── Flecha derecha ─────────────────────
            Icon(
                Icons.Outlined.ChevronRight,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ─── Empty State M3 con Canvas Glow ───────────────────────────────────────────

@Composable
private fun ClienteEmptyState(
    icon: ImageVector,
    titulo: String,
    subtitulo: String,
    colorIcono: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    accionLabel: String? = null,
    onAccion: (() -> Unit)? = null
) {
    val primaryColor = MaterialTheme.colorScheme.primary

    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(
            modifier = Modifier
                .padding(32.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier.size(80.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawCircle(color = primaryColor.copy(alpha = 0.15f))
                }
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceContainerHigh
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.padding(18.dp).size(36.dp),
                        tint = colorIcono
                    )
                }
            }

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
                Button(
                    onClick = onAccion,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        Icons.Outlined.Refresh,
                        null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(accionLabel, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
