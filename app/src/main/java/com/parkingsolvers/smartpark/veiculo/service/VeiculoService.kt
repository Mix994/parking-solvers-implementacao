package com.parkingsolvers.smartpark.veiculo.service

import com.parkingsolvers.smartpark.compartilhado.exceptions.ConflitoException
import com.parkingsolvers.smartpark.compartilhado.exceptions.RecursoInativoException
import com.parkingsolvers.smartpark.compartilhado.exceptions.RecursoNaoEncontradoException
import com.parkingsolvers.smartpark.compartilhado.util.Formatadores
import com.parkingsolvers.smartpark.compartilhado.validacao.ValidadorFormulario
import com.parkingsolvers.smartpark.compartilhado.validacao.ValidadorPlaca
import com.parkingsolvers.smartpark.servidor.repository.ServidorRepository
import com.parkingsolvers.smartpark.servidor.service.ServidorService
import com.parkingsolvers.smartpark.veiculo.dto.mapper.VeiculoMapper
import com.parkingsolvers.smartpark.veiculo.dto.request.VeiculoRequest
import com.parkingsolvers.smartpark.veiculo.dto.response.ResultadoConsultaPlaca
import com.parkingsolvers.smartpark.veiculo.dto.response.StatusAutorizacao
import com.parkingsolvers.smartpark.veiculo.dto.response.VeiculoResponse
import com.parkingsolvers.smartpark.veiculo.model.TipoCadastroVeiculo
import com.parkingsolvers.smartpark.veiculo.model.Veiculo
import com.parkingsolvers.smartpark.veiculo.repository.VeiculoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class VeiculoService(
    private val veiculoRepository: VeiculoRepository,
    private val servidorRepository: ServidorRepository,
    private val servidorService: ServidorService
) {

    fun observarTodos(): Flow<List<VeiculoResponse>> = combine(
        veiculoRepository.observarTodos(),
        servidorRepository.observarTodos()
    ) { veiculos, servidores ->
        val servidoresPorId = servidores.associateBy { it.id }
        val agora = System.currentTimeMillis()
        veiculos.map { veiculo ->
            VeiculoMapper.paraResponse(veiculo, servidoresPorId[veiculo.servidorId], agora)
        }
    }

    fun observarPorServidor(servidorId: Long): Flow<List<VeiculoResponse>> = combine(
        veiculoRepository.observarPorServidor(servidorId),
        servidorRepository.observarTodos()
    ) { veiculos, servidores ->
        val servidoresPorId = servidores.associateBy { it.id }
        val agora = System.currentTimeMillis()
        veiculos.map { veiculo ->
            VeiculoMapper.paraResponse(veiculo, servidoresPorId[veiculo.servidorId], agora)
        }
    }

    suspend fun cadastrar(request: VeiculoRequest): VeiculoResponse {
        val servidor = servidorService.buscarEntidade(request.servidorId)
        if (!servidor.ativo) {
            throw RecursoInativoException("Não é possível cadastrar veículo para servidor inativo.")
        }

        val erros = mutableMapOf<String, String>()
        if (!ValidadorPlaca.ehValida(request.placa)) {
            erros["placa"] = "Formato de placa inválido. Use AAA-1234 ou Mercosul (ABC1D23)."
        }
        if (request.modelo.isBlank()) {
            erros["modelo"] = "Informe o modelo do veículo."
        }
        if (request.cor.isBlank()) {
            erros["cor"] = "Informe a cor do veículo."
        }
        if (request.tipoCadastro == TipoCadastroVeiculo.TEMPORARIO) {
            ValidadorFormulario.validarPeriodoTemporario(
                request.validadeInicioMillis,
                request.validadeFimMillis
            )?.let { erros["validade"] = it }
        }
        ValidadorFormulario.lancarSeHouverErros(erros)

        val placaNormalizada = ValidadorPlaca.validarENormalizar(request.placa)

        if (veiculoRepository.buscarPorPlaca(placaNormalizada) != null) {
            throw ConflitoException("Já existe um veículo cadastrado com a placa ${Formatadores.formatarPlaca(placaNormalizada)}.")
        }

        val entidade = VeiculoMapper.paraEntidade(request, placaNormalizada)
        val id = veiculoRepository.salvar(entidade)
        return VeiculoMapper.paraResponse(entidade.copy(id = id), servidor)
    }

    suspend fun alternarAtivo(id: Long): VeiculoResponse {
        val veiculo = veiculoRepository.buscarPorId(id)
            ?: throw RecursoNaoEncontradoException("Veículo", id)
        val atualizado = veiculo.copy(ativo = !veiculo.ativo)
        veiculoRepository.atualizar(atualizado)
        val servidor = servidorRepository.buscarPorId(atualizado.servidorId)
        return VeiculoMapper.paraResponse(atualizado, servidor)
    }

    suspend fun consultarPlaca(
        placaEntrada: String,
        agoraMillis: Long = System.currentTimeMillis()
    ): ResultadoConsultaPlaca {
        val placaNormalizada = ValidadorPlaca.validarENormalizar(placaEntrada)
        val veiculo = veiculoRepository.buscarPorPlaca(placaNormalizada)
            ?: return ResultadoConsultaPlaca(
                placaConsultada = placaNormalizada,
                status = StatusAutorizacao.NAO_CADASTRADO,
                mensagem = "Placa não encontrada no cadastro do campus."
            )

        val servidor = servidorRepository.buscarPorId(veiculo.servidorId)
        val veiculoResponse = VeiculoMapper.paraResponse(veiculo, servidor, agoraMillis)
        return avaliarAutorizacao(veiculo, servidor?.ativo == true, veiculoResponse, agoraMillis)
    }

    companion object {
        /**
         * Regra pura de negócio para determinar se um veículo cadastrado está autorizado no instante informado.
         */
        fun avaliarAutorizacao(
            veiculo: Veiculo,
            servidorAtivo: Boolean,
            veiculoResponse: VeiculoResponse?,
            agoraMillis: Long
        ): ResultadoConsultaPlaca {
            if (!veiculo.ativo) {
                return ResultadoConsultaPlaca(
                    placaConsultada = veiculo.placa,
                    status = StatusAutorizacao.VEICULO_INATIVO,
                    mensagem = "Veículo cadastrado, porém marcado como inativo.",
                    veiculo = veiculoResponse
                )
            }
            if (!servidorAtivo) {
                return ResultadoConsultaPlaca(
                    placaConsultada = veiculo.placa,
                    status = StatusAutorizacao.SERVIDOR_INATIVO,
                    mensagem = "O servidor vinculado a este veículo está inativo.",
                    veiculo = veiculoResponse
                )
            }
            if (veiculo.tipoCadastro == TipoCadastroVeiculo.PERMANENTE) {
                return ResultadoConsultaPlaca(
                    placaConsultada = veiculo.placa,
                    status = StatusAutorizacao.AUTORIZADO_PERMANENTE,
                    mensagem = "Acesso liberado — veículo permanente do servidor.",
                    veiculo = veiculoResponse
                )
            }

            val inicio = veiculo.validadeInicioMillis
            val fim = veiculo.validadeFimMillis
            return when {
                inicio == null || fim == null || agoraMillis > fim -> ResultadoConsultaPlaca(
                    placaConsultada = veiculo.placa,
                    status = StatusAutorizacao.TEMPORARIO_EXPIRADO,
                    mensagem = "Cadastro temporário expirado${fim?.let { " em ${Formatadores.formatarDataHora(it)}" } ?: ""}.",
                    veiculo = veiculoResponse
                )
                agoraMillis < inicio -> ResultadoConsultaPlaca(
                    placaConsultada = veiculo.placa,
                    status = StatusAutorizacao.TEMPORARIO_FORA_VIGENCIA,
                    mensagem = "Cadastro temporário válido somente a partir de ${Formatadores.formatarDataHora(inicio)}.",
                    veiculo = veiculoResponse
                )
                else -> ResultadoConsultaPlaca(
                    placaConsultada = veiculo.placa,
                    status = StatusAutorizacao.AUTORIZADO_TEMPORARIO,
                    mensagem = "Acesso liberado — temporário válido até ${Formatadores.formatarDataHora(fim)}.",
                    veiculo = veiculoResponse
                )
            }
        }
    }
}
