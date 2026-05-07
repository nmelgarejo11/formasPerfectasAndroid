package com.spa.appointments.ui.servicios

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spa.appointments.data.repository.CitasRepository
import com.spa.appointments.domain.model.Servicio
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class ServiciosUiState {
    object Loading                          : ServiciosUiState()
    data class Success(val items: List<Servicio>) : ServiciosUiState()
    data class Error(val mensaje: String)   : ServiciosUiState()
}

@HiltViewModel
class ServiciosViewModel @Inject constructor(
    private val repo: CitasRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ServiciosUiState>(ServiciosUiState.Loading)
    val uiState: StateFlow<ServiciosUiState> = _uiState

    init { cargar() }

    private fun cargar() {
        viewModelScope.launch {
            try {
                val servicios = repo.getServicios()
                _uiState.value = ServiciosUiState.Success(servicios)
            } catch (e: Exception) {
                _uiState.value = ServiciosUiState.Error(
                    e.localizedMessage ?: "Error al cargar servicios"
                )
            }
        }
    }
}