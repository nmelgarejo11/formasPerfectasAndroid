package com.spa.appointments.ui.citas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spa.appointments.data.repository.ReagendamientoRepository
import com.spa.appointments.domain.model.AprobarReagendamientoRequest
import com.spa.appointments.domain.model.CitaPendiente
import com.spa.appointments.domain.model.RechazarReagendamientoRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class ReagendamientoUiState {
    object Loading : ReagendamientoUiState()
    data class Success(val pendientes: List<CitaPendiente>) : ReagendamientoUiState()
    data class Error(val mensaje: String) : ReagendamientoUiState()
}

sealed class ReagendamientoActionState {
    object Idle : ReagendamientoActionState()
    object Loading : ReagendamientoActionState()
    object Success : ReagendamientoActionState()
    data class Error(val mensaje: String) : ReagendamientoActionState()
}

@HiltViewModel
class ReagendamientoViewModel @Inject constructor(
    private val repository: ReagendamientoRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ReagendamientoUiState>(ReagendamientoUiState.Loading)
    val uiState: StateFlow<ReagendamientoUiState> = _uiState

    private val _actionState = MutableStateFlow<ReagendamientoActionState>(ReagendamientoActionState.Idle)
    val actionState: StateFlow<ReagendamientoActionState> = _actionState

    init { cargar() }

    fun cargar() {
        viewModelScope.launch {
            _uiState.value = ReagendamientoUiState.Loading
            runCatching { repository.getPendientes() }
                .onSuccess { _uiState.value = ReagendamientoUiState.Success(it) }
                .onFailure { _uiState.value = ReagendamientoUiState.Error(it.message ?: "Error") }
        }
    }

    fun aprobar(id: Int, nuevaFechaInicio: String, nuevaFechaFin: String) {
        viewModelScope.launch {
            _actionState.value = ReagendamientoActionState.Loading
            runCatching {
                repository.aprobar(id, AprobarReagendamientoRequest(nuevaFechaInicio, nuevaFechaFin))
            }
                .onSuccess {
                    _actionState.value = ReagendamientoActionState.Success
                    cargar()
                }
                .onFailure { _actionState.value = ReagendamientoActionState.Error(it.message ?: "Error") }
        }
    }

    fun rechazar(id: Int, motivo: String) {
        viewModelScope.launch {
            _actionState.value = ReagendamientoActionState.Loading
            runCatching {
                repository.rechazar(id, RechazarReagendamientoRequest(motivo))
            }
                .onSuccess {
                    _actionState.value = ReagendamientoActionState.Success
                    cargar()
                }
                .onFailure { _actionState.value = ReagendamientoActionState.Error(it.message ?: "Error") }
        }
    }

    fun resetActionState() { _actionState.value = ReagendamientoActionState.Idle }
}