package com.parkingsolvers.smartpark.usuario.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.parkingsolvers.smartpark.auth.model.PerfilUsuario
import com.parkingsolvers.smartpark.servidor.model.Servidor

/**
 * Entidade Room de contas de acesso do sistema, com nível de permissão
 * (`PerfilUsuario`: ADMIN, PORTARIA ou SERVIDOR) e vínculo opcional a um `Servidor`.
 */
@Entity(
    tableName = "usuario",
    foreignKeys = [
        ForeignKey(
            entity = Servidor::class,
            parentColumns = ["id"],
            childColumns = ["servidor_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["login"], unique = true),
        Index(value = ["servidor_id"])
    ]
)
data class Usuario(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "nome")
    val nome: String,

    @ColumnInfo(name = "login")
    val login: String,

    @ColumnInfo(name = "senha_hash")
    val senhaHash: String,

    @ColumnInfo(name = "perfil")
    val perfil: PerfilUsuario,

    @ColumnInfo(name = "servidor_id")
    val servidorId: Long? = null,

    @ColumnInfo(name = "ativo")
    val ativo: Boolean = true
)
