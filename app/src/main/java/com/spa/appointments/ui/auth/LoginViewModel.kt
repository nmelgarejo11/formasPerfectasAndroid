package com.spa.appointments.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spa.appointments.data.repository.AuthRepository
import com.spa.appointments.core.security.TokenStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import androidx.compose.runtime.*

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repo: AuthRepository,
    private val tokenStorage: TokenStorage
) : ViewModel() {

    var state by mutableStateOf(LoginState())
        private set

    fun login(user: String, pass: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                state = state.copy(loading = true)

                val response = repo.login(user, pass)

                tokenStorage.saveSession(
                    accessToken = response.accessToken,
                    refreshToken = response.refreshToken,
                    user = user
                )

                state = state.copy(loading = false, error = null)
                onSuccess()

            } catch (e: Exception) {
                state = state.copy(
                    loading = false,
                    error = e.localizedMessage ?: "Error de autenticación"
                )
            }
        }
    }

}

data class LoginState(
    val loading: Boolean = false,
    val error: String? = null
)
