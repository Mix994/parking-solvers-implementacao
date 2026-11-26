package com.parkingsolvers.smartpark.movimentacao.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.parkingsolvers.smartpark.compartilhado.util.Formatadores
import com.parkingsolvers.smartpark.compartilhado.validacao.ValidadorPlaca
import com.parkingsolvers.smartpark.movimentacao.dto.response.MovimentacaoResponse
import com.parkingsolvers.smartpark.movimentacao.model.TipoVaga
import com.parkingsolvers.smartpark.ui.components.BadgeTone
import com.parkingsolvers.smartpark.ui.components.EmptyState
import com.parkingsolvers.smartpark.ui.components.FeedbackAnimado
import com.parkingsolvers.smartpark.ui.components.PageHeader
import com.parkingsolvers.smartpark.ui.components.PlacaMercosulChip
import com.parkingsolvers.smartpark.ui.components.SmartParkCard
import com.parkingsolvers.smartpark.ui.components.SmartParkMotion
import com.parkingsolvers.smartpark.ui.components.StatusBadge
import com.parkingsolvers.smartpark.ui.components.escalaAoPressionar
import com.parkingsolvers.smartpark.veiculo.dto.response.ResultadoConsultaPlaca
import com.parkingsolvers.smartpark.veiculo.dto.response.StatusAutorizacao
import com.parkingsolvers.smartpark.veiculo.dto.response.VeiculoResponse

@Composable
fun PortariaScreen(
    viewModel: PortariaViewModel,
    onRegistrarComoVisitante: (placa: String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val noEstacionamento by viewModel.noEstacionamento.collectAsState()
    val historico by viewModel.historico.collectAsState()
    val correspondencias by viewModel.correspondenciasPlaca.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            PageHeader(
                titulo = "Portaria",
                subtitulo = "Consulte placas na cancela e controle entradas e saídas de servidores"
            )
        }

        item {
            SmartParkCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val placaDigitada = uiState.placaBusca
                    val placaComErro = placaDigitada.length >= 7 && !ValidadorPlaca.ehValida(placaDigitada)
                    val podeConsultar = ValidadorPlaca.ehValida(placaDigitada) || correspondencias.size == 1

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = placaDigitada,
                            onValueChange = viewModel::atualizarPlacaBusca,
                            label = { Text("Placa (digite parte ou completa)") },
                            singleLine = true,
                            isError = placaComErro,
                            supportingText = if (placaComErro) {
                                { Text("Formato inválido. Use AAA-1234 ou ABC1D23.") }
                            } else {
                                null
                            },
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Characters,
                                imeAction = ImeAction.Search
                            ),
                            keyboardActions = KeyboardActions(
                                onSearch = { if (podeConsultar) viewModel.consultarPlaca() }
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        val buscarInteraction = remember { MutableInteractionSource() }
                        Button(
                            onClick = { viewModel.consultarPlaca() },
                            enabled = podeConsultar,
                            interactionSource = buscarInteraction,
                            modifier = Modifier.escalaAoPressionar(buscarInteraction)
                        ) {
                            Icon(Icons.Default.Search, contentDescription = "Consultar placa")
                        }
                    }

                    AnimatedVisibility(
                        visible = correspondencias.isNotEmpty() && uiState.resultadoConsulta == null,
                        enter = fadeIn(tween(220, easing = SmartParkMotion.CurvaPadrao)) +
                            expandVertically(tween(240, easing = SmartParkMotion.CurvaPadrao)),
                        exit = fadeOut(tween(160, easing = SmartParkMotion.CurvaPadrao)) +
                            shrinkVertically(tween(180, easing = SmartParkMotion.CurvaPadrao))
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "Correspondências encontradas (toque para consultar)",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            correspondencias.forEach { veiculo ->
                                SugestaoPlacaItem(
                                    veiculo = veiculo,
                                    onClick = { viewModel.selecionarPlacaSugerida(veiculo.placa) }
                                )
                            }
                        }
                    }

                    Text(
                        text = "Tipo de vaga na entrada",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TipoVaga.entries.forEach { tipo ->
                            FilterChip(
                                selected = uiState.tipoVagaSelecionado == tipo,
                                onClick = { viewModel.selecionarTipoVaga(tipo) },
                                label = { Text(tipo.label, maxLines = 1, softWrap = false) }
                            )
                        }
                    }

                    FeedbackAnimado(mensagem = uiState.erro, ehErro = true)
                    FeedbackAnimado(mensagem = uiState.mensagemFeedback, ehErro = false)
                }
            }
        }

        item(key = "resultado_consulta") {
            val resultado = uiState.resultadoConsulta
            AnimatedVisibility(
                visible = resultado != null,
                enter = fadeIn(tween(260, easing = SmartParkMotion.CurvaPadrao)) +
                    expandVertically(tween(280, easing = SmartParkMotion.CurvaPadrao)),
                exit = fadeOut(tween(180, easing = SmartParkMotion.CurvaPadrao)) +
                    shrinkVertically(tween(220, easing = SmartParkMotion.CurvaPadrao))
            ) {
                if (resultado != null) {
                    val movimentacaoAberta = noEstacionamento.firstOrNull { it.placa == resultado.placaConsultada }
                    ResultadoConsultaCard(
                        resultado = resultado,
                        movimentacaoAberta = movimentacaoAberta,
                        onRegistrarEntrada = { permitirAvulso ->
                            viewModel.registrarEntrada(permitirNaoAutorizado = permitirAvulso)
                        },
                        onRegistrarSaida = { id -> viewModel.registrarSaida(id) },
                        onCadastrarVisitante = { onRegistrarComoVisitante(resultado.placaConsultada) }
                    )
                }
            }
        }

        item {
            Text(
                text = "No estacionamento agora (${noEstacionamento.size})",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 6.dp)
            )
        }

        if (noEstacionamento.isEmpty()) {
            item {
                EmptyState(
                    icone = Icons.Default.DirectionsCar,
                    titulo = "Nenhum veículo de servidor no pátio",
                    descricao = "As entradas registradas pela portaria aparecem nesta lista."
                )
            }
        } else {
            items(noEstacionamento, key = { it.id }) { mov ->
                MovimentacaoAtivaCard(
                    movimentacao = mov,
                    onRegistrarSaida = { viewModel.registrarSaida(mov.id) },
                    modifier = Modifier.animateItem()
                )
            }
        }

        val ultimasSaidas = historico.filter { !it.emAberto }.take(10)
        item {
            Text(
                text = "Últimas saídas",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 6.dp)
            )
        }
        if (ultimasSaidas.isEmpty()) {
            item {
                Text(
                    text = "Nenhuma saída registrada ainda.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            items(ultimasSaidas, key = { "saida_${it.id}" }) { mov ->
                HistoricoItem(movimentacao = mov, modifier = Modifier.animateItem())
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

@Composable
private fun SugestaoPlacaItem(
    veiculo: VeiculoResponse,
    onClick: () -> Unit
) {
    val itemInteraction = remember { MutableInteractionSource() }
    Surface(
        onClick = onClick,
        interactionSource = itemInteraction,
        modifier = Modifier
            .fillMaxWidth()
            .escalaAoPressionar(itemInteraction, 0.98f),
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                PlacaMercosulChip(placa = veiculo.placa)
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = veiculo.servidorNome,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "${veiculo.modelo} • ${veiculo.cor}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            StatusBadge(
                texto = if (veiculo.validoNoMomento) "Autorizado" else "Verificar",
                tone = if (veiculo.validoNoMomento) BadgeTone.SUCESSO else BadgeTone.ALERTA
            )
        }
    }
}

@Composable
private fun HistoricoItem(
    movimentacao: MovimentacaoResponse,
    modifier: Modifier = Modifier
) {
    SmartParkCard(
        modifier = modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = Formatadores.formatarPlaca(movimentacao.placa),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = movimentacao.servidorNome,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = "${Formatadores.formatarHora(movimentacao.dataHoraEntradaMillis)} às " +
                    (movimentacao.dataHoraSaidaMillis?.let { Formatadores.formatarHora(it) } ?: "--:--"),
                style = MaterialTheme.typography.labelMedium
            )
            if (!movimentacao.autorizado) {
                Spacer(modifier = Modifier.width(8.dp))
                StatusBadge(texto = "Excepcional", tone = BadgeTone.ALERTA)
            }
        }
    }
}

@Composable
private fun ResultadoConsultaCard(
    resultado: ResultadoConsultaPlaca,
    movimentacaoAberta: MovimentacaoResponse?,
    onRegistrarEntrada: (permitirAvulso: Boolean) -> Unit,
    onRegistrarSaida: (movimentacaoId: Long) -> Unit,
    onCadastrarVisitante: () -> Unit
) {
    val bgColor = if (resultado.autorizado) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
    } else {
        MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
    }
    val borderColor = if (resultado.autorizado) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
    } else {
        MaterialTheme.colorScheme.error.copy(alpha = 0.4f)
    }

    SmartParkCard(
        modifier = Modifier.fillMaxWidth(),
        containerColor = bgColor,
        borderColor = borderColor
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                PlacaMercosulChip(placa = resultado.placaConsultada, destaque = true)
                Spacer(modifier = Modifier.weight(1f))
                StatusBadge(
                    texto = resultado.status.label,
                    tone = if (resultado.autorizado) BadgeTone.SUCESSO else BadgeTone.ERRO
                )
            }

            Text(text = resultado.mensagem, style = MaterialTheme.typography.bodyMedium)

            resultado.veiculo?.let { v ->
                Text(
                    text = "${v.modelo} (${v.cor}) — ${v.servidorNome} (${v.servidorProntuario})",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }

            when {
                movimentacaoAberta != null -> {
                    Text(
                        text = "Veículo no pátio desde ${Formatadores.formatarHora(movimentacaoAberta.dataHoraEntradaMillis)}.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    val acaoInteraction = remember { MutableInteractionSource() }
                    Button(
                        onClick = { onRegistrarSaida(movimentacaoAberta.id) },
                        interactionSource = acaoInteraction,
                        modifier = Modifier
                            .fillMaxWidth()
                            .escalaAoPressionar(acaoInteraction)
                    ) {
                        Text("Dar baixa na saída")
                    }
                }
                resultado.autorizado -> {
                    val acaoInteraction = remember { MutableInteractionSource() }
                    Button(
                        onClick = { onRegistrarEntrada(false) },
                        interactionSource = acaoInteraction,
                        modifier = Modifier
                            .fillMaxWidth()
                            .escalaAoPressionar(acaoInteraction)
                    ) {
                        Text("Liberar entrada autorizada")
                    }
                }
                else -> {
                    if (resultado.status == StatusAutorizacao.NAO_CADASTRADO) {
                        val visInteraction = remember { MutableInteractionSource() }
                        Button(
                            onClick = onCadastrarVisitante,
                            interactionSource = visInteraction,
                            modifier = Modifier
                                .fillMaxWidth()
                                .escalaAoPressionar(visInteraction)
                        ) {
                            Text("Registrar como visitante externo")
                        }
                    }
                    val excInteraction = remember { MutableInteractionSource() }
                    OutlinedButton(
                        onClick = { onRegistrarEntrada(true) },
                        interactionSource = excInteraction,
                        modifier = Modifier
                            .fillMaxWidth()
                            .escalaAoPressionar(excInteraction)
                    ) {
                        Text("Liberar entrada excepcional")
                    }
                }
            }
        }
    }
}

@Composable
private fun MovimentacaoAtivaCard(
    movimentacao: MovimentacaoResponse,
    onRegistrarSaida: () -> Unit,
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
                PlacaMercosulChip(placa = movimentacao.placa)
                Spacer(modifier = Modifier.weight(1f))
                StatusBadge(
                    texto = movimentacao.tipoVaga.label,
                    tone = when {
                        movimentacao.tipoVaga.ehCarregamentoEletrico -> BadgeTone.SUCESSO
                        movimentacao.tipoVaga.ehExclusiva -> BadgeTone.ALERTA
                        else -> BadgeTone.INFO
                    }
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = movimentacao.servidorNome,
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = movimentacao.modeloCor,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (movimentacao.observacao.isNotBlank()) {
                    Text(
                        text = movimentacao.observacao,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Entrada às ${Formatadores.formatarHora(movimentacao.dataHoraEntradaMillis)} (${movimentacao.turno.label})",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                TextButton(onClick = onRegistrarSaida) {
                    Text("Dar baixa na saída", maxLines = 1, softWrap = false)
                }
            }
        }
    }
}
