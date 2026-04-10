package com.spa.appointments.ui.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spa.appointments.core.security.TokenStorage
import com.spa.appointments.core.utils.Constants
import com.spa.appointments.data.repository.LicenciaRepository
import com.spa.appointments.domain.model.EstadoLicencia
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class SplashDestination {
    object Loading   : SplashDestination()
    object GoLogin   : SplashDestination()
    object GoHome    : SplashDestination()
    object GoExpired : SplashDestination() // ← nuevo: demo expirado
}

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val tokenStorage:       TokenStorage,
    private val licenciaRepository: LicenciaRepository
) : ViewModel() {

    private val _destination = MutableStateFlow<SplashDestination>(SplashDestination.Loading)
    val destination: StateFlow<SplashDestination> = _destination

    init {
        android.util.Log.d("SPLASH", "SplashViewModel init ejecutado")
        checkSession()
    }

    private fun checkSession() {
        viewModelScope.launch {
            delay(Constants.SPLASH_DELAY_MS)

            val token = tokenStorage.getAccessToken()
            android.util.Log.d("SPLASH", "Token: $token")

            if (token.isNullOrBlank()) {
                android.util.Log.d("SPLASH", "Sin token → GoLogin")
                _destination.value = SplashDestination.GoLogin
                return@launch
            }

            try {
                android.util.Log.d("SPLASH", "Validando licencia...")
                val licencia = licenciaRepository.validarLicencia()
                android.util.Log.d("SPLASH", "Estado licencia: ${licencia.estado}")

                tokenStorage.saveLicencia(
                    estado        = licencia.estado,
                    mensaje       = licencia.mensaje,
                    diasRestantes = licencia.diasRestantes
                )

                when (licencia.estado) {
                    EstadoLicencia.EXPIRADO,
                    EstadoLicencia.INACTIVA -> {
                        android.util.Log.d("SPLASH", "→ GoExpired")
                        _destination.value = SplashDestination.GoExpired
                    }
                    else -> {
                        android.util.Log.d("SPLASH", "→ GoHome")
                        _destination.value = SplashDestination.GoHome
                    }
                }

            } catch (e: retrofit2.HttpException) {
                android.util.Log.e("SPLASH", "HttpException código: ${e.code()}")
                if (e.code() == 403) {
                    android.util.Log.d("SPLASH", "403 → GoExpired")
                    tokenStorage.saveLicencia(
                        estado        = EstadoLicencia.EXPIRADO,
                        mensaje       = "Tu período de prueba ha expirado",
                        diasRestantes = 0
                    )
                    _destination.value = SplashDestination.GoExpired
                } else {
                    android.util.Log.e("SPLASH", "Otro error HTTP → GoHome")
                    _destination.value = SplashDestination.GoHome
                }
            } catch (e: Exception) {
                android.util.Log.e("SPLASH", "Exception: ${e::class.simpleName} - ${e.message}")
                _destination.value = SplashDestination.GoHome
            }
        }
    }
}