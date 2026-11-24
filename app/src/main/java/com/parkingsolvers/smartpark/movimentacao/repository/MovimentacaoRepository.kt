package com.parkingsolvers.smartpark.movimentacao.repository

import com.parkingsolvers.smartpark.movimentacao.model.RegistroMovimentacao
import kotlinx.coroutines.flow.Flow

class MovimentacaoRepository(private val dao: MovimentacaoDao) {

    fun observarTodas(): Flow<List<RegistroMovimentacao>> = dao.observarTodas()

    fun observarEmAberto(): Flow<List<RegistroMovimentacao>> = dao.observarEmAberto()

    suspend fun buscarPorId(id: Long): RegistroMovimentacao? = dao.buscarPorId(id)

    suspend fun buscarEmAbertoPorPlaca(placa: String): RegistroMovimentacao? =
        dao.buscarEmAbertoPorPlaca(placa)

    suspend fun salvar(registro: RegistroMovimentacao): Long = dao.inserir(registro)

    suspend fun atualizar(registro: RegistroMovimentacao) = dao.atualizar(registro)
}
