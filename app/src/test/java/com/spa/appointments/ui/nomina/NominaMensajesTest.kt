package com.spa.appointments.ui.nomina

import com.spa.appointments.domain.model.NominaApiException
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class NominaMensajesTest {

    @Test
    fun testMapearError_ExcepcionesDeRed_RetornaErrorConexion() {
        assertEquals(NominaMensajes.ERROR_CONEXION, NominaMensajes.mapearError(IOException("Network error")))
        assertEquals(NominaMensajes.ERROR_CONEXION, NominaMensajes.mapearError(SocketTimeoutException("Timeout")))
        assertEquals(NominaMensajes.ERROR_CONEXION, NominaMensajes.mapearError(UnknownHostException("Host not found")))
    }

    @Test
    fun testMapearError_NominaApiException_ConservaMensajeExacto() {
        val apiEx = NominaApiException(422, -410, "La preliquidación está desactualizada")
        assertEquals("La preliquidación está desactualizada", NominaMensajes.mapearError(apiEx))
    }

    @Test
    fun testMapearError_ExcepcionNoEsperada_RetornaErrorInesperadoSinMensajeCrudo() {
        val ex = NullPointerException("Null reference at internal object")
        assertEquals(NominaMensajes.ERROR_INESPERADO, NominaMensajes.mapearError(ex))
    }
}
