package com.spa.appointments.ui.citas.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
import com.spa.appointments.domain.model.ServicioCita
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun CalendarioDiaView(
    citas:             List<Cita>,
    fechaSeleccionada: LocalDate,
    onFechaChange:     (LocalDate) -> Unit,
    onCitaClick:       (Cita) -> Unit,
    modifier:          Modifier = Modifier,
    mensajeVacio:      String   = "Sin citas programadas para este día"
) {
    val localeEs         = Locale("es", "ES")
    val formatterDisplay = DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM", localeEs)
    val esHoy            = fechaSeleccionada == LocalDate.now()

    Column(modifier = modifier.fillMaxSize()) {

        // ── Cabecera de Navegación Diaria M3 ─────────────────────────────────
        Surface(
            tonalElevation = 1.dp,
            color          = MaterialTheme.colorScheme.surfaceContainerLow,
            modifier       = Modifier.fillMaxWidth()
        ) {
            Column {
                Row(
                    modifier              = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(
                        onClick = { onFechaChange(fechaSeleccionada.minusDays(1)) },
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                    ) {
                        Icon(
                            Icons.Default.ChevronLeft,
                            contentDescription = "Día anterior",
                            tint               = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text       = fechaSeleccionada.format(formatterDisplay).replaceFirstChar { it.uppercase() },
                            style      = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color      = MaterialTheme.colorScheme.onSurface
                        )
                        if (esHoy) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Text(
                                    text       = "HOY",
                                    style      = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color      = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier   = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (!esHoy) {
                            TextButton(
                                onClick        = { onFechaChange(LocalDate.now()) },
                                contentPadding = PaddingValues(horizontal = 10.dp)
                            ) {
                                Text(
                                    "Ir a hoy",
                                    fontWeight = FontWeight.Bold,
                                    style      = MaterialTheme.typography.labelMedium
                                )
                            }
                        }

                        IconButton(
                            onClick = { onFechaChange(fechaSeleccionada.plusDays(1)) },
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                        ) {
                            Icon(
                                Icons.Default.ChevronRight,
                                contentDescription = "Día siguiente",
                                tint               = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                HorizontalDivider(
                    thickness = 1.dp,
                    color     = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
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

        val scrollState      = rememberScrollState()
        val horaInicioConfig = 6 // 06:00 AM
        val horaFinConfig    = 22  // 10:00 PM
        val altoPorHoraDp    = 72.dp

        Box(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            // ── Grid de Horas (Líneas de fondo M3) ───────────────────────────
            Column(modifier = Modifier.fillMaxWidth()) {
                for (hora in horaInicioConfig..horaFinConfig) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(altoPorHoraDp),
                        verticalAlignment = Alignment.Top
                    ) {
                        val amPm = if (hora >= 12) "PM" else "AM"
                        val h12  = if (hora > 12) hora - 12 else hora
                        Text(
                            text       = String.format(Locale.US, "%02d:00 %s", h12, amPm),
                            style      = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                            color      = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.SemiBold,
                            modifier   = Modifier
                                .width(64.dp)
                                .padding(start = 6.dp, top = 2.dp)
                        )

                        HorizontalDivider(
                            modifier  = Modifier.padding(top = 8.dp),
                            thickness = 1.dp,
                            color     = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                        )
                    }
                }
            }

            // 🎨 Canvas: Indicador de Hora Actual si es el día de HOY
            if (esHoy) {
                val ahora = LocalTime.now()
                if (ahora.hour in horaInicioConfig..horaFinConfig) {
                    val minutosTranscurridos = (ahora.hour - horaInicioConfig) * 60 + ahora.minute
                    val currentLineYDp      = (minutosTranscurridos * (72.0 / 60.0)).dp + 8.dp
                    val primaryColor         = MaterialTheme.colorScheme.primary

                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .offset(y = currentLineYDp)
                            .height(10.dp)
                    ) {
                        val widthPx = size.width
                        val dotXPx  = 64.dp.toPx()

                        // Punto indicador
                        drawCircle(
                            color  = primaryColor,
                            radius = 5.dp.toPx(),
                            center = androidx.compose.ui.geometry.Offset(dotXPx, 5.dp.toPx())
                        )
                        // Línea indicadora
                        drawLine(
                            color       = primaryColor,
                            start       = androidx.compose.ui.geometry.Offset(dotXPx, 5.dp.toPx()),
                            end         = androidx.compose.ui.geometry.Offset(widthPx, 5.dp.toPx()),
                            strokeWidth = 2.dp.toPx()
                        )
                    }
                }
            }

            // ── Bloques de Citas Renderizados ────────────────────────────────
            if (citasDelDia.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(320.dp)
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surfaceContainerHigh
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.EventNote,
                                contentDescription = null,
                                modifier           = Modifier.padding(16.dp).size(40.dp),
                                tint               = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text       = mensajeVacio,
                            style      = MaterialTheme.typography.bodyMedium,
                            color      = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            } else {
                // Layout superpuesto de citas en la línea de tiempo
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 68.dp, end = 12.dp)
                ) {
                    citasDelDia.forEach { cita ->
                        val inicioTime = parseFechaHora(cita.fechaHoraInicio)
                        val finTime    = parseFechaHora(cita.fechaHoraFin)

                        if (inicioTime != null && finTime != null) {
                            val minutosDesdeInicio = ((inicioTime.hour - horaInicioConfig) * 60 + inicioTime.minute).coerceAtLeast(0)
                            val duracionMinutos    = ((finTime.hour - inicioTime.hour) * 60 + (finTime.minute - inicioTime.minute)).coerceAtLeast(15)

                            val topOffset  = (minutosDesdeInicio * (72.0 / 60.0)).dp + 8.dp
                            val bloqueAlto = (duracionMinutos * (72.0 / 60.0)).dp.coerceAtLeast(76.dp)

                            val colorBase = parseColorHex(cita.colorEstado) ?: MaterialTheme.colorScheme.primaryContainer

                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .offset(y = topOffset)
                                    .height(bloqueAlto)
                                    .padding(vertical = 2.dp)
                                    .clickable { onCitaClick(cita) },
                                shape = RoundedCornerShape(12.dp),
                                color = colorBase.copy(alpha = 0.94f),
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
                                            .background(Color.White)
                                    )

                                    Spacer(Modifier.width(8.dp))

                                    Column(
                                        verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterVertically),
                                        modifier            = Modifier
                                            .weight(1f)
                                            .padding(vertical = 2.dp)
                                    ) {
                                        Row(
                                            modifier              = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment     = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text       = "${inicioTime.format(DateTimeFormatter.ofPattern("hh:mm a"))} – ${finTime.format(DateTimeFormatter.ofPattern("hh:mm a"))}",
                                                style      = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color      = Color.White
                                            )
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = Color.White.copy(alpha = 0.25f)
                                            ) {
                                                Text(
                                                    text       = cita.estado,
                                                    style      = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                                    fontWeight = FontWeight.Bold,
                                                    color      = Color.White,
                                                    modifier   = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                                )
                                            }
                                        }

                                        val cliente = cita.nombreCliente?.ifBlank { null } ?: "Cliente General"
                                        Text(
                                            text       = cliente,
                                            style      = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Bold,
                                            color      = Color.White,
                                            maxLines   = 1,
                                            overflow   = TextOverflow.Ellipsis
                                        )

                                        val textoServicio = remember(cita.servicios) { obtenerTextoServicios(cita.servicios) }
                                        if (textoServicio.isNotBlank()) {
                                            Text(
                                                text     = textoServicio,
                                                style    = MaterialTheme.typography.labelSmall,
                                                color    = Color.White.copy(alpha = 0.95f),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }

                                        if (cita.profesional.isNotBlank()) {
                                            Text(
                                                text     = "Prof: ${cita.profesional}",
                                                style    = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                                color    = Color.White.copy(alpha = 0.85f),
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
private fun obtenerTextoServicios(json: String?): String {
    if (json.isNullOrBlank()) return ""
    return try {
        val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
        val type  = Types.newParameterizedType(List::class.java, ServicioCita::class.java)
        val lista = moshi.adapter<List<ServicioCita>>(type).fromJson(json) ?: emptyList()
        if (lista.isNotEmpty()) {
            lista.joinToString(", ") { it.nombre }
        } else if (!json.trim().startsWith("[") && !json.trim().startsWith("{")) {
            json
        } else {
            ""
        }
    } catch (e: Exception) {
        if (!json.trim().startsWith("[") && !json.trim().startsWith("{")) json else ""
    }
}

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
