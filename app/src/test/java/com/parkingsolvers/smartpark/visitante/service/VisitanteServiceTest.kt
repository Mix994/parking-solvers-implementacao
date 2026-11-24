package com.parkingsolvers.smartpark.visitante.service

import com.parkingsolvers.smartpark.compartilhado.exceptions.ConflitoException
import com.parkingsolvers.smartpark.compartilhado.exceptions.ValidacaoException
import com.parkingsolvers.smartpark.movimentacao.model.TipoVaga
import com.parkingsolvers.smartpark.visitante.dto.request.VisitanteRequest
import com.parkingsolvers.smartpark.visitante.model.Visitante
import com.parkingsolvers.smartpark.visitante.repository.VisitanteDao
import com.parkingsolvers.smartpark.visitante.repository.VisitanteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class VisitanteServiceTest {

    @Test
    fun registraEntradaESaidaDeVisitanteExternoNormalizandoPlaca() = runBlocking {
        val service = VisitanteService(VisitanteRepository(FakeVisitanteDao()))

        val entrada = service.registrarEntrada(
            request = VisitanteRequest(
                nome = "Prof. Ricardo Mendes",
                documento = "321.654.987-00",
                instituicaoOrigem = "IFSP Campus Guarulhos",
                telefone = "(11) 97412-8890",
                placa = "ifg-8b24",
                modeloVeiculo = "Hyundai Creta",
                corVeiculo = "Azul",
                motivoVisita = "Banca de TCC",
                setorDestino = "Auditório Bloco B",
                tipoVaga = TipoVaga.COMUM
            ),
            registradoPor = "Guarita Principal",
            agoraMillis = 1_700_000_000_000L
        )

        assertEquals("IFG8B24", entrada.placa)
        assertEquals("IFSP Campus Guarulhos", entrada.instituicaoOrigem)
        assertTrue(entrada.noCampus)

        val saida = service.registrarSaida(entrada.id, agoraMillis = 1_700_003_600_000L)
        assertFalse(saida.noCampus)
        assertNotNull(saida.dataHoraSaidaMillis)
    }

    @Test(expected = ConflitoException::class)
    fun impedeEntradaDuplicadaDeVisitanteJaNoCampus() = runBlocking {
        val service = VisitanteService(VisitanteRepository(FakeVisitanteDao()))
        val request = VisitanteRequest(
            nome = "Mariana Ferreira",
            documento = "45.890.123-4",
            instituicaoOrigem = "TechRede Telecom",
            telefone = "(11) 96321-4400",
            placa = "FBR3C12",
            modeloVeiculo = "Fiat Fiorino",
            corVeiculo = "Branco",
            motivoVisita = "Manutenção",
            setorDestino = "CTI"
        )

        service.registrarEntrada(request, registradoPor = "Portaria", agoraMillis = 10_000L)
        service.registrarEntrada(request, registradoPor = "Portaria", agoraMillis = 20_000L)
        Unit
    }

    @Test(expected = ValidacaoException::class)
    fun rejeitaVisitanteComCamposObrigatoriosOuPlacaInvalidos() = runBlocking {
        val service = VisitanteService(VisitanteRepository(FakeVisitanteDao()))
        service.registrarEntrada(
            request = VisitanteRequest(
                nome = "A",
                documento = "12",
                instituicaoOrigem = "",
                telefone = "123",
                placa = "INVALIDA",
                modeloVeiculo = "",
                corVeiculo = "",
                motivoVisita = "",
                setorDestino = ""
            ),
            registradoPor = "Portaria"
        )
        Unit
    }

    @Test(expected = ConflitoException::class)
    fun impedeRegistrarSaidaDuasVezes() = runBlocking {
        val service = VisitanteService(VisitanteRepository(FakeVisitanteDao()))
        val entrada = service.registrarEntrada(
            request = VisitanteRequest(
                nome = "Roberto Alves",
                documento = "112.445.778-90",
                instituicaoOrigem = "Prefeitura",
                telefone = "(11) 95510-2030",
                placa = "PBP2026",
                modeloVeiculo = "Logan",
                corVeiculo = "Prata",
                motivoVisita = "Reunião",
                setorDestino = "Direção"
            ),
            registradoPor = "Portaria",
            agoraMillis = 10_000L
        )

        service.registrarSaida(entrada.id, agoraMillis = 20_000L)
        service.registrarSaida(entrada.id, agoraMillis = 30_000L)
        Unit
    }

    private class FakeVisitanteDao : VisitanteDao {
        private val itens = mutableListOf<Visitante>()
        private val fluxoTodos = MutableStateFlow<List<Visitante>>(emptyList())
        private val fluxoNoCampus = MutableStateFlow<List<Visitante>>(emptyList())
        private var seq = 1L

        override fun observarTodos(): Flow<List<Visitante>> = fluxoTodos
        override fun observarNoCampus(): Flow<List<Visitante>> = fluxoNoCampus
        override suspend fun buscarPorId(id: Long): Visitante? = itens.firstOrNull { it.id == id }
        override suspend fun buscarNoCampusPorPlaca(placa: String): Visitante? =
            itens.firstOrNull { it.placa.equals(placa, ignoreCase = true) && it.noCampus }
        override suspend fun contar(): Int = itens.size
        override suspend fun inserir(visitante: Visitante): Long {
            val id = seq++
            itens.add(visitante.copy(id = id))
            atualizarFluxos()
            return id
        }
        override suspend fun atualizar(visitante: Visitante) {
            val idx = itens.indexOfFirst { it.id == visitante.id }
            if (idx >= 0) {
                itens[idx] = visitante
                atualizarFluxos()
            }
        }
        private fun atualizarFluxos() {
            fluxoTodos.value = itens.toList()
            fluxoNoCampus.value = itens.filter { it.noCampus }
        }
    }
}
