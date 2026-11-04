package com.spa.appointments.data.remote

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import retrofit2.Response

class NominaErrorMapperTest {

    private lateinit var moshi: Moshi

    @Before
    fun setup() {
        moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
    }

    @Test
    fun testParsearError_Http403ConCuerpoJson_ConvierteANominaApiException() {
        val json403 = """{"mensaje":"No tiene permisos para acceder a este módulo","codigo":-403}"""
        val response = Response.error<Any>(403, json403.toResponseBody())

        val exception = NominaErrorMapper.parsearError(response, moshi)

        assertEquals(403, exception.httpStatus)
        assertEquals(-403, exception.codigo)
        assertEquals("No tiene permisos para acceder a este módulo", exception.message)
    }
}
