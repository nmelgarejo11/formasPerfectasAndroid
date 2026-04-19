package com.spa.appointments.ui.perfil

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spa.appointments.domain.model.ActualizarPerfilRequest
import com.spa.appointments.domain.model.Perfil
import com.spa.appointments.domain.repository.PerfilRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class PerfilUiState {
    object Loading : PerfilUiState()
    data class Success(val perfil: Perfil) : PerfilUiState()
    data class Error(val mensaje: String) : PerfilUiState()
}

sealed class PerfilActionState {
    object Idle : PerfilActionState()
    object Loading : PerfilActionState()
    object Success : PerfilActionState()
    data class Error(val mensaje: String) : PerfilActionState()
}

@HiltViewModel
class PerfilViewModel @Inject constructor(
    private val repository: PerfilRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<PerfilUiState>(PerfilUiState.Loading)
    val uiState: StateFlow<PerfilUiState> = _uiState

    private val _actionState = MutableStateFlow<PerfilActionState>(PerfilActionState.Idle)
    val actionState: StateFlow<PerfilActionState> = _actionState

    init { cargarPerfil() }

    fun cargarPerfil() {
        viewModelScope.launch {
            _uiState.value = PerfilUiState.Loading
            repository.obtenerPerfil()
                .onSuccess { _uiState.value = PerfilUiState.Success(it) }
                .onFailure { _uiState.value = PerfilUiState.Error(it.message ?: "Error") }
        }
    }

    fun actualizarPerfil(request: ActualizarPerfilRequest) {
        viewModelScope.launch {
            _actionState.value = PerfilActionState.Loading
            repository.actualizarPerfil(request)
                .onSuccess {
                    _actionState.value = PerfilActionState.Success
                    cargarPerfil()
                }
                .onFailure {
                    _actionState.value = PerfilActionState.Error(it.message ?: "Error")
                }
        }
    }

    fun subirFoto(uri: Uri, context: Context) {
        viewModelScope.launch {
            _actionState.value = PerfilActionState.Loading
            repository.subirFoto(uri, context)
                .onSuccess {
                    _actionState.value = PerfilActionState.Success
                    cargarPerfil()
                }
                .onFailure {
                    _actionState.value = PerfilActionState.Error(it.message ?: "Error al subir foto")
                }
        }
    }

    fun resetActionState() {
        _actionState.value = PerfilActionState.Idle
    }
}