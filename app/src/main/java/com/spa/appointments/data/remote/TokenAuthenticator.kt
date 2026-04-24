package com.spa.appointments.data.remote

import com.spa.appointments.core.security.TokenStorage
import com.spa.appointments.domain.model.RefreshRequest
import com.spa.appointments.core.utils.JwtUtils
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

class TokenAuthenticator(
    private val authApi: AuthApiService,
    private val tokenStorage: TokenStorage
) : Authenticator {

    companion object {
        // Mutex compartido: si dos peticiones fallan a la vez,
        // solo UNA hace el refresh; la otra espera y reutiliza el token nuevo.
        private val refreshMutex = Mutex()
    }

    override fun authenticate(route: Route?, response: Response): Request? {

        if (responseCount(response) >= 2) {
            tokenStorage.clearSession()
            return null
        }

        return runBlocking {
            refreshMutex.withLock {

                // Doble check: quizás el token ya fue renovado por otra corrutina
                // mientras esperábamos el lock.
                val tokenEnStorage = tokenStorage.getAccessToken()
                val tokenEnRequest = response.request.header("Authorization")
                    ?.removePrefix("Bearer ")?.trim()

                if (tokenEnStorage != null && tokenEnStorage != tokenEnRequest) {
                    // Ya hay un token nuevo — reutilizarlo sin llamar a la API
                    return@runBlocking response.request.newBuilder()
                        .header("Authorization", "Bearer $tokenEnStorage")
                        .build()
                }

                // Token sigue siendo el mismo — hacer el refresh
                val refreshToken = tokenStorage.getRefreshToken()
                    ?: run {
                        tokenStorage.clearSession()
                        return@runBlocking null
                    }

                try {
                    val refreshResponse = authApi.refresh(RefreshRequest(refreshToken))
                    val idEmpresa = JwtUtils.getIdEmpresa(refreshResponse.accessToken)

                    tokenStorage.saveSession(
                        accessToken  = refreshResponse.accessToken,
                        refreshToken = refreshToken,
                        user         = tokenStorage.getUser() ?: "",
                        idEmpresa    = idEmpresa
                    )

                    response.request.newBuilder()
                        .header("Authorization", "Bearer ${refreshResponse.accessToken}")
                        .build()

                } catch (e: Exception) {
                    tokenStorage.clearSession()
                    null
                }
            }
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