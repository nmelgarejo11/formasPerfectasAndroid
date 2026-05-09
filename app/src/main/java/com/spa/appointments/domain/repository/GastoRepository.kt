package com.spa.appointments.domain.repository

import com.spa.appointments.domain.model.GastoRequest
import com.spa.appointments.domain.model.GastoResponse
import com.spa.appointments.domain.model.GastoResultado
import com.spa.appointments.domain.model.MetodoPago
import com.spa.appointments.domain.model.Sede

interface GastoRepository {
    suspend fun registrarGasto(request: GastoRequest): Result<GastoResultado>
    suspend fun listarGastos(idSede: Int?, fechaDesde: String?, fechaHasta: String?): Result<List<GastoResponse>>
    suspend fun eliminarGasto(id: Int): Result<GastoResultado>
    suspend fun getMetodosPago(): Result<List<MetodoPago>>
    suspend fun getSedes(): Result<List<Sede>>
}