package com.parkingsolvers.smartpark.visitante.repository

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.parkingsolvers.smartpark.visitante.model.Visitante
import kotlinx.coroutines.flow.Flow

@Dao
interface VisitanteDao {

    @Query("SELECT * FROM visitante ORDER BY data_hora_entrada_millis DESC")
    fun observarTodos(): Flow<List<Visitante>>

    @Query("SELECT * FROM visitante WHERE data_hora_saida_millis IS NULL ORDER BY data_hora_entrada_millis DESC")
    fun observarNoCampus(): Flow<List<Visitante>>

    @Query("SELECT * FROM visitante WHERE id = :id LIMIT 1")
    suspend fun buscarPorId(id: Long): Visitante?

    @Query(
        "SELECT * FROM visitante " +
            "WHERE UPPER(placa) = UPPER(:placa) AND data_hora_saida_millis IS NULL LIMIT 1"
    )
    suspend fun buscarNoCampusPorPlaca(placa: String): Visitante?

    @Query("SELECT COUNT(*) FROM visitante")
    suspend fun contar(): Int

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun inserir(visitante: Visitante): Long

    @Update
    suspend fun atualizar(visitante: Visitante)
}
