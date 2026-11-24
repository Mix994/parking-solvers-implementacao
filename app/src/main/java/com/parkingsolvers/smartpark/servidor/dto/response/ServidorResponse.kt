package com.parkingsolvers.smartpark.servidor.dto.response

data class ServidorResponse(
    val id: Long,
    val nome: String,
    val prontuario: String,
    val email: String,
    val telefone: String,
    val setor: String,
    val ativo: Boolean
)
