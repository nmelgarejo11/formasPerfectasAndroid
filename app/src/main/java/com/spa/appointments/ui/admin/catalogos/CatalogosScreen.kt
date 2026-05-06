package com.spa.appointments.ui.admin.catalogos

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.spa.appointments.domain.model.CategoriaAdmin
import com.spa.appointments.domain.model.ServicioAdmin
import com.spa.appointments.domain.model.ServicioRequest

// ─── Screen principal ─────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiciosAdminScreen(
    onBack:    () -> Unit,
    viewModel: CatalogosViewModel = hiltViewModel()
) {
    val servicios  by viewModel.servicios.collectAsState()
    val categorias by viewModel.categorias.collectAsState()
    val uiState    by viewModel.uiState.collectAsState()

    var showDialog by remember { mutableStateOf(false) }
    var editando   by remember { mutableStateOf<ServicioAdmin?>(null) }
    var busqueda   by remember { mutableStateOf("") }

    val filtrados = remember(servicios, busqueda) {
        if (busqueda.isBlank()) servicios
        else servicios.filter {
            it.nombre.contains(busqueda, ignoreCase = true) ||
                    it.nombreCategoria.contains(busqueda, ignoreCase = true)
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.cargarServicios()
        viewModel.cargarCategorias()
    }

    LaunchedEffect(uiState) {
        when (val s = uiState) {
            is CatalogosUiState.Success -> {
                showDialog = false
                editando   = null
                viewModel.resetState()
            }
            is CatalogosUiState.Error -> {
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
                            text       = "Servicios",
                            style      = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        if (servicios.isNotEmpty()) {
                            Text(
                                text  = "${servicios.size} ${if (servicios.size == 1) "servicio" else "servicios"}",
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
                text    = { Text("Nuevo servicio") }
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
                label         = { Text("Buscar servicio o categoría") },
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
                    uiState is CatalogosUiState.Loading && servicios.isEmpty() -> {
                        Column(
                            modifier            = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            CircularProgressIndicator()
                            Text(
                                "Cargando servicios…",
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
                                    imageVector        = if (busqueda.isBlank()) Icons.Default.Spa
                                    else Icons.Default.SearchOff,
                                    contentDescription = null,
                                    modifier           = Modifier.padding(20.dp).size(48.dp),
                                    tint               = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text       = if (busqueda.isBlank()) "Sin servicios aún"
                                else "Sin resultados",
                                style      = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text      = if (busqueda.isBlank())
                                    "Crea el primer servicio con el botón inferior."
                                else
                                    "Ningún servicio coincide con \"$busqueda\".",
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
                                bottom = 88.dp   // espacio para el FAB extendido
                            ),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(filtrados, key = { it.id }) { srv ->
                                ServicioAdminCard(
                                    servicio = srv,
                                    onEditar = { editando = srv; showDialog = true },
                                    onToggle = { viewModel.toggleServicio(srv.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDialog) {
        ServicioDialog(
            servicio   = editando,
            categorias = categorias,
            guardando  = uiState is CatalogosUiState.Loading,
            onGuardar  = { req -> viewModel.guardarServicio(editando?.id, req) },
            onDismiss  = { showDialog = false; editando = null; viewModel.resetState() }
        )
    }
}

// ─── Card de servicio ─────────────────────────────────────────────────────────

@Composable
private fun ServicioAdminCard(
    servicio: ServicioAdmin,
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
            // Ícono de servicio
            Surface(
                shape    = RoundedCornerShape(10.dp),
                color    = MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector        = Icons.Default.Spa,
                        contentDescription = null,
                        modifier           = Modifier.size(22.dp),
                        tint               = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            Spacer(Modifier.width(12.dp))

            // Información del servicio
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text       = servicio.nombre,
                    style      = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier   = Modifier.fillMaxWidth()
                )

                // Categoría
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier          = Modifier.padding(top = 2.dp)
                ) {
                    Icon(
                        Icons.Default.Category, null,
                        modifier = Modifier.size(11.dp),
                        tint     = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.width(3.dp))
                    Text(
                        text  = servicio.nombreCategoria,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Duración y precio
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier              = Modifier.padding(top = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Timer, null,
                            modifier = Modifier.size(11.dp),
                            tint     = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.width(3.dp))
                        Text(
                            text  = "${servicio.duracionMinutos} min",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.AttachMoney, null,
                            modifier = Modifier.size(11.dp),
                            tint     = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text       = "%.0f".format(servicio.precioBase),
                            style      = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color      = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(Modifier.width(8.dp))

            // ─── ACCIONES AJUSTADAS (Pendiente #1) ────────────────────────
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // 1. Indicador de activo encima del toggle
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (servicio.estado)
                        MaterialTheme.colorScheme.primaryContainer
                    else
                        MaterialTheme.colorScheme.surfaceVariant
                ) {

                    Text(
                        text = if (servicio.estado) "Activa" else "Inactiva",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (servicio.estado)
                            MaterialTheme.colorScheme.onPrimaryContainer
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // 2. Lápiz junto al toggle
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
                        checked          = servicio.estado,
                        onCheckedChange  = { onToggle() },
                        modifier         = Modifier.scale(0.8f) // Escala para ajustar mejor visualmente al lado del icono
                    )
                }
            }
        }
    }
}

// ─── Diálogo crear / editar servicio ─────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ServicioDialog(
    servicio:   ServicioAdmin?,
    categorias: List<CategoriaAdmin>,
    guardando:  Boolean,
    onGuardar:  (ServicioRequest) -> Unit,
    onDismiss:  () -> Unit
) {
    var nombre      by remember(servicio) { mutableStateOf(servicio?.nombre ?: "") }
    var descripcion by remember(servicio) { mutableStateOf(servicio?.descripcion ?: "") }
    var duracion    by remember(servicio) { mutableStateOf(servicio?.duracionMinutos?.toString() ?: "") }
    var precio      by remember(servicio) { mutableStateOf(servicio?.precioBase?.toString() ?: "") }

    var iconoSeleccionado by remember(servicio) {
        mutableStateOf(ICONOS_DISPONIBLES.firstOrNull { it.clave == servicio?.icono })
    }
    var showIconoPicker by remember { mutableStateOf(false) }

    val categoriaInicial      = categorias.firstOrNull { it.id == servicio?.idCategoria }
    var categoriaSeleccionada by remember(servicio) { mutableStateOf(categoriaInicial) }
    var expandedCat           by remember { mutableStateOf(false) }

    val esValido = nombre.isNotBlank()
            && duracion.toIntOrNull() != null
            && precio.toDoubleOrNull() != null
            && categoriaSeleccionada != null

    AlertDialog(
        onDismissRequest = { if (!guardando) onDismiss() },
        shape = RoundedCornerShape(16.dp),
        icon  = {
            Surface(
                shape = RoundedCornerShape(50),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Icon(
                    imageVector        = if (servicio != null) Icons.Default.Edit else Icons.Default.Add,
                    contentDescription = null,
                    tint               = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier           = Modifier.padding(10.dp).size(22.dp)
                )
            }
        },
        title = {
            Text(
                text       = if (servicio != null) "Editar servicio" else "Nuevo servicio",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier            = Modifier.fillMaxWidth()
            ) {
                // Dropdown categoría
                ExposedDropdownMenuBox(
                    expanded         = expandedCat,
                    onExpandedChange = { expandedCat = it }
                ) {
                    OutlinedTextField(
                        value         = categoriaSeleccionada?.nombre ?: "Selecciona categoría",
                        onValueChange = {},
                        readOnly      = true,
                        label         = { Text("Categoría *") },
                        trailingIcon  = { ExposedDropdownMenuDefaults.TrailingIcon(expandedCat) },
                        shape         = RoundedCornerShape(12.dp),
                        modifier      = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded         = expandedCat,
                        onDismissRequest = { expandedCat = false }
                    ) {
                        categorias.filter { it.estado }.forEach { cat ->
                            DropdownMenuItem(
                                text    = { Text(cat.nombre) },
                                onClick = { categoriaSeleccionada = cat; expandedCat = false }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value         = nombre,
                    onValueChange = { nombre = it },
                    label         = { Text("Nombre *") },
                    leadingIcon   = { Icon(Icons.Default.Spa, null) },
                    modifier      = Modifier.fillMaxWidth(),
                    singleLine    = true,
                    shape         = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value         = descripcion,
                    onValueChange = { descripcion = it },
                    label         = { Text("Descripción") },
                    leadingIcon   = { Icon(Icons.Default.Notes, null) },
                    modifier      = Modifier.fillMaxWidth(),
                    maxLines      = 2,
                    shape         = RoundedCornerShape(12.dp)
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value         = duracion,
                        onValueChange = { duracion = it },
                        label         = { Text("Min *") },
                        leadingIcon   = { Icon(Icons.Default.Timer, null, modifier = Modifier.size(18.dp)) },
                        modifier      = Modifier.weight(1f),
                        singleLine    = true,
                        shape         = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    OutlinedTextField(
                        value         = precio,
                        onValueChange = { precio = it },
                        label         = { Text("Precio *") },
                        leadingIcon   = { Icon(Icons.Default.AttachMoney, null, modifier = Modifier.size(18.dp)) },
                        modifier      = Modifier.weight(1f),
                        singleLine    = true,
                        shape         = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onGuardar(
                        ServicioRequest(
                            idCategoria     = categoriaSeleccionada!!.id,
                            nombre          = nombre.trim(),
                            descripcion     = descripcion.trim().ifBlank { null },
                            duracionMinutos = duracion.toInt(),
                            precioBase      = precio.toDouble(),
                            icono           = iconoSeleccionado?.clave
                        )
                    )
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

    if (showIconoPicker) {
        IconoPickerDialog(
            iconoActual   = iconoSeleccionado?.clave,
            onSeleccionar = { opcion -> iconoSeleccionado = opcion; showIconoPicker = false },
            onDismiss     = { showIconoPicker = false }
        )
    }
}