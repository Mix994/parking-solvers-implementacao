package com.parkingsolvers.smartpark.compartilhado.exceptions

/**
 * Exceção lançada quando um ou mais campos de formulário falham na validação.
 * Mantém um mapa campo -> mensagem para exibição inline na interface.
 */
class ValidacaoException(
    val errosValidacao: Map<String, String>,
    message: String = errosValidacao.values.firstOrNull() ?: "Erro de validação nos campos."
) : RegraDeNegocioException(message)
