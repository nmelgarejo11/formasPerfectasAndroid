package com.spa.appointments.ui.citas

import com.spa.appointments.domain.model.ReagendarCitaDirectoRequest
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
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

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun testProcesarMensajeWhatsApp_ReemplazaPlantillaTemaEmpresa() {
        val testDispatcher = UnconfinedTestDispatcher()
        Dispatchers.setMain(testDispatcher)
        try {
            val dummyApi = java.lang.reflect.Proxy.newProxyInstance(
                com.spa.appointments.data.remote.ApiService::class.java.classLoader,
                arrayOf(com.spa.appointments.data.remote.ApiService::class.java)
            ) { _, _, _ -> null } as com.spa.appointments.data.remote.ApiService

            val repo = com.spa.appointments.data.repository.CitasRepository(dummyApi)
            val clienteRepo = com.spa.appointments.data.repository.ClienteRepository(dummyApi)
            val viewModel = MisCitasViewModel(repo, clienteRepo)

            val cita = com.spa.appointments.domain.model.Cita(
                id = 10,
                fechaHoraInicio = "2026-05-15T10:30:00",
                fechaHoraFin = "2026-05-15T12:00:00",
                notas = null,
                idEstado = 1,
                estado = "Confirmada",
                colorEstado = "#00FF00",
                idProfesional = 2,
                profesional = "Ana Gomez",
                fotoProfesional = null,
                cargoProfesional = "Esteticista",
                sede = "Sede Principal",
                servicios = "Masaje",
                total = 50000.0,
                idCitaOriginal = null,
                fechaHoraInicioOriginal = null,
                nombreCliente = "María García",
                cantidadPersonas = 2,
                idSede = 1
            )

            val plantillaTema = "Hola {Nombre}, confirmamos tu cita de {NombreCliente} ({Apellido}) para el {Fecha} a las {Hora}. Duracion: {Duracion}. Asistentes: {Asistentes}."
            val resultado = viewModel.procesarMensajeWhatsApp(plantillaTema, cita)

            val esperado = "Hola María, confirmamos tu cita de María García (García) para el 15/05/2026 a las 10:30 AM. Duracion: 1 hr 30 min. Asistentes: 2 personas."
            assertEquals(esperado, resultado)
        } finally {
            Dispatchers.resetMain()
        }
    }
}
