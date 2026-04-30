package com.spa.appointments.ui.admin.profesionales

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spa.appointments.data.repository.ProfesionalesAdminRepository
import com.spa.appointments.domain.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

sealed class ProfesionalesUiState {
    object Idle : ProfesionalesUiState()
    object Loading : ProfesionalesUiState()
    object Success : ProfesionalesUiState()
    data class Error(val mensaje: String) : ProfesionalesUiState()
}

@HiltViewModel
class ProfesionalesAdminViewModel @Inject constructor(
    private val repo: ProfesionalesAdminRepository
) : ViewModel() {

    private val _profesionales = MutableStateFlow<List<ProfesionalAdmin>>(emptyList())
    val profesionales: StateFlow<List<ProfesionalAdmin>> = _profesionales

    private val _cargos = MutableStateFlow<List<CargoAdmin>>(emptyList())
    val cargos: StateFlow<List<CargoAdmin>> = _cargos

    private val _sedes = MutableStateFlow<List<SedeAdmin>>(emptyList())
    val sedes: StateFlow<List<SedeAdmin>> = _sedes

    // Asignaciones del profesional seleccionado
    private val _sedesAsignadas = MutableStateFlow<List<Int>>(emptyList())
    val sedesAsignadas: StateFlow<List<Int>> = _sedesAsignadas

    private val _serviciosAsignados = MutableStateFlow<List<ServicioProfesionalResponse>>(emptyList())
    val serviciosAsignados: StateFlow<List<ServicioProfesionalResponse>> = _serviciosAsignados

    private val _uiState = MutableStateFlow<ProfesionalesUiState>(ProfesionalesUiState.Idle)
    val uiState: StateFlow<ProfesionalesUiState> = _uiState

    // Agrega este flag después de los StateFlows
    private var datosInicializados = false

    // Reemplaza cargarDatos() y cargarAsignaciones() por este método unificado
    fun cargarDatosInicial(idProfesional: Int) {
        if (datosInicializados) return        // ← Bloquea doble llamada
        datosInicializados = true
        viewModelScope.launch {
            _uiState.value = ProfesionalesUiState.Loading
            try {
                val defProf      = async { repo.getProfesionales() }
                val defCargos    = async { repo.getCargos() }
                val defSedes     = async { repo.getSedesEmpresa() }
                val defAsigSedes = async { repo.getSedesProfesional(idProfesional) }
                val defAsigServ  = async { repo.getServiciosProfesional(idProfesional) }

                _profesionales.value      = defProf.await()
                _cargos.value             = defCargos.await()
                _sedes.value              = defSedes.await()
                _sedesAsignadas.value     = defAsigSedes.await()
                _serviciosAsignados.value = defAsigServ.await()

                _uiState.value = ProfesionalesUiState.Idle
            } catch (e: Exception) {
                _uiState.value = ProfesionalesUiState.Error(e.message ?: "Error al cargar datos")
            }
        }
    }

    // Mantén cargarDatos() solo para cuando se llama desde la lista de profesionales
    fun cargarDatos() {
        viewModelScope.launch {
            _uiState.value = ProfesionalesUiState.Loading
            try {
                val defProf   = async { repo.getProfesionales() }
                val defCargos = async { repo.getCargos() }
                val defSedes  = async { repo.getSedesEmpresa() }
                _profesionales.value = defProf.await()
                _cargos.value        = defCargos.await()
                _sedes.value         = defSedes.await()
                _uiState.value = ProfesionalesUiState.Idle
            } catch (e: Exception) {
                _uiState.value = ProfesionalesUiState.Error(e.message ?: "Error al cargar datos")
            }
        }
    }

    // ─── CRUD ─────────────────────────────────────────────────

    fun guardarProfesional(id: Int?, req: ProfesionalRequest) {
        viewModelScope.launch {
            _uiState.value = ProfesionalesUiState.Loading
            try {
                if (id == null) repo.crearProfesional(req)
                else repo.editarProfesional(id, req)
                cargarDatos()
                _uiState.value = ProfesionalesUiState.Success
            } catch (e: Exception) {
                _uiState.value = ProfesionalesUiState.Error(e.message ?: "Error al guardar")
            }
        }
    }

    fun toggleEstado(id: Int) {
        viewModelScope.launch {
            try {
                val nuevoEstado = repo.toggleEstado(id)
                _profesionales.value = _profesionales.value.map {
                    if (it.id == id) it.copy(estado = nuevoEstado) else it
                }
            } catch (e: Exception) {
                _uiState.value = ProfesionalesUiState.Error(e.message ?: "Error al cambiar estado")
            }
        }
    }

    fun subirFoto(id: Int, uri: Uri, context: Context) {
        viewModelScope.launch {
            _uiState.value = ProfesionalesUiState.Loading
            try {
                val archivo = uriToFile(uri, context)
                val url     = repo.subirFoto(id, archivo)
                _profesionales.value = _profesionales.value.map {
                    if (it.id == id) it.copy(foto = url) else it
                }
                _uiState.value = ProfesionalesUiState.Idle
            } catch (e: Exception) {
                _uiState.value = ProfesionalesUiState.Error(e.message ?: "Error al subir foto")
            }
        }
    }

    // ─── Asignaciones ─────────────────────────────────────────

    fun guardarSedes(idProfesional: Int, idsSedes: List<Int>) {
        viewModelScope.launch {
            _uiState.value = ProfesionalesUiState.Loading
            try {
                repo.guardarSedes(idProfesional, idsSedes)
                _sedesAsignadas.value = idsSedes
                _uiState.value = ProfesionalesUiState.Success
                kotlinx.coroutines.delay(1000)   // ← Muestra éxito 1 segundo
                _uiState.value = ProfesionalesUiState.Idle
            } catch (e: Exception) {
                _uiState.value = ProfesionalesUiState.Error(e.message ?: "Error al guardar sedes")
            }
        }
    }

    fun guardarServicios(idProfesional: Int, servicios: List<ServicioProfesionalItem>) {
        viewModelScope.launch {
            _uiState.value = ProfesionalesUiState.Loading
            try {
                repo.guardarServicios(idProfesional, servicios)
                _serviciosAsignados.value = servicios.map {
                    ServicioProfesionalResponse(it.idServicio, it.precio)
                }
                _uiState.value = ProfesionalesUiState.Success
                kotlinx.coroutines.delay(1000)   // ← Muestra éxito 1 segundo
                _uiState.value = ProfesionalesUiState.Idle
            } catch (e: Exception) {
                _uiState.value = ProfesionalesUiState.Error(e.message ?: "Error al guardar servicios")
            }
        }
    }

    fun resetState() { _uiState.value = ProfesionalesUiState.Idle }

    // ─── Utilidad ─────────────────────────────────────────────

    private fun uriToFile(uri: Uri, context: Context): File {
        val stream  = context.contentResolver.openInputStream(uri)!!
        val extension = context.contentResolver.getType(uri)
            ?.substringAfter("/") ?: "jpg"
        val temp = File.createTempFile("prof_foto_", ".$extension", context.cacheDir)
        temp.outputStream().use { stream.copyTo(it) }
        return temp
    }
}