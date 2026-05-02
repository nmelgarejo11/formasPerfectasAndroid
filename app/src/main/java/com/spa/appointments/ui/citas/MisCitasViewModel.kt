package com.spa.appointments.ui.citas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spa.appointments.data.repository.CitasRepository
import com.spa.appointments.domain.model.Cita
import com.spa.appointments.domain.model.EstadoCita
import com.spa.appointments.domain.model.MetodoPago
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class MisCitasUiState {
    object Loading                            : MisCitasUiState()
    object Empty                              : MisCitasUiState()
    data class Success(val citas: List<Cita>) : MisCitasUiState()
    data class Error(val mensaje: String)     : MisCitasUiState()
}

sealed class AccionUiState {
    object Idle                                : AccionUiState()
    object Loading                             : AccionUiState()
    data class Success(val mensaje: String)    : AccionUiState()
    data class Error(val mensaje: String)      : AccionUiState()
}

data class FiltrosMisCitas(
    val nombreCliente:  String? = null,
    val fechaDesde:     String? = null,
    val fechaHasta:     String? = null,
    val idProfesional:  Int?    = null,
    val idEstado:       Int?    = null,
    val nombreEstado:   String? = null
) {
    val activo: Boolean get() =
        nombreCliente != null || fechaDesde  != null ||
                fechaHasta    != null || idProfesional != null || idEstado != null
}

@HiltViewModel
class MisCitasViewModel @Inject constructor(
    private val repo: CitasRepository
) : ViewModel() {

    private val _uiState      = MutableStateFlow<MisCitasUiState>(MisCitasUiState.Loading)
    val uiState: StateFlow<MisCitasUiState> = _uiState.asStateFlow()

    private val _accionState  = MutableStateFlow<AccionUiState>(AccionUiState.Idle)
    val accionState: StateFlow<AccionUiState> = _accionState.asStateFlow()

    private val _metodosPago  = MutableStateFlow<List<MetodoPago>>(emptyList())
    val metodosPago: StateFlow<List<MetodoPago>> = _metodosPago.asStateFlow()

    private val _estados      = MutableStateFlow<List<EstadoCita>>(emptyList())
    val estados: StateFlow<List<EstadoCita>> = _estados.asStateFlow()

    private val _filtros      = MutableStateFlow(FiltrosMisCitas())
    val filtros: StateFlow<FiltrosMisCitas> = _filtros.asStateFlow()

    private val _mostrarFiltros = MutableStateFlow(false)
    val mostrarFiltros: StateFlow<Boolean> = _mostrarFiltros.asStateFlow()

    init {
        cargarEstados()
        cargarMetodosPago()
        cargar()
    }

    fun cargar() {
        viewModelScope.launch {
            _uiState.value = MisCitasUiState.Loading
            try {
                val f     = _filtros.value
                val citas = repo.getCitasActivas(
                    nombreCliente = f.nombreCliente,
                    fechaDesde    = f.fechaDesde,
                    fechaHasta    = f.fechaHasta,
                    idProfesional = f.idProfesional,
                    idEstado      = f.idEstado
                )
                _uiState.value = if (citas.isEmpty()) MisCitasUiState.Empty
                else MisCitasUiState.Success(citas)
            } catch (e: Exception) {
                _uiState.value = MisCitasUiState.Error(
                    e.localizedMessage ?: "Error al cargar las citas"
                )
            }
        }
    }

    private fun cargarEstados() {
        viewModelScope.launch {
            runCatching { repo.getEstadosCita("ACTIVAS") }
                .onSuccess { _estados.value = it }
        }
    }

    fun cargarMetodosPago() {
        viewModelScope.launch {
            runCatching { repo.getMetodosPago() }
                .onSuccess { _metodosPago.value = it }
        }
    }

    fun toggleFiltros() { _mostrarFiltros.update { !it } }

    fun aplicarFiltros(nuevos: FiltrosMisCitas) {
        _filtros.value        = nuevos
        _mostrarFiltros.value = false
        cargar()
    }

    fun limpiarFiltros() {
        _filtros.value = FiltrosMisCitas()
        cargar()
    }

    fun cancelarCita(idCita: Int) {
        viewModelScope.launch {
            _accionState.value = AccionUiState.Loading
            try {
                val resp = repo.cancelarCita(idCita)
                if (resp.ok) { _accionState.value = AccionUiState.Success(resp.mensaje); cargar() }
                else           _accionState.value = AccionUiState.Error(resp.mensaje)
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
                if (resp.ok) { _accionState.value = AccionUiState.Success(resp.mensaje); cargar() }
                else           _accionState.value = AccionUiState.Error(resp.mensaje)
            } catch (e: Exception) {
                _accionState.value = AccionUiState.Error(
                    e.localizedMessage ?: "Error al solicitar reagendamiento"
                )
            }
        }
    }

    fun finalizarCita(idCita: Int, idMetodoPago: Int) {
        viewModelScope.launch {
            _accionState.value = AccionUiState.Loading
            try {
                val resp = repo.finalizarCita(idCita, idMetodoPago)
                if (resp.ok) { _accionState.value = AccionUiState.Success(resp.mensaje); cargar() }
                else           _accionState.value = AccionUiState.Error(resp.mensaje)
            } catch (e: Exception) {
                _accionState.value = AccionUiState.Error(
                    e.localizedMessage ?: "Error al finalizar la cita"
                )
            }
        }
    }

    fun resetAccion() { _accionState.value = AccionUiState.Idle }
}