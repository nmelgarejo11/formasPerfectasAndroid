// Ruta: app/src/main/java/com/spa/appointments/ui/home/HomeScreen.kt
package com.spa.appointments.ui.home

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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

// ─── Screen principal ─────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onLogout:           () -> Unit,
    onNavigate:         (String) -> Unit,
    pendingDestination: MutableState<String?> = mutableStateOf(null),
    vm: HomeViewModel   = hiltViewModel()
) {
    val uiState by vm.uiState.collectAsState()

    val userName = (uiState as? HomeUiState.Success)?.userName ?: ""
    val inicial  = userName.firstOrNull()?.uppercase() ?: "U"

    // ── Diálogo de notificación pendiente ────────────────────────────────────
    val destino = pendingDestination.value
    if (destino != null) {
        AlertDialog(
            onDismissRequest = { pendingDestination.value = null },
            shape = RoundedCornerShape(16.dp),
            icon  = {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Icon(
                        Icons.Default.Notifications, null,
                        tint     = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(10.dp).size(22.dp)
                    )
                }
            },
            title = { Text("Tienes una actualización", fontWeight = FontWeight.Bold) },
            text  = {
                Text(
                    text  = when (destino) {
                        "mis_citas" -> "Hay novedades en tus citas. ¿Deseas verlas ahora?"
                        "historial" -> "Hay novedades en tu historial. ¿Deseas verlo ahora?"
                        else        -> "Tienes una nueva notificación."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                Button(
                    onClick = { pendingDestination.value = null; onNavigate(destino) },
                    shape   = RoundedCornerShape(10.dp)
                ) { Text("Ver ahora") }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { pendingDestination.value = null },
                    shape   = RoundedCornerShape(10.dp)
                ) { Text("Después") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Avatar con inicial del usuario
                        Surface(
                            shape    = RoundedCornerShape(50),
                            color    = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text       = inicial,
                                    style      = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color      = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                        Spacer(Modifier.width(10.dp))
                        Column {
                            when (val s = uiState) {
                                is HomeUiState.Success -> {
                                    Text(
                                        text       = "Hola, ${s.userName}",
                                        style      = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text  = "¿Qué hacemos hoy?",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                else -> Text(
                                    text       = "Dashboard",
                                    style      = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { vm.logout(); onLogout() }) {
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
            // ── Banner licencia ──────────────────────────────────────────
            AnimatedVisibility(
                visible = vm.licenciaEstado == EstadoLicencia.EXPIRA_HOY,
                enter   = expandVertically() + fadeIn(),
                exit    = shrinkVertically() + fadeOut()
            ) {
                BannerDemoExpiracion(mensaje = vm.licenciaMensaje)
            }

            // ── Contenido principal ──────────────────────────────────────
            Box(modifier = Modifier.fillMaxSize()) {
                when (val state = uiState) {

                    is HomeUiState.Loading -> {
                        Column(
                            modifier            = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            CircularProgressIndicator()
                            Text(
                                "Cargando menú…",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    is HomeUiState.Empty -> {
                        Column(
                            modifier            = Modifier
                                .align(Alignment.Center)
                                .padding(40.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(24.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Icon(
                                    Icons.Default.LockPerson, null,
                                    modifier = Modifier.padding(20.dp).size(48.dp),
                                    tint     = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text       = "Sin acceso",
                                style      = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text      = "Tu usuario no tiene módulos asignados.\nContacta al administrador.",
                                style     = MaterialTheme.typography.bodyMedium,
                                color     = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                            OutlinedButton(
                                onClick = { vm.logout(); onLogout() },
                                shape   = RoundedCornerShape(10.dp)
                            ) { Text("Cerrar sesión") }
                        }
                    }

                    is HomeUiState.Error -> {
                        Column(
                            modifier            = Modifier
                                .align(Alignment.Center)
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = MaterialTheme.colorScheme.errorContainer
                            ) {
                                Icon(
                                    Icons.Default.CloudOff, null,
                                    modifier = Modifier.padding(16.dp).size(32.dp),
                                    tint     = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                            Text(
                                text       = "Error al cargar el menú",
                                style      = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text      = state.mensaje,
                                style     = MaterialTheme.typography.bodySmall,
                                color     = MaterialTheme.colorScheme.error,
                                textAlign = TextAlign.Center
                            )
                            Button(
                                onClick = { vm.logout(); onLogout() },
                                shape   = RoundedCornerShape(10.dp)
                            ) { Text("Volver al login") }
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

// ─── Banner de licencia ───────────────────────────────────────────────────────

@Composable
private fun BannerDemoExpiracion(mensaje: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color    = MaterialTheme.colorScheme.errorContainer
    ) {
        Row(
            modifier          = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(50),
                color = MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
            ) {
                Icon(
                    imageVector        = Icons.Default.Warning,
                    contentDescription = null,
                    tint               = MaterialTheme.colorScheme.error,
                    modifier           = Modifier.padding(5.dp).size(16.dp)
                )
            }
            Spacer(Modifier.width(10.dp))
            Text(
                text     = mensaje,
                style    = MaterialTheme.typography.bodySmall,
                color    = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

// ─── Menú dinámico ────────────────────────────────────────────────────────────

@Composable
private fun MenuDinamico(
    modulos:    List<Modulo>,
    onNavigate: (String) -> Unit
) {
    LazyColumn(
        contentPadding      = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(modulos) { modulo ->
            ModuloCard(modulo = modulo, onNavigate = onNavigate)
        }
    }
}

// ─── Card de módulo ───────────────────────────────────────────────────────────

@Composable
private fun ModuloCard(
    modulo:     Modulo,
    onNavigate: (String) -> Unit
) {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors    = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp, MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {

            // ── Cabecera del módulo ───────────────────────────────────────
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier          = Modifier.padding(bottom = 10.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Icon(
                        imageVector        = mapIcon(modulo.icono),
                        contentDescription = modulo.modulo,
                        tint               = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier           = Modifier.padding(8.dp).size(20.dp)
                    )
                }
                Spacer(Modifier.width(10.dp))
                Text(
                    text       = modulo.modulo,
                    style      = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            HorizontalDivider(
                color    = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                modifier = Modifier.padding(bottom = 6.dp)
            )

            // ── Submódulos ────────────────────────────────────────────────
            modulo.submodulos?.forEach { sub ->
                Surface(
                    shape    = RoundedCornerShape(10.dp),
                    color    = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextButton(
                        onClick        = { onNavigate(sub.ruta) },
                        modifier       = Modifier.fillMaxWidth(),
                        shape          = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 10.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            Icon(
                                imageVector        = mapIcon(sub.icono),
                                contentDescription = sub.nombre,
                                modifier           = Modifier.padding(6.dp).size(16.dp),
                                tint               = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text       = sub.nombre,
                            modifier   = Modifier.weight(1f),
                            style      = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            textAlign  = TextAlign.Start
                        )
                        Icon(
                            imageVector        = Icons.Default.ChevronRight,
                            contentDescription = null,
                            modifier           = Modifier.size(18.dp),
                            tint               = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}