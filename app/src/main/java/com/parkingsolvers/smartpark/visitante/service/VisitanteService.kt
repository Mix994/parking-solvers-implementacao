package com.parkingsolvers.smartpark.visitante.service

import com.parkingsolvers.smartpark.rede.ApiErrorMapper
import com.parkingsolvers.smartpark.rede.SincronizadorDados
import com.parkingsolvers.smartpark.rede.SmartParkApi
import com.parkingsolvers.smartpark.rede.fluxoRemoto
import com.parkingsolvers.smartpark.visitante.dto.request.VisitanteRequest
import com.parkingsolvers.smartpark.visitante.dto.response.VisitanteResponse
import kotlinx.coroutines.flow.Flow

class VisitanteService(
    private val api: SmartParkApi,
    private val sincronizador: SincronizadorDados,
    private val onAcaoLocal: () -> Unit = {}
) {
    fun observarNoCampus(): Flow<List<VisitanteResponse>> =
        fluxoRemoto(sincronizador, emptyList()) {
            api.listarVisitantesNoCampus().map { it.paraDominio() }
        }

    fun observarHistorico(): Flow<List<VisitanteResponse>> =
        fluxoRemoto(sincronizador, emptyList()) {
            api.listarVisitantes().map { it.paraDominio() }
        }

    suspend fun registrarEntrada(
        request: VisitanteRequest,
        @Suppress("UNUSED_PARAMETER") registradoPor: String
    ): VisitanteResponse {
        onAcaoLocal()
        return ApiErrorMapper.executar { api.registrarEntradaVisitante(request).paraDominio() }
            .also {
                onAcaoLocal()
                sincronizador.notificarMudanca()
            }
    }

    suspend fun registrarSaida(visitanteId: Long): VisitanteResponse {
        onAcaoLocal()
        return ApiErrorMapper.executar { api.registrarSaidaVisitante(visitanteId).paraDominio() }
            .also {
                onAcaoLocal()
                sincronizador.notificarMudanca()
            }
    }
}
