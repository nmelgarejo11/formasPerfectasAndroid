package com.spa.appointments.ui.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spa.appointments.core.security.TokenStorage
import com.spa.appointments.core.utils.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// Estados posibles del splash — la pantalla reacciona a estos estados
sealed class SplashDestination {
    object Loading  : SplashDestination() // Todavía verificando
    object GoLogin  : SplashDestination() // No hay sesión → ir al Login
    object GoHome   : SplashDestination() // Sesión activa → ir al Home
}

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val tokenStorage: TokenStorage
    // En el futuro puedes inyectar aquí un UseCase que valide el token
    // contra el servidor, no solo localmente
) : ViewModel() {

    // StateFlow es como un "canal de datos" que la UI escucha en tiempo real
    // Solo este ViewModel puede escribir en él (private set via MutableStateFlow)
    private val _destination = MutableStateFlow<SplashDestination>(SplashDestination.Loading)
    val destination: StateFlow<SplashDestination> = _destination

    init {
        // init se ejecuta automáticamente cuando el ViewModel se crea
        checkSession()
    }

    private fun checkSession() {
        viewModelScope.launch {
            // Mostramos el splash al menos SPLASH_DELAY_MS para que no parpadee
            delay(Constants.SPLASH_DELAY_MS)

            val token = tokenStorage.getAccessToken()

            // Si hay token guardado, asumimos sesión activa y vamos al Home
            // (En fase 2 agregaremos validación real contra el servidor)
            if (!token.isNullOrBlank()) {
                _destination.value = SplashDestination.GoHome
            } else {
                _destination.value = SplashDestination.GoLogin
            }
        }
    }
}