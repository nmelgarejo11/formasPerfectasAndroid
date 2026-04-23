package com.spa.appointments.ui.profesionales

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spa.appointments.data.repository.CitasRepository
import com.spa.appointments.domain.model.Profesional
import com.spa.appointments.domain.model.Servicio
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class ProfesionalesUiState {
    object Loading                                   : ProfesionalesUiState()
    data class Success(val items: List<Profesional>) : ProfesionalesUiState()
    data class Error(val mensaje: String)            : ProfesionalesUiState()
}

@HiltViewModel
class ProfesionalesViewModel @Inject constructor(
    private val repo: CitasRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProfesionalesUiState>(ProfesionalesUiState.Loading)
    val uiState: StateFlow<ProfesionalesUiState> = _uiState

    var servicioSeleccionado: Servicio? = null

    // Recibe el idServicio ya resuelto y carga
    fun iniciar(idServicio: Int?) {
        viewModelScope.launch {
            _uiState.value = ProfesionalesUiState.Loading
            try {
                val profesionales = repo.getProfesionales(
                    idSede     = null,
                    idServicio = idServicio
                )
                _uiState.value = ProfesionalesUiState.Success(profesionales)
            } catch (e: Exception) {
                _uiState.value = ProfesionalesUiState.Error(
                    e.localizedMessage ?: "Error al cargar profesionales"
                )
            }
        }
    }
}