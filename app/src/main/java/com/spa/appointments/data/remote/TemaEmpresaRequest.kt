package com.spa.appointments.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class TemaEmpresaRequest (
    @Json(name = "colorPrimario")   val colorPrimario: String,
    @Json(name = "colorSecundario") val colorSecundario: String,
    @Json(name = "colorTerciario")  val colorTerciario: String,
    @Json(name = "nombreApp")       val nombreApp: String,
    @Json(name = "slogan")          val slogan: String?,
    @Json(name = "mensajeWhatsApp") val mensajeWhatsApp: String?
)