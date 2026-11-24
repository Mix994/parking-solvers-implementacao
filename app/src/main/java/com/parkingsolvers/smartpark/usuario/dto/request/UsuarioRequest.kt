package com.parkingsolvers.smartpark.usuario.dto.request

import com.parkingsolvers.smartpark.auth.model.PerfilUsuario

data class UsuarioRequest(
    val nome: String,
    val login: String,
    val senha: String,
    val confirmacaoSenha: String = senha,
    val perfil: PerfilUsuario,
    val servidorId: Long? = null
)
