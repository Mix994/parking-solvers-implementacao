package com.parkingsolvers.smartpark

import android.app.Application
import com.parkingsolvers.smartpark.auth.service.AuthService
import com.parkingsolvers.smartpark.auth.service.LoginAttemptService
import com.parkingsolvers.smartpark.dashboard.service.CalculadoraOcupacao
import com.parkingsolvers.smartpark.dashboard.service.DashboardService
import com.parkingsolvers.smartpark.database.SmartParkDatabase
import com.parkingsolvers.smartpark.dev.DadosIniciais
import com.parkingsolvers.smartpark.movimentacao.repository.MovimentacaoRepository
import com.parkingsolvers.smartpark.movimentacao.service.MovimentacaoService
import com.parkingsolvers.smartpark.servidor.repository.ServidorRepository
import com.parkingsolvers.smartpark.servidor.service.ServidorService
import com.parkingsolvers.smartpark.usuario.repository.UsuarioRepository
import com.parkingsolvers.smartpark.usuario.service.UsuarioService
import com.parkingsolvers.smartpark.veiculo.repository.VeiculoRepository
import com.parkingsolvers.smartpark.veiculo.service.VeiculoService
import com.parkingsolvers.smartpark.visitante.repository.VisitanteRepository
import com.parkingsolvers.smartpark.visitante.service.VisitanteService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Container de dependências do aplicativo Parking Solvers (SmartPark),
 * estruturado por domínio seguindo o repositório de referência CarToon.
 */
class AppContainer(application: Application) {

    private val database = SmartParkDatabase.getInstance(application)

    val usuarioRepository = UsuarioRepository(database.usuarioDao())
    val servidorRepository = ServidorRepository(database.servidorDao())
    val veiculoRepository = VeiculoRepository(database.veiculoDao())
    val movimentacaoRepository = MovimentacaoRepository(database.movimentacaoDao())
    val visitanteRepository = VisitanteRepository(database.visitanteDao())

    val loginAttemptService = LoginAttemptService()
    val authService = AuthService(
        usuarioRepository = usuarioRepository,
        servidorRepository = servidorRepository,
        loginAttemptService = loginAttemptService
    )
    val usuarioService = UsuarioService(
        usuarioRepository = usuarioRepository,
        servidorRepository = servidorRepository
    )
    val servidorService = ServidorService(servidorRepository)
    val veiculoService = VeiculoService(
        veiculoRepository = veiculoRepository,
        servidorRepository = servidorRepository,
        servidorService = servidorService
    )
    val movimentacaoService = MovimentacaoService(
        movimentacaoRepository = movimentacaoRepository,
        veiculoRepository = veiculoRepository,
        servidorRepository = servidorRepository,
        veiculoService = veiculoService
    )
    val visitanteService = VisitanteService(
        visitanteRepository = visitanteRepository
    )
    val dashboardService = DashboardService(
        servidorRepository = servidorRepository,
        veiculoRepository = veiculoRepository,
        movimentacaoRepository = movimentacaoRepository,
        visitanteRepository = visitanteRepository,
        calculadoraOcupacao = CalculadoraOcupacao()
    )
    val dadosIniciais = DadosIniciais(
        usuarioRepository = usuarioRepository,
        servidorRepository = servidorRepository,
        veiculoRepository = veiculoRepository,
        movimentacaoRepository = movimentacaoRepository,
        visitanteRepository = visitanteRepository
    )
}

class SmartParkApplication : Application() {

    lateinit var container: AppContainer
        private set

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        applicationScope.launch {
            container.dadosIniciais.popularSeVazio()
        }
    }
}
