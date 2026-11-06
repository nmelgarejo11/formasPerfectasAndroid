package com.spa.appointments.ui.citas

import com.spa.appointments.domain.model.ReagendarCitaDirectoRequest
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class MisCitasLote2Test {

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    @Test
    fun testReagendarCitaDirectoRequest_Serializacion() {
        val req = ReagendarCitaDirectoRequest(
            idProfesional = 5,
            fechaHoraInicio = "2025-05-20T10:00:00",
            fechaHoraFin = "2025-05-20T11:00:00"
        )

        val adapter = moshi.adapter(ReagendarCitaDirectoRequest::class.java)
        val json = adapter.toJson(req)

        assertNotNull(json)
        assertEquals(true, json.contains("\"idProfesional\":5"))
        assertEquals(true, json.contains("\"fechaHoraInicio\":\"2025-05-20T10:00:00\""))
        assertEquals(true, json.contains("\"fechaHoraFin\":\"2025-05-20T11:00:00\""))
    }

    @Test
    fun testModoVistaCitas_Valores() {
        assertEquals(2, ModoVistaCitas.values().size)
        assertEquals(ModoVistaCitas.LISTA, ModoVistaCitas.valueOf("LISTA"))
        assertEquals(ModoVistaCitas.CALENDARIO_DIA, ModoVistaCitas.valueOf("CALENDARIO_DIA"))
    }
}
