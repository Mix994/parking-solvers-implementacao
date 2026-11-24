package com.parkingsolvers.smartpark.servidor.dto.mapper

import com.parkingsolvers.smartpark.servidor.dto.request.ServidorRequest
import com.parkingsolvers.smartpark.servidor.dto.response.ServidorResponse
import com.parkingsolvers.smartpark.servidor.model.Servidor

object ServidorMapper {

    fun paraEntidade(request: ServidorRequest): Servidor = Servidor(
        nome = request.nome.trim(),
        prontuario = request.prontuario.trim().uppercase(),
        email = request.email.trim().lowercase(),
        telefone = request.telefone.trim(),
        setor = request.setor.trim(),
        ativo = true
    )

    fun paraResponse(servidor: Servidor): ServidorResponse = ServidorResponse(
        id = servidor.id,
        nome = servidor.nome,
        prontuario = servidor.prontuario,
        email = servidor.email,
        telefone = servidor.telefone,
        setor = servidor.setor,
        ativo = servidor.ativo
    )
}
