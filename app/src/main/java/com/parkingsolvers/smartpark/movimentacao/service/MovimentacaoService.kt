package com.parkingsolvers.smartpark.movimentacao.service

import com.parkingsolvers.smartpark.movimentacao.dto.request.EntradaRequest
import com.parkingsolvers.smartpark.movimentacao.dto.response.MovimentacaoResponse
import com.parkingsolvers.smartpark.rede.ApiErrorMapper
import com.parkingsolvers.smartpark.rede.SincronizadorDados
import com.parkingsolvers.smartpark.rede.SmartParkApi
import com.parkingsolvers.smartpark.rede.fluxoRemoto
import kotlinx.coroutines.flow.Flow

class MovimentacaoService(
    private val api: SmartParkApi,
    private val sincronizador: SincronizadorDados,
    private val onAcaoLocal: () -> Unit = {}
) {
    fun observarNoEstacionamento(): Flow<List<MovimentacaoResponse>> =
        fluxoRemoto(sincronizador, emptyList()) {
            api.listarNoEstacionamento().map { it.paraDominio() }
        }

    fun observarMinhasAbertas(): Flow<List<MovimentacaoResponse>> =
        fluxoRemoto(sincronizador, emptyList()) {
            api.listarMinhasMovimentacoesAbertas().map { it.paraDominio() }
        }

    fun observarHistorico(): Flow<List<MovimentacaoResponse>> =
        fluxoRemoto(sincronizador, emptyList()) {
            api.listarMovimentacoes().map { it.paraDominio() }
        }

    suspend fun registrarEntrada(request: EntradaRequest): MovimentacaoResponse {
        onAcaoLocal()
        return ApiErrorMapper.executar { api.registrarEntradaMovimentacao(request).paraDominio() }
            .also {
                onAcaoLocal()
                sincronizador.notificarMudanca()
            }
    }

    suspend fun registrarSaida(movimentacaoId: Long): MovimentacaoResponse {
        onAcaoLocal()
        return ApiErrorMapper.executar { api.registrarSaidaMovimentacao(movimentacaoId).paraDominio() }
            .also {
                onAcaoLocal()
                sincronizador.notificarMudanca()
            }
    }
}
