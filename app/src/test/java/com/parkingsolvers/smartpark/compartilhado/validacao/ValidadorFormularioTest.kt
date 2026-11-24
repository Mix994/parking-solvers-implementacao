package com.parkingsolvers.smartpark.compartilhado.validacao

import com.parkingsolvers.smartpark.compartilhado.exceptions.ValidacaoException
import com.parkingsolvers.smartpark.compartilhado.util.Formatadores
import com.parkingsolvers.smartpark.compartilhado.util.TurnoUtils
import com.parkingsolvers.smartpark.movimentacao.model.Turno
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidadorFormularioTest {

    @Test
    fun validaEmailTelefoneDocumentoESenhasCoincidentes() {
        assertTrue(ValidadorFormulario.validarEmail("docente@ifsp.edu.br"))
        assertFalse(ValidadorFormulario.validarEmail("email-invalido"))

        assertTrue(ValidadorFormulario.validarTelefone("(11) 98765-4321"))
        assertTrue(ValidadorFormulario.validarTelefone("11987654321"))
        assertFalse(ValidadorFormulario.validarTelefone("123"))

        assertTrue(ValidadorFormulario.validarDocumento("321.654.987-00"))
        assertTrue(ValidadorFormulario.validarDocumento("45.890.123-4"))
        assertFalse(ValidadorFormulario.validarDocumento("123"))

        assertTrue(ValidadorFormulario.senhasCoincidem("segredo123", "segredo123"))
        assertFalse(ValidadorFormulario.senhasCoincidem("segredo123", "outro"))
        assertFalse(ValidadorFormulario.senhasCoincidem("", ""))
    }

    @Test
    fun validaPeriodoTemporario() {
        assertNotNull(ValidadorFormulario.validarPeriodoTemporario(null, 1000L))
        assertNotNull(ValidadorFormulario.validarPeriodoTemporario(2000L, 1000L))
        assertNull(ValidadorFormulario.validarPeriodoTemporario(1000L, 2000L))
    }

    @Test(expected = ValidacaoException::class)
    fun lancaValidacaoExceptionQuandoHaErros() {
        ValidadorFormulario.lancarSeHouverErros(mapOf("placa" to "Placa inválida"))
    }

    @Test
    fun formataPlacaAntigaEMercosulDeterminandoTurnoPorHora() {
        assertEquals("ABC-1234", Formatadores.formatarPlaca("abc1234"))
        assertEquals("ABC1D23", Formatadores.formatarPlaca("abc-1d23"))

        assertEquals(Turno.MANHA, TurnoUtils.determinarTurnoPorHora(8))
        assertEquals(Turno.TARDE, TurnoUtils.determinarTurnoPorHora(14))
        assertEquals(Turno.NOITE, TurnoUtils.determinarTurnoPorHora(20))
    }
}
