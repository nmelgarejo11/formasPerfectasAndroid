package com.spa.appointments.domain.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class HorarioItem(
    @Json(name = "id")         val id: Int,
    @Json(name = "idSede")     val idSede: Int,
    @Json(name = "diaSemana")  val diaSemana: Int,
    @Json(name = "horaInicio") val horaInicio: String,
    @Json(name = "horaFin")    val horaFin: String,
    @Json(name = "estado")     val estado: Boolean
)

@JsonClass(generateAdapter = true)
data class HorarioItemRequest(
    @Json(name = "diaSemana")  val diaSemana: Int,
    @Json(name = "horaInicio") val horaInicio: String,
    @Json(name = "horaFin")    val horaFin: String,
    @Json(name = "estado")     val estado: Boolean
)

@JsonClass(generateAdapter = true)
data class GuardarHorarioRequest(
    @Json(name = "idSede")   val idSede: Int,
    @Json(name = "horarios") val horarios: List<HorarioItemRequest>
)

@JsonClass(generateAdapter = true)
data class CopiarHorarioRequest(
    @Json(name = "idProfesionalOrigen") val idProfesionalOrigen: Int,
    @Json(name = "idSede")              val idSede: Int
)

@JsonClass(generateAdapter = true)
data class BloqueoResponse(
    @Json(name = "id")           val id: Int,
    @Json(name = "fechaInicio")  val fechaInicio: String,
    @Json(name = "fechaFin")     val fechaFin: String,
    @Json(name = "motivo")       val motivo: String
)

@JsonClass(generateAdapter = true)
data class BloqueoRequest(
    @Json(name = "fechaInicio") val fechaInicio: String,
    @Json(name = "fechaFin")    val fechaFin: String,
    @Json(name = "motivo")      val motivo: String
)