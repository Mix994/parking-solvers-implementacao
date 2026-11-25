package com.parkingsolvers.smartpark.dashboard.model

/**
 * Configuração de capacidades do estacionamento do campus (espelha a API).
 */
data class ConfiguracaoEstacionamento(
    val id: Int = ID_PADRAO,
    val capacidadeTotal: Int = CAPACIDADE_TOTAL_PADRAO,
    val capacidadeExclusivas: Int = CAPACIDADE_EXCLUSIVAS_PADRAO,
    val capacidadeCarregamentoEletrico: Int = CAPACIDADE_ELETRICO_PADRAO
) {
    companion object {
        const val ID_PADRAO = 1
        const val CAPACIDADE_TOTAL_PADRAO = 60
        const val CAPACIDADE_EXCLUSIVAS_PADRAO = 10
        const val CAPACIDADE_ELETRICO_PADRAO = 2
    }
}
