package com.spa.appointments.domain.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class FcmTokenRequest(
    val token: String,
    val plataforma: String = "ANDROID"
)