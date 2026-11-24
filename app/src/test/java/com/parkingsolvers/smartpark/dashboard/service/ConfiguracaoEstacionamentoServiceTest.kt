package com.parkingsolvers.smartpark.dashboard.service

import com.parkingsolvers.smartpark.compartilhado.exceptions.ValidacaoException
import com.parkingsolvers.smartpark.dashboard.model.ConfiguracaoEstacionamento
import com.parkingsolvers.smartpark.dashboard.repository.ConfiguracaoEstacionamentoDao
import com.parkingsolvers.smartpark.dashboard.repository.ConfiguracaoEstacionamentoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class ConfiguracaoEstacionamentoServiceTest {

    @Test
    fun iniciaComDuasVagasDeCarregamentoEletricoEPermiteExpansao() = runBlocking {
        val dao = FakeConfiguracaoDao()
        val service = ConfiguracaoEstacionamentoService(ConfiguracaoEstacionamentoRepository(dao))

        val inicial = service.buscarAtual()
        assertEquals(60, inicial.capacidadeTotal)
        assertEquals(10, inicial.capacidadeExclusivas)
        assertEquals(2, inicial.capacidadeCarregamentoEletrico)

        val expandida = service.atualizarCapacidades(
            capacidadeTotal = 70,
            capacidadeExclusivas = 10,
            capacidadeCarregamentoEletrico = 6
        )
        assertEquals(70, expandida.capacidadeTotal)
        assertEquals(6, expandida.capacidadeCarregamentoEletrico)
        assertEquals(6, service.buscarAtual().capacidadeCarregamentoEletrico)
    }

    @Test(expected = ValidacaoException::class)
    fun rejeitaQuandoSomaDasVagasReservadasUltrapassaTotal() = runBlocking {
        val service = ConfiguracaoEstacionamentoService(
            ConfiguracaoEstacionamentoRepository(FakeConfiguracaoDao())
        )
        service.atualizarCapacidades(
            capacidadeTotal = 10,
            capacidadeExclusivas = 8,
            capacidadeCarregamentoEletrico = 4
        )
        Unit
    }

    internal class FakeConfiguracaoDao(
        inicial: ConfiguracaoEstacionamento? = null
    ) : ConfiguracaoEstacionamentoDao {
        private val fluxo = MutableStateFlow(inicial)
        override fun observar(): Flow<ConfiguracaoEstacionamento?> = fluxo
        override suspend fun buscar(): ConfiguracaoEstacionamento? = fluxo.value
        override suspend fun salvar(configuracao: ConfiguracaoEstacionamento) {
            fluxo.value = configuracao
        }
    }
}
