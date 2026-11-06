package com.spa.appointments.domain.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class FinalizarCitaRequest(
    @Json(name = "idCita")               val idCita: Int,
    @Json(name = "idMetodoPago")         val idMetodoPago: Int,
    @Json(name = "idMetodoPagoDetalle")  val idMetodoPagoDetalle: Int? = null,
    @Json(name = "precioFinal")          val precioFinal: Double? = null,
    @Json(name = "observaciones")        val observaciones: String? = null
)

@JsonClass(generateAdapter = true)
data class FinalizarCitaResponse(
    @Json(name = "ok")                   val ok: Boolean,
    @Json(name = "mensaje")              val mensaje: String
)