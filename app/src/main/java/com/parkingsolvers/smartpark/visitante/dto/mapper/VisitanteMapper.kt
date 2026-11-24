package com.parkingsolvers.smartpark.visitante.dto.mapper

import com.parkingsolvers.smartpark.movimentacao.model.Turno
import com.parkingsolvers.smartpark.visitante.dto.request.VisitanteRequest
import com.parkingsolvers.smartpark.visitante.dto.response.VisitanteResponse
import com.parkingsolvers.smartpark.visitante.model.Visitante

object VisitanteMapper {

    fun paraEntidade(
        request: VisitanteRequest,
        placaNormalizada: String,
        turno: Turno,
        registradoPor: String,
        entradaMillis: Long
    ): Visitante = Visitante(
        nome = request.nome.trim(),
        documento = request.documento.trim().uppercase(),
        instituicaoOrigem = request.instituicaoOrigem.trim(),
        telefone = request.telefone.trim(),
        placa = placaNormalizada,
        modeloVeiculo = request.modeloVeiculo.trim(),
        corVeiculo = request.corVeiculo.trim(),
        motivoVisita = request.motivoVisita.trim(),
        setorDestino = request.setorDestino.trim(),
        tipoVaga = request.tipoVaga,
        turno = turno,
        registradoPor = registradoPor.trim(),
        dataHoraEntradaMillis = entradaMillis,
        dataHoraSaidaMillis = null,
        observacao = request.observacao.trim()
    )

    fun paraResponse(entidade: Visitante): VisitanteResponse = VisitanteResponse(
        id = entidade.id,
        nome = entidade.nome,
        documento = entidade.documento,
        instituicaoOrigem = entidade.instituicaoOrigem,
        telefone = entidade.telefone,
        placa = entidade.placa,
        modeloVeiculo = entidade.modeloVeiculo,
        corVeiculo = entidade.corVeiculo,
        motivoVisita = entidade.motivoVisita,
        setorDestino = entidade.setorDestino,
        tipoVaga = entidade.tipoVaga,
        turno = entidade.turno,
        registradoPor = entidade.registradoPor,
        dataHoraEntradaMillis = entidade.dataHoraEntradaMillis,
        dataHoraSaidaMillis = entidade.dataHoraSaidaMillis,
        observacao = entidade.observacao
    )
}
