// Ruta: app/src/main/java/com/spa/appointments/ui/home/HomeScreen.kt
package com.spa.appointments.ui.home

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.spa.appointments.core.utils.mapIcon
import com.spa.appointments.domain.model.EstadoLicencia
import com.spa.appointments.domain.model.Modulo

// ─── Screen Principal ─────────────────────────────────────────────────────────

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
            shape = RoundedCornerShape(28.dp),
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            icon = {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Icon(
                        Icons.Default.Notifications, null,
                        tint     = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(12.dp).size(26.dp)
                    )
                }
            },
            title = {
                Text(
                    text       = "Novedad en Prioriti",
                    fontWeight = FontWeight.Bold,
                    style      = MaterialTheme.typography.titleMedium
                )
            },
            text = {
                Text(
                    text  = when (destino) {
                        "mis_citas" -> "Tienes actualizaciones en tus citas agendadas. ¿Deseas revisarlas ahora?"
                        "historial" -> "Hay nuevos registros en tu historial. ¿Deseas verlos ahora?"
                        else        -> "Tienes una nueva notificación disponible."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                Button(
                    onClick = { pendingDestination.value = null; onNavigate(destino) },
                    shape   = RoundedCornerShape(12.dp)
                ) { Text("Ver ahora") }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { pendingDestination.value = null },
                    shape   = RoundedCornerShape(12.dp)
                ) { Text("Después") }
            }
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // ── Hero Dashboard Header con Canvas ─────────────────────────────
            DashboardHeroHeader(
                userName  = userName,
                inicial   = inicial,
                onLogout  = { vm.logout(); onLogout() }
            )

            // ── Banner Licencia Expira Hoy ──────────────────────────────────
            AnimatedVisibility(
                visible = vm.licenciaEstado == EstadoLicencia.EXPIRA_HOY,
                enter   = expandVertically() + fadeIn(),
                exit    = shrinkVertically() + fadeOut()
            ) {
                BannerDemoExpiracion(mensaje = vm.licenciaMensaje)
            }

            // ── Contenido Principal de Estados ──────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                when (val state = uiState) {

                    is HomeUiState.Loading -> {
                        Column(
                            modifier            = Modifier
                                .align(Alignment.Center)
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.primaryContainer
                            )
                            Text(
                                "Cargando tus módulos…",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    is HomeUiState.Empty -> {
                        OutlinedCard(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(24.dp)
                                .fillMaxWidth(),
                            shape  = RoundedCornerShape(24.dp),
                            colors = CardDefaults.outlinedCardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                            )
                        ) {
                            Column(
                                modifier            = Modifier
                                    .padding(32.dp)
                                    .fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.surfaceContainerHigh
                                ) {
                                    Icon(
                                        Icons.Default.LockPerson, null,
                                        modifier = Modifier.padding(20.dp).size(44.dp),
                                        tint     = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Text(
                                    text       = "Sin módulos asignados",
                                    style      = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text      = "Tu cuenta no tiene módulos asignados en este momento.\nPor favor contacta al administrador.",
                                    style     = MaterialTheme.typography.bodyMedium,
                                    color     = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                                Button(
                                    onClick = { vm.logout(); onLogout() },
                                    shape   = RoundedCornerShape(12.dp)
                                ) { Text("Cerrar sesión") }
                            }
                        }
                    }

                    is HomeUiState.Error -> {
                        OutlinedCard(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(24.dp)
                                .fillMaxWidth(),
                            shape  = RoundedCornerShape(24.dp),
                            colors = CardDefaults.outlinedCardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                            )
                        ) {
                            Column(
                                modifier            = Modifier
                                    .padding(32.dp)
                                    .fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.errorContainer
                                ) {
                                    Icon(
                                        Icons.Default.CloudOff, null,
                                        modifier = Modifier.padding(16.dp).size(36.dp),
                                        tint     = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                }
                                Text(
                                    text       = "Error al cargar menú",
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
                                    shape   = RoundedCornerShape(12.dp),
                                    colors  = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.error
                                    )
                                ) { Text("Volver al inicio") }
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

// ─── Hero Dashboard Header Dibujado con Canvas ────────────────────────────────

@Composable
private fun DashboardHeroHeader(
    userName: String,
    inicial:  String,
    onLogout: () -> Unit
) {
    val primaryColor   = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val tertiaryColor  = MaterialTheme.colorScheme.tertiary
    val onPrimary      = MaterialTheme.colorScheme.onPrimaryContainer

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        // 🎨 Arte en Canvas: Olas y curvas de fondo dinámicas
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width  = size.width
            val height = size.height

            // Ola 1 (Fondo primario suave)
            val path1 = Path().apply {
                moveTo(0f, height * 0.4f)
                cubicTo(
                    width * 0.35f, height * 0.1f,
                    width * 0.65f, height * 0.9f,
                    width, height * 0.5f
                )
                lineTo(width, 0f)
                lineTo(0f, 0f)
                close()
            }
            drawPath(
                path  = path1,
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        primaryColor.copy(alpha = 0.25f),
                        tertiaryColor.copy(alpha = 0.18f)
                    )
                )
            )

            // Ola 2 (Acento secundario sutil)
            val path2 = Path().apply {
                moveTo(0f, height * 0.7f)
                cubicTo(
                    width * 0.4f, height * 0.95f,
                    width * 0.7f, height * 0.3f,
                    width, height * 0.65f
                )
                lineTo(width, 0f)
                lineTo(0f, 0f)
                close()
            }
            drawPath(
                path  = path2,
                brush = Brush.linearGradient(
                    colors = listOf(
                        secondaryColor.copy(alpha = 0.15f),
                        primaryColor.copy(alpha = 0.2f)
                    )
                )
            )

            // Anillos artísticos en la esquina superior derecha
            drawCircle(
                color  = primaryColor.copy(alpha = 0.08f),
                radius = width * 0.35f,
                center = androidx.compose.ui.geometry.Offset(width * 0.9f, height * 0.1f)
            )
        }

        // Contenido del Header sobre el Canvas
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Fila Superior: Botón Cerrar Sesión (Derecha)
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick  = onLogout,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f))
                ) {
                    Icon(
                        imageVector        = Icons.Default.ExitToApp,
                        contentDescription = "Cerrar sesión",
                        tint               = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Fila Inferior: Avatar Canvas y Saludo
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier          = Modifier.padding(bottom = 8.dp)
            ) {
                // Avatar con anillo acentuado en Canvas
                Box(
                    contentAlignment = Alignment.Center,
                    modifier         = Modifier.size(54.dp)
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawCircle(
                            color = primaryColor.copy(alpha = 0.3f),
                            style = Stroke(width = 3.dp.toPx())
                        )
                    }
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
                                color      = onPrimary
                            )
                        }
                    }
                }

                Spacer(Modifier.width(14.dp))

                Column {
                    Text(
                        text       = if (userName.isNotBlank()) "¡Hola, $userName!" else "¡Hola de nuevo!",
                        style      = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color      = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text  = "¿Qué deseas gestionar hoy?",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

// ─── Banner de Licencia Expiración ────────────────────────────────────────────

@Composable
private fun BannerDemoExpiracion(mensaje: String) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.errorContainer
    ) {
        Row(
            modifier          = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.error.copy(alpha = 0.2f)
            ) {
                Icon(
                    imageVector        = Icons.Default.Warning,
                    contentDescription = null,
                    tint               = MaterialTheme.colorScheme.error,
                    modifier           = Modifier.padding(6.dp).size(18.dp)
                )
            }
            Spacer(Modifier.width(12.dp))
            Text(
                text     = mensaje,
                style    = MaterialTheme.typography.bodyMedium,
                color    = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

// ─── Menú Dinámico con Tarjetas M3 ───────────────────────────────────────────

@Composable
private fun MenuDinamico(
    modulos:    List<Modulo>,
    onNavigate: (String) -> Unit
) {
    LazyColumn(
        contentPadding      = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(modulos) { modulo ->
            ModuloCard(modulo = modulo, onNavigate = onNavigate)
        }
    }
}

// ─── Card de Módulo Acentuada M3 + Canvas ─────────────────────────────────────

@Composable
private fun ModuloCard(
    modulo:     Modulo,
    onNavigate: (String) -> Unit
) {
    val primaryColor = MaterialTheme.colorScheme.primary

    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(20.dp),
        colors   = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
        )
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)) {

            // ── Cabecera del Módulo con Acento Canvas ────────────────────────
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier          = Modifier.fillMaxWidth()
            ) {
                // Icono con Canvas accent ring
                Box(
                    contentAlignment = Alignment.Center,
                    modifier         = Modifier.size(44.dp)
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawCircle(
                            color = primaryColor.copy(alpha = 0.15f)
                        )
                    }
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Icon(
                            imageVector        = mapIcon(modulo.icono),
                            contentDescription = modulo.modulo,
                            tint               = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier           = Modifier.padding(10.dp).size(22.dp)
                        )
                    }
                }

                Spacer(Modifier.width(12.dp))

                Text(
                    text       = modulo.modulo,
                    style      = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color      = MaterialTheme.colorScheme.onSurface,
                    modifier   = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(14.dp))

            HorizontalDivider(
                color    = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                thickness = 1.dp
            )

            Spacer(Modifier.height(10.dp))

            // ── Lista de Submódulos ──────────────────────────────────────────
            modulo.submodulos?.forEach { sub ->
                Surface(
                    onClick  = { onNavigate(sub.ruta) },
                    shape    = RoundedCornerShape(14.dp),
                    color    = MaterialTheme.colorScheme.surfaceBright,
                    border   = androidx.compose.foundation.BorderStroke(
                        0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier          = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            Icon(
                                imageVector        = mapIcon(sub.icono),
                                contentDescription = sub.nombre,
                                modifier           = Modifier.padding(8.dp).size(18.dp),
                                tint               = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }

                        Spacer(Modifier.width(12.dp))

                        Text(
                            text       = sub.nombre,
                            modifier   = Modifier.weight(1f),
                            style      = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color      = MaterialTheme.colorScheme.onSurface
                        )

                        Icon(
                            imageVector        = Icons.Default.ChevronRight,
                            contentDescription = null,
                            modifier           = Modifier.size(20.dp),
                            tint               = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
