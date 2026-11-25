package com.parkingsolvers.smartpark.servidor.service

import com.parkingsolvers.smartpark.rede.ApiErrorMapper
import com.parkingsolvers.smartpark.rede.SincronizadorDados
import com.parkingsolvers.smartpark.rede.SmartParkApi
import com.parkingsolvers.smartpark.rede.fluxoRemoto
import com.parkingsolvers.smartpark.servidor.dto.request.ServidorRequest
import com.parkingsolvers.smartpark.servidor.dto.response.ServidorResponse
import kotlinx.coroutines.flow.Flow

class ServidorService(
    private val api: SmartParkApi,
    private val sincronizador: SincronizadorDados
) {
    fun observarTodos(): Flow<List<ServidorResponse>> =
        fluxoRemoto(sincronizador, emptyList()) { api.listarServidores() }

    suspend fun buscarPorId(id: Long): ServidorResponse =
        ApiErrorMapper.executar { api.buscarServidor(id) }

    suspend fun cadastrar(request: ServidorRequest): ServidorResponse =
        ApiErrorMapper.executar { api.cadastrarServidor(request) }
            .also { sincronizador.notificarMudanca() }

    suspend fun alternarAtivo(id: Long): ServidorResponse =
        ApiErrorMapper.executar { api.alternarAtivoServidor(id) }
            .also { sincronizador.notificarMudanca() }
}
