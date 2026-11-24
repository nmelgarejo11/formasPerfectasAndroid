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
import androidx.compose.material.icons.automirrored.filled.StickyNote2
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spa.appointments.domain.model.Cita
import com.spa.appointments.domain.model.Profesional
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
    citas:                         List<Cita>,
    fechaSeleccionada:             LocalDate,
    onFechaChange:                 (LocalDate) -> Unit,
    onCitaClick:                   (Cita) -> Unit,
    modifier:                      Modifier = Modifier,
    mensajeVacio:                  String   = "Sin citas programadas para este día",
    profesionales:                 List<Profesional> = emptyList(),
    idProfesionalSeleccionado:     Int? = null,
    nombreProfesionalSeleccionado: String? = null,
    onProfesionalChange:           ((id: Int?, nombre: String?) -> Unit)? = null
) {
    val localeEs         = Locale("es", "ES")
    val formatterDisplay = DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM", localeEs)
    val esHoy            = fechaSeleccionada == LocalDate.now()

    Column(modifier = modifier.fillMaxSize()) {

        // ── Cabecera de Navegación Diaria y Filtro de Profesional M3 ───────────
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

                // Selector de Profesional en Vista Día
                if (onProfesionalChange != null || profesionales.isNotEmpty()) {
                    var expandedMenu by remember { mutableStateOf(false) }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 12.dp, end = 12.dp, bottom = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Box {
                            Surface(
                                onClick = { expandedMenu = true },
                                shape = RoundedCornerShape(12.dp),
                                color = if (idProfesionalSeleccionado != null)
                                    MaterialTheme.colorScheme.primaryContainer
                                else
                                    MaterialTheme.colorScheme.surfaceContainerHigh,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (idProfesionalSeleccionado != null)
                                        MaterialTheme.colorScheme.primary
                                    else
                                        MaterialTheme.colorScheme.outlineVariant
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = "Filtrar por profesional",
                                        tint = if (idProfesionalSeleccionado != null)
                                            MaterialTheme.colorScheme.onPrimaryContainer
                                        else
                                            MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = nombreProfesionalSeleccionado ?: "Todos los profesionales",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (idProfesionalSeleccionado != null)
                                            MaterialTheme.colorScheme.onPrimaryContainer
                                        else
                                            MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Icon(
                                        imageVector = Icons.Default.ArrowDropDown,
                                        contentDescription = null,
                                        tint = if (idProfesionalSeleccionado != null)
                                            MaterialTheme.colorScheme.onPrimaryContainer
                                        else
                                            MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            DropdownMenu(
                                expanded = expandedMenu,
                                onDismissRequest = { expandedMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Group,
                                                contentDescription = null,
                                                modifier = Modifier.size(18.dp),
                                                tint = if (idProfesionalSeleccionado == null)
                                                    MaterialTheme.colorScheme.primary
                                                else
                                                    MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Text(
                                                text = "Todos los profesionales",
                                                fontWeight = if (idProfesionalSeleccionado == null) FontWeight.Bold else FontWeight.Normal,
                                                color = if (idProfesionalSeleccionado == null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    },
                                    onClick = {
                                        expandedMenu = false
                                        onProfesionalChange?.invoke(null, null)
                                    }
                                )

                                if (profesionales.isNotEmpty()) {
                                    HorizontalDivider()
                                }

                                profesionales.forEach { prof ->
                                    val esSeleccionado = idProfesionalSeleccionado == prof.id
                                    DropdownMenuItem(
                                        text = {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Person,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(18.dp),
                                                    tint = if (esSeleccionado)
                                                        MaterialTheme.colorScheme.primary
                                                    else
                                                        MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                                Column {
                                                    Text(
                                                        text = prof.nombre,
                                                        fontWeight = if (esSeleccionado) FontWeight.Bold else FontWeight.Medium,
                                                        color = if (esSeleccionado) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                                    )
                                                    prof.cargo?.let { cargo ->
                                                        if (cargo.isNotBlank()) {
                                                            Text(
                                                                text = cargo,
                                                                style = MaterialTheme.typography.labelSmall,
                                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        },
                                        onClick = {
                                            expandedMenu = false
                                            onProfesionalChange?.invoke(prof.id, prof.nombre)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                HorizontalDivider(
                    thickness = 1.dp,
                    color     = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )
            }
        }

        // ── Filtrado y Posicionamiento de Citas para el Día Seleccionado ───────
        val citasPosicionadas = remember(citas, fechaSeleccionada, idProfesionalSeleccionado) {
            val citasDelDia = citas.filter { cita ->
                val inicio = parseFechaHora(cita.fechaHoraInicio)
                val coincideFecha = inicio?.toLocalDate() == fechaSeleccionada
                val coincideProf = idProfesionalSeleccionado == null || cita.idProfesional == idProfesionalSeleccionado
                coincideFecha && coincideProf
            }
            calcularPosicionesCitas(citasDelDia)
        }

        val scrollState      = rememberScrollState()
        val horaInicioConfig = 6 // 06:00 AM
        val horaFinConfig    = 22  // 10:00 PM
        val altoPorHoraDp    = 72.dp

        val density = LocalDensity.current
        LaunchedEffect(fechaSeleccionada) {
            if (esHoy) {
                val ahora = LocalTime.now()
                val horaClamped = ahora.hour.coerceIn(horaInicioConfig, horaFinConfig)
                val minutosTranscurridos = (horaClamped - horaInicioConfig) * 60 + ahora.minute
                val minutosOffset = (minutosTranscurridos - 30).coerceAtLeast(0)
                val targetPx = with(density) { (minutosOffset * (72.0 / 60.0)).dp.toPx() }.toInt()
                scrollState.scrollTo(targetPx)
            } else {
                scrollState.scrollTo(0)
            }
        }

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
            if (citasPosicionadas.isEmpty()) {
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
                // Layout paralelo para citas con posible solapamiento de horario
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 68.dp, end = 12.dp)
                ) {
                    val anchoDisponible = this.maxWidth

                    citasPosicionadas.forEach { pos ->
                        val cita       = pos.cita
                        val inicioTime = pos.inicioTime
                        val finTime    = pos.finTime

                        val minutosDesdeInicio = ((inicioTime.hour - horaInicioConfig) * 60 + inicioTime.minute).coerceAtLeast(0)
                        val duracionMinutos    = ((finTime.hour - inicioTime.hour) * 60 + (finTime.minute - inicioTime.minute)).coerceAtLeast(15)

                        val pxPorMinuto = 72.0 / 60.0
                        val topOffset   = (minutosDesdeInicio * pxPorMinuto).dp + 8.dp
                        // Altura proporcional exacta a la duración de la cita (minimo 24.dp para 15 min)
                        val bloqueAlto  = (duracionMinutos * pxPorMinuto).dp.coerceAtLeast(24.dp)

                        val colWidth   = anchoDisponible / pos.totalColumns
                        val leftOffset = colWidth * pos.columnIndex

                        val colorBase   = parseColorHex(cita.colorEstado) ?: MaterialTheme.colorScheme.primary
                        val colorFranja = parseColorHex(cita.colorProfesional) ?: colorBase
                        val tieneNotas  = !cita.notas.isNullOrBlank()

                        val isNarrow       = pos.totalColumns > 1
                        val fontSizeHora   = if (isNarrow) 8.5.sp else 9.5.sp
                        val fontSizeNombre = if (isNarrow) 9.5.sp else 10.5.sp
                        val fontSizeSub    = if (isNarrow) 8.5.sp else 9.sp
                        val padHorizontal  = if (isNarrow) 4.dp else 6.dp

                        key(cita.id) {
                            Surface(
                                modifier = Modifier
                                    .width(colWidth)
                                    .offset(x = leftOffset, y = topOffset)
                                    .height(bloqueAlto)
                                    .padding(horizontal = 1.dp, vertical = 1.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { onCitaClick(cita) },
                                shape  = RoundedCornerShape(10.dp),
                                color  = colorBase.copy(alpha = 0.14f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, colorBase.copy(alpha = 0.45f))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .padding(horizontal = padHorizontal, vertical = 2.dp)
                                        .fillMaxSize(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Borde indicador del profesional en la franja de la tarjeta
                                    Box(
                                        modifier = Modifier
                                            .width(3.5.dp)
                                            .fillMaxHeight()
                                            .clip(CircleShape)
                                            .background(colorFranja)
                                    )

                                    Spacer(Modifier.width(4.dp))

                                    val cliente = cita.nombreCliente?.ifBlank { null } ?: "Cliente General"
                                    val horaFormateada = if (isNarrow) {
                                        "${inicioTime.format(DateTimeFormatter.ofPattern("hh:mm"))}–${finTime.format(DateTimeFormatter.ofPattern("hh:mm a"))}"
                                    } else {
                                        "${inicioTime.format(DateTimeFormatter.ofPattern("hh:mm a"))} – ${finTime.format(DateTimeFormatter.ofPattern("hh:mm a"))}"
                                    }

                                    if (bloqueAlto < 48.dp) {
                                        // ── Vista Compacta para Citas Cortas (15-30 mins) ──────────────
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "$horaFormateada · $cliente",
                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = fontSizeHora),
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                modifier = Modifier.weight(1f)
                                            )
                                            if (tieneNotas) {
                                                Icon(
                                                    imageVector = Icons.AutoMirrored.Filled.StickyNote2,
                                                    contentDescription = "Tiene observación",
                                                    modifier = Modifier.size(10.dp),
                                                    tint = MaterialTheme.colorScheme.tertiary
                                                )
                                            }
                                        }
                                    } else {
                                        // ── Vista Normal/Larga para Citas de 45+ mins ─────────────────
                                        Column(
                                            verticalArrangement = Arrangement.spacedBy(1.dp, Alignment.CenterVertically),
                                            modifier            = Modifier
                                                .weight(1f)
                                                .padding(vertical = 1.dp)
                                        ) {
                                            Row(
                                                modifier              = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment     = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text       = horaFormateada,
                                                    style      = MaterialTheme.typography.labelSmall.copy(fontSize = fontSizeHora),
                                                    fontWeight = FontWeight.Bold,
                                                    color      = colorBase,
                                                    maxLines   = 1,
                                                    overflow   = TextOverflow.Ellipsis
                                                )
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                                                ) {
                                                    if (tieneNotas) {
                                                        Surface(
                                                            shape = RoundedCornerShape(4.dp),
                                                            color = MaterialTheme.colorScheme.tertiaryContainer
                                                        ) {
                                                            Row(
                                                                modifier = Modifier.padding(horizontal = 3.dp, vertical = 1.dp),
                                                                verticalAlignment = Alignment.CenterVertically
                                                            ) {
                                                                Icon(
                                                                    imageVector = Icons.AutoMirrored.Filled.StickyNote2,
                                                                    contentDescription = "Tiene observación",
                                                                    modifier = Modifier.size(10.dp),
                                                                    tint = MaterialTheme.colorScheme.onTertiaryContainer
                                                                )
                                                            }
                                                        }
                                                    }
                                                    if (pos.totalColumns == 1) {
                                                        Surface(
                                                            shape = RoundedCornerShape(4.dp),
                                                            color = colorBase.copy(alpha = 0.2f)
                                                        ) {
                                                            Text(
                                                                text       = cita.estado,
                                                                style      = MaterialTheme.typography.labelSmall.copy(fontSize = 8.5.sp),
                                                                fontWeight = FontWeight.Bold,
                                                                color      = colorBase,
                                                                modifier   = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                            )
                                                        }
                                                    }
                                                }
                                            }

                                            Text(
                                                text       = cliente,
                                                style      = MaterialTheme.typography.bodySmall.copy(fontSize = fontSizeNombre),
                                                fontWeight = FontWeight.Bold,
                                                color      = MaterialTheme.colorScheme.onSurface,
                                                maxLines   = 1,
                                                overflow   = TextOverflow.Ellipsis
                                            )

                                            val textoServicio = remember(cita.id, cita.servicios) { obtenerTextoServicios(cita.servicios) }
                                            if (textoServicio.isNotBlank() && bloqueAlto >= 60.dp) {
                                                Text(
                                                    text     = textoServicio,
                                                    style    = MaterialTheme.typography.labelSmall.copy(fontSize = fontSizeSub),
                                                    color    = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }

                                            if (cita.profesional.isNotBlank() && bloqueAlto >= 76.dp) {
                                                Text(
                                                    text     = cita.profesional,
                                                    style    = MaterialTheme.typography.labelSmall.copy(fontSize = fontSizeSub),
                                                    fontWeight = FontWeight.SemiBold,
                                                    color    = MaterialTheme.colorScheme.onSurfaceVariant,
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

private data class CitaPosicionada(
    val cita: Cita,
    val inicioTime: LocalDateTime,
    val finTime: LocalDateTime,
    var columnIndex: Int = 0,
    var totalColumns: Int = 1
)

private fun calcularPosicionesCitas(citas: List<Cita>): List<CitaPosicionada> {
    val parsed = citas.mapNotNull { cita ->
        val inicio = parseFechaHora(cita.fechaHoraInicio)
        val fin = parseFechaHora(cita.fechaHoraFin)
        if (inicio != null && fin != null) {
            CitaPosicionada(cita = cita, inicioTime = inicio, finTime = fin)
        } else null
    }.sortedWith(compareBy({ it.inicioTime }, { it.finTime }))

    if (parsed.isEmpty()) return emptyList()

    val grupos = mutableListOf<MutableList<CitaPosicionada>>()
    var grupoActual = mutableListOf<CitaPosicionada>()
    var maxFinEnGrupo: LocalDateTime? = null

    for (item in parsed) {
        if (grupoActual.isEmpty()) {
            grupoActual.add(item)
            maxFinEnGrupo = item.finTime
        } else {
            if (item.inicioTime.isBefore(maxFinEnGrupo!!)) {
                grupoActual.add(item)
                if (item.finTime.isAfter(maxFinEnGrupo)) {
                    maxFinEnGrupo = item.finTime
                }
            } else {
                grupos.add(grupoActual)
                grupoActual = mutableListOf(item)
                maxFinEnGrupo = item.finTime
            }
        }
    }
    if (grupoActual.isNotEmpty()) {
        grupos.add(grupoActual)
    }

    for (grupo in grupos) {
        val columnasTermino = mutableListOf<LocalDateTime>()

        for (item in grupo) {
            var asignado = false
            for (colIdx in columnasTermino.indices) {
                if (!columnasTermino[colIdx].isAfter(item.inicioTime)) {
                    item.columnIndex = colIdx
                    columnasTermino[colIdx] = item.finTime
                    asignado = true
                    break
                }
            }
            if (!asignado) {
                item.columnIndex = columnasTermino.size
                columnasTermino.add(item.finTime)
            }
        }

        val maxCols = columnasTermino.size
        for (item in grupo) {
            item.totalColumns = maxCols
        }
    }

    return parsed
}
