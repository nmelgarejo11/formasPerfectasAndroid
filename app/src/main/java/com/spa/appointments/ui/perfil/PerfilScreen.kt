package com.spa.appointments.ui.perfil

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.spa.appointments.domain.model.ActualizarPerfilRequest

// ─── Screen principal ─────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilScreen(
    onBack:    () -> Unit,
    onLogout:  () -> Unit,
    viewModel: PerfilViewModel = hiltViewModel()
) {
    val context      = LocalContext.current
    val focusManager = LocalFocusManager.current
    val uiState      by viewModel.uiState.collectAsState()
    val actionState  by viewModel.actionState.collectAsState()

    var nombre      by remember { mutableStateOf("") }
    var apellido    by remember { mutableStateOf("") }
    var telefono    by remember { mutableStateOf("") }
    var email       by remember { mutableStateOf("") }
    var initialized by remember { mutableStateOf(false) }
    var fotoUri     by remember { mutableStateOf<Uri?>(null) }

    val fotoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { fotoUri = it; viewModel.subirFoto(it, context) }
    }

    if (uiState is PerfilUiState.Success && !initialized) {
        val p    = (uiState as PerfilUiState.Success).perfil
        nombre   = p.nombre
        apellido = p.apellido
        telefono = p.telefono ?: ""
        email    = p.email    ?: ""
        initialized = true
    }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(actionState) {
        when (actionState) {
            is PerfilActionState.Success ->
                snackbarHostState.showSnackbar("Guardado correctamente")
            is PerfilActionState.Error   ->
                snackbarHostState.showSnackbar(
                    (actionState as PerfilActionState.Error).mensaje
                )
            else -> Unit
        }
        if (actionState !is PerfilActionState.Idle) viewModel.resetActionState()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text       = "Mi perfil",
                            style      = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text  = "Edita tu información personal",
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

        when (val state = uiState) {

            is PerfilUiState.Loading -> {
                Box(
                    modifier         = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CircularProgressIndicator()
                        Text(
                            "Cargando perfil…",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            is PerfilUiState.Error -> {
                Box(
                    modifier         = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        modifier            = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = MaterialTheme.colorScheme.errorContainer
                        ) {
                            Icon(
                                Icons.Default.CloudOff, null,
                                modifier = Modifier.padding(16.dp).size(32.dp),
                                tint     = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                        Text(
                            text      = state.mensaje,
                            color     = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center,
                            style     = MaterialTheme.typography.bodyMedium
                        )
                        Button(
                            onClick = { viewModel.cargarPerfil() },
                            shape   = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Refresh, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Reintentar")
                        }
                    }
                }
            }

            is PerfilUiState.Success -> {
                val perfil = state.perfil

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp, vertical = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(0.dp)
                ) {

                    // ── Avatar ────────────────────────────────────────────
                    Box(contentAlignment = Alignment.BottomEnd) {
                        val fotoModel: Any? = fotoUri
                            ?: perfil.fotoUrl?.takeIf { it.isNotEmpty() }

                        if (fotoModel != null) {
                            AsyncImage(
                                model              = fotoModel,
                                contentDescription = "Foto de perfil",
                                modifier           = Modifier
                                    .size(110.dp)
                                    .clip(CircleShape),
                                contentScale       = ContentScale.Crop
                            )
                        } else {
                            Surface(
                                modifier = Modifier.size(110.dp),
                                shape    = CircleShape,
                                color    = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text       = perfil.nombre.firstOrNull()?.uppercase() ?: "?",
                                        style      = MaterialTheme.typography.headlineLarge,
                                        fontWeight = FontWeight.Bold,
                                        color      = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                        }

                        // Botón cámara
                        Surface(
                            shape    = CircleShape,
                            color    = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .size(34.dp)
                                .clickable { fotoLauncher.launch("image/*") }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector        = Icons.Default.CameraAlt,
                                    contentDescription = "Cambiar foto",
                                    tint               = MaterialTheme.colorScheme.onPrimary,
                                    modifier           = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    // Chip de nombre de usuario
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text     = "@${perfil.nombreUsuario}",
                            fontSize = 12.sp,
                            color    = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }

                    Spacer(Modifier.height(28.dp))

                    // ── Sección datos personales ──────────────────────────
                    SectionHeader(
                        icono  = Icons.Default.Person,
                        titulo = "Datos personales"
                    )

                    Spacer(Modifier.height(12.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value         = nombre,
                            onValueChange = { nombre = it },
                            label         = { Text("Nombre") },
                            leadingIcon   = { Icon(Icons.Default.Badge, null) },
                            modifier      = Modifier.fillMaxWidth(),
                            singleLine    = true,
                            shape         = RoundedCornerShape(12.dp),
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Words,
                                imeAction      = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = { focusManager.moveFocus(FocusDirection.Down) }
                            )
                        )

                        OutlinedTextField(
                            value         = apellido,
                            onValueChange = { apellido = it },
                            label         = { Text("Apellido") },
                            leadingIcon   = { Icon(Icons.Default.Badge, null) },
                            modifier      = Modifier.fillMaxWidth(),
                            singleLine    = true,
                            shape         = RoundedCornerShape(12.dp),
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Words,
                                imeAction      = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = { focusManager.moveFocus(FocusDirection.Down) }
                            )
                        )
                    }

                    Spacer(Modifier.height(20.dp))

                    // ── Sección contacto ──────────────────────────────────
                    SectionHeader(
                        icono  = Icons.Default.ContactPhone,
                        titulo = "Información de contacto"
                    )

                    Spacer(Modifier.height(12.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value         = telefono,
                            onValueChange = { telefono = it },
                            label         = { Text("Teléfono") },
                            leadingIcon   = { Icon(Icons.Default.Phone, null) },
                            modifier      = Modifier.fillMaxWidth(),
                            singleLine    = true,
                            shape         = RoundedCornerShape(12.dp),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Phone,
                                imeAction    = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = { focusManager.moveFocus(FocusDirection.Down) }
                            )
                        )

                        OutlinedTextField(
                            value         = email,
                            onValueChange = { email = it },
                            label         = { Text("Email") },
                            leadingIcon   = { Icon(Icons.Default.Email, null) },
                            modifier      = Modifier.fillMaxWidth(),
                            singleLine    = true,
                            shape         = RoundedCornerShape(12.dp),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction    = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = { focusManager.clearFocus() }
                            )
                        )
                    }

                    Spacer(Modifier.height(32.dp))

                    HorizontalDivider(
                        color    = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                        modifier = Modifier.padding(bottom = 20.dp)
                    )

                    // ── Botón guardar ─────────────────────────────────────
                    Button(
                        onClick  = {
                            focusManager.clearFocus()
                            viewModel.actualizarPerfil(
                                ActualizarPerfilRequest(
                                    nombre   = nombre,
                                    apellido = apellido,
                                    telefono = telefono.ifBlank { null },
                                    email    = email.ifBlank { null }
                                )
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape    = RoundedCornerShape(12.dp),
                        enabled  = actionState !is PerfilActionState.Loading
                    ) {
                        if (actionState is PerfilActionState.Loading) {
                            CircularProgressIndicator(
                                modifier    = Modifier.size(20.dp),
                                color       = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                Icons.Default.Save, null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Guardar cambios",
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    // ── Botón cerrar sesión ───────────────────────────────
                    OutlinedButton(
                        onClick  = onLogout,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape    = RoundedCornerShape(12.dp),
                        colors   = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        border = BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
                        )
                    ) {
                        Icon(
                            Icons.Default.ExitToApp, null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Cerrar sesión", fontWeight = FontWeight.Medium)
                    }

                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
}

// ─── Header de sección ────────────────────────────────────────────────────────

@Composable
private fun SectionHeader(
    icono:  androidx.compose.ui.graphics.vector.ImageVector,
    titulo: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier          = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector        = icono,
            contentDescription = null,
            modifier           = Modifier.size(16.dp),
            tint               = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text       = titulo,
            style      = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color      = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.width(8.dp))
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color    = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )
    }
}