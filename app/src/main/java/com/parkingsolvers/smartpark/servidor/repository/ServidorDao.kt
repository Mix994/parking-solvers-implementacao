package com.parkingsolvers.smartpark.servidor.repository

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.parkingsolvers.smartpark.servidor.model.Servidor
import kotlinx.coroutines.flow.Flow

@Dao
interface ServidorDao {

    @Query("SELECT * FROM servidor ORDER BY ativo DESC, nome ASC")
    fun observarTodos(): Flow<List<Servidor>>

    @Query("SELECT * FROM servidor ORDER BY nome ASC")
    suspend fun listarTodos(): List<Servidor>

    @Query("SELECT * FROM servidor WHERE id = :id LIMIT 1")
    suspend fun buscarPorId(id: Long): Servidor?

    @Query("SELECT * FROM servidor WHERE UPPER(prontuario) = UPPER(:prontuario) LIMIT 1")
    suspend fun buscarPorProntuario(prontuario: String): Servidor?

    @Query("SELECT COUNT(*) FROM servidor")
    suspend fun contar(): Int

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun inserir(servidor: Servidor): Long

    @Update
    suspend fun atualizar(servidor: Servidor)
}
