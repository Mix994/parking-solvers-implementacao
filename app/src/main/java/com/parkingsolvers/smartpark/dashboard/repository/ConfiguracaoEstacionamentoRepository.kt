package com.parkingsolvers.smartpark.dashboard.repository

import com.parkingsolvers.smartpark.dashboard.model.ConfiguracaoEstacionamento
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ConfiguracaoEstacionamentoRepository(
    private val dao: ConfiguracaoEstacionamentoDao
) {

    fun observar(): Flow<ConfiguracaoEstacionamento> =
        dao.observar().map { it ?: ConfiguracaoEstacionamento() }

    suspend fun buscarAtual(): ConfiguracaoEstacionamento =
        dao.buscar() ?: ConfiguracaoEstacionamento()

    suspend fun salvar(configuracao: ConfiguracaoEstacionamento) {
        dao.salvar(configuracao.copy(id = ConfiguracaoEstacionamento.ID_PADRAO))
    }
}
