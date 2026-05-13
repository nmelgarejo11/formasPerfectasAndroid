package com.spa.appointments.ui.clientes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
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
    val actionState by viewModel.actionState.collectAsState()

    var nombre by remember { mutableStateOf("") }
    var apellido by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var initialized by remember { mutableStateOf(false) }

    // Errores de validación
    var nombreError by remember { mutableStateOf<String?>(null) }
    var apellidoError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }

    var showConfirmDesactivar by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(idCliente) {
        if (!esNuevo) viewModel.cargarCliente(idCliente)
    }

    if (detalleState is ClienteDetalleState.Success && !initialized) {
        val c = (detalleState as ClienteDetalleState.Success).cliente
        nombre = c.nombre
        apellido = c.apellido
        telefono = c.telefono ?: ""
        email = c.email ?: ""
        initialized = true
    }

    LaunchedEffect(actionState) {
        when (actionState) {
            is ClienteActionState.Success -> {
                snackbarHostState.showSnackbar(if (esNuevo) "Cliente creado" else "Cambios guardados")
                viewModel.resetActionState()
                onBack()
            }
            is ClienteActionState.Error -> {
                snackbarHostState.showSnackbar((actionState as ClienteActionState.Error).mensaje)
                viewModel.resetActionState()
            }
            else -> Unit
        }
    }

    fun validar(): Boolean {
        nombreError = if (nombre.isBlank()) "Requerido" else null
        apellidoError = if (apellido.isBlank()) "Requerido" else null
        emailError = when {
            email.isNotBlank() && !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() ->
                "Email inválido"
            else -> null
        }
        return nombreError == null && apellidoError == null && emailError == null
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (esNuevo) "Nuevo cliente" else "Editar cliente",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (esNuevo) "Registro manual" else "Información de perfil",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.surface)
        ) {
            if (!esNuevo && detalleState is ClienteDetalleState.Loading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    // --- CABECERA (Solo si no es nuevo) ---
                    if (!esNuevo) {
                        ClienteHeaderView(nombre, apellido)
                        Spacer(Modifier.height(24.dp))
                    }

                    // --- SECCIÓN: DATOS PERSONALES ---
                    SectionLabel(Icons.Outlined.Badge, "Datos Personales")
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            CustomField(
                                value = nombre,
                                onValueChange = { nombre = it; nombreError = null },
                                label = "Nombre *",
                                icon = Icons.Outlined.Person,
                                error = nombreError
                            )
                            Spacer(Modifier.height(12.dp))
                            CustomField(
                                value = apellido,
                                onValueChange = { apellido = it; apellidoError = null },
                                label = "Apellido *",
                                icon = Icons.Outlined.Person,
                                error = apellidoError
                            )
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                    // --- SECCIÓN: CONTACTO ---
                    SectionLabel(Icons.Outlined.ContactMail, "Contacto")
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            CustomField(
                                value = telefono,
                                onValueChange = { if (it.length <= 15) telefono = it },
                                label = "Teléfono",
                                icon = Icons.Outlined.Phone,
                                keyboardType = KeyboardType.Phone
                            )
                            Spacer(Modifier.height(12.dp))
                            CustomField(
                                value = email,
                                onValueChange = { email = it; emailError = null },
                                label = "Correo electrónico",
                                icon = Icons.Outlined.Email,
                                error = emailError,
                                keyboardType = KeyboardType.Email
                            )
                        }
                    }

                    Spacer(Modifier.height(32.dp))

                    // --- BOTÓN GUARDAR ---
                    Button(
                        onClick = {
                            if (validar()) {
                                if (esNuevo) {
                                    viewModel.crearCliente(CrearClienteRequest(nombre.trim(), apellido.trim(), telefono.ifBlank { null }, email.ifBlank { null }))
                                } else {
                                    viewModel.actualizarCliente(idCliente, ActualizarClienteRequest(nombre.trim(), apellido.trim(), telefono.ifBlank { null }, email.ifBlank { null }))
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        enabled = actionState !is ClienteActionState.Loading
                    ) {
                        if (actionState is ClienteActionState.Loading) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                        } else {
                            Icon(Icons.Outlined.Save, null)
                            Spacer(Modifier.width(8.dp))
                            Text(if (esNuevo) "Registrar Cliente" else "Guardar Cambios", fontWeight = FontWeight.Bold)
                        }
                    }

                    if (!esNuevo) {
                        Spacer(Modifier.height(16.dp))
                        OutlinedButton(
                            onClick = { showConfirmDesactivar = true },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error)
                        ) {
                            Icon(Icons.Outlined.PersonOff, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Desactivar Cuenta", fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(Modifier.height(40.dp))
                }
            }
        }
    }

    // --- DIÁLOGOS ---
    if (showConfirmDesactivar) {
        AlertDialog(
            onDismissRequest = { showConfirmDesactivar = false },
            icon = { Icon(Icons.Outlined.Warning, null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("¿Desactivar cliente?") },
            text = { Text("El cliente ya no aparecerá en las listas de reserva activas.") },
            confirmButton = {
                Button(
                    onClick = { showConfirmDesactivar = false; viewModel.desactivarCliente(idCliente) },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Desactivar") }
            },
            dismissButton = { TextButton(onClick = { showConfirmDesactivar = false }) { Text("Cancelar") } }
        )
    }
}

@Composable
private fun ClienteHeaderView(nombre: String, apellido: String) {
    val iniciales = "${nombre.take(1)}${apellido.take(1)}".uppercase()
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(64.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(iniciales, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
            }
        }
        Spacer(Modifier.width(16.dp))
        Column {
            Text("$nombre $apellido", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text("Cliente Registrado", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun SectionLabel(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)) {
        Icon(icon, null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.width(8.dp))
        Text(text = text, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
private fun CustomField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    error: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = { Icon(icon, null, modifier = Modifier.size(20.dp)) },
        isError = error != null,
        supportingText = error?.let { { Text(it) } },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Words,
            keyboardType = keyboardType
        ),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        )
    )
}