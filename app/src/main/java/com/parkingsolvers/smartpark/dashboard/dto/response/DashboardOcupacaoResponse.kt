package com.parkingsolvers.smartpark.dashboard.dto.response

import com.parkingsolvers.smartpark.movimentacao.model.TipoVaga
import com.parkingsolvers.smartpark.movimentacao.model.Turno

data class OcupacaoPorTurnoItem(
    val turno: Turno,
    val totalEntradas: Int,
    val atualmenteEstacionados: Int,
    val mediaDiariaEntradas: Float,
    val percentualLotacao: Float
)

data class UsoVagaItem(
    val tipoVaga: TipoVaga,
    val ocupadasAgora: Int,
    val totalRegistros: Int
)

data class DashboardOcupacaoResponse(
    val capacidadeTotal: Int,
    val capacidadeExclusivas: Int,
    val capacidadeCarregamentoEletrico: Int,
    val veiculosNoCampusAgora: Int,
    val visitantesNoCampusAgora: Int,
    val vagasExclusivasOcupadasAgora: Int,
    val vagasEletricasOcupadasAgora: Int,
    val percentualOcupacaoAtual: Float,
    val percentualExclusivasAtual: Float,
    val percentualEletricasAtual: Float,
    val totalServidoresAtivos: Int,
    val totalVeiculosAtivos: Int,
    val totalTemporariosVigentes: Int,
    val totalEntradasPeriodo: Int,
    val totalVisitantesPeriodo: Int,
    val diasComMovimento: Int,
    val ocupacaoPorTurno: List<OcupacaoPorTurnoItem>,
    val usoPorTipoVaga: List<UsoVagaItem>
) {
    val capacidadeComuns: Int
        get() = (capacidadeTotal - capacidadeExclusivas - capacidadeCarregamentoEletrico).coerceAtLeast(0)

    val vagasComunsOcupadasAgora: Int
        get() = (veiculosNoCampusAgora - vagasExclusivasOcupadasAgora - vagasEletricasOcupadasAgora).coerceAtLeast(0)

    val vagasLivresAgora: Int
        get() = (capacidadeTotal - veiculosNoCampusAgora).coerceAtLeast(0)

    val vagasComunsLivresAgora: Int
        get() = (capacidadeComuns - vagasComunsOcupadasAgora).coerceAtLeast(0)

    val vagasExclusivasLivresAgora: Int
        get() = (capacidadeExclusivas - vagasExclusivasOcupadasAgora).coerceAtLeast(0)

    val vagasEletricasLivresAgora: Int
        get() = (capacidadeCarregamentoEletrico - vagasEletricasOcupadasAgora).coerceAtLeast(0)

    val percentualComunsAtual: Float
        get() = if (capacidadeComuns <= 0) 0f else (vagasComunsOcupadasAgora.toFloat() / capacidadeComuns.toFloat()).coerceIn(0f, 1f)
}
