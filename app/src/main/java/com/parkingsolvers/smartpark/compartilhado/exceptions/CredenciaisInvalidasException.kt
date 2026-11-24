package com.parkingsolvers.smartpark.compartilhado.exceptions

/**
 * Exceção lançada quando login ou senha são inválidos (análogo ao CarToon).
 */
class CredenciaisInvalidasException(
    message: String = "Login ou senha incorretos."
) : RegraDeNegocioException(message)
