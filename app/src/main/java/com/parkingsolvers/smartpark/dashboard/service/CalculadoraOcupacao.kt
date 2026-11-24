package com.parkingsolvers.smartpark.dashboard.service

import com.parkingsolvers.smartpark.dashboard.dto.response.DashboardOcupacaoResponse
import com.parkingsolvers.smartpark.dashboard.dto.response.OcupacaoPorTurnoItem
import com.parkingsolvers.smartpark.dashboard.dto.response.UsoVagaItem
import com.parkingsolvers.smartpark.dashboard.model.ConfiguracaoEstacionamento
import com.parkingsolvers.smartpark.movimentacao.model.RegistroMovimentacao
import com.parkingsolvers.smartpark.movimentacao.model.TipoVaga
import com.parkingsolvers.smartpark.movimentacao.model.Turno
import com.parkingsolvers.smartpark.servidor.model.Servidor
import com.parkingsolvers.smartpark.veiculo.model.TipoCadastroVeiculo
import com.parkingsolvers.smartpark.veiculo.model.Veiculo
import com.parkingsolvers.smartpark.visitante.model.Visitante
import java.util.Calendar

/**
 * Calculadora pura das métricas do painel de ocupação, consolidando
 * movimentações de servidores, visitantes externos e vagas de carregamento elétrico.
 */
class CalculadoraOcupacao(
    private val capacidadeTotal: Int = ConfiguracaoEstacionamento.CAPACIDADE_TOTAL_PADRAO,
    private val capacidadeExclusivas: Int = ConfiguracaoEstacionamento.CAPACIDADE_EXCLUSIVAS_PADRAO,
    private val capacidadeCarregamentoEletrico: Int = ConfiguracaoEstacionamento.CAPACIDADE_ELETRICO_PADRAO
) {

    fun calcular(
        servidores: List<Servidor>,
        veiculos: List<Veiculo>,
        movimentacoes: List<RegistroMovimentacao>,
        agoraMillis: Long = System.currentTimeMillis(),
        inicioPeriodoMillis: Long? = null,
        visitantes: List<Visitante> = emptyList(),
        configuracao: ConfiguracaoEstacionamento = ConfiguracaoEstacionamento(
            capacidadeTotal = capacidadeTotal,
            capacidadeExclusivas = capacidadeExclusivas,
            capacidadeCarregamentoEletrico = capacidadeCarregamentoEletrico
        )
    ): DashboardOcupacaoResponse {
        val capTotal = configuracao.capacidadeTotal
        val capExclusivas = configuracao.capacidadeExclusivas
        val capEletricas = configuracao.capacidadeCarregamentoEletrico

        val movEmAberto = movimentacoes.filter { it.emAberto }
        val visEmAberto = visitantes.filter { it.noCampus }

        val estacionadosAgora = movEmAberto.size + visEmAberto.size
        val visitantesAgora = visEmAberto.size
        val exclusivasAgora = movEmAberto.count { it.tipoVaga.ehExclusiva } +
            visEmAberto.count { it.tipoVaga.ehExclusiva }
        val eletricasAgora = movEmAberto.count { it.tipoVaga.ehCarregamentoEletrico } +
            visEmAberto.count { it.tipoVaga.ehCarregamentoEletrico }

        val movDoPeriodo = if (inicioPeriodoMillis == null) {
            movimentacoes
        } else {
            movimentacoes.filter { it.dataHoraEntradaMillis >= inicioPeriodoMillis }
        }
        val visDoPeriodo = if (inicioPeriodoMillis == null) {
            visitantes
        } else {
            visitantes.filter { it.dataHoraEntradaMillis >= inicioPeriodoMillis }
        }

        val todosInstantesEntrada = movDoPeriodo.map { it.dataHoraEntradaMillis } +
            visDoPeriodo.map { it.dataHoraEntradaMillis }
        val diasComMovimento = contarDiasDistintos(todosInstantesEntrada).coerceAtLeast(1)

        val ocupacaoPorTurno = Turno.entries.map { turno ->
            val entradasTurno = movDoPeriodo.count { it.turno == turno } +
                visDoPeriodo.count { it.turno == turno }
            val estacionadosTurno = movEmAberto.count { it.turno == turno } +
                visEmAberto.count { it.turno == turno }
            val mediaDiaria = entradasTurno.toFloat() / diasComMovimento
            OcupacaoPorTurnoItem(
                turno = turno,
                totalEntradas = entradasTurno,
                atualmenteEstacionados = estacionadosTurno,
                mediaDiariaEntradas = mediaDiaria,
                percentualLotacao = calcularPercentual(mediaDiaria, capTotal)
            )
        }

        val usoPorTipoVaga = TipoVaga.entries.map { tipo ->
            UsoVagaItem(
                tipoVaga = tipo,
                ocupadasAgora = movEmAberto.count { it.tipoVaga == tipo } +
                    visEmAberto.count { it.tipoVaga == tipo },
                totalRegistros = movDoPeriodo.count { it.tipoVaga == tipo } +
                    visDoPeriodo.count { it.tipoVaga == tipo }
            )
        }

        val temporariosVigentes = veiculos.count {
            it.ativo && it.tipoCadastro == TipoCadastroVeiculo.TEMPORARIO && it.estaDentroDaValidade(agoraMillis)
        }

        return DashboardOcupacaoResponse(
            capacidadeTotal = capTotal,
            capacidadeExclusivas = capExclusivas,
            capacidadeCarregamentoEletrico = capEletricas,
            veiculosNoCampusAgora = estacionadosAgora,
            visitantesNoCampusAgora = visitantesAgora,
            vagasExclusivasOcupadasAgora = exclusivasAgora,
            vagasEletricasOcupadasAgora = eletricasAgora,
            percentualOcupacaoAtual = calcularPercentual(estacionadosAgora.toFloat(), capTotal),
            percentualExclusivasAtual = calcularPercentual(exclusivasAgora.toFloat(), capExclusivas),
            percentualEletricasAtual = calcularPercentual(eletricasAgora.toFloat(), capEletricas),
            totalServidoresAtivos = servidores.count { it.ativo },
            totalVeiculosAtivos = veiculos.count { it.ativo },
            totalTemporariosVigentes = temporariosVigentes,
            totalEntradasPeriodo = movDoPeriodo.size + visDoPeriodo.size,
            totalVisitantesPeriodo = visDoPeriodo.size,
            diasComMovimento = diasComMovimento,
            ocupacaoPorTurno = ocupacaoPorTurno,
            usoPorTipoVaga = usoPorTipoVaga
        )
    }

    private fun contarDiasDistintos(instantesMillis: List<Long>): Int {
        val calendario = Calendar.getInstance()
        return instantesMillis.map { millis ->
            calendario.timeInMillis = millis
            calendario.get(Calendar.YEAR) * 1000 + calendario.get(Calendar.DAY_OF_YEAR)
        }.toSet().size
    }

    private fun calcularPercentual(atual: Float, capacidade: Int): Float {
        if (capacidade <= 0) return 0f
        return (atual / capacidade.toFloat()).coerceIn(0f, 1f)
    }

    companion object {
        const val CAPACIDADE_PADRAO_CAMPUS = ConfiguracaoEstacionamento.CAPACIDADE_TOTAL_PADRAO
        const val CAPACIDADE_PADRAO_EXCLUSIVAS = ConfiguracaoEstacionamento.CAPACIDADE_EXCLUSIVAS_PADRAO
        const val CAPACIDADE_PADRAO_ELETRICAS = ConfiguracaoEstacionamento.CAPACIDADE_ELETRICO_PADRAO
    }
}
