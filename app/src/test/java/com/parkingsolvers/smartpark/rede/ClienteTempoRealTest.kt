package com.parkingsolvers.smartpark.rede

import com.parkingsolvers.smartpark.auth.model.PerfilUsuario
import com.parkingsolvers.smartpark.auth.model.UsuarioSessao
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ClienteTempoRealTest {

    private lateinit var server: MockWebServer
    private lateinit var store: MemoriaSessaoStore
    private lateinit var sincronizador: SincronizadorDados

    @Before
    fun setup() {
        server = MockWebServer()
        server.start()
        store = MemoriaSessaoStore()
        store.salvar(
            CredenciaisSessao(
                accessToken = "tok-portaria",
                refreshToken = "tok-refresh",
                sessao = UsuarioSessao(1L, "Porteiro", "portaria", PerfilUsuario.PORTARIA)
            )
        )
        sincronizador = SincronizadorDados()
    }

    @After
    fun teardown() {
        server.shutdown()
    }

    @Test
    fun `processa stream SSE, deduplica ids, notifica sincronizador e reconecta`() = runBlocking {
        val stream1 = buildString {
            append(":keepalive\n\n")
            append("event: conectado\n")
            append("data: {\"status\":\"ok\"}\n\n")
            append("id: 1\n")
            append("event: atualizacao\n")
            append("data: {\"id\":1,\"tipo\":\"MOVIMENTACAO_ENTRADA\",\"placa\":\"ABC1D23\",\"responsavel\":\"Ana\",\"tipoVagaLabel\":\"Exclusiva Idoso\",\"simulacaoServidor\":true}\n\n")
            // Mesmo id duplicado dentro do mesmo stream deve ser ignorado
            append("id: 1\n")
            append("event: atualizacao\n")
            append("data: {\"id\":1,\"tipo\":\"MOVIMENTACAO_ENTRADA\",\"placa\":\"ABC1D23\"}\n\n")
            append("id: 2\n")
            append("event: atualizacao\n")
            append("data: {\"id\":2,\"tipo\":\"MOVIMENTACAO_SAIDA\",\"placa\":\"ABC1D23\",\"responsavel\":\"Ana\"}\n\n")
        }
        val stream2 = buildString {
            append("event: conectado\n")
            append("data: {\"status\":\"ok\"}\n\n")
            append("id: 1\n")
            append("event: atualizacao\n")
            append("data: {\"id\":1,\"tipo\":\"VISITANTE_ENTRADA\",\"placa\":\"XYZ9876\",\"responsavel\":\"Carlos\"}\n\n")
        }

        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "text/event-stream")
                .setBody(stream1)
        )
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "text/event-stream")
                .setBody(stream2)
        )

        val sseClient = ApiClient.criarClienteSse(
            baseUrl = server.url("/").toString(),
            sessaoStore = store,
            onSessaoExpirada = {}
        )
        val cliente = ClienteTempoReal(
            baseUrl = server.url("/").toString(),
            sseClient = sseClient,
            sessaoStore = store,
            sincronizador = sincronizador,
            atrasoInicialReconexaoMs = 50L
        )

        val coletor = async {
            withTimeout(5_000L) {
                cliente.eventosRecebidos.take(3).toList()
            }
        }

        // Garante que o coletor iniciou antes de abrir a conexão SSE.
        kotlinx.coroutines.yield()
        cliente.solicitarConexao("foreground")

        val eventos = coletor.await()
        cliente.encerrarTudo()

        assertEquals(3, eventos.size)
        assertEquals("MOVIMENTACAO_ENTRADA", eventos[0].tipo)
        assertEquals("ABC1D23", eventos[0].placa)
        assertEquals("Exclusiva Idoso", eventos[0].tipoVagaLabel)
        assertTrue(eventos[0].simulacaoServidor)
        assertEquals("MOVIMENTACAO_SAIDA", eventos[1].tipo)
        assertEquals("VISITANTE_ENTRADA", eventos[2].tipo)

        // 3 eventos atualizacao + 1 reconexao (conectado no stream2) = 4 incrementos
        assertEquals(4L, sincronizador.versao.value)

        val req1 = server.takeRequest()
        assertEquals("/eventos/stream", req1.path)
        assertEquals("Bearer tok-portaria", req1.getHeader("Authorization"))
        assertEquals("text/event-stream", req1.getHeader("Accept"))
    }

    @Test
    fun `ehAcaoLocalRecente respeita janela de tempo`() {
        var agoraMs = 10_000L
        val sseClient = ApiClient.criarClienteSse(
            baseUrl = server.url("/").toString(),
            sessaoStore = store,
            onSessaoExpirada = {}
        )
        val cliente = ClienteTempoReal(
            baseUrl = server.url("/").toString(),
            sseClient = sseClient,
            sessaoStore = store,
            sincronizador = sincronizador,
            relogioMs = { agoraMs }
        )

        assertFalse(cliente.ehAcaoLocalRecente())
        cliente.marcarAcaoLocal()
        assertTrue(cliente.ehAcaoLocalRecente(janelaMs = 2_500L))

        agoraMs += 2_000L
        assertTrue(cliente.ehAcaoLocalRecente(janelaMs = 2_500L))

        agoraMs += 1_000L
        assertFalse(cliente.ehAcaoLocalRecente(janelaMs = 2_500L))
    }
}
