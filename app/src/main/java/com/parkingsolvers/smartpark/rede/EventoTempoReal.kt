package com.parkingsolvers.smartpark.rede

data class EventoTempoReal(
    val id: Long = 0L,
    val tipo: String = "",
    val placa: String? = null,
    val modeloCor: String? = null,
    val responsavel: String? = null,
    val tipoVaga: String? = null,
    val tipoVagaLabel: String? = null,
    val origemUsuarioId: Long? = null,
    val origemPerfil: String? = null,
    val simulacaoServidor: Boolean = false,
    val ocorridoEm: String? = null
) {
    val ehEntrada: Boolean
        get() = tipo == TIPO_MOVIMENTACAO_ENTRADA || tipo == TIPO_VISITANTE_ENTRADA

    val ehSaida: Boolean
        get() = tipo == TIPO_MOVIMENTACAO_SAIDA || tipo == TIPO_VISITANTE_SAIDA

    val ehMovimentacaoVeiculo: Boolean
        get() = ehEntrada || ehSaida

    val ehVisitante: Boolean
        get() = tipo == TIPO_VISITANTE_ENTRADA || tipo == TIPO_VISITANTE_SAIDA

    companion object {
        const val TIPO_MOVIMENTACAO_ENTRADA = "MOVIMENTACAO_ENTRADA"
        const val TIPO_MOVIMENTACAO_SAIDA = "MOVIMENTACAO_SAIDA"
        const val TIPO_VISITANTE_ENTRADA = "VISITANTE_ENTRADA"
        const val TIPO_VISITANTE_SAIDA = "VISITANTE_SAIDA"
        const val TIPO_CONFIGURACAO_ATUALIZADA = "CONFIGURACAO_ATUALIZADA"
        const val TIPO_CADASTRO_ATUALIZADO = "CADASTRO_ATUALIZADO"
    }
}
