package com.parkingsolvers.smartpark.compartilhado.exceptions

/**
 * Exceção lançada quando há conflito de unicidade ou estado operacional (ex.: placa ou prontuário duplicado).
 */
class ConflitoException(message: String) : RegraDeNegocioException(message)
