package com.spa.appointments.domain.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CambiarEstadoCitaRequest(
    @Json(name = "idEstado")            val idEstado: Int,
    @Json(name = "motivo")              val motivo: String? = null,
    @Json(name = "idMetodoPago")        val idMetodoPago: Int? = null,
    @Json(name = "idMetodoPagoDetalle") val idMetodoPagoDetalle: Int? = null
)
