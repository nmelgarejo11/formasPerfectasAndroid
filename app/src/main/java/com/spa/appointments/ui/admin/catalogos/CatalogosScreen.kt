// Ruta: app/src/main/java/com/spa/appointments/ui/admin/catalogos/CatalogosScreen.kt
package com.spa.appointments.ui.admin.catalogos

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.spa.appointments.domain.model.CategoriaAdmin
import com.spa.appointments.domain.model.ServicioAdmin
import com.spa.appointments.domain.model.ServicioRequest
import com.spa.appointments.ui.components.SinPermisoContent

// ─── Screen Principal ─────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiciosAdminScreen(
    onBack: () -> Unit,
    viewModel: CatalogosViewModel = hiltViewModel()
) {
    val servicios  by viewModel.servicios.collectAsState()
    val categorias by viewModel.categorias.collectAsState()
    val uiState    by viewModel.uiState.collectAsState()

    var showDialog   by remember { mutableStateOf(false) }
    var editando     by remember { mutableStateOf<ServicioAdmin?>(null) }
    var busqueda     by remember { mutableStateOf("") }
    var idCatFiltro  by remember { mutableStateOf<Int?>(null) }

    var showDialogRangos    by remember { mutableStateOf(false) }
    var servicioParaRangos  by remember { mutableStateOf<ServicioAdmin?>(null) }

    val filtrados = remember(servicios, busqueda, idCatFiltro) {
        servicios.filter { s ->
            (idCatFiltro == null || s.idCategoria == idCatFiltro) &&
                    (busqueda.isBlank() || s.nombre.contains(busqueda, ignoreCase = true))
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.cargarServicios()
        viewModel.cargarCategorias()
    }

    LaunchedEffect(uiState) {
        when (uiState) {
            is CatalogosUiState.Error -> {
                snackbarHostState.showSnackbar((uiState as CatalogosUiState.Error).mensaje)
                viewModel.resetState()
            }
            is CatalogosUiState.Success -> {
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
                                text       = "Servicios",
                                style      = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            if (servicios.isNotEmpty() && uiState !is CatalogosUiState.Forbidden) {
                                Surface(
                                    shape    = RoundedCornerShape(50),
                                    color    = MaterialTheme.colorScheme.primaryContainer,
                                    modifier = Modifier.padding(top = 2.dp)
                                ) {
                                    Text(
                                        text     = "${servicios.size} ${if (servicios.size == 1) "servicio" else "servicios"}",
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
            if (uiState !is CatalogosUiState.Forbidden) {
                ExtendedFloatingActionButton(
                    onClick        = { editando = null; showDialog = true },
                    icon           = { Icon(Icons.Default.Add, null) },
                    text           = { Text("Nuevo servicio", fontWeight = FontWeight.Bold) },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor   = MaterialTheme.colorScheme.onPrimary,
                    shape          = RoundedCornerShape(16.dp)
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->

        if (uiState is CatalogosUiState.Forbidden) {
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

                // ── Buscador M3 ───────────────────────────────────────────────
                OutlinedTextField(
                    value         = busqueda,
                    onValueChange = { busqueda = it },
                    label         = { Text("Buscar servicio") },
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
                    enabled    = uiState !is CatalogosUiState.Loading
                )

                // ── Filtro por Categoría M3 ─────────────────────────────────────
                if (categorias.isNotEmpty()) {
                    var expCategory by remember { mutableStateOf(false) }

                    Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                        OutlinedCard(
                            onClick = { expCategory = true },
                            shape   = RoundedCornerShape(12.dp),
                            colors  = CardDefaults.outlinedCardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                            ),
                            border  = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Row(
                                modifier          = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = if (idCatFiltro == null) "Todas las categorías"
                                    else categorias.firstOrNull { it.id == idCatFiltro }?.nombre ?: "Categoría",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Icon(Icons.Default.ArrowDropDown, null)
                            }
                        }

                        DropdownMenu(
                            expanded         = expCategory,
                            onDismissRequest = { expCategory = false },
                            modifier         = Modifier.fillMaxWidth(0.9f)
                        ) {
                            DropdownMenuItem(
                                text    = { Text("Todas las categorías", fontWeight = FontWeight.Bold) },
                                onClick = { idCatFiltro = null; expCategory = false }
                            )
                            categorias.forEach { cat ->
                                DropdownMenuItem(
                                    text    = { Text(cat.nombre) },
                                    onClick = { idCatFiltro = cat.id; expCategory = false }
                                )
                            }
                        }
                    }
                }

                Box(modifier = Modifier.fillMaxSize()) {
                    when {
                        uiState is CatalogosUiState.Loading && servicios.isEmpty() -> {
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
                                    text  = "Cargando servicios…",
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
                                                imageVector        = if (busqueda.isBlank()) Icons.Default.Spa
                                                else Icons.Default.SearchOff,
                                                contentDescription = null,
                                                modifier           = Modifier.padding(18.dp).size(36.dp),
                                                tint               = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                    Text(
                                        text       = if (busqueda.isBlank()) "Sin servicios aún" else "Sin resultados",
                                        style      = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color      = MaterialTheme.colorScheme.onSurface
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
                        }

                        else -> {
                            LazyColumn(
                                contentPadding      = PaddingValues(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 88.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(filtrados, key = { it.id }) { s ->
                                    ServicioAdminCard(
                                        servicio    = s,
                                        onEditar    = { editando = s; showDialog = true },
                                        onVerRangos = {
                                            servicioParaRangos = s
                                            showDialogRangos   = true
                                        },
                                        onToggle    = { viewModel.toggleServicio(s.id) }
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
        ServicioDialog(
            servicio   = editando,
            categorias = categorias,
            servicios  = servicios,
            guardando  = uiState is CatalogosUiState.Loading,
            onGuardar  = { id, req ->
                viewModel.guardarServicio(id, req)
            },
            onCopiarMensajeWhatsApp = { mensajeWhatsApp, idsDestino, onSuccess ->
                viewModel.copiarMensajeWhatsApp(mensajeWhatsApp, idsDestino, onSuccess)
            },
            onDismiss  = { showDialog = false; editando = null }
        )
    }

    if (showDialogRangos && servicioParaRangos != null) {
        val servicio = servicioParaRangos!!
        val preciosRangos by viewModel.preciosRangos.collectAsState()

        LaunchedEffect(servicio.id) {
            viewModel.cargarPreciosRangos(servicio.id)
        }

        PreciosPersonasDialog(
            servicio = servicio,
            preciosRangos = preciosRangos,
            onGuardarRango = { cant, precio, esPorPersona ->
                viewModel.guardarPrecioRango(servicio.id, cant, precio, esPorPersona)
            },
            onEliminarRango = { idRango ->
                viewModel.eliminarPrecioRango(servicio.id, idRango)
            },
            onDismiss = {
                showDialogRangos   = false
                servicioParaRangos = null
            }
        )
    }
}

// ─── Card Servicio M3 ──────────────────────────────────────────────────────────

@Composable
private fun ServicioAdminCard(
    servicio    : ServicioAdmin,
    onEditar    : () -> Unit,
    onVerRangos : () -> Unit,
    onToggle    : () -> Unit
) {
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
                Surface(
                    shape    = RoundedCornerShape(12.dp),
                    color    = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(46.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector        = Icons.Default.Spa,
                            contentDescription = null,
                            modifier           = Modifier.size(22.dp),
                            tint               = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                Spacer(Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text       = servicio.nombre,
                        style      = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color      = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text     = servicio.nombreCategoria,
                        style    = MaterialTheme.typography.bodySmall,
                        color    = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Switch(
                    checked         = servicio.estado,
                    onCheckedChange = { onToggle() },
                    modifier        = Modifier.scale(0.85f)
                )
            }

            if (!servicio.descripcion.isNullOrBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text     = servicio.descripcion,
                    style    = MaterialTheme.typography.bodySmall,
                    color    = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
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
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier          = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text  = "${servicio.duracionMinutos} min",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }

                    if (servicio.maxPersonas != null && servicio.maxPersonas > 1) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.tertiaryContainer
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier          = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Groups,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    text  = "Max ${servicio.maxPersonas}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                            }
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (servicio.maxPersonas != null && servicio.maxPersonas > 1) {
                        IconButton(
                            onClick  = onVerRangos,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector        = Icons.Default.FormatListNumbered,
                                contentDescription = "Precios por personas",
                                tint               = MaterialTheme.colorScheme.secondary,
                                modifier           = Modifier.size(20.dp)
                            )
                        }
                    }

                    IconButton(
                        onClick  = onEditar,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector        = Icons.Default.Edit,
                            contentDescription = "Editar",
                            tint               = MaterialTheme.colorScheme.primary,
                            modifier           = Modifier.size(20.dp)
                        )
                    }

                    Text(
                        text       = "$${"%,.0f".format(servicio.precioBase)}",
                        style      = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color      = MaterialTheme.colorScheme.primary,
                        modifier   = Modifier.padding(start = 6.dp)
                    )
                }
            }
        }
    }
}

// ─── Diálogo Servicio M3 ───────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ServicioDialog(
    servicio      : ServicioAdmin?,
    categorias    : List<CategoriaAdmin>,
    servicios     : List<ServicioAdmin>,
    guardando     : Boolean,
    onGuardar     : (id: Int?, req: ServicioRequest) -> Unit,
    onCopiarMensajeWhatsApp: (mensajeWhatsApp: String?, idsServiciosDestino: List<Int>, onSuccess: () -> Unit) -> Unit,
    onDismiss     : () -> Unit
) {
    var nombre           by remember(servicio) { mutableStateOf(servicio?.nombre ?: "") }
    var desc             by remember(servicio) { mutableStateOf(servicio?.descripcion ?: "") }
    var duracion         by remember(servicio) { mutableStateOf(servicio?.duracionMinutos?.toString() ?: "30") }
    var precio           by remember(servicio) { mutableStateOf(servicio?.precioBase?.toString() ?: "0") }
    var idCat            by remember(servicio) { mutableIntStateOf(servicio?.idCategoria ?: (categorias.firstOrNull()?.id ?: 0)) }
    var esGrupal         by remember(servicio) { mutableStateOf(servicio?.maxPersonas != null && servicio.maxPersonas > 1) }
    var maxPersonas      by remember(servicio) { mutableStateOf(servicio?.maxPersonas?.toString() ?: "2") }
    var mensajeWhatsApp  by remember(servicio) { mutableStateOf(TextFieldValue(servicio?.mensajeWhatsApp ?: "")) }

    var tabSeleccionada  by remember { mutableIntStateOf(0) }
    val idsSeleccionados = remember { mutableStateListOf<Int>() }
    var estaCopianMensaje by remember { mutableStateOf(false) }

    val esNuevo = servicio == null

    AlertDialog(
        onDismissRequest = { if (!guardando && !estaCopianMensaje) onDismiss() },
        shape            = RoundedCornerShape(28.dp),
        containerColor   = MaterialTheme.colorScheme.surfaceContainerHigh,
        icon = {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Icon(
                    imageVector        = if (esNuevo) Icons.Default.Add else Icons.Default.Edit,
                    contentDescription = null,
                    tint               = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier           = Modifier.padding(12.dp).size(24.dp)
                )
            }
        },
        title = {
            Text(
                text       = if (esNuevo) "Nuevo servicio" else "Editar servicio",
                style      = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign  = TextAlign.Center,
                modifier   = Modifier.fillMaxWidth()
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 480.dp)
            ) {
                if (!esNuevo) {
                    PrimaryTabRow(selectedTabIndex = tabSeleccionada) {
                        Tab(
                            selected = tabSeleccionada == 0,
                            onClick  = { tabSeleccionada = 0 },
                            text     = { Text("General", fontWeight = FontWeight.Bold) }
                        )
                        Tab(
                            selected = tabSeleccionada == 1,
                            onClick  = { tabSeleccionada = 1 },
                            text     = { Text("Replicar MSJ", fontWeight = FontWeight.Bold) }
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                }

                if (tabSeleccionada == 0) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value         = nombre,
                            onValueChange = { nombre = it },
                            label         = { Text("Nombre *") },
                            modifier      = Modifier.fillMaxWidth(),
                            singleLine    = true,
                            shape         = RoundedCornerShape(14.dp)
                        )

                        OutlinedTextField(
                            value         = desc,
                            onValueChange = { desc = it },
                            label         = { Text("Descripción") },
                            modifier      = Modifier.fillMaxWidth(),
                            minLines      = 2,
                            shape         = RoundedCornerShape(14.dp)
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier              = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value           = duracion,
                                onValueChange   = { duracion = it },
                                label           = { Text("Duración (min) *") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine      = true,
                                modifier        = Modifier.weight(1f),
                                shape           = RoundedCornerShape(14.dp)
                            )

                            OutlinedTextField(
                                value           = precio,
                                onValueChange   = { precio = it },
                                label           = { Text("Precio base *") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine      = true,
                                modifier        = Modifier.weight(1f),
                                shape           = RoundedCornerShape(14.dp)
                            )
                        }

                        if (categorias.isNotEmpty()) {
                            var expCat by remember { mutableStateOf(false) }
                            Box {
                                OutlinedCard(
                                    onClick = { expCat = true },
                                    shape   = RoundedCornerShape(14.dp),
                                    colors  = CardDefaults.outlinedCardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                                    )
                                ) {
                                    Row(
                                        modifier          = Modifier.fillMaxWidth().padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = categorias.firstOrNull { it.id == idCat }?.nombre ?: "Seleccionar categoría",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        Icon(Icons.Default.ArrowDropDown, null)
                                    }
                                }

                                DropdownMenu(
                                    expanded         = expCat,
                                    onDismissRequest = { expCat = false }
                                ) {
                                    categorias.forEach { cat ->
                                        DropdownMenuItem(
                                            text    = { Text(cat.nombre) },
                                            onClick = { idCat = cat.id; expCat = false }
                                        )
                                    }
                                }
                            }
                        }

                        Row(
                            verticalAlignment     = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier              = Modifier.fillMaxWidth()
                        ) {
                            Text("¿Permite citas grupales?", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                            Switch(checked = esGrupal, onCheckedChange = { esGrupal = it })
                        }

                        AnimatedVisibility(visible = esGrupal) {
                            OutlinedTextField(
                                value           = maxPersonas,
                                onValueChange   = { maxPersonas = it },
                                label           = { Text("Máximo de personas *") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine      = true,
                                modifier        = Modifier.fillMaxWidth(),
                                shape           = RoundedCornerShape(14.dp)
                            )
                        }

                        OutlinedTextField(
                            value         = mensajeWhatsApp,
                            onValueChange = { mensajeWhatsApp = it },
                            label         = { Text("Plantilla WhatsApp (opcional)") },
                            modifier      = Modifier.fillMaxWidth(),
                            minLines      = 2,
                            shape         = RoundedCornerShape(14.dp),
                            trailingIcon  = {
                                IconButton(
                                    onClick = {
                                        val c = mensajeWhatsApp.selection
                                        val curText = mensajeWhatsApp.text
                                        val ins = "{nombreCliente}"
                                        val newText = StringBuilder(curText).insert(c.start, ins).toString()
                                        val newPos = c.start + ins.length
                                        mensajeWhatsApp = TextFieldValue(text = newText, selection = TextRange(newPos))
                                    }
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = "Insertar variable")
                                }
                            }
                        )
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text  = "Selecciona los servicios que recibirán este mensaje de WhatsApp:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        val otrosServicios = servicios.filter { it.id != servicio?.id }
                        if (otrosServicios.isEmpty()) {
                            Text("No hay otros servicios para replicar.", style = MaterialTheme.typography.bodySmall)
                        } else {
                            otrosServicios.forEach { serv ->
                                val sel = idsSeleccionados.contains(serv.id)
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier          = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            if (sel) idsSeleccionados.remove(serv.id)
                                            else idsSeleccionados.add(serv.id)
                                        }
                                        .padding(vertical = 4.dp)
                                ) {
                                    Checkbox(
                                        checked         = sel,
                                        onCheckedChange = { checked ->
                                            if (checked) idsSeleccionados.add(serv.id)
                                            else idsSeleccionados.remove(serv.id)
                                        }
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(serv.nombre, style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        }

                        Spacer(Modifier.height(10.dp))
                        Button(
                            onClick = {
                                estaCopianMensaje = true
                                onCopiarMensajeWhatsApp(
                                    mensajeWhatsApp.text.ifBlank { null },
                                    idsSeleccionados.toList()
                                ) {
                                    estaCopianMensaje = false
                                }
                            },
                            enabled = idsSeleccionados.isNotEmpty() && !estaCopianMensaje,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            if (estaCopianMensaje) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                            } else {
                                Icon(Icons.Default.ContentCopy, null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Replicar mensaje", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (tabSeleccionada == 0) {
                Button(
                    onClick  = {
                        val durMin  = duracion.toIntOrNull() ?: 30
                        val precVal = precio.toDoubleOrNull() ?: 0.0
                        val maxP    = if (esGrupal) maxPersonas.toIntOrNull() else null

                        onGuardar(
                            servicio?.id,
                            ServicioRequest(
                                idCategoria      = idCat,
                                nombre           = nombre.trim(),
                                descripcion      = desc.ifBlank { null },
                                duracionMinutos  = durMin,
                                precioBase       = precVal,
                                icono            = null,
                                esGrupal         = esGrupal,
                                maxPersonas      = maxP,
                                mensajeWhatsApp  = mensajeWhatsApp.text.ifBlank { null }
                            )
                        )
                    },
                    enabled  = nombre.isNotBlank() && idCat > 0 && !guardando,
                    shape    = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (guardando) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                    } else {
                        Icon(Icons.Default.Save, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Guardar", fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick  = onDismiss,
                enabled  = !guardando && !estaCopianMensaje,
                shape    = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) { Text("Cancelar") }
        }
    )
}
