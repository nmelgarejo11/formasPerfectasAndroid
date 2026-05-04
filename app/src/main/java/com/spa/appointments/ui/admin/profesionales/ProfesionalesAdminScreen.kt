package com.spa.appointments.ui.admin.profesionales

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.spa.appointments.domain.model.ProfesionalAdmin

// ─── Screen principal ─────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfesionalesAdminScreen(
    onBack:       () -> Unit,
    onVerDetalle: (Int) -> Unit,
    onVerHorario: (Int) -> Unit,
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

    val fotoLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { editando?.let { prof -> viewModel.subirFoto(prof.id, it, context) } }
    }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) { viewModel.cargarDatos() }

    LaunchedEffect(uiState) {
        when (val s = uiState) {
            is ProfesionalesUiState.Success -> {
                showDialog = false
                editando   = null
                viewModel.resetState()
            }
            is ProfesionalesUiState.Error -> {
                snackbarHostState.showSnackbar(s.mensaje)
                viewModel.resetState()
            }
            else -> Unit
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text       = "Profesionales",
                            style      = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        if (profesionales.isNotEmpty()) {
                            Text(
                                text  = "${profesionales.size} ${if (profesionales.size == 1) "profesional" else "profesionales"}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { editando = null; showDialog = true },
                icon    = { Icon(Icons.Default.Add, null) },
                text    = { Text("Nuevo profesional") }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // ── Buscador ──────────────────────────────────────────────────
            OutlinedTextField(
                value         = busqueda,
                onValueChange = { busqueda = it },
                label         = { Text("Buscar profesional") },
                leadingIcon   = { Icon(Icons.Default.Search, null) },
                trailingIcon  = {
                    if (busqueda.isNotBlank()) {
                        IconButton(onClick = { busqueda = "" }) {
                            Icon(Icons.Default.Clear, null)
                        }
                    }
                },
                modifier   = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape      = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    uiState is ProfesionalesUiState.Loading && profesionales.isEmpty() -> {
                        Column(
                            modifier            = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            CircularProgressIndicator()
                            Text(
                                "Cargando profesionales…",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    filtrados.isEmpty() -> {
                        Column(
                            modifier            = Modifier
                                .align(Alignment.Center)
                                .padding(40.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(24.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Icon(
                                    imageVector        = if (busqueda.isBlank()) Icons.Default.Group
                                    else Icons.Default.SearchOff,
                                    contentDescription = null,
                                    modifier           = Modifier.padding(20.dp).size(48.dp),
                                    tint               = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text       = if (busqueda.isBlank()) "Sin profesionales aún"
                                else "Sin resultados",
                                style      = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text      = if (busqueda.isBlank())
                                    "Crea el primer profesional con el botón inferior."
                                else
                                    "Ningún profesional coincide con \"$busqueda\".",
                                style     = MaterialTheme.typography.bodyMedium,
                                color     = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    else -> {
                        LazyColumn(
                            contentPadding      = PaddingValues(
                                start  = 16.dp,
                                end    = 16.dp,
                                top    = 4.dp,
                                bottom = 88.dp
                            ),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(filtrados, key = { it.id }) { prof ->
                                ProfesionalCard(
                                    profesional  = prof,
                                    onEditar     = { editando = prof; showDialog = true },
                                    onToggle     = { viewModel.toggleEstado(prof.id) },
                                    onFoto       = { editando = prof; fotoLauncher.launch("image/*") },
                                    onDetalle    = { onVerDetalle(prof.id) },
                                    onVerHorario = { onVerHorario(prof.id) }
                                )
                            }
                        }
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

// ─── Card de profesional ──────────────────────────────────────────────────────

@Composable
private fun ProfesionalCard(
    profesional:  ProfesionalAdmin,
    onEditar:     () -> Unit,
    onToggle:     () -> Unit,
    onFoto:       () -> Unit,
    onDetalle:    () -> Unit,
    onVerHorario: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }
    val inicial = "${profesional.nombre.firstOrNull() ?: ""}${profesional.apellido.firstOrNull() ?: ""}"
        .uppercase()

    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors    = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier          = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // ── Avatar con botón cámara ───────────────────────────────────
            Box(
                modifier         = Modifier.size(56.dp),
                contentAlignment = Alignment.BottomEnd
            ) {
                if (!profesional.foto.isNullOrBlank()) {
                    AsyncImage(
                        model              = profesional.foto,
                        contentDescription = null,
                        contentScale       = ContentScale.Crop,
                        modifier           = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                    )
                } else {
                    Surface(
                        modifier = Modifier.size(56.dp),
                        shape    = CircleShape,
                        color    = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text       = inicial,
                                style      = MaterialTheme.typography.titleSmall,
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
                        .size(22.dp)
                        .clickable { onFoto() }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.CameraAlt, "Cambiar foto",
                            modifier = Modifier.size(12.dp),
                            tint     = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }

            Spacer(Modifier.width(12.dp))

            // ── Info ──────────────────────────────────────────────────────
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier              = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text       = "${profesional.nombre} ${profesional.apellido}",
                        style      = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier   = Modifier.weight(1f)
                    )
                    // Chip estado
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (profesional.estado)
                            MaterialTheme.colorScheme.primaryContainer
                        else
                            MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text  = if (profesional.estado) "Activo" else "Inactivo",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (profesional.estado)
                                MaterialTheme.colorScheme.onPrimaryContainer
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Badge, null,
                        modifier = Modifier.size(11.dp),
                        tint     = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.width(3.dp))
                    Text(
                        text  = profesional.nombreCargo,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (!profesional.telefono.isNullOrBlank()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Phone, null,
                            modifier = Modifier.size(11.dp),
                            tint     = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.width(3.dp))
                        Text(
                            text  = profesional.telefono,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // ── Menú contextual (reemplaza 3 IconButtons) ─────────────────
            Box {
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(
                        Icons.Default.MoreVert, "Más opciones",
                        modifier = Modifier.size(20.dp)
                    )
                }
                DropdownMenu(
                    expanded         = menuExpanded,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text        = { Text("Asignaciones") },
                        leadingIcon = { Icon(Icons.Default.Tune, null) },
                        onClick     = { menuExpanded = false; onDetalle() }
                    )
                    DropdownMenuItem(
                        text        = { Text("Horario") },
                        leadingIcon = { Icon(Icons.Default.CalendarMonth, null) },
                        onClick     = { menuExpanded = false; onVerHorario() }
                    )
                    DropdownMenuItem(
                        text        = { Text("Editar") },
                        leadingIcon = { Icon(Icons.Default.Edit, null) },
                        onClick     = { menuExpanded = false; onEditar() }
                    )
                    HorizontalDivider()
                    DropdownMenuItem(
                        text        = {
                            Text(
                                if (profesional.estado) "Desactivar" else "Activar",
                                color = if (profesional.estado) MaterialTheme.colorScheme.error
                                else MaterialTheme.colorScheme.primary
                            )
                        },
                        leadingIcon = {
                            Icon(
                                if (profesional.estado) Icons.Default.PersonOff
                                else Icons.Default.PersonAdd,
                                null,
                                tint = if (profesional.estado) MaterialTheme.colorScheme.error
                                else MaterialTheme.colorScheme.primary
                            )
                        },
                        onClick = { menuExpanded = false; onToggle() }
                    )
                }
            }
        }
    }
}