// Ruta: app/src/main/java/com/spa/appointments/ui/servicios/ServiciosScreen.kt
package com.spa.appointments.ui.servicios

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.spa.appointments.core.utils.mapIcon
import com.spa.appointments.domain.model.Servicio

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiciosScreen(
    onBack:               () -> Unit,
    onFinalizarSeleccion: (List<Servicio>) -> Unit,
    vm: ServiciosViewModel = hiltViewModel()
) {
    val uiState        by vm.uiState.collectAsState()
    val listState      = rememberLazyListState()
    var query          by remember { mutableStateOf("") }
    val focusManager   = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }

    // Estado local para persistir múltiples ítems seleccionados
    val serviciosSeleccionados = remember { mutableStateListOf<Servicio>() }
    val precioTotalAcumulado  = remember(serviciosSeleccionados.toList()) {
        serviciosSeleccionados.sumOf { it.precioBase }
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

                            val subtitulo = when (val state = uiState) {
                                is ServiciosUiState.Loading -> "Cargando catálogo..."
                                is ServiciosUiState.Error   -> "Error al cargar"
                                is ServiciosUiState.Success -> {
                                    val filtrados = if (query.isBlank()) state.items
                                    else state.items.filter { s ->
                                        s.nombre.contains(query, ignoreCase = true) ||
                                                s.categoria.contains(query, ignoreCase = true) ||
                                                s.descripcion?.contains(query, ignoreCase = true) == true ||
                                                (query.contains("grupal", ignoreCase = true) && s.esGrupal)
                                    }
                                    val total = filtrados.size
                                    when {
                                        query.isBlank() -> "$total ${if (total == 1) "servicio disponible" else "servicios disponibles"}"
                                        total == 0      -> "Sin coincidencias"
                                        else            -> "$total ${if (total == 1) "servicio encontrado" else "servicios encontrados"}"
                                    }
                                }
                            }

                            if (subtitulo.isNotBlank()) {
                                Surface(
                                    shape    = RoundedCornerShape(50),
                                    color    = MaterialTheme.colorScheme.primaryContainer,
                                    modifier = Modifier.padding(top = 2.dp)
                                ) {
                                    Text(
                                        text     = subtitulo,
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
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
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

                Spacer(Modifier.height(8.dp))

                SearchBar(
                    query          = query,
                    onQueryChange  = { query = it },
                    onClear        = { query = ""; focusManager.clearFocus() },
                    focusRequester = focusRequester,
                    modifier       = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 8.dp)
                )

                HorizontalDivider(
                    thickness = 0.5.dp,
                    color     = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )
            }
        },
        bottomBar = {
            AnimatedVisibility(
                visible = serviciosSeleccionados.isNotEmpty(),
                enter   = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit    = slideOutVertically(targetOffsetY = { it }) + fadeOut()
            ) {
                Surface(
                    tonalElevation = 6.dp,
                    shadowElevation = 8.dp,
                    shape    = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                    color    = MaterialTheme.colorScheme.surfaceContainerHigh,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier          = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text  = if (serviciosSeleccionados.size == 1) "1 servicio seleccionado" else "${serviciosSeleccionados.size} servicios seleccionados",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text       = "Total: ${"$%,.0f".format(precioTotalAcumulado)}",
                                style      = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color      = MaterialTheme.colorScheme.primary
                            )
                        }

                        Button(
                            onClick  = { onFinalizarSeleccion(serviciosSeleccionados.toList()) },
                            shape    = RoundedCornerShape(12.dp),
                            modifier = Modifier.height(46.dp)
                        ) {
                            Text(
                                text       = "Continuar",
                                fontWeight = FontWeight.Bold,
                                fontSize   = 15.sp
                            )
                            Spacer(Modifier.width(6.dp))
                            Icon(
                                imageVector        = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                modifier           = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (val state = uiState) {
                is ServiciosUiState.Loading -> {
                    LoadingState(modifier = Modifier.align(Alignment.Center))
                }

                is ServiciosUiState.Error -> {
                    ErrorState(
                        mensaje  = state.mensaje,
                        modifier = Modifier.align(Alignment.Center).padding(24.dp)
                    )
                }

                is ServiciosUiState.Success -> {
                    val filtrados = remember(state.items, query) {
                        if (query.isBlank()) state.items
                        else state.items.filter { s ->
                            s.nombre.contains(query, ignoreCase = true) ||
                                    s.categoria.contains(query, ignoreCase = true) ||
                                    s.descripcion?.contains(query, ignoreCase = true) == true ||
                                    (query.contains("grupal", ignoreCase = true) && s.esGrupal)
                        }
                    }

                    if (filtrados.isEmpty()) {
                        EmptySearchState(
                            query    = query,
                            modifier = Modifier.align(Alignment.Center).padding(24.dp)
                        )
                    } else {
                        val grupos = filtrados.groupBy { it.categoria }

                        LazyColumn(
                            state          = listState,
                            contentPadding = PaddingValues(
                                start  = 16.dp,
                                end    = 16.dp,
                                top    = 8.dp,
                                bottom = 90.dp
                            )
                        ) {
                            grupos.forEach { (categoria, servicios) ->

                                item(key = "header_$categoria") {
                                    CategoriaHeader(
                                        categoria     = categoria,
                                        cantidad      = servicios.size,
                                        tieneGrupales = servicios.any { it.esGrupal }
                                    )
                                }

                                items(items = servicios, key = { it.id }) { servicio ->
                                    val estaSeleccionado = serviciosSeleccionados.contains(servicio)

                                    ServicioRow(
                                        servicio         = servicio,
                                        estaSeleccionado = estaSeleccionado,
                                        onClick          = {
                                            if (estaSeleccionado) {
                                                serviciosSeleccionados.remove(servicio)
                                            } else {
                                                if (servicio.esGrupal) {
                                                    serviciosSeleccionados.clear()
                                                } else {
                                                    serviciosSeleccionados.removeAll { it.esGrupal }
                                                }
                                                serviciosSeleccionados.add(servicio)
                                            }
                                        },
                                        modifier = Modifier.padding(bottom = 8.dp)
                                    )
                                }

                                item(key = "spacer_$categoria") {
                                    Spacer(Modifier.height(4.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ─── Buscador M3 ──────────────────────────────────────────────────────────────

@Composable
private fun SearchBar(
    query:          String,
    onQueryChange:  (String) -> Unit,
    onClear:        () -> Unit,
    focusRequester: FocusRequester,
    modifier:       Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current

    OutlinedTextField(
        value         = query,
        onValueChange = onQueryChange,
        modifier      = modifier.focusRequester(focusRequester),
        placeholder   = {
            Text(
                text  = "Buscar servicio o categoría...",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        },
        leadingIcon = {
            Icon(
                imageVector        = Icons.Default.Search,
                contentDescription = null,
                modifier           = Modifier.size(18.dp),
                tint               = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        trailingIcon = {
            AnimatedVisibility(
                visible = query.isNotEmpty(),
                enter   = fadeIn() + scaleIn(),
                exit    = fadeOut() + scaleOut()
            ) {
                IconButton(onClick = onClear) {
                    Icon(
                        imageVector        = Icons.Default.Clear,
                        contentDescription = "Limpiar",
                        modifier           = Modifier.size(16.dp),
                        tint               = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        singleLine      = true,
        shape           = RoundedCornerShape(14.dp),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
        colors          = OutlinedTextFieldDefaults.colors(
            focusedBorderColor      = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor    = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
            focusedContainerColor   = MaterialTheme.colorScheme.surfaceContainerLow,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        textStyle = MaterialTheme.typography.bodyMedium
    )
}

// ─── Header de Categoría M3 ───────────────────────────────────────────────────

@Composable
private fun CategoriaHeader(
    categoria:     String,
    cantidad:      Int,
    tieneGrupales: Boolean = false
) {
    Row(
        modifier              = Modifier
            .fillMaxWidth()
            .padding(top = 14.dp, bottom = 8.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(4.dp, 16.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(MaterialTheme.colorScheme.primary)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text          = categoria.uppercase(),
                style         = MaterialTheme.typography.labelMedium,
                fontWeight    = FontWeight.Bold,
                letterSpacing = 1.sp,
                color         = MaterialTheme.colorScheme.onBackground
            )
            if (tieneGrupales) {
                Spacer(Modifier.width(8.dp))
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.6f)
                ) {
                    Row(
                        modifier          = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector        = Icons.Default.Groups,
                            contentDescription = "Grupales",
                            modifier           = Modifier.size(13.dp),
                            tint               = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                        Spacer(Modifier.width(3.dp))
                        Text(
                            text  = "Grupales",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                }
            }
        }
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Text(
                text     = "$cantidad",
                style    = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color    = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
            )
        }
    }
}

// ─── Tarjeta Fila de Servicio M3 ──────────────────────────────────────────────

@Composable
private fun ServicioRow(
    servicio:         Servicio,
    estaSeleccionado: Boolean,
    onClick:          () -> Unit,
    modifier:         Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary

    OutlinedCard(
        onClick  = onClick,
        modifier = modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(16.dp),
        colors   = CardDefaults.outlinedCardColors(
            containerColor = if (estaSeleccionado)
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
            else
                MaterialTheme.colorScheme.surfaceContainerLow
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = if (estaSeleccionado) 1.5.dp else 1.dp,
            color = if (estaSeleccionado) primaryColor else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier          = Modifier
                .padding(horizontal = 14.dp, vertical = 12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked         = estaSeleccionado,
                onCheckedChange = { onClick() },
                modifier        = Modifier.padding(end = 6.dp)
            )

            // Icono de Servicio o Inicial del Servicio
            Box(
                modifier         = Modifier.size(42.dp),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (servicio.esGrupal)
                        MaterialTheme.colorScheme.tertiaryContainer
                    else
                        MaterialTheme.colorScheme.primaryContainer
                ) {
                    Box(
                        modifier         = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        if (servicio.esGrupal) {
                            Icon(
                                imageVector        = Icons.Default.Groups,
                                contentDescription = null,
                                modifier           = Modifier.size(22.dp),
                                tint               = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        } else if (!servicio.icono.isNullOrBlank() && mapIcon(servicio.icono) != Icons.Default.Circle) {
                            Icon(
                                imageVector        = mapIcon(servicio.icono),
                                contentDescription = null,
                                modifier           = Modifier.size(20.dp),
                                tint               = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        } else {
                            Text(
                                text       = servicio.nombre.take(1).uppercase(),
                                style      = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color      = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text       = servicio.nombre,
                        style      = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color      = MaterialTheme.colorScheme.onSurface,
                        maxLines   = 1,
                        overflow   = TextOverflow.Ellipsis,
                        modifier   = Modifier.weight(1f, fill = false)
                    )
                    if (servicio.esGrupal) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.tertiaryContainer
                        ) {
                            Text(
                                text       = "Grupal",
                                style      = MaterialTheme.typography.labelSmall,
                                color      = MaterialTheme.colorScheme.onTertiaryContainer,
                                fontWeight = FontWeight.Bold,
                                modifier   = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                servicio.descripcion?.let { desc ->
                    if (desc.isNotBlank()) {
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text     = desc,
                            style    = MaterialTheme.typography.bodySmall,
                            color    = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(Modifier.height(4.dp))

                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector        = Icons.Default.Schedule,
                            contentDescription = null,
                            modifier           = Modifier.size(13.dp),
                            tint               = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text  = formatearDuracion(servicio.duracionMinutos),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text       = "$%,.0f".format(servicio.precioBase),
                            style      = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color      = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier   = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

private fun formatearDuracion(minutos: Int): String {
    if (minutos < 60) return "$minutos min"
    val h = minutos / 60
    val m = minutos % 60
    return if (m == 0) "${h}h" else "${h}h ${m}min"
}

// ─── Estados M3 con Canvas ────────────────────────────────────────────────────

@Composable
private fun LoadingState(modifier: Modifier = Modifier) {
    Column(
        modifier            = modifier,
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
            text  = "Cargando catálogo de servicios…",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ErrorState(
    mensaje:  String,
    modifier: Modifier = Modifier
) {
    OutlinedCard(
        modifier = modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(24.dp),
        colors   = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier            = Modifier.padding(32.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.errorContainer
            ) {
                Icon(
                    imageVector        = Icons.Default.CloudOff,
                    contentDescription = null,
                    modifier           = Modifier.padding(16.dp).size(36.dp),
                    tint               = MaterialTheme.colorScheme.onErrorContainer
                )
            }
            Text(
                text       = "Error al cargar",
                style      = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text      = mensaje,
                style     = MaterialTheme.typography.bodyMedium,
                color     = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun EmptySearchState(
    query:    String,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary

    OutlinedCard(
        modifier = modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(24.dp),
        colors   = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(
            modifier            = Modifier.padding(32.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier         = Modifier.size(72.dp),
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
                        modifier           = Modifier.padding(16.dp).size(32.dp),
                        tint               = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Text(
                text       = "Sin coincidencias",
                style      = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text      = "No encontramos servicios para \"$query\"",
                style     = MaterialTheme.typography.bodyMedium,
                color     = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}
