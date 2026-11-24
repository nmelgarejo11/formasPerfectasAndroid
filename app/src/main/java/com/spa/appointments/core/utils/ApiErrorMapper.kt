package com.spa.appointments.core.utils

import retrofit2.HttpException
import java.net.ConnectException
import java.net.UnknownHostException

object ApiErrorMapper {

    fun parsearMensajeError(throwable: Throwable?): String {
        if (throwable == null) return "Ocurrió un error inesperado"

        return when (throwable) {
            is HttpException -> {
                val errorBody = try {
                    throwable.response()?.errorBody()?.string()
                } catch (_: Exception) {
                    null
                }

                val mensajeServidor = errorBody?.let { parsearMensajeJson(it) }

                when {
                    !mensajeServidor.isNullOrBlank() -> mensajeServidor
                    throwable.code() == 403 -> "No tienes permisos para acceder a esta sección o realizar esta acción."
                    throwable.code() == 401 -> "Tu sesión ha expirado. Por favor vuelve a iniciar sesión."
                    throwable.code() == 404 -> "El recurso solicitado no fue encontrado."
                    throwable.code() == 422 -> "La acción solicitada no es válida para el estado actual."
                    throwable.code() in listOf(500, 502, 503) -> "El servidor no se encuentra disponible momentáneamente. Intenta más tarde."
                    else -> throwable.message() ?: "Error de servidor (${throwable.code()})"
                }
            }
            is UnknownHostException, is ConnectException ->
                "Sin conexión a internet. Verifica tu red y vuelve a intentarlo."
            else -> throwable.localizedMessage ?: throwable.message ?: "Ocurrió un error inesperado"
        }
    }

    private fun parsearMensajeJson(json: String): String? {
        if (json.isBlank()) return null
        val lower = json.lowercase()
        if (lower.contains("registrado") || lower.contains("ya existe") || lower.contains("celular") || lower.contains("teléfono")) {
            return "El Cliente ya se encuentra registrado"
        }
        return try {
            val jsonObject = org.json.JSONObject(json)
            when {
                jsonObject.has("mensaje") -> jsonObject.getString("mensaje")
                jsonObject.has("message") -> jsonObject.getString("message")
                jsonObject.has("detail") -> jsonObject.getString("detail")
                jsonObject.has("title") -> jsonObject.getString("title")
                else -> null
            }
        } catch (_: Exception) {
            if (json.length < 150 && !json.trim().startsWith("<")) json.trim() else null
        }
    }

    fun esForbidden(throwable: Throwable?): Boolean {
        if (throwable == null) return false
        if (throwable is HttpException && throwable.code() == 403) return true
        val msg = throwable.message?.lowercase() ?: ""
        return msg.contains("403") || msg.contains("forbidden")
    }
}
