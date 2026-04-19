package com.spa.appointments.ui.disponibilidad

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spa.appointments.core.security.TokenStorage
import com.spa.appointments.core.utils.Constants
import com.spa.appointments.data.repository.CitasRepository
import com.spa.appointments.domain.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

// Estados de la pantalla
sealed class DisponibilidadUiState {
    object Idle                                      : DisponibilidadUiState()
    object LoadingSlots                              : DisponibilidadUiState()
    data class SlotsLoaded(val slots: List<SlotDisponible>) : DisponibilidadUiState()
    object CreandoCita                               : DisponibilidadUiState()
    data class CitaCreada(val mensaje: String)       : DisponibilidadUiState()
    data class Error(val mensaje: String)            : DisponibilidadUiState()
}

@HiltViewModel
class DisponibilidadViewModel @Inject constructor(
    private val repo: CitasRepository,
    private val tokenStorage: TokenStorage
) : ViewModel() {

    private val _uiState = MutableStateFlow<DisponibilidadUiState>(DisponibilidadUiState.Idle)
    val uiState: StateFlow<DisponibilidadUiState> = _uiState

    // Datos que llegan de las pantallas anteriores
    var servicio: Servicio?      = null
    var profesional: Profesional? = null
    var clienteSeleccionado: Cliente? = null

    // Fecha seleccionada por el usuario
    private val _fechaSeleccionada = MutableStateFlow(LocalDate.now())
    val fechaSeleccionada: StateFlow<LocalDate> = _fechaSeleccionada

    // Slot seleccionado
    private val _slotSeleccionado = MutableStateFlow<SlotDisponible?>(null)
    val slotSeleccionado: StateFlow<SlotDisponible?> = _slotSeleccionado

    fun seleccionarFecha(fecha: LocalDate) {
        _fechaSeleccionada.value = fecha
        _slotSeleccionado.value  = null  // resetear slot al cambiar fecha
        cargarSlots(fecha)
    }

    fun seleccionarSlot(slot: SlotDisponible) {
        _slotSeleccionado.value = if (_slotSeleccionado.value == slot) null else slot
    }

    private fun cargarSlots(fecha: LocalDate) {
        val prof = profesional ?: return
        val serv = servicio    ?: return

        viewModelScope.launch {
            _uiState.value = DisponibilidadUiState.LoadingSlots
            try {
                val slots = repo.getDisponibilidad(
                    idProfesional = prof.id,
                    idSede        = prof.idSede,
                    fecha         = fecha.toString(),  // formato yyyy-MM-dd
                    duracion      = serv.duracionMinutos
                )
                _uiState.value = DisponibilidadUiState.SlotsLoaded(slots)
            } catch (e: Exception) {
                _uiState.value = DisponibilidadUiState.Error(
                    e.localizedMessage ?: "Error al cargar disponibilidad"
                )
            }
        }
    }

    fun confirmarCita(notas: String?) {
        val prof  = profesional           ?: return
        val serv  = servicio              ?: return
        val slot  = _slotSeleccionado.value ?: return
        val fecha = _fechaSeleccionada.value

        viewModelScope.launch {
            _uiState.value = DisponibilidadUiState.CreandoCita
            try {
                // Construir fechas completas combinando fecha + hora del slot

                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
                val fechaInicio = LocalDateTime.parse(
                    "${fecha} ${slot.horaInicio}", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                )
                val fechaFin = LocalDateTime.parse(
                    "${fecha} ${slot.horaFin}", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                )
                // selecciona cliente
                val idCliente = clienteSeleccionado?.id ?: run {
                    _uiState.value = DisponibilidadUiState.Error("Debe seleccionar un cliente")
                    return@launch
                }

                val request = CrearCitaRequest(
                    idSede        = prof.idSede,
                    idCliente     = idCliente,
                    idProfesional = prof.id,
                    fechaInicio   = formatter.format(fechaInicio),
                    fechaFin      = formatter.format(fechaFin),
                    notas         = notas,
                    servicios     = listOf(
                        ServicioDetalle(
                            idServicio = serv.id,
                            precio     = serv.precioBase,
                            duracion   = serv.duracionMinutos
                        )
                    )
                )
                android.util.Log.d("DisponibilidadVM", "Request: idSede=${request.idSede}, idCliente=${request.idCliente}, idProfesional=${request.idProfesional}, fechaInicio=${request.fechaInicio}, fechaFin=${request.fechaFin}, servicios=${request.servicios}")
                val response = repo.crearCita(request)

                if (response.idCita > 0) {
                    _uiState.value = DisponibilidadUiState.CitaCreada(response.mensaje)
                } else {
                    _uiState.value = DisponibilidadUiState.Error(response.mensaje)
                }

            } catch (e: Exception) {
                _uiState.value = DisponibilidadUiState.Error(
                    e.localizedMessage ?: "Error al crear la cita"
                )
            }
        }
    }

    fun resetear() {
        _uiState.value = DisponibilidadUiState.Idle
    }
}