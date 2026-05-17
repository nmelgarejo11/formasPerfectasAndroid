package com.spa.appointments.ui.admin.perfilsubmodulo

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
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

    // Filtrado reactivo en memoria para ambos niveles
    val perfilesFiltrados = remember(perfiles, busqueda, mostrarSubModulos) {
        if (mostrarSubModulos || busqueda.isBlank()) perfiles
        else perfiles.filter { it.nombre.contains(busqueda, ignoreCase = true) }
    }

    val subModulosFiltrados = remember(subModulos, busqueda, mostrarSubModulos) {
        if (!mostrarSubModulos || busqueda.isBlank()) subModulos
        else subModulos.filter { it.nombre.contains(busqueda, ignoreCase = true) }
    }

    // Sincronización con cambios del ViewModel y limpieza del buscador al cambiar de nivel
    LaunchedEffect(seleccionado) {
        mostrarSubModulos = seleccionado != null
        busqueda = ""
    }

    // Manejo del botón físico de "Atrás" del dispositivo
    BackHandler(enabled = mostrarSubModulos) {
        mostrarSubModulos = false
        busqueda = ""
    }

    // Snackbar para mensajes de éxito/error
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(mensaje) {
        mensaje?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.limpiarMensaje()
        }
    }

    Scaffold(
        topBar = {
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
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // ── Buscador Dinámico Único ─────────────────────────────────────
            OutlinedTextField(
                value         = busqueda,
                onValueChange = { busqueda = it },
                label         = {
                    Text(if (!mostrarSubModulos) "Buscar perfil" else "Buscar submódulo")
                },
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
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            CircularProgressIndicator()
                            Text(
                                "Cargando datos…",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // ─── 2. Vista: Lista de Submódulos (Filtro Activo) ────────
                    mostrarSubModulos && seleccionado != null -> {
                        if (subModulosFiltrados.isEmpty()) {
                            // Estado vacío para submódulos sin resultados
                            Column(
                                modifier            = Modifier.align(Alignment.Center).padding(40.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(24.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant
                                ) {
                                    Icon(
                                        imageVector        = Icons.Default.SearchOff,
                                        contentDescription = null,
                                        modifier           = Modifier.padding(20.dp).size(48.dp),
                                        tint               = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Text(
                                    text       = "Sin resultados",
                                    style      = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text      = "Ningún submódulo coincide con \"$busqueda\".",
                                    style     = MaterialTheme.typography.bodyMedium,
                                    color     = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            }
                        } else {
                            LazyColumn(
                                modifier       = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(start = 16.dp, top = 4.dp, end = 16.dp, bottom = 24.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(subModulosFiltrados) { item ->
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
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 14.dp, vertical = 12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Surface(
                                                shape    = RoundedCornerShape(10.dp),
                                                color    = if (item.asignado)
                                                    MaterialTheme.colorScheme.primaryContainer
                                                else
                                                    MaterialTheme.colorScheme.surfaceVariant,
                                                modifier = Modifier.size(44.dp)
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

                                            Spacer(Modifier.width(12.dp))

                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text       = item.nombre,
                                                    style      = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                            }

                                            Spacer(Modifier.width(8.dp))

                                            Switch(
                                                checked         = item.asignado,
                                                onCheckedChange = { viewModel.toggleSubModulo(item) },
                                                modifier        = Modifier.scale(0.8f)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // ─── 3. Vista: Lista de Perfiles (Filtro Activo) ──────────
                    else -> {
                        if (perfilesFiltrados.isEmpty()) {
                            // Estado vacío para perfiles sin resultados o BD vacía
                            Column(
                                modifier            = Modifier.align(Alignment.Center).padding(40.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(24.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant
                                ) {
                                    Icon(
                                        imageVector        = Icons.Default.SearchOff,
                                        contentDescription = null,
                                        modifier           = Modifier.padding(20.dp).size(48.dp),
                                        tint               = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Text(
                                    text       = if (busqueda.isBlank()) "Sin perfiles aún" else "Sin resultados",
                                    style      = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text      = if (busqueda.isBlank()) "No se encontraron perfiles registrados." else "Ningún perfil coincide con \"$busqueda\".",
                                    style     = MaterialTheme.typography.bodyMedium,
                                    color     = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            }
                        } else {
                            LazyColumn(
                                modifier       = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(start = 16.dp, top = 4.dp, end = 16.dp, bottom = 24.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(perfilesFiltrados) { perfil ->
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape     = RoundedCornerShape(14.dp),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                        colors    = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.surface
                                        ),
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                        onClick = {
                                            viewModel.seleccionarPerfil(perfil)
                                            mostrarSubModulos = true
                                        }
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 14.dp, vertical = 14.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Surface(
                                                shape    = RoundedCornerShape(10.dp),
                                                color    = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                                                modifier = Modifier.size(44.dp)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Icon(
                                                        imageVector = Icons.Default.Group,
                                                        contentDescription = null,
                                                        modifier           = Modifier.size(22.dp),
                                                        tint               = MaterialTheme.colorScheme.primary
                                                    )
                                                }
                                            }

                                            Spacer(Modifier.width(12.dp))

                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text       = perfil.nombre,
                                                    style      = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.Bold
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
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
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