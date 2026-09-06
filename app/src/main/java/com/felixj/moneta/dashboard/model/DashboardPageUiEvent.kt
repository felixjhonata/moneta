package com.felixj.moneta.dashboard.model

import com.felixj.moneta.shared.model.MonetaRoute

sealed interface DashboardPageUiEvent {
    data class NavigateTo(val destination: MonetaRoute, val clearBackStack: Boolean = false): DashboardPageUiEvent
}