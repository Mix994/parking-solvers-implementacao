package com.parkingsolvers.smartpark.veiculo.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.parkingsolvers.smartpark.compartilhado.exceptions.RegraDeNegocioException
import com.parkingsolvers.smartpark.compartilhado.exceptions.ValidacaoException
import com.parkingsolvers.smartpark.servidor.dto.response.ServidorResponse
import com.parkingsolvers.smartpark.servidor.service.ServidorService
import com.parkingsolvers.smartpark.veiculo.dto.request.VeiculoRequest
import com.parkingsolvers.smartpark.veiculo.dto.response.VeiculoResponse
import com.parkingsolvers.smartpark.veiculo.service.VeiculoService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class VeiculoUiState(
    val mensagemFeedback: String? = null,
    val erroGeral: String? = null,
    val errosCampos: Map<String, String> = emptyMap()
)

class VeiculoViewModel(
    private val veiculoService: VeiculoService,
    servidorService: ServidorService,
    servidorIdFiltro: Long?
) : ViewModel() {

    val veiculos: StateFlow<List<VeiculoResponse>> = (
        if (servidorIdFiltro != null) {
            veiculoService.observarPorServidor(servidorIdFiltro)
        } else {
            veiculoService.observarTodos()
        }
    ).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val servidores: StateFlow<List<ServidorResponse>> = servidorService.observarTodos()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _uiState = MutableStateFlow(VeiculoUiState())
    val uiState: StateFlow<VeiculoUiState> = _uiState.asStateFlow()

    fun cadastrar(request: VeiculoRequest, onSucesso: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(erroGeral = null, errosCampos = emptyMap(), mensagemFeedback = null) }
            try {
                veiculoService.cadastrar(request)
                _uiState.update { it.copy(mensagemFeedback = "Veículo cadastrado com sucesso.") }
                onSucesso()
            } catch (e: ValidacaoException) {
                _uiState.update { it.copy(errosCampos = e.errosValidacao, erroGeral = e.message) }
            } catch (e: RegraDeNegocioException) {
                _uiState.update { it.copy(erroGeral = e.message) }
            }
        }
    }

    fun alternarAtivo(id: Long) {
        viewModelScope.launch {
            try {
                val atualizado = veiculoService.alternarAtivo(id)
                val msg = if (atualizado.ativo) "Veículo reativado." else "Veículo inativado."
                _uiState.update { it.copy(mensagemFeedback = msg, erroGeral = null) }
            } catch (e: RegraDeNegocioException) {
                _uiState.update { it.copy(erroGeral = e.message) }
            }
        }
    }

    fun limparErrosFormulario() {
        _uiState.update { it.copy(erroGeral = null, errosCampos = emptyMap()) }
    }

    fun limparFeedback() {
        _uiState.update { it.copy(mensagemFeedback = null, erroGeral = null) }
    }

    companion object {
        fun factory(
            veiculoService: VeiculoService,
            servidorService: ServidorService,
            servidorIdFiltro: Long?
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                VeiculoViewModel(veiculoService, servidorService, servidorIdFiltro) as T
        }
    }
}
