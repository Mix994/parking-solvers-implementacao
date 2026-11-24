package com.parkingsolvers.smartpark.auth.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.parkingsolvers.smartpark.auth.model.PerfilUsuario
import com.parkingsolvers.smartpark.auth.service.AuthService
import com.parkingsolvers.smartpark.compartilhado.exceptions.RegraDeNegocioException
import com.parkingsolvers.smartpark.compartilhado.exceptions.ValidacaoException
import com.parkingsolvers.smartpark.compartilhado.validacao.ValidadorFormulario
import com.parkingsolvers.smartpark.dev.DadosIniciais
import com.parkingsolvers.smartpark.servidor.dto.request.ServidorRequest
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
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginUiState(
    val login: String = "",
    val senha: String = "",
    val carregando: Boolean = false,
    val erroGeral: String? = null,
    val errosCampos: Map<String, String> = emptyMap(),
    val mensagemFeedback: String? = null,
    val erroCadastro: String? = null,
    val errosCadastroCampos: Map<String, String> = emptyMap()
)

class LoginViewModel(
    private val authService: AuthService,
    private val usuarioService: UsuarioService,
    private val servidorService: ServidorService
) : ViewModel() {

    val usuariosAtivos: StateFlow<List<UsuarioResponse>> = usuarioService.observarTodos()
        .map { lista -> lista.filter { it.ativo } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val servidoresSemConta: StateFlow<List<ServidorResponse>> = combine(
        servidorService.observarTodos(),
        usuarioService.observarTodos()
    ) { servidores, usuarios ->
        val idsComConta = usuarios.mapNotNull { it.servidorId }.toSet()
        servidores.filter { it.ativo && it.id !in idsComConta }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

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

    fun preencherCredencialDemo(usuario: UsuarioResponse) {
        _uiState.update {
            it.copy(
                login = usuario.login,
                senha = DadosIniciais.senhaDemoParaPerfil(usuario.perfil),
                erroGeral = null,
                errosCampos = emptyMap(),
                mensagemFeedback = "Credenciais de ${usuario.perfil.label} preenchidas. Toque em Entrar."
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

    fun cadastrarConta(
        nome: String,
        login: String,
        senha: String,
        confirmacaoSenha: String,
        perfil: PerfilUsuario,
        servidorExistenteId: Long?,
        prontuarioNovo: String,
        telefoneNovo: String,
        setorNovo: String,
        onSucesso: () -> Unit
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(erroCadastro = null, errosCadastroCampos = emptyMap()) }
            try {
                val errosPre = mutableMapOf<String, String>()
                if (nome.trim().length < 3) {
                    errosPre["nome"] = "Informe o nome completo (mínimo 3 caracteres)."
                }
                val loginLimpo = login.trim().lowercase()
                if (loginLimpo.length < 4 || loginLimpo.contains(" ")) {
                    errosPre["login"] = "Informe um login ou e-mail válido (mínimo 4 caracteres, sem espaços)."
                }
                if (senha.length < 6) {
                    errosPre["senha"] = "A senha deve ter no mínimo 6 caracteres."
                } else if (!ValidadorFormulario.senhasCoincidem(senha, confirmacaoSenha)) {
                    errosPre["confirmacaoSenha"] = "A confirmação de senha não confere."
                }
                ValidadorFormulario.lancarSeHouverErros(errosPre)

                val servidorIdParaVincular = if (perfil == PerfilUsuario.SERVIDOR) {
                    if (servidorExistenteId != null && servidorExistenteId > 0L) {
                        servidorExistenteId
                    } else {
                        val servidorCriado = servidorService.cadastrar(
                            ServidorRequest(
                                nome = nome,
                                prontuario = prontuarioNovo,
                                email = login,
                                telefone = telefoneNovo,
                                setor = setorNovo
                            )
                        )
                        servidorCriado.id
                    }
                } else {
                    null
                }

                usuarioService.cadastrar(
                    UsuarioRequest(
                        nome = nome,
                        login = login,
                        senha = senha,
                        confirmacaoSenha = confirmacaoSenha,
                        perfil = perfil,
                        servidorId = servidorIdParaVincular
                    )
                )

                _uiState.update {
                    it.copy(
                        login = loginLimpo,
                        senha = "",
                        mensagemFeedback = "Conta criada no banco local. Informe a senha para entrar.",
                        erroCadastro = null,
                        errosCadastroCampos = emptyMap()
                    )
                }
                onSucesso()
            } catch (e: ValidacaoException) {
                _uiState.update {
                    it.copy(errosCadastroCampos = e.errosValidacao, erroCadastro = e.message)
                }
            } catch (e: RegraDeNegocioException) {
                _uiState.update {
                    it.copy(erroCadastro = e.message)
                }
            }
        }
    }

    fun limparErrosCadastro() {
        _uiState.update { it.copy(erroCadastro = null, errosCadastroCampos = emptyMap()) }
    }

    companion object {
        fun factory(
            authService: AuthService,
            usuarioService: UsuarioService,
            servidorService: ServidorService
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                LoginViewModel(authService, usuarioService, servidorService) as T
        }
    }
}
