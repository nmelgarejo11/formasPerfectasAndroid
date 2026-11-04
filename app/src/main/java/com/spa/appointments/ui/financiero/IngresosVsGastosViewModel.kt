package com.spa.appointments.ui.financiero

import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spa.appointments.domain.model.IngresosVsGastosResponse
import com.spa.appointments.domain.model.Sede
import com.spa.appointments.domain.repository.FinancieroRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.time.LocalDate
import javax.inject.Inject

sealed class IngresosVsGastosUiState {
    object Idle    : IngresosVsGastosUiState()
    object Loading : IngresosVsGastosUiState()
    data class Success(val data: IngresosVsGastosResponse) : IngresosVsGastosUiState()
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

    init { cargarSedes() }

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
        cargar()
    }

    fun cambiarRango(inicio: LocalDate, fin: LocalDate) {
        _fechaInicio.value = inicio
        _fechaFin.value    = fin
        cargar()
    }

    fun cargar() {
        val sede = _sedeSeleccionada.value ?: return
        viewModelScope.launch {
            _uiState.value = IngresosVsGastosUiState.Loading
            try {
                val data = repo.getIngresosVsGastos(
                    idSede      = sede.id,
                    fechaInicio = _fechaInicio.value,
                    fechaFin    = _fechaFin.value
                )
                _uiState.value = IngresosVsGastosUiState.Success(data)
            } catch (e: Exception) {
                _uiState.value = IngresosVsGastosUiState.Error(e.message ?: "Error al cargar datos")
            }
        }
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
                    // Guarda en caché interna (no requiere permiso de storage)
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

    // ── Guarda en Downloads público — compatible desde API 1 ─────────────
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