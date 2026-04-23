package com.spa.appointments.ui.admin.profesionales

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.spa.appointments.domain.model.ProfesionalAdmin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfesionalesAdminScreen(
    onBack: () -> Unit,
    onVerDetalle: (Int) -> Unit,
    viewModel: ProfesionalesAdminViewModel = hiltViewModel()
) {
    val profesionales by viewModel.profesionales.collectAsState()
    val uiState       by viewModel.uiState.collectAsState()
    val context       = LocalContext.current

    var busqueda   by remember { mutableStateOf("") }
    var showDialog by remember { mutableStateOf(false) }
    var editando   by remember { mutableStateOf<ProfesionalAdmin?>(null) }

    val filtrados = remember(profesionales, busqueda) {
        if (busqueda.isBlank()) profesionales
        else profesionales.filter {
            it.nombre.contains(busqueda, ignoreCase = true) ||
                    it.apellido.contains(busqueda, ignoreCase = true) ||
                    it.nombreCargo.contains(busqueda, ignoreCase = true)
        }
    }

    // Launcher para seleccionar foto
    val fotoLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { editando?.let { prof -> viewModel.subirFoto(prof.id, it, context) } }
    }

    LaunchedEffect(Unit) { viewModel.cargarDatos() }

    LaunchedEffect(uiState) {
        if (uiState is ProfesionalesUiState.Success) {
            showDialog = false
            editando = null
            viewModel.resetState()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profesionales") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { editando = null; showDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Nuevo profesional")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            OutlinedTextField(
                value = busqueda,
                onValueChange = { busqueda = it },
                label = { Text("Buscar profesional") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                trailingIcon = {
                    if (busqueda.isNotBlank()) {
                        IconButton(onClick = { busqueda = "" }) {
                            Icon(Icons.Default.Clear, null)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                singleLine = true
            )

            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    uiState is ProfesionalesUiState.Loading && profesionales.isEmpty() -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                    filtrados.isEmpty() -> {
                        Text(
                            text = if (busqueda.isBlank()) "Sin profesionales. Crea el primero."
                            else "Sin resultados para \"$busqueda\"",
                            modifier = Modifier.align(Alignment.Center),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    else -> {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(filtrados, key = { it.id }) { prof ->
                                ProfesionalCard(
                                    profesional = prof,
                                    onEditar    = { editando = prof; showDialog = true },
                                    onToggle    = { viewModel.toggleEstado(prof.id) },
                                    onFoto      = { editando = prof; fotoLauncher.launch("image/*") },
                                    onDetalle   = { onVerDetalle(prof.id) }
                                )
                            }
                        }
                    }
                }

                if (uiState is ProfesionalesUiState.Error) {
                    Snackbar(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(16.dp)
                    ) {
                        Text((uiState as ProfesionalesUiState.Error).mensaje)
                    }
                }
            }
        }
    }

    if (showDialog) {
        ProfesionalDialog(
            profesional = editando,
            cargos      = viewModel.cargos.collectAsState().value,
            guardando   = uiState is ProfesionalesUiState.Loading,
            onGuardar   = { req -> viewModel.guardarProfesional(editando?.id, req) },
            onDismiss   = { showDialog = false; editando = null; viewModel.resetState() }
        )
    }
}

@Composable
private fun ProfesionalCard(
    profesional: ProfesionalAdmin,
    onEditar:  () -> Unit,
    onToggle:  () -> Unit,
    onFoto:    () -> Unit,
    onDetalle: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors   = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Foto
            Box(
                modifier = Modifier.size(56.dp),
                contentAlignment = Alignment.BottomEnd
            ) {
                if (!profesional.foto.isNullOrBlank()) {
                    AsyncImage(
                        model = profesional.foto,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(56.dp)
                            .clip(MaterialTheme.shapes.medium)
                    )
                } else {
                    Surface(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(MaterialTheme.shapes.medium),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                modifier = Modifier.size(32.dp),
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
                // Botón cámara encima de la foto
                SmallFloatingActionButton(
                    onClick = onFoto,
                    modifier = Modifier.size(20.dp),
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(
                        Icons.Default.CameraAlt,
                        contentDescription = "Cambiar foto",
                        modifier = Modifier.size(12.dp),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${profesional.nombre} ${profesional.apellido}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = profesional.nombreCargo,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (!profesional.telefono.isNullOrBlank()) {
                    Text(
                        text = profesional.telefono,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Detalle (sedes y servicios)
            IconButton(onClick = onDetalle) {
                Icon(Icons.Default.Tune, contentDescription = "Asignaciones")
            }
            IconButton(onClick = onEditar) {
                Icon(Icons.Default.Edit, contentDescription = "Editar")
            }
            Switch(checked = profesional.estado, onCheckedChange = { onToggle() })
        }
    }
}