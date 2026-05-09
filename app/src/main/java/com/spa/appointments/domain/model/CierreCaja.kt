package com.spa.appointments.domain.model

import java.time.LocalDate

data class CierreCajaResumen(
    val fecha               : String,
    val totalIngresos       : Double,
    val totalCitas          : Int,
    val totalGastos         : Double,
    val totalRegistrosGasto : Int,
    val utilidadNeta        : Double
)

data class CierreCajaProfesional(
    val idProfesional     : Int,
    val nombreProfesional : String,
    val totalCitas        : Int,
    val totalGenerado     : Double
)

data class CierreCajaGasto(
    val id         : Int,
    val concepto   : String,
    val valor      : Double,
    val fechaGasto : String
)

data class CierreCajaResponse(
    val resumen       : CierreCajaResumen,
    val profesionales : List<CierreCajaProfesional>,
    val ultimosGastos : List<CierreCajaGasto>
)