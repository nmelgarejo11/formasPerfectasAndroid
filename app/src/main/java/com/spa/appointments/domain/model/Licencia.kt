package com.spa.appointments.domain.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class LicenciaResponse(
    @Json(name = "estado")          val estado: String,
    @Json(name = "diasRestantes")   val diasRestantes: Int,
    @Json(name = "fechaExpiracion") val fechaExpiracion: String?,
    @Json(name = "esDemo")          val esDemo: Boolean,
    @Json(name = "mensaje")         val mensaje: String
)

// Estados posibles que retorna la API
object EstadoLicencia {
    const val ACTIVA      = "ACTIVA"
    const val DEMO        = "DEMO"
    const val EXPIRA_HOY  = "EXPIRA_HOY"
    const val EXPIRADO    = "EXPIRADO"
    const val INACTIVA    = "INACTIVA"
}