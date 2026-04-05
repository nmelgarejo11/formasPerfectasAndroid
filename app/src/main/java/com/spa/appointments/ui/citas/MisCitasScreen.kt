package com.spa.appointments.ui.citas

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.spa.appointments.domain.model.Cita

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MisCitasScreen(
    onBack: () -> Unit,
    onVerHistorial: () -> Unit,
    vm: MisCitasViewModel = hiltViewModel()
) {
    val uiState    by vm.uiState.collectAsState()
    val accionState by vm.accionState.collectAsState()

    // Cita seleccionada para acción (cancelar o reagendar)
    var citaAccion      by remember { mutableStateOf<Cita?>(null) }
    var mostrarCancelar by remember { mutableStateOf(false) }
    var mostrarReagendar by remember { mutableStateOf(false) }
    var motivoReagendar by remember { mutableStateOf("") }

    // Snackbar para mensajes de éxito o error
    val snackbarHost = remember { SnackbarHostState() }

    // Reaccionar a resultados de acciones
    LaunchedEffect(accionState) {
        when (val s = accionState) {
            is AccionUiState.Success -> {
                snackbarHost.showSnackbar(s.mensaje)
                vm.resetAccion()
            }
            is AccionUiState.Error -> {
                snackbarHost.showSnackbar(s.mensaje)
                vm.resetAccion()
            }
            else -> Unit
        }
    }

    // Diálogo: Cancelar cita
    if (mostrarCancelar && citaAccion != null) {
        AlertDialog(
            onDismissRequest = { mostrarCancelar = false },
            icon  = {
                Icon(
                    Icons.Default.Cancel,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = { Text("Cancelar cita") },
            text  = {
                Text("¿Estás seguro que deseas cancelar tu cita con " +
                        "${citaAccion?.profesional}?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        mostrarCancelar = false
                        vm.cancelarCita(citaAccion!!.id)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) { Text("Sí, cancelar") }
            },
            dismissButton = {
                OutlinedButton(onClick = { mostrarCancelar = false }) {
                    Text("No, volver")
                }
            }
        )
    }

    // Diálogo: Solicitar reagendamiento
    if (mostrarReagendar && citaAccion != null) {
        AlertDialog(
            onDismissRequest = { mostrarReagendar = false },
            icon  = {
                Icon(
                    Icons.Default.EditCalendar,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            title = { Text("Solicitar reagendamiento") },
            text  = {
                Column {
                    Text(
                        text = "Tu solicitud será revisada por el negocio. " +
                                "Te notificaremos cuando sea aprobada.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value         = motivoReagendar,
                        onValueChange = { motivoReagendar = it },
                        label         = { Text("Motivo (opcional)") },
                        placeholder   = { Text("Ej: Tengo un compromiso...") },
                        modifier      = Modifier.fillMaxWidth(),
                        minLines      = 2,
                        maxLines      = 3
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    mostrarReagendar = false
                    vm.reagendarCita(
                        citaAccion!!.id,
                        motivoReagendar.ifBlank { null }
                    )
                    motivoReagendar = ""
                }) { Text("Enviar solicitud") }
            },
            dismissButton = {
                OutlinedButton(onClick = {
                    mostrarReagendar = false
                    motivoReagendar  = ""
                }) { Text("Cancelar") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis citas") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Volver")
                    }
                },
                actions = {
                    // Botón para ver historial
                    IconButton(onClick = onVerHistorial) {
                        Icon(Icons.Default.History, "Historial")
                    }
                    // Botón para recargar
                    IconButton(onClick = { vm.cargar() }) {
                        Icon(Icons.Default.Refresh, "Recargar")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHost) }
    ) { padding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (val state = uiState) {

                is MisCitasUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                is MisCitasUiState.Empty -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.secondary
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            text = "No tienes citas activas",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "Reserva tu primera cita desde el menú principal.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                is MisCitasUiState.Error -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text  = state.mensaje,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = { vm.cargar() }) {
                            Text("Reintentar")
                        }
                    }
                }

                is MisCitasUiState.Success -> {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(state.citas) { cita ->
                            CitaCard(
                                cita = cita,
                                onCancelar = {
                                    citaAccion      = cita
                                    mostrarCancelar = true
                                },
                                onReagendar = {
                                    citaAccion       = cita
                                    mostrarReagendar = true
                                },
                                accionCargando = accionState is AccionUiState.Loading
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CitaCard(
    cita: Cita,
    onCancelar: () -> Unit,
    onReagendar: () -> Unit,
    accionCargando: Boolean
) {
    // Convertir el color hex del estado a Color de Compose
    val colorEstado = remember(cita.colorEstado) {
        runCatching {
            Color(android.graphics.Color.parseColor(cita.colorEstado ?: "#888888"))
        }.getOrDefault(Color.Gray)
    }

    Card(
        modifier  = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // ── Encabezado: estado + fecha ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Badge de estado
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = colorEstado.copy(alpha = 0.15f)
                ) {
                    Text(
                        text     = cita.estado,
                        color    = colorEstado,
                        style    = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                Text(
                    text  = formatearFecha(cita.fechaHoraInicio),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.height(12.dp))

            // ── Profesional ──
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text  = "${cita.profesional} · ${cita.cargoProfesional ?: ""}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(4.dp))

            // ── Hora y sede ──
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.secondary
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text  = "${formatearHora(cita.fechaHoraInicio)} - " +
                            "${formatearHora(cita.fechaHoraFin)} · ${cita.sede}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.height(4.dp))

            // ── Total ──
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.AttachMoney,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.secondary
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text  = "Total: ${"$%,.0f".format(cita.total)}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // ── Notas ──
            cita.notas?.let {
                Spacer(Modifier.height(4.dp))
                Text(
                    text  = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // ── Botones de acción ──
            // Solo disponibles si la cita está en estado Programada (1) o Confirmada (2)
            if (cita.idEstado in listOf(1, 2)) {
                Spacer(Modifier.height(12.dp))
                HorizontalDivider()
                Spacer(Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Botón reagendar
                    OutlinedButton(
                        onClick  = onReagendar,
                        enabled  = !accionCargando,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            Icons.Default.EditCalendar,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text("Reagendar")
                    }

                    // Botón cancelar
                    OutlinedButton(
                        onClick  = onCancelar,
                        enabled  = !accionCargando,
                        modifier = Modifier.weight(1f),
                        colors   = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        if (accionCargando) {
                            CircularProgressIndicator(
                                modifier    = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                Icons.Default.Cancel,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text("Cancelar")
                        }
                    }
                }
            }

            // Mensaje informativo si está pendiente de cambio
            if (cita.idEstado == 4) {
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text  = "Solicitud de cambio en revisión",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }
    }
}

// ── Helpers de formato de fecha ───────────────────────────────────────────────

private fun formatearFecha(fechaIso: String): String {
    return try {
        // El API devuelve formato: "2026-04-10T09:00:00"
        val partes = fechaIso.substring(0, 10).split("-")
        "${partes[2]}/${partes[1]}/${partes[0]}"
    } catch (e: Exception) { fechaIso }
}

private fun formatearHora(fechaIso: String): String {
    return try {
        // Extraemos solo HH:mm
        fechaIso.substring(11, 16)
    } catch (e: Exception) { fechaIso }
}