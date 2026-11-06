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
    val cargandoDetalle: Boolean = false,

    // ── Novedades ─────────────────────────────────────
    val conceptos: List<ConceptoResponse> = emptyList(),
    val cargandoConceptos: Boolean = false,
    val contratoNovedadesSeleccionado: ContratoElegibleNomina? = null,
    val novedadesContrato: List<NovedadNominaResponse> = emptyList(),
    val cargandoNovedades: Boolean = false,
    val mostrarInactivasNovedades: Boolean = false,
    val novedadEnAccion: Boolean = false,
    val novedadFormDialogState: NovedadFormDialogState? = null
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

    // ── Novedades de Nómina ───────────────────────────────────

    fun cargarConceptosSiEsNecesario() {
        if (_uiState.value.conceptos.isNotEmpty() || _uiState.value.cargandoConceptos) return
        viewModelScope.launch(ioDispatcher) {
            _uiState.update { it.copy(cargandoConceptos = true) }
            repository.listarConceptosNomina()
                .onSuccess { lista ->
                    _uiState.update { it.copy(conceptos = lista, cargandoConceptos = false) }
                }
                .onFailure { error ->
                    if (error is CancellationException) return@onFailure
                    _uiState.update { it.copy(cargandoConceptos = false) }
                    manejarError(error)
                }
        }
    }

    fun abrirNovedadesContrato(contrato: ContratoElegibleNomina) {
        _uiState.update { it.copy(contratoNovedadesSeleccionado = contrato) }
        cargarConceptosSiEsNecesario()
        cargarNovedades(contrato.idContratoLaboral)
    }

    fun cerrarNovedadesContrato() {
        _uiState.update {
            it.copy(
                contratoNovedadesSeleccionado = null,
                novedadesContrato = emptyList(),
                novedadFormDialogState = null
            )
        }
    }

    fun toggleMostrarInactivasNovedades(mostrar: Boolean) {
        _uiState.update { it.copy(mostrarInactivasNovedades = mostrar) }
    }

    fun cargarNovedades(idContratoLaboral: Int? = null) {
        val idContrato = idContratoLaboral ?: _uiState.value.contratoNovedadesSeleccionado?.idContratoLaboral ?: return
        viewModelScope.launch(ioDispatcher) {
            _uiState.update { it.copy(cargandoNovedades = true) }
            repository.listarNovedadesNomina(idContrato)
                .onSuccess { lista ->
                    val periodo = _uiState.value.periodoSeleccionado
                    val fechaInicio = periodo?.fechaInicio?.take(10)
                    val fechaFin = periodo?.fechaFin?.take(10)

                    val enPeriodo = lista.filter { nov ->
                        val fechaNov = nov.fechaNovedad.take(10)
                        (fechaInicio == null || fechaNov >= fechaInicio) &&
                        (fechaFin == null || fechaNov <= fechaFin)
                    }
                    _uiState.update { it.copy(novedadesContrato = enPeriodo, cargandoNovedades = false) }
                }
                .onFailure { error ->
                    if (error is CancellationException) return@onFailure
                    _uiState.update { it.copy(cargandoNovedades = false) }
                    manejarError(error)
                }
        }
    }

    fun abrirFormularioNuevaNovedad() {
        val periodo = _uiState.value.periodoSeleccionado
        if (!periodo.esAbierto()) {
            emitirMensaje(NominaMensajes.ERROR_PERIODO_NO_ABIERTO)
            return
        }
        val fechaInicial = periodo?.fechaInicio?.take(10) ?: ""
        _uiState.update {
            it.copy(
                novedadFormDialogState = NovedadFormDialogState(
                    novedadEditar = null,
                    idConceptoSeleccionado = it.conceptos.firstOrNull()?.id,
                    fechaNovedad = fechaInicial,
                    valor = "",
                    dias = "",
                    observaciones = ""
                )
            )
        }
    }

    fun abrirFormularioEditarNovedad(novedad: NovedadNominaResponse) {
        val periodo = _uiState.value.periodoSeleccionado
        if (!periodo.esAbierto()) {
            emitirMensaje(NominaMensajes.ERROR_PERIODO_NO_ABIERTO)
            return
        }
        _uiState.update {
            it.copy(
                novedadFormDialogState = NovedadFormDialogState(
                    novedadEditar = novedad,
                    idConceptoSeleccionado = novedad.idConcepto,
                    fechaNovedad = novedad.fechaNovedad.take(10),
                    valor = if (novedad.valor % 1.0 == 0.0) novedad.valor.toLong().toString() else novedad.valor.toString(),
                    dias = novedad.dias?.let { d -> if (d % 1.0 == 0.0) d.toLong().toString() else d.toString() } ?: "",
                    observaciones = novedad.observaciones ?: ""
                )
            )
        }
    }

    fun cerrarFormularioNovedad() {
        _uiState.update { it.copy(novedadFormDialogState = null) }
    }

    fun guardarNovedad(
        idConcepto: Int,
        fechaNovedad: String,
        valorStr: String,
        diasStr: String,
        observacionesStr: String
    ) {
        val periodo = _uiState.value.periodoSeleccionado
        if (!periodo.esAbierto()) {
            emitirMensaje(NominaMensajes.ERROR_PERIODO_NO_ABIERTO)
            return
        }

        val contrato = _uiState.value.contratoNovedadesSeleccionado
        if (contrato == null) return

        val concepto = _uiState.value.conceptos.firstOrNull { it.id == idConcepto }
        if (concepto == null) {
            emitirMensaje("Debe seleccionar un concepto válido.")
            return
        }

        val requiereDias = concepto.descuentaDiasSueldo
        val dias = diasStr.replace(",", ".").toDoubleOrNull()
        if (requiereDias && (dias == null || dias <= 0)) {
            emitirMensaje("El concepto elegido exige ingresar una cantidad de días mayor a 0.")
            return
        }

        var valor = valorStr.replace(",", ".").toDoubleOrNull()
        if (requiereDias && (valor == null || valor <= 0) && contrato.salarioBase > 0 && dias != null) {
            valor = (contrato.salarioBase / 30.0) * dias
        }

        if (valor == null || valor <= 0) {
            emitirMensaje("Debe ingresar un valor numérico mayor a cero para la novedad.")
            return
        }

        val fechaLimpia = fechaNovedad.trim().take(10)
        val fInicio = periodo?.fechaInicio?.take(10)
        val fFin = periodo?.fechaFin?.take(10)
        if (fInicio != null && fFin != null && (fechaLimpia < fInicio || fechaLimpia > fFin)) {
            emitirMensaje("La fecha de la novedad debe estar dentro del rango del período ($fInicio a $fFin).")
            return
        }

        val observaciones = observacionesStr.trim().ifBlank { null }
        val formState = _uiState.value.novedadFormDialogState
        val esEdicion = formState?.novedadEditar != null
        val novedadId = formState?.novedadEditar?.id

        viewModelScope.launch(ioDispatcher) {
            _uiState.update { it.copy(novedadEnAccion = true) }
            val res = if (esEdicion && novedadId != null) {
                repository.actualizarNovedadNomina(
                    novedadId,
                    ActualizarNovedadNominaRequest(
                        idConcepto = idConcepto,
                        fechaNovedad = fechaLimpia,
                        valor = valor,
                        dias = if (requiereDias) dias else null,
                        observaciones = observaciones
                    )
                )
            } else {
                repository.crearNovedadNomina(
                    CrearNovedadNominaRequest(
                        idContratoLaboral = contrato.idContratoLaboral,
                        idConcepto = idConcepto,
                        fechaNovedad = fechaLimpia,
                        valor = valor,
                        dias = if (requiereDias) dias else null,
                        observaciones = observaciones
                    )
                )
            }

            res.onSuccess { respuesta ->
                _uiState.update { it.copy(novedadEnAccion = false, novedadFormDialogState = null) }
                emitirMensaje(respuesta.mensaje ?: if (esEdicion) "Novedad actualizada con éxito" else "Novedad creada con éxito")

                if (periodo != null) {
                    repository.preliquidar(PreliquidarContratoRequest(contrato.idContratoLaboral, periodo.id))
                }
                cargarContratos()
                cargarNovedades(contrato.idContratoLaboral)
            }.onFailure { error ->
                if (error is CancellationException) return@onFailure
                _uiState.update { it.copy(novedadEnAccion = false) }
                manejarError(error)
            }
        }
    }

    fun anularNovedad(novedad: NovedadNominaResponse) {
        val periodo = _uiState.value.periodoSeleccionado
        if (!periodo.esAbierto()) {
            emitirMensaje(NominaMensajes.ERROR_PERIODO_NO_ABIERTO)
            return
        }

        val contrato = _uiState.value.contratoNovedadesSeleccionado ?: return

        viewModelScope.launch(ioDispatcher) {
            _uiState.update { it.copy(novedadEnAccion = true) }
            repository.cambiarEstadoNovedadNomina(novedad.id, nuevoEstado = false)
                .onSuccess { respuesta ->
                    _uiState.update { it.copy(novedadEnAccion = false) }
                    emitirMensaje(respuesta.mensaje ?: "Novedad anulada con éxito")

                    if (periodo != null) {
                        repository.preliquidar(PreliquidarContratoRequest(contrato.idContratoLaboral, periodo.id))
                    }
                    cargarContratos()
                    cargarNovedades(contrato.idContratoLaboral)
                }
                .onFailure { error ->
                    if (error is CancellationException) return@onFailure
                    _uiState.update { it.copy(novedadEnAccion = false) }
                    manejarError(error)
                }
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
