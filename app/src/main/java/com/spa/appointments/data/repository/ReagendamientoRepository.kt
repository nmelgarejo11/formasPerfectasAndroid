package com.spa.appointments.data.repository

import com.spa.appointments.data.remote.ApiService
import com.spa.appointments.domain.model.AprobarReagendamientoRequest
import com.spa.appointments.domain.model.CitaPendiente
import com.spa.appointments.domain.model.RechazarReagendamientoRequest
import javax.inject.Inject

class ReagendamientoRepository @Inject constructor(
    private val api: ApiService
) {
    suspend fun getPendientes(): List<CitaPendiente> =
        api.getPendientesReagendamiento()

    suspend fun aprobar(id: Int, request: AprobarReagendamientoRequest) =
        api.aprobarReagendamiento(id, request)

    suspend fun rechazar(id: Int, request: RechazarReagendamientoRequest) =
        api.rechazarReagendamiento(id, request)
}