package com.spa.appointments.domain.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ServicioPrecioRango(
    @Json(name = "id")               val id: Int,
    @Json(name = "idServicio")       val idServicio: Int,
    @Json(name = "cantidadPersonas") val cantidadPersonas: Int,
    @Json(name = "precio")           val precio: Double,
    @Json(name = "estado")           val estado: Boolean
)

@JsonClass(generateAdapter = true)
data class GuardarPrecioRangoRequest(
    @Json(name = "idServicio")       val idServicio: Int,
    @Json(name = "cantidadPersonas") val cantidadPersonas: Int,
    @Json(name = "precio")           val precio: Double
)