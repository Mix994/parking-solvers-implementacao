package com.parkingsolvers.smartpark.visitante.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.parkingsolvers.smartpark.movimentacao.model.TipoVaga
import com.parkingsolvers.smartpark.movimentacao.model.Turno

/**
 * Entidade Room para registro de visitantes externos (outras unidades do IF,
 * empresas parceiras, fornecedores, palestrantes ou comunidade externa) que
 * estacionam no campus.
 */
@Entity(
    tableName = "visitante",
    indices = [
        Index(value = ["placa"]),
        Index(value = ["data_hora_saida_millis"])
    ]
)
data class Visitante(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "nome")
    val nome: String,

    @ColumnInfo(name = "documento")
    val documento: String,

    @ColumnInfo(name = "instituicao_origem")
    val instituicaoOrigem: String,

    @ColumnInfo(name = "telefone")
    val telefone: String,

    @ColumnInfo(name = "placa")
    val placa: String,

    @ColumnInfo(name = "modelo_veiculo")
    val modeloVeiculo: String,

    @ColumnInfo(name = "cor_veiculo")
    val corVeiculo: String,

    @ColumnInfo(name = "motivo_visita")
    val motivoVisita: String,

    @ColumnInfo(name = "setor_destino")
    val setorDestino: String,

    @ColumnInfo(name = "tipo_vaga")
    val tipoVaga: TipoVaga = TipoVaga.COMUM,

    @ColumnInfo(name = "turno")
    val turno: Turno,

    @ColumnInfo(name = "registrado_por")
    val registradoPor: String,

    @ColumnInfo(name = "data_hora_entrada_millis")
    val dataHoraEntradaMillis: Long,

    @ColumnInfo(name = "data_hora_saida_millis")
    val dataHoraSaidaMillis: Long? = null,

    @ColumnInfo(name = "observacao")
    val observacao: String = ""
) {
    val noCampus: Boolean
        get() = dataHoraSaidaMillis == null
}
