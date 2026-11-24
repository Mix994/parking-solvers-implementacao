package com.parkingsolvers.smartpark.usuario.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.parkingsolvers.smartpark.compartilhado.exceptions.RegraDeNegocioException
import com.parkingsolvers.smartpark.compartilhado.exceptions.ValidacaoException
import com.parkingsolvers.smartpark.servidor.dto.response.ServidorResponse
import com.parkingsolvers.smartpark.servidor.service.ServidorService
import com.parkingsolvers.smartpark.usuario.dto.request.UsuarioRequest
import com.parkingsolvers.smartpark.usuario.dto.response.UsuarioResponse
import com.parkingsolvers.smartpark.usuario.service.UsuarioService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class UsuarioUiState(
    val mensagemFeedback: String? = null,
    val erroGeral: String? = null,
    val errosCampos: Map<String, String> = emptyMap()
)

class UsuarioViewModel(
    private val usuarioService: UsuarioService,
    servidorService: ServidorService
) : ViewModel() {

    val usuarios: StateFlow<List<UsuarioResponse>> = usuarioService.observarTodos()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val servidoresSemConta: StateFlow<List<ServidorResponse>> = combine(
        servidorService.observarTodos(),
        usuarioService.observarTodos()
    ) { servidores, contas ->
        val idsVinculados = contas.mapNotNull { it.servidorId }.toSet()
        servidores.filter { it.ativo && it.id !in idsVinculados }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _uiState = MutableStateFlow(UsuarioUiState())
    val uiState: StateFlow<UsuarioUiState> = _uiState.asStateFlow()

    fun cadastrar(request: UsuarioRequest, onSucesso: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(erroGeral = null, errosCampos = emptyMap(), mensagemFeedback = null) }
            try {
                usuarioService.cadastrar(request)
                _uiState.update { it.copy(mensagemFeedback = "Conta cadastrada com sucesso.") }
                onSucesso()
            } catch (e: ValidacaoException) {
                _uiState.update { it.copy(errosCampos = e.errosValidacao, erroGeral = e.message) }
            } catch (e: RegraDeNegocioException) {
                _uiState.update { it.copy(erroGeral = e.message) }
            }
        }
    }

    fun alternarAtivo(usuarioId: Long, usuarioLogadoId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(erroGeral = null, mensagemFeedback = null) }
            try {
                val atualizado = usuarioService.alternarAtivo(usuarioId, usuarioLogadoId)
                val msg = if (atualizado.ativo) "Conta reativada." else "Conta inativada."
                _uiState.update { it.copy(mensagemFeedback = msg) }
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
            usuarioService: UsuarioService,
            servidorService: ServidorService
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                UsuarioViewModel(usuarioService, servidorService) as T
        }
    }
}
