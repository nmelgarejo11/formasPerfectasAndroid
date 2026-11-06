package com.spa.appointments.domain.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class NovedadNominaResponse(
    @Json(name = "id") val id: Int,
    @Json(name = "idEmpresa") val idEmpresa: Int,
    @Json(name = "idContratoLaboral") val idContratoLaboral: Int,
    @Json(name = "idConcepto") val idConcepto: Int,
    @Json(name = "nombreConcepto") val nombreConcepto: String?,
    @Json(name = "fechaNovedad") val fechaNovedad: String,
    @Json(name = "valor") val valor: Double,
    @Json(name = "dias") val dias: Double? = null,
    @Json(name = "observaciones") val observaciones: String? = null,
    @Json(name = "estado") val estado: Boolean,
    @Json(name = "fechaCreacion") val fechaCreacion: String? = null
)

@JsonClass(generateAdapter = true)
data class CrearNovedadNominaRequest(
    @Json(name = "idContratoLaboral") val idContratoLaboral: Int,
    @Json(name = "idConcepto") val idConcepto: Int,
    @Json(name = "fechaNovedad") val fechaNovedad: String,
    @Json(name = "valor") val valor: Double,
    @Json(name = "dias") val dias: Double? = null,
    @Json(name = "observaciones") val observaciones: String? = null
)

@JsonClass(generateAdapter = true)
data class ActualizarNovedadNominaRequest(
    @Json(name = "idConcepto") val idConcepto: Int,
    @Json(name = "fechaNovedad") val fechaNovedad: String,
    @Json(name = "valor") val valor: Double,
    @Json(name = "dias") val dias: Double? = null,
    @Json(name = "observaciones") val observaciones: String? = null
)

@JsonClass(generateAdapter = true)
data class ConceptoResponse(
    @Json(name = "id") val id: Int,
    @Json(name = "codigo") val codigo: String?,
    @Json(name = "nombre") val nombre: String,
    @Json(name = "tipoConcepto") val tipoConcepto: Int, // 1 = Devengado, 2 = Deducción
    @Json(name = "aplicaSalarioBase") val aplicaSalarioBase: Boolean,
    @Json(name = "descuentaDiasSueldo") val descuentaDiasSueldo: Boolean
)

@JsonClass(generateAdapter = true)
data class CrearPeriodoNominaRequest(
    @Json(name = "nombre") val nombre: String? = null,
    @Json(name = "fechaInicio") val fechaInicio: String,
    @Json(name = "fechaFin") val fechaFin: String
)
