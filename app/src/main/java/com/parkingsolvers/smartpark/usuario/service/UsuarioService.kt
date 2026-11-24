package com.parkingsolvers.smartpark.usuario.service

import com.parkingsolvers.smartpark.auth.model.PerfilUsuario
import com.parkingsolvers.smartpark.auth.service.SenhaHasher
import com.parkingsolvers.smartpark.compartilhado.exceptions.ConflitoException
import com.parkingsolvers.smartpark.compartilhado.exceptions.RecursoInativoException
import com.parkingsolvers.smartpark.compartilhado.exceptions.RecursoNaoEncontradoException
import com.parkingsolvers.smartpark.compartilhado.exceptions.RegraDeNegocioException
import com.parkingsolvers.smartpark.compartilhado.validacao.ValidadorFormulario
import com.parkingsolvers.smartpark.servidor.repository.ServidorRepository
import com.parkingsolvers.smartpark.usuario.dto.mapper.UsuarioMapper
import com.parkingsolvers.smartpark.usuario.dto.request.AtualizarUsuarioRequest
import com.parkingsolvers.smartpark.usuario.dto.request.UsuarioRequest
import com.parkingsolvers.smartpark.usuario.dto.response.UsuarioResponse
import com.parkingsolvers.smartpark.usuario.model.Usuario
import com.parkingsolvers.smartpark.usuario.repository.UsuarioRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/**
 * Serviço dedicado à gestão de usuários e níveis de acesso (separado de `AuthService`,
 * seguindo a arquitetura do repositório de referência CarToon).
 */
class UsuarioService(
    private val usuarioRepository: UsuarioRepository,
    private val servidorRepository: ServidorRepository
) {

    fun observarTodos(): Flow<List<UsuarioResponse>> = combine(
        usuarioRepository.observarTodos(),
        servidorRepository.observarTodos()
    ) { usuarios, servidores ->
        val servidoresPorId = servidores.associateBy { it.id }
        usuarios.map { usuario ->
            UsuarioMapper.paraResponse(usuario, usuario.servidorId?.let { servidoresPorId[it] })
        }
    }

    suspend fun buscarPorId(id: Long): UsuarioResponse {
        val usuario = usuarioRepository.buscarPorId(id)
            ?: throw RecursoNaoEncontradoException("Usuário", id)
        val servidor = usuario.servidorId?.let { servidorRepository.buscarPorId(it) }
        return UsuarioMapper.paraResponse(usuario, servidor)
    }

    suspend fun cadastrar(request: UsuarioRequest): UsuarioResponse {
        val erros = mutableMapOf<String, String>()
        val nomeLimpo = request.nome.trim()
        val loginLimpo = request.login.trim().lowercase()

        if (nomeLimpo.length < 3) {
            erros["nome"] = "Informe o nome completo (mínimo 3 caracteres)."
        }
        if (loginLimpo.length < 4 || loginLimpo.contains(" ")) {
            erros["login"] = "Informe um login ou e-mail válido (mínimo 4 caracteres, sem espaços)."
        }
        if (request.senha.length < 6) {
            erros["senha"] = "A senha deve ter no mínimo 6 caracteres."
        } else if (!ValidadorFormulario.senhasCoincidem(request.senha, request.confirmacaoSenha)) {
            erros["confirmacaoSenha"] = "A confirmação de senha não confere."
        }
        if (request.perfil == PerfilUsuario.SERVIDOR && request.servidorId == null) {
            erros["servidor"] = "Selecione o servidor vinculado para contas de nível Servidor."
        }
        ValidadorFormulario.lancarSeHouverErros(erros)

        if (usuarioRepository.buscarPorLogin(loginLimpo) != null) {
            throw ConflitoException("Já existe uma conta cadastrada com o login '$loginLimpo'.")
        }

        val servidor = if (request.perfil == PerfilUsuario.SERVIDOR) {
            val idServidor = request.servidorId!!
            val encontrado = servidorRepository.buscarPorId(idServidor)
                ?: throw RecursoNaoEncontradoException("Servidor", idServidor)
            if (!encontrado.ativo) {
                throw RecursoInativoException("Não é possível criar conta para servidor inativo.")
            }
            if (usuarioRepository.buscarPorServidorId(idServidor) != null) {
                throw ConflitoException("Este servidor já possui uma conta de acesso cadastrada.")
            }
            encontrado
        } else {
            null
        }

        val novoUsuario = Usuario(
            nome = nomeLimpo,
            login = loginLimpo,
            senhaHash = SenhaHasher.gerarHash(request.senha),
            perfil = request.perfil,
            servidorId = servidor?.id,
            ativo = true
        )
        val novoId = usuarioRepository.salvar(novoUsuario)
        return UsuarioMapper.paraResponse(novoUsuario.copy(id = novoId), servidor)
    }

    /**
     * Atualiza os dados cadastrais do próprio usuário autenticado ("Meus Dados").
     */
    suspend fun atualizarProprioPerfil(
        usuarioId: Long,
        request: AtualizarUsuarioRequest
    ): UsuarioResponse {
        val usuario = usuarioRepository.buscarPorId(usuarioId)
            ?: throw RecursoNaoEncontradoException("Usuário", usuarioId)

        val erros = mutableMapOf<String, String>()
        val nomeLimpo = request.nome.trim()
        val loginLimpo = request.login.trim().lowercase()

        if (nomeLimpo.length < 3) {
            erros["nome"] = "Informe o nome completo (mínimo 3 caracteres)."
        }
        if (loginLimpo.length < 4 || loginLimpo.contains(" ")) {
            erros["login"] = "Informe um login ou e-mail válido."
        }
        if (request.novaSenha.isNotEmpty()) {
            if (request.novaSenha.length < 6) {
                erros["novaSenha"] = "A nova senha deve ter no mínimo 6 caracteres."
            } else if (!ValidadorFormulario.senhasCoincidem(request.novaSenha, request.confirmacaoNovaSenha)) {
                erros["confirmacaoNovaSenha"] = "As senhas informadas não coincidem."
            }
        }
        ValidadorFormulario.lancarSeHouverErros(erros)

        val existenteMesmoLogin = usuarioRepository.buscarPorLogin(loginLimpo)
        if (existenteMesmoLogin != null && existenteMesmoLogin.id != usuarioId) {
            throw ConflitoException("O login '$loginLimpo' já está em uso por outra conta.")
        }

        val atualizado = usuario.copy(
            nome = nomeLimpo,
            login = loginLimpo,
            senhaHash = if (request.novaSenha.isNotEmpty()) {
                SenhaHasher.gerarHash(request.novaSenha)
            } else {
                usuario.senhaHash
            }
        )
        usuarioRepository.atualizar(atualizado)
        val servidor = atualizado.servidorId?.let { servidorRepository.buscarPorId(it) }
        return UsuarioMapper.paraResponse(atualizado, servidor)
    }

    suspend fun alternarAtivo(usuarioId: Long, usuarioLogadoId: Long?): UsuarioResponse {
        if (usuarioLogadoId != null && usuarioLogadoId == usuarioId) {
            throw RegraDeNegocioException("Não é possível inativar a própria conta em uso.")
        }
        val usuario = usuarioRepository.buscarPorId(usuarioId)
            ?: throw RecursoNaoEncontradoException("Usuário", usuarioId)

        val atualizado = usuario.copy(ativo = !usuario.ativo)
        usuarioRepository.atualizar(atualizado)
        val servidor = atualizado.servidorId?.let { servidorRepository.buscarPorId(it) }
        return UsuarioMapper.paraResponse(atualizado, servidor)
    }

    suspend fun alterarPerfil(
        usuarioId: Long,
        novoPerfil: PerfilUsuario,
        usuarioLogadoId: Long?,
        servidorId: Long? = null
    ): UsuarioResponse {
        if (usuarioLogadoId != null && usuarioLogadoId == usuarioId) {
            throw RegraDeNegocioException("Não é possível alterar o nível de acesso da própria conta em uso.")
        }
        val usuario = usuarioRepository.buscarPorId(usuarioId)
            ?: throw RecursoNaoEncontradoException("Usuário", usuarioId)

        val servidor = if (novoPerfil == PerfilUsuario.SERVIDOR) {
            val idVincular = servidorId ?: usuario.servidorId
                ?: throw RegraDeNegocioException("Selecione um servidor para o nível Servidor / Docente.")
            val encontrado = servidorRepository.buscarPorId(idVincular)
                ?: throw RecursoNaoEncontradoException("Servidor", idVincular)
            val jaVinculado = usuarioRepository.buscarPorServidorId(idVincular)
            if (jaVinculado != null && jaVinculado.id != usuarioId) {
                throw ConflitoException("Este servidor já está vinculado a outra conta.")
            }
            encontrado
        } else {
            null
        }

        val atualizado = usuario.copy(
            perfil = novoPerfil,
            servidorId = servidor?.id
        )
        usuarioRepository.atualizar(atualizado)
        return UsuarioMapper.paraResponse(atualizado, servidor)
    }
}
