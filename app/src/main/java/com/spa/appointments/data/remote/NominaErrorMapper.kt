package com.spa.appointments.data.remote

import com.spa.appointments.domain.model.NominaApiException
import com.spa.appointments.domain.model.RespuestaSimpleNomina
import com.squareup.moshi.Moshi
import retrofit2.Response

object NominaErrorMapper {
    fun <T> parsearError(response: Response<T>, moshi: Moshi): NominaApiException {
        val httpCode = response.code()
        val retryAfter = response.headers()["Retry-After"]?.toIntOrNull()
        val rawJson = response.errorBody()?.string()

        if (!rawJson.isNullOrBlank()) {
            try {
                val adapter = moshi.adapter(RespuestaSimpleNomina::class.java)
                val errObj = adapter.fromJson(rawJson)
                if (errObj != null && !errObj.mensaje.isNullOrBlank()) {
                    return NominaApiException(
                        httpStatus = httpCode,
                        codigo = errObj.codigo,
                        message = errObj.mensaje,
                        retryAfterSegundos = retryAfter
                    )
                }
            } catch (_: Exception) { }
        }

        val defaultMsg = when (httpCode) {
            400 -> "Datos inválidos en la solicitud"
            404 -> "Contrato, concepto o novedad no encontrada"
            422 -> "Acción no permitida en el estado o período actual"
            503 -> "El servidor de nómina está ocupado. Intenta de nuevo en unos momentos"
            else -> "Error de red ($httpCode)"
        }

        return NominaApiException(httpCode, null, defaultMsg, retryAfter)
    }
}
