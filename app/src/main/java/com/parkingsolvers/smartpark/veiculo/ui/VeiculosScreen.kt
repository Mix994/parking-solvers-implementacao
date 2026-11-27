package com.parkingsolvers.smartpark.veiculo.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Accessible
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Elderly
import androidx.compose.material.icons.filled.EvStation
import androidx.compose.material.icons.filled.LocalParking
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.parkingsolvers.smartpark.compartilhado.util.Formatadores
import com.parkingsolvers.smartpark.dashboard.dto.response.DashboardOcupacaoResponse
import com.parkingsolvers.smartpark.movimentacao.dto.response.MovimentacaoResponse
import com.parkingsolvers.smartpark.movimentacao.model.TipoVaga
import com.parkingsolvers.smartpark.servidor.dto.response.ServidorResponse
import com.parkingsolvers.smartpark.ui.components.BadgeTone
import com.parkingsolvers.smartpark.ui.components.EmptyState
import com.parkingsolvers.smartpark.ui.components.FeedbackAnimado
import com.parkingsolvers.smartpark.ui.components.PageHeader
import com.parkingsolvers.smartpark.ui.components.PlacaMercosulChip
import com.parkingsolvers.smartpark.ui.components.SmartParkCard
import com.parkingsolvers.smartpark.ui.components.SmartParkMotion
import com.parkingsolvers.smartpark.ui.components.StatusBadge
import com.parkingsolvers.smartpark.ui.components.escalaAoPressionar
import com.parkingsolvers.smartpark.veiculo.dto.request.VeiculoRequest
import com.parkingsolvers.smartpark.veiculo.dto.response.VeiculoResponse
import com.parkingsolvers.smartpark.veiculo.model.TipoCadastroVeiculo

@Composable
fun VeiculosScreen(
    viewModel: VeiculoViewModel,
    servidorIdFixo: Long?
) {
    val veiculos by viewModel.veiculos.collectAsState()
    val servidores by viewModel.servidores.collectAsState()
    val movimentacoesAbertas by viewModel.movimentacoesAbertas.collectAsState()
    val ocupacaoAtual by viewModel.ocupacaoAtual.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    var mostrarDialogo by remember { mutableStateOf(false) }
    var veiculoParaSimular by remember { mutableStateOf<VeiculoResponse?>(null) }

    Scaffold(
        floatingActionButton = {
            val fabInteraction = remember { MutableInteractionSource() }
            FloatingActionButton(
                onClick = {
                    viewModel.limparErrosFormulario()
                    mostrarDialogo = true
                },
                interactionSource = fabInteraction,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.escalaAoPressionar(fabInteraction, 0.93f)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Cadastrar veículo")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                PageHeader(
                    titulo = if (servidorIdFixo != null) "Meus veículos" else "Veículos cadastrados",
                    subtitulo = if (servidorIdFixo != null) {
                        "Gerencie seus veículos ou simule estacionamento para testar o painel da portaria"
                    } else {
                        "Veículos permanentes e autorizações temporárias com prazo de validade"
                    }
                )
            }

            item {
                FeedbackAnimado(mensagem = uiState.mensagemFeedback)
            }

            item {
                val erroVisivel = if (!mostrarDialogo && veiculoParaSimular == null) uiState.erroGeral else null
                FeedbackAnimado(mensagem = erroVisivel, ehErro = true)
            }

            if (veiculos.isEmpty()) {
                item {
                    EmptyState(
                        icone = Icons.Default.DirectionsCar,
                        titulo = "Nenhum veículo cadastrado",
                        descricao = "Toque no botão + para registrar um veículo permanente ou temporário."
                    )
                }
            } else {
                items(veiculos, key = { it.id }) { veiculo ->
                    val movimentacaoAberta = if (servidorIdFixo != null) {
                        movimentacoesAbertas.firstOrNull { it.veiculoId == veiculo.id || it.placa == veiculo.placa }
                    } else {
                        null
                    }
                    VeiculoCard(
                        veiculo = veiculo,
                        permitirSimulacao = servidorIdFixo != null,
                        movimentacaoAberta = movimentacaoAberta,
                        processandoSimulacao = uiState.processandoSimulacao,
                        onAlternarAtivo = { viewModel.alternarAtivo(veiculo.id) },
                        onAbrirSimulacao = {
                            viewModel.limparErrosFormulario()
                            veiculoParaSimular = veiculo
                        },
                        onSimularSaida = { movId -> viewModel.simularSaida(movId) },
                        modifier = Modifier.animateItem()
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(84.dp)) }
        }
    }

    if (mostrarDialogo) {
        NovoVeiculoDialog(
            servidoresAtivos = servidores.filter { it.ativo },
            servidorIdFixo = servidorIdFixo,
            erroGeral = uiState.erroGeral,
            errosCampos = uiState.errosCampos,
            onDismiss = { mostrarDialogo = false },
            onConfirmar = { request ->
                viewModel.cadastrar(request) {
                    mostrarDialogo = false
                }
            }
        )
    }

    veiculoParaSimular?.let { veiculo ->
        SimularEstacionamentoDialog(
            veiculo = veiculo,
            ocupacaoAtual = ocupacaoAtual,
            processando = uiState.processandoSimulacao,
            erroGeral = uiState.erroGeral,
            onDismiss = { veiculoParaSimular = null },
            onConfirmar = { tipoVaga ->
                viewModel.simularEstacionamento(veiculo.placa, tipoVaga) {
                    veiculoParaSimular = null
                }
            }
        )
    }
}

@Composable
private fun VeiculoCard(
    veiculo: VeiculoResponse,
    permitirSimulacao: Boolean,
    movimentacaoAberta: MovimentacaoResponse?,
    processandoSimulacao: Boolean,
    onAlternarAtivo: () -> Unit,
    onAbrirSimulacao: () -> Unit,
    onSimularSaida: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    SmartParkCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                PlacaMercosulChip(placa = veiculo.placa)
                Spacer(modifier = Modifier.weight(1f))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    StatusBadge(
                        texto = veiculo.tipoCadastro.label,
                        tone = if (veiculo.tipoCadastro == TipoCadastroVeiculo.TEMPORARIO) {
                            BadgeTone.ALERTA
                        } else {
                            BadgeTone.INFO
                        }
                    )
                    StatusBadge(
                        texto = when {
                            !veiculo.ativo -> "Inativo"
                            veiculo.validoNoMomento -> "Válido"
                            else -> "Expirado"
                        },
                        tone = when {
                            !veiculo.ativo -> BadgeTone.NEUTRO
                            veiculo.validoNoMomento -> BadgeTone.SUCESSO
                            else -> BadgeTone.ERRO
                        }
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "${veiculo.modelo} (${veiculo.cor})",
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = "${veiculo.servidorNome} (${veiculo.servidorProntuario})",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (veiculo.tipoCadastro == TipoCadastroVeiculo.TEMPORARIO &&
                    veiculo.validadeInicioMillis != null &&
                    veiculo.validadeFimMillis != null
                ) {
                    Text(
                        text = "Vigência: ${Formatadores.formatarData(veiculo.validadeInicioMillis)} até ${Formatadores.formatarData(veiculo.validadeFimMillis)}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
            }

            if (movimentacaoAberta != null) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Estacionado • ${movimentacaoAberta.tipoVaga.label} (desde ${Formatadores.formatarHora(movimentacaoAberta.dataHoraEntradaMillis)})",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        StatusBadge(texto = "No pátio", tone = BadgeTone.SUCESSO)
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = onAlternarAtivo,
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    Text(
                        text = if (veiculo.ativo) "Inativar veículo" else "Reativar veículo",
                        maxLines = 1,
                        softWrap = false
                    )
                }
                Spacer(modifier = Modifier.weight(1f))

                if (permitirSimulacao) {
                    val simInteraction = remember { MutableInteractionSource() }
                    if (movimentacaoAberta != null) {
                        OutlinedButton(
                            onClick = { onSimularSaida(movimentacaoAberta.id) },
                            enabled = !processandoSimulacao,
                            interactionSource = simInteraction,
                            modifier = Modifier.escalaAoPressionar(simInteraction)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Sair da vaga", maxLines = 1, softWrap = false)
                        }
                    } else {
                        FilledTonalButton(
                            onClick = onAbrirSimulacao,
                            enabled = veiculo.ativo && veiculo.validoNoMomento && !processandoSimulacao,
                            interactionSource = simInteraction,
                            modifier = Modifier.escalaAoPressionar(simInteraction)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocalParking,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Simular vaga", maxLines = 1, softWrap = false)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SimularEstacionamentoDialog(
    veiculo: VeiculoResponse,
    ocupacaoAtual: DashboardOcupacaoResponse?,
    processando: Boolean,
    erroGeral: String?,
    onDismiss: () -> Unit,
    onConfirmar: (TipoVaga) -> Unit
) {
    val opcoesDisponibilidade = remember(ocupacaoAtual) {
        TipoVaga.entries.map { tipo ->
            val (livres, capacidade, textoDisponibilidade) = disponibilidadeParaTipo(tipo, ocupacaoAtual)
            val lotado = capacidade != null && capacidade > 0 && livres != null && livres <= 0
            Triple(tipo, textoDisponibilidade, lotado)
        }
    }

    var tipoSelecionado by remember(veiculo.id) {
        mutableStateOf(
            opcoesDisponibilidade.firstOrNull { !it.third }?.first ?: TipoVaga.COMUM
        )
    }

    AlertDialog(
        onDismissRequest = { if (!processando) onDismiss() },
        title = { Text("Simular estacionamento") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    PlacaMercosulChip(placa = veiculo.placa)
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${veiculo.modelo} (${veiculo.cor})",
                            style = MaterialTheme.typography.titleSmall
                        )
                        Text(
                            text = "Aparecerá em tempo real na Portaria e no Painel.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Text(
                    text = "Escolha em qual tipo de vaga você estacionou:",
                    style = MaterialTheme.typography.labelMedium
                )

                opcoesDisponibilidade.forEach { (tipo, disponibilidade, lotado) ->
                    val selecionado = tipoSelecionado == tipo
                    val itemInteraction = remember { MutableInteractionSource() }
                    val corFundo by animateColorAsState(
                        targetValue = if (selecionado) {
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                        },
                        animationSpec = tween(220, easing = SmartParkMotion.CurvaPadrao),
                        label = "fundo_${tipo.name}"
                    )
                    val corBorda by animateColorAsState(
                        targetValue = if (selecionado) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.outlineVariant
                        },
                        animationSpec = tween(220, easing = SmartParkMotion.CurvaPadrao),
                        label = "borda_${tipo.name}"
                    )
                    Surface(
                        onClick = { if (!lotado) tipoSelecionado = tipo },
                        enabled = !lotado && !processando,
                        interactionSource = itemInteraction,
                        modifier = Modifier
                            .fillMaxWidth()
                            .escalaAoPressionar(itemInteraction, 0.98f),
                        shape = RoundedCornerShape(10.dp),
                        color = corFundo,
                        border = BorderStroke(
                            width = if (selecionado) 1.5.dp else 1.dp,
                            color = corBorda
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            RadioButton(
                                selected = selecionado,
                                onClick = { if (!lotado) tipoSelecionado = tipo },
                                enabled = !lotado && !processando
                            )
                            Icon(
                                imageVector = iconePorTipoVaga(tipo),
                                contentDescription = null,
                                tint = if (lotado) {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                } else {
                                    MaterialTheme.colorScheme.primary
                                },
                                modifier = Modifier.size(20.dp)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = tipo.label,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = disponibilidade,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (lotado) {
                                        MaterialTheme.colorScheme.error
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    }
                                )
                            }
                            if (lotado) {
                                StatusBadge(texto = "Lotado", tone = BadgeTone.ALERTA)
                            }
                        }
                    }
                }

                FeedbackAnimado(mensagem = erroGeral, ehErro = true)
            }
        },
        confirmButton = {
            val confirmarInteraction = remember { MutableInteractionSource() }
            Button(
                onClick = { onConfirmar(tipoSelecionado) },
                enabled = !processando && opcoesDisponibilidade.any { it.first == tipoSelecionado && !it.third },
                interactionSource = confirmarInteraction,
                modifier = Modifier.escalaAoPressionar(confirmarInteraction)
            ) {
                Text(if (processando) "Registrando..." else "Confirmar entrada")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !processando) {
                Text("Cancelar")
            }
        }
    )
}

private fun disponibilidadeParaTipo(
    tipo: TipoVaga,
    ocupacao: DashboardOcupacaoResponse?
): Triple<Int?, Int?, String> {
    if (ocupacao == null) {
        return Triple(null, null, "Disponibilidade em atualização")
    }
    return when (tipo) {
        TipoVaga.COMUM -> Triple(
            ocupacao.vagasComunsLivresAgora,
            ocupacao.capacidadeComuns,
            "${ocupacao.vagasComunsLivresAgora} de ${ocupacao.capacidadeComuns} vagas livres"
        )
        TipoVaga.CARREGAMENTO_ELETRICO -> Triple(
            ocupacao.vagasEletricasLivresAgora,
            ocupacao.capacidadeCarregamentoEletrico,
            "${ocupacao.vagasEletricasLivresAgora} de ${ocupacao.capacidadeCarregamentoEletrico} estações livres"
        )
        TipoVaga.EXCLUSIVA_PCD,
        TipoVaga.EXCLUSIVA_IDOSO,
        TipoVaga.EXCLUSIVA_OFICIAL -> Triple(
            ocupacao.vagasExclusivasLivresAgora,
            ocupacao.capacidadeExclusivas,
            "${ocupacao.vagasExclusivasLivresAgora} de ${ocupacao.capacidadeExclusivas} vagas exclusivas livres"
        )
    }
}

private fun iconePorTipoVaga(tipo: TipoVaga): ImageVector = when (tipo) {
    TipoVaga.COMUM -> Icons.Default.DirectionsCar
    TipoVaga.CARREGAMENTO_ELETRICO -> Icons.Default.EvStation
    TipoVaga.EXCLUSIVA_PCD -> Icons.AutoMirrored.Filled.Accessible
    TipoVaga.EXCLUSIVA_IDOSO -> Icons.Default.Elderly
    TipoVaga.EXCLUSIVA_OFICIAL -> Icons.Default.VerifiedUser
}

@Composable
private fun NovoVeiculoDialog(
    servidoresAtivos: List<ServidorResponse>,
    servidorIdFixo: Long?,
    erroGeral: String?,
    errosCampos: Map<String, String>,
    onDismiss: () -> Unit,
    onConfirmar: (VeiculoRequest) -> Unit
) {
    var servidorSelecionadoId by remember {
        mutableLongStateOf(servidorIdFixo ?: servidoresAtivos.firstOrNull()?.id ?: 0L)
    }
    var placa by remember { mutableStateOf("") }
    var modelo by remember { mutableStateOf("") }
    var cor by remember { mutableStateOf("") }
    var tipoCadastro by remember { mutableStateOf(TipoCadastroVeiculo.PERMANENTE) }
    var diasValidade by remember { mutableIntStateOf(3) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Cadastrar veículo") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (servidorIdFixo == null && servidoresAtivos.isNotEmpty()) {
                    Text("Servidor vinculado:", style = MaterialTheme.typography.labelMedium)
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        servidoresAtivos.forEach { servidor ->
                            FilterChip(
                                selected = servidorSelecionadoId == servidor.id,
                                onClick = { servidorSelecionadoId = servidor.id },
                                label = { Text("${servidor.nome} (${servidor.prontuario})") }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = placa,
                    onValueChange = { placa = it.uppercase().take(8) },
                    label = { Text("Placa (AAA-1234 ou ABC1D23)") },
                    isError = errosCampos.containsKey("placa"),
                    supportingText = errosCampos["placa"]?.let { { Text(it) } },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = modelo,
                    onValueChange = { modelo = it },
                    label = { Text("Modelo") },
                    isError = errosCampos.containsKey("modelo"),
                    supportingText = errosCampos["modelo"]?.let { { Text(it) } },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = cor,
                    onValueChange = { cor = it },
                    label = { Text("Cor") },
                    isError = errosCampos.containsKey("cor"),
                    supportingText = errosCampos["cor"]?.let { { Text(it) } },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Tipo de cadastro:", style = MaterialTheme.typography.labelMedium)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TipoCadastroVeiculo.entries.forEach { tipo ->
                        FilterChip(
                            selected = tipoCadastro == tipo,
                            onClick = { tipoCadastro = tipo },
                            label = { Text(tipo.label, maxLines = 1, softWrap = false) }
                        )
                    }
                }

                if (tipoCadastro == TipoCadastroVeiculo.TEMPORARIO) {
                    Text("Validade temporária a partir de hoje:", style = MaterialTheme.typography.labelMedium)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(1, 3, 7, 15).forEach { dias ->
                            FilterChip(
                                selected = diasValidade == dias,
                                onClick = { diasValidade = dias },
                                label = { Text("${dias}d", maxLines = 1, softWrap = false) }
                            )
                        }
                    }
                }

                FeedbackAnimado(mensagem = erroGeral, ehErro = true)
            }
        },
        confirmButton = {
            val salvarInteraction = remember { MutableInteractionSource() }
            Button(
                onClick = {
                    val agora = System.currentTimeMillis()
                    val umDia = 24L * 60L * 60L * 1000L
                    onConfirmar(
                        VeiculoRequest(
                            servidorId = servidorSelecionadoId,
                            placa = placa,
                            modelo = modelo,
                            cor = cor,
                            tipoCadastro = tipoCadastro,
                            validadeInicioMillis = if (tipoCadastro == TipoCadastroVeiculo.TEMPORARIO) agora else null,
                            validadeFimMillis = if (tipoCadastro == TipoCadastroVeiculo.TEMPORARIO) agora + (diasValidade * umDia) else null
                        )
                    )
                },
                enabled = servidorSelecionadoId > 0L,
                interactionSource = salvarInteraction,
                modifier = Modifier.escalaAoPressionar(salvarInteraction)
            ) {
                Text("Salvar veículo")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
