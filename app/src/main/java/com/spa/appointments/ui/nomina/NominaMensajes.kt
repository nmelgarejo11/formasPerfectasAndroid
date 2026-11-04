package com.spa.appointments.ui.nomina

import com.spa.appointments.domain.model.NominaApiException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

object NominaMensajes {
    const val ERROR_CONEXION = "No se pudo conectar con el servidor. Revisa tu conexión."
    const val ERROR_INESPERADO = "Ocurrió un error inesperado."
    const val ERROR_MOTIVO_ANULACION_OBLIGATORIO = "El motivo de anulación es obligatorio."
    const val ERROR_MOTIVO_ANULACION_LARGO = "El motivo no puede superar los 1000 caracteres."
    const val ERROR_CONTRATO_LIQUIDADO = "El contrato ya se encuentra liquidado. No se puede preliquidar."
    const val ERROR_CONTRATO_NO_LISTO = "El contrato no está listo para liquidar. Debe preliquidarse primero."
    const val ERROR_SIN_PRELIQUIDACION_VIGENTE = "No existe una preliquidación vigente para este contrato."
    const val ERROR_SERVIDOR_OCUPADO = "El servidor está ocupado, inténtalo de nuevo."
    const val ERROR_PRELIQUIDACION_DESACTUALIZADA_RECALCULAR = "La preliquidación está desactualizada (cambiaron datos de origen)."

    fun mapearError(error: Throwable): String {
        return when (error) {
            is NominaApiException -> error.message
            is SocketTimeoutException, is UnknownHostException, is IOException -> ERROR_CONEXION
            else -> ERROR_INESPERADO
        }
    }
}
