package com.spa.appointments.ui.admin.catalogos

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.spa.appointments.domain.model.ServicioAdmin
import com.spa.appointments.domain.model.ServicioPrecioRango

@Composable
fun PreciosPersonasDialog(
    servicio: ServicioAdmin,
    preciosRangos: List<ServicioPrecioRango>,
    onGuardarRango: (cantidadPersonas: Int, precio: Double) -> Unit,
    onEliminarRango: (idRango: Int) -> Unit,
    onDismiss: () -> Unit
) {
    var cantidadText by remember { mutableStateOf("") }
    var precioText by remember { mutableStateOf("") }

    val esValido = (cantidadText.toIntOrNull() ?: 0) >= 1 && (precioText.toDoubleOrNull() ?: 0.0) > 0.0

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        icon = {
            Icon(
                Icons.Default.Groups,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
        },
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Tarifas por N° Personas",
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = servicio.nombre,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Banner informativo de la regla del tope máximo
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Text(
                            text = "Si la cantidad de asistentes supera el nivel máximo configurado, se aplicará el valor del último nivel.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }

                // Formularios para agregar nuevo nivel
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = cantidadText,
                        onValueChange = { cantidadText = it.filter { c -> c.isDigit() } },
                        label = { Text("Personas") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )

                    OutlinedTextField(
                        value = precioText,
                        onValueChange = { precioText = it.filter { c -> c.isDigit() || c == '.' } },
                        label = { Text("Precio ($)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1.2f),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )

                    IconButton(
                        onClick = {
                            val cant = cantidadText.toIntOrNull()
                            val prec = precioText.toDoubleOrNull()
                            if (cant != null && prec != null) {
                                onGuardarRango(cant, prec)
                                cantidadText = ""
                                precioText = ""
                            }
                        },
                        enabled = esValido
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Agregar tarifa", tint = MaterialTheme.colorScheme.primary)
                    }
                }

                HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)

                // Lista de rangos configurados
                Text(
                    text = "Niveles configurados:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )

                if (preciosRangos.isEmpty()) {
                    Text(
                        text = "Sin tarifas por persona configuradas aún. Se usará el precio base ($${servicio.precioBase}).",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 160.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(preciosRangos, key = { it.id }) { rango ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceContainerLow,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "${rango.cantidadPersonas} ${if (rango.cantidadPersonas == 1) "persona" else "personas"}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold
                                    )

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "$${String.format(java.util.Locale.US, "%,.0f", rango.precio)}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.primary
                                        )

                                        Spacer(Modifier.width(8.dp))

                                        IconButton(onClick = { onEliminarRango(rango.id) }) {
                                            Icon(
                                                Icons.Default.Delete,
                                                contentDescription = "Eliminar nivel",
                                                tint = MaterialTheme.colorScheme.error,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Listo")
            }
        }
    )
}
