package com.spa.appointments.ui.citas.components

import com.spa.appointments.domain.model.Cita
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDateTime

class CalendarioDiaViewTest {

    private fun createCita(id: Int, profesional: String, inicio: String, fin: String): Cita {
        return Cita(
            id = id,
            fechaHoraInicio = inicio,
            fechaHoraFin = fin,
            notas = null,
            idEstado = 1,
            estado = "Programada",
            colorEstado = "#4CAF50",
            idProfesional = id * 10,
            profesional = profesional,
            fotoProfesional = null,
            cargoProfesional = "Especialista",
            sede = "Sede Central",
            servicios = "Servicio Spa",
            total = 50000.0,
            idCitaOriginal = null,
            fechaHoraInicioOriginal = null,
            nombreCliente = "Cliente $id",
            idSede = 1
        )
    }

    @Test
    fun testPosicionamientoCitas_DosCitasMismaHora_AsignaColumnasEnParalelo() {
        val citaLucero = createCita(1, "Lucero", "2025-10-15T10:00:00", "2025-10-15T11:30:00")
        val citaLaura  = createCita(2, "Laura", "2025-10-15T10:00:00", "2025-10-15T11:30:00")

        val citas = listOf(citaLucero, citaLaura)

        // Reflejamos la lógica interna de la vista para test de modelo de posicionamiento
        val parseFecha = { str: String -> LocalDateTime.parse(str) }

        data class TestPosicion(val cita: Cita, val inicio: LocalDateTime, val fin: LocalDateTime, var colIdx: Int = 0, var totalCols: Int = 1)

        val posList = citas.map { TestPosicion(it, parseFecha(it.fechaHoraInicio), parseFecha(it.fechaHoraFin)) }

        val colEnds = mutableListOf<LocalDateTime>()
        for (item in posList) {
            var asignado = false
            for (i in colEnds.indices) {
                if (!colEnds[i].isAfter(item.inicio)) {
                    item.colIdx = i
                    colEnds[i] = item.fin
                    asignado = true
                    break
                }
            }
            if (!asignado) {
                item.colIdx = colEnds.size
                colEnds.add(item.fin)
            }
        }
        for (item in posList) item.totalCols = colEnds.size

        assertEquals(2, posList.size)
        assertEquals(2, posList[0].totalCols)
        assertEquals(2, posList[1].totalCols)
        assertEquals(0, posList[0].colIdx)
        assertEquals(1, posList[1].colIdx)
    }

    @Test
    fun testPosicionamientoCitas_CitasSecuenciales_NoDuplicanColumnas() {
        val cita1 = createCita(1, "Lucero", "2025-10-15T10:00:00", "2025-10-15T11:30:00")
        val cita2 = createCita(2, "Laura", "2025-10-15T11:30:00", "2025-10-15T13:00:00")

        val parseFecha = { str: String -> LocalDateTime.parse(str) }
        data class TestPosicion(val cita: Cita, val inicio: LocalDateTime, val fin: LocalDateTime, var colIdx: Int = 0, var totalCols: Int = 1)

        val posList = listOf(cita1, cita2).map { TestPosicion(it, parseFecha(it.fechaHoraInicio), parseFecha(it.fechaHoraFin)) }

        val colEnds = mutableListOf<LocalDateTime>()
        for (item in posList) {
            var asignado = false
            for (i in colEnds.indices) {
                if (!colEnds[i].isAfter(item.inicio)) {
                    item.colIdx = i
                    colEnds[i] = item.fin
                    asignado = true
                    break
                }
            }
            if (!asignado) {
                item.colIdx = colEnds.size
                colEnds.add(item.fin)
            }
        }
        for (item in posList) item.totalCols = colEnds.size

        assertEquals(0, posList[0].colIdx)
        assertEquals(0, posList[1].colIdx)
        assertEquals(1, posList[0].totalCols)
        assertEquals(1, posList[1].totalCols)
    }
}
