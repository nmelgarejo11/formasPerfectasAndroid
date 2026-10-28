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
                if (errObj != null) {
                    return NominaApiException(
                        httpStatus = httpCode,
                        codigo = errObj.codigo,
                        message = errObj.mensaje ?: "Error de red ($httpCode)",
                        retryAfterSegundos = retryAfter
                    )
                }
            } catch (_: Exception) { }
        }
        return NominaApiException(httpCode, null, "Error HTTP $httpCode", retryAfter)
    }
}
