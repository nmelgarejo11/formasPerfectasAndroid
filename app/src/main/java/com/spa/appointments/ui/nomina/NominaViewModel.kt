package com.spa.appointments.ui.nomina

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spa.appointments.di.IoDispatcher
import com.spa.appointments.domain.model.*
import com.spa.appointments.domain.repository.NominaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

data class NominaUiState(
    val periodos: List<PeriodoNominaItem> = emptyList(),
    val periodoSeleccionado: PeriodoNominaItem? = null,
    val busqueda: String = "",
    val contratos: List<ContratoElegibleNomina> = emptyList(),
    val cargandoPeriodos: Boolean = false,
    val cargandoContratos: Boolean = false,
    val contratosEnAccion: Set<Int> = emptySet(),
    val liquidacionesEnAccion: Set<Int> = emptySet(),
    val detallePreliquidacion: PreliquidacionResponse? = null,
    val cargandoDetalle: Boolean = false
)

sealed class NominaEvent {
    data class MostrarMensaje(val mensaje: String) : NominaEvent()
    data class MostrarRecalcular(val mensaje: String, val idContratoLaboral: Int) : NominaEvent()
    data class AbrirPdfDesprendible(val file: File) : NominaEvent()
    data class LiquidacionExitosa(val idLiquidacion: Int, val mensaje: String) : NominaEvent()
}

@HiltViewModel
class NominaViewModel @Inject constructor(
    private val repository: NominaRepository,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {

    private val _uiState = MutableStateFlow(NominaUiState())
    val uiState: StateFlow<NominaUiState> = _uiState.asStateFlow()

    private val _eventos = Channel<NominaEvent>(Channel.BUFFERED)
    val eventos: Flow<NominaEvent> = _eventos.receiveAsFlow()

    private var busquedaJob: Job? = null
    private var cargarContratosJob: Job? = null

    init {
        cargarPeriodosIniciales()
    }

    fun cargarPeriodosIniciales() {
        viewModelScope.launch(ioDispatcher) {
            _uiState.update { it.copy(cargandoPeriodos = true) }
            repository.listarPeriodosNomina(soloAbiertos = true)
                .onSuccess { lista ->
                    val primerPeriodo = lista.firstOrNull()
                    _uiState.update {
                        it.copy(
                            periodos = lista,
                            periodoSeleccionado = primerPeriodo,
                            cargandoPeriodos = false
                        )
                    }
                    if (primerPeriodo != null) {
                        cargarContratos(primerPeriodo.id, _uiState.value.busqueda)
                    } else {
                        _uiState.update { state -> state.copy(contratos = emptyList()) }
                    }
                }
                .onFailure { error ->
                    if (error is CancellationException) return@onFailure
                    _uiState.update { it.copy(cargandoPeriodos = false) }
                    manejarError(error)
                }
        }
    }

    fun seleccionarPeriodo(periodo: PeriodoNominaItem) {
        if (_uiState.value.periodoSeleccionado?.id == periodo.id) return
        _uiState.update { it.copy(periodoSeleccionado = periodo) }
        cargarContratos(periodo.id, _uiState.value.busqueda)
    }

    fun actualizarBusqueda(nuevaBusqueda: String) {
        _uiState.update { it.copy(busqueda = nuevaBusqueda) }
        busquedaJob?.cancel()
        busquedaJob = viewModelScope.launch(ioDispatcher) {
            delay(400) // Debounce 400ms
            val periodoId = _uiState.value.periodoSeleccionado?.id ?: return@launch
            cargarContratos(periodoId, nuevaBusqueda)
        }
    }

    fun cargarContratos(idPeriodoNomina: Int? = _uiState.value.periodoSeleccionado?.id, query: String = _uiState.value.busqueda) {
        if (idPeriodoNomina == null) return
        cargarContratosJob?.cancel()
        cargarContratosJob = viewModelScope.launch(ioDispatcher) {
            _uiState.update { it.copy(cargandoContratos = true) }
            val res = repository.listarContratosElegiblesNomina(idPeriodoNomina = idPeriodoNomina, busqueda = query.ifBlank { null })

            // Descarta respuesta obsoleta si el periodo cambió durante la llamada
            if (_uiState.value.periodoSeleccionado?.id != idPeriodoNomina) {
                return@launch
            }

            res.onSuccess { lista ->
                _uiState.update { it.copy(contratos = lista, cargandoContratos = false) }
            }.onFailure { error ->
                if (error is CancellationException) return@onFailure
                _uiState.update { it.copy(cargandoContratos = false) }
                manejarError(error)
            }
        }
    }

    fun preliquidar(contrato: ContratoElegibleNomina) {
        val estado = EstadoNominaContrato.desde(contrato)
        if (estado is EstadoNominaContrato.Liquidada) {
            emitirMensaje(NominaMensajes.ERROR_CONTRATO_LIQUIDADO)
            return
        }

        val idContrato = contrato.idContratoLaboral
        if (!marcarContratoEnAccion(idContrato)) return

        val periodoId = _uiState.value.periodoSeleccionado?.id
        if (periodoId == null) {
            desmarcarContratoEnAccion(idContrato)
            return
        }

        viewModelScope.launch(ioDispatcher) {
            ejecutarAccionConReintento503(
                accion = { repository.preliquidar(PreliquidarContratoRequest(idContrato, periodoId)) },
                idContratoLaboral = idContrato,
                onExito = {
                    cargarContratos()
                }
            )
        }
    }

    fun liquidar(contrato: ContratoElegibleNomina) {
        val estado = EstadoNominaContrato.desde(contrato)
        if (estado !is EstadoNominaContrato.PreliquidadaLista) {
            emitirMensaje(NominaMensajes.ERROR_CONTRATO_NO_LISTO)
            return
        }

        val idPreliq = contrato.idPreliquidacionVigente
        if (idPreliq == null) {
            emitirMensaje(NominaMensajes.ERROR_SIN_PRELIQUIDACION_VIGENTE)
            return
        }

        val idContrato = contrato.idContratoLaboral
        if (!marcarContratoEnAccion(idContrato)) return

        viewModelScope.launch(ioDispatcher) {
            ejecutarAccionConReintento503(
                accion = { repository.crearDesdePreliquidacion(CrearLiquidacionDesdePreliquidacionRequest(idPreliq)) },
                idContratoLaboral = idContrato,
                onExito = { respuesta ->
                    val idLiquidacion = respuesta.id ?: 0
                    cargarContratos()
                    _eventos.send(NominaEvent.LiquidacionExitosa(idLiquidacion, respuesta.mensaje ?: "Liquidación creada exitosamente"))
                }
            )
        }
    }

    fun verDetallePreliquidacion(idPreliquidacion: Int) {
        viewModelScope.launch(ioDispatcher) {
            _uiState.update { it.copy(cargandoDetalle = true) }
            repository.obtenerPreliquidacionPorId(idPreliquidacion)
                .onSuccess { detalle ->
                    _uiState.update { it.copy(detallePreliquidacion = detalle, cargandoDetalle = false) }
                }
                .onFailure { error ->
                    if (error is CancellationException) return@onFailure
                    _uiState.update { it.copy(cargandoDetalle = false) }
                    manejarError(error)
                }
        }
    }

    fun cerrarDetallePreliquidacion() {
        _uiState.update { it.copy(detallePreliquidacion = null) }
    }

    fun anularLiquidacion(idLiquidacion: Int, motivo: String) {
        val motivoLimpio = motivo.trim()
        if (motivoLimpio.isBlank()) {
            emitirMensaje(NominaMensajes.ERROR_MOTIVO_ANULACION_OBLIGATORIO)
            return
        }
        if (motivoLimpio.length > 1000) {
            emitirMensaje(NominaMensajes.ERROR_MOTIVO_ANULACION_LARGO)
            return
        }

        if (!marcarLiquidacionEnAccion(idLiquidacion)) return

        viewModelScope.launch(ioDispatcher) {
            ejecutarAccionConReintento503(
                accion = { repository.anularLiquidacion(idLiquidacion, AnularLiquidacionRequest(motivoLimpio)) },
                idLiquidacion = idLiquidacion,
                onExito = { respuesta ->
                    emitirMensaje(respuesta.mensaje ?: "Liquidación anulada con éxito")
                    cargarContratos()
                }
            )
        }
    }

    fun descargarDesprendible(idLiquidacion: Int) {
        if (!marcarLiquidacionEnAccion(idLiquidacion)) return

        viewModelScope.launch(ioDispatcher) {
            ejecutarAccionConReintento503(
                accion = { repository.descargarDesprendiblePdf(idLiquidacion) },
                idLiquidacion = idLiquidacion,
                onExito = { archivoPdf ->
                    _eventos.send(NominaEvent.AbrirPdfDesprendible(archivoPdf))
                }
            )
        }
    }

    // ── Control de acciones e idempotencia ────────────────────────────────────

    private fun marcarContratoEnAccion(idContratoLaboral: Int): Boolean {
        if (_uiState.value.contratosEnAccion.contains(idContratoLaboral)) return false
        _uiState.update { it.copy(contratosEnAccion = it.contratosEnAccion + idContratoLaboral) }
        return true
    }

    private fun desmarcarContratoEnAccion(idContratoLaboral: Int) {
        _uiState.update { it.copy(contratosEnAccion = it.contratosEnAccion - idContratoLaboral) }
    }

    private fun marcarLiquidacionEnAccion(idLiquidacion: Int): Boolean {
        if (_uiState.value.liquidacionesEnAccion.contains(idLiquidacion)) return false
        _uiState.update { it.copy(liquidacionesEnAccion = it.liquidacionesEnAccion + idLiquidacion) }
        return true
    }

    private fun desmarcarLiquidacionEnAccion(idLiquidacion: Int) {
        _uiState.update { it.copy(liquidacionesEnAccion = it.liquidacionesEnAccion - idLiquidacion) }
    }

    private suspend fun <T> ejecutarAccionConReintento503(
        accion: suspend () -> Result<T>,
        idContratoLaboral: Int? = null,
        idLiquidacion: Int? = null,
        onExito: suspend (T) -> Unit
    ) {
        try {
            var resultado = accion()

            if (resultado.isFailure) {
                val exception = resultado.exceptionOrNull()
                if (exception is NominaApiException && exception.esReintentable()) {
                    val waitSecs = (exception.retryAfterSegundos ?: 2).coerceIn(1, 5)
                    delay(waitSecs * 1000L)
                    resultado = accion() // Único reintento
                }
            }

            resultado
                .onSuccess { data ->
                    if (idContratoLaboral != null) desmarcarContratoEnAccion(idContratoLaboral)
                    if (idLiquidacion != null) desmarcarLiquidacionEnAccion(idLiquidacion)
                    onExito(data)
                }
                .onFailure { error ->
                    if (error is CancellationException) throw error
                    if (idContratoLaboral != null) desmarcarContratoEnAccion(idContratoLaboral)
                    if (idLiquidacion != null) desmarcarLiquidacionEnAccion(idLiquidacion)
                    manejarErrorAccion(error, idContratoLaboral)
                }
        } catch (e: Throwable) {
            if (e is CancellationException) throw e
            if (idContratoLaboral != null) desmarcarContratoEnAccion(idContratoLaboral)
            if (idLiquidacion != null) desmarcarLiquidacionEnAccion(idLiquidacion)
            manejarErrorAccion(e, idContratoLaboral)
        }
    }

    private fun manejarError(error: Throwable) {
        if (error is CancellationException) return
        viewModelScope.launch(ioDispatcher) {
            if (error is NominaApiException) {
                when {
                    error.esPreliquidacionDesactualizada() -> {
                        _eventos.send(NominaEvent.MostrarMensaje(error.message))
                        cargarContratos()
                    }
                    error.esConflicto() -> {
                        _eventos.send(NominaEvent.MostrarMensaje(error.message))
                        cargarContratos()
                    }
                    error.esNoEncontrado() -> {
                        _eventos.send(NominaEvent.MostrarMensaje(error.message))
                        cargarContratos()
                    }
                    error.esReintentable() -> {
                        _eventos.send(NominaEvent.MostrarMensaje(NominaMensajes.ERROR_SERVIDOR_OCUPADO))
                    }
                    else -> _eventos.send(NominaEvent.MostrarMensaje(error.message))
                }
            } else {
                _eventos.send(NominaEvent.MostrarMensaje(NominaMensajes.mapearError(error)))
            }
        }
    }

    private fun manejarErrorAccion(error: Throwable, idContratoLaboral: Int?) {
        if (error is CancellationException) return
        viewModelScope.launch(ioDispatcher) {
            if (error is NominaApiException) {
                when {
                    error.esPreliquidacionDesactualizada() -> {
                        if (idContratoLaboral != null) {
                            _eventos.send(NominaEvent.MostrarRecalcular(
                                NominaMensajes.ERROR_PRELIQUIDACION_DESACTUALIZADA_RECALCULAR,
                                idContratoLaboral
                            ))
                        } else {
                            _eventos.send(NominaEvent.MostrarMensaje(error.message))
                        }
                        cargarContratos()
                    }
                    error.esConflicto() -> {
                        _eventos.send(NominaEvent.MostrarMensaje(error.message))
                        cargarContratos()
                    }
                    error.esNoEncontrado() -> {
                        _eventos.send(NominaEvent.MostrarMensaje(error.message))
                        cargarContratos()
                    }
                    error.esReintentable() -> {
                        _eventos.send(NominaEvent.MostrarMensaje(NominaMensajes.ERROR_SERVIDOR_OCUPADO))
                    }
                    else -> _eventos.send(NominaEvent.MostrarMensaje(error.message))
                }
            } else {
                _eventos.send(NominaEvent.MostrarMensaje(NominaMensajes.mapearError(error)))
            }
        }
    }

    private fun emitirMensaje(mensaje: String) {
        viewModelScope.launch(ioDispatcher) {
            _eventos.send(NominaEvent.MostrarMensaje(mensaje))
        }
    }
}
