package com.spa.appointments.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spa.appointments.core.security.TokenStorage
import com.spa.appointments.data.repository.MenuRepository
import com.spa.appointments.domain.model.Modulo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// Estado de la pantalla Home
// Usamos sealed class para representar los 3 estados posibles
sealed class HomeUiState {
    object Loading : HomeUiState()
    data class Success(val modulos: List<Modulo>, val userName: String) : HomeUiState()
    data class Error(val mensaje: String) : HomeUiState()
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val menuRepository: MenuRepository,
    private val tokenStorage: TokenStorage
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState

    init {
        cargarMenu()
    }

    private fun cargarMenu() {
        viewModelScope.launch {
            try {
                val response = menuRepository.obtenerMenu()
                val userName = tokenStorage.getUser() ?: "Usuario"

                _uiState.value = HomeUiState.Success(
                    modulos = response.menu,
                    userName = userName
                )
            } catch (e: Exception) {
                _uiState.value = HomeUiState.Error(
                    mensaje = e.localizedMessage ?: "Error al cargar el menú"
                )
            }
        }
    }

    // Logout: limpia la sesión y notifica a la UI
    fun logout() {
        tokenStorage.clearSession()
    }
}