// Ruta: app/src/main/java/com/spa/appointments/ui/clientes/SeleccionarClienteScreen.kt
package com.spa.appointments.ui.clientes

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.spa.appointments.domain.model.Cliente

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeleccionarClienteScreen(
    onBack:                 () -> Unit,
    onClienteSeleccionado: (Cliente) -> Unit,
    vm: SeleccionarClienteViewModel = hiltViewModel()
) {
    val uiState       by vm.uiState.collectAsState()
    val textoBusqueda by vm.textoBusqueda.collectAsState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text       = "Clientes",
                                style      = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )

                            val subtitulo = when (uiState) {
                                is SeleccionarClienteUiState.Resultados -> {
                                    val total = (uiState as SeleccionarClienteUiState.Resultados).clientes.size
                                    "$total ${if (total == 1) "cliente encontrado" else "clientes encontrados"}"
                                }
                                is SeleccionarClienteUiState.Idle          -> "Busca por nombre o teléfono"
                                is SeleccionarClienteUiState.Buscando      -> "Buscando clientes..."
                                is SeleccionarClienteUiState.SinResultados -> "Sin coincidencias"
                                else                                       -> ""
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
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Volver"
                            )
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
                    value         = textoBusqueda,
                    onValueChange = { vm.buscar(it) },
                    placeholder   = {
                        Text(
                            "Nombre o teléfono...",
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
                            visible = textoBusqueda.isNotEmpty(),
                            enter   = fadeIn() + scaleIn(),
                            exit    = fadeOut() + scaleOut()
                        ) {
                            IconButton(onClick = { vm.resetear() }) {
                                Icon(
                                    imageVector        = Icons.Default.Close,
                                    contentDescription = "Limpiar",
                                    modifier           = Modifier.size(16.dp),
                                    tint               = MaterialTheme.colorScheme.onSurfaceVariant
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                when (val state = uiState) {

                    is SeleccionarClienteUiState.Idle -> {
                        EstadoHint(
                            icon      = Icons.Outlined.PersonSearch,
                            titulo    = "Busca un cliente",
                            subtitulo = "Ingresa nombre o teléfono para comenzar"
                        )
                    }

                    is SeleccionarClienteUiState.Buscando -> {
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
                                text  = "Buscando clientes…",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    is SeleccionarClienteUiState.SinResultados -> {
                        EstadoHint(
                            icon      = Icons.Outlined.SearchOff,
                            titulo    = "Sin coincidencias",
                            subtitulo = "No se encontraron clientes con \"$textoBusqueda\""
                        )
                    }

                    is SeleccionarClienteUiState.Resultados -> {
                        LazyColumn(
                            contentPadding      = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(state.clientes, key = { it.id }) { cliente ->
                                ClienteSelectItem(
                                    cliente = cliente,
                                    onClick = { onClienteSeleccionado(cliente) }
                                )
                            }
                        }
                    }

                    is SeleccionarClienteUiState.Error -> {
                        EstadoHint(
                            icon        = Icons.Default.CloudOff,
                            titulo      = "Ocurrió un error",
                            subtitulo   = state.mensaje,
                            esError     = true,
                            accionLabel = "Reintentar",
                            onAccion    = { vm.buscar(textoBusqueda) }
                        )
                    }

                    else -> Unit
                }
            }
        }
    }
}

// ─── Item de Cliente M3 ───────────────────────────────────────────────────────

@Composable
private fun ClienteSelectItem(cliente: Cliente, onClick: () -> Unit) {
    OutlinedCard(
        onClick  = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(16.dp),
        colors   = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
    ) {
        Row(
            modifier          = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text       = cliente.nombre.firstOrNull()?.uppercase() ?: "?",
                        style      = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color      = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text       = cliente.nombreCompleto,
                    style      = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color      = MaterialTheme.colorScheme.onSurface
                )
                if (!cliente.telefono.isNullOrBlank()) {
                    Spacer(Modifier.height(2.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector        = Icons.Outlined.Phone,
                            contentDescription = null,
                            modifier           = Modifier.size(13.dp),
                            tint               = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text  = cliente.telefono,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Icon(
                imageVector        = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint               = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier           = Modifier.size(20.dp)
            )
        }
    }
}

// ─── Estado Vacío / Error M3 con Canvas ───────────────────────────────────────

@Composable
private fun EstadoHint(
    icon:        ImageVector,
    titulo:      String,
    subtitulo:   String,
    esError:     Boolean     = false,
    accionLabel: String?     = null,
    onAccion:    (() -> Unit)? = null
) {
    val primaryColor = MaterialTheme.colorScheme.primary

    OutlinedCard(
        modifier = Modifier
            .padding(24.dp)
            .fillMaxWidth(),
        shape  = RoundedCornerShape(24.dp),
        colors = CardDefaults.outlinedCardColors(
            containerColor = if (esError)
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
            else
                MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(
            modifier            = Modifier
                .padding(32.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier         = Modifier.size(72.dp),
                contentAlignment = Alignment.Center
            ) {
                if (!esError) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawCircle(color = primaryColor.copy(alpha = 0.15f))
                    }
                }
                Surface(
                    shape = CircleShape,
                    color = if (esError)
                        MaterialTheme.colorScheme.errorContainer
                    else
                        MaterialTheme.colorScheme.surfaceContainerHigh
                ) {
                    Icon(
                        imageVector        = icon,
                        contentDescription = null,
                        modifier           = Modifier.padding(16.dp).size(32.dp),
                        tint               = if (esError)
                            MaterialTheme.colorScheme.onErrorContainer
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Text(
                text       = titulo,
                style      = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign  = TextAlign.Center,
                color      = if (esError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
            )

            Text(
                text      = subtitulo,
                style     = MaterialTheme.typography.bodyMedium,
                color     = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            if (accionLabel != null && onAccion != null) {
                Button(
                    onClick = onAccion,
                    shape   = RoundedCornerShape(12.dp)
                ) {
                    Text(accionLabel)
                }
            }
        }
    }
}
