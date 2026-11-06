package com.spa.appointments.ui.admin.catalogos

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.spa.appointments.domain.model.CategoriaAdmin
import com.spa.appointments.domain.model.ServicioAdmin
import com.spa.appointments.domain.model.ServicioRequest

// ─── Screen principal ──────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiciosAdminScreen(
    onBack: () -> Unit,
    viewModel: CatalogosViewModel = hiltViewModel()
) {
    val servicios  by viewModel.servicios.collectAsState()
    val categorias by viewModel.categorias.collectAsState()
    val uiState    by viewModel.uiState.collectAsState()

    var showDialog by remember { mutableStateOf(false) }
    var editando   by remember { mutableStateOf<ServicioAdmin?>(null) }
    var busqueda   by remember { mutableStateOf("") }

    var showPreciosRangosDialog by remember { mutableStateOf(false) }
    var servicioRangoSeleccionado by remember { mutableStateOf<ServicioAdmin?>(null) }

    val filtrados = remember(servicios, busqueda) {
        if (busqueda.isBlank()) servicios
        else servicios.filter {
            it.nombre.contains(busqueda, ignoreCase = true) ||
                    it.nombreCategoria.contains(busqueda, ignoreCase = true) ||
                    (busqueda.contains("grupal", ignoreCase = true) && it.esGrupal)
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
            // ── Buscador ───────────────────────────────────────────────────
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
                                    modifier           = Modifier
                                        .padding(20.dp)
                                        .size(48.dp),
                                    tint               = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text       = if (busqueda.isBlank()) "Sin servicios aún" else "Sin resultados",
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
                            contentPadding = PaddingValues(
                                start  = 16.dp,
                                top    = 4.dp,
                                end    = 16.dp,
                                bottom = 88.dp
                            ),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(filtrados, key = { it.id }) { srv ->
                                ServicioAdminCard(
                                    servicio = srv,
                                    onEditar = { editando = srv; showDialog = true },
                                    onToggle = { viewModel.toggleServicio(srv.id) },
                                    onVerPreciosRangos = {
                                        servicioRangoSeleccionado = srv
                                        viewModel.cargarPreciosRangos(srv.id)
                                        showPreciosRangosDialog = true
                                    }
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

    if (showPreciosRangosDialog && servicioRangoSeleccionado != null) {
        val preciosRangos by viewModel.preciosRangos.collectAsState()
        PreciosPersonasDialog(
            servicio = servicioRangoSeleccionado!!,
            preciosRangos = preciosRangos,
            onGuardarRango = { cant, prec ->
                viewModel.guardarPrecioRango(servicioRangoSeleccionado!!.id, cant, prec)
            },
            onEliminarRango = { idRango ->
                viewModel.eliminarPrecioRango(servicioRangoSeleccionado!!.id, idRango)
            },
            onDismiss = {
                showPreciosRangosDialog = false
                servicioRangoSeleccionado = null
            }
        )
    }
}

// ─── Card de servicio ──────────────────────────────────────────────────────────

@Composable
private fun ServicioAdminCard(
    servicio: ServicioAdmin,
    onEditar: () -> Unit,
    onToggle: () -> Unit,
    onVerPreciosRangos: () -> Unit
) {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors    = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // ── Fila superior: Ícono, Título, Categoría y Switch de estado ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (servicio.esGrupal)
                        MaterialTheme.colorScheme.tertiaryContainer
                    else
                        MaterialTheme.colorScheme.secondaryContainer,
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (servicio.esGrupal) Icons.Default.Groups else Icons.Default.Spa,
                            contentDescription = null,
                            modifier = Modifier.size(22.dp),
                            tint = if (servicio.esGrupal)
                                MaterialTheme.colorScheme.onTertiaryContainer
                            else
                                MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }

                Spacer(Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = servicio.nombre,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Category,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = servicio.nombreCategoria,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(Modifier.width(8.dp))

                Switch(
                    checked = servicio.estado,
                    onCheckedChange = { onToggle() },
                    modifier = Modifier.scale(0.85f)
                )
            }

            HorizontalDivider(
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
            )

            // ── Fila media: Badges de Duración, Precio, Grupal y Estado Activo/Inactivo ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Duración
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = null,
                                modifier = Modifier.size(13.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${servicio.duracionMinutos} min",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Precio Base
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AttachMoney,
                                contentDescription = null,
                                modifier = Modifier.size(13.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "%.0f".format(servicio.precioBase),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    // Badge Grupal
                    if (servicio.esGrupal) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.tertiaryContainer
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Groups,
                                    contentDescription = null,
                                    modifier = Modifier.size(13.dp),
                                    tint = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                                Text(
                                    text = "Grupal",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                            }
                        }
                    }
                }

                // Estado (Pill Activo / Inactivo)
                Surface(
                    shape = RoundedCornerShape(50),
                    color = if (servicio.estado)
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                    else
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(
                                    color = if (servicio.estado)
                                        MaterialTheme.colorScheme.primary
                                    else
                                        MaterialTheme.colorScheme.outline,
                                    shape = RoundedCornerShape(50)
                                )
                        )
                        Text(
                            text = if (servicio.estado) "Activo" else "Inactivo",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            color = if (servicio.estado)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // ── Fila inferior: Botones de Acción ──────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (servicio.esGrupal) {
                    TextButton(
                        onClick = onVerPreciosRangos,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Groups,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.tertiary
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = "Tarifas por persona",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.tertiary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                }

                OutlinedButton(
                    onClick = onEditar,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "Editar",
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }
    }
}

// ─── Diálogo crear / editar ────────────────────────────────────────────────────

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
    var esGrupal    by remember(servicio) { mutableStateOf(servicio?.esGrupal ?: false) }

    var iconoSeleccionado by remember(servicio) {
        mutableStateOf(ICONOS_DISPONIBLES.firstOrNull { it.clave == servicio?.icono })
    }

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
        icon = {
            Surface(
                shape = RoundedCornerShape(50),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Icon(
                    imageVector        = if (servicio != null) Icons.Default.Edit else Icons.Default.Add,
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
                        value           = duracion,
                        onValueChange   = { duracion = it },
                        label           = { Text("Min *") },
                        leadingIcon     = { Icon(Icons.Default.Timer, null, modifier = Modifier.size(18.dp)) },
                        modifier        = Modifier.weight(1f),
                        singleLine      = true,
                        shape           = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    OutlinedTextField(
                        value           = precio,
                        onValueChange   = { precio = it },
                        label           = { Text("Precio *") },
                        leadingIcon     = { Icon(Icons.Default.AttachMoney, null, modifier = Modifier.size(18.dp)) },
                        modifier        = Modifier.weight(1f),
                        singleLine      = true,
                        shape           = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 2.dp))

                // Toggle grupal
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (esGrupal)
                                MaterialTheme.colorScheme.tertiaryContainer
                            else
                                MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector        = Icons.Default.Groups,
                                    contentDescription = null,
                                    modifier           = Modifier.size(20.dp),
                                    tint               = if (esGrupal)
                                        MaterialTheme.colorScheme.onTertiaryContainer
                                    else
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Column {
                            Text(
                                text       = "Modalidad grupal",
                                style      = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text  = "Múltiples clientes simultáneos",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Switch(
                        checked         = esGrupal,
                        onCheckedChange = { esGrupal = it },
                        modifier        = Modifier.scale(0.85f)
                    )
                }

                // Hint informativo cuando está activo grupal
                AnimatedVisibility(
                    visible = esGrupal,
                    enter   = expandVertically(),
                    exit    = shrinkVertically()
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
                    ) {
                        Row(
                            modifier              = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment     = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector        = Icons.Default.Info,
                                contentDescription = null,
                                modifier           = Modifier.size(14.dp),
                                tint               = MaterialTheme.colorScheme.tertiary
                            )
                            Text(
                                text  = "Los datos del responsable se solicitarán al momento de crear la cita.",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                    }
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
                            icono           = iconoSeleccionado?.clave,
                            esGrupal        = esGrupal
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
}