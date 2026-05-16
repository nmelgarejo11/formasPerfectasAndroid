package com.spa.appointments.ui.citas

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.spa.appointments.ui.disponibilidad.DisponibilidadUiState
import kotlinx.coroutines.flow.StateFlow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResponsableGrupalScreen(
    onBack: () -> Unit,
    onContinuar: (nombre: String, telefono: String, correo: String, personas: Int) -> Unit,
    uiStateFlow: StateFlow<DisponibilidadUiState>, // <-- Recibimos el estado
    onExito: () -> Unit
) {
    val uiState by uiStateFlow.collectAsState()

    LaunchedEffect(uiState) {
        if (uiState is DisponibilidadUiState.CitaCreada) {
            onExito()
        }
    }

    var nombre by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }
    var cantidadPersonas by remember { mutableStateOf(2) }

    var errorNombre by remember { mutableStateOf(false) }
    var errorTelefono by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Datos del Grupo") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Regresar")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Por favor, ingresa los datos de la persona responsable del grupo.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Campo Nombre
            OutlinedTextField(
                value = nombre,
                onValueChange = {
                    nombre = it
                    errorNombre = false
                },
                label = { Text("Nombres *") },
                isError = errorNombre,
                supportingText = { if (errorNombre) Text("El nombre es obligatorio") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Campo Teléfono
            OutlinedTextField(
                value = telefono,
                onValueChange = {
                    telefono = it
                    errorTelefono = false
                },
                label = { Text("Teléfono *") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                isError = errorTelefono,
                supportingText = { if (errorTelefono) Text("El teléfono es obligatorio") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Campo Correo (Opcional)
            OutlinedTextField(
                value = correo,
                onValueChange = { correo = it },
                label = { Text("Correo electronico") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Selector de Cantidad de Personas
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Cantidad de Asistentes", style = MaterialTheme.typography.titleMedium)
                        Text("Incluyendo al responsable", style = MaterialTheme.typography.bodySmall)
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(
                            onClick = { if (cantidadPersonas > 2) cantidadPersonas-- },
                            enabled = cantidadPersonas > 2
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "Disminuir")
                        }

                        Text(
                            text = cantidadPersonas.toString(),
                            style = MaterialTheme.typography.titleLarge
                        )

                        IconButton(
                            onClick = { cantidadPersonas++ }
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Aumentar")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Botón Continuar
            Button(
                onClick = {
                    if (nombre.isBlank()) errorNombre = true
                    if (telefono.isBlank()) errorTelefono = true

                    if (!errorNombre && !errorTelefono) {
                        onContinuar(nombre, telefono, correo, cantidadPersonas)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Confirmar")
            }
        }
    }
}