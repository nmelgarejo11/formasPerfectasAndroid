package com.spa.appointments.domain.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// Representa un submódulo (ítem del menú)
// Ejemplo: "Reservar cita", "Mis citas", "Historial"
@JsonClass(generateAdapter = true)
data class SubModulo(
    @Json(name = "nombre") val nombre: String,
    @Json(name = "ruta")   val ruta: String,
    @Json(name = "icono")  val icono: String?
)

// Representa un módulo (grupo del menú)
// Ejemplo: "Citas", "Financiero", "Perfil"
@JsonClass(generateAdapter = true)
data class Modulo(
    @Json(name = "modulo")     val modulo: String,
    @Json(name = "icono")      val icono: String?,
    @Json(name = "submodulos") val submodulos: List<SubModulo>?
)

// Wrapper raíz del JSON: { "menu": [ ... ] }
@JsonClass(generateAdapter = true)
data class MenuResponse(
    @Json(name = "menu") val menu: List<Modulo>?
)