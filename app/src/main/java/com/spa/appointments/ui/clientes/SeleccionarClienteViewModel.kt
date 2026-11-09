package com.spa.appointments.ui.clientes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spa.appointments.data.repository.ClienteRepository
import com.spa.appointments.domain.model.Cliente
import com.spa.appointments.domain.model.CrearClienteRequest
import com.spa.appointments.domain.model.ordenarAlfabeticamente
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
    data class Resultados(
        val clientes: List<Cliente>,
        val cargandoMas: Boolean = false,
        val finDeResultados: Boolean = false
    ) : SeleccionarClienteUiState()
    object SinResultados                               : SeleccionarClienteUiState()
    object Creando                                     : SeleccionarClienteUiState()
    data class Error(val mensaje: String)              : SeleccionarClienteUiState()
}

@HiltViewModel
class SeleccionarClienteViewModel @Inject constructor(
    private val repo: ClienteRepository
) : ViewModel() {

    companion object {
        private const val TAMANO_PAGINA = 20
    }

    private val _uiState = MutableStateFlow<SeleccionarClienteUiState>(SeleccionarClienteUiState.Idle)
    val uiState: StateFlow<SeleccionarClienteUiState> = _uiState

    private val _textoBusqueda = MutableStateFlow("")
    val textoBusqueda: StateFlow<String> = _textoBusqueda

    private var paginaActual = 1
    private var finDeResultados = false
    private var cargandoMas = false
    private var clientesAcumulados = mutableListOf<Cliente>()

    private var searchJob: Job? = null

    init {
        cargarClientes(reset = true)
    }

    fun cargarClientes(reset: Boolean = false) {
        if (reset) {
            paginaActual = 1
            finDeResultados = false
            cargandoMas = false
            clientesAcumulados.clear()
            _uiState.value = SeleccionarClienteUiState.Buscando
        }

        viewModelScope.launch {
            try {
                val texto = _textoBusqueda.value
                val resultados = repo.buscarClientes(
                    busqueda = texto.ifBlank { null },
                    pagina = paginaActual,
                    tamanoPagina = TAMANO_PAGINA
                )

                if (resultados.size < TAMANO_PAGINA) {
                    finDeResultados = true
                }

                if (reset) {
                    clientesAcumulados = resultados.toMutableList()
                } else {
                    val idsExistentes = clientesAcumulados.map { it.id }.toSet()
                    clientesAcumulados.addAll(resultados.filterNot { it.id in idsExistentes })
                }

                val listaOrdenada = clientesAcumulados.ordenarAlfabeticamente()
                cargandoMas = false

                _uiState.value = if (listaOrdenada.isEmpty()) {
                    SeleccionarClienteUiState.SinResultados
                } else {
                    SeleccionarClienteUiState.Resultados(
                        clientes = listaOrdenada,
                        cargandoMas = false,
                        finDeResultados = finDeResultados
                    )
                }
            } catch (e: Exception) {
                cargandoMas = false
                if (reset) {
                    _uiState.value = SeleccionarClienteUiState.Error(
                        e.localizedMessage ?: "Error al buscar clientes"
                    )
                } else {
                    val listaOrdenada = clientesAcumulados.ordenarAlfabeticamente()
                    _uiState.value = SeleccionarClienteUiState.Resultados(
                        clientes = listaOrdenada,
                        cargandoMas = false,
                        finDeResultados = finDeResultados
                    )
                }
            }
        }
    }

    fun cargarMasClientes() {
        if (cargandoMas || finDeResultados) return
        val current = _uiState.value
        if (current !is SeleccionarClienteUiState.Resultados) return

        cargandoMas = true
        _uiState.value = current.copy(cargandoMas = true)
        paginaActual++
        cargarClientes(reset = false)
    }

    // Búsqueda con debounce de 400ms para no llamar en cada tecla
    fun buscar(texto: String) {
        _textoBusqueda.value = texto
        searchJob?.cancel()

        searchJob = viewModelScope.launch {
            delay(400)
            cargarClientes(reset = true)
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
        cargarClientes(reset = true)
    }
}