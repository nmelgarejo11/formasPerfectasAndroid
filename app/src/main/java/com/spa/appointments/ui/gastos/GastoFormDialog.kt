// Ruta: app/src/main/java/com/spa/appointments/ui/gastos/GastoFormDialog.kt
package com.spa.appointments.ui.gastos

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.spa.appointments.domain.model.GastoRequest
import com.spa.appointments.domain.model.MetodoPago
import com.spa.appointments.domain.model.MetodoPagoDetalle
import com.spa.appointments.domain.model.Sede
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GastoFormDialog(
    metodosPago: List<MetodoPago>,
    sedes: List<Sede>,
    onDismiss: () -> Unit,
    onConfirmar: (GastoRequest) -> Unit
) {
    var concepto by remember { mutableStateOf("") }
    var valor by remember { mutableStateOf("") }
    var fechaGasto by remember { mutableStateOf(LocalDate.now().toString()) }
    var sedeSeleccionada by remember { mutableStateOf<Sede?>(null) }
    var metodoPagoSeleccionado by remember { mutableStateOf<MetodoPago?>(null) }
    var detalleSeleccionado by remember { mutableStateOf<MetodoPagoDetalle?>(null) }
    var expandedSede by remember { mutableStateOf(false) }
    var expandedMetodo by remember { mutableStateOf(false) }
    var expandedDetalle by remember { mutableStateOf(false) }
    var mostrarDatePicker by remember { mutableStateOf(false) }

    val formularioValido = concepto.isNotBlank()
            && valor.toDoubleOrNull() != null
            && sedeSeleccionada != null
            && metodoPagoSeleccionado != null

    BasicAlertDialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            tonalElevation = 6.dp
        ) {
            Column(modifier = Modifier.padding(24.dp)) {

                // Header M3
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ReceiptLong,
                            contentDescription = null,
                            modifier = Modifier.padding(10.dp).size(24.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Column {
                        Text(
                            text = "Registrar Gasto",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Completa los datos del nuevo gasto",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                Spacer(modifier = Modifier.height(14.dp))

                // Campos en scroll
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Concepto
                    OutlinedTextField(
                        value = concepto,
                        onValueChange = { concepto = it },
                        label = { Text("Concepto") },
                        placeholder = { Text("Ej. Compra de insumos") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor   = MaterialTheme.colorScheme.surfaceContainerLow,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow
                        )
                    )

                    // Valor
                    OutlinedTextField(
                        value = valor,
                        onValueChange = { valor = it },
                        label = { Text("Valor ($)") },
                        placeholder = { Text("0") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        prefix = { Text("$ ") },
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor   = MaterialTheme.colorScheme.surfaceContainerLow,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow
                        ),
                        isError = valor.isNotBlank() && valor.toDoubleOrNull() == null,
                        supportingText = if (valor.isNotBlank() && valor.toDoubleOrNull() == null) {
                            { Text("Ingresa un número válido") }
                        } else null
                    )

                    // Fecha
                    OutlinedTextField(
                        value = fechaGasto,
                        onValueChange = {},
                        label = { Text("Fecha del gasto") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { mostrarDatePicker = true },
                        enabled = false,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = MaterialTheme.colorScheme.onSurface,
                            disabledBorderColor = MaterialTheme.colorScheme.outlineVariant,
                            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerLow
                        ),
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.CalendarMonth,
                                contentDescription = "Seleccionar fecha"
                            )
                        }
                    )

                    // Sede
                    ExposedDropdownMenuBox(
                        expanded = expandedSede,
                        onExpandedChange = { expandedSede = it }
                    ) {
                        OutlinedTextField(
                            value = sedeSeleccionada?.nombre ?: "",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Sede") },
                            placeholder = { Text("Selecciona una sede") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expandedSede) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor   = MaterialTheme.colorScheme.surfaceContainerLow,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = expandedSede,
                            onDismissRequest = { expandedSede = false }
                        ) {
                            sedes.forEach { sede ->
                                DropdownMenuItem(
                                    text = { Text(sede.nombre) },
                                    onClick = {
                                        sedeSeleccionada = sede
                                        expandedSede = false
                                    },
                                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                                )
                            }
                        }
                    }

                    // Método de pago
                    ExposedDropdownMenuBox(
                        expanded = expandedMetodo,
                        onExpandedChange = { expandedMetodo = it }
                    ) {
                        OutlinedTextField(
                            value = metodoPagoSeleccionado?.nombre ?: "",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Método de pago") },
                            placeholder = { Text("Selecciona un método") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expandedMetodo) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor   = MaterialTheme.colorScheme.surfaceContainerLow,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = expandedMetodo,
                            onDismissRequest = { expandedMetodo = false }
                        ) {
                            metodosPago.forEach { metodo ->
                                DropdownMenuItem(
                                    text = { Text(metodo.nombre) },
                                    onClick = {
                                        metodoPagoSeleccionado = metodo
                                        detalleSeleccionado = null
                                        expandedMetodo = false
                                    },
                                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                                )
                            }
                        }
                    }

                    // Detalle del método (solo si aplica)
                    if (metodoPagoSeleccionado?.detalles?.isNotEmpty() == true) {
                        ExposedDropdownMenuBox(
                            expanded = expandedDetalle,
                            onExpandedChange = { expandedDetalle = it }
                        ) {
                            OutlinedTextField(
                                value = detalleSeleccionado?.nombre ?: "",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Detalle del método") },
                                placeholder = { Text("Selecciona un detalle") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expandedDetalle) },
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor   = MaterialTheme.colorScheme.surfaceContainerLow,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow
                                )
                            )
                            ExposedDropdownMenu(
                                expanded = expandedDetalle,
                                onDismissRequest = { expandedDetalle = false }
                            ) {
                                metodoPagoSeleccionado?.detalles.orEmpty().forEach { detalle ->
                                    DropdownMenuItem(
                                        text = { Text(detalle.nombre) },
                                        onClick = {
                                            detalleSeleccionado = detalle
                                            expandedDetalle = false
                                        },
                                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                Spacer(modifier = Modifier.height(14.dp))

                // Botones M3
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cancelar")
                    }
                    Button(
                        enabled = formularioValido,
                        onClick = {
                            onConfirmar(
                                GastoRequest(
                                    idSede = sedeSeleccionada!!.id,
                                    concepto = concepto.trim(),
                                    valor = valor.toDouble(),
                                    idMetodoPago = metodoPagoSeleccionado!!.id,
                                    idMetodoPagoDetalle = detalleSeleccionado?.id,
                                    fechaGasto = fechaGasto
                                )
                            )
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Guardar", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // DatePicker
    if (mostrarDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = System.currentTimeMillis()
        )
        DatePickerDialog(
            onDismissRequest = { mostrarDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        fechaGasto = Instant.ofEpochMilli(millis)
                            .atOffset(ZoneOffset.UTC)
                            .toLocalDate()
                            .toString()
                    }
                    mostrarDatePicker = false
                }) { Text("Aceptar") }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDatePicker = false }) { Text("Cancelar") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
