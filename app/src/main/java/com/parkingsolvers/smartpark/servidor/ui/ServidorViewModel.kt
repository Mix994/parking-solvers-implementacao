package com.parkingsolvers.smartpark.servidor.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.parkingsolvers.smartpark.compartilhado.exceptions.RegraDeNegocioException
import com.parkingsolvers.smartpark.compartilhado.exceptions.ValidacaoException
import com.parkingsolvers.smartpark.servidor.dto.request.ServidorRequest
import com.parkingsolvers.smartpark.servidor.dto.response.ServidorResponse
import com.parkingsolvers.smartpark.servidor.service.ServidorService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ServidorUiState(
    val mensagemFeedback: String? = null,
    val erroGeral: String? = null,
    val errosCampos: Map<String, String> = emptyMap()
)

class ServidorViewModel(
    private val servidorService: ServidorService
) : ViewModel() {

    val servidores: StateFlow<List<ServidorResponse>> = servidorService.observarTodos()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _uiState = MutableStateFlow(ServidorUiState())
    val uiState: StateFlow<ServidorUiState> = _uiState.asStateFlow()

    fun cadastrar(request: ServidorRequest, onSucesso: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(erroGeral = null, errosCampos = emptyMap(), mensagemFeedback = null) }
            try {
                servidorService.cadastrar(request)
                _uiState.update { it.copy(mensagemFeedback = "Servidor cadastrado com sucesso.") }
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
                val atualizado = servidorService.alternarAtivo(id)
                val texto = if (atualizado.ativo) "Servidor reativado." else "Servidor inativado."
                _uiState.update { it.copy(mensagemFeedback = texto, erroGeral = null) }
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
        fun factory(service: ServidorService): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    ServidorViewModel(service) as T
            }
    }
}
