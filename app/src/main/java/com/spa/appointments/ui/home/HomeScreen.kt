package com.spa.appointments.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.spa.appointments.core.utils.mapIcon
import com.spa.appointments.domain.model.EstadoLicencia
import com.spa.appointments.domain.model.Modulo
import com.spa.appointments.core.security.TokenStorage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onLogout:   () -> Unit,
    onNavigate: (String) -> Unit,
    pendingDestination: MutableState<String?> = mutableStateOf(null),
    vm: HomeViewModel = hiltViewModel()
) {
    val uiState by vm.uiState.collectAsState()

    val destino = pendingDestination.value
    if (destino != null) {
        AlertDialog(
            onDismissRequest = { pendingDestination.value = null },
            icon = {
                Icon(
                    Icons.Default.Notifications,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            title = { Text("Tienes una actualización") },
            text  = {
                Text(
                    when (destino) {
                        "mis_citas" -> "Hay novedades en tus citas. ¿Deseas verlas ahora?"
                        "historial" -> "Hay novedades en tu historial. ¿Deseas verlo ahora?"
                        else        -> "Tienes una nueva notificación."
                    }
                )
            },
            confirmButton = {
                Button(onClick = {
                    pendingDestination.value = null
                    onNavigate(destino)
                }) { Text("Ver ahora") }
            },
            dismissButton = {
                OutlinedButton(onClick = { pendingDestination.value = null }) {
                    Text("Después")
                }
            }
        )
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    when (val s = uiState) {
                        is HomeUiState.Success -> Text("Hola, ${s.userName}")
                        else                   -> Text("Dashboard")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        vm.logout()
                        onLogout()
                    }) {
                        Icon(
                            imageVector        = Icons.Default.ExitToApp,
                            contentDescription = "Cerrar sesión"
                        )
                    }
                }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // ── Banner de licencia (solo si expira hoy) ──────────────────

            if (vm.licenciaEstado == EstadoLicencia.EXPIRA_HOY) {
                BannerDemoExpiracion(mensaje = vm.licenciaMensaje)
            }

            // ── Contenido principal ──────────────────────────────────────
            Box(modifier = Modifier.fillMaxSize()) {
                when (val state = uiState) {

                    is HomeUiState.Loading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }

                    is HomeUiState.Empty -> {
                        Column(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector        = Icons.Default.Warning,
                                contentDescription = null,
                                tint               = MaterialTheme.colorScheme.secondary,
                                modifier           = Modifier.size(48.dp)
                            )
                            Spacer(Modifier.height(16.dp))
                            Text(
                                text  = "Sin acceso",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text      = "Tu usuario no tiene módulos asignados.\nContacta al administrador.",
                                style     = MaterialTheme.typography.bodyMedium,
                                color     = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                            Spacer(Modifier.height(24.dp))
                            OutlinedButton(onClick = {
                                vm.logout()
                                onLogout()
                            }) {
                                Text("Cerrar sesión")
                            }
                        }
                    }

                    is HomeUiState.Error -> {
                        Column(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text      = "Error al cargar el menú",
                                style     = MaterialTheme.typography.titleMedium
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text      = state.mensaje,
                                style     = MaterialTheme.typography.bodySmall,
                                color     = MaterialTheme.colorScheme.error,
                                textAlign = TextAlign.Center
                            )
                            Spacer(Modifier.height(16.dp))
                            Button(onClick = {
                                vm.logout()
                                onLogout()
                            }) {
                                Text("Volver al login")
                            }
                        }
                    }

                    is HomeUiState.Success -> {
                        MenuDinamico(
                            modulos    = state.modulos,
                            onNavigate = onNavigate
                        )
                    }
                }
            }
        }
    }
}

// ── Banner demo expiración ────────────────────────────────────────────────────
@Composable
private fun BannerDemoExpiracion(mensaje: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color    = MaterialTheme.colorScheme.errorContainer
    ) {
        Row(
            modifier              = Modifier.padding(12.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector        = Icons.Default.Warning,
                contentDescription = null,
                tint               = MaterialTheme.colorScheme.error,
                modifier           = Modifier.size(20.dp)
            )
            Text(
                text     = mensaje,
                style    = MaterialTheme.typography.bodySmall,
                color    = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

// ── Menú dinámico ─────────────────────────────────────────────────────────────
@Composable
private fun MenuDinamico(
    modulos:    List<Modulo>,
    onNavigate: (String) -> Unit
) {
    LazyColumn(
        contentPadding      = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(modulos) { modulo ->
            ModuloCard(modulo = modulo, onNavigate = onNavigate)
        }
    }
}

@Composable
private fun ModuloCard(
    modulo:    Modulo,
    onNavigate: (String) -> Unit
) {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // Cabecera del módulo
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector        = mapIcon(modulo.icono),
                    contentDescription = modulo.modulo,
                    tint               = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text       = modulo.modulo,
                    style      = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(8.dp))
            HorizontalDivider()
            Spacer(Modifier.height(8.dp))

            // Submódulos
            modulo.submodulos?.forEach { sub ->
                TextButton(
                    onClick        = { onNavigate(sub.ruta) },
                    modifier       = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    Icon(
                        imageVector        = mapIcon(sub.icono),
                        contentDescription = sub.nombre,
                        modifier           = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text     = sub.nombre,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        imageVector        = Icons.Default.KeyboardArrowRight,
                        contentDescription = null,
                        modifier           = Modifier.size(16.dp),
                        tint               = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}