package com.spa.appointments.domain.model

class NominaApiException(
    val httpStatus: Int,
    val codigo: Int?,
    override val message: String,
    val retryAfterSegundos: Int? = null
) : Exception(message) {
    fun esPreliquidacionDesactualizada(): Boolean = codigo == -410
    fun esConflicto(): Boolean = httpStatus == 409
    fun esReintentable(): Boolean = httpStatus == 503
    fun esNoEncontrado(): Boolean = httpStatus == 404
}
