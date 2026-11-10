package com.spa.appointments.domain.model

import java.time.LocalDate

enum class TipoMovimientoFiltro(val titulo: String) {
    TODOS("Todos"),
    INGRESO("Ingresos"),
    GASTO("Gastos")
}

data class MovimientoBalance(
    val id                  : String,
    val tipo                : TipoMovimientoFiltro,
    val concepto            : String,
    val valor               : Double,
    val fecha               : String,
    val idMetodoPago        : Int? = null,
    val metodoPago          : String? = null,
    val idMetodoPagoDetalle : Int? = null,
    val metodoPagoDetalle   : String? = null,
    val idSede              : Int? = null,
    val sede                : String? = null,
    val categoria           : String? = null,
    val cliente             : String? = null,
    val idProfesional       : Int? = null,
    val profesional         : String? = null
)

data class FiltrosBalance(
    val fechaInicio       : LocalDate = LocalDate.now().withDayOfMonth(1),
    val fechaFin          : LocalDate = LocalDate.now(),
    val tipoMovimiento    : TipoMovimientoFiltro = TipoMovimientoFiltro.TODOS,
    val idMetodoPago      : Int? = null,
    val nombreMetodoPago  : String? = null,
    val conceptoCategoria : String = "",
    val cliente           : String = "",
    val idProfesional     : Int? = null,
    val nombreProfesional : String? = null,
    val esFiltroManual    : Boolean = false
) {
    val activoCount: Int get() {
        var count = 0
        if (tipoMovimiento != TipoMovimientoFiltro.TODOS) count++
        if (idMetodoPago != null || !nombreMetodoPago.isNullOrBlank()) count++
        if (conceptoCategoria.isNotBlank()) count++
        if (cliente.isNotBlank()) count++
        if (idProfesional != null || !nombreProfesional.isNullOrBlank()) count++
        return count
    }
    val esFiltroActivo: Boolean get() = activoCount > 0
}

data class IngresosVsGastosResumen(
    val fechaInicio   : String,
    val fechaFin      : String,
    val totalIngresos : Double,
    val totalGastos   : Double,
    val totalCitas    : Int,
    val utilidadNeta  : Double
)

data class IngresosVsGastosDia(
    val fecha         : String,
    val totalIngresos : Double,
    val totalCitas    : Int,
    val totalGastos   : Double,
    val utilidadNeta  : Double
)

data class IngresosVsGastosResponse(
    val resumen : IngresosVsGastosResumen,
    val detalle : List<IngresosVsGastosDia>
)

data class IngresosVsGastosDatos(
    val resumen              : IngresosVsGastosResumen,
    val detalle              : List<IngresosVsGastosDia>,
    val todosMovimientos     : List<MovimientoBalance> = emptyList(),
    val movimientosFiltrados : List<MovimientoBalance> = emptyList(),
    val filtros              : FiltrosBalance = FiltrosBalance()
)
