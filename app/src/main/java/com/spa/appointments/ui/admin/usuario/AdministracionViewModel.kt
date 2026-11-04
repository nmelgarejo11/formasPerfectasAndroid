package com.spa.appointments.ui.admin.usuario

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spa.appointments.domain.model.Cargo
import com.spa.appointments.domain.model.ConsultaPerfil
import com.spa.appointments.domain.model.CrearCargoRequest
import com.spa.appointments.domain.model.CrearPerfilRequest
import com.spa.appointments.domain.model.CrearUsuarioRequest
import com.spa.appointments.domain.model.SubModuloAdmin
import com.spa.appointments.domain.model.Usuario
import com.spa.appointments.domain.repository.AdministracionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdministracionViewModel @Inject constructor(
    private val repo: AdministracionRepository
) : ViewModel() {

    // ── Estado UI ──────────────────────────────────────
    var uiState by mutableStateOf(AdministracionUiState())
        private set

    init { cargarDatos() }

    private fun cargarDatos() {
        viewModelScope.launch {

            repo.obtenerSubModulos()
                .onSuccess {

                    uiState = uiState.copy(subModulos = it)
                }
                .onFailure { Log.e("AdminVM", "SubModulos ERROR: ${it.message}") }

            repo.obtenerPerfiles()
                .onSuccess {

                    uiState = uiState.copy(perfiles = it)
                }
                .onFailure { Log.e("AdminVM", "Perfiles ERROR: ${it.message}") }

            repo.obtenerUsuarios()
                .onSuccess {

                    uiState = uiState.copy(usuarios = it)
                }
                .onFailure { Log.e("AdminVM", "Usuarios ERROR: ${it.message}") }

            repo.obtenerCargos()
                .onSuccess {

                    uiState = uiState.copy(cargos = it)
                }
                .onFailure { Log.e("AdminVM", "Cargos ERROR: ${it.message}") }
        }
    }

    // ── Perfil – selección de submódulos ───────────────
    fun toggleSubModulo(id: Int) {
        uiState = uiState.copy(
            subModulos = uiState.subModulos.map {
                if (it.id == id) it.copy(seleccionado = !it.seleccionado) else it
            }
        )
    }

    // ── Crear Perfil ───────────────────────────────────
    fun crearPerfil(nombre: String, descripcion: String?) {
        val seleccionados = uiState.subModulos.filter { it.seleccionado }.map { it.id }
        if (seleccionados.isEmpty()) {
            uiState = uiState.copy(error = "Selecciona al menos un submódulo"); return
        }
        viewModelScope.launch {
            uiState = uiState.copy(cargando = true)
            repo.crearPerfil(CrearPerfilRequest(nombre, descripcion, seleccionados))
                .onSuccess { uiState = uiState.copy(cargando = false, exitoPerfil = true) }
                .onFailure { uiState = uiState.copy(cargando = false, error = it.message) }
        }
    }

    // ── Crear Usuario ──────────────────────────────────
    fun crearUsuario(usuario: String, clave: String, idPerfil: Int) {
        viewModelScope.launch {
            uiState = uiState.copy(cargando = true)
            repo.crearUsuario(CrearUsuarioRequest(usuario, clave, idPerfil = idPerfil))
                .onSuccess { uiState = uiState.copy(cargando = false, exitoUsuario = true) }
                .onFailure { uiState = uiState.copy(cargando = false, error = it.message) }
        }
    }

    fun cambiarPerfilUsuario(idUsuario: Int, idPerfil: Int) {
        viewModelScope.launch {
            uiState = uiState.copy(cargando = true)
            repo.cambiarPerfilUsuario(idUsuario, idPerfil)
                .onSuccess {
                    // Refrescar lista local sin llamar de nuevo a la API
                    uiState = uiState.copy(
                        cargando = false,
                        usuarios = uiState.usuarios.map { u ->
                            if (u.id == idUsuario)
                                u.copy(
                                    idPerfil     = idPerfil,
                                    nombrePerfil = uiState.perfiles.first { it.id == idPerfil }.nombre
                                )
                            else u
                        }
                    )
                }
                .onFailure { uiState = uiState.copy(cargando = false, error = it.message) }
        }
    }

    // ── Cargo ──────────────────────────────────────────
    fun crearCargo(nombre: String) {
        viewModelScope.launch {
            uiState = uiState.copy(cargando = true)
            repo.crearCargo(CrearCargoRequest(nombre))
                .onSuccess {
                    repo.obtenerCargos().onSuccess { lista ->
                        uiState = uiState.copy(cargando = false, cargos = lista)
                    }
                }
                .onFailure { uiState = uiState.copy(cargando = false, error = it.message) }
        }
    }

    fun cambiarEstadoCargo(id: Int, estado: Boolean) {
        viewModelScope.launch {
            repo.cambiarEstadoCargo(id, estado).onSuccess {
                uiState = uiState.copy(
                    cargos = uiState.cargos.map {
                        if (it.id == id) it.copy(estado = estado) else it
                    }
                )
            }
        }
    }

    fun limpiarError()      { uiState = uiState.copy(error = null) }
    fun limpiarExitos()     { uiState = uiState.copy(exitoUsuario = false, exitoPerfil = false) }
}

data class AdministracionUiState(
    val subModulos   : List<SubModuloAdmin> = emptyList(),
    val perfiles     : List<ConsultaPerfil>    = emptyList(),
    val usuarios     : List<Usuario>   = emptyList(),
    val cargos       : List<Cargo>     = emptyList(),
    val cargando     : Boolean         = false,
    val error        : String?         = null,
    val exitoUsuario : Boolean         = false,
    val exitoPerfil  : Boolean         = false
)