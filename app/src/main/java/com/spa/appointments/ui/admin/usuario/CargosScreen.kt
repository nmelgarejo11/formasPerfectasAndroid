// Ruta: app/src/main/java/com/spa/appointments/ui/admin/usuario/CargosScreen.kt
package com.spa.appointments.ui.admin.usuario

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.spa.appointments.domain.model.Cargo

data class CargoRequest(val nombre: String)

// ─── Screen Principal ──────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CargosScreen(
    onVolver: () -> Unit,
    viewModel: AdministracionViewModel = hiltViewModel()
) {
    val state = viewModel.uiState

    var showDialog by remember { mutableStateOf(false) }
    var editando   by remember { mutableStateOf<Cargo?>(null) }
    var busqueda   by remember { mutableStateOf("") }

    val filtrados = remember(state.cargos, busqueda) {
        if (busqueda.isBlank()) state.cargos
        else state.cargos.filter {
            it.nombre.contains(busqueda, ignoreCase = true)
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.error) {
        state.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.limpiarError()
        }
    }

    LaunchedEffect(state.cargando) {
        if (!state.cargando && !showDialog) {
            editando = null
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
                                text       = "Cargos",
                                style      = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            if (state.cargos.isNotEmpty()) {
                                Surface(
                                    shape    = RoundedCornerShape(50),
                                    color    = MaterialTheme.colorScheme.primaryContainer,
                                    modifier = Modifier.padding(top = 2.dp)
                                ) {
                                    Text(
                                        text     = "${state.cargos.size} ${if (state.cargos.size == 1) "cargo" else "cargos"}",
                                        style    = MaterialTheme.typography.labelSmall,
                                        color    = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 1.dp)
                                    )
                                }
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onVolver) {
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
                text           = { Text("Nuevo cargo", fontWeight = FontWeight.Bold) },
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
                label         = { Text("Buscar cargo") },
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
                    state.cargando && state.cargos.isEmpty() -> {
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
                                "Cargando cargos…",
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
                                            imageVector        = if (busqueda.isBlank()) Icons.Default.WorkOutline
                                            else Icons.Default.SearchOff,
                                            contentDescription = null,
                                            modifier           = Modifier.padding(18.dp).size(36.dp),
                                            tint               = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                Text(
                                    text       = if (busqueda.isBlank()) "Sin cargos aún" else "Sin resultados",
                                    style      = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color      = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text      = if (busqueda.isBlank())
                                        "Crea el primer cargo de empleado con el botón inferior."
                                    else
                                        "Ningún cargo coincide con \"$busqueda\".",
                                    style     = MaterialTheme.typography.bodyMedium,
                                    color     = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    else -> {
                        LazyColumn(
                            contentPadding = PaddingValues(
                                start  = 16.dp,
                                top    = 8.dp,
                                end    = 16.dp,
                                bottom = 88.dp
                            ),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(filtrados, key = { it.id }) { cargo ->
                                CargoAdminCard(
                                    cargo    = cargo,
                                    onEditar = { editando = cargo; showDialog = true },
                                    onToggle = { viewModel.cambiarEstadoCargo(cargo.id, !cargo.estado) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDialog) {
        CargoDialog(
            cargo     = editando,
            guardando = state.cargando,
            onGuardar = { req ->
                if (editando != null) {
                    // Actualizar cargo
                } else {
                    viewModel.crearCargo(req.nombre)
                }
                showDialog = false
            },
            onDismiss  = { showDialog = false; editando = null }
        )
    }
}

// ─── Card de Cargo M3 ─────────────────────────────────────────────────────────

@Composable
private fun CargoAdminCard(
    cargo:    Cargo,
    onEditar: () -> Unit,
    onToggle: () -> Unit
) {
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
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape    = RoundedCornerShape(12.dp),
                color    = if (cargo.estado)
                    MaterialTheme.colorScheme.primaryContainer
                else
                    MaterialTheme.colorScheme.surfaceContainerHigh,
                modifier = Modifier.size(46.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector        = Icons.Default.Badge,
                        contentDescription = null,
                        modifier           = Modifier.size(22.dp),
                        tint               = if (cargo.estado)
                            MaterialTheme.colorScheme.onPrimaryContainer
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text       = cargo.nombre,
                    style      = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color      = MaterialTheme.colorScheme.onSurface
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier          = Modifier.padding(top = 2.dp)
                ) {
                    Icon(
                        Icons.Default.Category, null,
                        modifier = Modifier.size(12.dp),
                        tint     = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text  = "Gestión de Personal",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.width(8.dp))

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Surface(
                        shape    = CircleShape,
                        color    = if (cargo.estado)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(8.dp)
                    ) {}
                    Text(
                        text  = if (cargo.estado) "Activo" else "Inactivo",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (cargo.estado)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(
                        onClick  = onEditar,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            Icons.Default.Edit, "Editar",
                            modifier = Modifier.size(18.dp),
                            tint     = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Switch(
                        checked         = cargo.estado,
                        onCheckedChange = { onToggle() },
                        modifier        = Modifier.scale(0.8f)
                    )
                }
            }
        }
    }
}

// ─── Diálogo Crear / Editar M3 ─────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CargoDialog(
    cargo:     Cargo?,
    guardando: Boolean,
    onGuardar: (CargoRequest) -> Unit,
    onDismiss: () -> Unit
) {
    var nombre by remember(cargo) { mutableStateOf(cargo?.nombre ?: "") }
    val esValido = nombre.isNotBlank()

    AlertDialog(
        onDismissRequest = { if (!guardando) onDismiss() },
        shape            = RoundedCornerShape(28.dp),
        containerColor   = MaterialTheme.colorScheme.surfaceContainerHigh,
        icon = {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Icon(
                    imageVector        = if (cargo != null) Icons.Default.Edit else Icons.Default.Add,
                    contentDescription = null,
                    tint               = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier           = Modifier
                        .padding(12.dp)
                        .size(24.dp)
                )
            }
        },
        title = {
            Text(
                text       = if (cargo != null) "Editar cargo" else "Nuevo cargo",
                fontWeight = FontWeight.Bold,
                style      = MaterialTheme.typography.titleMedium,
                textAlign  = TextAlign.Center,
                modifier   = Modifier.fillMaxWidth()
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier            = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value         = nombre,
                    onValueChange = { nombre = it },
                    label         = { Text("Nombre del cargo *") },
                    leadingIcon   = { Icon(Icons.Default.Badge, null, tint = MaterialTheme.colorScheme.primary) },
                    modifier      = Modifier.fillMaxWidth(),
                    singleLine    = true,
                    shape         = RoundedCornerShape(14.dp),
                    colors        = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor   = MaterialTheme.colorScheme.surfaceContainerLow,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    ),
                    enabled = !guardando
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onGuardar(CargoRequest(nombre = nombre.trim()))
                },
                enabled  = esValido && !guardando,
                shape    = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (guardando) {
                    CircularProgressIndicator(
                        modifier    = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color       = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Icon(Icons.Default.Save, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Guardar", fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick  = onDismiss,
                enabled  = !guardando,
                shape    = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) { Text("Cancelar") }
        }
    )
}
