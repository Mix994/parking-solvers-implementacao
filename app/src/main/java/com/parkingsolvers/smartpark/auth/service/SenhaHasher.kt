package com.parkingsolvers.smartpark.auth.service

import java.security.MessageDigest
import java.security.SecureRandom

/**
 * Gera e valida hashes SHA-256 com salt aleatório para armazenar senhas no Room
 * sem guardar texto puro no banco de dados.
 */
object SenhaHasher {

    private val random = SecureRandom()

    fun gerarHash(senha: String): String {
        val salt = ByteArray(16)
        random.nextBytes(salt)
        val saltHex = salt.toHex()
        val digestHex = sha256Hex("$saltHex:$senha")
        return "$saltHex:$digestHex"
    }

    fun verificar(senhaDigitada: String, hashArmazenado: String): Boolean {
        val partes = hashArmazenado.split(":")
        if (partes.size != 2) return false
        val (saltHex, hashEsperado) = partes
        val hashCalculado = sha256Hex("$saltHex:$senhaDigitada")
        return MessageDigest.isEqual(
            hashCalculado.toByteArray(Charsets.UTF_8),
            hashEsperado.toByteArray(Charsets.UTF_8)
        )
    }

    private fun sha256Hex(valor: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
            .digest(valor.toByteArray(Charsets.UTF_8))
        return digest.toHex()
    }

    private fun ByteArray.toHex(): String =
        joinToString(separator = "") { "%02x".format(it) }
}
