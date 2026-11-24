package com.parkingsolvers.smartpark.usuario.repository

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.parkingsolvers.smartpark.auth.model.PerfilUsuario
import com.parkingsolvers.smartpark.usuario.model.Usuario
import kotlinx.coroutines.flow.Flow

@Dao
interface UsuarioDao {

    @Query("SELECT * FROM usuario ORDER BY ativo DESC, perfil ASC, nome ASC")
    fun observarTodos(): Flow<List<Usuario>>

    @Query("SELECT * FROM usuario WHERE perfil = :perfil ORDER BY ativo DESC, nome ASC")
    fun observarPorPerfil(perfil: PerfilUsuario): Flow<List<Usuario>>

    @Query("SELECT * FROM usuario WHERE id = :id LIMIT 1")
    suspend fun buscarPorId(id: Long): Usuario?

    @Query("SELECT * FROM usuario WHERE LOWER(login) = LOWER(:login) LIMIT 1")
    suspend fun buscarPorLogin(login: String): Usuario?

    @Query("SELECT * FROM usuario WHERE servidor_id = :servidorId LIMIT 1")
    suspend fun buscarPorServidorId(servidorId: Long): Usuario?

    @Query("SELECT COUNT(*) FROM usuario")
    suspend fun contar(): Int

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun inserir(usuario: Usuario): Long

    @Update
    suspend fun atualizar(usuario: Usuario)
}
