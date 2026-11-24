package com.parkingsolvers.smartpark.visitante.repository

import com.parkingsolvers.smartpark.visitante.model.Visitante
import kotlinx.coroutines.flow.Flow

class VisitanteRepository(private val dao: VisitanteDao) {

    fun observarTodos(): Flow<List<Visitante>> = dao.observarTodos()

    fun observarNoCampus(): Flow<List<Visitante>> = dao.observarNoCampus()

    suspend fun buscarPorId(id: Long): Visitante? = dao.buscarPorId(id)

    suspend fun buscarNoCampusPorPlaca(placa: String): Visitante? = dao.buscarNoCampusPorPlaca(placa)

    suspend fun contar(): Int = dao.contar()

    suspend fun salvar(visitante: Visitante): Long = dao.inserir(visitante)

    suspend fun atualizar(visitante: Visitante) = dao.atualizar(visitante)
}
