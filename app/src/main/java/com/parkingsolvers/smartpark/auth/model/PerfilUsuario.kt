package com.parkingsolvers.smartpark.auth.model

/**
 * Perfis de acesso do SmartPark (análogo ao enum Role do repositório de referência).
 */
enum class PerfilUsuario(val label: String, val descricao: String) {
    ADMIN(
        label = "Administração",
        descricao = "Acompanha ocupação por turno, vagas exclusivas e cadastros do campus"
    ),
    PORTARIA(
        label = "Portaria",
        descricao = "Consulta placas rapidamente e registra entradas e saídas de veículos"
    ),
    SERVIDOR(
        label = "Servidor / Docente",
        descricao = "Gerencia veículos permanentes e informa trocas temporárias com validade"
    )
}
