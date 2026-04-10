package com.spa.appointments.ui.financiero

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spa.appointments.data.repository.FinancieroRepository
import com.spa.appointments.domain.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

// Períodos predefinidos de filtro
enum class Periodo(val label: String) {
    SEMANA("Esta semana"),
    MES("Este mes"),
    TRIMESTRE("Este trimestre"),
    ANIO("Este año")
}

data class FinancieroUiData(
    val resumen:             ResumenFinanciero,
    val ingresosDia:         List<IngresoDia>,
    val ingresosMes:         List<IngresoMes>,
    val serviciosVendidos:   List<ServicioVendido>,
    val profesionalesRanking: List<ProfesionalRanking>
)

sealed class FinancieroUiState {
    object Loading                             : FinancieroUiState()
    data class Success(val data: FinancieroUiData) : FinancieroUiState()
    data class Error(val mensaje: String)      : FinancieroUiState()
}

@HiltViewModel
class FinancieroViewModel @Inject constructor(
    private val repo: FinancieroRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<FinancieroUiState>(FinancieroUiState.Loading)
    val uiState: StateFlow<FinancieroUiState> = _uiState

    private val _periodoSeleccionado = MutableStateFlow(Periodo.MES)
    val periodoSeleccionado: StateFlow<Periodo> = _periodoSeleccionado

    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    init { cargar(Periodo.MES) }

    fun seleccionarPeriodo(periodo: Periodo) {
        _periodoSeleccionado.value = periodo
        cargar(periodo)
    }

    private fun cargar(periodo: Periodo) {
        viewModelScope.launch {
            _uiState.value = FinancieroUiState.Loading
            try {
                val hoy   = LocalDate.now()
                val anio  = hoy.year

                // Calcular fechas según período
                val (desde, hasta) = when (periodo) {
                    Periodo.SEMANA    -> hoy.minusDays(6) to hoy
                    Periodo.MES       -> hoy.withDayOfMonth(1) to hoy
                    Periodo.TRIMESTRE -> hoy.minusMonths(2).withDayOfMonth(1) to hoy
                    Periodo.ANIO      -> hoy.withDayOfYear(1) to hoy
                }

                val desdeStr = desde.format(formatter)
                val hastaStr = hasta.format(formatter)

                // Ejecutar todas las llamadas en paralelo
                val resumenDeferred    = async { repo.getResumen(desdeStr, hastaStr) }
                val diasDeferred       = async { repo.getIngresosPorDia(desdeStr, hastaStr) }
                val mesDeferred        = async { repo.getIngresosPorMes(anio) }
                val serviciosDeferred  = async { repo.getServiciosVendidos(desdeStr, hastaStr) }
                val profDeferred       = async { repo.getProfesionalesRanking(desdeStr, hastaStr) }

                _uiState.value = FinancieroUiState.Success(
                    FinancieroUiData(
                        resumen              = resumenDeferred.await(),
                        ingresosDia          = diasDeferred.await(),
                        ingresosMes          = mesDeferred.await(),
                        serviciosVendidos    = serviciosDeferred.await(),
                        profesionalesRanking = profDeferred.await()
                    )
                )
            } catch (e: Exception) {
                _uiState.value = FinancieroUiState.Error(
                    e.localizedMessage ?: "Error al cargar datos financieros"
                )
            }
        }
    }
}