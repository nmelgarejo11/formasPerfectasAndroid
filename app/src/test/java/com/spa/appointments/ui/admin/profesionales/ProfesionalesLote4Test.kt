package com.spa.appointments.ui.admin.profesionales

import com.spa.appointments.domain.model.ProfesionalAdmin
import com.spa.appointments.domain.model.ProfesionalRequest
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class ProfesionalesLote4Test {

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    @Test
    fun testProfesionalRequest_Serializacion_ConDocumentoYBanco() {
        val req = ProfesionalRequest(
            idCargo = 2,
            nombre = "María",
            apellido = "Pérez",
            telefono = "3001234567",
            email = "maria@example.com",
            idTipoDocumento = 1,
            identificacion = "1018222333",
            idBanco = 3,
            idTipoCuenta = 1,
            numeroCuenta = "987654321"
        )

        val adapter = moshi.adapter(ProfesionalRequest::class.java)
        val json = adapter.toJson(req)

        assertNotNull(json)
        assertEquals(true, json.contains("\"idCargo\":2"))
        assertEquals(true, json.contains("\"idTipoDocumento\":1"))
        assertEquals(true, json.contains("\"identificacion\":\"1018222333\""))
        assertEquals(true, json.contains("\"idBanco\":3"))
        assertEquals(true, json.contains("\"idTipoCuenta\":1"))
        assertEquals(true, json.contains("\"numeroCuenta\":\"987654321\""))
    }

    @Test
    fun testProfesionalAdmin_Deserializacion_ConDocumentoYBanco() {
        val json = """
            {
                "id": 10,
                "idCargo": 2,
                "nombreCargo": "Especialista",
                "nombre": "Carlos",
                "apellido": "López",
                "foto": null,
                "telefono": "3109876543",
                "email": "carlos@example.com",
                "estado": true,
                "idTipoDocumento": 1,
                "identificacion": "80123456",
                "idBanco": 2,
                "idTipoCuenta": 2,
                "numeroCuenta": "123456789"
            }
        """.trimIndent()

        val adapter = moshi.adapter(ProfesionalAdmin::class.java)
        val result = adapter.fromJson(json)

        assertNotNull(result)
        assertEquals(10, result?.id)
        assertEquals("80123456", result?.identificacion)
        assertEquals(2, result?.idBanco)
        assertEquals("123456789", result?.numeroCuenta)
    }
}
