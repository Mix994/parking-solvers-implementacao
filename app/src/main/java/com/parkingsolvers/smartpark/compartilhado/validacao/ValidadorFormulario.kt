package com.parkingsolvers.smartpark.compartilhado.validacao

import com.parkingsolvers.smartpark.compartilhado.exceptions.ValidacaoException

/**
 * Regras reutilizáveis de validação de formulários do SmartPark.
 */
object ValidadorFormulario {

    private val PADRAO_EMAIL = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    private val PADRAO_TELEFONE = Regex("^(\\(?\\d{2}\\)?[\\s-]?)?\\d{4,5}[\\s-]?\\d{4}$")
    private val PADRAO_DOCUMENTO = Regex("^[A-Za-z0-9.\\-/]{5,20}$")

    fun validarEmail(email: String): Boolean = PADRAO_EMAIL.matches(email.trim())

    fun validarTelefone(telefone: String): Boolean = PADRAO_TELEFONE.matches(telefone.trim())

    fun validarDocumento(documento: String): Boolean {
        val limpo = documento.trim()
        val apenasDigitos = limpo.filter { it.isDigit() }
        return PADRAO_DOCUMENTO.matches(limpo) && apenasDigitos.length >= 5
    }

    fun senhasCoincidem(senha: String, confirmacao: String): Boolean =
        senha.isNotEmpty() && senha == confirmacao

    fun validarPeriodoTemporario(inicioMillis: Long?, fimMillis: Long?): String? {
        if (inicioMillis == null || fimMillis == null) {
            return "Informe o período de validade do veículo temporário."
        }
        if (fimMillis <= inicioMillis) {
            return "A data final deve ser posterior à data inicial."
        }
        return null
    }

    fun lancarSeHouverErros(erros: Map<String, String>) {
        if (erros.isNotEmpty()) {
            throw ValidacaoException(erros)
        }
    }
}
