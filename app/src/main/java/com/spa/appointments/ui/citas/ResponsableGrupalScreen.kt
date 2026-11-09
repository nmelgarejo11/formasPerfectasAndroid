package com.spa.appointments.ui.citas

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.spa.appointments.domain.model.Cliente
import com.spa.appointments.ui.clientes.SeleccionarClienteUiState
import com.spa.appointments.ui.clientes.SeleccionarClienteViewModel
import com.spa.appointments.ui.disponibilidad.DisponibilidadUiState
import kotlinx.coroutines.flow.StateFlow

private const val MIN_PERSONAS = 1
private const val MAX_PERSONAS_DEFAULT = 20

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResponsableGrupalScreen(
    onBack: () -> Unit,
    onContinuar: (cliente: Cliente, personas: Int) -> Unit,
    maxPersonasPermitido: Int? = null,
    clienteVm: SeleccionarClienteViewModel = hiltViewModel()
) {
    val clienteUiState by clienteVm.uiState.collectAsState()
    val textoBusqueda by clienteVm.textoBusqueda.collectAsState()

    val limiteMaximo = (maxPersonasPermitido ?: MAX_PERSONAS_DEFAULT).coerceAtLeast(MIN_PERSONAS)
    var clienteSeleccionado by remember { mutableStateOf<Cliente?>(null) }
    var cantidadPersonas by remember { mutableIntStateOf(MIN_PERSONAS) }

    LaunchedEffect(limiteMaximo) {
        if (cantidadPersonas > limiteMaximo) {
            cantidadPersonas = limiteMaximo
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Reserva Grupal",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Encabezado
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
                    .padding(horizontal = 20.dp, vertical = 14.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "Cliente Responsable del Grupo",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Selecciona el cliente y la cantidad de asistentes.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // ── SECCIÓN 1: CLIENTE SELECCIONADO O BUSCADOR ────────────────
                if (clienteSeleccionado != null) {
                    val c = clienteSeleccionado!!
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                modifier = Modifier.size(48.dp).clip(CircleShape),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = c.nombre.firstOrNull()?.uppercase() ?: "?",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }

                            Spacer(Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = c.nombreCompleto,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }

                                if (!c.telefono.isNullOrBlank()) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(top = 2.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Phone,
                                            contentDescription = null,
                                            modifier = Modifier.size(12.dp),
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(Modifier.width(4.dp))
                                        Text(
                                            text = c.telefono,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }

                            TextButton(onClick = { clienteSeleccionado = null; clienteVm.resetear() }) {
                                Text("Cambiar")
                            }
                        }
                    }
                } else {
                    // Buscador de Cliente
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Buscar cliente *",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        OutlinedTextField(
                            value = textoBusqueda,
                            onValueChange = { clienteVm.buscar(it) },
                            placeholder = { Text("Escribe nombre o teléfono...") },
                            leadingIcon = { Icon(Icons.Default.Search, null) },
                            trailingIcon = {
                                if (textoBusqueda.isNotEmpty()) {
                                    IconButton(onClick = { clienteVm.resetear() }) {
                                        Icon(Icons.Default.Close, "Limpiar")
                                    }
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Resultados de búsqueda
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 200.dp)
                        ) {
                            when (val state = clienteUiState) {
                                is SeleccionarClienteUiState.Buscando -> {
                                    CircularProgressIndicator(
                                        modifier = Modifier.align(Alignment.Center).size(28.dp),
                                        strokeWidth = 2.dp
                                    )
                                }
                                is SeleccionarClienteUiState.SinResultados -> {
                                    Text(
                                        text = "No se encontraron clientes con \"$textoBusqueda\"",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(vertical = 12.dp)
                                    )
                                }
                                is SeleccionarClienteUiState.Resultados -> {
                                    LazyColumn(
                                        verticalArrangement = Arrangement.spacedBy(6.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        items(state.clientes, key = { it.id }) { item ->
                                            Surface(
                                                shape = RoundedCornerShape(10.dp),
                                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable {
                                                        clienteSeleccionado = item
                                                        clienteVm.resetear()
                                                    }
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Icon(
                                                        Icons.Default.Person,
                                                        contentDescription = null,
                                                        modifier = Modifier.size(18.dp),
                                                        tint = MaterialTheme.colorScheme.primary
                                                    )
                                                    Spacer(Modifier.width(10.dp))
                                                    Column(modifier = Modifier.weight(1f)) {
                                                        Text(
                                                            text = item.nombreCompleto,
                                                            style = MaterialTheme.typography.bodyMedium,
                                                            fontWeight = FontWeight.SemiBold
                                                        )
                                                        if (!item.telefono.isNullOrBlank()) {
                                                            Text(
                                                                text = item.telefono,
                                                                style = MaterialTheme.typography.labelSmall,
                                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                                else -> Unit
                            }
                        }
                    }
                }

                HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)

                // ── SECCIÓN 2: CANTIDAD DE ASISTENTES ─────────────────────────
                PersonasSelectorCard(
                    cantidad = cantidadPersonas,
                    limiteMaximo = limiteMaximo,
                    maxPersonasPermitido = maxPersonasPermitido,
                    onIncrement = { if (cantidadPersonas < limiteMaximo) cantidadPersonas++ },
                    onDecrement = { if (cantidadPersonas > MIN_PERSONAS) cantidadPersonas-- }
                )

                Spacer(modifier = Modifier.weight(1f))

                // ── SECCIÓN 3: BOTÓN CONFIRMAR ────────────────────────────────
                Button(
                    onClick = {
                        val c = clienteSeleccionado
                        if (c != null && cantidadPersonas <= limiteMaximo) {
                            onContinuar(c, cantidadPersonas)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    enabled = clienteSeleccionado != null && cantidadPersonas <= limiteMaximo,
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        text = "Continuar a selección de profesional",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun PersonasSelectorCard(
    cantidad: Int,
    limiteMaximo: Int,
    maxPersonasPermitido: Int?,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = "Cantidad de asistentes",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = if (maxPersonasPermitido != null)
                        "Máximo del servicio: $maxPersonasPermitido personas"
                    else
                        "Incluyendo al responsable",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (cantidad >= limiteMaximo && maxPersonasPermitido != null)
                        MaterialTheme.colorScheme.tertiary
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = if (cantidad >= limiteMaximo && maxPersonasPermitido != null)
                        FontWeight.Bold
                    else
                        FontWeight.Normal
                )
            }

            Spacer(Modifier.width(12.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Botón Disminuir (-)
                val puedeDisminuir = cantidad > MIN_PERSONAS
                Surface(
                    onClick = onDecrement,
                    enabled = puedeDisminuir,
                    shape = CircleShape,
                    color = if (puedeDisminuir)
                        MaterialTheme.colorScheme.secondaryContainer
                    else
                        MaterialTheme.colorScheme.surfaceContainerHigh,
                    contentColor = if (puedeDisminuir)
                        MaterialTheme.colorScheme.onSecondaryContainer
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f),
                    modifier = Modifier.size(38.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Remove,
                            contentDescription = "Disminuir",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Número de personas
                Box(
                    modifier = Modifier
                        .widthIn(min = 44.dp)
                        .height(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = cantidad.toString(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }

                // Botón Aumentar (+)
                val puedeAumentar = cantidad < limiteMaximo
                Surface(
                    onClick = onIncrement,
                    enabled = puedeAumentar,
                    shape = CircleShape,
                    color = if (puedeAumentar)
                        MaterialTheme.colorScheme.primaryContainer
                    else
                        MaterialTheme.colorScheme.surfaceContainerHigh,
                    contentColor = if (puedeAumentar)
                        MaterialTheme.colorScheme.onPrimaryContainer
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f),
                    modifier = Modifier.size(38.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Aumentar",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}
