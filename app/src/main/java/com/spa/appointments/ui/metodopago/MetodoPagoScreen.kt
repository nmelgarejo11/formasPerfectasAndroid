package com.spa.appointments.ui.metodopago

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.spa.appointments.domain.model.MetodoPagoAdmin

// ─── Screen principal ─────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MetodoPagoScreen(
    onBack: () -> Unit,                                  // ← navegación atrás
    viewModel: MetodoPagoViewModel = hiltViewModel(),
    onVerDetalles: (MetodoPagoAdmin) -> Unit
) {
    val metodos by viewModel.metodos.collectAsStateWithLifecycle()
    val loading by viewModel.loading.collectAsStateWithLifecycle()
    val error   by viewModel.error.collectAsStateWithLifecycle()

    var showDialog by remember { mutableStateOf(false) }
    var editando   by remember { mutableStateOf<MetodoPagoAdmin?>(null) }
    var busqueda   by remember { mutableStateOf("") }

    val filtrados = remember(metodos, busqueda) {
        if (busqueda.isBlank()) metodos
        else metodos.filter { it.nombre.contains(busqueda, ignoreCase = true) }
    }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) { viewModel.cargarMetodos() }

    LaunchedEffect(error) {
        error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.limpiarError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text       = "Métodos de pago",
                            style      = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        if (metodos.isNotEmpty()) {
                            Text(
                                text  = "${metodos.size} ${if (metodos.size == 1) "método" else "métodos"}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {          // ← flecha atrás
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { editando = null; showDialog = true },
                icon    = { Icon(Icons.Default.Add, null) },
                text    = { Text("Nuevo método") }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // ── Buscador ──────────────────────────────────────────────────
            OutlinedTextField(
                value         = busqueda,
                onValueChange = { busqueda = it },
                label         = { Text("Buscar método") },
                leadingIcon   = { Icon(Icons.Default.Search, null) },
                trailingIcon  = {
                    if (busqueda.isNotBlank()) {
                        IconButton(onClick = { busqueda = "" }) {
                            Icon(Icons.Default.Clear, null)
                        }
                    }
                },
                modifier   = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape      = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    // Estado cargando sin datos
                    loading && metodos.isEmpty() -> {
                        Column(
                            modifier            = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            CircularProgressIndicator()
                            Text(
                                "Cargando métodos…",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Lista vacía o sin resultados
                    filtrados.isEmpty() -> {
                        Column(
                            modifier            = Modifier
                                .align(Alignment.Center)
                                .padding(40.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(24.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Icon(
                                    imageVector        = if (busqueda.isBlank()) Icons.Default.CreditCard
                                    else Icons.Default.SearchOff,
                                    contentDescription = null,
                                    modifier           = Modifier
                                        .padding(20.dp)
                                        .size(48.dp),
                                    tint               = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text       = if (busqueda.isBlank()) "Sin métodos aún"
                                else "Sin resultados",
                                style      = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text      = if (busqueda.isBlank())
                                    "Crea el primer método de pago con el botón inferior."
                                else
                                    "Ningún método coincide con \"$busqueda\".",
                                style     = MaterialTheme.typography.bodyMedium,
                                color     = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    // Lista con datos
                    else -> {
                        LazyColumn(
                            contentPadding      = PaddingValues(
                                start  = 16.dp,
                                end    = 16.dp,
                                top    = 4.dp,
                                bottom = 88.dp          // espacio para el FAB
                            ),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(filtrados, key = { it.id }) { metodo ->
                                MetodoPagoCard(
                                    metodo        = metodo,
                                    onEditar      = { editando = metodo; showDialog = true },
                                    onVerDetalles = onVerDetalles
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDialog) {
        MetodoPagoDialog(
            metodo      = editando,
            guardando   = loading,
            onGuardar   = { nombre, activo ->
                if (editando == null) viewModel.crearMetodo(nombre)
                else viewModel.actualizarMetodo(editando!!.id, nombre, activo)
                showDialog = false
                editando   = null
            },
            onDismiss   = { showDialog = false; editando = null }
        )
    }
}

// ─── Card de método de pago ───────────────────────────────────────────────────

@Composable
private fun MetodoPagoCard(
    metodo: MetodoPagoAdmin,
    onEditar: () -> Unit,
    onVerDetalles: (MetodoPagoAdmin) -> Unit
) {
    Card(
        onClick    = onEditar,                          // ← tap en toda la card
        modifier   = Modifier.fillMaxWidth(),
        shape      = RoundedCornerShape(14.dp),
        elevation  = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors     = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier          = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Ícono contextual
            Surface(
                shape    = RoundedCornerShape(10.dp),
                color    = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector        = Icons.Default.CreditCard,
                        contentDescription = null,
                        modifier           = Modifier.size(22.dp),
                        tint               = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Text(
                        text       = metodo.nombre,
                        style      = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier   = Modifier.weight(1f)
                    )
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (metodo.activo)
                            MaterialTheme.colorScheme.primaryContainer
                        else
                            MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text     = if (metodo.activo) "Activo" else "Inactivo",
                            style    = MaterialTheme.typography.labelSmall,
                            color    = if (metodo.activo)
                                MaterialTheme.colorScheme.onPrimaryContainer
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            // Solo ícono de ver detalles
            IconButton(
                onClick  = { onVerDetalles(metodo) },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    Icons.Default.Visibility, "Ver detalles",
                    modifier = Modifier.size(18.dp),
                    tint     = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// ─── Diálogo crear / editar ───────────────────────────────────────────────────

@Composable
private fun MetodoPagoDialog(
    metodo: MetodoPagoAdmin?,
    guardando: Boolean,
    onGuardar: (nombre: String, activo: Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    var nombre by remember(metodo) { mutableStateOf(metodo?.nombre ?: "") }
    var activo by remember(metodo) { mutableStateOf(metodo?.activo ?: true) }
    val esNuevo = metodo == null

    AlertDialog(
        onDismissRequest = { if (!guardando) onDismiss() },
        shape = RoundedCornerShape(16.dp),
        icon  = {
            Surface(
                shape = RoundedCornerShape(50),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Icon(
                    imageVector        = if (esNuevo) Icons.Default.Add else Icons.Default.Edit,
                    contentDescription = null,
                    tint               = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier           = Modifier
                        .padding(10.dp)
                        .size(22.dp)
                )
            }
        },
        title = {
            Text(
                text       = if (esNuevo) "Nuevo método" else "Editar método",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value         = nombre,
                    onValueChange = { nombre = it },
                    label         = { Text("Nombre *") },
                    leadingIcon   = { Icon(Icons.Default.CreditCard, null) },
                    modifier      = Modifier.fillMaxWidth(),
                    singleLine    = true,
                    shape         = RoundedCornerShape(12.dp)
                )

                // Toggle estado (solo al editar)
                if (!esNuevo) {
                    Row(
                        modifier          = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "Estado",
                                style      = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                if (activo) "Visible en reservas" else "Oculto en reservas",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked         = activo,
                            onCheckedChange = { activo = it }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick  = { onGuardar(nombre.trim(), activo) },
                enabled  = nombre.isNotBlank() && !guardando,
                shape    = RoundedCornerShape(10.dp)
            ) {
                if (guardando) {
                    CircularProgressIndicator(
                        modifier    = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color       = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Icon(Icons.Default.Save, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Guardar")
                }
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick  = onDismiss,
                enabled  = !guardando,
                shape    = RoundedCornerShape(10.dp)
            ) { Text("Cancelar") }
        }
    )
}