package com.spa.appointments.ui.tema

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spa.appointments.core.theme.TemaStore
import com.spa.appointments.data.remote.TemaEmpresaRequest
import com.spa.appointments.domain.repository.TemaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TemaUiState(
    val cargando: Boolean = false,
    val guardando: Boolean = false,
    val error: String? = null,
    val exito: Boolean = false,
    val colorPrimario: String = "",
    val colorSecundario: String = "",
    val colorTerciario: String = "",
    val nombreApp: String = "",
    val slogan: String = "",
    val mensajeWhatsApp: TextFieldValue = TextFieldValue("")
)

@HiltViewModel
class TemaViewModel @Inject constructor(
    private val repository: TemaRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TemaUiState())
    val uiState: StateFlow<TemaUiState> = _uiState

    // Variables disponibles para el mensaje WhatsApp
    val variablesDisponibles = listOf("{NombreCliente}", "{Fecha}", "{Hora}")

    init { cargarTema() }

    private fun cargarTema() {
        viewModelScope.launch {
            _uiState.update { it.copy(cargando = true) }
            repository.obtenerTema()
                .onSuccess { tema ->
                    _uiState.update {
                        it.copy(
                            cargando = false,
                            colorPrimario = tema.colorPrimario,
                            colorSecundario = tema.colorSecundario,
                            colorTerciario = tema.colorTerciario,
                            nombreApp = tema.nombreApp,
                            slogan = tema.slogan ?: "",
                            mensajeWhatsApp = TextFieldValue(tema.mensajeWhatsApp ?: "")
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(cargando = false, error = e.message) }
                }
        }
    }

    fun onColorPrimarioChange(v: String)   { _uiState.update { it.copy(colorPrimario = v) } }
    fun onColorSecundarioChange(v: String) { _uiState.update { it.copy(colorSecundario = v) } }
    fun onColorTerciarioChange(v: String)  { _uiState.update { it.copy(colorTerciario = v) } }
    fun onNombreAppChange(v: String)       { _uiState.update { it.copy(nombreApp = v) } }
    fun onSloganChange(v: String)          { _uiState.update { it.copy(slogan = v) } }

    fun onMensajeWhatsAppChange(v: TextFieldValue) {
        _uiState.update { it.copy(mensajeWhatsApp = v) }
    }

    // Inserta variable en la posición actual del cursor
    fun insertarVariable(variable: String) {
        val actual = _uiState.value.mensajeWhatsApp
        val texto = actual.text
        val cursor = actual.selection.end
        val nuevoTexto = texto.substring(0, cursor) + variable + texto.substring(cursor)
        val nuevoCursor = cursor + variable.length
        _uiState.update {
            it.copy(
                mensajeWhatsApp = TextFieldValue(
                    text = nuevoTexto,
                    selection = TextRange(nuevoCursor)
                )
            )
        }
    }

    fun guardar() {
        val s = _uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(guardando = true, error = null, exito = false) }
            val request = TemaEmpresaRequest(
                colorPrimario   = s.colorPrimario.trim(),
                colorSecundario = s.colorSecundario.trim(),
                colorTerciario  = s.colorTerciario.trim(),
                nombreApp       = s.nombreApp.trim(),
                slogan          = s.slogan.trim().ifEmpty { null },
                mensajeWhatsApp = s.mensajeWhatsApp.text.trim().ifEmpty { null }
            )
            repository.actualizarTema(request)
                .onSuccess {
                    // Actualizar TemaStore con los nuevos valores
                    TemaStore.getTema()?.let { temaActual ->
                        TemaStore.setTema(
                            temaActual.copy(
                                colorPrimario   = request.colorPrimario,
                                colorSecundario = request.colorSecundario,
                                colorTerciario  = request.colorTerciario,
                                nombreApp       = request.nombreApp,
                                slogan          = request.slogan,
                                mensajeWhatsApp = request.mensajeWhatsApp
                            )
                        )
                    }
                    _uiState.update { it.copy(guardando = false, exito = true) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(guardando = false, error = e.message) }
                }
        }
    }

    fun limpiarExito() { _uiState.update { it.copy(exito = false) } }
    fun limpiarError() { _uiState.update { it.copy(error = null) } }
}