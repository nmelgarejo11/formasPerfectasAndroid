package com.spa.appointments.domain.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ContactoSoporte(
    @Json(name = "id")       val id: Int,
    @Json(name = "nombre")   val nombre: String,
    @Json(name = "cargo")    val cargo: String?,
    @Json(name = "celular")  val celular: String?,
    @Json(name = "email")    val email: String?,
    @Json(name = "whatsapp") val whatsapp: String?,
    @Json(name = "mensaje")  val mensaje: String?
)