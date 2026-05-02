package com.spa.appointments.domain.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class EstadoCita(
    @Json(name = "id")     val id: Int,
    @Json(name = "nombre") val nombre: String,
    @Json(name = "color")  val color: String,
    @Json(name = "grupo")  val grupo: String
)