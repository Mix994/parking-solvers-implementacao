package com.parkingsolvers.smartpark.rede

import com.google.gson.Gson
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.BufferedSource
import java.util.concurrent.atomic.AtomicLong
import kotlin.coroutines.coroutineContext
import kotlin.math.min

/**
 * Cliente SSE (Server-Sent Events) conectado a GET /eventos/stream.
 * Mantém uma única conexão compartilhada entre a UI em primeiro plano e o serviço em segundo plano,
 * dispara [SincronizadorDados.notificarMudanca] a cada atualização recebida e expõe os eventos
 * para banners in-app e notificações do sistema.
 */
class ClienteTempoReal(
    baseUrl: String,
    private val sseClient: OkHttpClient,
    private val sessaoStore: SessaoStore,
    private val sincronizador: SincronizadorDados,
    private val gson: Gson = Gson(),
    private val atrasoInicialReconexaoMs: Long = 2_000L,
    private val atrasoMaximoReconexaoMs: Long = 30_000L,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val relogioMs: () -> Long = { System.currentTimeMillis() }
) {
    private val urlStream = "${ApiClient.normalizarBaseUrl(baseUrl)}eventos/stream"
    private val scope = CoroutineScope(SupervisorJob() + ioDispatcher)

    private val lock = Any()
    private val origensAtivas = mutableSetOf<String>()
    private var jobConexao: Job? = null
    @Volatile
    private var chamadaAtiva: Call? = null

    private val _conectado = MutableStateFlow(false)
    val conectado: StateFlow<Boolean> = _conectado.asStateFlow()

    private val _eventosRecebidos = MutableSharedFlow<EventoTempoReal>(
        replay = 0,
        extraBufferCapacity = 32
    )
    val eventosRecebidos: SharedFlow<EventoTempoReal> = _eventosRecebidos.asSharedFlow()

    private val ultimoTimestampAcaoLocalMs = AtomicLong(0L)

    fun marcarAcaoLocal() {
        ultimoTimestampAcaoLocalMs.set(relogioMs())
    }

    fun ehAcaoLocalRecente(janelaMs: Long = 2_500L): Boolean {
        val ultimo = ultimoTimestampAcaoLocalMs.get()
        if (ultimo <= 0L) return false
        return (relogioMs() - ultimo) in 0L..janelaMs
    }

    fun solicitarConexao(origem: String) {
        if (sessaoStore.carregar() == null) return
        synchronized(lock) {
            origensAtivas.add(origem)
            if (jobConexao?.isActive != true) {
                jobConexao = scope.launch { executarLoop() }
            }
        }
    }

    fun liberarConexao(origem: String) {
        val encerrar = synchronized(lock) {
            origensAtivas.remove(origem)
            origensAtivas.isEmpty()
        }
        if (encerrar) {
            pararConexaoAtual()
        }
    }

    fun encerrarTudo() {
        synchronized(lock) {
            origensAtivas.clear()
        }
        pararConexaoAtual()
    }

    private fun pararConexaoAtual() {
        synchronized(lock) {
            jobConexao?.cancel()
            jobConexao = null
        }
        chamadaAtiva?.cancel()
        chamadaAtiva = null
        _conectado.value = false
    }

    private fun temOrigensAtivas(): Boolean = synchronized(lock) { origensAtivas.isNotEmpty() }

    private suspend fun executarLoop() {
        var atrasoMs = atrasoInicialReconexaoMs
        var jaConectouAntes = false

        while (coroutineContext.isActive && temOrigensAtivas()) {
            if (sessaoStore.carregar() == null) {
                _conectado.value = false
                break
            }

            var encerrarDefinitivo = false
            val inicioConexao = relogioMs()

            try {
                val request = Request.Builder()
                    .url(urlStream)
                    .header("Accept", "text/event-stream")
                    .header("Cache-Control", "no-cache")
                    .get()
                    .build()

                val call = sseClient.newCall(request)
                chamadaAtiva = call

                call.execute().use { response ->
                    when {
                        response.code == 401 || response.code == 403 -> {
                            encerrarDefinitivo = true
                        }
                        !response.isSuccessful -> {
                            // Falha transitória (5xx/gateway); tenta reconectar após backoff.
                        }
                        else -> {
                            val body = response.body
                            if (body != null) {
                                val eraReconexao = jaConectouAntes
                                val reconectou = processarStream(
                                    source = body.source(),
                                    aoConectar = {
                                        _conectado.value = true
                                        atrasoMs = atrasoInicialReconexaoMs
                                        if (eraReconexao) {
                                            sincronizador.notificarMudanca()
                                        }
                                        jaConectouAntes = true
                                    }
                                )
                                if (reconectou) {
                                    atrasoMs = atrasoInicialReconexaoMs
                                }
                            }
                        }
                    }
                }
            } catch (_: Exception) {
                // Erro de rede ou cancelamento; tratado abaixo.
            } finally {
                chamadaAtiva = null
                _conectado.value = false
            }

            if (encerrarDefinitivo || !coroutineContext.isActive || !temOrigensAtivas()) {
                break
            }

            // Se a conexão ficou aberta por pelo menos 15s, reinicia o backoff antes de reconectar.
            if (relogioMs() - inicioConexao >= 15_000L) {
                atrasoMs = atrasoInicialReconexaoMs
            }

            delay(atrasoMs)
            atrasoMs = min(atrasoMs * 2L, atrasoMaximoReconexaoMs)
        }
    }

    private suspend fun processarStream(
        source: BufferedSource,
        aoConectar: () -> Unit
    ): Boolean {
        var nomeEvento: String? = null
        var ultimoIdStream = 0L
        var conectouNesteStream = false
        val dados = StringBuilder()

        while (coroutineContext.isActive && temOrigensAtivas()) {
            val linha = source.readUtf8Line() ?: break

            when {
                linha.isEmpty() -> {
                    if (dados.isNotEmpty() || nomeEvento != null) {
                        val payload = dados.toString().trimEnd('\n')
                        val evento = nomeEvento
                        dados.setLength(0)
                        nomeEvento = null

                        when (evento) {
                            "conectado" -> {
                                ultimoIdStream = 0L
                                conectouNesteStream = true
                                aoConectar()
                            }
                            "atualizacao" -> {
                                if (!conectouNesteStream) {
                                    conectouNesteStream = true
                                    aoConectar()
                                }
                                if (payload.isNotBlank()) {
                                    val parsed = runCatching {
                                        gson.fromJson(payload, EventoTempoReal::class.java)
                                    }.getOrNull()
                                    if (parsed != null && (parsed.id <= 0L || parsed.id != ultimoIdStream)) {
                                        if (parsed.id > 0L) {
                                            ultimoIdStream = parsed.id
                                        }
                                        sincronizador.notificarMudanca()
                                        _eventosRecebidos.tryEmit(parsed)
                                    }
                                }
                            }
                        }
                    }
                }
                linha.startsWith(":") -> {
                    // Heartbeat (:keepalive): confirma stream ativo sem disparar atualização.
                    if (!conectouNesteStream) {
                        conectouNesteStream = true
                        aoConectar()
                    }
                }
                linha.startsWith("event:") -> {
                    nomeEvento = linha.substringAfter("event:").trim()
                }
                linha.startsWith("data:") -> {
                    val trecho = linha.substringAfter("data:").let {
                        if (it.startsWith(" ")) it.substring(1) else it
                    }
                    if (dados.isNotEmpty()) dados.append('\n')
                    dados.append(trecho)
                }
            }
        }
        return conectouNesteStream
    }
}
