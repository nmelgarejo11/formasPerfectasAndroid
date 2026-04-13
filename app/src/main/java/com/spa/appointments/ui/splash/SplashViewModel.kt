package com.spa.appointments.ui.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spa.appointments.core.security.TokenStorage
import com.spa.appointments.core.theme.TemaStore
import com.spa.appointments.core.utils.Constants
import com.spa.appointments.data.repository.LicenciaRepository
import com.spa.appointments.data.repository.TemaRepository
import com.spa.appointments.domain.model.EstadoLicencia
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import kotlinx.coroutines.delay
import javax.inject.Inject

sealed class SplashDestination {
    object Loading   : SplashDestination()
    object GoLogin   : SplashDestination()
    object GoHome    : SplashDestination()
    object GoExpired : SplashDestination()
}

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val tokenStorage:       TokenStorage,
    private val licenciaRepository: LicenciaRepository,
    private val temaRepository:     TemaRepository
) : ViewModel() {

    private val _destination = MutableStateFlow<SplashDestination>(SplashDestination.Loading)
    val destination: StateFlow<SplashDestination> = _destination

    // temaListo arranca en true — el splash se muestra de inmediato
    // con los valores por defecto si el tema no llega
    private val _temaListo = MutableStateFlow(true)
    val temaListo: StateFlow<Boolean> = _temaListo

    init { checkSession() }

    private fun checkSession() {
        viewModelScope.launch {
            delay(Constants.SPLASH_DELAY_MS)

            val token = tokenStorage.getAccessToken()

            // Sin token → Login directo
            if (token.isNullOrBlank()) {
                _destination.value = SplashDestination.GoLogin
                return@launch
            }

            // Con token → cargar tema y validar licencia
            try {
                // Primero cargamos el tema en segundo plano
                val temaDeferred = async {
                    try { temaRepository.getTema() } catch (e: Exception) { null }
                }

                // Validamos licencia
                val licencia = licenciaRepository.validarLicencia()

                // Aplicamos el tema si llegó
                temaDeferred.await()?.let { TemaStore.setTema(it) }

                // Guardamos estado de licencia
                tokenStorage.saveLicencia(
                    estado        = licencia.estado,
                    mensaje       = licencia.mensaje,
                    diasRestantes = licencia.diasRestantes
                )

                when (licencia.estado) {
                    EstadoLicencia.EXPIRADO,
                    EstadoLicencia.INACTIVA ->
                        _destination.value = SplashDestination.GoExpired
                    else ->
                        _destination.value = SplashDestination.GoHome
                }

            } catch (e: HttpException) {
                if (e.code() == 403) {
                    _destination.value = SplashDestination.GoExpired
                } else {
                    _destination.value = SplashDestination.GoHome
                }
            } catch (e: Exception) {
                _destination.value = SplashDestination.GoHome
            }
        }
    }
}