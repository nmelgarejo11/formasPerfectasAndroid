package com.spa.appointments.ui.metodopago

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spa.appointments.core.utils.ApiErrorMapper
import com.spa.appointments.domain.model.MetodoPagoAdmin
import com.spa.appointments.domain.model.MetodoPagoDetalleAdmin
import com.spa.appointments.domain.repository.MetodoPagoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MetodoPagoViewModel @Inject constructor(
    private val repo: MetodoPagoRepository
) : ViewModel() {

    private val _metodos = MutableStateFlow<List<MetodoPagoAdmin>>(emptyList())
    val metodos: StateFlow<List<MetodoPagoAdmin>> = _metodos

    private val _detalles = MutableStateFlow<List<MetodoPagoDetalleAdmin>>(emptyList())
    val detalles: StateFlow<List<MetodoPagoDetalleAdmin>> = _detalles

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _isForbidden = MutableStateFlow(false)
    val isForbidden: StateFlow<Boolean> = _isForbidden

    fun cargarMetodos() = viewModelScope.launch {
        _loading.value = true
        repo.listar()
            .onSuccess {
                _metodos.value = it
                _isForbidden.value = false
            }
            .onFailure {
                if (ApiErrorMapper.esForbidden(it)) {
                    _isForbidden.value = true
                } else {
                    _error.value = ApiErrorMapper.parsearMensajeError(it)
                }
            }
        _loading.value = false
    }

    fun cargarDetalles(idMetodoPago: Int) = viewModelScope.launch {
        repo.listarDetalles(idMetodoPago)
            .onSuccess { _detalles.value = it }
            .onFailure { _error.value = ApiErrorMapper.parsearMensajeError(it) }
    }

    fun crearMetodo(nombre: String) = viewModelScope.launch {
        repo.crear(nombre)
            .onSuccess { cargarMetodos() }
            .onFailure { _error.value = ApiErrorMapper.parsearMensajeError(it) }
    }

    fun actualizarMetodo(id: Int, nombre: String, activo: Boolean) = viewModelScope.launch {
        repo.actualizar(id, nombre, activo)
            .onSuccess { cargarMetodos() }
            .onFailure { _error.value = ApiErrorMapper.parsearMensajeError(it) }
    }

    fun crearDetalle(idMetodoPago: Int, nombre: String) = viewModelScope.launch {
        repo.crearDetalle(idMetodoPago, nombre)
            .onSuccess { cargarDetalles(idMetodoPago) }
            .onFailure { _error.value = ApiErrorMapper.parsearMensajeError(it) }
    }

    fun actualizarDetalle(idMetodoPago: Int, id: Int, nombre: String, activo: Boolean) =
        viewModelScope.launch {
            repo.actualizarDetalle(id, nombre, activo)
                .onSuccess { cargarDetalles(idMetodoPago) }
                .onFailure { _error.value = ApiErrorMapper.parsearMensajeError(it) }
        }

    fun limpiarError() { _error.value = null }
}
