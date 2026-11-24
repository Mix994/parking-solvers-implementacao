package com.parkingsolvers.smartpark.veiculo.dto.request

import com.parkingsolvers.smartpark.veiculo.model.TipoCadastroVeiculo

data class VeiculoRequest(
    val servidorId: Long,
    val placa: String,
    val modelo: String,
    val cor: String,
    val tipoCadastro: TipoCadastroVeiculo = TipoCadastroVeiculo.PERMANENTE,
    val validadeInicioMillis: Long? = null,
    val validadeFimMillis: Long? = null
)
