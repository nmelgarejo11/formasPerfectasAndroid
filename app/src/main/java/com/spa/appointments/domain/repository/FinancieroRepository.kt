package com.spa.appointments.domain.repository

import com.spa.appointments.domain.model.CierreCajaResponse
import com.spa.appointments.domain.model.Sede
import java.time.LocalDate

interface FinancieroRepository {
    suspend fun getCierreCaja(idSede: Int, fecha: LocalDate?): CierreCajaResponse
    suspend fun getSedes(): List<Sede>
}