package com.spa.appointments.ui.nomina

import com.spa.appointments.domain.model.ContratoElegibleNomina
import org.junit.Assert.*
import org.junit.Test

class NominaUiModelsTest {

    @Test
    fun testEstadoNominaContrato_PrecedenciaLiquidada() {
        val contrato = ContratoElegibleNomina(
            idContratoLaboral = 1,
            idProfesional = 2,
            profesionalNombre = "Test",
            profesionalDocumento = "123",
            cargo = "Cargo",
            idSede = 1,
            tipoContrato = "Indefinido",
            salarioBase = 1000000.0,
            fechaInicio = "2026-01-01",
            fechaFin = null,
            idPreliquidacionVigente = 2,
            netoPreliquidado = 900000.0,
            motivoPreliquidacion = "YA_LIQUIDADA",
            idLiquidacionVigente = 5,
            netoLiquidado = 900000.0
        )

        val estado = EstadoNominaContrato.desde(contrato)
        assertEquals(EstadoNominaContrato.Liquidada, estado)
    }

    @Test
    fun testEstadoNominaContrato_SinPreliquidar() {
        val contrato = ContratoElegibleNomina(
            idContratoLaboral = 1,
            idProfesional = 2,
            profesionalNombre = "Test",
            profesionalDocumento = "123",
            cargo = "Cargo",
            idSede = 1,
            tipoContrato = "Indefinido",
            salarioBase = 1000000.0,
            fechaInicio = "2026-01-01",
            fechaFin = null,
            idPreliquidacionVigente = null,
            netoPreliquidado = null,
            motivoPreliquidacion = null,
            idLiquidacionVigente = null,
            netoLiquidado = null
        )

        val estado = EstadoNominaContrato.desde(contrato)
        assertEquals(EstadoNominaContrato.SinPreliquidar, estado)
    }

    @Test
    fun testEstadoNominaContrato_PreliquidadaLista() {
        val contrato = ContratoElegibleNomina(
            idContratoLaboral = 1,
            idProfesional = 2,
            profesionalNombre = "Test",
            profesionalDocumento = "123",
            cargo = "Cargo",
            idSede = 1,
            tipoContrato = "Indefinido",
            salarioBase = 1000000.0,
            fechaInicio = "2026-01-01",
            fechaFin = null,
            idPreliquidacionVigente = 2,
            netoPreliquidado = 900000.0,
            motivoPreliquidacion = null,
            idLiquidacionVigente = null,
            netoLiquidado = null
        )

        val estado = EstadoNominaContrato.desde(contrato)
        assertEquals(EstadoNominaContrato.PreliquidadaLista, estado)
    }

    @Test
    fun testEstadoNominaContrato_PreliquidacionDesactualizada() {
        val contrato = ContratoElegibleNomina(
            idContratoLaboral = 1,
            idProfesional = 2,
            profesionalNombre = "Test",
            profesionalDocumento = "123",
            cargo = "Cargo",
            idSede = 1,
            tipoContrato = "Indefinido",
            salarioBase = 1000000.0,
            fechaInicio = "2026-01-01",
            fechaFin = null,
            idPreliquidacionVigente = 2,
            netoPreliquidado = 900000.0,
            motivoPreliquidacion = "SALARIO_CAMBIO",
            idLiquidacionVigente = null,
            netoLiquidado = null
        )

        val estado = EstadoNominaContrato.desde(contrato)
        assertEquals(EstadoNominaContrato.PreliquidacionDesactualizada("SALARIO_CAMBIO"), estado)
    }

    @Test
    fun testTextoMotivoNomina_TodosLosCasos() {
        assertEquals("Cambió el salario del contrato", textoMotivoNomina("SALARIO_CAMBIO"))
        assertEquals("Hay novedades nuevas en el periodo", textoMotivoNomina("NOVEDAD_NUEVA"))
        assertEquals("Se modificaron o inactivaron novedades", textoMotivoNomina("NOVEDAD_MODIFICADA"))
        assertEquals("Cambiaron los préstamos o sus saldos", textoMotivoNomina("PRESTAMO_CAMBIO"))
        assertEquals("Existe una preliquidación más reciente", textoMotivoNomina("NO_VIGENTE"))
        assertEquals("La preliquidación no fue exitosa", textoMotivoNomina("NO_EXITOSA"))
        assertEquals("El periodo no está abierto", textoMotivoNomina("PERIODO_NO_ABIERTO"))
        assertEquals("El contrato no está activo", textoMotivoNomina("CONTRATO_INACTIVO"))
        assertEquals("Ya está liquidada", textoMotivoNomina("YA_LIQUIDADA"))
        assertEquals("Los datos de origen cambiaron", textoMotivoNomina("OTRO_MOTIVO_DESCONOCIDO"))
        assertEquals("Los datos de origen cambiaron", textoMotivoNomina(null))
    }

    @Test
    fun testFormatearCop() {
        val formatted = formatearCop(1800000.0)
        assertTrue(formatted.contains("1.800.000") || formatted.contains("1,800,000"))
        assertEquals("$ 0", formatearCop(null))
    }

    @Test
    fun testFormatearFechaDisplay() {
        assertEquals("01/10/2026", formatearFechaDisplay("2026-10-01"))
        assertEquals("15/10/2026", formatearFechaDisplay("2026-10-15T12:30:00"))
        assertEquals("fecha_invalida", formatearFechaDisplay("fecha_invalida"))
        assertEquals("-", formatearFechaDisplay(null))
    }
}
