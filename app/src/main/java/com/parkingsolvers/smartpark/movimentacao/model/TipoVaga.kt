package com.parkingsolvers.smartpark.movimentacao.model

enum class TipoVaga(val label: String, val ehExclusiva: Boolean) {
    COMUM("Vaga Comum", false),
    EXCLUSIVA_PCD("Exclusiva PCD", true),
    EXCLUSIVA_IDOSO("Exclusiva Idoso", true),
    EXCLUSIVA_OFICIAL("Viatura / Oficial", true)
}
