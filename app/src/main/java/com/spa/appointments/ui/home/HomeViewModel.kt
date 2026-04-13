package com.spa.appointments.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spa.appointments.core.security.TokenStorage
import com.spa.appointments.data.repository.MenuRepository
import com.spa.appointments.domain.model.Modulo
import com.spa.appointments.core.theme.TemaStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class HomeUiState {
    object Loading                                                       : HomeUiState()
    object Empty                                                         : HomeUiState()
    data class Success(val modulos: List<Modulo>, val userName: String)  : HomeUiState()
    data class Error(val mensaje: String)                                : HomeUiState()
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val menuRepository: MenuRepository,
    private val tokenStorage:   TokenStorage
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState

    // Propiedades de licencia leídas desde TokenStorage
    val licenciaEstado:  String get() = tokenStorage.getLicenciaEstado()
    val licenciaMensaje: String get() = tokenStorage.getLicenciaMensaje()

    init { cargarMenu() }

    private fun cargarMenu() {
        viewModelScope.launch {
            try {
                val response = menuRepository.obtenerMenu()
                val userName = tokenStorage.getUser() ?: "Usuario"
                val modulos  = response.menu?.filter {
                    !it.submodulos.isNullOrEmpty()
                } ?: emptyList()

                if (modulos.isEmpty()) {
                    _uiState.value = HomeUiState.Empty
                } else {
                    _uiState.value = HomeUiState.Success(
                        modulos  = modulos,
                        userName = userName
                    )
                }
            } catch (e: Exception) {
                _uiState.value = HomeUiState.Error(
                    mensaje = e.localizedMessage ?: "Error al cargar el menú"
                )
            }
        }
    }

    fun logout() {
        tokenStorage.clearSession()
        TemaStore.limpiar()  // ← limpiar tema al cerrar sesión
    }
}