package com.parkingsolvers.smartpark.auth.service

import com.parkingsolvers.smartpark.auth.dto.LoginDTO
import com.parkingsolvers.smartpark.auth.model.UsuarioSessao
import com.parkingsolvers.smartpark.compartilhado.validacao.ValidadorFormulario
import com.parkingsolvers.smartpark.rede.ApiErrorMapper
import com.parkingsolvers.smartpark.rede.CredenciaisSessao
import com.parkingsolvers.smartpark.rede.SessaoStore
import com.parkingsolvers.smartpark.rede.SincronizadorDados
import com.parkingsolvers.smartpark.rede.SmartParkApi
import com.parkingsolvers.smartpark.usuario.dto.response.UsuarioResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Serviço de autenticação via API Spring Boot (JWT access + refresh).
 * Mantém a sessão ativa em [SessaoStore] para reuso entre aberturas do aplicativo.
 */
class AuthService(
    private val api: SmartParkApi,
    private val sessaoStore: SessaoStore,
    private val sincronizador: SincronizadorDados,
    private val onSessaoAlterada: () -> Unit = {}
) {
    private val _sessaoAtual = MutableStateFlow(sessaoStore.carregar()?.sessao)
    val sessaoAtual: StateFlow<UsuarioSessao?> = _sessaoAtual.asStateFlow()

    suspend fun login(dto: LoginDTO): UsuarioSessao = autenticar(dto.login, dto.senha)

    suspend fun autenticar(loginEntrada: String, senhaEntrada: String): UsuarioSessao {
        val erros = mutableMapOf<String, String>()
        val loginLimpo = loginEntrada.trim()
        if (loginLimpo.isBlank()) {
            erros["login"] = "Informe o e-mail, login ou prontuário."
        }
        if (senhaEntrada.isBlank()) {
            erros["senha"] = "Informe a senha."
        }
        ValidadorFormulario.lancarSeHouverErros(erros)

        val tokenResp = ApiErrorMapper.executar {
            api.login(LoginDTO(login = loginLimpo, senha = senhaEntrada))
        }
        sessaoStore.salvar(
            CredenciaisSessao(
                accessToken = tokenResp.accessToken,
                refreshToken = tokenResp.refreshToken,
                sessao = tokenResp.sessao
            )
        )
        _sessaoAtual.value = tokenResp.sessao
        sincronizador.notificarMudanca()
        onSessaoAlterada()
        return tokenResp.sessao
    }

    /**
     * Sincroniza a sessão ativa em memória e no storage quando o usuário atualiza seus dados.
     */
    fun sincronizarSessao(usuarioAtualizado: UsuarioResponse) {
        val atual = _sessaoAtual.value ?: return
        if (atual.usuarioId == usuarioAtualizado.id) {
            val novaSessao = atual.copy(
                nome = usuarioAtualizado.nome,
                identificacao = usuarioAtualizado.servidorProntuario ?: usuarioAtualizado.login
            )
            sessaoStore.atualizarSessao(novaSessao)
            _sessaoAtual.value = novaSessao
        }
    }

    fun sair() {
        sessaoStore.limpar()
        _sessaoAtual.value = null
        sincronizador.notificarMudanca()
        onSessaoAlterada()
    }

    fun notificarSessaoExpirada() {
        _sessaoAtual.value = null
        onSessaoAlterada()
    }
}
