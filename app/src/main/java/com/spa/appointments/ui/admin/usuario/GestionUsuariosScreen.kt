// Ruta: app/src/main/java/com/spa/appointments/ui/admin/usuario/GestionUsuariosScreen.kt
package com.spa.appointments.ui.admin.usuario

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.spa.appointments.domain.model.Usuario

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GestionUsuariosScreen(
    onVolver: () -> Unit,
    viewModel: AdministracionViewModel = hiltViewModel()
) {
    val state = viewModel.uiState
    var usuarioSeleccionado by remember { mutableStateOf<Usuario?>(null) }
    var mostrarDialog       by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.error) {
        state.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.limpiarError()
        }
    }

    LaunchedEffect(state.cargando) {
        if (!state.cargando && !mostrarDialog) {
            usuarioSeleccionado = null
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text       = "Gestión de Usuarios",
                                style      = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            if (state.usuarios.isNotEmpty()) {
                                Surface(
                                    shape    = RoundedCornerShape(50),
                                    color    = MaterialTheme.colorScheme.primaryContainer,
                                    modifier = Modifier.padding(top = 2.dp)
                                ) {
                                    Text(
                                        text     = "${state.usuarios.size} ${if (state.usuarios.size == 1) "usuario" else "usuarios"}",
                                        style    = MaterialTheme.typography.labelSmall,
                                        color    = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 1.dp)
                                    )
                                }
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onVolver) {
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
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                // 1. Estado de carga inicial
                state.cargando && state.usuarios.isEmpty() -> {
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
                            text  = "Cargando credenciales…",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // 2. Estado vacío
                state.usuarios.isEmpty() -> {
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
                                        imageVector        = Icons.Default.GroupOff,
                                        contentDescription = null,
                                        modifier           = Modifier.padding(18.dp).size(36.dp),
                                        tint               = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Text(
                                text       = "Sin usuarios activos",
                                style      = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color      = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text      = "Los usuarios creados en el módulo de seguridad aparecerán listados aquí de forma automática.",
                                style     = MaterialTheme.typography.bodyMedium,
                                color     = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                // 3. Renderizado de lista interactiva
                else -> {
                    LazyColumn(
                        contentPadding      = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier            = Modifier.fillMaxSize()
                    ) {
                        items(state.usuarios, key = { it.id }) { usuario ->
                            UsuarioAdminCard(
                                usuario  = usuario,
                                onEditar = {
                                    usuarioSeleccionado = usuario
                                    mostrarDialog = true
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // ── Dialog de Cambio de Perfil M3 ──────────────────────────────────
    if (mostrarDialog && usuarioSeleccionado != null) {
        var perfilSel by remember { mutableIntStateOf(usuarioSeleccionado!!.idPerfil) }

        AlertDialog(
            onDismissRequest = { if (!state.cargando) mostrarDialog = false },
            shape            = RoundedCornerShape(28.dp),
            containerColor   = MaterialTheme.colorScheme.surfaceContainerHigh,
            icon = {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Icon(
                        imageVector        = Icons.Default.ManageAccounts,
                        contentDescription = null,
                        tint               = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier           = Modifier.padding(12.dp).size(26.dp)
                    )
                }
            },
            title = {
                Text(
                    text       = "Modificar Perfil",
                    fontWeight = FontWeight.Bold,
                    style      = MaterialTheme.typography.titleMedium,
                    textAlign  = TextAlign.Center,
                    modifier   = Modifier.fillMaxWidth()
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier            = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text  = "Asigna un rol de accesos diferente para el usuario: ${usuarioSeleccionado!!.usuario}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape    = RoundedCornerShape(18.dp),
                        colors   = CardDefaults.outlinedCardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
                    ) {
                        Column {
                            state.perfiles.forEachIndexed { index, perfil ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable(enabled = !state.cargando) { perfilSel = perfil.id }
                                        .padding(horizontal = 16.dp, vertical = 14.dp)
                                ) {
                                    RadioButton(
                                        selected = perfilSel == perfil.id,
                                        onClick  = { perfilSel = perfil.id },
                                        enabled  = !state.cargando
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text       = perfil.nombre,
                                        style      = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color      = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                if (index < state.perfiles.lastIndex) {
                                    HorizontalDivider(
                                        modifier = Modifier.padding(horizontal = 16.dp),
                                        color    = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.cambiarPerfilUsuario(usuarioSeleccionado!!.id, perfilSel)
                        mostrarDialog = false
                    },
                    enabled  = !state.cargando,
                    shape    = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (state.cargando) {
                        CircularProgressIndicator(
                            modifier    = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color       = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Icon(Icons.Default.Save, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Guardar cambios", fontWeight = FontWeight.Bold)
                    }
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick  = { mostrarDialog = false },
                    enabled  = !state.cargando,
                    shape    = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Cancelar") }
            }
        )
    }
}

// ─── Card de Usuario M3 ────────────────────────────────────────────────────────

@Composable
private fun UsuarioAdminCard(
    usuario: Usuario,
    onEditar: () -> Unit
) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(18.dp),
        colors   = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        border  = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
        onClick = onEditar
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
                    Icon(
                        imageVector        = Icons.Default.Person,
                        contentDescription = null,
                        modifier           = Modifier.size(22.dp),
                        tint               = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text       = usuario.usuario,
                    style      = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color      = MaterialTheme.colorScheme.onSurface
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier          = Modifier.padding(top = 2.dp)
                ) {
                    Icon(
                        imageVector        = Icons.Default.Badge,
                        contentDescription = null,
                        modifier           = Modifier.size(13.dp),
                        tint               = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text  = "Perfil: ${usuario.nombrePerfil}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.width(8.dp))

            Surface(
                shape = RoundedCornerShape(6.dp),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier              = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                    )
                    Text(
                        text       = "Activo",
                        style      = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color      = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }
}
