package com.parkingsolvers.smartpark.dashboard.service

import com.parkingsolvers.smartpark.dashboard.dto.response.DashboardOcupacaoResponse
import com.parkingsolvers.smartpark.dashboard.model.PeriodoDashboard
import com.parkingsolvers.smartpark.movimentacao.repository.MovimentacaoRepository
import com.parkingsolvers.smartpark.servidor.repository.ServidorRepository
import com.parkingsolvers.smartpark.veiculo.repository.VeiculoRepository
import com.parkingsolvers.smartpark.visitante.repository.VisitanteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class DashboardService(
    private val servidorRepository: ServidorRepository,
    private val veiculoRepository: VeiculoRepository,
    private val movimentacaoRepository: MovimentacaoRepository,
    private val visitanteRepository: VisitanteRepository,
    private val calculadoraOcupacao: CalculadoraOcupacao = CalculadoraOcupacao()
) {

    fun observarDashboard(periodo: Flow<PeriodoDashboard>): Flow<DashboardOcupacaoResponse> = combine(
        periodo,
        servidorRepository.observarTodos(),
        veiculoRepository.observarTodos(),
        movimentacaoRepository.observarTodas(),
        visitanteRepository.observarTodos()
    ) { periodoAtual, servidores, veiculos, movimentacoes, visitantes ->
        val agora = System.currentTimeMillis()
        calculadoraOcupacao.calcular(
            servidores = servidores,
            veiculos = veiculos,
            movimentacoes = movimentacoes,
            agoraMillis = agora,
            inicioPeriodoMillis = periodoAtual.inicioMillis(agora),
            visitantes = visitantes
        )
    }
}
