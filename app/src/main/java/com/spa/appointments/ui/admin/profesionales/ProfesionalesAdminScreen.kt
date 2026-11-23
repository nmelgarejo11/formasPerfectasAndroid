// Ruta: app/src/main/java/com/spa/appointments/ui/admin/profesionales/ProfesionalesAdminScreen.kt
package com.spa.appointments.ui.admin.profesionales

import android.content.Context
import android.net.Uri
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.spa.appointments.domain.model.ProfesionalAdmin
import com.spa.appointments.ui.components.SinPermisoContent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfesionalesAdminScreen(
    onBack: () -> Unit,
    onVerDetalle: (Int) -> Unit,
    onVerHorario: (Int) -> Unit,
    viewModel: ProfesionalesAdminViewModel = hiltViewModel()
) {
    val profesionales    by viewModel.profesionales.collectAsState()
    val cargos           by viewModel.cargos.collectAsState()
    val bancos           by viewModel.bancos.collectAsState()
    val tiposDocumento   by viewModel.tiposDocumento.collectAsState()
    val tiposCuenta      by viewModel.tiposCuenta.collectAsState()
    val uiState          by viewModel.uiState.collectAsState()

    var showDialog by remember { mutableStateOf(false) }
    var editando   by remember { mutableStateOf<ProfesionalAdmin?>(null) }
    var busqueda   by remember { mutableStateOf("") }

    val filtrados = remember(profesionales, busqueda) {
        if (busqueda.isBlank()) profesionales
        else profesionales.filter {
            it.nombre.contains(busqueda, ignoreCase = true) ||
                    it.apellido.contains(busqueda, ignoreCase = true) ||
                    it.nombreCargo.contains(busqueda, ignoreCase = true)
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.cargarDatos()
    }

    LaunchedEffect(uiState) {
        when (uiState) {
            is ProfesionalesUiState.Error -> {
                snackbarHostState.showSnackbar((uiState as ProfesionalesUiState.Error).mensaje)
                viewModel.resetState()
            }
            is ProfesionalesUiState.Success -> {
                showDialog = false
                editando   = null
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
                                text       = "Equipo de Trabajo",
                                style      = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            if (profesionales.isNotEmpty() && uiState !is ProfesionalesUiState.Forbidden) {
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
            if (uiState !is ProfesionalesUiState.Forbidden) {
                ExtendedFloatingActionButton(
                    onClick        = { editando = null; showDialog = true },
                    icon           = { Icon(Icons.Default.Add, null) },
                    text           = { Text("Nuevo profesional", fontWeight = FontWeight.Bold) },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor   = MaterialTheme.colorScheme.onPrimary,
                    shape          = RoundedCornerShape(16.dp)
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->

        if (uiState is ProfesionalesUiState.Forbidden) {
            SinPermisoContent(
                modifier = Modifier.padding(padding),
                onVolver = onBack
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value         = busqueda,
                    onValueChange = { busqueda = it },
                    label         = { Text("Buscar profesional o cargo") },
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
                    singleLine = true,
                    enabled    = uiState !is ProfesionalesUiState.Loading
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
                                    text  = "Cargando equipo de trabajo…",
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
                                                imageVector        = if (busqueda.isBlank()) Icons.Default.Badge
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
                                contentPadding      = PaddingValues(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 88.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(filtrados, key = { it.id }) { prof ->
                                    ProfesionalCard(
                                        prof         = prof,
                                        onVerDetalle = { onVerDetalle(prof.id) },
                                        onVerHorario = { onVerHorario(prof.id) },
                                        onEditar     = { editando = prof; showDialog = true },
                                        onToggle     = { viewModel.toggleEstado(prof.id) },
                                        onSubirFoto  = { uri, ctx -> viewModel.subirFoto(prof.id, uri, ctx) }
                                    )
                                }
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
            cargos         = cargos,
            bancos         = bancos,
            tiposDocumento = tiposDocumento,
            tiposCuenta    = tiposCuenta,
            guardando      = uiState is ProfesionalesUiState.Loading,
            onGuardar      = { req ->
                viewModel.guardarProfesional(editando?.id, req)
            },
            onDismiss      = { showDialog = false; editando = null }
        )
    }
}

// ─── Card Profesional M3 ───────────────────────────────────────────────────────

@Composable
private fun ProfesionalCard(
    prof         : ProfesionalAdmin,
    onVerDetalle : () -> Unit,
    onVerHorario : () -> Unit,
    onEditar     : () -> Unit,
    onToggle     : () -> Unit,
    onSubirFoto  : (Uri, Context) -> Unit
) {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { onSubirFoto(it, context) }
    }

    OutlinedCard(
        shape    = RoundedCornerShape(18.dp),
        colors   = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        border   = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier          = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier         = Modifier
                        .size(52.dp)
                        .clickable { launcher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    if (prof.foto.isNullOrBlank()) {
                        Surface(
                            shape    = CircleShape,
                            color    = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text       = prof.nombre.firstOrNull()?.uppercase() ?: "P",
                                    style      = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color      = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    } else {
                        AsyncImage(
                            model              = prof.foto,
                            contentDescription = prof.nombre,
                            modifier           = Modifier
                                .fillMaxSize()
                                .clip(CircleShape),
                            contentScale       = ContentScale.Crop
                        )
                    }

                    Surface(
                        shape    = CircleShape,
                        color    = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .size(18.dp)
                            .align(Alignment.BottomEnd)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector        = Icons.Default.CameraAlt,
                                contentDescription = "Cambiar foto",
                                modifier           = Modifier.size(10.dp),
                                tint               = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }

                Spacer(Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text       = "${prof.nombre} ${prof.apellido}".trim(),
                        style      = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color      = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text       = prof.nombreCargo,
                        style      = MaterialTheme.typography.bodySmall,
                        color      = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (!prof.telefono.isNullOrBlank()) {
                        Text(
                            text     = prof.telefono,
                            style    = MaterialTheme.typography.labelSmall,
                            color    = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }

                Switch(
                    checked         = prof.estado,
                    onCheckedChange = { onToggle() },
                    modifier        = Modifier.scale(0.85f)
                )
            }

            Spacer(Modifier.height(12.dp))
            HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(Modifier.height(10.dp))

            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick  = onVerDetalle,
                    shape    = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector        = Icons.Default.Settings,
                        contentDescription = null,
                        modifier           = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text("Configurar", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick  = onVerHorario,
                    shape    = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector        = Icons.Default.Schedule,
                        contentDescription = null,
                        modifier           = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text("Horario", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                }

                IconButton(
                    onClick  = onEditar,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector        = Icons.Default.Edit,
                        contentDescription = "Editar datos",
                        tint               = MaterialTheme.colorScheme.primary,
                        modifier           = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
