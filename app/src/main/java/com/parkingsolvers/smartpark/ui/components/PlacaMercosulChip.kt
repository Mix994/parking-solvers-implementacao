package com.parkingsolvers.smartpark.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.parkingsolvers.smartpark.compartilhado.util.Formatadores
import com.parkingsolvers.smartpark.ui.theme.ArdosiaAsfalto
import com.parkingsolvers.smartpark.ui.theme.AzulMercosul

/**
 * Componente assinatura do SmartPark: renderiza a placa no formato visual brasileiro
 * (faixa superior azul Mercosul ou faixa cinza para padrão antigo) com tipografia
 * monoespaçada de alto contraste para leitura rápida na portaria.
 */
@Composable
fun PlacaMercosulChip(
    placa: String,
    modifier: Modifier = Modifier,
    destaque: Boolean = false
) {
    val limpa = placa.trim().uppercase().replace("-", "")
    val ehMercosul = limpa.length == 7 && limpa[4].isLetter()
    val formatada = Formatadores.formatarPlaca(limpa)

    val faixaCor = if (ehMercosul) AzulMercosul else Color(0xFF475569)
    val textoFaixa = if (ehMercosul) "BRASIL" else "BR"

    Surface(
        modifier = modifier.widthIn(min = if (destaque) 152.dp else 112.dp),
        shape = RoundedCornerShape(6.dp),
        color = Color.White,
        border = BorderStroke(if (destaque) 1.5.dp else 1.dp, ArdosiaAsfalto)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(faixaCor)
                    .padding(vertical = if (destaque) 2.5.dp else 1.5.dp, horizontal = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = textoFaixa,
                    color = Color.White,
                    fontSize = if (destaque) 9.sp else 7.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.4.sp
                )
            }
            Text(
                text = formatada,
                color = ArdosiaAsfalto,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.ExtraBold,
                fontSize = if (destaque) 22.sp else 15.sp,
                letterSpacing = if (destaque) 2.sp else 1.2.sp,
                modifier = Modifier.padding(
                    horizontal = if (destaque) 12.dp else 8.dp,
                    vertical = if (destaque) 6.dp else 3.dp
                )
            )
        }
    }
}
