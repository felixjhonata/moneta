package com.felixj.moneta.dashboard.model

import com.felixj.moneta.shared.model.MonetaRoute

sealed interface DashboardPageUserEvent {
    data class NavigateTo(val destination: MonetaRoute) : DashboardPageUserEvent
    data object SeeMoreButtonClick: DashboardPageUserEvent
}