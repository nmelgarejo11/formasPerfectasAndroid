package com.spa.appointments.ui.disponibilidad

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spa.appointments.core.security.TokenStorage
import com.spa.appointments.core.utils.Constants
import com.spa.appointments.data.repository.CitasRepository
import com.spa.appointments.domain.model.*
import com.spa.appointments.ui.reserva.ReservaSharedViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

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

    var servicio: Servicio?      = null
    var profesional: Profesional? = null
    var clienteSeleccionado: Cliente? = null

    private val _fechaSeleccionada = MutableStateFlow(LocalDate.now())
    val fechaSeleccionada: StateFlow<LocalDate> = _fechaSeleccionada

    private val _slotSeleccionado = MutableStateFlow<SlotDisponible?>(null)
    val slotSeleccionado: StateFlow<SlotDisponible?> = _slotSeleccionado

    fun seleccionarFecha(fecha: LocalDate) {
        _fechaSeleccionada.value = fecha
        _slotSeleccionado.value  = null
        cargarSlots(fecha)
    }

    fun seleccionarSlot(slot: SlotDisponible) {
        _slotSeleccionado.value = if (_slotSeleccionado.value == slot) null else slot
    }

    private fun cargarSlots(fecha: LocalDate) {
        // Si es cita grupal, no consultamos slots de un profesional individual
        val prof = profesional ?: return
        val serv = servicio    ?: return

        viewModelScope.launch {
            _uiState.value = DisponibilidadUiState.LoadingSlots
            try {
                val slots = repo.getDisponibilidad(
                    idProfesional = prof.id,
                    idSede        = prof.idSede,
                    fecha         = fecha.toString(),
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

    fun confirmarCita(sharedVm: ReservaSharedViewModel, notas: String?) {
        val serv = servicio ?: return
        val fecha = _fechaSeleccionada.value

        viewModelScope.launch {
            _uiState.value = DisponibilidadUiState.CreandoCita
            try {
                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
                var fechaInicioStr: String? = null
                var fechaFinStr: String? = null

                // Validación de flujo de negocio (Individual vs Grupal)
                if (!sharedVm.esGrupal) {
                    val slot = _slotSeleccionado.value
                    if (slot == null) {
                        _uiState.value = DisponibilidadUiState.Error("Debe seleccionar un horario")
                        return@launch
                    }
                    val fechaInicio = LocalDateTime.parse("${fecha} ${slot.horaInicio}", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                    val fechaFin = LocalDateTime.parse("${fecha} ${slot.horaFin}", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))

                    fechaInicioStr = formatter.format(fechaInicio)
                    fechaFinStr = formatter.format(fechaFin)
                } else {
                    // Para citas grupales abiertas, se guarda la fecha base sin bloque de hora específico
                    val fechaBase = LocalDateTime.of(fecha, java.time.LocalTime.MIN)
                    fechaInicioStr = formatter.format(fechaBase)
                    fechaFinStr = formatter.format(fechaBase)
                }

                // Cliente genérico si es grupal, de lo contrario requiere el cliente seleccionado
                val idClienteFinal = if (sharedVm.esGrupal) 0 else (clienteSeleccionado?.id ?: run {
                    _uiState.value = DisponibilidadUiState.Error("Debe seleccionar un cliente")
                    return@launch
                })

                val request = CrearCitaRequest(
                    idSede        = profesional?.idSede ?: Constants.ID_SEDE_DEFAULT,
                    idCliente     = idClienteFinal,
                    idProfesional = if (sharedVm.esGrupal) null else profesional?.id,
                    fechaInicio   = fechaInicioStr,
                    fechaFin      = fechaFinStr,
                    notas         = notas,
                    servicios     = listOf(
                        ServicioDetalle(
                            idServicio = serv.id,
                            precio     = serv.precioBase,
                            duracion   = serv.duracionMinutos
                        )
                    ),
                    // Inyección de nuevos campos mapeados hacia la API
                    cantidadPersonas    = sharedVm.cantidadPersonas,
                    responsableNombre   = sharedVm.responsableNombre.ifBlank { null },
                    responsableTelefono = sharedVm.responsableTelefono.ifBlank { null },
                    responsableEmail    = sharedVm.responsableCorreo.ifBlank { null }
                )

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