package com.spa.appointments.domain.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ProfesionalAdmin(
    @Json(name = "id")          val id: Int,
    @Json(name = "idCargo")     val idCargo: Int,
    @Json(name = "nombreCargo") val nombreCargo: String,
    @Json(name = "nombre")      val nombre: String,
    @Json(name = "apellido")    val apellido: String,
    @Json(name = "foto")        val foto: String?,
    @Json(name = "telefono")    val telefono: String?,
    @Json(name = "email")       val email: String?,
    @Json(name = "estado")      val estado: Boolean
)

@JsonClass(generateAdapter = true)
data class ProfesionalRequest(
    @Json(name = "idCargo")  val idCargo: Int,
    @Json(name = "nombre")   val nombre: String,
    @Json(name = "apellido") val apellido: String,
    @Json(name = "telefono") val telefono: String?,
    @Json(name = "email")    val email: String?
)

@JsonClass(generateAdapter = true)
data class CargoAdmin(
    @Json(name = "id")     val id: Int,
    @Json(name = "nombre") val nombre: String
)

@JsonClass(generateAdapter = true)
data class SedeAdmin(
    @Json(name = "id")        val id: Int,
    @Json(name = "nombre")    val nombre: String,
    @Json(name = "direccion") val direccion: String?
)

@JsonClass(generateAdapter = true)
data class ServicioProfesionalItem(
    @Json(name = "idServicio") val idServicio: Int,
    @Json(name = "precio")     val precio: Double
)

@JsonClass(generateAdapter = true)
data class ServicioProfesionalResponse(
    @Json(name = "idServicio") val idServicio: Int,
    @Json(name = "precio")     val precio: Double
)

@JsonClass(generateAdapter = true)
data class GuardarSedesRequest(
    @Json(name = "idsSedes") val idsSedes: List<Int>
)

@JsonClass(generateAdapter = true)
data class GuardarServiciosRequest(
    @Json(name = "servicios") val servicios: List<ServicioProfesionalItem>
)