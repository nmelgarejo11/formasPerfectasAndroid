package com.spa.appointments.ui.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spa.appointments.core.security.TokenStorage
import com.spa.appointments.core.theme.TemaStore
import com.spa.appointments.core.utils.Constants
import com.spa.appointments.data.repository.LicenciaRepository
import com.spa.appointments.data.repository.TemaRepository
import com.spa.appointments.domain.model.EstadoLicencia
import com.spa.appointments.core.notifications.FcmService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import javax.inject.Inject

sealed class SplashDestination {
    object Loading   : SplashDestination()
    object GoLogin   : SplashDestination()
    object GoHome    : SplashDestination()
    object GoExpired : SplashDestination()
}

// Modo del splash — genérico o con datos de empresa
enum class SplashModo { GENERICO, EMPRESA }

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val tokenStorage:       TokenStorage,
    private val licenciaRepository: LicenciaRepository,
    private val temaRepository:     TemaRepository
) : ViewModel() {

    private val _destination = MutableStateFlow<SplashDestination>(SplashDestination.Loading)
    val destination: StateFlow<SplashDestination> = _destination

    private val _modo = MutableStateFlow(SplashModo.GENERICO)
    val modo: StateFlow<SplashModo> = _modo

    init {checkSession()}

    private fun checkSession() {
        viewModelScope.launch {
            val token = tokenStorage.getAccessToken()

            if (token.isNullOrBlank()) {
                // Sin token → splash genérico → login
                _modo.value = SplashModo.GENERICO
                delay(Constants.SPLASH_DELAY_MS)
                _destination.value = SplashDestination.GoLogin
                return@launch
            }

            // Con token → splash genérico mientras carga el tema
            _modo.value = SplashModo.GENERICO

            try {
                val temaDeferred     = async {
                    try { temaRepository.getTema() } catch (e: Exception) { null }
                }
                val licenciaDeferred = async { licenciaRepository.validarLicencia() }

                // Cargamos el tema
                val temaResult = temaDeferred.await()
                temaResult?.let { TemaStore.setTema(it) }

                // Cambiamos al modo empresa con fade
                _modo.value = SplashModo.EMPRESA

                // Mostramos el splash de empresa el tiempo configurado
                delay(Constants.SPLASH_DELAY_MS)

                val licencia = licenciaDeferred.await()
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
                _modo.value = SplashModo.EMPRESA
                delay(Constants.SPLASH_DELAY_MS)
                if (e.code() == 403) {
                    _destination.value = SplashDestination.GoExpired
                } else {
                    _destination.value = SplashDestination.GoHome
                }
            } catch (e: Exception) {
                _modo.value = SplashModo.EMPRESA
                delay(Constants.SPLASH_DELAY_MS)
                _destination.value = SplashDestination.GoHome
            }
        }
    }
}