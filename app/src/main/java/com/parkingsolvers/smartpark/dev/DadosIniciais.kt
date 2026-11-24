package com.parkingsolvers.smartpark.dev

import com.parkingsolvers.smartpark.auth.model.PerfilUsuario
import com.parkingsolvers.smartpark.auth.service.SenhaHasher
import com.parkingsolvers.smartpark.movimentacao.model.RegistroMovimentacao
import com.parkingsolvers.smartpark.movimentacao.model.TipoVaga
import com.parkingsolvers.smartpark.movimentacao.model.Turno
import com.parkingsolvers.smartpark.movimentacao.repository.MovimentacaoRepository
import com.parkingsolvers.smartpark.servidor.model.Servidor
import com.parkingsolvers.smartpark.servidor.repository.ServidorRepository
import com.parkingsolvers.smartpark.usuario.model.Usuario
import com.parkingsolvers.smartpark.usuario.repository.UsuarioRepository
import com.parkingsolvers.smartpark.veiculo.model.TipoCadastroVeiculo
import com.parkingsolvers.smartpark.veiculo.model.Veiculo
import com.parkingsolvers.smartpark.veiculo.repository.VeiculoRepository
import com.parkingsolvers.smartpark.visitante.model.Visitante
import com.parkingsolvers.smartpark.visitante.repository.VisitanteRepository

/**
 * Carga inicial de dados fictícios (servidores, veículos, movimentações,
 * visitantes externos e contas de acesso dos 3 níveis) no banco local Room.
 */
class DadosIniciais(
    private val usuarioRepository: UsuarioRepository,
    private val servidorRepository: ServidorRepository,
    private val veiculoRepository: VeiculoRepository,
    private val movimentacaoRepository: MovimentacaoRepository,
    private val visitanteRepository: VisitanteRepository
) {

    suspend fun popularSeVazio() {
        popularServidoresVeiculosEMovimentacoes()
        popularUsuariosIniciais()
        popularVisitantesIniciais()
    }

    private suspend fun popularServidoresVeiculosEMovimentacoes() {
        if (servidorRepository.contar() > 0) return

        val agora = System.currentTimeMillis()
        val umDia = 24L * 60L * 60L * 1000L
        val umaHora = 60L * 60L * 1000L

        val idAna = servidorRepository.salvar(
            Servidor(
                nome = "Prof.ª Ana Beatriz Souza",
                prontuario = "BP102401",
                email = "ana.souza@campus.edu.br",
                telefone = "(11) 98765-1101",
                setor = "Coordenação BCC"
            )
        )
        val idCarlos = servidorRepository.salvar(
            Servidor(
                nome = "Prof. Carlos Eduardo Lima",
                prontuario = "BP102402",
                email = "carlos.lima@campus.edu.br",
                telefone = "(11) 98765-2202",
                setor = "Departamento de Computação"
            )
        )
        val idFernanda = servidorRepository.salvar(
            Servidor(
                nome = "Fernanda Oliveira",
                prontuario = "BP102403",
                email = "fernanda.oliveira@campus.edu.br",
                telefone = "(11) 98765-3303",
                setor = "Secretaria Acadêmica"
            )
        )

        val veiculoAna = veiculoRepository.salvar(
            Veiculo(
                servidorId = idAna,
                placa = "ABC1D23",
                modelo = "Honda Civic",
                cor = "Prata",
                tipoCadastro = TipoCadastroVeiculo.PERMANENTE
            )
        )
        val veiculoCarlos = veiculoRepository.salvar(
            Veiculo(
                servidorId = idCarlos,
                placa = "BRA2E19",
                modelo = "Toyota Corolla",
                cor = "Branco",
                tipoCadastro = TipoCadastroVeiculo.PERMANENTE
            )
        )
        val veiculoTemporarioVigente = veiculoRepository.salvar(
            Veiculo(
                servidorId = idAna,
                placa = "TMP4F56",
                modelo = "Fiat Argo (Locadora)",
                cor = "Vermelho",
                tipoCadastro = TipoCadastroVeiculo.TEMPORARIO,
                validadeInicioMillis = agora - umDia,
                validadeFimMillis = agora + (3 * umDia)
            )
        )
        veiculoRepository.salvar(
            Veiculo(
                servidorId = idFernanda,
                placa = "EXP9988",
                modelo = "VW Gol (Familiar)",
                cor = "Cinza",
                tipoCadastro = TipoCadastroVeiculo.TEMPORARIO,
                validadeInicioMillis = agora - (5 * umDia),
                validadeFimMillis = agora - umDia
            )
        )

        movimentacaoRepository.salvar(
            RegistroMovimentacao(
                veiculoId = veiculoAna,
                placaRegistrada = "ABC1D23",
                autorizado = true,
                dataHoraEntradaMillis = agora - (3 * umaHora),
                dataHoraSaidaMillis = null,
                turno = Turno.MANHA,
                tipoVaga = TipoVaga.COMUM,
                observacao = "Entrada regular docente"
            )
        )
        movimentacaoRepository.salvar(
            RegistroMovimentacao(
                veiculoId = veiculoCarlos,
                placaRegistrada = "BRA2E19",
                autorizado = true,
                dataHoraEntradaMillis = agora - (2 * umaHora),
                dataHoraSaidaMillis = null,
                turno = Turno.TARDE,
                tipoVaga = TipoVaga.EXCLUSIVA_PCD,
                observacao = "Vaga exclusiva bloco A"
            )
        )
        movimentacaoRepository.salvar(
            RegistroMovimentacao(
                veiculoId = veiculoTemporarioVigente,
                placaRegistrada = "TMP4F56",
                autorizado = true,
                dataHoraEntradaMillis = agora - (5 * umaHora),
                dataHoraSaidaMillis = agora - (4 * umaHora),
                turno = Turno.MANHA,
                tipoVaga = TipoVaga.COMUM,
                observacao = "Troca temporária validada"
            )
        )
        movimentacaoRepository.salvar(
            RegistroMovimentacao(
                veiculoId = veiculoAna,
                placaRegistrada = "ABC1D23",
                autorizado = true,
                dataHoraEntradaMillis = agora - (2 * umDia) - (4 * umaHora),
                dataHoraSaidaMillis = agora - (2 * umDia) - umaHora,
                turno = Turno.TARDE,
                tipoVaga = TipoVaga.COMUM,
                observacao = "Aula período vespertino"
            )
        )
        movimentacaoRepository.salvar(
            RegistroMovimentacao(
                veiculoId = veiculoCarlos,
                placaRegistrada = "BRA2E19",
                autorizado = true,
                dataHoraEntradaMillis = agora - (4 * umDia) - (2 * umaHora),
                dataHoraSaidaMillis = agora - (4 * umDia) - umaHora,
                turno = Turno.NOITE,
                tipoVaga = TipoVaga.EXCLUSIVA_PCD,
                observacao = "Reunião colegiado noturno"
            )
        )
    }

    private suspend fun popularUsuariosIniciais() {
        if (usuarioRepository.contar() > 0) return

        usuarioRepository.salvar(
            Usuario(
                nome = "Coordenação do Campus",
                login = LOGIN_DEMO_ADMIN,
                senhaHash = SenhaHasher.gerarHash(SENHA_DEMO_ADMIN),
                perfil = PerfilUsuario.ADMIN
            )
        )
        usuarioRepository.salvar(
            Usuario(
                nome = "Guarita Principal",
                login = LOGIN_DEMO_PORTARIA,
                senhaHash = SenhaHasher.gerarHash(SENHA_DEMO_PORTARIA),
                perfil = PerfilUsuario.PORTARIA
            )
        )

        val servidoresIniciais = listOf("BP102401", "BP102402", "BP102403")
        servidoresIniciais.forEach { prontuario ->
            val servidor = servidorRepository.buscarPorProntuario(prontuario) ?: return@forEach
            usuarioRepository.salvar(
                Usuario(
                    nome = servidor.nome,
                    login = servidor.email.lowercase(),
                    senhaHash = SenhaHasher.gerarHash(SENHA_DEMO_SERVIDOR),
                    perfil = PerfilUsuario.SERVIDOR,
                    servidorId = servidor.id
                )
            )
        }
    }

    private suspend fun popularVisitantesIniciais() {
        if (visitanteRepository.contar() > 0) return

        val agora = System.currentTimeMillis()
        val umaHora = 60L * 60L * 1000L
        val umDia = 24L * umaHora

        visitanteRepository.salvar(
            Visitante(
                nome = "Prof. Ricardo Mendes",
                documento = "321.654.987-00",
                instituicaoOrigem = "IFSP Campus Guarulhos",
                telefone = "(11) 97412-8890",
                placa = "IFG8B24",
                modeloVeiculo = "Hyundai Creta",
                corVeiculo = "Azul",
                motivoVisita = "Banca avaliadora de TCC",
                setorDestino = "Auditório Bloco B",
                tipoVaga = TipoVaga.COMUM,
                turno = Turno.TARDE,
                registradoPor = "Guarita Principal",
                dataHoraEntradaMillis = agora - umaHora,
                dataHoraSaidaMillis = null,
                observacao = "Convidado pela Coordenação BCC"
            )
        )
        visitanteRepository.salvar(
            Visitante(
                nome = "Mariana Ferreira",
                documento = "45.890.123-4",
                instituicaoOrigem = "TechRede Telecom",
                telefone = "(11) 96321-4400",
                placa = "FBR3C12",
                modeloVeiculo = "Fiat Fiorino",
                corVeiculo = "Branco",
                motivoVisita = "Manutenção de link óptico",
                setorDestino = "CTI / Datacenter",
                tipoVaga = TipoVaga.EXCLUSIVA_OFICIAL,
                turno = Turno.MANHA,
                registradoPor = "Guarita Principal",
                dataHoraEntradaMillis = agora - (2 * umaHora),
                dataHoraSaidaMillis = null,
                observacao = "Autorizado pela Diretoria Administrativa"
            )
        )
        visitanteRepository.salvar(
            Visitante(
                nome = "Roberto Alves",
                documento = "112.445.778-90",
                instituicaoOrigem = "Prefeitura de Bragança Paulista",
                telefone = "(11) 95510-2030",
                placa = "PBP2026",
                modeloVeiculo = "Renault Logan",
                corVeiculo = "Prata",
                motivoVisita = "Reunião convênio extensão",
                setorDestino = "Direção Geral",
                tipoVaga = TipoVaga.COMUM,
                turno = Turno.MANHA,
                registradoPor = "Guarita Principal",
                dataHoraEntradaMillis = agora - umDia - (3 * umaHora),
                dataHoraSaidaMillis = agora - umDia - umaHora,
                observacao = "Visita encerrada normalmente"
            )
        )
    }

    companion object {
        const val LOGIN_DEMO_ADMIN = "admin@campus.edu.br"
        const val SENHA_DEMO_ADMIN = "admin123"

        const val LOGIN_DEMO_PORTARIA = "portaria@campus.edu.br"
        const val SENHA_DEMO_PORTARIA = "portaria123"

        const val SENHA_DEMO_SERVIDOR = "123456"

        fun senhaDemoParaPerfil(perfil: PerfilUsuario): String = when (perfil) {
            PerfilUsuario.ADMIN -> SENHA_DEMO_ADMIN
            PerfilUsuario.PORTARIA -> SENHA_DEMO_PORTARIA
            PerfilUsuario.SERVIDOR -> SENHA_DEMO_SERVIDOR
        }
    }
}
