package com.parkingsolvers.smartpark.dashboard.model

import java.util.Calendar

/**
 * Período usado pelo painel da administração para filtrar as movimentações.
 * [dias] = null significa "todo o histórico".
 */
enum class PeriodoDashboard(val label: String, val dias: Int?) {
    HOJE("Hoje", 1),
    ULTIMOS_7_DIAS("7 dias", 7),
    TUDO("Tudo", null);

    /**
     * Retorna o instante (meia-noite) a partir do qual as movimentações entram no cálculo,
     * ou null quando não há filtro.
     */
    fun inicioMillis(agoraMillis: Long): Long? {
        val quantidadeDias = dias ?: return null
        val calendario = Calendar.getInstance().apply {
            timeInMillis = agoraMillis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            add(Calendar.DAY_OF_YEAR, -(quantidadeDias - 1))
        }
        return calendario.timeInMillis
    }
}
