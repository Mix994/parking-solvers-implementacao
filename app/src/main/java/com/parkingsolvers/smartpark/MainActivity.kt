package com.parkingsolvers.smartpark

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.parkingsolvers.smartpark.auth.ui.LoginScreen
import com.parkingsolvers.smartpark.auth.ui.LoginViewModel
import com.parkingsolvers.smartpark.ui.components.AppShell
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
                        (fadeIn(tween(280, easing = FastOutSlowInEasing)) +
                            scaleIn(tween(280, easing = FastOutSlowInEasing), initialScale = 0.98f))
                            .togetherWith(fadeOut(tween(180, easing = FastOutSlowInEasing)))
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
