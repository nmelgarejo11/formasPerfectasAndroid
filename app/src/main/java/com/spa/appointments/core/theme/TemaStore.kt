package com.spa.appointments.core.theme

import com.spa.appointments.domain.model.TemaEmpresa
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

// Singleton en memoria — guarda el tema activo de la empresa
object TemaStore {

    private val _tema = MutableStateFlow<TemaEmpresa?>(null)
    val tema: StateFlow<TemaEmpresa?> = _tema

    fun setTema(tema: TemaEmpresa) {
        _tema.value = tema
    }

    fun getTema(): TemaEmpresa? = _tema.value

    fun limpiar() {
        _tema.value = null
    }
}