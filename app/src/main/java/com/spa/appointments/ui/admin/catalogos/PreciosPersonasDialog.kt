// Ruta: app/src/main/java/com/spa/appointments/ui/admin/catalogos/PreciosPersonasDialog.kt
package com.spa.appointments.ui.admin.catalogos

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreciosPersonasDialog(
    servicio: ServicioAdmin,
    preciosRangos: List<ServicioPrecioRango>,
    onGuardarRango: (cantidadPersonas: Int, precio: Double, esPorPersona: Boolean) -> Unit,
    onEliminarRango: (idRango: Int) -> Unit,
    onDismiss: () -> Unit
) {
    var cantidadText by remember { mutableStateOf("") }
    var precioText   by remember { mutableStateOf("") }
    var esPorPersona by remember { mutableStateOf(false) }

    val maxPersonasExistente = remember(preciosRangos) {
        preciosRangos.maxOfOrNull { it.cantidadPersonas } ?: 0
    }
    val siguienteCantMultiplicable = maxPersonasExistente + 1

    val esValido = if (esPorPersona) {
        (precioText.toDoubleOrNull() ?: 0.0) > 0.0
    } else {
        (cantidadText.toIntOrNull() ?: 0) >= 1 && (precioText.toDoubleOrNull() ?: 0.0) > 0.0
    }

    BasicAlertDialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            tonalElevation = 6.dp
        ) {
            Column(modifier = Modifier.padding(20.dp)) {

                // Header M3
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.tertiaryContainer
                    ) {
                        Icon(
                            imageVector = Icons.Default.Groups,
                            contentDescription = null,
                            modifier = Modifier.padding(10.dp).size(24.dp),
                            tint = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                    Column {
                        Text(
                            text = "Tarifas por N° Personas",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = servicio.nombre,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                Spacer(modifier = Modifier.height(12.dp))

                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Banner Informativo
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.Info,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Text(
                                text = if (esPorPersona)
                                    "Modo Multiplicador (Aplica para $siguienteCantMultiplicable+ personas): El precio se multiplicará por el número de asistentes."
                                else
                                    "Modo Tarifa Fija: Se cobrará un precio fijo total para el número exacto de personas especificado.",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }

                    // Selector de modo de tarifa
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Checkbox(
                            checked = esPorPersona,
                            onCheckedChange = { esPorPersona = it }
                        )
                        Text(
                            text = "Tarifa Multiplicable (x N° de asistentes)",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Formulario para agregar nuevo nivel
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (!esPorPersona) {
                            OutlinedTextField(
                                value = cantidadText,
                                onValueChange = { cantidadText = it.filter { c -> c.isDigit() } },
                                placeholder = { Text("Personas", style = MaterialTheme.typography.bodySmall) },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Groups,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(52.dp),
                                singleLine = true,
                                shape = RoundedCornerShape(14.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor   = MaterialTheme.colorScheme.surfaceContainerLow,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow
                                ),
                                textStyle = MaterialTheme.typography.bodyMedium
                            )
                        } else {
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.6f),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(52.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 8.dp)) {
                                    Text(
                                        text = "$siguienteCantMultiplicable+ Asistentes",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onTertiaryContainer
                                    )
                                }
                            }
                        }

                        OutlinedTextField(
                            value = precioText,
                            onValueChange = { precioText = it.filter { c -> c.isDigit() || c == '.' } },
                            placeholder = {
                                Text(
                                    if (esPorPersona) "Precio / Persona ($)" else "Precio Total ($)",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.AttachMoney,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .weight(1.3f)
                                .height(52.dp),
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor   = MaterialTheme.colorScheme.surfaceContainerLow,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow
                            ),
                            textStyle = MaterialTheme.typography.bodyMedium
                        )

                        FilledTonalButton(
                            onClick = {
                                val prec = precioText.toDoubleOrNull()
                                if (prec != null) {
                                    val cant = if (esPorPersona) siguienteCantMultiplicable else (cantidadText.toIntOrNull() ?: 1)
                                    onGuardarRango(cant, prec, esPorPersona)
                                    cantidadText = ""
                                    precioText = ""
                                    esPorPersona = false
                                }
                            },
                            enabled = esValido,
                            modifier = Modifier.height(52.dp),
                            shape = RoundedCornerShape(14.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Agregar tarifa",
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                    Text(
                        text = "NIVELES CONFIGURADOS",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    if (preciosRangos.isEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerLow,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Sin tarifas por persona configuradas aún. Se usará el precio base ($${servicio.precioBase}).",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp)
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 160.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(preciosRangos, key = { it.id }) { rango ->
                                OutlinedCard(
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Text(
                                                text = if (rango.esPorPersona)
                                                    "${rango.cantidadPersonas}+ personas"
                                                else
                                                    "${rango.cantidadPersonas} ${if (rango.cantidadPersonas == 1) "persona" else "personas"}",
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = if (rango.esPorPersona) "Multiplicable x asistente" else "Monto fijo paquete",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = "$${String.format(java.util.Locale.US, "%,.0f", rango.precio)}${if (rango.esPorPersona) " /p" else ""}",
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )

                                            Spacer(Modifier.width(8.dp))

                                            IconButton(
                                                onClick = { onEliminarRango(rango.id) },
                                                modifier = Modifier.size(36.dp)
                                            ) {
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

                Spacer(modifier = Modifier.height(18.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Text("Listo", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
