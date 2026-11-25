package com.parkingsolvers.smartpark.usuario.dto.request

import com.parkingsolvers.smartpark.auth.model.PerfilUsuario

data class AtualizarUsuarioAdminRequest(
    val nome: String,
    val login: String,
    val novaSenha: String = "",
    val confirmacaoNovaSenha: String = "",
    val perfil: PerfilUsuario? = null,
    val servidorId: Long? = null
)
