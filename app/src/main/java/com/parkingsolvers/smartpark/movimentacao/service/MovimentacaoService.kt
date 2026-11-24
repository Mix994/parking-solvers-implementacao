package com.parkingsolvers.smartpark.movimentacao.service

import com.parkingsolvers.smartpark.compartilhado.exceptions.ConflitoException
import com.parkingsolvers.smartpark.compartilhado.exceptions.RecursoNaoEncontradoException
import com.parkingsolvers.smartpark.compartilhado.exceptions.RegraDeNegocioException
import com.parkingsolvers.smartpark.compartilhado.util.Formatadores
import com.parkingsolvers.smartpark.compartilhado.util.TurnoUtils
import com.parkingsolvers.smartpark.compartilhado.validacao.ValidadorPlaca
import com.parkingsolvers.smartpark.movimentacao.dto.mapper.MovimentacaoMapper
import com.parkingsolvers.smartpark.movimentacao.dto.request.EntradaRequest
import com.parkingsolvers.smartpark.movimentacao.dto.response.MovimentacaoResponse
import com.parkingsolvers.smartpark.movimentacao.model.RegistroMovimentacao
import com.parkingsolvers.smartpark.movimentacao.repository.MovimentacaoRepository
import com.parkingsolvers.smartpark.servidor.repository.ServidorRepository
import com.parkingsolvers.smartpark.veiculo.repository.VeiculoRepository
import com.parkingsolvers.smartpark.veiculo.service.VeiculoService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class MovimentacaoService(
    private val movimentacaoRepository: MovimentacaoRepository,
    private val veiculoRepository: VeiculoRepository,
    private val servidorRepository: ServidorRepository,
    private val veiculoService: VeiculoService
) {

    fun observarNoEstacionamento(): Flow<List<MovimentacaoResponse>> = combine(
        movimentacaoRepository.observarEmAberto(),
        veiculoRepository.observarTodos(),
        servidorRepository.observarTodos()
    ) { registros, veiculos, servidores ->
        val veiculosPorId = veiculos.associateBy { it.id }
        val servidoresPorId = servidores.associateBy { it.id }
        registros.map { registro ->
            val veiculo = registro.veiculoId?.let { veiculosPorId[it] }
            val servidor = veiculo?.let { servidoresPorId[it.servidorId] }
            MovimentacaoMapper.paraResponse(registro, veiculo, servidor)
        }
    }

    fun observarHistorico(): Flow<List<MovimentacaoResponse>> = combine(
        movimentacaoRepository.observarTodas(),
        veiculoRepository.observarTodos(),
        servidorRepository.observarTodos()
    ) { registros, veiculos, servidores ->
        val veiculosPorId = veiculos.associateBy { it.id }
        val servidoresPorId = servidores.associateBy { it.id }
        registros.map { registro ->
            val veiculo = registro.veiculoId?.let { veiculosPorId[it] }
            val servidor = veiculo?.let { servidoresPorId[it.servidorId] }
            MovimentacaoMapper.paraResponse(registro, veiculo, servidor)
        }
    }

    suspend fun registrarEntrada(
        request: EntradaRequest,
        agoraMillis: Long = System.currentTimeMillis()
    ): MovimentacaoResponse {
        val placaNormalizada = ValidadorPlaca.validarENormalizar(request.placa)

        if (movimentacaoRepository.buscarEmAbertoPorPlaca(placaNormalizada) != null) {
            throw ConflitoException(
                "O veículo ${Formatadores.formatarPlaca(placaNormalizada)} já está dentro do estacionamento."
            )
        }

        val consulta = veiculoService.consultarPlaca(placaNormalizada, agoraMillis)
        if (!consulta.autorizado && !request.permitirNaoAutorizado) {
            throw RegraDeNegocioException(consulta.mensagem)
        }

        val registro = RegistroMovimentacao(
            veiculoId = consulta.veiculo?.id,
            placaRegistrada = placaNormalizada,
            autorizado = consulta.autorizado,
            dataHoraEntradaMillis = agoraMillis,
            dataHoraSaidaMillis = null,
            turno = TurnoUtils.determinarTurno(agoraMillis),
            tipoVaga = request.tipoVaga,
            observacao = request.observacao.trim()
        )

        val novoId = movimentacaoRepository.salvar(registro)
        val veiculo = registro.veiculoId?.let { veiculoRepository.buscarPorId(it) }
        val servidor = veiculo?.let { servidorRepository.buscarPorId(it.servidorId) }
        return MovimentacaoMapper.paraResponse(registro.copy(id = novoId), veiculo, servidor)
    }

    suspend fun registrarSaida(
        movimentacaoId: Long,
        agoraMillis: Long = System.currentTimeMillis()
    ): MovimentacaoResponse {
        val registro = movimentacaoRepository.buscarPorId(movimentacaoId)
            ?: throw RecursoNaoEncontradoException("Movimentação", movimentacaoId)

        if (!registro.emAberto) {
            throw ConflitoException("Saída já foi registrada para esta movimentação.")
        }

        val atualizado = registro.copy(dataHoraSaidaMillis = agoraMillis)
        movimentacaoRepository.atualizar(atualizado)

        val veiculo = atualizado.veiculoId?.let { veiculoRepository.buscarPorId(it) }
        val servidor = veiculo?.let { servidorRepository.buscarPorId(it.servidorId) }
        return MovimentacaoMapper.paraResponse(atualizado, veiculo, servidor)
    }
}
