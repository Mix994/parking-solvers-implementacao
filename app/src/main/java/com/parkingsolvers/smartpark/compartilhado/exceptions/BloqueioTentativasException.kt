package com.parkingsolvers.smartpark.compartilhado.exceptions

/**
 * Exceção lançada quando uma conta ultrapassa o limite de tentativas consecutivas
 * de login falhas (proteção contra força bruta, análogo ao CarToon).
 */
class BloqueioTentativasException(
    message: String = "Número de tentativas excedido. Conta temporariamente bloqueada por 15 minutos."
) : RegraDeNegocioException(message)
