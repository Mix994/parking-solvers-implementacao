package com.parkingsolvers.smartpark.servidor.ui

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
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.parkingsolvers.smartpark.servidor.dto.request.ServidorRequest
import com.parkingsolvers.smartpark.servidor.dto.response.ServidorResponse
import com.parkingsolvers.smartpark.ui.components.BadgeTone
import com.parkingsolvers.smartpark.ui.components.EmptyState
import com.parkingsolvers.smartpark.ui.components.PageHeader
import com.parkingsolvers.smartpark.ui.components.SmartParkCard
import com.parkingsolvers.smartpark.ui.components.StatusBadge

@Composable
fun ServidoresScreen(
    viewModel: ServidorViewModel,
    mostrarCabecalho: Boolean = true
) {
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
                Icon(Icons.Default.Add, contentDescription = "Cadastrar servidor")
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
            if (mostrarCabecalho) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    PageHeader(
                        titulo = "Servidores e docentes",
                        subtitulo = "Cadastro institucional que substitui a planilha compartilhada"
                    )
                }
            } else {
                item { Spacer(modifier = Modifier.height(8.dp)) }
            }

            uiState.mensagemFeedback?.let { msg ->
                item {
                    Text(text = msg, color = MaterialTheme.colorScheme.primary)
                }
            }

            if (servidores.isEmpty()) {
                item {
                    EmptyState(
                        icone = Icons.Default.People,
                        titulo = "Nenhum servidor cadastrado",
                        descricao = "Toque no botão + para cadastrar um novo servidor ou docente."
                    )
                }
            } else {
                items(servidores, key = { it.id }) { servidor ->
                    ServidorCard(
                        servidor = servidor,
                        onAlternarAtivo = { viewModel.alternarAtivo(servidor.id) }
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(84.dp)) }
        }
    }

    if (mostrarDialogo) {
        NovoServidorDialog(
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
private fun ServidorCard(
    servidor: ServidorResponse,
    onAlternarAtivo: () -> Unit
) {
    SmartParkCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = servidor.nome,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                StatusBadge(
                    texto = if (servidor.ativo) "Ativo" else "Inativo",
                    tone = if (servidor.ativo) BadgeTone.SUCESSO else BadgeTone.NEUTRO
                )
            }
            Text(
                text = "Prontuário ${servidor.prontuario} — ${servidor.setor}",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "${servidor.email}  |  ${servidor.telefone}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onAlternarAtivo) {
                    Text(if (servidor.ativo) "Inativar servidor" else "Reativar servidor")
                }
            }
        }
    }
}

@Composable
private fun NovoServidorDialog(
    erroGeral: String?,
    errosCampos: Map<String, String>,
    onDismiss: () -> Unit,
    onConfirmar: (ServidorRequest) -> Unit
) {
    var nome by remember { mutableStateOf("") }
    var prontuario by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var telefone by remember { mutableStateOf("") }
    var setor by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Novo servidor ou docente") },
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
                    value = prontuario,
                    onValueChange = { prontuario = it.uppercase() },
                    label = { Text("Prontuário ou SIAPE") },
                    isError = errosCampos.containsKey("prontuario"),
                    supportingText = errosCampos["prontuario"]?.let { { Text(it) } },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("E-mail institucional") },
                    isError = errosCampos.containsKey("email"),
                    supportingText = errosCampos["email"]?.let { { Text(it) } },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = telefone,
                    onValueChange = { telefone = it },
                    label = { Text("Telefone") },
                    isError = errosCampos.containsKey("telefone"),
                    supportingText = errosCampos["telefone"]?.let { { Text(it) } },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = setor,
                    onValueChange = { setor = it },
                    label = { Text("Setor ou coordenação") },
                    isError = errosCampos.containsKey("setor"),
                    supportingText = errosCampos["setor"]?.let { { Text(it) } },
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
                        ServidorRequest(
                            nome = nome,
                            prontuario = prontuario,
                            email = email,
                            telefone = telefone,
                            setor = setor
                        )
                    )
                }
            ) {
                Text("Salvar servidor")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
