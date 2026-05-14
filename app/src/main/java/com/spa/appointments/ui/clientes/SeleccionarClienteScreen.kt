package com.spa.appointments.ui.clientes

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.spa.appointments.domain.model.Cliente

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeleccionarClienteScreen(
    onBack: () -> Unit,
    onClienteSeleccionado: (Cliente) -> Unit,
    vm: SeleccionarClienteViewModel = hiltViewModel()
) {
    val uiState       by vm.uiState.collectAsState()
    val textoBusqueda by vm.textoBusqueda.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Cliente",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // ── Buscador ──────────────────────────────────────────────────────
            OutlinedTextField(
                value         = textoBusqueda,
                onValueChange = { vm.buscar(it) },
                placeholder   = { Text("Nombre o teléfono...") },
                leadingIcon   = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
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
                                imageVector = Icons.Outlined.Close,
                                contentDescription = "Limpiar",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                shape      = RoundedCornerShape(14.dp),
                singleLine = true,
                colors     = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor   = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                )
            )

            // ── Contenido ─────────────────────────────────────────────────────
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
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center),
                            color    = MaterialTheme.colorScheme.primary
                        )
                    }

                    is SeleccionarClienteUiState.SinResultados -> {
                        EstadoHint(
                            icon      = Icons.Outlined.SearchOff,
                            titulo    = "Sin resultados",
                            subtitulo = "No se encontraron clientes con \"$textoBusqueda\""
                        )
                    }

                    is SeleccionarClienteUiState.Resultados -> {
                        LazyColumn(
                            contentPadding      = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
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
                            icon      = Icons.Outlined.ErrorOutline,
                            titulo    = "Ocurrió un error",
                            subtitulo = state.mensaje,
                            esError   = true,
                            accionLabel = "Reintentar",
                            onAccion  = { vm.buscar(textoBusqueda) }
                        )
                    }

                    else -> Unit
                }
            }
        }
    }
}

// ── Item de cliente ───────────────────────────────────────────────────────────

@Composable
private fun ClienteSelectItem(cliente: Cliente, onClick: () -> Unit) {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border    = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier
                .clickable(onClick = onClick)
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar inicial
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
                    fontWeight = FontWeight.SemiBold,
                    color      = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(2.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    if (!cliente.telefono.isNullOrBlank()) {
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

// ── Estado vacío / error ──────────────────────────────────────────────────────

@Composable
private fun EstadoHint(
    icon: ImageVector,
    titulo: String,
    subtitulo: String,
    esError: Boolean = false,
    accionLabel: String? = null,
    onAccion: (() -> Unit)? = null
) {
    Column(
        modifier            = Modifier
            .fillMaxSize()
            .padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            shape = CircleShape,
            color = if (esError)
                MaterialTheme.colorScheme.errorContainer
            else
                MaterialTheme.colorScheme.surfaceVariant
        ) {
            Icon(
                imageVector        = icon,
                contentDescription = null,
                modifier = Modifier
                    .padding(20.dp)
                    .size(44.dp),
                tint = if (esError)
                    MaterialTheme.colorScheme.error
                else
                    MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(Modifier.height(20.dp))

        Text(
            text       = titulo,
            style      = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            textAlign  = TextAlign.Center,
            color      = MaterialTheme.colorScheme.onSurface
        )

        Spacer(Modifier.height(6.dp))

        Text(
            text      = subtitulo,
            style     = MaterialTheme.typography.bodyMedium,
            color     = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        if (accionLabel != null && onAccion != null) {
            Spacer(Modifier.height(20.dp))
            OutlinedButton(onClick = onAccion) {
                Text(accionLabel)
            }
        }
    }
}