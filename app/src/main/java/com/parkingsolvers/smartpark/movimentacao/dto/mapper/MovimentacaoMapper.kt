package com.parkingsolvers.smartpark.movimentacao.dto.mapper

import com.parkingsolvers.smartpark.movimentacao.dto.response.MovimentacaoResponse
import com.parkingsolvers.smartpark.movimentacao.model.RegistroMovimentacao
import com.parkingsolvers.smartpark.servidor.model.Servidor
import com.parkingsolvers.smartpark.veiculo.model.Veiculo

object MovimentacaoMapper {

    fun paraResponse(
        registro: RegistroMovimentacao,
        veiculo: Veiculo?,
        servidor: Servidor?
    ): MovimentacaoResponse = MovimentacaoResponse(
        id = registro.id,
        veiculoId = registro.veiculoId,
        placa = registro.placaRegistrada,
        modeloCor = veiculo?.let { "${it.modelo} • ${it.cor}" } ?: "Veículo não cadastrado",
        servidorNome = servidor?.nome ?: "Acesso avulso / visitante",
        autorizado = registro.autorizado,
        dataHoraEntradaMillis = registro.dataHoraEntradaMillis,
        dataHoraSaidaMillis = registro.dataHoraSaidaMillis,
        turno = registro.turno,
        tipoVaga = registro.tipoVaga,
        observacao = registro.observacao
    )
}
