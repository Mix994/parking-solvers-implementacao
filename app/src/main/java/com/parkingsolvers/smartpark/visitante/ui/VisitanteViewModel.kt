package com.parkingsolvers.smartpark.visitante.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.parkingsolvers.smartpark.compartilhado.exceptions.RegraDeNegocioException
import com.parkingsolvers.smartpark.compartilhado.exceptions.ValidacaoException
import com.parkingsolvers.smartpark.visitante.dto.request.VisitanteRequest
import com.parkingsolvers.smartpark.visitante.dto.response.VisitanteResponse
import com.parkingsolvers.smartpark.visitante.service.VisitanteService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class VisitanteUiState(
    val termoBusca: String = "",
    val mensagemFeedback: String? = null,
    val erroGeral: String? = null,
    val errosCampos: Map<String, String> = emptyMap()
)

class VisitanteViewModel(
    private val visitanteService: VisitanteService,
    private val operadorNome: String
) : ViewModel() {

    val noCampus: StateFlow<List<VisitanteResponse>> = visitanteService.observarNoCampus()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val historico: StateFlow<List<VisitanteResponse>> = visitanteService.observarHistorico()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _uiState = MutableStateFlow(VisitanteUiState())
    val uiState: StateFlow<VisitanteUiState> = _uiState.asStateFlow()

    fun atualizarBusca(termo: String) {
        _uiState.update { it.copy(termoBusca = termo) }
    }

    fun registrarEntrada(request: VisitanteRequest, onSucesso: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(erroGeral = null, errosCampos = emptyMap(), mensagemFeedback = null) }
            try {
                visitanteService.registrarEntrada(request, registradoPor = operadorNome)
                _uiState.update { it.copy(mensagemFeedback = "Entrada do visitante registrada.") }
                onSucesso()
            } catch (e: ValidacaoException) {
                _uiState.update { it.copy(errosCampos = e.errosValidacao, erroGeral = e.message) }
            } catch (e: RegraDeNegocioException) {
                _uiState.update { it.copy(erroGeral = e.message) }
            }
        }
    }

    fun registrarSaida(visitanteId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(erroGeral = null, mensagemFeedback = null) }
            try {
                visitanteService.registrarSaida(visitanteId)
                _uiState.update { it.copy(mensagemFeedback = "Saída do visitante registrada.") }
            } catch (e: RegraDeNegocioException) {
                _uiState.update { it.copy(erroGeral = e.message) }
            }
        }
    }

    fun limparErrosFormulario() {
        _uiState.update { it.copy(erroGeral = null, errosCampos = emptyMap()) }
    }

    companion object {
        fun factory(
            visitanteService: VisitanteService,
            operadorNome: String
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                VisitanteViewModel(visitanteService, operadorNome) as T
        }
    }
}
