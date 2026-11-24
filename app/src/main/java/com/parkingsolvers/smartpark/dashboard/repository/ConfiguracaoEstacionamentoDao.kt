package com.parkingsolvers.smartpark.dashboard.repository

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.parkingsolvers.smartpark.dashboard.model.ConfiguracaoEstacionamento
import kotlinx.coroutines.flow.Flow

@Dao
interface ConfiguracaoEstacionamentoDao {

    @Query("SELECT * FROM configuracao_estacionamento WHERE id = 1 LIMIT 1")
    fun observar(): Flow<ConfiguracaoEstacionamento?>

    @Query("SELECT * FROM configuracao_estacionamento WHERE id = 1 LIMIT 1")
    suspend fun buscar(): ConfiguracaoEstacionamento?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun salvar(configuracao: ConfiguracaoEstacionamento)
}
