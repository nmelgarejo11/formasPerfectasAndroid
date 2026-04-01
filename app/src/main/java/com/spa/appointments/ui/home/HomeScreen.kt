package com.spa.appointments.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.spa.appointments.core.utils.mapIcon
import com.spa.appointments.domain.model.Modulo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onLogout: () -> Unit,             // Navega al login al cerrar sesión
    onNavigate: (String) -> Unit,     // Navega a una ruta del submodulo
    vm: HomeViewModel = hiltViewModel()
) {
    val uiState by vm.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    when (val s = uiState) {
                        is HomeUiState.Success ->
                            Text("Hola, ${s.userName}")
                        else -> Text("Dashboard")
                    }
                },
                actions = {
                    // Botón de cerrar sesión
                    IconButton(onClick = {
                        vm.logout()
                        onLogout()
                    }) {
                        Icon(
                            imageVector = Icons.Default.ExitToApp,
                            contentDescription = "Cerrar sesión"
                        )
                    }
                }
            )
        }
    ) { padding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (val state = uiState) {

                is HomeUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                is HomeUiState.Error -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Error al cargar el menú",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = state.mensaje,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = { vm.logout(); onLogout() }) {
                            Text("Volver al login")
                        }
                    }
                }

                is HomeUiState.Success -> {
                    MenuDinamico(
                        modulos = state.modulos,
                        onNavigate = onNavigate
                    )
                }
            }
        }
    }
}

@Composable
private fun MenuDinamico(
    modulos: List<Modulo>,
    onNavigate: (String) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(modulos) { modulo ->
            ModuloCard(modulo = modulo, onNavigate = onNavigate)
        }
    }
}

@Composable
private fun ModuloCard(
    modulo: Modulo,
    onNavigate: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // Cabecera del módulo
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = mapIcon(modulo.icono),
                    contentDescription = modulo.modulo,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = modulo.modulo,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(8.dp))
            HorizontalDivider()
            Spacer(Modifier.height(8.dp))

            // Submódulos como botones de texto
            modulo.submodulos.forEach { sub ->
                TextButton(
                    onClick = { onNavigate(sub.ruta) },
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = mapIcon(sub.icono),
                        contentDescription = sub.nombre,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = sub.nombre,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}