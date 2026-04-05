package com.spa.appointments.ui.reserva

import androidx.lifecycle.ViewModel
import com.spa.appointments.domain.model.Profesional
import com.spa.appointments.domain.model.Servicio
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

// ViewModel compartido entre las 3 pantallas del flujo de reserva
// Vive mientras el NavGraph esté activo
@HiltViewModel
class ReservaSharedViewModel @Inject constructor() : ViewModel() {
    var servicioSeleccionado: Servicio?      = null
    var profesionalSeleccionado: Profesional? = null
}