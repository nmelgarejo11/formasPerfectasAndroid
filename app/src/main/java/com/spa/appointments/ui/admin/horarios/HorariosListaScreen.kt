// Ruta: app/src/main/java/com/spa/appointments/ui/admin/horarios/HorariosListaScreen.kt
package com.spa.appointments.ui.admin.horarios

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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.spa.appointments.ui.admin.profesionales.ProfesionalesAdminViewModel
import com.spa.appointments.ui.admin.profesionales.ProfesionalesUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HorariosListaScreen(
    onBack:        () -> Unit,
    onSeleccionar: (Int) -> Unit,
    viewModel: ProfesionalesAdminViewModel = hiltViewModel()
) {
    val profesionales by viewModel.profesionales.collectAsState()
    val uiState       by viewModel.uiState.collectAsState()
    var busqueda      by remember { mutableStateOf("") }

    val activos   = profesionales.filter { it.estado }
    val filtrados = remember(activos, busqueda) {
        if (busqueda.isBlank()) activos
        else activos.filter {
            it.nombre.contains(busqueda, ignoreCase = true) ||
                    it.apellido.contains(busqueda, ignoreCase = true)
        }
    }

    LaunchedEffect(Unit) { viewModel.cargarDatos() }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text       = "Horarios",
                                style      = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text  = "Selecciona un profesional",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
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
        }
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
                label         = { Text("Buscar profesional") },
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
                    uiState is ProfesionalesUiState.Loading && profesionales.isEmpty() -> {
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
                                "Cargando profesionales…",
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
                                            imageVector        = if (busqueda.isBlank()) Icons.Default.Group
                                            else Icons.Default.SearchOff,
                                            contentDescription = null,
                                            modifier           = Modifier.padding(18.dp).size(36.dp),
                                            tint               = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                Text(
                                    text       = if (busqueda.isBlank()) "Sin profesionales activos" else "Sin resultados",
                                    style      = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color      = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text      = if (busqueda.isBlank())
                                        "No hay profesionales activos para gestionar horarios."
                                    else
                                        "Ningún profesional coincide con \"$busqueda\".",
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
                                horizontal = 16.dp,
                                vertical   = 8.dp
                            ),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(filtrados, key = { it.id }) { prof ->
                                ProfesionalHorarioCard(
                                    nombre  = "${prof.nombre} ${prof.apellido}",
                                    cargo   = prof.nombreCargo,
                                    onClick = { onSeleccionar(prof.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ─── Card de Profesional M3 ───────────────────────────────────────────────────

@Composable
private fun ProfesionalHorarioCard(
    nombre:  String,
    cargo:   String,
    onClick: () -> Unit
) {
    val inicial = nombre.firstOrNull()?.uppercase() ?: "P"

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
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape    = CircleShape,
                color    = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(46.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text       = inicial,
                        style      = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color      = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text       = nombre,
                    style      = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color      = MaterialTheme.colorScheme.onSurface
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier          = Modifier.padding(top = 2.dp)
                ) {
                    Icon(
                        Icons.Default.Badge, null,
                        modifier = Modifier.size(13.dp),
                        tint     = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text  = cargo,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.secondaryContainer
            ) {
                Icon(
                    Icons.Default.EditCalendar, null,
                    modifier = Modifier.padding(8.dp).size(18.dp),
                    tint     = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }
}
