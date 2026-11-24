package com.parkingsolvers.smartpark.dashboard.service

import com.parkingsolvers.smartpark.movimentacao.model.RegistroMovimentacao
import com.parkingsolvers.smartpark.movimentacao.model.TipoVaga
import com.parkingsolvers.smartpark.movimentacao.model.Turno
import com.parkingsolvers.smartpark.servidor.model.Servidor
import com.parkingsolvers.smartpark.veiculo.model.TipoCadastroVeiculo
import com.parkingsolvers.smartpark.veiculo.model.Veiculo
import com.parkingsolvers.smartpark.visitante.model.Visitante
import org.junit.Assert.assertEquals
import org.junit.Test

class CalculadoraOcupacaoTest {

    @Test
    fun calculaOcupacaoPorTurnoVagasExclusivasEVisitantesExternos() {
        val calculadora = CalculadoraOcupacao(capacidadeTotal = 20, capacidadeExclusivas = 4)
        val agora = 10_000L

        val servidores = listOf(
            Servidor(1, "Ana", "BP1001", "ana@if.edu.br", "11999990001", "BCC", ativo = true),
            Servidor(2, "Carlos", "BP1002", "carlos@if.edu.br", "11999990002", "ADS", ativo = false)
        )
        val veiculos = listOf(
            Veiculo(1, 1, "ABC1D23", "Civic", "Prata", TipoCadastroVeiculo.PERMANENTE, ativo = true),
            Veiculo(2, 1, "TMP1A23", "Argo", "Azul", TipoCadastroVeiculo.TEMPORARIO, 5_000L, 15_000L, ativo = true)
        )
        val movimentacoes = listOf(
            RegistroMovimentacao(1, 1, "ABC1D23", true, 8_000L, null, Turno.MANHA, TipoVaga.COMUM),
            RegistroMovimentacao(2, 2, "TMP1A23", true, 9_000L, null, Turno.TARDE, TipoVaga.EXCLUSIVA_PCD),
            RegistroMovimentacao(3, 1, "ABC1D23", true, 1_000L, 3_000L, Turno.MANHA, TipoVaga.COMUM)
        )
        val visitantes = listOf(
            Visitante(
                id = 1,
                nome = "Prof. Convidado",
                documento = "123.456.789-00",
                instituicaoOrigem = "IFSP Guarulhos",
                telefone = "11999990000",
                placa = "IFG8B24",
                modeloVeiculo = "Creta",
                corVeiculo = "Azul",
                motivoVisita = "Banca",
                setorDestino = "Auditório",
                tipoVaga = TipoVaga.EXCLUSIVA_OFICIAL,
                turno = Turno.TARDE,
                registradoPor = "Portaria",
                dataHoraEntradaMillis = 9_500L,
                dataHoraSaidaMillis = null
            )
        )

        val resultado = calculadora.calcular(servidores, veiculos, movimentacoes, agora, visitantes = visitantes)

        assertEquals(3, resultado.veiculosNoCampusAgora)
        assertEquals(1, resultado.visitantesNoCampusAgora)
        assertEquals(2, resultado.vagasExclusivasOcupadasAgora)
        assertEquals(0.15f, resultado.percentualOcupacaoAtual, 0.001f)
        assertEquals(0.50f, resultado.percentualExclusivasAtual, 0.001f)
        assertEquals(1, resultado.totalServidoresAtivos)
        assertEquals(2, resultado.totalVeiculosAtivos)
        assertEquals(1, resultado.totalTemporariosVigentes)
        assertEquals(4, resultado.totalEntradasPeriodo)
        assertEquals(1, resultado.totalVisitantesPeriodo)

        val manha = resultado.ocupacaoPorTurno.first { it.turno == Turno.MANHA }
        assertEquals(2, manha.totalEntradas)
        assertEquals(1, manha.atualmenteEstacionados)

        val tarde = resultado.ocupacaoPorTurno.first { it.turno == Turno.TARDE }
        assertEquals(2, tarde.totalEntradas)
        assertEquals(2, tarde.atualmenteEstacionados)
    }

    @Test
    fun filtraMovimentacoesPorInicioPeriodoMantendoVeiculosNoPatioAgora() {
        val calculadora = CalculadoraOcupacao(capacidadeTotal = 10, capacidadeExclusivas = 2)
        val umDia = 24L * 60L * 60L * 1000L
        val agora = 10 * umDia

        val movimentacoes = listOf(
            RegistroMovimentacao(1, 1, "ABC1D23", true, agora - 1_000L, null, Turno.MANHA, TipoVaga.COMUM),
            RegistroMovimentacao(2, 1, "ABC1D23", true, agora - (5 * umDia), agora - (5 * umDia) + 1_000L, Turno.TARDE, TipoVaga.COMUM)
        )

        val resultadoHoje = calculadora.calcular(
            servidores = emptyList(),
            veiculos = emptyList(),
            movimentacoes = movimentacoes,
            agoraMillis = agora,
            inicioPeriodoMillis = agora - umDia
        )

        assertEquals(1, resultadoHoje.veiculosNoCampusAgora)
        assertEquals(1, resultadoHoje.totalEntradasPeriodo)
        val tarde = resultadoHoje.ocupacaoPorTurno.first { it.turno == Turno.TARDE }
        assertEquals(0, tarde.totalEntradas)
    }
}
