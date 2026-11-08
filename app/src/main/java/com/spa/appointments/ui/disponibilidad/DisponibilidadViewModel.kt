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

    private val _precioCalculado = MutableStateFlow<Double>(0.0)
    val precioCalculado: StateFlow<Double> = _precioCalculado

    private var rangosServicio: List<ServicioPrecioRango> = emptyList()

    fun cargarPrecioServicio(idServicio: Int, cantidadPersonas: Int) {
        viewModelScope.launch {
            val serv = servicio
            val base = serv?.precioBase ?: 0.0
            if (idServicio <= 0 || serv == null) {
                _precioCalculado.value = 0.0
                return@launch
            }
            try {
                rangosServicio = repo.getPreciosRangosServicio(idServicio)
            } catch (_: Exception) {
                rangosServicio = emptyList()
            }
            _precioCalculado.value = calcularPrecio(base, rangosServicio, cantidadPersonas)
        }
    }

    private fun calcularPrecio(precioBase: Double, rangos: List<ServicioPrecioRango>, cantidadPersonas: Int): Double {
        if (rangos.isEmpty()) {
            return if (cantidadPersonas > 1) precioBase * cantidadPersonas else precioBase
        }

        // 1. Coincidencia exacta de nivel
        val exacto = rangos.firstOrNull { it.cantidadPersonas == cantidadPersonas }
        if (exacto != null) {
            return if (exacto.esPorPersona) exacto.precio * cantidadPersonas else exacto.precio
        }

        // 2. Si la cantidad de personas supera el nivel máximo configurado
        val maxRango = rangos.maxByOrNull { it.cantidadPersonas }
        if (maxRango != null && cantidadPersonas >= maxRango.cantidadPersonas) {
            return if (maxRango.esPorPersona) maxRango.precio * cantidadPersonas else maxRango.precio
        }

        return if (cantidadPersonas > 1) precioBase * cantidadPersonas else precioBase
    }

    fun seleccionarFecha(fecha: LocalDate) {
        _fechaSeleccionada.value = fecha
        _slotSeleccionado.value  = null
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
        if (!sharedVm.esMultiple && servicio == null) return
        if (sharedVm.esMultiple && sharedVm.serviciosSeleccionados.isEmpty()) return

        val fecha = _fechaSeleccionada.value

        viewModelScope.launch {
            _uiState.value = DisponibilidadUiState.CreandoCita
            try {
                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
                var fechaInicioStr: String? = null
                var fechaFinStr: String? = null

                // Flujo Abierto aplica solo para servicios múltiples sin horario ni profesional prefijado
                val esFlujoAbierto = sharedVm.esMultiple

                if (!esFlujoAbierto) {
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
                    val fechaBase = LocalDateTime.of(fecha, java.time.LocalTime.MIN)
                    fechaInicioStr = formatter.format(fechaBase)
                    fechaFinStr = formatter.format(fechaBase)
                }

                // Cliente seleccionado en flujo individual o cliente responsable en flujo grupal
                val clienteFinal = clienteSeleccionado ?: sharedVm.clienteSeleccionado
                val idClienteFinal = if (esFlujoAbierto) 0 else (clienteFinal?.id ?: run {
                    _uiState.value = DisponibilidadUiState.Error("Debe seleccionar un cliente")
                    return@launch
                })

                val precioFinalServicio = if (_precioCalculado.value > 0.0) {
                    _precioCalculado.value
                } else {
                    servicio?.let { item ->
                        if (sharedVm.cantidadPersonas > 1) item.precioBase * sharedVm.cantidadPersonas else item.precioBase
                    } ?: 0.0
                }

                // Mapeo dinámico de la lista de servicios recolectados
                val serviciosDetalle = if (sharedVm.esMultiple) {
                    sharedVm.serviciosSeleccionados.map { item ->
                        ServicioDetalle(
                            idServicio = item.id,
                            precio     = item.precioBase,
                            duracion   = item.duracionMinutos
                        )
                    }
                } else {
                    servicio?.let { item ->
                        listOf(
                            ServicioDetalle(
                                idServicio = item.id,
                                precio     = precioFinalServicio,
                                duracion   = item.duracionMinutos
                            )
                        )
                    } ?: emptyList()
                }

                val request = CrearCitaRequest(
                    idSede        = profesional?.idSede ?: Constants.ID_SEDE_DEFAULT,
                    idCliente     = idClienteFinal,
                    idProfesional = if (esFlujoAbierto) null else profesional?.id,
                    fechaInicio   = fechaInicioStr,
                    fechaFin      = fechaFinStr,
                    notas         = notas,
                    servicios     = serviciosDetalle,
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