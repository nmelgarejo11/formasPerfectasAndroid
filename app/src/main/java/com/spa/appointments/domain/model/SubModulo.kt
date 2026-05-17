package com.spa.appointments.domain.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SubModuloAdmin(
    @Json(name = "Id")           val id:           Int,
    @Json(name = "Nombre")       val nombre:       String,
    @Json(name = "Seleccionado") val seleccionado: Boolean = false
)