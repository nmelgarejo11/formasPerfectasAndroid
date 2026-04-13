package com.spa.appointments.domain.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class TemaEmpresa(
    @Json(name = "colorPrimario")   val colorPrimario: String,
    @Json(name = "colorSecundario") val colorSecundario: String,
    @Json(name = "colorTerciario")  val colorTerciario: String,
    @Json(name = "logoUrl")         val logoUrl: String?,
    @Json(name = "nombreApp")       val nombreApp: String,
    @Json(name = "slogan")          val slogan: String?,
    @Json(name = "nombreEmpresa")   val nombreEmpresa: String
)