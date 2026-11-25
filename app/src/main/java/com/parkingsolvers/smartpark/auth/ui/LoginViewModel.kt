package com.parkingsolvers.smartpark.auth.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.parkingsolvers.smartpark.auth.service.AuthService
import com.parkingsolvers.smartpark.compartilhado.exceptions.RegraDeNegocioException
import com.parkingsolvers.smartpark.compartilhado.exceptions.ValidacaoException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginUiState(
    val login: String = "",
    val senha: String = "",
    val carregando: Boolean = false,
    val erroGeral: String? = null,
    val errosCampos: Map<String, String> = emptyMap(),
    val mensagemFeedback: String? = null
)

class LoginViewModel(
    private val authService: AuthService
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun atualizarLogin(valor: String) {
        _uiState.update {
            it.copy(
                login = valor,
                erroGeral = null,
                errosCampos = it.errosCampos - "login"
            )
        }
    }

    fun atualizarSenha(valor: String) {
        _uiState.update {
            it.copy(
                senha = valor,
                erroGeral = null,
                errosCampos = it.errosCampos - "senha"
            )
        }
    }

    fun entrar() {
        val estado = _uiState.value
        viewModelScope.launch {
            _uiState.update {
                it.copy(carregando = true, erroGeral = null, errosCampos = emptyMap(), mensagemFeedback = null)
            }
            try {
                authService.autenticar(estado.login, estado.senha)
                _uiState.update { it.copy(carregando = false, senha = "") }
            } catch (e: ValidacaoException) {
                _uiState.update {
                    it.copy(
                        carregando = false,
                        errosCampos = e.errosValidacao,
                        erroGeral = e.message
                    )
                }
            } catch (e: RegraDeNegocioException) {
                _uiState.update {
                    it.copy(carregando = false, erroGeral = e.message)
                }
            }
        }
    }

    companion object {
        fun factory(authService: AuthService): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    LoginViewModel(authService) as T
            }
    }
}
