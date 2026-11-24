package com.parkingsolvers.smartpark.auth.service

import com.parkingsolvers.smartpark.auth.dto.LoginDTO
import com.parkingsolvers.smartpark.auth.model.PerfilUsuario
import com.parkingsolvers.smartpark.auth.model.UsuarioSessao
import com.parkingsolvers.smartpark.compartilhado.exceptions.BloqueioTentativasException
import com.parkingsolvers.smartpark.compartilhado.exceptions.CredenciaisInvalidasException
import com.parkingsolvers.smartpark.compartilhado.exceptions.RecursoInativoException
import com.parkingsolvers.smartpark.compartilhado.exceptions.RecursoNaoEncontradoException
import com.parkingsolvers.smartpark.compartilhado.validacao.ValidadorFormulario
import com.parkingsolvers.smartpark.servidor.repository.ServidorRepository
import com.parkingsolvers.smartpark.usuario.dto.response.UsuarioResponse
import com.parkingsolvers.smartpark.usuario.model.Usuario
import com.parkingsolvers.smartpark.usuario.repository.UsuarioRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Serviço de autenticação (separado da gestão de usuários, espelhando `AuthService` do CarToon).
 * Inclui bloqueio por força bruta via [LoginAttemptService] e validação de status do usuário e servidor.
 */
class AuthService(
    private val usuarioRepository: UsuarioRepository,
    private val servidorRepository: ServidorRepository,
    private val loginAttemptService: LoginAttemptService = LoginAttemptService()
) {

    private val _sessaoAtual = MutableStateFlow<UsuarioSessao?>(null)
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

        if (loginAttemptService.estaBloqueado(loginLimpo)) {
            throw BloqueioTentativasException()
        }

        val usuario = localizarUsuarioParaLogin(loginLimpo)
        if (usuario == null || !SenhaHasher.verificar(senhaEntrada, usuario.senhaHash)) {
            loginAttemptService.registrarFalha(loginLimpo)
            throw CredenciaisInvalidasException()
        }

        if (!usuario.ativo) {
            throw RecursoInativoException("Esta conta está inativa. Procure a Administração.")
        }

        val servidorVinculado = usuario.servidorId?.let { servidorRepository.buscarPorId(it) }
        if (usuario.perfil == PerfilUsuario.SERVIDOR) {
            if (servidorVinculado == null) {
                throw RecursoNaoEncontradoException("Servidor vinculado não encontrado para esta conta.")
            }
            if (!servidorVinculado.ativo) {
                throw RecursoInativoException("O servidor vinculado a esta conta está inativo.")
            }
        }

        loginAttemptService.registrarSucesso(loginLimpo)

        val sessao = UsuarioSessao(
            usuarioId = usuario.id,
            nome = usuario.nome,
            identificacao = servidorVinculado?.prontuario ?: usuario.login,
            perfil = usuario.perfil,
            servidorId = servidorVinculado?.id
        )
        _sessaoAtual.value = sessao
        return sessao
    }

    /**
     * Sincroniza a sessão ativa em memória quando o usuário atualiza seus dados em "Meus Dados".
     */
    fun sincronizarSessao(usuarioAtualizado: UsuarioResponse) {
        val atual = _sessaoAtual.value ?: return
        if (atual.usuarioId == usuarioAtualizado.id) {
            _sessaoAtual.value = atual.copy(
                nome = usuarioAtualizado.nome,
                identificacao = usuarioAtualizado.servidorProntuario ?: usuarioAtualizado.login
            )
        }
    }

    fun sair() {
        _sessaoAtual.value = null
    }

    private suspend fun localizarUsuarioParaLogin(loginLimpo: String): Usuario? {
        usuarioRepository.buscarPorLogin(loginLimpo)?.let { return it }
        val servidorPorProntuario = servidorRepository.buscarPorProntuario(loginLimpo.uppercase())
        return servidorPorProntuario?.let { usuarioRepository.buscarPorServidorId(it.id) }
    }
}
