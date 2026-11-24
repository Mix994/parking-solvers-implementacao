package com.parkingsolvers.smartpark.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = VerdeCampusClaro,
    onPrimary = OnVerdeCampusContainer,
    primaryContainer = VerdeCampusEscuro,
    onPrimaryContainer = VerdeCampusContainer,
    secondary = AzulContainer,
    tertiary = OcreContainer,
    background = FundoEscuro,
    onBackground = Color(0xFFE7EEEA),
    surface = SuperficieEscura,
    onSurface = Color(0xFFE7EEEA),
    surfaceVariant = Color(0xFF202C27),
    onSurfaceVariant = Color(0xFFA8B7B0),
    outline = Color(0xFF43544C),
    outlineVariant = BordaEscura,
    error = Color(0xFFFFB4AB),
    errorContainer = Color(0xFF5C1111)
)

private val LightColorScheme = lightColorScheme(
    primary = VerdeCampus,
    onPrimary = Color.White,
    primaryContainer = VerdeCampusContainer,
    onPrimaryContainer = OnVerdeCampusContainer,
    secondary = AzulSecundario,
    onSecondary = Color.White,
    secondaryContainer = AzulContainer,
    tertiary = OcreSinal,
    tertiaryContainer = OcreContainer,
    background = PedraPatio,
    onBackground = ArdosiaAsfalto,
    surface = PapelCartao,
    onSurface = ArdosiaAsfalto,
    surfaceVariant = Color(0xFFE6ECE8),
    onSurfaceVariant = TextoSecundario,
    outline = Color(0xFF8A9A92),
    outlineVariant = BordaSutil,
    error = CarmimBloqueio,
    errorContainer = CarmimContainer
)

@Composable
fun ParkingSolversTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
