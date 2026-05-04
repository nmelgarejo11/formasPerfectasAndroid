package com.spa.appointments.ui.clientes

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

    var nombre      by remember { mutableStateOf("") }
    var apellido    by remember { mutableStateOf("") }
    var telefono    by remember { mutableStateOf("") }
    var email       by remember { mutableStateOf("") }
    var initialized by remember { mutableStateOf(false) }

    // Errores de validación
    var nombreError   by remember { mutableStateOf<String?>(null) }
    var apellidoError by remember { mutableStateOf<String?>(null) }
    var emailError    by remember { mutableStateOf<String?>(null) }

    var showConfirmDesactivar by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(idCliente) {
        if (!esNuevo) viewModel.cargarCliente(idCliente)
    }

    if (detalleState is ClienteDetalleState.Success && !initialized) {
        val c = (detalleState as ClienteDetalleState.Success).cliente
        nombre   = c.nombre
        apellido = c.apellido
        telefono = c.telefono ?: ""
        email    = c.email ?: ""
        initialized = true
    }

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

    // ── Validación local ──────────────────────────────────────────────────────
    fun validar(): Boolean {
        nombreError   = if (nombre.isBlank()) "El nombre es requerido" else null
        apellidoError = if (apellido.isBlank()) "El apellido es requerido" else null
        emailError    = when {
            email.isNotBlank() && !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() ->
                "Formato de email inválido"
            else -> null
        }
        return nombreError == null && apellidoError == null && emailError == null
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
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->

        when {
            !esNuevo && detalleState is ClienteDetalleState.Loading -> {
                Box(
                    Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator() }
            }

            !esNuevo && detalleState is ClienteDetalleState.Error -> {
                Box(
                    Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Outlined.ErrorOutline,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            (detalleState as ClienteDetalleState.Error).mensaje,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
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

                    // ── Sección: Datos personales ─────────────────────────
                    SectionLabel("Datos personales")
                    Spacer(Modifier.height(12.dp))

                    OutlinedTextField(
                        value = nombre,
                        onValueChange = {
                            nombre = it
                            if (nombreError != null) nombreError = null
                        },
                        label = { Text("Nombre *") },
                        leadingIcon = {
                            Icon(Icons.Outlined.Person, contentDescription = null)
                        },
                        isError = nombreError != null,
                        supportingText = nombreError?.let { { Text(it) } },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Words
                        )
                    )
                    Spacer(Modifier.height(12.dp))

                    OutlinedTextField(
                        value = apellido,
                        onValueChange = {
                            apellido = it
                            if (apellidoError != null) apellidoError = null
                        },
                        label = { Text("Apellido *") },
                        leadingIcon = {
                            Icon(Icons.Outlined.Person, contentDescription = null)
                        },
                        isError = apellidoError != null,
                        supportingText = apellidoError?.let { { Text(it) } },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Words
                        )
                    )

                    Spacer(Modifier.height(20.dp))

                    // ── Sección: Contacto ─────────────────────────────────
                    SectionLabel("Contacto")
                    Spacer(Modifier.height(12.dp))

                    OutlinedTextField(
                        value = telefono,
                        onValueChange = { if (it.length <= 15) telefono = it },
                        label = { Text("Teléfono") },
                        leadingIcon = {
                            Icon(Icons.Outlined.Phone, contentDescription = null)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                    )
                    Spacer(Modifier.height(12.dp))

                    OutlinedTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            if (emailError != null) emailError = null
                        },
                        label = { Text("Email") },
                        leadingIcon = {
                            Icon(Icons.Outlined.Email, contentDescription = null)
                        },
                        isError = emailError != null,
                        supportingText = emailError?.let { { Text(it) } },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                    )

                    Spacer(Modifier.height(8.dp))
                    Text(
                        "* Campos requeridos",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(Modifier.height(32.dp))

                    // ── Botón guardar ─────────────────────────────────────
                    Button(
                        onClick = { if (validar()) {
                            if (esNuevo) {
                                viewModel.crearCliente(
                                    CrearClienteRequest(
                                        nombre   = nombre.trim(),
                                        apellido = apellido.trim(),
                                        telefono = telefono.ifBlank { null },
                                        email    = email.ifBlank { null }
                                    )
                                )
                            } else {
                                viewModel.actualizarCliente(
                                    idCliente,
                                    ActualizarClienteRequest(
                                        nombre   = nombre.trim(),
                                        apellido = apellido.trim(),
                                        telefono = telefono.ifBlank { null },
                                        email    = email.ifBlank { null }
                                    )
                                )
                            }
                        }},
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        enabled = actionState !is ClienteActionState.Loading,
                        shape = MaterialTheme.shapes.medium
                    ) {
                        if (actionState is ClienteActionState.Loading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Outlined.Save, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                if (esNuevo) "Crear cliente" else "Guardar cambios",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }

                    // ── Botón desactivar (solo edición) ───────────────────
                    if (!esNuevo) {
                        Spacer(Modifier.height(40.dp))
                        HorizontalDivider()
                        Spacer(Modifier.height(16.dp))

                        OutlinedButton(
                            onClick = { showConfirmDesactivar = true },
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp, MaterialTheme.colorScheme.error
                            ),
                            enabled = actionState !is ClienteActionState.Loading,
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Icon(Icons.Outlined.PersonOff, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Desactivar cliente", fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(Modifier.height(24.dp))
                }
            }
        }
    }

    // ── Diálogo confirmar desactivar ──────────────────────────────────────────
    if (showConfirmDesactivar) {
        AlertDialog(
            onDismissRequest = { showConfirmDesactivar = false },
            icon = {
                Icon(
                    Icons.Outlined.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = { Text("¿Desactivar cliente?", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "El cliente quedará inactivo y no podrá realizar nuevas reservas. " +
                            "Contacta al administrador si necesitas reactivarlo."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showConfirmDesactivar = false
                        viewModel.desactivarCliente(idCliente)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Sí, desactivar")
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

// ── Componente auxiliar ───────────────────────────────────────────────────────
@Composable
private fun SectionLabel(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary
    )
}