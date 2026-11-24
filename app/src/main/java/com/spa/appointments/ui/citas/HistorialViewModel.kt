package com.spa.appointments.ui.citas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spa.appointments.data.repository.CitasRepository
import com.spa.appointments.domain.model.Cita
import com.spa.appointments.domain.model.EstadoCita
import com.spa.appointments.domain.model.Profesional
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class FiltrosHistorial(
    val nombreCliente     : String? = null,
    val fechaDesde        : String? = null,
    val fechaHasta        : String? = null,
    val idProfesional     : Int?    = null,
    val nombreProfesional : String? = null,
    val idEstado          : Int?    = null,
    val nombreEstado      : String? = null,
    val esFiltroManual    : Boolean = false
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

    private val _estadosActivas = MutableStateFlow<List<EstadoCita>>(emptyList())
    val estadosActivas: StateFlow<List<EstadoCita>> = _estadosActivas.asStateFlow()

    private val _profesionales  = MutableStateFlow<List<Profesional>>(emptyList())
    val profesionales: StateFlow<List<Profesional>> = _profesionales.asStateFlow()

    private val _modoVista      = MutableStateFlow(ModoVistaCitas.CALENDARIO_DIA)
    val modoVista: StateFlow<ModoVistaCitas> = _modoVista.asStateFlow()

    private val _fechaCalendario = MutableStateFlow(LocalDate.now())
    val fechaCalendario: StateFlow<LocalDate> = _fechaCalendario.asStateFlow()

    // Estados para Ajuste de Estado
    private val _citaParaAjuste    = MutableStateFlow<Cita?>(null)
    val citaParaAjuste: StateFlow<Cita?> = _citaParaAjuste.asStateFlow()

    private val _mostrarDialogAjuste = MutableStateFlow(false)
    val mostrarDialogAjuste: StateFlow<Boolean> = _mostrarDialogAjuste.asStateFlow()

    private val _guardandoAjuste   = MutableStateFlow(false)
    val guardandoAjuste: StateFlow<Boolean> = _guardandoAjuste.asStateFlow()

    private val _haySolapamiento   = MutableStateFlow(false)
    val haySolapamiento: StateFlow<Boolean> = _haySolapamiento.asStateFlow()

    private val _verificandoSolapamiento = MutableStateFlow(false)
    val verificandoSolapamiento: StateFlow<Boolean> = _verificandoSolapamiento.asStateFlow()

    private val _mensajeFeedback   = MutableSharedFlow<String>()
    val mensajeFeedback: SharedFlow<String> = _mensajeFeedback.asSharedFlow()

    companion object {
        fun hoyIso(): String = LocalDate.now().toString()
        fun filtroHoy() = FiltrosHistorial(fechaDesde = hoyIso(), fechaHasta = hoyIso())
    }

    init {
        cargarEstados()
        cargarEstadosActivas()
        cargarProfesionales()
        cargar()
    }

    fun cargarProfesionales(idSede: Int? = null, idServicio: Int? = null) {
        viewModelScope.launch {
            runCatching { repo.getProfesionales(idSede = idSede, idServicio = idServicio) }
                .onSuccess { _profesionales.value = it }
        }
    }

    fun toggleModoVista() {
        _modoVista.update { actual ->
            val nuevoModo = if (actual == ModoVistaCitas.LISTA) ModoVistaCitas.CALENDARIO_DIA else ModoVistaCitas.LISTA
            if (nuevoModo == ModoVistaCitas.CALENDARIO_DIA) {
                val fechaIso = _fechaCalendario.value.toString()
                _filtros.update { it.copy(fechaDesde = fechaIso, fechaHasta = fechaIso) }
                cargar()
            }
            nuevoModo
        }
    }

    fun setFechaCalendario(nuevaFecha: LocalDate) {
        _fechaCalendario.value = nuevaFecha
        val fechaIso = nuevaFecha.toString()
        _filtros.update { it.copy(fechaDesde = fechaIso, fechaHasta = fechaIso) }
        cargar()
    }

    fun seleccionarProfesional(id: Int?, nombre: String?) {
        _filtros.update { actual ->
            actual.copy(
                idProfesional = id,
                nombreProfesional = nombre,
                esFiltroManual = id != null || actual.nombreCliente != null || actual.idEstado != null
            )
        }
        cargar()
    }

    private fun cargarEstados() {
        viewModelScope.launch {
            runCatching { repo.getEstadosCita("HISTORIAL") }
                .onSuccess { _estados.value = it }
        }
    }

    private fun cargarEstadosActivas() {
        viewModelScope.launch {
            runCatching { repo.getEstadosCita("ACTIVAS") }
                .onSuccess { _estadosActivas.value = it }
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
        _filtros.value        = nuevos.copy(esFiltroManual = true)
        _mostrarFiltros.value = false
        cargar()
    }

    fun limpiarFiltros() {
        _filtros.value = filtroHoy()
        _fechaCalendario.value = LocalDate.now()
        cargar()
    }

    // ── Acciones de Ajuste de Estado ──────────────────────────────────────────

    fun abrirDialogAjuste(cita: Cita) {
        _citaParaAjuste.value = cita
        _mostrarDialogAjuste.value = true
        verificarSolapamiento(cita)
    }

    fun cerrarDialogAjuste() {
        _mostrarDialogAjuste.value = false
        _citaParaAjuste.value = null
        _haySolapamiento.value = false
    }

    private fun verificarSolapamiento(cita: Cita) {
        viewModelScope.launch {
            _verificandoSolapamiento.value = true
            try {
                val fechaIso = cita.fechaHoraInicio.take(10)
                val citasActivas = repo.getCitasActivas(
                    fechaDesde = fechaIso,
                    fechaHasta = fechaIso,
                    idProfesional = cita.idProfesional
                )
                val inicioA = cita.fechaHoraInicio
                val finA = cita.fechaHoraFin

                val tieneSolapamiento = citasActivas.any { c ->
                    c.id != cita.id && (inicioA < c.fechaHoraFin && finA > c.fechaHoraInicio)
                }
                _haySolapamiento.value = tieneSolapamiento
            } catch (e: Exception) {
                _haySolapamiento.value = false
            } finally {
                _verificandoSolapamiento.value = false
            }
        }
    }

    fun cambiarEstadoCita(
        idCita: Int,
        idEstado: Int,
        motivo: String?,
        idMetodoPago: Int? = null,
        idMetodoPagoDetalle: Int? = null
    ) {
        viewModelScope.launch {
            _guardandoAjuste.value = true
            try {
                val resp = repo.cambiarEstadoCita(
                    idCita = idCita,
                    idEstado = idEstado,
                    motivo = motivo,
                    idMetodoPago = idMetodoPago,
                    idMetodoPagoDetalle = idMetodoPagoDetalle
                )
                if (resp.ok) {
                    val msgFinal = if (_haySolapamiento.value) {
                        "${resp.mensaje ?: "Estado actualizado"}. (Nota: Se detectó solapamiento con otra cita agendada)"
                    } else {
                        resp.mensaje ?: "Estado de la cita actualizado correctamente"
                    }
                    _mensajeFeedback.emit(msgFinal)
                    cerrarDialogAjuste()
                    cargar()
                } else {
                    _mensajeFeedback.emit(resp.mensaje ?: "Error al actualizar el estado de la cita")
                }
            } catch (e: Exception) {
                _mensajeFeedback.emit(e.localizedMessage ?: "Error de red al actualizar el estado")
            } finally {
                _guardandoAjuste.value = false
            }
        }
    }
}