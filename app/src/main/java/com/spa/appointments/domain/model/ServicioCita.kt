package com.spa.appointments.domain.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ServicioCita(
    @Json(name = "Id")       val id:       Int,
    @Json(name = "Nombre")   val nombre:   String,
    @Json(name = "Precio")   val precio:   Double,
    @Json(name = "Duracion") val duracion: Int
)