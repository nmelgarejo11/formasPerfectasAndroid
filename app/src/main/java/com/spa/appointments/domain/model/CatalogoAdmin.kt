package com.spa.appointments.domain.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// ─── Categorías ───────────────────────────────────────────

@JsonClass(generateAdapter = true)
data class CategoriaAdmin(
    @Json(name = "id")     val id: Int,
    @Json(name = "nombre") val nombre: String,
    @Json(name = "icono")  val icono: String?,
    @Json(name = "estado") val estado: Boolean
)

@JsonClass(generateAdapter = true)
data class CategoriaRequest(
    @Json(name = "nombre") val nombre: String,
    @Json(name = "icono")  val icono: String?
)

// ─── Servicios ────────────────────────────────────────────

@JsonClass(generateAdapter = true)
data class ServicioAdmin(
    @Json(name = "id")              val id: Int,
    @Json(name = "idCategoria")     val idCategoria: Int,
    @Json(name = "nombreCategoria") val nombreCategoria: String,
    @Json(name = "nombre")          val nombre: String,
    @Json(name = "descripcion")     val descripcion: String?,
    @Json(name = "duracionMinutos") val duracionMinutos: Int,
    @Json(name = "precioBase")      val precioBase: Double,
    @Json(name = "icono")           val icono: String?,
    @Json(name = "estado")          val estado: Boolean
)

@JsonClass(generateAdapter = true)
data class ServicioRequest(
    @Json(name = "idCategoria")     val idCategoria: Int,
    @Json(name = "nombre")          val nombre: String,
    @Json(name = "descripcion")     val descripcion: String?,
    @Json(name = "duracionMinutos") val duracionMinutos: Int,
    @Json(name = "precioBase")      val precioBase: Double,
    @Json(name = "icono")           val icono: String?
)

// ─── Respuestas genéricas ─────────────────────────────────

@JsonClass(generateAdapter = true)
data class IdResponse(
    @Json(name = "id") val id: Int
)

@JsonClass(generateAdapter = true)
data class EstadoResponse(
    @Json(name = "estado") val estado: Boolean
)