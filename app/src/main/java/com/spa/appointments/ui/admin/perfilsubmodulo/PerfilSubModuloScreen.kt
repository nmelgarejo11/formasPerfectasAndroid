// Ruta: app/src/main/java/com/spa/appointments/ui/admin/perfilsubmodulo/PerfilSubModuloScreen.kt
package com.spa.appointments.ui.admin.perfilsubmodulo

import androidx.activity.compose.BackHandler
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilSubModuloScreen(
    onVolver: () -> Unit,
    viewModel: PerfilSubModuloViewModel = hiltViewModel()
) {
    val perfiles     = viewModel.perfiles
    val subModulos   = viewModel.subModulos
    val seleccionado = viewModel.perfilSeleccionado
    val isLoading    = viewModel.isLoading
    val mensaje      = viewModel.mensaje

    var mostrarSubModulos by remember { mutableStateOf(false) }
    var busqueda          by remember { mutableStateOf("") }

    val perfilesFiltrados = remember(perfiles, busqueda, mostrarSubModulos) {
        if (mostrarSubModulos || busqueda.isBlank()) perfiles
        else perfiles.filter { it.nombre.contains(busqueda, ignoreCase = true) }
    }

    val subModulosFiltrados = remember(subModulos, busqueda, mostrarSubModulos) {
        if (!mostrarSubModulos || busqueda.isBlank()) subModulos
        else subModulos.filter { it.nombre.contains(busqueda, ignoreCase = true) }
    }

    LaunchedEffect(seleccionado) {
        mostrarSubModulos = seleccionado != null
        busqueda = ""
    }

    BackHandler(enabled = mostrarSubModulos) {
        mostrarSubModulos = false
        busqueda = ""
    }

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(mensaje) {
        mensaje?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.limpiarMensaje()
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
                                text       = if (!mostrarSubModulos || seleccionado == null) "Perfiles de Usuario" else seleccionado.nombre,
                                style      = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text  = if (!mostrarSubModulos || seleccionado == null) "Selecciona un perfil para gestionar accesos" else "Gestión de submódulos",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                if (mostrarSubModulos) {
                                    mostrarSubModulos = false
                                    busqueda = ""
                                } else {
                                    onVolver()
                                }
                            }
                        ) {
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
                label         = {
                    Text(if (!mostrarSubModulos) "Buscar perfil" else "Buscar submódulo")
                },
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
                enabled    = !isLoading
            )

            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    // ─── 1. Estado de Carga ───────────────────────────────────
                    isLoading -> {
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
                                "Cargando datos…",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // ─── 2. Vista: Lista de Submódulos ────────────────────────
                    mostrarSubModulos && seleccionado != null -> {
                        if (subModulosFiltrados.isEmpty()) {
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
                                                imageVector        = Icons.Default.SearchOff,
                                                contentDescription = null,
                                                modifier           = Modifier.padding(18.dp).size(36.dp),
                                                tint               = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                    Text(
                                        text       = "Sin resultados",
                                        style      = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color      = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text      = "Ningún submódulo coincide con \"$busqueda\".",
                                        style     = MaterialTheme.typography.bodyMedium,
                                        color     = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        } else {
                            LazyColumn(
                                modifier       = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 24.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(subModulosFiltrados) { item ->
                                    OutlinedCard(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape    = RoundedCornerShape(18.dp),
                                        colors   = CardDefaults.outlinedCardColors(
                                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                                        ),
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 16.dp, vertical = 14.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Surface(
                                                shape    = RoundedCornerShape(12.dp),
                                                color    = if (item.asignado)
                                                    MaterialTheme.colorScheme.primaryContainer
                                                else
                                                    MaterialTheme.colorScheme.surfaceContainerHigh,
                                                modifier = Modifier.size(46.dp)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Icon(
                                                        imageVector = Icons.Default.BookmarkBorder,
                                                        contentDescription = null,
                                                        modifier           = Modifier.size(22.dp),
                                                        tint               = if (item.asignado)
                                                            MaterialTheme.colorScheme.onPrimaryContainer
                                                        else
                                                            MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            }

                                            Spacer(Modifier.width(14.dp))

                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text       = item.nombre,
                                                    style      = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color      = MaterialTheme.colorScheme.onSurface
                                                )
                                            }

                                            Spacer(Modifier.width(8.dp))

                                            Switch(
                                                checked         = item.asignado,
                                                onCheckedChange = { viewModel.toggleSubModulo(item) },
                                                modifier        = Modifier.scale(0.85f)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // ─── 3. Vista: Lista de Perfiles ──────────────────────────
                    else -> {
                        if (perfilesFiltrados.isEmpty()) {
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
                                                imageVector        = Icons.Default.SearchOff,
                                                contentDescription = null,
                                                modifier           = Modifier.padding(18.dp).size(36.dp),
                                                tint               = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                    Text(
                                        text       = if (busqueda.isBlank()) "Sin perfiles aún" else "Sin resultados",
                                        style      = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color      = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text      = if (busqueda.isBlank()) "No se encontraron perfiles registrados." else "Ningún perfil coincide con \"$busqueda\".",
                                        style     = MaterialTheme.typography.bodyMedium,
                                        color     = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        } else {
                            LazyColumn(
                                modifier       = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 24.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(perfilesFiltrados) { perfil ->
                                    OutlinedCard(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape    = RoundedCornerShape(18.dp),
                                        colors   = CardDefaults.outlinedCardColors(
                                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                                        ),
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
                                        onClick = {
                                            viewModel.seleccionarPerfil(perfil)
                                            mostrarSubModulos = true
                                        }
                                    ) {
                                        Row(
                                            modifier = Modifier
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
                                                        imageVector = Icons.Default.Group,
                                                        contentDescription = null,
                                                        modifier           = Modifier.size(22.dp),
                                                        tint               = MaterialTheme.colorScheme.onPrimaryContainer
                                                    )
                                                }
                                            }

                                            Spacer(Modifier.width(14.dp))

                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text       = perfil.nombre,
                                                    style      = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color      = MaterialTheme.colorScheme.onSurface
                                                )
                                                perfil.descripcion?.let {
                                                    Text(
                                                        text  = it,
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                        modifier = Modifier.padding(top = 2.dp)
                                                    )
                                                }
                                            }

                                            Icon(
                                                imageVector = Icons.Default.ChevronRight,
                                                contentDescription = "Ver submódulos",
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant
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
}
