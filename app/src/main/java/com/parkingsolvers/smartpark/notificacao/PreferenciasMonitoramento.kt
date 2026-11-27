package com.parkingsolvers.smartpark.notificacao

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Armazena preferências locais de monitoramento fora da guarita (alertas em segundo plano)
 * e controle para solicitar a permissão POST_NOTIFICATIONS apenas uma vez automaticamente.
 */
class PreferenciasMonitoramento(context: Context) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _alertasForaDoAppAtivo = MutableStateFlow(
        prefs.getBoolean(KEY_ALERTAS_FORA_DO_APP, true)
    )
    val alertasForaDoAppAtivo: StateFlow<Boolean> = _alertasForaDoAppAtivo.asStateFlow()

    fun definirAlertasAtivos(ativo: Boolean) {
        prefs.edit().putBoolean(KEY_ALERTAS_FORA_DO_APP, ativo).apply()
        _alertasForaDoAppAtivo.value = ativo
    }

    fun jaSolicitouPermissaoNotificacao(): Boolean =
        prefs.getBoolean(KEY_PERMISSAO_SOLICITADA, false)

    fun marcarPermissaoNotificacaoSolicitada() {
        prefs.edit().putBoolean(KEY_PERMISSAO_SOLICITADA, true).apply()
    }

    companion object {
        private const val PREFS_NAME = "smartpark_monitoramento"
        private const val KEY_ALERTAS_FORA_DO_APP = "alertas_fora_do_app"
        private const val KEY_PERMISSAO_SOLICITADA = "permissao_notificacao_solicitada"
    }
}
