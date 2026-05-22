package com.spa.appointments.data.repository

import com.spa.appointments.data.remote.ApiService
import com.spa.appointments.domain.model.IngresoRequest
import com.spa.appointments.domain.model.IngresoResponse
import com.spa.appointments.domain.model.IngresoResultado
import com.spa.appointments.domain.model.MetodoPago
import com.spa.appointments.domain.model.Sede
import com.spa.appointments.domain.repository.IngresoRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IngresoRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : IngresoRepository {

    override suspend fun listarIngresos(
        idSede: Int?,
        fechaDesde: String?,
        fechaHasta: String?
    ): Result<List<IngresoResponse>> = runCatching {
        apiService.listarIngresos(idSede, fechaDesde, fechaHasta)
    }

    override suspend fun registrarIngreso(request: IngresoRequest): Result<IngresoResultado> = runCatching {
        apiService.registrarIngreso(request)
    }

    override suspend fun eliminarIngreso(id: Int): Result<IngresoResultado> = runCatching {
        apiService.eliminarIngreso(id)
    }

    override suspend fun getSedes(): Result<List<Sede>> {
        return try {
            val response = apiService.getSedes()
            if (response.isSuccessful)
                Result.success(response.body() ?: emptyList())
            else
                Result.failure(Exception("Error ${response.code()}: ${response.message()}"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getMetodosPago(): Result<List<MetodoPago>> = runCatching {
        apiService.getMetodosPago()
    }
}