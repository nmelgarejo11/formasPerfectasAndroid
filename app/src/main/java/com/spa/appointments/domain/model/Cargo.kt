package com.spa.appointments.domain.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Cargo(
    @Json(name = "Id")     val id:     Int,
    @Json(name = "Nombre") val nombre: String,
    @Json(name = "Estado") val estado: Boolean
)

@JsonClass(generateAdapter = true)
data class CrearCargoRequest(
    @Json(name = "Nombre") val nombre: String
)