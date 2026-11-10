package com.spa.appointments.data.repository

import com.spa.appointments.data.remote.ApiService
import com.spa.appointments.domain.model.CierreCajaResponse
import com.spa.appointments.domain.model.GastoResponse
import com.spa.appointments.domain.model.IngresoResponse
import com.spa.appointments.domain.model.IngresosVsGastosResponse
import com.spa.appointments.domain.model.MetodoPago
import com.spa.appointments.domain.model.Profesional
import com.spa.appointments.domain.model.Sede
import com.spa.appointments.domain.repository.FinancieroRepository
import java.time.LocalDate
import javax.inject.Inject

class FinancieroRepositoryImpl @Inject constructor(
    private val api: ApiService
) : FinancieroRepository {

    override suspend fun getCierreCaja(idSede: Int, fecha: LocalDate?): CierreCajaResponse {
        return api.getCierreCaja(
            idSede = idSede,
            fecha  = fecha?.toString()
        )
    }

    override suspend fun getSedes(): List<Sede> {
        val response = api.getSedes()
        return if (response.isSuccessful) response.body() ?: emptyList()
        else emptyList()
    }

    override suspend fun getIngresosVsGastos(
        idSede      : Int,
        fechaInicio : LocalDate,
        fechaFin    : LocalDate
    ): IngresosVsGastosResponse {
        return api.getIngresosVsGastos(
            idSede      = idSede,
            fechaInicio = fechaInicio.toString(),
            fechaFin    = fechaFin.toString()
        )
    }

    override suspend fun exportarIngresosVsGastosExcel(
        idSede      : Int,
        fechaInicio : String,
        fechaFin    : String
    ): ByteArray = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        val response = api.exportarIngresosVsGastosExcel(
            idSede      = idSede,
            fechaInicio = fechaInicio,
            fechaFin    = fechaFin
        )
        if (!response.isSuccessful)
            throw Exception("Error ${response.code()}")

        response.body()?.bytes()
            ?: throw Exception("Respuesta vacía del servidor")
    }

    override suspend fun listarIngresos(
        idSede: Int?,
        fechaDesde: String?,
        fechaHasta: String?
    ): List<IngresoResponse> {
        return runCatching { api.listarIngresos(idSede, fechaDesde, fechaHasta) }.getOrDefault(emptyList())
    }

    override suspend fun listarGastos(
        idSede: Int?,
        fechaDesde: String?,
        fechaHasta: String?
    ): List<GastoResponse> {
        return runCatching {
            val res = api.listarGastos(idSede, fechaDesde, fechaHasta)
            if (res.isSuccessful) res.body() ?: emptyList() else emptyList()
        }.getOrDefault(emptyList())
    }

    override suspend fun getMetodosPago(): List<MetodoPago> {
        return runCatching { api.getMetodosPago() }.getOrDefault(emptyList())
    }

    override suspend fun getProfesionales(idSede: Int?): List<Profesional> {
        return runCatching { api.getProfesionales(idSede = idSede, idServicio = null) }.getOrDefault(emptyList())
    }
}
