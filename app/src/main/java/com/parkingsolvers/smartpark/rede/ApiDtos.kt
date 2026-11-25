package com.parkingsolvers.smartpark.rede

import com.parkingsolvers.smartpark.auth.model.PerfilUsuario
import com.parkingsolvers.smartpark.auth.model.UsuarioSessao
import com.parkingsolvers.smartpark.dashboard.model.ConfiguracaoEstacionamento
import com.parkingsolvers.smartpark.movimentacao.dto.response.MovimentacaoResponse
import com.parkingsolvers.smartpark.movimentacao.model.TipoVaga
import com.parkingsolvers.smartpark.movimentacao.model.Turno
import com.parkingsolvers.smartpark.veiculo.dto.request.VeiculoRequest
import com.parkingsolvers.smartpark.veiculo.dto.response.ResultadoConsultaPlaca
import com.parkingsolvers.smartpark.veiculo.dto.response.StatusAutorizacao
import com.parkingsolvers.smartpark.veiculo.dto.response.VeiculoResponse
import com.parkingsolvers.smartpark.veiculo.model.TipoCadastroVeiculo
import com.parkingsolvers.smartpark.visitante.dto.response.VisitanteResponse

data class ErroApiResponse(
    val status: Int = 0,
    val erro: String? = null,
    val mensagem: String? = null,
    val campos: Map<String, String>? = null
)

data class TokenApiResponse(
    val accessToken: String,
    val refreshToken: String,
    val expiraEmSegundos: Long,
    val sessao: UsuarioSessao
)

data class RefreshApiRequest(
    val refreshToken: String
)

data class AlterarPerfilApiRequest(
    val perfil: PerfilUsuario,
    val servidorId: Long? = null
)

data class ConfiguracaoApiDto(
    val capacidadeTotal: Int,
    val capacidadeExclusivas: Int,
    val capacidadeCarregamentoEletrico: Int
) {
    fun paraDominio() = ConfiguracaoEstacionamento(
        id = ConfiguracaoEstacionamento.ID_PADRAO,
        capacidadeTotal = capacidadeTotal,
        capacidadeExclusivas = capacidadeExclusivas,
        capacidadeCarregamentoEletrico = capacidadeCarregamentoEletrico
    )
}

data class VeiculoApiRequest(
    val servidorId: Long,
    val placa: String,
    val modelo: String,
    val cor: String,
    val tipoCadastro: TipoCadastroVeiculo,
    val validadeInicio: String? = null,
    val validadeFim: String? = null
)

fun VeiculoRequest.paraApi() = VeiculoApiRequest(
    servidorId = servidorId,
    placa = placa,
    modelo = modelo,
    cor = cor,
    tipoCadastro = tipoCadastro,
    validadeInicio = Iso8601.deMillisOuNulo(validadeInicioMillis),
    validadeFim = Iso8601.deMillisOuNulo(validadeFimMillis)
)

data class VeiculoApiResponse(
    val id: Long,
    val servidorId: Long,
    val servidorNome: String,
    val servidorProntuario: String,
    val placa: String,
    val modelo: String,
    val cor: String,
    val tipoCadastro: TipoCadastroVeiculo,
    val validadeInicio: String? = null,
    val validadeFim: String? = null,
    val ativo: Boolean,
    val validoNoMomento: Boolean
) {
    fun paraDominio() = VeiculoResponse(
        id = id,
        servidorId = servidorId,
        servidorNome = servidorNome,
        servidorProntuario = servidorProntuario,
        placa = placa,
        modelo = modelo,
        cor = cor,
        tipoCadastro = tipoCadastro,
        validadeInicioMillis = Iso8601.paraMillisOuNulo(validadeInicio),
        validadeFimMillis = Iso8601.paraMillisOuNulo(validadeFim),
        ativo = ativo,
        validoNoMomento = validoNoMomento
    )
}

data class ResultadoConsultaPlacaApiResponse(
    val placaConsultada: String,
    val status: StatusAutorizacao,
    val mensagem: String,
    val veiculo: VeiculoApiResponse? = null
) {
    fun paraDominio() = ResultadoConsultaPlaca(
        placaConsultada = placaConsultada,
        status = status,
        mensagem = mensagem,
        veiculo = veiculo?.paraDominio()
    )
}

data class MovimentacaoApiResponse(
    val id: Long,
    val veiculoId: Long? = null,
    val placa: String,
    val modeloCor: String,
    val servidorNome: String,
    val autorizado: Boolean,
    val dataHoraEntrada: String,
    val dataHoraSaida: String? = null,
    val turno: Turno,
    val tipoVaga: TipoVaga,
    val observacao: String = ""
) {
    fun paraDominio() = MovimentacaoResponse(
        id = id,
        veiculoId = veiculoId,
        placa = placa,
        modeloCor = modeloCor,
        servidorNome = servidorNome,
        autorizado = autorizado,
        dataHoraEntradaMillis = Iso8601.paraMillis(dataHoraEntrada),
        dataHoraSaidaMillis = Iso8601.paraMillisOuNulo(dataHoraSaida),
        turno = turno,
        tipoVaga = tipoVaga,
        observacao = observacao
    )
}

data class VisitanteApiResponse(
    val id: Long,
    val nome: String,
    val documento: String,
    val instituicaoOrigem: String,
    val telefone: String,
    val placa: String,
    val modeloVeiculo: String,
    val corVeiculo: String,
    val motivoVisita: String,
    val setorDestino: String,
    val tipoVaga: TipoVaga,
    val turno: Turno,
    val registradoPor: String,
    val dataHoraEntrada: String,
    val dataHoraSaida: String? = null,
    val observacao: String = ""
) {
    fun paraDominio() = VisitanteResponse(
        id = id,
        nome = nome,
        documento = documento,
        instituicaoOrigem = instituicaoOrigem,
        telefone = telefone,
        placa = placa,
        modeloVeiculo = modeloVeiculo,
        corVeiculo = corVeiculo,
        motivoVisita = motivoVisita,
        setorDestino = setorDestino,
        tipoVaga = tipoVaga,
        turno = turno,
        registradoPor = registradoPor,
        dataHoraEntradaMillis = Iso8601.paraMillis(dataHoraEntrada),
        dataHoraSaidaMillis = Iso8601.paraMillisOuNulo(dataHoraSaida),
        observacao = observacao
    )
}
