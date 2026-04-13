package com.spa.appointments.data.repository

import com.spa.appointments.data.remote.ApiService
import com.spa.appointments.domain.model.ContactoSoporte
import javax.inject.Inject

class ContactoRepository @Inject constructor(
    private val api: ApiService
) {
    suspend fun getContacto(): ContactoSoporte =
        api.getContactoSoporte()
}