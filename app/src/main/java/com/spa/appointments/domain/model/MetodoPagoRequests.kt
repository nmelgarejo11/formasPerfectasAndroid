package com.spa.appointments.domain.model

data class CrearMetodoPagoRequest(val nombre: String)
data class ActualizarMetodoPagoRequest(val nombre: String, val activo: Boolean)
data class CrearDetalleRequest(val nombre: String)
data class ActualizarDetalleRequest(val nombre: String, val activo: Boolean)
data class MetodoPagoIdResponse(val id: Int)