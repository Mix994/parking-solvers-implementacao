package com.parkingsolvers.smartpark.servidor.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "servidor",
    indices = [
        Index(value = ["prontuario"], unique = true)
    ]
)
data class Servidor(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "nome")
    val nome: String,

    @ColumnInfo(name = "prontuario")
    val prontuario: String,

    @ColumnInfo(name = "email")
    val email: String,

    @ColumnInfo(name = "telefone")
    val telefone: String,

    @ColumnInfo(name = "setor")
    val setor: String,

    @ColumnInfo(name = "ativo")
    val ativo: Boolean = true
)
