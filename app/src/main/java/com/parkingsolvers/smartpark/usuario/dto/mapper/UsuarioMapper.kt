package com.parkingsolvers.smartpark.usuario.dto.mapper

import com.parkingsolvers.smartpark.servidor.model.Servidor
import com.parkingsolvers.smartpark.usuario.dto.response.UsuarioResponse
import com.parkingsolvers.smartpark.usuario.model.Usuario

object UsuarioMapper {

    fun paraResponse(usuario: Usuario, servidor: Servidor?): UsuarioResponse = UsuarioResponse(
        id = usuario.id,
        nome = usuario.nome,
        login = usuario.login,
        perfil = usuario.perfil,
        servidorId = usuario.servidorId,
        servidorProntuario = servidor?.prontuario,
        ativo = usuario.ativo
    )
}
