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
    suspend fun getProfesionales(idSede: Int? = null, idServicio: Int? = null): List<Profesional> =
        api.getProfesionales(idSede, idServicio)

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

    suspend fun getCitasActivas(
        nombreCliente:  String? = null,
        fechaDesde:     String? = null,
        fechaHasta:     String? = null,
        idProfesional:  Int?    = null,
        idEstado:       Int?    = null
    ): List<Cita> = api.getCitasActivas(nombreCliente, fechaDesde, fechaHasta, idProfesional, idEstado)

    suspend fun getCitasHistorial(
        nombreCliente:  String? = null,
        fechaDesde:     String? = null,
        fechaHasta:     String? = null,
        idProfesional:  Int?    = null,
        idEstado:       Int?    = null
    ): List<Cita> = api.getCitasHistorial(nombreCliente, fechaDesde, fechaHasta, idProfesional, idEstado)

    suspend fun cancelarCita(idCita: Int): AccionCitaResponse =
        api.cancelarCita(AccionCitaCancelarRequest(idCita))

    suspend fun reagendarCita(idCita: Int, motivo: String?): AccionCitaResponse =
        api.reagendarCita(AccionCitaReagendarRequest(idCita, motivo))

    // data/repository/CitasRepository.kt  (agregar al final de la clase existente)

    suspend fun getMetodosPago(): List<MetodoPago> =
        api.getMetodosPago()

    suspend fun finalizarCita(idCita: Int, idMetodoPago: Int, idMetodoPagoDetalle: Int?): AccionCitaResponse {
        val resp = api.finalizarCita(FinalizarCitaRequest(idCita, idMetodoPago, idMetodoPagoDetalle))
        return AccionCitaResponse(ok = resp.ok, mensaje = resp.mensaje)
    }

    suspend fun getEstadosCita(grupo: String? = null): List<EstadoCita> =
        api.getEstadosCita(grupo)

    suspend fun getWhatsAppCita(idCita: Int): WhatsAppCitaInfo {
        val response = api.getWhatsAppCita(idCita)
        if (!response.isSuccessful)
            throw Exception("Error al obtener datos de WhatsApp")
        val body = response.body()
            ?: throw Exception("Respuesta vacía")
        if (!body.ok)
            throw Exception(body.mensaje ?: "El cliente no tiene teléfono registrado")
        val data = body.data
            ?: throw Exception("Sin datos de WhatsApp")
        return data
    }

    suspend fun asignarCitaGrupal(
        idCita: Int,
        request: AsignarCitaGrupalRequest
    ): AccionCitaResponse {
        return api.asignarCitaGrupal(idCita, request)
    }
}