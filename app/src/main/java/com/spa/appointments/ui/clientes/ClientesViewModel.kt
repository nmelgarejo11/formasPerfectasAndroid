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
import com.spa.appointments.domain.model.ordenarAlfabeticamente
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
    data class Results(
        val clientes: List<Cliente>,
        val cargandoMas: Boolean = false,
        val finDeResultados: Boolean = false
    ) : ClientesUiState()
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

    companion object {
        private const val TAMANO_PAGINA = 20
    }

    var estaEscuchandoVoz by mutableStateOf(false)
    var cargandoParserVoz by mutableStateOf(false)
        private set

    private val _listState = MutableStateFlow<ClientesUiState>(ClientesUiState.Idle)
    val listState: StateFlow<ClientesUiState> = _listState

    private val _detalleState = MutableStateFlow<ClienteDetalleState>(ClienteDetalleState.Idle)
    val detalleState: StateFlow<ClienteDetalleState> = _detalleState

    private val _actionState = MutableStateFlow<ClienteActionState>(ClienteActionState.Idle)
    val actionState: StateFlow<ClienteActionState> = _actionState

    var query by mutableStateOf("")
        private set

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
            _listState.value = ClientesUiState.Loading
        }

        viewModelScope.launch {
            runCatching {
                repository.buscarClientes(
                    busqueda = query.ifBlank { null },
                    pagina = paginaActual,
                    tamanoPagina = TAMANO_PAGINA
                )
            }
                .onSuccess { nuevosClientes ->
                    if (nuevosClientes.size < TAMANO_PAGINA) {
                        finDeResultados = true
                    }

                    if (reset) {
                        clientesAcumulados = nuevosClientes.toMutableList()
                    } else {
                        val idsExistentes = clientesAcumulados.map { it.id }.toSet()
                        clientesAcumulados.addAll(nuevosClientes.filterNot { it.id in idsExistentes })
                    }

                    val listaOrdenada = clientesAcumulados.ordenarAlfabeticamente()
                    cargandoMas = false
                    _listState.value = ClientesUiState.Results(
                        clientes = listaOrdenada,
                        cargandoMas = false,
                        finDeResultados = finDeResultados
                    )
                }
                .onFailure { error ->
                    cargandoMas = false
                    if (reset) {
                        _listState.value = ClientesUiState.Error(error.message ?: "Error al cargar clientes")
                    } else {
                        val listaOrdenada = clientesAcumulados.ordenarAlfabeticamente()
                        _listState.value = ClientesUiState.Results(
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
        val current = _listState.value
        if (current !is ClientesUiState.Results) return

        cargandoMas = true
        _listState.value = current.copy(cargandoMas = true)
        paginaActual++
        cargarClientes(reset = false)
    }

    fun onQueryChange(value: String) {
        query = value
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(400)
            cargarClientes(reset = true)
        }
    }

    fun procesarRafagaVoz(
        textoAcumulado: String,
        onResultado: (nombre: String?, apellido: String?, telefono: String?, email: String?) -> Unit
    ) {
        if (textoAcumulado.isBlank()) return

        viewModelScope.launch {
            cargandoParserVoz = true

            // LOG INICIAL: Para confirmar que Android disparó la acción
            android.util.Log.d("IA_VOZ_DEBUG", "Enviando texto a la API: '$textoAcumulado'")

            runCatching {
                repository.extraerDatosDesdeVoz(textoAcumulado)
            }
                .onSuccess { dto ->
                    android.util.Log.d("IA_VOZ_DEBUG", "¡Éxito! Datos recibidos: $dto")
                    // Devolvemos los campos extraídos por la IA mediante un callback
                    onResultado(dto.nombre, dto.apellido, dto.telefono, dto.email)
                }
                .onFailure { error ->
                    // ─── AQUÍ REVELAMOS EL COMPORTAMIENTO OCULTO ───────────────────
                    android.util.Log.e("IA_VOZ_DEBUG", "ERROR CRÍTICO AL LLAMAR A C#", error)
                }
                .also {
                    cargandoParserVoz = false
                }
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