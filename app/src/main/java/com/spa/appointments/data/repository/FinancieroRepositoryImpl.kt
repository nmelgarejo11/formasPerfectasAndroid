package com.spa.appointments.data.repository

import com.spa.appointments.data.remote.ApiService
import com.spa.appointments.domain.model.CierreCajaResponse
import com.spa.appointments.domain.model.Sede
import com.spa.appointments.domain.repository.FinancieroRepository
import retrofit2.Response
import java.time.LocalDate
import javax.inject.Inject

class FinancieroRepositoryImpl @Inject constructor(
    private val api: ApiService
) : FinancieroRepository {

    override suspend fun getCierreCaja(idSede: Int, fecha: LocalDate?): CierreCajaResponse {
        return api.getCierreCaja(
            idSede = idSede,
            fecha  = fecha?.toString() // "2025-07-10"
        )
    }

    override suspend fun getSedes(): List<Sede> {
        val response = api.getSedes()
        return if (response.isSuccessful) response.body() ?: emptyList()
        else emptyList()
    }
}