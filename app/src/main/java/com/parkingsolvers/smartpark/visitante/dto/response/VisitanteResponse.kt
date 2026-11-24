package com.parkingsolvers.smartpark.visitante.dto.response

import com.parkingsolvers.smartpark.movimentacao.model.TipoVaga
import com.parkingsolvers.smartpark.movimentacao.model.Turno

data class VisitanteResponse(
    val id: Long,
    val nome: String,
    val documento: String,
    val instituicaoOrigem: String,
    val telefone: String,
    val placa: String,
    val modeloVeiculo: String,
    val corVeiculo: String,
    val motivoVisita: String,
    val setorDestino: String,
    val tipoVaga: TipoVaga,
    val turno: Turno,
    val registradoPor: String,
    val dataHoraEntradaMillis: Long,
    val dataHoraSaidaMillis: Long?,
    val observacao: String
) {
    val noCampus: Boolean
        get() = dataHoraSaidaMillis == null
}
