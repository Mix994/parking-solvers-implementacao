package com.parkingsolvers.smartpark.dashboard.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Accessible
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.EvStation
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.parkingsolvers.smartpark.dashboard.dto.response.DashboardOcupacaoResponse
import com.parkingsolvers.smartpark.movimentacao.model.TipoVaga
import com.parkingsolvers.smartpark.ui.components.BadgeTone
import com.parkingsolvers.smartpark.ui.components.PageHeader
import com.parkingsolvers.smartpark.ui.components.SmartParkCard
import com.parkingsolvers.smartpark.ui.components.StatusBadge

enum class FiltroCategoriaVaga(val label: String) {
    TODAS("Todas"),
    COMUNS("Comuns"),
    EXCLUSIVAS("PCD e exclusivas"),
    ELETRICAS("Carregamento elétrico")
}

/**
 * Tela dedicada para docentes e servidores conferirem em tempo real quantas vagas
 * estão livres e ocupadas no campus, com filtro por tipo (Comuns, PCD/Exclusivas e Elétricas).
 */
@Composable
fun VagasDisponiveisScreen(
    viewModel: DashboardViewModel
) {
    val dados by viewModel.dashboard.collectAsState()
    var filtro by remember { mutableStateOf(FiltroCategoriaVaga.TODAS) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            PageHeader(
                titulo = "Vagas disponíveis",
                subtitulo = "Confira vagas livres e ocupadas no campus por categoria"
            )
        }

        if (dados == null) {
            item {
                Text(
                    text = "Carregando disponibilidade de vagas...",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        } else {
            val dashboard = dados!!

            item {
                ResumoPatioDocenteBanner(dashboard = dashboard)
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FiltroCategoriaVaga.entries.forEach { opcao ->
                        FilterChip(
                            selected = filtro == opcao,
                            onClick = { filtro = opcao },
                            label = { Text(opcao.label) }
                        )
                    }
                }
            }

            item {
                AnimatedContent(
                    targetState = filtro,
                    transitionSpec = {
                        fadeIn(tween(220, easing = FastOutSlowInEasing)) togetherWith
                            fadeOut(tween(150, easing = FastOutSlowInEasing))
                    },
                    label = "filtro_vagas_docente"
                ) { categoriaAtual ->
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        if (categoriaAtual == FiltroCategoriaVaga.TODAS || categoriaAtual == FiltroCategoriaVaga.COMUNS) {
                            CategoriaVagaCard(
                                titulo = "Vagas comuns",
                                subtitulo = "Estacionamento geral de servidores e visitantes",
                                icone = Icons.Default.DirectionsCar,
                                livres = dashboard.vagasComunsLivresAgora,
                                ocupadas = dashboard.vagasComunsOcupadasAgora,
                                capacidade = dashboard.capacidadeComuns,
                                percentualOcupado = dashboard.percentualComunsAtual
                            )
                        }

                        if (categoriaAtual == FiltroCategoriaVaga.TODAS || categoriaAtual == FiltroCategoriaVaga.EXCLUSIVAS) {
                            val exclusivasDetalhe = dashboard.usoPorTipoVaga.filter { it.tipoVaga.ehExclusiva }
                            CategoriaVagaCard(
                                titulo = "Vagas PCD e exclusivas",
                                subtitulo = "PCD, Idoso e Viaturas Oficiais",
                                icone = Icons.Default.Accessible,
                                livres = dashboard.vagasExclusivasLivresAgora,
                                ocupadas = dashboard.vagasExclusivasOcupadasAgora,
                                capacidade = dashboard.capacidadeExclusivas,
                                percentualOcupado = dashboard.percentualExclusivasAtual,
                                detalhes = exclusivasDetalhe.map {
                                    it.tipoVaga.label to "${it.ocupadasAgora} ocupadas agora"
                                }
                            )
                        }

                        if (categoriaAtual == FiltroCategoriaVaga.TODAS || categoriaAtual == FiltroCategoriaVaga.ELETRICAS) {
                            CategoriaVagaCard(
                                titulo = "Carregamento de veículos elétricos",
                                subtitulo = "Estações de recarga do campus (estrutura expansível)",
                                icone = Icons.Default.EvStation,
                                livres = dashboard.vagasEletricasLivresAgora,
                                ocupadas = dashboard.vagasEletricasOcupadasAgora,
                                capacidade = dashboard.capacidadeCarregamentoEletrico,
                                percentualOcupado = dashboard.percentualEletricasAtual,
                                detalhes = listOf(
                                    TipoVaga.CARREGAMENTO_ELETRICO.label to
                                        "${dashboard.vagasEletricasLivresAgora} estação(ões) livre(s) no momento"
                                )
                            )
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

@Composable
private fun ResumoPatioDocenteBanner(dashboard: DashboardOcupacaoResponse) {
    val progressoAnimado by animateFloatAsState(
        targetValue = dashboard.percentualOcupacaoAtual,
        animationSpec = tween(durationMillis = 450, easing = FastOutSlowInEasing),
        label = "progresso_geral_docente"
    )

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
                        text = "Disponibilidade geral agora",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
                    )
                    Text(
                        text = "${dashboard.vagasLivresAgora} vagas livres",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
                Text(
                    text = "${dashboard.veiculosNoCampusAgora}/${dashboard.capacidadeTotal}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }

            LinearProgressIndicator(
                progress = { progressoAnimado },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp),
                color = MaterialTheme.colorScheme.onPrimary,
                trackColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.25f)
            )

            Text(
                text = "${dashboard.veiculosNoCampusAgora} ocupadas agora (${(dashboard.percentualOcupacaoAtual * 100).toInt()}% de lotação)",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f)
            )
        }
    }
}

@Composable
private fun CategoriaVagaCard(
    titulo: String,
    subtitulo: String,
    icone: ImageVector,
    livres: Int,
    ocupadas: Int,
    capacidade: Int,
    percentualOcupado: Float,
    detalhes: List<Pair<String, String>> = emptyList()
) {
    val lotado = capacidade > 0 && livres == 0
    val progressoAnimado by animateFloatAsState(
        targetValue = percentualOcupado,
        animationSpec = tween(durationMillis = 450, easing = FastOutSlowInEasing),
        label = "progresso_$titulo"
    )

    SmartParkCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = icone,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(text = titulo, style = MaterialTheme.typography.titleSmall)
                        Text(
                            text = subtitulo,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                StatusBadge(
                    texto = if (lotado) "Lotado" else "Disponível",
                    tone = if (lotado) BadgeTone.ALERTA else BadgeTone.SUCESSO
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IndicadorNumero(
                    label = "Livres",
                    valor = livres.toString(),
                    destaque = true
                )
                IndicadorNumero(
                    label = "Ocupadas",
                    valor = ocupadas.toString(),
                    destaque = false
                )
                IndicadorNumero(
                    label = "Capacidade",
                    valor = capacidade.toString(),
                    destaque = false
                )
            }

            LinearProgressIndicator(
                progress = { progressoAnimado },
                modifier = Modifier.fillMaxWidth()
            )

            if (detalhes.isNotEmpty()) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    detalhes.forEach { (nome, info) ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = nome, style = MaterialTheme.typography.bodyMedium)
                            Text(
                                text = info,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun IndicadorNumero(
    label: String,
    valor: String,
    destaque: Boolean
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = valor,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = if (destaque) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
    }
}
