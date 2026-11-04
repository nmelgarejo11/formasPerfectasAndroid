// ui/admin/usuario/GestionUsuariosScreen.kt
package com.spa.appointments.ui.admin.usuario

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text       = "Gestión de Perfiles",
                            style      = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        if (state.usuarios.isNotEmpty()) {
                            Text(
                                text  = "${state.usuarios.size} ${if (state.usuarios.size == 1) "usuario" else "usuarios"}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver")
                    }
                }
            )
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
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CircularProgressIndicator()
                        Text(
                            text  = "Cargando credenciales…",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // 2. Estado vacío (No hay cuentas registradas)
                state.usuarios.isEmpty() -> {
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
                                imageVector        = Icons.Default.GroupOff,
                                contentDescription = null,
                                modifier           = Modifier
                                    .padding(20.dp)
                                    .size(48.dp),
                                tint               = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text       = "Sin usuarios activos",
                            style      = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text      = "Los usuarios creados en el módulo de seguridad aparecerán listados aquí de forma automática.",
                            style     = MaterialTheme.typography.bodyMedium,
                            color     = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // 3. Renderizado de lista interactiva
                else -> {
                    LazyColumn(
                        contentPadding      = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
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

    // ── Dialog de Cambio de Perfil ────────────────────────────────────
    if (mostrarDialog && usuarioSeleccionado != null) {
        var perfilSel by remember { mutableIntStateOf(usuarioSeleccionado!!.idPerfil) }

        AlertDialog(
            onDismissRequest = { if (!state.cargando) mostrarDialog = false },
            shape = RoundedCornerShape(16.dp),
            icon  = {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Icon(
                        imageVector        = Icons.Default.ManageAccounts,
                        contentDescription = null,
                        tint               = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier           = Modifier
                            .padding(10.dp)
                            .size(22.dp)
                    )
                }
            },
            title = {
                Text(
                    text       = "Modificar Perfil",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier            = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text  = "Asigna un rol de accesos diferente para la cuenta del usuario: ${usuarioSeleccionado!!.usuario}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape    = RoundedCornerShape(12.dp),
                        border   = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        colors   = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column {
                            state.perfiles.forEachIndexed { index, perfil ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable(enabled = !state.cargando) { perfilSel = perfil.id }
                                        .padding(horizontal = 14.dp, vertical = 12.dp)
                                ) {
                                    RadioButton(
                                        selected = perfilSel == perfil.id,
                                        onClick  = { perfilSel = perfil.id },
                                        enabled  = !state.cargando
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text  = perfil.nombre,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                                if (index < state.perfiles.lastIndex) {
                                    HorizontalDivider(
                                        modifier = Modifier.padding(horizontal = 14.dp),
                                        color    = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
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
                    enabled = !state.cargando,
                    shape   = RoundedCornerShape(10.dp)
                ) {
                    if (state.cargando) {
                        CircularProgressIndicator(
                            modifier    = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color       = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Icon(Icons.Default.Save, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Guardar")
                    }
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick  = { mostrarDialog = false },
                    enabled  = !state.cargando,
                    shape    = RoundedCornerShape(10.dp)
                ) { Text("Cancelar") }
            }
        )
    }
}

// ─── Card de Usuario Cliqueable ───────────────────────────────────────────────

@Composable
private fun UsuarioAdminCard(
    usuario: Usuario,
    onEditar: () -> Unit
) {
    Card(
        modifier  = Modifier
            .fillMaxWidth()
            // 1. Delimitamos el efecto de onda a las esquinas redondeadas
            .clip(RoundedCornerShape(14.dp))
            // 2. Volvemos toda la superficie reactiva al toque del administrador
            .clickable { onEditar() },
        shape     = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border    = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier          = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 14.dp), // Aumentamos padding vertical para mejor touch target
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Contenedor del Avatar/Ícono de Cuenta
            Surface(
                shape    = RoundedCornerShape(10.dp),
                color    = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                modifier = Modifier.size(44.dp)
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

            Spacer(Modifier.width(12.dp))

            // Datos informativos principales de la cuenta
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text       = usuario.usuario,
                    style      = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier          = Modifier.padding(top = 4.dp)
                ) {
                    Icon(
                        imageVector        = Icons.Default.Badge,
                        contentDescription = null,
                        modifier           = Modifier.size(12.dp),
                        tint               = MaterialTheme.colorScheme.onSurfaceVariant
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

            // Estado Lateral derecho limpio sin ícono de lápiz de por medio
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Surface(
                    shape    = RoundedCornerShape(50),
                    color    = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(6.dp)
                ) {}
                Text(
                    text  = "Activo",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}