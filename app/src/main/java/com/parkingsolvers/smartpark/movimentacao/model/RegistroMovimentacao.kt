package com.parkingsolvers.smartpark.movimentacao.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.parkingsolvers.smartpark.veiculo.model.Veiculo

@Entity(
    tableName = "registro_movimentacao",
    foreignKeys = [
        ForeignKey(
            entity = Veiculo::class,
            parentColumns = ["id"],
            childColumns = ["veiculo_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["veiculo_id"]),
        Index(value = ["placa_registrada"]),
        Index(value = ["data_hora_saida_millis"])
    ]
)
data class RegistroMovimentacao(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "veiculo_id")
    val veiculoId: Long? = null,

    @ColumnInfo(name = "placa_registrada")
    val placaRegistrada: String,

    @ColumnInfo(name = "autorizado")
    val autorizado: Boolean,

    @ColumnInfo(name = "data_hora_entrada_millis")
    val dataHoraEntradaMillis: Long,

    @ColumnInfo(name = "data_hora_saida_millis")
    val dataHoraSaidaMillis: Long? = null,

    @ColumnInfo(name = "turno")
    val turno: Turno,

    @ColumnInfo(name = "tipo_vaga")
    val tipoVaga: TipoVaga = TipoVaga.COMUM,

    @ColumnInfo(name = "observacao")
    val observacao: String = ""
) {
    val emAberto: Boolean
        get() = dataHoraSaidaMillis == null
}
