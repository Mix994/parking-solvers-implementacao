package com.parkingsolvers.smartpark.veiculo.ui

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
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.unit.dp
import com.parkingsolvers.smartpark.compartilhado.util.Formatadores
import com.parkingsolvers.smartpark.servidor.dto.response.ServidorResponse
import com.parkingsolvers.smartpark.ui.components.BadgeTone
import com.parkingsolvers.smartpark.ui.components.EmptyState
import com.parkingsolvers.smartpark.ui.components.PageHeader
import com.parkingsolvers.smartpark.ui.components.PlacaMercosulChip
import com.parkingsolvers.smartpark.ui.components.SmartParkCard
import com.parkingsolvers.smartpark.ui.components.StatusBadge
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
    val uiState by viewModel.uiState.collectAsState()
    var mostrarDialogo by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    viewModel.limparErrosFormulario()
                    mostrarDialogo = true
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
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
                    subtitulo = "Veículos permanentes e autorizações temporárias com prazo de validade"
                )
            }

            uiState.mensagemFeedback?.let { msg ->
                item {
                    Text(text = msg, color = MaterialTheme.colorScheme.primary)
                }
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
                    VeiculoCard(
                        veiculo = veiculo,
                        onAlternarAtivo = { viewModel.alternarAtivo(veiculo.id) }
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
}

@Composable
private fun VeiculoCard(
    veiculo: VeiculoResponse,
    onAlternarAtivo: () -> Unit
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
                PlacaMercosulChip(placa = veiculo.placa)
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

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onAlternarAtivo) {
                    Text(if (veiculo.ativo) "Inativar veículo" else "Reativar veículo")
                }
            }
        }
    }
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
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TipoCadastroVeiculo.entries.forEach { tipo ->
                        FilterChip(
                            selected = tipoCadastro == tipo,
                            onClick = { tipoCadastro = tipo },
                            label = { Text(tipo.label) }
                        )
                    }
                }

                if (tipoCadastro == TipoCadastroVeiculo.TEMPORARIO) {
                    Text("Validade temporária a partir de hoje:", style = MaterialTheme.typography.labelMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(1, 3, 7, 15).forEach { dias ->
                            FilterChip(
                                selected = diasValidade == dias,
                                onClick = { diasValidade = dias },
                                label = { Text("${dias}d") }
                            )
                        }
                    }
                }

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
                enabled = servidorSelecionadoId > 0L
            ) {
                Text("Salvar veículo")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
