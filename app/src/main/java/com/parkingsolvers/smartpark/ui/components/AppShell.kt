package com.parkingsolvers.smartpark.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountCircle
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
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
import com.parkingsolvers.smartpark.movimentacao.ui.PortariaScreen
import com.parkingsolvers.smartpark.movimentacao.ui.PortariaViewModel
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

    var mostrarMeusDados by remember { mutableStateOf(false) }
    var placaVisitantePendente by remember { mutableStateOf<String?>(null) }

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
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "${sessao.perfil.label} — ${sessao.nome}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = { mostrarMeusDados = true }) {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = "Meus dados"
                            )
                        }
                        IconButton(onClick = onSair) {
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
                        NavigationBarItem(
                            selected = rotaAtual == aba.rota,
                            onClick = {
                                if (rotaAtual != aba.rota) {
                                    navController.navigate(aba.rota) {
                                        popUpTo(navController.graph.startDestinationId) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = { Icon(aba.icone, contentDescription = aba.label) },
                            label = { Text(aba.label) }
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
                startDestination = AbaNavegacao.rotaInicial(sessao.perfil)
            ) {
                composable(AbaNavegacao.DASHBOARD.rota) {
                    val vm: DashboardViewModel = viewModel(
                        factory = DashboardViewModel.factory(container.dashboardService)
                    )
                    DashboardScreen(viewModel = vm)
                }

                composable(AbaNavegacao.PORTARIA.rota) {
                    val vm: PortariaViewModel = viewModel(
                        factory = PortariaViewModel.factory(
                            veiculoService = container.veiculoService,
                            movimentacaoService = container.movimentacaoService
                        )
                    )
                    PortariaScreen(
                        viewModel = vm,
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

        Box(modifier = Modifier.weight(1f)) {
            if (abaSelecionada == 0) {
                val servidorVm: ServidorViewModel = viewModel(
                    factory = ServidorViewModel.factory(container.servidorService)
                )
                ServidoresScreen(viewModel = servidorVm, mostrarCabecalho = false)
            } else {
                val usuarioVm: UsuarioViewModel = viewModel(
                    factory = UsuarioViewModel.factory(
                        usuarioService = container.usuarioService,
                        servidorService = container.servidorService
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
