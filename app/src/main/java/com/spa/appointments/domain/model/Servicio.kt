package com.spa.appointments.domain.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Servicio(
    @Json(name = "id")              val id: Int,
    @Json(name = "nombre")          val nombre: String,
    @Json(name = "descripcion")     val descripcion: String?,
    @Json(name = "duracionMinutos") val duracionMinutos: Int,
    @Json(name = "precioBase")      val precioBase: Double,
    @Json(name = "icono")           val icono: String?,
    @Json(name = "idCategoria")     val idCategoria: Int,
    @Json(name = "categoria")       val categoria: String
)