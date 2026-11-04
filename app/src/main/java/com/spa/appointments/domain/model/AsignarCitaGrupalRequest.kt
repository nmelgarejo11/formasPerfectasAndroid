package com.spa.appointments.domain.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AsignarCitaGrupalRequest(
    @Json(name = "fechaHoraInicio")  val fechaHoraInicio:  String?    = null,
    @Json(name = "fechaHoraFin")     val fechaHoraFin:     String?    = null,
    @Json(name = "idsProfesionales") val idsProfesionales: List<Int>? = null
)