package com.spa.appointments.ui.clientes

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
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
            creando   = uiState is SeleccionarClienteUiState.Creando,
            onDismiss = { mostrarFormNuevo = false },
            onCreate  = { nombre, apellido, telefono, email ->
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
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = { mostrarFormNuevo = true }) {
                        Icon(Icons.Outlined.PersonAdd, "Nuevo cliente")
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
            // ── Buscador Estilizado (Igual a Profesionales) ──────────────
            OutlinedTextField(
                value         = textoBusqueda,
                onValueChange = { vm.buscar(it) },
                label         = { Text("Buscar cliente") },
                leadingIcon   = { Icon(Icons.Default.Search, null) },
                trailingIcon  = {
                    if (textoBusqueda.isNotEmpty()) {
                        IconButton(onClick = { vm.resetear() }) {
                            Icon(Icons.Outlined.Close, null)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape      = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Box(modifier = Modifier.fillMaxSize()) {
                when (val state = uiState) {
                    is SeleccionarClienteUiState.Idle -> {
                        EstadoHint(
                            icon      = Icons.Outlined.PersonSearch,
                            titulo    = "Busca un cliente",
                            subtitulo = "Ingresa nombre o teléfono para comenzar",
                            accionLabel = "Crear nuevo",
                            onAccion  = { mostrarFormNuevo = true }
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
                            accionLabel = "Crear nuevo",
                            onAccion  = { mostrarFormNuevo = true }
                        )
                    }

                    is SeleccionarClienteUiState.Resultados -> {
                        LazyColumn(
                            contentPadding      = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(state.clientes, key = { it.id }) { cliente ->
                                ClienteSelectItem(
                                    cliente = cliente,
                                    onClick = { onClienteSeleccionado(cliente) }
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

@Composable
private fun ClienteSelectItem(cliente: Cliente, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border    = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier
                .clickable(onClick = onClick)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar con inicial
            Surface(
                modifier = Modifier.size(48.dp),
                shape    = CircleShape,
                color    = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text  = cliente.nombre.firstOrNull()?.uppercase() ?: "?",
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
                if (!cliente.telefono.isNullOrBlank()) {
                    Text(
                        text  = cliente.telefono,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                null,
                tint     = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun EstadoHint(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    titulo: String,
    subtitulo: String,
    esError: Boolean = false,
    accionLabel: String? = null,
    onAccion: (() -> Unit)? = null
) {
    Column(
        modifier            = Modifier.fillMaxSize().padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = if (esError) MaterialTheme.colorScheme.errorContainer
            else MaterialTheme.colorScheme.surfaceVariant
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.padding(20.dp).size(48.dp),
                tint = if (esError) MaterialTheme.colorScheme.error
                else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(Modifier.height(16.dp))
        Text(
            text       = titulo,
            style      = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            textAlign  = TextAlign.Center
        )
        Text(
            text      = subtitulo,
            style     = MaterialTheme.typography.bodyMedium,
            color     = MaterialTheme.colorScheme.onSurfaceVariant,
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
        title = { Text("Nuevo cliente", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it; nombreError = false },
                    label = { Text("Nombre *") },
                    isError = nombreError,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = apellido,
                    onValueChange = { apellido = it; apellidoError = false },
                    label = { Text("Apellido *") },
                    isError = apellidoError,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = telefono,
                    onValueChange = { telefono = it },
                    label = { Text("Teléfono") },
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (nombre.isBlank()) nombreError = true
                    if (apellido.isBlank()) apellidoError = true
                    if (!nombreError && !apellidoError) {
                        onCreate(nombre, apellido, telefono, email)
                    }
                },
                enabled = !creando
            ) {
                if (creando) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                else Text("Crear")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}