package com.parkingsolvers.smartpark.auth.service

import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SenhaHasherTest {

    @Test
    fun geraHashesDistintosComSaltEValidaSenhaCorreta() {
        val hash1 = SenhaHasher.gerarHash("admin123")
        val hash2 = SenhaHasher.gerarHash("admin123")

        assertNotEquals(hash1, hash2)
        assertTrue(SenhaHasher.verificar("admin123", hash1))
        assertTrue(SenhaHasher.verificar("admin123", hash2))
        assertFalse(SenhaHasher.verificar("senhaErrada", hash1))
    }

    @Test
    fun rejeitaHashMalFormado() {
        assertFalse(SenhaHasher.verificar("123456", "hash_invalido_sem_salt"))
    }
}
