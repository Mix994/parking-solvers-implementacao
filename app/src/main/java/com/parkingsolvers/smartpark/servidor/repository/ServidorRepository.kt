package com.parkingsolvers.smartpark.servidor.repository

import com.parkingsolvers.smartpark.servidor.model.Servidor
import kotlinx.coroutines.flow.Flow

class ServidorRepository(private val dao: ServidorDao) {

    fun observarTodos(): Flow<List<Servidor>> = dao.observarTodos()

    suspend fun listarTodos(): List<Servidor> = dao.listarTodos()

    suspend fun buscarPorId(id: Long): Servidor? = dao.buscarPorId(id)

    suspend fun buscarPorProntuario(prontuario: String): Servidor? = dao.buscarPorProntuario(prontuario)

    suspend fun contar(): Int = dao.contar()

    suspend fun salvar(servidor: Servidor): Long = dao.inserir(servidor)

    suspend fun atualizar(servidor: Servidor) = dao.atualizar(servidor)
}
