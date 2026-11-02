package com.spa.appointments.data.repository

import com.spa.appointments.domain.model.*
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class NominaJsonContractTest {

    private lateinit var moshi: Moshi

    @Before
    fun setup() {
        moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
    }

    @Test
    fun testParseoLiquidacionNominaResponse_ConCamposSnapshot() {
        val json = """
            {
                "id": 1,
                "idEmpresa": 1,
                "idContratoLaboral": 2,
                "idPeriodoNomina": 1,
                "totalDevengado": 1000000.0,
                "totalDeducciones": 72000.0,
                "netoPagar": 928000.0,
                "estado": 1,
                "nombreEstado": "Vigente",
                "fechaLiquidacion": "2026-10-27T19:43:31",
                "salarioBaseSnapshot": 2000000.0,
                "smmlvSnapshot": 1423500.0,
                "auxTransporteSnapshot": 200000.0,
                "idPreliquidacion": 2,
                "ibcCalculado": 1500000.0,
                "detalles": []
            }
        """.trimIndent()

        val adapter = moshi.adapter(LiquidacionNominaResponse::class.java)
        val result = adapter.fromJson(json)

        assertNotNull(result)
        assertEquals(1, result!!.id)
        assertEquals(1423500.0, result.smmlvSnapshot, 0.001)
        assertEquals(200000.0, result.auxTransporteSnapshot, 0.001)
        assertEquals(1500000.0, result.ibcCalculado!!, 0.001)
    }

    @Test
    fun testParseoPreliquidacionResponse_ConCamposExtensionFase2A() {
        val json = """
            {
                "idPreliquidacion": 2,
                "idContratoLaboral": 2,
                "idPeriodoNomina": 1,
                "totalDevengado": 1000000.0,
                "totalDeducciones": 72000.0,
                "netoPagar": 928000.0,
                "salarioBaseSnapshot": 2000000.0,
                "smmlvSnapshot": 1423500.0,
                "auxTransporteSnapshot": 200000.0,
                "ibcCalculado": 1500000.0,
                "mensaje": "Pre-liquidacion calculada y persistida exitosamente",
                "liquidable": true,
                "motivo": null,
                "profesionalNombre": "Lucero Bernal",
                "estadoEjecucion": "EXITOSO",
                "fechaEjecucion": "2026-10-29T22:53:48",
                "diasComerciales": 15.0,
                "vigente": true,
                "detalles": []
            }
        """.trimIndent()

        val adapter = moshi.adapter(PreliquidacionResponse::class.java)
        val result = adapter.fromJson(json)

        assertNotNull(result)
        assertEquals(2, result!!.idPreliquidacion)
        assertEquals(1423500.0, result.smmlvSnapshot, 0.001)
        assertEquals(200000.0, result.auxTransporteSnapshot, 0.001)
        assertEquals(1500000.0, result.ibcCalculado, 0.001)
        assertTrue(result.liquidable!!)
        assertEquals("Lucero Bernal", result.profesionalNombre)
    }

    @Test
    fun testParseoPeriodoNominaItem() {
        val json = """
            {
                "id": 1,
                "nombre": "Quincena 1 - Octubre 2026",
                "fechaInicio": "2026-10-01",
                "fechaFin": "2026-10-15",
                "estado": 1,
                "nombreEstado": "Abierto"
            }
        """.trimIndent()

        val adapter = moshi.adapter(PeriodoNominaItem::class.java)
        val result = adapter.fromJson(json)

        assertNotNull(result)
        assertEquals(1, result!!.id)
        assertEquals("Quincena 1 - Octubre 2026", result.nombre)
        assertEquals("Abierto", result.nombreEstado)
    }

    @Test
    fun testParseoContratoElegibleNomina() {
        val json = """
            {
                "idContratoLaboral": 2,
                "idProfesional": 2,
                "profesionalNombre": "Lucero Bernal",
                "profesionalDocumento": "1070942075",
                "cargo": "Esteticista",
                "idSede": 1,
                "tipoContrato": "Término Indefinido",
                "salarioBase": 2000000.0,
                "fechaInicio": "2026-01-01",
                "fechaFin": null,
                "idPreliquidacionVigente": 2,
                "netoPreliquidado": 928000.0,
                "motivoPreliquidacion": null,
                "idLiquidacionVigente": 1,
                "netoLiquidado": 928000.0
            }
        """.trimIndent()

        val adapter = moshi.adapter(ContratoElegibleNomina::class.java)
        val result = adapter.fromJson(json)

        assertNotNull(result)
        assertEquals(2, result!!.idContratoLaboral)
        assertEquals("Lucero Bernal", result.profesionalNombre)
        assertEquals(2000000.0, result.salarioBase, 0.001)
    }

    @Test
    fun testParseoPreliquidacionVigenteItem() {
        val json = """
            {
                "idPreliquidacion": 2,
                "idContratoLaboral": 2,
                "idPeriodoNomina": 1,
                "profesionalNombre": "Lucero Bernal",
                "profesionalDocumento": "1070942075",
                "fechaEjecucion": "2026-10-29T22:53:48",
                "diasComerciales": 15.0,
                "totalDevengado": 1000000.0,
                "totalDeducciones": 72000.0,
                "netoPagar": 928000.0,
                "ibcCalculado": 1500000.0,
                "salarioBaseSnapshot": 2000000.0,
                "smmlvSnapshot": 1423500.0,
                "auxTransporteSnapshot": 200000.0,
                "liquidable": true,
                "motivo": null
            }
        """.trimIndent()

        val adapter = moshi.adapter(PreliquidacionVigenteItem::class.java)
        val result = adapter.fromJson(json)

        assertNotNull(result)
        assertEquals(2, result!!.idPreliquidacion)
        assertTrue(result.liquidable)
        assertEquals(1423500.0, result.smmlvSnapshot, 0.001)
    }

    @Test
    fun testParseoPaginaLiquidaciones() {
        val json = """
            {
                "items": [
                    {
                        "id": 1,
                        "idContratoLaboral": 2,
                        "idPeriodoNomina": 1,
                        "periodoNombre": "Quincena 1 - Octubre 2026",
                        "periodoDesde": "2026-10-01",
                        "periodoHasta": "2026-10-15",
                        "profesionalNombre": "Lucero Bernal",
                        "profesionalDocumento": "1070942075",
                        "totalDevengado": 1000000.0,
                        "totalDeducciones": 72000.0,
                        "netoPagar": 928000.0,
                        "estado": 1,
                        "nombreEstado": "Vigente",
                        "fechaLiquidacion": "2026-10-27T19:43:31"
                    }
                ],
                "total": 1,
                "pagina": 1,
                "tamanoPagina": 20
            }
        """.trimIndent()

        val adapter = moshi.adapter(PaginaLiquidaciones::class.java)
        val result = adapter.fromJson(json)

        assertNotNull(result)
        assertEquals(1, result!!.total)
        assertEquals(1, result.items.size)
        assertEquals("Lucero Bernal", result.items[0].profesionalNombre)
    }
}
