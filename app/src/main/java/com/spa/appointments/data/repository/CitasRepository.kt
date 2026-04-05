package com.spa.appointments.data.repository

import com.spa.appointments.data.remote.ApiService
import com.spa.appointments.domain.model.*
import javax.inject.Inject

class CitasRepository @Inject constructor(
    private val api: ApiService
) {
    // Servicios
    suspend fun getServicios(): List<Servicio> =
        api.getServicios()

    // Profesionales
    suspend fun getProfesionales(idSede: Int? = null): List<Profesional> =
        api.getProfesionales(idSede)

    suspend fun getServiciosPorProfesional(idProfesional: Int): List<Servicio> =
        api.getServiciosPorProfesional(idProfesional)

    // Disponibilidad
    suspend fun getDisponibilidad(
        idProfesional: Int,
        idSede: Int,
        fecha: String,
        duracion: Int
    ): List<SlotDisponible> =
        api.getDisponibilidad(idProfesional, idSede, fecha, duracion)

    // Citas
    suspend fun crearCita(request: CrearCitaRequest): CrearCitaResponse =
        api.crearCita(request)

    suspend fun getCitasActivas(idCliente: Int): List<Cita> =
        api.getCitasActivas(idCliente)

    suspend fun getCitasHistorial(idCliente: Int): List<Cita> =
        api.getCitasHistorial(idCliente)

    suspend fun cancelarCita(idCita: Int): AccionCitaResponse =
        api.cancelarCita(AccionCitaCancelarRequest(idCita))

    suspend fun reagendarCita(idCita: Int, motivo: String?): AccionCitaResponse =
        api.reagendarCita(AccionCitaReagendarRequest(idCita, motivo))
}