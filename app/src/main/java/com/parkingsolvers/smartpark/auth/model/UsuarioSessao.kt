package com.parkingsolvers.smartpark.auth.model

data class UsuarioSessao(
    val usuarioId: Long,
    val nome: String,
    val identificacao: String,
    val perfil: PerfilUsuario,
    val servidorId: Long? = null
)
