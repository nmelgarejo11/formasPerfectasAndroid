package com.spa.appointments.data.remote

import com.spa.appointments.core.security.TokenStorage
import okhttp3.Interceptor
import okhttp3.Response

const val PLATFORM_MOBILE = "3"

class AuthInterceptor(
    private val tokenStorage: TokenStorage

) : Interceptor {


    override fun intercept(chain: Interceptor.Chain): Response {


        val originalRequest = chain.request()
        val accessToken = tokenStorage.getAccessToken()
        val requestBuilder = originalRequest.newBuilder()
            .addHeader("X-Platform", PLATFORM_MOBILE)

        if (!accessToken.isNullOrBlank()) {
            requestBuilder.addHeader("Authorization", "Bearer $accessToken")
        }

        return chain.proceed(requestBuilder.build())
    }
}
