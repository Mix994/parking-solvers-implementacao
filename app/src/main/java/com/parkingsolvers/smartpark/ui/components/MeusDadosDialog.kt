package com.parkingsolvers.smartpark.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.parkingsolvers.smartpark.auth.model.UsuarioSessao
import com.parkingsolvers.smartpark.auth.service.AuthService
import com.parkingsolvers.smartpark.compartilhado.exceptions.RegraDeNegocioException
import com.parkingsolvers.smartpark.compartilhado.exceptions.ValidacaoException
import com.parkingsolvers.smartpark.usuario.dto.request.AtualizarUsuarioRequest
import com.parkingsolvers.smartpark.usuario.service.UsuarioService
import kotlinx.coroutines.launch

/**
 * Modal global "Meus Dados" (análogo ao `meus-dados-modal.tsx` / UC01 do CarToon),
 * acessível por todos os perfis autenticados a partir da barra superior.
 */
@Composable
fun MeusDadosDialog(
    sessao: UsuarioSessao,
    usuarioService: UsuarioService,
    authService: AuthService,
    onDismiss: () -> Unit,
    onSalvo: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var nome by remember { mutableStateOf(sessao.nome) }
    var login by remember { mutableStateOf(sessao.identificacao) }
    var novaSenha by remember { mutableStateOf("") }
    var confirmacaoSenha by remember { mutableStateOf("") }
    var erroGeral by remember { mutableStateOf<String?>(null) }
    var errosCampos by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var salvando by remember { mutableStateOf(false) }

    LaunchedEffect(sessao.usuarioId) {
        try {
            val conta = usuarioService.buscarPorId(sessao.usuarioId)
            nome = conta.nome
            login = conta.login
        } catch (_: RegraDeNegocioException) {
            // Mantém valores da sessão
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Meus dados") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatusBadge(texto = sessao.perfil.label, tone = BadgeTone.INFO)

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
                    value = novaSenha,
                    onValueChange = { novaSenha = it },
                    label = { Text("Nova senha (opcional)") },
                    visualTransformation = PasswordVisualTransformation(),
                    isError = errosCampos.containsKey("novaSenha"),
                    supportingText = errosCampos["novaSenha"]?.let { { Text(it) } },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                if (novaSenha.isNotEmpty()) {
                    OutlinedTextField(
                        value = confirmacaoSenha,
                        onValueChange = { confirmacaoSenha = it },
                        label = { Text("Confirmar nova senha") },
                        visualTransformation = PasswordVisualTransformation(),
                        isError = errosCampos.containsKey("confirmacaoNovaSenha"),
                        supportingText = errosCampos["confirmacaoNovaSenha"]?.let { { Text(it) } },
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
                enabled = !salvando,
                onClick = {
                    scope.launch {
                        salvando = true
                        erroGeral = null
                        errosCampos = emptyMap()
                        try {
                            val atualizado = usuarioService.atualizarProprioPerfil(
                                usuarioId = sessao.usuarioId,
                                request = AtualizarUsuarioRequest(
                                    nome = nome,
                                    login = login,
                                    novaSenha = novaSenha,
                                    confirmacaoNovaSenha = confirmacaoSenha
                                )
                            )
                            authService.sincronizarSessao(atualizado)
                            onSalvo()
                        } catch (e: ValidacaoException) {
                            errosCampos = e.errosValidacao
                            erroGeral = e.message
                        } catch (e: RegraDeNegocioException) {
                            erroGeral = e.message
                        } finally {
                            salvando = false
                        }
                    }
                }
            ) {
                Text(if (salvando) "Salvando..." else "Salvar alterações")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
