package com.spa.appointments.ui.financiero

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spa.appointments.domain.model.IngresosVsGastosResponse
import com.spa.appointments.domain.model.Sede
import com.spa.appointments.domain.repository.FinancieroRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

sealed class IngresosVsGastosUiState {
    object Idle    : IngresosVsGastosUiState()
    object Loading : IngresosVsGastosUiState()
    data class Success(val data: IngresosVsGastosResponse) : IngresosVsGastosUiState()
    data class Error(val mensaje: String)                  : IngresosVsGastosUiState()
}

@HiltViewModel
class IngresosVsGastosViewModel @Inject constructor(
    private val repo: FinancieroRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<IngresosVsGastosUiState>(IngresosVsGastosUiState.Idle)
    val uiState: StateFlow<IngresosVsGastosUiState> = _uiState

    private val _sedes = MutableStateFlow<List<Sede>>(emptyList())
    val sedes: StateFlow<List<Sede>> = _sedes

    private val _sedeSeleccionada = MutableStateFlow<Sede?>(null)
    val sedeSeleccionada: StateFlow<Sede?> = _sedeSeleccionada

    // Rango por defecto: último mes
    private val _fechaInicio = MutableStateFlow(LocalDate.now().withDayOfMonth(1))
    val fechaInicio: StateFlow<LocalDate> = _fechaInicio

    private val _fechaFin = MutableStateFlow(LocalDate.now())
    val fechaFin: StateFlow<LocalDate> = _fechaFin

    init { cargarSedes() }

    fun cargarSedes() {
        viewModelScope.launch {
            _uiState.value = IngresosVsGastosUiState.Loading
            try {
                val lista = repo.getSedes()
                _sedes.value = lista
                if (_sedeSeleccionada.value == null && lista.isNotEmpty()) {
                    _sedeSeleccionada.value = lista.first()
                    cargar()
                } else {
                    _uiState.value = IngresosVsGastosUiState.Idle
                }
            } catch (e: Exception) {
                _uiState.value = IngresosVsGastosUiState.Error(e.message ?: "Error al cargar sedes")
            }
        }
    }

    fun seleccionarSede(sede: Sede) {
        _sedeSeleccionada.value = sede
        cargar()
    }

    fun cambiarRango(inicio: LocalDate, fin: LocalDate) {
        _fechaInicio.value = inicio
        _fechaFin.value    = fin
        cargar()
    }

    fun cargar() {
        val sede = _sedeSeleccionada.value ?: return
        viewModelScope.launch {
            _uiState.value = IngresosVsGastosUiState.Loading
            try {
                val data = repo.getIngresosVsGastos(
                    idSede      = sede.id,
                    fechaInicio = _fechaInicio.value,
                    fechaFin    = _fechaFin.value
                )
                _uiState.value = IngresosVsGastosUiState.Success(data)
            } catch (e: Exception) {
                _uiState.value = IngresosVsGastosUiState.Error(e.message ?: "Error al cargar datos")
            }
        }
    }
}