// Ruta: app/src/main/java/com/spa/appointments/ui/admin/catalogos/CatalogosScreen.kt
package com.spa.appointments.ui.admin.catalogos

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.spa.appointments.R
import com.spa.appointments.domain.model.CategoriaAdmin
import com.spa.appointments.domain.model.ServicioAdmin
import com.spa.appointments.domain.model.ServicioRequest
import com.spa.appointments.ui.tema.MensajeWhatsAppField

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
                            if (servicios.isNotEmpty()) {
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
            ExtendedFloatingActionButton(
                onClick        = { editando = null; showDialog = true },
                icon           = { Icon(Icons.Default.Add, null) },
                text           = { Text("Nuevo servicio", fontWeight = FontWeight.Bold) },
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

            // ── Buscador Compacto Sin Etiqueta Flotante M3 ─────────────────
            OutlinedTextField(
                value         = busqueda,
                onValueChange = { busqueda = it },
                placeholder   = { Text("Buscar servicio o categoría…", style = MaterialTheme.typography.bodyMedium) },
                leadingIcon   = { Icon(Icons.Default.Search, null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                trailingIcon  = {
                    if (busqueda.isNotBlank()) {
                        IconButton(onClick = { busqueda = "" }) {
                            Icon(Icons.Default.Clear, null, modifier = Modifier.size(18.dp))
                        }
                    }
                },
                modifier   = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                shape      = RoundedCornerShape(14.dp),
                colors     = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor   = MaterialTheme.colorScheme.surfaceContainerLow,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow
                ),
                singleLine = true
            )

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
                                "Cargando servicios…",
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
                            contentPadding      = PaddingValues(
                                start  = 16.dp,
                                end    = 16.dp,
                                top    = 8.dp,
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
            servicio             = editando,
            categorias           = categorias,
            todosLosServicios    = servicios,
            guardando            = uiState is CatalogosUiState.Loading,
            onGuardar            = { req -> viewModel.guardarServicio(editando?.id, req) },
            onCopiarMensaje      = { mensaje, ids ->
                viewModel.copiarMensajeWhatsApp(mensaje, ids) {
                    showDialog = false
                    editando = null
                }
            },
            onDismiss            = { showDialog = false; editando = null; viewModel.resetState() }
        )
    }

    if (showPreciosRangosDialog && servicioRangoSeleccionado != null) {
        val preciosRangos by viewModel.preciosRangos.collectAsState()
        PreciosPersonasDialog(
            servicio = servicioRangoSeleccionado!!,
            preciosRangos = preciosRangos,
            onGuardarRango = { cant, prec, esPorPersona ->
                viewModel.guardarPrecioRango(servicioRangoSeleccionado!!.id, cant, prec, esPorPersona)
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

// ─── Card de Servicio Simplificada (2 Filas) M3 ───────────────────────────────

@Composable
private fun ServicioAdminCard(
    servicio: ServicioAdmin,
    onEditar: () -> Unit,
    onToggle: () -> Unit,
    onVerPreciosRangos: () -> Unit
) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(16.dp),
        colors   = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // ── Fila 1: Icono, Nombre, Categoría, Precio Base y Switch ──────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (servicio.esGrupal)
                        MaterialTheme.colorScheme.tertiaryContainer
                    else
                        MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(42.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (servicio.esGrupal) Icons.Default.Groups else Icons.Default.Spa,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = if (servicio.esGrupal)
                                MaterialTheme.colorScheme.onTertiaryContainer
                            else
                                MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                Spacer(Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = servicio.nombre,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = servicio.nombreCategoria,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(Modifier.width(8.dp))

                // Precio Base destacado
                Text(
                    text = "$ ${"%,.0f".format(servicio.precioBase)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(Modifier.width(6.dp))

                Switch(
                    checked = servicio.estado,
                    onCheckedChange = { onToggle() },
                    modifier = Modifier.scale(0.8f)
                )
            }

            HorizontalDivider(
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
            )

            // ── Fila 2: Badges compactas + Botones de Acción M3 ───────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f, fill = false),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Duración
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${servicio.duracionMinutos} min",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
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
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Groups,
                                    contentDescription = null,
                                    modifier = Modifier.size(12.dp),
                                    tint = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                                Text(
                                    text = "Grupal",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                            }
                        }
                    }

                    // Ícono de WhatsApp personalizado (Únicamente ícono compacto)
                    if (!servicio.mensajeWhatsApp.isNullOrBlank()) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFE7F8EE),
                            modifier = Modifier.size(24.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_whatsapp),
                                    contentDescription = "WhatsApp personalizado",
                                    tint = Color.Unspecified,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }

                // Botones de acción compactos
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (servicio.esGrupal) {
                        FilledTonalButton(
                            onClick = onVerPreciosRangos,
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                            ),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.FormatListNumbered,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = "Tarifas",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    IconButton(
                        onClick = onEditar,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Editar",
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ─── Diálogo Crear / Editar M3 ─────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ServicioDialog(
    servicio:             ServicioAdmin?,
    categorias:           List<CategoriaAdmin>,
    todosLosServicios:    List<ServicioAdmin>,
    guardando:            Boolean,
    onGuardar:            (ServicioRequest) -> Unit,
    onCopiarMensaje:      (String?, List<Int>) -> Unit,
    onDismiss:            () -> Unit
) {
    var nombre      by remember(servicio) { mutableStateOf(servicio?.nombre ?: "") }
    var descripcion by remember(servicio) { mutableStateOf(servicio?.descripcion ?: "") }
    var duracion    by remember(servicio) { mutableStateOf(servicio?.duracionMinutos?.toString() ?: "") }
    var precio      by remember(servicio) { mutableStateOf(servicio?.precioBase?.toString() ?: "") }
    var esGrupal    by remember(servicio) { mutableStateOf(servicio?.esGrupal ?: false) }
    var maxPersonas by remember(servicio) { mutableStateOf(servicio?.maxPersonas?.toString() ?: "") }
    var mensajeWhatsApp by remember(servicio) {
        mutableStateOf(TextFieldValue(servicio?.mensajeWhatsApp ?: ""))
    }
    var mostrarDialogoCopiar by remember { mutableStateOf(false) }

    val variablesWhatsApp = remember(esGrupal) {
        if (esGrupal) {
            listOf("{NombreCliente}", "{Fecha}", "{Hora}", "{Duracion}", "{Asistentes}")
        } else {
            listOf("{NombreCliente}", "{Fecha}", "{Hora}", "{Duracion}")
        }
    }

    fun insertarVariableWhatsApp(variable: String) {
        val texto = mensajeWhatsApp.text
        val cursor = mensajeWhatsApp.selection.end.coerceIn(0, texto.length)
        val nuevoTexto = texto.substring(0, cursor) + variable + texto.substring(cursor)
        val nuevoCursor = cursor + variable.length
        mensajeWhatsApp = TextFieldValue(
            text = nuevoTexto,
            selection = TextRange(nuevoCursor)
        )
    }

    var iconoSeleccionado by remember(servicio) {
        mutableStateOf(ICONOS_DISPONIBLES.firstOrNull { it.clave == servicio?.icono })
    }

    val categoriaInicial      = categorias.firstOrNull { it.id == servicio?.idCategoria }
    var categoriaSeleccionada by remember(servicio) { mutableStateOf(categoriaInicial) }
    var expandedCat           by remember { mutableStateOf(false) }

    val maxPersonasInt = maxPersonas.toIntOrNull()
    val maxPersonasValido = !esGrupal || (maxPersonasInt != null && maxPersonasInt > 1)

    val esValido = nombre.isNotBlank()
            && duracion.toIntOrNull() != null
            && precio.toDoubleOrNull() != null
            && categoriaSeleccionada != null
            && maxPersonasValido

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
                    imageVector        = if (servicio != null) Icons.Default.Edit else Icons.Default.Add,
                    contentDescription = null,
                    tint               = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier           = Modifier.padding(12.dp).size(24.dp)
                )
            }
        },
        title = {
            Text(
                text       = if (servicio != null) "Editar servicio" else "Nuevo servicio",
                fontWeight = FontWeight.Bold,
                style      = MaterialTheme.typography.titleMedium,
                textAlign  = TextAlign.Center,
                modifier   = Modifier.fillMaxWidth()
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier            = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Dropdown Categoría
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
                        shape         = RoundedCornerShape(14.dp),
                        colors        = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor   = MaterialTheme.colorScheme.surfaceContainerLow,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow
                        ),
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
                    leadingIcon   = { Icon(Icons.Default.Spa, null, tint = MaterialTheme.colorScheme.primary) },
                    modifier      = Modifier.fillMaxWidth(),
                    singleLine    = true,
                    shape         = RoundedCornerShape(14.dp),
                    colors        = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor   = MaterialTheme.colorScheme.surfaceContainerLow,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    )
                )

                OutlinedTextField(
                    value         = descripcion,
                    onValueChange = { descripcion = it },
                    label         = { Text("Descripción") },
                    leadingIcon   = { Icon(Icons.Default.Notes, null, tint = MaterialTheme.colorScheme.primary) },
                    modifier      = Modifier.fillMaxWidth(),
                    maxLines      = 2,
                    shape         = RoundedCornerShape(14.dp),
                    colors        = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor   = MaterialTheme.colorScheme.surfaceContainerLow,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    )
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value           = duracion,
                        onValueChange   = { duracion = it },
                        label           = { Text("Min *") },
                        leadingIcon     = { Icon(Icons.Default.Timer, null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary) },
                        modifier        = Modifier.weight(1f),
                        singleLine      = true,
                        shape           = RoundedCornerShape(14.dp),
                        colors          = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor   = MaterialTheme.colorScheme.surfaceContainerLow,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    OutlinedTextField(
                        value           = precio,
                        onValueChange   = { precio = it },
                        label           = { Text("Precio ($) *") },
                        leadingIcon     = { Icon(Icons.Default.AttachMoney, null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary) },
                        modifier        = Modifier.weight(1f),
                        singleLine      = true,
                        shape           = RoundedCornerShape(14.dp),
                        colors          = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor   = MaterialTheme.colorScheme.surfaceContainerLow,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 2.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                // Toggle Grupal
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
                            shape = RoundedCornerShape(10.dp),
                            color = if (esGrupal)
                                MaterialTheme.colorScheme.tertiaryContainer
                            else
                                MaterialTheme.colorScheme.surfaceContainerLow,
                            modifier = Modifier.size(38.dp)
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
                                fontWeight = FontWeight.Bold
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

                // Campo Máximo de personas (Obligatorio en servicios grupales)
                AnimatedVisibility(
                    visible = esGrupal,
                    enter   = expandVertically(),
                    exit    = shrinkVertically()
                ) {
                    val maxInt = maxPersonas.toIntOrNull()
                    val esInvalidoMax = maxPersonas.isNotBlank() && (maxInt == null || maxInt <= 1)

                    OutlinedTextField(
                        value         = maxPersonas,
                        onValueChange = { input -> maxPersonas = input.filter { it.isDigit() } },
                        label         = { Text("Máximo de personas *") },
                        leadingIcon   = { Icon(Icons.Default.Groups, null, tint = MaterialTheme.colorScheme.primary) },
                        isError       = esInvalidoMax,
                        supportingText = {
                            if (esInvalidoMax) {
                                Text(
                                    text = "El máximo debe ser un número mayor a 1",
                                    color = MaterialTheme.colorScheme.error
                                )
                            } else {
                                Text(
                                    text = "Obligatorio (mayor que 1)",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        singleLine    = true,
                        shape         = RoundedCornerShape(14.dp),
                        colors        = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor   = MaterialTheme.colorScheme.surfaceContainerLow,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier      = Modifier.fillMaxWidth()
                    )
                }

                // Hint Informativo
                AnimatedVisibility(
                    visible = esGrupal,
                    enter   = expandVertically(),
                    exit    = shrinkVertically()
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
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
                                modifier           = Modifier.size(16.dp),
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

                HorizontalDivider(modifier = Modifier.padding(vertical = 2.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                // Mensaje Automático de WhatsApp
                OutlinedCard(
                    shape  = RoundedCornerShape(18.dp),
                    colors = CardDefaults.outlinedCardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    MensajeWhatsAppField(
                        value              = mensajeWhatsApp,
                        onValueChange      = { mensajeWhatsApp = it },
                        variables          = variablesWhatsApp,
                        onInsertarVariable = ::insertarVariableWhatsApp
                    )
                }

                // Botón para copiar plantilla a otros servicios
                if (servicio != null && mensajeWhatsApp.text.isNotBlank()) {
                    Spacer(Modifier.height(4.dp))
                    OutlinedButton(
                        onClick  = { mostrarDialogoCopiar = true },
                        shape    = RoundedCornerShape(12.dp),
                        colors   = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.ContentCopy, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Copiar plantilla a otros servicios", style = MaterialTheme.typography.labelMedium)
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
                            esGrupal        = esGrupal,
                            maxPersonas     = if (esGrupal) maxPersonas.toIntOrNull() else null,
                            mensajeWhatsApp = mensajeWhatsApp.text.trim().ifEmpty { null }
                        )
                    )
                },
                enabled  = esValido && !guardando,
                shape    = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (guardando) {
                    CircularProgressIndicator(
                        modifier    = Modifier.size(18.dp),
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

    if (mostrarDialogoCopiar && servicio != null) {
        CopiarMensajeServiciosDialog(
            servicioOrigen       = servicio,
            mensajeACopiar       = mensajeWhatsApp.text,
            serviciosDisponibles = todosLosServicios,
            guardando            = guardando,
            onConfirmar          = { ids ->
                onCopiarMensaje(mensajeWhatsApp.text.trim().ifEmpty { null }, ids)
                mostrarDialogoCopiar = false
            },
            onDismiss            = { mostrarDialogoCopiar = false }
        )
    }
}

// ─── Diálogo Seleccionar Servicios para Copiar Mensaje ─────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CopiarMensajeServiciosDialog(
    servicioOrigen:       ServicioAdmin,
    mensajeACopiar:       String,
    serviciosDisponibles: List<ServicioAdmin>,
    guardando:            Boolean,
    onConfirmar:          (List<Int>) -> Unit,
    onDismiss:            () -> Unit
) {
    val otrosServicios = remember(serviciosDisponibles, servicioOrigen) {
        serviciosDisponibles.filter { it.id != servicioOrigen.id }
    }

    var busqueda by remember { mutableStateOf("") }
    var idsSeleccionados by remember { mutableStateOf(setOf<Int>()) }

    val serviciosFiltrados = remember(otrosServicios, busqueda) {
        if (busqueda.isBlank()) otrosServicios
        else otrosServicios.filter {
            it.nombre.contains(busqueda, ignoreCase = true) ||
                    it.nombreCategoria.contains(busqueda, ignoreCase = true)
        }
    }

    val todosSeleccionados = serviciosFiltrados.isNotEmpty() &&
            serviciosFiltrados.all { idsSeleccionados.contains(it.id) }

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
                    imageVector        = Icons.Default.ContentCopy,
                    contentDescription = null,
                    tint               = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier           = Modifier.padding(12.dp).size(24.dp)
                )
            }
        },
        title = {
            Text(
                text       = "Copiar mensaje de WhatsApp",
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
                Text(
                    text  = "Selecciona los servicios a los cuales deseas aplicar el mensaje de \"${servicioOrigen.nombre}\":",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Buscador
                OutlinedTextField(
                    value         = busqueda,
                    onValueChange = { busqueda = it },
                    placeholder   = { Text("Buscar servicio...") },
                    leadingIcon   = { Icon(Icons.Default.Search, null, modifier = Modifier.size(18.dp)) },
                    singleLine    = true,
                    shape         = RoundedCornerShape(14.dp),
                    colors        = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor   = MaterialTheme.colorScheme.surfaceContainerLow,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    ),
                    modifier      = Modifier.fillMaxWidth()
                )

                // Checkbox Seleccionar Todos
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier          = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked         = todosSeleccionados,
                        onCheckedChange = { check ->
                            val idsVisibles = serviciosFiltrados.map { it.id }.toSet()
                            idsSeleccionados = if (check) {
                                idsSeleccionados + idsVisibles
                            } else {
                                idsSeleccionados - idsVisibles
                            }
                        }
                    )
                    Text(
                        text       = if (todosSeleccionados) "Desmarcar todos" else "Seleccionar todos (${serviciosFiltrados.size})",
                        style      = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                // Lista de servicios con Checkboxes
                LazyColumn(
                    modifier            = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 240.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(serviciosFiltrados, key = { it.id }) { srv ->
                        val seleccionado = idsSeleccionados.contains(srv.id)
                        Surface(
                            shape    = RoundedCornerShape(10.dp),
                            color    = if (seleccionado) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                                       else MaterialTheme.colorScheme.surfaceContainerLow,
                            onClick  = {
                                idsSeleccionados = if (seleccionado) {
                                    idsSeleccionados - srv.id
                                } else {
                                    idsSeleccionados + srv.id
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier          = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                Checkbox(
                                    checked         = seleccionado,
                                    onCheckedChange = { check ->
                                        idsSeleccionados = if (check) idsSeleccionados + srv.id else idsSeleccionados - srv.id
                                    }
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text       = srv.nombre,
                                        style      = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text  = srv.nombreCategoria,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick  = { onConfirmar(idsSeleccionados.toList()) },
                enabled  = idsSeleccionados.isNotEmpty() && !guardando,
                shape    = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (guardando) {
                    CircularProgressIndicator(
                        modifier    = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color       = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Icon(Icons.Default.ContentCopy, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Aplicar a (${idsSeleccionados.size}) servicios", fontWeight = FontWeight.Bold)
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
