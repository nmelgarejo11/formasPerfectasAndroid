package com.spa.appointments.ui.admin.usuario

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.spa.appointments.domain.model.Cargo

// Representación en caso de que manejes una estructura de datos limpia para requests
// Si no la tienes, puedes mutar este parámetro directamente a String en los callbacks.
data class CargoRequest(val nombre: String)

// ─── Screen principal ──────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CargosScreen(
    onVolver: () -> Unit,
    viewModel: AdministracionViewModel = hiltViewModel()
) {
    // Vinculación directa con tu patrón de nombres del AdministracionUiState
    val state = viewModel.uiState

    var showDialog by remember { mutableStateOf(false) }
    var editando   by remember { mutableStateOf<Cargo?>(null) } // Reemplazar 'Cargo' por tu modelo real
    var busqueda   by remember { mutableStateOf("") }

    // Filtrado reactivo en memoria idéntico a Servicios
    val filtrados = remember(state.cargos, busqueda) {
        if (busqueda.isBlank()) state.cargos
        else state.cargos.filter {
            it.nombre.contains(busqueda, ignoreCase = true)
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }

    // Mostrar errores o cierres de diálogos basados en los cambios de estado
    LaunchedEffect(state.error) {
        state.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.limpiarError()
        }
    }

    // Simulación de control de éxito para cerrar diálogo si tu state lo soporta.
    // Si manejas un flag en tu arquitectura, puedes mapearlo aquí de la misma forma que en catálogos.
    LaunchedEffect(state.cargando) {
        if (!state.cargando && !showDialog) {
            editando = null
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text       = "Cargos",
                            style      = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        if (state.cargos.isNotEmpty()) {
                            Text(
                                text  = "${state.cargos.size} ${if (state.cargos.size == 1) "cargo" else "cargos"}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { editando = null; showDialog = true },
                icon    = { Icon(Icons.Default.Add, null) },
                text    = { Text("Nuevo cargo") }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // ── Buscador ───────────────────────────────────────────────────
            OutlinedTextField(
                value         = busqueda,
                onValueChange = { busqueda = it },
                label         = { Text("Buscar cargo") },
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
                    state.cargando && state.cargos.isEmpty() -> {
                        Column(
                            modifier            = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            CircularProgressIndicator()
                            Text(
                                "Cargando cargos…",
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
                                    imageVector        = if (busqueda.isBlank()) Icons.Default.WorkOutline
                                    else Icons.Default.SearchOff,
                                    contentDescription = null,
                                    modifier           = Modifier
                                        .padding(20.dp)
                                        .size(48.dp),
                                    tint               = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text       = if (busqueda.isBlank()) "Sin cargos aún" else "Sin resultados",
                                style      = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
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

                    else -> {
                        LazyColumn(
                            contentPadding = PaddingValues(
                                start  = 16.dp,
                                top    = 4.dp,
                                end    = 16.dp,
                                bottom = 88.dp
                            ),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
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
                    // Si tu viewmodel requiere ID para actualizar, adáptalo aquí:
                    // viewModel.actualizarCargo(editando.id, req.nombre)
                } else {
                    viewModel.crearCargo(req.nombre)
                }
                showDialog = false
            },
            onDismiss  = { showDialog = false; editando = null }
        )
    }
}

// ─── Card de cargo ─────────────────────────────────────────────────────────────

@Composable
private fun CargoAdminCard(
    cargo: Cargo, // Reemplazar por tu modelo de datos real
    onEditar: () -> Unit,
    onToggle: () -> Unit
) {
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
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Contenedor del ícono izquierdo personalizado
            Surface(
                shape    = RoundedCornerShape(10.dp),
                color    = if (cargo.estado)
                    MaterialTheme.colorScheme.primaryContainer
                else
                    MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.size(44.dp)
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

            // Información del Cargo
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text       = cargo.nombre,
                    style      = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier          = Modifier.padding(top = 4.dp)
                ) {
                    Icon(
                        Icons.Default.Category, null,
                        modifier = Modifier.size(11.dp),
                        tint     = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.width(3.dp))
                    Text(
                        text  = "Gestión de Personal",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.width(8.dp))

            // Indicadores y acciones del costado derecho
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Punto de color dinámico para reflejar estado activo
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Surface(
                        shape  = RoundedCornerShape(50),
                        color  = if (cargo.estado)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(7.dp)
                    ) {}
                    Text(
                        text  = if (cargo.estado) "Activo" else "Inactivo",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (cargo.estado)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(
                    verticalAlignment      = Alignment.CenterVertically,
                    horizontalArrangement  = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(
                        onClick  = onEditar,
                        modifier = Modifier.size(32.dp)
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

// ─── Diálogo crear / editar ────────────────────────────────────────────────────

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
        shape = RoundedCornerShape(16.dp),
        icon = {
            Surface(
                shape = RoundedCornerShape(50),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Icon(
                    imageVector        = if (cargo != null) Icons.Default.Edit else Icons.Default.Add,
                    contentDescription = null,
                    tint               = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier           = Modifier
                        .padding(10.dp)
                        .size(22.dp)
                )
            }
        },
        title = {
            Text(
                text       = if (cargo != null) "Editar cargo" else "Nuevo cargo",
                fontWeight = FontWeight.Bold
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
                    leadingIcon   = { Icon(Icons.Default.Badge, null) },
                    modifier      = Modifier.fillMaxWidth(),
                    singleLine    = true,
                    shape         = RoundedCornerShape(12.dp),
                    enabled       = !guardando
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onGuardar(CargoRequest(nombre = nombre.trim()))
                },
                enabled = esValido && !guardando,
                shape   = RoundedCornerShape(10.dp)
            ) {
                if (guardando) {
                    CircularProgressIndicator(
                        modifier    = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color       = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Icon(Icons.Default.Save, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Guardar")
                }
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick  = onDismiss,
                enabled  = !guardando,
                shape    = RoundedCornerShape(10.dp)
            ) { Text("Cancelar") }
        }
    )
}