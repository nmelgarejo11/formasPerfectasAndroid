package com.spa.appointments.data.repository

import com.spa.appointments.core.utils.runCatchingSuspend
import com.spa.appointments.data.remote.ApiService
import com.spa.appointments.data.remote.NominaErrorMapper
import com.spa.appointments.domain.model.*
import com.spa.appointments.domain.repository.CatalogosRepository
import com.squareup.moshi.Moshi
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CatalogosRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val moshi: Moshi
) : CatalogosRepository {

    override suspend fun getBancos(): Result<List<BancoItem>> = runCatchingSuspend {
        procesarRespuesta(apiService.getBancos())
    }

    override suspend fun getTiposDocumento(): Result<List<TipoDocumentoItem>> = runCatchingSuspend {
        procesarRespuesta(apiService.getTiposDocumento())
    }

    override suspend fun getTiposCuenta(): Result<List<TipoCuentaItem>> = runCatchingSuspend {
        procesarRespuesta(apiService.getTiposCuenta())
    }

    private fun <T> procesarRespuesta(response: Response<T>): T {
        if (response.isSuccessful) {
            return response.body() ?: throw NominaApiException(response.code(), null, "Respuesta vacía del servidor")
        }
        throw NominaErrorMapper.parsearError(response, moshi)
    }
}
