package com.spa.appointments.domain.model

data class MetodoPago(
    val id: Int,
    val nombre: String,
    val detalles: List<MetodoPagoDetalle>? = emptyList()
)

data class MetodoPagoDetalle(
    val id: Int,
    val nombre: String
)