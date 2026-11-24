package com.parkingsolvers.smartpark.dashboard.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.parkingsolvers.smartpark.compartilhado.exceptions.RegraDeNegocioException
import com.parkingsolvers.smartpark.compartilhado.exceptions.ValidacaoException
import com.parkingsolvers.smartpark.dashboard.dto.response.DashboardOcupacaoResponse
import com.parkingsolvers.smartpark.dashboard.model.PeriodoDashboard
import com.parkingsolvers.smartpark.dashboard.service.ConfiguracaoEstacionamentoService
import com.parkingsolvers.smartpark.dashboard.service.DashboardService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DashboardUiState(
    val mensagemFeedback: String? = null,
    val erroConfiguracao: String? = null,
    val errosCampos: Map<String, String> = emptyMap()
)

class DashboardViewModel(
    dashboardService: DashboardService,
    private val configuracaoService: ConfiguracaoEstacionamentoService
) : ViewModel() {

    private val _periodo = MutableStateFlow(PeriodoDashboard.HOJE)
    val periodo: StateFlow<PeriodoDashboard> = _periodo.asStateFlow()

    val dashboard: StateFlow<DashboardOcupacaoResponse?> = dashboardService.observarDashboard(_periodo)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    fun selecionarPeriodo(periodo: PeriodoDashboard) {
        _periodo.value = periodo
    }

    fun atualizarCapacidades(
        capacidadeTotal: Int,
        capacidadeExclusivas: Int,
        capacidadeCarregamentoEletrico: Int,
        onSucesso: () -> Unit
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(erroConfiguracao = null, errosCampos = emptyMap(), mensagemFeedback = null) }
            try {
                configuracaoService.atualizarCapacidades(
                    capacidadeTotal = capacidadeTotal,
                    capacidadeExclusivas = capacidadeExclusivas,
                    capacidadeCarregamentoEletrico = capacidadeCarregamentoEletrico
                )
                _uiState.update {
                    it.copy(
                        mensagemFeedback = "Capacidade atualizada ($capacidadeCarregamentoEletrico vagas de carregamento elétrico)."
                    )
                }
                onSucesso()
            } catch (e: ValidacaoException) {
                _uiState.update { it.copy(errosCampos = e.errosValidacao, erroConfiguracao = e.message) }
            } catch (e: RegraDeNegocioException) {
                _uiState.update { it.copy(erroConfiguracao = e.message) }
            }
        }
    }

    fun limparErrosConfiguracao() {
        _uiState.update { it.copy(erroConfiguracao = null, errosCampos = emptyMap()) }
    }

    companion object {
        fun factory(
            dashboardService: DashboardService,
            configuracaoService: ConfiguracaoEstacionamentoService
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    DashboardViewModel(dashboardService, configuracaoService) as T
            }
    }
}
