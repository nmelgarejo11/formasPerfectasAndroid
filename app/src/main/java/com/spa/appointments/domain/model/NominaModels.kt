package com.spa.appointments.domain.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class LiquidacionNominaResponse(
    @Json(name = "id") val id: Int,
    @Json(name = "idEmpresa") val idEmpresa: Int,
    @Json(name = "idContratoLaboral") val idContratoLaboral: Int,
    @Json(name = "idPeriodoNomina") val idPeriodoNomina: Int,
    @Json(name = "totalDevengado") val totalDevengado: Double,
    @Json(name = "totalDeducciones") val totalDeducciones: Double,
    @Json(name = "netoPagar") val netoPagar: Double,
    @Json(name = "estado") val estado: Int,
    @Json(name = "nombreEstado") val nombreEstado: String?,
    @Json(name = "fechaLiquidacion") val fechaLiquidacion: String,
    @Json(name = "salarioBaseSnapshot") val salarioBaseSnapshot: Double,
    @Json(name = "smmlvSnapshot") val smmlvSnapshot: Double,
    @Json(name = "auxTransporteSnapshot") val auxTransporteSnapshot: Double,
    @Json(name = "idPreliquidacion") val idPreliquidacion: Int?,
    @Json(name = "ibcCalculado") val ibcCalculado: Double?,
    @Json(name = "detalles") val detalles: List<LiquidacionNominaDetalleResponse> = emptyList()
)

@JsonClass(generateAdapter = true)
data class LiquidacionNominaDetalleResponse(
    @Json(name = "id") val id: Int,
    @Json(name = "idLiquidacionNomina") val idLiquidacionNomina: Int,
    @Json(name = "idConcepto") val idConcepto: Int,
    @Json(name = "nombreConcepto") val nombreConcepto: String?,
    @Json(name = "tipoConcepto") val tipoConcepto: Int,
    @Json(name = "valor") val valor: Double,
    @Json(name = "cantidad") val cantidad: Double?,
    @Json(name = "idNovedadNomina") val idNovedadNomina: Int?,
    @Json(name = "divisorHorarioSnapshot") val divisorHorarioSnapshot: Int?,
    @Json(name = "valorHoraSnapshot") val valorHoraSnapshot: Double?,
    @Json(name = "factorAplicadoSnapshot") val factorAplicadoSnapshot: Double?,
    @Json(name = "idDescuento") val idDescuento: Int?,
    @Json(name = "esBaseSalarial") val esBaseSalarial: Boolean
)

@JsonClass(generateAdapter = true)
data class PreliquidarContratoRequest(
    @Json(name = "idContratoLaboral") val idContratoLaboral: Int,
    @Json(name = "idPeriodoNomina") val idPeriodoNomina: Int
)

@JsonClass(generateAdapter = true)
data class PreliquidacionResponse(
    @Json(name = "idPreliquidacion") val idPreliquidacion: Int,
    @Json(name = "idContratoLaboral") val idContratoLaboral: Int,
    @Json(name = "idPeriodoNomina") val idPeriodoNomina: Int,
    @Json(name = "totalDevengado") val totalDevengado: Double,
    @Json(name = "totalDeducciones") val totalDeducciones: Double,
    @Json(name = "netoPagar") val netoPagar: Double,
    @Json(name = "salarioBaseSnapshot") val salarioBaseSnapshot: Double,
    @Json(name = "smmlvSnapshot") val smmlvSnapshot: Double,
    @Json(name = "auxTransporteSnapshot") val auxTransporteSnapshot: Double,
    @Json(name = "ibcCalculado") val ibcCalculado: Double,
    @Json(name = "mensaje") val mensaje: String?,
    @Json(name = "detalles") val detalles: List<PreliquidacionDetalleResponse> = emptyList()
)

@JsonClass(generateAdapter = true)
data class PreliquidacionDetalleResponse(
    @Json(name = "idConcepto") val idConcepto: Int,
    @Json(name = "codigoConcepto") val codigoConcepto: String?,
    @Json(name = "nombreConcepto") val nombreConcepto: String?,
    @Json(name = "tipoConcepto") val tipoConcepto: Int,
    @Json(name = "valor") val valor: Double,
    @Json(name = "cantidad") val cantidad: Double?,
    @Json(name = "esBaseSalarial") val esBaseSalarial: Boolean,
    @Json(name = "idNovedadNomina") val idNovedadNomina: Int?,
    @Json(name = "idDescuento") val idDescuento: Int?
)

@JsonClass(generateAdapter = true)
data class CrearLiquidacionDesdePreliquidacionRequest(
    @Json(name = "idPreliquidacion") val idPreliquidacion: Int
)

@JsonClass(generateAdapter = true)
data class AnularLiquidacionRequest(
    @Json(name = "motivoAnulacion") val motivoAnulacion: String
)

@JsonClass(generateAdapter = true)
data class RespuestaSimpleNomina(
    @Json(name = "id") val id: Int? = null,
    @Json(name = "mensaje") val mensaje: String? = null,
    @Json(name = "codigo") val codigo: Int? = null
)
