package com.parkingsolvers.smartpark.movimentacao.repository

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.parkingsolvers.smartpark.movimentacao.model.RegistroMovimentacao
import kotlinx.coroutines.flow.Flow

@Dao
interface MovimentacaoDao {

    @Query("SELECT * FROM registro_movimentacao ORDER BY data_hora_entrada_millis DESC")
    fun observarTodas(): Flow<List<RegistroMovimentacao>>

    @Query("SELECT * FROM registro_movimentacao WHERE data_hora_saida_millis IS NULL ORDER BY data_hora_entrada_millis DESC")
    fun observarEmAberto(): Flow<List<RegistroMovimentacao>>

    @Query("SELECT * FROM registro_movimentacao WHERE id = :id LIMIT 1")
    suspend fun buscarPorId(id: Long): RegistroMovimentacao?

    @Query(
        "SELECT * FROM registro_movimentacao " +
            "WHERE UPPER(placa_registrada) = UPPER(:placa) AND data_hora_saida_millis IS NULL LIMIT 1"
    )
    suspend fun buscarEmAbertoPorPlaca(placa: String): RegistroMovimentacao?

    @Query("SELECT COUNT(*) FROM registro_movimentacao WHERE veiculo_id = :veiculoId AND data_hora_saida_millis IS NULL")
    suspend fun contarEmAbertoPorVeiculo(veiculoId: Long): Int

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun inserir(registro: RegistroMovimentacao): Long

    @Update
    suspend fun atualizar(registro: RegistroMovimentacao)
}
