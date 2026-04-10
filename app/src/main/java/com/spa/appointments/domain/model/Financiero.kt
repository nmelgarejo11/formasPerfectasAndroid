package com.spa.appointments.domain.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ResumenFinanciero(
    @Json(name = "totalActual")         val totalActual: Double,
    @Json(name = "totalAnterior")       val totalAnterior: Double,
    @Json(name = "totalCitas")          val totalCitas: Int,
    @Json(name = "variacionPorcentual") val variacionPorcentual: Double,
    @Json(name = "fechaDesde")          val fechaDesde: String,
    @Json(name = "fechaHasta")          val fechaHasta: String
)

@JsonClass(generateAdapter = true)
data class IngresoDia(
    @Json(name = "fecha") val fecha: String,
    @Json(name = "total") val total: Double,
    @Json(name = "citas") val citas: Int
)

@JsonClass(generateAdapter = true)
data class IngresoMes(
    @Json(name = "mes")       val mes: Int,
    @Json(name = "nombreMes") val nombreMes: String,
    @Json(name = "total")     val total: Double,
    @Json(name = "citas")     val citas: Int
)

@JsonClass(generateAdapter = true)
data class ServicioVendido(
    @Json(name = "id")       val id: Int,
    @Json(name = "servicio") val servicio: String,
    @Json(name = "cantidad") val cantidad: Int,
    @Json(name = "total")    val total: Double
)

@JsonClass(generateAdapter = true)
data class ProfesionalRanking(
    @Json(name = "id")            val id: Int,
    @Json(name = "profesional")   val profesional: String,
    @Json(name = "cargo")         val cargo: String,
    @Json(name = "totalCitas")    val totalCitas: Int,
    @Json(name = "totalIngresos") val totalIngresos: Double
)