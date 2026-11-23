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
import com.spa.appointments.ui.components.SinPermisoContent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientesScreen(
    onBack: () -> Unit,
    viewModel: ClientesViewModel = hiltViewModel()
) {
    val listState    by viewModel.listState.collectAsState()
    val actionState  by viewModel.actionState.collectAsState()

    var showClienteDialog     by remember { mutableStateOf(false) }
    var clienteSeleccionadoId by remember { mutableIntStateOf(0) }
    var esNuevoCliente        by remember { mutableStateOf(true) }

    val query = viewModel.query
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(actionState) {
        when (actionState) {
            is ClienteActionState.Success -> {
                showClienteDialog = false
                viewModel.resetActionState()
                viewModel.cargarClientes(reset = true)
            }
            is ClienteActionState.Error -> {
                val msg = (actionState as ClienteActionState.Error).mensaje
                snackbarHostState.showSnackbar(msg)
                viewModel.resetActionState()
            }
            else -> Unit
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost   = { SnackbarHost(snackbarHostState) },
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text       = "Directorio de Clientes",
                                style      = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            if (listState is ClientesUiState.Results) {
                                val total = (listState as ClientesUiState.Results).clientes.size
                                Surface(
                                    shape    = RoundedCornerShape(50),
                                    color    = MaterialTheme.colorScheme.primaryContainer,
                                    modifier = Modifier.padding(top = 2.dp)
                                ) {
                                    Text(
                                        text     = "$total ${if (total == 1) "cliente" else "clientes"}",
                                        style    = MaterialTheme.typography.labelSmall,
                                        color    = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 1.dp)
                                    )
                                }
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )

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
            if (listState !is ClientesUiState.Forbidden) {
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
        }
    ) { padding ->

        if (listState is ClientesUiState.Forbidden) {
            SinPermisoContent(
                modifier = Modifier.padding(padding),
                onVolver = onBack
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                Spacer(Modifier.height(8.dp))

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

                            if (clientes.isEmpty()) {
                                ClienteEmptyState(
                                    icon = Icons.Outlined.PersonOff,
                                    titulo = if (query.isNotBlank()) "Sin resultados" else "Sin clientes registrados",
                                    subtitulo = if (query.isNotBlank())
                                        "Ningún cliente coincide con \"$query\"."
                                    else
                                        "Aún no hay clientes creados. Usa el botón inferior."
                                )
                            } else {
                                LazyColumn(
                                    contentPadding = PaddingValues(
                                        start = 16.dp, top = 8.dp, end = 16.dp, bottom = 88.dp
                                    ),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    items(
                                        items = clientes,
                                        key = { it.id }
                                    ) { cliente ->
                                        ClienteCard(
                                            cliente = cliente,
                                            onItemClick = {
                                                esNuevoCliente = false
                                                clienteSeleccionadoId = cliente.id
                                                showClienteDialog = true
                                            }
                                        )
                                    }

                                    if (resultsState.cargandoMas) {
                                        item {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(16.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                CircularProgressIndicator(
                                                    modifier = Modifier.size(24.dp),
                                                    strokeWidth = 2.dp
                                                )
                                            }
                                        }
                                    } else if (!resultsState.finDeResultados && clientes.size >= 20) {
                                        item {
                                            LaunchedEffect(Unit) {
                                                viewModel.cargarMasClientes()
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        else -> Unit
                    }
                }
            }
        }
    }

    if (showClienteDialog) {
        ClienteDetalleDialog(
            idCliente = clienteSeleccionadoId,
            esNuevo   = esNuevoCliente,
            viewModel = viewModel,
            onDismiss = {
                showClienteDialog = false
            }
        )
    }
}

@Composable
private fun ClienteCard(
    cliente: Cliente,
    onItemClick: () -> Unit
) {
    val iniciales = (cliente.nombre.firstOrNull()?.uppercase() ?: "") + (cliente.apellido.firstOrNull()?.uppercase() ?: "")

    OutlinedCard(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onItemClick() }
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(46.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = iniciales.ifBlank { "C" },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = cliente.nombreCompleto,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (!cliente.telefono.isNullOrBlank()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Phone,
                            contentDescription = null,
                            modifier = Modifier.size(13.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = cliente.telefono,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (!cliente.email.isNullOrBlank()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Email,
                            contentDescription = null,
                            modifier = Modifier.size(13.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = cliente.email,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Icon(
                imageVector = Icons.Outlined.ChevronRight,
                contentDescription = "Ver detalle",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

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
                color = MaterialTheme.colorScheme.onSurface
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
                    Text(accionLabel)
                }
            }
        }
    }
}
