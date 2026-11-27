package com.spa.appointments.ui.financiero

import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spa.appointments.core.utils.ApiErrorMapper
import com.spa.appointments.domain.model.FiltrosBalance
import com.spa.appointments.domain.model.IngresosVsGastosDatos
import com.spa.appointments.domain.model.IngresosVsGastosResponse
import com.spa.appointments.domain.model.IngresosVsGastosResumen
import com.spa.appointments.domain.model.MetodoPago
import com.spa.appointments.domain.model.MovimientoBalance
import com.spa.appointments.domain.model.Profesional
import com.spa.appointments.domain.model.Sede
import com.spa.appointments.domain.model.TipoMovimientoFiltro
import com.spa.appointments.domain.repository.FinancieroRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.time.LocalDate
import javax.inject.Inject

sealed class IngresosVsGastosUiState {
    object Idle      : IngresosVsGastosUiState()
    object Loading   : IngresosVsGastosUiState()
    data class Success(val datos: IngresosVsGastosDatos) : IngresosVsGastosUiState()
    data class Error(val mensaje: String)                  : IngresosVsGastosUiState()
    object Forbidden : IngresosVsGastosUiState()
}

sealed interface DescargaState {
    object Idle     : DescargaState
    object Cargando : DescargaState
    data class Error(val mensaje: String) : DescargaState
    data class Listo(val uri: Uri)        : DescargaState
}

@HiltViewModel
class IngresosVsGastosViewModel @Inject constructor(
    private val repo: FinancieroRepository
) : ViewModel() {

    private val _descargaState = MutableStateFlow<DescargaState>(DescargaState.Idle)
    val descargaState: StateFlow<DescargaState> = _descargaState.asStateFlow()

    private val _uiState = MutableStateFlow<IngresosVsGastosUiState>(IngresosVsGastosUiState.Idle)
    val uiState: StateFlow<IngresosVsGastosUiState> = _uiState

    private val _sedes = MutableStateFlow<List<Sede>>(emptyList())
    val sedes: StateFlow<List<Sede>> = _sedes

    private val _sedeSeleccionada = MutableStateFlow<Sede?>(null)
    val sedeSeleccionada: StateFlow<Sede?> = _sedeSeleccionada

    private val _fechaInicio = MutableStateFlow(LocalDate.now().withDayOfMonth(1))
    val fechaInicio: StateFlow<LocalDate> = _fechaInicio

    private val _fechaFin = MutableStateFlow(LocalDate.now())
    val fechaFin: StateFlow<LocalDate> = _fechaFin

    private val _filtros = MutableStateFlow(FiltrosBalance())
    val filtros: StateFlow<FiltrosBalance> = _filtros.asStateFlow()

    private val _metodosPago = MutableStateFlow<List<MetodoPago>>(emptyList())
    val metodosPago: StateFlow<List<MetodoPago>> = _metodosPago.asStateFlow()

    private val _profesionales = MutableStateFlow<List<Profesional>>(emptyList())
    val profesionales: StateFlow<List<Profesional>> = _profesionales.asStateFlow()

    private val _mostrarDialogFiltros = MutableStateFlow(false)
    val mostrarDialogFiltros: StateFlow<Boolean> = _mostrarDialogFiltros.asStateFlow()

    private var todosLosMovimientosCache: List<MovimientoBalance> = emptyList()
    private var responseOriginalCache: IngresosVsGastosResponse? = null

    init {
        cargarFiltrosAuxiliares()
        cargarSedes()
    }

    private fun cargarFiltrosAuxiliares() {
        viewModelScope.launch {
            runCatching { repo.getMetodosPago() }
                .onSuccess { _metodosPago.value = it }
            runCatching { repo.getProfesionales(_sedeSeleccionada.value?.id) }
                .onSuccess { _profesionales.value = it }
        }
    }

    fun cargarSedes() {
        viewModelScope.launch {
            _uiState.value = IngresosVsGastosUiState.Loading
            try {
                val lista = repo.getSedes()
                _sedes.value = lista
                if (_sedeSeleccionada.value == null && lista.isNotEmpty()) {
                    _sedeSeleccionada.value = lista.first()
                    cargar()
                } else {
                    _uiState.value = IngresosVsGastosUiState.Idle
                }
            } catch (e: Exception) {
                if (ApiErrorMapper.esForbidden(e)) {
                    _uiState.value = IngresosVsGastosUiState.Forbidden
                } else {
                    _uiState.value = IngresosVsGastosUiState.Error(ApiErrorMapper.parsearMensajeError(e))
                }
            }
        }
    }

    fun seleccionarSede(sede: Sede) {
        _sedeSeleccionada.value = sede
        _filtros.update { it.copy(idProfesional = null, nombreProfesional = null) }
        viewModelScope.launch {
            runCatching { repo.getProfesionales(sede.id) }
                .onSuccess { _profesionales.value = it }
        }
        cargar()
    }

    fun cambiarRango(inicio: LocalDate, fin: LocalDate) {
        _fechaInicio.value = inicio
        _fechaFin.value    = fin
        _filtros.update { it.copy(fechaInicio = inicio, fechaFin = fin) }
        cargar()
    }

    fun toggleDialogFiltros() {
        _mostrarDialogFiltros.update { !it }
    }

    fun aplicarFiltros(nuevosFiltros: FiltrosBalance) {
        _filtros.value = nuevosFiltros.copy(
            fechaInicio = _fechaInicio.value,
            fechaFin    = _fechaFin.value,
            esFiltroManual = true
        )
        _mostrarDialogFiltros.value = false
        procesarYEmitirState()
    }

    fun limpiarFiltros() {
        _filtros.value = FiltrosBalance(
            fechaInicio = _fechaInicio.value,
            fechaFin    = _fechaFin.value,
            esFiltroManual = false
        )
        _mostrarDialogFiltros.value = false
        procesarYEmitirState()
    }

    fun cargar() {
        val sede = _sedeSeleccionada.value ?: return
        viewModelScope.launch {
            _uiState.value = IngresosVsGastosUiState.Loading
            try {
                val inicioIso = _fechaInicio.value.toString()
                val finIso = _fechaFin.value.toString()

                val responseVs = repo.getIngresosVsGastos(
                    idSede      = sede.id,
                    fechaInicio = _fechaInicio.value,
                    fechaFin    = _fechaFin.value
                )
                responseOriginalCache = responseVs

                val ingresosRaw = repo.listarIngresos(sede.id, inicioIso, finIso, incluirCitas = true)
                val gastosRaw   = repo.listarGastos(sede.id, inicioIso, finIso)

                val movsIngresos = ingresosRaw.map { ing ->
                    MovimientoBalance(
                        id                  = "ING_${ing.id}",
                        tipo                = TipoMovimientoFiltro.INGRESO,
                        concepto            = ing.concepto,
                        valor               = ing.valor,
                        fecha               = ing.fechaIngreso,
                        idMetodoPago        = ing.idMetodoPago,
                        metodoPago          = ing.metodoPago,
                        idMetodoPagoDetalle = ing.idMetodoPagoDetalle,
                        metodoPagoDetalle   = ing.metodoPagoDetalle,
                        idSede              = ing.idSede,
                        sede                = ing.sede
                    )
                }

                val movsGastos = gastosRaw.map { gst ->
                    MovimientoBalance(
                        id                  = "GST_${gst.id}",
                        tipo                = TipoMovimientoFiltro.GASTO,
                        concepto            = gst.concepto,
                        valor               = gst.valor,
                        fecha               = gst.fechaGasto,
                        idMetodoPago        = gst.idMetodoPago,
                        metodoPago          = gst.metodoPago,
                        idMetodoPagoDetalle = gst.idMetodoPagoDetalle,
                        metodoPagoDetalle   = gst.metodoPagoDetalle,
                        idSede              = gst.idSede,
                        sede                = gst.sede
                    )
                }

                todosLosMovimientosCache = (movsIngresos + movsGastos).sortedByDescending { it.fecha }
                procesarYEmitirState()
            } catch (e: Exception) {
                if (ApiErrorMapper.esForbidden(e)) {
                    _uiState.value = IngresosVsGastosUiState.Forbidden
                } else {
                    _uiState.value = IngresosVsGastosUiState.Error(ApiErrorMapper.parsearMensajeError(e))
                }
            }
        }
    }

    private fun MovimientoBalance.cumpleFiltro(f: FiltrosBalance): Boolean {
        if (f.tipoMovimiento != TipoMovimientoFiltro.TODOS && tipo != f.tipoMovimiento) return false
        if (f.idMetodoPago != null && idMetodoPago != f.idMetodoPago) return false
        if (f.conceptoCategoria.isNotBlank() && !concepto.contains(f.conceptoCategoria, ignoreCase = true)) return false
        if (f.cliente.isNotBlank()) {
            val coincideClienteProp = cliente?.contains(f.cliente, ignoreCase = true) ?: false
            val coincideConcepto = concepto.contains(f.cliente, ignoreCase = true)
            if (!coincideClienteProp && !coincideConcepto) return false
        }
        if (f.idProfesional != null && idProfesional != f.idProfesional) return false
        return true
    }

    private fun procesarYEmitirState() {
        val baseResponse = responseOriginalCache ?: return
        val currentFiltros = _filtros.value

        val filtrados = todosLosMovimientosCache.filter { mov ->
            mov.cumpleFiltro(currentFiltros)
        }

        val resumenFinal = if (currentFiltros.esFiltroActivo) {
            val totalIngresos = filtrados.filter { it.tipo == TipoMovimientoFiltro.INGRESO }.sumOf { it.valor }
            val totalGastos = filtrados.filter { it.tipo == TipoMovimientoFiltro.GASTO }.sumOf { it.valor }
            IngresosVsGastosResumen(
                fechaInicio   = _fechaInicio.value.toString(),
                fechaFin      = _fechaFin.value.toString(),
                totalIngresos = totalIngresos,
                totalGastos   = totalGastos,
                totalCitas    = baseResponse.resumen.totalCitas,
                utilidadNeta  = totalIngresos - totalGastos
            )
        } else {
            baseResponse.resumen
        }

        _uiState.value = IngresosVsGastosUiState.Success(
            IngresosVsGastosDatos(
                resumen              = resumenFinal,
                detalle              = baseResponse.detalle,
                todosMovimientos     = todosLosMovimientosCache,
                movimientosFiltrados = filtrados,
                filtros              = currentFiltros
            )
        )
    }

    fun guardarExcel(context: Context) {
        val sede = _sedeSeleccionada.value ?: return
        viewModelScope.launch {
            _descargaState.value = DescargaState.Cargando
            try {
                val bytes = repo.exportarIngresosVsGastosExcel(
                    idSede      = sede.id,
                    fechaInicio = _fechaInicio.value.toString(),
                    fechaFin    = _fechaFin.value.toString()
                )
                val fileName = "Reporte_Balance_${sede.nombre.replace(" ", "_")}_${_fechaInicio.value}_${_fechaFin.value}.xlsx"
                val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), fileName)
                file.writeBytes(bytes)

                val uri = androidx.core.content.FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )
                _descargaState.value = DescargaState.Listo(uri)
            } catch (e: Exception) {
                _descargaState.value = DescargaState.Error(ApiErrorMapper.parsearMensajeError(e))
            }
        }
    }

    fun compartirExcel(context: Context) {
        val sede = _sedeSeleccionada.value ?: return
        viewModelScope.launch {
            _descargaState.value = DescargaState.Cargando
            try {
                val bytes = repo.exportarIngresosVsGastosExcel(
                    idSede      = sede.id,
                    fechaInicio = _fechaInicio.value.toString(),
                    fechaFin    = _fechaFin.value.toString()
                )
                val fileName = "Reporte_Balance_${sede.nombre.replace(" ", "_")}_${_fechaInicio.value}_${_fechaFin.value}.xlsx"
                val file = File(context.cacheDir, fileName)
                file.writeBytes(bytes)

                val uri = androidx.core.content.FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )

                val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                    type = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                    putExtra(android.content.Intent.EXTRA_STREAM, uri)
                    addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(android.content.Intent.createChooser(intent, "Compartir reporte Excel"))
                _descargaState.value = DescargaState.Idle
            } catch (e: Exception) {
                _descargaState.value = DescargaState.Error(ApiErrorMapper.parsearMensajeError(e))
            }
        }
    }

    fun resetDescarga() {
        _descargaState.value = DescargaState.Idle
    }
}
