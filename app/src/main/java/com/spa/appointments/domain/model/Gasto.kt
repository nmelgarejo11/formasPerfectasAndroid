package com.spa.appointments.domain.model

data class GastoResponse(
    val id: Int,
    val idSede: Int,
    val sede: String,
    val concepto: String,
    val valor: Double,
    val idMetodoPago: Int,
    val metodoPago: String,
    val idMetodoPagoDetalle: Int?,
    val metodoPagoDetalle: String?,
    val fechaGasto: String,
    val fechaRegistro: String
)

data class GastoRequest(
    val idSede: Int,
    val concepto: String,
    val valor: Double,
    val idMetodoPago: Int,
    val idMetodoPagoDetalle: Int?,
    val fechaGasto: String
)

data class GastoResultado(
    val id: Int?,
    val mensaje: String
)