// Ruta: app/src/main/java/com/spa/appointments/ui/citas/HistorialViewModel.kt
package com.spa.appointments.ui.citas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spa.appointments.data.repository.CitasRepository
import com.spa.appointments.domain.model.Cita
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FiltrosHistorial(
    val nombreCliente:  String? = null,
    val fechaDesde:     String? = null,
    val fechaHasta:     String? = null,
    val idProfesional:  Int?    = null,
    val idEstado:       Int?    = null,
    val nombreEstado:   String? = null
) {
    val activo: Boolean get() =
        nombreCliente != null || fechaDesde != null ||
                fechaHasta    != null || idProfesional != null || idEstado != null
}

sealed class HistorialUiState {
    object Loading                            : HistorialUiState()
    object Empty                              : HistorialUiState()
    data class Success(val citas: List<Cita>) : HistorialUiState()
    data class Error(val mensaje: String)     : HistorialUiState()
}

@HiltViewModel
class HistorialViewModel @Inject constructor(
    private val repo: CitasRepository
) : ViewModel() {

    private val _uiState       = MutableStateFlow<HistorialUiState>(HistorialUiState.Loading)
    val uiState: StateFlow<HistorialUiState> = _uiState.asStateFlow()

    private val _filtros       = MutableStateFlow(FiltrosHistorial())
    val filtros: StateFlow<FiltrosHistorial> = _filtros.asStateFlow()

    private val _mostrarFiltros = MutableStateFlow(false)
    val mostrarFiltros: StateFlow<Boolean> = _mostrarFiltros.asStateFlow()

    init { cargar() }

    fun cargar() {
        viewModelScope.launch {
            _uiState.value = HistorialUiState.Loading
            try {
                val f     = _filtros.value
                val citas = repo.getCitasHistorial(
                    nombreCliente = f.nombreCliente,
                    fechaDesde    = f.fechaDesde,
                    fechaHasta    = f.fechaHasta,
                    idProfesional = f.idProfesional,
                    idEstado      = f.idEstado
                )
                _uiState.value = if (citas.isEmpty()) HistorialUiState.Empty
                else HistorialUiState.Success(citas)
            } catch (e: Exception) {
                _uiState.value = HistorialUiState.Error(
                    e.localizedMessage ?: "Error al cargar el historial"
                )
            }
        }
    }

    fun toggleFiltros() { _mostrarFiltros.update { !it } }

    fun aplicarFiltros(nuevos: FiltrosHistorial) {
        _filtros.value        = nuevos
        _mostrarFiltros.value = false
        cargar()
    }

    fun limpiarFiltros() {
        _filtros.value = FiltrosHistorial()
        cargar()
    }
}