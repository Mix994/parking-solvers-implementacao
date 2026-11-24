package com.parkingsolvers.smartpark.compartilhado.exceptions

/**
 * Exceção lançada quando uma operação exige que o recurso (servidor ou veículo) esteja ativo.
 */
class RecursoInativoException(message: String) : RegraDeNegocioException(message)
