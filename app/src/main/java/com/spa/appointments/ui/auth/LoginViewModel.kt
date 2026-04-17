package com.spa.appointments.ui.auth

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spa.appointments.data.repository.AuthRepository
import com.spa.appointments.core.security.TokenStorage
import com.spa.appointments.core.utils.JwtUtils
import com.spa.appointments.core.theme.TemaStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.spa.appointments.domain.model.EstadoLicencia
import com.spa.appointments.data.repository.LicenciaRepository
import androidx.compose.runtime.*
import com.spa.appointments.data.repository.TemaRepository
import com.spa.appointments.domain.model.FcmTokenRequest

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repo: AuthRepository,
    private val tokenStorage: TokenStorage,
    private val licenciaRepository: LicenciaRepository,
    private val temaRepository: TemaRepository
) : ViewModel() {

    var state by mutableStateOf(LoginState())
        private set


    fun login(user: String, pass: String, onSuccess: () -> Unit, onExpired: () -> Unit) {

        TemaStore.limpiar()

        viewModelScope.launch {
            try {
                state = state.copy(loading = true)

                val response = repo.login(user, pass)

                val idEmpresa = JwtUtils.getIdEmpresa(response.accessToken)

                tokenStorage.saveSession(
                    accessToken = response.accessToken,
                    refreshToken = response.refreshToken,
                    user = user,
                    idEmpresa    = idEmpresa
                )

                // Registrar token FCM en la API
                try {
                    val fcmToken = tokenStorage.getFcmToken()
                    if (!fcmToken.isNullOrBlank()) {
                        repo.registrarFcmToken(FcmTokenRequest(token = fcmToken))
                    }
                } catch (e: Exception) {
                    Log.e("LoginViewModel", "Error registrando FCM token: ${e.message}")
                }

                // Cargar tema después del login
                try {
                    val tema = temaRepository.getTema()
                    TemaStore.setTema(tema)
                } catch (e: Exception) {
                    // Si falla el tema igual continuamos
                }

                try {
                    val licencia = licenciaRepository.validarLicencia()
                    tokenStorage.saveLicencia(
                        estado        = licencia.estado,
                        mensaje       = licencia.mensaje,
                        diasRestantes = licencia.diasRestantes
                    )

                    state = state.copy(loading = false, error = null)

                    if (licencia.estado == EstadoLicencia.EXPIRADO ||
                        licencia.estado == EstadoLicencia.INACTIVA) {
                        onExpired()
                    } else {
                        onSuccess()
                    }

                } catch (e: retrofit2.HttpException) {
                    state = state.copy(loading = false, error = null)
                    if (e.code() == 403) {
                        onExpired()
                    } else {
                        onSuccess() // otro error HTTP → dejamos pasar
                    }
                } catch (e: Exception) {
                    // Sin internet → dejamos pasar
                    state = state.copy(loading = false, error = null)
                    onSuccess()
                }

            } catch (e: Exception) {
                state = state.copy(
                    loading = false,
                    error   = e.localizedMessage ?: "Error de autenticación"
                )
            }
        }
    }

data class LoginState(
    val loading: Boolean = false,
    val error: String? = null
)
}
