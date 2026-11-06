package com.spa.appointments.ui.tema

import com.spa.appointments.data.remote.TemaEmpresaRequest
import com.spa.appointments.domain.model.TemaEmpresa
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class TemaModelTest {

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    @Test
    fun testTemaEmpresa_Deserializacion_ConNitYRazonSocial() {
        val json = """
            {
                "colorPrimario": "#123456",
                "colorSecundario": "#654321",
                "colorTerciario": "#abcdef",
                "logoUrl": "http://example.com/logo.png",
                "nombreApp": "Mi App",
                "slogan": "Un gran slogan",
                "nombreEmpresa": "Mi Empresa S.A.S",
                "mensajeWhatsApp": "Hola {NombreCliente}",
                "nit": "900.123.456-7",
                "razonSocial": "Mi Empresa Sociedad por Acciones Simplificada"
            }
        """.trimIndent()

        val adapter = moshi.adapter(TemaEmpresa::class.java)
        val result = adapter.fromJson(json)

        assertNotNull(result)
        assertEquals("#123456", result?.colorPrimario)
        assertEquals("900.123.456-7", result?.nit)
        assertEquals("Mi Empresa Sociedad por Acciones Simplificada", result?.razonSocial)
    }

    @Test
    fun testTemaEmpresaRequest_Serializacion_ConNitYRazonSocial() {
        val req = TemaEmpresaRequest(
            colorPrimario = "#111111",
            colorSecundario = "#222222",
            colorTerciario = "#333333",
            nombreApp = "Test App",
            slogan = "Test Slogan",
            mensajeWhatsApp = "Hola",
            nit = "900.999.888-1",
            razonSocial = "Empresa Test S.A."
        )

        val adapter = moshi.adapter(TemaEmpresaRequest::class.java)
        val json = adapter.toJson(req)

        assertEquals(true, json.contains("\"nit\":\"900.999.888-1\""))
        assertEquals(true, json.contains("\"razonSocial\":\"Empresa Test S.A.\""))
    }
}
