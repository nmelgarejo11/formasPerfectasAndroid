package com.spa.appointments.ui.gastos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spa.appointments.domain.model.GastoRequest
import com.spa.appointments.domain.model.GastoResponse
import com.spa.appointments.domain.model.MetodoPago
import com.spa.appointments.domain.model.Sede
import com.spa.appointments.domain.repository.GastoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow

// ui/gastos/GastoViewModel.kt
@HiltViewModel
class GastoViewModel @Inject constructor(
    private val repository: GastoRepository
) : ViewModel() {

    // --- Lista ---
    private val _gastos = MutableStateFlow<List<GastoResponse>>(emptyList())
    val gastos: StateFlow<List<GastoResponse>> = _gastos

    // --- Métodos de pago ---
    private val _metodosPago = MutableStateFlow<List<MetodoPago>>(emptyList())
    val metodosPago: StateFlow<List<MetodoPago>> = _metodosPago

    private val _sedes = MutableStateFlow<List<Sede>>(emptyList())
    val sedes: StateFlow<List<Sede>> = _sedes

    // --- UI State ---
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _mensaje = MutableStateFlow<String?>(null)
    val mensaje: StateFlow<String?> = _mensaje

    // --- Filtros activos ---
    private var filtroIdSede: Int? = null
    private var filtroFechaDesde: String? = null
    private var filtroFechaHasta: String? = null

    init {
        cargarMetodosPago()
        cargarSedes()
        cargarGastos()
    }

    fun cargarSedes() {
        viewModelScope.launch {
            repository.getSedes()
                .onSuccess { _sedes.value = it }
                .onFailure { _mensaje.value = it.message }
        }
    }

    fun cargarGastos(
        idSede: Int? = filtroIdSede,
        fechaDesde: String? = filtroFechaDesde,
        fechaHasta: String? = filtroFechaHasta
    ) {
        filtroIdSede = idSede
        filtroFechaDesde = fechaDesde
        filtroFechaHasta = fechaHasta

        viewModelScope.launch {
            _isLoading.value = true
            repository.listarGastos(idSede, fechaDesde, fechaHasta)
                .onSuccess { _gastos.value = it }
                .onFailure { _mensaje.value = it.message }
            _isLoading.value = false
        }
    }

    fun cargarMetodosPago() {
        viewModelScope.launch {
            repository.getMetodosPago()
                .onSuccess { _metodosPago.value = it }
                .onFailure { _mensaje.value = it.message }
        }
    }

    fun registrarGasto(request: GastoRequest, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            repository.registrarGasto(request)
                .onSuccess {
                    _mensaje.value = it.mensaje
                    cargarGastos()
                    onSuccess()
                }
                .onFailure { _mensaje.value = it.message }
            _isLoading.value = false
        }
    }

    fun eliminarGasto(id: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            repository.eliminarGasto(id)
                .onSuccess {
                    _mensaje.value = it.mensaje
                    cargarGastos()
                }
                .onFailure { _mensaje.value = it.message }
            _isLoading.value = false
        }
    }

    fun limpiarMensaje() { _mensaje.value = null }
}