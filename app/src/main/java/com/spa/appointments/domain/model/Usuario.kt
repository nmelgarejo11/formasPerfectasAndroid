package com.spa.appointments.domain.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CrearUsuarioRequest(
    @Json(name = "Usuario")  val usuario:  String,
    @Json(name = "Clave")    val clave:    String,
    @Json(name = "FotoUrl")  val fotoUrl:  String? = null,
    @Json(name = "IdPerfil") val idPerfil: Int
)

@JsonClass(generateAdapter = true)
data class Usuario(
    @Json(name = "Id")           val id:           Int,
    @Json(name = "Usuario")      val usuario:      String,
    @Json(name = "Estado")       val estado:       Boolean,
    @Json(name = "IdPerfil")     val idPerfil:     Int,
    @Json(name = "NombrePerfil") val nombrePerfil: String
)
