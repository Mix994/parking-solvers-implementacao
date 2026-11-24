package com.parkingsolvers.smartpark.dashboard.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.parkingsolvers.smartpark.dashboard.model.PeriodoDashboard
import com.parkingsolvers.smartpark.ui.components.PageHeader
import com.parkingsolvers.smartpark.ui.components.SmartParkCard

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel
) {
    val dados by viewModel.dashboard.collectAsState()
    val periodo by viewModel.periodo.collectAsState()

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
                subtitulo = "Monitoramento de servidores, visitantes externos, turnos e vagas exclusivas"
            )
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PeriodoDashboard.entries.forEach { opcao ->
                    FilterChip(
                        selected = periodo == opcao,
                        onClick = { viewModel.selecionarPeriodo(opcao) },
                        label = { Text(opcao.label) }
                    )
                }
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
