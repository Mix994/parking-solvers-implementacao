package com.parkingsolvers.smartpark.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.ManageAccounts
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.ui.graphics.vector.ImageVector
import com.parkingsolvers.smartpark.auth.model.PerfilUsuario

enum class AbaNavegacao(
    val rota: String,
    val label: String,
    val icone: ImageVector,
    val perfisPermitidos: Set<PerfilUsuario>
) {
    DASHBOARD(
        rota = "dashboard",
        label = "Painel",
        icone = Icons.Default.BarChart,
        perfisPermitidos = setOf(PerfilUsuario.ADMIN, PerfilUsuario.PORTARIA)
    ),
    PORTARIA(
        rota = "portaria",
        label = "Portaria",
        icone = Icons.Default.VerifiedUser,
        perfisPermitidos = setOf(PerfilUsuario.ADMIN, PerfilUsuario.PORTARIA)
    ),
    VISITANTES(
        rota = "visitantes",
        label = "Visitantes",
        icone = Icons.Default.Badge,
        perfisPermitidos = setOf(PerfilUsuario.ADMIN, PerfilUsuario.PORTARIA)
    ),
    VEICULOS(
        rota = "veiculos",
        label = "Veículos",
        icone = Icons.Default.DirectionsCar,
        perfisPermitidos = setOf(PerfilUsuario.ADMIN, PerfilUsuario.PORTARIA, PerfilUsuario.SERVIDOR)
    ),
    GESTAO(
        rota = "gestao",
        label = "Gestão",
        icone = Icons.Default.ManageAccounts,
        perfisPermitidos = setOf(PerfilUsuario.ADMIN)
    );

    companion object {
        fun paraPerfil(perfil: PerfilUsuario): List<AbaNavegacao> =
            entries.filter { perfil in it.perfisPermitidos }

        fun rotaInicial(perfil: PerfilUsuario): String = when (perfil) {
            PerfilUsuario.ADMIN -> DASHBOARD.rota
            PerfilUsuario.PORTARIA -> PORTARIA.rota
            PerfilUsuario.SERVIDOR -> VEICULOS.rota
        }
    }
}
