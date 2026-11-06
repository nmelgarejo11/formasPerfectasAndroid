package com.spa.appointments.domain.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ReagendarCitaDirectoRequest(
    @Json(name = "idProfesional")   val idProfesional: Int,
    @Json(name = "fechaHoraInicio") val fechaHoraInicio: String,
    @Json(name = "fechaHoraFin")    val fechaHoraFin: String
)