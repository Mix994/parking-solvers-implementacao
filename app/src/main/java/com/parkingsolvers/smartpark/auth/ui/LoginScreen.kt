package com.parkingsolvers.smartpark.auth.ui

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalParking
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.parkingsolvers.smartpark.auth.model.PerfilUsuario
import com.parkingsolvers.smartpark.dev.DadosIniciais
import com.parkingsolvers.smartpark.servidor.dto.response.ServidorResponse
import com.parkingsolvers.smartpark.ui.components.BadgeTone
import com.parkingsolvers.smartpark.ui.components.SmartParkCard
import com.parkingsolvers.smartpark.ui.components.StatusBadge
import com.parkingsolvers.smartpark.usuario.dto.response.UsuarioResponse

@Composable
fun LoginScreen(
    viewModel: LoginViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val usuariosAtivos by viewModel.usuariosAtivos.collectAsState()
    val servidoresSemConta by viewModel.servidoresSemConta.collectAsState()

    var senhaVisivel by remember { mutableStateOf(false) }
    var mostrarDialogoCadastro by remember { mutableStateOf(false) }

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp, vertical = 18.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.primary
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.16f),
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalParking,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = "SmartPark",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Text(
                            text = "Gestão de estacionamento e portaria do campus",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.88f)
                        )
                    }
                }
            }

            SmartParkCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Entrar no sistema",
                        style = MaterialTheme.typography.titleMedium
                    )

                    OutlinedTextField(
                        value = uiState.login,
                        onValueChange = viewModel::atualizarLogin,
                        label = { Text("E-mail institucional ou prontuário") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                        isError = uiState.errosCampos.containsKey("login"),
                        supportingText = uiState.errosCampos["login"]?.let { { Text(it) } },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = uiState.senha,
                        onValueChange = viewModel::atualizarSenha,
                        label = { Text("Senha") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                        trailingIcon = {
                            IconButton(onClick = { senhaVisivel = !senhaVisivel }) {
                                Icon(
                                    imageVector = if (senhaVisivel) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = if (senhaVisivel) "Ocultar senha" else "Mostrar senha"
                                )
                            }
                        },
                        visualTransformation = if (senhaVisivel) VisualTransformation.None else PasswordVisualTransformation(),
                        isError = uiState.errosCampos.containsKey("senha"),
                        supportingText = uiState.errosCampos["senha"]?.let { { Text(it) } },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(onDone = { viewModel.entrar() }),
                        modifier = Modifier.fillMaxWidth()
                    )

                    uiState.erroGeral?.let { erro ->
                        Text(
                            text = erro,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    uiState.mensagemFeedback?.let { msg ->
                        Text(
                            text = msg,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Button(
                        onClick = viewModel::entrar,
                        enabled = !uiState.carregando,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (uiState.carregando) "Autenticando..." else "Entrar")
                    }

                    OutlinedButton(
                        onClick = {
                            viewModel.limparErrosCadastro()
                            mostrarDialogoCadastro = true
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Criar nova conta")
                    }
                }
            }

            if (usuariosAtivos.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Contas no banco local",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "Toque em uma conta para preencher o formulário e testar cada nível de acesso.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    usuariosAtivos.forEach { usuario ->
                        ContaDemoRow(
                            usuario = usuario,
                            onClick = { viewModel.preencherCredencialDemo(usuario) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }

    if (mostrarDialogoCadastro) {
        NovaContaDialog(
            servidoresSemConta = servidoresSemConta,
            erroGeral = uiState.erroCadastro,
            errosCampos = uiState.errosCadastroCampos,
            onDismiss = { mostrarDialogoCadastro = false },
            onConfirmar = { nome, login, senha, confirmacao, perfil, servidorExistenteId, prontuario, telefone, setor ->
                viewModel.cadastrarConta(
                    nome = nome,
                    login = login,
                    senha = senha,
                    confirmacaoSenha = confirmacao,
                    perfil = perfil,
                    servidorExistenteId = servidorExistenteId,
                    prontuarioNovo = prontuario,
                    telefoneNovo = telefone,
                    setorNovo = setor,
                    onSucesso = { mostrarDialogoCadastro = false }
                )
            }
        )
    }
}

@Composable
private fun ContaDemoRow(
    usuario: UsuarioResponse,
    onClick: () -> Unit
) {
    val badgeTone = when (usuario.perfil) {
        PerfilUsuario.ADMIN -> BadgeTone.INFO
        PerfilUsuario.PORTARIA -> BadgeTone.SUCESSO
        PerfilUsuario.SERVIDOR -> BadgeTone.ALERTA
    }
    val dicaSenha = DadosIniciais.senhaDemoParaPerfil(usuario.perfil)

    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
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
                    text = usuario.nome,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = usuario.login,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Senha padrão: $dicaSenha",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            StatusBadge(texto = usuario.perfil.label, tone = badgeTone)
        }
    }
}

@Composable
private fun NovaContaDialog(
    servidoresSemConta: List<ServidorResponse>,
    erroGeral: String?,
    errosCampos: Map<String, String>,
    onDismiss: () -> Unit,
    onConfirmar: (
        nome: String,
        login: String,
        senha: String,
        confirmacaoSenha: String,
        perfil: PerfilUsuario,
        servidorExistenteId: Long?,
        prontuario: String,
        telefone: String,
        setor: String
    ) -> Unit
) {
    var nome by remember { mutableStateOf("") }
    var login by remember { mutableStateOf("") }
    var senha by remember { mutableStateOf("") }
    var confirmacaoSenha by remember { mutableStateOf("") }
    var perfil by remember { mutableStateOf(PerfilUsuario.SERVIDOR) }
    var servidorExistenteId by remember { mutableLongStateOf(0L) }
    var prontuario by remember { mutableStateOf("") }
    var telefone by remember { mutableStateOf("") }
    var setor by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Criar nova conta") },
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

                if (perfil == PerfilUsuario.SERVIDOR && servidoresSemConta.isNotEmpty()) {
                    Text("Vincular a servidor existente:", style = MaterialTheme.typography.labelMedium)
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        FilterChip(
                            selected = servidorExistenteId == 0L,
                            onClick = { servidorExistenteId = 0L },
                            label = { Text("Cadastrar novo servidor") }
                        )
                        servidoresSemConta.forEach { s ->
                            FilterChip(
                                selected = servidorExistenteId == s.id,
                                onClick = {
                                    servidorExistenteId = s.id
                                    nome = s.nome
                                    login = s.email
                                },
                                label = { Text("${s.nome} (${s.prontuario})") }
                            )
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
                    label = { Text("E-mail institucional ou login") },
                    isError = errosCampos.containsKey("login") || errosCampos.containsKey("email"),
                    supportingText = (errosCampos["login"] ?: errosCampos["email"])?.let { { Text(it) } },
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

                if (perfil == PerfilUsuario.SERVIDOR && servidorExistenteId == 0L) {
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
                    onConfirmar(
                        nome,
                        login,
                        senha,
                        confirmacaoSenha,
                        perfil,
                        if (servidorExistenteId > 0L) servidorExistenteId else null,
                        prontuario,
                        telefone,
                        setor
                    )
                }
            ) {
                Text("Criar conta")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
