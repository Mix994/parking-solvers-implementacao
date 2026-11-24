package com.parkingsolvers.smartpark.servidor.service

import com.parkingsolvers.smartpark.compartilhado.exceptions.ConflitoException
import com.parkingsolvers.smartpark.compartilhado.exceptions.RecursoNaoEncontradoException
import com.parkingsolvers.smartpark.compartilhado.validacao.ValidadorFormulario
import com.parkingsolvers.smartpark.servidor.dto.mapper.ServidorMapper
import com.parkingsolvers.smartpark.servidor.dto.request.ServidorRequest
import com.parkingsolvers.smartpark.servidor.dto.response.ServidorResponse
import com.parkingsolvers.smartpark.servidor.model.Servidor
import com.parkingsolvers.smartpark.servidor.repository.ServidorRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ServidorService(
    private val repository: ServidorRepository
) {

    fun observarTodos(): Flow<List<ServidorResponse>> =
        repository.observarTodos().map { lista -> lista.map(ServidorMapper::paraResponse) }

    suspend fun buscarEntidade(id: Long): Servidor =
        repository.buscarPorId(id) ?: throw RecursoNaoEncontradoException("Servidor", id)

    suspend fun cadastrar(request: ServidorRequest): ServidorResponse {
        validarCampos(request)

        val prontuarioNormalizado = request.prontuario.trim().uppercase()
        if (repository.buscarPorProntuario(prontuarioNormalizado) != null) {
            throw ConflitoException("Já existe um servidor cadastrado com o prontuário $prontuarioNormalizado.")
        }

        val entidade = ServidorMapper.paraEntidade(request)
        val novoId = repository.salvar(entidade)
        return ServidorMapper.paraResponse(entidade.copy(id = novoId))
    }

    suspend fun alternarAtivo(id: Long): ServidorResponse {
        val servidor = buscarEntidade(id)
        val atualizado = servidor.copy(ativo = !servidor.ativo)
        repository.atualizar(atualizado)
        return ServidorMapper.paraResponse(atualizado)
    }

    private fun validarCampos(request: ServidorRequest) {
        val erros = mutableMapOf<String, String>()
        if (request.nome.isBlank()) {
            erros["nome"] = "Informe o nome completo do servidor."
        }
        if (request.prontuario.trim().length < 4) {
            erros["prontuario"] = "Informe um prontuário/SIAPE válido (mínimo 4 caracteres)."
        }
        if (!ValidadorFormulario.validarEmail(request.email)) {
            erros["email"] = "Informe um e-mail institucional válido."
        }
        if (!ValidadorFormulario.validarTelefone(request.telefone)) {
            erros["telefone"] = "Informe um telefone válido (ex.: (11) 99999-8888)."
        }
        if (request.setor.isBlank()) {
            erros["setor"] = "Informe o setor ou coordenação."
        }
        ValidadorFormulario.lancarSeHouverErros(erros)
    }
}
