package com.spa.appointments.ui.ingresos

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.unit.dp
import com.spa.appointments.domain.model.IngresoRequest
import com.spa.appointments.domain.model.MetodoPago
import com.spa.appointments.domain.model.MetodoPagoDetalle
import com.spa.appointments.domain.model.Sede
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IngresoFormDialog(
    metodosPago: List<MetodoPago>,
    sedes: List<Sede>,
    onDismiss: () -> Unit,
    onConfirmar: (IngresoRequest) -> Unit
) {
    var concepto by remember { mutableStateOf("") }
    var valor by remember { mutableStateOf("") }
    var fechaIngreso by remember { mutableStateOf(LocalDate.now().toString()) }
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
                            text = "Registrar Ingreso",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Completa los datos del ingreso manual",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(16.dp))

                // Formulario con Scroll
                Column(
                    modifier = Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Concepto
                    OutlinedTextField(
                        value = concepto,
                        onValueChange = { concepto = it },
                        label = { Text("Concepto") },
                        placeholder = { Text("Ej. Venta de productos de vitrina") },
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

                    // Selector de Fecha
                    OutlinedTextField(
                        value = fechaIngreso,
                        onValueChange = {},
                        label = { Text("Fecha del ingreso") },
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

                    // Dropdown Sedes
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
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
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

                    // Dropdown Método de Pago
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
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
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

                    // Dropdown Detalle de Método (Opcional)
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
                                modifier = Modifier.menuAnchor().fillMaxWidth(),
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

                Spacer(modifier = Modifier.height(24.dp))

                // Acciones
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancelar")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (formularioValido) {
                                onConfirmar(
                                    IngresoRequest(
                                        idSede = sedeSeleccionada!!.id,
                                        concepto = concepto.trim(),
                                        valor = valor.toDouble(),
                                        idMetodoPago = metodoPagoSeleccionado!!.id,
                                        idMetodoPagoDetalle = detalleSeleccionado?.id,
                                        fechaIngreso = fechaIngreso
                                    )
                                )
                            }
                        },
                        enabled = formularioValido,
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Text("Confirmar")
                    }
                }
            }
        }
    }

    // Modal DatePicker de Material3
    if (mostrarDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { mostrarDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            fechaIngreso = Instant.ofEpochMilli(millis)
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate()
                                .toString()
                        }
                        mostrarDatePicker = false
                    }
                ) { Text("Aceptar") }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDatePicker = false }) { Text("Cancelar") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}