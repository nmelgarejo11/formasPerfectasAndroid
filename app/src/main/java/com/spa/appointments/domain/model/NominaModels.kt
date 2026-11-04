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
    @Json(name = "liquidable") val liquidable: Boolean? = null,
    @Json(name = "motivo") val motivo: String? = null,
    @Json(name = "profesionalNombre") val profesionalNombre: String? = null,
    @Json(name = "estadoEjecucion") val estadoEjecucion: String? = null,
    @Json(name = "fechaEjecucion") val fechaEjecucion: String? = null,
    @Json(name = "diasComerciales") val diasComerciales: Double? = null,
    @Json(name = "vigente") val vigente: Boolean? = null,
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

@JsonClass(generateAdapter = true)
data class PeriodoNominaItem(
    @Json(name = "id") val id: Int,
    @Json(name = "nombre") val nombre: String,
    @Json(name = "fechaInicio") val fechaInicio: String,
    @Json(name = "fechaFin") val fechaFin: String,
    @Json(name = "estado") val estado: Int,
    @Json(name = "nombreEstado") val nombreEstado: String
)

@JsonClass(generateAdapter = true)
data class ContratoElegibleNomina(
    @Json(name = "idContratoLaboral") val idContratoLaboral: Int,
    @Json(name = "idProfesional") val idProfesional: Int,
    @Json(name = "profesionalNombre") val profesionalNombre: String,
    @Json(name = "profesionalDocumento") val profesionalDocumento: String?,
    @Json(name = "cargo") val cargo: String?,
    @Json(name = "idSede") val idSede: Int,
    @Json(name = "tipoContrato") val tipoContrato: String,
    @Json(name = "salarioBase") val salarioBase: Double,
    @Json(name = "fechaInicio") val fechaInicio: String,
    @Json(name = "fechaFin") val fechaFin: String?,
    @Json(name = "idPreliquidacionVigente") val idPreliquidacionVigente: Int?,
    @Json(name = "netoPreliquidado") val netoPreliquidado: Double?,
    @Json(name = "motivoPreliquidacion") val motivoPreliquidacion: String?,
    @Json(name = "idLiquidacionVigente") val idLiquidacionVigente: Int?,
    @Json(name = "netoLiquidado") val netoLiquidado: Double?
)

@JsonClass(generateAdapter = true)
data class PreliquidacionVigenteItem(
    @Json(name = "idPreliquidacion") val idPreliquidacion: Int,
    @Json(name = "idContratoLaboral") val idContratoLaboral: Int,
    @Json(name = "idPeriodoNomina") val idPeriodoNomina: Int,
    @Json(name = "profesionalNombre") val profesionalNombre: String,
    @Json(name = "profesionalDocumento") val profesionalDocumento: String?,
    @Json(name = "fechaEjecucion") val fechaEjecucion: String,
    @Json(name = "diasComerciales") val diasComerciales: Double,
    @Json(name = "totalDevengado") val totalDevengado: Double,
    @Json(name = "totalDeducciones") val totalDeducciones: Double,
    @Json(name = "netoPagar") val netoPagar: Double,
    @Json(name = "ibcCalculado") val ibcCalculado: Double,
    @Json(name = "salarioBaseSnapshot") val salarioBaseSnapshot: Double,
    @Json(name = "smmlvSnapshot") val smmlvSnapshot: Double,
    @Json(name = "auxTransporteSnapshot") val auxTransporteSnapshot: Double,
    @Json(name = "liquidable") val liquidable: Boolean,
    @Json(name = "motivo") val motivo: String?
)

@JsonClass(generateAdapter = true)
data class LiquidacionNominaResumen(
    @Json(name = "id") val id: Int,
    @Json(name = "idContratoLaboral") val idContratoLaboral: Int,
    @Json(name = "idPeriodoNomina") val idPeriodoNomina: Int,
    @Json(name = "periodoNombre") val periodoNombre: String,
    @Json(name = "periodoDesde") val periodoDesde: String,
    @Json(name = "periodoHasta") val periodoHasta: String,
    @Json(name = "profesionalNombre") val profesionalNombre: String,
    @Json(name = "profesionalDocumento") val profesionalDocumento: String?,
    @Json(name = "totalDevengado") val totalDevengado: Double,
    @Json(name = "totalDeducciones") val totalDeducciones: Double,
    @Json(name = "netoPagar") val netoPagar: Double,
    @Json(name = "estado") val estado: Int,
    @Json(name = "nombreEstado") val nombreEstado: String,
    @Json(name = "fechaLiquidacion") val fechaLiquidacion: String
)

@JsonClass(generateAdapter = true)
data class PaginaLiquidaciones(
    @Json(name = "items") val items: List<LiquidacionNominaResumen> = emptyList(),
    @Json(name = "total") val total: Int,
    @Json(name = "pagina") val pagina: Int,
    @Json(name = "tamanoPagina") val tamanoPagina: Int
)
