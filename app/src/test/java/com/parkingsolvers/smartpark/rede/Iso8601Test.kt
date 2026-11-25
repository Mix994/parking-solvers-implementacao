package com.parkingsolvers.smartpark.rede

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class Iso8601Test {

    @Test
    fun `converte iso utc com e sem fracoes para millis e volta`() {
        val esperado = 1773154800000L // 2026-03-10T15:00:00Z
        assertEquals(esperado, Iso8601.paraMillis("2026-03-10T15:00:00Z"))
        assertEquals(esperado + 123L, Iso8601.paraMillis("2026-03-10T15:00:00.123456Z"))
        assertEquals("2026-03-10T15:00:00.000Z", Iso8601.deMillis(esperado))
    }

    @Test
    fun `converte offset de fuso e lida com nulos`() {
        val utc = Iso8601.paraMillis("2026-03-10T15:00:00Z")
        val sp = Iso8601.paraMillis("2026-03-10T12:00:00-03:00")
        assertEquals(utc, sp)
        assertNull(Iso8601.paraMillisOuNulo(null))
        assertNull(Iso8601.deMillisOuNulo(null))
    }
}
