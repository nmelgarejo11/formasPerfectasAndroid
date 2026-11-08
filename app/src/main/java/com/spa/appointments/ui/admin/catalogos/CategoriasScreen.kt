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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.spa.appointments.domain.model.CategoriaAdmin

// ─── Screen Principal ─────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriasScreen(
    onBack:    () -> Unit,
    viewModel: CatalogosViewModel = hiltViewModel()
) {
    val categorias by viewModel.categorias.collectAsState()
    val uiState    by viewModel.uiState.collectAsState()

    var showDialog by remember { mutableStateOf(false) }
    var editando   by remember { mutableStateOf<CategoriaAdmin?>(null) }
    var busqueda   by remember { mutableStateOf("") }

    val filtradas = remember(categorias, busqueda) {
        if (busqueda.isBlank()) categorias
        else categorias.filter { it.nombre.contains(busqueda, ignoreCase = true) }
    }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) { viewModel.cargarCategorias() }

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
                                text       = "Categorías",
                                style      = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            if (categorias.isNotEmpty()) {
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
                text           = { Text("Nueva categoría", fontWeight = FontWeight.Bold) },
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
                singleLine = true
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
                                "Cargando categorías…",
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
                            contentPadding      = PaddingValues(
                                start  = 16.dp,
                                end    = 16.dp,
                                top    = 8.dp,
                                bottom = 88.dp
                            ),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(filtradas, key = { it.id }) { cat ->
                                CategoriaCard(
                                    categoria = cat,
                                    onEditar  = { editando = cat; showDialog = true },
                                    onToggle  = { viewModel.toggleCategoria(cat.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDialog) {
        CategoriaDialog(
            categoria = editando,
            guardando = uiState is CatalogosUiState.Loading,
            onGuardar = { nombre, icono -> viewModel.guardarCategoria(editando?.id, nombre, icono) },
            onDismiss = { showDialog = false; editando = null; viewModel.resetState() }
        )
    }
}

// ─── Card de Categoría M3 ─────────────────────────────────────────────────────

@Composable
private fun CategoriaCard(
    categoria: CategoriaAdmin,
    onEditar:  () -> Unit,
    onToggle:  () -> Unit
) {
    val iconoOpcion = ICONOS_DISPONIBLES.firstOrNull { it.clave == categoria.icono }

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
            // Ícono en Surface
            Surface(
                shape    = RoundedCornerShape(12.dp),
                color    = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(46.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector        = iconoOpcion?.icono ?: Icons.Default.Category,
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
                        text       = categoria.nombre,
                        style      = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color      = MaterialTheme.colorScheme.onSurface,
                        maxLines   = 1,
                        overflow   = TextOverflow.Ellipsis,
                        modifier   = Modifier.weight(1f)
                    )
                    // Chip de estado
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (categoria.estado)
                            MaterialTheme.colorScheme.primaryContainer
                        else
                            MaterialTheme.colorScheme.surfaceContainerHigh
                    ) {
                        Text(
                            text       = if (categoria.estado) "Activa" else "Inactiva",
                            style      = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color      = if (categoria.estado)
                                MaterialTheme.colorScheme.onPrimaryContainer
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier   = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.width(8.dp))

            // Acciones
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
                checked         = categoria.estado,
                onCheckedChange = { onToggle() }
            )
        }
    }
}

// ─── Diálogo Crear / Editar Categoría M3 ──────────────────────────────────────

@Composable
private fun CategoriaDialog(
    categoria: CategoriaAdmin?,
    guardando: Boolean,
    onGuardar: (nombre: String, icono: String?) -> Unit,
    onDismiss: () -> Unit
) {
    var nombre           by remember(categoria) { mutableStateOf(categoria?.nombre ?: "") }
    var iconoSeleccionado by remember(categoria) {
        mutableStateOf(ICONOS_DISPONIBLES.firstOrNull { it.clave == categoria?.icono })
    }
    var showIconoPicker by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = { if (!guardando) onDismiss() },
        shape            = RoundedCornerShape(28.dp),
        containerColor   = MaterialTheme.colorScheme.surfaceContainerHigh,
        icon  = {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Icon(
                    imageVector        = if (categoria != null) Icons.Default.Edit else Icons.Default.Add,
                    contentDescription = null,
                    tint               = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier           = Modifier.padding(12.dp).size(24.dp)
                )
            }
        },
        title = {
            Text(
                text       = if (categoria != null) "Editar categoría" else "Nueva categoría",
                fontWeight = FontWeight.Bold,
                style      = MaterialTheme.typography.titleMedium,
                textAlign  = TextAlign.Center,
                modifier   = Modifier.fillMaxWidth()
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value         = nombre,
                    onValueChange = { nombre = it },
                    label         = { Text("Nombre *") },
                    leadingIcon   = { Icon(Icons.Default.Category, null, tint = MaterialTheme.colorScheme.primary) },
                    modifier      = Modifier.fillMaxWidth(),
                    singleLine    = true,
                    shape         = RoundedCornerShape(14.dp),
                    colors        = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor   = MaterialTheme.colorScheme.surfaceContainerLow,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    )
                )

                // Selector de ícono M3
                OutlinedButton(
                    onClick  = { showIconoPicker = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape    = RoundedCornerShape(14.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Icon(
                            imageVector        = iconoSeleccionado?.icono ?: Icons.Default.GridView,
                            contentDescription = null,
                            modifier           = Modifier.padding(6.dp).size(18.dp),
                            tint               = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text     = iconoSeleccionado?.etiqueta ?: "Seleccionar ícono",
                        modifier = Modifier.weight(1f),
                        fontWeight = FontWeight.SemiBold
                    )
                    Icon(
                        Icons.Default.ChevronRight, null,
                        modifier = Modifier.size(18.dp),
                        tint     = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick  = { onGuardar(nombre.trim(), iconoSeleccionado?.clave) },
                enabled  = nombre.isNotBlank() && !guardando,
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

    if (showIconoPicker) {
        IconoPickerDialog(
            iconoActual   = iconoSeleccionado?.clave,
            onSeleccionar = { opcion -> iconoSeleccionado = opcion; showIconoPicker = false },
            onDismiss     = { showIconoPicker = false }
        )
    }
}
