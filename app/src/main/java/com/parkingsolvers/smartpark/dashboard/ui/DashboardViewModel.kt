package com.parkingsolvers.smartpark.dashboard.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.parkingsolvers.smartpark.dashboard.dto.response.DashboardOcupacaoResponse
import com.parkingsolvers.smartpark.dashboard.model.PeriodoDashboard
import com.parkingsolvers.smartpark.dashboard.service.DashboardService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn

class DashboardViewModel(
    dashboardService: DashboardService
) : ViewModel() {

    private val _periodo = MutableStateFlow(PeriodoDashboard.HOJE)
    val periodo: StateFlow<PeriodoDashboard> = _periodo.asStateFlow()

    val dashboard: StateFlow<DashboardOcupacaoResponse?> = dashboardService.observarDashboard(_periodo)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun selecionarPeriodo(periodo: PeriodoDashboard) {
        _periodo.value = periodo
    }

    companion object {
        fun factory(service: DashboardService): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    DashboardViewModel(service) as T
            }
    }
}
