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

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val tokenStorage:       TokenStorage,
    private val licenciaRepository: LicenciaRepository,
    private val temaRepository:     TemaRepository
) : ViewModel() {

    private val _destination = MutableStateFlow<SplashDestination>(SplashDestination.Loading)
    val destination: StateFlow<SplashDestination> = _destination

    // Controla si el contenido del splash está listo para mostrarse
    private val _contenidoListo = MutableStateFlow(false)
    val contenidoListo: StateFlow<Boolean> = _contenidoListo

    init { checkSession() }

    private fun checkSession() {
        viewModelScope.launch {
            val token = tokenStorage.getAccessToken()

            // Sin token → Login directo sin mostrar splash
            if (token.isNullOrBlank()) {
                _destination.value = SplashDestination.GoLogin
                return@launch
            }

            // Con token → cargar tema primero, luego mostrar splash
            try {
                // Cargamos tema y licencia en paralelo
                val temaDeferred     = async {
                    try { temaRepository.getTema() } catch (e: Exception) { null }
                }
                val licenciaDeferred = async { licenciaRepository.validarLicencia() }

                // Esperamos mínimo el delay del splash y que el tema cargue
                val temaResult = temaDeferred.await()
                temaResult?.let { TemaStore.setTema(it) }

                // Ahora sí mostramos el contenido — el tema ya está aplicado
                _contenidoListo.value = true

                // Esperamos el delay antes de navegar
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
                _contenidoListo.value = true
                delay(Constants.SPLASH_DELAY_MS)
                if (e.code() == 403) {
                    _destination.value = SplashDestination.GoExpired
                } else {
                    _destination.value = SplashDestination.GoHome
                }
            } catch (e: Exception) {
                _contenidoListo.value = true
                delay(Constants.SPLASH_DELAY_MS)
                _destination.value = SplashDestination.GoHome
            }
        }
    }
}