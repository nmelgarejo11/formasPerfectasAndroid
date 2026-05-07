package com.spa.appointments.domain.model

data class FinalizarCitaRequest(
    val idCita:       Int,
    val idMetodoPago: Int,
    val idMetodoPagoDetalle: Int? = null
)

data class FinalizarCitaResponse(
    val ok:      Boolean,
    val mensaje: String
)