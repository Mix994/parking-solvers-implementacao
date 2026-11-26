package com.parkingsolvers.smartpark.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.IntSize

object SmartParkMotion {
    val CurvaPadrao = CubicBezierEasing(0.2f, 0.0f, 0.0f, 1.0f)

    val SpringToque = spring<Float>(
        dampingRatio = 0.65f,
        stiffness = Spring.StiffnessMedium
    )

    val SpringTamanho = spring<IntSize>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMediumLow
    )
}

@Composable
fun Modifier.escalaAoPressionar(
    interactionSource: InteractionSource,
    escalaPressionado: Float = 0.96f
): Modifier {
    val pressionado by interactionSource.collectIsPressedAsState()
    val escala by animateFloatAsState(
        targetValue = if (pressionado) escalaPressionado else 1f,
        animationSpec = SmartParkMotion.SpringToque,
        label = "escala_toque"
    )
    return graphicsLayer {
        scaleX = escala
        scaleY = escala
    }
}

@Composable
fun FeedbackAnimado(
    mensagem: String?,
    ehErro: Boolean = false,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = !mensagem.isNullOrBlank(),
        enter = fadeIn(tween(240, easing = SmartParkMotion.CurvaPadrao)) +
            expandVertically(tween(240, easing = SmartParkMotion.CurvaPadrao)),
        exit = fadeOut(tween(180, easing = SmartParkMotion.CurvaPadrao)) +
            shrinkVertically(tween(180, easing = SmartParkMotion.CurvaPadrao)),
        modifier = modifier
    ) {
        Text(
            text = mensagem.orEmpty(),
            style = MaterialTheme.typography.bodyMedium,
            color = if (ehErro) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
        )
    }
}
