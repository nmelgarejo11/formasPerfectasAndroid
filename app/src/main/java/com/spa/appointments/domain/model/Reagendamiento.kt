package com.spa.appointments.domain.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CitaPendiente(
    @Json(name = "id")                   val id: Int,
    @Json(name = "fechaHoraInicio")      val fechaHoraInicio: String,
    @Json(name = "fechaHoraFin")         val fechaHoraFin: String,
    @Json(name = "motivoSolicitud")      val motivoSolicitud: String?,
    @Json(name = "fechaSolicitudCambio") val fechaSolicitudCambio: String?,
    @Json(name = "notas")                val notas: String?,
    @Json(name = "cliente")              val cliente: String,
    @Json(name = "telefonoCliente")      val telefonoCliente: String?,
    @Json(name = "profesional")          val profesional: String,
    @Json(name = "sede")                 val sede: String,
    @Json(name = "servicios")            val servicios: String?,
    @Json(name = "total")                val total: Double
)

@JsonClass(generateAdapter = true)
data class AprobarReagendamientoRequest(
    @Json(name = "nuevaFechaInicio") val nuevaFechaInicio: String,
    @Json(name = "nuevaFechaFin")    val nuevaFechaFin: String
)

@JsonClass(generateAdapter = true)
data class RechazarReagendamientoRequest(
    @Json(name = "motivoRechazo") val motivoRechazo: String
)