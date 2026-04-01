package com.spa.appointments.data.repository

import com.spa.appointments.data.remote.ApiService
import com.spa.appointments.domain.model.MenuResponse
import javax.inject.Inject

class MenuRepository @Inject constructor(
    private val api: ApiService
    // Ya no necesitamos Moshi aquí
) {
    suspend fun obtenerMenu(): MenuResponse {
        return api.obtenerMenu()
    }
}