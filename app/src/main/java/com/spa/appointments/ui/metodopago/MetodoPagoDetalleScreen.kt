package com.spa.appointments.ui.metodopago

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.spa.appointments.domain.model.MetodoPagoDetalleAdmin

// ─── Screen principal ─────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MetodoPagoDetalleScreen(
    metodoId: Int,
    metodoNombre: String,
    onBack: () -> Unit,
    viewModel: MetodoPagoViewModel = hiltViewModel()
) {
    val detalles by viewModel.detalles.collectAsStateWithLifecycle()
    val loading  by viewModel.loading.collectAsStateWithLifecycle()
    val error    by viewModel.error.collectAsStateWithLifecycle()

    var showDialog   by remember { mutableStateOf(false) }
    var editando     by remember { mutableStateOf<MetodoPagoDetalleAdmin?>(null) }
    var busqueda     by remember { mutableStateOf("") }

    val filtrados = remember(detalles, busqueda) {
        if (busqueda.isBlank()) detalles
        else detalles.filter { it.nombre.contains(busqueda, ignoreCase = true) }
    }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(metodoId) { viewModel.cargarDetalles(metodoId) }

    LaunchedEffect(error) {
        error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.limpiarError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text       = metodoNombre,
                            style      = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        if (detalles.isNotEmpty()) {
                            Text(
                                text  = "${detalles.size} ${if (detalles.size == 1) "detalle" else "detalles"}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { editando = null; showDialog = true },
                icon    = { Icon(Icons.Default.Add, null) },
                text    = { Text("Nuevo detalle") }
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
                label         = { Text("Buscar detalle") },
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
                    // Cargando sin datos
                    loading && detalles.isEmpty() -> {
                        Column(
                            modifier            = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            CircularProgressIndicator()
                            Text(
                                "Cargando detalles…",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Lista vacía o sin resultados
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
                                    imageVector        = if (busqueda.isBlank()) Icons.Default.ListAlt
                                    else Icons.Default.SearchOff,
                                    contentDescription = null,
                                    modifier           = Modifier
                                        .padding(20.dp)
                                        .size(48.dp),
                                    tint               = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text       = if (busqueda.isBlank()) "Sin detalles aún"
                                else "Sin resultados",
                                style      = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text      = if (busqueda.isBlank())
                                    "Agrega el primer detalle con el botón inferior."
                                else
                                    "Ningún detalle coincide con \"$busqueda\".",
                                style     = MaterialTheme.typography.bodyMedium,
                                color     = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    // Lista con datos
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
                            items(filtrados, key = { it.id }) { detalle ->
                                DetalleCard(
                                    detalle  = detalle,
                                    onEditar = { editando = detalle; showDialog = true }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDialog) {
        DetalleDialog(
            detalle   = editando,
            guardando = loading,
            onGuardar = { nombre, activo ->
                if (editando == null) viewModel.crearDetalle(metodoId, nombre)
                else viewModel.actualizarDetalle(metodoId, editando!!.id, nombre, activo)
                showDialog = false
                editando   = null
            },
            onDismiss = { showDialog = false; editando = null }
        )
    }
}

// ─── Card de detalle ──────────────────────────────────────────────────────────

@Composable
private fun DetalleCard(
    detalle: MetodoPagoDetalleAdmin,
    onEditar: () -> Unit
) {
    Card(
        onClick   = onEditar,                           // ← tap en toda la card
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
            // Ícono
            Surface(
                shape    = RoundedCornerShape(10.dp),
                color    = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector        = Icons.Default.Receipt,
                        contentDescription = null,
                        modifier           = Modifier.size(22.dp),
                        tint               = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Text(
                        text       = detalle.nombre,
                        style      = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier   = Modifier.weight(1f)
                    )
                    // Chip de estado
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (detalle.activo)
                            MaterialTheme.colorScheme.primaryContainer
                        else
                            MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text     = if (detalle.activo) "Activo" else "Inactivo",
                            style    = MaterialTheme.typography.labelSmall,
                            color    = if (detalle.activo)
                                MaterialTheme.colorScheme.onPrimaryContainer
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

// ─── Diálogo crear / editar ───────────────────────────────────────────────────

@Composable
private fun DetalleDialog(
    detalle: MetodoPagoDetalleAdmin?,
    guardando: Boolean,
    onGuardar: (nombre: String, activo: Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    var nombre by remember(detalle) { mutableStateOf(detalle?.nombre ?: "") }
    var activo by remember(detalle) { mutableStateOf(detalle?.activo ?: true) }
    val esNuevo = detalle == null

    AlertDialog(
        onDismissRequest = { if (!guardando) onDismiss() },
        shape = RoundedCornerShape(16.dp),
        icon  = {
            Surface(
                shape = RoundedCornerShape(50),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Icon(
                    imageVector        = if (esNuevo) Icons.Default.Add else Icons.Default.Edit,
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
                text       = if (esNuevo) "Nuevo detalle" else "Editar detalle",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value         = nombre,
                    onValueChange = { nombre = it },
                    label         = { Text("Nombre *") },
                    leadingIcon   = { Icon(Icons.Default.Receipt, null) },
                    modifier      = Modifier.fillMaxWidth(),
                    singleLine    = true,
                    shape         = RoundedCornerShape(12.dp)
                )

                // Toggle estado (solo al editar)
                if (!esNuevo) {
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "Estado",
                                style      = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                if (activo) "Visible en reservas" else "Oculto en reservas",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked         = activo,
                            onCheckedChange = { activo = it }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick  = { onGuardar(nombre.trim(), activo) },
                enabled  = nombre.isNotBlank() && !guardando,
                shape    = RoundedCornerShape(10.dp)
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