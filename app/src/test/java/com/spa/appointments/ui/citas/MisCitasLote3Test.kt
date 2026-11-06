package com.spa.appointments.ui.citas

import com.spa.appointments.domain.model.FinalizarCitaRequest
import com.spa.appointments.domain.model.GuardarPrecioRangoRequest
import com.spa.appointments.domain.model.ServicioPrecioRango
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class MisCitasLote3Test {

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    @Test
    fun testFinalizarCitaRequest_Serializacion_ConPrecioFinalYObservaciones() {
        val req = FinalizarCitaRequest(
            idCita = 101,
            idMetodoPago = 1,
            idMetodoPagoDetalle = 10,
            precioFinal = 150000.0,
            observaciones = "Descuento por cliente frecuente aplicado"
        )

        val adapter = moshi.adapter(FinalizarCitaRequest::class.java)
        val json = adapter.toJson(req)

        assertNotNull(json)
        assertEquals(true, json.contains("\"idCita\":101"))
        assertEquals(true, json.contains("\"precioFinal\":150000.0"))
        assertEquals(true, json.contains("\"observaciones\":\"Descuento por cliente frecuente aplicado\""))
    }

    @Test
    fun testServicioPrecioRango_Deserializacion() {
        val json = """
            {
                "id": 1,
                "idServicio": 10,
                "cantidadPersonas": 3,
                "precio": 50000.0,
                "estado": true
            }
        """.trimIndent()

        val adapter = moshi.adapter(ServicioPrecioRango::class.java)
        val result = adapter.fromJson(json)

        assertNotNull(result)
        assertEquals(3, result?.cantidadPersonas)
        assertEquals(50000.0, result?.precio ?: 0.0, 0.001)
    }

    @Test
    fun testGuardarPrecioRangoRequest_Serializacion() {
        val req = GuardarPrecioRangoRequest(
            idServicio = 10,
            cantidadPersonas = 4,
            precio = 75000.0
        )

        val adapter = moshi.adapter(GuardarPrecioRangoRequest::class.java)
        val json = adapter.toJson(req)

        assertEquals(true, json.contains("\"cantidadPersonas\":4"))
        assertEquals(true, json.contains("\"precio\":75000.0"))
    }
}
