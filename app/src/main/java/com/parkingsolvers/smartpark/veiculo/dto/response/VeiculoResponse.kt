package com.parkingsolvers.smartpark.veiculo.dto.response

import com.parkingsolvers.smartpark.veiculo.model.TipoCadastroVeiculo

data class VeiculoResponse(
    val id: Long,
    val servidorId: Long,
    val servidorNome: String,
    val servidorProntuario: String,
    val placa: String,
    val modelo: String,
    val cor: String,
    val tipoCadastro: TipoCadastroVeiculo,
    val validadeInicioMillis: Long?,
    val validadeFimMillis: Long?,
    val ativo: Boolean,
    val validoNoMomento: Boolean
)
