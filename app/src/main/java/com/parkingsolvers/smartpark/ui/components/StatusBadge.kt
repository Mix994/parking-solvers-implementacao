package com.parkingsolvers.smartpark.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.parkingsolvers.smartpark.ui.theme.AzulContainer
import com.parkingsolvers.smartpark.ui.theme.AzulSecundario
import com.parkingsolvers.smartpark.ui.theme.CarmimBloqueio
import com.parkingsolvers.smartpark.ui.theme.CarmimContainer
import com.parkingsolvers.smartpark.ui.theme.OcreContainer
import com.parkingsolvers.smartpark.ui.theme.OcreSinal
import com.parkingsolvers.smartpark.ui.theme.VerdeCampusContainer
import com.parkingsolvers.smartpark.ui.theme.VerdeCampusEscuro

enum class BadgeTone {
    SUCESSO,
    ALERTA,
    ERRO,
    INFO,
    NEUTRO
}

@Composable
fun StatusBadge(
    texto: String,
    tone: BadgeTone,
    modifier: Modifier = Modifier
) {
    val (bg, fg) = when (tone) {
        BadgeTone.SUCESSO -> VerdeCampusContainer to VerdeCampusEscuro
        BadgeTone.ALERTA -> OcreContainer to OcreSinal
        BadgeTone.ERRO -> CarmimContainer to CarmimBloqueio
        BadgeTone.INFO -> AzulContainer to AzulSecundario
        BadgeTone.NEUTRO -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(6.dp),
        color = bg,
        border = BorderStroke(1.dp, fg.copy(alpha = 0.22f))
    ) {
        Text(
            text = texto,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = fg,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}
