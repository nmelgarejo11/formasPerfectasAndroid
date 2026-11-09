// Ruta: app/src/main/java/com/spa/appointments/ui/profesionales/ProfesionalesScreen.kt
package com.spa.appointments.ui.profesionales

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.People
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.spa.appointments.domain.model.Profesional
import com.spa.appointments.ui.reserva.ReservaSharedViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfesionalesScreen(
    onBack:                   () -> Unit,
    onSeleccionarProfesional: (Profesional) -> Unit,
    vm: ProfesionalesViewModel = hiltViewModel(),
    sharedVm: ReservaSharedViewModel? = null
) {
    val uiState by vm.uiState.collectAsState()
    var busqueda by remember { mutableStateOf("") }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Column {
                if (sharedVm != null && sharedVm.esGrupal) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Group,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "Cita Grupal — Asistente ${sharedVm.asistenteActualIndex + 1} de ${sharedVm.cantidadPersonas}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }

                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text       = "Profesional",
                                style      = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            if (uiState is ProfesionalesUiState.Success) {
                                val count = (uiState as ProfesionalesUiState.Success).items.size
                                Surface(
                                    shape    = RoundedCornerShape(50),
                                    color    = MaterialTheme.colorScheme.primaryContainer,
                                    modifier = Modifier.padding(top = 2.dp)
                                ) {
                                    Text(
                                        text     = "$count disponibles",
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

                Spacer(Modifier.height(8.dp))

                // ── Buscador ──────────────────────────────────────────────────
                OutlinedTextField(
                    value         = busqueda,
                    onValueChange = { busqueda = it },
                    placeholder   = {
                        Text(
                            "Buscar por nombre o cargo...",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Search, null,
                            modifier = Modifier.size(18.dp),
                            tint     = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    trailingIcon = {
                        if (busqueda.isNotBlank()) {
                            IconButton(onClick = { busqueda = "" }) {
                                Icon(
                                    Icons.Default.Clear, null,
                                    modifier = Modifier.size(16.dp),
                                    tint     = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 8.dp),
                    shape           = RoundedCornerShape(14.dp),
                    singleLine      = true,
                    colors          = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor      = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor    = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
                        focusedContainerColor   = MaterialTheme.colorScheme.surfaceContainerLow,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    ),
                    textStyle = MaterialTheme.typography.bodyMedium
                )

                HorizontalDivider(
                    thickness = 0.5.dp,
                    color     = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (val state = uiState) {
                is ProfesionalesUiState.Loading -> {
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
                            text  = "Cargando profesionales…",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                is ProfesionalesUiState.Error -> {
                    ProfesionalErrorState(
                        mensaje  = state.mensaje,
                        modifier = Modifier.align(Alignment.Center).padding(24.dp)
                    )
                }

                is ProfesionalesUiState.Success -> {
                    val filtrados = if (busqueda.isBlank()) state.items
                    else state.items.filter {
                        it.nombreCompleto.contains(busqueda, ignoreCase = true) ||
                                it.cargo.contains(busqueda, ignoreCase = true)
                    }

                    if (filtrados.isEmpty()) {
                        ProfesionalEmptyState(
                            isSearch = busqueda.isNotBlank(),
                            modifier = Modifier.align(Alignment.Center).padding(24.dp)
                        )
                    } else {
                        LazyColumn(
                            contentPadding      = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(filtrados, key = { it.id }) { profesional ->
                                ProfesionalCard(
                                    profesional = profesional,
                                    onClick     = { onSeleccionarProfesional(profesional) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ─── Tarjeta Profesional M3 ──────────────────────────────────────────────────

@Composable
private fun ProfesionalCard(
    profesional: Profesional,
    onClick:     () -> Unit
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val iniciales    = profesional.nombreCompleto.split(" ")
        .mapNotNull { it.firstOrNull() }
        .take(2)
        .joinToString("")
        .uppercase()

    OutlinedCard(
        onClick  = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(18.dp),
        colors   = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
    ) {
        Row(
            modifier          = Modifier
                .padding(horizontal = 14.dp, vertical = 14.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // ── Avatar Estilizado con Anillo Canvas ─────────────────────────
            Box(
                modifier         = Modifier.size(56.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawCircle(
                        color = primaryColor.copy(alpha = 0.25f),
                        style = Stroke(width = 2.dp.toPx())
                    )
                }

                if (!profesional.foto.isNullOrBlank()) {
                    AsyncImage(
                        model              = profesional.foto,
                        contentDescription = null,
                        contentScale       = ContentScale.Crop,
                        modifier           = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                    )
                } else {
                    Surface(
                        modifier = Modifier.size(48.dp),
                        shape    = CircleShape,
                        color    = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text       = iniciales,
                                style      = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color      = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.width(14.dp))

            // ── Información ───────────────────────────────────────────────
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text       = profesional.nombreCompleto,
                    style      = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color      = MaterialTheme.colorScheme.onSurface
                )

                Spacer(Modifier.height(2.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Badge, null,
                        modifier = Modifier.size(14.dp),
                        tint     = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text  = profesional.cargo,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(Modifier.height(2.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.LocationOn, null,
                        modifier = Modifier.size(14.dp),
                        tint     = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text  = profesional.sede,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Icon(
                imageVector        = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint               = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier           = Modifier.size(22.dp)
            )
        }
    }
}

// ─── Estados Vacío y Error ────────────────────────────────────────────────────

@Composable
private fun ProfesionalEmptyState(
    isSearch: Boolean,
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
                        imageVector        = if (isSearch) Icons.Default.Search else Icons.Outlined.People,
                        contentDescription = null,
                        modifier           = Modifier.padding(16.dp).size(32.dp),
                        tint               = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Text(
                text       = if (isSearch) "Sin resultados" else "No hay profesionales",
                style      = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text      = if (isSearch) "Intenta con otro nombre o cargo." else "No hay profesionales disponibles en este momento.",
                style     = MaterialTheme.typography.bodyMedium,
                color     = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun ProfesionalErrorState(
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
