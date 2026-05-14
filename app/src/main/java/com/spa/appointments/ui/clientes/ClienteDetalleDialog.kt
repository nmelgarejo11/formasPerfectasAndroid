package com.spa.appointments.ui.clientes

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.spa.appointments.domain.model.ActualizarClienteRequest
import com.spa.appointments.domain.model.CrearClienteRequest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClienteDetalleDialog(
    idCliente : Int,
    esNuevo   : Boolean = false,
    onDismiss : () -> Unit,
    viewModel : ClientesViewModel = hiltViewModel()
) {
    val detalleState by viewModel.detalleState.collectAsState()
    val actionState  by viewModel.actionState.collectAsState()

    var nombre    by remember { mutableStateOf("") }
    var apellido  by remember { mutableStateOf("") }
    var telefono  by remember { mutableStateOf("") }
    var email     by remember { mutableStateOf("") }
    var initialized by remember { mutableStateOf(false) }

    var nombreError   by remember { mutableStateOf<String?>(null) }
    var apellidoError by remember { mutableStateOf<String?>(null) }
    var emailError    by remember { mutableStateOf<String?>(null) }

    var showConfirmDesactivar by remember { mutableStateOf(false) }
    val snackbarHostState     = remember { SnackbarHostState() }

    val guardando = actionState is ClienteActionState.Loading

    LaunchedEffect(idCliente, esNuevo) {

        // limpiar estado local SIEMPRE
        initialized = false

        nombre = ""
        apellido = ""
        telefono = ""
        email = ""

        nombreError = null
        apellidoError = null
        emailError = null

        // limpiar estado ViewModel
        viewModel.resetDetalleState()
        viewModel.resetActionState()

        // cargar cliente si es edición
        if (!esNuevo && idCliente > 0) {
            viewModel.cargarCliente(idCliente)
        }
    }

    if (detalleState is ClienteDetalleState.Success && !initialized) {
        val c = (detalleState as ClienteDetalleState.Success).cliente
        nombre   = c.nombre
        apellido = c.apellido
        telefono = c.telefono ?: ""
        email    = c.email    ?: ""
        initialized = true
    }

    LaunchedEffect(actionState) {
        when (actionState) {
            is ClienteActionState.Success -> {
                viewModel.resetActionState()
                onDismiss()
            }
            else -> Unit
        }
    }

    fun validar(): Boolean {

        nombreError =
            if (nombre.isBlank())
                "Requerido"
            else
                null

        apellidoError =
            if (apellido.isBlank())
                "Requerido"
            else
                null

        emailError = when {
            email.isBlank() ->
                "Requerido"

            !android.util.Patterns
                .EMAIL_ADDRESS
                .matcher(email)
                .matches() ->
                "Email inválido"

            else -> null
        }

        return nombreError == null &&
                apellidoError == null &&
                emailError == null
    }

    // ── Dialog principal ──────────────────────────────────────────────────────
    AlertDialog(
        onDismissRequest = { if (!guardando) onDismiss() },
        title = {
            Row(
                verticalAlignment    = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector        = if (esNuevo) Icons.Outlined.PersonAdd else Icons.Outlined.Edit,
                    contentDescription = null,
                    tint               = MaterialTheme.colorScheme.primary
                )
                Text(if (esNuevo) "Nuevo cliente" else "Editar cliente")
            }
        },
        text = {
            // Cargando datos iniciales (solo modo edición)
            if (!esNuevo && detalleState is ClienteDetalleState.Loading) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(strokeWidth = 2.5.dp)
                }
            } else {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Subtítulo
                    Text(
                        text  = "Los campos marcados con * son obligatorios.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(Modifier.height(6.dp))

                    // ── Datos personales ──────────────────────────────────
                    SectionLabel(icon = Icons.Outlined.Badge, texto = "Datos personales")
                    Spacer(Modifier.height(2.dp))

                    OutlinedTextField(
                        value         = nombre,
                        onValueChange = { nombre = it; nombreError = null },
                        label         = { Text("Nombre *") },
                        leadingIcon   = { Icon(Icons.Outlined.Person, null) },
                        isError       = nombreError != null,
                        supportingText = nombreError?.let { { Text(it) } },
                        modifier      = Modifier.fillMaxWidth(),
                        singleLine    = true,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Words
                        )
                    )

                    OutlinedTextField(
                        value         = apellido,
                        onValueChange = { apellido = it; apellidoError = null },
                        label         = { Text("Apellido *") },
                        leadingIcon   = { Icon(Icons.Outlined.Person, null) },
                        isError       = apellidoError != null,
                        supportingText = apellidoError?.let { { Text(it) } },
                        modifier      = Modifier.fillMaxWidth(),
                        singleLine    = true,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Words
                        )
                    )

                    Spacer(Modifier.height(4.dp))

                    // ── Contacto ──────────────────────────────────────────
                    SectionLabel(icon = Icons.Outlined.ContactMail, texto = "Contacto")
                    Spacer(Modifier.height(2.dp))

                    OutlinedTextField(
                        value         = telefono,
                        onValueChange = { telefono = it },
                        label         = { Text("Teléfono") },
                        leadingIcon   = { Icon(Icons.Outlined.Phone, null) },
                        modifier      = Modifier.fillMaxWidth(),
                        singleLine    = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                    )

                    OutlinedTextField(
                        value         = email,
                        onValueChange = { email = it; emailError = null },
                        label         = { Text("Correo electrónico *") },
                        leadingIcon   = { Icon(Icons.Outlined.Email, null) },
                        isError       = emailError != null,
                        supportingText = emailError?.let { { Text(it) } },
                        modifier      = Modifier.fillMaxWidth(),
                        singleLine    = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                    )

                    // Botón desactivar (solo edición)
                    if (!esNuevo) {
                        Spacer(Modifier.height(8.dp))
                        OutlinedButton(
                            onClick  = { showConfirmDesactivar = true },
                            modifier = Modifier.fillMaxWidth(),
                            enabled  = !guardando,
                            colors   = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                0.5.dp, MaterialTheme.colorScheme.error
                            )
                        ) {
                            Icon(Icons.Outlined.PersonOff, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Desactivar cuenta", fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (validar()) {
                        if (esNuevo) {
                            viewModel.crearCliente(
                                CrearClienteRequest(
                                    nombre.trim(), apellido.trim(),
                                    telefono.ifBlank { null }, email.ifBlank { null }
                                )
                            )
                        } else {
                            viewModel.actualizarCliente(
                                idCliente, ActualizarClienteRequest(
                                    nombre.trim(), apellido.trim(),
                                    telefono.ifBlank { null }, email.ifBlank { null }
                                )
                            )
                        }
                    }
                },
                enabled = !guardando
            ) {
                if (guardando) {
                    CircularProgressIndicator(
                        modifier    = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color       = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(Modifier.width(6.dp))
                    Text("Guardando...")
                } else {
                    Icon(Icons.Outlined.Check, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(if (esNuevo) "Registrar" else "Guardar")
                }
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                enabled = !guardando
            ) {
                Text("Cancelar")
            }
        }
    )

    // ── Confirmar desactivar ──────────────────────────────────────────────────
    if (showConfirmDesactivar) {
        AlertDialog(
            onDismissRequest = { showConfirmDesactivar = false },
            icon  = { Icon(Icons.Outlined.Warning, null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("¿Desactivar cliente?") },
            text  = { Text("El cliente ya no aparecerá en las listas de reserva activas.") },
            confirmButton = {
                Button(
                    onClick = {
                        showConfirmDesactivar = false
                        viewModel.desactivarCliente(idCliente)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) { Text("Desactivar") }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDesactivar = false }) { Text("Cancelar") }
            }
        )
    }
}

// ─── Section label (privado) ──────────────────────────────────────────────────
@Composable
private fun SectionLabel(icon: androidx.compose.ui.graphics.vector.ImageVector, texto: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier          = Modifier.padding(start = 2.dp)
    ) {
        Icon(
            imageVector        = icon,
            contentDescription = null,
            modifier           = Modifier.size(14.dp),
            tint               = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.width(5.dp))
        Text(
            text       = texto,
            style      = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium,
            color      = MaterialTheme.colorScheme.primary
        )
    }
}