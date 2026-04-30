package com.spa.appointments.ui.clientes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spa.appointments.data.repository.ClienteRepository
import com.spa.appointments.domain.model.Cliente
import com.spa.appointments.domain.model.CrearClienteRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class SeleccionarClienteUiState {
    object Idle                                        : SeleccionarClienteUiState()
    object Buscando                                    : SeleccionarClienteUiState()
    data class Resultados(val clientes: List<Cliente>) : SeleccionarClienteUiState()
    object SinResultados                               : SeleccionarClienteUiState()
    object Creando                                     : SeleccionarClienteUiState()
    data class Error(val mensaje: String)              : SeleccionarClienteUiState()
}

@HiltViewModel
class SeleccionarClienteViewModel @Inject constructor(
    private val repo: ClienteRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<SeleccionarClienteUiState>(SeleccionarClienteUiState.Idle)
    val uiState: StateFlow<SeleccionarClienteUiState> = _uiState

    private var searchJob: Job? = null


    private val _textoBusqueda = MutableStateFlow("")
    val textoBusqueda: StateFlow<String> = _textoBusqueda

    // Búsqueda con debounce de 400ms para no llamar en cada tecla
    fun buscar(texto: String) {
        _textoBusqueda.value = texto
        searchJob?.cancel()

        if (texto.length < 2) {
            _uiState.value = SeleccionarClienteUiState.Idle
            return
        }

        searchJob = viewModelScope.launch {
            delay(400)
            _uiState.value = SeleccionarClienteUiState.Buscando
            try {
                val resultados = repo.buscarClientes(texto)
                _uiState.value = if (resultados.isEmpty())
                    SeleccionarClienteUiState.SinResultados
                else
                    SeleccionarClienteUiState.Resultados(resultados)
            } catch (e: Exception) {
                _uiState.value = SeleccionarClienteUiState.Error(
                    e.localizedMessage ?: "Error al buscar clientes"
                )
            }
        }
    }

    fun crearCliente(
        nombre: String,
        apellido: String,
        telefono: String?,
        email: String?,
        onCreado: (Cliente) -> Unit
    ) {
        viewModelScope.launch {
            _uiState.value = SeleccionarClienteUiState.Creando
            try {
                val cliente = repo.crearCliente(
                    CrearClienteRequest(
                        nombre   = nombre,
                        apellido = apellido,
                        telefono = telefono.takeIf { !it.isNullOrBlank() },
                        email    = email.takeIf    { !it.isNullOrBlank() }
                    )
                )
                onCreado(cliente)
            } catch (e: Exception) {
                _uiState.value = SeleccionarClienteUiState.Error(
                    e.localizedMessage ?: "Error al crear cliente"
                )
            }
        }
    }

    fun resetear() {
        _textoBusqueda.value = ""
        searchJob?.cancel()
        _uiState.value = SeleccionarClienteUiState.Idle
    }
}