package com.parkingsolvers.smartpark.movimentacao.dto.response

import com.parkingsolvers.smartpark.movimentacao.model.TipoVaga
import com.parkingsolvers.smartpark.movimentacao.model.Turno

data class MovimentacaoResponse(
    val id: Long,
    val veiculoId: Long?,
    val placa: String,
    val modeloCor: String,
    val servidorNome: String,
    val autorizado: Boolean,
    val dataHoraEntradaMillis: Long,
    val dataHoraSaidaMillis: Long?,
    val turno: Turno,
    val tipoVaga: TipoVaga,
    val observacao: String
) {
    val emAberto: Boolean
        get() = dataHoraSaidaMillis == null
}
