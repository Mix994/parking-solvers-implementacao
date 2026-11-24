package com.parkingsolvers.smartpark.dashboard.ui

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.EvStation
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.parkingsolvers.smartpark.dashboard.model.PeriodoDashboard
import com.parkingsolvers.smartpark.ui.components.BadgeTone
import com.parkingsolvers.smartpark.ui.components.PageHeader
import com.parkingsolvers.smartpark.ui.components.SmartParkCard
import com.parkingsolvers.smartpark.ui.components.StatusBadge

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    podeConfigurarVagas: Boolean = false
) {
    val dados by viewModel.dashboard.collectAsState()
    val periodo by viewModel.periodo.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    var mostrarDialogoCapacidade by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            PageHeader(
                titulo = "Ocupação do campus",
                subtitulo = "Monitoramento de servidores, visitantes externos, recarga elétrica e turnos"
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PeriodoDashboard.entries.forEach { opcao ->
                        FilterChip(
                            selected = periodo == opcao,
                            onClick = { viewModel.selecionarPeriodo(opcao) },
                            label = { Text(opcao.label) }
                        )
                    }
                }
                if (podeConfigurarVagas && dados != null) {
                    TextButton(
                        onClick = {
                            viewModel.limparErrosConfiguracao()
                            mostrarDialogoCapacidade = true
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Vagas")
                    }
                }
            }
        }

        uiState.mensagemFeedback?.let { msg ->
            item {
                Text(
                    text = msg,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        if (dados == null) {
            item {
                Text(
                    text = "Carregando indicadores do estacionamento...",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        } else {
            val dashboard = dados!!
            val servidoresNoPatio = (dashboard.veiculosNoCampusAgora - dashboard.visitantesNoCampusAgora).coerceAtLeast(0)

            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.primary
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Column {
                                Text(
                                    text = "Veículos no pátio agora",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
                                )
                                Text(
                                    text = "${dashboard.veiculosNoCampusAgora} de ${dashboard.capacidadeTotal} vagas",
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                            Text(
                                text = "${(dashboard.percentualOcupacaoAtual * 100).toInt()}%",
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }

                        LinearProgressIndicator(
                            progress = { dashboard.percentualOcupacaoAtual },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            trackColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.25f)
                        )

                        Text(
                            text = "$servidoresNoPatio de servidores e ${dashboard.visitantesNoCampusAgora} de visitantes externos",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f)
                        )
                    }
                }
            }

            item {
                val lotadoEletrico = dashboard.capacidadeCarregamentoEletrico > 0 &&
                    dashboard.vagasEletricasOcupadasAgora >= dashboard.capacidadeCarregamentoEletrico
                SmartParkCard(
                    modifier = Modifier.fillMaxWidth(),
                    borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.EvStation,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Carregamento de veículos elétricos",
                                    style = MaterialTheme.typography.titleSmall
                                )
                            }
                            StatusBadge(
                                texto = if (lotadoEletrico) "Lotado" else "Disponível",
                                tone = if (lotadoEletrico) BadgeTone.ALERTA else BadgeTone.SUCESSO
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Text(
                                text = "${dashboard.vagasEletricasOcupadasAgora} de ${dashboard.capacidadeCarregamentoEletrico} vagas em uso",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "${(dashboard.percentualEletricasAtual * 100).toInt()}%",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        LinearProgressIndicator(
                            progress = { dashboard.percentualEletricasAtual },
                            modifier = Modifier.fillMaxWidth()
                        )

                        if (podeConfigurarVagas) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Estrutura expansível conforme novas estações",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedButton(
                                    onClick = {
                                        viewModel.limparErrosConfiguracao()
                                        mostrarDialogoCapacidade = true
                                    }
                                ) {
                                    Text("Expandir")
                                }
                            }
                        }
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricaCard(
                        titulo = "Vagas exclusivas",
                        valor = "${dashboard.vagasExclusivasOcupadasAgora}/${dashboard.capacidadeExclusivas}",
                        subtitulo = "${(dashboard.percentualExclusivasAtual * 100).toInt()}% em uso agora",
                        modifier = Modifier.weight(1f)
                    )
                    MetricaCard(
                        titulo = "Visitantes externos",
                        valor = dashboard.visitantesNoCampusAgora.toString(),
                        subtitulo = "${dashboard.totalVisitantesPeriodo} no período",
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricaCard(
                        titulo = "Servidores ativos",
                        valor = dashboard.totalServidoresAtivos.toString(),
                        subtitulo = "${dashboard.totalVeiculosAtivos} veículos ativos",
                        modifier = Modifier.weight(1f)
                    )
                    MetricaCard(
                        titulo = "Temporários",
                        valor = dashboard.totalTemporariosVigentes.toString(),
                        subtitulo = "vigentes hoje",
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                SmartParkCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Movimentação por turno",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "${dashboard.totalEntradasPeriodo} entradas em ${dashboard.diasComMovimento} dia(s). " +
                                "A barra compara a média diária de entradas do turno com as ${dashboard.capacidadeTotal} vagas.",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        dashboard.ocupacaoPorTurno.forEach { item ->
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${item.turno.label} (${item.turno.faixaHorario})",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = "${item.totalEntradas} entradas (${(item.percentualLotacao * 100).toInt()}%)",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                LinearProgressIndicator(
                                    progress = { item.percentualLotacao },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }

            item {
                SmartParkCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Uso por tipo de vaga",
                            style = MaterialTheme.typography.titleMedium
                        )
                        dashboard.usoPorTipoVaga.forEachIndexed { index, vaga ->
                            if (index > 0) {
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = vaga.tipoVaga.label, style = MaterialTheme.typography.bodyMedium)
                                Text(
                                    text = "${vaga.ocupadasAgora} ocupadas agora (${vaga.totalRegistros} no período)",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }

    if (mostrarDialogoCapacidade && dados != null) {
        ConfigurarVagasDialog(
            capacidadeTotalAtual = dados!!.capacidadeTotal,
            capacidadeExclusivasAtual = dados!!.capacidadeExclusivas,
            capacidadeEletricasAtual = dados!!.capacidadeCarregamentoEletrico,
            erroGeral = uiState.erroConfiguracao,
            errosCampos = uiState.errosCampos,
            onDismiss = { mostrarDialogoCapacidade = false },
            onSalvar = { total, exclusivas, eletricas ->
                viewModel.atualizarCapacidades(
                    capacidadeTotal = total,
                    capacidadeExclusivas = exclusivas,
                    capacidadeCarregamentoEletrico = eletricas,
                    onSucesso = { mostrarDialogoCapacidade = false }
                )
            }
        )
    }
}

@Composable
private fun ConfigurarVagasDialog(
    capacidadeTotalAtual: Int,
    capacidadeExclusivasAtual: Int,
    capacidadeEletricasAtual: Int,
    erroGeral: String?,
    errosCampos: Map<String, String>,
    onDismiss: () -> Unit,
    onSalvar: (total: Int, exclusivas: Int, eletricas: Int) -> Unit
) {
    var totalTexto by remember { mutableStateOf(capacidadeTotalAtual.toString()) }
    var exclusivasTexto by remember { mutableStateOf(capacidadeExclusivasAtual.toString()) }
    var eletricas by remember { mutableIntStateOf(capacidadeEletricasAtual) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Capacidade e expansão de vagas") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Ajuste as vagas de carregamento de veículos elétricos conforme a expansão do campus (padrão inicial: 2 vagas).",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Carregamento elétrico",
                            style = MaterialTheme.typography.titleSmall
                        )
                        Text(
                            text = "Expanda para mais estações",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilledTonalIconButton(
                            onClick = { if (eletricas > 0) eletricas-- }
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "Diminuir vaga elétrica")
                        }
                        Text(
                            text = eletricas.toString(),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        FilledTonalIconButton(
                            onClick = { eletricas++ }
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Expandir vaga elétrica")
                        }
                    }
                }

                OutlinedTextField(
                    value = totalTexto,
                    onValueChange = { totalTexto = it.filter { c -> c.isDigit() }.take(4) },
                    label = { Text("Capacidade total do pátio") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = errosCampos.containsKey("capacidadeTotal"),
                    supportingText = errosCampos["capacidadeTotal"]?.let { { Text(it) } },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = exclusivasTexto,
                    onValueChange = { exclusivasTexto = it.filter { c -> c.isDigit() }.take(4) },
                    label = { Text("Vagas exclusivas (PCD, Idoso, Oficial)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = errosCampos.containsKey("capacidadeExclusivas"),
                    supportingText = errosCampos["capacidadeExclusivas"]?.let { { Text(it) } },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                erroGeral?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSalvar(
                        totalTexto.toIntOrNull() ?: 0,
                        exclusivasTexto.toIntOrNull() ?: -1,
                        eletricas
                    )
                }
            ) {
                Text("Salvar capacidades")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@Composable
private fun MetricaCard(
    titulo: String,
    valor: String,
    subtitulo: String,
    modifier: Modifier = Modifier
) {
    SmartParkCard(modifier = modifier) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = titulo,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = valor,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = subtitulo,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
