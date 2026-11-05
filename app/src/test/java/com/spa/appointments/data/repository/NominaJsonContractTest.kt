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
                "id": 5,
                "idEmpresa": 1,
                "idContratoLaboral": 101,
                "idPeriodoNomina": 10,
                "totalDevengado": 2000000.0,
                "totalDeducciones": 200000.0,
                "netoPagar": 1800000.0,
                "estado": 1,
                "nombreEstado": "Vigente",
                "fechaLiquidacion": "2026-10-16T00:00:00",
                "salarioBaseSnapshot": 2000000.0,
                "smmlvSnapshot": 1423500.0,
                "auxTransporteSnapshot": 200000.0,
                "idPreliquidacion": 12,
                "ibcCalculado": 2000000.0,
                "detalles": []
            }
        """.trimIndent()

        val adapter = moshi.adapter(LiquidacionNominaResponse::class.java)
        val result = adapter.fromJson(json)

        assertNotNull(result)
        assertEquals(5, result!!.id)
        assertEquals(1423500.0, result.smmlvSnapshot, 0.001)
        assertEquals(200000.0, result.auxTransporteSnapshot, 0.001)
        assertEquals(2000000.0, result.ibcCalculado!!, 0.001)
    }

    @Test
    fun testParseoPreliquidacionResponse_ConCamposExtensionFase2A() {
        val json = """
            {
                "idPreliquidacion": 12,
                "idContratoLaboral": 101,
                "idPeriodoNomina": 10,
                "totalDevengado": 2000000.0,
                "totalDeducciones": 200000.0,
                "netoPagar": 1800000.0,
                "salarioBaseSnapshot": 2000000.0,
                "smmlvSnapshot": 1423500.0,
                "auxTransporteSnapshot": 200000.0,
                "ibcCalculado": 2000000.0,
                "mensaje": "Preliquidado con éxito",
                "liquidable": true,
                "motivo": null,
                "profesionalNombre": "Juan Perez Dummy",
                "estadoEjecucion": "EXITOSO",
                "fechaEjecucion": "2026-10-15T12:00:00",
                "diasComerciales": 15.0,
                "vigente": true,
                "detalles": []
            }
        """.trimIndent()

        val adapter = moshi.adapter(PreliquidacionResponse::class.java)
        val result = adapter.fromJson(json)

        assertNotNull(result)
        assertEquals(12, result!!.idPreliquidacion)
        assertEquals(1423500.0, result.smmlvSnapshot, 0.001)
        assertEquals(200000.0, result.auxTransporteSnapshot, 0.001)
        assertEquals(2000000.0, result.ibcCalculado, 0.001)
        assertTrue(result.liquidable!!)
        assertEquals("Juan Perez Dummy", result.profesionalNombre)
    }

    @Test
    fun testParseoPeriodoNominaItem() {
        val json = """
            {
                "id": 10,
                "nombre": "Periodo Dummy 2026",
                "fechaInicio": "2026-10-01",
                "fechaFin": "2026-10-15",
                "estado": 1,
                "nombreEstado": "Abierto"
            }
        """.trimIndent()

        val adapter = moshi.adapter(PeriodoNominaItem::class.java)
        val result = adapter.fromJson(json)

        assertNotNull(result)
        assertEquals(10, result!!.id)
        assertEquals("Periodo Dummy 2026", result.nombre)
        assertEquals("2026-10-01", result.fechaInicio)
        assertEquals("2026-10-15", result.fechaFin)
        assertEquals("Abierto", result.nombreEstado)
    }

    @Test
    fun testParseoContratoElegibleNomina() {
        val json = """
            {
                "idContratoLaboral": 101,
                "idProfesional": 201,
                "profesionalNombre": "Juan Perez Dummy",
                "profesionalDocumento": "12345678",
                "cargo": "Especialista Dummy",
                "idSede": 1,
                "tipoContrato": "Término Indefinido",
                "salarioBase": 2000000.0,
                "fechaInicio": "2026-01-01T00:00:00",
                "fechaFin": null,
                "idPreliquidacionVigente": 12,
                "netoPreliquidado": 1800000.0,
                "motivoPreliquidacion": null,
                "idLiquidacionVigente": 5,
                "netoLiquidado": 1800000.0
            }
        """.trimIndent()

        val adapter = moshi.adapter(ContratoElegibleNomina::class.java)
        val result = adapter.fromJson(json)

        assertNotNull(result)
        assertEquals(101, result!!.idContratoLaboral)
        assertEquals("Juan Perez Dummy", result.profesionalNombre)
        assertEquals(2000000.0, result.salarioBase, 0.001)
    }

    @Test
    fun testParseoPreliquidacionVigenteItem() {
        val json = """
            {
                "idPreliquidacion": 12,
                "idContratoLaboral": 101,
                "idPeriodoNomina": 10,
                "profesionalNombre": "Juan Perez Dummy",
                "profesionalDocumento": "12345678",
                "fechaEjecucion": "2026-10-15T12:00:00",
                "diasComerciales": 15.0,
                "totalDevengado": 2000000.0,
                "totalDeducciones": 200000.0,
                "netoPagar": 1800000.0,
                "ibcCalculado": 2000000.0,
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
        assertEquals(12, result!!.idPreliquidacion)
        assertTrue(result.liquidable)
        assertEquals(1423500.0, result.smmlvSnapshot, 0.001)
    }

    @Test
    fun testParseoPaginaLiquidaciones() {
        val json = """
            {
                "items": [
                    {
                        "id": 5,
                        "idContratoLaboral": 101,
                        "idPeriodoNomina": 10,
                        "periodoNombre": "Periodo Dummy 2026",
                        "periodoDesde": "2026-10-01T00:00:00",
                        "periodoHasta": "2026-10-15T00:00:00",
                        "profesionalNombre": "Juan Perez Dummy",
                        "profesionalDocumento": "12345678",
                        "totalDevengado": 2000000.0,
                        "totalDeducciones": 200000.0,
                        "netoPagar": 1800000.0,
                        "estado": 1,
                        "nombreEstado": "Vigente",
                        "fechaLiquidacion": "2026-10-16T00:00:00"
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
        assertEquals("Juan Perez Dummy", result.items[0].profesionalNombre)
    }

    @Test
    fun testParseoNovedadNominaResponse() {
        val json = """
            {
                "id": 1,
                "idEmpresa": 1,
                "idContratoLaboral": 101,
                "idConcepto": 5,
                "nombreConcepto": "Bonificación Especial",
                "fechaNovedad": "2026-10-05",
                "valor": 150000.0,
                "dias": null,
                "observaciones": "Novedad de prueba",
                "estado": true,
                "fechaCreacion": "2026-10-05T10:00:00"
            }
        """.trimIndent()

        val adapter = moshi.adapter(NovedadNominaResponse::class.java)
        val result = adapter.fromJson(json)

        assertNotNull(result)
        assertEquals(1, result!!.id)
        assertEquals(101, result.idContratoLaboral)
        assertEquals("Bonificación Especial", result.nombreConcepto)
        assertEquals(150000.0, result.valor, 0.001)
        assertTrue(result.estado)
    }

    @Test
    fun testParseoConceptoResponse() {
        val json = """
            {
                "id": 5,
                "codigo": "BON01",
                "nombre": "Bonificación",
                "tipoConcepto": 1,
                "aplicaSalarioBase": false,
                "descuentaDiasSueldo": true
            }
        """.trimIndent()

        val adapter = moshi.adapter(ConceptoResponse::class.java)
        val result = adapter.fromJson(json)

        assertNotNull(result)
        assertEquals(5, result!!.id)
        assertEquals("BON01", result.codigo)
        assertEquals(1, result.tipoConcepto)
        assertFalse(result.aplicaSalarioBase)
        assertTrue(result.descuentaDiasSueldo)
    }

    @Test
    fun testSerializacionBoolBodyPatch() {
        val adapter = moshi.adapter(Boolean::class.javaObjectType)
        assertEquals("true", adapter.toJson(true))
        assertEquals("false", adapter.toJson(false))
    }
}
