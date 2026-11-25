package com.parkingsolvers.smartpark.dashboard.service

import com.parkingsolvers.smartpark.dashboard.model.ConfiguracaoEstacionamento
import com.parkingsolvers.smartpark.rede.ApiErrorMapper
import com.parkingsolvers.smartpark.rede.ConfiguracaoApiDto
import com.parkingsolvers.smartpark.rede.SincronizadorDados
import com.parkingsolvers.smartpark.rede.SmartParkApi
import com.parkingsolvers.smartpark.rede.fluxoRemoto
import kotlinx.coroutines.flow.Flow

class ConfiguracaoEstacionamentoService(
    private val api: SmartParkApi,
    private val sincronizador: SincronizadorDados
) {
    fun observar(): Flow<ConfiguracaoEstacionamento> =
        fluxoRemoto(sincronizador, ConfiguracaoEstacionamento()) {
            api.obterConfiguracao().paraDominio()
        }

    suspend fun buscarAtual(): ConfiguracaoEstacionamento =
        ApiErrorMapper.executar { api.obterConfiguracao().paraDominio() }

    suspend fun atualizarCapacidades(
        capacidadeTotal: Int,
        capacidadeExclusivas: Int,
        capacidadeCarregamentoEletrico: Int
    ): ConfiguracaoEstacionamento =
        ApiErrorMapper.executar {
            api.atualizarConfiguracao(
                ConfiguracaoApiDto(
                    capacidadeTotal = capacidadeTotal,
                    capacidadeExclusivas = capacidadeExclusivas,
                    capacidadeCarregamentoEletrico = capacidadeCarregamentoEletrico
                )
            ).paraDominio()
        }.also { sincronizador.notificarMudanca() }
}
