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

sealed class MisCitasUiState {
    object Loading                              : MisCitasUiState()
    object Empty                                : MisCitasUiState()
    data class Success(val citas: List<Cita>)   : MisCitasUiState()
    data class Error(val mensaje: String)        : MisCitasUiState()
}

sealed class AccionUiState {
    object Idle                          : AccionUiState()
    object Loading                       : AccionUiState()
    data class Success(val mensaje: String) : AccionUiState()
    data class Error(val mensaje: String)   : AccionUiState()
}

@HiltViewModel
class MisCitasViewModel @Inject constructor(
    private val repo: CitasRepository,
    private val tokenStorage: TokenStorage
) : ViewModel() {

    private val _uiState = MutableStateFlow<MisCitasUiState>(MisCitasUiState.Loading)
    val uiState: StateFlow<MisCitasUiState> = _uiState

    private val _accionState = MutableStateFlow<AccionUiState>(AccionUiState.Idle)
    val accionState: StateFlow<AccionUiState> = _accionState

    // Usamos IdEmpresa como IdCliente temporalmente
    // En el futuro habrá un flujo de selección de cliente real
    private val idCliente: Int get() = tokenStorage.getIdEmpresa()

    init { cargar() }

    fun cargar() {
        viewModelScope.launch {
            _uiState.value = MisCitasUiState.Loading
            try {
                val citas = repo.getCitasActivas(idCliente)
                _uiState.value = if (citas.isEmpty()) MisCitasUiState.Empty
                else MisCitasUiState.Success(citas)
            } catch (e: Exception) {
                _uiState.value = MisCitasUiState.Error(
                    e.localizedMessage ?: "Error al cargar las citas"
                )
            }
        }
    }

    fun cancelarCita(idCita: Int) {
        viewModelScope.launch {
            _accionState.value = AccionUiState.Loading
            try {
                val resp = repo.cancelarCita(idCita)
                if (resp.ok) {
                    _accionState.value = AccionUiState.Success(resp.mensaje)
                    cargar() // recargar la lista después de cancelar
                } else {
                    _accionState.value = AccionUiState.Error(resp.mensaje)
                }
            } catch (e: Exception) {
                _accionState.value = AccionUiState.Error(
                    e.localizedMessage ?: "Error al cancelar la cita"
                )
            }
        }
    }

    fun reagendarCita(idCita: Int, motivo: String?) {
        viewModelScope.launch {
            _accionState.value = AccionUiState.Loading
            try {
                val resp = repo.reagendarCita(idCita, motivo)
                if (resp.ok) {
                    _accionState.value = AccionUiState.Success(resp.mensaje)
                    cargar()
                } else {
                    _accionState.value = AccionUiState.Error(resp.mensaje)
                }
            } catch (e: Exception) {
                _accionState.value = AccionUiState.Error(
                    e.localizedMessage ?: "Error al solicitar reagendamiento"
                )
            }
        }
    }

    fun resetAccion() {
        _accionState.value = AccionUiState.Idle
    }
}