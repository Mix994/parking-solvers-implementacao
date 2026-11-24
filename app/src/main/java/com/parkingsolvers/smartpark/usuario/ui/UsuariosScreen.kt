package com.parkingsolvers.smartpark.usuario.ui

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
import androidx.compose.material.icons.filled.ManageAccounts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.parkingsolvers.smartpark.auth.model.PerfilUsuario
import com.parkingsolvers.smartpark.servidor.dto.response.ServidorResponse
import com.parkingsolvers.smartpark.ui.components.BadgeTone
import com.parkingsolvers.smartpark.ui.components.EmptyState
import com.parkingsolvers.smartpark.ui.components.PageHeader
import com.parkingsolvers.smartpark.ui.components.SmartParkCard
import com.parkingsolvers.smartpark.ui.components.StatusBadge
import com.parkingsolvers.smartpark.usuario.dto.request.UsuarioRequest
import com.parkingsolvers.smartpark.usuario.dto.response.UsuarioResponse

@Composable
fun UsuariosScreen(
    viewModel: UsuarioViewModel,
    usuarioLogadoId: Long,
    mostrarCabecalho: Boolean = true
) {
    val usuarios by viewModel.usuarios.collectAsState()
    val servidoresSemConta by viewModel.servidoresSemConta.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    var filtroPerfil by remember { mutableStateOf<PerfilUsuario?>(null) }
    var mostrarDialogo by remember { mutableStateOf(false) }

    val usuariosFiltrados = remember(usuarios, filtroPerfil) {
        if (filtroPerfil == null) usuarios else usuarios.filter { it.perfil == filtroPerfil }
    }

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
                Icon(Icons.Default.Add, contentDescription = "Criar conta")
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
                        titulo = "Contas e níveis de acesso",
                        subtitulo = "Administração, portaria e servidores cadastrados no banco local"
                    )
                }
            } else {
                item { Spacer(modifier = Modifier.height(8.dp)) }
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = filtroPerfil == null,
                        onClick = { filtroPerfil = null },
                        label = { Text("Todos (${usuarios.size})") }
                    )
                    PerfilUsuario.entries.forEach { perfil ->
                        FilterChip(
                            selected = filtroPerfil == perfil,
                            onClick = { filtroPerfil = perfil },
                            label = { Text(perfil.label) }
                        )
                    }
                }
            }

            uiState.mensagemFeedback?.let { msg ->
                item {
                    Text(text = msg, color = MaterialTheme.colorScheme.primary)
                }
            }

            uiState.erroGeral?.let { erro ->
                if (!mostrarDialogo) {
                    item {
                        Text(text = erro, color = MaterialTheme.colorScheme.error)
                    }
                }
            }

            if (usuariosFiltrados.isEmpty()) {
                item {
                    EmptyState(
                        icone = Icons.Default.ManageAccounts,
                        titulo = "Nenhuma conta encontrada",
                        descricao = "Toque no botão + para cadastrar um novo usuário."
                    )
                }
            } else {
                items(usuariosFiltrados, key = { it.id }) { usuario ->
                    UsuarioCard(
                        usuario = usuario,
                        ehContaAtual = usuario.id == usuarioLogadoId,
                        onAlternarAtivo = { viewModel.alternarAtivo(usuario.id, usuarioLogadoId) }
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(84.dp)) }
        }
    }

    if (mostrarDialogo) {
        NovoUsuarioDialog(
            servidoresSemConta = servidoresSemConta,
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
private fun UsuarioCard(
    usuario: UsuarioResponse,
    ehContaAtual: Boolean,
    onAlternarAtivo: () -> Unit
) {
    val perfilTone = when (usuario.perfil) {
        PerfilUsuario.ADMIN -> BadgeTone.INFO
        PerfilUsuario.PORTARIA -> BadgeTone.SUCESSO
        PerfilUsuario.SERVIDOR -> BadgeTone.ALERTA
    }

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
                    text = if (ehContaAtual) "${usuario.nome} (você)" else usuario.nome,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    StatusBadge(texto = usuario.perfil.label, tone = perfilTone)
                    StatusBadge(
                        texto = if (usuario.ativo) "Ativo" else "Inativo",
                        tone = if (usuario.ativo) BadgeTone.SUCESSO else BadgeTone.NEUTRO
                    )
                }
            }

            Text(
                text = "Login: ${usuario.login}",
                style = MaterialTheme.typography.bodyMedium
            )
            usuario.servidorProntuario?.let { prontuario ->
                Text(
                    text = "Prontuário vinculado: $prontuario",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (!ehContaAtual) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onAlternarAtivo) {
                        Text(if (usuario.ativo) "Inativar conta" else "Reativar conta")
                    }
                }
            }
        }
    }
}

@Composable
private fun NovoUsuarioDialog(
    servidoresSemConta: List<ServidorResponse>,
    erroGeral: String?,
    errosCampos: Map<String, String>,
    onDismiss: () -> Unit,
    onConfirmar: (UsuarioRequest) -> Unit
) {
    var nome by remember { mutableStateOf("") }
    var login by remember { mutableStateOf("") }
    var senha by remember { mutableStateOf("") }
    var confirmacaoSenha by remember { mutableStateOf("") }
    var perfil by remember { mutableStateOf(PerfilUsuario.PORTARIA) }
    var servidorSelecionadoId by remember {
        mutableLongStateOf(servidoresSemConta.firstOrNull()?.id ?: 0L)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nova conta de acesso") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Nível de acesso:", style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    PerfilUsuario.entries.forEach { opcao ->
                        FilterChip(
                            selected = perfil == opcao,
                            onClick = { perfil = opcao },
                            label = {
                                Text(
                                    when (opcao) {
                                        PerfilUsuario.ADMIN -> "Admin"
                                        PerfilUsuario.PORTARIA -> "Portaria"
                                        PerfilUsuario.SERVIDOR -> "Servidor"
                                    }
                                )
                            }
                        )
                    }
                }

                if (perfil == PerfilUsuario.SERVIDOR) {
                    if (servidoresSemConta.isEmpty()) {
                        Text(
                            text = "Todos os servidores ativos já possuem conta. Cadastre um servidor primeiro na aba Servidores.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    } else {
                        Text("Servidor vinculado:", style = MaterialTheme.typography.labelMedium)
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            servidoresSemConta.forEach { servidor ->
                                FilterChip(
                                    selected = servidorSelecionadoId == servidor.id,
                                    onClick = {
                                        servidorSelecionadoId = servidor.id
                                        nome = servidor.nome
                                        login = servidor.email
                                    },
                                    label = { Text("${servidor.nome} (${servidor.prontuario})") }
                                )
                            }
                        }
                    }
                }

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
                    value = login,
                    onValueChange = { login = it },
                    label = { Text("Login ou e-mail institucional") },
                    isError = errosCampos.containsKey("login"),
                    supportingText = errosCampos["login"]?.let { { Text(it) } },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = senha,
                    onValueChange = { senha = it },
                    label = { Text("Senha (mínimo 6 caracteres)") },
                    visualTransformation = PasswordVisualTransformation(),
                    isError = errosCampos.containsKey("senha"),
                    supportingText = errosCampos["senha"]?.let { { Text(it) } },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = confirmacaoSenha,
                    onValueChange = { confirmacaoSenha = it },
                    label = { Text("Confirmar senha") },
                    visualTransformation = PasswordVisualTransformation(),
                    isError = errosCampos.containsKey("confirmacaoSenha"),
                    supportingText = errosCampos["confirmacaoSenha"]?.let { { Text(it) } },
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
                        UsuarioRequest(
                            nome = nome,
                            login = login,
                            senha = senha,
                            confirmacaoSenha = confirmacaoSenha,
                            perfil = perfil,
                            servidorId = if (perfil == PerfilUsuario.SERVIDOR && servidorSelecionadoId > 0L) {
                                servidorSelecionadoId
                            } else {
                                null
                            }
                        )
                    )
                },
                enabled = perfil != PerfilUsuario.SERVIDOR || servidorSelecionadoId > 0L
            ) {
                Text("Criar conta")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
