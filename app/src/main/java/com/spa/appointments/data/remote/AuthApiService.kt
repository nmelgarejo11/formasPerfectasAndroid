package com.spa.appointments.data.remote

import com.spa.appointments.domain.model.RefreshRequest
import com.spa.appointments.domain.model.RefreshResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {

    @POST("Auth/Refresh")
    suspend fun refresh(@Body request: RefreshRequest): RefreshResponse
}
