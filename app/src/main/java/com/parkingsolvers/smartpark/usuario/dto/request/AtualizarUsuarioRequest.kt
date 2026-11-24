package com.parkingsolvers.smartpark.usuario.dto.request

/**
 * DTO para edição dos próprios dados cadastrais ("Meus Dados" — UC01 do CarToon).
 * Quando [novaSenha] estiver em branco, a senha atual é preservada.
 */
data class AtualizarUsuarioRequest(
    val nome: String,
    val login: String,
    val novaSenha: String = "",
    val confirmacaoNovaSenha: String = ""
)
