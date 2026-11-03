package com.spa.appointments.ui.nomina

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spa.appointments.domain.model.*
import com.spa.appointments.domain.repository.NominaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
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
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    private val _uiState = MutableStateFlow(NominaUiState())
    val uiState: StateFlow<NominaUiState> = _uiState.asStateFlow()

    private val _eventos = MutableSharedFlow<NominaEvent>()
    val eventos: SharedFlow<NominaEvent> = _eventos.asSharedFlow()

    private var busquedaJob: Job? = null

    init {
        cargarPeriodosIniciales()
    }

    fun cargarPeriodosIniciales() {
        viewModelScope.launch(dispatcher) {
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
        busquedaJob = viewModelScope.launch(dispatcher) {
            delay(400) // Debounce 400ms
            val periodoId = _uiState.value.periodoSeleccionado?.id ?: return@launch
            cargarContratos(periodoId, nuevaBusqueda)
        }
    }

    fun cargarContratos(idPeriodoNomina: Int? = _uiState.value.periodoSeleccionado?.id, query: String = _uiState.value.busqueda) {
        if (idPeriodoNomina == null) return
        viewModelScope.launch(dispatcher) {
            _uiState.update { it.copy(cargandoContratos = true) }
            repository.listarContratosElegiblesNomina(idPeriodoNomina = idPeriodoNomina, busqueda = query.ifBlank { null })
                .onSuccess { lista ->
                    _uiState.update { it.copy(contratos = lista, cargandoContratos = false) }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(cargandoContratos = false) }
                    manejarError(error)
                }
        }
    }

    fun preliquidar(contrato: ContratoElegibleNomina) {
        val estado = EstadoNominaContrato.desde(contrato)
        if (estado is EstadoNominaContrato.Liquidada) {
            emitirMensaje("El contrato ya se encuentra liquidado. No se puede preliquidar.")
            return
        }

        val idContrato = contrato.idContratoLaboral
        if (!marcarAccionEnProgreso(idContrato)) return

        val periodoId = _uiState.value.periodoSeleccionado?.id
        if (periodoId == null) {
            desmarcarAccion(idContrato)
            return
        }

        viewModelScope.launch(dispatcher) {
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
            emitirMensaje("El contrato no está listo para liquidar. Debe preliquidarse primero.")
            return
        }

        val idPreliq = contrato.idPreliquidacionVigente
        if (idPreliq == null) {
            emitirMensaje("No existe una preliquidación vigente para este contrato.")
            return
        }

        val idContrato = contrato.idContratoLaboral
        if (!marcarAccionEnProgreso(idContrato)) return

        viewModelScope.launch(dispatcher) {
            ejecutarAccionConReintento503(
                accion = { repository.crearDesdePreliquidacion(CrearLiquidacionDesdePreliquidacionRequest(idPreliq)) },
                idContratoLaboral = idContrato,
                onExito = { respuesta ->
                    val idLiquidacion = respuesta.id ?: 0
                    cargarContratos()
                    _eventos.emit(NominaEvent.LiquidacionExitosa(idLiquidacion, respuesta.mensaje ?: "Liquidación creada exitosamente"))
                }
            )
        }
    }

    fun verDetallePreliquidacion(idPreliquidacion: Int) {
        viewModelScope.launch(dispatcher) {
            _uiState.update { it.copy(cargandoDetalle = true) }
            repository.obtenerPreliquidacionPorId(idPreliquidacion)
                .onSuccess { detalle ->
                    _uiState.update { it.copy(detallePreliquidacion = detalle, cargandoDetalle = false) }
                }
                .onFailure { error ->
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
            emitirMensaje("El motivo de anulación es obligatorio.")
            return
        }
        if (motivoLimpio.length > 1000) {
            emitirMensaje("El motivo no puede superar los 1000 caracteres.")
            return
        }

        viewModelScope.launch(dispatcher) {
            repository.anularLiquidacion(idLiquidacion, AnularLiquidacionRequest(motivoLimpio))
                .onSuccess { respuesta ->
                    emitirMensaje(respuesta.mensaje ?: "Liquidación anulada con éxito")
                    cargarContratos()
                }
                .onFailure { error ->
                    manejarError(error)
                }
        }
    }

    fun descargarDesprendible(idLiquidacion: Int) {
        viewModelScope.launch(dispatcher) {
            repository.descargarDesprendiblePdf(idLiquidacion)
                .onSuccess { archivoPdf ->
                    _eventos.emit(NominaEvent.AbrirPdfDesprendible(archivoPdf))
                }
                .onFailure { error ->
                    manejarError(error)
                }
        }
    }

    // ── Métodos auxiliares de control de estado y reintento ───────────────────

    private fun marcarAccionEnProgreso(idContratoLaboral: Int): Boolean {
        if (_uiState.value.contratosEnAccion.contains(idContratoLaboral)) {
            return false // Ya hay una acción en curso para este contrato
        }
        _uiState.update { it.copy(contratosEnAccion = it.contratosEnAccion + idContratoLaboral) }
        return true
    }

    private fun desmarcarAccion(idContratoLaboral: Int) {
        _uiState.update { it.copy(contratosEnAccion = it.contratosEnAccion - idContratoLaboral) }
    }

    private suspend fun <T> ejecutarAccionConReintento503(
        accion: suspend () -> Result<T>,
        idContratoLaboral: Int,
        onExito: suspend (T) -> Unit
    ) {
        try {
            var resultado = accion()

            if (resultado.isFailure) {
                val exception = resultado.exceptionOrNull()
                if (exception is NominaApiException && exception.esReintentable()) {
                    val waitSecs = (exception.retryAfterSegundos ?: 2).coerceIn(1, 5)
                    delay(waitSecs * 1000L)
                    resultado = accion() // Único reintento automático
                }
            }

            resultado
                .onSuccess { data ->
                    desmarcarAccion(idContratoLaboral)
                    onExito(data)
                }
                .onFailure { error ->
                    desmarcarAccion(idContratoLaboral)
                    manejarErrorAccion(error, idContratoLaboral)
                }
        } catch (e: Throwable) {
            desmarcarAccion(idContratoLaboral)
            manejarErrorAccion(e, idContratoLaboral)
        }
    }

    private fun manejarError(error: Throwable) {
        viewModelScope.launch(dispatcher) {
            if (error is NominaApiException) {
                when {
                    error.esPreliquidacionDesactualizada() -> {
                        _eventos.emit(NominaEvent.MostrarMensaje("La preliquidación está desactualizada. Se recargará la lista."))
                        cargarContratos()
                    }
                    error.esConflicto() -> {
                        _eventos.emit(NominaEvent.MostrarMensaje("Ya existe una liquidación vigente para este contrato y periodo."))
                        cargarContratos()
                    }
                    error.esNoEncontrado() -> {
                        _eventos.emit(NominaEvent.MostrarMensaje(error.message))
                        cargarContratos()
                    }
                    error.esReintentable() -> {
                        _eventos.emit(NominaEvent.MostrarMensaje("El servidor está ocupado, inténtalo de nuevo."))
                    }
                    else -> _eventos.emit(NominaEvent.MostrarMensaje(error.message))
                }
            } else {
                _eventos.emit(NominaEvent.MostrarMensaje(error.message ?: "Error desconocido"))
            }
        }
    }

    private fun manejarErrorAccion(error: Throwable, idContratoLaboral: Int) {
        viewModelScope.launch(dispatcher) {
            if (error is NominaApiException) {
                when {
                    error.esPreliquidacionDesactualizada() -> {
                        _eventos.emit(NominaEvent.MostrarRecalcular(
                            "La preliquidación está desactualizada (cambiaron datos de origen).",
                            idContratoLaboral
                        ))
                        cargarContratos()
                    }
                    error.esConflicto() -> {
                        _eventos.emit(NominaEvent.MostrarMensaje("Ya existe una liquidación vigente para este contrato y periodo."))
                        cargarContratos()
                    }
                    error.esNoEncontrado() -> {
                        _eventos.emit(NominaEvent.MostrarMensaje(error.message))
                        cargarContratos()
                    }
                    error.esReintentable() -> {
                        _eventos.emit(NominaEvent.MostrarMensaje("El servidor está ocupado, inténtalo de nuevo."))
                    }
                    else -> _eventos.emit(NominaEvent.MostrarMensaje(error.message))
                }
            } else {
                _eventos.emit(NominaEvent.MostrarMensaje(error.message ?: "Error en la operación"))
            }
        }
    }

    private fun emitirMensaje(mensaje: String) {
        viewModelScope.launch(dispatcher) {
            _eventos.emit(NominaEvent.MostrarMensaje(mensaje))
        }
    }
}
