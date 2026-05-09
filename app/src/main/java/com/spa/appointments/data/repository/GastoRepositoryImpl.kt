package com.spa.appointments.data.repository

import com.spa.appointments.data.remote.ApiService
import com.spa.appointments.domain.model.GastoRequest
import com.spa.appointments.domain.model.GastoResponse
import com.spa.appointments.domain.model.GastoResultado
import com.spa.appointments.domain.model.MetodoPago
import com.spa.appointments.domain.model.Sede
import com.spa.appointments.domain.repository.GastoRepository

// data/repository/GastoRepositoryImpl.kt
class GastoRepositoryImpl(
    private val api: ApiService
) : GastoRepository {

    override suspend fun registrarGasto(request: GastoRequest): Result<GastoResultado> {
        return try {
            val response = api.registrarGasto(request)
            if (response.isSuccessful)
                Result.success(response.body()!!)
            else
                Result.failure(Exception("Error ${response.code()}: ${response.message()}"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun listarGastos(
        idSede: Int?, fechaDesde: String?, fechaHasta: String?
    ): Result<List<GastoResponse>> {
        return try {
            val response = api.listarGastos(idSede, fechaDesde, fechaHasta)
            if (response.isSuccessful)
                Result.success(response.body() ?: emptyList())
            else
                Result.failure(Exception("Error ${response.code()}: ${response.message()}"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun eliminarGasto(id: Int): Result<GastoResultado> {
        return try {
            val response = api.eliminarGasto(id)
            if (response.isSuccessful)
                Result.success(response.body()!!)
            else
                Result.failure(Exception("Error ${response.code()}: ${response.message()}"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getMetodosPago(): Result<List<MetodoPago>> {
        return try {
            val response = api.getMetodosPago()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // AGREGAR al final de la clase, antes del último }
    override suspend fun getSedes(): Result<List<Sede>> {
        return try {
            val response = api.getSedes()
            if (response.isSuccessful)
                Result.success(response.body() ?: emptyList())
            else
                Result.failure(Exception("Error ${response.code()}: ${response.message()}"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}