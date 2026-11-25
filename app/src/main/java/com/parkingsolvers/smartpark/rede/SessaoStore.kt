package com.parkingsolvers.smartpark.rede

import android.content.Context
import android.content.SharedPreferences
import com.parkingsolvers.smartpark.auth.model.PerfilUsuario
import com.parkingsolvers.smartpark.auth.model.UsuarioSessao

data class CredenciaisSessao(
    val accessToken: String,
    val refreshToken: String,
    val sessao: UsuarioSessao
)

interface SessaoStore {
    fun carregar(): CredenciaisSessao?
    fun salvar(credenciais: CredenciaisSessao)
    fun atualizarSessao(sessao: UsuarioSessao)
    fun limpar()
}

class SharedPreferencesSessaoStore(context: Context) : SessaoStore {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    @Synchronized
    override fun carregar(): CredenciaisSessao? {
        val access = prefs.getString(KEY_ACCESS, null) ?: return null
        val refresh = prefs.getString(KEY_REFRESH, null) ?: return null
        val usuarioId = prefs.getLong(KEY_USER_ID, -1L)
        val nome = prefs.getString(KEY_NOME, null) ?: return null
        val identificacao = prefs.getString(KEY_IDENTIFICACAO, null) ?: return null
        val perfilStr = prefs.getString(KEY_PERFIL, null) ?: return null
        val perfil = runCatching { PerfilUsuario.valueOf(perfilStr) }.getOrNull() ?: return null
        if (usuarioId <= 0L) return null
        val servidorId = if (prefs.contains(KEY_SERVIDOR_ID)) prefs.getLong(KEY_SERVIDOR_ID, -1L).takeIf { it > 0L } else null
        return CredenciaisSessao(
            accessToken = access,
            refreshToken = refresh,
            sessao = UsuarioSessao(usuarioId, nome, identificacao, perfil, servidorId)
        )
    }

    @Synchronized
    override fun salvar(credenciais: CredenciaisSessao) {
        prefs.edit().apply {
            putString(KEY_ACCESS, credenciais.accessToken)
            putString(KEY_REFRESH, credenciais.refreshToken)
            putLong(KEY_USER_ID, credenciais.sessao.usuarioId)
            putString(KEY_NOME, credenciais.sessao.nome)
            putString(KEY_IDENTIFICACAO, credenciais.sessao.identificacao)
            putString(KEY_PERFIL, credenciais.sessao.perfil.name)
            if (credenciais.sessao.servidorId != null) {
                putLong(KEY_SERVIDOR_ID, credenciais.sessao.servidorId)
            } else {
                remove(KEY_SERVIDOR_ID)
            }
            apply()
        }
    }

    @Synchronized
    override fun atualizarSessao(sessao: UsuarioSessao) {
        val atual = carregar() ?: return
        salvar(atual.copy(sessao = sessao))
    }

    @Synchronized
    override fun limpar() {
        prefs.edit().clear().apply()
    }

    companion object {
        private const val PREFS_NAME = "smartpark_sessao"
        private const val KEY_ACCESS = "access_token"
        private const val KEY_REFRESH = "refresh_token"
        private const val KEY_USER_ID = "usuario_id"
        private const val KEY_NOME = "nome"
        private const val KEY_IDENTIFICACAO = "identificacao"
        private const val KEY_PERFIL = "perfil"
        private const val KEY_SERVIDOR_ID = "servidor_id"
    }
}

class MemoriaSessaoStore : SessaoStore {
    @Volatile
    private var atual: CredenciaisSessao? = null

    override fun carregar(): CredenciaisSessao? = atual
    override fun salvar(credenciais: CredenciaisSessao) {
        atual = credenciais
    }

    override fun atualizarSessao(sessao: UsuarioSessao) {
        atual = atual?.copy(sessao = sessao)
    }

    override fun limpar() {
        atual = null
    }
}
