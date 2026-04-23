package com.spa.appointments.data.repository

import com.spa.appointments.data.remote.ApiService
import com.spa.appointments.domain.model.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import javax.inject.Inject

class ProfesionalesAdminRepository @Inject constructor(
    private val api: ApiService
) {
    suspend fun getProfesionales(): List<ProfesionalAdmin> =
        api.getProfesionalesAdmin()

    suspend fun crearProfesional(req: ProfesionalRequest): Int =
        api.crearProfesional(req).id

    suspend fun editarProfesional(id: Int, req: ProfesionalRequest) =
        api.editarProfesional(id, req)

    suspend fun toggleEstado(id: Int): Boolean =
        api.toggleEstadoProfesional(id).estado

    suspend fun subirFoto(id: Int, archivo: File): String {
        val body = archivo.asRequestBody("image/*".toMediaTypeOrNull())
        val part = MultipartBody.Part.createFormData("foto", archivo.name, body)
        return api.subirFotoProfesional(id, part).fotoUrl
    }

    suspend fun getCargos(): List<CargoAdmin> =
        api.getCargos()

    suspend fun getSedesEmpresa(): List<SedeAdmin> =
        api.getSedesEmpresa()

    suspend fun getSedesProfesional(id: Int): List<Int> =
        api.getSedesProfesional(id)

    suspend fun guardarSedes(id: Int, idsSedes: List<Int>) =
        api.guardarSedesProfesional(id, GuardarSedesRequest(idsSedes))

    suspend fun getServiciosProfesional(id: Int): List<ServicioProfesionalResponse> =
        api.getServiciosProfesional(id)

    suspend fun guardarServicios(id: Int, servicios: List<ServicioProfesionalItem>) =
        api.guardarServiciosProfesional(id, GuardarServiciosRequest(servicios))
}