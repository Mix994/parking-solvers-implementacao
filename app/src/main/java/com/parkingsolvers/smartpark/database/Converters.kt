package com.parkingsolvers.smartpark.database

import androidx.room.TypeConverter
import com.parkingsolvers.smartpark.auth.model.PerfilUsuario
import com.parkingsolvers.smartpark.movimentacao.model.TipoVaga
import com.parkingsolvers.smartpark.movimentacao.model.Turno
import com.parkingsolvers.smartpark.veiculo.model.TipoCadastroVeiculo

class Converters {

    @TypeConverter
    fun fromPerfilUsuario(value: PerfilUsuario): String = value.name

    @TypeConverter
    fun toPerfilUsuario(value: String): PerfilUsuario = PerfilUsuario.valueOf(value)

    @TypeConverter
    fun fromTipoCadastro(value: TipoCadastroVeiculo): String = value.name

    @TypeConverter
    fun toTipoCadastro(value: String): TipoCadastroVeiculo = TipoCadastroVeiculo.valueOf(value)

    @TypeConverter
    fun fromTurno(value: Turno): String = value.name

    @TypeConverter
    fun toTurno(value: String): Turno = Turno.valueOf(value)

    @TypeConverter
    fun fromTipoVaga(value: TipoVaga): String = value.name

    @TypeConverter
    fun toTipoVaga(value: String): TipoVaga = TipoVaga.valueOf(value)
}
