package com.parkingsolvers.smartpark.compartilhado.validacao

import com.parkingsolvers.smartpark.compartilhado.exceptions.PlacaInvalidaException

/**
 * Utilitário para validação e normalização de placas brasileiras (padrão antigo e Mercosul).
 */
object ValidadorPlaca {

    private val PADRAO_PLACA = Regex("^([A-Za-z]{3}-?[0-9]{4}|[A-Za-z]{3}-?[0-9][A-Za-z][0-9]{2})$")

    fun ehValida(placa: String?): Boolean {
        if (placa.isNullOrBlank()) return false
        return PADRAO_PLACA.matches(placa.trim())
    }

    /**
     * Remove espaços e hífen e converte para maiúsculas (ex.: "abc-1234" -> "ABC1234").
     */
    fun normalizar(placa: String): String {
        return placa.trim().uppercase().replace("-", "")
    }

    /**
     * Valida e retorna a placa normalizada; lança [PlacaInvalidaException] se inválida.
     */
    fun validarENormalizar(placa: String?): String {
        if (placa.isNullOrBlank() || !ehValida(placa)) {
            throw PlacaInvalidaException()
        }
        return normalizar(placa)
    }
}
