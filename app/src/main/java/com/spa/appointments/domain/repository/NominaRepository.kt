package com.spa.appointments.domain.repository

import com.spa.appointments.domain.model.*
import java.io.File

interface NominaRepository {
    suspend fun obtenerLiquidacionPorId(id: Int): Result<LiquidacionNominaResponse>
    suspend fun descargarDesprendiblePdf(id: Int): Result<File>
    suspend fun preliquidar(request: PreliquidarContratoRequest): Result<PreliquidacionResponse>
    suspend fun crearDesdePreliquidacion(request: CrearLiquidacionDesdePreliquidacionRequest): Result<RespuestaSimpleNomina>
    suspend fun anularLiquidacion(id: Int, request: AnularLiquidacionRequest): Result<RespuestaSimpleNomina>

    // ── Métodos Fase 2A ─────────────────────────────────────────
    suspend fun listarPeriodosNomina(soloAbiertos: Boolean = true): Result<List<PeriodoNominaItem>>
    suspend fun listarContratosElegiblesNomina(idPeriodoNomina: Int, idSede: Int? = null, busqueda: String? = null): Result<List<ContratoElegibleNomina>>
    suspend fun listarPreliquidacionesVigentesNomina(idPeriodoNomina: Int): Result<List<PreliquidacionVigenteItem>>
    suspend fun obtenerPreliquidacionPorId(id: Int): Result<PreliquidacionResponse>
    suspend fun listarLiquidacionesNomina(idPeriodoNomina: Int? = null, idContratoLaboral: Int? = null, estado: Int? = null, pagina: Int = 1, tamanoPagina: Int = 20): Result<PaginaLiquidaciones>
}
