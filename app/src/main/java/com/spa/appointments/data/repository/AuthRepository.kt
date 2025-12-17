package com.spa.appointments.data.repository

import com.spa.appointments.data.remote.ApiService
import com.spa.appointments.domain.model.LoginRequest
import javax.inject.Inject

class AuthRepository @Inject constructor(
    private val api: ApiService
) {

    suspend fun login(user: String, pass: String) =
        api.login(
            LoginRequest(
                usuario = user,
                clave = pass
            )
        )
}
