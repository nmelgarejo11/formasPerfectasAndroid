package com.spa.appointments.data.remote

import com.spa.appointments.domain.model.LoginRequest
import com.spa.appointments.domain.model.LoginResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {

    @POST("Auth/Login")
    suspend fun login(
        @Body request: LoginRequest
    ): LoginResponse

}
