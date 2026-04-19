package com.spa.appointments.ui.reserva

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
}