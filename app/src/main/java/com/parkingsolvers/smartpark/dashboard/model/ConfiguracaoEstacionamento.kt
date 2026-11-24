package com.parkingsolvers.smartpark.dashboard.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Configuração persistida da capacidade de vagas do campus.
 * Permite expandir facilmente a quantidade de vagas de carregamento elétrico
 * (padrão inicial: 2 vagas) e demais capacidades sem alterar o código do app.
 */
@Entity(tableName = "configuracao_estacionamento")
data class ConfiguracaoEstacionamento(
    @PrimaryKey
    val id: Int = ID_PADRAO,

    @ColumnInfo(name = "capacidade_total")
    val capacidadeTotal: Int = CAPACIDADE_TOTAL_PADRAO,

    @ColumnInfo(name = "capacidade_exclusivas")
    val capacidadeExclusivas: Int = CAPACIDADE_EXCLUSIVAS_PADRAO,

    @ColumnInfo(name = "capacidade_carregamento_eletrico")
    val capacidadeCarregamentoEletrico: Int = CAPACIDADE_ELETRICO_PADRAO
) {
    companion object {
        const val ID_PADRAO = 1
        const val CAPACIDADE_TOTAL_PADRAO = 60
        const val CAPACIDADE_EXCLUSIVAS_PADRAO = 10
        const val CAPACIDADE_ELETRICO_PADRAO = 2
    }
}
