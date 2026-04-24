package com.spa.appointments.data.repository

import com.spa.appointments.data.remote.ApiService
import com.spa.appointments.domain.model.LoginRequest
import com.spa.appointments.domain.model.LoginResponse
import com.spa.appointments.domain.model.FcmTokenRequest
import com.spa.appointments.domain.repository.AuthRepository
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val api: ApiService
) : AuthRepository {

    override suspend fun login(user: String, pass: String): LoginResponse =
        api.login(LoginRequest(usuario = user, clave = pass))

    override suspend fun registrarFcmToken(request: FcmTokenRequest) =
        api.registrarFcmToken(request)
}