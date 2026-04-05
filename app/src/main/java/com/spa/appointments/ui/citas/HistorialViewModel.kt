package com.spa.appointments.ui.citas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spa.appointments.core.security.TokenStorage
import com.spa.appointments.data.repository.CitasRepository
import com.spa.appointments.domain.model.Cita
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class HistorialUiState {
    object Loading                            : HistorialUiState()
    object Empty                              : HistorialUiState()
    data class Success(val citas: List<Cita>) : HistorialUiState()
    data class Error(val mensaje: String)     : HistorialUiState()
}

@HiltViewModel
class HistorialViewModel @Inject constructor(
    private val repo: CitasRepository,
    private val tokenStorage: TokenStorage
) : ViewModel() {

    private val _uiState = MutableStateFlow<HistorialUiState>(HistorialUiState.Loading)
    val uiState: StateFlow<HistorialUiState> = _uiState

    private val idCliente: Int get() = tokenStorage.getIdEmpresa()

    init { cargar() }

    fun cargar() {
        viewModelScope.launch {
            _uiState.value = HistorialUiState.Loading
            try {
                val citas = repo.getCitasHistorial(idCliente)
                _uiState.value = if (citas.isEmpty()) HistorialUiState.Empty
                else HistorialUiState.Success(citas)
            } catch (e: Exception) {
                _uiState.value = HistorialUiState.Error(
                    e.localizedMessage ?: "Error al cargar el historial"
                )
            }
        }
    }
}