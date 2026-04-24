package com.spa.appointments.domain.repository

import com.spa.appointments.domain.model.LoginResponse
import com.spa.appointments.domain.model.FcmTokenRequest

interface AuthRepository {
    suspend fun login(user: String, pass: String): LoginResponse
    suspend fun registrarFcmToken(request: FcmTokenRequest)
}