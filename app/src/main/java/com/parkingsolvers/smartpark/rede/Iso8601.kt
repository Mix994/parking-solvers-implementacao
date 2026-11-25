package com.parkingsolvers.smartpark.rede

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Converte timestamps ISO-8601 UTC (retornados pela API Spring Boot como Instant)
 * para epoch millis (usados nas telas Compose do app) e vice-versa, sem exigir API 26+.
 */
object Iso8601 {

    private val UTC: TimeZone = TimeZone.getTimeZone("UTC")
    private val PADRAO = Regex(
        "^(\\d{4})-(\\d{2})-(\\d{2})T(\\d{2}):(\\d{2}):(\\d{2})(?:\\.(\\d{1,9}))?(Z|[+-]\\d{2}:?\\d{2})$"
    )

    fun paraMillis(iso: String): Long {
        val m = PADRAO.matchEntire(iso.trim())
            ?: throw IllegalArgumentException("Data ISO-8601 inválida: $iso")
        val ano = m.groupValues[1].toInt()
        val mes = m.groupValues[2].toInt() - 1
        val dia = m.groupValues[3].toInt()
        val hora = m.groupValues[4].toInt()
        val min = m.groupValues[5].toInt()
        val seg = m.groupValues[6].toInt()
        val fracao = m.groupValues[7]
        val millis = if (fracao.isEmpty()) 0 else fracao.padEnd(3, '0').substring(0, 3).toInt()

        val tzStr = m.groupValues[8]
        val tz = if (tzStr == "Z") UTC else TimeZone.getTimeZone("GMT$tzStr")

        val cal = Calendar.getInstance(tz).apply {
            clear()
            set(Calendar.YEAR, ano)
            set(Calendar.MONTH, mes)
            set(Calendar.DAY_OF_MONTH, dia)
            set(Calendar.HOUR_OF_DAY, hora)
            set(Calendar.MINUTE, min)
            set(Calendar.SECOND, seg)
            set(Calendar.MILLISECOND, millis)
        }
        return cal.timeInMillis
    }

    fun paraMillisOuNulo(iso: String?): Long? = iso?.takeIf { it.isNotBlank() }?.let(::paraMillis)

    fun deMillis(millis: Long): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
            timeZone = UTC
        }
        return sdf.format(Date(millis))
    }

    fun deMillisOuNulo(millis: Long?): String? = millis?.let(::deMillis)
}
