package com.spa.appointments.ui.admin.horarios

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spa.appointments.data.repository.HorariosRepository
import com.spa.appointments.domain.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class HorariosUiState {
    object Idle    : HorariosUiState()
    object Loading : HorariosUiState()
    object Success : HorariosUiState()
    data class Error(val mensaje: String) : HorariosUiState()
}

// Modelo de trabajo en UI — soporte para horario partido (hasta 2 franjas)
data class DiaHorario(
    val diaSemana:    Int,
    val nombreDia:    String,
    var activo:       Boolean,
    var horaInicio:   String,
    var horaFin:      String,
    var tieneFranja2: Boolean = false,
    var horaInicio2:  String = "14:00",
    var horaFin2:     String = "18:00"
) {
    val esValido: Boolean
        get() {
            if (!activo) return true
            if (horaFin <= horaInicio) return false
            if (tieneFranja2) {
                if (horaInicio2 < horaFin) return false
                if (horaFin2 <= horaInicio2) return false
            }
            return true
        }

    val mensajeError: String?
        get() {
            if (!activo) return null
            if (horaFin <= horaInicio) return "Franja 1: La hora fin debe ser mayor a la inicio."
            if (tieneFranja2) {
                if (horaInicio2 < horaFin) return "Franja 2: Debe iniciar después del fin de Franja 1."
                if (horaFin2 <= horaInicio2) return "Franja 2: La hora fin debe ser mayor a la inicio."
            }
            return null
        }
}

@HiltViewModel
class HorariosViewModel @Inject constructor(
    private val repo: HorariosRepository
) : ViewModel() {

    private val _diasHorario = MutableStateFlow<List<DiaHorario>>(diasVacios())
    val diasHorario: StateFlow<List<DiaHorario>> = _diasHorario

    private val _bloqueos = MutableStateFlow<List<BloqueoResponse>>(emptyList())
    val bloqueos: StateFlow<List<BloqueoResponse>> = _bloqueos

    private val _uiState = MutableStateFlow<HorariosUiState>(HorariosUiState.Idle)
    val uiState: StateFlow<HorariosUiState> = _uiState

    // ─── Carga ────────────────────────────────────────────────

    fun cargar(idProfesional: Int) {
        viewModelScope.launch {
            _uiState.value = HorariosUiState.Loading
            try {
                val defHorario  = async { repo.getHorario(idProfesional) }
                val defBloqueos = async { repo.getBloqueos(idProfesional) }
                val horario     = defHorario.await()
                _bloqueos.value = defBloqueos.await()
                _diasHorario.value = diasDesdeApi(horario)
                _uiState.value = HorariosUiState.Idle
            } catch (e: Exception) {
                _uiState.value = HorariosUiState.Error(e.message ?: "Error al cargar")
            }
        }
    }

    // ─── Horario ──────────────────────────────────────────────

    fun toggleDia(diaSemana: Int, activo: Boolean) {
        _diasHorario.value = _diasHorario.value.map {
            if (it.diaSemana == diaSemana) it.copy(activo = activo) else it
        }
    }

    fun actualizarHora1(diaSemana: Int, horaInicio: String, horaFin: String) {
        _diasHorario.value = _diasHorario.value.map {
            if (it.diaSemana == diaSemana) it.copy(horaInicio = horaInicio, horaFin = horaFin) else it
        }
    }

    fun toggleFranja2(diaSemana: Int, tieneFranja2: Boolean) {
        _diasHorario.value = _diasHorario.value.map {
            if (it.diaSemana == diaSemana) it.copy(tieneFranja2 = tieneFranja2) else it
        }
    }

    fun actualizarHora2(diaSemana: Int, horaInicio2: String, horaFin2: String) {
        _diasHorario.value = _diasHorario.value.map {
            if (it.diaSemana == diaSemana) it.copy(horaInicio2 = horaInicio2, horaFin2 = horaFin2) else it
        }
    }

    fun guardarHorario(idProfesional: Int, idSede: Int) {
        val diasInvalidos = _diasHorario.value.filter { it.activo && !it.esValido }
        if (diasInvalidos.isNotEmpty()) {
            _uiState.value = HorariosUiState.Error(
                diasInvalidos.first().mensajeError ?: "El horario contiene franjas inválidas"
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = HorariosUiState.Loading
            try {
                val items = _diasHorario.value
                    .filter { it.activo }
                    .map { dia ->
                        HorarioItemRequest(
                            diaSemana   = dia.diaSemana,
                            horaInicio  = dia.horaInicio,
                            horaFin     = dia.horaFin,
                            horaInicio2 = if (dia.tieneFranja2) dia.horaInicio2 else null,
                            horaFin2    = if (dia.tieneFranja2) dia.horaFin2 else null,
                            estado      = true
                        )
                    }
                repo.guardarHorario(idProfesional, GuardarHorarioRequest(idSede, items))
                _uiState.value = HorariosUiState.Success
            } catch (e: Exception) {
                _uiState.value = HorariosUiState.Error(e.message ?: "Error al guardar horario")
            }
        }
    }

    fun copiarHorario(idProfesionalDestino: Int, idProfesionalOrigen: Int, idSede: Int) {
        viewModelScope.launch {
            _uiState.value = HorariosUiState.Loading
            try {
                repo.copiarHorario(
                    idProfesionalDestino,
                    CopiarHorarioRequest(idProfesionalOrigen, idSede)
                )
                cargar(idProfesionalDestino)
                _uiState.value = HorariosUiState.Success
            } catch (e: Exception) {
                _uiState.value = HorariosUiState.Error(e.message ?: "Error al copiar horario")
            }
        }
    }

    // ─── Bloqueos ─────────────────────────────────────────────

    fun crearBloqueo(idProfesional: Int, fechaInicio: String, fechaFin: String, motivo: String) {
        viewModelScope.launch {
            _uiState.value = HorariosUiState.Loading
            try {
                repo.crearBloqueo(idProfesional, BloqueoRequest(fechaInicio, fechaFin, motivo))
                val bloqueos = repo.getBloqueos(idProfesional)
                _bloqueos.value = bloqueos
                _uiState.value = HorariosUiState.Success
            } catch (e: Exception) {
                _uiState.value = HorariosUiState.Error(e.message ?: "Error al crear bloqueo")
            }
        }
    }

    fun eliminarBloqueo(idProfesional: Int, id: Int) {
        viewModelScope.launch {
            try {
                repo.eliminarBloqueo(idProfesional, id)
                _bloqueos.value = _bloqueos.value.filter { it.id != id }
            } catch (e: Exception) {
                _uiState.value = HorariosUiState.Error(e.message ?: "Error al eliminar bloqueo")
            }
        }
    }

    fun resetState() { _uiState.value = HorariosUiState.Idle }

    // ─── Utilidades ───────────────────────────────────────────

    private fun diasVacios(): List<DiaHorario> = listOf(
        DiaHorario(1, "Lunes",     false, "08:00", "12:00", false, "14:00", "18:00"),
        DiaHorario(2, "Martes",    false, "08:00", "12:00", false, "14:00", "18:00"),
        DiaHorario(3, "Miércoles", false, "08:00", "12:00", false, "14:00", "18:00"),
        DiaHorario(4, "Jueves",    false, "08:00", "12:00", false, "14:00", "18:00"),
        DiaHorario(5, "Viernes",   false, "08:00", "12:00", false, "14:00", "18:00"),
        DiaHorario(6, "Sábado",    false, "08:00", "12:00", false, "14:00", "18:00"),
        DiaHorario(7, "Domingo",   false, "08:00", "12:00", false, "14:00", "18:00"),
    )

    private fun diasDesdeApi(horario: List<HorarioItem>): List<DiaHorario> {
        val base = diasVacios()
        return base.map { dia ->
            val item = horario.firstOrNull { it.diaSemana == dia.diaSemana }
            if (item != null) {
                val tieneF2 = !item.horaInicio2.isNullOrBlank() && !item.horaFin2.isNullOrBlank()
                dia.copy(
                    activo       = item.estado,
                    horaInicio   = item.horaInicio,
                    horaFin      = item.horaFin,
                    tieneFranja2 = tieneF2,
                    horaInicio2  = item.horaInicio2 ?: "14:00",
                    horaFin2     = item.horaFin2 ?: "18:00"
                )
            } else dia
        }
    }
}