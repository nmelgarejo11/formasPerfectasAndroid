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
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Clientes",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium
                        )

                        val subtitulo = when (listState) {
                            is ClientesUiState.Results -> {
                                val total =
                                    (listState as ClientesUiState.Results)
                                        .clientes.size

                                "$total ${
                                    if (total == 1)
                                        "cliente encontrado"
                                    else
                                        "clientes encontrados"
                                }"
                            }

                            is ClientesUiState.Idle ->
                                "Busca por nombre o teléfono"

                            else -> ""
                        }

                        if (subtitulo.isNotEmpty()) {
                            Text(
                                text = subtitulo,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme
                                    .onSurfaceVariant
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
                    containerColor =
                    MaterialTheme.colorScheme.surface
                )
            )
        },

        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    esNuevoCliente = true
                    clienteSeleccionadoId = 0
                    showClienteDialog = true
                },
                icon = {
                    Icon(Icons.Outlined.PersonAdd, null)
                },
                text = {
                    Text("Nuevo cliente")
                }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {

            // ── Buscador ─────────────────────────────
            OutlinedTextField(
                value = query,
                onValueChange = viewModel::onQueryChange,
                placeholder = {
                    Text("Nombre, teléfono o email...")
                },

                leadingIcon = {
                    Icon(
                        Icons.Outlined.Search,
                        null,
                        modifier = Modifier.size(20.dp)
                    )
                },

                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(
                            onClick = {
                                viewModel.onQueryChange("")
                            }
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
                    .padding(
                        horizontal = 16.dp,
                        vertical = 10.dp
                    ),

                shape = RoundedCornerShape(14.dp),
                singleLine = true,

                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor =
                    MaterialTheme.colorScheme
                        .outlineVariant
                )
            )

            // ── Contenido ───────────────────────────
            Box(
                modifier = Modifier.fillMaxSize()
            ) {

                when (listState) {

                    is ClientesUiState.Loading -> {

                        Column(
                            modifier = Modifier.align(
                                Alignment.Center
                            ),
                            horizontalAlignment =
                            Alignment.CenterHorizontally,
                            verticalArrangement =
                            Arrangement.spacedBy(10.dp)
                        ) {

                            CircularProgressIndicator(
                                strokeWidth = 2.5.dp
                            )

                            Text(
                                text = "Buscando clientes...",
                                style =
                                MaterialTheme.typography
                                    .bodySmall,
                                color =
                                MaterialTheme.colorScheme
                                    .onSurfaceVariant
                            )
                        }
                    }

                    is ClientesUiState.Idle -> {

                        ClienteEmptyState(
                            icon =
                            Icons.Outlined.PersonSearch,
                            titulo =
                            "Busca un cliente",
                            subtitulo =
                            "Escribe un nombre, teléfono o correo\npara encontrar resultados."
                        )
                    }

                    is ClientesUiState.Error -> {

                        ClienteEmptyState(
                            icon =
                            Icons.Outlined.ErrorOutline,
                            titulo =
                            "Ocurrió un error",
                            subtitulo =
                            (listState as ClientesUiState.Error)
                                .mensaje,
                            colorIcono =
                            MaterialTheme.colorScheme.error,
                            accionLabel =
                            "Reintentar",
                            onAccion = {
                                viewModel.onQueryChange(
                                    query
                                )
                            }
                        )
                    }

                    is ClientesUiState.Results -> {

                        val clientes =
                            (listState as
                                    ClientesUiState.Results)
                                .clientes

                        if (clientes.isEmpty()) {

                            ClienteEmptyState(
                                icon =
                                Icons.Outlined.SearchOff,
                                titulo =
                                "Sin resultados",
                                subtitulo =
                                "No encontramos clientes que coincidan\ncon \"$query\"."
                            )

                        } else {

                            LazyColumn(
                                contentPadding =
                                PaddingValues(
                                    start = 16.dp,
                                    end = 16.dp,
                                    top = 4.dp,
                                    bottom = 96.dp
                                ),

                                verticalArrangement =
                                Arrangement
                                    .spacedBy(8.dp)
                            ) {

                                items(
                                    clientes,
                                    key = { it.id }
                                ) { cliente ->

                                    ClienteCard(
                                        cliente =
                                        cliente,

                                        onVerDetalle = {
                                            esNuevoCliente =
                                                false

                                            clienteSeleccionadoId =
                                                cliente.id

                                            showClienteDialog =
                                                true
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // ── Dialog Cliente ───────────────────────────
    if (
        showClienteDialog &&
        clienteSeleccionadoId != null
    ) {

        ClienteDetalleDialog(
            idCliente =
            clienteSeleccionadoId!!,

            esNuevo =
            esNuevoCliente,

            onDismiss = {

                showClienteDialog =
                    false

                clienteSeleccionadoId =
                    null

                if (query.length >= 2) {
                    viewModel.onQueryChange(
                        query
                    )
                }
            }
        )
    }
}

// ────────────────────────────────────────────────
// Card Cliente
// ────────────────────────────────────────────────

@Composable
private fun ClienteCard(
    cliente: Cliente,
    onVerDetalle: () -> Unit
) {

    val iniciales = buildString {
        append(cliente.nombre.firstOrNull() ?: "")
        append(cliente.apellido.firstOrNull() ?: "")
    }.uppercase().ifBlank { "?" }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor =
            MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            0.5.dp,
            MaterialTheme.colorScheme.outlineVariant
        )
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    onVerDetalle()
                }
                .padding(12.dp),

            verticalAlignment =
            Alignment.CenterVertically
        ) {

            // ── Avatar ───────────────────────────────
            Surface(
                modifier = Modifier.size(48.dp),
                shape = CircleShape,
                color =
                MaterialTheme.colorScheme
                    .primaryContainer
            ) {

                Box(
                    contentAlignment =
                    Alignment.Center
                ) {
                    Text(
                        text = iniciales,
                        style =
                        MaterialTheme.typography
                            .titleSmall,
                        fontWeight =
                        FontWeight.Medium,
                        color =
                        MaterialTheme.colorScheme
                            .onPrimaryContainer
                    )
                }
            }

            Spacer(Modifier.width(12.dp))

            // ── Información cliente ─────────────────
            Column(
                modifier = Modifier.weight(1f)
            ) {

                Text(
                    text =
                    "${cliente.nombre} ${cliente.apellido}"
                        .trim(),

                    style =
                    MaterialTheme.typography
                        .bodyMedium,

                    fontWeight =
                    FontWeight.Medium,

                    maxLines = 1,

                    overflow =
                    TextOverflow.Ellipsis
                )

                Spacer(
                    Modifier.height(3.dp)
                )

                val contacto = listOfNotNull(
                    cliente.telefono,
                    cliente.email
                )

                if (contacto.isNotEmpty()) {

                    cliente.telefono?.let {
                        Row(
                            verticalAlignment =
                            Alignment.CenterVertically
                        ) {

                            Icon(
                                Icons.Outlined.Phone,
                                null,
                                modifier =
                                Modifier.size(12.dp),
                                tint =
                                MaterialTheme.colorScheme
                                    .onSurfaceVariant
                            )

                            Spacer(
                                Modifier.width(4.dp)
                            )

                            Text(
                                text = it,
                                style =
                                MaterialTheme.typography
                                    .bodySmall,
                                color =
                                MaterialTheme.colorScheme
                                    .onSurfaceVariant
                            )
                        }
                    }

                    cliente.email?.let {

                        Spacer(
                            Modifier.height(2.dp)
                        )

                        Row(
                            verticalAlignment =
                            Alignment.CenterVertically
                        ) {

                            Icon(
                                Icons.Outlined.Email,
                                null,
                                modifier =
                                Modifier.size(12.dp),
                                tint =
                                MaterialTheme.colorScheme
                                    .onSurfaceVariant
                            )

                            Spacer(
                                Modifier.width(4.dp)
                            )

                            Text(
                                text = it,
                                style =
                                MaterialTheme.typography
                                    .bodySmall,
                                color =
                                MaterialTheme.colorScheme
                                    .onSurfaceVariant,
                                maxLines = 1,
                                overflow =
                                TextOverflow.Ellipsis
                            )
                        }
                    }

                } else {

                    Text(
                        text =
                        "Sin información de contacto",

                        style =
                        MaterialTheme.typography
                            .bodySmall,

                        color =
                        MaterialTheme.colorScheme
                            .onSurfaceVariant
                            .copy(alpha = 0.5f)
                    )
                }
            }

            // ── Flecha derecha ─────────────────────
            Icon(
                Icons.Outlined.ChevronRight,
                contentDescription = null,
                modifier =
                Modifier.size(18.dp),
                tint =
                MaterialTheme.colorScheme
                    .onSurfaceVariant
                    .copy(alpha = 0.6f)
            )
        }
    }
}

// ────────────────────────────────────────────────
// Empty State
// ────────────────────────────────────────────────

@Composable
private fun ClienteEmptyState(
    icon: ImageVector,
    titulo: String,
    subtitulo: String,
    colorIcono: Color =
        MaterialTheme.colorScheme
            .onSurfaceVariant,
    accionLabel: String? = null,
    onAccion: (() -> Unit)? = null
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(40.dp),

        horizontalAlignment =
        Alignment.CenterHorizontally,

        verticalArrangement =
        Arrangement.Center
    ) {

        Surface(
            shape =
            RoundedCornerShape(20.dp),

            color =
            MaterialTheme
                .colorScheme
                .surfaceVariant
                .copy(alpha = 0.6f)
        ) {

            Icon(
                imageVector =
                icon,

                contentDescription =
                null,

                modifier =
                Modifier
                    .padding(20.dp)
                    .size(40.dp),

                tint =
                colorIcono
            )
        }

        Spacer(
            Modifier.height(16.dp)
        )

        Text(
            text =
            titulo,

            style =
            MaterialTheme
                .typography
                .titleSmall,

            fontWeight =
            FontWeight.Medium,

            textAlign =
            TextAlign.Center
        )

        Spacer(
            Modifier.height(4.dp)
        )

        Text(
            text =
            subtitulo,

            style =
            MaterialTheme
                .typography
                .bodySmall,

            color =
            MaterialTheme
                .colorScheme
                .onSurfaceVariant,

            textAlign =
            TextAlign.Center
        )

        if (
            accionLabel != null &&
            onAccion != null
        ) {

            Spacer(
                Modifier.height(20.dp)
            )

            OutlinedButton(
                onClick =
                onAccion
            ) {

                Icon(
                    Icons.Outlined.Refresh,
                    null
                )

                Spacer(
                    Modifier.width(6.dp)
                )

                Text(
                    accionLabel
                )
            }
        }
    }
}