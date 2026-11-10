package com.spa.appointments.domain.repository

import com.spa.appointments.domain.model.CierreCajaResponse
import com.spa.appointments.domain.model.GastoResponse
import com.spa.appointments.domain.model.IngresoResponse
import com.spa.appointments.domain.model.IngresosVsGastosResponse
import com.spa.appointments.domain.model.MetodoPago
import com.spa.appointments.domain.model.Profesional
import com.spa.appointments.domain.model.Sede
import java.time.LocalDate

interface FinancieroRepository {
    suspend fun getCierreCaja(idSede: Int, fecha: LocalDate?): CierreCajaResponse
    suspend fun getSedes(): List<Sede>
    suspend fun getIngresosVsGastos(
        idSede      : Int,
        fechaInicio : LocalDate,
        fechaFin    : LocalDate
    ): IngresosVsGastosResponse
    suspend fun exportarIngresosVsGastosExcel(
        idSede      : Int,
        fechaInicio : String,
        fechaFin    : String
    ): ByteArray

    suspend fun listarIngresos(idSede: Int?, fechaDesde: String?, fechaHasta: String?): List<IngresoResponse>
    suspend fun listarGastos(idSede: Int?, fechaDesde: String?, fechaHasta: String?): List<GastoResponse>
    suspend fun getMetodosPago(): List<MetodoPago>
    suspend fun getProfesionales(idSede: Int?): List<Profesional>
}
