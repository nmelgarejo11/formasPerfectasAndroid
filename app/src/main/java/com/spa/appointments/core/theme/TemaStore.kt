package com.spa.appointments.core.theme

import com.spa.appointments.domain.model.TemaEmpresa
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

enum class ThemeMode { SYSTEM, LIGHT, DARK }

// Singleton en memoria — guarda el tema activo de la empresa y la preferencia de modo
object TemaStore {

    private val _tema = MutableStateFlow<TemaEmpresa?>(null)
    val tema: StateFlow<TemaEmpresa?> = _tema

    private val _modoTema = MutableStateFlow(ThemeMode.SYSTEM)
    val modoTema: StateFlow<ThemeMode> = _modoTema

    fun setTema(tema: TemaEmpresa) {
        _tema.value = tema
    }

    fun getTema(): TemaEmpresa? = _tema.value

    fun setModoTema(modo: ThemeMode) {
        _modoTema.value = modo
    }

    fun getModoTema(): ThemeMode = _modoTema.value

    fun limpiar() {
        _tema.value = null
    }
}