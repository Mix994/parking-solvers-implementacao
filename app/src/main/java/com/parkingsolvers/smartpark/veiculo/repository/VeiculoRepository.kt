package com.parkingsolvers.smartpark.veiculo.repository

import com.parkingsolvers.smartpark.veiculo.model.Veiculo
import kotlinx.coroutines.flow.Flow

class VeiculoRepository(private val dao: VeiculoDao) {

    fun observarTodos(): Flow<List<Veiculo>> = dao.observarTodos()

    fun observarPorServidor(servidorId: Long): Flow<List<Veiculo>> = dao.observarPorServidor(servidorId)

    suspend fun buscarPorId(id: Long): Veiculo? = dao.buscarPorId(id)

    suspend fun buscarPorPlaca(placa: String): Veiculo? = dao.buscarPorPlaca(placa)

    suspend fun salvar(veiculo: Veiculo): Long = dao.inserir(veiculo)

    suspend fun atualizar(veiculo: Veiculo) = dao.atualizar(veiculo)
}
