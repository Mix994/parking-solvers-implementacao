package com.parkingsolvers.smartpark.visitante.service

import com.parkingsolvers.smartpark.compartilhado.exceptions.ConflitoException
import com.parkingsolvers.smartpark.compartilhado.exceptions.RecursoNaoEncontradoException
import com.parkingsolvers.smartpark.compartilhado.util.Formatadores
import com.parkingsolvers.smartpark.compartilhado.util.RelogioSistema
import com.parkingsolvers.smartpark.compartilhado.util.RelogioSistemaReal
import com.parkingsolvers.smartpark.compartilhado.util.TurnoUtils
import com.parkingsolvers.smartpark.compartilhado.validacao.ValidadorFormulario
import com.parkingsolvers.smartpark.compartilhado.validacao.ValidadorPlaca
import com.parkingsolvers.smartpark.visitante.dto.mapper.VisitanteMapper
import com.parkingsolvers.smartpark.visitante.dto.request.VisitanteRequest
import com.parkingsolvers.smartpark.visitante.dto.response.VisitanteResponse
import com.parkingsolvers.smartpark.visitante.repository.VisitanteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class VisitanteService(
    private val visitanteRepository: VisitanteRepository,
    private val relogio: RelogioSistema = RelogioSistemaReal
) {

    fun observarNoCampus(): Flow<List<VisitanteResponse>> =
        visitanteRepository.observarNoCampus().map { lista -> lista.map(VisitanteMapper::paraResponse) }

    fun observarHistorico(): Flow<List<VisitanteResponse>> =
        visitanteRepository.observarTodos().map { lista -> lista.map(VisitanteMapper::paraResponse) }

    suspend fun registrarEntrada(
        request: VisitanteRequest,
        registradoPor: String,
        agoraMillis: Long = relogio.agoraMillis()
    ): VisitanteResponse {
        validarCampos(request)

        val placaNormalizada = ValidadorPlaca.validarENormalizar(request.placa)

        if (visitanteRepository.buscarNoCampusPorPlaca(placaNormalizada) != null) {
            throw ConflitoException(
                "O visitante com placa ${Formatadores.formatarPlaca(placaNormalizada)} já está registrado no campus."
            )
        }

        val turno = TurnoUtils.determinarTurno(agoraMillis)
        val entidade = VisitanteMapper.paraEntidade(
            request = request,
            placaNormalizada = placaNormalizada,
            turno = turno,
            registradoPor = registradoPor.ifBlank { "Portaria" },
            entradaMillis = agoraMillis
        )
        val novoId = visitanteRepository.salvar(entidade)
        return VisitanteMapper.paraResponse(entidade.copy(id = novoId))
    }

    suspend fun registrarSaida(
        visitanteId: Long,
        agoraMillis: Long = relogio.agoraMillis()
    ): VisitanteResponse {
        val visitante = visitanteRepository.buscarPorId(visitanteId)
            ?: throw RecursoNaoEncontradoException("Visitante", visitanteId)

        if (!visitante.noCampus) {
            throw ConflitoException("A saída deste visitante já foi registrada.")
        }

        val atualizado = visitante.copy(dataHoraSaidaMillis = agoraMillis)
        visitanteRepository.atualizar(atualizado)
        return VisitanteMapper.paraResponse(atualizado)
    }

    private fun validarCampos(request: VisitanteRequest) {
        val erros = mutableMapOf<String, String>()
        if (request.nome.trim().length < 3) {
            erros["nome"] = "Informe o nome completo do visitante."
        }
        if (!ValidadorFormulario.validarDocumento(request.documento)) {
            erros["documento"] = "Informe um CPF ou RG válido (mínimo 5 dígitos)."
        }
        if (request.instituicaoOrigem.isBlank()) {
            erros["instituicaoOrigem"] = "Informe a instituição, cidade ou empresa de origem."
        }
        if (!ValidadorFormulario.validarTelefone(request.telefone)) {
            erros["telefone"] = "Informe um telefone válido (ex.: (11) 99999-8888)."
        }
        if (!ValidadorPlaca.ehValida(request.placa)) {
            erros["placa"] = "Formato inválido. Use AAA-1234 ou Mercosul (ABC1D23)."
        }
        if (request.modeloVeiculo.isBlank()) {
            erros["modeloVeiculo"] = "Informe o modelo do veículo."
        }
        if (request.corVeiculo.isBlank()) {
            erros["corVeiculo"] = "Informe a cor do veículo."
        }
        if (request.motivoVisita.isBlank()) {
            erros["motivoVisita"] = "Informe o motivo da visita."
        }
        if (request.setorDestino.isBlank()) {
            erros["setorDestino"] = "Informe o setor ou pessoa visitada no campus."
        }
        ValidadorFormulario.lancarSeHouverErros(erros)
    }
}
