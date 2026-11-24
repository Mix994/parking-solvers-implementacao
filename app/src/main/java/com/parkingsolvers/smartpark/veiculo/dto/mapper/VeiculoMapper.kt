package com.parkingsolvers.smartpark.veiculo.dto.mapper

import com.parkingsolvers.smartpark.servidor.model.Servidor
import com.parkingsolvers.smartpark.veiculo.dto.request.VeiculoRequest
import com.parkingsolvers.smartpark.veiculo.dto.response.VeiculoResponse
import com.parkingsolvers.smartpark.veiculo.model.TipoCadastroVeiculo
import com.parkingsolvers.smartpark.veiculo.model.Veiculo

object VeiculoMapper {

    fun paraEntidade(request: VeiculoRequest, placaNormalizada: String): Veiculo = Veiculo(
        servidorId = request.servidorId,
        placa = placaNormalizada,
        modelo = request.modelo.trim(),
        cor = request.cor.trim(),
        tipoCadastro = request.tipoCadastro,
        validadeInicioMillis = if (request.tipoCadastro == TipoCadastroVeiculo.TEMPORARIO) {
            request.validadeInicioMillis
        } else {
            null
        },
        validadeFimMillis = if (request.tipoCadastro == TipoCadastroVeiculo.TEMPORARIO) {
            request.validadeFimMillis
        } else {
            null
        },
        ativo = true
    )

    fun paraResponse(
        veiculo: Veiculo,
        servidor: Servidor?,
        agoraMillis: Long = System.currentTimeMillis()
    ): VeiculoResponse = VeiculoResponse(
        id = veiculo.id,
        servidorId = veiculo.servidorId,
        servidorNome = servidor?.nome ?: "Servidor #${veiculo.servidorId}",
        servidorProntuario = servidor?.prontuario ?: "-",
        placa = veiculo.placa,
        modelo = veiculo.modelo,
        cor = veiculo.cor,
        tipoCadastro = veiculo.tipoCadastro,
        validadeInicioMillis = veiculo.validadeInicioMillis,
        validadeFimMillis = veiculo.validadeFimMillis,
        ativo = veiculo.ativo,
        validoNoMomento = veiculo.ativo && (servidor?.ativo ?: true) && veiculo.estaDentroDaValidade(agoraMillis)
    )
}
