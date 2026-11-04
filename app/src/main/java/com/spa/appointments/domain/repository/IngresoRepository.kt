package com.spa.appointments.domain.repository

import com.spa.appointments.domain.model.IngresoRequest
import com.spa.appointments.domain.model.IngresoResponse
import com.spa.appointments.domain.model.IngresoResultado
import com.spa.appointments.domain.model.MetodoPago
import com.spa.appointments.domain.model.Sede

interface IngresoRepository {
    suspend fun listarIngresos(idSede: Int?, fechaDesde: String?, fechaHasta: String?): Result<List<IngresoResponse>>
    suspend fun registrarIngreso(request: IngresoRequest): Result<IngresoResultado>
    suspend fun eliminarIngreso(id: Int): Result<IngresoResultado>
    suspend fun getSedes(): Result<List<Sede>>
    suspend fun getMetodosPago(): Result<List<MetodoPago>>
}