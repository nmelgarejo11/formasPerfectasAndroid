// Ruta: app/src/main/java/com/spa/appointments/ui/admin/profesionales/ProfesionalDialog.kt
package com.spa.appointments.ui.admin.profesionales

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.spa.appointments.domain.model.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfesionalDialog(
    profesional:    ProfesionalAdmin?,
    cargos:         List<CargoAdmin>,
    bancos:         List<BancoItem> = emptyList(),
    tiposDocumento: List<TipoDocumentoItem> = emptyList(),
    tiposCuenta:    List<TipoCuentaItem> = emptyList(),
    guardando:      Boolean,
    onGuardar:      (ProfesionalRequest) -> Unit,
    onDismiss:      () -> Unit
) {
    var nombre          by remember(profesional) { mutableStateOf(profesional?.nombre   ?: "") }
    var apellido        by remember(profesional) { mutableStateOf(profesional?.apellido ?: "") }
    var telefono        by remember(profesional) { mutableStateOf(profesional?.telefono ?: "") }
    var email           by remember(profesional) { mutableStateOf(profesional?.email    ?: "") }
    var identificacion  by remember(profesional) { mutableStateOf(profesional?.identificacion ?: "") }
    var numeroCuenta    by remember(profesional) { mutableStateOf(profesional?.numeroCuenta ?: "") }

    val cargoInicial = cargos.firstOrNull { it.id == profesional?.idCargo }
    var cargoSeleccionado by remember(profesional) { mutableStateOf(cargoInicial) }
    var expandedCargo    by remember { mutableStateOf(false) }

    val tipoDocInicial = tiposDocumento.firstOrNull { it.id == profesional?.idTipoDocumento }
    var tipoDocSeleccionado by remember(profesional) { mutableStateOf(tipoDocInicial) }
    var expandedTipoDoc by remember { mutableStateOf(false) }

    val bancoInicial = bancos.firstOrNull { it.id == profesional?.idBanco }
    var bancoSeleccionado by remember(profesional) { mutableStateOf(bancoInicial) }
    var expandedBanco by remember { mutableStateOf(false) }

    val tipoCuentaInicial = tiposCuenta.firstOrNull { it.id == profesional?.idTipoCuenta }
    var tipoCuentaSeleccionado by remember(profesional) { mutableStateOf(tipoCuentaInicial) }
    var expandedTipoCuenta by remember { mutableStateOf(false) }

    val esValido = nombre.isNotBlank() && apellido.isNotBlank() && cargoSeleccionado != null
    val esNuevo  = profesional == null

    BasicAlertDialog(onDismissRequest = { if (!guardando) onDismiss() }) {
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
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Icon(
                            imageVector = if (esNuevo) Icons.Outlined.PersonAdd else Icons.Outlined.Edit,
                            contentDescription = null,
                            modifier = Modifier.padding(10.dp).size(24.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Column {
                        Text(
                            text = if (esNuevo) "Nuevo profesional" else "Editar profesional",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Campos marcados con * son obligatorios",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                Spacer(modifier = Modifier.height(12.dp))

                // Scrollable Form
                Column(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
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
                            leadingIcon = { Icon(Icons.Outlined.Work, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expandedCargo) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor   = MaterialTheme.colorScheme.surfaceContainerLow,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow
                            )
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

                    // Nombre
                    OutlinedTextField(
                        value = nombre,
                        onValueChange = { nombre = it },
                        label = { Text("Nombre *") },
                        leadingIcon = { Icon(Icons.Outlined.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor   = MaterialTheme.colorScheme.surfaceContainerLow,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow
                        )
                    )

                    // Apellido
                    OutlinedTextField(
                        value = apellido,
                        onValueChange = { apellido = it },
                        label = { Text("Apellido *") },
                        leadingIcon = { Icon(Icons.Outlined.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor   = MaterialTheme.colorScheme.surfaceContainerLow,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow
                        )
                    )

                    // Tipo de Documento
                    if (tiposDocumento.isNotEmpty()) {
                        ExposedDropdownMenuBox(
                            expanded = expandedTipoDoc,
                            onExpandedChange = { expandedTipoDoc = it }
                        ) {
                            OutlinedTextField(
                                value = tipoDocSeleccionado?.nombre ?: "",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Tipo de Documento") },
                                placeholder = { Text("Selecciona tipo documento") },
                                leadingIcon = { Icon(Icons.Outlined.Badge, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expandedTipoDoc) },
                                modifier = Modifier.menuAnchor().fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(14.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor   = MaterialTheme.colorScheme.surfaceContainerLow,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow
                                )
                            )
                            ExposedDropdownMenu(
                                expanded = expandedTipoDoc,
                                onDismissRequest = { expandedTipoDoc = false }
                            ) {
                                tiposDocumento.filter { it.estado }.forEach { doc ->
                                    DropdownMenuItem(
                                        text = { Text("${doc.codigo} - ${doc.nombre}") },
                                        onClick = { tipoDocSeleccionado = doc; expandedTipoDoc = false }
                                    )
                                }
                            }
                        }
                    }

                    // Identificación
                    OutlinedTextField(
                        value = identificacion,
                        onValueChange = { identificacion = it },
                        label = { Text("Número de Documento") },
                        leadingIcon = { Icon(Icons.Outlined.Badge, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor   = MaterialTheme.colorScheme.surfaceContainerLow,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )

                    // Teléfono
                    OutlinedTextField(
                        value = telefono,
                        onValueChange = { telefono = it },
                        label = { Text("Teléfono") },
                        leadingIcon = { Icon(Icons.Outlined.Phone, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor   = MaterialTheme.colorScheme.surfaceContainerLow,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                    )

                    // Email
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email") },
                        leadingIcon = { Icon(Icons.Outlined.Email, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor   = MaterialTheme.colorScheme.surfaceContainerLow,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), thickness = 0.5.dp)

                    Text(
                        text = "INFORMACIÓN BANCARIA",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    // Banco
                    if (bancos.isNotEmpty()) {
                        ExposedDropdownMenuBox(
                            expanded = expandedBanco,
                            onExpandedChange = { expandedBanco = it }
                        ) {
                            OutlinedTextField(
                                value = bancoSeleccionado?.nombre ?: "",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Banco") },
                                placeholder = { Text("Selecciona banco") },
                                leadingIcon = { Icon(Icons.Outlined.AccountBalance, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expandedBanco) },
                                modifier = Modifier.menuAnchor().fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(14.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor   = MaterialTheme.colorScheme.surfaceContainerLow,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow
                                )
                            )
                            ExposedDropdownMenu(
                                expanded = expandedBanco,
                                onDismissRequest = { expandedBanco = false }
                            ) {
                                bancos.filter { it.estado }.forEach { banco ->
                                    DropdownMenuItem(
                                        text = { Text(banco.nombre) },
                                        onClick = { bancoSeleccionado = banco; expandedBanco = false }
                                    )
                                }
                            }
                        }
                    }

                    // Tipo de Cuenta
                    if (tiposCuenta.isNotEmpty()) {
                        ExposedDropdownMenuBox(
                            expanded = expandedTipoCuenta,
                            onExpandedChange = { expandedTipoCuenta = it }
                        ) {
                            OutlinedTextField(
                                value = tipoCuentaSeleccionado?.nombre ?: "",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Tipo de Cuenta") },
                                placeholder = { Text("Selecciona tipo de cuenta") },
                                leadingIcon = { Icon(Icons.Outlined.CreditCard, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expandedTipoCuenta) },
                                modifier = Modifier.menuAnchor().fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(14.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor   = MaterialTheme.colorScheme.surfaceContainerLow,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow
                                )
                            )
                            ExposedDropdownMenu(
                                expanded = expandedTipoCuenta,
                                onDismissRequest = { expandedTipoCuenta = false }
                            ) {
                                tiposCuenta.filter { it.estado }.forEach { tc ->
                                    DropdownMenuItem(
                                        text = { Text(tc.nombre) },
                                        onClick = { tipoCuentaSeleccionado = tc; expandedTipoCuenta = false }
                                    )
                                }
                            }
                        }
                    }

                    // Número de Cuenta
                    OutlinedTextField(
                        value = numeroCuenta,
                        onValueChange = { numeroCuenta = it },
                        label = { Text("Número de Cuenta") },
                        leadingIcon = { Icon(Icons.Outlined.Pin, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor   = MaterialTheme.colorScheme.surfaceContainerLow,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                Spacer(modifier = Modifier.height(14.dp))

                // Acciones M3
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        enabled = !guardando,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cancelar", fontWeight = FontWeight.SemiBold)
                    }

                    Button(
                        onClick = {
                            onGuardar(
                                ProfesionalRequest(
                                    idCargo         = cargoSeleccionado!!.id,
                                    nombre          = nombre.trim(),
                                    apellido        = apellido.trim(),
                                    telefono        = telefono.trim().ifBlank { null },
                                    email           = email.trim().ifBlank { null },
                                    idTipoDocumento = tipoDocSeleccionado?.id,
                                    identificacion  = identificacion.trim().ifBlank { null },
                                    idBanco         = bancoSeleccionado?.id,
                                    idTipoCuenta    = tipoCuentaSeleccionado?.id,
                                    numeroCuenta    = numeroCuenta.trim().ifBlank { null }
                                )
                            )
                        },
                        enabled = esValido && !guardando,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (guardando) {
                            CircularProgressIndicator(
                                modifier    = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color       = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Icon(Icons.Outlined.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Guardar", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
