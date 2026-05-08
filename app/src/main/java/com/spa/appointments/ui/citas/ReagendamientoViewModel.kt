package com.spa.appointments.ui.citas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spa.appointments.data.repository.CitasRepository
import com.spa.appointments.data.repository.ReagendamientoRepository
import com.spa.appointments.domain.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

// ── Estados de lista ──────────────────────────────────────────────────────────
sealed class ReagendamientoUiState {
    object Loading : ReagendamientoUiState()
    data class Success(val pendientes: List<CitaPendiente>) : ReagendamientoUiState()
    data class Error(val mensaje: String) : ReagendamientoUiState()
}

// ── Estados de acción (aprobar/rechazar) ──────────────────────────────────────
sealed class ReagendamientoActionState {
    object Idle    : ReagendamientoActionState()
    object Loading : ReagendamientoActionState()
    object Success : ReagendamientoActionState()
    data class Error(val mensaje: String) : ReagendamientoActionState()
}

// ── Estados de disponibilidad para el diálogo de aprobación ──────────────────
sealed class DisponibilidadDialogState {
    object Idle                                             : DisponibilidadDialogState()
    object Loading                                          : DisponibilidadDialogState()
    data class Loaded(val slots: List<SlotDisponible>)      : DisponibilidadDialogState()
    data class Error(val mensaje: String)                   : DisponibilidadDialogState()
}

@HiltViewModel
class ReagendamientoViewModel @Inject constructor(
    private val repository:     ReagendamientoRepository,
    private val citasRepository: CitasRepository          // mismo repo que DisponibilidadViewModel
) : ViewModel() {

    private val _uiState = MutableStateFlow<ReagendamientoUiState>(ReagendamientoUiState.Loading)
    val uiState: StateFlow<ReagendamientoUiState> = _uiState

    private val _actionState = MutableStateFlow<ReagendamientoActionState>(ReagendamientoActionState.Idle)
    val actionState: StateFlow<ReagendamientoActionState> = _actionState

    // ── Disponibilidad ────────────────────────────────────────────────────────
    private val _dispState = MutableStateFlow<DisponibilidadDialogState>(DisponibilidadDialogState.Idle)
    val dispState: StateFlow<DisponibilidadDialogState> = _dispState

    private val _fechaDialog = MutableStateFlow(LocalDate.now())
    val fechaDialog: StateFlow<LocalDate> = _fechaDialog

    private val _slotDialog = MutableStateFlow<SlotDisponible?>(null)
    val slotDialog: StateFlow<SlotDisponible?> = _slotDialog

    init { cargar() }

    fun cargar() {
        viewModelScope.launch {
            _uiState.value = ReagendamientoUiState.Loading
            runCatching { repository.getPendientes() }
                .onSuccess { _uiState.value = ReagendamientoUiState.Success(it) }
                .onFailure { _uiState.value = ReagendamientoUiState.Error(it.message ?: "Error") }
        }
    }

    // ── Disponibilidad: se llama al abrir el diálogo o cambiar fecha ──────────
    fun cargarSlotsParaReagendar(
        idProfesional: Int,
        idSede:        Int,
        duracionMin:   Int,
        fecha:         LocalDate = _fechaDialog.value
    ) {
        _fechaDialog.value = fecha
        _slotDialog.value  = null
        viewModelScope.launch {
            _dispState.value = DisponibilidadDialogState.Loading
            runCatching {
                citasRepository.getDisponibilidad(
                    idProfesional = idProfesional,
                    idSede        = idSede,
                    fecha         = fecha.toString(),
                    duracion      = duracionMin
                )
            }
                .onSuccess { _dispState.value = DisponibilidadDialogState.Loaded(it) }
                .onFailure { _dispState.value = DisponibilidadDialogState.Error(it.message ?: "Error de disponibilidad") }
        }
    }

    fun seleccionarSlotDialog(slot: SlotDisponible) {
        _slotDialog.value = if (_slotDialog.value == slot) null else slot
    }

    fun resetDisponibilidad() {
        _dispState.value  = DisponibilidadDialogState.Idle
        _fechaDialog.value = LocalDate.now()
        _slotDialog.value  = null
    }

    // ── Aprobar / Rechazar ────────────────────────────────────────────────────
    fun aprobar(id: Int, nuevaFechaInicio: String, nuevaFechaFin: String) {
        viewModelScope.launch {
            _actionState.value = ReagendamientoActionState.Loading
            runCatching {
                repository.aprobar(id, AprobarReagendamientoRequest(nuevaFechaInicio, nuevaFechaFin))
            }
                .onSuccess { _actionState.value = ReagendamientoActionState.Success; cargar() }
                .onFailure { _actionState.value = ReagendamientoActionState.Error(it.message ?: "Error") }
        }
    }

    fun rechazar(id: Int, motivo: String) {
        viewModelScope.launch {
            _actionState.value = ReagendamientoActionState.Loading
            runCatching {
                repository.rechazar(id, RechazarReagendamientoRequest(motivo))
            }
                .onSuccess { _actionState.value = ReagendamientoActionState.Success; cargar() }
                .onFailure { _actionState.value = ReagendamientoActionState.Error(it.message ?: "Error") }
        }
    }

    fun resetActionState() { _actionState.value = ReagendamientoActionState.Idle }
}