// Ruta: app/src/main/java/com/spa/appointments/ui/clientes/ClienteDetalleDialog.kt
package com.spa.appointments.ui.clientes

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.style.TextAlign
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
    val context = LocalContext.current
    val detalleState by viewModel.detalleState.collectAsState()
    val actionState  by viewModel.actionState.collectAsState()

    var nombre      by remember { mutableStateOf("") }
    var apellido    by remember { mutableStateOf("") }
    var telefono    by remember { mutableStateOf("") }
    var email       by remember { mutableStateOf("") }
    var initialized by remember { mutableStateOf(false) }

    var nombreError   by remember { mutableStateOf<String?>(null) }
    var apellidoError by remember { mutableStateOf<String?>(null) }
    var emailError    by remember { mutableStateOf<String?>(null) }
    var telefonoError by remember { mutableStateOf<String?>(null) }

    var showConfirmDesactivar by remember { mutableStateOf(false) }
    val guardando = actionState is ClienteActionState.Loading

    val voiceLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        viewModel.estaEscuchandoVoz = false

        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            val matches = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val textoDetectado = matches?.get(0) ?: ""
            Log.d("IA_VOZ_DEBUG", "Texto capturado por el Intent: '$textoDetectado'")

            if (textoDetectado.isNotBlank()) {
                viewModel.procesarRafagaVoz(textoDetectado) { rNombre, rApellido, rTelefono, rEmail ->
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
                        email = rEmail
                            .trim()
                            .lowercase()
                            .replace(" ", "")
                            .replace("á", "a")
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
            "Teléfono inválido"
        } else {
            null
        }
        emailError = when {
            email.isNotBlank() && !android.util.Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches() -> "Email inválido"
            else -> null
        }
        return nombreError == null && apellidoError == null && emailError == null && telefonoError == null
    }

    BasicAlertDialog(
        onDismissRequest = {
            if (!guardando) {
                viewModel.estaEscuchandoVoz = false
                onDismiss()
            }
        }
    ) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            tonalElevation = 6.dp
        ) {
            Column(modifier = Modifier.padding(20.dp)) {

                // Header M3
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Icon(
                            imageVector = if (esNuevo) Icons.Outlined.PersonAdd else Icons.Outlined.Edit,
                            contentDescription = null,
                            modifier = Modifier.padding(10.dp).size(24.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Column {
                        Text(
                            text = if (esNuevo) "Nuevo cliente" else "Editar cliente",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Campos marcados con * son obligatorios",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                Spacer(modifier = Modifier.height(12.dp))

                if (!esNuevo && detalleState is ClienteDetalleState.Loading) {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(140.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(strokeWidth = 3.dp)
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .weight(1f, fill = false)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Card Asistente de Voz M3
                        OutlinedCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
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
                                    modifier = Modifier.scale(0.8f),
                                    enabled = !guardando
                                )
                            }
                        }

                        SectionLabel(icon = Icons.Outlined.Badge, texto = "DATOS PERSONALES")

                        OutlinedTextField(
                            value         = nombre,
                            onValueChange = { nombre = it; nombreError = null },
                            label         = { Text("Nombre *") },
                            leadingIcon   = { Icon(Icons.Outlined.Person, null, tint = MaterialTheme.colorScheme.primary) },
                            isError       = nombreError != null,
                            supportingText = nombreError?.let { { Text(it) } },
                            modifier      = Modifier.fillMaxWidth(),
                            singleLine    = true,
                            enabled       = !guardando,
                            shape         = RoundedCornerShape(14.dp),
                            colors        = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor   = MaterialTheme.colorScheme.surfaceContainerLow,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow
                            ),
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Words,
                                imeAction = ImeAction.Next
                            )
                        )

                        OutlinedTextField(
                            value         = apellido,
                            onValueChange = { apellido = it; apellidoError = null },
                            label         = { Text("Apellido *") },
                            leadingIcon   = { Icon(Icons.Outlined.Person, null, tint = MaterialTheme.colorScheme.primary) },
                            isError       = apellidoError != null,
                            supportingText = apellidoError?.let { { Text(it) } },
                            modifier      = Modifier.fillMaxWidth(),
                            singleLine    = true,
                            enabled       = !guardando,
                            shape         = RoundedCornerShape(14.dp),
                            colors        = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor   = MaterialTheme.colorScheme.surfaceContainerLow,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow
                            ),
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Words,
                                imeAction = ImeAction.Next
                            )
                        )

                        SectionLabel(icon = Icons.Outlined.ContactMail, texto = "CONTACTO")

                        OutlinedTextField(
                            value         = telefono,
                            onValueChange = {
                                telefono = it
                                telefonoError = null
                            },
                            label         = { Text("Teléfono *") },
                            leadingIcon   = { Icon(Icons.Outlined.Phone, null, tint = MaterialTheme.colorScheme.primary) },
                            isError       = telefonoError != null,
                            supportingText = telefonoError?.let { { Text(it) } },
                            modifier      = Modifier.fillMaxWidth(),
                            singleLine    = true,
                            enabled       = !guardando,
                            shape         = RoundedCornerShape(14.dp),
                            colors        = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor   = MaterialTheme.colorScheme.surfaceContainerLow,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow
                            ),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Next)
                        )

                        OutlinedTextField(
                            value         = email,
                            onValueChange = { email = it; emailError = null },
                            label         = { Text("Correo electrónico") },
                            leadingIcon   = { Icon(Icons.Outlined.Email, null, tint = MaterialTheme.colorScheme.primary) },
                            isError       = emailError != null,
                            supportingText = emailError?.let { { Text(it) } },
                            modifier      = Modifier.fillMaxWidth(),
                            singleLine    = true,
                            enabled       = !guardando,
                            shape         = RoundedCornerShape(14.dp),
                            colors        = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor   = MaterialTheme.colorScheme.surfaceContainerLow,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow
                            ),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Done)
                        )

                        if (!esNuevo) {
                            Spacer(Modifier.height(4.dp))
                            OutlinedButton(
                                onClick  = { showConfirmDesactivar = true },
                                modifier = Modifier.fillMaxWidth(),
                                enabled  = !guardando,
                                shape    = RoundedCornerShape(12.dp),
                                colors   = ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.error
                                ),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f))
                            ) {
                                Icon(Icons.Outlined.PersonOff, null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Desactivar cuenta", fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                Spacer(modifier = Modifier.height(14.dp))

                // Acciones M3
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            viewModel.estaEscuchandoVoz = false
                            onDismiss()
                        },
                        enabled = !guardando,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cancelar", fontWeight = FontWeight.SemiBold)
                    }

                    Button(
                        onClick = {
                            if (validar()) {
                                viewModel.estaEscuchandoVoz = false
                                val telefonoSanitizado = telefono.replace(" ", "").trim()

                                val emailSanitizado = email.trim().ifBlank { null }

                                if (esNuevo) {
                                    viewModel.crearCliente(
                                        CrearClienteRequest(
                                            nombre.trim(),
                                            apellido.trim(),
                                            telefonoSanitizado.trim(),
                                            emailSanitizado
                                        )
                                    )
                                } else {
                                    viewModel.actualizarCliente(
                                        idCliente,
                                        ActualizarClienteRequest(
                                            nombre.trim(),
                                            apellido.trim(),
                                            telefonoSanitizado.trim(),
                                            emailSanitizado
                                        )
                                    )
                                }
                            }
                        },
                        enabled = !guardando,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (guardando) {
                            CircularProgressIndicator(
                                modifier    = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color       = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Icon(Icons.Outlined.Check, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(if (esNuevo) "Registrar" else "Guardar", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    if (showConfirmDesactivar) {
        AlertDialog(
            onDismissRequest = { showConfirmDesactivar = false },
            shape            = RoundedCornerShape(28.dp),
            containerColor   = MaterialTheme.colorScheme.surfaceContainerHigh,
            icon  = {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.errorContainer
                ) {
                    Icon(Icons.Outlined.Warning, null, tint = MaterialTheme.colorScheme.onErrorContainer, modifier = Modifier.padding(12.dp).size(26.dp))
                }
            },
            title = { Text("¿Desactivar cliente?", fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) },
            text  = { Text("El cliente ya no aparecerá en las listas de reserva activas.", style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center) },
            confirmButton = {
                Button(
                    onClick = {
                        showConfirmDesactivar = false
                        viewModel.estaEscuchandoVoz = false
                        viewModel.desactivarCliente(idCliente)
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Desactivar cliente", fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showConfirmDesactivar = false },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Cancelar") }
            }
        )
    }
}

@Composable
private fun SectionLabel(icon: androidx.compose.ui.graphics.vector.ImageVector, texto: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier          = Modifier.padding(top = 6.dp, bottom = 2.dp)
    ) {
        Icon(
            imageVector        = icon,
            contentDescription = null,
            modifier           = Modifier.size(16.dp),
            tint               = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text       = texto,
            style      = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color      = MaterialTheme.colorScheme.primary
        )
    }
}
