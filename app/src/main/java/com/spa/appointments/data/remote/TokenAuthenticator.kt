package com.spa.appointments.data.remote

import com.spa.appointments.core.security.TokenStorage
import com.spa.appointments.domain.model.RefreshRequest
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

class TokenAuthenticator(
    private val authApi: AuthApiService,
    private val tokenStorage: TokenStorage
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {

        if (responseCount(response) >= 2) {
            tokenStorage.clearSession()
            return null
        }

        val refreshToken = tokenStorage.getRefreshToken() ?: return null

        return try {
            val refreshResponse = runBlocking {
                authApi.refresh(RefreshRequest(refreshToken))
            }

            tokenStorage.saveSession(
                accessToken = refreshResponse.accessToken,
                refreshToken = refreshToken,
                user = tokenStorage.getUser() ?: ""
            )

            response.request.newBuilder()
                .header(
                    "Authorization",
                    "Bearer ${refreshResponse.accessToken}"
                )
                .build()

        } catch (e: Exception) {
            tokenStorage.clearSession()
            null
        }
    }

    private fun responseCount(response: Response): Int {
        var count = 1
        var prior = response.priorResponse
        while (prior != null) {
            count++
            prior = prior.priorResponse
        }
        return count
    }
}
