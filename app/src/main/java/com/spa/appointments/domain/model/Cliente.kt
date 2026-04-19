package com.spa.appointments.domain.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Cliente(
    @Json(name = "id")       val id: Int,
    @Json(name = "nombre")   val nombre: String,
    @Json(name = "apellido") val apellido: String,
    @Json(name = "telefono") val telefono: String?,
    @Json(name = "email")    val email: String?
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