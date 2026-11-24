package com.parkingsolvers.smartpark.usuario.repository

import com.parkingsolvers.smartpark.auth.model.PerfilUsuario
import com.parkingsolvers.smartpark.usuario.model.Usuario
import kotlinx.coroutines.flow.Flow

class UsuarioRepository(private val dao: UsuarioDao) {

    fun observarTodos(): Flow<List<Usuario>> = dao.observarTodos()

    fun observarPorPerfil(perfil: PerfilUsuario): Flow<List<Usuario>> = dao.observarPorPerfil(perfil)

    suspend fun buscarPorId(id: Long): Usuario? = dao.buscarPorId(id)

    suspend fun buscarPorLogin(login: String): Usuario? = dao.buscarPorLogin(login)

    suspend fun buscarPorServidorId(servidorId: Long): Usuario? = dao.buscarPorServidorId(servidorId)

    suspend fun contar(): Int = dao.contar()

    suspend fun salvar(usuario: Usuario): Long = dao.inserir(usuario)

    suspend fun atualizar(usuario: Usuario) = dao.atualizar(usuario)
}
