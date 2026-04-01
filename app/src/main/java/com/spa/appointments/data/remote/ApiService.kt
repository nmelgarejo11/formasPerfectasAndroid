package com.spa.appointments.data.remote

import com.spa.appointments.domain.model.LoginRequest
import com.spa.appointments.domain.model.LoginResponse
import com.spa.appointments.domain.model.MenuResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {

    @POST("Auth/Login")
    suspend fun login(
        @Body request: LoginRequest
    ): LoginResponse

    @GET("menu/menu")
    suspend fun obtenerMenu(): MenuResponse

}
