package com.spa.appointments.data.repository

import com.spa.appointments.data.remote.ApiService
import com.spa.appointments.domain.model.*
import javax.inject.Inject

class FinancieroRepository @Inject constructor(
    private val api: ApiService
) {
    suspend fun getResumen(
        fechaDesde: String,
        fechaHasta: String
    ): ResumenFinanciero = api.getResumenFinanciero(fechaDesde, fechaHasta)

    suspend fun getIngresosPorDia(
        fechaDesde: String,
        fechaHasta: String
    ): List<IngresoDia> = api.getIngresosPorDia(fechaDesde, fechaHasta)

    suspend fun getIngresosPorMes(anio: Int): List<IngresoMes> =
        api.getIngresosPorMes(anio)

    suspend fun getServiciosVendidos(
        fechaDesde: String,
        fechaHasta: String,
        top: Int = 5
    ): List<ServicioVendido> = api.getServiciosVendidos(fechaDesde, fechaHasta, top)

    suspend fun getProfesionalesRanking(
        fechaDesde: String,
        fechaHasta: String,
        top: Int = 5
    ): List<ProfesionalRanking> = api.getProfesionalesRanking(fechaDesde, fechaHasta, top)
}