package com.spa.appointments.data.repository

import com.spa.appointments.data.remote.ApiService
import com.spa.appointments.domain.model.TemaEmpresa
import javax.inject.Inject

class TemaRepository @Inject constructor(
    private val api: ApiService
) {
    suspend fun getTema(): TemaEmpresa = api.getTemaEmpresa()
}