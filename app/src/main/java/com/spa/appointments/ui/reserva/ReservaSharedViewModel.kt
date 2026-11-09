package com.spa.appointments.ui.reserva

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.spa.appointments.domain.model.Cliente
import com.spa.appointments.domain.model.Profesional
import com.spa.appointments.domain.model.Servicio
import com.spa.appointments.domain.model.SlotDisponible
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import javax.inject.Inject

data class AsistenteCitaConfig(
    val numeroAsistente: Int,
    var profesional: Profesional? = null,
    var fecha: LocalDate? = null,
    var slot: SlotDisponible? = null
)

@HiltViewModel
class ReservaSharedViewModel @Inject constructor() : ViewModel() {
    var clienteSeleccionado: Cliente?         = null
    var servicioSeleccionado: Servicio?       = null
    var profesionalSeleccionado: Profesional? = null

    // ── Control de Cita Múltiple ─────────────────────────────────────────
    val serviciosSeleccionados = mutableStateListOf<Servicio>()
    var esMultiple by mutableStateOf(false)

    // ── Control de Cita Grupal ───────────────────────────────────────────
    var esGrupal by mutableStateOf(false)
    var responsableNombre by mutableStateOf("")
    var responsableTelefono by mutableStateOf("")
    var responsableCorreo by mutableStateOf("")
    var cantidadPersonas by mutableStateOf(1)

    val asistentesGrupales = mutableStateListOf<AsistenteCitaConfig>()
    var asistenteActualIndex by mutableIntStateOf(0)

    fun prepararCitaGrupal(cliente: Cliente, personas: Int) {
        clienteSeleccionado = cliente
        responsableNombre = cliente.nombreCompleto
        responsableTelefono = cliente.telefono ?: ""
        responsableCorreo = cliente.email ?: ""
        cantidadPersonas = personas

        asistentesGrupales.clear()
        for (i in 1..personas) {
            asistentesGrupales.add(AsistenteCitaConfig(numeroAsistente = i))
        }
        asistenteActualIndex = 0
    }

    fun limpiarReserva() {
        clienteSeleccionado = null
        servicioSeleccionado = null
        profesionalSeleccionado = null
        serviciosSeleccionados.clear()
        esMultiple = false
        esGrupal = false
        responsableNombre = ""
        responsableTelefono = ""
        responsableCorreo = ""
        cantidadPersonas = 1
        asistentesGrupales.clear()
        asistenteActualIndex = 0
    }
}