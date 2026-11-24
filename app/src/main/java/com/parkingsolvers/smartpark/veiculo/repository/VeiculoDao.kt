package com.parkingsolvers.smartpark.veiculo.repository

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.parkingsolvers.smartpark.veiculo.model.Veiculo
import kotlinx.coroutines.flow.Flow

@Dao
interface VeiculoDao {

    @Query("SELECT * FROM veiculo ORDER BY ativo DESC, placa ASC")
    fun observarTodos(): Flow<List<Veiculo>>

    @Query("SELECT * FROM veiculo WHERE servidor_id = :servidorId ORDER BY ativo DESC, placa ASC")
    fun observarPorServidor(servidorId: Long): Flow<List<Veiculo>>

    @Query("SELECT * FROM veiculo WHERE id = :id LIMIT 1")
    suspend fun buscarPorId(id: Long): Veiculo?

    @Query("SELECT * FROM veiculo WHERE UPPER(placa) = UPPER(:placa) LIMIT 1")
    suspend fun buscarPorPlaca(placa: String): Veiculo?

    @Query("SELECT * FROM veiculo WHERE servidor_id = :servidorId")
    suspend fun listarPorServidor(servidorId: Long): List<Veiculo>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun inserir(veiculo: Veiculo): Long

    @Update
    suspend fun atualizar(veiculo: Veiculo)
}
