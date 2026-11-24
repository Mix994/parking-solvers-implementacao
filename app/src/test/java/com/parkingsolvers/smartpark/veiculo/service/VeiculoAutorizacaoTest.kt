package com.parkingsolvers.smartpark.veiculo.service

import com.parkingsolvers.smartpark.veiculo.dto.response.StatusAutorizacao
import com.parkingsolvers.smartpark.veiculo.model.TipoCadastroVeiculo
import com.parkingsolvers.smartpark.veiculo.model.Veiculo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class VeiculoAutorizacaoTest {

    @Test
    fun veiculoPermanenteAtivoComServidorAtivoEhAutorizado() {
        val veiculo = Veiculo(
            id = 1,
            servidorId = 1,
            placa = "ABC1D23",
            modelo = "Civic",
            cor = "Prata",
            tipoCadastro = TipoCadastroVeiculo.PERMANENTE,
            ativo = true
        )

        val resultado = VeiculoService.avaliarAutorizacao(
            veiculo = veiculo,
            servidorAtivo = true,
            veiculoResponse = null,
            agoraMillis = 1_000_000L
        )

        assertTrue(resultado.autorizado)
        assertEquals(StatusAutorizacao.AUTORIZADO_PERMANENTE, resultado.status)
    }

    @Test
    fun veiculoTemporarioDentroDaValidadeEhAutorizado() {
        val veiculo = Veiculo(
            id = 2,
            servidorId = 1,
            placa = "TMP4F56",
            modelo = "Argo",
            cor = "Vermelho",
            tipoCadastro = TipoCadastroVeiculo.TEMPORARIO,
            validadeInicioMillis = 1_000L,
            validadeFimMillis = 5_000L,
            ativo = true
        )

        val resultado = VeiculoService.avaliarAutorizacao(
            veiculo = veiculo,
            servidorAtivo = true,
            veiculoResponse = null,
            agoraMillis = 3_000L
        )

        assertTrue(resultado.autorizado)
        assertEquals(StatusAutorizacao.AUTORIZADO_TEMPORARIO, resultado.status)
    }

    @Test
    fun veiculoTemporarioExpiradoEhBloqueado() {
        val veiculo = Veiculo(
            id = 3,
            servidorId = 1,
            placa = "EXP9988",
            modelo = "Gol",
            cor = "Cinza",
            tipoCadastro = TipoCadastroVeiculo.TEMPORARIO,
            validadeInicioMillis = 1_000L,
            validadeFimMillis = 5_000L,
            ativo = true
        )

        val resultado = VeiculoService.avaliarAutorizacao(
            veiculo = veiculo,
            servidorAtivo = true,
            veiculoResponse = null,
            agoraMillis = 9_000L
        )

        assertFalse(resultado.autorizado)
        assertEquals(StatusAutorizacao.TEMPORARIO_EXPIRADO, resultado.status)
    }

    @Test
    fun veiculoComServidorInativoEhBloqueado() {
        val veiculo = Veiculo(
            id = 4,
            servidorId = 1,
            placa = "ABC1234",
            modelo = "Corolla",
            cor = "Branco",
            tipoCadastro = TipoCadastroVeiculo.PERMANENTE,
            ativo = true
        )

        val resultado = VeiculoService.avaliarAutorizacao(
            veiculo = veiculo,
            servidorAtivo = false,
            veiculoResponse = null,
            agoraMillis = 1_000L
        )

        assertFalse(resultado.autorizado)
        assertEquals(StatusAutorizacao.SERVIDOR_INATIVO, resultado.status)
    }
}
