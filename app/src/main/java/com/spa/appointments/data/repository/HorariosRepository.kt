package com.spa.appointments.data.repository

import com.spa.appointments.data.remote.ApiService
import com.spa.appointments.domain.model.*
import javax.inject.Inject

class HorariosRepository @Inject constructor(
    private val api: ApiService
) {
    suspend fun getHorario(idProfesional: Int): List<HorarioItem> =
        api.getHorario(idProfesional)

    suspend fun guardarHorario(idProfesional: Int, req: GuardarHorarioRequest) =
        api.guardarHorario(idProfesional, req)

    suspend fun copiarHorario(idProfesional: Int, req: CopiarHorarioRequest) =
        api.copiarHorario(idProfesional, req)

    suspend fun getBloqueos(idProfesional: Int): List<BloqueoResponse> =
        api.getBloqueos(idProfesional)

    suspend fun crearBloqueo(idProfesional: Int, req: BloqueoRequest) =
        api.crearBloqueo(idProfesional, req)

    suspend fun eliminarBloqueo(idProfesional: Int, id: Int) =
        api.eliminarBloqueo(idProfesional, id)
}