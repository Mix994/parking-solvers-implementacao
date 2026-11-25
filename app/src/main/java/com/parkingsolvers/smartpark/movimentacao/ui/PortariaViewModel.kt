package com.parkingsolvers.smartpark.movimentacao.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.parkingsolvers.smartpark.compartilhado.exceptions.RegraDeNegocioException
import com.parkingsolvers.smartpark.compartilhado.validacao.ValidadorPlaca
import com.parkingsolvers.smartpark.movimentacao.dto.request.EntradaRequest
import com.parkingsolvers.smartpark.movimentacao.dto.response.MovimentacaoResponse
import com.parkingsolvers.smartpark.movimentacao.model.TipoVaga
import com.parkingsolvers.smartpark.movimentacao.service.MovimentacaoService
import com.parkingsolvers.smartpark.veiculo.dto.response.ResultadoConsultaPlaca
import com.parkingsolvers.smartpark.veiculo.dto.response.VeiculoResponse
import com.parkingsolvers.smartpark.veiculo.service.VeiculoService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PortariaUiState(
    val placaBusca: String = "",
    val tipoVagaSelecionado: TipoVaga = TipoVaga.COMUM,
    val resultadoConsulta: ResultadoConsultaPlaca? = null,
    val mensagemFeedback: String? = null,
    val erro: String? = null
)

class PortariaViewModel(
    private val veiculoService: VeiculoService,
    private val movimentacaoService: MovimentacaoService
) : ViewModel() {

    val noEstacionamento: StateFlow<List<MovimentacaoResponse>> =
        movimentacaoService.observarNoEstacionamento()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val historico: StateFlow<List<MovimentacaoResponse>> =
        movimentacaoService.observarHistorico()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val veiculosCadastrados: StateFlow<List<VeiculoResponse>> =
        veiculoService.observarTodos()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _uiState = MutableStateFlow(PortariaUiState())
    val uiState: StateFlow<PortariaUiState> = _uiState.asStateFlow()

    /**
     * Correspondências em tempo real ao digitar parte da placa (a partir de 2 caracteres).
     * Prioriza placas que começam com o trecho digitado e veículos ativos.
     */
    val correspondenciasPlaca: StateFlow<List<VeiculoResponse>> = combine(
        _uiState,
        veiculosCadastrados
    ) { estado, veiculos ->
        val termo = estado.placaBusca.trim().uppercase().replace("-", "")
        if (termo.length < 2) {
            emptyList()
        } else if (estado.resultadoConsulta?.placaConsultada == termo) {
            emptyList()
        } else {
            veiculos
                .filter { it.placa.contains(termo) }
                .sortedWith(
                    compareByDescending<VeiculoResponse> { it.placa.startsWith(termo) }
                        .thenByDescending { it.ativo }
                        .thenBy { it.placa }
                )
                .take(5)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun atualizarPlacaBusca(placa: String) {
        val limpa = placa.uppercase().take(8)
        _uiState.update { atual ->
            val mudouPlaca = atual.resultadoConsulta?.placaConsultada != limpa.trim().replace("-", "")
            atual.copy(
                placaBusca = limpa,
                resultadoConsulta = if (mudouPlaca) null else atual.resultadoConsulta,
                erro = null,
                mensagemFeedback = null
            )
        }
    }

    fun selecionarPlacaSugerida(placa: String) {
        _uiState.update {
            it.copy(
                placaBusca = placa,
                erro = null,
                mensagemFeedback = null
            )
        }
        consultarPlaca(placa)
    }

    fun selecionarTipoVaga(tipoVaga: TipoVaga) {
        _uiState.update { it.copy(tipoVagaSelecionado = tipoVaga) }
    }

    fun consultarPlaca(placaForcada: String? = null) {
        val digitada = placaForcada ?: _uiState.value.placaBusca
        val placaParaConsultar = when {
            ValidadorPlaca.ehValida(digitada) -> digitada
            correspondenciasPlaca.value.size == 1 -> correspondenciasPlaca.value.first().placa
            else -> digitada
        }
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    placaBusca = placaParaConsultar,
                    erro = null,
                    mensagemFeedback = null
                )
            }
            try {
                val resultado = veiculoService.consultarPlaca(placaParaConsultar)
                _uiState.update { it.copy(resultadoConsulta = resultado) }
            } catch (e: RegraDeNegocioException) {
                _uiState.update { it.copy(erro = e.message, resultadoConsulta = null) }
            }
        }
    }

    fun registrarEntrada(permitirNaoAutorizado: Boolean = false) {
        val estado = _uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(erro = null, mensagemFeedback = null) }
            try {
                movimentacaoService.registrarEntrada(
                    EntradaRequest(
                        placa = estado.placaBusca,
                        tipoVaga = estado.tipoVagaSelecionado,
                        observacao = if (permitirNaoAutorizado) "Liberação excepcional pela portaria" else "",
                        permitirNaoAutorizado = permitirNaoAutorizado
                    )
                )
                _uiState.update {
                    it.copy(
                        placaBusca = "",
                        resultadoConsulta = null,
                        mensagemFeedback = "Entrada registrada com sucesso."
                    )
                }
            } catch (e: RegraDeNegocioException) {
                _uiState.update { it.copy(erro = e.message) }
            }
        }
    }

    fun registrarSaida(movimentacaoId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(erro = null, mensagemFeedback = null) }
            try {
                val saida = movimentacaoService.registrarSaida(movimentacaoId)
                _uiState.update {
                    val consultaDaMesmaPlaca = it.resultadoConsulta?.placaConsultada == saida.placa
                    it.copy(
                        resultadoConsulta = if (consultaDaMesmaPlaca) null else it.resultadoConsulta,
                        placaBusca = if (consultaDaMesmaPlaca) "" else it.placaBusca,
                        mensagemFeedback = "Saída registrada com sucesso."
                    )
                }
            } catch (e: RegraDeNegocioException) {
                _uiState.update { it.copy(erro = e.message) }
            }
        }
    }

    companion object {
        fun factory(
            veiculoService: VeiculoService,
            movimentacaoService: MovimentacaoService
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                PortariaViewModel(veiculoService, movimentacaoService) as T
        }
    }
}
