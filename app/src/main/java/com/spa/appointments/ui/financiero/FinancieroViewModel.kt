package com.spa.appointments.ui.financiero

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spa.appointments.domain.model.CierreCajaResponse
import com.spa.appointments.domain.model.Sede
import com.spa.appointments.domain.repository.FinancieroRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

sealed class FinancieroUiState {
    object Idle    : FinancieroUiState()
    object Loading : FinancieroUiState()
    data class Success(val data: CierreCajaResponse) : FinancieroUiState()
    data class Error(val mensaje: String)             : FinancieroUiState()
}

@HiltViewModel
class FinancieroViewModel @Inject constructor(
    private val repo: FinancieroRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<FinancieroUiState>(FinancieroUiState.Idle)
    val uiState: StateFlow<FinancieroUiState> = _uiState

    private val _sedes = MutableStateFlow<List<Sede>>(emptyList())
    val sedes: StateFlow<List<Sede>> = _sedes

    private val _sedeSeleccionada = MutableStateFlow<Sede?>(null)
    val sedeSeleccionada: StateFlow<Sede?> = _sedeSeleccionada

    private val _fechaSeleccionada = MutableStateFlow(LocalDate.now())
    val fechaSeleccionada: StateFlow<LocalDate> = _fechaSeleccionada

    init { cargarSedes() }

    fun cargarSedes() {
        viewModelScope.launch {
            _uiState.value = FinancieroUiState.Loading
            try {
                val lista = repo.getSedes()
                _sedes.value = lista
                // Selecciona la primera sede automáticamente
                if (_sedeSeleccionada.value == null && lista.isNotEmpty()) {
                    _sedeSeleccionada.value = lista.first()
                    cargarCierreCaja()
                } else {
                    _uiState.value = FinancieroUiState.Idle
                }
            } catch (e: Exception) {
                _uiState.value = FinancieroUiState.Error(e.message ?: "Error al cargar sedes")
            }
        }
    }

    fun seleccionarSede(sede: Sede) {
        _sedeSeleccionada.value = sede
        cargarCierreCaja()
    }

    fun cambiarFecha(nuevaFecha: LocalDate) {
        _fechaSeleccionada.value = nuevaFecha
        cargarCierreCaja()
    }

    fun cargarCierreCaja() {
        val sede = _sedeSeleccionada.value ?: return
        viewModelScope.launch {
            _uiState.value = FinancieroUiState.Loading
            try {
                val data = repo.getCierreCaja(
                    idSede = sede.id,
                    fecha  = _fechaSeleccionada.value
                )
                _uiState.value = FinancieroUiState.Success(data)
            } catch (e: Exception) {
                _uiState.value = FinancieroUiState.Error(e.message ?: "Error al cargar cierre de caja")
            }
        }
    }
}