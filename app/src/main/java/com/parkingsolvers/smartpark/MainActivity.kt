package com.parkingsolvers.smartpark

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.parkingsolvers.smartpark.auth.ui.LoginScreen
import com.parkingsolvers.smartpark.auth.ui.LoginViewModel
import com.parkingsolvers.smartpark.ui.components.AppShell
import com.parkingsolvers.smartpark.ui.components.SmartParkMotion
import com.parkingsolvers.smartpark.ui.theme.ParkingSolversTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val container = (application as SmartParkApplication).container

        setContent {
            ParkingSolversTheme {
                val sessao by container.authService.sessaoAtual.collectAsState()

                AnimatedContent(
                    targetState = sessao,
                    transitionSpec = {
                        (fadeIn(tween(340, easing = SmartParkMotion.CurvaPadrao)) +
                            scaleIn(tween(340, easing = SmartParkMotion.CurvaPadrao), initialScale = 0.96f))
                            .togetherWith(
                                fadeOut(tween(200, easing = SmartParkMotion.CurvaPadrao)) +
                                    scaleOut(tween(200, easing = SmartParkMotion.CurvaPadrao), targetScale = 1.02f)
                            )
                    },
                    label = "transicao_sessao"
                ) { usuarioLogado ->
                    if (usuarioLogado == null) {
                        val loginViewModel: LoginViewModel = viewModel(
                            factory = LoginViewModel.factory(
                                authService = container.authService
                            )
                        )
                        LoginScreen(viewModel = loginViewModel)
                    } else {
                        AppShell(
                            sessao = usuarioLogado,
                            container = container,
                            onSair = { container.authService.sair() }
                        )
                    }
                }
            }
        }
    }
}
