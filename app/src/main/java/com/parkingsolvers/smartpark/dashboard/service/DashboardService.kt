package com.parkingsolvers.smartpark.dashboard.service

import com.parkingsolvers.smartpark.dashboard.dto.response.DashboardOcupacaoResponse
import com.parkingsolvers.smartpark.dashboard.model.PeriodoDashboard
import com.parkingsolvers.smartpark.rede.SincronizadorDados
import com.parkingsolvers.smartpark.rede.SmartParkApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow

class DashboardService(
    private val api: SmartParkApi,
    private val sincronizador: SincronizadorDados
) {
    fun observarDashboard(periodo: Flow<PeriodoDashboard>): Flow<DashboardOcupacaoResponse> = flow {
        var ultimo: DashboardOcupacaoResponse? = null
        combine(periodo, sincronizador.versao) { periodoAtual, _ -> periodoAtual }
            .collect { periodoAtual ->
                val resultado = runCatching { api.obterDashboard(periodoAtual.name) }
                if (resultado.isSuccess) {
                    ultimo = resultado.getOrThrow()
                }
                ultimo?.let { emit(it) }
            }
    }
}
