package com.parkingsolvers.smartpark.veiculo.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.parkingsolvers.smartpark.servidor.model.Servidor

@Entity(
    tableName = "veiculo",
    foreignKeys = [
        ForeignKey(
            entity = Servidor::class,
            parentColumns = ["id"],
            childColumns = ["servidor_id"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index(value = ["placa"], unique = true),
        Index(value = ["servidor_id"])
    ]
)
data class Veiculo(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "servidor_id")
    val servidorId: Long,

    @ColumnInfo(name = "placa")
    val placa: String,

    @ColumnInfo(name = "modelo")
    val modelo: String,

    @ColumnInfo(name = "cor")
    val cor: String,

    @ColumnInfo(name = "tipo_cadastro")
    val tipoCadastro: TipoCadastroVeiculo = TipoCadastroVeiculo.PERMANENTE,

    @ColumnInfo(name = "validade_inicio_millis")
    val validadeInicioMillis: Long? = null,

    @ColumnInfo(name = "validade_fim_millis")
    val validadeFimMillis: Long? = null,

    @ColumnInfo(name = "ativo")
    val ativo: Boolean = true
) {
    fun estaDentroDaValidade(agoraMillis: Long): Boolean {
        if (tipoCadastro == TipoCadastroVeiculo.PERMANENTE) return true
        val inicio = validadeInicioMillis ?: return false
        val fim = validadeFimMillis ?: return false
        return agoraMillis in inicio..fim
    }
}
