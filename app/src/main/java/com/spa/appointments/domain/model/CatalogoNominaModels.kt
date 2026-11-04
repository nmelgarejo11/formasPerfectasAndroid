package com.spa.appointments.domain.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class BancoItem(
    @Json(name = "id") val id: Int,
    @Json(name = "nombre") val nombre: String,
    @Json(name = "estado") val estado: Boolean
)

@JsonClass(generateAdapter = true)
data class TipoDocumentoItem(
    @Json(name = "id") val id: Int,
    @Json(name = "codigo") val codigo: String?,
    @Json(name = "nombre") val nombre: String,
    @Json(name = "estado") val estado: Boolean
)

@JsonClass(generateAdapter = true)
data class TipoCuentaItem(
    @Json(name = "id") val id: Int,
    @Json(name = "nombre") val nombre: String,
    @Json(name = "estado") val estado: Boolean
)
