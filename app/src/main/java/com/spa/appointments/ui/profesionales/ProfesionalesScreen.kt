package com.spa.appointments.ui.profesionales

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.spa.appointments.domain.model.Profesional

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfesionalesScreen(
    onBack: () -> Unit,
    onSeleccionarProfesional: (Profesional) -> Unit,
    vm: ProfesionalesViewModel = hiltViewModel()
) {
    val uiState by vm.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Selecciona un profesional") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
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

                is ProfesionalesUiState.Loading ->
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))

                is ProfesionalesUiState.Error ->
                    Text(
                        text = state.mensaje,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(24.dp)
                    )

                is ProfesionalesUiState.Success -> {
                    LazyColumn(contentPadding = PaddingValues(16.dp)) {
                        items(state.items) { profesional ->
                            ProfesionalCard(
                                profesional = profesional,
                                onClick = { onSeleccionarProfesional(profesional) }
                            )
                            Spacer(Modifier.height(8.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfesionalCard(
    profesional: Profesional,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar placeholder (puedes reemplazar con Coil para cargar la foto)
            Surface(
                modifier = Modifier.size(56.dp),
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Spacer(Modifier.width(16.dp))

            Column {
                Text(
                    text = profesional.nombreCompleto,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = profesional.cargo,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
                Text(
                    text = profesional.sede,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}