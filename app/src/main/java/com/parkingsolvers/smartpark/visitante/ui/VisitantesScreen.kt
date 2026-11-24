package com.parkingsolvers.smartpark.visitante.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.parkingsolvers.smartpark.compartilhado.util.Formatadores
import com.parkingsolvers.smartpark.movimentacao.model.TipoVaga
import com.parkingsolvers.smartpark.ui.components.BadgeTone
import com.parkingsolvers.smartpark.ui.components.EmptyState
import com.parkingsolvers.smartpark.ui.components.PageHeader
import com.parkingsolvers.smartpark.ui.components.PlacaMercosulChip
import com.parkingsolvers.smartpark.ui.components.SmartParkCard
import com.parkingsolvers.smartpark.ui.components.StatusBadge
import com.parkingsolvers.smartpark.visitante.dto.request.VisitanteRequest
import com.parkingsolvers.smartpark.visitante.dto.response.VisitanteResponse

@Composable
fun VisitantesScreen(
    viewModel: VisitanteViewModel,
    placaInicialParaCadastro: String? = null,
    onConsumirPlacaInicial: () -> Unit = {}
) {
    val noCampus by viewModel.noCampus.collectAsState()
    val historico by viewModel.historico.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    var mostrarDialogo by remember { mutableStateOf(false) }
    var placaPrePreenchida by remember { mutableStateOf("") }

    LaunchedEffect(placaInicialParaCadastro) {
        if (!placaInicialParaCadastro.isNullOrBlank()) {
            placaPrePreenchida = placaInicialParaCadastro.uppercase()
            viewModel.limparErrosFormulario()
            mostrarDialogo = true
            onConsumirPlacaInicial()
        }
    }

    val termo = uiState.termoBusca.trim()
    val noCampusFiltrados = remember(noCampus, termo) {
        if (termo.isBlank()) noCampus else noCampus.filter { combinaBusca(it, termo) }
    }
    val encerradosFiltrados = remember(historico, termo) {
        historico.filter { !it.noCampus && (termo.isBlank() || combinaBusca(it, termo)) }.take(15)
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    placaPrePreenchida = ""
                    viewModel.limparErrosFormulario()
                    mostrarDialogo = true
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Registrar visitante")
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
                    titulo = "Visitantes externos",
                    subtitulo = "Controle de entrada e saída de convidados, outros campi e prestadores de serviço"
                )
            }

            item {
                OutlinedTextField(
                    value = uiState.termoBusca,
                    onValueChange = viewModel::atualizarBusca,
                    label = { Text("Buscar por nome, instituição ou placa") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
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

            uiState.erroGeral?.let { erro ->
                if (!mostrarDialogo) {
                    item {
                        Text(
                            text = erro,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            item {
                Text(
                    text = "No campus agora (${noCampusFiltrados.size})",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            if (noCampusFiltrados.isEmpty()) {
                item {
                    EmptyState(
                        icone = Icons.Default.Badge,
                        titulo = "Nenhum visitante no pátio",
                        descricao = "Toque no botão + para registrar a entrada de um visitante externo."
                    )
                }
            } else {
                items(noCampusFiltrados, key = { it.id }) { visitante ->
                    VisitanteAtivoCard(
                        visitante = visitante,
                        onRegistrarSaida = { viewModel.registrarSaida(visitante.id) }
                    )
                }
            }

            item {
                Text(
                    text = "Visitas encerradas",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            if (encerradosFiltrados.isEmpty()) {
                item {
                    Text(
                        text = "Nenhuma visita encerrada registrada ainda.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                items(encerradosFiltrados, key = { "hist_${it.id}" }) { visitante ->
                    VisitanteHistoricoItem(visitante = visitante)
                }
            }

            item { Spacer(modifier = Modifier.height(84.dp)) }
        }
    }

    if (mostrarDialogo) {
        NovoVisitanteDialog(
            placaInicial = placaPrePreenchida,
            erroGeral = uiState.erroGeral,
            errosCampos = uiState.errosCampos,
            onDismiss = { mostrarDialogo = false },
            onConfirmar = { request ->
                viewModel.registrarEntrada(request) {
                    mostrarDialogo = false
                }
            }
        )
    }
}

private fun combinaBusca(visitante: VisitanteResponse, termo: String): Boolean {
    val q = termo.lowercase()
    return visitante.nome.lowercase().contains(q) ||
        visitante.instituicaoOrigem.lowercase().contains(q) ||
        visitante.placa.lowercase().contains(q.replace("-", "")) ||
        visitante.setorDestino.lowercase().contains(q)
}

@Composable
private fun VisitanteAtivoCard(
    visitante: VisitanteResponse,
    onRegistrarSaida: () -> Unit
) {
    SmartParkCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                PlacaMercosulChip(placa = visitante.placa)
                StatusBadge(
                    texto = visitante.tipoVaga.label,
                    tone = if (visitante.tipoVaga.ehExclusiva) BadgeTone.ALERTA else BadgeTone.INFO
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = visitante.nome,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = visitante.instituicaoOrigem,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Destino: ${visitante.setorDestino}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Motivo: ${visitante.motivoVisita}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${visitante.modeloVeiculo} (${visitante.corVeiculo})  |  Doc: ${visitante.documento}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Entrada às ${Formatadores.formatarHora(visitante.dataHoraEntradaMillis)} (${visitante.turno.label})",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedButton(onClick = onRegistrarSaida) {
                    Text("Dar baixa na saída")
                }
            }
        }
    }
}

@Composable
private fun VisitanteHistoricoItem(visitante: VisitanteResponse) {
    SmartParkCard(
        modifier = Modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = visitante.nome,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "${visitante.instituicaoOrigem} (${Formatadores.formatarPlaca(visitante.placa)})",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Setor: ${visitante.setorDestino}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = Formatadores.formatarData(visitante.dataHoraEntradaMillis),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${Formatadores.formatarHora(visitante.dataHoraEntradaMillis)} às " +
                        (visitante.dataHoraSaidaMillis?.let { Formatadores.formatarHora(it) } ?: "--:--"),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun NovoVisitanteDialog(
    placaInicial: String,
    erroGeral: String?,
    errosCampos: Map<String, String>,
    onDismiss: () -> Unit,
    onConfirmar: (VisitanteRequest) -> Unit
) {
    var nome by remember { mutableStateOf("") }
    var documento by remember { mutableStateOf("") }
    var instituicaoOrigem by remember { mutableStateOf("") }
    var telefone by remember { mutableStateOf("") }
    var placa by remember(placaInicial) { mutableStateOf(placaInicial) }
    var modeloVeiculo by remember { mutableStateOf("") }
    var corVeiculo by remember { mutableStateOf("") }
    var motivoVisita by remember { mutableStateOf("") }
    var setorDestino by remember { mutableStateOf("") }
    var tipoVaga by remember { mutableStateOf(TipoVaga.COMUM) }
    var observacao by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Registrar visitante externo") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = nome,
                    onValueChange = { nome = it },
                    label = { Text("Nome completo") },
                    isError = errosCampos.containsKey("nome"),
                    supportingText = errosCampos["nome"]?.let { { Text(it) } },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = documento,
                    onValueChange = { documento = it },
                    label = { Text("CPF ou RG") },
                    isError = errosCampos.containsKey("documento"),
                    supportingText = errosCampos["documento"]?.let { { Text(it) } },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = instituicaoOrigem,
                    onValueChange = { instituicaoOrigem = it },
                    label = { Text("Instituição, cidade ou empresa de origem") },
                    isError = errosCampos.containsKey("instituicaoOrigem"),
                    supportingText = errosCampos["instituicaoOrigem"]?.let { { Text(it) } },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = telefone,
                    onValueChange = { telefone = it },
                    label = { Text("Telefone de contato") },
                    isError = errosCampos.containsKey("telefone"),
                    supportingText = errosCampos["telefone"]?.let { { Text(it) } },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = placa,
                    onValueChange = { placa = it.uppercase().take(8) },
                    label = { Text("Placa do veículo (AAA-1234 ou ABC1D23)") },
                    isError = errosCampos.containsKey("placa"),
                    supportingText = errosCampos["placa"]?.let { { Text(it) } },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = modeloVeiculo,
                        onValueChange = { modeloVeiculo = it },
                        label = { Text("Modelo") },
                        isError = errosCampos.containsKey("modeloVeiculo"),
                        supportingText = errosCampos["modeloVeiculo"]?.let { { Text(it) } },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = corVeiculo,
                        onValueChange = { corVeiculo = it },
                        label = { Text("Cor") },
                        isError = errosCampos.containsKey("corVeiculo"),
                        supportingText = errosCampos["corVeiculo"]?.let { { Text(it) } },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
                OutlinedTextField(
                    value = setorDestino,
                    onValueChange = { setorDestino = it },
                    label = { Text("Setor ou pessoa visitada no campus") },
                    isError = errosCampos.containsKey("setorDestino"),
                    supportingText = errosCampos["setorDestino"]?.let { { Text(it) } },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = motivoVisita,
                    onValueChange = { motivoVisita = it },
                    label = { Text("Motivo da visita") },
                    isError = errosCampos.containsKey("motivoVisita"),
                    supportingText = errosCampos["motivoVisita"]?.let { { Text(it) } },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Tipo de vaga:", style = MaterialTheme.typography.labelMedium)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    TipoVaga.entries.forEach { opcao ->
                        FilterChip(
                            selected = tipoVaga == opcao,
                            onClick = { tipoVaga = opcao },
                            label = { Text(opcao.label) }
                        )
                    }
                }

                OutlinedTextField(
                    value = observacao,
                    onValueChange = { observacao = it },
                    label = { Text("Observação (opcional)") },
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
                    onConfirmar(
                        VisitanteRequest(
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
                            observacao = observacao
                        )
                    )
                }
            ) {
                Text("Registrar entrada")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
