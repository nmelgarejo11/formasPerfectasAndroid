package com.spa.appointments.ui.servicios

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.spa.appointments.domain.model.Servicio

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiciosScreen(
    onBack: () -> Unit,
    onSeleccionarServicio: (Servicio) -> Unit,
    vm: ServiciosViewModel = hiltViewModel()
) {
    val uiState by vm.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Selecciona un servicio") },
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

                is ServiciosUiState.Loading ->
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))

                is ServiciosUiState.Error ->
                    Text(
                        text = state.mensaje,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(24.dp)
                    )

                is ServiciosUiState.Success -> {
                    // Agrupar por categoría
                    val grupos = state.items.groupBy { it.categoria }

                    LazyColumn(contentPadding = PaddingValues(16.dp)) {
                        grupos.forEach { (categoria, servicios) ->

                            item {
                                Text(
                                    text = categoria,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }

                            items(servicios) { servicio ->
                                ServicioCard(
                                    servicio = servicio,
                                    onClick = { onSeleccionarServicio(servicio) }
                                )
                                Spacer(Modifier.height(8.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ServicioCard(
    servicio: Servicio,
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
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = servicio.nombre,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                servicio.descripcion?.let {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "${servicio.duracionMinutos} min",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
            Spacer(Modifier.width(16.dp))
            Text(
                text = "$${"%,.0f".format(servicio.precioBase)}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}