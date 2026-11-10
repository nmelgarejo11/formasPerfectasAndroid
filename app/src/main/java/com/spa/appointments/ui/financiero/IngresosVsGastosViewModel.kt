package com.spa.appointments.ui.financiero

import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    object Idle    : IngresosVsGastosUiState()
    object Loading : IngresosVsGastosUiState()
    data class Success(val datos: IngresosVsGastosDatos) : IngresosVsGastosUiState()
    data class Error(val mensaje: String)                  : IngresosVsGastosUiState()
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
                _uiState.value = IngresosVsGastosUiState.Error(e.message ?: "Error al cargar sedes")
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

                val ingresosRaw = repo.listarIngresos(sede.id, inicioIso, finIso)
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
                _uiState.value = IngresosVsGastosUiState.Error(e.message ?: "Error al cargar datos")
            }
        }
    }

    private fun procesarYEmitirState() {
        val baseResponse = responseOriginalCache ?: return
        val currentFiltros = _filtros.value

        val filtrados = todosLosMovimientosCache.filter { mov ->
            mov.cumpleFiltro(currentFiltros)
        }

        val resumenFinal = if (currentFiltros.esFiltroActivo) {
            val totalIngresos = filtrados.filter { it.tipo == TipoMovimientoFiltro.INGRESO }.sumOf { it.valor }
            val totalGastos   = filtrados.filter { it.tipo == TipoMovimientoFiltro.GASTO }.sumOf { it.valor }
            val totalCitas    = filtrados.count { it.tipo == TipoMovimientoFiltro.INGRESO }
            val utilidad      = totalIngresos - totalGastos
            IngresosVsGastosResumen(
                fechaInicio   = _fechaInicio.value.toString(),
                fechaFin      = _fechaFin.value.toString(),
                totalIngresos = totalIngresos,
                totalGastos   = totalGastos,
                totalCitas    = totalCitas,
                utilidadNeta  = utilidad
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

    private fun MovimientoBalance.cumpleFiltro(f: FiltrosBalance): Boolean {
        if (f.tipoMovimiento != TipoMovimientoFiltro.TODOS && this.tipo != f.tipoMovimiento) {
            return false
        }
        if (f.idMetodoPago != null && this.idMetodoPago != f.idMetodoPago) {
            return false
        }
        if (!f.nombreMetodoPago.isNullOrBlank() && f.idMetodoPago == null) {
            val queryMp = f.nombreMetodoPago.trim().lowercase()
            val matchMp = this.metodoPago?.lowercase()?.contains(queryMp) == true
            if (!matchMp) return false
        }
        if (f.conceptoCategoria.isNotBlank()) {
            val query = f.conceptoCategoria.trim().lowercase()
            val matchConcepto = this.concepto.lowercase().contains(query)
            val matchCategoria = this.categoria?.lowercase()?.contains(query) == true
            if (!matchConcepto && !matchCategoria) return false
        }
        if (f.cliente.isNotBlank()) {
            val query = f.cliente.trim().lowercase()
            val matchCliente = this.cliente?.lowercase()?.contains(query) == true ||
                    this.concepto.lowercase().contains(query)
            if (!matchCliente) return false
        }
        if (f.idProfesional != null) {
            val matchProfId = (this.idProfesional == f.idProfesional)
            val matchProfName = f.nombreProfesional != null &&
                    this.profesional?.lowercase()?.contains(f.nombreProfesional.lowercase()) == true
            if (!matchProfId && !matchProfName) return false
        }
        if (!f.nombreProfesional.isNullOrBlank() && f.idProfesional == null) {
            val queryProf = f.nombreProfesional.trim().lowercase()
            val matchProf = this.profesional?.lowercase()?.contains(queryProf) == true ||
                    this.concepto.lowercase().contains(queryProf)
            if (!matchProf) return false
        }
        return true
    }

    // ── Acción 1: Guardar en Downloads ────────────────────────────────────
    fun guardarExcel(context: Context) {
        val sede   = _sedeSeleccionada.value ?: return
        val inicio = _fechaInicio.value.toString()
        val fin    = _fechaFin.value.toString()

        viewModelScope.launch {
            _descargaState.value = DescargaState.Cargando
            try {
                val uri = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                    val bytes = repo.exportarIngresosVsGastosExcel(
                        idSede      = sede.id,
                        fechaInicio = inicio,
                        fechaFin    = fin
                    )
                    guardarEnDescargas(
                        context = context,
                        nombre  = "IngresosVsGastos_${inicio}_${fin}.xlsx",
                        bytes   = bytes
                    )
                }
                _descargaState.value = if (uri != null) DescargaState.Listo(uri)
                else DescargaState.Error("No se pudo guardar el archivo")
            } catch (e: Exception) {
                android.util.Log.e("DESCARGA_EXCEL", "Error: ${e::class.simpleName} → ${e.message}", e)
                _descargaState.value = DescargaState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    // ── Acción 2: Compartir (ShareSheet — WhatsApp, correo, Drive, etc) ───
    fun compartirExcel(context: Context) {
        val sede   = _sedeSeleccionada.value ?: return
        val inicio = _fechaInicio.value.toString()
        val fin    = _fechaFin.value.toString()

        viewModelScope.launch {
            _descargaState.value = DescargaState.Cargando
            try {
                val uri = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                    val bytes = repo.exportarIngresosVsGastosExcel(
                        idSede      = sede.id,
                        fechaInicio = inicio,
                        fechaFin    = fin
                    )
                    val cacheDir = File(context.cacheDir, "excel").also { it.mkdirs() }
                    val archivo  = File(cacheDir, "IngresosVsGastos_${inicio}_${fin}.xlsx")
                    archivo.writeBytes(bytes)

                    androidx.core.content.FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.fileprovider",
                        archivo
                    )
                }

                if (uri != null) {
                    val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                        type    = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                        putExtra(android.content.Intent.EXTRA_STREAM, uri)
                        putExtra(android.content.Intent.EXTRA_SUBJECT,
                            "Ingresos vs Gastos $inicio - $fin")
                        addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    context.startActivity(
                        android.content.Intent.createChooser(intent, "Compartir reporte via…")
                    )
                    _descargaState.value = DescargaState.Idle
                } else {
                    _descargaState.value = DescargaState.Error("No se pudo generar el archivo")
                }

            } catch (e: Exception) {
                android.util.Log.e("DESCARGA_EXCEL", "Error: ${e::class.simpleName} → ${e.message}", e)
                _descargaState.value = DescargaState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    fun resetDescarga() { _descargaState.value = DescargaState.Idle }

    private fun guardarEnDescargas(context: Context, nombre: String, bytes: ByteArray): Uri? {
        return try {
            val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            if (!dir.exists()) dir.mkdirs()

            val archivo = File(dir, nombre)
            archivo.writeBytes(bytes)

            androidx.core.content.FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                archivo
            )
        } catch (e: Exception) {
            android.util.Log.e("DESCARGA_EXCEL", "Error guardando: ${e.message}", e)
            null
        }
    }
}
