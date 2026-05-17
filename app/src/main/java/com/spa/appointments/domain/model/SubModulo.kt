package com.spa.appointments.domain.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SubModuloAdmin(
    @Json(name = "Id")           val id:           Int,
    @Json(name = "Nombre")       val nombre:       String,
    @Json(name = "Seleccionado") val seleccionado: Boolean = false
)

@JsonClass(generateAdapter = true)
data class SubModuloAsignadoDto(
    @Json(name = "idSubModulo") val idSubModulo: Int,
    @Json(name = "nombre") val nombre: String,
    @Json(name = "ruta") val ruta: String?,
    @Json(name = "icono") val icono: String?,
    @Json(name = "idModulo") val idModulo: Int,
    @Json(name = "asignado") val asignado: Boolean
)

@JsonClass(generateAdapter = true)
data class AsignarSubModuloRequest(
    @Json(name = "idPerfil") val idPerfil: Int,
    @Json(name = "idSubModulo") val idSubModulo: Int
)