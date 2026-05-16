package com.spa.appointments.ui.reserva

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.spa.appointments.domain.model.Cliente
import com.spa.appointments.domain.model.Profesional
import com.spa.appointments.domain.model.Servicio
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ReservaSharedViewModel @Inject constructor() : ViewModel() {
    var clienteSeleccionado: Cliente?         = null
    var servicioSeleccionado: Servicio?       = null
    var profesionalSeleccionado: Profesional? = null

    // ── Control de Cita Grupal ───────────────────────────────────────────
    var esGrupal by mutableStateOf(false)
    var responsableNombre by mutableStateOf("")
    var responsableTelefono by mutableStateOf("")
    var responsableCorreo by mutableStateOf("")
    var cantidadPersonas by mutableStateOf(1)

    fun limpiarReserva() {
        clienteSeleccionado = null
        servicioSeleccionado = null
        profesionalSeleccionado = null
        esGrupal = false
        responsableNombre = ""
        responsableTelefono = ""
        responsableCorreo = ""
        cantidadPersonas = 1
    }
}