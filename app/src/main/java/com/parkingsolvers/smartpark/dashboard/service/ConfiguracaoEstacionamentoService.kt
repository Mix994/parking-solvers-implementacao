package com.parkingsolvers.smartpark.dashboard.service

import com.parkingsolvers.smartpark.compartilhado.validacao.ValidadorFormulario
import com.parkingsolvers.smartpark.dashboard.model.ConfiguracaoEstacionamento
import com.parkingsolvers.smartpark.dashboard.repository.ConfiguracaoEstacionamentoRepository
import kotlinx.coroutines.flow.Flow

/**
 * Gerencia a capacidade configurável do estacionamento (incluindo expansão
 * das vagas de carregamento de carros elétricos a partir das 2 vagas iniciais).
 */
class ConfiguracaoEstacionamentoService(
    private val repository: ConfiguracaoEstacionamentoRepository
) {

    fun observar(): Flow<ConfiguracaoEstacionamento> = repository.observar()

    suspend fun buscarAtual(): ConfiguracaoEstacionamento = repository.buscarAtual()

    suspend fun atualizarCapacidades(
        capacidadeTotal: Int,
        capacidadeExclusivas: Int,
        capacidadeCarregamentoEletrico: Int
    ): ConfiguracaoEstacionamento {
        val erros = mutableMapOf<String, String>()
        if (capacidadeTotal < 1) {
            erros["capacidadeTotal"] = "A capacidade total deve ser de pelo menos 1 vaga."
        }
        if (capacidadeExclusivas < 0) {
            erros["capacidadeExclusivas"] = "Informe um número válido de vagas exclusivas."
        }
        if (capacidadeCarregamentoEletrico < 0) {
            erros["capacidadeCarregamentoEletrico"] = "Informe um número válido de vagas elétricas."
        }
        if (erros.isEmpty() && capacidadeExclusivas + capacidadeCarregamentoEletrico > capacidadeTotal) {
            erros["capacidadeTotal"] =
                "A soma das vagas exclusivas e de carregamento elétrico não pode ultrapassar a capacidade total."
        }
        ValidadorFormulario.lancarSeHouverErros(erros)

        val novaConfig = ConfiguracaoEstacionamento(
            id = ConfiguracaoEstacionamento.ID_PADRAO,
            capacidadeTotal = capacidadeTotal,
            capacidadeExclusivas = capacidadeExclusivas,
            capacidadeCarregamentoEletrico = capacidadeCarregamentoEletrico
        )
        repository.salvar(novaConfig)
        return novaConfig
    }
}
