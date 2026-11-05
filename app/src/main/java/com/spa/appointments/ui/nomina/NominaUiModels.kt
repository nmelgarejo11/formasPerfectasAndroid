package com.spa.appointments.ui.nomina

import com.spa.appointments.domain.model.ContratoElegibleNomina
import java.text.NumberFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

sealed class EstadoNominaContrato {
    object SinPreliquidar : EstadoNominaContrato()
    object PreliquidadaLista : EstadoNominaContrato()
    data class PreliquidacionDesactualizada(val motivo: String?) : EstadoNominaContrato()
    object Liquidada : EstadoNominaContrato()

    companion object {
        fun desde(contrato: ContratoElegibleNomina): EstadoNominaContrato {
            return when {
                contrato.idLiquidacionVigente != null -> Liquidada
                contrato.idPreliquidacionVigente == null -> SinPreliquidar
                contrato.motivoPreliquidacion == null -> PreliquidadaLista
                else -> PreliquidacionDesactualizada(contrato.motivoPreliquidacion)
            }
        }
    }
}

fun textoMotivoNomina(motivo: String?): String {
    return when (motivo) {
        "SALARIO_CAMBIO" -> "Cambió el salario del contrato"
        "NOVEDAD_NUEVA" -> "Hay novedades nuevas en el periodo"
        "NOVEDAD_MODIFICADA" -> "Se modificaron o inactivaron novedades"
        "PRESTAMO_CAMBIO" -> "Cambiaron los préstamos o sus saldos"
        "NO_VIGENTE" -> "Existe una preliquidación más reciente"
        "NO_EXITOSA" -> "La preliquidación no fue exitosa"
        "PERIODO_NO_ABIERTO" -> "El periodo no está abierto"
        "CONTRATO_INACTIVO" -> "El contrato no está activo"
        "YA_LIQUIDADA" -> "Ya está liquidada"
        else -> "Los datos de origen cambiaron"
    }
}

fun formatearCop(valor: Double?): String {
    if (valor == null) return "$ 0"
    val format = NumberFormat.getCurrencyInstance(Locale("es", "CO")).apply {
        maximumFractionDigits = 0
        minimumFractionDigits = 0
    }
    return format.format(valor)
}

fun formatearFechaDisplay(fechaStr: String?): String {
    if (fechaStr.isNullOrBlank()) return "-"
    return try {
        val clean = fechaStr.trim()
        val datePart = if (clean.contains("T")) clean.substringBefore("T") else clean
        val parsed = LocalDate.parse(datePart, DateTimeFormatter.ISO_LOCAL_DATE)
        parsed.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
    } catch (_: Exception) {
        fechaStr
    }
}

data class NovedadFormDialogState(
    val novedadEditar: com.spa.appointments.domain.model.NovedadNominaResponse? = null,
    val idConceptoSeleccionado: Int? = null,
    val fechaNovedad: String = "",
    val valor: String = "",
    val dias: String = "",
    val observaciones: String = ""
)

fun com.spa.appointments.domain.model.PeriodoNominaItem?.esAbierto(): Boolean {
    if (this == null) return false
    return estado == 1 || nombreEstado.equals("Abierto", ignoreCase = true)
}
