package com.parkingsolvers.smartpark.compartilhado.exceptions

/**
 * Exceção lançada quando um recurso (Servidor, Veículo, Movimentação) não é encontrado na base local.
 */
class RecursoNaoEncontradoException : RegraDeNegocioException {
    constructor(message: String) : super(message)
    constructor(recurso: String, id: Any) : super("$recurso não encontrado(a) com ID: $id")
}
