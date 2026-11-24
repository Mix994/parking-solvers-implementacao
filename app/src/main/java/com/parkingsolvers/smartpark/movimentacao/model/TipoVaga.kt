package com.parkingsolvers.smartpark.movimentacao.model

enum class TipoVaga(
    val label: String,
    val ehExclusiva: Boolean,
    val ehCarregamentoEletrico: Boolean = false
) {
    COMUM("Vaga Comum", ehExclusiva = false),
    CARREGAMENTO_ELETRICO(
        label = "Carregamento Elétrico",
        ehExclusiva = false,
        ehCarregamentoEletrico = true
    ),
    EXCLUSIVA_PCD("Exclusiva PCD", ehExclusiva = true),
    EXCLUSIVA_IDOSO("Exclusiva Idoso", ehExclusiva = true),
    EXCLUSIVA_OFICIAL("Viatura / Oficial", ehExclusiva = true)
}
