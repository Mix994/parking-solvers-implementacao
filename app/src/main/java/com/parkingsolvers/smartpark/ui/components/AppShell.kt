package com.parkingsolvers.smartpark.ui.components

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextOverflow
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.parkingsolvers.smartpark.AppContainer
import com.parkingsolvers.smartpark.auth.model.PerfilUsuario
import com.parkingsolvers.smartpark.auth.model.UsuarioSessao
import com.parkingsolvers.smartpark.dashboard.ui.DashboardScreen
import com.parkingsolvers.smartpark.dashboard.ui.DashboardViewModel
import com.parkingsolvers.smartpark.dashboard.ui.VagasDisponiveisScreen
import com.parkingsolvers.smartpark.movimentacao.ui.PortariaScreen
import com.parkingsolvers.smartpark.movimentacao.ui.PortariaViewModel
import com.parkingsolvers.smartpark.notificacao.NotificacaoPortariaHelper
import com.parkingsolvers.smartpark.servidor.ui.ServidorViewModel
import com.parkingsolvers.smartpark.servidor.ui.ServidoresScreen
import com.parkingsolvers.smartpark.ui.navigation.AbaNavegacao
import com.parkingsolvers.smartpark.usuario.ui.UsuarioViewModel
import com.parkingsolvers.smartpark.usuario.ui.UsuariosScreen
import com.parkingsolvers.smartpark.veiculo.ui.VeiculoViewModel
import com.parkingsolvers.smartpark.veiculo.ui.VeiculosScreen
import com.parkingsolvers.smartpark.visitante.ui.VisitanteViewModel
import com.parkingsolvers.smartpark.visitante.ui.VisitantesScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppShell(
    sessao: UsuarioSessao,
    container: AppContainer,
    onSair: () -> Unit
) {
    val navController = rememberNavController()
    val abas = AbaNavegacao.paraPerfil(sessao.perfil)
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val rotaAtual = navBackStackEntry?.destination?.route
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val ehPortariaOuAdmin = sessao.perfil == PerfilUsuario.PORTARIA || sessao.perfil == PerfilUsuario.ADMIN
    val alertasForaDoAppAtivos by container.preferenciasMonitoramento.alertasForaDoAppAtivo.collectAsState()
    val abrirPortariaPendente by container.abrirPortariaPendente.collectAsState()

    var permissaoNotificacaoConcedida by remember {
        mutableStateOf(NotificacaoPortariaHelper.temPermissaoNotificacao(context))
    }

    val launcherPermissaoNotificacao = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { concedida ->
        permissaoNotificacaoConcedida = NotificacaoPortariaHelper.temPermissaoNotificacao(context)
        if (concedida) {
            container.preferenciasMonitoramento.definirAlertasAtivos(true)
        }
        container.atualizarEstadoTempoReal()
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                val atual = NotificacaoPortariaHelper.temPermissaoNotificacao(context)
                if (atual != permissaoNotificacaoConcedida) {
                    permissaoNotificacaoConcedida = atual
                    container.atualizarEstadoTempoReal()
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(sessao.usuarioId, ehPortariaOuAdmin) {
        if (ehPortariaOuAdmin &&
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            !permissaoNotificacaoConcedida &&
            !container.preferenciasMonitoramento.jaSolicitouPermissaoNotificacao()
        ) {
            container.preferenciasMonitoramento.marcarPermissaoNotificacaoSolicitada()
            launcherPermissaoNotificacao.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    LaunchedEffect(abrirPortariaPendente) {
        if (abrirPortariaPendente) {
            if (AbaNavegacao.PORTARIA.perfisPermitidos.contains(sessao.perfil) &&
                rotaAtual != AbaNavegacao.PORTARIA.rota
            ) {
                navController.navigate(AbaNavegacao.PORTARIA.rota) {
                    popUpTo(navController.graph.startDestinationId) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
            }
            container.consumirAberturaPortaria()
        }
    }

    fun alternarAlertasForaDoApp() {
        if (!permissaoNotificacaoConcedida) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val jaSolicitou = container.preferenciasMonitoramento.jaSolicitouPermissaoNotificacao()
                val activity = context.encontrarActivity()
                val deveAbrirConfig = jaSolicitou &&
                    activity != null &&
                    !ActivityCompat.shouldShowRequestPermissionRationale(
                        activity,
                        Manifest.permission.POST_NOTIFICATIONS
                    )
                if (deveAbrirConfig) {
                    abrirConfiguracoesNotificacao(context)
                } else {
                    container.preferenciasMonitoramento.marcarPermissaoNotificacaoSolicitada()
                    launcherPermissaoNotificacao.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            } else {
                abrirConfiguracoesNotificacao(context)
            }
            return
        }

        container.preferenciasMonitoramento.definirAlertasAtivos(!alertasForaDoAppAtivos)
        container.atualizarEstadoTempoReal()
    }

    var mostrarMeusDados by remember { mutableStateOf(false) }
    var placaVisitantePendente by remember { mutableStateOf<String?>(null) }
    var girosAtualizar by remember { mutableIntStateOf(0) }
    val rotacaoAtualizar by animateFloatAsState(
        targetValue = girosAtualizar * 360f,
        animationSpec = tween(500, easing = SmartParkMotion.CurvaPadrao),
        label = "rotacao_atualizar"
    )

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    title = {
                        Column {
                            Text(
                                text = "SmartPark",
                                style = MaterialTheme.typography.titleMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${sessao.perfil.label} — ${sessao.nome}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    },
                    actions = {
                        val refreshInteraction = remember { MutableInteractionSource() }
                        val alertaInteraction = remember { MutableInteractionSource() }
                        val perfilInteraction = remember { MutableInteractionSource() }
                        val sairInteraction = remember { MutableInteractionSource() }
                        if (ehPortariaOuAdmin) {
                            val alertasLigados = alertasForaDoAppAtivos && permissaoNotificacaoConcedida
                            IconButton(
                                onClick = { alternarAlertasForaDoApp() },
                                interactionSource = alertaInteraction,
                                modifier = Modifier.escalaAoPressionar(alertaInteraction, 0.9f)
                            ) {
                                Icon(
                                    imageVector = if (alertasLigados) {
                                        Icons.Default.NotificationsActive
                                    } else {
                                        Icons.Default.NotificationsOff
                                    },
                                    contentDescription = if (alertasLigados) {
                                        "Alertas fora do aplicativo ativados"
                                    } else {
                                        "Alertas fora do aplicativo desativados"
                                    },
                                    tint = if (alertasLigados) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    }
                                )
                            }
                        }
                        IconButton(
                            onClick = {
                                girosAtualizar++
                                container.sincronizador.notificarMudanca()
                            },
                            interactionSource = refreshInteraction,
                            modifier = Modifier.escalaAoPressionar(refreshInteraction, 0.9f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Atualizar dados",
                                modifier = Modifier.graphicsLayer { rotationZ = rotacaoAtualizar }
                            )
                        }
                        IconButton(
                            onClick = { mostrarMeusDados = true },
                            interactionSource = perfilInteraction,
                            modifier = Modifier.escalaAoPressionar(perfilInteraction, 0.9f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = "Meus dados"
                            )
                        }
                        IconButton(
                            onClick = onSair,
                            interactionSource = sairInteraction,
                            modifier = Modifier.escalaAoPressionar(sairInteraction, 0.9f)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Logout,
                                contentDescription = "Sair"
                            )
                        }
                    }
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            }
        },
        bottomBar = {
            if (abas.size > 1) {
                NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                    abas.forEach { aba ->
                        val selecionada = rotaAtual == aba.rota
                        val escalaIcone by animateFloatAsState(
                            targetValue = if (selecionada) 1.12f else 1f,
                            animationSpec = SmartParkMotion.SpringToque,
                            label = "escala_aba_${aba.rota}"
                        )
                        val tabInteraction = remember { MutableInteractionSource() }
                        NavigationBarItem(
                            selected = selecionada,
                            onClick = {
                                if (!selecionada) {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    navController.navigate(aba.rota) {
                                        popUpTo(navController.graph.startDestinationId) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            interactionSource = tabInteraction,
                            icon = {
                                Icon(
                                    imageVector = aba.icone,
                                    contentDescription = aba.label,
                                    modifier = Modifier.graphicsLayer {
                                        scaleX = escalaIcone
                                        scaleY = escalaIcone
                                    }
                                )
                            },
                            label = {
                                Text(
                                    text = aba.label,
                                    maxLines = 1,
                                    softWrap = false,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            modifier = Modifier.escalaAoPressionar(tabInteraction, 0.95f)
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            NavHost(
                navController = navController,
                startDestination = AbaNavegacao.rotaInicial(sessao.perfil),
                enterTransition = {
                    val origem = abas.indexOfFirst { it.rota == initialState.destination.route }
                    val destino = abas.indexOfFirst { it.rota == targetState.destination.route }
                    val direcao = if (origem >= 0 && destino >= 0 && destino < origem) -1 else 1
                    fadeIn(tween(300, easing = SmartParkMotion.CurvaPadrao)) +
                        slideInHorizontally(tween(300, easing = SmartParkMotion.CurvaPadrao)) { direcao * (it / 10) }
                },
                exitTransition = {
                    val origem = abas.indexOfFirst { it.rota == initialState.destination.route }
                    val destino = abas.indexOfFirst { it.rota == targetState.destination.route }
                    val direcao = if (origem >= 0 && destino >= 0 && destino < origem) -1 else 1
                    fadeOut(tween(220, easing = SmartParkMotion.CurvaPadrao)) +
                        slideOutHorizontally(tween(220, easing = SmartParkMotion.CurvaPadrao)) { -direcao * (it / 14) }
                },
                popEnterTransition = {
                    fadeIn(tween(300, easing = SmartParkMotion.CurvaPadrao)) +
                        slideInHorizontally(tween(300, easing = SmartParkMotion.CurvaPadrao)) { -it / 10 }
                },
                popExitTransition = {
                    fadeOut(tween(220, easing = SmartParkMotion.CurvaPadrao)) +
                        slideOutHorizontally(tween(220, easing = SmartParkMotion.CurvaPadrao)) { it / 14 }
                }
            ) {
                composable(AbaNavegacao.VAGAS.rota) {
                    val vm: DashboardViewModel = viewModel(
                        factory = DashboardViewModel.factory(
                            dashboardService = container.dashboardService,
                            configuracaoService = container.configuracaoEstacionamentoService
                        )
                    )
                    VagasDisponiveisScreen(viewModel = vm)
                }

                composable(AbaNavegacao.DASHBOARD.rota) {
                    val vm: DashboardViewModel = viewModel(
                        factory = DashboardViewModel.factory(
                            dashboardService = container.dashboardService,
                            configuracaoService = container.configuracaoEstacionamentoService
                        )
                    )
                    DashboardScreen(
                        viewModel = vm,
                        podeConfigurarVagas = sessao.perfil == PerfilUsuario.ADMIN
                    )
                }

                composable(AbaNavegacao.PORTARIA.rota) {
                    val vm: PortariaViewModel = viewModel(
                        factory = PortariaViewModel.factory(
                            veiculoService = container.veiculoService,
                            movimentacaoService = container.movimentacaoService,
                            clienteTempoReal = container.clienteTempoReal
                        )
                    )
                    PortariaScreen(
                        viewModel = vm,
                        alertasForaDoAppAtivos = alertasForaDoAppAtivos,
                        permissaoNotificacaoConcedida = permissaoNotificacaoConcedida,
                        onAlternarAlertasForaDoApp = ::alternarAlertasForaDoApp,
                        onRegistrarComoVisitante = { placa ->
                            placaVisitantePendente = placa
                            navController.navigate(AbaNavegacao.VISITANTES.rota) {
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }

                composable(AbaNavegacao.VISITANTES.rota) {
                    val vm: VisitanteViewModel = viewModel(
                        key = "visitantes_${sessao.usuarioId}",
                        factory = VisitanteViewModel.factory(
                            visitanteService = container.visitanteService,
                            operadorNome = sessao.nome
                        )
                    )
                    VisitantesScreen(
                        viewModel = vm,
                        placaInicialParaCadastro = placaVisitantePendente,
                        onConsumirPlacaInicial = { placaVisitantePendente = null }
                    )
                }

                composable(AbaNavegacao.VEICULOS.rota) {
                    val servidorFiltro = if (sessao.perfil == PerfilUsuario.SERVIDOR) {
                        sessao.servidorId
                    } else {
                        null
                    }
                    val vm: VeiculoViewModel = viewModel(
                        key = "veiculos_${servidorFiltro ?: "todos"}",
                        factory = VeiculoViewModel.factory(
                            veiculoService = container.veiculoService,
                            servidorService = container.servidorService,
                            movimentacaoService = container.movimentacaoService,
                            dashboardService = container.dashboardService,
                            servidorIdFiltro = servidorFiltro
                        )
                    )
                    VeiculosScreen(viewModel = vm, servidorIdFixo = servidorFiltro)
                }

                composable(AbaNavegacao.GESTAO.rota) {
                    GestaoAdminScreen(
                        container = container,
                        usuarioLogadoId = sessao.usuarioId
                    )
                }
            }
        }
    }

    if (mostrarMeusDados) {
        MeusDadosDialog(
            sessao = sessao,
            usuarioService = container.usuarioService,
            authService = container.authService,
            onDismiss = { mostrarMeusDados = false },
            onSalvo = { mostrarMeusDados = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GestaoAdminScreen(
    container: AppContainer,
    usuarioLogadoId: Long
) {
    var abaSelecionada by remember { mutableIntStateOf(0) }
    val titulos = listOf("Servidores", "Contas de acesso")

    Column(modifier = Modifier.fillMaxSize()) {
        PrimaryTabRow(selectedTabIndex = abaSelecionada) {
            titulos.forEachIndexed { indice, titulo ->
                Tab(
                    selected = abaSelecionada == indice,
                    onClick = { abaSelecionada = indice },
                    text = { Text(titulo) }
                )
            }
        }

        AnimatedContent(
            targetState = abaSelecionada,
            modifier = Modifier.weight(1f),
            transitionSpec = {
                val direcao = if (targetState > initialState) 1 else -1
                (fadeIn(tween(280, easing = SmartParkMotion.CurvaPadrao)) +
                    slideInHorizontally(tween(280, easing = SmartParkMotion.CurvaPadrao)) { direcao * (it / 10) })
                    .togetherWith(
                        fadeOut(tween(200, easing = SmartParkMotion.CurvaPadrao)) +
                            slideOutHorizontally(tween(200, easing = SmartParkMotion.CurvaPadrao)) { -direcao * (it / 14) }
                    )
            },
            label = "gestao_tabs"
        ) { aba ->
            if (aba == 0) {
                val servidorVm: ServidorViewModel = viewModel(
                    factory = ServidorViewModel.factory(container.servidorService)
                )
                ServidoresScreen(viewModel = servidorVm, mostrarCabecalho = false)
            } else {
                val usuarioVm: UsuarioViewModel = viewModel(
                    factory = UsuarioViewModel.factory(
                        usuarioService = container.usuarioService,
                        servidorService = container.servidorService,
                        authService = container.authService
                    )
                )
                UsuariosScreen(
                    viewModel = usuarioVm,
                    usuarioLogadoId = usuarioLogadoId,
                    mostrarCabecalho = false
                )
            }
        }
    }
}

private tailrec fun Context.encontrarActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.encontrarActivity()
    else -> null
}

private fun abrirConfiguracoesNotificacao(context: Context) {
    val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
            putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
        }
    } else {
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = android.net.Uri.fromParts("package", context.packageName, null)
        }
    }.apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    runCatching { context.startActivity(intent) }
}
