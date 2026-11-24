package com.parkingsolvers.smartpark.servidor.service

import com.parkingsolvers.smartpark.auth.service.AuthServiceTest
import com.parkingsolvers.smartpark.compartilhado.exceptions.ConflitoException
import com.parkingsolvers.smartpark.compartilhado.exceptions.ValidacaoException
import com.parkingsolvers.smartpark.servidor.dto.request.ServidorRequest
import com.parkingsolvers.smartpark.servidor.repository.ServidorRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ServidorServiceTest {

    @Test
    fun cadastraEAlternaStatusDoServidor() = runBlocking {
        val service = ServidorService(ServidorRepository(AuthServiceTest.FakeServidorDao()))

        val salvo = service.cadastrar(
            ServidorRequest(
                nome = "Prof. Ana Beatriz",
                prontuario = "bp102401",
                email = "ANA@CAMPUS.EDU.BR",
                telefone = "(11) 98765-1101",
                setor = "Coordenação BCC"
            )
        )

        assertEquals("BP102401", salvo.prontuario)
        assertEquals("ana@campus.edu.br", salvo.email)
        assertTrue(salvo.ativo)

        val inativado = service.alternarAtivo(salvo.id)
        assertFalse(inativado.ativo)
    }

    @Test(expected = ConflitoException::class)
    fun impedeProntuarioDuplicado() = runBlocking {
        val service = ServidorService(ServidorRepository(AuthServiceTest.FakeServidorDao()))
        val request = ServidorRequest("Ana", "BP102401", "ana@campus.edu.br", "11987651101", "BCC")
        service.cadastrar(request)
        service.cadastrar(request.copy(nome = "Outra Pessoa"))
        Unit
    }

    @Test(expected = ValidacaoException::class)
    fun rejeitaCamposInvalidos() = runBlocking {
        val service = ServidorService(ServidorRepository(AuthServiceTest.FakeServidorDao()))
        service.cadastrar(ServidorRequest("", "BP", "invalido", "12", ""))
        Unit
    }
}
