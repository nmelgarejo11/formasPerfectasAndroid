package com.spa.appointments.data.repository

import com.spa.appointments.data.remote.ApiService
import com.spa.appointments.data.remote.TemaEmpresaRequest
import com.spa.appointments.domain.model.TemaEmpresa
import com.spa.appointments.domain.repository.TemaRepository
import javax.inject.Inject

class TemaRepositoryImpl @Inject constructor(
    private val api: ApiService
) : TemaRepository {

    override suspend fun obtenerTema(): Result<TemaEmpresa> = runCatching {
        val response = api.getTema()
        android.util.Log.d("TemaRepo", "Code: ${response.code()}")
        android.util.Log.d("TemaRepo", "Body: ${response.body()}")
        android.util.Log.d("TemaRepo", "Error: ${response.errorBody()?.string()}")
        response.body() ?: error("Respuesta vacía")
    }

    override suspend fun actualizarTema(request: TemaEmpresaRequest): Result<Unit> = runCatching {
        val response = api.actualizarTema(request)
        if (!response.isSuccessful) error("Error ${response.code()}")
    }
}