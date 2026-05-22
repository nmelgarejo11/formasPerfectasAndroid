package com.spa.appointments.ui.ingresos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spa.appointments.domain.model.IngresoRequest
import com.spa.appointments.domain.model.IngresoResponse
import com.spa.appointments.domain.model.MetodoPago
import com.spa.appointments.domain.model.Sede
import com.spa.appointments.domain.repository.IngresoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@HiltViewModel
class IngresoViewModel @Inject constructor(
    private val repository: IngresoRepository
) : ViewModel() {

    private fun getFechaHoy(): String =
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    // --- Lista ---
    private val _ingresos = MutableStateFlow<List<IngresoResponse>>(emptyList())
    val ingresos: StateFlow<List<IngresoResponse>> = _ingresos

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
    private val _fechaFiltro = MutableStateFlow(getFechaHoy())
    val fechaFiltro: StateFlow<String> = _fechaFiltro

    init {
        cargarMetodosPago()
        cargarSedes()
        cargarIngresos(fechaDesde = _fechaFiltro.value, fechaHasta = _fechaFiltro.value)
    }

    fun cargarSedes() {
        viewModelScope.launch {
            repository.getSedes()
                .onSuccess { _sedes.value = it }
                .onFailure { _mensaje.value = it.message }
        }
    }

    fun actualizarFechaFiltro(nuevaFecha: String) {
        _fechaFiltro.value = nuevaFecha
        cargarIngresos(fechaDesde = nuevaFecha, fechaHasta = nuevaFecha)
    }

    fun cargarIngresos(
        idSede: Int? = filtroIdSede,
        fechaDesde: String? = _fechaFiltro.value,
        fechaHasta: String? = _fechaFiltro.value
    ) {
        filtroIdSede = idSede

        viewModelScope.launch {
            _isLoading.value = true
            repository.listarIngresos(idSede, fechaDesde, fechaHasta)
                .onSuccess { _ingresos.value = it }
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

    fun registrarIngreso(request: IngresoRequest, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            repository.registrarIngreso(request)
                .onSuccess {
                    _mensaje.value = it.mensaje
                    cargarIngresos()
                    onSuccess()
                }
                .onFailure { _mensaje.value = it.message }
            _isLoading.value = false
        }
    }

    fun eliminarIngreso(id: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            repository.eliminarIngreso(id)
                .onSuccess {
                    _mensaje.value = it.mensaje
                    cargarIngresos()
                }
                .onFailure { _mensaje.value = it.message }
            _isLoading.value = false
        }
    }

    fun limpiarMensaje() { _mensaje.value = null }
}