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
    val veiculosNoCampusAgora: Int,
    val visitantesNoCampusAgora: Int,
    val vagasExclusivasOcupadasAgora: Int,
    val percentualOcupacaoAtual: Float,
    val percentualExclusivasAtual: Float,
    val totalServidoresAtivos: Int,
    val totalVeiculosAtivos: Int,
    val totalTemporariosVigentes: Int,
    val totalEntradasPeriodo: Int,
    val totalVisitantesPeriodo: Int,
    val diasComMovimento: Int,
    val ocupacaoPorTurno: List<OcupacaoPorTurnoItem>,
    val usoPorTipoVaga: List<UsoVagaItem>
)
