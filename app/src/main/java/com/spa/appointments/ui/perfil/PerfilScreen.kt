package com.spa.appointments.ui.perfil

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.spa.appointments.core.security.TokenStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import androidx.lifecycle.ViewModel
import javax.inject.Inject

// ── ViewModel ────────────────────────────────────────────────────────────────
@HiltViewModel
class PerfilViewModel @Inject constructor(
    private val tokenStorage: TokenStorage
) : ViewModel() {
    val nombreUsuario: String get() = tokenStorage.getUser()     ?: "Usuario"
    val idEmpresa:     Int    get() = tokenStorage.getIdEmpresa()
}

// ── Screen ───────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilScreen(
    onBack:   () -> Unit,
    onLogout: () -> Unit,
    vm: PerfilViewModel = hiltViewModel()
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mi perfil") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Volver")
                    }
                }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {

            // ── Avatar y nombre ──
            Column(
                modifier            = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    modifier = Modifier.size(88.dp),
                    shape    = MaterialTheme.shapes.extraLarge,
                    color    = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector        = Icons.Default.Person,
                            contentDescription = null,
                            modifier           = Modifier.size(48.dp),
                            tint               = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                Text(
                    text       = vm.nombreUsuario,
                    style      = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text  = "Empresa ID: ${vm.idEmpresa}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            HorizontalDivider()

            // ── Opciones de perfil ──
            PerfilOpcion(
                icono  = Icons.Default.Person,
                titulo = "Nombre de usuario",
                valor  = vm.nombreUsuario
            )

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            PerfilOpcion(
                icono  = Icons.Default.Business,
                titulo = "Empresa",
                valor  = "ID: ${vm.idEmpresa}"
            )

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            PerfilOpcion(
                icono  = Icons.Default.Security,
                titulo = "Versión de la app",
                valor  = "1.0.0"
            )

            Spacer(Modifier.height(32.dp))
            HorizontalDivider()

            // ── Botón cerrar sesión ──
            ListItem(
                headlineContent = {
                    Text(
                        text  = "Cerrar sesión",
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                },
                leadingContent = {
                    Icon(
                        imageVector        = Icons.Default.ExitToApp,
                        contentDescription = null,
                        tint               = MaterialTheme.colorScheme.error
                    )
                },
                modifier = Modifier.padding(vertical = 4.dp),
                trailingContent = {
                    Icon(
                        Icons.Default.KeyboardArrowRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                colors = ListItemDefaults.colors(
                    containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                )
            )
        }
    }
}

@Composable
private fun PerfilOpcion(
    icono:  androidx.compose.ui.graphics.vector.ImageVector,
    titulo: String,
    valor:  String
) {
    ListItem(
        headlineContent  = { Text(titulo) },
        supportingContent = {
            Text(
                text  = valor,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        leadingContent = {
            Icon(
                imageVector        = icono,
                contentDescription = null,
                tint               = MaterialTheme.colorScheme.primary
            )
        },
        trailingContent = {
            Icon(
                Icons.Default.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    )
}