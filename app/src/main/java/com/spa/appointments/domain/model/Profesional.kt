package com.spa.appointments.domain.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Profesional(
    @Json(name = "id")             val id: Int,
    @Json(name = "nombre")         val nombre: String,
    @Json(name = "apellido")       val apellido: String,
    @Json(name = "nombreCompleto") val nombreCompleto: String,
    @Json(name = "foto")           val foto: String?,
    @Json(name = "telefono")       val telefono: String?,
    @Json(name = "email")          val email: String?,
    @Json(name = "idCargo")        val idCargo: Int,
    @Json(name = "cargo")          val cargo: String,
    @Json(name = "idSede")         val idSede: Int,
    @Json(name = "sede")           val sede: String
)