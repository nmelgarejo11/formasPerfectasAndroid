package com.spa.appointments.domain.repository

import com.spa.appointments.data.remote.TemaEmpresaRequest
import com.spa.appointments.domain.model.TemaEmpresa


interface TemaRepository {
    suspend fun obtenerTema(): Result<TemaEmpresa>
    suspend fun actualizarTema(request: TemaEmpresaRequest): Result<Unit>
}