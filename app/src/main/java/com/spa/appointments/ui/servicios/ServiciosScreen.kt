package com.spa.appointments.ui.servicios

import androidx.compose.animation.*
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.spa.appointments.domain.model.Servicio

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiciosScreen(
    onBack: () -> Unit,
    onFinalizarSeleccion: (List<Servicio>) -> Unit, // Firma corregida para concordar con NavGraph
    vm: ServiciosViewModel = hiltViewModel()
) {
    val uiState by vm.uiState.collectAsState()
    val listState = rememberLazyListState()
    var query by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }

    // Estado local para persistir múltiples ítems seleccionados
    val serviciosSeleccionados = remember { mutableStateListOf<Servicio>() }

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
                                fontWeight = FontWeight.SemiBold
                            )

                            val subtitulo = when (val state = uiState) {
                                is ServiciosUiState.Loading -> "Cargando servicios..."
                                is ServiciosUiState.Error   -> "Ocurrió un error"
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
                                Text(
                                    text  = subtitulo,
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
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )

                SearchBar(
                    query          = query,
                    onQueryChange  = { query = it },
                    onClear        = { query = ""; focusManager.clearFocus() },
                    focusRequester = focusRequester,
                    modifier       = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 10.dp)
                )

                HorizontalDivider(
                    thickness = 0.5.dp,
                    color     = MaterialTheme.colorScheme.outlineVariant
                )
            }
        },
        bottomBar = {
            AnimatedVisibility(
                visible = serviciosSeleccionados.isNotEmpty(),
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
            ) {
                Surface(
                    tonalElevation = 4.dp,
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = { onFinalizarSeleccion(serviciosSeleccionados.toList()) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .height(48.dp)
                    ) {
                        Text(
                            text = if (serviciosSeleccionados.size == 1) "Continuar con 1 servicio"
                            else "Continuar con ${serviciosSeleccionados.size} servicios",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
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
                                bottom = 80.dp // Margen extra para que el botón flotante no tape elementos
                            )
                        ) {
                            grupos.forEach { (categoria, servicios) ->

                                item(key = "header_$categoria") {
                                    CategoriaHeader(
                                        categoria = categoria,
                                        cantidad  = servicios.size,
                                        tieneGrupales = servicios.any { it.esGrupal }
                                    )
                                }

                                items(items = servicios, key = { it.id }) { servicio ->
                                    val estaSeleccionado = serviciosSeleccionados.contains(servicio)

                                    ServicioRow(
                                        servicio = servicio,
                                        estaSeleccionado = estaSeleccionado,
                                        onClick  = {
                                            if (estaSeleccionado) {
                                                serviciosSeleccionados.remove(servicio)
                                            } else {
                                                if (servicio.esGrupal) {
                                                    // Las citas grupales van por flujo solitario cerrado
                                                    serviciosSeleccionados.clear()
                                                } else {
                                                    // Si mete uno tradicional, limpia rastros grupales del array
                                                    serviciosSeleccionados.removeAll { it.esGrupal }
                                                }
                                                serviciosSeleccionados.add(servicio)
                                            }
                                        },
                                        modifier = Modifier.padding(bottom = 6.dp)
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

// ─── Buscador ─────────────────────────────────────────────────────────────────

@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClear: () -> Unit,
    focusRequester: FocusRequester,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current

    OutlinedTextField(
        value         = query,
        onValueChange = onQueryChange,
        modifier      = modifier.focusRequester(focusRequester),
        placeholder   = {
            Text(
                text  = "Buscar servicio o categoría",
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
        shape           = RoundedCornerShape(12.dp),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
        colors          = OutlinedTextFieldDefaults.colors(
            focusedBorderColor      = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor    = MaterialTheme.colorScheme.outlineVariant,
            focusedContainerColor   = MaterialTheme.colorScheme.surfaceContainerLow,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        textStyle = MaterialTheme.typography.bodySmall
    )
}

// ─── Header de categoría ──────────────────────────────────────────────────────

@Composable
private fun CategoriaHeader(
    categoria: String,
    cantidad: Int,
    tieneGrupales: Boolean = false
) {
    Row(
        modifier              = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp, bottom = 6.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(3.dp, 14.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(MaterialTheme.colorScheme.primary)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text          = categoria.uppercase(),
                style         = MaterialTheme.typography.labelSmall,
                fontWeight    = FontWeight.Bold,
                letterSpacing = 1.sp,
                color         = MaterialTheme.colorScheme.onBackground
            )
            if (tieneGrupales) {
                Spacer(Modifier.width(6.6.dp))
                Icon(
                    imageVector        = Icons.Default.Groups,
                    contentDescription = "Tiene servicios grupales",
                    modifier           = Modifier.size(14.dp),
                    tint               = MaterialTheme.colorScheme.tertiary
                )
            }
        }
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Text(
                text     = "$cantidad",
                style    = MaterialTheme.typography.labelSmall,
                color    = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
            )
        }
    }
}

// ─── Fila de servicio ─────────────────────────────────────────────────────────

@Composable
private fun ServicioRow(
    servicio: Servicio,
    estaSeleccionado: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick        = onClick,
        modifier       = modifier.fillMaxWidth(),
        shape          = RoundedCornerShape(12.dp),
        color          = if (estaSeleccionado) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        else MaterialTheme.colorScheme.surfaceContainerLow,
        border         = if (estaSeleccionado) androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null
    ) {
        Row(
            modifier          = Modifier
                .padding(horizontal = 12.dp, vertical = 10.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Checkbox lateral integrado para control múltiple explícito
            Checkbox(
                checked = estaSeleccionado,
                onCheckedChange = { onClick() },
                modifier = Modifier.padding(end = 4.dp)
            )

            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        if (servicio.esGrupal)
                            Brush.linearGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.tertiaryContainer,
                                    MaterialTheme.colorScheme.secondaryContainer
                                )
                            )
                        else
                            Brush.linearGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primaryContainer,
                                    MaterialTheme.colorScheme.secondaryContainer
                                )
                            )
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (servicio.esGrupal) {
                    Icon(
                        imageVector        = Icons.Default.Groups,
                        contentDescription = null,
                        modifier           = Modifier.size(20.dp),
                        tint               = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                } else {
                    Text(
                        text       = servicio.nombre.take(1).uppercase(),
                        style      = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color      = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text      = servicio.nombre,
                        style     = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color     = MaterialTheme.colorScheme.onSurface,
                        maxLines  = 1,
                        overflow  = TextOverflow.Ellipsis,
                        modifier  = Modifier.weight(1f, fill = false)
                    )
                    if (servicio.esGrupal) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text       = "Grupal",
                                style      = MaterialTheme.typography.labelSmall,
                                color      = MaterialTheme.colorScheme.tertiary,
                                fontWeight = FontWeight.SemiBold,
                                modifier   = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                            )
                        }
                    }
                }
                Spacer(Modifier.height(2.dp))
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector        = if (servicio.esGrupal) Icons.Default.Groups else Icons.Default.Person,
                        contentDescription = null,
                        modifier           = Modifier.size(11.dp),
                        tint               = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text  = if (servicio.esGrupal) "Grupal · ${formatearDuracion(servicio.duracionMinutos)}"
                        else formatearDuracion(servicio.duracionMinutos),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Text(
                text       = "$${"%,.0f".format(servicio.precioBase)}",
                style      = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color      = MaterialTheme.colorScheme.primary
            )
        }
    }
}

private fun formatearDuracion(minutos: Int): String {
    if (minutos < 60) return "$minutos min"
    val h = minutos / 60
    val m = minutos % 60
    return if (m == 0) "${h}h" else "${h}h ${m}min"
}

// ─── Estados ──────────────────────────────────────────────────────────────────

@Composable
private fun LoadingState(modifier: Modifier = Modifier) {
    Column(
        modifier            = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        CircularProgressIndicator(
            color       = MaterialTheme.colorScheme.primary,
            strokeWidth = 2.5.dp,
            modifier    = Modifier.size(32.dp)
        )
        Text(
            text  = "Cargando servicios...",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ErrorState(
    mensaje: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier            = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(text = "😕", fontSize = 36.sp)
        Text(
            text       = "Algo salió mal",
            style      = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color      = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text  = mensaje,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun EmptySearchState(
    query: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier            = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(text = "🔍", fontSize = 36.sp)
        Text(
            text       = "Sin resultados",
            style      = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color      = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text  = "No encontramos servicios para \"$query\"",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}