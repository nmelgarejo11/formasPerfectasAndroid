package com.spa.appointments.ui.clientes

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spa.appointments.data.repository.ClienteRepository
import com.spa.appointments.domain.model.ActualizarClienteRequest
import com.spa.appointments.domain.model.Cliente
import com.spa.appointments.domain.model.CrearClienteRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class ClientesUiState {
    object Idle : ClientesUiState()
    object Loading : ClientesUiState()
    data class Results(val clientes: List<Cliente>) : ClientesUiState()
    data class Error(val mensaje: String) : ClientesUiState()
}

sealed class ClienteDetalleState {
    object Idle : ClienteDetalleState()
    object Loading : ClienteDetalleState()
    data class Success(val cliente: Cliente) : ClienteDetalleState()
    data class Error(val mensaje: String) : ClienteDetalleState()
}

sealed class ClienteActionState {
    object Idle : ClienteActionState()
    object Loading : ClienteActionState()
    object Success : ClienteActionState()
    data class Error(val mensaje: String) : ClienteActionState()
}

@HiltViewModel
class ClientesViewModel @Inject constructor(
    private val repository: ClienteRepository
) : ViewModel() {

    private val _listState = MutableStateFlow<ClientesUiState>(ClientesUiState.Idle)
    val listState: StateFlow<ClientesUiState> = _listState

    private val _detalleState = MutableStateFlow<ClienteDetalleState>(ClienteDetalleState.Idle)
    val detalleState: StateFlow<ClienteDetalleState> = _detalleState

    private val _actionState = MutableStateFlow<ClienteActionState>(ClienteActionState.Idle)
    val actionState: StateFlow<ClienteActionState> = _actionState

    var query by mutableStateOf("")
        private set

    private var searchJob: Job? = null

    fun onQueryChange(value: String) {
        query = value
        searchJob?.cancel()
        if (value.length < 2) {
            _listState.value = ClientesUiState.Idle
            return
        }
        searchJob = viewModelScope.launch {
            delay(400)
            _listState.value = ClientesUiState.Loading
            runCatching { repository.buscarClientes(value) }
                .onSuccess { _listState.value = ClientesUiState.Results(it) }
                .onFailure { _listState.value = ClientesUiState.Error(it.message ?: "Error") }
        }
    }

    fun cargarCliente(id: Int) {
        viewModelScope.launch {
            _detalleState.value = ClienteDetalleState.Loading
            runCatching { repository.obtenerCliente(id) }
                .onSuccess { _detalleState.value = ClienteDetalleState.Success(it) }
                .onFailure { _detalleState.value = ClienteDetalleState.Error(it.message ?: "Error") }
        }
    }

    fun crearCliente(request: CrearClienteRequest) {
        viewModelScope.launch {
            _actionState.value = ClienteActionState.Loading
            runCatching { repository.crearCliente(request) }
                .onSuccess { _actionState.value = ClienteActionState.Success }
                .onFailure { _actionState.value = ClienteActionState.Error(it.message ?: "Error") }
        }
    }

    fun actualizarCliente(id: Int, request: ActualizarClienteRequest) {
        viewModelScope.launch {
            _actionState.value = ClienteActionState.Loading
            runCatching { repository.actualizarCliente(id, request) }
                .onSuccess { _actionState.value = ClienteActionState.Success }
                .onFailure { _actionState.value = ClienteActionState.Error(it.message ?: "Error") }
        }
    }

    fun desactivarCliente(id: Int) {
        viewModelScope.launch {
            _actionState.value = ClienteActionState.Loading
            runCatching { repository.desactivarCliente(id) }
                .onSuccess { _actionState.value = ClienteActionState.Success }
                .onFailure { _actionState.value = ClienteActionState.Error(it.message ?: "Error") }
        }
    }

    fun resetActionState() { _actionState.value = ClienteActionState.Idle }
    fun resetDetalleState() { _detalleState.value = ClienteDetalleState.Idle }
}