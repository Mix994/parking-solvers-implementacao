package com.parkingsolvers.smartpark.veiculo.service

import com.parkingsolvers.smartpark.compartilhado.validacao.ValidadorPlaca
import com.parkingsolvers.smartpark.rede.ApiErrorMapper
import com.parkingsolvers.smartpark.rede.SincronizadorDados
import com.parkingsolvers.smartpark.rede.SmartParkApi
import com.parkingsolvers.smartpark.rede.fluxoRemoto
import com.parkingsolvers.smartpark.rede.paraApi
import com.parkingsolvers.smartpark.veiculo.dto.request.VeiculoRequest
import com.parkingsolvers.smartpark.veiculo.dto.response.ResultadoConsultaPlaca
import com.parkingsolvers.smartpark.veiculo.dto.response.VeiculoResponse
import kotlinx.coroutines.flow.Flow

class VeiculoService(
    private val api: SmartParkApi,
    private val sincronizador: SincronizadorDados
) {
    fun observarTodos(): Flow<List<VeiculoResponse>> =
        fluxoRemoto(sincronizador, emptyList()) { api.listarVeiculos().map { it.paraDominio() } }

    fun observarPorServidor(servidorId: Long): Flow<List<VeiculoResponse>> =
        fluxoRemoto(sincronizador, emptyList()) {
            api.listarVeiculosPorServidor(servidorId).map { it.paraDominio() }
        }

    suspend fun cadastrar(request: VeiculoRequest): VeiculoResponse =
        ApiErrorMapper.executar { api.cadastrarVeiculo(request.paraApi()).paraDominio() }
            .also { sincronizador.notificarMudanca() }

    suspend fun alternarAtivo(id: Long): VeiculoResponse =
        ApiErrorMapper.executar { api.alternarAtivoVeiculo(id).paraDominio() }
            .also { sincronizador.notificarMudanca() }

    suspend fun consultarPlaca(placaEntrada: String): ResultadoConsultaPlaca {
        val placaNormalizada = ValidadorPlaca.validarENormalizar(placaEntrada)
        return ApiErrorMapper.executar { api.consultarPlaca(placaNormalizada).paraDominio() }
    }
}
