package com.spa.appointments.ui.clientes

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.hilt.navigation.compose.hiltViewModel
import com.spa.appointments.domain.model.ActualizarClienteRequest
import com.spa.appointments.domain.model.CrearClienteRequest
import kotlinx.coroutines.launch

// Modelo interno para controlar qué campo se está editando
private data class CampoEdicion(
    val label: String,
    val valor: String,
    val keyboardType: KeyboardType = KeyboardType.Text,
    val maxLength: Int = 80,
    val onGuardar: (String) -> Unit
)

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
    val scope        = rememberCoroutineScope()

    var nombre   by remember { mutableStateOf("") }
    var apellido by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var email    by remember { mutableStateOf("") }
    var initialized by remember { mutableStateOf(false) }

    var nombreError  by remember { mutableStateOf<String?>(null) }
    var apellidoError by remember { mutableStateOf<String?>(null) }
    var emailError   by remember { mutableStateOf<String?>(null) }

    var showConfirmDesactivar by remember { mutableStateOf(false) }
    val snackbarHostState     = remember { SnackbarHostState() }

    // BottomSheet
    val sheetState   = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var campoActivo  by remember { mutableStateOf<CampoEdicion?>(null) }

    fun abrirSheet(campo: CampoEdicion) {
        campoActivo = campo
        scope.launch { sheetState.show() }
    }

    fun cerrarSheet() {
        scope.launch { sheetState.hide() }
            .invokeOnCompletion { campoActivo = null }
    }

    LaunchedEffect(idCliente) {
        if (!esNuevo) viewModel.cargarCliente(idCliente)
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
        nombreError   = if (nombre.isBlank()) "Requerido" else null
        apellidoError = if (apellido.isBlank()) "Requerido" else null
        emailError = if (email.isNotBlank() &&
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
        ) "Email inválido" else null
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
                            fontWeight = FontWeight.Medium
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
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
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
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Avatar header (solo en modo edición)
                    if (!esNuevo) {
                        ClienteHeaderCard(nombre = nombre, apellido = apellido)
                    }

                    // Sección: Datos personales
                    SectionLabel(icon = Icons.Outlined.Badge, texto = "Datos personales")

                    CampoCard {
                        FilaEditable(
                            icon       = Icons.Outlined.Person,
                            label      = "Nombre",
                            valor      = nombre,
                            requerido  = true,
                            error      = nombreError,
                            onClick    = {
                                abrirSheet(CampoEdicion(
                                    label       = "Nombre",
                                    valor       = nombre,
                                    onGuardar   = { nombre = it; nombreError = null }
                                ))
                            }
                        )
                        HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
                        FilaEditable(
                            icon      = Icons.Outlined.Person,
                            label     = "Apellido",
                            valor     = apellido,
                            requerido = true,
                            error     = apellidoError,
                            onClick   = {
                                abrirSheet(CampoEdicion(
                                    label     = "Apellido",
                                    valor     = apellido,
                                    onGuardar = { apellido = it; apellidoError = null }
                                ))
                            }
                        )
                    }

                    // Sección: Contacto
                    SectionLabel(icon = Icons.Outlined.ContactMail, texto = "Contacto")

                    CampoCard {
                        FilaEditable(
                            icon    = Icons.Outlined.Phone,
                            label   = "Teléfono",
                            valor   = telefono,
                            onClick = {
                                abrirSheet(CampoEdicion(
                                    label        = "Teléfono",
                                    valor        = telefono,
                                    keyboardType = KeyboardType.Phone,
                                    maxLength    = 15,
                                    onGuardar    = { telefono = it }
                                ))
                            }
                        )
                        HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
                        FilaEditable(
                            icon    = Icons.Outlined.Email,
                            label   = "Correo electrónico",
                            valor   = email,
                            error   = emailError,
                            onClick = {
                                abrirSheet(CampoEdicion(
                                    label        = "Correo electrónico",
                                    valor        = email,
                                    keyboardType = KeyboardType.Email,
                                    onGuardar    = { email = it; emailError = null }
                                ))
                            }
                        )
                    }

                    Spacer(Modifier.height(8.dp))

                    // Botón guardar
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
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape    = RoundedCornerShape(14.dp),
                        enabled  = actionState !is ClienteActionState.Loading
                    ) {
                        if (actionState is ClienteActionState.Loading) {
                            CircularProgressIndicator(
                                modifier   = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color      = MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("Guardando...")
                        } else {
                            Icon(Icons.Outlined.Save, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text       = if (esNuevo) "Registrar cliente" else "Guardar cambios",
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    // Botón desactivar (solo edición)
                    if (!esNuevo) {
                        OutlinedButton(
                            onClick  = { showConfirmDesactivar = true },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape    = RoundedCornerShape(14.dp),
                            colors   = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                0.5.dp, MaterialTheme.colorScheme.error
                            )
                        ) {
                            Icon(Icons.Outlined.PersonOff, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Desactivar cuenta", fontWeight = FontWeight.Medium)
                        }
                    }

                    Spacer(Modifier.height(32.dp))
                }
            }
        }
    }

    // BottomSheet para edición de campo
    if (campoActivo != null) {
        EditarCampoSheet(
            campo      = campoActivo!!,
            sheetState = sheetState,
            onDismiss  = { cerrarSheet() }
        )
    }

    // Diálogo confirmar desactivar
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

// ─── Header avatar ───────────────────────────────────────────────────────────
@Composable
private fun ClienteHeaderCard(nombre: String, apellido: String) {
    val iniciales = "${nombre.take(1)}${apellido.take(1)}".uppercase()

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(16.dp),
        color    = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        tonalElevation = 0.dp
    ) {
        Row(
            modifier          = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Iniciales
            Surface(
                modifier = Modifier.size(56.dp),
                shape    = CircleShape,
                color    = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text       = iniciales,
                        style      = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Medium,
                        color      = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(Modifier.width(14.dp))

            Column {
                Text(
                    text       = "$nombre $apellido".trim(),
                    style      = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                Spacer(Modifier.height(4.dp))
                // Badge estado
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Row(
                        modifier          = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint     = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text  = "Cliente registrado",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }
        }
    }
}

// ─── Section label ───────────────────────────────────────────────────────────
@Composable
private fun SectionLabel(icon: ImageVector, texto: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier          = Modifier.padding(start = 4.dp)
    ) {
        Icon(
            imageVector        = icon,
            contentDescription = null,
            modifier           = Modifier.size(15.dp),
            tint               = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text       = texto,
            style      = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium,
            color      = MaterialTheme.colorScheme.primary
        )
    }
}

// ─── Card contenedor ─────────────────────────────────────────────────────────
@Composable
private fun CampoCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(14.dp),
        colors   = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        ),
        border = androidx.compose.foundation.BorderStroke(
            0.5.dp, MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Column { content() }
    }
}

// ─── Fila editable ───────────────────────────────────────────────────────────
@Composable
private fun FilaEditable(
    icon      : ImageVector,
    label     : String,
    valor     : String,
    requerido : Boolean   = false,
    error     : String?   = null,
    onClick   : () -> Unit
) {
    val colorTexto = when {
        error != null       -> MaterialTheme.colorScheme.error
        valor.isBlank()     -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
        else                -> MaterialTheme.colorScheme.onSurface
    }
    val valorMostrar = when {
        error != null   -> error
        valor.isBlank() -> "Sin $label registrado"
        else            -> valor
    }

    Row(
        modifier          = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Ícono con fondo
        Surface(
            modifier = Modifier.size(34.dp),
            shape    = RoundedCornerShape(8.dp),
            color    = MaterialTheme.colorScheme.surface,
            border   = androidx.compose.foundation.BorderStroke(
                0.5.dp, MaterialTheme.colorScheme.outlineVariant
            )
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector        = icon,
                    contentDescription = null,
                    modifier           = Modifier.size(17.dp),
                    tint               = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text  = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (requerido) {
                    Text(
                        text  = " *",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            Spacer(Modifier.height(2.dp))
            Text(
                text  = valorMostrar,
                style = MaterialTheme.typography.bodyMedium,
                color = colorTexto
            )
        }

        Icon(
            imageVector        = Icons.Outlined.ChevronRight,
            contentDescription = "Editar $label",
            modifier           = Modifier.size(18.dp),
            tint               = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
    }
}

// ─── BottomSheet editor de campo ─────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditarCampoSheet(
    campo      : CampoEdicion,
    sheetState : SheetState,
    onDismiss  : () -> Unit
) {
    var texto by remember(campo) { mutableStateOf(campo.valor) }
    val esValido = texto.isNotBlank() || !campo.label.contains("*")

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState       = sheetState,
        dragHandle       = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text       = "Editar ${campo.label.lowercase()}",
                style      = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium
            )

            OutlinedTextField(
                value         = texto,
                onValueChange = { if (it.length <= campo.maxLength) texto = it },
                label         = { Text(campo.label) },
                modifier      = Modifier.fillMaxWidth(),
                singleLine    = true,
                shape         = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(
                    keyboardType   = campo.keyboardType,
                    capitalization = if (campo.keyboardType == KeyboardType.Text)
                        KeyboardCapitalization.Words else KeyboardCapitalization.None
                )
            )

            Row(
                modifier            = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick  = onDismiss,
                    modifier = Modifier.weight(1f).height(46.dp),
                    shape    = RoundedCornerShape(12.dp)
                ) { Text("Cancelar") }

                Button(
                    onClick  = { campo.onGuardar(texto.trim()); onDismiss() },
                    modifier = Modifier.weight(1f).height(46.dp),
                    shape    = RoundedCornerShape(12.dp),
                    enabled  = esValido
                ) { Text("Confirmar") }
            }
        }
    }
}

