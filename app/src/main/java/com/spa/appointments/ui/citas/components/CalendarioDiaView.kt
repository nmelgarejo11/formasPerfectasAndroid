package com.spa.appointments.ui.citas.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.EventNote
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spa.appointments.domain.model.Cita
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun CalendarioDiaView(
    citas: List<Cita>,
    fechaSeleccionada: LocalDate,
    onFechaChange: (LocalDate) -> Unit,
    onCitaClick: (Cita) -> Unit,
    modifier: Modifier = Modifier
) {
    val localeEs = Locale("es", "ES")
    val formatterDisplay = DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM", localeEs)
    val esHoy = fechaSeleccionada == LocalDate.now()

    Column(modifier = modifier.fillMaxSize()) {

        // ── Cabecera de Navegación Diaria ─────────────────────────────────────
        Surface(
            tonalElevation = 2.dp,
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(
                        onClick = { onFechaChange(fechaSeleccionada.minusDays(1)) }
                    ) {
                        Icon(
                            Icons.Default.ChevronLeft,
                            contentDescription = "Día anterior"
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = fechaSeleccionada.format(formatterDisplay).replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (esHoy) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Text(
                                    text = "HOY",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                                )
                            }
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (!esHoy) {
                            TextButton(
                                onClick = { onFechaChange(LocalDate.now()) },
                                contentPadding = PaddingValues(horizontal = 8.dp)
                            ) {
                                Text("Hoy", fontWeight = FontWeight.Bold)
                            }
                        }

                        IconButton(
                            onClick = { onFechaChange(fechaSeleccionada.plusDays(1)) }
                        ) {
                            Icon(
                                Icons.Default.ChevronRight,
                                contentDescription = "Día siguiente"
                            )
                        }
                    }
                }

                HorizontalDivider(
                    thickness = 0.5.dp,
                    color = MaterialTheme.colorScheme.outlineVariant
                )
            }
        }

        // ── Filtrado de Citas para el Día Seleccionado ────────────────────────
        val citasDelDia = remember(citas, fechaSeleccionada) {
            citas.filter { cita ->
                val inicio = parseFechaHora(cita.fechaHoraInicio)
                inicio?.toLocalDate() == fechaSeleccionada
            }.sortedBy { parseFechaHora(it.fechaHoraInicio) }
        }

        val scrollState = rememberScrollState()
        val horaInicioConfig = 6 // 06:00 AM
        val horaFinConfig = 22  // 10:00 PM
        val altoPorHoraDp = 72.dp

        Box(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            // ── Grid de Horas (Líneas de fondo) ──────────────────────────────
            Column(modifier = Modifier.fillMaxWidth()) {
                for (hora in horaInicioConfig..horaFinConfig) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(altoPorHoraDp),
                        verticalAlignment = Alignment.Top
                    ) {
                        // Etiqueta de la hora
                        Text(
                            text = String.format(Locale.getDefault(), "%02d:00", hora),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier
                                .width(52.dp)
                                .padding(start = 8.dp, top = 2.dp)
                        )

                        // Línea divisora horizontal
                        HorizontalDivider(
                            modifier = Modifier.padding(top = 8.dp),
                            thickness = 0.5.dp,
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )
                    }
                }
            }

            // ── Bloques de Citas Renderizados ────────────────────────────────
            if (citasDelDia.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.EventNote,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                        Text(
                            text = "Sin citas programadas para este día",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            } else {
                // Layout superpuesto de citas
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 56.dp, end = 12.dp)
                ) {
                    citasDelDia.forEach { cita ->
                        val inicioTime = parseFechaHora(cita.fechaHoraInicio)
                        val finTime = parseFechaHora(cita.fechaHoraFin)

                        if (inicioTime != null && finTime != null) {
                            val minutosDesdeInicio = ((inicioTime.hour - horaInicioConfig) * 60 + inicioTime.minute).coerceAtLeast(0)
                            val duracionMinutos = ((finTime.hour - inicioTime.hour) * 60 + (finTime.minute - inicioTime.minute)).coerceAtLeast(15)

                            val topOffset = (minutosDesdeInicio * (72.0 / 60.0)).dp + 8.dp
                            val bloqueAlto = (duracionMinutos * (72.0 / 60.0)).dp

                            val colorBase = parseColorHex(cita.colorEstado) ?: MaterialTheme.colorScheme.primaryContainer

                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .offset(y = topOffset)
                                    .height(bloqueAlto)
                                    .padding(vertical = 1.dp)
                                    .clickable { onCitaClick(cita) },
                                shape = RoundedCornerShape(10.dp),
                                color = colorBase.copy(alpha = 0.88f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, colorBase)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                        .fillMaxSize(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Borde indicador de estado
                                    Box(
                                        modifier = Modifier
                                            .width(4.dp)
                                            .fillMaxHeight()
                                            .clip(CircleShape)
                                            .background(Color.White.copy(alpha = 0.8f))
                                    )

                                    Spacer(Modifier.width(8.dp))

                                    Column(
                                        verticalArrangement = Arrangement.Center,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(
                                            text = "${inicioTime.format(DateTimeFormatter.ofPattern("hh:mm a"))} - ${finTime.format(DateTimeFormatter.ofPattern("hh:mm a"))}",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )

                                        val cliente = cita.nombreCliente?.ifBlank { null } ?: "Cliente General"
                                        Text(
                                            text = cliente,
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color.White,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )

                                        if (!cita.servicios.isNullOrBlank()) {
                                            Text(
                                                text = cita.servicios,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color.White.copy(alpha = 0.9f),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ── Helpers Privados ──────────────────────────────────────────────────────────
private fun parseFechaHora(str: String?): LocalDateTime? {
    if (str.isNullOrBlank()) return null
    return runCatching {
        LocalDateTime.parse(str)
    }.getOrElse {
        runCatching {
            val date = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).parse(str)
            date?.toInstant()?.atZone(java.time.ZoneId.systemDefault())?.toLocalDateTime()
        }.getOrNull()
    }
}

private fun parseColorHex(hex: String?): Color? {
    if (hex.isNullOrBlank()) return null
    return runCatching {
        val cleanHex = hex.trimStart('#')
        val colorInt = android.graphics.Color.parseColor("#$cleanHex")
        Color(colorInt)
    }.getOrNull()
}
