package com.spa.appointments.domain.model

data class ExtraccionVoz(
    val nombre: String?,
    val apellido: String?,
    val telefono: String?,
    val email: String?
)

data class ExtraerDatosVozRequest(
    val textoDictado: String
)