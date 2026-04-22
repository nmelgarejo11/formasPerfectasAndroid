package com.spa.appointments.ui.admin.catalogos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spa.appointments.data.repository.CatalogosAdminRepository
import com.spa.appointments.domain.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class CatalogosUiState {
    object Idle : CatalogosUiState()
    object Loading : CatalogosUiState()
    object Success : CatalogosUiState()
    data class Error(val mensaje: String) : CatalogosUiState()
}

@HiltViewModel
class CatalogosViewModel @Inject constructor(
    private val repo: CatalogosAdminRepository
) : ViewModel() {

    private val _categorias = MutableStateFlow<List<CategoriaAdmin>>(emptyList())
    val categorias: StateFlow<List<CategoriaAdmin>> = _categorias

    private val _servicios = MutableStateFlow<List<ServicioAdmin>>(emptyList())
    val servicios: StateFlow<List<ServicioAdmin>> = _servicios

    private val _uiState = MutableStateFlow<CatalogosUiState>(CatalogosUiState.Idle)
    val uiState: StateFlow<CatalogosUiState> = _uiState

    // ─── Categorías ───────────────────────────────────────

    fun cargarCategorias() {
        viewModelScope.launch {
            _uiState.value = CatalogosUiState.Loading
            try {
                _categorias.value = repo.getCategorias()
                _uiState.value = CatalogosUiState.Idle
            } catch (e: Exception) {
                _uiState.value = CatalogosUiState.Error(e.message ?: "Error al cargar categorías")
            }
        }
    }

    fun guardarCategoria(id: Int?, nombre: String, icono: String?) {
        viewModelScope.launch {
            _uiState.value = CatalogosUiState.Loading
            try {
                if (id == null) repo.crearCategoria(nombre, icono)
                else repo.editarCategoria(id, nombre, icono)
                cargarCategorias()
                _uiState.value = CatalogosUiState.Success
            } catch (e: Exception) {
                _uiState.value = CatalogosUiState.Error(e.message ?: "Error al guardar")
            }
        }
    }

    fun toggleCategoria(id: Int) {
        viewModelScope.launch {
            try {
                val nuevoEstado = repo.toggleEstadoCategoria(id)
                _categorias.value = _categorias.value.map {
                    if (it.id == id) it.copy(estado = nuevoEstado) else it
                }
            } catch (e: Exception) {
                _uiState.value = CatalogosUiState.Error(e.message ?: "Error al cambiar estado")
            }
        }
    }

    // ─── Servicios ────────────────────────────────────────

    fun cargarServicios() {
        viewModelScope.launch {
            _uiState.value = CatalogosUiState.Loading
            try {
                _servicios.value = repo.getServicios()
                _uiState.value = CatalogosUiState.Idle
            } catch (e: Exception) {
                _uiState.value = CatalogosUiState.Error(e.message ?: "Error al cargar servicios")
            }
        }
    }

    fun guardarServicio(id: Int?, req: ServicioRequest) {
        viewModelScope.launch {
            _uiState.value = CatalogosUiState.Loading
            try {
                if (id == null) repo.crearServicio(req)
                else repo.editarServicio(id, req)
                cargarServicios()
                _uiState.value = CatalogosUiState.Success
            } catch (e: Exception) {
                _uiState.value = CatalogosUiState.Error(e.message ?: "Error al guardar")
            }
        }
    }

    fun toggleServicio(id: Int) {
        viewModelScope.launch {
            try {
                val nuevoEstado = repo.toggleEstadoServicio(id)
                _servicios.value = _servicios.value.map {
                    if (it.id == id) it.copy(estado = nuevoEstado) else it
                }
            } catch (e: Exception) {
                _uiState.value = CatalogosUiState.Error(e.message ?: "Error al cambiar estado")
            }
        }
    }

    fun resetState() { _uiState.value = CatalogosUiState.Idle }
}