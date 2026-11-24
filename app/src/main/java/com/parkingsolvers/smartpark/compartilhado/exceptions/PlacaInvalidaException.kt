package com.parkingsolvers.smartpark.compartilhado.exceptions

/**
 * Exceção lançada quando uma placa de veículo não segue o padrão antigo (AAA-1234) ou Mercosul (AAA1A23).
 */
class PlacaInvalidaException(
    message: String = "Formato de placa inválido. Utilize o padrão AAA-1234 ou Mercosul (ABC1D23)."
) : RegraDeNegocioException(message)
