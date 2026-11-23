package com.spa.appointments.core.utils

import retrofit2.HttpException
import java.net.ConnectException
import java.net.UnknownHostException

object ApiErrorMapper {

    fun parsearMensajeError(throwable: Throwable?): String {
        if (throwable == null) return "Ocurrió un error inesperado"

        return when (throwable) {
            is HttpException -> when (throwable.code()) {
                403 -> "No tienes permisos para acceder a esta sección o realizar esta acción."
                401 -> "Tu sesión ha expirado. Por favor vuelve a iniciar sesión."
                404 -> "El recurso solicitado no fue encontrado."
                422 -> "La acción solicitada no es válida para el estado actual."
                500, 502, 503 -> "El servidor no se encuentra disponible momentáneamente. Intenta más tarde."
                else -> throwable.message() ?: "Error de servidor (${throwable.code()})"
            }
            is UnknownHostException, is ConnectException ->
                "Sin conexión a internet. Verifica tu red y vuelve a intentarlo."
            else -> throwable.localizedMessage ?: throwable.message ?: "Ocurrió un error inesperado"
        }
    }

    fun esForbidden(throwable: Throwable?): Boolean {
        if (throwable == null) return false
        if (throwable is HttpException && throwable.code() == 403) return true
        val msg = throwable.message?.lowercase() ?: ""
        return msg.contains("403") || msg.contains("forbidden")
    }
}
