package com.parkingsolvers.smartpark.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.parkingsolvers.smartpark.dashboard.model.ConfiguracaoEstacionamento
import com.parkingsolvers.smartpark.dashboard.repository.ConfiguracaoEstacionamentoDao
import com.parkingsolvers.smartpark.movimentacao.model.RegistroMovimentacao
import com.parkingsolvers.smartpark.movimentacao.repository.MovimentacaoDao
import com.parkingsolvers.smartpark.servidor.model.Servidor
import com.parkingsolvers.smartpark.servidor.repository.ServidorDao
import com.parkingsolvers.smartpark.usuario.model.Usuario
import com.parkingsolvers.smartpark.usuario.repository.UsuarioDao
import com.parkingsolvers.smartpark.veiculo.model.Veiculo
import com.parkingsolvers.smartpark.veiculo.repository.VeiculoDao
import com.parkingsolvers.smartpark.visitante.model.Visitante
import com.parkingsolvers.smartpark.visitante.repository.VisitanteDao

@Database(
    entities = [
        Usuario::class,
        Servidor::class,
        Veiculo::class,
        RegistroMovimentacao::class,
        Visitante::class,
        ConfiguracaoEstacionamento::class
    ],
    version = 4,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class SmartParkDatabase : RoomDatabase() {

    abstract fun usuarioDao(): UsuarioDao
    abstract fun servidorDao(): ServidorDao
    abstract fun veiculoDao(): VeiculoDao
    abstract fun movimentacaoDao(): MovimentacaoDao
    abstract fun visitanteDao(): VisitanteDao
    abstract fun configuracaoEstacionamentoDao(): ConfiguracaoEstacionamentoDao

    companion object {
        private const val DATABASE_NAME = "parking_solvers_smartpark.db"

        @Volatile
        private var instance: SmartParkDatabase? = null

        fun getInstance(context: Context): SmartParkDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    SmartParkDatabase::class.java,
                    DATABASE_NAME
                )
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                    .also { instance = it }
            }
        }
    }
}
