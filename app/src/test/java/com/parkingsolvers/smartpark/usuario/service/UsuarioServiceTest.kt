package com.parkingsolvers.smartpark.usuario.service

import com.parkingsolvers.smartpark.auth.model.PerfilUsuario
import com.parkingsolvers.smartpark.auth.service.AuthServiceTest
import com.parkingsolvers.smartpark.auth.service.SenhaHasher
import com.parkingsolvers.smartpark.compartilhado.exceptions.ConflitoException
import com.parkingsolvers.smartpark.compartilhado.exceptions.RegraDeNegocioException
import com.parkingsolvers.smartpark.compartilhado.exceptions.ValidacaoException
import com.parkingsolvers.smartpark.servidor.repository.ServidorRepository
import com.parkingsolvers.smartpark.usuario.dto.request.AtualizarUsuarioRequest
import com.parkingsolvers.smartpark.usuario.dto.request.UsuarioRequest
import com.parkingsolvers.smartpark.usuario.repository.UsuarioRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class UsuarioServiceTest {

    @Test(expected = ConflitoException::class)
    fun impedeCadastroDeLoginDuplicado() = runBlocking {
        val service = criarService()
        service.cadastrar(UsuarioRequest("Admin 1", "admin@campus.edu.br", "admin123", "admin123", PerfilUsuario.ADMIN))
        service.cadastrar(UsuarioRequest("Admin 2", "ADMIN@CAMPUS.EDU.BR", "outrasenha", "outrasenha", PerfilUsuario.ADMIN))
        Unit
    }

    @Test(expected = ValidacaoException::class)
    fun rejeitaConfirmacaoDeSenhaDivergente() = runBlocking {
        val service = criarService()
        service.cadastrar(UsuarioRequest("Porteiro", "portaria@campus.edu.br", "123456", "654321", PerfilUsuario.PORTARIA))
        Unit
    }

    @Test(expected = RegraDeNegocioException::class)
    fun impedeAutoInativacaoDaPropriaConta() = runBlocking {
        val service = criarService()
        val admin = service.cadastrar(UsuarioRequest("Admin", "admin@campus.edu.br", "admin123", "admin123", PerfilUsuario.ADMIN))
        service.alternarAtivo(admin.id, usuarioLogadoId = admin.id)
        Unit
    }

    @Test
    fun atualizaProprioPerfilMantendoOuAlterandoSenha() = runBlocking {
        val usuarioDao = AuthServiceTest.FakeUsuarioDao()
        val usuarioRepo = UsuarioRepository(usuarioDao)
        val service = UsuarioService(usuarioRepo, ServidorRepository(AuthServiceTest.FakeServidorDao()))

        val criada = service.cadastrar(
            UsuarioRequest("Guarita 01", "guarita@campus.edu.br", "senha123", "senha123", PerfilUsuario.PORTARIA)
        )

        val atualizadoSemSenha = service.atualizarProprioPerfil(
            usuarioId = criada.id,
            request = AtualizarUsuarioRequest(nome = "Guarita Principal", login = "guarita@campus.edu.br")
        )
        assertEquals("Guarita Principal", atualizadoSemSenha.nome)
        assertTrue(SenhaHasher.verificar("senha123", usuarioRepo.buscarPorId(criada.id)!!.senhaHash))

        service.atualizarProprioPerfil(
            usuarioId = criada.id,
            request = AtualizarUsuarioRequest(
                nome = "Guarita Principal",
                login = "guarita.nova@campus.edu.br",
                novaSenha = "novaSenha99",
                confirmacaoNovaSenha = "novaSenha99"
            )
        )
        val entidade = usuarioRepo.buscarPorId(criada.id)!!
        assertEquals("guarita.nova@campus.edu.br", entidade.login)
        assertTrue(SenhaHasher.verificar("novaSenha99", entidade.senhaHash))
    }

    private fun criarService(): UsuarioService =
        UsuarioService(
            UsuarioRepository(AuthServiceTest.FakeUsuarioDao()),
            ServidorRepository(AuthServiceTest.FakeServidorDao())
        )
}
