package com.parkingsolvers.smartpark.compartilhado.util

/**
 * Abstração de relógio injetável (análogo ao ClockConfig do CarToon, compatível com API 24+)
 * para permitir testes determinísticos de bloqueio de login, vigência temporária e turnos.
 */
fun interface RelogioSistema {
    fun agoraMillis(): Long
}

object RelogioSistemaReal : RelogioSistema {
    override fun agoraMillis(): Long = System.currentTimeMillis()
}
