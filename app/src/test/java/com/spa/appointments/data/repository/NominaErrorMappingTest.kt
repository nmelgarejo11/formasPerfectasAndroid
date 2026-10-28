package com.spa.appointments.data.repository

import com.spa.appointments.data.remote.NominaErrorMapper
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import retrofit2.Response

class NominaErrorMappingTest {

    private lateinit var moshi: Moshi

    @Before
    fun setup() {
        moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
    }

    @Test
    fun testParseoError422_ConCodigo410_PreliquidacionDesactualizada() {
        val jsonError = """{"mensaje":"La preliquidación expiró","codigo":-410}"""
        val response = Response.error<Any>(422, jsonError.toResponseBody())

        val exception = NominaErrorMapper.parsearError(response, moshi)

        assertEquals(422, exception.httpStatus)
        assertEquals(-410, exception.codigo)
        assertEquals("La preliquidación expiró", exception.message)
        assertTrue(exception.esPreliquidacionDesactualizada())
        assertFalse(exception.esConflicto())
        assertFalse(exception.esReintentable())
    }

    @Test
    fun testParseoError409_Conflicto() {
        val jsonError = """{"mensaje":"Ya existe una liquidación vigente","codigo":-409}"""
        val response = Response.error<Any>(409, jsonError.toResponseBody())

        val exception = NominaErrorMapper.parsearError(response, moshi)

        assertEquals(409, exception.httpStatus)
        assertEquals(-409, exception.codigo)
        assertTrue(exception.esConflicto())
    }

    @Test
    fun testParseoError503_ServiceUnavailable_ConRetryAfterHeader() {
        val jsonError = """{"mensaje":"Error temporal de concurrencia","codigo":-503}"""
        val body = jsonError.toResponseBody()

        val rawResponse = okhttp3.Response.Builder()
            .code(503)
            .message("Service Unavailable")
            .protocol(Protocol.HTTP_1_1)
            .request(Request.Builder().url("http://localhost/").build())
            .addHeader("Retry-After", "2")
            .build()

        val response = Response.error<Any>(body, rawResponse)

        val exception = NominaErrorMapper.parsearError(response, moshi)

        assertEquals(503, exception.httpStatus)
        assertEquals(-503, exception.codigo)
        assertEquals(2, exception.retryAfterSegundos)
        assertTrue(exception.esReintentable())
    }

    @Test
    fun testParseoError404_NoEncontrado() {
        val jsonError = """{"mensaje":"Liquidación no encontrada"}"""
        val response = Response.error<Any>(404, jsonError.toResponseBody())

        val exception = NominaErrorMapper.parsearError(response, moshi)

        assertEquals(404, exception.httpStatus)
        assertNull(exception.codigo)
        assertTrue(exception.esNoEncontrado())
    }
}
