package com.parkingsolvers.smartpark

import android.app.Activity
import android.app.Application
import android.os.Bundle
import com.parkingsolvers.smartpark.auth.model.PerfilUsuario
import com.parkingsolvers.smartpark.auth.service.AuthService
import com.parkingsolvers.smartpark.dashboard.service.ConfiguracaoEstacionamentoService
import com.parkingsolvers.smartpark.dashboard.service.DashboardService
import com.parkingsolvers.smartpark.movimentacao.service.MovimentacaoService
import com.parkingsolvers.smartpark.notificacao.MonitoramentoPortariaService
import com.parkingsolvers.smartpark.notificacao.NotificacaoPortariaHelper
import com.parkingsolvers.smartpark.notificacao.PreferenciasMonitoramento
import com.parkingsolvers.smartpark.rede.ApiClient
import com.parkingsolvers.smartpark.rede.ClienteTempoReal
import com.parkingsolvers.smartpark.rede.SharedPreferencesSessaoStore
import com.parkingsolvers.smartpark.rede.SincronizadorDados
import com.parkingsolvers.smartpark.servidor.service.ServidorService
import com.parkingsolvers.smartpark.usuario.service.UsuarioService
import com.parkingsolvers.smartpark.veiculo.service.VeiculoService
import com.parkingsolvers.smartpark.visitante.service.VisitanteService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicInteger

/**
 * Container de dependências do aplicativo Parking Solvers (SmartPark),
 * conectado à API REST Spring Boot (sem banco local Room).
 */
class AppContainer(private val application: Application) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val atividadesIniciadas = AtomicInteger(0)

    val sessaoStore = SharedPreferencesSessaoStore(application)
    val sincronizador = SincronizadorDados()
    val preferenciasMonitoramento = PreferenciasMonitoramento(application)

    private val _emPrimeiroPlano = MutableStateFlow(false)
    val emPrimeiroPlano: StateFlow<Boolean> = _emPrimeiroPlano.asStateFlow()

    private val _abrirPortariaPendente = MutableStateFlow(false)
    val abrirPortariaPendente: StateFlow<Boolean> = _abrirPortariaPendente.asStateFlow()

    private lateinit var _authService: AuthService

    private val onSessaoExpiradaCallback: () -> Unit = {
        if (::_authService.isInitialized) {
            _authService.notificarSessaoExpirada()
        }
    }

    val api = ApiClient.criar(
        baseUrl = BuildConfig.API_BASE_URL,
        sessaoStore = sessaoStore,
        debug = BuildConfig.DEBUG,
        onSessaoExpirada = onSessaoExpiradaCallback
    )

    private val sseClient = ApiClient.criarClienteSse(
        baseUrl = BuildConfig.API_BASE_URL,
        sessaoStore = sessaoStore,
        onSessaoExpirada = onSessaoExpiradaCallback
    )

    val clienteTempoReal = ClienteTempoReal(
        baseUrl = BuildConfig.API_BASE_URL,
        sseClient = sseClient,
        sessaoStore = sessaoStore,
        sincronizador = sincronizador
    )

    val authService: AuthService = AuthService(
        api = api,
        sessaoStore = sessaoStore,
        sincronizador = sincronizador,
        onSessaoAlterada = { atualizarEstadoTempoReal() }
    ).also { _authService = it }

    val usuarioService = UsuarioService(api, sincronizador)
    val servidorService = ServidorService(api, sincronizador)
    val veiculoService = VeiculoService(api, sincronizador)
    val movimentacaoService = MovimentacaoService(
        api = api,
        sincronizador = sincronizador,
        onAcaoLocal = { clienteTempoReal.marcarAcaoLocal() }
    )
    val visitanteService = VisitanteService(
        api = api,
        sincronizador = sincronizador,
        onAcaoLocal = { clienteTempoReal.marcarAcaoLocal() }
    )
    val configuracaoEstacionamentoService = ConfiguracaoEstacionamentoService(api, sincronizador)
    val dashboardService = DashboardService(api, sincronizador)

    init {
        NotificacaoPortariaHelper.garantirCanais(application)
        observarEventosParaNotificacao()
    }

    fun aoIniciarAtividade() {
        if (atividadesIniciadas.incrementAndGet() == 1) {
            _emPrimeiroPlano.value = true
            atualizarEstadoTempoReal()
        }
    }

    fun aoPararAtividade() {
        val restantes = atividadesIniciadas.updateAndGet { atual -> if (atual > 0) atual - 1 else 0 }
        if (restantes == 0) {
            _emPrimeiroPlano.value = false
            atualizarEstadoTempoReal()
        }
    }

    fun solicitarAberturaPortaria() {
        _abrirPortariaPendente.value = true
    }

    fun consumirAberturaPortaria() {
        _abrirPortariaPendente.value = false
    }

    fun atualizarEstadoTempoReal() {
        val sessao = authService.sessaoAtual.value
        if (sessao == null) {
            clienteTempoReal.encerrarTudo()
            MonitoramentoPortariaService.parar(application)
            return
        }

        val noPrimeiroPlano = _emPrimeiroPlano.value
        if (noPrimeiroPlano) {
            clienteTempoReal.solicitarConexao(ORIGEM_FOREGROUND)
            sincronizador.notificarMudanca()
        } else {
            clienteTempoReal.liberarConexao(ORIGEM_FOREGROUND)
        }

        val perfilMonitoraFora = sessao.perfil == PerfilUsuario.PORTARIA || sessao.perfil == PerfilUsuario.ADMIN
        val deveMonitorarFora = perfilMonitoraFora &&
            preferenciasMonitoramento.alertasForaDoAppAtivo.value &&
            NotificacaoPortariaHelper.temPermissaoNotificacao(application)

        if (deveMonitorarFora) {
            if (noPrimeiroPlano) {
                MonitoramentoPortariaService.iniciar(application)
            }
        } else {
            MonitoramentoPortariaService.parar(application)
        }
    }

    private fun observarEventosParaNotificacao() {
        scope.launch {
            clienteTempoReal.eventosRecebidos.collect { evento ->
                if (!evento.ehMovimentacaoVeiculo) return@collect
                if (_emPrimeiroPlano.value) return@collect
                val sessao = authService.sessaoAtual.value ?: return@collect
                val perfilPermitido = sessao.perfil == PerfilUsuario.PORTARIA || sessao.perfil == PerfilUsuario.ADMIN
                if (!perfilPermitido || !preferenciasMonitoramento.alertasForaDoAppAtivo.value) return@collect
                NotificacaoPortariaHelper.exibirAlertaMovimentacao(application, evento)
            }
        }
    }

    companion object {
        private const val ORIGEM_FOREGROUND = "foreground"
    }
}

class SmartParkApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) = Unit
            override fun onActivityStarted(activity: Activity) = container.aoIniciarAtividade()
            override fun onActivityResumed(activity: Activity) = Unit
            override fun onActivityPaused(activity: Activity) = Unit
            override fun onActivityStopped(activity: Activity) = container.aoPararAtividade()
            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit
            override fun onActivityDestroyed(activity: Activity) = Unit
        })
    }
}
