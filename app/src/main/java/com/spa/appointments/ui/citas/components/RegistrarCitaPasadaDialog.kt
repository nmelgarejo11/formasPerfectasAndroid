package com.spa.appointments.ui.citas.components

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spa.appointments.core.utils.Constants
import com.spa.appointments.domain.model.Cliente
import com.spa.appointments.domain.model.MetodoPago
import com.spa.appointments.domain.model.MetodoPagoDetalle
import com.spa.appointments.domain.model.Profesional
import com.spa.appointments.domain.model.Servicio
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale

data class ServicioPasadoSeleccionado(
    val id: String = java.util.UUID.randomUUID().toString(),
    val servicio: Servicio,
    var profesional: Profesional? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistrarCitaPasadaDialog(
    clientesDisponibles: List<Cliente>,
    servicios: List<Servicio>,
    profesionalesBase: List<Profesional>,
    metodosPago: List<MetodoPago>,
    onBuscarCliente: (String) -> Unit,
    onObtenerProfesionalesPorServicio: suspend (idServicio: Int) -> List<Profesional>,
    onConfirmar: (
        idSede: Int,
        idCliente: Int,
        serviciosConProfesional: List<Pair<Servicio, Profesional>>,
        duracionTotalMinutos: Int,
        fechaHoraInicioIso: String,
        fechaHoraFinIso: String,
        idMetodoPago: Int,
        idMetodoPagoDetalle: Int?,
        precioFinal: Double,
        notas: String?
    ) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    // 1. Estado Cliente
    var queryBusqueda by remember { mutableStateOf("") }
    var clienteSeleccionado by remember { mutableStateOf<Cliente?>(null) }
    var mostrarBuscadorCliente by remember { mutableStateOf(false) }

    // 2. Estado Lista Múltiple de Servicios
    val serviciosAgregados = remember { mutableStateListOf<ServicioPasadoSeleccionado>() }
    var busquedaServicio by remember { mutableStateOf("") }
    var mostrarBuscadorServicio by remember { mutableStateOf(false) }

    // 3. Estado Fecha y Hora
    var fechaSeleccionada by remember { mutableStateOf(LocalDate.now().minusDays(1)) }
    var horaInicio by remember { mutableStateOf(LocalTime.of(10, 0)) }

    // 4. Estado Pago y Monto Inicial $0
    var metodoPagoSeleccionado by remember { mutableStateOf<MetodoPago?>(null) }
    var detallePagoSeleccionado by remember { mutableStateOf<MetodoPagoDetalle?>(null) }
    var precioFinalText by remember { mutableStateOf("0") }
    var observacionesText by remember { mutableStateOf("") }

    // Dropdowns de Métodos de Pago
    var expandedMetodoPago by remember { mutableStateOf(false) }
    var expandedDetallePago by remember { mutableStateOf(false) }

    var mostrarDatePicker by remember { mutableStateOf(false) }
    var mostrarTimePicker by remember { mutableStateOf(false) }

    // Filtrados dinámicos de Servicios (Mostrar solo nombre)
    val serviciosFiltrados = remember(servicios, busquedaServicio) {
        if (busquedaServicio.isBlank()) servicios
        else servicios.filter { it.nombre.contains(busquedaServicio, ignoreCase = true) }
    }

    // Duración total acumulada de los servicios seleccionados
    val duracionTotalMinutos = remember(serviciosAgregados.size) {
        if (serviciosAgregados.isEmpty()) 60
        else serviciosAgregados.sumOf { it.servicio.duracionMinutos }
    }

    // Recalcular total acumulado sugerido (0 si no hay servicios)
    val totalAcumuladoSugerido = remember(serviciosAgregados.size) {
        serviciosAgregados.sumOf { it.servicio.precioBase }
    }

    LaunchedEffect(serviciosAgregados.size, totalAcumuladoSugerido) {
        precioFinalText = if (serviciosAgregados.isEmpty()) "0" else String.format(Locale.US, "%.0f", totalAcumuladoSugerido)
    }

    // Hora de fin según la duración acumulada
    val horaFin = remember(horaInicio, duracionTotalMinutos) {
        horaInicio.plusMinutes(duracionTotalMinutos.toLong())
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(28.dp),
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        icon = {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.HistoryEdu,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(28.dp)
                )
            }
        },
        title = {
            Text(
                text = "Registrar Cita de Fecha Pasada",
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.titleMedium
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "Esta opción ingresa la cita directamente como finalizada con su correspondiente pago.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }

                // ── 1. Cliente ──────────────────────────────────────────────
                Text(
                    text = "Cliente *",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )

                if (clienteSeleccionado != null && !mostrarBuscadorCliente) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Person, null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Column {
                                    Text(
                                        text = clienteSeleccionado!!.nombreCompleto,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    if (!clienteSeleccionado!!.telefono.isNullOrBlank()) {
                                        Text(
                                            text = clienteSeleccionado!!.telefono ?: "",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                            TextButton(onClick = { mostrarBuscadorCliente = true }) {
                                Text("Cambiar")
                            }
                        }
                    }
                } else {
                    OutlinedTextField(
                        value = queryBusqueda,
                        onValueChange = {
                            queryBusqueda = it
                            onBuscarCliente(it)
                        },
                        label = { Text("Buscar cliente") },
                        leadingIcon = { Icon(Icons.Default.Search, null) },
                        trailingIcon = {
                            if (queryBusqueda.isNotEmpty()) {
                                IconButton(onClick = { queryBusqueda = ""; onBuscarCliente("") }) {
                                    Icon(Icons.Default.Clear, null)
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    if (clientesDisponibles.isNotEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerLow,
                            modifier = Modifier.fillMaxWidth().heightIn(max = 130.dp)
                        ) {
                            LazyColumn(modifier = Modifier.padding(2.dp)) {
                                items(clientesDisponibles) { cli ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                clienteSeleccionado = cli
                                                mostrarBuscadorCliente = false
                                                queryBusqueda = ""
                                            }
                                            .padding(horizontal = 8.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Default.Person, null,
                                            modifier = Modifier.size(16.dp),
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(Modifier.width(6.dp))
                                        Column {
                                            Text(
                                                text = cli.nombreCompleto,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            if (!cli.telefono.isNullOrBlank()) {
                                                Text(
                                                    text = cli.telefono ?: "",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }
                                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                                }
                            }
                        }
                    }
                }

                // ── 2. Lista Múltiple de Servicios con Profesional Individual ──
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Servicios Seleccionados (${serviciosAgregados.size}) *",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (serviciosAgregados.isNotEmpty() && !mostrarBuscadorServicio) {
                        TextButton(
                            onClick = { mostrarBuscadorServicio = true },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                        ) {
                            Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Agregar otro", fontSize = 12.sp)
                        }
                    }
                }

                // Ítems de Servicios Agregados
                serviciosAgregados.forEachIndexed { index, item ->
                    // Carga de profesionales validados para este servicio específico
                    var profesionalesDelServicio by remember(item.servicio.id) { mutableStateOf<List<Profesional>>(emptyList()) }
                    var cargandoProfsServicio by remember(item.servicio.id) { mutableStateOf(true) }

                    LaunchedEffect(item.servicio.id) {
                        cargandoProfsServicio = true
                        val res = onObtenerProfesionalesPorServicio(item.servicio.id)
                        profesionalesDelServicio = if (res.isNotEmpty()) res else profesionalesBase
                        cargandoProfsServicio = false
                        // Autoseleccionar si sólo hay uno disponible
                        if (item.profesional == null && profesionalesDelServicio.size == 1) {
                            item.profesional = profesionalesDelServicio.first()
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerLow,
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Icon(
                                        Icons.Default.Spa, null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        text = "${index + 1}. ${item.servicio.nombre}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                IconButton(
                                    onClick = { serviciosAgregados.removeAt(index) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(Icons.Default.Close, "Eliminar", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                                }
                            }

                            // Selector de Profesional validado para este servicio
                            var expProfItem by remember { mutableStateOf(false) }
                            ExposedDropdownMenuBox(
                                expanded = expProfItem,
                                onExpandedChange = { expProfItem = it }
                            ) {
                                OutlinedTextField(
                                    value = when {
                                        cargandoProfsServicio -> "Cargando profesionales..."
                                        item.profesional != null -> item.profesional!!.nombreCompleto
                                        else -> "Seleccionar Profesional *"
                                    },
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Profesional del servicio", fontSize = 11.sp) },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expProfItem) },
                                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp)
                                )
                                ExposedDropdownMenu(
                                    expanded = expProfItem,
                                    onDismissRequest = { expProfItem = false }
                                ) {
                                    if (profesionalesDelServicio.isEmpty()) {
                                        DropdownMenuItem(
                                            text = { Text("Sin profesionales para este servicio", style = MaterialTheme.typography.bodySmall) },
                                            onClick = { expProfItem = false }
                                        )
                                    } else {
                                        profesionalesDelServicio.forEach { prof ->
                                            DropdownMenuItem(
                                                text = { Text(prof.nombreCompleto) },
                                                onClick = {
                                                    item.profesional = prof
                                                    expProfItem = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Buscador/Añadir Servicio
                if (serviciosAgregados.isEmpty() || mostrarBuscadorServicio) {
                    OutlinedTextField(
                        value = busquedaServicio,
                        onValueChange = { busquedaServicio = it },
                        label = { Text("Buscar servicio") },
                        leadingIcon = { Icon(Icons.Default.Search, null) },
                        trailingIcon = {
                            if (busquedaServicio.isNotEmpty()) {
                                IconButton(onClick = { busquedaServicio = "" }) {
                                    Icon(Icons.Default.Clear, null)
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    if (serviciosFiltrados.isNotEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerLow,
                            modifier = Modifier.fillMaxWidth().heightIn(max = 130.dp)
                        ) {
                            LazyColumn(modifier = Modifier.padding(2.dp)) {
                                items(serviciosFiltrados) { serv ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                serviciosAgregados.add(
                                                    ServicioPasadoSeleccionado(
                                                        servicio = serv
                                                    )
                                                )
                                                mostrarBuscadorServicio = false
                                                busquedaServicio = ""
                                            }
                                            .padding(horizontal = 10.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Default.Spa, null,
                                            modifier = Modifier.size(16.dp),
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text(
                                            text = serv.nombre,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                                }
                            }
                        }
                    }
                }

                // Banner de Total Acumulado de Servicios
                if (serviciosAgregados.isNotEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Total de servicios (${serviciosAgregados.size}):",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "$%,.0f".format(totalAcumuladoSugerido),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }

                // ── 3. Fecha y Hora de la Cita (2 filas separadas) ──────────
                Text(
                    text = "Fecha y Hora de la Cita *",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedButton(
                        onClick = { mostrarDatePicker = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.CalendarToday, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Fecha: ${fechaSeleccionada.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    OutlinedButton(
                        onClick = { mostrarTimePicker = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Schedule, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Hora: ${formatearHora12h(horaInicio)} - ${formatearHora12h(horaFin)}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // ── 4. Método de Pago ────────────────────────────────────────
                Text(
                    text = "Método de Pago *",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )

                ExposedDropdownMenuBox(
                    expanded = expandedMetodoPago,
                    onExpandedChange = { expandedMetodoPago = it }
                ) {
                    OutlinedTextField(
                        value = metodoPagoSeleccionado?.nombre ?: "Seleccionar método de pago...",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expandedMetodoPago) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = expandedMetodoPago,
                        onDismissRequest = { expandedMetodoPago = false }
                    ) {
                        metodosPago.forEach { mp ->
                            DropdownMenuItem(
                                text = { Text(mp.nombre) },
                                onClick = {
                                    metodoPagoSeleccionado = mp
                                    detallePagoSeleccionado = null
                                    expandedMetodoPago = false
                                }
                            )
                        }
                    }
                }

                if (metodoPagoSeleccionado?.detalles?.isNotEmpty() == true) {
                    Text(
                        text = "Detalle de Pago *",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )

                    ExposedDropdownMenuBox(
                        expanded = expandedDetallePago,
                        onExpandedChange = { expandedDetallePago = it }
                    ) {
                        OutlinedTextField(
                            value = detallePagoSeleccionado?.nombre ?: "Seleccionar detalle...",
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expandedDetallePago) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = expandedDetallePago,
                            onDismissRequest = { expandedDetallePago = false }
                        ) {
                            metodoPagoSeleccionado!!.detalles!!.forEach { det ->
                                DropdownMenuItem(
                                    text = { Text(det.nombre) },
                                    onClick = {
                                        detallePagoSeleccionado = det
                                        expandedDetallePago = false
                                    }
                                )
                            }
                        }
                    }
                }

                // ── 5. Monto / Precio Final ─────────────────────────────────
                Text(
                    text = "Precio / Monto Final *",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )

                OutlinedTextField(
                    value = precioFinalText,
                    onValueChange = { precioFinalText = it },
                    label = { Text("Monto pagado ($)") },
                    leadingIcon = { Icon(Icons.Default.AttachMoney, null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                // ── 6. Observaciones / Notas ────────────────────────────────
                Text(
                    text = "Notas / Observaciones (Opcional)",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )

                OutlinedTextField(
                    value = observacionesText,
                    onValueChange = { observacionesText = it },
                    label = { Text("Ej: Cita pasada") },
                    leadingIcon = { Icon(Icons.AutoMirrored.Filled.Notes, null) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    maxLines = 2
                )
            }
        },
        confirmButton = {
            val cliente = clienteSeleccionado
            val metodo = metodoPagoSeleccionado
            val precioDouble = precioFinalText.toDoubleOrNull()
            val requiereDetalle = metodo?.detalles?.isNotEmpty() == true
            val detalleValido = !requiereDetalle || detallePagoSeleccionado != null
            val tieneServicios = serviciosAgregados.isNotEmpty()
            val todosTienenProf = serviciosAgregados.all { it.profesional != null }

            val formularioValido = cliente != null &&
                    tieneServicios &&
                    todosTienenProf &&
                    metodo != null &&
                    detalleValido &&
                    precioDouble != null && precioDouble >= 0

            Button(
                onClick = {
                    if (formularioValido) {
                        val formatterIso = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
                        val startDateTime = LocalDateTime.of(fechaSeleccionada, horaInicio)
                        val endDateTime = LocalDateTime.of(fechaSeleccionada, horaFin)

                        val startIso = formatterIso.format(startDateTime)
                        val endIso = formatterIso.format(endDateTime)

                        val listaPairs = serviciosAgregados.map { item ->
                            Pair(item.servicio, item.profesional!!)
                        }

                        val idSedeUsar = listaPairs.firstOrNull()?.second?.idSede ?: Constants.ID_SEDE_DEFAULT

                        onConfirmar(
                            idSedeUsar,
                            cliente!!.id,
                            listaPairs,
                            duracionTotalMinutos,
                            startIso,
                            endIso,
                            metodo!!.id,
                            detallePagoSeleccionado?.id,
                            precioDouble!!,
                            observacionesText.ifBlank { "Cita pasada" }
                        )
                        onDismiss()
                    }
                },
                enabled = formularioValido,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Check, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Registrar Cita Pasada", fontWeight = FontWeight.Bold)
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

    // Diálogo DatePicker (restringido únicamente a fechas pasadas)
    if (mostrarDatePicker) {
        val cal = Calendar.getInstance()
        cal.set(fechaSeleccionada.year, fechaSeleccionada.monthValue - 1, fechaSeleccionada.dayOfMonth)

        val dpd = DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val nuevaFecha = LocalDate.of(year, month + 1, dayOfMonth)
                if (nuevaFecha.isBefore(LocalDate.now())) {
                    fechaSeleccionada = nuevaFecha
                }
                mostrarDatePicker = false
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        )

        // Deshabilitar hoy y todas las fechas futuras (maxDate = ayer)
        val maxCal = Calendar.getInstance()
        maxCal.add(Calendar.DAY_OF_MONTH, -1)
        maxCal.set(Calendar.HOUR_OF_DAY, 23)
        maxCal.set(Calendar.MINUTE, 59)
        maxCal.set(Calendar.SECOND, 59)
        dpd.datePicker.maxDate = maxCal.timeInMillis

        dpd.setOnCancelListener { mostrarDatePicker = false }
        dpd.setOnDismissListener { mostrarDatePicker = false }
        LaunchedEffect(Unit) { dpd.show() }
    }

    // Diálogo TimePicker (hora de inicio)
    if (mostrarTimePicker) {
        val tpd = TimePickerDialog(
            context,
            { _, hourOfDay, minute ->
                horaInicio = LocalTime.of(hourOfDay, minute)
                mostrarTimePicker = false
            },
            horaInicio.hour,
            horaInicio.minute,
            false // 12h AM/PM
        )
        tpd.setOnCancelListener { mostrarTimePicker = false }
        tpd.setOnDismissListener { mostrarTimePicker = false }
        LaunchedEffect(Unit) { tpd.show() }
    }
}

private fun formatearHora12h(time: LocalTime): String {
    val formatter = DateTimeFormatter.ofPattern("hh:mm a", Locale.US)
    return time.format(formatter)
}
