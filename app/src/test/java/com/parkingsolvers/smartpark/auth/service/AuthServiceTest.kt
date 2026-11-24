package com.parkingsolvers.smartpark.auth.service

import com.parkingsolvers.smartpark.auth.model.PerfilUsuario
import com.parkingsolvers.smartpark.compartilhado.exceptions.BloqueioTentativasException
import com.parkingsolvers.smartpark.compartilhado.exceptions.CredenciaisInvalidasException
import com.parkingsolvers.smartpark.compartilhado.exceptions.RecursoInativoException
import com.parkingsolvers.smartpark.servidor.model.Servidor
import com.parkingsolvers.smartpark.servidor.repository.ServidorDao
import com.parkingsolvers.smartpark.servidor.repository.ServidorRepository
import com.parkingsolvers.smartpark.usuario.dto.request.UsuarioRequest
import com.parkingsolvers.smartpark.usuario.model.Usuario
import com.parkingsolvers.smartpark.usuario.repository.UsuarioDao
import com.parkingsolvers.smartpark.usuario.repository.UsuarioRepository
import com.parkingsolvers.smartpark.usuario.service.UsuarioService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AuthServiceTest {

    @Test
    fun autenticaAdminPortariaEServidorPorEmailEProntuario() = runBlocking {
        val servidorRepo = ServidorRepository(FakeServidorDao())
        val usuarioRepo = UsuarioRepository(FakeUsuarioDao())
        val usuarioService = UsuarioService(usuarioRepo, servidorRepo)
        val authService = AuthService(usuarioRepo, servidorRepo, LoginAttemptService())

        val idServidor = servidorRepo.salvar(
            Servidor(
                nome = "Prof. Ana",
                prontuario = "BP102401",
                email = "ana@campus.edu.br",
                telefone = "11999990001",
                setor = "BCC"
            )
        )

        usuarioService.cadastrar(
            UsuarioRequest("Admin", "admin@campus.edu.br", "admin123", "admin123", PerfilUsuario.ADMIN)
        )
        usuarioService.cadastrar(
            UsuarioRequest("Guarita", "portaria@campus.edu.br", "portaria123", "portaria123", PerfilUsuario.PORTARIA)
        )
        usuarioService.cadastrar(
            UsuarioRequest("Prof. Ana", "ana@campus.edu.br", "123456", "123456", PerfilUsuario.SERVIDOR, idServidor)
        )

        val sessaoAdmin = authService.autenticar("admin@campus.edu.br", "admin123")
        assertEquals(PerfilUsuario.ADMIN, sessaoAdmin.perfil)

        val sessaoPortaria = authService.autenticar("portaria@campus.edu.br", "portaria123")
        assertEquals(PerfilUsuario.PORTARIA, sessaoPortaria.perfil)

        val sessaoServidor = authService.autenticar("BP102401", "123456")
        assertEquals(PerfilUsuario.SERVIDOR, sessaoServidor.perfil)
        assertEquals(idServidor, sessaoServidor.servidorId)

        authService.sair()
        assertNull(authService.sessaoAtual.value)
    }

    @Test(expected = CredenciaisInvalidasException::class)
    fun rejeitaSenhaIncorretaComCredenciaisInvalidasException() = runBlocking {
        val servidorRepo = ServidorRepository(FakeServidorDao())
        val usuarioRepo = UsuarioRepository(FakeUsuarioDao())
        val usuarioService = UsuarioService(usuarioRepo, servidorRepo)
        val authService = AuthService(usuarioRepo, servidorRepo, LoginAttemptService())

        usuarioService.cadastrar(
            UsuarioRequest("Admin", "admin@campus.edu.br", "admin123", "admin123", PerfilUsuario.ADMIN)
        )
        authService.autenticar("admin@campus.edu.br", "errada")
        Unit
    }

    @Test(expected = BloqueioTentativasException::class)
    fun bloqueiaContaAposCincoTentativasIncorretas() = runBlocking {
        val servidorRepo = ServidorRepository(FakeServidorDao())
        val usuarioRepo = UsuarioRepository(FakeUsuarioDao())
        val usuarioService = UsuarioService(usuarioRepo, servidorRepo)
        val authService = AuthService(usuarioRepo, servidorRepo, LoginAttemptService())

        usuarioService.cadastrar(
            UsuarioRequest("Admin", "admin@campus.edu.br", "admin123", "admin123", PerfilUsuario.ADMIN)
        )

        repeat(5) {
            try {
                authService.autenticar("admin@campus.edu.br", "senha_errada")
            } catch (_: CredenciaisInvalidasException) {
                // esperado nas 5 primeiras falhas
            }
        }

        // 6ª tentativa mesmo com senha certa deve lançar BloqueioTentativasException
        authService.autenticar("admin@campus.edu.br", "admin123")
        Unit
    }

    @Test(expected = RecursoInativoException::class)
    fun rejeitaContaInativa() = runBlocking {
        val servidorRepo = ServidorRepository(FakeServidorDao())
        val usuarioRepo = UsuarioRepository(FakeUsuarioDao())
        val usuarioService = UsuarioService(usuarioRepo, servidorRepo)
        val authService = AuthService(usuarioRepo, servidorRepo, LoginAttemptService())

        val criada = usuarioService.cadastrar(
            UsuarioRequest("Porteiro", "portaria@campus.edu.br", "portaria123", "portaria123", PerfilUsuario.PORTARIA)
        )
        usuarioService.alternarAtivo(criada.id, usuarioLogadoId = 999L)
        authService.autenticar("portaria@campus.edu.br", "portaria123")
        Unit
    }

    internal class FakeUsuarioDao : UsuarioDao {
        private val itens = mutableListOf<Usuario>()
        private val fluxo = MutableStateFlow<List<Usuario>>(emptyList())
        private var seq = 1L

        override fun observarTodos(): Flow<List<Usuario>> = fluxo
        override fun observarPorPerfil(perfil: PerfilUsuario): Flow<List<Usuario>> = fluxo
        override suspend fun buscarPorId(id: Long): Usuario? = itens.firstOrNull { it.id == id }
        override suspend fun buscarPorLogin(login: String): Usuario? =
            itens.firstOrNull { it.login.equals(login, ignoreCase = true) }
        override suspend fun buscarPorServidorId(servidorId: Long): Usuario? =
            itens.firstOrNull { it.servidorId == servidorId }
        override suspend fun contar(): Int = itens.size
        override suspend fun inserir(usuario: Usuario): Long {
            val id = seq++
            itens.add(usuario.copy(id = id))
            fluxo.value = itens.toList()
            return id
        }
        override suspend fun atualizar(usuario: Usuario) {
            val idx = itens.indexOfFirst { it.id == usuario.id }
            if (idx >= 0) {
                itens[idx] = usuario
                fluxo.value = itens.toList()
            }
        }
    }

    internal class FakeServidorDao : ServidorDao {
        private val itens = mutableListOf<Servidor>()
        private val fluxo = MutableStateFlow<List<Servidor>>(emptyList())
        private var seq = 1L

        override fun observarTodos(): Flow<List<Servidor>> = fluxo
        override suspend fun listarTodos(): List<Servidor> = itens.toList()
        override suspend fun buscarPorId(id: Long): Servidor? = itens.firstOrNull { it.id == id }
        override suspend fun buscarPorProntuario(prontuario: String): Servidor? =
            itens.firstOrNull { it.prontuario.equals(prontuario, ignoreCase = true) }
        override suspend fun contar(): Int = itens.size
        override suspend fun inserir(servidor: Servidor): Long {
            val id = seq++
            itens.add(servidor.copy(id = id))
            fluxo.value = itens.toList()
            return id
        }
        override suspend fun atualizar(servidor: Servidor) {
            val idx = itens.indexOfFirst { it.id == servidor.id }
            if (idx >= 0) {
                itens[idx] = servidor
                fluxo.value = itens.toList()
            }
        }
    }
}
