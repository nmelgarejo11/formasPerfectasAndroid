package com.spa.appointments.ui.perfil

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.spa.appointments.domain.model.ActualizarPerfilRequest
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.automirrored.filled.ArrowBack

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilScreen(
    onBack: () -> Unit,
    onLogout: () -> Unit,
    viewModel: PerfilViewModel = hiltViewModel()
) {
    val context     = LocalContext.current
    val uiState     by viewModel.uiState.collectAsState()
    val actionState by viewModel.actionState.collectAsState()

    // Campos editables
    var nombre   by remember { mutableStateOf("") }
    var apellido by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var email    by remember { mutableStateOf("") }
    var initialized by remember { mutableStateOf(false) }

    // URI de foto seleccionada localmente (preview inmediato)
    var fotoUri by remember { mutableStateOf<Uri?>(null) }

    val fotoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            fotoUri = it
            viewModel.subirFoto(it, context)
        }
    }

    // Inicializar campos cuando llega el perfil
    if (uiState is PerfilUiState.Success && !initialized) {
        val p = (uiState as PerfilUiState.Success).perfil
        nombre      = p.nombre
        apellido    = p.apellido
        telefono    = p.telefono ?: ""
        email       = p.email ?: ""
        initialized = true
    }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(actionState) {
        when (actionState) {
            is PerfilActionState.Success ->
                snackbarHostState.showSnackbar("Guardado correctamente")
            is PerfilActionState.Error ->
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
                title = { Text("Mi Perfil", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->

        when (uiState) {

            is PerfilUiState.Loading -> {
                Box(
                    Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator() }
            }

            is PerfilUiState.Error -> {
                Box(
                    Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = (uiState as PerfilUiState.Error).mensaje,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            is PerfilUiState.Success -> {
                val perfil = (uiState as PerfilUiState.Success).perfil

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp, vertical = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    // ── Foto de perfil ──────────────────────────────────────
                    Box(contentAlignment = Alignment.BottomEnd) {
                        val fotoModel: Any? = fotoUri ?: perfil.fotoUrl?.takeIf { it.isNotEmpty() }

                        if (fotoModel != null) {
                            AsyncImage(
                                model = fotoModel,
                                contentDescription = "Foto de perfil",
                                modifier = Modifier
                                    .size(110.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(110.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    modifier = Modifier.size(60.dp),
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }

                        // Botón cámara
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                                .clickable { fotoLauncher.launch("image/*") },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = "Cambiar foto",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    // Nombre de usuario (solo lectura, informativo)
                    Text(
                        text = "@${perfil.nombreUsuario}",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(Modifier.height(28.dp))

                    // ── Campos editables ────────────────────────────────────
                    OutlinedTextField(
                        value = nombre,
                        onValueChange = { nombre = it },
                        label = { Text("Nombre") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(Modifier.height(12.dp))

                    OutlinedTextField(
                        value = apellido,
                        onValueChange = { apellido = it },
                        label = { Text("Apellido") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(Modifier.height(12.dp))

                    OutlinedTextField(
                        value = telefono,
                        onValueChange = { telefono = it },
                        label = { Text("Teléfono") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(Modifier.height(12.dp))

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(Modifier.height(32.dp))

                    // ── Botón guardar ───────────────────────────────────────
                    Button(
                        onClick = {
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
                            .height(50.dp),
                        enabled = actionState !is PerfilActionState.Loading
                    ) {
                        if (actionState is PerfilActionState.Loading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Guardar cambios", fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    OutlinedButton(
                        onClick = onLogout,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error)
                    ) {
                        Text("Cerrar sesión", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}