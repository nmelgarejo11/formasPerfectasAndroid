package com.spa.appointments.domain.model

data class IngresoRequest(
    val idSede: Int,
    val concepto: String,
    val valor: Double,
    val idMetodoPago: Int,
    val idMetodoPagoDetalle: Int?,
    val fechaIngreso: String
)

data class IngresoResponse(
    val id: Int,
    val idSede: Int,
    val sede: String,
    val concepto: String,
    val valor: Double,
    val idMetodoPago: Int,
    val metodoPago: String,
    val idMetodoPagoDetalle: Int?,
    val metodoPagoDetalle: String?,
    val fechaIngreso: String,
    val fechaRegistro: String
)

data class IngresoResultado(
    val id: Int?,
    val mensaje: String
)