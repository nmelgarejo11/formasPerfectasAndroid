package com.spa.appointments.domain.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Cliente(
    @Json(name = "id")       val id: Int,
    @Json(name = "nombre")   val nombre: String,
    @Json(name = "apellido") val apellido: String,
    @Json(name = "telefono") val telefono: String?,
    @Json(name = "email")    val email: String?,
    @Json(name = "estado") val estado: Boolean = true
) {
    val nombreCompleto get() = "$nombre $apellido"
}

@JsonClass(generateAdapter = true)
data class CrearClienteRequest(
    @Json(name = "nombre")   val nombre: String,
    @Json(name = "apellido") val apellido: String,
    @Json(name = "telefono") val telefono: String?,
    @Json(name = "email")    val email: String?
)

@JsonClass(generateAdapter = true)
data class ActualizarClienteRequest(
    @Json(name = "nombre") val nombre: String,
    @Json(name = "apellido") val apellido: String,
    @Json(name = "telefono") val telefono: String?,
    @Json(name = "email") val email: String?
)