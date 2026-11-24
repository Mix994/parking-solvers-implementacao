package com.parkingsolvers.smartpark.auth.service

import com.parkingsolvers.smartpark.compartilhado.util.RelogioSistema
import com.parkingsolvers.smartpark.compartilhado.util.RelogioSistemaReal
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

/**
 * Proteção contra força bruta no login (análogo ao `LoginAttemptService` do CarToon):
 * bloqueia a conta por 15 minutos após 5 tentativas consecutivas com erro.
 */
class LoginAttemptService(
    private val relogio: RelogioSistema = RelogioSistemaReal
) {

    private val tentativas = ConcurrentHashMap<String, TentativaInfo>()

    fun estaBloqueado(identificador: String?): Boolean {
        if (identificador.isNullOrBlank()) return false
        val chave = normalizar(identificador)
        val info = tentativas[chave] ?: return false
        val ate = info.bloqueadoAteMillis
        if (ate != null) {
            return if (relogio.agoraMillis() < ate) {
                true
            } else {
                tentativas.remove(chave)
                false
            }
        }
        return false
    }

    fun registrarFalha(identificador: String?) {
        if (identificador.isNullOrBlank()) return
        evitarExcessoEmMemoria()
        val chave = normalizar(identificador)
        tentativas.compute(chave) { _, existente ->
            val agora = relogio.agoraMillis()
            val info = if (existente == null || (existente.bloqueadoAteMillis != null && agora >= existente.bloqueadoAteMillis!!)) {
                TentativaInfo()
            } else {
                existente
            }
            val contagem = info.contagem.incrementAndGet()
            if (contagem >= MAX_TENTATIVAS) {
                info.bloqueadoAteMillis = agora + TEMPO_BLOQUEIO_MILLIS
            }
            info
        }
    }

    fun registrarSucesso(identificador: String?) {
        if (identificador.isNullOrBlank()) return
        tentativas.remove(normalizar(identificador))
    }

    fun getTentativasFalhas(identificador: String?): Int {
        if (identificador.isNullOrBlank()) return 0
        return tentativas[normalizar(identificador)]?.contagem?.get() ?: 0
    }

    fun resetar(identificador: String?) {
        if (identificador != null) {
            tentativas.remove(normalizar(identificador))
        }
    }

    private fun evitarExcessoEmMemoria() {
        if (tentativas.size >= MAX_ENTRIES) {
            val agora = relogio.agoraMillis()
            tentativas.entries.removeIf { entry ->
                val ate = entry.value.bloqueadoAteMillis
                ate == null || agora >= ate
            }
        }
    }

    private fun normalizar(identificador: String): String = identificador.trim().lowercase()

    private class TentativaInfo {
        val contagem = AtomicInteger(0)
        @Volatile
        var bloqueadoAteMillis: Long? = null
    }

    companion object {
        const val MAX_TENTATIVAS = 5
        const val TEMPO_BLOQUEIO_MILLIS = 15L * 60L * 1000L
        const val MAX_ENTRIES = 10_000
    }
}
