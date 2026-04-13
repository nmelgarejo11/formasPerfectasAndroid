package com.spa.appointments.ui.licencia

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spa.appointments.data.repository.ContactoRepository
import com.spa.appointments.domain.model.ContactoSoporte
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DemoExpiradoViewModel @Inject constructor(
    private val repo: ContactoRepository
) : ViewModel() {

    private val _contacto = MutableStateFlow<ContactoSoporte?>(null)
    val contacto: StateFlow<ContactoSoporte?> = _contacto

    private val _cargando = MutableStateFlow(false)
    val cargando: StateFlow<Boolean> = _cargando

    init { cargarContacto() }

    private fun cargarContacto() {
        viewModelScope.launch {
            _cargando.value = true
            try {
                _contacto.value = repo.getContacto()
            } catch (e: Exception) {
                // Si falla la carga igual mostramos la pantalla
                // sin los botones de contacto
            } finally {
                _cargando.value = false
            }
        }
    }
}