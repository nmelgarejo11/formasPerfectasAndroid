// Ruta: app/src/main/java/com/spa/appointments/ui/admin/catalogos/CategoriasScreen.kt
package com.spa.appointments.ui.admin.catalogos

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
import com.spa.appointments.core.utils.mapIcon
import com.spa.appointments.domain.model.CategoriaAdmin
import com.spa.appointments.ui.components.SinPermisoContent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriasScreen(
    onBack: () -> Unit,
    viewModel: CatalogosViewModel = hiltViewModel()
) {
    val categorias by viewModel.categorias.collectAsState()
    val uiState    by viewModel.uiState.collectAsState()

    var showDialog by remember { mutableStateOf(false) }
    var editando   by remember { mutableStateOf<CategoriaAdmin?>(null) }
    var busqueda   by remember { mutableStateOf("") }

    val filtradas = remember(categorias, busqueda) {
        if (busqueda.isBlank()) categorias
        else categorias.filter {
            it.nombre.contains(busqueda, ignoreCase = true)
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
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
                                text       = "Categorías",
                                style      = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            if (categorias.isNotEmpty() && uiState !is CatalogosUiState.Forbidden) {
                                Surface(
                                    shape    = RoundedCornerShape(50),
                                    color    = MaterialTheme.colorScheme.primaryContainer,
                                    modifier = Modifier.padding(top = 2.dp)
                                ) {
                                    Text(
                                        text     = "${categorias.size} ${if (categorias.size == 1) "categoría" else "categorías"}",
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
            if (uiState !is CatalogosUiState.Forbidden) {
                ExtendedFloatingActionButton(
                    onClick        = { editando = null; showDialog = true },
                    icon           = { Icon(Icons.Default.Add, null) },
                    text           = { Text("Nueva categoría", fontWeight = FontWeight.Bold) },
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
                    label         = { Text("Buscar categoría") },
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

                Box(modifier = Modifier.fillMaxSize()) {
                    when {
                        uiState is CatalogosUiState.Loading && categorias.isEmpty() -> {
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
                                    text  = "Cargando categorías…",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        filtradas.isEmpty() -> {
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
                                                imageVector        = if (busqueda.isBlank()) Icons.Default.Category
                                                else Icons.Default.SearchOff,
                                                contentDescription = null,
                                                modifier           = Modifier.padding(18.dp).size(36.dp),
                                                tint               = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                    Text(
                                        text       = if (busqueda.isBlank()) "Sin categorías aún" else "Sin resultados",
                                        style      = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color      = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text      = if (busqueda.isBlank())
                                            "Crea la primera categoría con el botón inferior."
                                        else
                                            "Ninguna categoría coincide con \"$busqueda\".",
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
                                items(filtradas, key = { it.id }) { cat ->
                                    OutlinedCard(
                                        shape    = RoundedCornerShape(18.dp),
                                        colors   = CardDefaults.outlinedCardColors(
                                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                                        ),
                                        border   = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier          = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 16.dp, vertical = 14.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Surface(
                                                shape    = RoundedCornerShape(12.dp),
                                                color    = MaterialTheme.colorScheme.primaryContainer,
                                                modifier = Modifier.size(46.dp)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Icon(
                                                        imageVector        = mapIcon(cat.icono),
                                                        contentDescription = null,
                                                        modifier           = Modifier.size(22.dp),
                                                        tint               = MaterialTheme.colorScheme.onPrimaryContainer
                                                    )
                                                }
                                            }

                                            Spacer(Modifier.width(14.dp))

                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text       = cat.nombre,
                                                    style      = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color      = MaterialTheme.colorScheme.onSurface
                                                )
                                            }

                                            Spacer(Modifier.width(8.dp))

                                            IconButton(
                                                onClick  = { editando = cat; showDialog = true },
                                                modifier = Modifier.size(36.dp)
                                            ) {
                                                Icon(
                                                    imageVector        = Icons.Default.Edit,
                                                    contentDescription = "Editar",
                                                    tint               = MaterialTheme.colorScheme.primary,
                                                    modifier           = Modifier.size(18.dp)
                                                )
                                            }

                                            Switch(
                                                checked         = cat.estado,
                                                onCheckedChange = { viewModel.toggleCategoria(cat.id) },
                                                modifier        = Modifier.scale(0.85f)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDialog) {
        CategoriaDialog(
            cat       = editando,
            guardando = uiState is CatalogosUiState.Loading,
            onGuardar = { nombre, icono ->
                viewModel.guardarCategoria(editando?.id, nombre, icono)
            },
            onDismiss = { showDialog = false; editando = null }
        )
    }
}

// ─── Diálogo Crear / Editar Categoría M3 ──────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoriaDialog(
    cat: CategoriaAdmin?,
    guardando: Boolean,
    onGuardar: (nombre: String, icono: String?) -> Unit,
    onDismiss: () -> Unit
) {
    var nombre     by remember(cat) { mutableStateOf(cat?.nombre ?: "") }
    var iconoSel   by remember(cat) { mutableStateOf(cat?.icono ?: "Category") }
    var showPicker by remember { mutableStateOf(false) }
    val esNuevo    = cat == null

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
                    imageVector        = if (esNuevo) Icons.Default.Add else Icons.Default.Edit,
                    contentDescription = null,
                    tint               = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier           = Modifier.padding(12.dp).size(24.dp)
                )
            }
        },
        title = {
            Text(
                text       = if (esNuevo) "Nueva categoría" else "Editar categoría",
                style      = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign  = TextAlign.Center,
                modifier   = Modifier.fillMaxWidth()
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                OutlinedTextField(
                    value         = nombre,
                    onValueChange = { nombre = it },
                    label         = { Text("Nombre *") },
                    leadingIcon   = {
                        Icon(
                            imageVector        = Icons.Default.Label,
                            contentDescription = null,
                            tint               = MaterialTheme.colorScheme.primary
                        )
                    },
                    modifier      = Modifier.fillMaxWidth(),
                    singleLine    = true,
                    shape         = RoundedCornerShape(14.dp),
                    colors        = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor   = MaterialTheme.colorScheme.surfaceContainerLow,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    )
                )

                OutlinedCard(
                    onClick = { showPicker = true },
                    shape   = RoundedCornerShape(14.dp),
                    colors  = CardDefaults.outlinedCardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    ),
                    border  = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Row(
                        modifier          = Modifier.fillMaxWidth().padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Icon(
                                    imageVector = mapIcon(iconoSel),
                                    contentDescription = null,
                                    modifier = Modifier.padding(6.dp).size(20.dp),
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                            Spacer(Modifier.width(10.dp))
                            Text("Ícono seleccionable", style = MaterialTheme.typography.bodyMedium)
                        }
                        Icon(Icons.Default.ArrowDropDown, null)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick  = { onGuardar(nombre.trim(), iconoSel) },
                enabled  = nombre.isNotBlank() && !guardando,
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
                    Icon(
                        imageVector        = Icons.Default.Save,
                        contentDescription = null,
                        modifier           = Modifier.size(18.dp)
                    )
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

    if (showPicker) {
        IconoPickerDialog(
            iconoActual = iconoSel,
            onSeleccionar = {
                iconoSel   = it.clave
                showPicker = false
            },
            onDismiss = { showPicker = false }
        )
    }
}
