package com.parkingsolvers.smartpark.compartilhado.exceptions

/**
 * Exceção base para violações de regras de negócio no domínio do SmartPark.
 */
open class RegraDeNegocioException(message: String) : RuntimeException(message)
