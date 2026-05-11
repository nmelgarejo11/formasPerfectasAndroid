package com.spa.appointments.domain.model

data class MetodoPagoAdmin(
    val id: Int,
    val idEmpresa: Int,
    val nombre: String,
    val activo: Boolean,
    val detalles: List<MetodoPagoDetalle> = emptyList()
)

data class MetodoPagoDetalleAdmin(
    val id: Int,
    val idMetodoPago: Int,
    val nombre: String,
    val activo: Boolean
)