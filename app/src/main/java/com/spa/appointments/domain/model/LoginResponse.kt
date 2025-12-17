package com.spa.appointments.domain.model
import com.squareup.moshi.JsonClass


@JsonClass(generateAdapter = true)
data class LoginResponse(
    val accessToken: String,
    val refreshToken: String
)