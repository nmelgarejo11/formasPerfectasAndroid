// Ruta: app/src/main/java/com/spa/appointments/ui/admin/profesionales/ProfesionalesAdminScreen.kt
package com.spa.appointments.ui.admin.profesionales

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.spa.appointments.domain.model.ProfesionalAdmin

// ─── Screen Principal ─────────────────────────────────────────────────────────

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
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text       = "Profesionales",
                                style      = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            if (profesionales.isNotEmpty()) {
                                Surface(
                                    shape    = RoundedCornerShape(50),
                                    color    = MaterialTheme.colorScheme.primaryContainer,
                                    modifier = Modifier.padding(top = 2.dp)
                                ) {
                                    Text(
                                        text     = "${profesionales.size} ${if (profesionales.size == 1) "profesional" else "profesionales"}",
                                        style    = MaterialTheme.typography.labelSmall,
                                        color    = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 1.dp)
                                    )
                                }
                            }
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
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick        = { editando = null; showDialog = true },
                icon           = { Icon(Icons.Default.Add, null) },
                text           = { Text("Nuevo profesional", fontWeight = FontWeight.Bold) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor   = MaterialTheme.colorScheme.onPrimary,
                shape          = RoundedCornerShape(16.dp)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Spacer(Modifier.height(8.dp))

            // ── Buscador M3 ───────────────────────────────────────────────
            OutlinedTextField(
                value         = busqueda,
                onValueChange = { busqueda = it },
                label         = { Text("Buscar profesional") },
                leadingIcon   = { Icon(Icons.Default.Search, null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                trailingIcon  = {
                    if (busqueda.isNotBlank()) {
                        IconButton(onClick = { busqueda = "" }) {
                            Icon(Icons.Default.Clear, null)
                        }
                    }
                },
                modifier   = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                shape      = RoundedCornerShape(14.dp),
                colors     = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor   = MaterialTheme.colorScheme.surfaceContainerLow,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow
                ),
                singleLine = true
            )

            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    uiState is ProfesionalesUiState.Loading && profesionales.isEmpty() -> {
                        Column(
                            modifier            = Modifier.align(Alignment.Center),
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
                                "Cargando profesionales…",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    filtrados.isEmpty() -> {
                        val primaryColor = MaterialTheme.colorScheme.primary

                        OutlinedCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp)
                                .align(Alignment.Center),
                            shape  = RoundedCornerShape(24.dp),
                            colors = CardDefaults.outlinedCardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                            )
                        ) {
                            Column(
                                modifier            = Modifier.padding(32.dp).fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                Box(
                                    modifier         = Modifier.size(80.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Canvas(modifier = Modifier.fillMaxSize()) {
                                        drawCircle(color = primaryColor.copy(alpha = 0.15f))
                                    }
                                    Surface(
                                        shape = CircleShape,
                                        color = MaterialTheme.colorScheme.surfaceContainerHigh
                                    ) {
                                        Icon(
                                            imageVector        = if (busqueda.isBlank()) Icons.Default.Group
                                            else Icons.Default.SearchOff,
                                            contentDescription = null,
                                            modifier           = Modifier.padding(18.dp).size(36.dp),
                                            tint               = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                Text(
                                    text       = if (busqueda.isBlank()) "Sin profesionales aún" else "Sin resultados",
                                    style      = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color      = MaterialTheme.colorScheme.onSurface
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
                    }

                    else -> {
                        LazyColumn(
                            contentPadding      = PaddingValues(
                                start  = 16.dp,
                                end    = 16.dp,
                                top    = 8.dp,
                                bottom = 88.dp
                            ),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
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
            profesional    = editando,
            cargos         = viewModel.cargos.collectAsState().value,
            bancos         = viewModel.bancos.collectAsState().value,
            tiposDocumento = viewModel.tiposDocumento.collectAsState().value,
            tiposCuenta    = viewModel.tiposCuenta.collectAsState().value,
            guardando      = uiState is ProfesionalesUiState.Loading,
            onGuardar      = { req -> viewModel.guardarProfesional(editando?.id, req) },
            onDismiss      = { showDialog = false; editando = null; viewModel.resetState() }
        )
    }
}

// ─── Card de Profesional M3 ───────────────────────────────────────────────────

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

    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(18.dp),
        colors   = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
    ) {
        Row(
            modifier          = Modifier
                .fillMaxWidth()
                .padding(14.dp),
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
                                style      = MaterialTheme.typography.titleMedium,
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
                        .size(24.dp)
                        .clickable { onFoto() }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.CameraAlt, "Cambiar foto",
                            modifier = Modifier.size(13.dp),
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
                        style      = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color      = MaterialTheme.colorScheme.onSurface,
                        modifier   = Modifier.weight(1f)
                    )
                    // Chip estado
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (profesional.estado)
                            MaterialTheme.colorScheme.primaryContainer
                        else
                            MaterialTheme.colorScheme.surfaceContainerHigh
                    ) {
                        Text(
                            text       = if (profesional.estado) "Activo" else "Inactivo",
                            style      = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color      = if (profesional.estado)
                                MaterialTheme.colorScheme.onPrimaryContainer
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier   = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Badge, null,
                        modifier = Modifier.size(12.dp),
                        tint     = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.width(4.dp))
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
                            modifier = Modifier.size(12.dp),
                            tint     = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text  = profesional.telefono,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // ── Menú Contextual M3 ─────────────────────────────────────────
            Box {
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(
                        Icons.Default.MoreVert, "Más opciones",
                        modifier = Modifier.size(20.dp),
                        tint     = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                DropdownMenu(
                    expanded         = menuExpanded,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text        = { Text("Asignaciones", fontWeight = FontWeight.SemiBold) },
                        leadingIcon = { Icon(Icons.Default.Tune, null, tint = MaterialTheme.colorScheme.primary) },
                        onClick     = { menuExpanded = false; onDetalle() }
                    )
                    DropdownMenuItem(
                        text        = { Text("Horario", fontWeight = FontWeight.SemiBold) },
                        leadingIcon = { Icon(Icons.Default.CalendarMonth, null, tint = MaterialTheme.colorScheme.primary) },
                        onClick     = { menuExpanded = false; onVerHorario() }
                    )
                    DropdownMenuItem(
                        text        = { Text("Editar", fontWeight = FontWeight.SemiBold) },
                        leadingIcon = { Icon(Icons.Default.Edit, null) },
                        onClick     = { menuExpanded = false; onEditar() }
                    )
                    HorizontalDivider()
                    DropdownMenuItem(
                        text        = {
                            Text(
                                if (profesional.estado) "Desactivar" else "Activar",
                                fontWeight = FontWeight.Bold,
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
