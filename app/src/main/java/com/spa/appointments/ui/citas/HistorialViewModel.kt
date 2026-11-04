package com.spa.appointments.ui.citas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spa.appointments.data.repository.CitasRepository
import com.spa.appointments.domain.model.Cita
import com.spa.appointments.domain.model.EstadoCita
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class FiltrosHistorial(
    val nombreCliente : String? = null,
    val fechaDesde    : String? = null,
    val fechaHasta    : String? = null,
    val idProfesional : Int?    = null,
    val idEstado      : Int?    = null,
    val nombreEstado  : String? = null,
    val esFiltroManual: Boolean = false   // ← nuevo
) {
    val activo: Boolean get() = esFiltroManual
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

    private val _uiState        = MutableStateFlow<HistorialUiState>(HistorialUiState.Loading)
    val uiState: StateFlow<HistorialUiState> = _uiState.asStateFlow()

    private val _filtros        = MutableStateFlow(filtroHoy())
    val filtros: StateFlow<FiltrosHistorial> = _filtros.asStateFlow()

    private val _mostrarFiltros = MutableStateFlow(false)
    val mostrarFiltros: StateFlow<Boolean> = _mostrarFiltros.asStateFlow()

    private val _estados        = MutableStateFlow<List<EstadoCita>>(emptyList())
    val estados: StateFlow<List<EstadoCita>> = _estados.asStateFlow()

    companion object {
        fun hoyIso(): String = LocalDate.now().toString()
        fun filtroHoy() = FiltrosHistorial(fechaDesde = hoyIso(), fechaHasta = hoyIso())
    }

    init {
        cargarEstados()
        cargar()
    }

    private fun cargarEstados() {
        viewModelScope.launch {
            runCatching { repo.getEstadosCita("HISTORIAL") }
                .onSuccess { _estados.value = it }
        }
    }

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
        _filtros.value        = nuevos.copy(esFiltroManual = true)  // ← marca manual
        _mostrarFiltros.value = false
        cargar()
    }

    fun limpiarFiltros() {
        _filtros.value = filtroHoy()   // vuelve a hoy, sin banner
        cargar()
    }
}