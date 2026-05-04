package com.spa.appointments.ui.clientes

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
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
    val uiState       by vm.uiState.collectAsState()
    val textoBusqueda by vm.textoBusqueda.collectAsState()

    var mostrarFormNuevo by remember { mutableStateOf(false) }

    // ── Diálogo nuevo cliente ─────────────────────────────────────────────────
    if (mostrarFormNuevo) {
        NuevoClienteDialog(
            creando = uiState is SeleccionarClienteUiState.Creando,
            onDismiss = { mostrarFormNuevo = false },
            onCreate = { nombre, apellido, telefono, email ->
                vm.crearCliente(nombre, apellido, telefono, email) { cliente ->
                    mostrarFormNuevo = false
                    onClienteSeleccionado(cliente)
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Seleccionar cliente", fontWeight = FontWeight.Bold)
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = { mostrarFormNuevo = true }) {
                        Icon(Icons.Outlined.PersonAdd, contentDescription = "Nuevo cliente")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // ── Buscador ──────────────────────────────────────────────────
            OutlinedTextField(
                value         = textoBusqueda,
                onValueChange = { vm.buscar(it) },
                placeholder   = { Text("Buscar cliente") },
                leadingIcon   = { Icon(Icons.Outlined.Search, contentDescription = null) },
                trailingIcon  = {
                    if (textoBusqueda.isNotEmpty()) {
                        IconButton(onClick = { vm.resetear() }) {
                            Icon(Icons.Outlined.Close, contentDescription = "Limpiar")
                        }
                    }
                },
                modifier   = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                singleLine = true,
                shape      = MaterialTheme.shapes.medium
            )

            // ── Estados ───────────────────────────────────────────────────
            Box(modifier = Modifier.fillMaxSize()) {
                when (val state = uiState) {

                    is SeleccionarClienteUiState.Idle -> {
                        EstadoHint(
                            icon     = Icons.Outlined.PersonSearch,
                            titulo   = "Busca un cliente",
                            subtitulo = "Ingresa nombre o teléfono para comenzar",
                            accionLabel = "Crear nuevo cliente",
                            onAccion = { mostrarFormNuevo = true }
                        )
                    }

                    is SeleccionarClienteUiState.Buscando -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }

                    is SeleccionarClienteUiState.SinResultados -> {
                        EstadoHint(
                            icon      = Icons.Outlined.SearchOff,
                            titulo    = "Sin resultados",
                            subtitulo = "No hay clientes con \"$textoBusqueda\"",
                            accionLabel = "Crear nuevo cliente",
                            onAccion  = { mostrarFormNuevo = true }
                        )
                    }

                    is SeleccionarClienteUiState.Resultados -> {
                        LazyColumn(
                            contentPadding = PaddingValues(
                                horizontal = 16.dp,
                                vertical   = 8.dp
                            )
                        ) {
                            items(state.clientes, key = { it.id }) { cliente ->
                                ClienteSelectItem(
                                    cliente = cliente,
                                    onClick = { onClienteSeleccionado(cliente) }
                                )
                                HorizontalDivider(
                                    modifier = Modifier.padding(horizontal = 56.dp)
                                )
                            }
                        }
                    }

                    is SeleccionarClienteUiState.Error -> {
                        EstadoHint(
                            icon      = Icons.Outlined.ErrorOutline,
                            titulo    = "Ocurrió un error",
                            subtitulo = state.mensaje,
                            esError   = true,
                            accionLabel = "Reintentar",
                            onAccion  = { vm.buscar(textoBusqueda) }
                        )
                    }

                    else -> Unit
                }
            }
        }
    }
}

// ── Diálogo nuevo cliente (extraído para tener su propio scroll) ──────────────
@Composable
private fun NuevoClienteDialog(
    creando: Boolean,
    onDismiss: () -> Unit,
    onCreate: (nombre: String, apellido: String, telefono: String, email: String) -> Unit
) {
    var nombre   by remember { mutableStateOf("") }
    var apellido by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var email    by remember { mutableStateOf("") }

    var nombreError   by remember { mutableStateOf(false) }
    var apellidoError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon  = {
            Icon(Icons.Outlined.PersonAdd, contentDescription = null,
                tint = MaterialTheme.colorScheme.primary)
        },
        title = {
            Text("Nuevo cliente", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value         = nombre,
                    onValueChange = { nombre = it; nombreError = false },
                    label         = { Text("Nombre *") },
                    leadingIcon   = { Icon(Icons.Outlined.Person, null) },
                    isError       = nombreError,
                    supportingText = if (nombreError) ({ Text("Requerido") }) else null,
                    modifier      = Modifier.fillMaxWidth(),
                    singleLine    = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words
                    )
                )
                OutlinedTextField(
                    value         = apellido,
                    onValueChange = { apellido = it; apellidoError = false },
                    label         = { Text("Apellido *") },
                    leadingIcon   = { Icon(Icons.Outlined.Person, null) },
                    isError       = apellidoError,
                    supportingText = if (apellidoError) ({ Text("Requerido") }) else null,
                    modifier      = Modifier.fillMaxWidth(),
                    singleLine    = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words
                    )
                )
                OutlinedTextField(
                    value         = telefono,
                    onValueChange = { if (it.length <= 15) telefono = it },
                    label         = { Text("Teléfono") },
                    leadingIcon   = { Icon(Icons.Outlined.Phone, null) },
                    modifier      = Modifier.fillMaxWidth(),
                    singleLine    = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                )
                OutlinedTextField(
                    value         = email,
                    onValueChange = { email = it },
                    label         = { Text("Email") },
                    leadingIcon   = { Icon(Icons.Outlined.Email, null) },
                    modifier      = Modifier.fillMaxWidth(),
                    singleLine    = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                )
                Text(
                    "* Campos requeridos",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    nombreError   = nombre.isBlank()
                    apellidoError = apellido.isBlank()
                    if (!nombreError && !apellidoError) {
                        onCreate(
                            nombre.trim(),
                            apellido.trim(),
                            telefono.trim(),
                            email.trim()
                        )
                    }
                },
                enabled = !creando
            ) {
                if (creando) {
                    CircularProgressIndicator(
                        modifier    = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color       = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Crear cliente")
                }
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

// ── Item de cliente en lista de selección ─────────────────────────────────────
@Composable
private fun ClienteSelectItem(cliente: Cliente, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar con inicial
        Surface(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape),
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text  = cliente.nombre.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text       = cliente.nombreCompleto,
                style      = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )
            cliente.telefono?.let {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Outlined.Phone,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint     = MaterialTheme.colorScheme.onSurfaceVariant
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
                        tint     = MaterialTheme.colorScheme.onSurfaceVariant
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
            Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// ── Estado vacío / error reutilizable ────────────────────────────────────────
@Composable
private fun EstadoHint(
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
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(32.dp)
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
                style      = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color      = if (esError) MaterialTheme.colorScheme.error
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
                    Icon(Icons.Outlined.PersonAdd, null, Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(accionLabel)
                }
            }
        }
    }
}