package com.parkingsolvers.smartpark

import android.app.Application
import com.parkingsolvers.smartpark.auth.service.AuthService
import com.parkingsolvers.smartpark.dashboard.service.ConfiguracaoEstacionamentoService
import com.parkingsolvers.smartpark.dashboard.service.DashboardService
import com.parkingsolvers.smartpark.movimentacao.service.MovimentacaoService
import com.parkingsolvers.smartpark.rede.ApiClient
import com.parkingsolvers.smartpark.rede.SharedPreferencesSessaoStore
import com.parkingsolvers.smartpark.rede.SincronizadorDados
import com.parkingsolvers.smartpark.servidor.service.ServidorService
import com.parkingsolvers.smartpark.usuario.service.UsuarioService
import com.parkingsolvers.smartpark.veiculo.service.VeiculoService
import com.parkingsolvers.smartpark.visitante.service.VisitanteService

/**
 * Container de dependências do aplicativo Parking Solvers (SmartPark),
 * conectado à API REST Spring Boot (sem banco local Room).
 */
class AppContainer(application: Application) {

    val sessaoStore = SharedPreferencesSessaoStore(application)
    val sincronizador = SincronizadorDados()

    private lateinit var _authService: AuthService

    val api = ApiClient.criar(
        baseUrl = BuildConfig.API_BASE_URL,
        sessaoStore = sessaoStore,
        debug = BuildConfig.DEBUG,
        onSessaoExpirada = {
            if (::_authService.isInitialized) {
                _authService.notificarSessaoExpirada()
            }
        }
    )

    val authService: AuthService = AuthService(
        api = api,
        sessaoStore = sessaoStore,
        sincronizador = sincronizador
    ).also { _authService = it }

    val usuarioService = UsuarioService(api, sincronizador)
    val servidorService = ServidorService(api, sincronizador)
    val veiculoService = VeiculoService(api, sincronizador)
    val movimentacaoService = MovimentacaoService(api, sincronizador)
    val visitanteService = VisitanteService(api, sincronizador)
    val configuracaoEstacionamentoService = ConfiguracaoEstacionamentoService(api, sincronizador)
    val dashboardService = DashboardService(api, sincronizador)
}

class SmartParkApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
