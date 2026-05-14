package com.spa.appointments.ui.admin.profesionales

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.spa.appointments.domain.model.CargoAdmin
import com.spa.appointments.domain.model.ProfesionalAdmin
import com.spa.appointments.domain.model.ProfesionalRequest
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.sp

// ui/admin/profesionales/ProfesionalDialog.kt
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
    val esNuevo  = profesional == null

    AlertDialog(
        onDismissRequest = { if (!guardando) onDismiss() },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(
                    imageVector = if (esNuevo) Icons.Outlined.PersonAdd else Icons.Outlined.Edit,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(if (esNuevo) "Nuevo profesional" else "Editar profesional")
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {

                // Subtítulo
                Text(
                    text = "Completa los datos del profesional. Los campos con * son obligatorios.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.height(8.dp))

                // Cargo
                ExposedDropdownMenuBox(
                    expanded = expandedCargo,
                    onExpandedChange = { expandedCargo = it }
                ) {
                    OutlinedTextField(
                        value = cargoSeleccionado?.nombre ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Cargo *") },
                        placeholder = { Text("Selecciona cargo") },
                        leadingIcon = { Icon(Icons.Outlined.Work, contentDescription = null) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expandedCargo) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        singleLine = true
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

                Spacer(Modifier.height(4.dp))

                    OutlinedTextField(
                        value = nombre,
                        onValueChange = { nombre = it },
                        label = { Text("Nombre *") },
                        leadingIcon = { Icon(Icons.Outlined.Person, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                Spacer(Modifier.height(4.dp))

                    OutlinedTextField(
                        value = apellido,
                        onValueChange = { apellido = it },
                        label = { Text("Apellido *") },
                        leadingIcon = { Icon(Icons.Outlined.Person, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                Spacer(Modifier.height(4.dp))

                OutlinedTextField(
                    value = telefono,
                    onValueChange = { telefono = it },
                    label = { Text("Teléfono") },
                    leadingIcon = { Icon(Icons.Outlined.Phone, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    leadingIcon = { Icon(Icons.Outlined.Email, contentDescription = null) },
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
                if (guardando) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(Modifier.width(6.dp))
                    Text("Guardando...")
                } else {
                    Icon(Icons.Outlined.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Guardar")
                }
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss, enabled = !guardando) {
                Text("Cancelar")
            }
        }
    )
}