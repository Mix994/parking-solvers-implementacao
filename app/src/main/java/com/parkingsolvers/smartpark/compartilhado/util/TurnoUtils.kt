package com.parkingsolvers.smartpark.compartilhado.util

import com.parkingsolvers.smartpark.movimentacao.model.Turno
import java.util.Calendar

object TurnoUtils {

    fun determinarTurno(timestampMillis: Long): Turno {
        val calendar = Calendar.getInstance().apply { timeInMillis = timestampMillis }
        return determinarTurnoPorHora(calendar.get(Calendar.HOUR_OF_DAY))
    }

    fun determinarTurnoPorHora(hora24: Int): Turno = when (hora24) {
        in 6..11 -> Turno.MANHA
        in 12..17 -> Turno.TARDE
        else -> Turno.NOITE
    }
}
