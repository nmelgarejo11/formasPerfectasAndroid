package com.spa.appointments.domain.model

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