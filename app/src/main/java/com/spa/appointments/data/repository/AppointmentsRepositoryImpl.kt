package com.spa.appointments.data.repository

import com.spa.appointments.domain.repository.AppointmentsRepository

class AppointmentsRepositoryImpl : AppointmentsRepository {
    override fun testRepository(): String {
        return "Repository funcionando correctamente"
    }
}
