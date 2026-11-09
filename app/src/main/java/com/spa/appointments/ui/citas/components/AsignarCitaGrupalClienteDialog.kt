package com.spa.appointments.ui.citas.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.spa.appointments.domain.model.Cita
import com.spa.appointments.domain.model.Cliente
import com.spa.appointments.domain.model.Profesional

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AsignarCitaGrupalClienteDialog(
    cita: Cita,
    clientesDisponibles: List<Cliente>,
    profesionales: List<Profesional>,
    onBuscarCliente: (String) -> Unit,
    onConfirmar: (idCita: Int, idCliente: Int, cantidadPersonas: Int, idsProfesionales: List<Int>?) -> Unit,
    onDismiss: () -> Unit
) {
    var queryBusqueda by remember { mutableStateOf("") }
    var clienteSeleccionado by remember { mutableStateOf<Cliente?>(null) }
    var cantidadPersonas by remember(cita) {
        mutableStateOf((cita.cantidadPersonas ?: 2).coerceAtLeast(2).toString())
    }

    val selectedProfesionales = remember { mutableStateListOf<Int>() }

    val cantInt = cantidadPersonas.toIntOrNull() ?: 0
    val excedeMax = cita.maxPersonas != null && cita.maxPersonas > 0 && cantInt > cita.maxPersonas

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        icon = {
            Icon(
                Icons.Default.Group,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
        },
        title = {
            Text(
                "Asignar Cita Grupal",
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // 1. Selector de Cliente Existente con Buscador
                Text(
                    text = "1. Seleccionar Cliente Responsable:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )

                OutlinedTextField(
                    value = queryBusqueda,
                    onValueChange = {
                        queryBusqueda = it
                        onBuscarCliente(it)
                    },
                    label = { Text("Buscar cliente por nombre o teléfono") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (queryBusqueda.isNotBlank()) {
                            IconButton(onClick = { queryBusqueda = ""; onBuscarCliente("") }) {
                                Icon(Icons.Default.Clear, contentDescription = null)
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                if (clienteSeleccionado != null) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${clienteSeleccionado?.nombre} ${clienteSeleccionado?.apellido}".trim(),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                if (!clienteSeleccionado?.telefono.isNullOrBlank()) {
                                    Text(
                                        text = clienteSeleccionado?.telefono ?: "",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            IconButton(onClick = { clienteSeleccionado = null }) {
                                Icon(Icons.Default.Clear, contentDescription = "Quitar cliente")
                            }
                        }
                    }
                } else {
                    val filtrados = remember(clientesDisponibles, queryBusqueda) {
                        if (queryBusqueda.isBlank()) clientesDisponibles.take(5)
                        else clientesDisponibles.filter {
                            "${it.nombre} ${it.apellido}".contains(queryBusqueda, ignoreCase = true) ||
                                    (it.telefono?.contains(queryBusqueda) == true)
                        }
                    }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 140.dp)
                    ) {
                        items(filtrados, key = { it.id }) { cli ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceContainerLow,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp)
                                    .clickable {
                                        clienteSeleccionado = cli
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${cli.nombre} ${cli.apellido}".trim(),
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.weight(1f)
                                    )
                                    if (!cli.telefono.isNullOrBlank()) {
                                        Text(
                                            text = cli.telefono ?: "",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // 2. Cantidad de Asistentes
                Text(
                    text = "2. Cantidad de Asistentes:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )

                OutlinedTextField(
                    value = cantidadPersonas,
                    onValueChange = { cantidadPersonas = it.filter { c -> c.isDigit() } },
                    label = { Text("Número de personas") },
                    isError = excedeMax,
                    supportingText = {
                        if (excedeMax) {
                            Text(
                                "El número de personas no puede superar el límite del servicio (${cita.maxPersonas}).",
                                color = MaterialTheme.colorScheme.error
                            )
                        } else if (cita.maxPersonas != null && cita.maxPersonas > 0) {
                            Text("Máximo permitido: ${cita.maxPersonas} personas")
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val cli = clienteSeleccionado
                    val cant = cantidadPersonas.toIntOrNull() ?: 2
                    if (cli != null && !excedeMax) {
                        onConfirmar(cita.id, cli.id, cant, selectedProfesionales.toList().ifEmpty { null })
                    }
                },
                enabled = clienteSeleccionado != null && (cantidadPersonas.toIntOrNull() ?: 0) >= 1 && !excedeMax,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Confirmar Asignación Grupal", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cancelar")
            }
        }
    )
}
