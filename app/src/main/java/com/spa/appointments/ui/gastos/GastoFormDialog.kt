package com.spa.appointments.ui.gastos

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.spa.appointments.domain.model.GastoRequest
import com.spa.appointments.domain.model.MetodoPago
import com.spa.appointments.domain.model.MetodoPagoDetalle
import com.spa.appointments.domain.model.Sede
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
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
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(modifier = Modifier.padding(24.dp)) {

                // Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ReceiptLong,
                        contentDescription = null,
                        modifier = Modifier.size(28.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Column {
                        Text(
                            text = "Registrar Gasto",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Completa los datos del gasto",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(16.dp))

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
                        shape = MaterialTheme.shapes.medium
                    )

                    // Valor
                    OutlinedTextField(
                        value = valor,
                        onValueChange = { valor = it },
                        label = { Text("Valor") },
                        placeholder = { Text("0") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        prefix = { Text("$ ") },
                        shape = MaterialTheme.shapes.medium,
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
                        shape = MaterialTheme.shapes.medium,
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = MaterialTheme.colorScheme.onSurface,
                            disabledBorderColor = MaterialTheme.colorScheme.outline,
                            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
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
                            shape = MaterialTheme.shapes.medium
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
                            shape = MaterialTheme.shapes.medium
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
                                shape = MaterialTheme.shapes.medium
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

                Spacer(modifier = Modifier.height(20.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(16.dp))

                // Botones
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
                ) {
                    TextButton(onClick = onDismiss) {
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
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Text("Guardar")
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