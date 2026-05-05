package com.spa.appointments.domain.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class WhatsAppCitaInfo(
    @Json(name = "telefono") val telefono: String = "",
    @Json(name = "mensaje")  val mensaje: String
)

@JsonClass(generateAdapter = true)
data class WhatsAppCitaResponse(
    @Json(name = "ok") val ok: Boolean,
    @Json(name = "mensaje") val mensaje: String? = null,
    @Json(name = "data") val data: WhatsAppCitaInfo? = null
)