package com.spa.appointments.ui.citas

import com.spa.appointments.domain.model.Cliente
import com.spa.appointments.domain.model.CrearCitaRequest
import com.spa.appointments.domain.model.CrearCitaGrupalItem
import com.spa.appointments.domain.model.CrearCitasGrupalesRequest
import com.spa.appointments.domain.model.Profesional
import com.spa.appointments.domain.model.ServicioDetalle
import com.spa.appointments.domain.model.SlotDisponible
import com.spa.appointments.ui.reserva.ReservaSharedViewModel
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class AgendamientoGrupalTest {

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    @Test
    fun testPrepararCitaGrupal_InicializaListaDeAsistentesCorrectamente() {
        val sharedVm = ReservaSharedViewModel()
        val cliente = Cliente(
            id = 1,
            nombre = "Carlos",
            apellido = "Pérez",
            telefono = "3001234567",
            email = "carlos@test.com"
        )

        sharedVm.esGrupal = true
        sharedVm.prepararCitaGrupal(cliente, 3)

        assertEquals(3, sharedVm.cantidadPersonas)
        assertEquals(3, sharedVm.asistentesGrupales.size)
        assertEquals(0, sharedVm.asistenteActualIndex)
        assertEquals(1, sharedVm.asistentesGrupales[0].numeroAsistente)
        assertEquals(2, sharedVm.asistentesGrupales[1].numeroAsistente)
        assertEquals(3, sharedVm.asistentesGrupales[2].numeroAsistente)
    }

    @Test
    fun testConfiguracionAsistentesGrupales_MantieneProfesionalesYFechasDiferentes() {
        val sharedVm = ReservaSharedViewModel()
        val cliente = Cliente(id = 1, nombre = "Ana", apellido = "Gomez", telefono = "3001234567", email = "ana@test.com")
        sharedVm.esGrupal = true
        sharedVm.prepararCitaGrupal(cliente, 3)

        val prof1 = Profesional(id = 10, nombre = "Laura", apellido = "M", nombreCompleto = "Laura M", foto = null, telefono = null, email = null, idCargo = 1, cargo = "Especialista", idSede = 1, sede = "Sede Central")
        val prof2 = Profesional(id = 20, nombre = "Diego", apellido = "R", nombreCompleto = "Diego R", foto = null, telefono = null, email = null, idCargo = 1, cargo = "Especialista", idSede = 1, sede = "Sede Central")
        val prof3 = Profesional(id = 10, nombre = "Laura", apellido = "M", nombreCompleto = "Laura M", foto = null, telefono = null, email = null, idCargo = 1, cargo = "Especialista", idSede = 1, sede = "Sede Central")

        val slot1 = SlotDisponible(horaInicio = "09:00:00", horaFin = "10:00:00", disponible = true)
        val slot2 = SlotDisponible(horaInicio = "10:00:00", horaFin = "11:00:00", disponible = true)
        val slot3 = SlotDisponible(horaInicio = "11:00:00", horaFin = "12:00:00", disponible = true)

        val fecha = LocalDate.of(2025, 10, 15)

        sharedVm.asistentesGrupales[0].profesional = prof1
        sharedVm.asistentesGrupales[0].fecha = fecha
        sharedVm.asistentesGrupales[0].slot = slot1

        sharedVm.asistentesGrupales[1].profesional = prof2
        sharedVm.asistentesGrupales[1].fecha = fecha
        sharedVm.asistentesGrupales[1].slot = slot2

        sharedVm.asistentesGrupales[2].profesional = prof3
        sharedVm.asistentesGrupales[2].fecha = fecha
        sharedVm.asistentesGrupales[2].slot = slot3

        assertEquals(10, sharedVm.asistentesGrupales[0].profesional?.id)
        assertEquals("09:00:00", sharedVm.asistentesGrupales[0].slot?.horaInicio)

        assertEquals(20, sharedVm.asistentesGrupales[1].profesional?.id)
        assertEquals("10:00:00", sharedVm.asistentesGrupales[1].slot?.horaInicio)

        assertEquals(10, sharedVm.asistentesGrupales[2].profesional?.id)
        assertEquals("11:00:00", sharedVm.asistentesGrupales[2].slot?.horaInicio)
    }

    @Test
    fun testCrearCitaRequest_Serializacion_CitaGrupalIndependiente() {
        val request = CrearCitaRequest(
            idSede = 1,
            idCliente = 5,
            idProfesional = 10,
            fechaInicio = "2025-10-15T09:00:00",
            fechaFin = "2025-10-15T10:00:00",
            notas = "Cita Grupal (Asistente 1 de 3)",
            cantidadPersonas = 3,
            responsableNombre = "Carlos Pérez",
            responsableTelefono = "3001234567",
            responsableEmail = "carlos@test.com",
            servicios = listOf(ServicioDetalle(idServicio = 2, precio = 50000.0, duracion = 60))
        )

        val adapter = moshi.adapter(CrearCitaRequest::class.java)
        val json = adapter.toJson(request)

        assertNotNull(json)
        assertTrue(json.contains("\"idProfesional\":10"))
        assertTrue(json.contains("\"cantidadPersonas\":3"))
        assertTrue(json.contains("\"notas\":\"Cita Grupal (Asistente 1 de 3)\""))
    }

    @Test
    fun testCrearCitasGrupalesRequest_Serializacion() {
        val request = CrearCitasGrupalesRequest(
            idSede = 1,
            idCliente = 5,
            cantidadPersonas = 2,
            responsableNombre = "Nicolas",
            responsableTelefono = "3001234567",
            responsableEmail = "nicolas@test.com",
            citas = listOf(
                CrearCitaGrupalItem(
                    idProfesional = 10,
                    fechaInicio = "2025-10-15T09:00:00",
                    fechaFin = "2025-10-15T10:00:00",
                    notas = "Cita Grupal (Asistente 1 de 2)",
                    servicios = listOf(ServicioDetalle(idServicio = 2, precio = 50000.0, duracion = 60))
                )
            )
        )

        val adapter = moshi.adapter(CrearCitasGrupalesRequest::class.java)
        val json = adapter.toJson(request)

        assertNotNull(json)
        assertTrue(json.contains("\"cantidadPersonas\":2"))
        assertTrue(json.contains("\"responsableEmail\":\"nicolas@test.com\""))
        assertTrue(json.contains("\"citas\":"))
    }

    @Test
    fun testNormalizarHora_SinSegundos_AgregaSegundosCorrectamente() {
        fun normalizarHora(hora: String): String {
            val trimmed = hora.trim()
            return if (trimmed.length == 5) "$trimmed:00" else trimmed
        }

        assertEquals("11:00:00", normalizarHora("11:00"))
        assertEquals("11:00:00", normalizarHora("11:00:00"))
    }

    @Test
    fun testSolapamientoHorario_Servicio90Minutos_DetectaConflictoEnHorariosIntermedios() {
        val slotOcupado = SlotDisponible(horaInicio = "10:00:00", horaFin = "11:30:00", disponible = true)

        val slotIntermedio = SlotDisponible(horaInicio = "10:30:00", horaFin = "12:00:00", disponible = true)
        val slotMismaHora = SlotDisponible(horaInicio = "10:00:00", horaFin = "11:30:00", disponible = true)
        val slotPosteriorValido = SlotDisponible(horaInicio = "11:30:00", horaFin = "13:00:00", disponible = true)
        val slotAnteriorValido = SlotDisponible(horaInicio = "08:30:00", horaFin = "10:00:00", disponible = true)

        fun parseLocalTime(horaStr: String): java.time.LocalTime {
            val partes = horaStr.split(":")
            return java.time.LocalTime.of(partes[0].toInt(), partes[1].toInt())
        }

        fun haySolapamiento(s1: SlotDisponible, s2: SlotDisponible): Boolean {
            val startA = parseLocalTime(s1.horaInicio)
            val endA = parseLocalTime(s1.horaFin)
            val startB = parseLocalTime(s2.horaInicio)
            val endB = parseLocalTime(s2.horaFin)
            return startB.isBefore(endA) && startA.isBefore(endB)
        }

        assertTrue(haySolapamiento(slotOcupado, slotIntermedio)) // 10:30 a 12:00 debe solaparse con 10:00 a 11:30
        assertTrue(haySolapamiento(slotOcupado, slotMismaHora))  // 10:00 a 11:30 solapa
        assertEquals(false, haySolapamiento(slotOcupado, slotPosteriorValido)) // 11:30 a 13:00 NO solapa
        assertEquals(false, haySolapamiento(slotOcupado, slotAnteriorValido))  // 08:30 a 10:00 NO solapa
    }
}
