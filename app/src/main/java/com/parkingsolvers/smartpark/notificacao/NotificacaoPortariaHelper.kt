package com.parkingsolvers.smartpark.notificacao

import android.Manifest
import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.parkingsolvers.smartpark.MainActivity
import com.parkingsolvers.smartpark.R
import com.parkingsolvers.smartpark.compartilhado.util.Formatadores
import com.parkingsolvers.smartpark.rede.EventoTempoReal

object NotificacaoPortariaHelper {

    const val CANAL_MONITORAMENTO = "smartpark_monitoramento_ativo"
    const val CANAL_MOVIMENTACOES = "smartpark_movimentacoes"
    const val ID_NOTIFICACAO_SERVICO = 1001
    const val EXTRA_ABRIR_PORTARIA = "abrir_portaria"

    fun garantirCanais(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return

        val canalMonitoramento = NotificationChannel(
            CANAL_MONITORAMENTO,
            "Monitoramento da Portaria",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Mantém o recebimento em tempo real de entradas e saídas fora do aplicativo."
            setShowBadge(false)
            setSound(null, null)
            enableVibration(false)
        }

        val canalMovimentacoes = NotificationChannel(
            CANAL_MOVIMENTACOES,
            "Entradas e saídas na guarita",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Alertas imediatos quando um veículo entra ou sai do estacionamento."
            enableVibration(true)
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        }

        manager.createNotificationChannel(canalMonitoramento)
        manager.createNotificationChannel(canalMovimentacoes)
    }

    fun temPermissaoNotificacao(context: Context): Boolean {
        val habilitadoNoSistema = NotificationManagerCompat.from(context).areNotificationsEnabled()
        if (!habilitadoNoSistema) return false
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun criarNotificacaoMonitoramento(context: Context, conectado: Boolean): Notification {
        garantirCanais(context)

        val abrirPortariaIntent = PendingIntent.getActivity(
            context,
            100,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra(EXTRA_ABRIR_PORTARIA, true)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val pausarIntent = PendingIntent.getService(
            context,
            101,
            Intent(context, MonitoramentoPortariaService::class.java).apply {
                action = MonitoramentoPortariaService.ACTION_PAUSAR
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val textoStatus = if (conectado) {
            "Recebendo entradas e saídas em tempo real"
        } else {
            "Reconectando à portaria..."
        }

        return NotificationCompat.Builder(context, CANAL_MONITORAMENTO)
            .setSmallIcon(R.drawable.ic_stat_smartpark)
            .setContentTitle("Portaria SmartPark ativa")
            .setContentText(textoStatus)
            .setOngoing(true)
            .setSilent(true)
            .setOnlyAlertOnce(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .setContentIntent(abrirPortariaIntent)
            .addAction(0, "Pausar", pausarIntent)
            .build()
    }

    @SuppressLint("MissingPermission")
    fun atualizarNotificacaoMonitoramento(context: Context, conectado: Boolean) {
        if (!temPermissaoNotificacao(context)) return
        runCatching {
            NotificationManagerCompat.from(context)
                .notify(ID_NOTIFICACAO_SERVICO, criarNotificacaoMonitoramento(context, conectado))
        }
    }

    @SuppressLint("MissingPermission")
    fun exibirAlertaMovimentacao(context: Context, evento: EventoTempoReal) {
        garantirCanais(context)
        if (!temPermissaoNotificacao(context)) return

        val placaFormatada = evento.placa
            ?.takeIf { it.isNotBlank() }
            ?.let { Formatadores.formatarPlaca(it) }
            ?: "Veículo"

        val titulo = when (evento.tipo) {
            EventoTempoReal.TIPO_MOVIMENTACAO_ENTRADA ->
                if (evento.simulacaoServidor) "Entrada simulada — $placaFormatada"
                else "Entrada no pátio — $placaFormatada"
            EventoTempoReal.TIPO_MOVIMENTACAO_SAIDA ->
                if (evento.simulacaoServidor) "Saída simulada — $placaFormatada"
                else "Saída registrada — $placaFormatada"
            EventoTempoReal.TIPO_VISITANTE_ENTRADA -> "Visitante entrou — $placaFormatada"
            EventoTempoReal.TIPO_VISITANTE_SAIDA -> "Visitante saiu — $placaFormatada"
            else -> return
        }

        val detalhes = listOfNotNull(
            evento.responsavel?.takeIf { it.isNotBlank() },
            evento.modeloCor?.takeIf { it.isNotBlank() },
            evento.tipoVagaLabel?.takeIf { it.isNotBlank() }
        ).joinToString(" • ").ifBlank { "Movimentação atualizada na portaria." }

        val abrirPortariaIntent = PendingIntent.getActivity(
            context,
            200 + (evento.id % 1000).toInt(),
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra(EXTRA_ABRIR_PORTARIA, true)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificacao = NotificationCompat.Builder(context, CANAL_MOVIMENTACOES)
            .setSmallIcon(R.drawable.ic_stat_smartpark)
            .setContentTitle(titulo)
            .setContentText(detalhes)
            .setStyle(NotificationCompat.BigTextStyle().bigText(detalhes))
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setGroup("smartpark_movimentacoes_group")
            .setContentIntent(abrirPortariaIntent)
            .build()

        val notificationId = 2000 + (evento.id % 1000).toInt()
        try {
            NotificationManagerCompat.from(context).notify(notificationId, notificacao)
        } catch (_: SecurityException) {
            // Permissão revogada entre a checagem e a postagem.
        }
    }
}
