package com.spa.appointments.ui.admin.perfilsubmodulo

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spa.appointments.domain.model.ConsultaPerfil
import com.spa.appointments.domain.model.SubModuloAsignadoDto
import com.spa.appointments.domain.repository.AdministracionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PerfilSubModuloViewModel @Inject constructor(
    private val repo: AdministracionRepository
) : ViewModel() {

    var perfiles by mutableStateOf<List<ConsultaPerfil>>(emptyList())
        private set
    var subModulos by mutableStateOf<List<SubModuloAsignadoDto>>(emptyList())
        private set
    var perfilSeleccionado by mutableStateOf<ConsultaPerfil?>(null)
        private set
    var isLoading by mutableStateOf(false)
        private set
    var mensaje by mutableStateOf<String?>(null)
        private set

    init { cargarPerfiles() }

    fun cargarPerfiles() = viewModelScope.launch {
        isLoading = true
        repo.obtenerPerfiles().onSuccess { perfiles = it }
        isLoading = false
    }

    fun seleccionarPerfil(perfil: ConsultaPerfil) = viewModelScope.launch {
        perfilSeleccionado = perfil
        isLoading = true
        repo.getSubModulos(perfil.id).onSuccess { subModulos = it }
        isLoading = false
    }

    fun toggleSubModulo(item: SubModuloAsignadoDto) = viewModelScope.launch {
        val idPerfil = perfilSeleccionado?.id ?: return@launch
        if (item.asignado) {
            repo.quitar(idPerfil, item.idSubModulo)
                .onSuccess { mensaje = "\"${item.nombre}\" removido" }
        } else {
            repo.asignar(idPerfil, item.idSubModulo)
                .onSuccess { mensaje = "\"${item.nombre}\" asignado" }
        }
        // Refresca la lista
        repo.getSubModulos(idPerfil).onSuccess { subModulos = it }
    }

    fun limpiarMensaje() { mensaje = null }
}