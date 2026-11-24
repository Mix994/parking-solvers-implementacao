package com.parkingsolvers.smartpark.servidor.dto.request

data class ServidorRequest(
    val nome: String,
    val prontuario: String,
    val email: String,
    val telefone: String,
    val setor: String
)
