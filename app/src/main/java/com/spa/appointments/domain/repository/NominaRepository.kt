package com.spa.appointments.domain.repository

import com.spa.appointments.domain.model.*
import java.io.File

interface NominaRepository {
    suspend fun obtenerLiquidacionPorId(id: Int): Result<LiquidacionNominaResponse>
    suspend fun descargarDesprendiblePdf(id: Int): Result<File>
    suspend fun preliquidar(request: PreliquidarContratoRequest): Result<PreliquidacionResponse>
    suspend fun crearDesdePreliquidacion(request: CrearLiquidacionDesdePreliquidacionRequest): Result<RespuestaSimpleNomina>
    suspend fun anularLiquidacion(id: Int, request: AnularLiquidacionRequest): Result<RespuestaSimpleNomina>
}
