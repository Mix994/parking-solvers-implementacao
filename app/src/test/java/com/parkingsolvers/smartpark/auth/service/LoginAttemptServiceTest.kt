package com.parkingsolvers.smartpark.auth.service

import com.parkingsolvers.smartpark.compartilhado.util.RelogioSistema
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Testes unitários de proteção contra força bruta (análogo ao `LoginAttemptServiceTest` do CarToon).
 */
class LoginAttemptServiceTest {

    @Test
    fun estadoInicialNaoDeveEstarBloqueado() {
        val service = LoginAttemptService()
        assertFalse(service.estaBloqueado("usuario@campus.edu.br"))
        assertEquals(0, service.getTentativasFalhas("usuario@campus.edu.br"))
    }

    @Test
    fun registraTentativasFalhasSemBloquearAntesDoLimite() {
        val service = LoginAttemptService()
        val login = "usuario@campus.edu.br"

        for (i in 1..4) {
            service.registrarFalha(login)
            assertEquals(i, service.getTentativasFalhas(login))
            assertFalse(service.estaBloqueado(login))
        }
    }

    @Test
    fun bloqueiaAoAtingirQuintaTentativaFalha() {
        val service = LoginAttemptService()
        val login = "usuario@campus.edu.br"

        repeat(5) { service.registrarFalha(login) }

        assertEquals(5, service.getTentativasFalhas(login))
        assertTrue(service.estaBloqueado(login))
    }

    @Test
    fun normalizaLoginDesconsiderandoMaiusculasEEspacos() {
        val service = LoginAttemptService()

        service.registrarFalha("  Usuario@Campus.EDU.BR ")
        service.registrarFalha("usuario@campus.edu.br")

        assertEquals(2, service.getTentativasFalhas("USUARIO@campus.edu.br"))
    }

    @Test
    fun limpaHistoricoAoRegistrarSucesso() {
        val service = LoginAttemptService()
        val login = "usuario@campus.edu.br"

        repeat(4) { service.registrarFalha(login) }
        assertEquals(4, service.getTentativasFalhas(login))

        service.registrarSucesso(login)

        assertEquals(0, service.getTentativasFalhas(login))
        assertFalse(service.estaBloqueado(login))
    }

    @Test
    fun desbloqueiaAutomaticamenteAposQuinzeMinutos() {
        var agora = 1_700_000_000_000L
        val relogioFalso = RelogioSistema { agora }
        val service = LoginAttemptService(relogioFalso)
        val login = "usuario@campus.edu.br"

        repeat(5) { service.registrarFalha(login) }
        assertTrue(service.estaBloqueado(login))

        // 14 minutos e 59 segundos depois -> ainda bloqueado
        agora += (14 * 60 + 59) * 1000L
        assertTrue(service.estaBloqueado(login))

        // Mais 2 segundos -> bloqueio expirou
        agora += 2_000L
        assertFalse(service.estaBloqueado(login))
    }

    @Test
    fun entradasNulasOuVaziasNaoGeramErro() {
        val service = LoginAttemptService()
        assertFalse(service.estaBloqueado(null))
        assertFalse(service.estaBloqueado("   "))
        service.registrarFalha(null)
        service.registrarFalha("   ")
        service.registrarSucesso(null)
        service.resetar(null)
        assertEquals(0, service.getTentativasFalhas(null))
    }
}
