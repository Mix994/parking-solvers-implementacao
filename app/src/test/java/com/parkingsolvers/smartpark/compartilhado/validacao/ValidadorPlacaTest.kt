package com.parkingsolvers.smartpark.compartilhado.validacao

import com.parkingsolvers.smartpark.compartilhado.exceptions.PlacaInvalidaException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidadorPlacaTest {

    @Test
    fun aceitaPlacaAntigaEMercosul() {
        assertTrue(ValidadorPlaca.ehValida("ABC-1234"))
        assertTrue(ValidadorPlaca.ehValida("abc1234"))
        assertTrue(ValidadorPlaca.ehValida("ABC1D23"))
        assertTrue(ValidadorPlaca.ehValida("bra-2e19"))
    }

    @Test
    fun normalizaRemovendoHifenEConvertendoParaMaiusculas() {
        assertEquals("ABC1234", ValidadorPlaca.validarENormalizar(" abc-1234 "))
        assertEquals("ABC1D23", ValidadorPlaca.validarENormalizar("abc1d23"))
    }

    @Test
    fun rejeitaPlacasInvalidas() {
        assertFalse(ValidadorPlaca.ehValida(""))
        assertFalse(ValidadorPlaca.ehValida("1234567"))
        assertFalse(ValidadorPlaca.ehValida("AB1234"))
    }

    @Test(expected = PlacaInvalidaException::class)
    fun lancaExcecaoQuandoPlacaInvalida() {
        ValidadorPlaca.validarENormalizar("INVALIDA")
    }
}
