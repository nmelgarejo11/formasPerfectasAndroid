package com.spa.appointments.data.repository

import com.spa.appointments.data.remote.ApiService
import com.spa.appointments.domain.model.LicenciaResponse
import javax.inject.Inject

class LicenciaRepository @Inject constructor(
    private val api: ApiService
) {
    suspend fun validarLicencia(): LicenciaResponse =
        api.validarLicencia()
}