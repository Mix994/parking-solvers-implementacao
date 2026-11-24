package com.parkingsolvers.smartpark.movimentacao.service

import com.parkingsolvers.smartpark.auth.service.AuthServiceTest
import com.parkingsolvers.smartpark.compartilhado.exceptions.ConflitoException
import com.parkingsolvers.smartpark.compartilhado.exceptions.RegraDeNegocioException
import com.parkingsolvers.smartpark.movimentacao.dto.request.EntradaRequest
import com.parkingsolvers.smartpark.movimentacao.model.RegistroMovimentacao
import com.parkingsolvers.smartpark.movimentacao.model.TipoVaga
import com.parkingsolvers.smartpark.movimentacao.repository.MovimentacaoDao
import com.parkingsolvers.smartpark.movimentacao.repository.MovimentacaoRepository
import com.parkingsolvers.smartpark.servidor.dto.request.ServidorRequest
import com.parkingsolvers.smartpark.servidor.repository.ServidorRepository
import com.parkingsolvers.smartpark.servidor.service.ServidorService
import com.parkingsolvers.smartpark.veiculo.dto.request.VeiculoRequest
import com.parkingsolvers.smartpark.veiculo.model.TipoCadastroVeiculo
import com.parkingsolvers.smartpark.veiculo.model.Veiculo
import com.parkingsolvers.smartpark.veiculo.repository.VeiculoDao
import com.parkingsolvers.smartpark.veiculo.repository.VeiculoRepository
import com.parkingsolvers.smartpark.veiculo.service.VeiculoService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MovimentacaoServiceTest {

    @Test
    fun registraEntradaAutorizadaESaidaComSucesso() = runBlocking {
        val env = criarAmbiente()
        val servidor = env.servidorService.cadastrar(
            ServidorRequest("Ana", "BP102401", "ana@campus.edu.br", "11987651101", "BCC")
        )
        env.veiculoService.cadastrar(
            VeiculoRequest(servidor.id, "ABC1D23", "Civic", "Prata", TipoCadastroVeiculo.PERMANENTE)
        )

        val entrada = env.movimentacaoService.registrarEntrada(
            EntradaRequest(placa = "ABC1D23", tipoVaga = TipoVaga.COMUM),
            agoraMillis = 10_000L
        )
        assertTrue(entrada.autorizado)
        assertTrue(entrada.emAberto)

        val saida = env.movimentacaoService.registrarSaida(entrada.id, agoraMillis = 20_000L)
        assertFalse(saida.emAberto)
    }

    @Test(expected = ConflitoException::class)
    fun bloqueiaEntradaDuplicadaQuandoVeiculoJaEstaNoPatio() = runBlocking {
        val env = criarAmbiente()
        val servidor = env.servidorService.cadastrar(
            ServidorRequest("Ana", "BP102401", "ana@campus.edu.br", "11987651101", "BCC")
        )
        env.veiculoService.cadastrar(
            VeiculoRequest(servidor.id, "ABC1D23", "Civic", "Prata", TipoCadastroVeiculo.PERMANENTE)
        )

        env.movimentacaoService.registrarEntrada(EntradaRequest("ABC1D23"), agoraMillis = 10_000L)
        env.movimentacaoService.registrarEntrada(EntradaRequest("ABC1D23"), agoraMillis = 15_000L)
        Unit
    }

    @Test(expected = RegraDeNegocioException::class)
    fun bloqueiaPlacaNaoCadastradaSemLiberacaoExcepcional() = runBlocking {
        val env = criarAmbiente()
        env.movimentacaoService.registrarEntrada(
            EntradaRequest("XYZ9A99", permitirNaoAutorizado = false),
            agoraMillis = 10_000L
        )
        Unit
    }

    @Test
    fun permiteEntradaExcepcionalQuandoAutorizadoPelaPortaria() = runBlocking {
        val env = criarAmbiente()
        val mov = env.movimentacaoService.registrarEntrada(
            EntradaRequest("XYZ9A99", permitirNaoAutorizado = true, observacao = "Entrega"),
            agoraMillis = 10_000L
        )
        assertFalse(mov.autorizado)
        assertTrue(mov.emAberto)
    }

    private data class Ambiente(
        val servidorService: ServidorService,
        val veiculoService: VeiculoService,
        val movimentacaoService: MovimentacaoService
    )

    private fun criarAmbiente(): Ambiente {
        val servidorRepo = ServidorRepository(AuthServiceTest.FakeServidorDao())
        val veiculoRepo = VeiculoRepository(FakeVeiculoDao())
        val movRepo = MovimentacaoRepository(FakeMovimentacaoDao())
        val servidorService = ServidorService(servidorRepo)
        val veiculoService = VeiculoService(veiculoRepo, servidorRepo, servidorService)
        val movService = MovimentacaoService(movRepo, veiculoRepo, servidorRepo, veiculoService)
        return Ambiente(servidorService, veiculoService, movService)
    }

    private class FakeVeiculoDao : VeiculoDao {
        private val itens = mutableListOf<Veiculo>()
        private val fluxo = MutableStateFlow<List<Veiculo>>(emptyList())
        private var seq = 1L

        override fun observarTodos(): Flow<List<Veiculo>> = fluxo
        override fun observarPorServidor(servidorId: Long): Flow<List<Veiculo>> = fluxo
        override suspend fun buscarPorId(id: Long): Veiculo? = itens.firstOrNull { it.id == id }
        override suspend fun buscarPorPlaca(placa: String): Veiculo? =
            itens.firstOrNull { it.placa.equals(placa, ignoreCase = true) }
        override suspend fun listarPorServidor(servidorId: Long): List<Veiculo> =
            itens.filter { it.servidorId == servidorId }
        override suspend fun inserir(veiculo: Veiculo): Long {
            val id = seq++
            itens.add(veiculo.copy(id = id))
            fluxo.value = itens.toList()
            return id
        }
        override suspend fun atualizar(veiculo: Veiculo) {
            val idx = itens.indexOfFirst { it.id == veiculo.id }
            if (idx >= 0) {
                itens[idx] = veiculo
                fluxo.value = itens.toList()
            }
        }
    }

    private class FakeMovimentacaoDao : MovimentacaoDao {
        private val itens = mutableListOf<RegistroMovimentacao>()
        private val fluxoTodas = MutableStateFlow<List<RegistroMovimentacao>>(emptyList())
        private val fluxoEmAberto = MutableStateFlow<List<RegistroMovimentacao>>(emptyList())
        private var seq = 1L

        override fun observarTodas(): Flow<List<RegistroMovimentacao>> = fluxoTodas
        override fun observarEmAberto(): Flow<List<RegistroMovimentacao>> = fluxoEmAberto
        override suspend fun buscarPorId(id: Long): RegistroMovimentacao? = itens.firstOrNull { it.id == id }
        override suspend fun buscarEmAbertoPorPlaca(placa: String): RegistroMovimentacao? =
            itens.firstOrNull { it.placaRegistrada.equals(placa, ignoreCase = true) && it.emAberto }
        override suspend fun contarEmAbertoPorVeiculo(veiculoId: Long): Int =
            itens.count { it.veiculoId == veiculoId && it.emAberto }
        override suspend fun inserir(registro: RegistroMovimentacao): Long {
            val id = seq++
            itens.add(registro.copy(id = id))
            fluxoTodas.value = itens.toList()
            fluxoEmAberto.value = itens.filter { it.emAberto }
            return id
        }
        override suspend fun atualizar(registro: RegistroMovimentacao) {
            val idx = itens.indexOfFirst { it.id == registro.id }
            if (idx >= 0) {
                itens[idx] = registro
                fluxoTodas.value = itens.toList()
                fluxoEmAberto.value = itens.filter { it.emAberto }
            }
        }
    }
}
