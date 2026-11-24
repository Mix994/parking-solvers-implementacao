package com.parkingsolvers.smartpark.veiculo.dto.response

enum class StatusAutorizacao(val label: String, val autorizado: Boolean) {
    AUTORIZADO_PERMANENTE("Autorizado (Permanente)", true),
    AUTORIZADO_TEMPORARIO("Autorizado (Temporário)", true),
    TEMPORARIO_EXPIRADO("Temporário Expirado", false),
    TEMPORARIO_FORA_VIGENCIA("Fora da Vigência", false),
    VEICULO_INATIVO("Veículo Inativo", false),
    SERVIDOR_INATIVO("Servidor Inativo", false),
    NAO_CADASTRADO("Não Cadastrado", false)
}

data class ResultadoConsultaPlaca(
    val placaConsultada: String,
    val status: StatusAutorizacao,
    val mensagem: String,
    val veiculo: VeiculoResponse? = null
) {
    val autorizado: Boolean
        get() = status.autorizado
}
