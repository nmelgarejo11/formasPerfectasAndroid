package com.spa.appointments.ui.clientes

import android.Manifest
import android.app.Activity
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import android.speech.RecognizerIntent
import android.util.Log
import com.spa.appointments.domain.model.ActualizarClienteRequest
import com.spa.appointments.domain.model.CrearClienteRequest

@Composable
fun ClienteDetalleDialog(
    idCliente : Int,
    esNuevo   : Boolean = false,
    onDismiss : () -> Unit,
    viewModel : ClientesViewModel = hiltViewModel()
) {
    val context = LocalContext.current
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
    var telefonoError by remember { mutableStateOf<String?>(null) }

    var showConfirmDesactivar by remember { mutableStateOf(false) }
    val guardando = actionState is ClienteActionState.Loading

    // ── Enfoque por Intent (Seguro y compatible con variantes es-CO, es-US) ──
    // ── Enfoque por Intent (Seguro y compatible con variantes es-CO, es-US) ──
    val voiceLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        viewModel.estaEscuchandoVoz = false // Apagamos el switch visual al volver

        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            val matches = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val textoDetectado = matches?.get(0) ?: ""
            Log.d("IA_VOZ_DEBUG", "Texto capturado por el Intent: '$textoDetectado'")

            if (textoDetectado.isNotBlank()) {
                viewModel.procesarRafagaVoz(textoDetectado) { rNombre, rApellido, rTelefono, rEmail ->
                    Log.d("IA_VOZ_DEBUG", "Inyectando mapeo de la IA en los campos.")

                    if (!rNombre.isNullOrBlank()) {

                        nombre = rNombre.trim().split(" ").joinToString(" ") { palabra ->
                            palabra.lowercase().replaceFirstChar { it.uppercase() }
                        }
                        nombreError = null
                    }

                    if (!rApellido.isNullOrBlank()) {

                        apellido = rApellido.trim().split(" ").joinToString(" ") { palabra ->
                            palabra.lowercase().replaceFirstChar { it.uppercase() }
                        }
                        apellidoError = null
                    }

                    if (!rTelefono.isNullOrBlank()) telefono = rTelefono

                    if (!rEmail.isNullOrBlank()) {
                        // Sanitización estricta de Email para la respuesta de la IA
                        email = rEmail
                            .trim()
                            .lowercase()                 // Pasa todo a minúsculas (Nicolás -> nicolás)
                            .replace(" ", "")            // Elimina los espacios intermedios
                            .replace("á", "a")           // Remueve tildes comunes
                            .replace("é", "e")
                            .replace("í", "i")
                            .replace("ó", "o")
                            .replace("ú", "o")

                        emailError = null
                    }
                }
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { permitido ->
        if (permitido) {
            viewModel.estaEscuchandoVoz = true
            try {
                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, "es-CO")
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "es-CO")
                    putExtra(RecognizerIntent.EXTRA_SUPPORTED_LANGUAGES, arrayListOf("es-CO", "es-US", "es-419"))
                    putExtra(RecognizerIntent.EXTRA_PROMPT, "Hable de corrido para registrar los datos...")
                }
                voiceLauncher.launch(intent)
            } catch (e: Exception) {
                Log.e("IA_VOZ_DEBUG", "El dispositivo no soporta reconocimiento de voz nativo por intent.", e)
                viewModel.estaEscuchandoVoz = false
            }
        } else {
            viewModel.estaEscuchandoVoz = false
        }
    }

    val micContainerColor by animateColorAsState(
        targetValue = if (viewModel.estaEscuchandoVoz) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer,
        label = "MicColor"
    )
    // ─────────────────────────────────────────────────────────────────────────

    LaunchedEffect(idCliente, esNuevo) {
        initialized = false
        nombre = ""; apellido = ""; telefono = ""; email = ""
        nombreError = null; apellidoError = null; emailError = null; telefonoError = null

        viewModel.estaEscuchandoVoz = false
        viewModel.resetDetalleState()
        viewModel.resetActionState()

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
                viewModel.estaEscuchandoVoz = false
                viewModel.resetActionState()
                onDismiss()
            }
            else -> Unit
        }
    }

    fun validar(): Boolean {
        nombreError = if (nombre.isBlank()) "Requerido" else null
        apellidoError = if (apellido.isBlank()) "Requerido" else null
        telefonoError = if (telefono.isBlank()) {
            "Requerido"
        } else if (telefono.replace(" ", "").length < 7) {
            "Teléfono inválido" // Validación opcional de longitud mínima
        } else {
            null
        }
        emailError = when {
            email.isBlank() -> "Requerido"
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> "Email inválido"
            else -> null
        }
        return nombreError == null && apellidoError == null && emailError == null && telefonoError == null
    }

    AlertDialog(
        onDismissRequest = {
            if (!guardando) {
                viewModel.estaEscuchandoVoz = false
                onDismiss()
            }
        },
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
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = micContainerColor,
                                modifier = Modifier.size(38.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    if (viewModel.cargandoParserVoz) {
                                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                                    } else {
                                        Icon(
                                            imageVector = if (viewModel.estaEscuchandoVoz) Icons.Outlined.Mic else Icons.Outlined.MicOff,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp),
                                            tint = if (viewModel.estaEscuchandoVoz) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }
                                }
                            }

                            Spacer(Modifier.width(10.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (viewModel.estaEscuchandoVoz) "Escucha activa" else "Asistente de voz",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (viewModel.estaEscuchandoVoz) "Procesando audio..." else "Active para dictar datos",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Switch(
                                checked = viewModel.estaEscuchandoVoz,
                                onCheckedChange = { encendido ->
                                    if (encendido) {
                                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                    } else {
                                        viewModel.estaEscuchandoVoz = false
                                    }
                                },
                                modifier = Modifier.scale(0.75f),
                                enabled = !guardando
                            )
                        }
                    }

                    Text(
                        text  = "Los campos marcados con * son obligatorios.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(Modifier.height(2.dp))

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
                        enabled       = !guardando,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Words,
                            imeAction = ImeAction.Next
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
                        enabled       = !guardando,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Words,
                            imeAction = ImeAction.Next
                        )
                    )

                    Spacer(Modifier.height(4.dp))

                    SectionLabel(icon = Icons.Outlined.ContactMail, texto = "Contacto")
                    Spacer(Modifier.height(2.dp))

                    OutlinedTextField(
                        value         = telefono,
                        onValueChange = {
                            telefono = it
                            telefonoError = null // <-- Limpia el error al escribir
                        },
                        label         = { Text("Teléfono *") }, // <-- Añadido el asterisco (*)
                        leadingIcon   = { Icon(Icons.Outlined.Phone, null) },
                        isError       = telefonoError != null, // <-- Vinculado al estado de error
                        supportingText = telefonoError?.let { { Text(it) } }, // <-- Muestra el mensaje "Requerido"
                        modifier      = Modifier.fillMaxWidth(),
                        singleLine    = true,
                        enabled       = !guardando,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Next)
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
                        enabled       = !guardando,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Done)
                    )

                    if (!esNuevo) {
                        Spacer(Modifier.height(8.dp))
                        OutlinedButton(
                            onClick  = { showConfirmDesactivar = true },
                            modifier = Modifier.fillMaxWidth(),
                            enabled  = !guardando,
                            colors   = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            ),
                            border = ButtonDefaults.outlinedButtonColors().run {
                                BorderStroke(0.5.dp, MaterialTheme.colorScheme.error)
                            }
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
                        viewModel.estaEscuchandoVoz = false

                        // Sanitizamos el teléfono eliminando todos los espacios en blanco
                        val telefonoSanitizado = telefono.replace(" ", "").trim()

                        if (esNuevo) {
                            viewModel.crearCliente(
                                CrearClienteRequest(
                                    nombre.trim(),
                                    apellido.trim(),
                                    telefonoSanitizado.trim(),
                                    email.trim()
                                )
                            )
                        } else {
                            viewModel.actualizarCliente(
                                idCliente,
                                ActualizarClienteRequest(
                                    nombre.trim(),
                                    apellido.trim(),
                                    telefonoSanitizado.trim(),
                                    email.trim()
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
                onClick = {
                    viewModel.estaEscuchandoVoz = false
                    onDismiss()
                },
                enabled = !guardando
            ) {
                Text("Cancelar")
            }
        }
    )

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
                        viewModel.estaEscuchandoVoz = false
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