// Ruta: app/src/main/java/com/spa/appointments/ui/perfil/PerfilScreen.kt
package com.spa.appointments.ui.perfil

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.spa.appointments.core.theme.ThemeMode
import com.spa.appointments.domain.model.ActualizarPerfilRequest

// ─── Screen Principal ─────────────────────────────────────────────────────────

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
    val modoTema     by viewModel.modoTema.collectAsState()

    var nombreUsuario   by remember { mutableStateOf("") }
    var clave           by remember { mutableStateOf("") }
    var claveVisible    by remember { mutableStateOf(false) }
    var initialized     by remember { mutableStateOf(false) }
    var fotoUri         by remember { mutableStateOf<Uri?>(null) }

    val fotoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { fotoUri = it; viewModel.subirFoto(it, context) }
    }

    if (uiState is PerfilUiState.Success && !initialized) {
        val p          = (uiState as PerfilUiState.Success).perfil
        nombreUsuario = p.nombreUsuario
        initialized    = true
    }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(actionState) {
        when (actionState) {
            is PerfilActionState.Success -> {
                snackbarHostState.showSnackbar("Perfil guardado correctamente")
                clave = ""
            }
            is PerfilActionState.Error   ->
                snackbarHostState.showSnackbar(
                    (actionState as PerfilActionState.Error).mensaje
                )
            else -> Unit
        }
        if (actionState !is PerfilActionState.Idle) viewModel.resetActionState()
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text       = "Mi perfil",
                                style      = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text  = "Administra tus datos de usuario",
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
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )

                // 🎨 Canvas: Acento decorativo debajo de la TopAppBar
                val primaryColor   = MaterialTheme.colorScheme.primary
                val secondaryColor = MaterialTheme.colorScheme.secondary
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                ) {
                    drawRect(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                primaryColor.copy(alpha = 0.4f),
                                secondaryColor.copy(alpha = 0.2f),
                                primaryColor.copy(alpha = 0.05f)
                            )
                        )
                    )
                }

                HorizontalDivider(
                    thickness = 0.5.dp,
                    color     = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )
            }
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
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator(
                            color       = MaterialTheme.colorScheme.primary,
                            trackColor  = MaterialTheme.colorScheme.primaryContainer,
                            strokeWidth = 3.dp,
                            modifier    = Modifier.size(36.dp)
                        )
                        Text(
                            "Cargando perfil…",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            is PerfilUiState.Error -> {
                OutlinedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(padding)
                        .padding(24.dp),
                    shape  = RoundedCornerShape(24.dp),
                    colors = CardDefaults.outlinedCardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                    )
                ) {
                    Column(
                        modifier            = Modifier.padding(32.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.errorContainer
                        ) {
                            Icon(
                                Icons.Default.CloudOff, null,
                                modifier = Modifier.padding(16.dp).size(36.dp),
                                tint     = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                        Text(
                            text       = "Error al cargar perfil",
                            style      = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text      = state.mensaje,
                            color     = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center,
                            style     = MaterialTheme.typography.bodyMedium
                        )
                        Button(
                            onClick = { viewModel.cargarPerfil() },
                            shape   = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Refresh, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Reintentar")
                        }
                    }
                }
            }

            is PerfilUiState.Success -> {
                val perfil       = state.perfil
                val primaryColor = MaterialTheme.colorScheme.primary

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(0.dp)
                ) {

                    // ── Avatar con Canvas Glow Rings ──────────────────────
                    Box(
                        contentAlignment = Alignment.BottomEnd,
                        modifier         = Modifier.padding(top = 8.dp)
                    ) {
                        Box(
                            modifier         = Modifier.size(124.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                drawCircle(
                                    color = primaryColor.copy(alpha = 0.3f),
                                    style = Stroke(width = 2.dp.toPx())
                                )
                            }

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
                                            text       = perfil.nombreUsuario.firstOrNull()?.uppercase() ?: perfil.usuario.firstOrNull()?.uppercase() ?: "?",
                                            style      = MaterialTheme.typography.headlineLarge,
                                            fontWeight = FontWeight.Bold,
                                            color      = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }
                                }
                            }
                        }

                        // Botón cámara flotante
                        Surface(
                            shape    = CircleShape,
                            color    = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .size(36.dp)
                                .clickable { fotoLauncher.launch("image/*") }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector        = Icons.Default.CameraAlt,
                                    contentDescription = "Cambiar foto",
                                    tint               = MaterialTheme.colorScheme.onPrimary,
                                    modifier           = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(10.dp))

                    // Chip de Nombre de usuario / login
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh
                    ) {
                        Text(
                            text       = "@${perfil.usuario.ifBlank { perfil.nombreUsuario }}",
                            style      = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color      = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier   = Modifier.padding(horizontal = 14.dp, vertical = 4.dp)
                        )
                    }

                    Spacer(Modifier.height(28.dp))

                    // ── Sección Datos de Usuario ──────────────────────────
                    SectionHeader(
                        icono  = Icons.Default.ManageAccounts,
                        titulo = "DATOS DE USUARIO"
                    )

                    Spacer(Modifier.height(12.dp))

                    OutlinedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape    = RoundedCornerShape(18.dp),
                        colors   = CardDefaults.outlinedCardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
                    ) {
                        Column(
                            modifier            = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedTextField(
                                value         = nombreUsuario,
                                onValueChange = { nombreUsuario = it },
                                label         = { Text("Nombre a mostrar") },
                                leadingIcon   = { Icon(Icons.Default.Person, null, tint = MaterialTheme.colorScheme.primary) },
                                modifier      = Modifier.fillMaxWidth(),
                                singleLine    = true,
                                shape         = RoundedCornerShape(14.dp),
                                colors        = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor   = MaterialTheme.colorScheme.surfaceContainerLow,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow
                                ),
                                keyboardOptions = KeyboardOptions(
                                    capitalization = KeyboardCapitalization.Words,
                                    imeAction      = ImeAction.Next
                                ),
                                keyboardActions = KeyboardActions(
                                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                                )
                            )

                            OutlinedTextField(
                                value         = clave,
                                onValueChange = { clave = it },
                                label         = { Text("Nueva contraseña (opcional)") },
                                leadingIcon   = { Icon(Icons.Default.Lock, null, tint = MaterialTheme.colorScheme.primary) },
                                trailingIcon  = {
                                    val icon = if (claveVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility
                                    IconButton(onClick = { claveVisible = !claveVisible }) {
                                        Icon(icon, contentDescription = "Mostrar contraseña")
                                    }
                                },
                                visualTransformation = if (claveVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                modifier      = Modifier.fillMaxWidth(),
                                singleLine    = true,
                                shape         = RoundedCornerShape(14.dp),
                                colors        = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor   = MaterialTheme.colorScheme.surfaceContainerLow,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow
                                ),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Password,
                                    imeAction    = ImeAction.Done
                                ),
                                keyboardActions = KeyboardActions(
                                    onDone = { focusManager.clearFocus() }
                                )
                            )
                        }
                    }

                    Spacer(Modifier.height(28.dp))

                    // ── Sección Apariencia y Tema ──────────────────────────
                    SectionHeader(
                        icono  = Icons.Default.Palette,
                        titulo = "APARIENCIA Y TEMA"
                    )

                    Spacer(Modifier.height(12.dp))

                    OutlinedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape    = RoundedCornerShape(18.dp),
                        colors   = CardDefaults.outlinedCardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
                    ) {
                        Column(
                            modifier = Modifier.padding(8.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            ThemeOptionItem(
                                titulo       = "Predeterminado del sistema",
                                subtitulo    = "Sigue la configuración de tu dispositivo",
                                icono        = Icons.Default.SettingsSuggest,
                                seleccionado = modoTema == ThemeMode.SYSTEM,
                                onClick      = { viewModel.cambiarModoTema(ThemeMode.SYSTEM) }
                            )
                            HorizontalDivider(
                                color    = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                            ThemeOptionItem(
                                titulo       = "Modo Claro",
                                subtitulo    = "Fondo claro activado siempre",
                                icono        = Icons.Default.LightMode,
                                seleccionado = modoTema == ThemeMode.LIGHT,
                                onClick      = { viewModel.cambiarModoTema(ThemeMode.LIGHT) }
                            )
                            HorizontalDivider(
                                color    = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                            ThemeOptionItem(
                                titulo       = "Modo Oscuro",
                                subtitulo    = "Fondo oscuro activado siempre",
                                icono        = Icons.Default.DarkMode,
                                seleccionado = modoTema == ThemeMode.DARK,
                                onClick      = { viewModel.cambiarModoTema(ThemeMode.DARK) }
                            )
                        }
                    }

                    Spacer(Modifier.height(32.dp))

                    // ── Botón Guardar ─────────────────────────────────────
                    Button(
                        onClick  = {
                            focusManager.clearFocus()
                            viewModel.actualizarPerfil(
                                ActualizarPerfilRequest(
                                    nombreUsuario = nombreUsuario,
                                    clave         = clave.ifBlank { null }
                                )
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape    = RoundedCornerShape(14.dp),
                        enabled  = actionState !is PerfilActionState.Loading && nombreUsuario.isNotBlank()
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
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Guardar cambios",
                                fontWeight = FontWeight.Bold,
                                fontSize   = 15.sp
                            )
                        }
                    }

                    Spacer(Modifier.height(16.dp))
                }
            }
        }
    }
}

// ─── Header de Sección M3 ─────────────────────────────────────────────────────

@Composable
private fun SectionHeader(
    icono:  androidx.compose.ui.graphics.vector.ImageVector,
    titulo: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier          = Modifier.fillMaxWidth().padding(bottom = 4.dp)
    ) {
        Icon(
            imageVector        = icono,
            contentDescription = null,
            modifier           = Modifier.size(18.dp),
            tint               = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text       = titulo,
            style      = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color      = MaterialTheme.colorScheme.primary
        )
    }
}

// ─── Opción de Tema M3 ─────────────────────────────────────────────────────────

@Composable
private fun ThemeOptionItem(
    titulo:       String,
    subtitulo:    String,
    icono:        androidx.compose.ui.graphics.vector.ImageVector,
    seleccionado: Boolean,
    onClick:      () -> Unit
) {
    Surface(
        onClick = onClick,
        shape   = RoundedCornerShape(12.dp),
        color   = if (seleccionado) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                  else MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Row(
            modifier          = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape    = CircleShape,
                color    = if (seleccionado) MaterialTheme.colorScheme.primary
                           else MaterialTheme.colorScheme.surfaceContainerHigh,
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector        = icono,
                        contentDescription = null,
                        tint               = if (seleccionado) MaterialTheme.colorScheme.onPrimary
                                             else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier           = Modifier.size(18.dp)
                    )
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text       = titulo,
                    style      = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (seleccionado) FontWeight.Bold else FontWeight.Medium,
                    color      = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text  = subtitulo,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            RadioButton(
                selected = seleccionado,
                onClick  = onClick,
                colors   = RadioButtonDefaults.colors(
                    selectedColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    }
}