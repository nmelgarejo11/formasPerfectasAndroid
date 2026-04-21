package com.spa.appointments.domain.model
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Perfil(
    @Json(name = "idUsuario") val idUsuario: Int,
    @Json(name = "nombreUsuario") val nombreUsuario: String,
    @Json(name = "fotoUrl") val fotoUrl: String?,
    @Json(name = "idCliente") val idCliente: Int,
    @Json(name = "nombre") val nombre: String,
    @Json(name = "apellido") val apellido: String,
    @Json(name = "telefono") val telefono: String?,
    @Json(name = "email") val email: String?
)

@JsonClass(generateAdapter = true)
data class ActualizarPerfilRequest(
    @Json(name = "nombre") val nombre: String,
    @Json(name = "apellido") val apellido: String,
    @Json(name = "telefono") val telefono: String?,
    @Json(name = "email") val email: String?
)

@JsonClass(generateAdapter = true)
data class FotoResponse(
    @Json(name = "fotoUrl") val fotoUrl: String
)