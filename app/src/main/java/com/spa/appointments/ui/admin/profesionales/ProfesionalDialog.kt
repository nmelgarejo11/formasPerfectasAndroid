package com.spa.appointments.ui.admin.profesionales

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.spa.appointments.domain.model.CargoAdmin
import com.spa.appointments.domain.model.ProfesionalAdmin
import com.spa.appointments.domain.model.ProfesionalRequest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfesionalDialog(
    profesional: ProfesionalAdmin?,
    cargos:      List<CargoAdmin>,
    guardando:   Boolean,
    onGuardar:   (ProfesionalRequest) -> Unit,
    onDismiss:   () -> Unit
) {
    var nombre    by remember(profesional) { mutableStateOf(profesional?.nombre   ?: "") }
    var apellido  by remember(profesional) { mutableStateOf(profesional?.apellido ?: "") }
    var telefono  by remember(profesional) { mutableStateOf(profesional?.telefono ?: "") }
    var email     by remember(profesional) { mutableStateOf(profesional?.email    ?: "") }

    val cargoInicial = cargos.firstOrNull { it.id == profesional?.idCargo }
    var cargoSeleccionado by remember(profesional) { mutableStateOf(cargoInicial) }
    var expandedCargo    by remember { mutableStateOf(false) }

    val esValido = nombre.isNotBlank() && apellido.isNotBlank() && cargoSeleccionado != null

    AlertDialog(
        onDismissRequest = { if (!guardando) onDismiss() },
        title = { Text(if (profesional != null) "Editar profesional" else "Nuevo profesional") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {

                // Cargo
                ExposedDropdownMenuBox(
                    expanded = expandedCargo,
                    onExpandedChange = { expandedCargo = it }
                ) {
                    OutlinedTextField(
                        value = cargoSeleccionado?.nombre ?: "Selecciona cargo",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Cargo *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expandedCargo) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedCargo,
                        onDismissRequest = { expandedCargo = false }
                    ) {
                        cargos.forEach { cargo ->
                            DropdownMenuItem(
                                text = { Text(cargo.nombre) },
                                onClick = { cargoSeleccionado = cargo; expandedCargo = false }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = apellido,
                    onValueChange = { apellido = it },
                    label = { Text("Apellido *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = telefono,
                    onValueChange = { telefono = it },
                    label = { Text("Teléfono") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onGuardar(ProfesionalRequest(
                        idCargo  = cargoSeleccionado!!.id,
                        nombre   = nombre.trim(),
                        apellido = apellido.trim(),
                        telefono = telefono.trim().ifBlank { null },
                        email    = email.trim().ifBlank { null }
                    ))
                },
                enabled = esValido && !guardando
            ) {
                if (guardando) CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                else Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !guardando) { Text("Cancelar") }
        }
    )
}