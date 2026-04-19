package com.spa.appointments.domain.model

data class Perfil(
    val idUsuario: Int,
    val nombreUsuario: String,
    val fotoUrl: String?,
    val idCliente: Int,
    val nombre: String,
    val apellido: String,
    val telefono: String?,
    val email: String?
)

data class ActualizarPerfilRequest(
    val nombre: String,
    val apellido: String,
    val telefono: String?,
    val email: String?
)

data class FotoResponse(
    val fotoUrl: String
)