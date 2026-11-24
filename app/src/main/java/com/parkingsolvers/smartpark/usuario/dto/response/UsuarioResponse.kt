package com.parkingsolvers.smartpark.usuario.dto.response

import com.parkingsolvers.smartpark.auth.model.PerfilUsuario

data class UsuarioResponse(
    val id: Long,
    val nome: String,
    val login: String,
    val perfil: PerfilUsuario,
    val servidorId: Long?,
    val servidorProntuario: String?,
    val ativo: Boolean
)
