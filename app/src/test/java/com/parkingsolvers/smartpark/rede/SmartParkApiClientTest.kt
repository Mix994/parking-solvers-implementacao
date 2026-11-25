package com.parkingsolvers.smartpark.rede

import com.parkingsolvers.smartpark.auth.model.PerfilUsuario
import com.parkingsolvers.smartpark.auth.service.AuthService
import com.parkingsolvers.smartpark.compartilhado.exceptions.BloqueioTentativasException
import com.parkingsolvers.smartpark.compartilhado.exceptions.ConflitoException
import com.parkingsolvers.smartpark.compartilhado.exceptions.CredenciaisInvalidasException
import com.parkingsolvers.smartpark.compartilhado.exceptions.ValidacaoException
import com.parkingsolvers.smartpark.movimentacao.dto.request.EntradaRequest
import com.parkingsolvers.smartpark.movimentacao.model.TipoVaga
import com.parkingsolvers.smartpark.movimentacao.model.Turno
import com.parkingsolvers.smartpark.movimentacao.service.MovimentacaoService
import com.parkingsolvers.smartpark.servidor.dto.request.ServidorRequest
import com.parkingsolvers.smartpark.servidor.service.ServidorService
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SmartParkApiClientTest {

    private lateinit var server: MockWebServer
    private lateinit var store: MemoriaSessaoStore
    private lateinit var sincronizador: SincronizadorDados
    private lateinit var api: SmartParkApi
    private lateinit var authService: AuthService
    private var expirouCount = 0

    @Before
    fun setup() {
        server = MockWebServer()
        server.start()
        store = MemoriaSessaoStore()
        sincronizador = SincronizadorDados()
        expirouCount = 0
        api = ApiClient.criar(
            baseUrl = server.url("/").toString(),
            sessaoStore = store,
            debug = false,
            onSessaoExpirada = {
                expirouCount++
                authService.notificarSessaoExpirada()
            }
        )
        authService = AuthService(api, store, sincronizador)
    }

    @After
    fun teardown() {
        server.shutdown()
    }

    @Test
    fun `login salva tokens e envia bearer nas chamadas seguintes`() = runTest {
        server.enqueue(
            MockResponse().setResponseCode(200).setBody(
                """
                {
                  "accessToken": "tok-access-1",
                  "refreshToken": "tok-refresh-1",
                  "expiraEmSegundos": 3600,
                  "sessao": {
                    "usuarioId": 1,
                    "nome": "Admin",
                    "identificacao": "admin",
                    "perfil": "ADMIN",
                    "servidorId": null
                  }
                }
                """.trimIndent()
            )
        )
        server.enqueue(
            MockResponse().setResponseCode(201).setBody(
                """
                {
                  "id": 10,
                  "nome": "Ana",
                  "prontuario": "SP1234",
                  "email": "ana@ifsp.edu.br",
                  "telefone": "(11) 99999-8888",
                  "setor": "TI",
                  "ativo": true
                }
                """.trimIndent()
            )
        )

        val sessao = authService.autenticar("admin", "admin123")
        assertEquals(PerfilUsuario.ADMIN, sessao.perfil)
        assertEquals("tok-access-1", store.carregar()?.accessToken)

        val servidorService = ServidorService(api, sincronizador)
        val criado = servidorService.cadastrar(ServidorRequest("Ana", "SP1234", "ana@ifsp.edu.br", "(11) 99999-8888", "TI"))
        assertEquals(10L, criado.id)

        val loginReq = server.takeRequest()
        assertEquals("/auth/login", loginReq.path)
        assertNull(loginReq.getHeader("Authorization"))

        val srvReq = server.takeRequest()
        assertEquals("/servidores", srvReq.path)
        assertEquals("Bearer tok-access-1", srvReq.getHeader("Authorization"))
    }

    @Test
    fun `renova token automaticamente ao receber 401 e refaz requisicao`() = runTest {
        store.salvar(
            CredenciaisSessao(
                accessToken = "tok-expirado",
                refreshToken = "tok-refresh",
                sessao = com.parkingsolvers.smartpark.auth.model.UsuarioSessao(1, "Port", "port", PerfilUsuario.PORTARIA)
            )
        )

        // 1ª tentativa -> 401
        server.enqueue(MockResponse().setResponseCode(401).setBody("""{"status":401,"mensagem":"Não autenticado"}"""))
        // Chamada de /auth/refresh -> 200
        server.enqueue(
            MockResponse().setResponseCode(200).setBody(
                """
                {
                  "accessToken": "tok-novo",
                  "refreshToken": "tok-refresh-2",
                  "expiraEmSegundos": 3600,
                  "sessao": {
                    "usuarioId": 1,
                    "nome": "Port",
                    "identificacao": "port",
                    "perfil": "PORTARIA"
                  }
                }
                """.trimIndent()
            )
        )
        // Reenvio automático da requisição original -> 201
        server.enqueue(
            MockResponse().setResponseCode(201).setBody(
                """
                {
                  "id": 42,
                  "veiculoId": 7,
                  "placa": "ABC1D23",
                  "modeloCor": "Onix • Prata",
                  "servidorNome": "Ana",
                  "autorizado": true,
                  "dataHoraEntrada": "2026-03-10T15:00:00Z",
                  "dataHoraSaida": null,
                  "turno": "TARDE",
                  "tipoVaga": "COMUM",
                  "observacao": "",
                  "emAberto": true
                }
                """.trimIndent()
            )
        )

        val movService = MovimentacaoService(api, sincronizador)
        val mov = movService.registrarEntrada(EntradaRequest("ABC1D23", TipoVaga.COMUM))
        assertEquals(42L, mov.id)
        assertEquals(Turno.TARDE, mov.turno)
        assertTrue(mov.emAberto)
        assertEquals("tok-novo", store.carregar()?.accessToken)

        val r1 = server.takeRequest()
        assertEquals("Bearer tok-expirado", r1.getHeader("Authorization"))
        val rRefresh = server.takeRequest()
        assertEquals("/auth/refresh", rRefresh.path)
        val rRetry = server.takeRequest()
        assertEquals("Bearer tok-novo", rRetry.getHeader("Authorization"))
    }

    @Test
    fun `mapeia erros http 422, 409, 401 e 429 para excecoes de dominio`() = runTest {
        val servidorService = ServidorService(api, sincronizador)

        // 422 com campos -> ValidacaoException
        server.enqueue(
            MockResponse().setResponseCode(422).setBody(
                """{"status":422,"mensagem":"Dados inválidos","campos":{"email":"E-mail inválido"}}"""
            )
        )
        val exValidacao = runCatching {
            servidorService.cadastrar(ServidorRequest("", "", "", "", ""))
        }.exceptionOrNull()
        assertTrue(exValidacao is ValidacaoException)
        assertEquals("E-mail inválido", (exValidacao as ValidacaoException).errosValidacao["email"])

        // 409 -> ConflitoException
        server.enqueue(MockResponse().setResponseCode(409).setBody("""{"status":409,"mensagem":"Prontuário duplicado"}"""))
        val exConflito = runCatching {
            servidorService.cadastrar(ServidorRequest("Ana", "SP1", "a@b.com", "11999998888", "TI"))
        }.exceptionOrNull()
        assertTrue(exConflito is ConflitoException)

        // 401 no login -> CredenciaisInvalidasException
        server.enqueue(MockResponse().setResponseCode(401).setBody("""{"status":401,"mensagem":"Login ou senha inválidos"}"""))
        val exCred = runCatching { authService.autenticar("admin", "errada") }.exceptionOrNull()
        assertTrue(exCred is CredenciaisInvalidasException)

        // 429 no login -> BloqueioTentativasException
        server.enqueue(MockResponse().setResponseCode(429).setBody("""{"status":429,"mensagem":"Muitas tentativas"}"""))
        val exBloq = runCatching { authService.autenticar("admin", "errada") }.exceptionOrNull()
        assertTrue(exBloq is BloqueioTentativasException)
        assertNotNull(exBloq?.message)
    }
}
