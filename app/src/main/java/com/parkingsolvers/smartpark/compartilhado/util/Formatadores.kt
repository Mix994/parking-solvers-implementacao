package com.parkingsolvers.smartpark.compartilhado.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object Formatadores {

    private val localeBr = Locale.Builder().setLanguage("pt").setRegion("BR").build()

    fun formatarPlaca(placaNormalizada: String): String {
        val limpa = placaNormalizada.trim().uppercase().replace("-", "")
        if (limpa.length != 7) return limpa
        val ehAntiga = limpa[4].isDigit()
        return if (ehAntiga) {
            "${limpa.substring(0, 3)}-${limpa.substring(3)}"
        } else {
            limpa
        }
    }

    fun formatarDataHora(millis: Long): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", localeBr)
        return sdf.format(Date(millis))
    }

    fun formatarData(millis: Long): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy", localeBr)
        return sdf.format(Date(millis))
    }

    fun formatarHora(millis: Long): String {
        val sdf = SimpleDateFormat("HH:mm", localeBr)
        return sdf.format(Date(millis))
    }
}
