package com.parkingsolvers.smartpark.movimentacao.model

enum class Turno(val label: String, val faixaHorario: String) {
    MANHA("Manhã", "06h às 11h59"),
    TARDE("Tarde", "12h às 17h59"),
    NOITE("Noite", "18h às 05h59")
}
