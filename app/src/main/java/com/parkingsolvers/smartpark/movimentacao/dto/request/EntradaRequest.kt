package com.parkingsolvers.smartpark.movimentacao.dto.request

import com.parkingsolvers.smartpark.movimentacao.model.TipoVaga

data class EntradaRequest(
    val placa: String,
    val tipoVaga: TipoVaga = TipoVaga.COMUM,
    val observacao: String = "",
    val permitirNaoAutorizado: Boolean = false
)
