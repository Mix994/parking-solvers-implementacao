package com.parkingsolvers.smartpark.visitante.dto.request

import com.parkingsolvers.smartpark.movimentacao.model.TipoVaga

data class VisitanteRequest(
    val nome: String,
    val documento: String,
    val instituicaoOrigem: String,
    val telefone: String,
    val placa: String,
    val modeloVeiculo: String,
    val corVeiculo: String,
    val motivoVisita: String,
    val setorDestino: String,
    val tipoVaga: TipoVaga = TipoVaga.COMUM,
    val observacao: String = ""
)
