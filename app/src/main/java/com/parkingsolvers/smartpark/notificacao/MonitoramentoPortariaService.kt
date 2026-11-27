package com.parkingsolvers.smartpark.notificacao

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import com.parkingsolvers.smartpark.SmartParkApplication
import com.parkingsolvers.smartpark.auth.model.PerfilUsuario
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Mantém a escuta SSE ativa quando o porteiro ou administrador sai do app / da guarita,
 * permitindo disparar notificações imediatas de entrada e saída no aparelho.
 */
class MonitoramentoPortariaService : Service() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var observacaoJob: Job? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val app = applicationContext as? SmartParkApplication ?: run {
            stopSelf()
            return START_NOT_STICKY
        }
        val container = app.container

        if (intent?.action == ACTION_PAUSAR) {
            container.preferenciasMonitoramento.definirAlertasAtivos(false)
            container.atualizarEstadoTempoReal()
            stopSelf()
            return START_NOT_STICKY
        }

        val sessao = container.authService.sessaoAtual.value
        val perfilPermitido = sessao?.perfil == PerfilUsuario.PORTARIA || sessao?.perfil == PerfilUsuario.ADMIN
        val ativo = container.preferenciasMonitoramento.alertasForaDoAppAtivo.value
        if (!perfilPermitido || !ativo || !NotificacaoPortariaHelper.temPermissaoNotificacao(this)) {
            stopSelf()
            return START_NOT_STICKY
        }

        val tipoForeground = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
        } else {
            0
        }

        val notificacaoInicial = NotificacaoPortariaHelper.criarNotificacaoMonitoramento(
            context = this,
            conectado = container.clienteTempoReal.conectado.value
        )

        val iniciouForeground = runCatching {
            ServiceCompat.startForeground(
                this,
                NotificacaoPortariaHelper.ID_NOTIFICACAO_SERVICO,
                notificacaoInicial,
                tipoForeground
            )
        }.isSuccess

        if (!iniciouForeground) {
            stopSelf()
            return START_NOT_STICKY
        }

        container.clienteTempoReal.solicitarConexao(ORIGEM_SERVICO)

        if (observacaoJob?.isActive != true) {
            observacaoJob = scope.launch {
                launch {
                    container.clienteTempoReal.conectado.collectLatest { conectado ->
                        NotificacaoPortariaHelper.atualizarNotificacaoMonitoramento(
                            this@MonitoramentoPortariaService,
                            conectado
                        )
                    }
                }
                launch {
                    container.authService.sessaoAtual.collectLatest { atual ->
                        val permitido = atual?.perfil == PerfilUsuario.PORTARIA ||
                            atual?.perfil == PerfilUsuario.ADMIN
                        if (!permitido) {
                            stopSelf()
                        }
                    }
                }
            }
        }

        return START_STICKY
    }

    override fun onDestroy() {
        observacaoJob?.cancel()
        scope.cancel()
        (applicationContext as? SmartParkApplication)
            ?.container
            ?.clienteTempoReal
            ?.liberarConexao(ORIGEM_SERVICO)
        super.onDestroy()
    }

    companion object {
        const val ACTION_INICIAR = "com.parkingsolvers.smartpark.action.INICIAR_MONITORAMENTO"
        const val ACTION_PAUSAR = "com.parkingsolvers.smartpark.action.PAUSAR_MONITORAMENTO"
        private const val ORIGEM_SERVICO = "servico_portaria"

        fun iniciar(context: Context) {
            runCatching {
                val intent = Intent(context, MonitoramentoPortariaService::class.java).apply {
                    action = ACTION_INICIAR
                }
                ContextCompat.startForegroundService(context, intent)
            }
        }

        fun parar(context: Context) {
            runCatching {
                context.stopService(Intent(context, MonitoramentoPortariaService::class.java))
            }
        }
    }
}
